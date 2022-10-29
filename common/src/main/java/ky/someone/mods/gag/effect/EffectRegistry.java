package ky.someone.mods.gag.effect;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;

public interface EffectRegistry {
    DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(GAGUtil.MOD_ID, Registry.MOB_EFFECT_REGISTRY);

    RegistrySupplier<MobEffect> REPELLING = EFFECTS.register("repelling", RepellingEffect::new);
}
