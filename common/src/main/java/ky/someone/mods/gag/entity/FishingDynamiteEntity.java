package ky.someone.mods.gag.entity;

import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.item.ItemRegistry;
import ky.someone.mods.gag.network.FishsplosionPacket;
import ky.someone.mods.gag.platform.PlatformInvokers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishingDynamiteEntity extends AbstractDynamiteEntity {

	public static final TagKey<EntityType<?>> FISH_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, GAGUtil.id("fishing_dynamite_fish"));

	public FishingDynamiteEntity(EntityType<? extends FishingDynamiteEntity> type, Level level) {
		super(type, level);
	}

	public FishingDynamiteEntity(double x, double y, double z, Level level) {
		super(EntityTypeRegistry.FISHING_DYNAMITE.get(), x, y, z, level);
	}

	public FishingDynamiteEntity(LivingEntity owner, Level level) {
		super(EntityTypeRegistry.FISHING_DYNAMITE.get(), owner, level);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		// add some smoke particles above the entity to make it look nicer
		level.addParticle(ParticleTypes.DRIPPING_WATER,
				getX(-vec3.x) + random.nextDouble() * 0.6 - 0.3,
				getY(-vec3.y) + random.nextDouble() * getBbHeight(),
				getZ(-vec3.z) + random.nextDouble() * 0.6 - 0.3,
				vec3.x, vec3.y, vec3.z
		);
		if (isInWater()) {
			// this dynamite is a good swimmer
			setDeltaMovement(getDeltaMovement().scale(1.2));
		}
	}

	@Override
	public void detonate(Vec3 pos) {
		var r = GAGConfig.Dynamite.FISHING_RADIUS.get();
		var explosion = new Fishsplosion(level, this, pos.x, pos.y, pos.z, r);
		if (!PlatformInvokers.explosionPre(level, explosion)) {
			explosion.explode();

			explosion.finalizeExplosion(false);
			for (Player player : level.players()) {
				if (player.distanceToSqr(this) < 4096.0D) {
					new FishsplosionPacket(explosion).sendTo((ServerPlayer) player);
				}
			}
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ItemRegistry.FISHING_DYNAMITE.get();
	}

	public static class Fishsplosion extends Explosion {

		private int fishHit = 0;

		public final Vec3 pos = new Vec3(this.x, this.y, this.z);

		private final boolean isInWater;

		public Fishsplosion(Level level, @Nullable Entity entity, double x, double y, double z, float radius) {
			super(level, entity, null, null, x, y, z, radius, false, BlockInteraction.NONE);

			var eps = 0.028;
			isInWater = BlockPos.betweenClosedStream(AABB.ofSize(pos, eps, eps, eps))
					.anyMatch(pos -> level.getFluidState(pos).is(FluidTags.WATER));
		}

		/**
		 * Unlike with the mining dynamite, we *only* want to hurt entities,
		 * specifically fish (as well as other entities if configured)
		 */
		@Override
		public void explode() {
			if (!isInWater) {
				return; // in reality i wanna add like a little puddle of water where the dynamite was, but idk that didn't work out when i tried it
			}

			var hitbox = AABB.unitCubeFromLowerCorner(pos).inflate(radius);
			for (var entity : this.level.getEntitiesOfClass(LivingEntity.class, hitbox)) {
				double distSqr = entity.distanceToSqr(pos);
				if (distSqr > radius * radius) continue;
				if (entity.isInWater() && !entity.ignoreExplosion()) {
					var filter = GAGConfig.Dynamite.FISHING_TARGET_FILTER.get();
					if (GAGConfig.Dynamite.FISHING_INSTAKILL_FISH.get() && filter.isFish(entity)) {
						fishHit++;
						entity.hurt(this.getDamageSource(), Float.MAX_VALUE);
					} else {
						var relDist = Math.sqrt(distSqr) / radius;
						double seen = getSeenPercent(pos, entity);
						double damageFactor = (1.0 - relDist) * seen;
						var damage = (float) ((int) (damageFactor * damageFactor + damageFactor) / 2.0 * 7.0 * radius + 1.0);

						// fish take double damage
						if (filter.isFish(entity)) {
							fishHit++;
							entity.hurt(this.getDamageSource(), damage * 2);
							damage *= 2;
						} else if (GAGConfig.Dynamite.FISHING_DAMAGE_ALL.get()) {
							entity.hurt(this.getDamageSource(), damage / 2);
						}
					}
				}
			}
		}

		@Override
		public void finalizeExplosion(boolean isClient) {
			if (isClient) {
				var random = level.random;

				if (isInWater) {
					// make a cylinder of particles up to the surface
					var particleCount = (int) (radius * 4);
					List<Vec3> points = new ArrayList<>();
					for (int i = 0; i < particleCount; i++) {
						var angle = random.nextDouble() * Math.PI * 2;
						var dist = random.nextDouble() * radius;
						var x = pos.x + Math.cos(angle) * dist;
						var z = pos.z + Math.sin(angle) * dist;
						points.add(new Vec3(x, 0, z));
					}

					var cur = new BlockPos(pos).above();
					while (level.isInWorldBounds(cur) && !level.getFluidState(cur).isEmpty()) {
						for (var p : points) {
							level.addParticle(ParticleTypes.BUBBLE, p.x, cur.getY() + 0.5, p.z, 0, 0.1, 0);
						}
						cur = cur.above();
					}

					// add some more particles and play a (quieter) boom sound once it hits the surface
					for (int i = 0; i < particleCount * 4; i++) {
						var angle = random.nextDouble() * Math.PI * 2;
						var dist = random.nextDouble() * radius;
						var x = pos.x + Math.cos(angle) * dist;
						var y = cur.getY() + 0.5;
						var z = pos.z + Math.sin(angle) * dist;
						level.addParticle(ParticleTypes.FISHING, x, y, z, 0, 0.05, 0);
						level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.2, 0);
						level.addParticle(ParticleTypes.SPLASH, x, y, z, 0, 0.1, 0);
					}

					level.playLocalSound(cur.getX(), cur.getY(), cur.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.3F, 1.5F, false);
				} else {
					// if the dynamite did not hit water, just add a small splash at the position it landed
					for (int i = 0; i < 20; i++) {
						level.addParticle(ParticleTypes.FISHING,
								pos.x + random.nextGaussian(), pos.y + random.nextFloat(), pos.z + random.nextGaussian(),
								0, 0.05, 0);
					}
					level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.6F, (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F, false);
				}
			}

			if (isInWater) {
				level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.6F, 1.0F);

				if (!isClient) {
					var add = GAGConfig.Dynamite.ADDITIONAL_FISHING_LOOT.get();
					var fishDropped = fishHit;
					List<ItemStack> itemsToDrop = new ArrayList<>();

					LootContext lootParams = new LootContext.Builder((ServerLevel) level)
							.withParameter(LootContextParams.ORIGIN, pos)
							.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
							.withOptionalParameter(LootContextParams.THIS_ENTITY, source)
							.create(LootContextParamSets.FISHING);
					LootTable lootTable = level.getServer().getLootTables().get(BuiltInLootTables.FISHING_FISH);

					// attempt to drop additional fish, with a lower chance to drop fish the more fish we've hit
					for (int i = 0; i < add; i++) {
						// only ever drop up to 1.5x the wanted amount of fish
						if (fishDropped > 1.5 * add) {
							break;
						}

						// use a logistic function to calculate the chance to drop fish
						// (roughly 1 at 0, 0.5 at 0.75 * `add` and 0 at `add` fish dropped each)
						double chance = 1.0 / (1.0 + Math.exp(fishDropped - 0.75 * add));

						if (level.random.nextDouble() < chance) {
							lootTable.getRandomItems(lootParams, itemsToDrop::add);
							fishDropped++;
						}
					}

					for (ItemStack itemStack : itemsToDrop) {
						level.addFreshEntity(new ItemEntity(level, x, y, z, itemStack));
					}
				}
			}
		}
	}
}

