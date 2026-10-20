package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Husk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Deliberately bypasses LivingEntityRenderer and its events, like some modded dummies. Test source only. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CustomBurnCaptureRenderer extends EntityRenderer<Husk> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("minecraft","textures/entity/zombie/husk.png");
    private final ZombieModel<Husk> model;
    private CustomBurnCaptureRenderer(EntityRendererProvider.Context context) {
        super(context);model=new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK));
    }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        if(Boolean.getBoolean("wuxingxiantu.flameCustomRenderer"))
            event.registerEntityRenderer(net.minecraft.world.entity.EntityType.HUSK,CustomBurnCaptureRenderer::new);
    }
    @Override public ResourceLocation getTextureLocation(Husk entity){return TEXTURE;}
    @Override public void render(Husk entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        try {
            pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.scale(-1,-1,1);pose.translate(0,-1.501,0);
            model.young=entity.isBaby();model.riding=entity.isPassenger();model.attackTime=entity.getAttackAnim(partial);
            model.prepareMobModel(entity,0,0,partial);model.setupAnim(entity,0,0,entity.tickCount+partial,0,0);
            model.renderToBuffer(pose,buffers.getBuffer(model.renderType(TEXTURE)),light,OverlayTexture.NO_OVERLAY,1,1,1,1);
        } finally {pose.popPose();}
        WeaponFlameClientCapture.recordAnchor(entity,pose);
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
