package cn.blockforge.generated.generatedmod.robe;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Live capability-backed storage: open menus never retain a second copy of equipment. */
public final class RobeContainer implements Container {
    private final Player owner;
    public RobeContainer(Player owner){this.owner=owner;}
    @Override public int getContainerSize(){return 2;}
    @Override public boolean isEmpty(){return getItem(0).isEmpty()&&getItem(1).isEmpty();}
    @Override public ItemStack getItem(int index){var data=GeneratedMod.data(owner);return data==null?ItemStack.EMPTY:
            index==0?data.wuxingxiantu$getRobeStack():index==1?data.wuxingxiantu$getYujianStack():ItemStack.EMPTY;}
    @Override public ItemStack removeItem(int index,int count){
        if(index<0||index>1||count<=0)return ItemStack.EMPTY;
        if(index==1&&YujianSlot.locked(owner)&&owner instanceof net.minecraft.server.level.ServerPlayer server)YujianController.stop(server);
        ItemStack removed=getItem(index).split(count);if(!removed.isEmpty())setChanged();return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int index){
        if(index==1&&YujianSlot.locked(owner)&&owner instanceof net.minecraft.server.level.ServerPlayer server)YujianController.stop(server);
        ItemStack old=getItem(index);setItem(index,ItemStack.EMPTY);return old;
    }
    @Override public void setItem(int index,ItemStack stack){
        if(owner.level().isClientSide){
            var data=GeneratedMod.data(owner);if(data==null)return;
            if(index==0)data.wuxingxiantu$setRobeStack(stack);
            else if(index==1)data.wuxingxiantu$setYujianStack(stack);
        }else if(index==0)RobeSlots.setRobe(owner,stack);else if(index==1)YujianSlot.set(owner,stack);
    }
    @Override public int getMaxStackSize(){return 1;}
    @Override public void setChanged(){if(owner instanceof ServerPlayer server)RobeSlots.sync(server);}
    @Override public boolean stillValid(Player player){return player==owner&&owner.isAlive();}
    @Override public boolean canPlaceItem(int index,ItemStack stack){return index==0?RobeSlots.canEquipRobe(stack):index==1&&YujianSlot.accepts(stack);}
    @Override public void clearContent(){RobeSlots.setRobe(owner,ItemStack.EMPTY);YujianSlot.set(owner,ItemStack.EMPTY);}
}
