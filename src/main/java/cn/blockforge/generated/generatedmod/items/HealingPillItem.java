package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** 按最大灵力百分比恢复灵力的回灵丹。 */
public class HealingPillItem extends BasePillItem {
    private final float restorePercent;

    public HealingPillItem(float restorePercent, Properties properties) {
        super(restorePercent <= 0.02F ? 30 : 5, 0, properties);
        this.restorePercent = restorePercent;
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        float max = CultivationLogic.maxAura(data);
        float before = data.wuxingxiantu$getAura();
        float restored = max * restorePercent;
        if (before >= max || restored <= 0.0F) return false;
        if (CultivationLogic.restoreMana(data, restored) <= 0.0F) return false;
        if (restorePercent <= 0.02F) {
            extendEffect(player, MobEffects.CONFUSION, 600, 0, true, false, true);
        } else {
            extendEffect(player, MobEffects.REGENERATION, 100, 0);
        }
        return true;
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) {
        return Component.translatable("message.wuxingxiantu.pill_used",
                Math.round(CultivationLogic.maxAura(data) * restorePercent));
    }
}
