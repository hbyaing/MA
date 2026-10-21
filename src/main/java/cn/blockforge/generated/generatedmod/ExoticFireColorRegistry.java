package cn.blockforge.generated.generatedmod;

import org.joml.Vector3f;

/** 火焰、物品与余焰共用的九阶配色和亮度配置。 */
public final class ExoticFireColorRegistry {
    private static final int[] LIGHT_LEVELS = {15, 15, 7, 12, 10, 15, 15, 0, 15};
    private static final String[] NAME_KEYS = {
            "fire.wuxingxiantu.red_flow", "fire.wuxingxiantu.molten_gold",
            "fire.wuxingxiantu.cold_bone", "fire.wuxingxiantu.azure_wood",
            "fire.wuxingxiantu.cobalt_sea", "fire.wuxingxiantu.violet_thunder",
            "fire.wuxingxiantu.sun_saint", "fire.wuxingxiantu.nether_void",
            "fire.wuxingxiantu.nirvana"
    };

    private ExoticFireColorRegistry() {}

    public static int clampTier(int tier) { return Math.max(1, Math.min(9, tier)); }

    public static int flintVisualTier(int tier) { return clampTier(tier); }

    public static int color(int tier, long gameTime) {
        int safeTier = clampTier(tier);
        return safeTier == 9 ? SpiritFirePalette.spectrum(gameTime) : SpiritFirePalette.base(safeTier);
    }

    public static int accentColor(int tier, long gameTime) {
        return switch (clampTier(tier)) {
            case 1 -> 0xFF9A28;
            case 2 -> 0xFFFFB8;
            case 3 -> 0xDCE3EB;
            case 4 -> 0xA0FFB0;
            case 5 -> 0xB8F2FF;
            case 6 -> 0xE0A0FF;
            case 7 -> 0xFFF3C4;
            case 8 -> 0x5A2A78;
            default -> SpiritFirePalette.spectrum(gameTime + 8L);
        };
    }

    public static int nextColor(int tier, long gameTime) { return color(tier, gameTime + 4L); }

    public static int lightLevel(int tier) { return LIGHT_LEVELS[clampTier(tier) - 1]; }

    public static String nameKey(int tier) { return NAME_KEYS[clampTier(tier) - 1]; }

    public static boolean isDark(int tier) { return clampTier(tier) == 8; }

    public static Vector3f vector(int color) {
        return new Vector3f((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }
}
