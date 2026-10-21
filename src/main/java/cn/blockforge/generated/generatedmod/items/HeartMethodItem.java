package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 心法秘籍：装备后替换当前心法，数量限制由能力数据保证为一个。 */
public final class HeartMethodItem extends Item {
    private final String methodId;
    public HeartMethodItem(String methodId, Properties properties) { super(properties); this.methodId = methodId; }
    public String methodId() { return methodId; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return InteractionResultHolder.fail(stack);
        // 统一进度门槛：心法各有最低境界；境界不足不学习、不装备、不消耗秘籍。
        if (!ProgressionGate.canLearnHeartMethod(player, methodId)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_HEART_LEARN,
                    ProgressionGate.requiredRealmForHeartMethod(methodId));
            return InteractionResultHolder.fail(stack);
        }
        if (!data.wuxingxiantu$hasLearnedHeartMethod(methodId)
                && !data.wuxingxiantu$learnHeartMethod(methodId)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_method_learn_limit"), true);
            return InteractionResultHolder.fail(stack);
        }
        data.wuxingxiantu$setEquippedHeartMethod(methodId);
        stack.shrink(1);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_equipped", getDescription()), true);
        GeneratedMod.sync((net.minecraft.server.level.ServerPlayer) player);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS
                : use(context.getLevel(), player, context.getHand()).getResult();
    }
}
