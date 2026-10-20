package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;

/** 使用灵火透明纹理的上升余焰；旧粒子ID保留以兼容存档和指令。 */
public final class CodeFireParticle extends TextureSheetParticle {
    public enum Kind { CORE, FLAME, EMBER }
    private final int tier;
    private final Kind kind;

    public CodeFireParticle(ClientLevel level, double x, double y, double z,
                            double yd, double zd, int tier, Kind kind, SpriteSet sprites) {
        super(level, x, y, z);
        this.tier = ExoticFireColorRegistry.clampTier(tier);
        this.kind = kind;
        pickSprite(sprites);
        this.xd = (random.nextDouble() - 0.5D) * 0.012D;
        this.yd = yd;
        this.zd = zd + (random.nextDouble() - 0.5D) * 0.012D;
        this.quadSize = kind == Kind.EMBER ? 0.045F : 0.09F;
        this.lifetime = 16 + random.nextInt(12);
        this.hasPhysics = false;
        this.friction = 0.98F;
        updateColor();
    }

    private void updateColor() {
        int color = kind == Kind.CORE || kind == Kind.EMBER
                ? ExoticFireColorRegistry.accentColor(tier, level.getGameTime() + age)
                : ExoticFireColorRegistry.color(tier, level.getGameTime() + age);
        setColor((color >>> 16 & 255) / 255.0F, (color >>> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    @Override
    public void tick() {
        super.tick();
        updateColor();
        alpha = Math.max(0.0F, 1.0F - age / (float) lifetime);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = (age + partialTick) / lifetime;
        return quadSize * Math.max(0.0F, 1.0F - life * life * 0.5F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return tier == 8 ? super.getLightColor(partialTick) : LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
