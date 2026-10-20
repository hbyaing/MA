package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

/** 愈魂室：消耗灵魂沙/灵魂土修复破碎魂珠。 */
public final class SoulHealingRoomBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public SoulHealingRoomBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
    }
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulHealingRoomBlockEntity(
                cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SOUL_HEALING_ROOM.get(), pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                                BlockEntityType<T> type) {
        return type == cn.blockforge.generated.generatedmod.registry.ModBlockEntities.SOUL_HEALING_ROOM.get()
                ? (lvl, pos, blockState, entity) -> SoulHealingRoomBlockEntity.tick(
                        lvl, pos, blockState, (SoulHealingRoomBlockEntity) entity) : null;
    }

    @Override public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SoulHealingRoomBlockEntity room ? room.menuProvider() : null;
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof SoulHealingRoomBlockEntity room)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // 统一进度门槛：愈魂室投料同样校验操作者本人境界；界面仍可参观。
            if (ProgressionGate.canUseSoulSystem(player, ProgressionGate.SOUL_FURNACE)) {
                room.insertHeldItem(player, hand);
            } else {
                ProgressionGate.deny(player, ProgressionGate.MSG_SOUL,
                        ProgressionGate.requiredRealmForSoulFeature(ProgressionGate.SOUL_FURNACE));
            }
            NetworkHooks.openScreen(serverPlayer, room.menuProvider(), pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!moving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof SoulHealingRoomBlockEntity) {
            // 愈魂室破坏时不掉落任何物品：既不掉方块本体，也不掉内部正在修复的魂珠。
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    /** 愈魂室破坏时方块本体不掉落，符合"破坏时的掉落物消失"。 */
    @Override public java.util.List<net.minecraft.world.item.ItemStack> getDrops(
            BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }
}
