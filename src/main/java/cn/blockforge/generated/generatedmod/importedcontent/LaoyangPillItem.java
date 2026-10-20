package cn.blockforge.generated.generatedmod.importedcontent;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Original easter egg: a fair coin chooses regeneration X or poison X plus nausea. */
public final class LaoyangPillItem extends Item {
    public LaoyangPillItem(Properties properties) {super(properties);}
    @Override public UseAnim getUseAnimation(ItemStack stack) {return UseAnim.EAT;}
    @Override public int getUseDuration(ItemStack stack) {return 32;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity.getRandom().nextBoolean())entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,600,9));
            else {entity.addEffect(new MobEffectInstance(MobEffects.POISON,200,9));entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));}
            if(!(entity instanceof Player player)||!player.getAbilities().instabuild)stack.shrink(1);
        }
        return stack;
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> tooltip,TooltipFlag flag) {
        tooltip.add(Component.translatable("content.wuxingxiantu.laoyang_good").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("content.wuxingxiantu.laoyang_bad").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("content.wuxingxiantu.laoyang_source").withStyle(ChatFormatting.GRAY));
    }
}
