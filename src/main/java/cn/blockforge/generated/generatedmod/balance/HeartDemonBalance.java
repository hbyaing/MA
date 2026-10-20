package cn.blockforge.generated.generatedmod.balance;

/** Phase-one mirror tuning; separate controls avoid coupling HP, damage and mobility changes. */
public final class HeartDemonBalance {
    public static final double HEALTH_MULTIPLIER = 10.0D;
    public static final double ATTACK_MULTIPLIER = 10.0D;
    public static final double SPEED_MULTIPLIER = 10.0D;
    public static final double ARMOR_MULTIPLIER = 10.0D;
    public static final double TOUGHNESS_MULTIPLIER = 10.0D;
    public static final double KNOCKBACK_MULTIPLIER = 10.0D;
    private HeartDemonBalance() {}
    public static double mirror(double value, double minimum, double multiplier) {
        double mirrored=value*multiplier;
        return Double.isFinite(mirrored)?Math.max(minimum,mirrored):minimum;
    }
    public static double phaseOneHealth(double playerHealth) {
        return mirror(playerHealth,20.0D,HEALTH_MULTIPLIER);
    }
}
