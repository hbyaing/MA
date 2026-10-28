package cn.blockforge.generated.generatedmod.alchemy.research;
import cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge;
import cn.blockforge.generated.generatedmod.alchemy.PillCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class ResearchTableMenu extends AbstractContainerMenu {
    public static final int WIDTH=440,HEIGHT=300;
    private final Container table;private final Player viewer;private final BlockPos pos;private final ContainerData data=new SimpleContainerData(54);
    private ItemStack inspected=ItemStack.EMPTY;
    public ResearchTableMenu(int id,Inventory inv,ResearchTableEntity table){this(id,inv,table,table.getBlockPos());}
    private ResearchTableMenu(int id,Inventory inv,Container table,BlockPos pos){super(cn.blockforge.generated.generatedmod.registry.ModMenus.ALCHEMY_RESEARCH_TABLE.get(),id);this.table=table;this.viewer=inv.player;this.pos=pos;
        addSlot(new Slot(table,0,64,88){public boolean mayPlace(ItemStack s){return ResearchView.sample(s);}public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(table,1,40,177){public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
        addSlot(new Slot(table,2,88,177){public boolean mayPlace(ItemStack s){return s.is(Items.INK_SAC);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,118+col*20,225+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,118+col*20,281));addDataSlots(data);refresh();
    }
    public static ResearchTableMenu fromNetwork(int id,Inventory inv,net.minecraft.network.FriendlyByteBuf b){return new ResearchTableMenu(id,inv,new SimpleContainer(3),b.readBlockPos());}
    public int page(){return data.get(0);}public boolean analyzed(){return data.get(1)>0;}public boolean recorded(){return data.get(1)==2;}public String lastRecord(){var text=new StringBuilder();for(int i=0;i<Math.min(48,data.get(5));i++)text.append((char)(data.get(6+i)&65535));return text.toString();}public int records(){return data.get(2);}public int knownFormulas(){return data.get(3);}public boolean unlocked(){return data.get(4)==1;}
    public ItemStack specimen(){return table.getItem(0);}public boolean supplies(){return table.getItem(1).is(Items.PAPER)&&table.getItem(2).is(Items.INK_SAC);}
    private void refresh(){if(viewer.level().isClientSide)return;if(!ItemStack.matches(inspected,specimen())){data.set(1,0);inspected=ItemStack.EMPTY;}var notes=FormulaKnowledge.researchRecords(viewer);data.set(2,notes.size());String last=notes.isEmpty()?"":notes.getCompound(notes.size()-1).getString("Summary");data.set(5,Math.min(48,last.length()));for(int i=0;i<48;i++)data.set(6+i,i<last.length()?last.charAt(i):0);data.set(3,(int)java.util.Arrays.stream(PillCatalog.values()).filter(p->FormulaKnowledge.knows(viewer,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu",p.id))).count());data.set(4,FormulaKnowledge.unlocked(viewer)?1:0);}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return p.level().isClientSide||p.isAlive()&&!p.isSpectator()&&p.level().getBlockEntity(pos)==table&&table.stillValid(p);}
    @Override public boolean clickMenuButton(Player p,int button){if(p.level().isClientSide||!stillValid(p)||p!=viewer)return false;
        if(button>=0&&button<6){data.set(0,button);broadcastChanges();return true;}
        if(!(p instanceof net.minecraft.server.level.ServerPlayer player)||!FormulaKnowledge.unlocked(player)||page()!=0)return false;
        if(button==10){if(!ResearchView.authenticate(player,specimen()))return false;inspected=specimen().copy();data.set(1,FormulaKnowledge.researchRecords(p).stream().anyMatch(t->((net.minecraft.nbt.CompoundTag)t).getString("Fingerprint").equals(ResearchView.fingerprint(specimen())))?2:1);broadcastChanges();return true;}
        if(button==11&&!recorded()&&analyzed()&&ItemStack.matches(inspected,specimen())&&supplies()&&ResearchView.authenticate(player,specimen())){
            if(!FormulaKnowledge.recordResearch(player,ResearchView.fingerprint(specimen()),ResearchView.effect(specimen())))return false;
            table.removeItem(1,1);table.removeItem(2,1);table.setChanged();data.set(1,2);broadcastChanges();return true;
        }return false;
    }
    @Override public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(i<3){if(!moveItemStackTo(stack,3,39,true))return ItemStack.EMPTY;}
        else{int target=ResearchView.sample(stack)?0:stack.is(Items.PAPER)?1:stack.is(Items.INK_SAC)?2:-1;if(target<0||!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);broadcastChanges();return before;
    }
}
