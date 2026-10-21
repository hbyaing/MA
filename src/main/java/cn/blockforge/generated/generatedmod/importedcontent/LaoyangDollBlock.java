package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** The source's two dolls share direction/model rules; only Laoyang dispenses pills. */
public final class LaoyangDollBlock extends HorizontalDirectionalBlock {
    public static final String NEXT="MyriadLaoyangNext";
    private final boolean givesPills;
    public LaoyangDollBlock(boolean givesPills,Properties properties) {super(properties);this.givesPills=givesPills;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState state,Rotation rotation) {return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public BlockState mirror(BlockState state,Mirror mirror) {return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return Block.box(2,0,2,14,16,14);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!givesPills||hand==InteractionHand.OFF_HAND||player.isSpectator()||!player.isAlive())return InteractionResult.PASS;
        if(!level.isClientSide) {
            var data=player.getPersistentData();long now=level.getGameTime();
            if(data.contains(NEXT)&&now<data.getLong(NEXT))return InteractionResult.CONSUME;
            data.putLong(NEXT,now+40);GeneratedMod.giveOrDrop(player,new ItemStack(ImportedContent.LAOYANG_PILL.get()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
