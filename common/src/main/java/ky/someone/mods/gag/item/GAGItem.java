package ky.someone.mods.gag.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Temporary class implementing certain methods of
 * item extensions common to both Forge and Fabric, as
 * well as some general convenience methods.
 */
public abstract class GAGItem extends Item {
	public GAGItem(Properties properties) {
		super(properties);
	}

	public boolean shouldDamage(@Nullable Player player, ItemStack stack) {
		return player == null || !player.isCreative();
	}

	public boolean shouldBob(ItemStack oldStack, ItemStack newStack) {
		return !oldStack.equals(newStack);
	}

	// forge
	public final boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return shouldBob(oldStack, newStack);
	}

	// fabric
	public final boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return shouldBob(oldStack, newStack);
	}

	public List<Component> getHoldingTooltip(Player player, ItemStack stack) {
		return List.of();
	}

	public List<Component> getUsingTooltip(Player player, ItemStack stack, int useTicks) {
		return List.of();
	}
}
