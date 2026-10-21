package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.SoulBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class SoulBodyRenderer extends MobRenderer<SoulBodyEntity, PlayerModel<SoulBodyEntity>> {
    private final PlayerModel<SoulBodyEntity> wide, slim;
    public SoulBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), .5F);
        wide = model; slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }
    @Override public ResourceLocation getTextureLocation(SoulBodyEntity body) {
        var id = body.owner().orElse(body.getUUID());
        var connection = Minecraft.getInstance().getConnection();
        var info = connection == null ? null : connection.getPlayerInfo(id);
        return info == null ? DefaultPlayerSkin.getDefaultSkin(id) : info.getSkinLocation();
    }
    @Override public void render(SoulBodyEntity body, float yaw, float partial,
            com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        var connection = Minecraft.getInstance().getConnection();
        var info = connection == null ? null : body.owner().map(connection::getPlayerInfo).orElse(null);
        model = info != null && "slim".equals(info.getModelName()) ? slim : wide;
        super.render(body, yaw, partial, pose, buffers, light);
    }
}
