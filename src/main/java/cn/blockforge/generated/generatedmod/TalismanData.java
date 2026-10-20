package cn.blockforge.generated.generatedmod;

import net.minecraft.world.item.ItemStack;

/**
 * 符箓相关物品的 NBT 读写助手。
 * <p>
 * 印章（Seal）与符咒（Talisman）都通过 {@code TalismanId} 记录当前携带的符箓种类；
 * 印章可反复使用，符咒使用一次即消耗。
 */
public final class TalismanData {
    public static final String KEY = "TalismanId";
    /** 品级：保存 {@link TalismanCatalog.Quality} 的 ordinal（-1 表示未记录）。 */
    public static final String QUALITY_KEY = "TalismanQuality";

    private TalismanData() {}

    public static void setTalisman(ItemStack stack, String id) {
        stack.getOrCreateTag().putString(KEY, id);
    }

    public static String getTalisman(ItemStack stack) {
        if(stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.TalismanItem item
                && !item.talismanId().isEmpty())return item.talismanId();
        if (stack.isEmpty() || !stack.hasTag()) return "";
        return stack.getTag().getString(KEY);
    }

    /** 写入品级（把 {@link TalismanCatalog.Quality} 存为 ordinal）。 */
    public static void setQuality(ItemStack stack, TalismanCatalog.Quality quality) {
        stack.getOrCreateTag().putInt(QUALITY_KEY, quality == null ? -1 : quality.ordinal());
    }

    /** 读取品级 ordinal；未记录时返回 -1。 */
    public static int getQuality(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(QUALITY_KEY,net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)) return -1;
        return stack.getTag().getInt(QUALITY_KEY);
    }

    /**
     * 把 ordinal 还原成品级；已有符箓种类但未记录品级时按新获得的极品处理，
     * 只有空白或未知种类才返回 null。
     */
    public static TalismanCatalog.Quality quality(ItemStack stack) {
        int ordinal = getQuality(stack);
        /*
         * A registered talisman obtained without an explicit quality is a
         * complete item, so it starts at the highest grade.  Crafted/drawn
         * and traded stacks still carry their rolled quality in NBT and are
         * therefore allowed to vary independently.
         */
        if (ordinal < 0 && TalismanCatalog.has(getTalisman(stack))) {
            return TalismanCatalog.Quality.PERFECT;
        }
        if (ordinal < 0) return null;
        TalismanCatalog.Quality[] values = TalismanCatalog.Quality.values();
        return ordinal < values.length ? values[ordinal] : null;
    }

    /** 印章是否已经镌刻了某种符箓符文。 */
    public static boolean hasTalisman(ItemStack stack) {
        String id = getTalisman(stack);
        return !id.isEmpty() && TalismanCatalog.has(id);
    }
    public static ItemStack create(String id,int count,TalismanCatalog.Quality quality){
        // Old engraved shields/healing seals can still produce their saved kind.
        boolean current=TalismanCatalog.ids().contains(id);
        if(!TalismanCatalog.has(id))throw new IllegalArgumentException("Unknown talisman: "+id);
        ItemStack stack=new ItemStack(current?WuXingItems.talismanItem(id):WuXingItems.TALISMAN.get(),count);
        if(!current)setTalisman(stack,id);
        setQuality(stack,quality==null?TalismanCatalog.Quality.PERFECT:quality);
        return stack;
    }
}
