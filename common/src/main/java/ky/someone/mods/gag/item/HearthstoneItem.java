package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static ky.someone.mods.gag.GAG.CHAT_UUID;

public class HearthstoneItem extends GAGItem {

	public HearthstoneItem() {
		this(GAGConfig.Hearthstone.DURABILITY.get());
	}

	public HearthstoneItem(int durability) {
		super(new Properties()
				.tab(GAG.CREATIVE_TAB)
				.durability(durability));
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.BOW;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticks) {
		var pos = entity.position();

		double radius = 0.75;
		double spirals = 4;

		// today on "max has to remember how to do simple trig"
		for (int i = 1; i <= spirals; i++) {
			double x = Math.cos((ticks + 2 * Math.PI * i) / spirals) * radius;
			double z = Math.sin((ticks + 2 * Math.PI * i) / spirals) * radius;
			level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.x + x, pos.y + 0.1 * (20 - (double) ticks % 20), pos.z + z, 0, 0, 0);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		var stack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return GAGConfig.Hearthstone.WARMUP.get();
	}

	@Nullable
	public TeleportPos getTeleportPos(@Nullable Player player, ItemStack stack) {
		boolean allowSpawn = GAGConfig.Hearthstone.ALLOW_SPAWN.get();
		boolean ignoreSpawnBlock = GAGConfig.Hearthstone.IGNORE_SPAWN_BLOCK.get();
		boolean useAnchorCharge = GAGConfig.Hearthstone.USE_ANCHOR_CHARGE.get();

		if (player instanceof ServerPlayer serverPlayer) {
			var server = serverPlayer.server;
			var respawnDim = server.getLevel(serverPlayer.getRespawnDimension());

			if (respawnDim != null) {
				var respawnPos = serverPlayer.getRespawnPosition();
				if (respawnPos != null) {
					var actualPos = Player.findRespawnPositionAndUseSpawnBlock(respawnDim, respawnPos, serverPlayer.getRespawnAngle(), ignoreSpawnBlock, useAnchorCharge);
					if (actualPos.isPresent()) {
						return new TeleportPos(respawnDim.dimension().location(), actualPos.get(), serverPlayer.getRespawnAngle());
					}
				}
			} else {
				respawnDim = server.overworld();
			}

			if (allowSpawn) {
				var spawnPos = Vec3.atBottomCenterOf(respawnDim.getSharedSpawnPos());
				return new TeleportPos(respawnDim.dimension().location(), spawnPos, serverPlayer.getRespawnAngle());
			}

		}

		return null;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide && entity instanceof ServerPlayer player) {
			var target = getTeleportPos(player, stack);
			if (target != null) {
				var targetLevel = target.getLevel(player.server);
				if (targetLevel != null) {
					return tryTeleport(stack, targetLevel, player, target.pos, target.yaw);
				}
			}

			player.sendMessage(getTranslation("no_target").withStyle(ChatFormatting.RED), CHAT_UUID);
			level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
		}
		return stack;
	}

	private ItemStack tryTeleport(ItemStack stack, ServerLevel level, ServerPlayer player, Vec3 pos, float yaw) {
		var creative = player.isCreative();

		var durabilityUsed = level.equals(player.getLevel()) ? 1 : GAGConfig.Hearthstone.DIMENSION_MULTIPLIER.get();
		var distance = player.position().distanceTo(pos) * durabilityUsed;
		var range = GAGConfig.Hearthstone.RANGE.get();

		if (durabilityUsed > 0) {
			if (range < 0 || distance < range) {
				stack.hurtAndBreak(durabilityUsed, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
				player.teleportTo(level, pos.x, pos.y, pos.z, yaw, 0f);
				level.playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);

				if (!stack.isEmpty() && !creative) {
					player.getCooldowns().addCooldown(stack.getItem(), GAGConfig.Hearthstone.COOLDOWN.get());
				}
			} else {
				player.sendMessage(getTranslation("too_weak").withStyle(ChatFormatting.RED), CHAT_UUID);
				level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
			}
		} else {
			player.sendMessage(getTranslation("too_weak").withStyle(ChatFormatting.RED), CHAT_UUID);
			level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
		}
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		GAGUtil.appendInfoTooltip(tooltip, List.of(
				getTranslation("info").withStyle(GAGUtil.TOOLTIP_MAIN),
				new TranslatableComponent("info.gag.supports_unbreaking").withStyle(GAGUtil.TOOLTIP_SIDENOTE)
		));
	}

	public Component getTargetText(Player player, ItemStack stack) {
		return getTranslation("target.bound", getTranslation("target.respawn").withStyle(GAGUtil.COLOUR_TRUE)).withStyle(GAGUtil.COLOUR_INFO);
	}

	@Override
	public List<Component> getHoldingTooltip(Player player, ItemStack stack) {
		return List.of(
				stack.getHoverName(),
				getTargetText(player, stack)
		);
	}

	@Override
	public List<Component> getUsingTooltip(Player player, ItemStack stack, int useTicks) {
		var totalUseTicks = getUseDuration(stack);
		useTicks = Math.min(useTicks, totalUseTicks);
		var warmupText = GAGUtil.asStyledValue(useTicks, totalUseTicks, String.format("%.2f", (totalUseTicks - useTicks) / 20d));
		return List.of(
				stack.getHoverName(),
				getTargetText(player, stack),
				getTranslation("warmup", warmupText).withStyle(GAGUtil.TOOLTIP_MAIN)
		);
	}

	protected TranslatableComponent getTranslation(String key, Object... args) {
		return new TranslatableComponent("item.gag.hearthstone." + key, args);
	}

	record TeleportPos(ResourceLocation level, Vec3 pos, float yaw) {
		static TeleportPos fromNbt(CompoundTag nbt) {
			var level = new ResourceLocation(nbt.getString("dim"));
			var x = nbt.getDouble("x");
			var y = nbt.getDouble("y");
			var z = nbt.getDouble("z");
			return new TeleportPos(level, new Vec3(x, y, z), nbt.getFloat("yaw"));
		}

		public CompoundTag toNbt() {
			return Util.make(new CompoundTag(), nbt -> {
				nbt.putString("dim", level.toString());
				nbt.putDouble("x", pos.x);
				nbt.putDouble("y", pos.y);
				nbt.putDouble("z", pos.z);
				nbt.putFloat("yaw", yaw);
			});
		}

		@Nullable
		public ServerLevel getLevel(MinecraftServer server) {
			return server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, level));
		}
	}
}
