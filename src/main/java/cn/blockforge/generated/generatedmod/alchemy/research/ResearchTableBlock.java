package cn.blockforge.generated.generatedmod.alchemy.research;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
public final class ResearchTableBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public ResearchTableBlock(){super(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ResearchTableEntity(p,s);}
    @Override public net.minecraft.world.InteractionResult use(BlockState s,Level l,BlockPos p,net.minecraft.world.entity.player.Player u,net.minecraft.world.InteractionHand h,net.minecraft.world.phys.BlockHitResult hit){if(u instanceof net.minecraft.server.level.ServerPlayer player&&l.getBlockEntity(p) instanceof ResearchTableEntity table)net.minecraftforge.network.NetworkHooks.openScreen(player,table.menuProvider(),p);return net.minecraft.world.InteractionResult.sidedSuccess(l.isClientSide);}
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(s.getBlock()!=n.getBlock()&&l.getBlockEntity(p) instanceof ResearchTableEntity t)net.minecraft.world.Containers.dropContents(l,p,t);super.onRemove(s,l,p,n,moving);}
}
