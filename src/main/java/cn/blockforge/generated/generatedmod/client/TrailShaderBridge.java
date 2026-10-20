package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import java.io.IOException;
import java.lang.reflect.Method;

/** 在世界阶段保存深度，合成结束后按保存的世界深度逐像素遮挡剑气。 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class TrailShaderBridge {
    private static ShaderInstance shader,meteorShader,meteorPostShader;
    private static ShaderInstance dipperShader,texturedPostShader;
    private static int depthTexture=-1,width,height;
    private static boolean depthReady;
    private static boolean searched;
    private static Object irisApi;
    private static Method shaderEnabled;
    private static Method shadowEnabled;
    public static String detection="未检测";
    public static String depthStatus="尚未捕获";
    public static int sourceFramebuffer;
    private static int failures;

    @SubscribeEvent public static void shaders(RegisterShadersEvent event)throws IOException{
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"sword_trail_post"),DefaultVertexFormat.POSITION_COLOR),s->shader=s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"dipper_ritual"),DefaultVertexFormat.POSITION_TEX_COLOR),s->dipperShader=s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"dipper_ritual_post"),DefaultVertexFormat.POSITION_TEX_COLOR),s->texturedPostShader=s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"realm_meteor"),DefaultVertexFormat.NEW_ENTITY),s->meteorShader=s);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"realm_meteor_post"),DefaultVertexFormat.NEW_ENTITY),s->meteorPostShader=s);
    }
    public static ShaderInstance meteorShader(){return meteorShader;}
    public static ShaderInstance meteorPostShader(){return meteorPostShader;}
    public static int capturedDepthTexture(){return depthReady?depthTexture:-1;}
    public static boolean meteorReady(){return meteorShader!=null&&meteorPostShader!=null;}
    public static boolean drawMeteor(BufferBuilder.RenderedBuffer mesh,Matrix4f projection){return drawInternal(mesh,projection,null,true);}
    public static boolean drawMeteor(BufferBuilder.RenderedBuffer mesh,Matrix4f projection,Matrix4f view){
        meteorPostShader.safeGetUniform("TrailView").set(view);
        meteorPostShader.safeGetUniform("EmissionPass").set(0F);
        return drawInternal(mesh,projection,null,true);
    }
    public static ShaderInstance dipperShader(){return dipperShader;}
    public static boolean shaderPackDetected(){
        if(!searched){
            searched=true;
            for(String name:new String[]{"net.irisshaders.iris.api.v0.IrisApi","net.coderbot.iris.api.v0.IrisApi"}){
                try{
                    Class<?> api=Class.forName(name);
                    irisApi=api.getMethod("getInstance").invoke(null);
                    shaderEnabled=api.getMethod("isShaderPackInUse");
                    try{shadowEnabled=api.getMethod("isRenderingShadowPass");}catch(NoSuchMethodException ignored){}
                    break;
                }catch(ReflectiveOperationException|LinkageError ignored){}
            }
        }
        if(shaderEnabled!=null){
            try{boolean active=(boolean)shaderEnabled.invoke(irisApi);detection=active?"已开启":"已关闭";return active;}
            catch(ReflectiveOperationException ignored){detection="接口检测失败";}
        }
        boolean installed=net.minecraftforge.fml.ModList.get().isLoaded("oculus")||net.minecraftforge.fml.ModList.get().isLoaded("iris");
        detection=installed?"未知，兼容通道":"未安装";
        return installed;
    }
    public static boolean shadowPass(){
        shaderPackDetected();if(irisApi==null||shadowEnabled==null)return false;
        try{return (boolean)shadowEnabled.invoke(irisApi);}catch(ReflectiveOperationException e){return false;}
    }
    public static void resetFrame(){depthReady=false;}
    public static void release(){
        depthReady=false;
        if(depthTexture!=-1){RenderSystem.deleteTexture(depthTexture);depthTexture=-1;}
        width=height=0;
    }
    public static boolean captureDepth(){
        if(depthReady)return true;
        int oldRead=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int oldActive=GL11.glGetInteger(org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE);
        GlStateManager._activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        int oldTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        sourceFramebuffer=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        try{
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,sourceFramebuffer);
            int attachment=sourceFramebuffer==0?GL11.GL_BACK:GL30.GL_DEPTH_ATTACHMENT;
            if(sourceFramebuffer!=0&&GL30.glGetFramebufferAttachmentParameteri(GL30.GL_READ_FRAMEBUFFER,attachment,GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE)==GL11.GL_NONE){
                depthStatus="当前目标缺少深度附件";return false;
            }
            if(depthTexture==-1)depthTexture=GL11.glGenTextures();
            RenderSystem.bindTexture(depthTexture);
            if(width!=viewport[2]||height!=viewport[3]){
                width=viewport[2];height=viewport[3];
                GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_DEPTH_COMPONENT32F,viewport[0],viewport[1],width,height,0);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL30.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL30.GL_CLAMP_TO_EDGE);
            }else GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,viewport[0],viewport[1],width,height);
            int error=GL11.glGetError();
            depthReady=error==GL11.GL_NO_ERROR;
            if(!depthReady){width=height=0;}
            depthStatus=depthReady?"世界深度已捕获 "+width+"×"+height:"深度捕获失败 GL="+error;
            if(!depthReady&&failures++<3)com.mojang.logging.LogUtils.getLogger().warn("御剑拖尾：{}，源 FBO={}",depthStatus,sourceFramebuffer);
            return depthReady;
        }finally{
            RenderSystem.bindTexture(oldTexture);
            GlStateManager._activeTexture(oldActive);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,oldRead);
        }
    }
    public static boolean draw(BufferBuilder.RenderedBuffer mesh,Matrix4f projection){
        return draw(mesh,projection,null);
    }
    /** Textured counterpart uses atlas on unit 0 and captured scene depth on unit 1. */
    public static boolean draw(BufferBuilder.RenderedBuffer mesh,Matrix4f projection,ResourceLocation atlas){
        return drawInternal(mesh,projection,atlas,false);
    }
    private static boolean drawInternal(BufferBuilder.RenderedBuffer mesh,Matrix4f projection,ResourceLocation atlas,boolean meteor){
        ShaderInstance selected=meteor?meteorPostShader:atlas==null?shader:texturedPostShader;
        if(!depthReady||selected==null){mesh.release();return false;}
        var target=Minecraft.getInstance().getMainRenderTarget();
        int oldDraw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int oldRead=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int[] scissorBox=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissorBox);
        float[] color=RenderSystem.getShaderColor().clone();
        int srcRgb=GL11.glGetInteger(GL14Compat.SRC_RGB),dstRgb=GL11.glGetInteger(GL14Compat.DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14Compat.SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14Compat.DST_ALPHA);
        int texture=RenderSystem.getShaderTexture(0);
        int texture1=RenderSystem.getShaderTexture(1);
        var previousShader=RenderSystem.getShader();
        var previousBlend=BlendMode.lastApplied;
        try{
            target.bindWrite(true);
            // HUD 阶段的硬件深度已经清空，世界遮挡由片元着色器读取快照执行。
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
            RenderSystem.setShaderColor(1,1,1,1);RenderSystem.disableScissor();
            selected.safeGetUniform("TrailProjection").set(projection);
            if(meteor)selected.safeGetUniform("EffectTime").set((Minecraft.getInstance().level.getGameTime()+Minecraft.getInstance().getFrameTime())/20F);
            selected.safeGetUniform("OutputSize").set((float)target.viewWidth,(float)target.viewHeight);
            if(atlas==null)RenderSystem.setShaderTexture(0,depthTexture);
            else {RenderSystem.setShaderTexture(0,atlas);RenderSystem.setShaderTexture(1,depthTexture);}
            RenderSystem.setShader(()->selected);
            BufferUploader.drawWithShader(mesh);
            return true;
        }finally{
            RenderSystem.setShaderTexture(0,texture);RenderSystem.setShader(()->previousShader);
            RenderSystem.setShaderTexture(1,texture1);
            RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
            if(scissor)RenderSystem.enableScissor(scissorBox[0],scissorBox[1],scissorBox[2],scissorBox[3]);
            RenderSystem.depthMask(depthMask);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,oldDraw);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,oldRead);
            RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            BlendMode.lastApplied=previousBlend;
        }
    }
    private static final class GL14Compat {
        static final int SRC_RGB=org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB,DST_RGB=org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB;
        static final int SRC_ALPHA=org.lwjgl.opengl.GL14.GL_BLEND_SRC_ALPHA,DST_ALPHA=org.lwjgl.opengl.GL14.GL_BLEND_DST_ALPHA;
    }
}
