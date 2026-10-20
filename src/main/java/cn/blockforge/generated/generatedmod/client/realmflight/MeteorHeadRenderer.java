package cn.blockforge.generated.generatedmod.client.realmflight;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.TrailShaderBridge;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import java.io.IOException;
import java.util.List;

/** Projected bounds for a Yuanying volume or the higher-realm transparent sphere. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MeteorHeadRenderer {
    private static ShaderInstance shader;
    private static VertexBuffer emissionGeometry;
    public static int draws;
    public record Head(Matrix4f viewToHead,float left,float bottom,float right,float top,int color,float opacity,int steps,float phase,int style,float speed,float turn) {}
    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
        release();
        event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"realm_head_energy"),DefaultVertexFormat.POSITION_TEX),s->shader=s);
    }
    public static boolean ready(){return shader!=null;}
    public static void release(){if(emissionGeometry!=null){emissionGeometry.close();emissionGeometry=null;}}
    public static Head create(Matrix4f headToView,Matrix4f projection,int color,float opacity,int steps,float phase,int style){
        return create(headToView,projection,color,opacity,steps,phase,style,0,0);
    }
    public static Head create(Matrix4f headToView,Matrix4f projection,int color,float opacity,int steps,float phase,int style,float speed,float turn){
        Matrix4f matrix=new Matrix4f(projection).mul(headToView);
        float left=1,bottom=1,right=-1,top=-1;
        boolean intersectsEye=false;
        int visibleCorners=0;
        float side=style>0?1.02F:1.5F;
        float rear=style>0?-1.02F:-2.5F,front=style>0?1.02F:1.25F;
        for(float x:new float[]{-side,side})for(float y:new float[]{-side,side})for(float z:new float[]{rear,front}){
            var clip=matrix.transform(new Vector4f(x,y,z,1));
            if(clip.w<.001F){intersectsEye=true;continue;}
            visibleCorners++;
            left=Math.min(left,clip.x/clip.w);right=Math.max(right,clip.x/clip.w);
            bottom=Math.min(bottom,clip.y/clip.w);top=Math.max(top,clip.y/clip.w);
        }
        if(visibleCorners==0)return null;
        if(intersectsEye){left=bottom=-1;right=top=1;}
        else if(right<-1||left>1||top<-1||bottom>1)return null;
        float safeSpeed=Float.isFinite(speed)?Math.max(0,Math.min(1,speed)):0;
        float safeTurn=Float.isFinite(turn)?Math.max(-1,Math.min(1,turn)):0;
        return new Head(new Matrix4f(headToView).invert(),Math.max(-1,left),Math.max(-1,bottom),Math.min(1,right),Math.min(1,top),color,opacity,steps,phase,style,safeSpeed,safeTurn);
    }
    private static void uniforms(Head head,int width,int height,Matrix4f inverseProjection,boolean emission){
        shader.safeGetUniform("ViewToHead").set(head.viewToHead());
        shader.safeGetUniform("InverseProjection").set(inverseProjection);
        shader.safeGetUniform("OutputSize").set((float)width,(float)height);
        shader.safeGetUniform("HeadColor").set((head.color()>>16&255)/255F,(head.color()>>8&255)/255F,(head.color()&255)/255F,head.opacity());
        shader.safeGetUniform("Steps").set((float)head.steps());
        shader.safeGetUniform("FlightStyle").set((float)head.style());
        shader.safeGetUniform("FlightMotion").set(head.speed(),head.turn());
        shader.safeGetUniform("EffectTime").set((Minecraft.getInstance().level.getGameTime()+Minecraft.getInstance().getFrameTime())/20F+head.phase());
        shader.safeGetUniform("EmissionPass").set(emission?1F:0F);
        shader.setSampler("Sampler0",TrailShaderBridge.capturedDepthTexture());
    }
    public static void drawBase(List<Head> heads,Matrix4f inverseProjection){
        if(heads.isEmpty()||!ready()||TrailShaderBridge.capturedDepthTexture()<0)return;
        try(var state=new MeteorBloomRenderer.State()){
            var main=Minecraft.getInstance().getMainRenderTarget();main.bindWrite(true);
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableScissor();
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.colorMask(true,true,true,false);
            for(var head:heads){
                uniforms(head,main.viewWidth,main.viewHeight,inverseProjection,false);
                RenderSystem.setShader(()->shader);RenderSystem.setShaderTexture(0,TrailShaderBridge.capturedDepthTexture());
                BufferUploader.drawWithShader(mesh(head));draws++;
            }
        }
    }
    /** Called while the shared emission target is bound; no separate full-screen pass per player. */
    public static void drawEmission(List<Head> heads,int width,int height,Matrix4f inverseProjection){
        if(heads.isEmpty()||!ready())return;
        for(var head:heads){
            uniforms(head,width,height,inverseProjection,true);
            if(emissionGeometry==null)emissionGeometry=new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            {
                emissionGeometry.bind();emissionGeometry.upload(mesh(head));shader.apply();
                RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE,GL11.GL_ONE,GL11.GL_ONE);
                GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_MIN);
                emissionGeometry.draw();shader.clear();VertexBuffer.unbind();BufferUploader.invalidate();
            }
        }
        shader.safeGetUniform("EmissionPass").set(0F);
        GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);
    }
    private static BufferBuilder.RenderedBuffer mesh(Head head){
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        b.vertex(head.left(),head.bottom(),0).uv(0,0).endVertex();
        b.vertex(head.right(),head.bottom(),0).uv(1,0).endVertex();
        b.vertex(head.right(),head.top(),0).uv(1,1).endVertex();
        b.vertex(head.left(),head.top(),0).uv(0,1).endVertex();return b.end();
    }
    private MeteorHeadRenderer(){}
}
