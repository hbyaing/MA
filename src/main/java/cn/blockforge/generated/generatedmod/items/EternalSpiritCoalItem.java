package cn.blockforge.generated.generatedmod.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 永燃灵煤：由灵蕴台转化灵煤得到的永恒燃料。
 * 原版熔炉按近无穷烧制时长处理；炼魂炉与化魄炉把它当作永不消耗的燃料。
 */
public final class EternalSpiritCoalItem extends Item {
    /** 约 57 个现实日的连续烧炼时长，等效“无尽燃烧”。 */
    public static final int ENDLESS_BURN_TICKS = 100_000_000;

    public EternalSpiritCoalItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
        return ENDLESS_BURN_TICKS;
    }
}
