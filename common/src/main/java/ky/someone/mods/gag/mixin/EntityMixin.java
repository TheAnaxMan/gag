package ky.someone.mods.gag.mixin;

import ky.someone.mods.gag.item.EnergizedHearthstoneItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: fix lightning strike event in architectury!
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt, CallbackInfo ci) {
		//noinspection ConstantValue
		if ((Object) this instanceof ItemEntity item && EnergizedHearthstoneItem.lightningStrike(item)) {
			ci.cancel();
		}
	}
}
