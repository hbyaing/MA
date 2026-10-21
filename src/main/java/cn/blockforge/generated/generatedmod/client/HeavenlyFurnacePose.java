package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import java.util.*;

/** Casting pose and camera tracking; actual inventory remains untouched. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class HeavenlyFurnacePose {
    private record Rotations(float body,float oldBody,float head,float oldHead){}
    private record Palm(Vec3 point,long frame){}
    private static final Map<UUID,Rotations> SAVED=new HashMap<>();
    private static final Map<UUID,Palm> PALMS=new HashMap<>();
    private static final Map<UUID,Palm> LEFT_PALMS=new HashMap<>();
    private static long frame;
    @SubscribeEvent public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.START){frame++;PALMS.clear();LEFT_PALMS.clear();
        var mc=Minecraft.getInstance();if(mc.player!=null&&mc.screen==null){var tag=HeavenlyFurnaceVisuals.active(mc.player.getUUID());if(tag!=null){var d=HeavenlyFurnace.fireballCenter(tag).subtract(mc.player.getEyePosition(e.renderTickTime));float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z)),pitch=-(float)Math.toDegrees(Math.atan2(d.y,d.horizontalDistance()));mc.player.setYRot(yaw);mc.player.yRotO=yaw;mc.player.setXRot(pitch);mc.player.xRotO=pitch;}}
    }}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;var tag=HeavenlyFurnaceVisuals.active(mc.player.getUUID());if(tag==null)return;var d=HeavenlyFurnace.fireballCenter(tag).subtract(e.getCamera().getPosition());e.setYaw((float)Math.toDegrees(Math.atan2(-d.x,d.z)));e.setPitch(-(float)Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOW) public static void before(RenderPlayerEvent.Pre e){
        var p=e.getEntity();var tag=HeavenlyFurnaceVisuals.active(p.getUUID());if(tag==null||e.isCanceled())return;
        SAVED.put(p.getUUID(),new Rotations(p.yBodyRot,p.yBodyRotO,p.yHeadRot,p.yHeadRotO));
        Vec3 d=HeavenlyFurnace.fireballCenter(tag).subtract(p.getPosition(e.getPartialTick()));float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=yaw;
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void after(RenderPlayerEvent.Post e){
        var p=e.getEntity();var s=SAVED.remove(p.getUUID());if(s!=null){p.yBodyRot=s.body;p.yBodyRotO=s.oldBody;p.yHeadRot=s.head;p.yHeadRotO=s.oldHead;}
    }
    public static boolean apply(PlayerModel<AbstractClientPlayer> m,AbstractClientPlayer p,float partial){
        var tag=HeavenlyFurnaceVisuals.active(p.getUUID());if(tag==null||p.isSpectator())return false;
        Vec3 d=HeavenlyFurnace.fireballCenter(tag).subtract(p.getPosition(partial).add(0,1.4,0));
        float pitch=-(float)Math.atan2(d.y,d.horizontalDistance());
        m.body.xRot=m.body.yRot=m.body.zRot=0;m.head.xRot=pitch;m.head.yRot=m.head.zRot=0;
        m.leftArm.xRot=m.rightArm.xRot=-(float)Math.PI/2+pitch;
        m.leftArm.yRot=.12f;m.rightArm.yRot=-.12f;m.leftArm.zRot=-.08f;m.rightArm.zRot=.08f;
        m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.rightSleeve.copyFrom(m.rightArm);m.leftSleeve.copyFrom(m.leftArm);return true;
    }
    public static Vec3 palm(UUID owner,Vec3 fallback){
        return palm(owner,false,fallback);
    }
    public static Vec3 palm(UUID owner,boolean left,Vec3 fallback){
        var mc=Minecraft.getInstance();
        if(mc.player!=null&&owner.equals(mc.player.getUUID())&&mc.options.getCameraType().isFirstPerson()){
            var p=new Vector3f(left?-.36f:.36f,-.12f,-.72f).rotate(mc.gameRenderer.getMainCamera().rotation());
            return mc.gameRenderer.getMainCamera().getPosition().add(p.x,p.y,p.z);
        }
        var p=(left?LEFT_PALMS:PALMS).get(owner);return p!=null&&p.frame==frame?p.point:fallback;
    }
    @SubscribeEvent public static void first(RenderHandEvent e){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null||p.isInvisible()||HeavenlyFurnaceVisuals.active(p.getUUID())==null)return;
        e.setCanceled(true);if(e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND)return;
        if(!(mc.getEntityRenderDispatcher().getRenderer(p) instanceof PlayerRenderer renderer))return;
        for(boolean left:new boolean[]{false,true}){var m=renderer.getModel();var arm=left?m.leftArm:m.rightArm;var sleeve=left?m.leftSleeve:m.rightSleeve;
        var old=arm.storePose();var oldSleeve=sleeve.storePose();boolean av=arm.visible,sv=sleeve.visible;var pose=e.getPoseStack();pose.pushPose();
        try{
            pose.translate(left?-.36:.36,-.12,-.72);
            var point=pose.last().pose().transformPosition(new Vector3f());point.rotate(mc.gameRenderer.getMainCamera().rotation());
            (left?LEFT_PALMS:PALMS).put(p.getUUID(),new Palm(mc.gameRenderer.getMainCamera().getPosition().add(point.x,point.y,point.z),frame));
            arm.loadPose(arm.getInitialPose());sleeve.copyFrom(arm);arm.visible=true;sleeve.visible=p.isModelPartShown(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE:net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE);
            pose.mulPose(Axis.XP.rotationDegrees(-90));float center=("slim".equals(p.getModelName())?.5f:1f)*(left?1:-1);
            pose.translate(-(arm.x+center)/16,-(arm.y+10)/16,-arm.z/16);
            var skin=p.getSkinTextureLocation();arm.render(pose,e.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(skin)),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            sleeve.render(pose,e.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(skin)),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        }finally{arm.loadPose(old);sleeve.loadPose(oldSleeve);arm.visible=av;sleeve.visible=sv;pose.popPose();}
        }
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration{
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){for(String skin:e.getSkins())if(e.getSkin(skin) instanceof PlayerRenderer renderer)renderer.addLayer(new HandLayer(renderer));}
    }
    private static final class HandLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>{
        HandLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> p){super(p);}
        @Override public void render(PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,AbstractClientPlayer p,float walk,float amount,float partial,float age,float yaw,float pitch){
            if(HeavenlyFurnaceVisuals.active(p.getUUID())==null||TrailShaderBridge.shadowPass())return;
            for(boolean left:new boolean[]{false,true}){var handPose=new PoseStack();var position=p.getPosition(partial);handPose.translate(position.x,position.y+1.501*.9375,position.z);
            handPose.mulPose(Axis.YP.rotationDegrees(180-Mth.rotLerp(partial,p.yBodyRotO,p.yBodyRot)));handPose.scale(-.9375f,-.9375f,.9375f);
            var arm=left?getParentModel().leftArm:getParentModel().rightArm;arm.translateAndRotate(handPose);
            {
                float center=("slim".equals(p.getModelName())?.5f:1f)*(left?1:-1);
                var point=handPose.last().pose().transformPosition(new Vector3f(center/16,10f/16,0));
                (left?LEFT_PALMS:PALMS).put(p.getUUID(),new Palm(new Vec3(point.x,point.y,point.z),frame));
            }
            }
        }
    }
}
