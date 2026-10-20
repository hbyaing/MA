package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The render root rotates about the blade; bones only describe the standing stance. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class YujianRidingPose {
    private record Saved(float body,float oldBody,float walk) {}
    private static final java.util.Map<java.util.UUID,Saved> SAVED=new java.util.HashMap<>();
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void before(net.minecraftforge.client.event.RenderPlayerEvent.Pre event){
        var player=event.getEntity();
        if(cn.blockforge.generated.generatedmod.ParalysisControl.blocked(player)||event.isCanceled() || JindouCloudRenderState.isActive(player)
                || !(player.getVehicle() instanceof ControlledSwordEntity sword) || !sword.flightRoot())return;
        SAVED.put(player.getUUID(),new Saved(player.yBodyRot,player.yBodyRotO,player.walkAnimation.speed()));
        var frame=InterpolatedFlyingSwordTransform.get(sword,event.getPartialTick());
        player.yBodyRot=frame.yaw();player.yBodyRotO=frame.yaw();
        player.walkAnimation.setSpeed(0);
        float partial=event.getPartialTick();
        float yaw=frame.yaw();
        var delta=frame.position().subtract(Mth.lerp(partial,player.xOld,player.getX()),Mth.lerp(partial,player.yOld,player.getY()),Mth.lerp(partial,player.zOld,player.getZ()));
        var pose=event.getPoseStack();pose.pushPose();
        pose.translate(delta.x,delta.y,delta.z);
        pose.mulPose(frame.rotation());
        pose.translate(0,sword.riderOffset(),0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
    }
    @SubscribeEvent
    public static void after(net.minecraftforge.client.event.RenderPlayerEvent.Post event){
        var saved=SAVED.remove(event.getEntity().getUUID());
        if(saved==null)return;
        var player=event.getEntity();player.yBodyRot=saved.body;player.yBodyRotO=saved.oldBody;
        player.walkAnimation.setSpeed(saved.walk);event.getPoseStack().popPose();
    }

    public static float acceleration(ControlledSwordEntity sword,float partial) {
        float w=Mth.clamp(sword.sprintBlend(Mth.clamp(partial,0,1)),0,1);
        return w*w*(3-2*w);
    }

    public static void animate(PlayerModel<?> model,ControlledSwordEntity sword,float partial){
        float sprint=acceleration(sword,partial);
        float lean=32*Mth.DEG_TO_RAD*sprint;
        float hipY=12+1.4F*sprint,hipZ=-1.4F*sprint;
        float topY=hipY-12*Mth.cos(lean),topZ=hipZ-12*Mth.sin(lean);
        model.riding=false;model.crouching=false;
        // Rotate the torso about the hips, translating the neck and shoulders with it.
        model.body.setPos(0,topY,topZ);model.body.xRot=lean;model.body.yRot=0;model.body.zRot=0;
        model.head.setPos(0,topY,topZ);model.head.xRot=lean*.55F;model.head.yRot=0;model.head.zRot=0;
        model.rightArm.setPos(-5,topY+2*Mth.cos(lean),topZ+2*Mth.sin(lean));
        model.leftArm.setPos(5,topY+2*Mth.cos(lean),topZ+2*Mth.sin(lean));
        model.rightArm.xRot=model.leftArm.xRot=lean+42*Mth.DEG_TO_RAD*sprint;
        model.rightArm.yRot=model.leftArm.yRot=0;
        model.rightArm.zRot=-0.04F;model.leftArm.zRot=0.04F;
        model.rightLeg.setPos(-1.4F,hipY,hipZ);model.leftLeg.setPos(1.4F,hipY,hipZ);
        model.rightLeg.xRot=model.leftLeg.xRot=0;
        model.rightLeg.yRot=model.leftLeg.yRot=0;model.rightLeg.zRot=model.leftLeg.zRot=0;
        model.hat.copyFrom(model.head);model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);
    }
}