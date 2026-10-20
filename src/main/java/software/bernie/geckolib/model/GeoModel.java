/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

public abstract class GeoModel<T extends GeoAnimatable> {
    public GeoModel() {}

    public abstract ResourceLocation getModelResource(T animatable);

    public abstract ResourceLocation getTextureResource(T animatable);

    public abstract ResourceLocation getAnimationResource(T animatable);

    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }

    public BakedGeoModel getBakedModel(ResourceLocation modelLocation) { return null; }
}
