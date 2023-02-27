package ky.someone.mods.gag.forge;

import dev.architectury.platform.forge.EventBuses;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.item.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GAGUtil.MOD_ID)
@Mod.EventBusSubscriber
public class GAGForge {
	public GAGForge() {
		EventBuses.registerModEventBus(GAGUtil.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new GAG().init();
	}

	@SubscribeEvent
	public static void onRegistryMissingMappings(RegistryEvent.MissingMappings<Item> event) {
		// remap "tiab:time_in_a_bottle" to "gag:temporal_pouch" if TIAB Standalone is missing
		//  (requested by people wanting to transition from TIAB Standalone to GAG)
		for (var mapping : event.getAllMappings()) {
			if (mapping.key.equals(new ResourceLocation("tiab:time_in_a_bottle"))) {
				mapping.remap(ItemRegistry.TIME_SAND_POUCH.get());
				break;
			}
		}
	}
}
