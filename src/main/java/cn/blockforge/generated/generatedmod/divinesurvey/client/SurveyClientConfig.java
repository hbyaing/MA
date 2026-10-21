package cn.blockforge.generated.generatedmod.divinesurvey.client;

import net.minecraftforge.common.ForgeConfigSpec;

/** Explicit opt-in: survey markers work without creating external map waypoints. */
public final class SurveyClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue XAERO_WAYPOINTS=BUILDER.comment("Export divine survey structure discoveries to Xaero waypoints (optional mods).").define("exportSurveyStructuresToXaero",false);
    public static final ForgeConfigSpec SPEC=BUILDER.build();
    private SurveyClientConfig(){}
}
