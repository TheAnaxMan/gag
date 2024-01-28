package ky.someone.mods.gag.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class MagicParticle extends TextureSheetParticle {

	final float origSize;

	public MagicParticle(ClientLevel worldIn, double x, double y, double z, SpriteSet sprite) {
		super(worldIn, x, y, z, 0, 0, 0);
		this.setColor(1, 1, 1);
		this.xd *= 0.05F;
		this.yd -= 0.5F;
		this.yd *= 0.05F;
		this.zd *= 0.05F;
		this.lifetime = 20;
		this.origSize = this.quadSize /= 2;
		this.pickSprite(sprite);
	}

	@Override
	public void tick() {
		super.tick();
		this.xd *= 0.65F;
		this.yd *= 0.65F;
		this.zd *= 0.65F;
		if (random.nextInt(4) == 0) {
			this.age--;
		}
		this.quadSize = (1 - ((float) this.age / this.lifetime)) * origSize;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public boolean isAlive() {
		return this.age < this.lifetime;
	}

	@Override
	protected int getLightColor(float f) {
		return 0xf000f0;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteProvider;

		public Provider(SpriteSet spriteSet) {
			this.spriteProvider = spriteSet;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			MagicParticle particle = new MagicParticle(clientLevel, d, e, f, this.spriteProvider);
			particle.setAlpha(1.0F);
			return particle;
		}
	}
}
