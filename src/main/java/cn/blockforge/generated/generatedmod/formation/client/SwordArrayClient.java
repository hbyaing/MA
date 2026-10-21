package cn.blockforge.generated.generatedmod.formation.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.formation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import java.util.function.DoubleFunction;

/** 迁入来源四剑落位、阵纹、穹顶、剑束与剑幕。护罩仅为源视觉，不截挡伤害或实体。 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SwordArrayClient {
    private static final Map<UUID,FormationState> ACTIVE=new HashMap<>();
    public static void accept(SwordArrayStatePacket packet){
        var state=packet.cast();if(!state.valid())return;
        var current=ACTIVE.get(state.owner());
        if(packet.stop()){
            if(current==null||current.start()<=state.start())ACTIVE.remove(state.owner());
        }else if(current==null||current.start()<=state.start())ACTIVE.put(state.owner(),state);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){ACTIVE.clear();}
    private static void item(PoseStack pose,MultiBufferSource.BufferSource buffer,ItemStack item,double x,double y,double z){
        pose.pushPose();pose.translate(x,y,z);pose.mulPose(Axis.ZP.rotationDegrees(180));pose.scale(3.4F,3.4F,3.4F);
        var minecraft=Minecraft.getInstance();minecraft.getItemRenderer().renderStatic(item,ItemDisplayContext.FIXED,
                15728880,OverlayTexture.NO_OVERLAY,pose,buffer,minecraft.level,0);pose.popPose();
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var minecraft=Minecraft.getInstance();if(minecraft.level==null)return;
        var eye=event.getCamera().getPosition();var pose=event.getPoseStack();
        try(PassState ignored=new PassState()){
            for(var cast:ACTIVE.values()){
                if(!cast.visible(minecraft.level.dimension().location(),eye))continue;
                double time=(minecraft.level.getGameTime()-cast.start()+event.getPartialTick())/20.0D;if(time<0)continue;
                pose.pushPose();
                try{
                    double dx=cast.origin().x-eye.x,dy=cast.origin().y-eye.y+.04,dz=cast.origin().z-eye.z;
                    pose.translate(dx,dy,dz);pose.scale((float)SwordArrayShield.SCALE,(float)SwordArrayShield.SCALE,(float)SwordArrayShield.SCALE);
                    SwordArrayShield.ground(pose,dx,dy,dz,time);
                    var buffer=minecraft.renderBuffers().bufferSource();
                    var swords=List.of(FormationBootstrap.ZHUXIAN_SWORD,FormationBootstrap.XIANXIAN_SWORD,
                            FormationBootstrap.LUXIAN_SWORD,FormationBootstrap.JUEXIAN_SWORD);
                    if(time>=2)for(int i=0;i<4;i++){
                        double angle=-i*Math.PI/2,fall=12*Math.pow(Math.max(0,1-(time-2)/2),2);
                        item(pose,buffer,new ItemStack(swords.get(i).get()),Math.cos(angle)*8.06,1.7+fall,Math.sin(angle)*8.06);
                    }
                    buffer.endBatch();
                    List<SwordArrayGeometry.Blade> blades=new ArrayList<>();
                    if(time>6&&time<12){
                        double fade=FormationMath.ease((12-time)/1.2),head=FormationMath.ease((time-6)/3);
                        for(int arm=0;arm<4;arm++)for(int lane=0;lane<5;lane++)for(int k=0;k<36;k++){
                            double u=((time*.72-k/36.0-lane*.007)%1+1)%1;if(u>head)continue;
                            final int a=arm,l=lane;DoubleFunction<FormationMath.V> path=value->arrayStream(a,value,time,l);
                            blades.add(new SwordArrayGeometry.Blade(path.apply(u),SwordArrayGeometry.dir(path,u),(lane==0?.44:.38)*fade));
                        }
                    }
                    if(time>9){
                        int field=384;double formed=FormationMath.ease((time-9)/3);
                        for(int i=0;i<field;i++){
                            final int index=i;DoubleFunction<FormationMath.V> path=t->{var vertex=FormationMath.field(index,field,t+10,cast.seed());
                                return new FormationMath.V(vertex.x()*.418,vertex.y()*.45+1.6,vertex.z()*.418);};
                            var position=path.apply(time);var direction=SwordArrayGeometry.dir(path,time);
                            var side=direction.cross(new FormationMath.V(0,1,0)).unit();
                            for(int k=0;k<5;k++){
                                int row=k==0?0:k<3?1:2;double lateral=k==0?0:(k%2==1?-.14:.14)*(row==2?1.6:1);
                                var companion=position.add(side.mul(lateral)).add(direction.mul(-row*.27));
                                blades.add(new SwordArrayGeometry.Blade(companion,direction,(.31+(i/(field/3))*.025)*(k==0?1:.88)*formed));
                            }
                        }
                    }
                    SwordArrayGeometry.draw(pose,blades,new FormationMath.V((eye.x-cast.origin().x)/SwordArrayShield.SCALE,
                            (eye.y-cast.origin().y)/SwordArrayShield.SCALE,(eye.z-cast.origin().z)/SwordArrayShield.SCALE));
                    SwordArrayShield.render(pose,dx,dy,dz,time);buffer.endBatch();
                }finally{pose.popPose();}
            }
        }
    }
    private static FormationMath.V arrayStream(int arm,double u,double time,int lane){
        var vertex=FormationMath.bundle(arm,.32+.68*FormationMath.clamp(u),time,lane);
        var base=FormationMath.stream(arm,.32,time);double angle=-arm*Math.PI/2;
        return new FormationMath.V(Math.cos(angle)*8.06+(vertex.x()-base.x())*.8,
                3.1+(vertex.y()-.12)*.30,Math.sin(angle)*8.06+(vertex.z()-base.z())*.8);
    }
    private SwordArrayClient(){}
}
