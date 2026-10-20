package cn.blockforge.generated.generatedmod.meditationqte;

/** Source r115 three cursor rounds; one authoritative clock, with no client-submitted position. */
public final class QteRules {
    public static final int LEVELS = 3, GRAY = 0, RED = 1, GREEN = 2;
    public static final long SESSION_MILLIS = 30_000L;
    public static final int SESSION_TICKS = 600, DEVIATION_TICKS = 15 * 60 * 20;
    private static final double[] GREEN_WIDTH = {15, 8, 4};
    private static final double[] RED_WIDTH = {25, 27, 26};
    private static final long[] PERIOD = {2000, 1333, 909};
    private QteRules() {}
    private static int index(int level) { return Math.max(0, Math.min(2, level - 1)); }
    public static long period(int level) { return PERIOD[index(level)]; }
    public static double greenHalf(int level) { return GREEN_WIDTH[index(level)] / 2; }
    public static double redHalf(int level) { return RED_WIDTH[index(level)] / 2; }
    public static double position(long elapsedMillis, int level) {
        return 50 - 50 * Math.cos(Math.max(0L, elapsedMillis) / (double) period(level) * Math.PI * 2);
    }
    public static int judge(int level, double position) {
        if (level < 1 || level > LEVELS || !Double.isFinite(position) || position < 0 || position > 100) return GRAY;
        double distance = Math.abs(position - 50);
        if (distance <= greenHalf(level)) return GREEN;
        return distance <= greenHalf(level) + redHalf(level) ? RED : GRAY;
    }
    public static double precision(int level, double position) {
        return Double.isFinite(position) ? Math.max(0, 1 - Math.abs(position - 50) / greenHalf(level)) : 0;
    }
    public static long reward(long requirement, double averagePrecision) {
        if (requirement <= 0 || !Double.isFinite(averagePrecision)) return 0;
        return Math.max(1, (long) Math.floor(requirement * (.05 + .15 * Math.max(0, Math.min(1, averagePrecision)))));
    }
}
