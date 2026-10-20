/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.List;

public class GeoEntityRenderer<T extends Entity & GeoAnimatable> extends EntityRenderer<T> implements GeoRenderer<T> {
    protected final List<GeoRenderLayer<T>> renderLayers = new ArrayList<>();
    protected final GeoModel<T> model;
    protected T animatable;
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    public GeoEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context);
        this.model = model;
    }

    @Override
    public GeoModel<T> getGeoModel() { return this.model; }

    @Override
    public T getAnimatable() { return this.animatable; }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.model.getTextureResource(entity);
    }

    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() { return this.renderLayers; }

    public GeoEntityRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.add(renderLayer);
        return this;
    }

    public GeoEntityRenderer<T> withScale(float scale) {
        this.scaleWidth = scale;
        this.scaleHeight = scale;
        return this;
    }

    public GeoEntityRenderer<T> withScale(float scaleX, float scaleY) {
        this.scaleWidth = scaleX;
        this.scaleHeight = scaleY;
        return this;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {}

    @Override
    public void reRender(BakedGeoModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                         T animatable, RenderType renderType, VertexConsumer buffer,
                         float partialTick, int packedLight, int packedOverlay,
                         float red, float green, float blue, float alpha) {}
}
