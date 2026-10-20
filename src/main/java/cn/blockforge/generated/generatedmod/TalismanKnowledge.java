package cn.blockforge.generated.generatedmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** 符箓知识：由《万法归宗》残卷解锁；持有时才允许在镌印台/绘制台描绘符文。 */
public final class TalismanKnowledge {
    private static final String TAG = "wuxingxiantu_talisman_knowledge";

    private TalismanKnowledge() {}

    public static boolean has(Player player) {
        if (player == null || player.level().isClientSide) return false;
        CompoundTag tag = player.getPersistentData();
        return tag.contains(TAG) && tag.getBoolean(TAG);
    }

    public static void grant(Player player) {
        if (player == null || player.level().isClientSide) return;
        player.getPersistentData().putBoolean(TAG, true);
    }
}
