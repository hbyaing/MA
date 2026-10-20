package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/** 兼容旧粒子品阶编码，改用标准透明粒子图集。 */
public final class CodeFireParticleProvider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
    private final CodeFireParticle.Kind kind;
    private final SpriteSet sprites;

    public CodeFireParticleProvider(SpriteSet sprites, CodeFireParticle.Kind kind) {
        this.sprites = sprites;
        this.kind = kind;
    }

    @Override
    public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                   double xd, double yd, double zd) {
        int tier = Math.max(1, Math.min(9, (int) Math.round(xd * 100.0D)));
        return new CodeFireParticle(level, x, y, z, yd, zd, tier, kind, sprites);
    }
}
