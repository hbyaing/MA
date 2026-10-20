package cn.blockforge.generated.generatedmod.boss;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public final class BossAnimations {
    public static <T extends LivingEntity & BossVisualEntity> void register(
            T entity, BossVisualDefinition definition, AnimatableManager.ControllerRegistrar registrar) {
        for (int index = 0; index < definition.idle().size(); index++) {
            String clip = definition.idle().get(index);
            boolean primary = index == 0;
            registrar.add(new AnimationController<T>(entity, "ambient_" + index, 4, state -> {
                if (!active(entity) || previewActive(entity)) return PlayState.STOP;
                String selected = primary && state.isMoving() && definition.move() != null ? definition.move() : clip;
                return state.setAndContinue(RawAnimation.begin().thenLoop(selected));
            }));
        }
        long[] previousStart = {Long.MIN_VALUE};
        registrar.add(new AnimationController<T>(entity, "visual_preview", 0, state -> {
            if (!active(entity) || !previewActive(entity)) return PlayState.STOP;
            if (previousStart[0] != entity.previewStarted()) {
                state.getController().forceAnimationReset();
                previousStart[0] = entity.previewStarted();
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay(entity.previewAnimation()));
        }));
    }

    private static <T extends LivingEntity & BossVisualEntity> boolean active(T entity) {
        return entity.isAlive() && entity.visualDefinition().model() != null;
    }

    private static <T extends LivingEntity & BossVisualEntity> boolean previewActive(T entity) {
        return !entity.previewAnimation().isEmpty() && entity.previewUntil() > entity.level().getGameTime();
    }

    private BossAnimations() {}
}
