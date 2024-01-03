package ky.someone.mods.gag.recipe.pigment;

import ky.someone.mods.gag.item.ItemRegistry;
import ky.someone.mods.gag.item.PigmentJarItem;
import ky.someone.mods.gag.misc.Pigment;
import ky.someone.mods.gag.recipe.GAGRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Map;

public class PigmentJarLeatherDyingRecipe extends CustomRecipe {

	private static final Map<Item, Integer> VALID_ITEMS = Map.of(
			Items.LEATHER_HELMET, 5,
			Items.LEATHER_CHESTPLATE, 8,
			Items.LEATHER_LEGGINGS, 7,
			Items.LEATHER_BOOTS, 4,
			Items.LEATHER_HORSE_ARMOR, 7
	);

	public PigmentJarLeatherDyingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		// leather armour + pigment jar(s), where the total pigment amount is at least VALID_ITEMS[leather]
		var leatherItem = ItemStack.EMPTY;
		var hasPigment = false; // we want to fall back to vanilla if there are no pigment jars
		var pigmentAmount = 0;

		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.getItem() instanceof DyeableLeatherItem item && VALID_ITEMS.containsKey(item)) {
				if (!leatherItem.isEmpty()) return false;
				leatherItem = stack;
			} else if (stack.is(ItemRegistry.PIGMENT_JAR.get())) {
				hasPigment = true;
				pigmentAmount += PigmentJarItem.getColorAmount(stack);
			} else if (stack.getItem() instanceof DyeItem dye) {
				pigmentAmount += PigmentJarItem.DYE_AMOUNT;
			} else if (!stack.isEmpty()) {
				return false;
			}
		}

		return !leatherItem.isEmpty() && hasPigment && pigmentAmount >= VALID_ITEMS.get(leatherItem.getItem());
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
		// output should be the dyed leather armour
		var leatherItem = ItemStack.EMPTY;
		var output = Pigment.EMPTY; // use an empty pigment to start with

		for (int i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.getItem() instanceof DyeableLeatherItem item && VALID_ITEMS.containsKey(item)) {
				if (!leatherItem.isEmpty()) return ItemStack.EMPTY;
				leatherItem = stack.copy();

				if (item.hasCustomColor(stack)) {
					var leatherPigment = Pigment.ofRgb(item.getColor(stack), VALID_ITEMS.get(item));
					output = output.mix(leatherPigment);
				}
			} else if (stack.is(ItemRegistry.PIGMENT_JAR.get())) {
				var pigment = PigmentJarItem.getPigment(stack);
				output = output.mix(pigment);
			} else if (stack.getItem() instanceof DyeItem dye) {
				var dyePigment = Pigment.forLeather(dye.getDyeColor());
				output = output.mix(dyePigment);
			}
		}

		if (leatherItem.isEmpty() || output.isEmpty()) return ItemStack.EMPTY;

		// set the color of the leather item
		((DyeableLeatherItem) leatherItem.getItem()).setColor(leatherItem, output.rgb());
		return leatherItem;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		// get all jars in the crafting grid and return empty jars in their place
		var remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < remaining.size(); i++) {
			var stack = container.getItem(i);
			if (PigmentJarItem.isNonEmptyJar(stack)) {
				remaining.set(i, ItemRegistry.PIGMENT_JAR.get().getDefaultInstance());
			}
		}

		return remaining;
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
