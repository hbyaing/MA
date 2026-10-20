package cn.blockforge.generated.generatedmod.client;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.io.IOException;
@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class AlchemyPreviewRenderer{
    private static ShaderInstance shader;
    @SubscribeEvent public static void register(net.minecraftforge.client.event.RegisterShadersEvent e)throws IOException{e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("wuxingxiantu","alchemy_preview_grey"),DefaultVertexFormat.POSITION_TEX_COLOR),s->shader=s);}
    static void grey(GuiGraphics g,ItemStack item,int x,int y){
        if(shader==null)return;var mc=Minecraft.getInstance();var sprite=mc.getItemRenderer().getModel(item,mc.level,mc.player,0).getParticleIcon();
        g.flush();var previous=RenderSystem.getShader();int texture=RenderSystem.getShaderTexture(0);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShader(()->shader);RenderSystem.setShaderTexture(0,TextureAtlas.LOCATION_BLOCKS);
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);var pose=g.pose().last().pose();
        b.vertex(pose,x,y+16,0).uv(sprite.getU0(),sprite.getV1()).color(255,255,255,255).endVertex();b.vertex(pose,x+16,y+16,0).uv(sprite.getU1(),sprite.getV1()).color(255,255,255,255).endVertex();b.vertex(pose,x+16,y,0).uv(sprite.getU1(),sprite.getV0()).color(255,255,255,255).endVertex();b.vertex(pose,x,y,0).uv(sprite.getU0(),sprite.getV0()).color(255,255,255,255).endVertex();
        BufferUploader.drawWithShader(b.end());RenderSystem.setShader(()->previous);RenderSystem.setShaderTexture(0,texture);
    }
}
