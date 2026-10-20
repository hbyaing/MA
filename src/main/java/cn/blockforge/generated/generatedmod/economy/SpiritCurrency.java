package cn.blockforge.generated.generatedmod.economy;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Atomic main-inventory payment with exact change; failed payment leaves inventory untouched. */
public final class SpiritCurrency {
    public static long value(Iterable<ItemStack> stacks){long value=0;for(var s:stacks){var g=SpiritStoneGrade.of(s.getItem());if(g!=null)value+=(long)g.value*s.getCount();}return value;}
    public static boolean pay(Inventory inventory,long cost){
        if(cost<0)throw new IllegalArgumentException("Negative price");
        if(cost==0)return true;
        if(value(inventory.items)<cost)return false;
        var copy=new ArrayList<ItemStack>();for(var s:inventory.items)copy.add(s.copy());
        long remaining=cost,change=0;
        for(var grade:SpiritStoneGrade.values())for(var s:copy)if(remaining>0&&s.is(grade.item())){
            int used=(int)Math.min(s.getCount(),(remaining+grade.value-1)/grade.value);
            s.shrink(used);long paid=(long)used*grade.value;
            if(paid>=remaining){change=paid-remaining;remaining=0;}else remaining-=paid;
        }
        var grades=SpiritStoneGrade.values();
        for(int i=grades.length-1;i>=0;i--){var grade=grades[i];int count=(int)(change/grade.value);change%=grade.value;
            for(var s:copy)if(count>0&&s.is(grade.item())&&s.getTag()==null){int n=Math.min(count,s.getMaxStackSize()-s.getCount());s.grow(n);count-=n;}
            for(int slot=0;slot<copy.size()&&count>0;slot++)if(copy.get(slot).isEmpty()){int n=Math.min(count,grade.item().getMaxStackSize());copy.set(slot,new ItemStack(grade.item(),n));count-=n;}
            if(count>0)return false;
        }
        for(int i=0;i<copy.size();i++)inventory.items.set(i,copy.get(i));inventory.setChanged();return true;
    }
    private SpiritCurrency(){}
}
