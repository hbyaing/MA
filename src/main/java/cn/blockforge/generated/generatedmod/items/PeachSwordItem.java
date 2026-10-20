package cn.blockforge.generated.generatedmod.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

/** 旧类名兼容层；新注册统一使用 SpiritSwordItem。 */
@Deprecated
public final class PeachSwordItem extends SpiritSwordItem {
    public PeachSwordItem(Tier tier, float damage, Item.Properties properties, String tierId,
                          int maxSpirit, int flightDistance, int spiritCost, int pierceCount) {
        super(tier, damage, properties, tierId, maxSpirit, flightDistance, spiritCost, pierceCount);
    }
}
