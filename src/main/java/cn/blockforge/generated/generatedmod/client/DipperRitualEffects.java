package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.divineart.DipperPattern;
import cn.blockforge.generated.generatedmod.network.packet.divineart.DipperVisualPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;

/** 天罡显圣: server-confirmed footfalls, client-local 20 Hz motes and atlas geometry. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class DipperRitualEffects {
    private static final ResourceLocation ATLAS=new ResourceLocation(GeneratedMod.MOD_ID,"textures/effect/dipper_ritual_atlas.png");
    private static final int CYAN=0x82DBED,WHITE=0xE9FBFF,GOLD=0xEFD191;
    private static final int MAX_EFFECTS=32,MAX_MOTES=1200,PER_EFFECT_MOTES=160;
    private static final Map<UUID,Ritual> RITUALS=new LinkedHashMap<>();
    private static final MultiBufferSource.BufferSource BUFFERS=MultiBufferSource.immediate(new BufferBuilder(262144));
    private static final BufferBuilder POST=new BufferBuilder(262144);
    private static BufferBuilder.RenderedBuffer pending;
    private static Matrix4f projection;
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static long clock;
    private static Vec3 cameraOrigin=Vec3.ZERO;

    private static final class Mote {
        Vec3 position,previous; final Vec3 velocity; final int life,color; int age;
        Mote(Vec3 p,Vec3 v,int life,int color){position=previous=p;velocity=v;this.life=life;this.color=color;}
        void tick(){previous=position;position=position.add(velocity);age++;}
    }
    private record Pulse(int star,long started) {}
    private static final class Ritual {
        final UUID player; final Vec3 origin; final float yaw; final long session;
        final Vec3[] stars=new Vec3[7],ground=new Vec3[7];
        final ArrayList<Mote> motes=new ArrayList<>(); final ArrayList<Pulse> pulses=new ArrayList<>();
        final Random random; final Vec3 center; final double ringY;
        int step,phase; long start,phaseStart,lastSeen;
        Ritual(DipperVisualPacket p) {
            player=p.player();origin=p.origin();yaw=p.yaw();session=p.startedAt();
            random=new Random(player.getMostSignificantBits()^session);
            start=clock-p.age();phaseStart=start;lastSeen=clock;step=p.step();phase=p.phase();
            for(int i=0;i<7;i++){
                stars[i]=DipperPattern.star(origin,yaw,i);ground[i]=floor(stars[i].add(0,-1.5,0));
            }
            center=DipperPattern.world(origin,yaw,new Vec3(-.2,0,-1.25));
            // A large horizontal ring is omitted on steep/unsupported terrain; local footfall seals remain.
            double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
            for(int i=0;i<32;i++){
                double a=i*Math.PI*2/32;Vec3 f=floor(center.add(Math.cos(a)*17.5,0,Math.sin(a)*17.5));
                if(f==null){min=Double.NaN;break;}min=Math.min(min,f.y);max=Math.max(max,f.y);
            }
            ringY=Double.isFinite(min)&&max-min<.8?max:Double.NaN;
        }
        float strength(double now){
            if(phase==DipperVisualPacket.COMPLETE)return (float)Mth.clamp(1-(now-phaseStart)/30,0,1);
            if(phase==DipperVisualPacket.CANCEL)return (float)Mth.clamp(1-(now-phaseStart)/8,0,1);
            return (float)Mth.clamp((now-start)/16,0,1);
        }
    }
    private static abstract class Types extends RenderType {
        private Types(){super("unused",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,256,false,false,()->{},()->{});}
        static final RenderType RITUAL=create("wuxing_dipper_ritual",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,262144,false,true,
            CompositeState.builder().setShaderState(new ShaderStateShard(TrailShaderBridge::dipperShader))
                .setTextureState(new TextureStateShard(ATLAS,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }
    public static void accept(DipperVisualPacket p) {
        var client=Minecraft.getInstance();
        if(client.level==null||!client.level.dimension().location().equals(p.dimension()))return;
        if(!Double.isFinite(p.origin().x)||!Double.isFinite(p.origin().y)||!Double.isFinite(p.origin().z)
            ||!Float.isFinite(p.yaw())||p.step()<0||p.step()>7||p.phase()<0||p.phase()>2||p.age()<0)return;
        if(world!=client.level){reset();world=client.level;}
        Ritual ritual=RITUALS.get(p.player());
        if(ritual==null||ritual.session!=p.startedAt()){
            if(p.phase()==DipperVisualPacket.CANCEL)return;
            if(RITUALS.size()>=MAX_EFFECTS)RITUALS.remove(RITUALS.keySet().iterator().next());
            ritual=new Ritual(p);RITUALS.put(p.player(),ritual);
            if(p.phase()==DipperVisualPacket.COMPLETE&&p.age()<10)burst(ritual,6,48);
            return;
        }
        ritual.lastSeen=clock;
        // Repeated heartbeat snapshots never replay a footfall burst or restart the completion fade.
        for(int i=ritual.step;i<p.step();i++){
            ritual.pulses.add(new Pulse(i,clock));burst(ritual,i,18);
        }
        ritual.step=Math.max(ritual.step,p.step());
        if(p.phase()>ritual.phase){
            ritual.phase=p.phase();ritual.phaseStart=clock-p.age();
            if(p.phase()==DipperVisualPacket.COMPLETE)burst(ritual,6,48);
        }
    }
    private static void burst(Ritual r,int star,int count){
        for(int i=0;i<count;i++){
            double angle=r.random.nextDouble()*Math.PI*2,speed=.02+r.random.nextDouble()*.055;
            Vec3 p=r.ground[star]==null?r.stars[star].add(0,-1.35,0):r.ground[star].add(0,.06,0);
            add(r,new Mote(p,new Vec3(Math.cos(angle)*speed,.025+r.random.nextDouble()*.065,Math.sin(angle)*speed),14+r.random.nextInt(15),GOLD));
        }
    }
    private static void add(Ritual r,Mote mote){
        if(r.motes.size()>=PER_EFFECT_MOTES)return;
        int total=0;for(Ritual effect:RITUALS.values())total+=effect.motes.size();
        if(total<MAX_MOTES)r.motes.add(mote);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        var client=Minecraft.getInstance();
        if(client.level!=world){reset();world=client.level;}
        if(world==null)return;
        if(client.isPaused())return;
        clock++;
        RITUALS.values().removeIf(r->{
            var player=world.getPlayerByUUID(r.player);
            return clock-r.lastSeen>65||(player!=null&&(!player.isAlive()||player.position().distanceToSqr(r.origin)>1024))
                ||(r.phase!=DipperVisualPacket.ACTIVE&&r.strength(clock)<=0);
        });
        for(Ritual r:RITUALS.values()){
            r.motes.forEach(Mote::tick);r.motes.removeIf(m->m.age>=m.life);
            r.pulses.removeIf(p->clock-p.started>12);
            if(r.phase==DipperVisualPacket.ACTIVE&&clock%2==0){
                for(int k=0;k<4;k++){
                    int index=r.random.nextInt(7);Vec3 p=r.stars[index];
                    double a=r.random.nextDouble()*Math.PI*2;
                    add(r,new Mote(p.add(Math.cos(a)*.28,-.3,Math.sin(a)*.28),new Vec3(-Math.sin(a)*.008,.01,Math.cos(a)*.008),24+r.random.nextInt(12),index<r.step?GOLD:CYAN));
                }
            }
        }
    }
    private static void reset(){RITUALS.clear();clock=0;if(pending!=null){pending.release();pending=null;}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void frame(TickEvent.RenderTickEvent event){
        if(event.phase==TickEvent.Phase.START){if(pending!=null){pending.release();pending=null;}}
        else if(Minecraft.getInstance().options.hideGui)submit();
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hud(RenderGuiEvent.Pre event){
        if(pending!=null){event.getGuiGraphics().flush();submit();}
    }
    private static void submit(){
        if(pending==null)return;var mesh=pending;pending=null;
        if(Minecraft.getInstance().level!=world){mesh.release();return;}
        TrailShaderBridge.draw(mesh,projection,ATLAS);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||RITUALS.isEmpty())return;
        var client=Minecraft.getInstance();if(client.level!=world||world==null)return;
        if(pending!=null){pending.release();pending=null;}
        Vec3 eye=event.getCamera().getPosition();
        cameraOrigin=eye;
        var pose=event.getPoseStack();pose.pushPose();
        projection=new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
        boolean post=TrailShaderBridge.shaderPackDetected();
        VertexConsumer buffer;
        if(post){POST.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);buffer=POST;}
        else buffer=BUFFERS.getBuffer(Types.RITUAL);
        Vector3f right=new Vector3f(1,0,0).rotate(event.getCamera().rotation()),up=new Vector3f(0,1,0).rotate(event.getCamera().rotation());
        Vec3 side=new Vec3(right.x,right.y,right.z),vertical=new Vec3(up.x,up.y,up.z);
        double now=clock+event.getPartialTick();
        try{
            for(Ritual r:RITUALS.values()){
                if(r.origin.distanceToSqr(eye)>4096)continue;
                draw(buffer,pose.last().pose(),r,eye,side,vertical,now,event.getPartialTick());
            }
            if(post){
                pending=POST.endOrDiscardIfEmpty();
                if(pending!=null&&!TrailShaderBridge.captureDepth()){pending.release();pending=null;}
            }else BUFFERS.endBatch(Types.RITUAL);
        }finally{pose.popPose();}
    }
    private static void draw(VertexConsumer b,Matrix4f pose,Ritual r,Vec3 eye,Vec3 side,Vec3 up,double now,float partial){
        float strength=r.strength(now);if(strength<=0)return;
        int frame=((int)((now-r.start)*.65))&15;
        if(Double.isFinite(r.ringY)){
            Vec3 center=new Vec3(r.center.x,r.ringY,r.center.z);
            ring(b,pose,center,17.5,.07,CYAN,.72F*strength,now*.003,Math.PI*2);
            ring(b,pose,center,16.1,.045,GOLD,.56F*strength,-now*.002,Math.PI*2);
            for(int i=0;i<12;i++){
                double a=i*Math.PI*2/12+now*.004;
                Vec3 p=center.add(Math.cos(a)*16.75,.008,Math.sin(a)*16.75);
                flat(b,pose,p,.7,a,runeCell((frame+i)&15),CYAN,.75F*strength);
            }
        }
        for(int i=0;i<7;i++){
            boolean done=i<r.step,current=i==r.step&&r.phase==DipperVisualPacket.ACTIVE;
            int color=done?GOLD:CYAN;
            float alpha=(done?.9F:current?1F:.65F)*strength;
            billboard(b,pose,r.stars[i],side,up,current?.95:.65,nodeCell((frame+i)&15),color,alpha);
            billboard(b,pose,r.stars[i],side,up,.25,32,WHITE,alpha);
            if(r.ground[i]!=null){
                ring(b,pose,r.ground[i],current?.65:.4,current?.06:.035,color,alpha*.9F,now*.02,Math.PI*2);
                if(current){
                    ring(b,pose,r.ground[i].add(0,.007,0),.9,.06,CYAN,alpha*.9F,-now*.045,Math.PI*1.35);
                    flat(b,pose,r.ground[i].add(0,.01,0),.55,now*.012,runeCell(frame),WHITE,alpha*.85F);
                }
            }
            if(i<6){
                boolean linked=i+1<r.step;Vec3 a=r.stars[i],c=r.stars[i+1];
                ribbon(b,pose,a,c,eye,linked?.07:.04,linked?GOLD:CYAN,(linked?.95F:.6F)*strength);
                if(linked){
                    double t=(now*.025+i*.23)%1;Vec3 p=a.lerp(c,t);
                    billboard(b,pose,p,side,up,.15,32,WHITE,.85F*strength);
                }
            }
        }
        for(Pulse pulse:r.pulses){
            Vec3 floor=r.ground[pulse.star];if(floor==null)continue;
            double t=Mth.clamp((now-pulse.started)/12,0,1);
            ring(b,pose,floor.add(0,.02,0),.25+t*.85,.024,GOLD,(float)(1-t)*strength,0,Math.PI*2);
        }
        for(Mote m:r.motes){
            Vec3 p=m.previous.lerp(m.position,partial);
            float fade=(float)Math.sin(Math.PI*Math.min(1,(m.age+partial)/m.life));
            billboard(b,pose,p,side,up,.14,32,m.color,fade*.95F*strength);
        }
        if(r.phase==DipperVisualPacket.COMPLETE){
            double age=now-r.phaseStart,t=Mth.clamp(age/30,0,1);
            Vec3 end=r.stars[6];double height=8*Math.min(1,age/5);
            ribbon(b,pose,end,end.add(0,height,0),eye,.13,CYAN,.16F*strength);
            ribbon(b,pose,end,end.add(0,height,0),eye,.028,WHITE,.82F*strength);
            billboard(b,pose,end,side,up,.65,nodeCell(frame),GOLD,.9F*strength);
            if(r.ground[6]!=null)ring(b,pose,r.ground[6].add(0,.02,0),.4+3.4*t,.035,GOLD,strength*.65F,0,Math.PI*2);
        }
    }
    private static int nodeCell(int frame){return (frame/4)*8+frame%4;}
    private static int runeCell(int frame){return (frame/4)*8+4+frame%4;}
    private static Vec3 floor(Vec3 point){
        if(world==null)return null;
        BlockPos base=BlockPos.containing(point);if(!world.hasChunkAt(base))return null;
        // Pick the closest collision surface within three blocks; never load a chunk for VFX.
        Vec3 best=null;double distance=Double.POSITIVE_INFINITY;
        for(int dy=-3;dy<=2;dy++){
            BlockPos pos=base.offset(0,dy,0);
            var shape=world.getBlockState(pos).getCollisionShape(world,pos);
            if(shape.isEmpty())continue;
            for(var box:shape.toAabbs()){
                double x=point.x-pos.getX(),z=point.z-pos.getZ();
                if(x<box.minX||x>box.maxX||z<box.minZ||z>box.maxZ)continue;
                double y=pos.getY()+box.maxY+.035,d=Math.abs(y-point.y);
                if(d<distance){distance=d;best=new Vec3(point.x,y,point.z);}
            }
        }
        return best;
    }
    private static void ring(VertexConsumer b,Matrix4f pose,Vec3 c,double radius,double width,int color,float alpha,double start,double arc){
        int segments=Math.max(12,(int)(48*arc/(Math.PI*2)));
        for(int i=0;i<segments;i++){
            double a=start+arc*i/segments,d=start+arc*(i+1)/segments;
            quad(b,pose,c.add(Math.cos(a)*(radius-width),0,Math.sin(a)*(radius-width)),
                c.add(Math.cos(a)*(radius+width),0,Math.sin(a)*(radius+width)),
                c.add(Math.cos(d)*(radius+width),0,Math.sin(d)*(radius+width)),
                c.add(Math.cos(d)*(radius-width),0,Math.sin(d)*(radius-width)),33,color,alpha);
        }
    }
    private static void ribbon(VertexConsumer b,Matrix4f pose,Vec3 a,Vec3 c,Vec3 eye,double width,int color,float alpha){
        Vec3 axis=c.subtract(a),normal=axis.cross(eye.subtract(a.lerp(c,.5)));
        if(axis.lengthSqr()<1e-9||normal.lengthSqr()<1e-9)return;
        Vec3 n=normal.normalize().scale(width);
        quad(b,pose,a.subtract(n),a.add(n),c.add(n),c.subtract(n),33,color,alpha);
        if(width<.1){n=n.scale(3);quad(b,pose,a.subtract(n),a.add(n),c.add(n),c.subtract(n),32,color,alpha*.32F);}
    }
    private static void flat(VertexConsumer b,Matrix4f pose,Vec3 p,double size,double angle,int cell,int color,float alpha){
        Vec3 x=new Vec3(Math.cos(angle)*size,0,Math.sin(angle)*size),z=new Vec3(-Math.sin(angle)*size,0,Math.cos(angle)*size);
        quad(b,pose,p.subtract(x).subtract(z),p.add(x).subtract(z),p.add(x).add(z),p.subtract(x).add(z),cell,color,alpha);
    }
    private static void billboard(VertexConsumer b,Matrix4f pose,Vec3 p,Vec3 right,Vec3 up,double size,int cell,int color,float alpha){
        Vec3 x=right.scale(size),y=up.scale(size);
        quad(b,pose,p.subtract(x).add(y),p.add(x).add(y),p.add(x).subtract(y),p.subtract(x).subtract(y),cell,color,alpha);
    }
    private static void quad(VertexConsumer b,Matrix4f pose,Vec3 a,Vec3 c,Vec3 d,Vec3 e,int cell,int color,float alpha){
        float u=(cell%8)/8F,v=(cell/8)/8F,epsilon=.5F/1024;
        vertex(b,pose,a,u+epsilon,v+epsilon,color,alpha);
        vertex(b,pose,c,u+.125F-epsilon,v+epsilon,color,alpha);
        vertex(b,pose,d,u+.125F-epsilon,v+.125F-epsilon,color,alpha);
        vertex(b,pose,e,u+epsilon,v+.125F-epsilon,color,alpha);
    }
    private static void vertex(VertexConsumer b,Matrix4f pose,Vec3 p,float u,float v,int color,float alpha){
        Vec3 relative=p.subtract(cameraOrigin);
        b.vertex(pose,(float)relative.x,(float)relative.y,(float)relative.z).uv(u,v)
            .color((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,Mth.clamp(alpha,0,1)).endVertex();
    }
    private DipperRitualEffects() {}
}
