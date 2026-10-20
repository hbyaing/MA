package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 承华台：可承载物品的基座（Goety 祭坛基座语义），右键放入/取回一叠物品。
 * 同时充当炼器台的素材入口——启动炼制时优先从炼器台周围的承华台上抽取素材，
 * 素材会以实体形态飞入中央异火中完成炼制（见 cn.blockforge.generated.generatedmod.network.packet.alchemy.RefiningPacket.handle）。
 */
public final class ChenghuaPedestalBlock extends BaseEntityBlock {
    /** 托盘沿顶 14/16，碰撞与视觉形状统一收在这个高度。 */
    private static final VoxelShape SHAPE = Shapes.or(
            box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D),
            box(1.5D, 12.0D, 1.5D, 14.5D, 14.0D, 14.5D));

    public ChenghuaPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                         CollisionContext context) {
        return SHAPE;
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChenghuaPedestalBlockEntity(
                cn.blockforge.generated.generatedmod.registry.ModBlockEntities.CHENGHUA_PEDESTAL.get(), pos, state);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player.isSpectator()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ChenghuaPedestalBlockEntity pedestal))
            return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        // —— 槽位已被占：先取回承载物品 ——
        if (pedestal.hasFloating()) {
            ItemStack taken = pedestal.takeFloating();
            giveOrDrop(player, taken);
            server.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            auraBurst(server, pos);
            player.displayClientMessage(
                    Component.translatable("message.wuxingxiantu.pedestal_item_taken"), true);
            return InteractionResult.CONSUME;
        }

        // —— 空槽空手：查看状态 ——
        if (held.isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.pedestal_idle"), true);
            return InteractionResult.CONSUME;
        }

        // —— 空槽手持物品：整叠放入，悬浮展示 ——
        ItemStack stored;
        if (player.getAbilities().instabuild) {
            stored = held.copyWithCount(1);
        } else if (held.getCount() <= held.getMaxStackSize()) {
            stored = held.copy();
            player.setItemInHand(hand, ItemStack.EMPTY);
        } else {
            stored = held.split(held.getMaxStackSize());
        }
        if (!pedestal.insertStack(stored)) {
            giveOrDrop(player, stored);
            return InteractionResult.CONSUME;
        }
        server.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        auraBurst(server, pos);
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.pedestal_item_inserted"), true);
        return InteractionResult.CONSUME;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    /** 放入/取出瞬间的灵气飞散小特效，突出“悬浮”交接感。 */
    private static void auraBurst(ServerLevel server, BlockPos pos) {
        server.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                10, 0.25D, 0.2D, 0.25D, 0.05D);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof ChenghuaPedestalBlockEntity pedestal) {
            ItemStack shown = pedestal.takeFloating();
            if (!shown.isEmpty()) net.minecraft.world.Containers.dropItemStack(
                    level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, shown);
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    /** 结构校验等外部逻辑用的便捷判定。 */
    public static boolean isPedestal(BlockState state) {
        return state.getBlock() instanceof ChenghuaPedestalBlock;
    }

    /** 承华台被炼器台抽取素材后刷新方块（保留状态、触发一次光照同步）。 */
    public static void refresh(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isPedestal(state)) level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    }
}
