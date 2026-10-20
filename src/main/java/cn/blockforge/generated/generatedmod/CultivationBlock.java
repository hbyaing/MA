package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 可交互的修炼功能方块：聚灵、洞府标记与打坐共用轻量方块实现。 */
public final class CultivationBlock extends Block {
    public enum Kind { ALTAR, ARRAY, CAVE, CUSHION, SPIRIT_CUSHION, PLATFORM }
    private static final VoxelShape CUSHION_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    private static final VoxelShape WOVEN_CUSHION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    private static final VoxelShape SPIRIT_CUSHION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D);
    private static final VoxelShape PLATFORM_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(1.0D, 2.0D, 1.0D, 15.0D, 4.0D, 15.0D),
            Block.box(3.0D, 4.0D, 3.0D, 13.0D, 6.0D, 13.0D));
    private final Kind kind;
    private final boolean wideCushion;

    public CultivationBlock(Kind kind, BlockBehaviour.Properties properties) {
        this(kind, properties, false);
    }

    public CultivationBlock(Kind kind, BlockBehaviour.Properties properties, boolean wideCushion) {
        super(properties);
        this.kind = kind;
        this.wideCushion = wideCushion;
    }

    public Kind kind() { return kind; }

    public boolean isMeditationCushion() {
        return kind == Kind.CUSHION || kind == Kind.SPIRIT_CUSHION || kind == Kind.PLATFORM;
    }

    public double meditationSurfaceHeight() {
        return switch (kind) {
            case CUSHION -> 3.0D / 16.0D;
            case SPIRIT_CUSHION -> 4.0D / 16.0D;
            case PLATFORM -> 6.0D / 16.0D;
            default -> 1.0D;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (kind) {
            case CUSHION -> wideCushion ? WOVEN_CUSHION_SHAPE : CUSHION_SHAPE;
            case SPIRIT_CUSHION -> SPIRIT_CUSHION_SHAPE;
            case PLATFORM -> PLATFORM_SHAPE;
            default -> super.getShape(state, level, pos, context);
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private int meditationTier() {
        return switch (kind) {
            case CUSHION -> 0;
            case SPIRIT_CUSHION -> 1;
            case PLATFORM -> 2;
            default -> 0;
        };
    }

    public static void startMeditation(ServerPlayer player, CultivationData data, BlockPos pos, int tier) {
        if (player.isPassenger() || player.isShiftKeyDown() || !player.isAlive()) return;
        if (player.getDeltaMovement().horizontalDistanceSqr() > 0.0025D || player.getAbilities().flying) return;
        if (!player.level().getEntitiesOfClass(MeditationSeatEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(0.1D),
                seat -> seat.isAlive() && seat.cushionPos().equals(pos) && !seat.getPassengers().isEmpty()).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.cushion_occupied"), true);
            return;
        }
        MeditationSeatEntity seat = new MeditationSeatEntity(player.level(), pos, tier);
        player.level().addFreshEntity(seat);
        if (!player.startRiding(seat, true)) {
            seat.discard();
            return;
        }
        data.wuxingxiantu$setMeditating(true);
        seat.setMeditating(true);
        data.wuxingxiantu$setMeditationPos(pos);
        data.wuxingxiantu$setMeditationTier(tier);
        player.setPose(net.minecraft.world.entity.Pose.SITTING);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.meditation_started"), true);
    }

    public static void stopMeditation(ServerPlayer player, CultivationData data, boolean notify) {
        if (player.getVehicle() instanceof MeditationSeatEntity seat) {
            seat.setMeditating(false);
            player.stopRiding();
            seat.discard();
        }
        clearMeditationState(player, data);
        if (notify) player.displayClientMessage(Component.translatable("message.wuxingxiantu.meditation_stopped"), true);
    }

    public static void clearMeditationState(ServerPlayer player, CultivationData data) {
        data.wuxingxiantu$setMeditating(false);
        data.wuxingxiantu$setMeditationPos(null);
        data.wuxingxiantu$setMeditationTier(0);
        if (player.getPose() == net.minecraft.world.entity.Pose.SITTING) {
            player.setPose(net.minecraft.world.entity.Pose.STANDING);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (isMeditationCushion()) {
            // Shift is a dismount request. A held use packet must not immediately mount again.
            if (player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer server && server.getVehicle() instanceof MeditationSeatEntity
                        && GeneratedMod.data(server) != null) stopMeditation(server, GeneratedMod.data(server), false);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (player.isUsingItem() || player.getItemInHand(hand).getUseDuration() > 0) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.CONSUME;
            if (player instanceof ServerPlayer server &&
                    cn.blockforge.generated.generatedmod.event.MeditationEvents.blocksMeditation(server)) return InteractionResult.CONSUME;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        CultivationData data = GeneratedMod.data(serverPlayer);
        if (data == null) return InteractionResult.PASS;
        switch (kind) {
            case CAVE -> {
                if (data.wuxingxiantu$getCavePos() == null) {
                    data.wuxingxiantu$setCavePos(pos);
                    serverPlayer.displayClientMessage(Component.translatable(
                            "message.wuxingxiantu.cave_marked", CaveTokenBlock.PROTECTION_RADIUS), false);
                } else if (data.wuxingxiantu$getTeleportCooldown() <= level.getGameTime()) {
                    int cost = Math.max(1, Math.round(CultivationLogic.maxAura(data) * 0.10F));
                    if (data.wuxingxiantu$getAura() < cost) {
                        serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"), true);
                    } else {
                        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
                        BlockPos target = data.wuxingxiantu$getCavePos();
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverPlayer.teleportTo(serverLevel, target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D,
                                    serverPlayer.getYRot(), serverPlayer.getXRot());
                        }
                        data.wuxingxiantu$setTeleportCooldown(level.getGameTime() + 6000L);
                        serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.cave_teleported"), true);
                    }
                } else {
                    serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.cave_cooldown"), true);
                }
            }
            case CUSHION, SPIRIT_CUSHION, PLATFORM -> {
                BlockPos meditationPos = data.wuxingxiantu$getMeditationPos();
                if (data.wuxingxiantu$isMeditating() && pos.equals(meditationPos)
                        && serverPlayer.getVehicle() instanceof MeditationSeatEntity) {
                    stopMeditation(serverPlayer, data, true);
                } else if (!data.wuxingxiantu$isMeditating()) {
                    startMeditation(serverPlayer, data, pos, meditationTier());
                }
            }
            case ALTAR -> serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.platform_ready"), true);
            case ARRAY -> serverPlayer.displayClientMessage(Component.translatable("message.wuxingxiantu.array_active"), true);
        }
        GeneratedMod.sync(serverPlayer);
        return InteractionResult.CONSUME;
    }
}
