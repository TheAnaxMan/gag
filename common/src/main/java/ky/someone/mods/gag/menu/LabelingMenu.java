package ky.someone.mods.gag.menu;

import dev.ftb.mods.ftblibrary.ui.BaseContainer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class LabelingMenu extends BaseContainer {

	protected final Player player;

	protected final Container input = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			slotsChanged(this);
		}
	};

	public final ResultContainer output = new ResultContainer();

	private String name = "";

	protected LabelingMenu(@Nullable MenuType<?> menuType, int id, Inventory inventory, Player player) {
		super(menuType, id, inventory);
		this.player = player;

		this.addSlot(new Slot(this.input, 0, 77, 42));

		this.addSlot(new Slot(this.input, 1, 26, 42) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return false;
			}

			@Override
			public boolean isActive() {
				return false;
			}
		});

		this.addSlot(new Slot(this.output, 2, 135, 42) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return true;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				player.playNotifySound(SoundEvents.ANVIL_USE, player.getSoundSource(), 0.5F, 2.0F);
				input.setItem(0, ItemStack.EMPTY);
			}
		});

		addPlayerSlots(inventory, 8, 84, true);
	}

	public LabelingMenu(int i, Inventory inventory) {
		this(MenuTypeRegistry.LABELING.get(), i, inventory, inventory.player);
	}

	@Override
	public int getNonPlayerSlots() {
		return 3;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack ret = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack stackInSlot = slot.getItem();
			ret = stackInSlot.copy();

			// rewrite the above as a switch statement
			switch (index) {
				case 0, 1 -> {
					if (!this.moveItemStackTo(stackInSlot, 3, this.slots.size(), false)) {
						return ItemStack.EMPTY;
					}
				}
				case 2 -> {
					if (!this.moveItemStackTo(stackInSlot, 3, this.slots.size(), true)) {
						return ItemStack.EMPTY;
					}
					slot.onQuickCraft(stackInSlot, ret);
				}
				default -> {
					if (!this.moveItemStackTo(stackInSlot, 0, 2, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (stackInSlot.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (stackInSlot.getCount() == ret.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stackInSlot);
		}

		return ret;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		clearContainer(player, this.input);
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == this.input) {
			updateOutput();
		}
	}

	public void setName(String name) {
		this.name = name;
		if (this.getSlot(2).hasItem()) {
			ItemStack itemStack = this.getSlot(2).getItem();
			if (StringUtils.isBlank(name)) {
				itemStack.resetHoverName();
			} else {
				itemStack.setHoverName(new TextComponent(this.name));
			}
		}

		this.updateOutput();
	}

	private void updateOutput() {
		ItemStack inputStack = this.input.getItem(0);
		if (inputStack.isEmpty()) {
			this.output.setItem(0, ItemStack.EMPTY);
		} else {
			ItemStack ret = inputStack.copy();
			var dirty = false;

			if (StringUtils.isBlank(this.name)) {
				if (inputStack.hasCustomHoverName()) {
					ret.resetHoverName();
				}
			} else if (!this.name.equals(inputStack.getHoverName().getString())) {
				ret.setHoverName(new TextComponent(this.name));
			}

			this.output.setItem(0, ret);
			this.broadcastChanges();
		}
	}
}
