package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.StringRepresentable;

/**
 * 炼器台角座：成型后的四角外观方块。外观是外角位的镂空火盆座（盆体靠结构外侧，
 * 四个角各朝向外方，故带 facing 属性选择对应切片模型），未成型时该位置是玩家
 * 摆放的磨制黑石砖台阶。炼器台点火时角座同步进入 LIT 态，腔内立橙黄烛火；
 * 右键任意角座都能打开炼器台界面。
 */
public final class RefiningForgeCornerBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    /** 角座所在的结构外角方向（斜向），决定使用哪一份切片模型。 */
    public static final EnumProperty<CornerFacing> FACING =
            EnumProperty.create("facing", CornerFacing.class);
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public enum CornerFacing implements StringRepresentable {
        NORTH_WEST("north_west", -1, -1), NORTH_EAST("north_east", 1, -1),
        SOUTH_WEST("south_west", -1, 1), SOUTH_EAST("south_east", 1, 1);
        private final String name;
        private final int dx;
        private final int dz;
        CornerFacing(String name, int dx, int dz) {
            this.name = name;
            this.dx = dx;
            this.dz = dz;
        }
        @Override public String getSerializedName() { return this.name; }
        public int dx() { return this.dx; }
        public int dz() { return this.dz; }
    }

    public RefiningForgeCornerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false)
                .setValue(FACING, CornerFacing.NORTH_WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos core = RefiningForgeStructure.findCore(level, pos);
        if (core == null) return InteractionResult.PASS;
        return RefiningForgeBlock.open(level, core, player);
    }

    /** 点火时角座烛火摇曳：橙黄火苗加偶发烟粒；火苗立在朝外一角的盆腔中心。 */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || random.nextInt(3) != 0) return;
        CornerFacing facing = state.getValue(FACING);
        double cx = 0.5D + facing.dx() * 0.11D;
        double cz = 0.5D + facing.dz() * 0.11D;
        level.addParticle(ParticleTypes.SMALL_FLAME,
                pos.getX() + cx + (random.nextDouble() - 0.5D) * 0.34D,
                pos.getY() + 0.42D + random.nextDouble() * 0.16D,
                pos.getZ() + cz + (random.nextDouble() - 0.5D) * 0.34D,
                0.0D, 0.028D, 0.0D);
        if (random.nextInt(7) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + cx, pos.getY() + 0.72D, pos.getZ() + cz,
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !level.isClientSide && state.getBlock() != newState.getBlock()) {
            RefiningForgeStructure.refreshAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
