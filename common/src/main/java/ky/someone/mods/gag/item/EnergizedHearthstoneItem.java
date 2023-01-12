package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnergizedHearthstoneItem extends HearthstoneItem {

	private static final String TARGET_KEY = "target";

	public EnergizedHearthstoneItem() {
		super(GAGConfig.Hearthstone.ENERGIZED_DURABILITY.get());
	}

	public boolean isBound(ItemStack stack) {
		return stack.getTagElement(TARGET_KEY) != null;
	}

	@Override
	public @Nullable TeleportPos getTeleportPos(@Nullable Player player, ItemStack stack) {
		if (isBound(stack)) {
			return TeleportPos.fromNbt(stack.getTagElement(TARGET_KEY));
		}

		return null;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(getTargetText(null, stack));
		GAGUtil.appendInfoTooltip(tooltip, List.of(
				getTranslation("info_adv").withStyle(GAGUtil.TOOLTIP_MAIN),
				getTranslation("info_adv_2").withStyle(GAGUtil.TOOLTIP_MAIN),
				getTranslation("info_adv_3").withStyle(GAGUtil.TOOLTIP_MAIN),
				new TranslatableComponent("info.gag.supports_unbreaking").withStyle(GAGUtil.TOOLTIP_SIDENOTE)
		));
	}

	public Component getTargetText(@Nullable Player player, ItemStack stack) {
		var target = getTeleportPos(player, stack);

		if (target != null) {
			var pos = target.pos();
			var level = target.level();

			var text = new TextComponent(String.format("(%.1f %.1f %.1f)", pos.x, pos.y, pos.z)).withStyle(GAGUtil.COLOUR_TRUE);

			if (player == null || !level.equals(player.level.dimension().location())) {
				text.append(" @ ").append(new TextComponent(level.toString()).withStyle(ChatFormatting.GRAY));
			}

			return getTranslation("target.bound", text).withStyle(GAGUtil.COLOUR_INFO);
		}

		return getTranslation("target.unbound").withStyle(GAGUtil.COLOUR_FALSE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		var stack = player.getItemInHand(hand);
		if (!isBound(stack)) {
			if (player.isShiftKeyDown()) {
				var pos = new TeleportPos(player.level.dimension().location(), player.position(), player.getYRot());
				stack.addTagElement(TARGET_KEY, pos.toNbt());

				player.playSound(SoundEvents.TRIDENT_THUNDER, 0.5f, 1.25f);
				return InteractionResultHolder.success(stack);
			} else {
				return InteractionResultHolder.fail(stack);
			}
		}
		return super.use(level, player, hand);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return isBound(stack) ? super.getUseDuration(stack) : 0;
	}

	public static void lightningStrike(LightningBolt bolt, Level level, Vec3 pos, List<Entity> toStrike) {
		for (var iter = toStrike.iterator(); iter.hasNext(); ) {
			Entity entity = iter.next();
			if (entity instanceof ItemEntity itemEntity) {
				var stack = itemEntity.getItem();
				if (stack.is(ItemRegistry.HEARTHSTONE.get())) {
					var newStack = new ItemStack(ItemRegistry.ENERGIZED_HEARTHSTONE.get());
					// damage the new stack relative to the old one
					var damage = stack.getDamageValue() / (float) stack.getMaxDamage();
					newStack.setDamageValue((int) (newStack.getMaxDamage() * damage));
					// copy enchantments over to the new stack
					EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), newStack);
					itemEntity.setItem(newStack);
					bolt.hitEntities.add(entity);
					iter.remove();
				} else if (stack.is(ItemRegistry.ENERGIZED_HEARTHSTONE.get())) {
					// why are lightning bolts like this mojang...
					if (!bolt.hitEntities.contains(entity)) {
						// unbind the hearthstone first
						stack.removeTagKey(TARGET_KEY);
						// and repair it by up to 25% of its durability on hit
						var damage = stack.getDamageValue() / (float) stack.getMaxDamage();
						stack.setDamageValue((int) (stack.getMaxDamage() * Math.max(0, damage - 0.25)));
					}
					itemEntity.setInvulnerable(true);
					bolt.hitEntities.add(entity);
					iter.remove();
				}
			}
		}
	}
}
