package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.effect.EffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RepellingItem extends GAGItem {
    public final int duration;
    public final int amplifier;

    private final boolean addExtraTooltip;

    public RepellingItem(Properties properties, int duration, int amplifier, boolean addExtraTooltip) {
        super(properties);

        this.duration = duration;
        this.amplifier = amplifier;

        this.addExtraTooltip = addExtraTooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        var infoTooltips = new ArrayList<Component>();
        // generic info tooltip
        infoTooltips.add(new TranslatableComponent("info.gag.repelling_item").withStyle(GAGUtil.TOOLTIP_MAIN));
        // optional item-specific tooltip
        if (addExtraTooltip) {
            infoTooltips.add(getTranslation("extra").withStyle(GAGUtil.TOOLTIP_SIDENOTE));
        }
        GAGUtil.appendInfoTooltip(tooltip, infoTooltips);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var hasEffect = player.hasEffect(EffectRegistry.REPELLING.get());
        var stack = player.getItemInHand(hand);
        if (!hasEffect) {
            level.playSound(null, player.blockPosition(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.5f, 1);
            player.addEffect(new MobEffectInstance(EffectRegistry.REPELLING.get(), duration, amplifier));
            stack.shrink(1);
        }
        return hasEffect ? InteractionResultHolder.fail(stack) : InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
