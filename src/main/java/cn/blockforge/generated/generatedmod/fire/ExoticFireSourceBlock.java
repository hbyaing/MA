package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Stable resource node: no FireBlock behaviour, no block item, no loot. */
public final class ExoticFireSourceBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty FIRE_TIER = IntegerProperty.create("fire_tier", 1, 9);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public ExoticFireSourceBlock() {
        super(Properties.of().strength(-1.0F, 3600000.0F).noLootTable().noOcclusion()
                .lightLevel(state -> 12).pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FIRE_TIER, 1).setValue(BlockStateProperties.WATERLOGGED, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FIRE_TIER, BlockStateProperties.WATERLOGGED);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) { return false; }
    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override public BlockState updateShape(BlockState state, Direction side, BlockState adjacent,
                                            LevelAccessor level, BlockPos pos, BlockPos adjacentPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state;
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        ItemStack empty = player.getItemInHand(hand);
        if (empty.isEmpty() || !(empty.getItem() instanceof FireSeedItem seed) || seed.tier() != 0) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer server) || level.getBlockState(pos) != state
                || !player.mayUseItemAt(pos, hit.getDirection(), empty)) return InteractionResult.FAIL;
        int tier = state.getValue(FIRE_TIER);
        ItemStack filled = FireBottleData.fresh(tier);
        BlockState replacement = state.getValue(BlockStateProperties.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        if (!level.setBlock(pos, replacement, Block.UPDATE_ALL)) return InteractionResult.FAIL;
        var output = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), filled);
        output.setTarget(player.getUUID());
        output.setNoPickUpDelay();
        if (!level.addFreshEntity(output)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            return InteractionResult.FAIL;
        }
        if (!player.isCreative()) empty.shrink(1);
        FireDiscovery.discover(server, tier);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.fire_captured", filled.getHoverName()), true);
        return InteractionResult.CONSUME;
    }
}
