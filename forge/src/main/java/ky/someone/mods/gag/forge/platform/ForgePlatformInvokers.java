package ky.someone.mods.gag.forge.platform;

import ky.someone.mods.gag.platform.PlatformInvokers;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class ForgePlatformInvokers implements PlatformInvokers {
    public boolean invokeExplosionPre(Level level, Explosion explosion) {
        return ForgeEventFactory.onExplosionStart(level, explosion);
    }
}
