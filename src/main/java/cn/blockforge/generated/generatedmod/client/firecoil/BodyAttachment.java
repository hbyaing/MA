package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.TrailShaderBridge;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.util.*;

/** Captures the rendered torso root, including flight, crouch and interpolation. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class BodyAttachment {
 private static final Map<Integer,Matrix4f> POSES=new HashMap<>();
 static void beginFrame(){POSES.clear();}
 static Matrix4f snapshot(int id){var value=POSES.get(id);return value==null?null:new Matrix4f(value);}
 @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event){
  for(String skin:event.getSkins()){
   var candidate=event.getSkin(skin);if(candidate instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer renderer)renderer.addLayer(new Layer(renderer));
  }
 }
 private static final class Layer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>{
  Layer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
  @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer p,float limb,float amount,float partial,float age,float yaw,float pitch){
   if(!Client.bodyEligible(p.getId())||TrailShaderBridge.shadowPass())return;
   pose.pushPose();try{
    getParentModel().body.translateAndRotate(pose);
    POSES.put(p.getId(),new Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix()).mul(pose.last().pose()).translate(0,1.5f,0).scale(-1,-1,1));
   }finally{pose.popPose();}
  }
 }
 static Matrix4f matrix(RenderLevelStageEvent event,Player p,float partial){
  var captured=POSES.get(p.getId());if(captured!=null)return new Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix()).invert().mul(captured);
  var eye=event.getCamera().getPosition();var pose=new PoseStack();pose.last().pose().set(event.getPoseStack().last().pose());
  pose.translate(net.minecraft.util.Mth.lerp(partial,p.xOld,p.getX())-eye.x,net.minecraft.util.Mth.lerp(partial,p.yOld,p.getY())-eye.y,net.minecraft.util.Mth.lerp(partial,p.zOld,p.getZ())-eye.z);
  RealmFlightVisuals.applyBodyTransform(p,partial,pose);
  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-net.minecraft.util.Mth.rotLerp(partial,p.yBodyRotO,p.yBodyRot)));
  return new Matrix4f(pose.last().pose());
 }
}
