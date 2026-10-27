package cn.blockforge.generated.generatedmod.alchemy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import cn.blockforge.generated.generatedmod.plants.*;
import net.minecraft.world.Container;

/** Legacy medicinal ranks only. New V5 results use their separate nine-grade manifest. */
public final class MedicineTier {
    public static final String KEY="MedicineTier";
    public static int of(ItemStack stack){return stack.hasTag()&&stack.getTag().contains(KEY)?Math.max(1,Math.min(5,stack.getTag().getInt(KEY))):1;}
    public static boolean explicit(ItemStack stack){return stack.hasTag()&&stack.getTag().contains(KEY);}
    public static ItemStack set(ItemStack stack,int tier){if(tier<1||tier>5)throw new IllegalArgumentException("Medicine rank must be 1..5");stack.getOrCreateTag().putInt(KEY,tier);return stack;}
    public static Component name(int tier){return Component.translatable("medicine_tier.wuxingxiantu."+Math.max(1,Math.min(5,tier)));}
    public static boolean imported(PillCatalog pill){return switch(pill){
        case GATHER_QI,ORIGIN_CONDENSING,MERIDIAN_EXPANSION,BARRIER_PREPARATION,REJUVENATION->true;
        default->false;};}
    /** A ranked batch needs at least one allocated herb of that rank as its medicinal guide. */
    public static boolean accepts(ItemStack output,Container input,int[] reserved){
        if(!explicit(output))return true;int rank=of(output);
        for(int i=0;i<Math.min(reserved.length,input.getContainerSize());i++)
            if(reserved[i]>0&&HerbAge.definition(input.getItem(i))!=null&&HerbAge.years(input.getItem(i))>=HerbAge.legacyMinimumYears(rank))return true;
        return false;
    }
    private MedicineTier(){}
}
