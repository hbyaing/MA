package cn.blockforge.generated.generatedmod.client.heartsword;

/** Local OBJ coordinates from the authored heart sword 2.0; see reports/heart_sword_v2_import.json. */
public final class HeartSwordModelGeometry {
    public static final float MIN_Y = -.414063811F;
    public static final float MAX_Y = 1.460936189F;
    public static final float GRIP_Y = -.2F;
    public static final float GUARD_Y = -.028158566F;
    public static final float BLADE_ROOT_Y = -.013161496F;
    public static final float POMMEL_Y = -.381214603F;
    public static final float HALF_WIDTH = .030F;
    public static final float GUARD_HALF_WIDTH = .115789882F;
    public static final float CRESCENT_X = .098105837F;
    public static final float HALF_DEPTH = .016434290F;
    public static final float CORE_Z = .013434876F;
    public static final float[] CORE_Y = {.121812139F, .477992565F, .840421771F};

    public static float revealY(float reveal) {
        return MIN_Y + (MAX_Y - MIN_Y) * Math.max(0, Math.min(1, reveal));
    }

    private HeartSwordModelGeometry() {}
}
