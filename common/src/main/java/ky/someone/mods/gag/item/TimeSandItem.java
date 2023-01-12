package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.config.GAGConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimeSandItem extends GAGItem {
	public TimeSandItem() {
		super(new Item.Properties().tab(GAG.CREATIVE_TAB).stacksTo(16));
	}

	@Override
	public void inventoryTick(ItemStack sand, Level level, Entity entity, int slot, boolean isSelected) {
		if (entity instanceof Player player && !level.isClientSide) {
			var inv = player.getInventory();
			for (int i = 0; i < inv.getContainerSize(); ++i) {
				ItemStack maybePouch = inv.getItem(i);
				if (maybePouch.is(ItemRegistry.TIME_SAND_POUCH.get())) {
					// add the sand to the pouch, and remove it from the player's inventory
					var oldCount = TemporalPouchItem.getStoredGrains(maybePouch);
					var toAdd = Math.min(GAGConfig.SandsOfTime.POUCH_CAPACITY.get() - oldCount, sand.getCount());
					if (toAdd > 0) {
						TemporalPouchItem.setStoredGrains(maybePouch, oldCount + (toAdd * GAGConfig.SandsOfTime.GRAINS_PER_SAND.get()));
						sand.shrink(toAdd);
					}

					// if we're out of sand, stop checking the rest of the inventory unnecessarily
					if (sand.isEmpty()) {
						return;
					}
				}
			}
			// if the player has no pouch (meaning the sand is still in their inventory),
			// it will randomly decay while the player holding it is not in Creative Mode
			if (!sand.isEmpty() && !player.isCreative() && level.getGameTime() % 10 == 0) {
				if (level.random.nextBoolean()) {
					sand.shrink(1);
				}
				//var decayed = (int) level.random.doubles(sand.getCount()).filter(d -> d < 0.1).count();
				//sand.shrink(decayed);
			}
		}
	}
}
