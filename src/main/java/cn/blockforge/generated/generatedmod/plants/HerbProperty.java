package cn.blockforge.generated.generatedmod.plants;

import java.util.Locale;

/**
 * 灵植药性。第三批炼丹、异火药力等系统按这里的枚举读取数据，
 * 不依赖逐物品 if-else。语言键：herb.wuxingxiantu.&lt;id&gt;。
 */
public enum HerbProperty {
    METAL("jin"),        // 金
    SPIRIT("ling"),      // 灵
    WOOD("mu"),          // 木
    WATER("shui"),       // 水
    FIRE("huo"),         // 火
    EARTH("tu"),         // 土
    WIND("feng"),        // 风
    THUNDER("lei"),      // 雷
    YIN("yin"),          // 阴
    YANG("yang"),        // 阳
    COLD("han"),         // 寒
    BLOOD("xue"),        // 血
    DRAGON("long"),      // 龙
    STAR("xing"),        // 星
    GHOST("you"),        // 幽
    HARMONY("tiaohe"),   // 调和
    NOURISH("ziyang"),   // 滋养
    RECOVERY("huifu"),   // 恢复
    CLEANSE("qingjing"), // 清净
    SOUL("shenhun"),     // 神魂
    GATHER_QI("juqi"),   // 聚气
    CULTIVATION("xiulian"), // 修炼
    VITALITY("shengji"),    // 生机
    MERIDIAN("jingmai"),    // 经脉
    QI_BLOOD("qixue"),      // 气血
    BODY_REFINE("lianti"),  // 炼体
    TOUGHNESS("jianren"),   // 坚韧
    MOVEMENT("shenfa"),     // 身法
    TEMPER("cuilian"),      // 淬炼
    VENOM("du"),           // 来源毒鸩草：毒
    ILLUSION("huan"),      // 来源幻灵草：幻
    FORTUNE("yun"),        // 来源天机花：运
    ABUNDANCE("fengrao");  // 来源丰灵草、灵稻：丰

    private final String id;

    HerbProperty(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "herb.wuxingxiantu." + id;
    }

    public static HerbProperty byId(String id) {
        for (HerbProperty p : values()) {
            if (p.id.equals(id.toLowerCase(Locale.ROOT))) return p;
        }
        throw new IllegalArgumentException("未知药性: " + id);
    }
}
