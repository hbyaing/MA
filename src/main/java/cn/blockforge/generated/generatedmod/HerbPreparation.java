package cn.blockforge.generated.generatedmod;
import net.minecraft.world.item.*;
import net.minecraft.world.Container;
import cn.blockforge.generated.generatedmod.plants.SpiritPlantBlock;

/** Same herb and ingredient identity; prepared state cannot be repeatedly upgraded. */
public final class HerbPreparation {
    public static final String KEY="WuxingHerbPrepared";
    public static final int MAX_BONUS=6;
    private HerbPreparation() {}
    public static boolean herb(ItemStack stack){return !stack.isEmpty()&&stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof SpiritPlantBlock;}
    public static boolean prepared(ItemStack stack){return herb(stack)&&stack.hasTag()&&stack.getTag().getBoolean(KEY);}
    public static boolean raw(ItemStack stack){return herb(stack)&&!prepared(stack);}
    public static ItemStack prepare(ItemStack stack){
        if(!raw(stack))return ItemStack.EMPTY;
        var result=stack.copyWithCount(1);result.getOrCreateTag().putBoolean(KEY,true);return result;
    }
    /** Only a complete recipe's allocated herbs count; incomplete previews and spare inputs give no bonus. */
    public static int bonus(AlchemyRecipe recipe,Container input){
        if(recipe==null||input==null)return 0;
        int[] consumed=recipe.tryReservation(input).orElse(null);
        if(consumed==null)return 0;
        long herbs=0,processed=0;
        for(int slot=0;slot<Math.min(input.getContainerSize(),consumed.length);slot++){
            var stack=input.getItem(slot);if(consumed[slot]<=0||!herb(stack))continue;
            herbs+=consumed[slot];if(prepared(stack))processed+=consumed[slot];
        }
        return herbs==0?0:(int)Math.min(MAX_BONUS,MAX_BONUS*processed/herbs);
    }
}
