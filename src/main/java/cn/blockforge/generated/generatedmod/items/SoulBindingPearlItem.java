package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.SoulPearlData;
import cn.blockforge.generated.generatedmod.SpiritMerchant;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 捕获村民或流浪商人的交易灵魂；仅潜行右键空气打开保存的交易界面。 */
public final class SoulBindingPearlItem extends Item {
    public SoulBindingPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        // 统一进度门槛：神魂系统按功能分级，境界不足不得驭魂。
        if (!ProgressionGate.canUseSoulSystem(player, ProgressionGate.SOUL_BINDING)) {
            if (!level.isClientSide) {
                ProgressionGate.deny(player, ProgressionGate.MSG_SOUL,
                        ProgressionGate.requiredRealmForSoulFeature(ProgressionGate.SOUL_BINDING));
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            SpiritMerchant.open(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   net.minecraft.world.entity.LivingEntity target,
                                                   InteractionHand hand) {
        if (!(target instanceof AbstractVillager villager)) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        // 统一进度门槛：摄魄捕魂同样校验使用者本人境界。
        if (!ProgressionGate.canUseSoulSystem(player, ProgressionGate.SOUL_BINDING)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_SOUL,
                    ProgressionGate.requiredRealmForSoulFeature(ProgressionGate.SOUL_BINDING));
            return InteractionResult.FAIL;
        }
        // 人皇幡记录的个体（魂仆）不能被拘魂珠拿走肉身，否则幡内魂魄会与现实脱节；
        // 必须先用渡灵（灭魂珠）在人皇幡内处理其魂魄。
        if (HumanEmperorBannerItem.isServant(villager)) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.soul_binding_servant"), true);
            return InteractionResult.FAIL;
        }
        SoulPearlData.captureVillager(stack, villager);
        villager.discard();
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.soul_captured"), true);
        return InteractionResult.CONSUME;
    }
}
