package cn.blockforge.generated.generatedmod;

import java.util.Locale;

/** 文档定义的八种体质；万灵道体只在飞升后自然获得。 */
public enum PhysiqueType {
    TAICHU("太初凡体"),
    QINGLIAN("青莲灵体"),
    XUANHUANG("玄黄圣体"),
    JIUQIAO("九窍灵脉"),
    ZHANWANG("战王圣体"),
    TAIYIN("太阴寒体"),
    TAIYANG("太阳炎体"),
    WANLING("万灵道体");

    private final String displayName;
    PhysiqueType(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
    public String id() { return name().toLowerCase(Locale.ROOT); }
    public static PhysiqueType fromId(String id) {
        for (PhysiqueType type : values()) if (type.id().equals(id)) return type;
        return TAICHU;
    }
}
