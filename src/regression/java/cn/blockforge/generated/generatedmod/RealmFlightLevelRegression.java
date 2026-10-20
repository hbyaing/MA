package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.RealmFlightLevel;

/** Public level metadata has six realm tiers and stays bounded for saved/command data. */
public final class RealmFlightLevelRegression {
    public static void run() {
        for(int realm=3;realm<=8;realm++) {
            int expected=realm-2;
            require(RealmFlightLevel.forRealm(realm)==expected,"Realm " + realm + " has Lv" + expected);
            require(RealmFlightLevel.amplifierForRealm(realm)==expected-1,"Effect amplifier is zero based");
            require(RealmFlightLevel.labelForAmplifier(expected-1).equals("Lv"+expected),"HUD uses explicit Lv level");
        }
        require(RealmFlightLevel.forRealm(2)==0&&RealmFlightLevel.forRealm(Integer.MIN_VALUE)==0,
                "Realms below Nascent Soul have no realm-flight level");
        require(RealmFlightLevel.forRealm(Integer.MAX_VALUE)==6,"Invalid high realm cannot overflow effect metadata");
        require(RealmFlightLevel.labelForAmplifier(Integer.MIN_VALUE).equals("Lv1")
                &&RealmFlightLevel.labelForAmplifier(Integer.MAX_VALUE).equals("Lv6"),"Stale amplifier labels stay bounded");
        System.out.println("Realm flight level regression: six realm levels, zero-based amplifier and HUD labels passed");
    }

    private static void require(boolean result,String message) {
        if(!result)throw new AssertionError(message);
    }

    private RealmFlightLevelRegression() {}
}
