package ky.someone.mods.gag.block;

import dev.architectury.event.EventResult;
import ky.someone.mods.gag.GAG;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.world.GAGPointOfInterestStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ky.someone.mods.gag.GAGUtil.TOOLTIP_MAIN;
import static ky.someone.mods.gag.GAGUtil.TOOLTIP_SIDENOTE;

public class NoSolicitorsSign extends Block {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty SILENT = BooleanProperty.create("silent");

	private static final Map<Direction, VoxelShape> AABBS = new EnumMap<>(Map.of(
			Direction.NORTH, Shapes.or(Block.box(7, 5, 12, 9, 9, 14),
					Block.box(4, 4, 13, 12, 14, 15),
					Block.box(1, 1, 15, 15, 15, 16),
					Block.box(0, 15, 14, 16, 16, 16),
					Block.box(0, 0, 14, 16, 1, 16),
					Block.box(15, 1, 14, 16, 15, 16),
					Block.box(0, 1, 14, 1, 15, 16)),
			Direction.SOUTH, Shapes.or(Block.box(7, 5, 2, 9, 9, 4),
					Block.box(4, 4, 1, 12, 14, 3),
					Block.box(1, 1, 0, 15, 15, 1),
					Block.box(0, 15, 0, 16, 16, 2),
					Block.box(0, 0, 0, 16, 1, 2),
					Block.box(0, 1, 0, 1, 15, 2),
					Block.box(15, 1, 0, 16, 15, 2)),
			Direction.EAST, Shapes.or(Block.box(2, 5, 7, 4, 9, 9),
					Block.box(1, 4, 4, 3, 14, 12),
					Block.box(0, 1, 1, 1, 15, 15),
					Block.box(0, 15, 0, 2, 16, 16),
					Block.box(0, 0, 0, 2, 1, 16),
					Block.box(0, 1, 15, 2, 15, 16),
					Block.box(0, 1, 0, 2, 15, 1)),
			Direction.WEST, Shapes.or(Block.box(12, 5, 7, 14, 9, 9),
					Block.box(13, 4, 4, 15, 14, 12),
					Block.box(15, 1, 1, 16, 15, 15),
					Block.box(14, 15, 0, 16, 16, 16),
					Block.box(14, 0, 0, 16, 1, 16),
					Block.box(14, 1, 0, 16, 15, 1),
					Block.box(14, 1, 15, 16, 15, 16))
	));

	public NoSolicitorsSign() {
		super(BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).noCollission().strength(4.0F));
		this.registerDefaultState(this.stateDefinition.any().setValue(SILENT, false)
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SILENT, FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return AABBS.get(state.getValue(FACING));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
		GAGUtil.appendInfoTooltip(tooltip, List.of(
				new TranslatableComponent("block.gag.no_solicitors.info.1").withStyle(TOOLTIP_MAIN),
				new TranslatableComponent("block.gag.no_solicitors.info.2").withStyle(TOOLTIP_SIDENOTE)
		));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getMaterial().isSolid();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var state = this.defaultBlockState();
		var fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
		var level = ctx.getLevel();
		var pos = ctx.getClickedPos();

		for (var direction : ctx.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				state = state.setValue(FACING, direction.getOpposite());
				if (state.canSurvive(level, pos)) {
					return state.setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
				}
			}
		}

		return null;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos) {
		if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, newState, level, pos, newPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player.getItemInHand(hand).is(ItemTags.WOOL)) {
			state = state.cycle(SILENT);
			level.setBlockAndUpdate(pos, state);
			level.playSound(null, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 0.2F, 0.7F);
			player.displayClientMessage(new TranslatableComponent("block.gag.no_solicitors.silent",
					GAGUtil.styledBool(state.getValue(SILENT))), true);
			return InteractionResult.sidedSuccess(level.isClientSide());
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			GAGPointOfInterestStorage.get(serverLevel).removeIfPresent(pos);
		}
		super.onRemove(state, level, pos, newState, bl);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		if (level instanceof ServerLevel serverLevel) {
			GAGPointOfInterestStorage.get(serverLevel).add(pos, this);
		}
		super.setPlacedBy(level, pos, state, entity, stack);
	}

	public static EventResult notBuyingYourStuff(Entity entity, Level level) {
		if (entity.getType() == EntityType.WANDERING_TRADER && level instanceof ServerLevel serverLevel && blockWandererSpawn(serverLevel, entity.blockPosition())) {
			return EventResult.interruptFalse();
		}
		return EventResult.pass();
	}

	public static boolean blockWandererSpawn(ServerLevel serverLevel, BlockPos pos) {
		var ward = GAGPointOfInterestStorage.get(serverLevel)
				.checkNearbyPOIs(BlockRegistry.NO_SOLICITORS_SIGN.get(), pos, GAGConfig.Miscellaneous.NO_SOLICITORS_RADIUS.get());

		GAG.LOGGER.debug("Wanderer spawn check at {} returned {}", pos, ward.isPresent());

		if (ward.isPresent()) {
			var wardPos = ward.get();

			// shouldn't be necessary, but just in case someone decides to spawn a worldgen structure with
			// a wandering trader *and* a no solicitors sign in it, getBlockState *might* deadlock
			if (!serverLevel.isLoaded(wardPos)) return false;
			var state = serverLevel.getBlockState(wardPos);

			if (state.getBlock() != BlockRegistry.NO_SOLICITORS_SIGN.get()) {
				GAG.LOGGER.warn("No Solicitors Sign at {} does not exist, has it been removed?", wardPos);
				GAGPointOfInterestStorage.get(serverLevel).removeIfPresent(wardPos);
				return false;
			}

			var silent = state.getValue(SILENT);
			if (!state.getValue(NoSolicitorsSign.SILENT)) {
				serverLevel.playSound(null, pos, SoundEvents.WANDERING_TRADER_DEATH, SoundSource.BLOCKS, 0.2F, 0.7F);
				double d = serverLevel.random.nextGaussian() * 0.25;
				double e = serverLevel.random.nextGaussian() * 0.5;
				double f = serverLevel.random.nextGaussian() * 0.25;
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + d, pos.getY() + e, pos.getZ() + f, 5, d, e, f, 0.01);
			}

			return true;
		}

		return false;
	}
}
