package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.AlchemyRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

/** Server and screen use the same batch failure chance; grade odds remain conditional on success. */
public final class AlchemyFailureOdds {
    private static final int[] BASE = {18,14,10,6};
    public static int percent(AlchemyRecipe recipe,int furnaceTier,int fireTier,boolean free) {
        return percent(recipe,furnaceTier,fireTier,free,0);
    }
    public static int percent(AlchemyRecipe recipe,int furnaceTier,int fireTier,boolean free,int preparedBonus) {
        int bonus=Math.max(0,Math.min(cn.blockforge.generated.generatedmod.HerbPreparation.MAX_BONUS+9,preparedBonus));
        if(free)return 100-FreeAlchemy.SUCCESS_PERCENT-bonus;
        if(recipe==null)return 0;
        var pill=PillData.read(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY));
        int base=pill==null?18:pill.pill()==PillCatalog.CALMING?8:BASE[pill.grade().ordinal()];
        int furnaceBonus=2*Math.max(0,furnaceTier-recipe.furnaceTier());
        int fireBonus=Math.min(3,Math.max(0,fireTier-recipe.minFireTier()));
        return Math.max(2,Math.min(25,base-furnaceBonus-fireBonus)-bonus);
    }
    public static ItemStack roll(AlchemyRecipe recipe,ItemStack source,int furnaceTier,int fireTier,RandomSource random) {
        return random.nextInt(100)<percent(recipe,furnaceTier,fireTier,false)?ItemStack.EMPTY:AlchemyGradeOdds.roll(source,random);
    }
    public static ItemStack roll(AlchemyRecipe recipe,ItemStack source,int furnaceTier,int fireTier,RandomSource random,int bonus) {
        return random.nextInt(100)<percent(recipe,furnaceTier,fireTier,false,bonus)?ItemStack.EMPTY:AlchemyGradeOdds.roll(source,random);
    }
    private AlchemyFailureOdds() {}
}
