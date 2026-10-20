package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.TalismanKnowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/** 《万法归宗》残卷：右键研读一次，永久解锁画符箓的权限。 */
public final class WanfaGuizongItem extends Item {
    public WanfaGuizongItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (TalismanKnowledge.has(player)) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.wanfa_already_learned"), true);
            return InteractionResultHolder.fail(stack);
        }
        TalismanKnowledge.grant(player);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.wanfa_learned"), true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.wanfa_guizong"));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creative_test_badge")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
