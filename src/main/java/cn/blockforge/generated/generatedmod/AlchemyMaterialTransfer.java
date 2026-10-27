package cn.blockforge.generated.generatedmod;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Simulate both inventories first; a rejected transfer changes neither side. */
public final class AlchemyMaterialTransfer {
    public enum Result { OK, BUSY, MISSING, SLOTS_FULL, INVENTORY_FULL, AGE_MISSING }
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
            var bagOrder=new java.util.ArrayList<Integer>();for(int b=0;b<36;b++)bagOrder.add(b);
            boolean ranked=cn.blockforge.generated.generatedmod.alchemy.MedicineTier.explicit(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY));
            if(ranked){var originalBag=bag;bagOrder.sort(java.util.Comparator.comparingInt(b->cn.blockforge.generated.generatedmod.plants.HerbAge.years(originalBag[b])));}
            for(int i=0;i<recipe.getIngredients().size();i++) {
                var ingredient=recipe.getIngredients().get(i);int needed=recipe.counts().get(i);
                for(int s=0;s<8&&needed>0;s++)if(ingredient.test(slots[s])) {
                    int amount=Math.min(needed,slots[s].getCount()-used[s]);used[s]+=amount;needed-=amount;
                }
                for(int b:bagOrder)if(needed>0&&ingredient.test(bag[b])&&!bag[b].isEmpty()) {
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
            if(recipe.tryReservation(new net.minecraft.world.SimpleContainer(slots)).isEmpty()) {
                if(!ranked)return Result.MISSING;
                var swap=replaceGuide(slots,bag,recipe,machine);
                if(swap.result()!=Result.OK)return swap.result();
                slots=swap.slots();bag=swap.bag();
            }
        }
        for(int i=0;i<36;i++)inventory.setItem(i,bag[i]);
        for(int i=0;i<8;i++)machine.setItem(i,slots[i]);
        inventory.setChanged();machine.setChanged();
        return Result.OK;
    }
    private record GuideSwap(Result result,ItemStack[] slots,ItemStack[] bag) {}
    private static ItemStack[] copy(ItemStack[] stacks) {
        ItemStack[] result=new ItemStack[stacks.length];
        for(int i=0;i<stacks.length;i++)result[i]=stacks[i].copy();
        return result;
    }
    private static GuideSwap replaceGuide(ItemStack[] slots,ItemStack[] bag,AlchemyRecipe recipe,Container machine) {
        int rank=cn.blockforge.generated.generatedmod.alchemy.MedicineTier.of(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY));
        int[] allocated=recipe.tryIngredientReservation(new net.minecraft.world.SimpleContainer(slots)).orElse(null);
        if(allocated==null)return new GuideSwap(Result.MISSING,null,null);
        var order=new java.util.ArrayList<Integer>();for(int b=0;b<bag.length;b++)order.add(b);
        order.sort(java.util.Comparator.comparingInt(b->cn.blockforge.generated.generatedmod.plants.HerbAge.years(bag[b])));
        boolean inventoryFull=false,slotsFull=false;
        for(int b:order) {
            if(bag[b].isEmpty()||cn.blockforge.generated.generatedmod.plants.HerbAge.years(bag[b])<cn.blockforge.generated.generatedmod.plants.HerbAge.legacyMinimumYears(rank))continue;
            for(var ingredient:recipe.getIngredients()) {
                if(!ingredient.test(bag[b]))continue;
                for(int slot=0;slot<slots.length;slot++) {
                    if(allocated[slot]<=0||!ingredient.test(slots[slot])||cn.blockforge.generated.generatedmod.plants.HerbAge.years(slots[slot])>=cn.blockforge.generated.generatedmod.plants.HerbAge.legacyMinimumYears(rank))continue;
                    var nextBag=copy(bag);var nextSlots=copy(slots);
                    var guide=nextBag[b].split(1);var returned=nextSlots[slot].split(1);
                    if(!insert(nextBag,returned,null)){inventoryFull=true;continue;}
                    if(!insert(nextSlots,guide,machine)){slotsFull=true;continue;}
                    if(recipe.tryReservation(new net.minecraft.world.SimpleContainer(nextSlots)).isPresent())
                        return new GuideSwap(Result.OK,nextSlots,nextBag);
                }
            }
        }
        return new GuideSwap(slotsFull?Result.SLOTS_FULL:inventoryFull?Result.INVENTORY_FULL:Result.AGE_MISSING,null,null);
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
