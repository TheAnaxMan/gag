package ky.someone.mods.gag.config;

import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.entity.FishingDynamiteEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;

import java.util.List;


public interface GAGConfig {
	SNBTConfig CONFIG = SNBTConfig.create(GAGUtil.MOD_ID)
			.comment("Config for GAG", "If you're a modpack maker, use the defaultconfigs folder instead!");

	interface SandsOfTime {
		SNBTConfig GROUP = CONFIG.getGroup("sands_of_time").comment(
				"Settings related to the Sands Of Time mechanic",
				"(You can also selectively disable this mechanic for certain block entities using the gag:do_not_accelerate tag)"
		);

		IntValue POUCH_CAPACITY = GROUP.getInt("pouchCapacity", Integer.MAX_VALUE, 0, Integer.MAX_VALUE)
				.comment("Max amount of grains a Pouch can hold");

		StringListValue LEVEL_FILTER = GROUP.getStringList("levelFilter", List.of())
				.comment("List of levels that the Sands Of Time mechanic will not work in");

		BooleanValue INVERT_LEVEL_FILTER = GROUP.getBoolean("invertLevelFilter", false)
				.comment("If true, the Sands Of Time mechanic will instead *only* work in the levels specified in the levelFilter list");

		IntValue GRAINS_PER_SAND = GROUP.getInt("grainsPerSand", 32, 1, Integer.MAX_VALUE)
				.comment("[NYI] Amount of grains one Sand Of Time yields");

		IntValue GRAINS_USED = GROUP.getInt("grainsUsed", 600, 1, Integer.MAX_VALUE)
				.comment("Amount of grains used per click of the Temporal Pouch")
				.comment("(Since the actual sands are NYI, this is currently just equivalent to the 'ticks' used per click)");

		IntValue DURATION_PER_USE = GROUP.getInt("durationPerUse", 30, 1, 60).comment(
				"Time (in seconds) that a block is accelerated per use, default is 30 seconds",
				"This determines the 'worth' of grains as displayed in the Pouch's tooltip"
		);
		IntValue MAX_RATE = GROUP.getInt("maxRate", 8, 1, 16)
				.comment("Maximum times the Temporal Pouch can be used in a row, corresponding to maximum speed, default is max speed of 2^8 = x256");

		BooleanValue ALLOW_RANDOM_TICKS = GROUP.getBoolean("allowRandomTicks", true)
				.comment("Whether the Temporal Pouch is allowed to accelerate random ticks");

		IntValue RANDOM_TICK_CHANCE = GROUP.getInt("randomTickChance", 1 << 12, 1 << 8, 1 << 16).comment(
				"Chance that a random tick will be performed when a random ticking block like crops or saplings is accelerated",
				"On average, this is done every 4096 / 3 ≈ 1365.33 ticks in Vanilla (see https://minecraft.gamepedia.com/Tick#Random_tick)",
				"Actual value is (config value) / (random tick game rule)"
		);

		static boolean isLevelAllowed(Level level) {
			var dim = level.dimension().location().toString();
			return LEVEL_FILTER.get().contains(dim) == INVERT_LEVEL_FILTER.get();
		}

		static void init() {
		}
	}

	interface EscapeRope {
		SNBTConfig GROUP = CONFIG.getGroup("escape_rope").comment("Settings related to the Escape Rope");

		IntValue DURABILITY = GROUP.getInt("durability", 512, 0, Short.MAX_VALUE)
				.comment("Maximum durability of the rope, default is 512");

		IntValue WARMUP = GROUP.getInt("warmup", seconds(3), 0, 72000)
				.comment("Time (in ticks) it takes to use the rope, default is 3 seconds");

		IntValue COOLDOWN = GROUP.getInt("cooldown", seconds(10), 0, 72000)
				.comment("Time (in ticks) the player has to wait after using the rope, default is 10 seconds");

		static void init() {
		}
	}

	interface Hearthstone {
		SNBTConfig GROUP = CONFIG.getGroup("hearthstone").comment("Settings related to the Hearthstone");

		IntValue DURABILITY = GROUP.getInt("durability", 64, 0, Short.MAX_VALUE)
				.comment("Maximum durability of the stone, default is 64");

		IntValue ENERGIZED_DURABILITY = GROUP.getInt("energizedDurability", 256, 0, Short.MAX_VALUE)
				.comment("Maximum durability of the energized hearthstone, default is 256");

		IntValue RANGE = GROUP.getInt("range", -1)
				.comment("Maximum range of the stone, set to -1 for unlimited range");

		IntValue DIMENSION_MULTIPLIER = GROUP.getInt("dimensionMultiplier", 2)
				.comment("Damage multiplier for using the stone across dimensions, default is 2")
				.comment("Set to -1 to disable teleporting across dimensions");

		IntValue WARMUP = GROUP.getInt("warmup", seconds(5), 0, 72000)
				.comment("Time (in ticks) it takes to use the stone, default is 5 seconds");

