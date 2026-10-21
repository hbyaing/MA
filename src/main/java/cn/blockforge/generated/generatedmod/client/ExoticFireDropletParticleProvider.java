package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 水滴粒子工厂：沿用旧发射器的参数编码——xd 携带品阶（tier*0.01），zd 携带水滴尺寸，
 * yd 为上升速度，避免为此新增网络/存档格式。
 */
public final class ExoticFireDropletParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprites;

    public ExoticFireDropletParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z, double xd, double yd, double zd) {
        int tier = Math.max(1, Math.min(9, (int) Math.round(xd * 100.0D)));
        double size = Math.max(0.08D, Math.min(0.9D, zd));
        return new ExoticFireDropletParticle(level, x, y, z, yd, size, tier, sprites);
    }
}
