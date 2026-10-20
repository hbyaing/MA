package cn.blockforge.generated.generatedmod.storage.menu;
import cn.blockforge.generated.generatedmod.storage.*;
import cn.blockforge.generated.generatedmod.storage.StorageBagItem;
import cn.blockforge.generated.generatedmod.storage.StorageRegistry;
import cn.blockforge.generated.generatedmod.crafting.BoundEquipmentCrafting;
import cn.blockforge.generated.generatedmod.crafting.EquipmentBond;
import cn.blockforge.generated.generatedmod.ParalysisControl;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.server.level.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;

public final class BagMenu extends AbstractContainerMenu {
 public final UUID bagId;private final Player player;private final int bagSlot;
 private final CraftingContainer craft=new TransientCraftingContainer(this,3,3);private final ResultContainer result=new ResultContainer();
 private final Container storage;private final BagData data;private final BagInventory inventory;
 public final ContainerData info=new SimpleContainerData(3);public final long[] totals=new long[54];private final int[] indices=new int[54];
 private String query="";private int category=0;private int sentRow=-1,sentRanges=-1;private int sentMagnet=-1;private long[] sentTotals=new long[54];
 public static BagMenu create(int id,Inventory inv,FriendlyByteBuf buf){return new BagMenu(id,inv,buf.readVarInt(),buf.readUUID());}
 public BagMenu(int id,Inventory inv,int bagSlot){this(id,inv,bagSlot,null);}
 private BagMenu(int id,Inventory inv,int bagSlot,UUID synchronizedId){super(StorageRegistry.STORAGE_BAG.get(),id);this.player=inv.player;this.bagSlot=bagSlot;
  var bag=inv.getItem(bagSlot);bagId=synchronizedId!=null?synchronizedId:bag.hasTag()&&bag.getTag().hasUUID("bag_id")?bag.getTag().getUUID("bag_id"):null;
  data=player instanceof ServerPlayer sp&&bagId!=null?BagData.get(sp.serverLevel()):null;inventory=data==null?new BagInventory():data.inventory(bagId);
  Arrays.fill(indices,-1);storage=data==null?new SimpleContainer(54):new Page();
  addSlot(new ResultSlot(player,craft,result,0,270,80){@Override public boolean mayPickup(Player p){return canInteract(p)&&EquipmentBond.canConsume(p,craft.getItems());}});
  for(int y=0;y<6;y++)for(int x=0;x<9;x++)addSlot(new Slot(storage,y*9+x,8+x*18,40+y*18){@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean mayPickup(Player p){return false;}});
  for(int y=0;y<3;y++)for(int x=0;x<3;x++)addSlot(new Slot(craft,y*3+x,194+x*18,58+y*18){@Override public boolean mayPlace(ItemStack s){return !(s.getItem() instanceof StorageBagItem);}});
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addPlayerSlot(inv,9+y*9+x,8+x*18,158+y*18);
  for(int x=0;x<9;x++)addPlayerSlot(inv,x,8+x*18,216);
  refresh();sentRow=-1;sentRanges=-1;
 }
 public boolean magnet(){var bag=player.getInventory().getItem(bagSlot);return bag.hasTag()&&bag.getTag().getBoolean("bag_magnet");}
 private void syncInfo(boolean force){if(data==null)return;info.set(2,magnet()?1:0);for(int i=0;i<54;i++)totals[i]=indices[i]<0?0:inventory.entries.get(indices[i]).total();if(player instanceof ServerPlayer sp&&(force||sentRow!=info.get(0)||sentRanges!=info.get(1)||sentMagnet!=info.get(2)||!Arrays.equals(sentTotals,totals))){sentRow=info.get(0);sentRanges=info.get(1);sentMagnet=info.get(2);sentTotals=totals.clone();cn.blockforge.generated.generatedmod.Network.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->sp),new cn.blockforge.generated.generatedmod.storage.network.BagStatePacket(containerId,sentRow,sentRanges,sentMagnet!=0,totals.clone()));}}
 @Override public void broadcastChanges(){super.broadcastChanges();if(info!=null)syncInfo(false);}
 @Override public void broadcastFullState(){super.broadcastFullState();syncInfo(true);}
 private void addPlayerSlot(Inventory inv,int index,int x,int y){addSlot(new Slot(inv,index,x,y){@Override public boolean mayPickup(Player p){return index!=bagSlot;}@Override public boolean mayPlace(ItemStack s){return index!=bagSlot;}});}
 private void refresh(){if(data==null)return;inventory.compact();var page=BagPaging.scroll(inventory.entries,e->BagFilter.matches(e.display(),query,category),info.get(0),9,6);info.set(0,page.number());info.set(1,page.pages());System.arraycopy(page.indices(),0,indices,0,54);broadcastChanges();}
 public void storageChanged(){refresh();}
 public void scrollTo(int row){if(data!=null&&canInteract(player)){info.set(0,row);refresh();}}
 public void filter(String q,int cat){if(data==null||!canInteract(player))return;query=q.length()>80?q.substring(0,80):q;category=Math.max(0,Math.min(BagFilter.LABELS.length-1,cat));info.set(0,0);refresh();}
 @Override public void clicked(int slot,int button,ClickType type,Player p){
  if(data==null){if(player.level().isClientSide)super.clicked(slot,button,type,p);return;}
  if(!canInteract(p)||(type==ClickType.SWAP&&button==bagSlot))return;
  if(slot>=1&&slot<=54&&type!=ClickType.QUICK_CRAFT){int view=slot-1;BagInventory.Entry entry=indices[view]<0?null:inventory.entries.get(indices[view]);ItemStack cursor=getCarried();
   if(type==ClickType.QUICK_MOVE){quickMoveStack(p,slot);return;}
   if(type==ClickType.PICKUP){if(cursor.isEmpty()&&entry!=null)setCarried(entry.take(button==1?1:entry.display().getMaxStackSize()));else if(!cursor.isEmpty()&&!(cursor.getItem() instanceof StorageBagItem)){if(button==1){if(inventory.insert(cursor.copyWithCount(1)))cursor.shrink(1);}else inventory.insert(cursor);}}
   else if(type==ClickType.THROW&&entry!=null){p.drop(entry.take(button==0?1:entry.display().getMaxStackSize()),false);}
   else if(type==ClickType.CLONE&&p.getAbilities().instabuild&&entry!=null){ItemStack copy=entry.display();copy.setCount(copy.getMaxStackSize());setCarried(copy);}
   else if(type==ClickType.SWAP&&(button>=0&&button<=8||button==40)&&button!=bagSlot){ItemStack held=p.getInventory().getItem(button);if(!(held.getItem() instanceof StorageBagItem)){if(!held.isEmpty()&&!inventory.insert(held))return;p.getInventory().setItem(button,entry==null?ItemStack.EMPTY:entry.take(entry.display().getMaxStackSize()));}}
   else if(type==ClickType.PICKUP_ALL&&!cursor.isEmpty()){for(var e:inventory.entries){ItemStack taken=e.takeMatching(cursor,cursor.getMaxStackSize()-cursor.getCount());cursor.grow(taken.getCount());if(cursor.getCount()>=cursor.getMaxStackSize())break;}}
   data.setDirty();refresh();broadcastFullState();return;
  }
  super.clicked(slot,button,type,p);if(type!=ClickType.QUICK_CRAFT)refresh();
 }
 @Override public boolean clickMenuButton(Player p,int id){if(data==null||!canInteract(p))return false;if(id==0)info.set(0,info.get(0)-1);else if(id==1)info.set(0,info.get(0)+1);else if(id==2)sort();else if(id==3){var bag=p.getInventory().getItem(bagSlot);bag.getOrCreateTag().putBoolean("bag_magnet",!magnet());p.getInventory().setChanged();}else return false;refresh();return true;}
 private void sort(){inventory.sort();data.setDirty();}
 @Override public ItemStack quickMoveStack(Player p,int index){
  if(data==null||!canInteract(p)||index<0||index>=slots.size())return ItemStack.EMPTY;
  if(index>=1&&index<=54){int mapped=indices[index-1];if(mapped<0)return ItemStack.EMPTY;var entry=inventory.entries.get(mapped);ItemStack first=ItemStack.EMPTY;while(entry.total()>0){ItemStack offered=entry.preview(entry.display().getMaxStackSize());if(offered.isEmpty())break;ItemStack copy=offered.copy();if(!moveItemStackTo(offered,64,100,false))break;int moved=copy.getCount()-offered.getCount();if(moved<=0)break;entry.take(moved);if(first.isEmpty())first=copy;}data.setDirty();refresh();return first;}
  Slot slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;ItemStack stack=slot.getItem(),copy=stack.copy();
  if(index>=64){if(!inventory.insert(stack))return ItemStack.EMPTY;data.setDirty();}
  else if(index==0){if(!moveItemStackTo(stack,64,100,true))return ItemStack.EMPTY;slot.onQuickCraft(stack,copy);}else if(!moveItemStackTo(stack,64,100,false))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);if(index==0&&!stack.isEmpty())p.drop(stack,false);refresh();return copy;
 }
 @Override public void slotsChanged(Container c){if(player instanceof ServerPlayer sp){if(BoundEquipmentCrafting.update(this,sp.serverLevel(),sp,craft,result)){broadcastChanges();return;}var recipe=sp.serverLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft,sp.serverLevel());ItemStack out=ItemStack.EMPTY;if(recipe.isPresent()&&result.setRecipeUsed(sp.serverLevel(),sp,recipe.get())){var assembled=recipe.get().assemble(craft,sp.serverLevel().registryAccess());if(assembled.isItemEnabled(sp.serverLevel().enabledFeatures()))out=assembled;}result.setItem(0,out);broadcastChanges();}}
 @Override public boolean stillValid(Player p){if(p!=player||!p.isAlive()||bagSlot<0||bagSlot>=p.getInventory().getContainerSize())return false;ItemStack bag=p.getInventory().getItem(bagSlot);return bag.getItem() instanceof StorageBagItem&&bag.hasTag()&&bag.getTag().hasUUID("bag_id")&&Objects.equals(bagId,bag.getTag().getUUID("bag_id"));}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide){clearContainer(p,craft);if(data!=null)data.setDirty();}}
 private boolean canInteract(Player p){return p==player&&stillValid(p)&&!ParalysisControl.blocked(p);}
 private final class Page extends SimpleContainer{
  Page(){super(54);}
  @Override public ItemStack getItem(int i){return indices[i]<0?ItemStack.EMPTY:inventory.entries.get(indices[i]).display();}
  @Override public void setItem(int i,ItemStack s){}
  @Override public ItemStack removeItem(int i,int count){return ItemStack.EMPTY;}
  @Override public ItemStack removeItemNoUpdate(int i){return ItemStack.EMPTY;}
  @Override public void setChanged(){}
 }
}
