package cn.blockforge.generated.generatedmod.balance;

public final class TribulationBalance {
    public static volatile float healthBudget=2.3F;
    public static int strikes(int realm){return 18+Math.max(0,Math.min(7,realm))*9;}
    public static float damage(float maximumHealth,int total,int strike){
        if(!Float.isFinite(maximumHealth)||maximumHealth<=0||total<1)return 0;
        float ramp=total==1?1: .75F+.5F*(Math.max(1,Math.min(total,strike))-1)/(total-1F);
        return maximumHealth*healthBudget/total*ramp;
    }
    private TribulationBalance(){}
}
