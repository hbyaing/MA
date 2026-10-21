package cn.blockforge.generated.generatedmod.crafting;

import net.minecraft.world.item.ItemStack;

/** Optional manual finishing; recipe inputs, duration, fire gates and quantity remain authoritative. */
public final class RefiningOperation {
    public static final String[] STAGES = {"烧胚", "塑形", "锻打", "淬炼", "铭纹"};
    public static final String[] FINISHES = {"平常", "工整", "精良", "圆融"};
    private RefiningOperation() {}

    public static int stage(long start, long finish, long now) {
        if (finish <= start || now < start || now >= finish) return -1;
        return Math.min(4, (int) ((now - start) * 5.0D / (finish - start)));
    }

    /** The middle third of each stage is the precise operating window. */
    public static boolean precise(long start, long finish, long now) {
        if (stage(start, finish, now) < 0) return false;
        double part = (now - start) * 5.0D / (finish - start);
        double fraction = part - Math.floor(part);
        return fraction >= 1.0D / 3.0D && fraction <= 2.0D / 3.0D;
    }

    public static int quality(int preciseMask) {
        return Math.min(3, Integer.bitCount(preciseMask & 31) * 3 / 5);
    }

    public static ItemStack finish(ItemStack original, int attempts, int preciseMask) {
        ItemStack result = original.copy();
        if ((attempts & 31) == 0 || !EquipmentCrafting.eligible(result)) return result;
        var data = EquipmentCrafting.write(result);
        int good = Integer.bitCount(preciseMask & attempts & 31);
        data.putInt("ForgeFinishQuality", quality(preciseMask & attempts));
        // A bounded bonus reuses the target's existing stat authority; never add an extra reward.
        if (good >= 3) data.putInt("Refine", Math.min(9, EquipmentCrafting.refine(result) + 1));
        return result;
    }
}
