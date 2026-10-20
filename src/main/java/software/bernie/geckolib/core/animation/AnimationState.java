/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animation;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.object.PlayState;

public class AnimationState<T extends GeoAnimatable> {
    protected AnimationController<T> controller;
    public double animationTick;

    public AnimationState(T animatable, float limbSwing, float limbSwingAmount, float partialTick, boolean netSync) {}

    public T getAnimatable() { return null; }

    public float getPartialTick() { return 0; }

    public boolean isMoving() { return false; }

    public AnimationController<T> getController() { return controller; }

    public void setAnimation(RawAnimation animation) {}

    public PlayState setAndContinue(RawAnimation animation) { return PlayState.CONTINUE; }

    public boolean isCurrentAnimation(RawAnimation animation) { return false; }
}
