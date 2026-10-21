package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.SpiritOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 灵力球渲染：与原版 ExperienceOrbRenderer（1.20.1，已反编译核对）逐行同构。
 *
 * <p>不再使用任何独立贴图——直接复用原版内置纹理
 * {@code minecraft:textures/entity/experience_orb.png}（64×64，4×4 共 16 个球体变体）。
 * 该纹理本体是灰白色的，原版经验球的黄绿色完全由顶点色 {@code .color(r, g, b, 128)}
 * 在渲染代码里乘出来；这里保持同样的三条 sin 波形，但整体提亮为冰蓝 #6EC6FF：
 * 呼吸波底幅从 50% 抬到 60%（最暗时刻也是亮蓝），顶点 alpha 由 128 提到 215，
 * 并把方块光强制拉满到 15，使灵球在任何环境下都像自发光一样明亮：
 * <pre>
 *   原版:  R = (sin(t) + 1) * 0.5 * 255   同相满摆幅
 *          G = 255                        恒亮
 *          B = (sin(t + 4π/3) + 1) * 0.1 * 255   反相小摆幅   → 黄绿呼吸
 *   灵力:  G = 0xC6 * (0.6 + 0.4 * sin(t))         同相呼吸 119..198
 *          B = 0xFF                                 恒亮
 *          R = 0x6E * (0.6 + 0.4 * sin(t + 4π/3))   反相微光 66..110   → 亮蓝呼吸
 * </pre>
 * 0.15 阴影与原版经验球一致，半透明度与自发光强度则明显更高。
 */
public class SpiritOrbRenderer extends EntityRenderer<SpiritOrbEntity> {
    /** 原版经验球内置纹理（Mojang 资产，按路径直接引用，模组无需自带贴图文件）。 */
    private static final ResourceLocation ORB_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(ORB_TEXTURE);

    /** 目标染色 #6EC6FF（明亮冰蓝）。 */
    private static final int TINT_R = 0x6E;
    private static final int TINT_G = 0xC6;
    private static final int TINT_B = 0xFF;

    public SpiritOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    /** 方块光直接拉满：灵球在洞穴、夜晚等暗处也保持自发光般的明亮。 */
    @Override
    protected int getBlockLightLevel(SpiritOrbEntity orb, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(SpiritOrbEntity orb, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // 按球体价值选 16 宫格纹理变体（与原版 getIcon → UV 计算一致）
        int icon = orb.getIcon();
        float u0 = (icon % 4 * 16) / 64.0F;
        float u1 = (icon % 4 * 16 + 16) / 64.0F;
        float v0 = (icon / 4 * 16) / 64.0F;
        float v1 = (icon / 4 * 16 + 16) / 64.0F;

        float t = (orb.tickCount + partialTicks) / 2.0F;
        float pulse = (Mth.sin(t) + 1.0F) * 0.5F;
        float counter = (Mth.sin(t + Mth.PI * 4.0F / 3.0F) + 1.0F) * 0.5F;
        int red = (int) (TINT_R * (0.6F + 0.4F * counter));
        int green = (int) (TINT_G * (0.6F + 0.4F * pulse));

        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.3F, 0.3F, 0.3F);

        VertexConsumer buffer = bufferSource.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        vertex(buffer, matrix4f, matrix3f, -0.5F, -0.25F, red, green, u0, v1, packedLight);
        vertex(buffer, matrix4f, matrix3f, 0.5F, -0.25F, red, green, u1, v1, packedLight);
        vertex(buffer, matrix4f, matrix3f, 0.5F, 0.75F, red, green, u1, v0, packedLight);
        vertex(buffer, matrix4f, matrix3f, -0.5F, 0.75F, red, green, u0, v0, packedLight);
        poseStack.popPose();
        super.render(orb, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    /** 与原版 vertex() 同构，但 alpha 提到 215：灵球更实、更亮，不再灰蒙蒙。 */
    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normals,
                               float x, float y, int red, int green, float u, float v, int packedLight) {
        buffer.vertex(matrix, x, y, 0.0F)
                .color(red, green, TINT_B, 215)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normals, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritOrbEntity entity) {
        return ORB_TEXTURE;
    }
}
