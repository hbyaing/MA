package cn.blockforge.generated.generatedmod.robe;

import cn.blockforge.generated.generatedmod.registry.ModMenus;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class RobeMenu extends AbstractContainerMenu {
    private final Container robe;
    private final Player owner;
    public RobeMenu(int id,Inventory inventory,Container robe){
        super(ModMenus.ROBE.get(),id);this.robe=robe;this.owner=inventory.player;checkContainerSize(robe,2);
        addSlot(new EquipmentSlots.EquipmentSlot(robe,owner,0,53,30));
        addSlot(new EquipmentSlots.EquipmentSlot(robe,owner,YujianSlot.YUJIAN_SLOT,107,30));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,65+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,123));
    }
    public static RobeMenu fromNetwork(int id,Inventory inventory,FriendlyByteBuf buffer){return new RobeMenu(id,inventory,new RobeContainer(inventory.player));}
    @Override public void clicked(int index,int button,ClickType type,Player player){
        if(index==YujianSlot.YUJIAN_SLOT&&YujianSlot.locked(player)&&player instanceof net.minecraft.server.level.ServerPlayer server)YujianController.stop(server);
        super.clicked(index,button,type,player);
    }
    @Override public boolean stillValid(Player player){return robe.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;
        ItemStack source=slot.getItem(),before=source.copy();
        if(index<2){if(!moveItemStackTo(source,2,38,true))return ItemStack.EMPTY;}
        else if(RobeSlots.canEquipRobe(source)&&!slots.get(0).hasItem()){
            if(!moveItemStackTo(source,0,1,false))return ItemStack.EMPTY;
        }else if(YujianSlot.accepts(source)&&!slots.get(1).hasItem()&&slots.get(1).mayPlace(source)){
            if(!moveItemStackTo(source,1,2,false))return ItemStack.EMPTY;
        }else if(index<29){if(!moveItemStackTo(source,29,38,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(source,2,29,false))return ItemStack.EMPTY;
        if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        if(source.getCount()==before.getCount())return ItemStack.EMPTY;
        slot.onTake(player,source);return before;
    }
}
