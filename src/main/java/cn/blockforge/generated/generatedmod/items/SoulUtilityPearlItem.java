package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.SoulPearlData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 人皇幡专用工具珠：万魂珠按模式收纳或释放魂魄，灭魂珠直接销毁魂魄。 */
public final class SoulUtilityPearlItem extends Item {
    private final boolean myriad;
    private final boolean creative;

    public SoulUtilityPearlItem(Properties properties, boolean myriad) {
        this(properties, myriad, false);
    }

    public SoulUtilityPearlItem(Properties properties, boolean myriad, boolean creative) {
        super(properties);
        this.myriad = myriad;
        this.creative = creative;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 统一进度门槛：驭魂属魂魄系统，境界不足无法切换收纳/释放（创造珠不受限）。
        if (!creative && !cn.blockforge.generated.generatedmod.ProgressionGate.canUseSoulSystem(
                player, cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_UTILITY)) {
            if (!level.isClientSide) {
                cn.blockforge.generated.generatedmod.ProgressionGate.deny(player,
                        cn.blockforge.generated.generatedmod.ProgressionGate.MSG_SOUL,
                        cn.blockforge.generated.generatedmod.ProgressionGate.requiredRealmForSoulFeature(
                                cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_UTILITY));
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && myriad) {
            boolean release = SoulPearlData.toggleMyriadMode(stack);
            player.displayClientMessage(Component.translatable(release
                    ? "message.wuxingxiantu.myriad_mode_release"
                    : "message.wuxingxiantu.myriad_mode_absorb"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 创造万魂珠右键生物即可直接收服，物品本身和目标都不会产生普通消耗。 */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!creative || !myriad || target == player) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        if (target instanceof net.minecraft.world.entity.player.Player
                || HumanEmperorBannerItem.isServant(target)) return InteractionResult.FAIL;
        if (SoulPearlData.captureEntityIntoMyriad(stack, target)) {
            target.discard();
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.creative_myriad_captured", target.getDisplayName()), true);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.creative_myriad_capture_failed"), true);
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (myriad) {
            tooltip.add(Component.translatable(creative
                    ? "tooltip.wuxingxiantu.creative_myriad_soul_pearl"
                    : "tooltip.wuxingxiantu.myriad_soul_pearl"));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.myriad_soul_pearl_count",
                    SoulPearlData.storedSoulTotal(stack)));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.myriad_soul_pearl_intact",
                    SoulPearlData.storedIntactSoulTotal(stack)));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.myriad_soul_pearl_broken",
                    SoulPearlData.storedBrokenSoulTotal(stack)));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.myriad_soul_pearl_mode",
                    Component.translatable(SoulPearlData.isMyriadReleaseMode(stack)
                            ? "gui.wuxingxiantu.myriad_mode_release"
                            : "gui.wuxingxiantu.myriad_mode_absorb")));
        } else {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.destroy_soul_pearl"));
        }
    }
}
