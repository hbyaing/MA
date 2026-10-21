package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class VisualSmokeTest {
    private static boolean done;

    @SubscribeEvent
    public static void render(TickEvent.RenderTickEvent event) {
        Minecraft client=Minecraft.getInstance();
        if (done || event.phase!=TickEvent.Phase.END || client.getOverlay()!=null || client.screen==null) return;
        done=true;
        TextureTarget target=new TextureTarget(32,24,false,Minecraft.ON_OSX);
        try (PostChain chain=new PostChain(client.getTextureManager(),client.getResourceManager(),target,
                new ResourceLocation(GeneratedMod.MOD_ID,"shaders/post/time_stop.json"))) {
            var pass=chain.addPass("wuxingxiantu:time_stop",target,chain.getTempTarget("swap"));
            chain.addPass("blit",chain.getTempTarget("swap"),target);
            chain.resize(32,24);
            for (float phase:new float[]{0,0.5F,1}) {
                target.setClearColor(0.8F,0.1F,0.2F,1);
                target.clear(Minecraft.ON_OSX);
                pass.getEffect().safeGetUniform("Phase").set(phase);
                RenderSystem.disableDepthTest();
                chain.process(0);
                RenderSystem.bindTexture(target.getColorTextureId());
                try (NativeImage pixels=new NativeImage(32,24,false)) {
                    pixels.downloadTexture(0,false);
                    int c=pixels.getPixelRGBA(16,12),r=c&255,g=(c>>8)&255,b=(c>>16)&255;
                    if (phase==1 && (Math.abs(r-g)>1 || Math.abs(g-b)>1 || r<40 || r>110))
                        throw new IllegalStateException("Scene did not become luminance gray: "+r+","+g+","+b);
                    if (phase==0 && (r<195 || g>35 || b>60))
                        throw new IllegalStateException("Inactive shader changed the scene");
                    if (phase==0.5F) {
                        int edge=pixels.getPixelRGBA(0,0);
                        if (Math.abs(r-g)>1 || (edge&255)<195) throw new IllegalStateException("Radial transition failed");
                    }
                }
            }
            for (String id:new String[]{"meditation_cushion","spirit_cushion","meditation_platform"}) {
                var block=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,id));
                var model=client.getBlockRenderer().getBlockModel(block.defaultBlockState());
                if (model==client.getModelManager().getMissingModel()
                        || model.getQuads(null,null,net.minecraft.util.RandomSource.create()).isEmpty())
                    throw new IllegalStateException("Missing/empty model: "+id);
            }
            for (String id:new String[]{"art_sword_flight","art_sword_pierce","art_sword_sweep","art_sword_guard","art_sword_array"}) {
                var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,id));
                var model=client.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(item),null,null,0);
                if(model==client.getModelManager().getMissingModel()) throw new IllegalStateException("Missing sword manual model: "+id);
            }
            var swordItem=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,"dark_iron_sword"));
            var swordStack=new net.minecraft.world.item.ItemStack(swordItem);
            var handModel=client.getItemRenderer().getModel(swordStack,null,null,0);
            var swordModel=client.getModelManager().getModel(FlyingSwordItemModels.WORLD_MODEL);
            if(handModel==swordModel||handModel==client.getModelManager().getMissingModel())throw new IllegalStateException("Hand/world models not separated");
            int quads=swordModel.getQuads(null,null,net.minecraft.util.RandomSource.create()).size();
            if(swordModel==client.getModelManager().getMissingModel()||quads<2500)
                throw new IllegalStateException("Sword model failed to bake all 434 cubes: "+quads);
            client.getMainRenderTarget().bindWrite(true);
            var gui=new net.minecraft.client.gui.GuiGraphics(client,client.renderBuffers().bufferSource());
            int width=client.getWindow().getGuiScaledWidth(),height=client.getWindow().getGuiScaledHeight();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,width,height,0,-1000,3000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
            var modelView=RenderSystem.getModelViewStack();modelView.pushPose();modelView.setIdentity();RenderSystem.applyModelViewMatrix();
            gui.fill(0,0,width,height,0xFF20272A);
            gui.pose().pushPose();
            float scale=Math.min(width,height)/20F;
            gui.pose().translate(width/2F-8*scale,height/2F-8*scale,0);gui.pose().scale(scale,scale,1);
            gui.renderItem(swordStack,0,0);gui.pose().popPose();gui.flush();
            try(var screenshot=net.minecraft.client.Screenshot.takeScreenshot(client.getMainRenderTarget())){
                int background=screenshot.getPixelRGBA(2,2),changed=0;
                for(int y=0;y<screenshot.getHeight();y++)for(int x=0;x<screenshot.getWidth();x++)
                    if(screenshot.getPixelRGBA(x,y)!=background)changed++;
                if(changed<1000||changed>screenshot.getWidth()*screenshot.getHeight()/2)
                    throw new IllegalStateException("Sword render pixel coverage invalid: "+changed);
                screenshot.writeToFile(java.nio.file.Path.of("sword-model-smoke.png"));
            }
            modelView.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();
            java.nio.file.Files.writeString(java.nio.file.Path.of("visual-smoke-result.txt"),
                    "PASS: actual Minecraft GL shader phases 0/0.5/1, three baked blocks, sword "+quads+" quads and sword-model-smoke.png");
            com.mojang.logging.LogUtils.getLogger().info("VISUAL_SMOKE_PASS");
        } catch (Exception ex) {
            com.mojang.logging.LogUtils.getLogger().error("VISUAL_SMOKE_FAIL",ex);
            try{java.nio.file.Files.writeString(java.nio.file.Path.of("visual-smoke-result.txt"),"FAIL: "+ex);}catch(Exception ignored){}
        } finally {
            target.destroyBuffers();
            client.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest();
            client.stop();
        }
    }
}
