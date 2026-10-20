package cn.blockforge.generated.generatedmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 刻刀：镌印台的刻字工具，具备耐久度。镌刻失败时消耗耐久。 */
public final class CarvingKnifeItem extends Item {
    public CarvingKnifeItem(Properties properties) {
        super(properties.durability(128));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.carving_knife"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
