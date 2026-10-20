/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animatable;

import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

public interface GeoAnimatable {
    void registerControllers(AnimatableManager.ControllerRegistrar controllers);

    AnimatableInstanceCache getAnimatableInstanceCache();

    default double getBoneResetTime() { return 0.0D; }

    default boolean shouldPlayAnimsWhileGamePaused() { return true; }

    default double getTick(Object animatable) { return 0.0D; }
}
