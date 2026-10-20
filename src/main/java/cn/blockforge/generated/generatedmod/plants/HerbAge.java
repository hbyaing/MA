package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.world.item.*;

/** Medicinal years are independent of visual maturity and never advance in an inventory. */
public final class HerbAge {
    public static final String KEY="WuxingHerbYears";
    public static final String ASH_USES="WuxingHerbAshUses";
    public static final int BASE=500,STEP=2000,MAX=10499;
    public static int years(ItemStack stack){return stack.hasTag()?Math.max(0,Math.min(MAX,stack.getTag().getInt(KEY))):0;}
    public static void setYears(ItemStack stack,int age){stack.getOrCreateTag().putInt(KEY,Math.max(0,Math.min(MAX,age)));}
    public static int tier(int years){return years<BASE?0:Math.min(5,1+(years-BASE)/STEP);}
    public static int speciesCap(SpiritPlantDefinition def){return def.medicinalTierCap;}
    public static int maximumYears(int tier){return tier<=0?BASE-1:Math.min(MAX,BASE+STEP*tier-1);}
    public static int boundedYears(SpiritPlantDefinition def,int years){return Math.max(0,Math.min(maximumYears(speciesCap(def)),years));}
    public static SpiritPlantDefinition definition(ItemStack stack){return stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof SpiritPlantBlock plant?plant.definition():null;}
    public static int effectiveTier(ItemStack stack){var def=definition(stack);return def==null?0:Math.min(speciesCap(def),tier(years(stack)));}
    private HerbAge(){}
}
