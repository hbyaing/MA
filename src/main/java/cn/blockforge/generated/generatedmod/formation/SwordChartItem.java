package cn.blockforge.generated.generatedmod.formation;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;

public final class SwordChartItem extends Item {
    public SwordChartItem(){super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));}
    @Override public InteractionResult useOn(UseOnContext context){
        if(context.getClickedFace()!=Direction.UP)return InteractionResult.PASS;
        if(context.getPlayer() instanceof ServerPlayer player)
            return SwordArrayServer.useChart(player,context.getItemInHand(),context.getClickedPos())?InteractionResult.CONSUME:InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }
    @Override public Component getName(ItemStack stack){return ChaosTreasureName.gradient(super.getName(stack));}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.chaos_grade").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.chart").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.cost",SwordArrayUpkeep.COST_LOW).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.wuxingxiantu.formation.no_combat").withStyle(ChatFormatting.DARK_GRAY));
        for(int i=1;i<=8;i++)lines.add(Component.translatable("tooltip.wuxingxiantu.formation.poem."+i).withStyle(ChatFormatting.GOLD));
    }
}
