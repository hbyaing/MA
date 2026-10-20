package cn.blockforge.generated.generatedmod;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * 旧异火方块 API 的兼容桥。异火现在标记原版 FireBlock，视觉由动态粒子叠加。
 */
public final class EternalSpiritFireBlock {
    private EternalSpiritFireBlock() {}

    public static int temperatureForTier(int tier) {
        return 300 + ExoticFireColorRegistry.clampTier(tier) * 100;
    }

    public static int color(int tier, long gameTime) {
        return ExoticFireColorRegistry.color(tier, gameTime);
    }

    public static DustParticleOptions exoticFireDust(int color, float scale) {
        int safeColor = color & 0xFFFFFF;
        return new DustParticleOptions(new Vector3f(
                ((safeColor >> 16) & 255) / 255.0F,
                ((safeColor >> 8) & 255) / 255.0F,
                (safeColor & 255) / 255.0F), Mth.clamp(scale, 0.1F, 4.0F));
    }
}
