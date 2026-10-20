package cn.blockforge.generated.generatedmod;

import net.minecraftforge.common.ForgeConfigSpec;

/** Presentation only; none of these values affect flight or server authority. */
public final class JindouCloudConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    // Separate profile keys ensure existing second-version settings do not override the restored shape.
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET = B.defineInRange("restoredBodyYOffset", -0.27, -1.0, 0.2);
    public static final ForgeConfigSpec.DoubleValue RADIUS_X = B.defineInRange("restoredBodyRadiusX", 2.025, 0.8, 3.0);
    public static final ForgeConfigSpec.DoubleValue RADIUS_Y = B.defineInRange("restoredBodyRadiusY", 0.529, 0.3, 1.0);
    public static final ForgeConfigSpec.DoubleValue RADIUS_Z = B.defineInRange("restoredBodyRadiusZ", 1.575, 0.7, 2.5);
    public static final ForgeConfigSpec.DoubleValue VERTICAL_FULLNESS = B.defineInRange("verticalFullness", 1.20, 1.0, 1.5);
    public static final ForgeConfigSpec.DoubleValue FOOT_ENVELOPMENT = B.defineInRange("footEnvelopment", 0.10, 0.0, 0.2);
    public static final ForgeConfigSpec.BooleanValue DEBUG_VOLUME = B.define("debugVolume", false);
    public static final ForgeConfigSpec.BooleanValue HIDE_FIRST_PERSON = B.define("hideOwnCloudInFirstPerson", true);
    public static final ForgeConfigSpec.DoubleValue DENSITY = B.defineInRange("densityScale", 1.0, 0.2, 2.0);
    public static final ForgeConfigSpec.DoubleValue EXTINCTION = B.defineInRange("extinctionCoefficient", 2.6, 0.5, 5.0);
    public static final ForgeConfigSpec.DoubleValue AMBIENT = B.defineInRange("ambientStrength", 0.66, 0.5, 0.85);
    public static final ForgeConfigSpec.DoubleValue SOFT_INTERSECTION = B.defineInRange("softIntersection", 0.12, 0.02, 0.3);
    public static final ForgeConfigSpec.IntValue MAX_TRAIL_POINTS = B.defineInRange("maxTrailPoints", 32, 4, 64);
    public static final ForgeConfigSpec.IntValue TRAIL_LIFETIME = B.defineInRange("maxTrailLifetime", 20, 4, 40);
    public static final ForgeConfigSpec.DoubleValue TRAIL_TRAVEL = B.defineInRange("wakeTravelDistance", 5.0, 2.0, 12.0);
    public static final ForgeConfigSpec.DoubleValue TRAIL_SPACING = B.defineInRange("trailSampleDistance", 0.55, 0.25, 1.0);
    public static final ForgeConfigSpec.DoubleValue TELEPORT_DISTANCE = B.defineInRange("teleportDistanceThreshold", 16.0, 4.0, 64.0);
    public static final ForgeConfigSpec SPEC = B.build();

    private JindouCloudConfig() {}
}
