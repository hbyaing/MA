package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.RealmFlightSelection;

/** Unlock and downgrade behavior shared by inventory preferences and authoritative tracking. */
public final class RealmFlightSelectionRegression {
    public static void run() {
        int checks = 0;
        for (int actual = -2; actual <= 10; actual++) {
            var unlocked = RealmFlightSelection.unlockedRealms(actual);
            require(unlocked.size() == Math.max(0, Math.min(8, actual) - 2), "Only current and lower unlocked choices"); checks++;
            for (int preference : new int[]{Integer.MIN_VALUE, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, Integer.MAX_VALUE}) {
                int selected = RealmFlightSelection.sanitizePreference(actual, preference);
                int visual = RealmFlightSelection.resolve(actual, preference);
                require(selected == 0 || unlocked.contains(selected), "Untrusted request cannot select a locked realm"); checks++;
                require(visual == (selected == 0 ? Math.max(0, Math.min(8, actual)) : selected), "Auto follows current; explicit resolves selection"); checks++;
                require(RealmFlightSelection.sanitizePreference(actual, selected) == selected, "Sanitization is stable"); checks++;
            }
        }
        require(RealmFlightSelection.resolve(8, 3) == 3, "Earth immortal can choose Nascent Soul appearance"); checks++;
        require(RealmFlightSelection.resolve(4, 8) == 4, "Downgrade cannot retain locked visual tier"); checks++;
        require(RealmFlightSelection.sanitizePreference(2, 8) == 0, "Pre-unlock has no explicit visual choice"); checks++;
        require(RealmFlightSelection.resolve(8, 0) == 8, "Default follows current tier"); checks++;
        System.out.println("PASS: " + checks + " realm-flight cosmetic unlock/selection checks");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private RealmFlightSelectionRegression() {}
}
