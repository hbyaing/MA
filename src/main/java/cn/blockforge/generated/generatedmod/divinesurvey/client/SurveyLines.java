package cn.blockforge.generated.generatedmod.divinesurvey.client;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
/** Explicit depth state: the default NO_DEPTH_TEST shard doesn't disable an already enabled depth test. */
public final class SurveyLines {
 public static final RenderType THROUGH=new RenderType("sense_through_lines",DefaultVertexFormat.POSITION_COLOR_NORMAL,VertexFormat.Mode.LINES,256,false,false,()->{
  RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
  RenderSystem.disableDepthTest();RenderSystem.depthMask(false);
  RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.lineWidth(2F);
 },()->{
  RenderSystem.lineWidth(1F);RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.enableCull();RenderSystem.disableBlend();
 }){};
}
