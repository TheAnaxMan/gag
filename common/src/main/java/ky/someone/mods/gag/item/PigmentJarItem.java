package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.misc.Pigment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static ky.someone.mods.gag.GAGUtil.*;

public class PigmentJarItem extends GAGItem {

	/**
	 * example nbt:
	 * { "pigment": { "color": 16711680, "amount": 255 } }
	 */

	public static final String PIGMENT_NBT_KEY = "pigment";
	public static final String COLOR_NBT_KEY = "color";
	public static final String AMOUNT_NBT_KEY = "amount";

	public static final int MAX_AMOUNT = 64;
	public static final int DYE_AMOUNT = 4;

	public PigmentJarItem() {
		super(new Properties().tab(GAG.CREATIVE_TAB).stacksTo(16));
	}

	@Nullable
	public static Pigment getPigment(ItemStack stack) {
		var pigmentTag = stack.getTagElement(PIGMENT_NBT_KEY);
		if (pigmentTag == null) return null;

		var color = pigmentTag.getInt(COLOR_NBT_KEY);
		var amount = pigmentTag.getInt(AMOUNT_NBT_KEY);

		return Pigment.ofRgb(color, amount);
	}

	public static boolean isEmpty(ItemStack stack) {
		var pigment = getPigment(stack);
		return pigment == null || pigment.isEmpty();
	}

	public static int getRgbColor(ItemStack stack) {
		var pigment = getPigment(stack);
		return pigment == null ? -1 : pigment.rgb();
	}

	public static int getColorAmount(ItemStack stack) {
		var pigment = getPigment(stack);
		return pigment == null ? 0 : pigment.amount();
	}

	public static boolean isNonEmptyJar(ItemStack stack) {
		return stack.is(ItemRegistry.PIGMENT_JAR.get()) && !isEmpty(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		if (isEmpty(stack)) {
			list.add(Component.translatable("item.gag.pigment_jar.contents.empty").withStyle(ChatFormatting.ITALIC).withStyle(TOOLTIP_FLAVOUR));
			GAGUtil.appendInfoTooltip(list, List.of(
					Component.translatable("item.gag.pigment_jar.info.empty.1").withStyle(TOOLTIP_MAIN),
					Component.translatable("item.gag.pigment_jar.info.empty.2").withStyle(TOOLTIP_EXTRA)
			));
		} else {
			var pigment = Objects.requireNonNull(getPigment(stack));
			list.add(Component.translatable("item.gag.pigment_jar.contents",
					GAGUtil.asStyledValue(pigment.amount(), MAX_AMOUNT / 2.0, Integer.toString(pigment.amount())),
					Component.literal(pigment.hex()).withStyle(s -> s.withColor(pigment.rgb()))
			).withStyle(TOOLTIP_FLAVOUR));
			GAGUtil.appendInfoTooltip(list, List.of(
					Component.translatable("item.gag.pigment_jar.info.filled.1").withStyle(TOOLTIP_MAIN),
					Component.translatable("item.gag.pigment_jar.info.filled.2").withStyle(TOOLTIP_EXTRA)
			));
		}
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
		if (allowedIn(tab)) {
			stacks.add(getDefaultInstance());
			for (DyeColor color : DyeColor.values()) {
				stacks.add(Pigment.forText(color).asJar());
			}
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		var name = super.getName(stack);
		var pigment = getPigment(stack);

		if (pigment == null) return name;
		return name.copy().withStyle(s -> s.withColor(pigment.rgb()));
	}
}
