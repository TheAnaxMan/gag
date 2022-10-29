package ky.someone.mods.gag.forge;

import dev.architectury.platform.forge.EventBuses;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GAGUtil.MOD_ID)
public class GAGForge {
    public GAGForge() {
        EventBuses.registerModEventBus(GAGUtil.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        new GAG().init();
    }
}
