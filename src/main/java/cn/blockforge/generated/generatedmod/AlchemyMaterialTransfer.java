package cn.blockforge.generated.generatedmod;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Simulate both inventories first; a rejected transfer changes neither side. */
public final class AlchemyMaterialTransfer {
    public enum Result { OK, BUSY, MISSING, SLOTS_FULL, INVENTORY_FULL }
    public static Result move(Container machine,Inventory inventory,AlchemyRecipe recipe,boolean clear) {
        if(machine instanceof AlchemyFurnaceBlockEntity furnace&&furnace.busy())return Result.BUSY;
        ItemStack[] bag=new ItemStack[36], slots=new ItemStack[8];
        for(int i=0;i<36;i++)bag[i]=inventory.getItem(i).copy();
        for(int i=0;i<8;i++)slots[i]=machine.getItem(i).copy();
        if(clear) {
            for(int i=0;i<8;i++) {
                if(!insert(bag,slots[i],null))return Result.INVENTORY_FULL;
                slots[i]=ItemStack.EMPTY;
            }
        } else {
            if(recipe==null)return Result.MISSING;
            int[] used=new int[8];
            for(int i=0;i<recipe.getIngredients().size();i++) {
                var ingredient=recipe.getIngredients().get(i);int needed=recipe.counts().get(i);
                for(int s=0;s<8&&needed>0;s++)if(ingredient.test(slots[s])) {
                    int amount=Math.min(needed,slots[s].getCount()-used[s]);used[s]+=amount;needed-=amount;
                }
                for(int b=0;b<36&&needed>0;b++)if(ingredient.test(bag[b])&&!bag[b].isEmpty()) {
                    int amount=Math.min(needed,bag[b].getCount());
                    ItemStack moving=bag[b].copyWithCount(amount);
                    // Added stacks count as reserved for this ingredient, too.
                    int[] before=new int[8];for(int s=0;s<8;s++)before[s]=slots[s].getCount();
                    if(!insert(slots,moving,machine))return Result.SLOTS_FULL;
                    for(int s=0;s<8;s++)used[s]+=slots[s].getCount()-before[s];
                    bag[b].shrink(amount);needed-=amount;
                }
                if(needed>0)return Result.MISSING;
            }
        }
        for(int i=0;i<36;i++)inventory.setItem(i,bag[i]);
        for(int i=0;i<8;i++)machine.setItem(i,slots[i]);
        inventory.setChanged();machine.setChanged();
        return Result.OK;
    }
    private static boolean insert(ItemStack[] targets,ItemStack source,Container machine) {
        ItemStack remaining=source.copy();
        for(int pass=0;pass<2;pass++)for(int i=0;i<targets.length&&!remaining.isEmpty();i++) {
            ItemStack target=targets[i];
            if(pass==0&&(target.isEmpty()||!ItemStack.isSameItemSameTags(target,remaining)))continue;
            if(pass==1&&!target.isEmpty())continue;
            if(machine!=null&&!machine.canPlaceItem(i,remaining))continue;
            int amount=Math.min(remaining.getCount(),remaining.getMaxStackSize()-target.getCount());
            if(amount<=0)continue;
            if(target.isEmpty())targets[i]=remaining.copyWithCount(amount);else target.grow(amount);
            remaining.shrink(amount);
        }
        return remaining.isEmpty();
    }
    private AlchemyMaterialTransfer() {}
}
