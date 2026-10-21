package cn.blockforge.generated.generatedmod.robe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Both menu entrances use the same capability container and permission predicates. */
public final class EquipmentSlots {
    public static final class EquipmentSlot extends Slot {
        private final Player owner;
        private final boolean sword;
        public EquipmentSlot(Container container, Player owner, int index, int x, int y) {
            super(container,index,x,y);this.owner=owner;this.sword=index==YujianSlot.YUJIAN_SLOT;
        }
        @Override public boolean mayPlace(ItemStack stack) {
            return sword?YujianSlot.accepts(stack):RobeSlots.canEquipRobe(stack);
        }
        @Override public boolean mayPickup(Player player) {
            return sword?true:!EnchantmentHelper.hasBindingCurse(getItem())||player.isCreative()||!player.isAlive();
        }
        @Override public int getMaxStackSize(){return 1;}
        @Override public com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation,net.minecraft.resources.ResourceLocation> getNoItemIcon() {
            return com.mojang.datafixers.util.Pair.of(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu",
                            sword?"item/empty_sword_slot":"item/empty_robe_slot"));
        }
    }
    public static Slot inventoryRobe(Inventory inventory){return new EquipmentSlot(new RobeContainer(inventory.player),inventory.player,0,80,8);}
    public static Slot inventorySword(Inventory inventory){return new EquipmentSlot(new RobeContainer(inventory.player),inventory.player,YujianSlot.YUJIAN_SLOT,80,35);}

    /** Null delegates ordinary transfers to vanilla. Equipment transfers obey native Slot limits. */
    public static ItemStack quickMove(InventoryMenu menu,Player player,int index){
        if(index<0||index>=menu.slots.size())return null;
        Slot slot=menu.slots.get(index);
        if(slot instanceof EquipmentSlot){
            if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;
            ItemStack source=slot.getItem(),before=source.copy();
            for(int pass=0;pass<2&&!source.isEmpty();pass++)for(int i=44;i>=9&&!source.isEmpty();i--){
                Slot target=menu.slots.get(i);
                if(target.hasItem()!=(pass==0))continue;
                target.safeInsert(source);
            }
            if(source.getCount()==before.getCount())return ItemStack.EMPTY;
            if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
            slot.onTake(player,source);return before;
        }
        if(index<9||index>=45||!slot.hasItem()||!slot.mayPickup(player))return null;
        for(Slot target:menu.slots){
            if(!(target instanceof EquipmentSlot)||target.hasItem()||!target.mayPlace(slot.getItem()))continue;
            ItemStack source=slot.getItem(),before=source.copy();
            target.safeInsert(source);
            if(source.getCount()==before.getCount())continue;
            if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
            slot.onTake(player,source);return before;
        }
        return null;
    }
    private EquipmentSlots(){}
}
