package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.fire.FireCatalog;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.util.Map;
import java.util.WeakHashMap;

/** World geometry, with an independent post-composite buffer for shader packs. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class AlchemyFurnaceEffects {
    public static int externalVolumeHookVersion(){return 1;}
    private static final BufferBuilder BUFFER=new BufferBuilder(262144);
    private static final Map<AlchemyFurnaceBlockEntity,Long> VISIBLE=new WeakHashMap<>();
    private static final Map<OfferingItemEntity,Long> OFFERINGS=new WeakHashMap<>();
    private static final Vec3 GOLD=new Vec3(1,.78,.31),WHITE_GOLD=new Vec3(1,.97,.78);
    private static BufferBuilder.RenderedBuffer pending;
    private static Matrix4f projection;
    public static int warningEyeQuads;
    public static float warningShakeDegrees;
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void warningCamera(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event){
        var client=Minecraft.getInstance();warningShakeDegrees=0;
        if(client.level==null)return;
        float yaw=0,pitch=0,roll=0;
        for(var f:cn.blockforge.generated.generatedmod.client.furnacevolume.HostAdapter.warningFurnaces()) {
            if(f.isRemoved()||f.getLevel()!=client.level||!f.explosionPending()||!AlchemyFurnaceBlock.isFormed(f.getBlockState()))continue;
            double distance=event.getCamera().getPosition().distanceTo(Vec3.atCenterOf(f.getBlockPos()));
            if(distance>=12)continue;
            double t=(60-Math.max(0,f.explosionWarningTicks()-event.getPartialTick()))/20D;
            double weight=Math.pow(1-distance/12,2)*(.2+.8*t/3),seed=(f.visualSeed()&1023)*.01;
            yaw+=(float)(Math.sin(t*37+seed)*weight*1.3);
            pitch+=(float)(Math.sin(t*43+seed+1)*weight);
            roll+=(float)(Math.sin(t*31+seed+2)*weight*.45);
        }
        yaw=net.minecraft.util.Mth.clamp(yaw,-1.5F,1.5F);pitch=net.minecraft.util.Mth.clamp(pitch,-1.2F,1.2F);roll=net.minecraft.util.Mth.clamp(roll,-.6F,.6F);
        warningShakeDegrees=Math.max(Math.abs(yaw),Math.abs(pitch));
        event.setYaw(event.getYaw()+yaw);event.setPitch(event.getPitch()+pitch);event.setRoll(event.getRoll()+roll);
    }
    public static void observe(AlchemyFurnaceBlockEntity furnace){
        if(furnace.busy()||furnace.hasResult())VISIBLE.put(furnace,furnace.getLevel().getGameTime());
    }
    public static void observe(OfferingItemEntity offering){OFFERINGS.put(offering,offering.level().getGameTime());}
    @SubscribeEvent public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent event){
        if(event.phase==net.minecraftforge.event.TickEvent.Phase.END){submit();return;}
        warningEyeQuads=0;
        if(pending!=null){pending.release();pending=null;}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void post(net.minecraftforge.client.event.RenderGuiEvent.Pre event){
        if(pending!=null){event.getGuiGraphics().flush();submit();}
    }
    private static void submit(){if(pending!=null){var mesh=pending;pending=null;TrailShaderBridge.draw(mesh,projection);}}
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){
        if(event.getLevel().isClientSide()){VISIBLE.clear();OFFERINGS.clear();if(pending!=null){pending.release();pending=null;}}
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var client=Minecraft.getInstance();if(client.level==null)return;
        long tick=client.level.getGameTime();
        VISIBLE.entrySet().removeIf(e->e.getKey().isRemoved()||e.getKey().getLevel()!=client.level||tick-e.getValue()>2);
        OFFERINGS.entrySet().removeIf(e->e.getKey().isRemoved()||e.getKey().level()!=client.level||tick-e.getValue()>2);
        if(VISIBLE.isEmpty()&&OFFERINGS.isEmpty())return;
        Vec3 eye=event.getCamera().getPosition();
        Vec3 right=new Vec3(new Vector3f(1,0,0).rotate(event.getCamera().rotation()));
        Vec3 up=new Vec3(new Vector3f(0,1,0).rotate(event.getCamera().rotation()));
        var pose=event.getPoseStack();pose.pushPose();pose.translate(-eye.x,-eye.y,-eye.z);
        projection=new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
        BUFFER.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);
        try{
            int drawn=0;
            for(var furnace:VISIBLE.keySet()){
                if(drawn>=16||!AlchemyFurnaceBlock.isFormed(furnace.getBlockState())
                        ||furnace.getBlockPos().distToCenterSqr(eye)>48*48)continue;
                drawn++;
                drawFurnace(furnace,pose.last().pose(),tick+event.getPartialTick(),event.getPartialTick(),eye,right,up);
            }
            int offerings=0;
            for(var offering:OFFERINGS.keySet()) {
                if(offerings>=32||offering.distanceToSqr(eye)>48*48)continue;
                offerings++;
                drawOffering(offering,pose.last().pose(),event.getPartialTick(),eye,right,up);
            }
            var mesh=BUFFER.end();
            if(TrailShaderBridge.shaderPackDetected()){
                if(pending!=null)pending.release();pending=mesh;TrailShaderBridge.captureDepth();
            }else drawNormal(mesh);
        }finally{pose.popPose();if(BUFFER.building())BUFFER.end().release();}
    }
    private static Vec3 point(AlchemyFurnaceBlockEntity furnace,Vec3 local){
        return AlchemyFurnaceVisualLayout.world(furnace.getBlockPos(),AlchemyFurnaceStructure.inwardOf(furnace.getBlockState()),local);
    }
    private static Vec3 rgb(int color){return new Vec3(ExoticFireColorRegistry.vector(color));}
    private static java.util.List<Vec3[]> WARNING_EYES;
    private static java.util.List<Vec3[]> warningEyes(){
        if(WARNING_EYES!=null)return WARNING_EYES;
        var quads=new java.util.ArrayList<Vec3[]>();
        try(var in=AlchemyFurnaceEffects.class.getResourceAsStream("/assets/wuxingxiantu/vfx/furnace_warning_eyes.json")) {
            var root=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            for(var element:root.getAsJsonArray("quads")) {
                var vertices=element.getAsJsonArray();Vec3[] quad=new Vec3[4];
                for(int i=0;i<4;i++){var v=vertices.get(i).getAsJsonArray();quad[i]=new Vec3(v.get(0).getAsDouble(),v.get(1).getAsDouble(),v.get(2).getAsDouble());}quads.add(quad);
            }
        }catch(Exception failure){com.mojang.logging.LogUtils.getLogger().error("Cannot load furnace dragon eyes",failure);}
        return WARNING_EYES=java.util.List.copyOf(quads);
    }
    private static void drawWarningEyes(AlchemyFurnaceBlockEntity f,Matrix4f matrix,float partial,Vec3 eye,Vec3 right,Vec3 up){
        double seconds=(60-Math.max(0,f.explosionWarningTicks()-partial))/20D;
        float intensity=(float)(.5+.5*(.5+.5*Math.sin((seconds*1.5+seconds*seconds*.7)*Math.PI*2)));
        warningEyeQuads+=warningEyes().size();
        for(var quad:warningEyes())for(int index:new int[]{0,1,2,0,2,3}) {
            Vec3 p=point(f,quad[index]);BUFFER.vertex(matrix,(float)p.x,(float)p.y,(float)p.z).color(intensity,.008F,.012F,1F).endVertex();
        }
        // Surface cores remain model-aligned; soft camera-facing light expands out of each eye.
        double phase=(seconds*(1.5+seconds*.7))%1;
        var eyes=warningEyes();
        for(int first=0;first<eyes.size();first+=6) {
            Vec3 center=Vec3.ZERO;int vertices=0;
            for(int face=first;face<Math.min(first+6,eyes.size());face++)
                for(var vertex:eyes.get(face)){center=center.add(point(f,vertex));vertices++;}
            center=center.scale(1D/vertices);
            Vec3 towardEye=eye.subtract(center).normalize();
            center=center.add(towardEye.scale(.025));
            float radius=(float)(.10+.13*seconds/3+.15*phase);
            disc(matrix,center,right,up,radius,.45F*intensity,new Vec3(1,.015,.025));
            disc(matrix,center.add(towardEye.scale(.003)),right,up,.055F,.9F*intensity,new Vec3(1,.25,.12));
            disc(matrix,center.add(towardEye.scale(.006)),right,up,(float)(.07+.30*phase),
                    (float)((1-phase)*.28)*intensity,new Vec3(1,0,.015));
        }
    }
    private static void drawFurnace(AlchemyFurnaceBlockEntity f,Matrix4f matrix,double time,float partial,Vec3 eye,Vec3 right,Vec3 up){
        if(f.explosionPending()){drawWarningEyes(f,matrix,partial,eye,right,up);return;}
        if(f.outcome()==2)return;
        var fire=FireCatalog.id(f.activeFireId());
        Vec3 outer=fire==null?GOLD:rgb(ExoticFireColorRegistry.color(fire.tier(),(long)time));
        Vec3 accent=fire==null?WHITE_GOLD:rgb(ExoticFireColorRegistry.accentColor(fire.tier(),(long)time));
        float age=f.completionAge(partial),progress=f.brewing()?net.minecraft.util.Mth.clamp(
                (float)((time-f.startTick())/Math.max(1L,f.finishTick()-f.startTick())),0,1):0;
        float power=f.brewing()?Math.min(1,progress*5+.12F)*(progress>.95F?1-(progress-.95F)*12:1)
                :f.hasResult()&&age<AlchemyFurnaceVisualLayout.COLLAPSE_TICKS?1-age/AlchemyFurnaceVisualLayout.COLLAPSE_TICKS:0;
        Vec3 fireCenter=point(f,AlchemyFurnaceVisualLayout.FIRE_CENTER);
        if(power>0&&!cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.claims(f)){
            float strength=progress>.7F?1.15F:1;
            disc(matrix,fireCenter.add(0,.2,0),right,up,.34F*power,.2F,outer);
            for(int strand=0;strand<7;strand++)for(int step=0;step<12;step++){
                double a=step/12.0,b=(step+1)/12.0;
                Vec3 from=flame(fireCenter,time,strand,a,power),to=flame(fireCenter,time,strand,b,power);
                Vec3 side=to.subtract(from).cross(eye.subtract(from)).normalize();
                ribbon(matrix,from,to,side.scale(.10*(1-a)*power),side.scale(.10*(1-b)*power),
                        (float)(.42*(1-a)*strength),strand%2==0?outer:accent);
            }
            disc(matrix,fireCenter.add(0,.12,0),right,up,.11F*power,.8F,accent);
        }
        if(f.brewing()&&!cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.claimsUpper(f))drawBrewing(f,matrix,time,progress,power,eye,right,up,outer,accent);
        if(!f.hasResult())return;
        var pill=PillData.read(f.completedResult());int grade=pill==null?1:pill.grade().value();
        float size=.68F+grade*.14F;
        Vec3 mouth=point(f,AlchemyFurnaceVisualLayout.MOUTH),result=point(f,AlchemyFurnaceVisualLayout.RESULT);
        // Only a real Element tag can add an elemental accent; visual rendering never writes NBT.
        if(pill!=null&&f.completedResult().hasTag()&&f.completedResult().getTag().contains("Element")){
            int tier=switch(pill.element()){case METAL->2;case WOOD->4;case WATER->5;case FIRE->1;case EARTH->3;};
            accent=rgb(ExoticFireColorRegistry.color(tier,(long)time)).lerp(GOLD,.3);
        }
        if(age>=AlchemyFurnaceVisualLayout.COLLAPSE_TICKS&&age<18){
            float phase=(age-8)/10,pulse=(float)Math.sin(phase*Math.PI);
            disc(matrix,mouth.add(0,.08,0),right,up,(.14F+.20F*pulse)*size,.35F*pulse,GOLD);
            disc(matrix,mouth.add(0,.08,0),right,up,.10F*size,.8F*pulse,WHITE_GOLD);
            ring(matrix,mouth.add(0,.06,0),.18+phase*.8,.015,pulse*.6F,GOLD,eye,time*.02);
        }
        if(age>=18&&age<AlchemyFurnaceVisualLayout.COMPLETE_TICKS){
            float phase=(age-18)/AlchemyFurnaceVisualLayout.RISE_TICKS;
            float fade=Math.min(1,phase*6)*Math.min(1,(1-phase)*4);
            double height=(1.65+.22*grade)*Math.min(1,phase*4);
            for(int strand=0;strand<3+grade;strand++){
                Vec3 color=strand%3==0?GOLD:strand%3==1?outer.lerp(WHITE_GOLD,.22):accent;
                for(int step=0;step<28;step++){
                    double a=step/28.0,b=(step+1)/28.0;
                    Vec3 from=spiral(mouth,time,strand,a,height,size),to=spiral(mouth,time,strand,b,height,size);
                    Vec3 side=to.subtract(from).cross(eye.subtract(from)).normalize();
                    double width=(.055+.045*Math.sin(a*Math.PI))*size;
                    ribbon(matrix,from,to,side.scale(width),side.scale(width),fade*(float)(.38*(1-a)),color);
                }
            }
            disc(matrix,mouth.add(0,.12,0),right,up,.20F*size,.5F*fade,WHITE_GOLD);
            // Sparse geometric rune strokes rise with the ribbons, independent of ParticleEngine.
            for(int rune=0;rune<4+grade;rune++){
                double p=(phase+rune/(double)(4+grade))%1,angle=rune*2.4+time*.016;
                Vec3 at=mouth.add(Math.cos(angle)*(.35+p*.18),p*height,Math.sin(angle)*(.35+p*.18));
                float alpha=fade*(float)(.5*(1-p));double r=.035*size;
                Vec3 a=at.add(up.scale(r)),b=at.add(right.scale(r)),c=at.subtract(up.scale(r)),d=at.subtract(right.scale(r));
                stroke(matrix,a,b,eye,.009,alpha,accent);stroke(matrix,b,c,eye,.009,alpha,accent);
                stroke(matrix,c,d,eye,.009,alpha,accent);stroke(matrix,d,a,eye,.009,alpha,accent);
                stroke(matrix,at.subtract(right.scale(r*1.6)),at.add(right.scale(r*1.6)),eye,.007,alpha,GOLD);
            }
        }
        if(age>=AlchemyFurnaceVisualLayout.REVEAL_TICK)
            disc(matrix,result.add(0,.055*Math.sin(time*.06),0),right,up,.30F*size,.055F+.008F*grade,GOLD);
    }
    private static void drawOffering(OfferingItemEntity entity,Matrix4f matrix,float partial,Vec3 eye,Vec3 right,Vec3 up){
        float p=entity.progress(partial),impact=entity.impactAge(partial);
        Vec3 outer=rgb(ExoticFireColorRegistry.color(entity.fireTier(),entity.level().getGameTime()));
        Vec3 core=rgb(ExoticFireColorRegistry.accentColor(entity.fireTier(),entity.level().getGameTime())).lerp(WHITE_GOLD,.4);
        if(impact<0){
            Vec3 at=entity.path(p);
            for(int i=0;i<18;i++){
                float a=Math.max(0,p-i*.012F),b=Math.max(0,p-(i+1)*.012F),fade=1-i/18F;
                Vec3 from=entity.path(a),to=entity.path(b);
                stroke(matrix,from,to,eye,.035*fade,.5F*fade,outer);
                stroke(matrix,from,to,eye,.010*fade,.85F*fade,core);
                if(i%4==0){
                    double angle=i*2.4+entity.age(partial)*.15;
                    Vec3 mote=from.add(Math.cos(angle)*.07,.025*Math.sin(angle),Math.sin(angle)*.07);
                    shard(matrix,mote,right,up,.014,.65F*fade,core);
                }
            }
            disc(matrix,at,right,up,.13F,.25F,outer);
        }else if(impact<12){
            float t=impact/12,fade=(1-t)*(1-t);
            Vec3 center=entity.destination();
            ring(matrix,center,.08+t*.65,.025*(1-t),fade*.8F,outer,eye,0);
            disc(matrix,center,right,up,.18F*(1-t),fade*.8F,core);
            for(int i=0;i<16;i++){
                double a=i*2.39996,speed=.35+(i%4)*.12;
                Vec3 direction=new Vec3(Math.cos(a)*speed,.25+(i%5)*.11,Math.sin(a)*speed);
                Vec3 at=center.add(direction.scale(t)).add(0,-t*t*.28,0);
                stroke(matrix,at,at.subtract(direction.scale(.07*(1-t))),eye,.012,fade,core);
                shard(matrix,at,right,up,.012*(1-t),fade,outer);
            }
        }
    }
    private static void drawBrewing(AlchemyFurnaceBlockEntity f,Matrix4f matrix,double time,float progress,float power,
            Vec3 eye,Vec3 right,Vec3 up,Vec3 outer,Vec3 accent){
        Vec3 center=point(f,AlchemyFurnaceVisualLayout.MATERIAL_CENTER),mouth=point(f,AlchemyFurnaceVisualLayout.MOUTH);
        float gather=net.minecraft.util.Mth.clamp((progress-.65F)/.35F,0,1);
        double radius=.78*(1-gather)+.10;
        ring(matrix,center.add(0,-.14,0),radius,.015,power*.42F,outer,eye,time*.025);
        ring(matrix,center.add(0,.12,0),radius*.8,.009,power*.3F,accent,eye,-time*.018);
        // Herb essences circulate, then tighten into the pill core; all points are furnace-local.
        for(int strand=0;strand<5;strand++){
            for(int step=0;step<16;step++){
                double a=time*.027+strand*Math.PI*2/5-step*.065,b=a-.065;
                Vec3 from=center.add(Math.cos(a)*radius,.07*Math.sin(a*3+strand),Math.sin(a)*radius);
                Vec3 to=center.add(Math.cos(b)*radius,.07*Math.sin(b*3+strand),Math.sin(b)*radius);
                stroke(matrix,from,to,eye,.012+.01*gather,power*(1-step/16F)*.65F,strand%2==0?outer:accent);
            }
            double cycle=(time*.013+strand*.2)%1,angle=strand*2.39996-time*.035;
            Vec3 mote=center.add(Math.cos(angle)*radius*(1-cycle),cycle*.4,Math.sin(angle)*radius*(1-cycle));
            shard(matrix,mote,right,up,.018,power*(float)Math.sin(cycle*Math.PI),accent);
        }
        disc(matrix,center,right,up,.10F+.12F*gather,power*(.15F+.55F*gather),WHITE_GOLD);
        // Thin vapor filaments remain visible above the opaque furnace body during brewing.
        for(int strand=0;strand<3;strand++)for(int step=0;step<12;step++){
            double a=step/12.0,b=(step+1)/12.0,height=.65+progress*.45;
            Vec3 from=spiral(mouth,time*.65,strand,a,height,.48F),to=spiral(mouth,time*.65,strand,b,height,.48F);
            stroke(matrix,from,to,eye,.013*(1-a),power*(float)(.24*(1-a)),strand==0?accent:outer);
        }
        ring(matrix,mouth.add(0,.04,0),.27+.06*Math.sin(time*.05),.009,power*.25F,accent,eye,time*.02);
    }
    private static void ring(Matrix4f matrix,Vec3 center,double radius,double width,float alpha,Vec3 color,Vec3 eye,double rotation){
        for(int i=0;i<40;i++){
            if(i%10==9)continue;
            double a=rotation+i*Math.PI/20,b=rotation+(i+1)*Math.PI/20;
            stroke(matrix,center.add(Math.cos(a)*radius,0,Math.sin(a)*radius),
                    center.add(Math.cos(b)*radius,0,Math.sin(b)*radius),eye,width,alpha,color);
        }
    }
    private static void shard(Matrix4f matrix,Vec3 at,Vec3 right,Vec3 up,double size,float alpha,Vec3 color){
        ribbon(matrix,at.subtract(up.scale(size)),at.add(up.scale(size)),right.scale(size),right.scale(size),alpha,color);
    }
    private static Vec3 flame(Vec3 center,double time,int strand,double p,float power){
        double angle=strand*Math.PI*2/7+time*.04+p*2,radius=.27*(1-p)*power;
        return center.add(Math.cos(angle)*radius,AlchemyFurnaceVisualLayout.FIRE_HEIGHT*p*power*(.9+.1*Math.sin(time*.15+strand)),Math.sin(angle)*radius);
    }
    private static Vec3 spiral(Vec3 mouth,double time,int strand,double p,double height,float size){
        double angle=strand*2.39996+p*4.2-time*.045,radius=(.06+.30*Math.sin(p*Math.PI*.8))*size;
        return mouth.add(Math.cos(angle)*radius,p*height,Math.sin(angle)*radius);
    }
    private static void drawNormal(BufferBuilder.RenderedBuffer mesh){
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var shader=RenderSystem.getShader();float[] color=RenderSystem.getShaderColor().clone();
        try{
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.enableDepthTest();RenderSystem.depthMask(false);
            RenderSystem.disableCull();RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(mesh);
        }finally{
            RenderSystem.setShader(()->shader);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);RenderSystem.depthMask(mask);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(src,dst,srcA,dstA);if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
        }
    }
    private static void vertex(Matrix4f m,Vec3 p,float a,Vec3 c){BUFFER.vertex(m,(float)p.x,(float)p.y,(float)p.z).color((float)c.x,(float)c.y,(float)c.z,a).endVertex();}
    private static void disc(Matrix4f m,Vec3 p,Vec3 right,Vec3 up,float r,float alpha,Vec3 color){
        for(int i=0;i<20;i++){double a=i*Math.PI/10,b=(i+1)*Math.PI/10;
            vertex(m,p,alpha,color);vertex(m,p.add(right.scale(Math.cos(a)*r)).add(up.scale(Math.sin(a)*r)),0,color);
            vertex(m,p.add(right.scale(Math.cos(b)*r)).add(up.scale(Math.sin(b)*r)),0,color);}
    }
    private static void stroke(Matrix4f m,Vec3 a,Vec3 b,Vec3 eye,double width,float alpha,Vec3 color){
        Vec3 side=b.subtract(a).cross(eye.subtract(a)).normalize().scale(width);ribbon(m,a,b,side,side,alpha,color);
    }
    private static void ribbon(Matrix4f m,Vec3 a,Vec3 b,Vec3 aw,Vec3 bw,float alpha,Vec3 color){
        vertex(m,a.add(aw),alpha,color);vertex(m,a.subtract(aw),alpha,color);vertex(m,b.add(bw),alpha*.85F,color);
        vertex(m,b.add(bw),alpha*.85F,color);vertex(m,a.subtract(aw),alpha,color);vertex(m,b.subtract(bw),alpha*.85F,color);
    }
    private AlchemyFurnaceEffects(){}
}
