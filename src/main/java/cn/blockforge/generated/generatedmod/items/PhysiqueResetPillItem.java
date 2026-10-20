package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.PhysiqueType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** 洗经伐髓丹：随机重置体质，不受境界限制。 */
public final class PhysiqueResetPillItem extends BasePillItem {
    public PhysiqueResetPillItem(Properties properties) {
        super(30, 0, properties);
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        PhysiqueType chosen = PhysiqueType.values()[player.getRandom().nextInt(7)];
        data.wuxingxiantu$setPhysique(chosen);
        PhysiqueLogic.apply(player, data);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.physique_reset", chosen.displayName()), false);
        return true;
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) {
        return null;
    }
}
