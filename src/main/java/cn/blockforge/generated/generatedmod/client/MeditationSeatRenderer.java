package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.MeditationSeatEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;

/** 隐形座位实体不绘制任何几何体，玩家本身仍正常渲染。 */
public final class MeditationSeatRenderer extends EntityRenderer<MeditationSeatEntity> {
    public MeditationSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MeditationSeatEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(MeditationSeatEntity entity) {
        return new ResourceLocation("wuxingxiantu", "textures/block/meditation_cushion.png");
    }
}
