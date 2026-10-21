package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** 空灵液壶：右键灵液源块会取走源块并变为装满的灵液壶。 */
public final class EmptySpiritLiquidPotItem extends Item {
    public EmptySpiritLiquidPotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack emptyPot = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(emptyPot);
        BlockPos pos = hit.getBlockPos();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.isSourceOfType(ModFluids.SPIRIT_LIQUID_SOURCE.get())) {
            return InteractionResultHolder.pass(emptyPot);
        }
        if (!level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, hit.getDirection(), emptyPot)) {
            return InteractionResultHolder.fail(emptyPot);
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return InteractionResultHolder.pass(emptyPot);
        }
        if (level.isClientSide) return InteractionResultHolder.success(emptyPot);

        ItemStack pickedUp = pickup.pickupBlock(level, pos, state);
        if (!pickedUp.is(WuXingItems.SPIRIT_LIQUID_BUCKET.get())) {
            return InteractionResultHolder.fail(emptyPot);
        }
        pickup.getPickupSound().ifPresent(sound ->
                level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
        ItemStack filledPot = new ItemStack(WuXingItems.LIQUID_POT.get());
        ItemStack result = ItemUtils.createFilledResult(emptyPot, player, filledPot);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.liquid_pot_filled"), true);
        return InteractionResultHolder.consume(result);
    }
}
