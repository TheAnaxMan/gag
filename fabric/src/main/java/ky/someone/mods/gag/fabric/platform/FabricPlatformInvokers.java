package ky.someone.mods.gag.fabric.platform;

import ky.someone.mods.gag.platform.PlatformInvokers;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class FabricPlatformInvokers implements PlatformInvokers {
	public boolean invokeExplosionPre(Level level, Explosion explosion) {
		// NYI on fabric, so NOP
		return false;
	}
}
