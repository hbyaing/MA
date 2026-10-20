package cn.blockforge.generated.generatedmod.importedcontent;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Four original herbs make one bath. All target players are already in the qi-condensation realm. */
public final class SpiritBathBlock extends Block implements EntityBlock {
    public static final BooleanProperty FILLED=BooleanProperty.create("filled");
    private static final VoxelShape SHAPE=Block.box(0,0,0,16,14,16),COLLISION=Block.box(0,0,0,16,4,16);
    public SpiritBathBlock(Properties properties) {super(properties);registerDefaultState(stateDefinition.any().setValue(FILLED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {builder.add(FILLED);}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return SHAPE;}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return COLLISION;}
    @Override public RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!(level.getBlockEntity(pos) instanceof SpiritBathBlockEntity bath))return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!player.isAlive()||player.isSpectator())return InteractionResult.PASS;
        if(state.getValue(FILLED)) {player.displayClientMessage(Component.translatable("content.wuxingxiantu.bath_ready"),false);return InteractionResult.CONSUME;}
        var held=player.getItemInHand(hand);
        if(!bath.deposit(held)) {player.displayClientMessage(Component.translatable("content.wuxingxiantu.bath_herbs"),true);return InteractionResult.CONSUME;}
        held.shrink(1);
        if(bath.isReady())level.setBlock(pos,state.setValue(FILLED,true),3);
        player.displayClientMessage(Component.translatable(bath.isReady()?"content.wuxingxiantu.bath_ready":"content.wuxingxiantu.bath_deposited",bath.depositCount()),true);
        return InteractionResult.CONSUME;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {return new SpiritBathBlockEntity(pos,state);}
    @Override @Nullable public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide||type!=ImportedContent.BATH_ENTITY.get())return null;
        return (tickLevel,pos,tickState,entity)->{if(entity instanceof SpiritBathBlockEntity bath)bath.serverTick();};
    }
    @Override public void onRemove(BlockState oldState,Level level,BlockPos pos,BlockState newState,boolean moving) {
        if(!oldState.is(newState.getBlock())&&level.getBlockEntity(pos) instanceof SpiritBathBlockEntity bath)
            for(var stack:bath.herbs())if(!stack.isEmpty())Block.popResource(level,pos,stack.copy());
        super.onRemove(oldState,level,pos,newState,moving);
    }
}
