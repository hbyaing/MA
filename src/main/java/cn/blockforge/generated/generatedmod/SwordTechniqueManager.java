package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.yujian.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** 剑技只使用当前蕴养灵剑的技能集合；御剑能力与主动剑技分离。 */
public final class SwordTechniqueManager {
    private SwordTechniqueManager() {}
    public static boolean isSwordTechnique(String id) { return SwordTechniqueRegistry.isTechnique(id); }
    public static boolean canCast(ServerPlayer player,String id) {
        if (!isSwordTechnique(id)) return true;
        ItemStack sword = SwordNurture.current(player);
        return SwordNurture.canCast(player,sword,id);
    }
    public static boolean cast(ServerPlayer player,String id,int mastery) {
        ItemStack sword = SwordNurture.current(player);
        return SwordNurture.cast(player,sword,id);
    }
}
