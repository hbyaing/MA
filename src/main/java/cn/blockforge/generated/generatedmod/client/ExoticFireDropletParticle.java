package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;

/**
 * 异火缠绕水滴粒子：使用 exotic_fire_droplet 实心水滴贴图（尖朝上、圆底朝下），
 * 按装备异火品阶动态染色，缓慢上升、轻微摆动并渐隐，取代旧版用异火方块贴图绘制的火苗四边形。
 */
public final class ExoticFireDropletParticle extends TextureSheetParticle {
    private final int tier;
    private final float baseSize;

    ExoticFireDropletParticle(ClientLevel level, double x, double y, double z,
                              double rise, double size, int tier, SpriteSet sprites) {
        super(level, x, y, z);
        this.tier = ExoticFireColorRegistry.clampTier(tier);
        this.baseSize = (float) size;
        this.quadSize = this.baseSize;
        this.pickSprite(sprites);
        this.xd = (this.random.nextDouble() - 0.5D) * 0.006D;
        this.yd = rise;
        this.zd = (this.random.nextDouble() - 0.5D) * 0.006D;
        this.lifetime = 14 + this.random.nextInt(9);
        this.hasPhysics = false;
        this.friction = 0.985F;
        this.gravity = 0.0F;
        updateColor();
    }

    private void updateColor() {
        int color = ExoticFireColorRegistry.color(tier, level.getGameTime() + age);
        setColor((color >>> 16 & 255) / 255.0F, (color >>> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    @Override
    public void tick() {
        super.tick();
        updateColor();
        float life = age / (float) lifetime;
        // 前 1/4 生命保持高亮，之后线性渐隐；尺寸在 getQuadSize 里同步收缩。
        alpha = life < 0.25F ? 0.95F : Math.max(0.0F, 0.95F * (1.0F - (life - 0.25F) / 0.75F));
        // 上升途中轻微摆动，模拟火苗的跳动。
        xd += (random.nextDouble() - 0.5D) * 0.0006D;
        zd += (random.nextDouble() - 0.5D) * 0.0006D;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = (age + partialTick) / lifetime;
        return baseSize * (1.0F - life * life * 0.55F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
