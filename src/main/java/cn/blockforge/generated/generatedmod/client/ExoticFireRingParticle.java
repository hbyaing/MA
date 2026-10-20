package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 异火缠绕环形水滴：锁定主人，沿"倾斜环面"轨迹绕身体旋转。
 * 默认环面绕水平轴倾斜 45°（tilt），从正面看向 YZ 竖直面投影时呈现一条与地面成 45° 的线；
 * 缠绕等级更高时另一条环取 -45°（135°）。整个环面还可绕竖直轴缓慢自转（spinOmega>0），
 * 让环形轨迹沿身材中心旋转。环大致位于身体中段、半径在身体轮廓之外，单颗尺寸小、整圈稀疏，
 * 沿用 exotic_fire_droplet 水滴贴图与品阶染色。
 */
public final class ExoticFireRingParticle extends TextureSheetParticle {
    private static SpriteSet sprites;

    /** 由粒子注册处注入水滴图集（环形粒子是客户端直接构造的，不走 Provider 参数编码）。 */
    public static void initSprites(SpriteSet set) {
        sprites = set;
    }

    public static SpriteSet sprites() {
        return sprites;
    }

    private final int ownerId;
    private final double phaseOffset;
    private final double radius;
    private final double heightRatio;
    private final double omega;
    /** 环面相对水平面的倾角（弧度）；45° 单环，第二环取 -45°。 */
    private final double tilt;
    /** 环面绕竖直轴自转的角速度；<=0 表示不自转。 */
    private final double spinOmega;
    private final int tier;
    private final float baseSize;
    private final long generation;

    ExoticFireRingParticle(ClientLevel level, int ownerId, double phaseOffset, double radius,
                           double heightRatio, double omega, double tilt, double spinOmega,
                           double size, int tier, int initialAge, int lifetime, long generation) {
        super(level, 0.0D, 0.0D, 0.0D);
        this.ownerId = ownerId;
        this.phaseOffset = phaseOffset;
        this.radius = radius;
        this.heightRatio = heightRatio;
        this.omega = omega;
        this.tilt = tilt;
        this.spinOmega = spinOmega;
        this.tier = ExoticFireColorRegistry.clampTier(tier);
        this.generation = generation;
        this.baseSize = (float) size;
        this.quadSize = this.baseSize;
        this.age = initialAge;
        this.lifetime = lifetime;
        this.pickSprite(sprites);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.alpha = 0.0F;
        placeOnRing();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        updateColor();
    }

    /**
     * 把固定槽相位摆到"倾斜环面"上。先按全局时间推进角度，再把圆环点绕水平轴倾斜 tilt
     * （得到 {@code (cos(a)*r, sin(a)*r*sin(tilt), sin(a)*r*cos(tilt))}，即与地面成 tilt 角的
     * 斜环），随后绕竖直轴旋转到主人的水平朝向上，使"从玩家正面看"始终是一条与地面成 tilt 角的
     * 斜线；等级 4 时叠加环面绕竖直轴的自转。各槽位间永远等角距，不会挤成一团。
     */
    private void placeOnRing() {
        Entity owner = level.getEntity(ownerId);
        if (owner == null) return;
        double angle = phaseOffset + (double) level.getGameTime() * omega;
        double bob = Math.sin(age * 0.07D + phaseOffset) * 0.05D;
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        // 斜环：先绕水平轴倾斜（y 轴分量由 sin(tilt) 决定，z 轴分量由 cos(tilt) 决定）。
        double x0 = cosA * radius;
        double y0 = sinA * radius * Math.sin(tilt);
        double z0 = sinA * radius * Math.cos(tilt);
        // 基础朝向跟随主人的水平朝向，使"从玩家正面看"时环始终投影成一条与地面成 tilt 角的斜线；
        // 等级 4 时在此基础上叠加绕竖直轴的自转（spinCos/spinSin）。
        double spinYaw = spinOmega * (double) level.getGameTime();
        double yawTotal = Math.toRadians(owner.getYRot()) + spinYaw;
        double cosY = Math.cos(yawTotal);
        double sinY = Math.sin(yawTotal);
        this.x = owner.getX() + x0 * cosY + z0 * sinY;
        this.z = owner.getZ() - x0 * sinY + z0 * cosY;
        this.y = owner.getY() + owner.getBbHeight() * heightRatio + y0 + bob;
    }

    private void updateColor() {
        int color = ExoticFireColorRegistry.color(tier, level.getGameTime() + age);
        setColor((color >>> 16 & 255) / 255.0F, (color >>> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    @Override
    public void tick() {
        Entity owner = level.getEntity(ownerId);
        if (!(owner instanceof Player player) || !player.isAlive() || player.isSpectator()
                || !ExoticFireOrbitRenderer.isWrapping(ownerId, generation)) {
            remove();
            return;
        }
        super.tick();
        if (removed) return;
        placeOnRing();
        updateColor();
        // 入场渐显、离场渐隐，避免环上出现突兀的闪烁。
        float fadeIn = Math.min(1.0F, age / 12.0F);
        float fadeOut = Math.min(1.0F, (lifetime - age) / 25.0F);
        alpha = 0.85F * Math.min(fadeIn, fadeOut);
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.VertexConsumer vertices, net.minecraft.client.Camera camera, float partialTick) {
        if (cn.blockforge.generated.generatedmod.client.firecoil.Client.visualsEnabled()) super.render(vertices, camera, partialTick);
    }

    @Override
    public float getQuadSize(float partialTick) {
        // 呼吸式微幅脉动，幅度只有 ±6%，不放大整体观感。
        return baseSize * (1.0F + 0.06F * Mth.sin((age + partialTick) * 0.25F));
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
