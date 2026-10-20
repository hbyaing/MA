package cn.blockforge.generated.generatedmod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 境界特性：炼气期自带挖掘等级 0，每提升一个大境界挖掘等级 +1，无视手持工具；
 * 化神期起删除所有挖掘速度惩罚（包括飞行中与水下方块）。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RealmMiningTrait {
    /** 化神（境界索引 4）及以上免除全部挖掘速度惩罚。 */
    private static final int NO_PENALTY_REALM = 4;

    private RealmMiningTrait() {}

    /** 玩家当前境界对应的挖掘等级；无法取得境界时返回 -1。 */
    public static int miningLevel(Player player) {
        if (player == null) return -1;
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return -1;
        return Math.max(0, data.wuxingxiantu$getRealm());
    }

    /** 方块掉落所需的最低挖掘等级（按原版工具层级换算，无法用任何工具收割时返回极大值）。 */
    public static int requiredLevel(BlockState state) {
        if (!state.requiresCorrectToolForDrops()) return 0;
        int best = Integer.MAX_VALUE;
        for (Tier tier : TierSortingRegistry.getSortedTiers()) {
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state) && tier.getLevel() < best) {
                best = tier.getLevel();
            }
        }
        return best;
    }

    /** 境界挖掘等级达标即视为可收割，与原版的工具判定无关。 */
    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) return;
        int level = miningLevel(event.getEntity());
        if (level >= 0 && level >= requiredLevel(event.getTargetBlock())) {
            event.setCanHarvest(true);
        }
    }

    /** 化神期删除挖掘速度惩罚：原版在水下与离地（含飞行）时各除以 5，这里等比补回。 */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (miningLevel(player) < NO_PENALTY_REALM) return;
        float speed = event.getNewSpeed();
        if (speed <= 0.0F) return;
        if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            speed *= 5.0F;
        }
        if (!player.onGround()) {
            speed *= 5.0F;
        }
        event.setNewSpeed(speed);
    }
}
