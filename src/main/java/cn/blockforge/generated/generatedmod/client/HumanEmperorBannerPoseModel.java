package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * 人皇幡手持渲染包装模型：把物品模型的变换链劫持下来，在使用（释放光线）状态下
 * 依据手持者实体的当前手臂姿态逐帧计算一个补偿四元数，使幡杆在世界里严格垂直于
 * 地面、幡面徽记朝前；空闲手持与其它场景（GUI/掉落/插地座 FIXED）保持原变换。
 * 第三人称配合 UseAnim.BOW：双臂水平前伸（手平行于地面）。
 */
public final class HumanEmperorBannerPoseModel implements BakedModel {
    private static final float DEG = (float) (Math.PI / 180.0);
    /** 使用态下的杆身缩放：第三人称 2 格杆缩到约 1.7 格，握点自然落在幡面下沿。 */
    private static final float THIRD_PERSON_SCALE = 0.85F;
    private static final float FIRST_PERSON_SCALE = 0.9F;

    private final BakedModel parent;
    private final ItemOverrides overrides;
    /** 由 resolve 在每次渲染前注入，随后 applyTransform 立即消费。仅渲染线程访问。 */
    private LivingEntity holder;
    private ItemStack heldStack = ItemStack.EMPTY;

    public HumanEmperorBannerPoseModel(BakedModel parent) {
        this.parent = parent;
        this.overrides = new ItemOverrides() {
            @Override public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level,
                                                LivingEntity entity, int seed) {
                holder = entity;
                heldStack = stack;
                return HumanEmperorBannerPoseModel.this;
            }
        };
    }

    // ---------- BakedModel 委托 ----------
    @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return parent.getQuads(state, side, random);
    }
    @Override public boolean useAmbientOcclusion() { return parent.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return parent.isGui3d(); }
    @Override public boolean usesBlockLight() { return parent.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return parent.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return parent.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return parent.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return overrides; }

    // ---------- 使用态补偿 ----------
    @Override public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack,
                                               boolean applyLeftHandTransform) {
        LivingEntity entity = holder;
        if (entity != null && isUsingBanner(entity)) {
            float partialTick = Minecraft.getInstance().getFrameTime();
            switch (context) {
                case THIRD_PERSON_RIGHT_HAND:
                    thirdPerson(poseStack, partialTick, entity, false);
                    return this;
                case THIRD_PERSON_LEFT_HAND:
                    thirdPerson(poseStack, partialTick, entity, true);
                    return this;
                case FIRST_PERSON_RIGHT_HAND:
                    firstPerson(poseStack, false);
                    return this;
                case FIRST_PERSON_LEFT_HAND:
                    firstPerson(poseStack, true);
                    return this;
                default:
                    break;
            }
        }
        return parent.applyTransform(context, poseStack, applyLeftHandTransform);
    }

    private static boolean isUsingBanner(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0
                && entity.getUseItem().getItem() instanceof HumanEmperorBannerItem;
    }

    /**
     * 第三人称：HumanoidModel 的 BOW_AND_ARROW 姿态把右臂设为
     * xRot=-90°+head.xRot, yRot=-0.1rad+head.yRot（左臂 yRot 为 +0.5rad+head.yRot），
     * 物品链为 arm·X(-90)·Y(180)·display。取 display=R=(arm·X(-90)·Y(180))^-1·X(180)
     * 即可让模型 +Y（杆顶）在世界里竖直向上、徽记 +Z 朝前，与头部转动解耦。
     */
    private void thirdPerson(PoseStack poseStack, float partialTick, LivingEntity entity, boolean leftArm) {
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float yawRelative = (headYaw - bodyYaw) * DEG;
        float headPitchRad = pitch * DEG;
        // HumanoidModel.poseRightArm / poseLeftArm 的 BOW_AND_ARROW 分支（弧度制常量）。
        float armY = (leftArm ? 0.5F : -0.1F) + yawRelative;
        float armX = -((float) Math.PI / 2F) + headPitchRad;
        Quaternionf arm = new Quaternionf().rotationZYX(0.0F, armY, armX);
        Quaternionf chain = new Quaternionf(arm)
                .mul(new Quaternionf().rotationX(-((float) Math.PI / 2F)))
                .mul(new Quaternionf().rotationY((float) Math.PI));
        Quaternionf correction = chain.invert().mul(new Quaternionf().rotationX((float) Math.PI));
        poseStack.mulPose(correction);
        poseStack.scale(THIRD_PERSON_SCALE, THIRD_PERSON_SCALE, THIRD_PERSON_SCALE);
    }

    /**
     * 第一人称：ItemInHandRenderer 的 BOW 分支依次乘
     * XP(-13.935)·YP(k·35.3)·ZP(k·-9.785)·S(1,1,1+0.2·f12)·YN(k·45)。
     * 求 R 使 P·R 保持模型 +Y 指向屏幕正上方（相机空间 +Y），
     * 且绕杆轴扭转以尽量让徽记面向镜头。
     */
    private void firstPerson(PoseStack poseStack, boolean leftHand) {
        float pull = pullProgress();
        int k = leftHand ? -1 : 1;
        Matrix4f chain = new Matrix4f()
                .mul(new Matrix4f().rotateX(-13.935F * DEG))
                .mul(new Matrix4f().rotateY(k * 35.3F * DEG))
                .mul(new Matrix4f().rotateZ(k * -9.785F * DEG))
                .mul(new Matrix4f().scale(1.0F, 1.0F, 1.0F + 0.2F * pull))
                .mul(new Matrix4f().rotateY(-k * 45.0F * DEG));
        Vector3f wanted = chain.invert().transformDirection(new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f());
        wanted.normalize();
        // 以 wanted 为 Y 轴构造正交基；让徽记（模型 +Z）落在屏幕平面内最朝外的一侧。
        Vector3f ez = new Vector3f(0.0F, 0.0F, 1.0F);
        Vector3f zAxis = new Vector3f(ez).sub(new Vector3f(wanted).mul(wanted.dot(ez)));
        if (zAxis.lengthSquared() < 1.0E-6F) zAxis = new Vector3f(0.0F, 0.0F, 1.0F);
        zAxis.normalize();
        Vector3f xAxis = new Vector3f(wanted).cross(zAxis);
        poseStack.mulPoseMatrix(new Matrix4f(
                xAxis.x, xAxis.y, xAxis.z, 0.0F,
                wanted.x, wanted.y, wanted.z, 0.0F,
                zAxis.x, zAxis.y, zAxis.z, 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F));
        poseStack.scale(FIRST_PERSON_SCALE, FIRST_PERSON_SCALE, FIRST_PERSON_SCALE);
    }

    /** 复刻 ItemInHandRenderer 蓄力进度 f12（对本地玩家；第一人称只渲染本地玩家）。 */
    private float pullProgress() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || heldStack.isEmpty()) return 0.0F;
        float f8 = (float) heldStack.getUseDuration()
                - ((float) player.getUseItemRemainingTicks() - Minecraft.getInstance().getFrameTime() + 1.0F);
        float f12 = f8 / 20.0F;
        f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
        return Math.min(1.0F, Math.max(0.0F, f12));
    }
}
