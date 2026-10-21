/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.util;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;

public final class GeckoLibUtil {
    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
        return new AnimatableInstanceCache(animatable);
    }

    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean multiInstance) {
        return new AnimatableInstanceCache(animatable);
    }
}
