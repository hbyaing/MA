package cn.blockforge.generated.generatedmod.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

public final class SpiritStoneItem extends Item {
    private final SpiritStoneGrade grade;
    public SpiritStoneItem(SpiritStoneGrade grade,Properties properties){super(properties);this.grade=grade;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.stone_value",grade.value).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.stone_exchange").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
