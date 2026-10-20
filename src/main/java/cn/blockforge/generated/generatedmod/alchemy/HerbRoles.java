package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.AlchemyRecipe;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Draft adapter: source herb roles use the target's eight slots and five-element essence system. */
public final class HerbRoles {
    public enum Role { MAIN, STABILITY, ESSENCE, YIELD }
    public static final int MAX_STABILITY = 5;

    public static Role role(ItemStack stack) {
        var definition=HerbAge.definition(stack);
        if(definition==null)return null;
        return switch(definition.id) {
            case "herb_juling","herb_huayuan","herb_xisui","herb_pozhang","herb_huichun" -> Role.MAIN;
            case "herb_xingchen","herb_tianji","herb_jingang" -> Role.STABILITY;
            case "herb_huanling","herb_hanbing","herb_lieyang","herb_duzhen","herb_jifeng","herb_lingzhi","herb_leiteng","herb_yanjia" -> Role.ESSENCE;
            case "herb_fengling","herb_lingdao" -> Role.YIELD;
            default -> null;
        };
    }

    /** Optional herbs are selected only from the furnace, never silently from the player's bag. */
    public static void includeOptional(Container input,int[] reserved) {
        for(Role role:new Role[]{Role.STABILITY,Role.ESSENCE,Role.YIELD}) {
            boolean included=false;int best=-1;
            for(int slot=0;slot<Math.min(reserved.length,input.getContainerSize());slot++) {
                var stack=input.getItem(slot);
                if(role(stack)!=role)continue;
                if(reserved[slot]>0){included=true;break;}
                if(stack.getCount()>reserved[slot]&&(best<0||HerbAge.effectiveTier(stack)>HerbAge.effectiveTier(input.getItem(best))))best=slot;
            }
            if(!included&&best>=0)reserved[best]++;
        }
    }

    public static int mask(AlchemyRecipe recipe,Container input,int[] reserved) {
        int mask=0;
        for(int slot=0;slot<Math.min(reserved.length,input.getContainerSize());slot++) {
            if(reserved[slot]<=0)continue;
            var stack=input.getItem(slot);var role=role(stack);
            if(role!=null)mask|=1<<role.ordinal();
            if(HerbAge.definition(stack)!=null) {
                mask|=1<<Role.ESSENCE.ordinal();
                if(recipe!=null&&!recipe.getIngredients().isEmpty()&&recipe.getIngredients().get(0).test(stack))mask|=1<<Role.MAIN.ordinal();
            }
        }
        return mask;
    }

    public static int stability(Container input,int[] reserved) {
        int bonus=0;
        for(int slot=0;slot<Math.min(reserved.length,input.getContainerSize());slot++)
            if(reserved[slot]>0&&role(input.getItem(slot))==Role.STABILITY)
                bonus=Math.max(bonus,Math.max(1,HerbAge.effectiveTier(input.getItem(slot))));
        return Math.min(MAX_STABILITY,bonus);
    }

    public static int stability(AlchemyRecipe recipe,Container input) {
        if(recipe==null)return 0;
        return recipe.tryReservation(input).map(reserved->stability(input,reserved)).orElse(0);
    }

    public static ItemStack addYield(ItemStack result,int mask) {
        ItemStack copy=result.copy();
        if(PillData.read(copy)!=null&&(mask&(1<<Role.YIELD.ordinal()))!=0&&copy.getCount()<copy.getMaxStackSize())copy.grow(1);
        return copy;
    }

    private HerbRoles() {}
}
