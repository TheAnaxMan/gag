package ky.someone.mods.gag.forge;

import dev.architectury.platform.forge.EventBuses;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.item.ItemRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod(GAGUtil.MOD_ID)
@Mod.EventBusSubscriber
public class GAGForge {
	public GAGForge() {
		EventBuses.registerModEventBus(GAGUtil.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new GAG().init();
	}

	@SubscribeEvent
	public static void onRegistryMissingMappings(MissingMappingsEvent event) {
		// remap "tiab:time_in_a_bottle" to "gag:temporal_pouch" if TIAB Standalone is missing
		//  (requested by people wanting to transition from TIAB Standalone to GAG)
		for (var mapping : event.getAllMappings(Registry.ITEM_REGISTRY)) {
			if (mapping.getKey().equals(new ResourceLocation("tiab:time_in_a_bottle"))) {
				mapping.remap(ItemRegistry.TIME_SAND_POUCH.get());
				break;
			}
		}
	}
}
