package cn.blockforge.generated.generatedmod.alchemy;

import java.util.Arrays;

/** One entry per pill type; quality and elemental variants belong to ItemStacks. */
public enum PillCatalog {
    AURA("aura_pill", Source.BASIC, 0, false),
    GOLDEN_WOUND("golden_wound_pill", Source.BASIC, 0, false),
    BIGU("bigu_pill", Source.BASIC, 0, false),
    CLEAR_HEART("clear_heart_pill", Source.BASIC, 0, false),
    GATHERING("gathering_pill", Source.BASIC, 0, true),
    FOUNDATION("foundation_pill", Source.BASIC, 0, true),
    CONTINUE_VEIN("continue_vein_pill", Source.EXPLORATION, 1, false),
    DIAMOND_BODY("diamond_body_pill", Source.EXPLORATION, 1, true),
    BERSERK("berserk_pill", Source.EXPLORATION, 1, true),
    GALE("gale_pill", Source.EXPLORATION, 1, true),
    PROFOUND_WATER("profound_water_pill", Source.EXPLORATION, 1, true),
    CULTIVATION("cultivation_pill", Source.PROGRESSION, 0, true),
    BREAKTHROUGH("breakthrough_pill", Source.PROGRESSION, 0, true),
    TRIBULATION("tribulation_pill", Source.PROGRESSION, 0, true),
    CALMING("calming_pill", Source.PROGRESSION, 2, true),
    ROOT("root_awakening_pill", Source.ROOT_KNOWLEDGE, 0, true),
    PHYSIQUE("physique_reset_pill", Source.DRAGON, 3, true),
    GATHER_QI("gather_qi_pill", Source.BASIC, 0, false),
    ORIGIN_CONDENSING("origin_condensing_pill", Source.PROGRESSION, 0, true),
    MERIDIAN_EXPANSION("meridian_expansion_pill", Source.PROGRESSION, 0, true),
    BARRIER_PREPARATION("barrier_preparation_pill", Source.PROGRESSION, 0, true),
    REJUVENATION("rejuvenation_pill", Source.BASIC, 0, false);

    public enum Source { BASIC, EXPLORATION, PROGRESSION, ROOT_KNOWLEDGE, DRAGON }
    public final String id;
    public final Source source;
    public final int minimumRealm;
    public final boolean progression;

    PillCatalog(String id, Source source, int minimumRealm, boolean progression) {
        this.id = id;
        this.source = source;
        this.minimumRealm = minimumRealm;
        this.progression = progression;
    }

    public static PillCatalog byId(String id) {
        String path = id.startsWith("wuxingxiantu:") ? id.substring(13) : id;
        return Arrays.stream(values()).filter(pill -> pill.id.equals(path)).findFirst().orElse(null);
    }
}
