package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

import static ky.someone.mods.gag.GAGUtil.TOOLTIP_MAIN;
import static ky.someone.mods.gag.GAGUtil.TOOLTIP_SIDENOTE;

public class EscapeRopeItem extends GAGItem {
	public EscapeRopeItem() {
		super(new Item.Properties()
				.tab(GAG.CREATIVE_TAB)
				.durability(GAGConfig.EscapeRope.DURABILITY.get()));
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
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		var stack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return GAGConfig.EscapeRope.WARMUP.get();
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide && entity instanceof Player player) {
			ServerLevel serverLevel = (ServerLevel) level;
			var pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, entity.getOnPos());
			BlockPos teleportPos = null;
			if (pos.getY() > 0) {
				if (pos.getY() >= serverLevel.getLogicalHeight()) { // broken heightmap (nether, other mod dimensions)
					for (var newPos : BlockPos.spiralAround(new BlockPos(pos.getX(), level.getSeaLevel(), pos.getY()), 16, Direction.EAST, Direction.SOUTH)) {
						BlockState bs = level.getBlockState(newPos);

						if (bs.getMaterial().isSolidBlocking() && level.isEmptyBlock(newPos.above(1)) && level.isEmptyBlock(newPos.above(2)) && level.isEmptyBlock(newPos.above(3))) {
							teleportPos = newPos.above();
							break;
						}
					}
				} else {
					teleportPos = pos.above();
				}
			}

			var creative = player.isCreative();

			if (teleportPos != null) {
				var durabilityUsed = player.blockPosition().distManhattan(teleportPos);
				var durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
				if (creative || durabilityUsed <= durabilityLeft) {
					stack.hurtAndBreak(durabilityUsed, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
					player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY() + 0.5, teleportPos.getZ() + 0.5);
					level.playSound(null, teleportPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5f, 1f);
				}
			} else {
				player.sendSystemMessage(Component.translatable("item.gag.escape_rope.no_space").withStyle(ChatFormatting.RED));
				level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
			}

			if (!stack.isEmpty() && !creative) {
				player.getCooldowns().addCooldown(stack.getItem(), GAGConfig.EscapeRope.COOLDOWN.get());
			}
		}
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		GAGUtil.appendInfoTooltip(tooltip, List.of(
				Component.translatable("item.gag.escape_rope.info").withStyle(TOOLTIP_MAIN),
				Component.translatable("info.gag.supports_unbreaking").withStyle(TOOLTIP_SIDENOTE)
		));
	}
}
