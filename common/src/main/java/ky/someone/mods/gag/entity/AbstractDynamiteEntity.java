package ky.someone.mods.gag.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractDynamiteEntity extends ThrowableItemProjectile {
    public AbstractDynamiteEntity(EntityType<? extends AbstractDynamiteEntity> type, Level level) {
        super(type, level);
    }

    public AbstractDynamiteEntity(EntityType<? extends AbstractDynamiteEntity> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public AbstractDynamiteEntity(EntityType<? extends AbstractDynamiteEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide && shouldExplode(hitResult)) {
            this.detonate(hitResult.getLocation());
            this.discard();
        }
    }

    // controls automatic detonation on hitting something
    protected boolean shouldExplode(HitResult hitResult) {
        return true;
    }

    public abstract void detonate(Vec3 pos);
}
