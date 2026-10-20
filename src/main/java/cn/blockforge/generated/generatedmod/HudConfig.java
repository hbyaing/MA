package cn.blockforge.generated.generatedmod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HudConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue SPIRIT_GUARD_VISUAL = BUILDER.define("spiritGuardVisual",true);
    public static final ForgeConfigSpec.BooleanValue EXOTIC_FIRE_WRAP_VISUAL = BUILDER.define("exoticFireWrapVisual",true);
    public static final ForgeConfigSpec.BooleanValue EXOTIC_FIRE_FIRST_PERSON_VISUAL = BUILDER
            .comment("Show exotic-fire wrap in first person, including armor, held weapons and particle fallback. Disabled by default to avoid close-range GPU load.")
            .define("exoticFireFirstPersonVisual",false);
    public static final ForgeConfigSpec.BooleanValue EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION = BUILDER
            .comment("First-person high-FPS mode: at most 192 ray steps and 90000 integration pixels. Softer fine flames; disable for native precision. Third person is unchanged.")
            .define("exoticFireFirstPersonOptimization",true);
    public static final ForgeConfigSpec.BooleanValue EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION = BUILDER
            .comment("Limit third-person close-range integration to 90000 pixels per draw while preserving 512 ray steps. Fine flames become softer; small distant effects remain native. Disable for native precision.")
            .define("exoticFireThirdPersonOptimization",true);
    public static final ForgeConfigSpec.BooleanValue HIDE_EXPERIMENTAL_WARNING = BUILDER
            .comment("Continue experimental-feature confirmations automatically. Other warnings and backups remain visible.")
            .define("hideExperimentalWarning", true);
    public static final ForgeConfigSpec.DoubleValue SCALE = BUILDER
            .comment("Whole player HUD scale, including portrait, bars, frames and text. Automatically fits small windows.")
            .defineInRange("hudScale", 1.0, 0.5, 2.0);
    public static final ForgeConfigSpec.DoubleValue PLAYER_X = BUILDER.defineInRange("playerHudXPercent", 0.0, 0.0, 100.0);
    public static final ForgeConfigSpec.DoubleValue PLAYER_Y = BUILDER.defineInRange("playerHudYPercent", 0.0, 0.0, 100.0);
    public static final ForgeConfigSpec.BooleanValue BOSS_AUTO = BUILDER
            .comment("Place boss bars around the player HUD. Moving a boss position slider switches to manual placement.")
            .define("bossHudAutoPosition", true);
    public static final ForgeConfigSpec.DoubleValue BOSS_X = BUILDER.defineInRange("bossHudXPercent", 50.0, 0.0, 100.0);
    public static final ForgeConfigSpec.DoubleValue BOSS_Y = BUILDER.defineInRange("bossHudYPercent", 0.0, 0.0, 100.0);
    public static final ForgeConfigSpec.DoubleValue BOSS_NAME_OFFSET_Y = BUILDER
            .comment("Vertical title adjustment within the gap between the boss bar ornaments, in GUI pixels.")
            .defineInRange("bossNameOffsetY", 0.0, -4.0, 4.0);
    public static final ForgeConfigSpec.BooleanValue REALM_FLIGHT_ANIMATION = BUILDER.define("realmFlightAnimation", true);
    public static final ForgeConfigSpec.BooleanValue REALM_FLIGHT_EFFECTS = BUILDER.define("realmFlightEffects", true);
    public static final ForgeConfigSpec.BooleanValue REALM_FLIGHT_FIRST_PERSON_EFFECTS = BUILDER.define("realmFlightFirstPersonEffects", true);
    public static final ForgeConfigSpec.IntValue REALM_FLIGHT_DEFAULT_CAMERA = BUILDER.defineInRange("realmFlightDefaultCamera", 1, 0, 2);
    public static final ForgeConfigSpec.IntValue SWORD_FLIGHT_DEFAULT_CAMERA = BUILDER.defineInRange("swordFlightDefaultCamera", 1, 0, 2);
    public static final ForgeConfigSpec.BooleanValue REALM_FLIGHT_HORIZONTAL = BUILDER.define("realmFlightHorizontalPose", true);
    public static final ForgeConfigSpec.BooleanValue REALM_FLIGHT_CAMERA = BUILDER.define("realmFlightCameraBank", true);
    public static final ForgeConfigSpec.DoubleValue REALM_FLIGHT_CAMERA_SCALE = BUILDER.defineInRange("realmFlightCameraBankScale", .35, 0, 1);
    public static final ForgeConfigSpec.IntValue REALM_FLIGHT_VISUAL_REALM = BUILDER
            .comment("Realm-flight appearance only: 0 follows current realm; 3 through 8 select an unlocked lower/equal realm. Server validates unlocks.")
            .defineInRange("realmFlightVisualRealm", 0, 0, 8);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void resetLayout() {
        SCALE.set(1.0);PLAYER_X.set(0.0);PLAYER_Y.set(0.0);
        BOSS_AUTO.set(true);BOSS_X.set(50.0);BOSS_Y.set(0.0);BOSS_NAME_OFFSET_Y.set(0.0);
        SPEC.save();
    }

    private HudConfig() {}
}
