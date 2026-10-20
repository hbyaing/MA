package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Single extension point for heart-sword unlocks and realm scaling. */
public final class HeartSwordAbility {
    private HeartSwordAbility() {}
    public static boolean unlocked(Player player) {
        CultivationData data = GeneratedMod.data(player);
        return data != null && data.wuxingxiantu$getRealm() >= 0;
    }
    public static float damageMultiplier(Player player, ItemStack stack) {
        return cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.isValidHeartOwner(player,stack)
                ?(float)(cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.heartSwordRealmMultiplier(player)
                *cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.heartSwordStageMultiplier(player)):1;
    }
    public static boolean canForge(Player player, ItemStack stack) { return unlocked(player); }
}
