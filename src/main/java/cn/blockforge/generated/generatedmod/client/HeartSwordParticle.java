package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;

/** 心剑动作使用的模组自有粒子：小尺寸、短寿命、全亮度，不调用原版粒子。 */
public final class HeartSwordParticle extends TextureSheetParticle {
    private final boolean forge;
    HeartSwordParticle(ClientLevel level, double x, double y, double z,
                       double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        forge = xd > 0.5D;
        pickSprite(sprites);
        this.xd = (random.nextDouble() - 0.5D) * 0.018D + zd * 0.25D;
        this.yd = yd;
        this.zd = (random.nextDouble() - 0.5D) * 0.018D;
        this.quadSize = forge ? 0.045F : 0.055F;
        this.lifetime = forge ? 8 + random.nextInt(5) : 10 + random.nextInt(6);
        this.hasPhysics = false;
        setColor(forge ? 0.24F : 0.82F, forge ? 0.62F : 1.0F, 1.0F);
    }
    @Override public void tick() { super.tick(); alpha = Math.max(0.0F, 1.0F - age / (float) lifetime); }
    @Override public int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
