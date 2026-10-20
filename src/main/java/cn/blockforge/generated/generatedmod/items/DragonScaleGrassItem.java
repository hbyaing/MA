package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.plants.SpiritPlantItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 末地主岛的稀有药草，可直接食用并作为洗髓丹材料；植株本体可再植。 */
public final class DragonScaleGrassItem extends BlockItem {
    public DragonScaleGrassItem(Block block, Properties properties) {
        super(block, properties.food(new FoodProperties.Builder()
                .nutrition(2).saturationMod(0.3F).alwaysEat().build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        SpiritPlantItem.appendHerbTooltip(getBlock(), tooltip);
    }
}
