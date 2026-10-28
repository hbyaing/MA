package cn.blockforge.generated.generatedmod.alchemy.research;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class ResearchTableEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items=NonNullList.withSize(3,ItemStack.EMPTY);
    public ResearchTableEntity(BlockPos p,BlockState s){super(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_RESEARCH_TABLE.get(),p,s);}
    public MenuProvider menuProvider(){return new SimpleMenuProvider((id,inv,p)->new ResearchTableMenu(id,inv,this),net.minecraft.network.chat.Component.literal("丹道研析台"));}
    public int getContainerSize(){return 3;}public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    public ItemStack getItem(int i){return i>=0&&i<3?items.get(i):ItemStack.EMPTY;}
    public ItemStack removeItem(int i,int count){if(i<0||i>=3)return ItemStack.EMPTY;var s=ContainerHelper.removeItem(items,i,count);if(!s.isEmpty())setChanged();return s;}
    public ItemStack removeItemNoUpdate(int i){return i>=0&&i<3?ContainerHelper.takeItem(items,i):ItemStack.EMPTY;}
    public void setItem(int i,ItemStack s){if(i<0||i>=3)return;var copy=s.copy();copy.setCount(Math.min(copy.getCount(),i==0?1:Math.min(64,copy.getMaxStackSize())));items.set(i,copy);setChanged();}
    public boolean canPlaceItem(int i,ItemStack s){return i==0?ResearchView.sample(s):i==1?s.is(Items.PAPER):i==2&&s.is(Items.INK_SAC);}
    public boolean stillValid(net.minecraft.world.entity.player.Player p){return Container.stillValidBlockEntity(this,p);}
    public void clearContent(){items.clear();setChanged();}
    @Override protected void saveAdditional(CompoundTag n){super.saveAdditional(n);ContainerHelper.saveAllItems(n,items);}
    @Override public void load(CompoundTag n){super.load(n);for(int i=0;i<3;i++)items.set(i,ItemStack.EMPTY);ContainerHelper.loadAllItems(n,items);}
}
