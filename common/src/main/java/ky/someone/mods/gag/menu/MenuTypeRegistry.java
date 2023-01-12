package ky.someone.mods.gag.menu;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public interface MenuTypeRegistry {
	DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(GAGUtil.MOD_ID, Registry.MENU_REGISTRY);

	RegistrySupplier<MenuType<LabelingMenu>> LABELING = MENUS.register("labeling", () -> new MenuType<>(LabelingMenu::new));
}
