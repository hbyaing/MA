package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** 灵根觉醒丹：觉醒指定灵根并提升其品级。 */
public class RootAwakeningItem extends BasePillItem {
    private final int root;
    private final int quality;

    public RootAwakeningItem(int root, int quality, Properties properties) {
        super(30, 0, properties);
        this.root = root;
        this.quality = quality;
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        // 统一进度门槛：高品质灵根丹需要对应境界才能承受药力；
        // 拒绝时 applyEffect 返回 false，丹药不消耗、灵力不扣。
        if (!ProgressionGate.canUsePill(player, quality)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_PILL, ProgressionGate.requiredRealmForRootQuality(quality));
            return false;
        }
        int current = data.wuxingxiantu$getRootQuality(root);
        if ((data.wuxingxiantu$getRootMask() & (1 << root)) != 0 && current >= quality) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.root_already_awakened"), true);
            return false;
        }
        data.wuxingxiantu$setRootMask(data.wuxingxiantu$getRootMask() | (1 << root));
        data.wuxingxiantu$setRootQuality(root, Math.max(current, quality));
        CultivationLogic.refreshChaosRoot(player, data);
        return true;
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) {
        return Component.translatable("message.wuxingxiantu.root_awakened",
                Component.translatable("root.wuxingxiantu." + root),
                Component.translatable("quality.wuxingxiantu." + Math.max(0, Math.min(3, quality - 1))));
    }
}
