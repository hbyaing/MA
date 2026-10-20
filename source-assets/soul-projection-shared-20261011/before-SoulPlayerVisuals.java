package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The retained SoulBody is opaque; the mobile player is only a skin projection. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class SoulPlayerVisuals {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void player(RenderPlayerEvent.Pre event) {
        var player = (AbstractClientPlayer) event.getEntity();
        var effect = player.getEffect(ModEffects.YI_HUN_JI_PO.get());
        if (effect == null) return;
        event.setCanceled(true);
        if (effect.getAmplifier() > 0 || player.isInvisible()) return;
        var model = event.getRenderer().getModel();
        float partial = event.getPartialTick();
        float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
        model.setAllVisible(true);
        model.crouching = player.isCrouching();
        model.riding = false;
        model.young = false;
        model.attackTime = 0;
        model.leftArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.EMPTY;
        model.prepareMobModel(player, 0, 0, partial);
        model.setupAnim(player, 0, 0, player.tickCount + partial,
                Mth.wrapDegrees(headYaw - bodyYaw), Mth.lerp(partial, player.xRotO, player.getXRot()));
        var pose = event.getPoseStack();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - bodyYaw));
        pose.scale(-.9375F, -.9375F, .9375F);
        pose.translate(0, -1.501, 0);
        model.renderToBuffer(pose, event.getMultiBufferSource().getBuffer(
                RenderType.entityTranslucent(player.getSkinTextureLocation())), event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, .65F, .9F, 1F, .35F);
        pose.popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        var effect = player.getEffect(ModEffects.YI_HUN_JI_PO.get());
        if (effect == null) return;
        event.setCanceled(true);
        if (effect.getAmplifier() > 0 || player.isInvisible()
                || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (!(mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
        MultiBufferSource buffers = type -> new SoulVertex(event.getMultiBufferSource().getBuffer(
                RenderType.entityTranslucent(player.getSkinTextureLocation())));
        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(.48, -.48, -.8);
        pose.mulPose(Axis.XP.rotationDegrees(-35));
        pose.translate("slim".equals(player.getModelName())?5.5/16:6.0/16,-.75,0);
        renderer.renderRightHand(pose, buffers, event.getPackedLight(), player);
        pose.popPose();
    }

    private record SoulVertex(VertexConsumer delegate) implements VertexConsumer {
        public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x,y,z); return this; }
        public VertexConsumer color(int r,int g,int b,int a) { delegate.color((int)(r*.65F),(int)(g*.9F),b,(int)(a*.35F)); return this; }
        public VertexConsumer uv(float u,float v) { delegate.uv(u,v); return this; }
        public VertexConsumer overlayCoords(int u,int v) { delegate.overlayCoords(u,v); return this; }
        public VertexConsumer uv2(int u,int v) { delegate.uv2(u,v); return this; }
        public VertexConsumer normal(float x,float y,float z) { delegate.normal(x,y,z); return this; }
        public void endVertex() { delegate.endVertex(); }
        public void defaultColor(int r,int g,int b,int a) { delegate.defaultColor((int)(r*.65F),(int)(g*.9F),b,(int)(a*.35F)); }
        public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
    }
    private SoulPlayerVisuals() {}
}
