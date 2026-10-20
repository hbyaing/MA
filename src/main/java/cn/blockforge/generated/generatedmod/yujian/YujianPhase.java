package cn.blockforge.generated.generatedmod.yujian;

public enum YujianPhase {
    BASIC(2, 0, 1.0F), ACCELERATING(2, 0, 1.05F), JIYU(2, 20, 1.3F),
    LINGKONG(3, 40, 0.8F), DUNXU(5, 80, 2.4F);
    public final int realm, mastery;
    public final float trailIntensity;
    YujianPhase(int realm, int mastery, float trailIntensity) {
        this.realm=realm; this.mastery=mastery; this.trailIntensity=trailIntensity;
    }
    public boolean burst() { return this==DUNXU; }
    public String translationKey() { return "yujian.wuxingxiantu.phase."+name().toLowerCase(java.util.Locale.ROOT); }
}
