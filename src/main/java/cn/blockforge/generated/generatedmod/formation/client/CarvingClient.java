package cn.blockforge.generated.generatedmod.formation.client;
import cn.blockforge.generated.generatedmod.GeneratedMod;import cn.blockforge.generated.generatedmod.formation.carving.*;
import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraft.client.gui.screens.MenuScreens;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.*;import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;import net.minecraft.world.item.*;import net.minecraft.client.Minecraft;import com.mojang.blaze3d.vertex.*;import com.mojang.blaze3d.systems.RenderSystem;import com.mojang.math.Axis;import org.joml.Matrix4f;
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CarvingClient {
 private static ShaderInstance field;
 @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->MenuScreens.register(CarvingRegistry.MENU.get(),CarvingScreen::new));}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(CarvingRegistry.TILE.get(),context->new EyeRenderer());}
 @SubscribeEvent public static void shader(RegisterShadersEvent e){try{e.registerShader(new ShaderInstance(e.getResourceProvider(),ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"carved_formation"),DefaultVertexFormat.POSITION_COLOR),s->field=s);}catch(java.io.IOException ex){throw new IllegalStateException(ex);}}
 private static final class EyeRenderer implements BlockEntityRenderer<CarvingEye>{
  @Override public int getViewDistance(){return 80;}
  @Override public boolean shouldRenderOffScreen(CarvingEye eye){return eye.powered();}
  @Override public void render(CarvingEye eye,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
   if(!eye.isEye()||eye.getLevel()==null)return;float time=(eye.getLevel().getGameTime()%72000+partial)/20F;
   ItemStack stone=ItemStack.EMPTY;for(int i=0;i<4;i++)if(!eye.fuel.getItem(i).isEmpty()){stone=eye.fuel.getItem(i);break;}
   if(!stone.isEmpty()){pose.pushPose();pose.translate(.5,.72+Math.sin(time*1.8)*.035,.5);pose.mulPose(Axis.YP.rotation(time*.7F));pose.scale(.45F,.45F,.45F);Minecraft.getInstance().getItemRenderer().renderStatic(stone,ItemDisplayContext.GROUND,15728880,OverlayTexture.NO_OVERLAY,pose,buffers,eye.getLevel(),0);pose.popPose();}
   if(!eye.powered()||field==null)return;
   if(buffers instanceof MultiBufferSource.BufferSource source)source.endBatch();
   try(PassState ignored=new PassState()){
    pose.pushPose();try{pose.translate(.5,.025,.5);float radius=CarvingConfig.RADIUS.get().floatValue();pose.scale(radius,radius,radius);
    RenderSystem.enableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShader(()->field);
    field.getUniform("LocalFromPosition").set(new Matrix4f(pose.last().pose()).invert());field.getUniform("Time").set(time);field.getUniform("Kind").set((float)eye.kind());
    var camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();field.getUniform("Eye").set((float)((camera.x-eye.getBlockPos().getX()-.5)/radius),(float)((camera.y-eye.getBlockPos().getY()-.025)/radius),(float)((camera.z-eye.getBlockPos().getZ()-.5)/radius));
    field.getUniform("Mode").set(0F);var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);var m=pose.last().pose();
    for(float[] v:new float[][]{{-1.25F,0,-1.25F},{1.25F,0,-1.25F},{1.25F,0,1.25F},{-1.25F,0,-1.25F},{1.25F,0,1.25F},{-1.25F,0,1.25F}})b.vertex(m,v[0],v[1],v[2]).color(255,255,255,255).endVertex();BufferUploader.drawWithShader(b.end());
    if(eye.kind()>0){field.getUniform("Mode").set(1F);b.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);for(int lat=0;lat<16;lat++)for(int lon=0;lon<64;lon++)for(int[] v:new int[][]{{0,0},{1,0},{1,1},{0,0},{1,1},{0,1}}){double a=(lat+v[0])*Math.PI/32,h=(lon+v[1])*Math.PI/32;b.vertex(m,(float)(Math.cos(a)*Math.cos(h)),(float)(.8*Math.sin(a)),(float)(Math.cos(a)*Math.sin(h))).color(255,255,255,255).endVertex();}BufferUploader.drawWithShader(b.end());}
    }finally{pose.popPose();}
   }
  }
 }
 private CarvingClient(){}
}
