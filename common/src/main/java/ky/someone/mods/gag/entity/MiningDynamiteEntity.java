package ky.someone.mods.gag.entity;

import com.google.common.collect.Sets;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.item.ItemRegistry;
import ky.someone.mods.gag.platform.PlatformInvokers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class MiningDynamiteEntity extends AbstractDynamiteEntity {

	public static final TagKey<Block> MINING_DYNAMITE_EFFECTIVE = TagKey.create(Registry.BLOCK_REGISTRY, GAGUtil.id("mining_dynamite_effective"));

	public MiningDynamiteEntity(EntityType<? extends MiningDynamiteEntity> type, Level level) {
		super(type, level);
	}

	public MiningDynamiteEntity(double x, double y, double z, Level level) {
		super(EntityTypeRegistry.MINING_DYNAMITE.get(), x, y, z, level);
	}

	public MiningDynamiteEntity(LivingEntity owner, Level level) {
		super(EntityTypeRegistry.MINING_DYNAMITE.get(), owner, level);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		// add some smoke particles above the entity to make it look nicer
		this.level.addParticle(ParticleTypes.SMOKE,
				getX(-vec3.x) + random.nextDouble() * 0.6 - 0.3,
				getY(-vec3.y) + random.nextDouble() * getBbHeight(),
				getZ(-vec3.z) + random.nextDouble() * 0.6 - 0.3,
				vec3.x, vec3.y, vec3.z
		);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitEntity) {
		super.onHitEntity(hitEntity);
		if (GAGConfig.Dynamite.MINING_GIVES_HASTE.get() && hitEntity.getEntity() instanceof LivingEntity entity) {
			entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 160, 1, false, false));
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 1, false, false));
		}
	}

	@Override
	public void detonate(Vec3 pos) {
		var r = GAGConfig.Dynamite.MINING_RADIUS.get();
		var explosion = new BlockMiningExplosion(this.level, this, pos.x, pos.y, pos.z, r);
		if (!PlatformInvokers.explosionPre(level, explosion)) {
			explosion.explode();

			explosion.finalizeExplosion(false);
			for (Player player : level.players()) {
				if (player.distanceToSqr(this) < 4096.0D) {
					((ServerPlayer) player).connection.send(new ClientboundExplodePacket(pos.x, pos.y, pos.z, r, explosion.getToBlow(), null));
				}
			}
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ItemRegistry.MINING_DYNAMITE.get();
	}

	private static class BlockMiningExplosion extends Explosion {
		public BlockMiningExplosion(Level level, @Nullable Entity entity, double x, double y, double z, float radius) {
			super(level, entity, null, new ExplosionDamageCalculator() {
				@Override
				public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
					if (!fluidState.isEmpty()) {
						return Optional.empty();
					}

					var orig = super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);

					return orig.map(f -> {
						if (blockState.is(MINING_DYNAMITE_EFFECTIVE)) {
							return f * 0.75f;
						}
						return f;
					});
				}

				@Override
				public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
					return blockState.getFluidState().isEmpty() && super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
				}
			}, x, y, z, radius, false, BlockInteraction.BREAK);
		}

		/**
		 * Does the first part of the explosion (destroy blocks)
		 * <p>
		 * Note: For now, this is just a copy of the vanilla method, but with entity damage removed
		 */
		@Override
		public void explode() {
			this.level.gameEvent(this.source, GameEvent.EXPLODE, new BlockPos(this.x, this.y, this.z));
			Set<BlockPos> set = Sets.newHashSet();
			for (int j = 0; j < 16; ++j) {
				for (int k = 0; k < 16; ++k) {
					for (int l = 0; l < 16; ++l) {
						if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
							double d = (float) j / 15.0F * 2.0F - 1.0F;
							double e = (float) k / 15.0F * 2.0F - 1.0F;
							double f = (float) l / 15.0F * 2.0F - 1.0F;
							double g = Math.sqrt(d * d + e * e + f * f);
							d /= g;
							e /= g;
							f /= g;
							float h = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
							double m = this.x;
							double n = this.y;
							double o = this.z;

							for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
								BlockPos blockPos = new BlockPos(m, n, o);
								BlockState blockState = this.level.getBlockState(blockPos);
								FluidState fluidState = this.level.getFluidState(blockPos);
								if (!this.level.isInWorldBounds(blockPos)) {
									break;
								}

								Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
								if (optional.isPresent()) {
									h -= (optional.get() + 0.3F) * 0.3F;
								}

								if (h > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
									set.add(blockPos);
								}

								m += d * 0.3F;
								n += e * 0.3F;
								o += f * 0.3F;
							}
						}
					}
				}
			}

			this.getToBlow().addAll(set);
		}
	}
}

