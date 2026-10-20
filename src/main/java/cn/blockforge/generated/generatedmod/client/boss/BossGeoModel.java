package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.boss.BossVisualDefinition;
import cn.blockforge.generated.generatedmod.boss.BossVisualEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BossGeoModel<T extends BossVisualEntity> extends GeoModel<T> {
    private final BossVisualDefinition definition;
    public BossGeoModel(BossVisualDefinition definition) { this.definition = definition; }
    @Override public ResourceLocation getModelResource(T entity) { return definition.resource(definition.model()); }
    @Override public ResourceLocation getTextureResource(T entity) { return definition.resource(definition.texture()); }
    @Override public ResourceLocation getAnimationResource(T entity) { return definition.resource(definition.animation()); }
    @Override public RenderType getRenderType(T entity, ResourceLocation texture) { return RenderType.entityTranslucent(texture); }
}
