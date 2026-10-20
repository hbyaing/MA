package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.boss.BossVisualDefinition;
import cn.blockforge.generated.generatedmod.boss.BossVisualEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class BossGeoRenderer<T extends LivingEntity & BossVisualEntity> extends GeoEntityRenderer<T> {
    private final BossVisualDefinition definition;

    public BossGeoRenderer(EntityRendererProvider.Context context, BossVisualDefinition definition) {
        super(context, new BossGeoModel<T>(definition));
        this.definition = definition;
        shadowRadius = definition.shadow();
        withScale(definition.scale());
        if (definition.glow() != null) addRenderLayer(new GeoRenderLayer<T>(this) {
            @Override public void render(PoseStack pose, T entity, BakedGeoModel model, RenderType type,
                    MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
                if (entity.isInvisible()) return;
                RenderType glow = RenderType.eyes(definition.resource(definition.glow()));
                getRenderer().reRender(model, pose, buffers, entity, glow, buffers.getBuffer(glow),
                        partialTick, 15728640, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            }
        });
    }

    @Override public void preRender(PoseStack pose, T entity, BakedGeoModel model, MultiBufferSource buffers,
            VertexConsumer buffer, boolean reRender, float partialTick, int light, int overlay,
            float red, float green, float blue, float alpha) {
        super.preRender(pose, entity, model, buffers, buffer, reRender, partialTick, light, overlay, red, green, blue, alpha);
        if (!reRender) pose.translate(0, definition.renderOffsetY(), 0);
    }
}
