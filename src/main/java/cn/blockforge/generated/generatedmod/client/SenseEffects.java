package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.divinesurvey.client.SurveyClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/** Private perception visuals follow authoritative receipts, never the request button. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SenseEffects {
    private record Burst(String kind,double start,int duration,Vec3 at,float radius,int target){}
    private record Draw(BufferBuilder.RenderedBuffer mesh,Matrix4f matrix,float mode,float time,float progress){}
    private static final List<Burst> BURSTS=new ArrayList<>();
    private static final List<Draw> PENDING=new ArrayList<>();
    private static final BufferBuilder BUFFER=new BufferBuilder(524288);
    private static final Vec3 CYAN=new Vec3(.25,.78,1),GOLD=new Vec3(1,.72,.28),VIOLET=new Vec3(.48,.65,1);
    private static Object world;private static long serial;private static boolean hasState;private static double receivedAt;
    private static Vec3 camera,right,up;private static Matrix4f matrix;
    public static int preparedDraws;
    public static void clear(){BURSTS.clear();release();serial=0;hasState=false;world=null;}
    private static void release(){for(var draw:PENDING)draw.mesh.release();PENDING.clear();}
    public static void accept(CompoundTag state){
        var mc=Minecraft.getInstance();if(mc.level==null)return;if(world!=mc.level){clear();world=mc.level;}
        hasState=true;receivedAt=mc.level.getGameTime();var fx=state.getCompound("SenseVisual");long next=fx.getLong("Serial");if(next<=0||next==serial)return;serial=next;
        String kind=fx.getString("Kind");if(kind.equals("unlock"))return;
        int duration=Mth.clamp(fx.getInt("Duration"),1,100);double age=mc.level.getGameTime()-fx.getLong("Start");if(age>duration||age< -20)return;
        Vec3 at=new Vec3(fx.getDouble("X"),fx.getDouble("Y"),fx.getDouble("Z"));if(!Double.isFinite(at.lengthSqr()))return;
        if(!kind.equals("opening")&&!kind.equals("identify"))BURSTS.add(new Burst(kind,mc.level.getGameTime()-Math.max(0,age),duration,at,Mth.clamp(fx.getFloat("Radius"),.1f,64),fx.getInt("Target")));
    }
    public static void survey(boolean enabled){if(!enabled){BURSTS.removeIf(b->b.kind.equals("survey"));return;}var mc=Minecraft.getInstance();if(mc.level!=null&&mc.player!=null){if(world!=mc.level){clear();world=mc.level;}BURSTS.add(new Burst("survey",mc.level.getGameTime(),52,mc.player.position().add(0,.1,0),64,-1));}}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var level=Minecraft.getInstance().level;if(world!=null&&world!=level){clear();world=level;}if(level!=null)BURSTS.removeIf(b->level.getGameTime()-b.start>b.duration);
    }
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide())clear();}
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event){if(event.phase==TickEvent.Phase.START){release();preparedDraws=0;}else submit();}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void gui(RenderGuiEvent.Pre event){if(!PENDING.isEmpty()){event.getGuiGraphics().flush();submit();}}
    private static void submit(){for(var draw:PENDING)SenseFieldGpu.draw(draw.mesh,draw.matrix,draw.mode,draw.time,draw.progress);PENDING.clear();}
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||TrailShaderBridge.shadowPass()||!SenseFieldGpu.ready())return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        camera=event.getCamera().getPosition();right=new Vec3(new Vector3f(1,0,0).rotate(event.getCamera().rotation()));up=new Vec3(new Vector3f(0,1,0).rotate(event.getCamera().rotation()));
        matrix=new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix()).mul(event.getPoseStack().last().pose());double now=mc.level.getGameTime()+event.getPartialTick();float time=(float)(now%24000/20);
        for(var b:BURSTS){float age=(float)(now-b.start),p=Mth.clamp(age/b.duration,0,1),fade=(float)Math.sin(Math.PI*p);if(age<0||age>b.duration)continue;
            if(b.kind.equals("scan")){float growth=Mth.clamp(age/34,0,1);sphere(b.at,Math.max(.15f,b.radius*growth),fade*.52f,CYAN,time);disc(b.at.add(0,-.8,0),b.radius*growth,fade,CYAN,1,time,p);}
            else if(b.kind.equals("survey")){disc(b.at,b.radius*p,fade*.85f,VIOLET,1,time,p);}
            else if(b.kind.equals("opened")){sphere(b.at,.35f+2*p,fade*.65f,CYAN,time);disc(b.at.add(0,-.8,0),.8f+2*p,fade,GOLD,3,time,p);}
            else if(b.kind.equals("interrupt")){disc(b.at.add(0,.08,0),.65f+p,fade*.7f,new Vec3(1,.36,.22),3,time,p);}
            else {Vec3 at=target(b.target,b.at);if(at!=null)billboard(at,.85f-p*.2f,fade,b.kind.equals("lock")?GOLD:CYAN,2,time,p);}
        }
        var state=PlayerProgressionClient.state;
        if(hasState&&state.getInt("Channel")>0){int kind=state.getInt("Channel"),total=kind==1?100:40;float remaining=(float)(state.getInt("Remaining")-(now-receivedAt)),p=1-Mth.clamp(remaining/total,0,1);
            if(remaining>0){Vec3 at=mc.player.getPosition(event.getPartialTick());float fade=Mth.clamp((1-p)*12,0,1);
                disc(at.add(0,.09,0),1.2f,fade*.8f,CYAN,3,time,p);disc(at.add(0,.14,0),.85f,fade*.6f,GOLD,1,-time,p);
                for(int i=0;i<6;i++){double a=time*1.5+i*Math.PI/3;Vec3 mote=at.add(Math.cos(a)*.64,.65+((time*.24+i/6d)%1)*.9,Math.sin(a)*.64);billboardTall(mote,.16f,.5f,fade*.85f,CYAN,5,time+i,p);}
                if(kind==2){var fx=state.getCompound("SenseVisual");Vec3 focus=target(fx.getInt("Target"),new Vec3(fx.getDouble("X"),fx.getDouble("Y"),fx.getDouble("Z")));if(focus!=null)billboard(focus,1.15f-p*.35f,fade,CYAN,2,time,p);}
            }
        }
        if(hasState&&!state.getString("Lock").isEmpty()){Vec3 at=target(state.getInt("LockEntity"),null);if(at!=null){var entity=mc.level.getEntity(state.getInt("LockEntity"));float size=Math.max(.65f,entity.getBbHeight()*.62f);billboard(at,size,.85f,GOLD,2,time,1);disc(entity.getPosition(event.getPartialTick()).add(0,.05,0),Math.max(.45f,entity.getBbWidth()),.65f,GOLD,1,time,1);}}
        if(hasState)for(int id:state.getIntArray("ScanTargets")){Vec3 at=target(id,null);if(at!=null)billboard(at,.35f,.45f,CYAN,2,time,1);}
        if(SurveyClient.active())disc(mc.player.getPosition(event.getPartialTick()).add(0,.07,0),1.8f,.75f,VIOLET,4,time,SurveyClient.completed()/64f);
        preparedDraws=PENDING.size();if(!PENDING.isEmpty()&&!TrailShaderBridge.captureDepth())release();
    }
    private static Vec3 target(int id,Vec3 fallback){
        var mc=Minecraft.getInstance();var entity=id<0?null:mc.level.getEntity(id);
        if(entity==null)return id<0?fallback:null;
        if(!(entity instanceof LivingEntity living)||!living.isAlive()||!mc.player.hasLineOfSight(entity)||mc.player.distanceToSqr(entity)>64*64)return null;
        return entity.getBoundingBox().getCenter().add(camera.subtract(entity.getBoundingBox().getCenter()).normalize().scale(Math.max(.25,entity.getBbWidth()*.55)));
    }
    private static void begin(){BUFFER.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_TEX_COLOR);}
    private static void end(float mode,float time,float progress){PENDING.add(new Draw(BUFFER.end(),new Matrix4f(matrix),mode,time,progress));}
    private static void vertex(Vec3 p,float u,float v,float alpha,Vec3 color){BUFFER.vertex(p.x-camera.x,p.y-camera.y,p.z-camera.z).uv(u,v).color((float)color.x,(float)color.y,(float)color.z,alpha).endVertex();}
    private static void quad(Vec3 a,Vec3 b,Vec3 c,Vec3 d,float alpha,Vec3 color){vertex(a,0,0,alpha,color);vertex(b,1,0,alpha,color);vertex(c,1,1,alpha,color);vertex(a,0,0,alpha,color);vertex(c,1,1,alpha,color);vertex(d,0,1,alpha,color);}
    private static void billboard(Vec3 at,float radius,float alpha,Vec3 color,float mode,float time,float progress){begin();Vec3 r=right.scale(radius),u=up.scale(radius);quad(at.subtract(r).subtract(u),at.add(r).subtract(u),at.add(r).add(u),at.subtract(r).add(u),alpha,color);end(mode,time,progress);}
    private static void billboardTall(Vec3 at,float halfWidth,float halfHeight,float alpha,Vec3 color,float mode,float time,float progress){begin();Vec3 r=right.scale(halfWidth),u=up.scale(halfHeight);quad(at.subtract(r).subtract(u),at.add(r).subtract(u),at.add(r).add(u),at.subtract(r).add(u),alpha,color);end(mode,time,progress);}
    private static void disc(Vec3 at,float radius,float alpha,Vec3 color,float mode,float time,float progress){if(radius<.02)return;begin();quad(at.add(-radius,0,-radius),at.add(radius,0,-radius),at.add(radius,0,radius),at.add(-radius,0,radius),alpha,color);end(mode,time,progress);}
    private static Vec3 point(Vec3 center,float radius,int y,int x){double lat=Math.PI*y/32,lon=Math.PI*2*x/96;return center.add(Math.sin(lat)*Math.cos(lon)*radius,Math.cos(lat)*radius,Math.sin(lat)*Math.sin(lon)*radius);}
    private static void sphere(Vec3 at,float radius,float alpha,Vec3 color,float time){begin();for(int y=0;y<32;y++)for(int x=0;x<96;x++){
        Vec3 a=point(at,radius,y,x),b=point(at,radius,y+1,x),c=point(at,radius,y+1,x+1),d=point(at,radius,y,x+1);float u=x/96f,v=y/32f,un=(x+1)/96f,vn=(y+1)/32f;
        vertex(a,u,v,alpha,color);vertex(b,u,vn,alpha,color);vertex(c,un,vn,alpha,color);vertex(a,u,v,alpha,color);vertex(c,un,vn,alpha,color);vertex(d,un,v,alpha,color);
    }end(0,time,0);}
    private SenseEffects(){}
}
