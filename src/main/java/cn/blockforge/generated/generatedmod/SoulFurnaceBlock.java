package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** 炼魂炉/化魄炉：正面显示燃烧状态，右键投入手持物品并打开机器界面。 */
public final class SoulFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final SoulFurnaceBlockEntity.Kind kind;

    public SoulFurnaceBlock(SoulFurnaceBlockEntity.Kind kind) {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F, 3.5F)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0));
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    public SoulFurnaceBlockEntity.Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BlockEntityType<SoulFurnaceBlockEntity> entityType = kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING
                ? cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SOUL_REFINING_FURNACE.get()
                : cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SPIRIT_DISMANTLING_FURNACE.get();
        return new SoulFurnaceBlockEntity(entityType, pos, state, kind);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        BlockEntityType<?> expected = kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING
                ? cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SOUL_REFINING_FURNACE.get()
                : cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SPIRIT_DISMANTLING_FURNACE.get();
        return type == expected
                ? (lvl, pos, blockState, blockEntity) -> SoulFurnaceBlockEntity.tick(
                        lvl, pos, blockState, (SoulFurnaceBlockEntity) blockEntity)
                : null;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SoulFurnaceBlockEntity furnace
                ? furnace.menuProvider() : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof SoulFurnaceBlockEntity furnace)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            // 统一进度门槛：界面可以查看（多人服务器可参观高阶设施），
            // 但投料操作必须校验操作者本人境界，低境界玩家不能借他人魂炉生产。
            if (ProgressionGate.canUseSoulSystem(player, ProgressionGate.SOUL_FURNACE)) {
                furnace.insertHeldItem(player, hand);
            } else {
                ProgressionGate.deny(player, ProgressionGate.MSG_SOUL,
                        ProgressionGate.requiredRealmForSoulFeature(ProgressionGate.SOUL_FURNACE));
            }
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, furnace.menuProvider(), pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D + facing.getStepX() * 0.51D;
        double y = pos.getY() + 0.42D + random.nextDouble() * 0.20D;
        double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.51D;
        level.addParticle(ParticleTypes.FLAME, x, y, z,
                random.nextGaussian() * 0.015D, 0.045D, random.nextGaussian() * 0.015D);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.10D, z,
                    random.nextGaussian() * 0.012D, 0.035D, random.nextGaussian() * 0.012D);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof SoulFurnaceBlockEntity furnace) {
            net.minecraft.world.Containers.dropContents(level, pos, furnace);
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
