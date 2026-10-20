package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.balance.RealmBalance;

/** Alchemy costs use the same realm bases as mana and breakthroughs. */
public final class AlchemyBalance {
    public static int burnPerSecond(int furnaceTier) {
        return Math.max(1, RealmBalance.mana(ProgressionGate.requiredRealmForFurnace(furnaceTier)) / 400);
    }
    private static int tier(int value){return Math.max(0,Math.min(5,value));}
    private static final int[] STORED_SECONDS={120,180,240,300,360,420};
    private static final int[] INJECTION_SECONDS={2,4,6,8,10,12};
    public static int fuelCapacity(int furnaceTier) { return burnPerSecond(furnaceTier) * STORED_SECONDS[tier(furnaceTier)]; }
    public static int injectionPerTransfer(int furnaceTier){return burnPerSecond(furnaceTier)*INJECTION_SECONDS[tier(furnaceTier)];}
    public static int injectionPerSecond(int furnaceTier){return injectionPerTransfer(furnaceTier)*5;}
    public static long cultivationGain(int tier, int grade) {
        return Math.max(1, RealmBalance.cultivation(tier, 0) / 5) * Math.max(1, Math.min(4, grade));
    }
    private AlchemyBalance() {}
}
