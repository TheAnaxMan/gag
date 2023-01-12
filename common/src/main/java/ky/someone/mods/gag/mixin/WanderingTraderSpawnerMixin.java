package ky.someone.mods.gag.mixin;

import ky.someone.mods.gag.block.NoSolicitorsSign;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(WanderingTraderSpawner.class)
public class WanderingTraderSpawnerMixin {

	@Inject(method = "spawn", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"
	), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void stopSpawn(ServerLevel serverLevel, CallbackInfoReturnable<Boolean> cir, Player _0, BlockPos _1, int _2, PoiManager _3, Optional<BlockPos> _4, BlockPos pos) {
		if (NoSolicitorsSign.blockWandererSpawn(serverLevel, pos)) {
			cir.setReturnValue(false);
		}
	}
}
