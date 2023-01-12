package ky.someone.mods.gag.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.entity.MiningDynamiteEntity;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.function.UnaryOperator;

public interface ItemRegistry {
	DeferredRegister<Item> ITEMS = DeferredRegister.create(GAGUtil.MOD_ID, Registry.ITEM_REGISTRY);

	RegistrySupplier<TemporalPouchItem> TIME_SAND_POUCH = ITEMS.register("time_sand_pouch", TemporalPouchItem::new);
	//RegistrySupplier<TimeSandItem> SANDS_OF_TIME = ITEMS.register("time_sand", TimeSandItem::new);

	RegistrySupplier<Item> ESCAPE_ROPE = ITEMS.register("escape_rope", EscapeRopeItem::new);
	RegistrySupplier<Item> HEARTHSTONE = ITEMS.register("hearthstone", HearthstoneItem::new);
	RegistrySupplier<Item> ENERGIZED_HEARTHSTONE = ITEMS.register("energized_hearthstone", EnergizedHearthstoneItem::new);

	RegistrySupplier<Item> SACRED_SALT = repelling("sacred_salt", p -> p.stacksTo(16).rarity(Rarity.UNCOMMON), 40 * 20, 1, false);
	RegistrySupplier<Item> SACRED_SALVE = repelling("sacred_salve", p -> p.stacksTo(4).rarity(Rarity.RARE), 120 * 20, 2, true);
	RegistrySupplier<Item> SACRED_BALM = repelling("sacred_balm", p -> p.stacksTo(4).rarity(Rarity.RARE), 360 * 20, 0, true);

	// TODO: only downward throwing speed should be accelerated
	RegistrySupplier<Item> MINING_DYNAMITE = dynamite("mining_dynamite", MiningDynamiteEntity::new, List.of(
			new TranslatableComponent("item.gag.mining_dynamite.info").withStyle(GAGUtil.TOOLTIP_MAIN)
	), 1.5);
	// TODO: cba to implement this right now
	//RegistrySupplier<Item> FISHING_DYNAMITE = dynamite("fishing_dynamite", FishingGrenadeEntity::new);

	RegistrySupplier<Item> LABELING_TOOL = ITEMS.register("labeling_tool", LabelingToolItem::new);

	private static RegistrySupplier<Item> repelling(String name, UnaryOperator<Item.Properties> properties, int duration, int amplifier, boolean hasTooltip) {
		return ITEMS.register(name, () -> new RepellingItem(properties.apply(new Item.Properties().tab(GAG.CREATIVE_TAB)), duration, amplifier, hasTooltip));
	}

	private static RegistrySupplier<Item> dynamite(String name, DynamiteItem.EntityFactory factory, List<Component> tooltip, double throwSpeed) {
		return ITEMS.register(name, () -> new DynamiteItem<>(new Item.Properties().tab(GAG.CREATIVE_TAB), factory, tooltip, throwSpeed));
	}
}
