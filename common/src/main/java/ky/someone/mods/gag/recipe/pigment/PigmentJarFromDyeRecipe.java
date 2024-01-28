package ky.someone.mods.gag.recipe.pigment;

import ky.someone.mods.gag.item.ItemRegistry;
import ky.someone.mods.gag.item.PigmentJarItem;
import ky.someone.mods.gag.misc.Pigment;
import ky.someone.mods.gag.recipe.GAGRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Inputs are as follows:
 * - Empty Pigment Jar
 * - Flint
 * - Milk Bucket
 * - [Any amount of a *single* dye]
 * <p>
 * Outputs are as follows:
 * - Empty Bucket (replaces milk bucket)
 * - Pigment Jar with color of dye and amount = 4 * amount of dye
 */
public class PigmentJarFromDyeRecipe extends CustomRecipe {
	public PigmentJarFromDyeRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		var emptyJar = false;
		var flint = false;
		var milk = false;

		DyeColor dye = null;
		var dyeAmount = 0;

		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (!emptyJar && stack.is(ItemRegistry.PIGMENT_JAR.get()) && PigmentJarItem.isEmpty(stack)) {
				emptyJar = true;
			} else if (!flint && stack.is(Items.FLINT)) {
				flint = true;
			} else if (!milk && stack.is(Items.MILK_BUCKET)) {
				milk = true;
			} else if (stack.getItem() instanceof DyeItem dyeItem) {
				if (dye == null) {
					dye = dyeItem.getDyeColor();
					dyeAmount = 1;
				} else if (dye == dyeItem.getDyeColor()) {
					dyeAmount++;
				} else {
					return false;
				}
			} else if (!stack.isEmpty()) {
				return false;
			}
		}

		return emptyJar && flint && milk && dye != null;
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
		// count the number of dye and create a pigment jar with that color and amount
		DyeColor dye = null;
		var amount = 0;

		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.getItem() instanceof DyeItem dyeItem) {
				if (dye == null) {
					dye = dyeItem.getDyeColor();
					amount = 1;
				} else if (dye == dyeItem.getDyeColor()) {
					amount++;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}

		if (dye == null) return ItemStack.EMPTY;

		amount *= PigmentJarItem.DYE_AMOUNT;
		if (amount > PigmentJarItem.MAX_AMOUNT) {
			return ItemStack.EMPTY;
		}

		return Pigment.forText(dye).withAmount(amount).asJar();
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= 2 && j >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GAGRecipeSerializers.PIGMENT_JAR_FROM_DYE.get();
	}
}
