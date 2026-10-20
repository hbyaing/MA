package cn.blockforge.generated.generatedmod.items;

import net.minecraft.world.InteractionResult;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

public final class SpiritLiquidPotItem extends SpiritLiquidDrinkItem {
    public SpiritLiquidPotItem(Item emptyItem, Properties properties) {
        super(0.20F, emptyItem, properties.stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (state.is(Blocks.WATER)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, WuXingBlocks.SPIRIT_LIQUID.get().defaultBlockState());
                returnEmptyContainer(stack, context.getPlayer());
                if (context.getPlayer() != null) context.getPlayer().displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.liquid_purified"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.getBlock() instanceof CropBlock crop) {
            if (!level.isClientSide && crop.isValidBonemealTarget(level, pos, state, false)) {
                crop.performBonemeal((net.minecraft.server.level.ServerLevel) level, level.random, pos, state);
                returnEmptyContainer(stack, context.getPlayer());
                if (context.getPlayer() != null) context.getPlayer().displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.liquid_nourished"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

}
