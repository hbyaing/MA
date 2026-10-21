package cn.blockforge.generated.generatedmod.realmflight;

import java.util.List;
import java.util.stream.IntStream;

/** Cosmetic selection with the same realm unlock boundary on both logical sides. */
public final class RealmFlightSelection {
    public static final int FOLLOW_CURRENT = 0;
    private static final String[] NAMES = {"元婴", "化神", "炼虚", "合体", "大乘", "地仙"};

    public static int sanitizePreference(int actualRealm, int preferredRealm) {
        if (actualRealm < RealmFlightLevel.FIRST_REALM || preferredRealm < RealmFlightLevel.FIRST_REALM)
            return FOLLOW_CURRENT;
        return Math.min(Math.min(actualRealm, RealmFlightLevel.LAST_REALM), preferredRealm);
    }

    public static int resolve(int actualRealm, int preferredRealm) {
        int current = Math.max(0, Math.min(RealmFlightLevel.LAST_REALM, actualRealm));
        int selected = sanitizePreference(actualRealm, preferredRealm);
        return selected == FOLLOW_CURRENT ? current : selected;
    }

    public static List<Integer> unlockedRealms(int actualRealm) {
        int last = Math.min(RealmFlightLevel.LAST_REALM, actualRealm);
        return last < RealmFlightLevel.FIRST_REALM ? List.of()
                : IntStream.rangeClosed(RealmFlightLevel.FIRST_REALM, last).boxed().toList();
    }

    public static String label(int realm) {
        return realm < RealmFlightLevel.FIRST_REALM || realm > RealmFlightLevel.LAST_REALM
                ? "随当前境界" : NAMES[realm - RealmFlightLevel.FIRST_REALM] + " Lv" + RealmFlightLevel.forRealm(realm);
    }

    private RealmFlightSelection() {}
}
