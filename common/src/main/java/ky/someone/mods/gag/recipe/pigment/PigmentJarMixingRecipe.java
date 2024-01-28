package ky.someone.mods.gag.recipe.pigment;

import ky.someone.mods.gag.item.ItemRegistry;
import ky.someone.mods.gag.item.PigmentJarItem;
import ky.someone.mods.gag.misc.Pigment;
import ky.someone.mods.gag.recipe.GAGRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PigmentJarMixingRecipe extends CustomRecipe {
	public PigmentJarMixingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		// accept iff there are exactly 2 non-empty pigment jars and nothing else
		int found = 0;

		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.is(ItemRegistry.PIGMENT_JAR.get())) {
				if (!PigmentJarItem.isEmpty(stack)) {
					found++;
				}
			} else if (!stack.isEmpty()) {
				return false;
			}
		}

		return found >= 2;
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
		// mix the contents of the two jars together
		Pigment result = null;
		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (PigmentJarItem.isNonEmptyJar(stack)) {
				var pigment = PigmentJarItem.getPigment(stack);
				if (result == null) {
					result = pigment;
				} else {
					result = result.mix(pigment);
				}
			}
		}

		if (result == null) return ItemStack.EMPTY;
		return result.asJar();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		var list = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

		var first = true;
		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.is(ItemRegistry.PIGMENT_JAR.get())) {
				if (first) {
					first = false;
				} else {
					list.set(i, ItemRegistry.PIGMENT_JAR.get().getDefaultInstance());
				}
			}
		}

		return list;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i * j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GAGRecipeSerializers.PIGMENT_JAR_MIXING.get();
	}
}
