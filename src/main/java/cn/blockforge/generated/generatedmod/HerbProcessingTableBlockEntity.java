package cn.blockforge.generated.generatedmod;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Five seconds per herb, one-for-one output, hopper-safe and persistent. */
public final class HerbProcessingTableBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int PROCESS_TICKS=100;
    private final NonNullList<ItemStack> items=NonNullList.withSize(2,ItemStack.EMPTY);
    private int progress;
    private ItemStack work=ItemStack.EMPTY;
    private final ContainerData data=new ContainerData(){public int get(int i){return i==0?progress:0;}public void set(int i,int v){if(i==0)progress=Math.max(0,Math.min(PROCESS_TICKS,v));}public int getCount(){return 1;}};
    public HerbProcessingTableBlockEntity(BlockPos pos,BlockState state){super(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.HERB_PROCESSING_TABLE.get(),pos,state);}
    public ContainerData dataAccess(){return data;}
    public MenuProvider menuProvider(){return new SimpleMenuProvider((id,inv,p)->new HerbProcessingTableMenu(id,inv,this),net.minecraft.network.chat.Component.translatable("block.wuxingxiantu.herb_processing_table"));}
    public int progress(){return progress;}
    public static void tick(Level level,BlockPos pos,BlockState state,HerbProcessingTableBlockEntity table){
        if(level.isClientSide)return;
        ItemStack input=table.items.get(0);
        if(!HerbPreparation.raw(input)){table.reset();return;}
        if(!ItemStack.isSameItemSameTags(input,table.work)){table.progress=0;table.work=input.copyWithCount(1);table.setChanged();}
        ItemStack result=HerbPreparation.prepare(input),output=table.items.get(1);
        if(!output.isEmpty()&&(!ItemStack.isSameItemSameTags(output,result)||output.getCount()>=Math.min(output.getMaxStackSize(),table.getMaxStackSize())))return;
        table.progress++;
        if(table.progress>=PROCESS_TICKS){
            if(output.isEmpty())table.items.set(1,result);else output.grow(1);
            input.shrink(1);table.progress=0;
            if(input.isEmpty())table.work=ItemStack.EMPTY;
            table.setChanged();
        }else if(table.progress%20==0)table.setChanged();
    }
    private void reset(){if(progress!=0||!work.isEmpty()){progress=0;work=ItemStack.EMPTY;setChanged();}}
    @Override public int getContainerSize(){return 2;}
    @Override public boolean isEmpty(){return items.get(0).isEmpty()&&items.get(1).isEmpty();}
    @Override public ItemStack getItem(int slot){return slot>=0&&slot<2?items.get(slot):ItemStack.EMPTY;}
    @Override public ItemStack removeItem(int slot,int count){if(slot<0||slot>=2)return ItemStack.EMPTY;var s=ContainerHelper.removeItem(items,slot,count);if(!s.isEmpty()){if(slot==0&&items.get(0).isEmpty())reset();setChanged();}return s;}
    @Override public ItemStack removeItemNoUpdate(int slot){if(slot<0||slot>=2)return ItemStack.EMPTY;var s=ContainerHelper.takeItem(items,slot);if(slot==0)reset();return s;}
    @Override public void setItem(int slot,ItemStack stack){if(slot<0||slot>=2)return;var s=stack.copy();s.setCount(Math.min(s.getCount(),Math.min(64,s.getMaxStackSize())));items.set(slot,s);if(slot==0&&!ItemStack.isSameItemSameTags(s,work))reset();setChanged();}
    @Override public void clearContent(){items.clear();reset();setChanged();}
    @Override public boolean stillValid(Player player){return Container.stillValidBlockEntity(this,player);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==0&&HerbPreparation.raw(stack);}
    @Override public int[] getSlotsForFace(Direction side){return side==Direction.DOWN?new int[]{1}:new int[]{0};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return side!=Direction.DOWN&&canPlaceItem(slot,stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return slot==1&&side==Direction.DOWN;}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);ContainerHelper.saveAllItems(tag,items);tag.putInt("Progress",progress);tag.put("Work",work.save(new CompoundTag()));}
    @Override public void load(CompoundTag tag){super.load(tag);ContainerHelper.loadAllItems(tag,items);progress=Math.max(0,Math.min(PROCESS_TICKS-1,tag.getInt("Progress")));work=ItemStack.of(tag.getCompound("Work"));if(!HerbPreparation.raw(items.get(0))||!ItemStack.isSameItemSameTags(items.get(0),work)){progress=0;work=ItemStack.EMPTY;}}
}
