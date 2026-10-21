package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;

/** 所有坐标采用烘焙模型的像素单位（16 单位为一格），旋转绕真实握柄中心进行。 */
public record SwordHeldTransform(GripAnchor gripOffset, Rotation modelRotation,
                                 Rotation firstPersonRotation, float firstPersonScale,
                                 Rotation thirdPersonRotation, float thirdPersonScale,
                                 Rotation groundRotation) {
    public record GripAnchor(float x, float y, float z) {}
    public record Rotation(float x, float y, float z) {
        void apply(PoseStack pose, boolean left) {
            // 与 ItemTransform.apply 一致：左手镜像 Y/Z，矩阵按 X、Y、Z 相乘。
            pose.mulPose(Axis.XP.rotationDegrees(x));
            pose.mulPose(Axis.YP.rotationDegrees(left ? -y : y));
            pose.mulPose(Axis.ZP.rotationDegrees(left ? -z : z));
        }
    }

    public static boolean held(ItemDisplayContext context) {
        return context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public void apply(PoseStack pose, ItemDisplayContext context, float baseScale) {
        boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (held(context)) {
            // 上游 ItemInHandLayer / ItemInHandRenderer 已提供当前手掌挂点。
            // 不再叠加整剑位置补偿；第一、第三人称共用同一个 GripAnchor。
            (context.firstPerson() ? firstPersonRotation : thirdPersonRotation).apply(pose, left);
            float scale = baseScale * (context.firstPerson() ? firstPersonScale : thirdPersonScale);
            pose.scale(scale, scale, scale);
            // 原模型剑尖为 -X。先转到原版剑的 XY 对角线，再应用手持姿态。
            // 左手额外旋转 180°，将源模型长轴映射到镜像后的对角线，避免剑尖反向。
            if (left) pose.mulPose(Axis.YP.rotationDegrees(180));
            modelRotation.apply(pose, false);
            // 内层 ItemRenderer.render 还会平移 -0.5，故此处用中心化后的锚点。
            pose.translate((8 - gripOffset.x()) / 16.0, (8 - gripOffset.y()) / 16.0,
                    (8 - gripOffset.z()) / 16.0);
        } else {
            float scale = baseScale;
            switch (context) {
                case GROUND -> { pose.translate(0, .10, 0); groundRotation.apply(pose, false); scale *= .75F; }
                case FIXED -> new Rotation(90, 0, -45).apply(pose, false);
                case HEAD -> { pose.translate(0, .4, 0); scale *= .8F; }
                default -> {}
            }
            pose.scale(scale, scale, scale);
        }
    }
}
