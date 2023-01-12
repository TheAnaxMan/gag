package ky.someone.mods.gag.platform;

import dev.architectury.event.events.common.ExplosionEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import oshi.util.Memoizer;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface PlatformInvokers {

	Supplier<PlatformInvokers> INSTANCE = Memoizer.memoize(() -> ServiceLoader.load(PlatformInvokers.class).findFirst()
			.orElseThrow(() -> new IllegalStateException("No PlatformInvokers implementation found!")));

	boolean invokeExplosionPre(Level level, Explosion explosion);

	static PlatformInvokers get() {
		return INSTANCE.get();
	}

	static boolean explosionPre(Level level, Explosion explosion) {
		return get().invokeExplosionPre(level, explosion) || ExplosionEvent.PRE.invoker().explode(level, explosion).isTrue();
	}
}
