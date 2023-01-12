package ky.someone.mods.gag.item;

import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.entity.AbstractDynamiteEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamiteItem<T extends AbstractDynamiteEntity> extends GAGItem {

	private final EntityFactory factory;
	private final @Nullable List<Component> infoTooltip;
	private final double throwSpeed;

	public DynamiteItem(Properties properties, EntityFactory factory, @Nullable List<Component> infoTooltip, double throwSpeed) {
		super(properties);
		this.factory = factory;
		this.infoTooltip = infoTooltip;
		this.throwSpeed = throwSpeed;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		if (!level.isClientSide) {
			AbstractDynamiteEntity dynamite = factory.create(player, level);
			dynamite.setItem(itemStack);
			dynamite.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
			dynamite.setDeltaMovement(dynamite.getDeltaMovement().scale(throwSpeed));
			level.addFreshEntity(dynamite);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.getAbilities().instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
		if (infoTooltip != null) {
			GAGUtil.appendInfoTooltip(tooltip, this.infoTooltip);
		}
	}

	@FunctionalInterface
	public interface EntityFactory {
		AbstractDynamiteEntity create(Player owner, Level level);
	}
}
