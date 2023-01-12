package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.menu.LabelingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static ky.someone.mods.gag.GAGUtil.TOOLTIP_MAIN;

public class LabelingToolItem extends GAGItem implements MenuProvider {

	public static final Component TITLE = new TranslatableComponent("item.gag.labeling_tool.text_box");

	public static final int XP_COST = 10;

	public LabelingToolItem() {
		super(new Properties().tab(GAG.CREATIVE_TAB).stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide && !player.isShiftKeyDown()) {
			var stack = player.getItemInHand(hand);
			player.openMenu(this);
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		GAGUtil.appendInfoTooltip(tooltip, List.of(new TranslatableComponent("item.gag.labeling_tool.info").withStyle(TOOLTIP_MAIN)));
	}

	@Override
	public Component getDisplayName() {
		return TITLE;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new LabelingMenu(i, inventory);
	}
}
