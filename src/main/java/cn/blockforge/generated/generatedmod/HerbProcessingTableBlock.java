package cn.blockforge.generated.generatedmod;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

/** Ordinary full cube with six textures; processing recipes are not yet part of this block. */
public final class HerbProcessingTableBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public HerbProcessingTableBlock() {
        super(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,BlockState state){return new HerbProcessingTableBlockEntity(pos,state);}
    @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){
        return level.isClientSide?null:createTickerHelper(type,cn.blockforge.generated.generatedmod.registry.ModBlockEntities.HERB_PROCESSING_TABLE.get(),HerbProcessingTableBlockEntity::tick);
    }
    @Override public net.minecraft.world.MenuProvider getMenuProvider(BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos){return level.getBlockEntity(pos) instanceof HerbProcessingTableBlockEntity table?table.menuProvider():null;}
    @Override public net.minecraft.world.InteractionResult use(BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        if(!level.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer server&&level.getBlockEntity(pos) instanceof HerbProcessingTableBlockEntity table)
            net.minecraftforge.network.NetworkHooks.openScreen(server,table.menuProvider(),pos);
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,BlockState next,boolean moving){
        if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof HerbProcessingTableBlockEntity table){net.minecraft.world.Containers.dropContents(level,pos,table);level.updateNeighbourForOutputSignal(pos,this);}
        super.onRemove(state,level,pos,next,moving);
    }
}
