package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;

/** 永燃灵煤块：承载独立灵火，支持点燃、换火和主动扑灭。 */
public final class EternalSpiritCoalBlock extends Block {
    public EternalSpiritCoalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFireSource(BlockState state, net.minecraft.world.level.LevelReader level,
                                BlockPos pos, Direction side) {
        return side == Direction.UP;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.FLINT_AND_STEEL)) {
            int tier = equippedTier(player);
            if (tier <= 0) {
                // 未装备异火：放行给原版打火石逻辑，在煤块上点燃普通火焰。
                return InteractionResult.PASS;
            }
            BlockPos firePos = pos.above();
            if (!canPlaceFire(level, firePos, Direction.UP) && !ExoticFireState.isFire(level.getBlockState(firePos))) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) placeExoticFire(level, firePos, tier, player, hand, held, true);
            else ExoticFireState.predictClient(level, firePos, ExoticFireColorRegistry.flintVisualTier(tier));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockPos core = RefiningForgeStructure.findCore(level, pos);
        if (core != null) return RefiningForgeBlock.open(level, core, player);
        if (held.getItem() instanceof FireSeedItem seed && seed.tier() > 0) {
            int tier = cn.blockforge.generated.generatedmod.fire.FireBottleData.tier(held);
            if (!ProgressionGate.canUseFire(player, tier)) {
                if (!level.isClientSide) {
                    ProgressionGate.denyFireUse(player, tier);
                }
                return InteractionResult.FAIL;
            }
            if (!canPlaceFire(level, pos.above(), Direction.UP)) return InteractionResult.PASS;
            if (!level.isClientSide) placeExoticFire(level, pos.above(), tier, player, hand, held, false, Direction.UP);
            else ExoticFireState.predictClient(level, pos.above(), tier);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static int equippedTier(Player player) {
        CultivationData data = GeneratedMod.data(player);
        // 驭使资格复核：境界不足时装备栏里的高阶异火按未装备处理，
        // 打火石退回原版点火。
        return data == null ? 0 : ProgressionGate.effectiveFire(data, data.wuxingxiantu$getEquippedFire());
    }

    private static boolean canPlaceFire(Level level, BlockPos firePos, Direction supportDirection) {
        return level.getBlockState(firePos).isAir()
                && (level.getBlockState(firePos.below()).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())
                || BaseFireBlock.canBePlacedAt(level, firePos, supportDirection));
    }

    /** 放置灵火并保留树干侧面附着、顶部火焰和连接面属性。 */
    public static void placeExoticFire(Level level, BlockPos firePos, int tier, Player player,
                                       InteractionHand hand, ItemStack held, boolean flint) {
        placeExoticFire(level, firePos, tier, player, hand, held, flint, Direction.UP);
    }

    public static void placeExoticFire(Level level, BlockPos firePos, int tier, Player player,
                                       InteractionHand hand, ItemStack held, boolean flint, Direction supportDirection) {
        if (level.isClientSide) return;
        if (!ProgressionGate.canUseFire(player, tier)) {
            ProgressionGate.denyFireUse(player, tier);
            return;
        }
        if (!canPlaceFire(level, firePos, supportDirection) && !ExoticFireState.isFire(level.getBlockState(firePos))) return;
        int visualTier = ExoticFireColorRegistry.clampTier(tier);
        BlockState previous = level.getBlockState(firePos);
        level.setBlock(firePos, ExoticFireState.stateForTier(visualTier,
                ExoticFireState.isFire(previous) ? previous : BaseFireBlock.getState(level, firePos)), Block.UPDATE_ALL);
        ExoticFireState.set(level, firePos, visualTier);
        if (level.getBlockState(firePos.below()).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) {
            ExoticFireState.setEternalCoal(level, firePos.below(), visualTier);
        }
        if (!player.getAbilities().instabuild) {
            if (flint) held.hurtAndBreak(1, player, broken -> player.broadcastBreakEvent(hand));
        }
        level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                1.0F, level.random.nextFloat() * 0.4F + 0.8F);
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.eternal_fire_placed", ExoticFireColorRegistry.clampTier(tier)), true);
        BlockPos core = RefiningForgeStructure.findCore(level, firePos.below(1));
        if (core != null) RefiningForgeBlock.setStructureLit(level, core, true);
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Network.sendFireStateAround(serverLevel, firePos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock() && !level.isClientSide) {
            RefiningForgeStructure.refreshAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
