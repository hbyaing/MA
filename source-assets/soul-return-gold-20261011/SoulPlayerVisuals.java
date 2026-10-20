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
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The retained SoulBody is opaque; the mobile player is only a skin projection. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class SoulPlayerVisuals {
    private record Projection(AbstractClientPlayer player, PlayerRenderer renderer, Matrix4f pose,
                              Matrix3f normal, Matrix4f view, Matrix4f projection, float partial, int light) {}
    private static final java.util.Map<java.util.UUID,Projection> PENDING=new java.util.LinkedHashMap<>();
    private static final MultiBufferSource.BufferSource SOUL_BUFFERS=MultiBufferSource.immediate(new BufferBuilder(4096));
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(event.phase==TickEvent.Phase.START) PENDING.clear();
    }
    private static boolean soulActive(AbstractClientPlayer player) {
        return player.hasEffect(ModEffects.YI_HUN_JI_PO.get())
                || player.hasEffect(ModEffects.YUAN_SHEN_CHU_QIAO.get());
    }

    private static boolean hiddenSoul(AbstractClientPlayer player) {
        var possession = player.getEffect(ModEffects.YI_HUN_JI_PO.get());
        if (possession != null && possession.getAmplifier() > 0) return true;
        // Out-of-body uses spectator mode; its invisibility must not hide the projection.
        return !player.hasEffect(ModEffects.YUAN_SHEN_CHU_QIAO.get()) && player.isInvisible();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void player(RenderPlayerEvent.Pre event) {
        var player = (AbstractClientPlayer) event.getEntity();
        if (!soulActive(player)) return;
        event.setCanceled(true);
        if (hiddenSoul(player)) return;
        var pose=event.getPoseStack().last();
        PENDING.put(player.getUUID(),new Projection(player,event.getRenderer(),new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),new Matrix4f(RenderSystem.getModelViewMatrix()),
                new Matrix4f(RenderSystem.getProjectionMatrix()),event.getPartialTick(),event.getPackedLight()));
    }

    @SubscribeEvent public static void afterWater(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL || PENDING.isEmpty()) return;
        var mc=Minecraft.getInstance();
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting=RenderSystem.getVertexSorting();
        var view=RenderSystem.getModelViewStack();view.pushPose();
        try {
            for(var entry:PENDING.values()) {
                if(entry.player.level()!=mc.level || !soulActive(entry.player) || hiddenSoul(entry.player)) continue;
                view.setIdentity();view.mulPoseMatrix(entry.view);RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(entry.projection,VertexSorting.DISTANCE_TO_ORIGIN);
                var pose=new PoseStack();pose.last().pose().set(entry.pose);pose.last().normal().set(entry.normal);
                renderSoul(entry,pose);
                SOUL_BUFFERS.endBatch();
            }
        } finally {
            PENDING.clear();view.popPose();RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection,sorting);
        }
    }

    private static void renderSoul(Projection entry,PoseStack pose) {
        var player=entry.player;
        var model = entry.renderer.getModel();
        float partial = entry.partial;
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
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - bodyYaw));
        pose.scale(-.9375F, -.9375F, .9375F);
        pose.translate(0, -1.501, 0);
        model.renderToBuffer(pose, SOUL_BUFFERS.getBuffer(
                SoulMaterial.type(player.getSkinTextureLocation())), entry.light,
                OverlayTexture.NO_OVERLAY, .65F, .9F, 1F, .35F);
        pose.popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        if (!soulActive(player)) return;
        event.setCanceled(true);
        if (hiddenSoul(player)
                || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (!(mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return;
        MultiBufferSource buffers = type -> new SoulVertex(event.getMultiBufferSource().getBuffer(
                SoulMaterial.type(player.getSkinTextureLocation())));
        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(.48, -.48, -.8);
        pose.mulPose(Axis.XP.rotationDegrees(-35));
        pose.translate("slim".equals(player.getModelName())?5.5/16:6.0/16,-.75,0);
        renderer.renderRightHand(pose, buffers, event.getPackedLight(), player);
        pose.popPose();
    }

    private static final class SoulMaterial extends RenderType {
        private static final java.util.Map<ResourceLocation,RenderType> TYPES=new java.util.HashMap<>();
        static RenderType type(ResourceLocation skin) {
            return TYPES.computeIfAbsent(skin,s->create("soul_projection",DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,4096,false,true,CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(s,false,false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                    .setOutputState(MAIN_TARGET).createCompositeState(false)));
        }
        private SoulMaterial(String n,VertexFormat f,VertexFormat.Mode m,int size,boolean c,boolean s,Runnable a,Runnable b) {
            super(n,f,m,size,c,s,a,b);
        }
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
