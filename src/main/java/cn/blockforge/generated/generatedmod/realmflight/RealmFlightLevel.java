package cn.blockforge.generated.generatedmod.realmflight;

/** Display metadata only; movement and mana remain keyed by realm and cultivation stage. */
public final class RealmFlightLevel {
    public static final int FIRST_REALM = 3;
    public static final int LAST_REALM = 8;

    public static int forRealm(int realm) {
        return realm < FIRST_REALM ? 0 : Math.min(LAST_REALM, realm) - FIRST_REALM + 1;
    }

    public static int amplifierForRealm(int realm) {
        return Math.max(0, forRealm(realm) - 1);
    }

    public static String labelForAmplifier(int amplifier) {
        return "Lv" + (Math.max(0, Math.min(LAST_REALM - FIRST_REALM, amplifier)) + 1);
    }

    private RealmFlightLevel() {}
}
