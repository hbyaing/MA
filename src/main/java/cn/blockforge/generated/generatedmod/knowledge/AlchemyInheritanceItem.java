package cn.blockforge.generated.generatedmod.knowledge;

import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Adapts Xiantu's four inheritance tiers to the one world-owned alchemy profession. */
public final class AlchemyInheritanceItem extends Item {
    private final int tier;
    public AlchemyInheritanceItem(int tier,Properties properties) {super(properties);this.tier=tier;}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("knowledge.wuxingxiantu.scroll_hint"));
        lines.add(Component.translatable("knowledge.wuxingxiantu.scroll_limit"));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(!(player instanceof ServerPlayer serverPlayer))return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        int gain=PlayerCrafts.get(serverPlayer.server).studyAlchemyInheritance(serverPlayer,tier);
        if(gain<=0) {
            player.displayClientMessage(Component.translatable("knowledge.wuxingxiantu.scroll_blocked"),true);
            return InteractionResultHolder.fail(stack);
        }
        if(!player.getAbilities().instabuild)stack.shrink(1);
        level.playSound(null,player.blockPosition(),SoundEvents.ENCHANTMENT_TABLE_USE,SoundSource.PLAYERS,.8F,.9F);
        player.displayClientMessage(Component.translatable("knowledge.wuxingxiantu.scroll_studied",gain),false);
        return InteractionResultHolder.consume(stack);
    }
}
