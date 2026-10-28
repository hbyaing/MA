package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.world.item.*;

/** Medicinal years are continuous age, never a species rank. Inventory time does not age herbs. */
public final class HerbAge {
    public static final String KEY="WuxingHerbYears", ASH_USES="WuxingHerbAshUses";
    public static final String CREATIVE_TEMPLATE="WuxingHerbCreativeTemplate";
    public static final String LOT="WuxingHerbLot";
    public static void newAcquisitionLot(ItemStack stack){if(definition(stack)!=null&&!stack.getOrCreateTag().hasUUID(LOT))stack.getOrCreateTag().putUUID(LOT,java.util.UUID.randomUUID());}
    public static final int BASE=500, STEP=2000, MAX=1000000000;
    public static int years(ItemStack stack){return stack.hasTag()?Math.max(0,Math.min(MAX,stack.getTag().getInt(KEY))):0;}
    public static void setYears(ItemStack stack,int age){stack.getOrCreateTag().putInt(KEY,Math.max(0,Math.min(MAX,age)));stack.getTag().remove(CREATIVE_TEMPLATE);}
    public static int boundedYears(SpiritPlantDefinition def,int years){return Math.max(def.minimumYears,Math.min(MAX,years));}
    public static SpiritPlantDefinition definition(ItemStack stack){return stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof SpiritPlantBlock plant?plant.definition():null;}
    public static ItemStack creativeTemplate(ItemStack source){var copy=source.copy();setYears(copy,0);copy.getOrCreateTag().putBoolean(CREATIVE_TEMPLATE,true);return copy;}
    /** Roll only marked catalogue copies. Explicit zero-age seedlings and existing saves are untouched. */
    public static int randomYears(SpiritPlantDefinition def,net.minecraft.util.RandomSource random){return def.minimumYears+random.nextInt(def.randomMaximumYears-def.minimumYears+1);}
    public static boolean initializeTemplate(ItemStack stack,net.minecraft.util.RandomSource random){
        var def=definition(stack);if(def==null||!stack.hasTag()||!stack.getTag().getBoolean(CREATIVE_TEMPLATE))return false;
        if(years(stack)>0){stack.getTag().remove(CREATIVE_TEMPLATE);return false;}
        setYears(stack,randomYears(def,random));newAcquisitionLot(stack);return true;
    }
    /** Compatibility strength of OLD effects only; not a herb grade or a V5 input. */
    public static int legacyEffectBand(ItemStack stack){int years=years(stack);return definition(stack)==null||years<BASE?0:Math.min(5,1+(years-BASE)/STEP);}
    public static int legacyMinimumYears(int medicineRank){return BASE+STEP*(Math.max(1,Math.min(5,medicineRank))-1);}
    private HerbAge(){}
}
