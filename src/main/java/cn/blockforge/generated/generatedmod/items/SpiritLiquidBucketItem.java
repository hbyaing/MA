package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** 灵液桶：可放置灵液源块，也可用于配方消耗。 */
public final class SpiritLiquidBucketItem extends Item {
    public SpiritLiquidBucketItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(target, WuXingBlocks.SPIRIT_LIQUID.get().defaultBlockState());
            ItemStack stack = context.getItemInHand();
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (!context.getPlayer().getInventory().add(emptyBucket)) {
                    context.getPlayer().drop(emptyBucket, false);
                }
            }
            if (context.getPlayer() != null) context.getPlayer().displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.liquid_placed"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
