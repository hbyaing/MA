package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.TadpoleRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.frog.Tadpole;

/**
 * 鱼类竖直游泳渲染器。
 *
 * 原版 1.20.1 的 Cod/Salmon/TropicalFish/Pufferfish/Tadpole/Turtle 模型与渲染器都从不把
 * 实体的俯仰角（xRot）应用到鱼身上：无论七十二变（伪装鱼）还是神魂附身控制的鱼，低头时
 * 鱼身依旧水平“平移滑行”，看起来无法切换成竖直游泳姿态。这些子类在原版 setupRotations
 * （已绕体轴完成偏航旋转）之后，补一段绕鱼身中心的俯仰旋转；角度符号与原版激流冲刺的
 * {@code -90 - xRot} 约定同源（正俯仰 = 视线朝下 = 鱼头朝下）。
 *
 * 离水状态完全保持原版行为（鳕鱼等上岸后仍是侧翻扑腾），只有 {@code isInWater()} 为真时
 * 才施加俯仰。海豚/美西螈/守卫者等原版模型自身已按 headPitch 弯身，不在此列。
 */
public final class VerticalSwimFishRenderer {
    private VerticalSwimFishRenderer() {}

    /**
     * 在原版旋转之后追加俯仰角。必须在 {@code super.setupRotations} 内（含 YP(180-yaw) 与
     * 鱼体摆动）之后调用，此时局部 -Z 即鱼头方向；绕鱼身高度中心旋转，避免鱼头朝下时整条
     * 鱼上下漂移。
     */
    private static void applyVerticalSwimPitch(LivingEntity entity, PoseStack poseStack, float partialTick) {
        if (!entity.isInWater()) return;
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        if (Math.abs(pitch) < 0.75F) return;
        // 相机俯仰可达 ±90°，限制在 ±80° 内，防止完全竖直时与原版头部摆动叠加出翻转感。
        pitch = Mth.clamp(pitch, -80.0F, 80.0F);
        float pivot = entity.getBbHeight() * entity.getScale() * 0.5F;
        poseStack.translate(0.0D, pivot, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(0.0D, -pivot, 0.0D);
    }

    /** 鳕鱼：低头下潜、抬头上浮，离水仍走原版侧翻逻辑。 */
    public static class TiltingCod extends CodRenderer {
        public TiltingCod(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(Cod entity, PoseStack poseStack, float bob, float yRot, float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }

    /** 鲑鱼。 */
    public static class TiltingSalmon extends SalmonRenderer {
        public TiltingSalmon(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(Salmon entity, PoseStack poseStack, float bob, float yRot, float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }

    /** 热带鱼。 */
    public static class TiltingTropicalFish extends TropicalFishRenderer {
        public TiltingTropicalFish(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(TropicalFish entity, PoseStack poseStack, float bob, float yRot,
                float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }

    /** 河豚（含膨胀三档模型，俯仰作用于整个鱼身）。 */
    public static class TiltingPufferfish extends PufferfishRenderer {
        public TiltingPufferfish(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(Pufferfish entity, PoseStack poseStack, float bob, float yRot,
                float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }

    /** 蝌蚪。 */
    public static class TiltingTadpole extends TadpoleRenderer {
        public TiltingTadpole(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(Tadpole entity, PoseStack poseStack, float bob, float yRot,
                float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }

    /** 海龟：原版只摆头鳍、不倾身，这里补整体俯仰（模型自带的颈部 headPitch 保留）。 */
    public static class TiltingTurtle extends TurtleRenderer {
        public TiltingTurtle(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(Turtle entity, PoseStack poseStack, float bob, float yRot,
                float partialTick) {
            super.setupRotations(entity, poseStack, bob, yRot, partialTick);
            applyVerticalSwimPitch(entity, poseStack, partialTick);
        }
    }
}
