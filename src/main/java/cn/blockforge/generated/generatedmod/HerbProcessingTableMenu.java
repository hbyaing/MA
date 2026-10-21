package cn.blockforge.generated.generatedmod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class HerbProcessingTableMenu extends AbstractContainerMenu {
    private final Container input;private final ContainerData data;private final net.minecraft.world.level.Level level;private final BlockPos pos;
    public HerbProcessingTableMenu(int id,Inventory inv,HerbProcessingTableBlockEntity table){this(id,inv,table,table.dataAccess(),table.getBlockPos());}
    private HerbProcessingTableMenu(int id,Inventory inv,Container container,ContainerData data,BlockPos pos){
        super(cn.blockforge.generated.generatedmod.registry.ModMenus.HERB_PROCESSING_TABLE.get(),id);this.input=container;this.data=data;this.level=inv.player.level();this.pos=pos;
        checkContainerSize(container,2);checkContainerDataCount(data,1);
        addSlot(new Slot(container,0,44,38){@Override public boolean mayPlace(ItemStack s){return HerbPreparation.raw(s);}});
        addSlot(new Slot(container,1,116,38){@Override public boolean mayPlace(ItemStack s){return false;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,106+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,164));addDataSlots(data);
    }
    public static HerbProcessingTableMenu fromNetwork(int id,Inventory inv,net.minecraft.network.FriendlyByteBuf buf){return new HerbProcessingTableMenu(id,inv,new SimpleContainer(2),new SimpleContainerData(1),buf.readBlockPos());}
    public int progress(){return Math.max(0,Math.min(100,data.get(0)));}
    @Override public boolean stillValid(Player player){return level.isClientSide||level.getBlockEntity(pos)==input&&input.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(index<2){if(!moveItemStackTo(stack,2,slots.size(),true))return ItemStack.EMPTY;}
        else if(HerbPreparation.raw(stack)){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        else if(index<29){if(!moveItemStackTo(stack,29,38,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,2,29,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
