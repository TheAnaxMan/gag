package ky.someone.mods.gag.entity;

import dev.architectury.networking.NetworkManager;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.particle.ParticleTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TimeAcceleratorEntity extends Entity {

	public static final String TIMES_ACCELERATED = "timesAccelerated";
	public static final String TICKS_REMAINING = "ticksRemaining";

	private static final EntityDataAccessor<Integer> timesAccelerated = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ticksRemaining = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);

	public TimeAcceleratorEntity(EntityType<? extends TimeAcceleratorEntity> type, Level level) {
		super(type, level);
		entityData.set(timesAccelerated, 0);
		entityData.set(ticksRemaining, 0);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(timesAccelerated, 0);
		entityData.define(ticksRemaining, 0);
	}

	@Override
	@SuppressWarnings("unchecked") // it's a safe cast, calm down Java
	public void tick() {
		super.tick();

		var pos = blockPosition();
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		BlockState state = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);

		for (int i = 0; i < getSpeedMultiplier() - 1; i++) {
			if (be != null) {
				// if it's a BlockEntity, tick it
				var ticker = be.getBlockState().getTicker(level, (BlockEntityType<BlockEntity>) be.getType());
				if (ticker != null) {
					ticker.tick(level, pos, state, be);
				}
			} else if (state.isRandomlyTicking()) {
				// if it's a random ticking block, try to tick it
				if (level instanceof ServerLevel level) {
					// this might be 0 if people increase randomTickSpeed with cheats, so be careful
					var randomChance = GAGConfig.SandsOfTime.RANDOM_TICK_CHANCE.get() / level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
					if (randomChance == 0 || level.random.nextInt(randomChance) == 0) {
						state.randomTick(level, pos, level.random);
					}
				}
			} else {
				// block entity broken
				this.remove(RemovalReason.KILLED);
				break;
			}
		}

		var particleChance = (double) getTimesAccelerated() / GAGConfig.SandsOfTime.MAX_RATE.get() *
				(double) getTicksRemaining() / (GAGConfig.SandsOfTime.DURATION_PER_USE.get() * 20);

		if (random.nextDouble() < particleChance) {
			var magic = ParticleTypeRegistry.MAGIC.get();
			level.addParticle(magic, x, y + 0.05D + random.nextFloat(), z + random.nextFloat(), 0D, 0D, 0D);
			level.addParticle(magic, x + 1D, y + 0.05D + random.nextFloat(), z + random.nextFloat(), 0D, 0D, 0D);
			level.addParticle(magic, x + random.nextFloat(), y + 0.05D + random.nextFloat(), z, 0D, 0D, 0D);
			level.addParticle(magic, x + random.nextFloat(), y + 0.05D + random.nextFloat(), z + 1D, 0D, 0D, 0D);
			level.addParticle(magic, x + random.nextFloat(), y + 1.1D, z + random.nextFloat(), 0D, 0D, 0D);
			level.addParticle(magic, x + random.nextFloat(), y - 0.05D, z + random.nextFloat(), 0D, 0D, 0D);
		}

		if (getTicksRemaining() <= 0 && !level.isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

		setTicksRemaining(getTicksRemaining() - 1);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		entityData.set(timesAccelerated, compound.getInt(TIMES_ACCELERATED));
		entityData.set(ticksRemaining, compound.getInt(TICKS_REMAINING));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putInt(TIMES_ACCELERATED, getTimesAccelerated());
		compound.putInt(TICKS_REMAINING, getTicksRemaining());
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkManager.createAddEntityPacket(this);
	}

	public int getSpeedMultiplier() {
		return 1 << getTimesAccelerated();
	}

	public int getTimesAccelerated() {
		return entityData.get(timesAccelerated);
	}

	public void setTimesAccelerated(int rate) {
		entityData.set(timesAccelerated, rate);
	}

	public int getTicksRemaining() {
		return entityData.get(ticksRemaining);
	}

	public void setTicksRemaining(int ticks) {
		entityData.set(ticksRemaining, ticks);
	}

}
