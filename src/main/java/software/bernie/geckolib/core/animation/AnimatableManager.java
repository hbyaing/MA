/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animation;

import software.bernie.geckolib.core.animatable.GeoAnimatable;

public class AnimatableManager<T extends GeoAnimatable> {
    private final T animatable;

    public AnimatableManager(T animatable) {
        this.animatable = animatable;
    }

    public void addController(AnimationController<T> controller) {}

    public void removeController(String name) {}

    public static final class ControllerRegistrar {
        public ControllerRegistrar() {}

        @SafeVarargs
        public final ControllerRegistrar add(AnimationController<?>... controllers) {
            return this;
        }

        public ControllerRegistrar remove(String name) {
            return this;
        }
    }
}
