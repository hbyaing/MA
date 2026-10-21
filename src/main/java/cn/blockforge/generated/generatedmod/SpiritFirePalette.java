package cn.blockforge.generated.generatedmod;

/** 灵火调色公式；只替换色相和焰心，完整保留参考素材的透明轮廓与逐帧细节。 */
public final class SpiritFirePalette {
    private static final int[] BASE = {0xFF3C1E, 0xFFAA28, 0x9194AD, 0x3CC882, 0x32A0FF,
            0xAA50FF, 0xFFD34F, 0x280A3C, 0x40E0D0};
    private static final int[] CORE = {0xFFF0BB, 0xFFF6AC, 0xDCE3EB, 0xC2FFD8, 0xD1F6FF,
            0xEACDFF, 0xFFF3C4, 0x54256C, 0xF4FFFF};
    private static final int[] SPECTRUM = {0x40E0D0, 0xFF70B8, 0xB070FF, 0xFFD45C};

    private SpiritFirePalette() {}

    public static int base(int tier) { return BASE[Math.max(1, Math.min(9, tier)) - 1]; }

    /** 输入和输出均为 NativeImage 使用的 ABGR；透明度保持逐像素完全一致。 */
    public static int recolor(int abgr, int tier, int frame, int x, int y) {
        int alpha = abgr >>> 24;
        if (alpha == 0) return 0;
        int r = abgr & 255, g = abgr >>> 8 & 255, b = abgr >>> 16 & 255;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        if (tier == 0) return alpha << 24 | max << 16 | max << 8 | max;
        int safeTier = Math.max(1, Math.min(9, tier));
        int base = safeTier == 9 ? spectrum(frame + (15 - y) * 2 + x / 4) : base(safeTier);
        int core = safeTier == 9 ? mix(base, 0xFFFFFF, 0.8F) : CORE[safeTier - 1];
        float heat = max == 0 ? 0.0F : (float) Math.pow(min / (float) max, 1.5D);
        float brightness = 0.4F + 0.6F * max / 255.0F;
        if (safeTier == 6 && frame % 8 == 0) brightness = Math.min(1.0F, brightness + 0.16F);
        int rgb = mix(base, core, heat);
        r = Math.round((rgb >>> 16 & 255) * brightness);
        g = Math.round((rgb >>> 8 & 255) * brightness);
        b = Math.round((rgb & 255) * brightness);
        return alpha << 24 | b << 16 | g << 8 | r;
    }

    public static int spectrum(long time) {
        int phase = (int) Math.floorMod(time, 128L);
        int segment = phase / 32;
        float amount = (float) ((1.0D - Math.cos((phase % 32) / 32.0D * Math.PI)) * 0.5D);
        return mix(SPECTRUM[segment], SPECTRUM[(segment + 1) % 4], amount);
    }

    private static int mix(int a, int b, float amount) {
        int r = Math.round((a >>> 16 & 255) * (1 - amount) + (b >>> 16 & 255) * amount);
        int g = Math.round((a >>> 8 & 255) * (1 - amount) + (b >>> 8 & 255) * amount);
        int blue = Math.round((a & 255) * (1 - amount) + (b & 255) * amount);
        return r << 16 | g << 8 | blue;
    }
}
