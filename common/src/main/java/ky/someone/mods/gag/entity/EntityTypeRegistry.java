package ky.someone.mods.gag.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface EntityTypeRegistry {
    DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(GAGUtil.MOD_ID, Registry.ENTITY_TYPE_REGISTRY);

    RegistrySupplier<EntityType<TimeAcceleratorEntity>> TIME_ACCELERATOR =
            ENTITIES.register("time_accelerator", () -> EntityType.Builder.of(TimeAcceleratorEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .noSummon()
                    .build(new ResourceLocation(GAGUtil.MOD_ID, "time_accelerator").toString()));      // no serialisation means no data fixers
}
