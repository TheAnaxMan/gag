package ky.someone.mods.gag.particle;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public interface ParticleTypeRegistry {
    DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(GAGUtil.MOD_ID, Registry.PARTICLE_TYPE_REGISTRY);

    RegistrySupplier<SimpleParticleType> MAGIC = PARTICLE_TYPES.register("magic", () -> new SimpleParticleType(true));
}
