package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.TribulationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** 渡劫丹：按丹药品级执行跨阶惩罚与服务端成功率判定。 */
public class BreakthroughPillItem extends BasePillItem {
    private final int tier;
    private final int bonus;
    private final boolean consumeOnUse;

    public BreakthroughPillItem(int tier, int bonus, Properties properties) {
        this(tier, bonus, properties, true);
    }

    public BreakthroughPillItem(int tier, int bonus, Properties properties, boolean consumeOnUse) {
        super(consumeOnUse ? 0 : 30, 0, properties, !consumeOnUse);
        this.tier = tier;
        this.bonus = bonus;
        this.consumeOnUse = consumeOnUse;
    }

    @Override
    protected boolean consumesOnUse() { return consumeOnUse; }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        int realm = data.wuxingxiantu$getRealm();
        int targetTier = TribulationManager.requiredCoreIndex(realm) + 1;
        if (realm >= 8 && data.wuxingxiantu$getCultivationStage() >= 3) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.immortal"), true);
            return false;
        }
        if (tier > targetTier) {
            cn.blockforge.generated.generatedmod.ProgressionGate.deny(player,
                    cn.blockforge.generated.generatedmod.ProgressionGate.MSG_PILL, Math.max(0, tier - 1));
            return false;
        }
        if (tier < targetTier) {
            int restore = Math.max(1, Math.round(CultivationLogic.maxAura(data) * 0.05F));
            data.wuxingxiantu$setAura(Math.min(CultivationLogic.maxAura(data), data.wuxingxiantu$getAura() + restore));
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.low_core_restore", restore), true);
            return true;
        }
        return player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && TribulationManager.startFromItem(serverPlayer);
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) {
        return null;
    }
}
