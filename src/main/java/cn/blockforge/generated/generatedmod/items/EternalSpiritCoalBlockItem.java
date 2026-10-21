package cn.blockforge.generated.generatedmod.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

/** 永燃灵煤块物品：块本体禁止作为燃料（炼魂炉、化魄炉与原版熔炉都不可入炉），永燃特性只属于灵煤物品。 */
public final class EternalSpiritCoalBlockItem extends BlockItem {
    public EternalSpiritCoalBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
        return 0;
    }
}
