package ky.someone.mods.gag.misc;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record TeleportPos(ResourceLocation level, Vec3 pos, float yaw) {

	@Nullable
	public static TeleportPos fromNbt(@Nullable CompoundTag nbt) {
		if (nbt == null) return null;
		var level = new ResourceLocation(nbt.getString("dim"));
		var x = nbt.getDouble("x");
		var y = nbt.getDouble("y");
		var z = nbt.getDouble("z");
		return new TeleportPos(level, new Vec3(x, y, z), nbt.getFloat("yaw"));
	}

	public CompoundTag toNbt() {
		return Util.make(new CompoundTag(), nbt -> {
			nbt.putString("dim", level.toString());
			nbt.putDouble("x", pos.x);
			nbt.putDouble("y", pos.y);
			nbt.putDouble("z", pos.z);
			nbt.putFloat("yaw", yaw);
		});
	}

	@Nullable
	public ServerLevel getLevel(MinecraftServer server) {
		return server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, level));
	}
}
