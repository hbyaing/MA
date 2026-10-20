package cn.blockforge.generated.generatedmod.yujian;

import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;

/** 品阶属于剑种类，剑蕴属于具体物品。 */
public record SwordProfile(int tier, String swordTier, int maxNurtureStage, int maxTechniqueSlots,
                           boolean flightCapability) {
    private static final Map<String, SwordProfile> PROFILES = Map.of(
            "peach_wood", new SwordProfile(0,"桃木",2,1,false),
            "dark_iron", new SwordProfile(1,"玄铁",4,2,true),
            "azure_steel", new SwordProfile(2,"明玦",5,3,true),
            "tiangang", new SwordProfile(3,"青冥",6,4,true),
            "big_dipper", new SwordProfile(4,"霁霄",7,4,true),
            "demon_slayer", new SwordProfile(5,"无相",8,5,true),
            "pure_yang", new SwordProfile(6,"归元",9,6,true),
            "xuanyuan", new SwordProfile(7,"天阙",10,6,true));
    private static final SwordProfile DEFAULT = new SwordProfile(0,"凡剑",2,1,false);
    public static SwordProfile of(String tierId) { return PROFILES.getOrDefault(tierId,DEFAULT); }
    public static SwordProfile of(ItemStack stack) {
        return stack.getItem() instanceof SpiritSwordItem sword ? sword.profile() : DEFAULT;
    }
    public FlyingSwordDefinition baseFlightStats(ItemStack stack) { return FlyingSwordRegistry.get(stack); }

    /** 明玦剑起为凌空 I，之后每提升一档灵剑品阶增加一级，天阙剑为凌空 VI。 */
    public int lingkongLevel() {
        return flightCapability && tier >= 2 ? Math.min(6, tier - 1) : 0;
    }
    public List<String> techniquePool() {
        return SwordTechniqueRegistry.all().stream().filter(t -> tier >= t.requiredTier()).map(SwordTechniqueRegistry.Technique::id).toList();
    }
    public boolean mastery(int stage, int requiredStage) {
        // 文档要求的玄铁调试豁免只影响御剑成长能力，不免除剑技学习条件。
        return flightCapability && (tier == 1 || stage >= requiredStage);
    }
}
