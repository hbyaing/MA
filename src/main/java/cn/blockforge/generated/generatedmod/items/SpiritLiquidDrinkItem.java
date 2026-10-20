package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import cn.blockforge.generated.generatedmod.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

/** 可饮用的灵液容器；饮用后补充玩家灵力并返还空容器。 */
public class SpiritLiquidDrinkItem extends Item {
    private static final int DRINK_DURATION_TICKS = 32;
    private final float restorePercent;
    protected final Item emptyItem;

    public SpiritLiquidDrinkItem(float restorePercent, Item emptyItem, Properties properties) {
        super(properties);
        this.restorePercent = restorePercent;
        this.emptyItem = emptyItem == null ? Items.GLASS_BOTTLE : emptyItem;
    }

    protected final void returnEmptyContainer(ItemStack stack, Player player) {
        if (player == null || player.getAbilities().instabuild) return;
        stack.shrink(1);
        ItemStack empty = new ItemStack(emptyItem);
        if (!player.getInventory().add(empty)) player.drop(empty, false);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canDrink(level, player)) return InteractionResultHolder.fail(stack);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    /** A glass bottle can collect a spirit-liquid source directly, matching the pot route. */
    @Override public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack bottle = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.isSourceOfType(ModFluids.SPIRIT_LIQUID_SOURCE.get())) return net.minecraft.world.InteractionResult.PASS;
        if (player == null || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), bottle)) return net.minecraft.world.InteractionResult.FAIL;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) return net.minecraft.world.InteractionResult.PASS;
        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        ItemStack picked = pickup.pickupBlock(level, pos, state);
        if (!picked.is(cn.blockforge.generated.generatedmod.WuXingItems.SPIRIT_LIQUID_BUCKET.get())) return net.minecraft.world.InteractionResult.FAIL;
        pickup.getPickupSound().ifPresent(sound -> level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
        ItemStack filled = new ItemStack(cn.blockforge.generated.generatedmod.WuXingItems.SPIRIT_LIQUID_BOTTLE.get());
        ItemStack result = ItemUtils.createFilledResult(bottle, player, filled);
        player.setItemInHand(context.getHand(), result);
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    @Override public int getUseDuration(ItemStack stack) { return DRINK_DURATION_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public SoundEvent getDrinkingSound() { return SoundEvents.GENERIC_DRINK; }

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
        float restored = Math.max(1.0F, maximum * restorePercent);
        float after = Math.min(maximum, before + restored);
        if (after <= before) {
            GeneratedMod.sync((net.minecraft.server.level.ServerPlayer) player);
            stopClientUse(player);
            return stack;
        }
        data.wuxingxiantu$setAura(after);
        returnEmptyContainer(stack, player);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.liquid_drunk",
                Math.round(after - before)), true);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            GeneratedMod.sync(serverPlayer);
            stopClientUse(serverPlayer);
        }
        return stack;
    }

    private static void stopClientUse(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            cn.blockforge.generated.generatedmod.Network.stopUsingItem(serverPlayer, null, 0);
        }
    }

    private boolean canDrink(Level level, Player player) {
        if (level.isClientSide) return true;
        CultivationData data = GeneratedMod.data(player);
        if (data == null) {
            stopClientUse(player);
            return false;
        }
        if (data.wuxingxiantu$getAura() >= CultivationLogic.maxAura(data)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.aura_full"), true);
            stopClientUse(player);
            return false;
        }
        return true;
    }
}
