package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
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
import org.lwjgl.opengl.*;
import java.io.IOException;

/** Draws after shader-pack composition, against the captured real world depth. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SenseFieldGpu {
    private static ShaderInstance shader;
    public static long submitted;
    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
        event.registerShader(new ShaderInstance(event.getResourceProvider(),ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"sense_field"),DefaultVertexFormat.POSITION_TEX_COLOR),s->shader=s);
    }
    public static boolean ready(){return shader!=null;}
    public static void draw(BufferBuilder.RenderedBuffer mesh,Matrix4f matrix,float mode,float time,float progress){
        if(shader==null||TrailShaderBridge.capturedDepthTexture()<0){mesh.release();return;}
        int oldDraw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),oldRead=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int sr=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dr=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),sa=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),da=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var previous=RenderSystem.getShader();var oldBlend=BlendMode.lastApplied;int tex=RenderSystem.getShaderTexture(0);float[] color=RenderSystem.getShaderColor().clone();
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);int bound=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try{
            var target=Minecraft.getInstance().getMainRenderTarget();target.bindWrite(true);RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableScissor();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(1,1,1,1);
            shader.safeGetUniform("SenseMatrix").set(matrix);shader.safeGetUniform("OutputSize").set((float)target.viewWidth,(float)target.viewHeight);
            shader.safeGetUniform("ShapeMode").set(mode);shader.safeGetUniform("EffectTime").set(time);shader.safeGetUniform("Progress").set(progress);
            RenderSystem.setShaderTexture(0,TrailShaderBridge.capturedDepthTexture());RenderSystem.setShader(()->shader);BufferUploader.drawWithShader(mesh);submitted++;
        }finally{
            RenderSystem.setShaderTexture(0,tex);GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(bound);GlStateManager._activeTexture(active);
            RenderSystem.setShader(()->previous);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);RenderSystem.depthMask(mask);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(sr,dr,sa,da);if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();if(scissor)GL11.glEnable(GL11.GL_SCISSOR_TEST);else GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,oldDraw);GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,oldRead);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);BlendMode.lastApplied=oldBlend;
        }
    }
    private SenseFieldGpu(){}
}
