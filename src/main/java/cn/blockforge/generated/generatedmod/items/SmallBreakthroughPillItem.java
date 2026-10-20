package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import net.minecraft.world.entity.player.Player;

/** Advances a minor stage once cultivation is full; major breakthroughs keep their own rules. */
public final class SmallBreakthroughPillItem extends BasePillItem {
    public SmallBreakthroughPillItem(int tier, Properties properties) {
        super(0, 0, properties);
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        return CultivationLogic.advanceMinorStage(player, data);
    }
}
