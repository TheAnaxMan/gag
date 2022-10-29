package ky.someone.mods.gag.fabric;

import ky.someone.mods.gag.GAG;
import net.fabricmc.api.ModInitializer;

public class GAGFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new GAG().init();
    }
}
