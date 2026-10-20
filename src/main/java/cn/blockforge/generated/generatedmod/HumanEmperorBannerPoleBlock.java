package cn.blockforge.generated.generatedmod;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * 人皇幡插地后的杆身延伸段：插地座往上共 4 格的隐形辅助方块，只为延长可交互
 * 范围（右键/左键命中任意一截都等同于对插地座操作），不渲染、不阻挡移动；
 * 破坏任意一截即整幡收回，掉落由插地座统一处理。
 */
public final class HumanEmperorBannerPoleBlock extends Block {
    private static final VoxelShape POLE = Block.box(6.5, 0, 6.5, 9.5, 16, 9.5);
    /** 一座幡的杆身最多向上延伸 4 格（总高 5 格）。 */
    public static final int MAX_ABOVE = 4;

    public HumanEmperorBannerPoleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return POLE;
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                                  CollisionContext context) {
        return Shapes.empty();
    }

    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    /** 从任一杆身格向下寻找到插地座；途中出现非杆方块即视为断链。 */
    @Nullable public static BlockPos findStandBelow(LevelReader level, BlockPos pos) {
        for (int i = 1; i <= MAX_ABOVE; i++) {
            BlockPos below = pos.below(i);
            Block block = level.getBlockState(below).getBlock();
            if (block instanceof HumanEmperorBannerStandBlock) return below;
            if (!(block instanceof HumanEmperorBannerPoleBlock)) return null;
        }
        return null;
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        BlockPos standPos = findStandBelow(level, pos);
        if (standPos == null) return InteractionResult.PASS;
        return HumanEmperorBannerStandBlock.interactStand(level, standPos, player, hand);
    }

    @Override public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // 破坏任意一截杆身：整幡移除（幡体由插地座的 onRemove 掉落）。
        BlockPos standPos = findStandBelow(level, pos);
        if (standPos != null) {
            HumanEmperorBannerStandBlock.removeWholeBanner(level, standPos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                                        @Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                        @Nullable ItemStack tool) {
        // 杆身不是物品，不掉落任何东西（物品归插地座管理）。
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Block below = level.getBlockState(pos.below()).getBlock();
        return below instanceof HumanEmperorBannerStandBlock || below instanceof HumanEmperorBannerPoleBlock;
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                             LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState() : state;
    }
}
