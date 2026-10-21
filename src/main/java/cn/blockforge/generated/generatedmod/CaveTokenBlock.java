package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 薄型洞府令牌，可贴附在完整方块的墙面、地面或天花板上。 */
public final class CaveTokenBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final int PROTECTION_RADIUS = 64;

    private static final VoxelShape FLOOR_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    private static final VoxelShape CEILING_SHAPE = Block.box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 2.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(14.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 2.0D, 2.0D, 2.0D, 14.0D, 14.0D);

    public CaveTokenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACE, AttachFace.WALL)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> FLOOR_SHAPE;
            case CEILING -> CEILING_SHAPE;
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case WEST -> WEST_SHAPE;
                case EAST -> EAST_SHAPE;
                default -> NORTH_SHAPE;
            };
        };
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            CaveTokenLocations.get(serverLevel).add(pos);
            CaveTokenLocations.ensureForcedChunks(serverLevel, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {
            CaveTokenLocations locations = CaveTokenLocations.get(serverLevel);
            locations.remove(pos);
            CaveTokenLocations.releaseForcedChunks(serverLevel, pos);
            locations.ensureAllForcedChunks(serverLevel);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        CultivationData data = GeneratedMod.data(serverPlayer);
        if (data == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        CaveTokenLocations locations = CaveTokenLocations.get(serverLevel);
        locations.add(pos);
        CaveTokenLocations.ensureForcedChunks(serverLevel, pos);
        BlockPos cavePos = data.wuxingxiantu$getCavePos();
        if (cavePos == null) {
            data.wuxingxiantu$setCavePos(pos);
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.cave_marked", PROTECTION_RADIUS), false);
        } else if (data.wuxingxiantu$getTeleportCooldown() <= level.getGameTime()) {
            int cost = Math.max(1, Math.round(CultivationLogic.maxAura(data) * 0.10F));
            if (data.wuxingxiantu$getAura() < cost) {
                serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"), true);
            } else {
                data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
                serverPlayer.teleportTo(serverLevel, cavePos.getX() + 0.5D, cavePos.getY() + 1.0D,
                        cavePos.getZ() + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
                data.wuxingxiantu$setTeleportCooldown(level.getGameTime() + 6000L);
                serverPlayer.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.cave_teleported"), true);
            }
        } else {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.cave_cooldown"), true);
        }
        GeneratedMod.sync(serverPlayer);
        return InteractionResult.CONSUME;
    }
}
