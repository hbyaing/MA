package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.MeditationSeatEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
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

/** Own geometry buffer; the sword bridge supplies world depth and the post-composite shader. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class MeditationAura {
    private static final BufferBuilder BUFFER=new BufferBuilder(131072);
    private static BufferBuilder.RenderedBuffer pending;
    private static Matrix4f projection;
    private static final Vec3 GOLD=new Vec3(1,.73,.25),AMBER=new Vec3(.78,.38,.12);
    private static final Vec3 AURA_BLUE=new Vec3(.24,.68,1),AURA_HOT=new Vec3(.72,.95,1);
    private static final Vec3 MIXED=new Vec3(.65,.22,.17),DARK_RED=new Vec3(.43,.075,.13);
    public static boolean lastPostSubmitted;

    public static Vec3 trajectory(Vec3 center,double angle,double height,double progress){
        double remaining=1-progress,radius=2.4*remaining*remaining,turn=angle+progress*1.4;
        return center.add(Math.cos(turn)*radius,height*remaining+Math.sin(progress*Math.PI)*.25,Math.sin(turn)*radius);
    }
    public static Vec3 coreColor(float ticks){
        if(ticks<=60)return GOLD;
        if(ticks<=160)return GOLD.lerp(AMBER,smooth((ticks-60)/100));
        if(ticks<=240)return AMBER.lerp(MIXED,smooth((ticks-160)/80));
        return MIXED.lerp(DARK_RED,smooth((ticks-240)/60));
    }
    private static float smooth(float value){float x=Mth.clamp(value,0,1);return x*x*(3-2*x);}

    @SubscribeEvent public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent event){
        if(event.phase==net.minecraftforge.event.TickEvent.Phase.END){submit();return;}
        if(pending!=null){pending.release();pending=null;}
        lastPostSubmitted=false;
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void post(net.minecraftforge.client.event.RenderGuiEvent.Pre event){
        if(pending!=null){event.getGuiGraphics().flush();submit();}
    }
    private static void submit(){
        if(pending==null)return;
        var mesh=pending;pending=null;
        lastPostSubmitted=TrailShaderBridge.draw(mesh,projection);
    }
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){
        if(event.getLevel().isClientSide()&&pending!=null){pending.release();pending=null;}
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var client=Minecraft.getInstance();if(client.level==null)return;
        var camera=event.getCamera();Vec3 eye=camera.getPosition();
        var players=client.level.players().stream().filter(p->p.getVehicle() instanceof MeditationSeatEntity seat
                &&seat.isMeditating()&&!seat.isRemoved()&&p.distanceToSqr(eye)<32*32&&!p.isInvisible()).limit(32).toList();
        if(players.isEmpty())return;
        boolean post=TrailShaderBridge.shaderPackDetected();
        Vec3 right=new Vec3(new Vector3f(1,0,0).rotate(camera.rotation()));
        Vec3 up=new Vec3(new Vector3f(0,1,0).rotate(camera.rotation()));
        var pose=event.getPoseStack();pose.pushPose();pose.translate(-eye.x,-eye.y,-eye.z);
        projection=new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
        BUFFER.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);
        try{
            Matrix4f matrix=pose.last().pose();double time=client.level.getGameTime()+event.getPartialTick();
            for(var player:players){
                var seat=(MeditationSeatEntity)player.getVehicle();
                float entry=seat.entryTicks(event.getPartialTick()),closing=smooth((entry-290)/10);
                Vec3 color=coreColor(entry);
                double yaw=Math.toRadians(Mth.rotLerp(event.getPartialTick(),player.yBodyRotO,player.yBodyRot));
                Vec3 center=seat.position().add(-Math.sin(yaw)*.26,.38,Math.cos(yaw)*.26);
                float arrival=0;
                // Integrating the final acceleration avoids jumping the trails to a new cycle.
                double clockSpeed=time+Math.pow(Math.max(0,entry-290),2)*.12;
                for(int i=0;i<9;i++){
                    double clock=clockSpeed+i*48.0/9+(player.getId()%37),cycle=Math.floor(clock/48);
                    double progress=(clock-cycle*48)/48,angle=i*2.399963+cycle*.91+player.getId();
                    double height=.1+.9*Math.sin(i*1.7+cycle*.6);
                    float fade=(float)Math.min(1,Math.min(progress*8,(1-progress)*12));
                    arrival=Math.max(arrival,(float)Math.exp(-Math.pow((progress-.94)/.05,2)));
                    Vec3 head=trajectory(center,angle,height,progress);
                    for(int segment=0;segment<12;segment++){
                        double a=Math.max(0,progress-segment*.014),b=Math.max(0,a-.014);
                        Vec3 start=trajectory(center,angle,height,a),end=trajectory(center,angle,height,b);
                        Vec3 side=end.subtract(start).cross(eye.subtract(start)).normalize();
                        float width=.026F*(1-segment/12F),alpha=fade*.64F*(1-segment/12F);
                        ribbon(BUFFER,matrix,start,end,side.scale(width),side.scale(width*.8),alpha,AURA_BLUE);
                    }
                    disc(BUFFER,matrix,head,right,up,.11F,fade*.38F,AURA_BLUE);
                    disc(BUFFER,matrix,head,right,up,.032F,fade*.98F,AURA_HOT);
                }
                float breathe=(float)Math.sin(time*.09+player.getId())*.06F;
                float gainAge=seat.gainAge(event.getPartialTick());
                float gain=entry==0&&gainAge>=0&&gainAge<14?(float)Math.sin(Math.PI*gainAge/14):0;
                float radius=(.105F+breathe*.1F+arrival*.014F+gain*.018F)*(1-.72F*closing);
                float dim=1-.5F*closing;
                disc(BUFFER,matrix,center,right,up,radius*2.4F,(.22F+gain*.08F)*dim,color);
                disc(BUFFER,matrix,center,right,up,radius*1.4F,(.68F+arrival*.15F)*dim,color);
                Vec3 hot=color.lerp(new Vec3(1,.96,.78),.55*(1-entry/300));
                disc(BUFFER,matrix,center,right,up,radius*.72F,(.9F+arrival*.09F)*dim,hot);
                if(gain>0)disc(BUFFER,matrix,center,right,up,.13F+gainAge*.01F,.2F*gain,GOLD);
            }
            var mesh=BUFFER.end();
            if(post){
                if(pending!=null)pending.release();pending=mesh;
                TrailShaderBridge.captureDepth();
            }else drawNormal(mesh);
        }finally{pose.popPose();if(BUFFER.building())BUFFER.end().release();}
    }

    private static void drawNormal(BufferBuilder.RenderedBuffer mesh){
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var shader=RenderSystem.getShader();float[] color=RenderSystem.getShaderColor().clone();
        try{
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.setShaderColor(1,1,1,1);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);BufferUploader.drawWithShader(mesh);
        }finally{
            RenderSystem.setShader(()->shader);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
            RenderSystem.depthMask(mask);if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(src,dst,srcA,dstA);if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
        }
    }
    private static void vertex(BufferBuilder b,Matrix4f m,Vec3 p,float alpha,Vec3 color){
        b.vertex(m,(float)p.x,(float)p.y,(float)p.z).color((float)color.x,(float)color.y,(float)color.z,alpha).endVertex();
    }
    private static void disc(BufferBuilder b,Matrix4f m,Vec3 p,Vec3 right,Vec3 up,float radius,float alpha,Vec3 color){
        for(int i=0;i<20;i++){
            double a=i*Math.PI/10,c=(i+1)*Math.PI/10;
            vertex(b,m,p,alpha,color);
            vertex(b,m,p.add(right.scale(Math.cos(a)*radius)).add(up.scale(Math.sin(a)*radius)),0,color);
            vertex(b,m,p.add(right.scale(Math.cos(c)*radius)).add(up.scale(Math.sin(c)*radius)),0,color);
        }
    }
    private static void ribbon(BufferBuilder b,Matrix4f m,Vec3 a,Vec3 c,Vec3 aw,Vec3 cw,float alpha,Vec3 color){
        vertex(b,m,a.add(aw),alpha,color);vertex(b,m,a.subtract(aw),alpha,color);vertex(b,m,c.add(cw),alpha*.8F,color);
        vertex(b,m,c.add(cw),alpha*.8F,color);vertex(b,m,a.subtract(aw),alpha,color);vertex(b,m,c.subtract(cw),alpha*.8F,color);
    }
    private MeditationAura(){}
}
