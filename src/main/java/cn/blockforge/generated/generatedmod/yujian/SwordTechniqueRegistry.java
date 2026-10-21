package cn.blockforge.generated.generatedmod.yujian;

import java.util.*;

/** 剑技固定注册表；学习条件由剑品阶、剑蕴阶段和洞察点共同决定。 */
public final class SwordTechniqueRegistry {
    public record Technique(String id, String name, String description, int requiredTier, int requiredStage,
                            int insightCost, int maxShadows) {}
    private static final Map<String, Technique> VALUES = new LinkedHashMap<>();
    static {
        register(new Technique("art_sword_pierce", "飞星贯日", "远程单体追击与贯穿", 0, 0, 1, 1));
        register(new Technique("art_sword_sweep", "回风环刃", "近身范围环斩", 1, 0, 1, 6));
        register(new Technique("art_sword_guard", "六御金罡", "防御剑阵", 1, 1, 2, 6));
        register(new Technique("art_sword_array", "太虚落剑", "区域连续落剑", 2, 2, 3, 6));
        register(new Technique("art_myriad_swords", "万剑诀", "顶级多目标剑阵", 5, 4, 5, 12));
    }
    private SwordTechniqueRegistry() {}
    public static void register(Technique technique) { VALUES.put(technique.id(), technique); }
    public static Technique get(String id) { return VALUES.get(id); }
    public static boolean isTechnique(String id) { return id != null && VALUES.containsKey(id); }
    public static Collection<Technique> all() { return List.copyOf(VALUES.values()); }
    public static boolean canLearn(Technique t, SwordProfile profile, NurtureData data) {
        return supports(t, profile, data) && data.insightPoints >= t.insightCost();
    }
    public static boolean supports(Technique t, SwordProfile profile, NurtureData data) {
        return t != null && profile.techniquePool().contains(t.id()) && profile.maxNurtureStage() >= t.requiredStage()
                && data.nurtureStage >= t.requiredStage();
    }
}
