/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Collections;
import java.util.List;

public interface GeoRenderer<T extends GeoAnimatable> {
    GeoModel<T> getGeoModel();

    T getAnimatable();

    default List<GeoRenderLayer<T>> getRenderLayers() {
        return Collections.emptyList();
    }

    default void reRender(BakedGeoModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                          T animatable, RenderType renderType, VertexConsumer buffer,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {}

    default void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                           VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {}
}
