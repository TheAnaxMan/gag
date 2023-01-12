package ky.someone.mods.gag.world;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GAGPointOfInterestStorage extends SavedData {

	private static final String FILE_NAME = "gag_poi_info";

	private final Map<BlockPos, Block> pois = new HashMap<>();

	public static GAGPointOfInterestStorage get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(GAGPointOfInterestStorage::load, GAGPointOfInterestStorage::new, FILE_NAME);
	}

	public Block add(BlockPos pos, Block block) {
		setDirty();
		return pois.put(pos, block);
	}

	public boolean removeIfPresent(BlockPos pos) {
		setDirty();
		return pois.remove(pos) != null;
	}

	public Optional<BlockPos> checkNearbyPOIs(Block block, BlockPos pos, int radius) {
		for (var poi : pois.entrySet()) {
			if (poi.getValue() == block && poi.getKey().distSqr(pos) < radius * radius) {
				return Optional.of(poi.getKey());
			}
		}
		return Optional.empty();
	}

	public static GAGPointOfInterestStorage load(CompoundTag nbt) {
		var storage = new GAGPointOfInterestStorage();
		if (nbt.contains("pois")) {
			ListTag list = nbt.getList("pois", Tag.TAG_COMPOUND);
			for (var tag : list) {
				var poiTag = (CompoundTag) tag;
				var block = Registry.BLOCK.get(ResourceLocation.tryParse(poiTag.getString("block")));
				var pos = BlockPos.of(poiTag.getLong("pos"));
				storage.pois.put(pos, block);
			}
		}
		return storage;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		ListTag list = new ListTag();
		for (var poi : pois.entrySet()) {
			list.add(Util.make(new CompoundTag(), (tag) -> {
				tag.putString("block", Registry.BLOCK.getKey(poi.getValue()).toString());
				tag.putLong("pos", poi.getKey().asLong());
			}));
		}
		compoundTag.put("pois", list);
		return compoundTag;
	}
}
