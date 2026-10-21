package cn.blockforge.generated.generatedmod.balance;

/** Safety bounds and legal bonus composition shared by capability and gameplay code. */
public final class ManaBalance {
    private ManaBalance() {}
    public static float maximum(int realm,int auraBonus,float physiqueMultiplier) {
        float result=RealmBalance.mana(realm)*(1+Math.max(-5,Math.min(25,auraBonus))/100F)*physiqueMultiplier;
        return sanitizeMaximum(realm,result);
    }
    public static float sanitizeMaximum(int realm,float requested) {
        return Float.isFinite(requested)?Math.max(0,Math.min(RealmBalance.mana(realm)*3F,requested)):0;
    }
    public static float clamp(float value,float maximum) {
        return Float.isFinite(value)&&Float.isFinite(maximum)?Math.max(0,Math.min(Math.max(0,maximum),value)):0;
    }
    public static float regenerationCap(float maximum,boolean meditating) {
        return Math.max(0,maximum)*(meditating?.04F:.008F);
    }
}
