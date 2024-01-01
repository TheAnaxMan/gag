package ky.someone.mods.gag.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAGUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface EntityTypeRegistry {
	DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(GAGUtil.MOD_ID, Registry.ENTITY_TYPE_REGISTRY);

	RegistrySupplier<EntityType<TimeAcceleratorEntity>> TIME_ACCELERATOR =
			ENTITIES.register("time_accelerator", () -> EntityType.Builder.of(TimeAcceleratorEntity::new, MobCategory.MISC)
					.sized(0.1f, 0.1f)
					.noSummon()
					.build(GAGUtil.id("time_accelerator").toString()));      // no serialisation means no data fixers

	RegistrySupplier<EntityType<MiningDynamiteEntity>> MINING_DYNAMITE =
			ENTITIES.register("mining_dynamite", () -> EntityType.Builder.<MiningDynamiteEntity>of(MiningDynamiteEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(GAGUtil.id("mining_dynamite").toString()));

	RegistrySupplier<EntityType<FishingDynamiteEntity>> FISHING_DYNAMITE =
			ENTITIES.register("fishing_dynamite", () -> EntityType.Builder.<FishingDynamiteEntity>of(FishingDynamiteEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(GAGUtil.id("fishing_dynamite").toString()));
}
