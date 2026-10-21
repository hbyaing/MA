/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animatable.instance;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class AnimatableInstanceCache {
    protected final GeoAnimatable animatable;
    private final AnimatableManager<?> manager;

    public AnimatableInstanceCache(GeoAnimatable animatable) {
        this.animatable = animatable;
        this.manager = new AnimatableManager<>(animatable);
    }

    @SuppressWarnings("unchecked")
    public <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long instanceId) {
        return (AnimatableManager<T>) manager;
    }
}
