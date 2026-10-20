/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib;

public class GeckoLib {
    public static final String MOD_ID = "geckolib";
    public static volatile boolean hasInitialized = false;

    public static synchronized void initialize() {
        hasInitialized = true;
    }
}
