package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** 可种植的发光蓝蘑菇；空手右键空气时也能作为小型灵力食材使用。 */
public final class GlowingBlueMushroomItem extends BlockItem {
    private static final int USE_DURATION_TICKS = 32;
    private static final float RESTORE_PERCENT = 0.02F;

    public GlowingBlueMushroomItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canConsume(level, player)) return InteractionResultHolder.fail(stack);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override public int getUseDuration(ItemStack stack) { return USE_DURATION_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;
        if (level.isClientSide) {
            player.stopUsingItem();
            return stack.copy();
        }
        CultivationData data = GeneratedMod.data(player);
        if (data == null) {
            stopClientUse(player);
            return stack;
        }
        float maximum = CultivationLogic.maxAura(data);
        float before = data.wuxingxiantu$getAura();
        float after = Math.min(maximum, before + maximum * RESTORE_PERCENT);
        if (after <= before) {
            stopClientUse(player);
            return stack;
        }
        data.wuxingxiantu$setAura(after);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.aura_restored",
                Math.round(after - before)), true);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) GeneratedMod.sync(serverPlayer);
        stopClientUse(player);
        return stack;
    }

    private static void stopClientUse(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            cn.blockforge.generated.generatedmod.Network.stopUsingItem(serverPlayer, null, 0);
        }
    }

    private boolean canConsume(Level level, Player player) {
        if (level.isClientSide) return true;
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return false;
        if (data.wuxingxiantu$getAura() >= CultivationLogic.maxAura(data)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.aura_full"), true);
            return false;
        }
        return true;
    }
}
