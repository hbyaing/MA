package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public final class HeartSwordParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprites;
    public HeartSwordParticleProvider(SpriteSet sprites) { this.sprites = sprites; }
    @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                             double xd, double yd, double zd) {
        boolean flame = type == cn.blockforge.generated.generatedmod.ModParticles.HEART_FLAME.get();
        return new HeartSwordParticle(level, x, y, z, flame ? 1 : xd, yd, zd, sprites);
    }
}
