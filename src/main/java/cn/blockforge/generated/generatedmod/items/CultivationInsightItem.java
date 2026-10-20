package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 修炼心得：右键将固定数值转化为当前阶段修为。 */
public final class CultivationInsightItem extends Item {
    public static final long CULTIVATION_VALUE = CultivationLogic.INSIGHT_CULTIVATION;

    public CultivationInsightItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CultivationData data = GeneratedMod.data(player);
            if (data != null) {
                if(!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,true))return InteractionResultHolder.fail(stack);
                long remaining = CultivationLogic.breakthroughRequirement(data) - data.wuxingxiantu$getCultivation();
                long gain = Math.min(CULTIVATION_VALUE, Math.max(0L, remaining));
                if (gain > 0L) {
                    data.wuxingxiantu$setCultivation(data.wuxingxiantu$getCultivation() + gain);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    player.displayClientMessage(Component.translatable(
                            "message.wuxingxiantu.insight_used", gain), true);
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        GeneratedMod.sync(serverPlayer);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.cultivation_insight",
                stack.getHoverName(), CULTIVATION_VALUE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
