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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 灵蕴台：吸收附近灵石块；右键一次放入一个物品，物品借鉴 Goety 祭坛基座的做法
 * 存入方块实体槽位，并以悬浮旋转的姿态展示在托盘之上（渲染见 PedestalFloatingRenderer）。
 * 基座造型为独立方块模型（底座-束腰-台面-托盘沿-角柱），碰撞形状与模型一致。
 */
public final class SpiritPlatformBlock extends BaseEntityBlock {
    /** 与 spirit_platform 方块模型对齐（r407 修长版）：底座/束腰 0..12.5，台面托盘 12.5..15，四角柱 13.5..15.5。 */
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
            net.minecraft.world.phys.shapes.Shapes.or(
                    box(2.0D, 0.0D, 2.0D, 14.0D, 12.5D, 14.0D),
                    box(2.5D, 12.5D, 2.5D, 13.5D, 15.0D, 13.5D),
                    box(2.0D, 13.5D, 2.0D, 3.0D, 15.5D, 3.0D),
                    box(13.0D, 13.5D, 2.0D, 14.0D, 15.5D, 3.0D),
                    box(2.0D, 13.5D, 13.0D, 3.0D, 15.5D, 14.0D),
                    box(13.0D, 13.5D, 13.0D, 14.0D, 15.5D, 14.0D));

    public SpiritPlatformBlock(Properties properties) {
        super(properties);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
            net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritPlatformBlockEntity(
                cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SPIRIT_PLATFORM.get(), pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                                BlockEntityType<T> type) {
        return type == cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SPIRIT_PLATFORM.get()
                ? (lvl, pos, blockState, entity) -> SpiritPlatformBlockEntity.tick(
                        lvl, pos, blockState, (SpiritPlatformBlockEntity) entity) : null;
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player.isSpectator()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof SpiritPlatformBlockEntity platform))
            return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        // —— 槽位已被占：先取回悬浮物品（goety 语义：即使手里拿着东西也一样取回，不直接交换） ——
        if (platform.hasFloating()) {
            ItemStack taken = platform.takeFloating();
            giveOrDrop(player, taken);
            server.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            auraBurst(server, pos, 12);
            player.displayClientMessage(Component.translatable(takenMessage(taken)), true);
            return InteractionResult.CONSUME;
        }

        // —— 空槽空手：查看灵气 ——
        if (held.isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.platform_aura", platform.aura(), SpiritPlatformBlockEntity.MAX_AURA), true);
            return InteractionResult.CONSUME;
        }

        // —— 空槽手持物品：一次只收一个，悬浮展示 ——
        String insertedMessage = SpiritPlatformBlockEntity.isCoarseCoal(held)
                ? "message.wuxingxiantu.platform_coal_inserted"
                : SpiritPlatformConversions.find(held) != null
                ? "message.wuxingxiantu.platform_nurture_inserted"
                : "message.wuxingxiantu.platform_item_inserted";
        ItemStack stored = held.copyWithCount(1);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
            if (held.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
        }
        if (!platform.insertStack(stored)) {
            giveOrDrop(player, stored);
            return InteractionResult.CONSUME;
        }
        server.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        auraBurst(server, pos, 20);
        player.displayClientMessage(Component.translatable(insertedMessage), true);
        return InteractionResult.CONSUME;
    }

    private static String takenMessage(ItemStack taken) {
        if (taken.is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK_ITEM.get())
                || taken.is(cn.blockforge.generated.generatedmod.WuXingItems.ETERNAL_SPIRIT_COAL.get())) {
            return "message.wuxingxiantu.platform_coal_taken_ready";
        }
        if (SpiritPlatformBlockEntity.isCoarseCoal(taken)) return "message.wuxingxiantu.platform_coal_taken";
        return "message.wuxingxiantu.platform_item_taken";
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    /** 放入/取出瞬间的灵气飞散小特效，突出“悬浮”交接感。 */
    private static void auraBurst(ServerLevel server, BlockPos pos, int count) {
        server.sendParticles(ParticleTypes.ENCHANT,
                pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                count, 0.3D, 0.25D, 0.3D, 0.06D);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof SpiritPlatformBlockEntity platform) {
            ItemStack shown = platform.takeFloating();
            if (!shown.isEmpty()) net.minecraft.world.Containers.dropItemStack(
                    level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, shown);
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
