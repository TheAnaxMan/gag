package ky.someone.mods.gag.item;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.platform.Platform;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.entity.EntityTypeRegistry;
import ky.someone.mods.gag.entity.TimeAcceleratorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ky.someone.mods.gag.GAGUtil.TOOLTIP_MAIN;
import static ky.someone.mods.gag.config.GAGConfig.SandsOfTime;

public class TemporalPouchItem extends GAGItem {

	public static final String GRAINS_NBT_KEY = "grains";

	// This is the key for stored time used by TIAB Standalone,
	//  which we want to remap to our own if the mod is not loaded
	public static final String TIAB_STORED_KEY = "storedTime";

	public static TagKey<BlockEntityType<?>> DO_NOT_ACCELERATE = TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, GAGUtil.id("do_not_accelerate"));

	public TemporalPouchItem() {
		super(new Item.Properties().tab(GAG.CREATIVE_TAB).stacksTo(1));
	}

	public static int getStoredGrains(ItemStack stack) {
		return stack.getOrCreateTag().getInt("grains");
	}

	public static void setStoredGrains(ItemStack stack, int time) {
		int newStoredTime = Math.min(time, SandsOfTime.POUCH_CAPACITY.get());
		stack.getOrCreateTag().putInt("grains", newStoredTime);
	}

	@Override
	public void verifyTagAfterLoad(CompoundTag tag) {
		super.verifyTagAfterLoad(tag);
		if (!Platform.isModLoaded("tiab") && tag.contains(TIAB_STORED_KEY)) {
			tag.putInt(GRAINS_NBT_KEY, tag.getInt(TIAB_STORED_KEY));
			tag.remove(TIAB_STORED_KEY);
		}
	}

	public MutableComponent getTimeForDisplay(ItemStack stack) {
		int storedGrains = getStoredGrains(stack);
		int seconds = storedGrains * SandsOfTime.DURATION_PER_USE.get() / SandsOfTime.GRAINS_USED.get();
		int minutes = seconds / 60;
		int hours = seconds / 3600;

		String timeString = String.format("%ds", seconds);

		if (hours > 0) {
			timeString = String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
		} else {
			if (minutes > 0) {
				timeString = String.format("%dm %ds", minutes, seconds % 60);
			}
		}

		return new TranslatableComponent("item.gag.time_sand_pouch.info.stored_grains", storedGrains, timeString);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, level, entity, itemSlot, isSelected);
		if (level.isClientSide || !(entity instanceof Player player) || PlayerHooks.isFake(player)) {
			return;
		}

		if (level.getGameTime() % 20 == 0) {
			int storedGrains = getStoredGrains(stack);
			if (storedGrains + 20 < SandsOfTime.POUCH_CAPACITY.get()) {
				setStoredGrains(stack, storedGrains + 20);
			}
		}

		// remove time from any other TIAB items in the player's inventory
		// because this is relatively expensive, only do it every 10 seconds,
		// and only on bottles that have time stored in them
		if (level.getGameTime() % (20 * 10) == 0 && getStoredGrains(stack) != 0) {
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack invStack = player.getInventory().getItem(i);
				if (invStack.getItem() == this) {
					if (invStack != stack) {
						int otherTimeData = getStoredGrains(invStack);
						int myTimeData = getStoredGrains(stack);

						if (myTimeData < otherTimeData) {
							setStoredGrains(stack, 0);
						} else {
							setStoredGrains(invStack, 0);
						}
					}
				}
			}
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();

		if (level.isClientSide) {
			return InteractionResult.PASS;
		}

		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		Player player = ctx.getPlayer();

		// good lord this is a mouthful...
		var validBlockEntity = Optional.ofNullable(level.getBlockEntity(pos))
				.map(BlockEntity::getType)
				.flatMap(Registry.BLOCK_ENTITY_TYPE::getResourceKey)
				.flatMap(Registry.BLOCK_ENTITY_TYPE::getHolder)
				.filter(type -> !type.is(DO_NOT_ACCELERATE))
				.isPresent();

		var randomTickingState = level.getBlockState(pos).isRandomlyTicking();

		if (!(SandsOfTime.isLevelAllowed(level) && (validBlockEntity || SandsOfTime.ALLOW_RANDOM_TICKS.get() && randomTickingState))) {
			return InteractionResult.FAIL;
		}

		var baseDuration = 20 * SandsOfTime.DURATION_PER_USE.get();

		var accelerator = level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)).stream().findFirst().orElse(null);

		if (accelerator == null) {
			// First use needs to create a new accelerator
			if (shouldDamage(player, stack) && getStoredGrains(stack) < grainsRequired(1)) {
				return InteractionResult.SUCCESS;
			}

			accelerator = Objects.requireNonNull(EntityTypeRegistry.TIME_ACCELERATOR.get().create(level));
			accelerator.setPos(Vec3.atCenterOf(pos));
			accelerator.setTicksRemaining(baseDuration);
			level.addFreshEntity(accelerator);
		}

		int clicks = accelerator.getTimesAccelerated();
		if (clicks++ >= SandsOfTime.MAX_RATE.get() || shouldDamage(player, stack) && getStoredGrains(stack) < grainsRequired(clicks)) {
			return InteractionResult.SUCCESS;
		}

		accelerator.setTimesAccelerated(clicks);
		accelerator.setTicksRemaining((accelerator.getTicksRemaining() + baseDuration) / 2);

		if (shouldDamage(player, stack)) {
			setStoredGrains(stack, getStoredGrains(stack) - grainsRequired(clicks));
		}

		playNote(level, pos, clicks);

		return InteractionResult.SUCCESS;
	}

	public int grainsRequired(int level) {
		return (1 << Math.max(0, level - 1)) * SandsOfTime.GRAINS_USED.get();
	}

	private void playNote(Level level, BlockPos pos, int rate) {
		var pitches = new int[]{-6, -4, -2, -1, 1, 3, 5, 6};
		var pitch = (float) Math.pow(2.0D, (pitches[(rate - 1) % 8]) / 12.0D);
		level.playSound(null, pos, rate > 8 ? SoundEvents.NOTE_BLOCK_FLUTE : SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 3.0F, pitch);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(new TextComponent("(Note: This item is subject to change in the future,")
				.withStyle(GAGUtil.COLOUR_TRUE));
		tooltip.add(new TextComponent("currently it just works like a Time in A Bottle!)")
				.withStyle(GAGUtil.COLOUR_TRUE));

		GAGUtil.appendInfoTooltip(tooltip, List.of(
				new TranslatableComponent("item.gag.time_sand_pouch.info.1").withStyle(TOOLTIP_MAIN),
				new TranslatableComponent("item.gag.time_sand_pouch.info.2").withStyle(TOOLTIP_MAIN)
		));

		float hue = level == null ? 0.0F : level.getGameTime() % 1200F;
		//   "item.gag.time_sand_pouch.info.stored_grains": "Contains %1$s Grains of Time (worth %2$s)",
		tooltip.add(getTimeForDisplay(stack).withStyle(style -> style.withColor(Mth.hsvToRgb(hue / 1200F, 1.0F, 1.0F))));
	}

	@Override
	public boolean shouldBob(ItemStack oldStack, ItemStack newStack) {
		return false;
	}
}