		IntValue COOLDOWN = GROUP.getInt("cooldown", seconds(60), 0, 72000)
				.comment("Time (in ticks) the player has to wait after using the stone, default is 60 seconds");

		BooleanValue ALLOW_SPAWN = GROUP.getBoolean("allowSpawn", true)
				.comment("Whether the stone should teleport a player to the spawn point if they have no respawn point");

		BooleanValue USE_ANCHOR_CHARGE = GROUP.getBoolean("useAnchorCharge", true)
				.comment("Whether the stone should use a charge on the player's respawn anchor, if applicable");

		BooleanValue IGNORE_SPAWN_BLOCK = GROUP.getBoolean("ignoreSpawnBlock", false)
				.comment("Whether the stone should ignore checking whether the spawn block is still valid and unobstructed");

		static void init() {
		}
	}

	interface Miscellaneous {
		SNBTConfig GROUP = CONFIG.getGroup("misc").comment("Settings related to miscellaneous items and features");

		IntValue NO_SOLICITORS_RADIUS = GROUP.getInt("noSolicitorsRadius", 64, 1, 512)
				.comment("Radius (in blocks) in which the 'No Solicitors!' sign will stop Wandering Traders from spawning, default is 32");

		static void init() {
		}
	}

	interface Dynamite {
		SNBTConfig GROUP = CONFIG.getGroup("dynamite").comment("Settings related to dynamite");
		IntValue MINING_RADIUS = GROUP.getInt("miningRadius", 7, 1, 64)
				.comment("Radius (in blocks) of the Mining Dynamite's explosion, default is 7");
		BooleanValue MINING_GIVES_HASTE = GROUP.getBoolean("miningGivesHaste", true)
				.comment("Controls whether the Mining Dynamite should give the Haste status effect if it hits a player");

		IntValue FISHING_RADIUS = GROUP.getInt("fishingRadius", 4, 1, 64)
				.comment("Radius (in blocks) of the Fishing Dynamite's explosion, default is 4");

		BooleanValue FISHING_INSTAKILL_FISH = GROUP.getBoolean("fishingInstakillFish", true)
				.comment("Controls whether the Fishing Dynamite should instakill fish")
				.comment("If false, the Fishing Dynamite will instead deal 2x damage to fish");

		BooleanValue FISHING_DAMAGE_ALL = GROUP.getBoolean("fishingDamageAll", true)
				.comment("Controls whether the Fishing Dynamite should deal damage to all entities, or only to fish");

		EnumValue<TargetFilter> FISHING_TARGET_FILTER = GROUP.getEnum("fishingTargetFilter", TargetFilter.MAP)
				.comment("Controls what entities the Fishing Dynamite should target as fish")
				.comment("Valid values are: tag, water_animal, abstract_fish, hybrid (default)")
				.comment("tag: Only entities with the 'gag:fishing_dynamite_fish' tag will be targeted, this includes all vanilla fish by default")
				.comment("water_animal: Only entities that are instances of WaterAnimal will be targeted, note this *will* also include dolphins and other water animals!")
				.comment("abstract_fish: Only entities that are instances of AbstractFish will be targeted, this might not work with some modded fish that do not extend AbstractFish")
				.comment("hybrid: Combines the abstract_fish check with the tag filter, this is the default value since it should be the most reliable");

		IntValue ADDITIONAL_FISHING_LOOT = GROUP.getInt("fishingAdditionalLoot", 5, 0, 16)
				.comment("Describes the amount of additional fish (generated from the vanilla loot table) that may be dropped by Fishing Dynamite")
				.comment("(This value is random and biased towards dropping less the more fish were already hit by the explosion)");

		enum TargetFilter {
			TAG {
				@Override
				public boolean isFish(Entity entity) {
					return entity.getType().is(FishingDynamiteEntity.FISH_TAG);
				}
			},

			WATER_ANIMAL {
				@Override
				public boolean isFish(Entity entity) {
					return entity instanceof WaterAnimal;
				}
			},

			ABSTRACT_FISH {
				@Override
				public boolean isFish(Entity entity) {
					return entity instanceof AbstractFish;
				}
			},

			HYBRID {
				@Override
				public boolean isFish(Entity entity) {
					return TAG.isFish(entity) || ABSTRACT_FISH.isFish(entity);
				}
			};

			public abstract boolean isFish(Entity entity);

			public static final NameMap<TargetFilter> MAP = NameMap.of(TargetFilter.HYBRID, TargetFilter.values()).create();
		}

		static void init() {
		}
	}

	private static int seconds(int i) {
		return i * 20;
	}

	// gotta love classloading!
	static SNBTConfig init() {
		SandsOfTime.init();
		EscapeRope.init();
		Hearthstone.init();
		Miscellaneous.init();
		Dynamite.init();

		return CONFIG;
	}
}
