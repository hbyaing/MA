package cn.blockforge.generated.generatedmod.formation.carving;
import net.minecraft.core.BlockPos;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
public final class CarvingMenu extends AbstractContainerMenu {
 public final CarvingEye eye;public final BlockPos pos;public final ContainerData data;
 public static CarvingMenu fromNetwork(int id,Inventory inv,FriendlyByteBuf buf){var pos=buf.readBlockPos();return new CarvingMenu(id,inv,new CarvingEye(pos,inv.player.level().getBlockState(pos)));}
 public CarvingMenu(int id,Inventory inv,CarvingEye eye){super(CarvingRegistry.MENU.get(),id);this.eye=eye;pos=eye.getBlockPos();
 data=inv.player.level().isClientSide?new SimpleContainerData(11):new ContainerData(){public int getCount(){return 11;}public void set(int i,int value){}public int get(int i){return switch(i){case 0->eye.isEye()?1:0;case 1->eye.kind();case 2->eye.powered()?1:0;case 3->CarvingConfig.GRADES.get();case 4->CarvingConfig.cost(eye.kind())&65535;case 5->CarvingConfig.cost(eye.kind())>>>16;case 6->(int)(eye.charge()&65535);case 7->(int)(eye.charge()>>>16);case 8->eye.enabled()?1:0;case 9->CarvingConfig.configured(eye.kind())?1:0;case 10->eye.reserveGrade();default->0;};}};
 addDataSlots(data);
 for(int i=0;i<4;i++)addSlot(new Slot(eye.fuel,i,220+i*18,67){@Override public boolean mayPlace(ItemStack s){return data.get(0)==1&&SpiritStoneGrade.of(s.getItem())!=null;}@Override public boolean isActive(){return data.get(0)==1;}});
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,9+y*9+x,89+x*18,160+y*18));
 for(int x=0;x<9;x++)addSlot(new Slot(inv,x,89+x*18,218));
 }
 public int cost(){return(data.get(4)&65535)|((data.get(5)&65535)<<16);}public long reserve(){return(data.get(6)&65535)|((long)(data.get(7)&65535)<<16);}
 @Override public boolean stillValid(Player p){return p.level().isClientSide||p.isAlive()&&!p.isSpectator()&&p.level().getBlockEntity(pos)==eye&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().mayInteract(p,pos);}
 @Override public boolean clickMenuButton(Player p,int action){if(p.level().isClientSide||!stillValid(p)||!getCarried().isEmpty())return false;if(action>=0&&action<3&&!eye.isEye())eye.select(action);else if(action==10&&!eye.isEye()){return eye.carve(p);}else if(action==11&&eye.isEye())eye.toggle();else return false;broadcastChanges();return true;}
 @Override public ItemStack quickMoveStack(Player p,int id){if(!stillValid(p)||id<0||id>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(id);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var s=slot.getItem();var before=s.copy();if(id<4){if(!moveItemStackTo(s,4,40,true))return ItemStack.EMPTY;}else if(data.get(0)==1&&SpiritStoneGrade.of(s.getItem())!=null){if(!moveItemStackTo(s,0,4,false))return ItemStack.EMPTY;}else if(id<31){if(!moveItemStackTo(s,31,40,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,4,31,false))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();if(before.getCount()==s.getCount())return ItemStack.EMPTY;slot.onTake(p,s);return before;}
}
