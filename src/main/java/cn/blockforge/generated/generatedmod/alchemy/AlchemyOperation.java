package cn.blockforge.generated.generatedmod.alchemy;

import net.minecraft.world.item.ItemStack;

/** Optional manual operation; the original formula, failure roll and progression gates remain authoritative. */
public final class AlchemyOperation {
    public static final int CONTROL_BUTTON = 248;
    public static final int IDEAL_MIN = 350, IDEAL_MAX = 650;
    public static final String[] PHASE_NAMES = {"温炉", "淬药", "融药", "凝丹"};

    public static int phase(long elapsed, long duration) {
        if (duration <= 0) return 0;
        return Math.min(3, (int)(Math.max(0, Math.min(elapsed, duration)) * 4 / duration));
    }

    /** Four-second triangular cycle, based on powered ticks rather than wall time. */
    public static int heat(long elapsed) {
        int tick = (int)Math.floorMod(elapsed, 80L);
        return (tick <= 40 ? tick : 80 - tick) * 25;
    }

    public static boolean precise(int heat) { return heat >= IDEAL_MIN && heat <= IDEAL_MAX; }

    /** Three precise stages improve a successful pill by one quality, never its medicinal rank or quantity. */
    public static ItemStack finish(ItemStack rolled, int preciseMask) {
        ItemStack result = rolled.copy();
        var pill = PillData.read(result);
        if (Integer.bitCount(preciseMask & 15) < 3 || pill == null || pill.pill() == PillCatalog.CALMING
                || pill.grade() == PillData.Grade.PERFECT) return result;
        result = PillData.normalize(result);
        result.getOrCreateTag().putString("Grade", PillData.Grade.values()[pill.grade().ordinal() + 1].name());
        return result;
    }

    private AlchemyOperation() {}
}
