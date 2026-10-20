package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 人皇幡的地面插旗座：由人皇幡物品右键方块生成，可空手右键取回原物品；上方杆身由无碰撞的杆格延伸可交互范围。 */
public final class HumanEmperorBannerStandBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape POLE = Block.box(6, 0, 6, 10, 16, 10);

    public HumanEmperorBannerStandBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return POLE;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                             LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : state;
    }

    @Override public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HumanEmperorBannerStandBlockEntity(ModBlockEntities.HUMAN_EMPEROR_BANNER_STAND.get(), pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                               BlockEntityType<T> type) {
        return type == ModBlockEntities.HUMAN_EMPEROR_BANNER_STAND.get()
                ? (lvl, pos, blockState, entity) -> HumanEmperorBannerStandBlockEntity.tick(
                        lvl, pos, blockState, (HumanEmperorBannerStandBlockEntity) entity) : null;
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        return interactStand(level, pos, player, hand);
    }

    /** 与插地座交互的统一入口（杆身任意一截也经此转发）：潜行空手取回、手持幡穿过、否则开座菜单。 */
    public static InteractionResult interactStand(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof HumanEmperorBannerStandBlockEntity stand)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            ItemStack banner = stand.takeBanner();
            if (banner.isEmpty()) return InteractionResult.PASS;
            if (!player.addItem(banner)) player.drop(banner, false);
            level.removeBlock(pos, false);
            return InteractionResult.CONSUME;
        }
        if (!player.isShiftKeyDown() && !player.getItemInHand(hand).isEmpty()
                && player.getItemInHand(hand).getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            HumanEmperorBannerMenu.openStandMenu(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 整幡移除：破坏任意杆身格时调用，座方块的 onRemove 会级联清掉上方所有杆格并掉出幡体。 */
    public static void removeWholeBanner(Level level, BlockPos standPos) {
        if (level.getBlockState(standPos).getBlock() instanceof HumanEmperorBannerStandBlock) {
            level.removeBlock(standPos, false);
        }
        for (int i = 1; i <= HumanEmperorBannerPoleBlock.MAX_ABOVE; i++) {
            BlockPos above = standPos.above(i);
            if (level.getBlockState(above).getBlock() instanceof HumanEmperorBannerPoleBlock) {
                level.removeBlock(above, false);
            } else break;
        }
    }

    /** 放置（或补齐）座上方最多 4 格隐形杆身，使整根幡杆都能右键交互。幂等，只对空格拉伸。 */
    public static void placePolesAbove(net.minecraft.world.level.LevelAccessor level, BlockPos standPos) {
        BlockState pole = WuXingBlocks.HUMAN_EMPEROR_BANNER_POLE.get().defaultBlockState();
        for (int i = 1; i <= HumanEmperorBannerPoleBlock.MAX_ABOVE; i++) {
            BlockPos above = standPos.above(i);
            if (!level.isEmptyBlock(above)) break;
            level.setBlock(above, pole, 3);
        }
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && state.getBlock() != newState.getBlock()) {
            // 座被移除时级联清除上方杆身格。
            for (int i = 1; i <= HumanEmperorBannerPoleBlock.MAX_ABOVE; i++) {
                BlockPos above = pos.above(i);
                if (level.getBlockState(above).getBlock() instanceof HumanEmperorBannerPoleBlock) {
                    level.removeBlock(above, false);
                } else break;
            }
            if (level.getBlockEntity(pos) instanceof HumanEmperorBannerStandBlockEntity stand) {
                ItemStack banner = stand.takeBanner();
                if (!banner.isEmpty()) Block.popResource(level, pos, banner);
                level.removeBlockEntity(pos);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
