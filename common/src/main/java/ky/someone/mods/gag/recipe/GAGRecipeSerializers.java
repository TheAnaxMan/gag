package ky.someone.mods.gag.recipe;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.recipe.pigment.PigmentJarFromDyeRecipe;
import ky.someone.mods.gag.recipe.pigment.PigmentJarLeatherDyingRecipe;
import ky.someone.mods.gag.recipe.pigment.PigmentJarMixingRecipe;
import ky.someone.mods.gag.recipe.pigment.PigmentJarSplittingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import java.util.function.Function;

public interface GAGRecipeSerializers {
	DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(GAGUtil.MOD_ID, Registry.RECIPE_SERIALIZER_REGISTRY);

	RegistrySupplier<RecipeSerializer<?>> PIGMENT_JAR_MIXING = special("pigment_jar_mixing", PigmentJarMixingRecipe::new);
	RegistrySupplier<RecipeSerializer<?>> PIGMENT_JAR_FROM_DYE = special("pigment_jar_from_dye", PigmentJarFromDyeRecipe::new);
	RegistrySupplier<RecipeSerializer<?>> PIGMENT_JAR_SPLITTING = special("pigment_jar_splitting", PigmentJarSplittingRecipe::new);
	RegistrySupplier<RecipeSerializer<?>> PIGMENT_JAR_LEATHER_DYING = special("pigment_jar_leather_dying", PigmentJarLeatherDyingRecipe::new);

	private static <T extends CustomRecipe> RegistrySupplier<RecipeSerializer<?>> special(String name, Function<ResourceLocation, T> factory) {
		return RECIPE_SERIALIZERS.register(name, () -> new SimpleRecipeSerializer<>(factory));
	}
}
