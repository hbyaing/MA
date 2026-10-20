package cn.blockforge.generated.generatedmod;

import net.minecraft.network.chat.Component;

import java.util.List;

/** 神通目录：神通不占用普通功法槽位，单独显示在轮盘外圈。 */
public final class DivineArtCatalog {
    public record Entry(String id, int requiredRealm, int cooldownTicks, String itemId) {}

    public static final List<Entry> ENTRIES = List.of(
            new Entry("fa_tian_xiang_di", 3, 12000, "daode_jing_fragment"),
            new Entry("ta_gang_bu_dou", 2, 6000, "gangdou_starmap"),
            new Entry("yuan_shen_chu_qiao", 4, 6000, "zhonglv_fragment"),
            new Entry("yi_hun_ji_po", 5, 12000, "yin_fu_jing_yi_shen_fragment"),
            new Entry("qi_shi_er_bian", 5, 6000, "da_pin_tian_xian_jue_fragment_one"),
            new Entry("jin_dou_yun", 5, 6000, "da_pin_tian_xian_jue_fragment_two"),
            new Entry("ri_yue_ting_lun", 5, 12000, "ri_yue_ting_lun_fragment"),
            new Entry("tian_di_wei_lu", ProgressionGate.NASCENT_SOUL, 1200, "funiao_fu_fragment"));

    private DivineArtCatalog() {}

    public static Entry entry(String id) {
        return ENTRIES.stream().filter(value -> value.id().equals(id)).findFirst().orElse(null);
    }

    public static boolean isValid(String id) { return entry(id) != null; }
    public static int requiredRealm(String id) { Entry e = entry(id); return e == null ? 99 : e.requiredRealm(); }
    public static int cooldownTicks(String id) { Entry e = entry(id); return e == null ? 0 : e.cooldownTicks(); }
    public static String cooldownText(String id) {
        return String.format(java.util.Locale.ROOT, "%.1f秒", cooldownTicks(id) / 20.0F);
    }
    public static String costText(String id) {
        if ("yuan_shen_chu_qiao".equals(id)) return Component.translatable("tooltip.wuxingxiantu.divine_soul_cost",
                cn.blockforge.generated.generatedmod.balance.DivineArtCosts.activation(id),
                cn.blockforge.generated.generatedmod.balance.DivineArtCosts.perSecond(id),
                String.format(java.util.Locale.ROOT,"%.0f",cn.blockforge.generated.generatedmod.balance.DivineArtCosts.SOUL_DISTANCE_SCALE)).getString();
        return Component.translatable("tooltip.wuxingxiantu.divine_cost",
                cn.blockforge.generated.generatedmod.balance.DivineArtCosts.activation(id),
                cn.blockforge.generated.generatedmod.balance.DivineArtCosts.perSecond(id)).getString();
    }
    public static Component name(String id) { return Component.translatable("divine.wuxingxiantu." + id); }
    public static Component labeledName(String id) {
        return Component.literal("【神通】" + name(id).getString())
                .withStyle(style -> style.withColor(0xFFFF69C8));
    }
    public static Component function(String id) { return Component.translatable("divine.wuxingxiantu.function." + id,
            cn.blockforge.generated.generatedmod.balance.DivineArtCosts.activation(id),
            cn.blockforge.generated.generatedmod.balance.DivineArtCosts.perSecond(id),
            String.format(java.util.Locale.ROOT,"%.0f",cn.blockforge.generated.generatedmod.balance.DivineArtCosts.SOUL_DISTANCE_SCALE)); }
    public static String[] ids() { return ENTRIES.stream().map(Entry::id).toArray(String[]::new); }
}
