package ky.someone.mods.gag;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.UnaryOperator;

public interface GAGUtil {

	String MOD_ID = "gag";

	UnaryOperator<Style> TOOLTIP_MAIN = (style) -> style.withColor(0xfcb95b);
	UnaryOperator<Style> TOOLTIP_SIDENOTE = (style) -> style.withColor(0x0fd1ec).withItalic(true);

	UnaryOperator<Style> COLOUR_TRUE = (style) -> style.withColor(0x4ecc8d);
	UnaryOperator<Style> COLOUR_FALSE = (style) -> style.withColor(0xfd6d5d);

	UnaryOperator<Style> COLOUR_INFO = (style) -> style.withColor(0x5555ff);

	static void appendInfoTooltip(List<Component> tooltip, List<Component> info) {
		var isShift = Screen.hasShiftDown();

		tooltip.add(new TextComponent("ℹ")
				.append(new TextComponent(" (Shift)").withStyle(isShift ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY))
				.withStyle(COLOUR_INFO));

		if (isShift) {
			tooltip.addAll(info);
		}
	}

	static Component styledBool(boolean b) {
		return new TextComponent(b ? "✔" : "✘").withStyle(b ? COLOUR_TRUE : COLOUR_FALSE);
	}

	// returns a new style using "parent" with a colour representing
	// the given ratio of the range [0, 1] (0 = red, 1 = green)
	static Style styledRatio(Style parent, double ratio) {
		return parent.withColor(Mth.hsvToRgb((float) (ratio / 3), 1, 1));
	}

	static Component asStyledValue(double value, double max) {
		return asStyledValue(value, max, Double.toString(value));
	}

	static Component asStyledValue(double value, double max, String formattedValue) {
		return new TextComponent(formattedValue).withStyle(styledRatio(Style.EMPTY, value / max));
	}

	static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	static <U> U TODO() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
