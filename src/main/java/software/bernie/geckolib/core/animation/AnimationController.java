/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animation;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.object.PlayState;

public class AnimationController<T extends GeoAnimatable> {
    protected final T animatable;
    protected final String name;

    public AnimationController(T animatable, AnimationStateHandler<T> stateHandler) {
        this(animatable, "Default", 0, stateHandler);
    }

    public AnimationController(T animatable, String name, AnimationStateHandler<T> stateHandler) {
        this(animatable, name, 0, stateHandler);
    }

    public AnimationController(T animatable, int slideHeight, AnimationStateHandler<T> stateHandler) {
        this(animatable, "Default", slideHeight, stateHandler);
    }

    public AnimationController(T animatable, String name, int slideHeight, AnimationStateHandler<T> stateHandler) {
        this.animatable = animatable;
        this.name = name;
    }

    public String getName() { return name; }

    public void forceAnimationReset() {}

    public void setAnimation(RawAnimation animation) {}

    public AnimationController<T> setAnimationSpeed(double speed) { return this; }

    public AnimationController<T> triggerAnimation(String animName) { return this; }

    public interface AnimationStateHandler<A extends GeoAnimatable> {
        PlayState handle(AnimationState<A> animationState);
    }
}
