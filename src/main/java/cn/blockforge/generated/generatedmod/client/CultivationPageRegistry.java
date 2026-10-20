package cn.blockforge.generated.generatedmod.client;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** 附属可注册页面及可见条件；未实现的任务、创世不生成占位入口。 */
public final class CultivationPageRegistry {
    public record Entry(String id, String title, Item icon, int order, BooleanSupplier visible,
                        Supplier<? extends CultivationPage> factory) {}
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    static {
        register(new Entry("inventory", "背包", Items.CHEST, 0, () -> true, null));
        register(new Entry("vision", "内视", Items.ENDER_EYE, 10, () -> true, CultivationVisionPage::new));
        register(new Entry("arts", "功法", Items.ENCHANTED_BOOK, 20, () -> true, CultivationArtsPage::new));
        register(new Entry("sword", "灵剑", Items.IRON_SWORD, 30, () -> true, CultivationSwordPage::new));
        register(new Entry("crafts", "百艺", Items.CRAFTING_TABLE, 34, () -> true, () -> new PlayerProgressionPage(false)));
        register(new Entry("sense", "识海", Items.ENDER_EYE, 36, () -> true, () -> new PlayerProgressionPage(true)));
        register(new Entry("advancements", "成就", Items.GOLD_INGOT, 40, () -> true, CultivationAdvancementsPage::new));
        register(new Entry("settings", "设置", Items.COMPARATOR, 50, () -> true, CultivationSettingsPage::new));
    }
    private CultivationPageRegistry() {}
    public static void register(Entry entry) {
        Objects.requireNonNull(entry);
        if (ENTRIES.putIfAbsent(entry.id(), entry) != null) throw new IllegalArgumentException("重复页面：" + entry.id());
    }
    public static List<Entry> visible() {
        return ENTRIES.values().stream().filter(e -> e.visible().getAsBoolean())
                .sorted(Comparator.comparingInt(Entry::order)).toList();
    }
}
