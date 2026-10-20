package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.IOException;

/** Draw supplied silhouettes in the inventory palette without altering their pixels. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CultivationMaskIcons {
    private static ShaderInstance shader;
    @SubscribeEvent public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"ui_alpha_icon"),DefaultVertexFormat.POSITION_TEX),s->shader=s);
    }
    static void draw(GuiGraphics g,String name,int x,int y,int size) {
        if(shader==null)return;
        int u=name.equals("domain")?86:117,v=name.equals("domain")?166:121;
        int sw=name.equals("domain")?1089:1020,sh=name.equals("domain")?879:1014;
        float scale=Math.min(size/(float)sw,size/(float)sh);
        float w=sw*scale,h=sh*scale,left=x+(size-w)/2,right=left+w,top=y+(size-h)/2,bottom=top+h;
        float u0=u/1254F,v0=v/1254F,u1=(u+sw)/1254F,v1=(v+sh)/1254F;
        g.flush();
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(()->shader);
        RenderSystem.setShaderTexture(0,new ResourceLocation(GeneratedMod.MOD_ID,"textures/gui/wuxia/vision/components/"+name+".png"));
        RenderSystem.setShaderColor(232/255F,200/255F,139/255F,1);
        var matrix=g.pose().last().pose();
        var buffer=Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix,left,top,0).uv(u0,v0).endVertex();
        buffer.vertex(matrix,left,bottom,0).uv(u0,v1).endVertex();
        buffer.vertex(matrix,right,bottom,0).uv(u1,v1).endVertex();
        buffer.vertex(matrix,right,top,0).uv(u1,v0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.disableBlend();
    }
}
