package cn.blockforge.generated.generatedmod.formation.carving;
import net.minecraftforge.common.ForgeConfigSpec;
/** Zero cost or empty grade mask means unconfigured; never grants free operation. */
public final class CarvingConfig {
 public static final ForgeConfigSpec SPEC;
 public static final ForgeConfigSpec.IntValue GATHER_COST,PROTECT_COST,SEAL_COST,GRADES;
 public static final ForgeConfigSpec.DoubleValue RADIUS;
 static {var b=new ForgeConfigSpec.Builder();b.push("carvedFormations");
 GATHER_COST=b.comment("LOW stone value per 1200 loaded server ticks. 0 disables fueling until configured.").defineInRange("gatheringCostPerMinute",0,0,1000000);
 PROTECT_COST=b.defineInRange("protectiveCostPerMinute",0,0,1000000);SEAL_COST=b.defineInRange("sealingCostPerMinute",0,0,1000000);
 GRADES=b.comment("Allowed stone grade bits: LOW=1, MIDDLE=2, SUPERIOR=4, EXQUISITE=8; 0 is unconfigured.").defineInRange("allowedStoneGradeMask",0,0,15);
 RADIUS=b.defineInRange("radius",5.2,2.,15.);b.pop();SPEC=b.build();}
 public static int cost(int kind){return switch(kind){case 1->PROTECT_COST.get();case 2->SEAL_COST.get();default->GATHER_COST.get();};}
 public static boolean configured(int kind){return cost(kind)>0&&GRADES.get()!=0;}
 private CarvingConfig(){}
}
