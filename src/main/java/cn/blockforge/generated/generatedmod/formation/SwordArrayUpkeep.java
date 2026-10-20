package cn.blockforge.generated.generatedmod.formation;

import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;

/** 来源的 5 极品=5×729=3645 下品，按五行统一货币结算；不受渲染帧率影响。 */
public final class SwordArrayUpkeep {
    public static final int READY=240,PERIOD=1200;
    public static final long COST_LOW=5L*SpiritStoneGrade.EXQUISITE.value;
    public static boolean due(long age){return age>=READY&&(age-READY)%PERIOD==0;}
    /** 跳过 tick 时只结算已经到期的区间，重复处理同一刻不会再次扣款。 */
    public static long periodsDue(long age,long nextPayment){return age<nextPayment?0:1+(age-nextPayment)/PERIOD;}
    private SwordArrayUpkeep(){}
}
