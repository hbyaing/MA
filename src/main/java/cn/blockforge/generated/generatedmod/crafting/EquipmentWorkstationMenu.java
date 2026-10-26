package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import cn.blockforge.generated.generatedmod.client.UiLayoutConfig;

public final class EquipmentWorkstationMenu extends AbstractContainerMenu {
    public final boolean inscription;
    private final Player owner;private final BlockPos pos;
    private final SimpleContainer input=new SimpleContainer(2);
    private final ContainerData data=new SimpleContainerData(12);
    public EquipmentWorkstationMenu(int id,Inventory inv,BlockPos pos,boolean inscription){
        super(inscription?ModMenus.INSCRIPTION_TABLE.get():ModMenus.REFORGE_TABLE.get(),id);owner=inv.player;this.pos=pos;this.inscription=inscription;
        String profile=EquipmentWorkstationLayout.profile(inscription);
        int equipmentX=UiLayoutConfig.x(profile,"equipmentSlot",UiLayoutConfig.x(profile,"equipment",EquipmentWorkstationLayout.EQUIPMENT_X)),equipmentY=UiLayoutConfig.y(profile,"equipmentSlot",UiLayoutConfig.y(profile,"equipment",EquipmentWorkstationLayout.EQUIPMENT_Y));
        int bookX=UiLayoutConfig.x(profile,"bookSlot",UiLayoutConfig.x(profile,"book",inscription?EquipmentWorkstationLayout.BOOK_X:-10000)),bookY=UiLayoutConfig.y(profile,"bookSlot",UiLayoutConfig.y(profile,"book",inscription?EquipmentWorkstationLayout.BOOK_Y:-10000));
        addSlot(new Slot(input,0,equipmentX,equipmentY){@Override public boolean mayPlace(ItemStack s){return EquipmentCrafting.eligible(s);}@Override public int getMaxStackSize(){return 1;}@Override public void setChanged(){super.setChanged();bump();}});
        // Legacy input remains non-insertable; its compact card stays above inventory.
        addSlot(new Slot(input,1,bookX,bookY){@Override public boolean mayPlace(ItemStack s){return false;}@Override public int getMaxStackSize(){return 1;}@Override public void setChanged(){super.setChanged();bump();}});
        // Versioned compact profiles ignore incompatible older workbench exports.
        int inventoryX=UiLayoutConfig.x(profile,"inventory",EquipmentWorkstationLayout.INVENTORY_X),inventoryY=UiLayoutConfig.y(profile,"inventory",EquipmentWorkstationLayout.inventoryY(inscription));
        // `inventory` is the panel anchor.  Slot spacing is a separate value;
        // using the panel width here stretched the vanilla 16px slots across
        // the reference screenshot and made the live inventory drift.
        int inventoryStep=Math.max(18,UiLayoutConfig.width(profile,"inventoryStepX",18));
        int inventoryRowStep=Math.max(18,UiLayoutConfig.height(profile,"inventoryStepY",18));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,inventoryX+col*inventoryStep,inventoryY+row*inventoryRowStep));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,inventoryX+col*inventoryStep,inventoryY+3*inventoryRowStep+EquipmentWorkstationLayout.HOTBAR_GAP));
        addDataSlots(data);refresh();
    }
    public static EquipmentWorkstationMenu fromNetwork(int id,Inventory inv,FriendlyByteBuf buf){return new EquipmentWorkstationMenu(id,inv,buf.readBlockPos(),buf.readBoolean());}
    public ItemStack equipment(){return input.getItem(0);}public ItemStack book(){return input.getItem(1);}
    public int selected(){return data.get(1);}public int value(int i){return data.get(i);}public int command(int action){return (data.get(0)&65535)*128+action;}
    private void bump(){if(!owner.level().isClientSide){data.set(0,(data.get(0)+1)&65535);refresh();}}
    private void refresh(){
        if(!(owner instanceof ServerPlayer p))return;EquipmentCrafting.migrate(equipment());
        int action=data.get(1);if(!inscription&&action<20)action=20;data.set(1,action);
        var q=EquipmentCrafting.quote(p,equipment(),action);data.set(2,q.chance());data.set(3,q.stone());data.set(4,q.metal());data.set(5,q.lapis());data.set(6,q.essence());data.set(7,q.crystal());data.set(8,q.aura());data.set(9,q.ready()?1:0);data.set(10,Math.max(0,EquipmentCrafting.REASONS.indexOf(q.reason())));
        var cultivation=GeneratedMod.data(p);int fire=cultivation==null?0:cultivation.wuxingxiantu$getEquippedFire();
        data.set(11,(Math.max(0,fire)<<1)|(!inscription&&EquipmentCrafting.usesRefineSeal(p,equipment(),action)?1:0));
    }
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean clickMenuButton(Player player,int id){
        if(!(player instanceof ServerPlayer p)||player!=owner||!stillValid(player)||id<0||id/128!=data.get(0))return false;
        int action=id%128;var stack=equipment();String message="";
        if((action==100||action==101||action==102||action==104)&&!EquipmentBond.canUse(p,stack))return false;
        if(action>=60&&action<=64&&inscription)data.set(1,action-60);
        else if(action>=80&&action<=82&&!inscription)data.set(1,action-60);
        else if(action>=90&&action<=93&&!inscription)data.set(1,action-60);
        else if(action==100){int chosen=data.get(1);if(inscription&&chosen>=5||!inscription&&(chosen<20||chosen>33))return false;message=EquipmentCrafting.attempt(p,stack,chosen);}
        else if((action==101||action==102)&&!inscription){if(!EquipmentCrafting.choose(p,stack,action==101))return false;message=action==101?"已采用新胚性":"保留原胚性，本次材料不退还";}
        else if(action==103&&inscription){if(EquipmentCrafting.legacyBookLevel(book())<=0)return false;EquipmentCrafting.convertBook(book());message="旧御体附魔书已转为同阶刻印凭据";}
        else if(action==104&&inscription){int level=Math.max(EquipmentCrafting.legacyBookLevel(book()),EquipmentCrafting.read(book()).getInt("GuardCredit"));
            if(level<=EquipmentCrafting.guardLevel(stack)||!(stack.getItem() instanceof ArmorItem)||!EquipmentCrafting.eligible(stack))return false;
            EquipmentCrafting.convertBook(book());EquipmentCrafting.migrate(stack);EquipmentCrafting.setRune(stack,"guard",Math.max(EquipmentCrafting.level(stack,"guard"),Math.min(4,level)),(int)Math.round((EquipmentCrafting.potency(stack,"guard")-1)*20));EquipmentCrafting.write(book()).remove("GuardCredit");message="已兑换同阶灵力御体铭纹";
        }else return false;
        data.set(0,(data.get(0)+1)&65535);input.setChanged();refresh();broadcastChanges();p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();
        if(!message.isEmpty())p.displayClientMessage(Component.literal(message),true);return true;
    }
    @Override public boolean stillValid(Player p){return p.level().isClientSide||p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).is(inscription?WuXingBlocks.INSCRIPTION_TABLE.get():WuXingBlocks.REFORGE_TABLE.get());}
    @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide)clearContainer(p,input);}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var old=s.copy();
        if(index<2){if(!moveItemStackTo(s,2,slots.size(),true))return ItemStack.EMPTY;}
        else if(EquipmentCrafting.eligible(s)){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}
        
        else if(index<29){if(!moveItemStackTo(s,29,38,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(s,2,29,false))return ItemStack.EMPTY;
        if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return old;
    }
}
