package ky.someone.mods.gag.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import ky.someone.mods.gag.entity.FishingDynamiteEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class FishsplosionPacket extends BaseS2CMessage {

	public final Vec3 pos;

	public final float radius;

	public FishsplosionPacket(FriendlyByteBuf buf) {
		this.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		this.radius = buf.readFloat();
	}

	public FishsplosionPacket(FishingDynamiteEntity.Fishsplosion fishsplosion) {
		this.pos = fishsplosion.pos;
		this.radius = fishsplosion.radius;
	}

	@Override
	public MessageType getType() {
		return GAGNetwork.FISHSPLOSION;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeDouble(this.pos.x);
		buf.writeDouble(this.pos.y);
		buf.writeDouble(this.pos.z);
		buf.writeFloat(this.radius);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		var explosion = new FishingDynamiteEntity.Fishsplosion(context.getPlayer().level, null, this.pos.x, this.pos.y, this.pos.z, this.radius);
		explosion.finalizeExplosion(true);
	}
}
