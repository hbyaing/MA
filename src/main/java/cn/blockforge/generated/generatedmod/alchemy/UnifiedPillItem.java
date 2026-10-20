package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.items.BasePillItem;
import net.minecraft.world.entity.player.Player;

/** Effect dispatch is stack-aware in BasePillItem, shared with legacy pill objects. */
public final class UnifiedPillItem extends BasePillItem {
    public UnifiedPillItem(Properties properties) { super(0, 0, properties); }
    @Override protected boolean applyEffect(Player player, CultivationData data) { return false; }
}
