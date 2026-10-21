package cn.blockforge.generated.generatedmod.formation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

/** 保留来源四剑独立宝物的近战属性；剑阵本身不产生来源未实现的伤害。 */
public final class ChaosSwordItem extends SwordItem {
    public ChaosSwordItem(){super(Tiers.NETHERITE,20,-2.4F,new Item.Properties().fireResistant().rarity(Rarity.EPIC));}
    @Override public Component getName(ItemStack stack){return ChaosTreasureName.gradient(super.getName(stack));}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.chaos_grade").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.sword").withStyle(ChatFormatting.GRAY));
    }
}
