package cn.blockforge.generated.generatedmod.plants;

/**
 * 灵植野外稀有度。这个值只描述自然生成与探索难度，
 * 不代表药效强弱，也不构成任何“凡/灵/玄/天”式品阶。
 */
public enum WildRarity {
    COMMON(6),
    UNCOMMON(4),
    RARE(3),
    VERY_RARE(2),
    SPECIAL(2);

    /** Local legal-point search only; probability and quotas live in SpiritPlantQuota. */
    public final int attempts;
    WildRarity(int attempts) {
        this.attempts = attempts;
    }

    /** 语言文件键：wild.wuxingxiantu.common 等。 */
    public String translationKey() {
        return "wild.wuxingxiantu." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
