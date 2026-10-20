package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

/** 心魔沿用玩家身形与装备，并使用原版生物渲染流程保持朝向、名称和阴影正确。 */
public final class HeartDemonRenderer extends MobRenderer<HeartDemonEntity, PlayerModel<HeartDemonEntity>> {
    private final PlayerModel<HeartDemonEntity> standardModel;
    private final PlayerModel<HeartDemonEntity> slimModel;
    private final cn.blockforge.generated.generatedmod.client.boss.BossGeoRenderer<HeartDemonEntity> phaseTwo;

    public HeartDemonRenderer(EntityRendererProvider.Context context) {
        this(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true));
    }

    private HeartDemonRenderer(EntityRendererProvider.Context context,
                               PlayerModel<HeartDemonEntity> standardModel,
                               PlayerModel<HeartDemonEntity> slimModel) {
        super(context, standardModel, 0.5F);
        this.standardModel = standardModel;
        this.slimModel = slimModel;
        phaseTwo = new cn.blockforge.generated.generatedmod.client.boss.BossGeoRenderer<>(context,
                cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.get("heart_demon_phase2"));
    }

    @Override
    public void render(HeartDemonEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight) {
        shadowRadius = entity.visualDefinition().shadow();
        if (entity.visualPhase() == HeartDemonEntity.VisualPhase.PHASE_2) {
            phaseTwo.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
            return;
        }
        // LivingEntityRenderer 负责实体朝向、翻转检测、名称标签和模型的标准坐标变换。
        model = model(entity);
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private PlayerModel<HeartDemonEntity> model(HeartDemonEntity entity) {
        net.minecraft.client.player.AbstractClientPlayer owner = clientOwner(entity);
        return owner != null && "slim".equals(owner.getModelName()) ? slimModel : standardModel;
    }

    private net.minecraft.client.player.AbstractClientPlayer clientOwner(HeartDemonEntity entity) {
        if (entity.ownerUuid() == null || Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.players().stream()
                .filter(player -> player.getUUID().equals(entity.ownerUuid()))
                .findFirst().orElse(null);
    }

    @Override
    public ResourceLocation getTextureLocation(HeartDemonEntity entity) {
        net.minecraft.client.player.AbstractClientPlayer owner = clientOwner(entity);
        if (owner != null) return owner.getSkinTextureLocation();
        return entity.ownerUuid() == null ? DefaultPlayerSkin.getDefaultSkin()
                : DefaultPlayerSkin.getDefaultSkin(entity.ownerUuid());
    }
}
