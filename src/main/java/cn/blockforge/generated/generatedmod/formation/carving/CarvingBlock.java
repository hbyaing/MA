package cn.blockforge.generated.generatedmod.formation.carving;
import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;import net.minecraft.world.phys.shapes.*;import net.minecraftforge.network.NetworkHooks;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
public final class CarvingBlock extends BaseEntityBlock {
 public static final BooleanProperty ACTIVE=BooleanProperty.create("active");public final boolean eye;
 public CarvingBlock(boolean eye){super(BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.STONE).noOcclusion().lightLevel(s->s.getValue(ACTIVE)?10:0));this.eye=eye;registerDefaultState(stateDefinition.any().setValue(ACTIVE,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ACTIVE);}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(1,0,1,15,eye?5:9,15);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new CarvingEye(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,CarvingRegistry.TILE.get(),CarvingEye::tick);}
 @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity placer,net.minecraft.world.item.ItemStack stack){super.setPlacedBy(level,pos,state,placer,stack);if(!eye&&!level.isClientSide&&placer instanceof ServerPlayer player&&level.getBlockEntity(pos) instanceof CarvingEye be)NetworkHooks.openScreen(player,be,pos);}
 @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
  if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.CONSUME;
  if(!l.isClientSide&&player instanceof ServerPlayer server&&l.getBlockEntity(pos) instanceof CarvingEye be&&l.mayInteract(player,pos)){
   var held=player.getItemInHand(hand);
   if(eye&&!player.isShiftKeyDown()&&!held.isEmpty()&&SpiritStoneGrade.of(held.getItem())!=null){int moved=be.insertStone(held,player.isCreative());player.displayClientMessage(net.minecraft.network.chat.Component.literal(moved>0?"已向阵眼放入灵石":"阵眼灵石槽已满"),true);}
   else NetworkHooks.openScreen(server,be,pos);
  }
  return InteractionResult.sidedSuccess(l.isClientSide);
 }
 @Override public void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){if(!l.isClientSide&&old.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof CarvingEye be){Containers.dropContents(l,p,be.fuel);l.updateNeighbourForOutputSignal(p,this);}super.onRemove(old,l,p,next,moving);}
}
