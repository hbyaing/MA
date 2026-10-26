package cn.blockforge.generated.generatedmod.formation.carving;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import net.minecraft.core.*;import net.minecraft.nbt.*;import net.minecraft.network.chat.Component;import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;import net.minecraft.world.level.block.entity.BlockEntity;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.level.chunk.*;import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.BlockSnapshot;import net.minecraftforge.event.ForgeEventFactory;
public final class CarvingEye extends BlockEntity implements MenuProvider {
 public static final String[] NAMES={"聚灵阵","防护阵","禁法结界"};
 public final SimpleContainer fuel=new SimpleContainer(4){@Override public void setChanged(){super.setChanged();CarvingEye.this.sync();}};
 private int kind,reserveGrade;private long charge;private boolean enabled=true;private long lastTick=Long.MIN_VALUE;
 public CarvingEye(BlockPos p,BlockState s){super(CarvingRegistry.TILE.get(),p,s);}
 public int kind(){return kind;}public long charge(){return charge;}public int reserveGrade(){return reserveGrade;}public boolean enabled(){return enabled;}
 public boolean isEye(){return getBlockState().is(CarvingRegistry.EYE.get());}
 public boolean powered(){return isEye()&&getBlockState().getValue(CarvingBlock.ACTIVE);}
 public void select(int type){if(!isEye()&&type>=0&&type<3){kind=type;sync();}}
 public void toggle(){enabled=!enabled;if(!enabled)setPowered(false);sync();}
 private boolean allowed(int grade){return grade>=0&&grade<4&&(CarvingConfig.GRADES.get()&(1<<grade))!=0;}
 public int insertStone(ItemStack held,boolean creative){if(!isEye()||held.isEmpty()||SpiritStoneGrade.of(held.getItem())==null)return 0;ItemStack remaining=held.copy();for(int i=0;i<4;i++){ItemStack slot=fuel.getItem(i);if(!slot.isEmpty()&&!ItemStack.isSameItemSameTags(slot,held))continue;int n=Math.min(remaining.getCount(),64-slot.getCount());if(n<=0)continue;if(slot.isEmpty())fuel.setItem(i,held.copyWithCount(n));else{slot.grow(n);fuel.setChanged();}remaining.shrink(n);if(remaining.isEmpty())break;}int moved=held.getCount()-remaining.getCount();if(!creative)held.shrink(moved);if(moved>0)sync();return moved;}
 /** Transactional preflight: insufficient value never consumes a partial stone stack. */
 public boolean spendTick(int cost,int allowedMask){
  if(cost<=0||allowedMask==0)return false;
  if(charge>0&&(allowedMask&(1<<reserveGrade))==0)return false;
  long available=charge;
  for(int i=0;i<4;i++){var stack=fuel.getItem(i);var grade=SpiritStoneGrade.of(stack.getItem());if(grade!=null&&(allowedMask&(1<<grade.ordinal()))!=0)available+=(long)stack.getCount()*grade.value*1200;}
  if(available<cost)return false;
  for(int i=0;i<4&&charge<cost;i++){var stack=fuel.getItem(i);var grade=SpiritStoneGrade.of(stack.getItem());if(grade==null||(allowedMask&(1<<grade.ordinal()))==0)continue;while(!stack.isEmpty()&&charge<cost){charge+=(long)grade.value*1200;reserveGrade=grade.ordinal();stack.shrink(1);}fuel.setChanged();}
  charge-=cost;setChanged();return true;
 }
 public static void tick(Level l,BlockPos p,BlockState state,CarvingEye be){if(l.isClientSide||!be.isEye()||be.lastTick==l.getGameTime())return;be.lastTick=l.getGameTime();boolean running=be.enabled&&be.spendTick(CarvingConfig.cost(be.kind),CarvingConfig.GRADES.get());be.setPowered(running);if(running&&l.getGameTime()%20==0)be.sync();}
 private void setPowered(boolean active){if(level!=null&&powered()!=active&&isEye()){level.setBlock(worldPosition,getBlockState().setValue(CarvingBlock.ACTIVE,active),3);sync();}}
 public boolean carve(Player p){
  if(level==null||level.isClientSide||isEye()||!fuel.isEmpty()||p.isSpectator()||p.level()!=level||level.getBlockEntity(worldPosition)!=this||p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64||!level.mayInteract(p,worldPosition)||!p.mayUseItemAt(worldPosition,Direction.UP,new ItemStack(CarvingRegistry.CORE_ITEM.get()))||!p.containerMenu.getCarried().isEmpty())return false;
  var snapshot=BlockSnapshot.create(level.dimension(),level,worldPosition);
  if(!level.setBlock(worldPosition,CarvingRegistry.EYE.get().defaultBlockState(),3))return false;
  if(ForgeEventFactory.onBlockPlace(p,snapshot,Direction.UP)){snapshot.restore(true,false);return false;}
  if(!(level.getBlockEntity(worldPosition) instanceof CarvingEye next)){snapshot.restore(true,false);return false;}
  next.kind=kind;next.charge=charge;next.reserveGrade=reserveGrade;next.sync();
  if(p instanceof ServerPlayer server)net.minecraftforge.network.NetworkHooks.openScreen(server,next,worldPosition);
  return true;
 }
 public static boolean gatheringAt(Player player){var level=player.level();var at=player.blockPosition();int r=(int)Math.ceil(CarvingConfig.RADIUS.get());for(int cx=(at.getX()-r)>>4;cx<=(at.getX()+r)>>4;cx++)for(int cz=(at.getZ()-r)>>4;cz<=(at.getZ()+r)>>4;cz++){
  var access=level.getChunk(cx,cz,ChunkStatus.FULL,false);if(!(access instanceof LevelChunk chunk))continue;
  for(var block:chunk.getBlockEntities().values())if(block instanceof CarvingEye be&&be.kind==0&&be.powered()&&Math.abs(player.getY()-be.worldPosition.getY())<3&&player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(be.worldPosition))<=CarvingConfig.RADIUS.get()*CarvingConfig.RADIUS.get())return true;
 }return false;}
 @Override public Component getDisplayName(){return Component.literal(isEye()?NAMES[kind]+" · 阵眼":"阵法刻画核心");}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new CarvingMenu(id,inv,this);}
 @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("FormationKind",kind);tag.putLong("FormationCharge",charge);tag.putInt("ReserveGrade",reserveGrade);tag.putBoolean("Enabled",enabled);ContainerHelper.saveAllItems(tag,net.minecraft.core.NonNullList.of(ItemStack.EMPTY,fuel.getItem(0),fuel.getItem(1),fuel.getItem(2),fuel.getItem(3)));}
 @Override public void load(CompoundTag tag){super.load(tag);kind=Math.max(0,Math.min(2,tag.getInt("FormationKind")));reserveGrade=Math.max(0,Math.min(3,tag.getInt("ReserveGrade")));charge=Math.max(0,Math.min(874800L,tag.getLong("FormationCharge")));enabled=!tag.contains("Enabled")||tag.getBoolean("Enabled");var items=NonNullList.withSize(4,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items);for(int i=0;i<4;i++){var stack=items.get(i);if(SpiritStoneGrade.of(stack.getItem())==null)stack=ItemStack.EMPTY;else if(stack.getCount()>64)stack.setCount(64);fuel.setItem(i,stack);}lastTick=Long.MIN_VALUE;}
 public void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(16);}
}
