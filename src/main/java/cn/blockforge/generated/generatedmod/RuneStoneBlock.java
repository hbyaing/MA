package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 炼器台符文石；单块摆放时是普通刻纹石砖，炼器台成型（FORGED）后切换为器台造型模型。
 * 成型时由 RefiningForgeStructure 写入 part/facing，让模型从整幅墙带/法阵大图上
 * 裁取自己那一片，花纹跨方块缝连续；炼制期间（LIT）点亮灵纹。
 * 右键已成型结构内的符文石可直接打开炼器台界面。
 */
public final class RuneStoneBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty FORGED = BooleanProperty.create("forged");
    /** 结构内位置：lower 底层边石 / upper 顶环边石 / upper_corner 顶环角石。 */
    public static final EnumProperty<ForgePart> PART = EnumProperty.create("part", ForgePart.class);
    /** 石面朝外的朝向（角石为斜向），决定裁哪一片大图。 */
    public static final EnumProperty<ForgeFace> FACING =
            EnumProperty.create("facing", ForgeFace.class);

    public enum ForgePart implements StringRepresentable {
        LOWER("lower"), UPPER("upper"), UPPER_CORNER("upper_corner");
        private final String name;
        ForgePart(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public enum ForgeFace implements StringRepresentable {
        NORTH("north"), EAST("east"), SOUTH("south"), WEST("west"),
        NORTH_WEST("north_west"), NORTH_EAST("north_east"),
        SOUTH_WEST("south_west"), SOUTH_EAST("south_east");
        private final String name;
        ForgeFace(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public RuneStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FORGED, false)
                .setValue(PART, ForgePart.LOWER).setValue(FACING, ForgeFace.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FORGED, PART, FACING);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
                                                     net.minecraft.world.entity.player.Player player,
                                                     net.minecraft.world.InteractionHand hand,
                                                     net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide) return net.minecraft.world.InteractionResult.PASS;
        // 先按炼器台判定；符文石同时作为炼丹炉结构的组件时，再回退按炼丹炉处理，打开炼丹界面。
        BlockPos core = RefiningForgeStructure.findCore(level, pos);
        if (core != null) return RefiningForgeBlock.open(level, core, player);
        BlockPos alchemyCore = AlchemyFurnaceStructure.findCore(level, pos);
        if (alchemyCore != null) return AlchemyFurnaceBlock.openAlchemy(level, alchemyCore, player);
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            animateLit(state, level, pos, random);
            return;
        }
        if (state.getValue(FORGED) && random.nextInt(9) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D,
                    pos.getY() + 0.9D + random.nextDouble() * 0.1D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D,
                    0.0D, 0.012D, 0.0D);
        }
    }

    private void animateLit(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.72D;
        double y = pos.getY() + 0.91D + random.nextDouble() * 0.12D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.72D;
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                (random.nextDouble() - 0.5D) * 0.012D, 0.018D,
                (random.nextDouble() - 0.5D) * 0.012D);
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.WITCH, x, y + 0.08D, z,
                    (random.nextDouble() - 0.5D) * 0.018D, 0.025D,
                    (random.nextDouble() - 0.5D) * 0.018D);
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
