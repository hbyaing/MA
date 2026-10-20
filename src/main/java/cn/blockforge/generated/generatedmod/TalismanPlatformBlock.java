package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** 符箓平台：镌印台 / 绘制台 / 拓印台共用同一方块逻辑，按 kind 区分行为。 */
public final class TalismanPlatformBlock extends BaseEntityBlock {
    private final TalismanPlatformBlockEntity.Kind kind;

    public TalismanPlatformBlock(TalismanPlatformBlockEntity.Kind kind,
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public TalismanPlatformBlockEntity.Kind kind() { return kind; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TalismanPlatformBlockEntity(pos, state, kind);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                             BlockEntityType<T> type) {
        return null;
    }

    @Override public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TalismanPlatformBlockEntity be
                ? be.menuProvider() : null;
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                            InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TalismanPlatformBlockEntity be
                && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // 随开界面把上次翻到的符箓页码一并写给客户端，下次打开就能接着翻。
            NetworkHooks.openScreen(serverPlayer, be.menuProvider(), buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(be.selectedIndex());
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof TalismanPlatformBlockEntity be) {
            net.minecraft.world.Containers.dropContents(level, pos, be);
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
