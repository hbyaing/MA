package cn.blockforge.generated.generatedmod.client.cloud;

import com.google.gson.*;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import java.io.*;
import java.nio.file.*;

/** Finite world media. Never samples an attachment on the active output FBO. */
@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT)
public final class TribulationClouds {
    static ShaderInstance shader,composite;
    static TextureTarget snapshot,volume;
    static VolumeRecipe defaults,recipe;
    static boolean failed,frameReady;
    static String reason="not-loaded";
    static int generation,draws;
    static float fixedTime,actualTime;
    private static net.minecraft.client.CloudStatus savedVanillaClouds;
    private static boolean hasClouds(){
        var mc=Minecraft.getInstance();
        // Vanilla clouds must be hidden for the whole tribulation, even if the volume pass is unavailable.
        return mc.level!=null && clouds.values().stream().anyMatch(c->mc.level.getGameTime()-c.seen()<=60);
    }
    private static void cloudMode(net.minecraft.client.CloudStatus mode){
        var mc=Minecraft.getInstance();
        // The Fabulous option callback clears a target; preserve its framebuffer and viewport.
        if(!Minecraft.useShaderTransparency()){mc.options.cloudStatus().set(mode);return;}
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        try{mc.options.cloudStatus().set(mode);}
        finally{GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
            RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);}
    }
    private static void hideVanillaClouds(){
        if(savedVanillaClouds!=null || !hasClouds())return;
        var setting=Minecraft.getInstance().options.cloudStatus().get();
        if(setting==net.minecraft.client.CloudStatus.OFF)return;
        savedVanillaClouds=setting;cloudMode(net.minecraft.client.CloudStatus.OFF);
    }
    private static void restoreVanillaClouds(){
        if(savedVanillaClouds==null)return;
        var setting=savedVanillaClouds;savedVanillaClouds=null;cloudMode(setting);
    }

    static final Matrix4f inverseProjection=new Matrix4f();
    static final Matrix3f inverseRotation=new Matrix3f();
    static final Vector3f inverseTranslation=new Vector3f();
    private static final java.util.Map<java.util.UUID,Cloud> clouds=new java.util.HashMap<>();
    private record Cloud(net.minecraft.world.phys.Vec3 anchor,int realm,int seed,long started,long seen){}
    private static Cloud current;
    private static double formationDuration=9,contraction=.65,swirl=1.5;
    public static void accept(cn.blockforge.generated.generatedmod.tribulation.TribulationVisualPacket packet){
        if(packet.kind()==3){clouds.remove(packet.owner());return;}
        if(packet.kind()!=6)return;
        clouds.put(packet.owner(),new Cloud(packet.start(),packet.realm(),packet.seed(),packet.tick()-packet.count(),packet.tick()));
    }
    private static java.util.Map<String,Double> parameters(double seconds){
        double t=Math.max(0,Math.min(1,seconds/formationDuration)),ease=t*t*(3-2*t);
        double radius=Math.max(recipe.value("radius"),384+current.realm()*96);
        return java.util.Map.ofEntries(
            java.util.Map.entry("centerX",current.anchor().x),java.util.Map.entry("centerY",current.anchor().y+25),java.util.Map.entry("centerZ",current.anchor().z),
            java.util.Map.entry("bottom",current.anchor().y),java.util.Map.entry("radius",radius*(1+contraction*(1-ease))),
            java.util.Map.entry("density",recipe.value("density")*ease),java.util.Map.entry("curl",recipe.value("curl")*(1+swirl*(1-ease))),
            java.util.Map.entry("seed",(double)Math.floorMod(current.seed(),100000)),java.util.Map.entry("flashEnabled",seconds>=3?recipe.value("flashEnabled"):0.0));
    }

    @Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Resources {
        @SubscribeEvent public static void shaders(RegisterShadersEvent event) throws IOException {
            try {
            var provider=event.getResourceProvider();
            try(var r=provider.getResourceOrThrow(new ResourceLocation("wuxingxiantu","volume/schema.json")).openAsReader()){VolumeRecipe.schema=JsonParser.parseReader(r).getAsJsonObject();}
            try(var r=provider.getResourceOrThrow(new ResourceLocation("wuxingxiantu","volume/contract.json")).openAsReader()){VolumeRecipe.contract=JsonParser.parseReader(r).getAsJsonObject();}
            VolumeRecipe loaded;
            try(var r=provider.getResourceOrThrow(new ResourceLocation("wuxingxiantu","volume/tribulation-cloud.json")).openAsReader()){
                var root=JsonParser.parseReader(r).getAsJsonObject();loaded=VolumeRecipe.read(new StringReader(root.getAsJsonObject("recipe").toString()));
                var formation=root.getAsJsonObject("formation");formationDuration=formation.get("enabled").getAsBoolean()?Math.max(.1,formation.get("duration").getAsDouble()):.1;
                contraction=formation.get("contraction").getAsDouble();swirl=formation.get("swirl").getAsDouble();
            }
            final VolumeRecipe newDefault=loaded;
            event.registerShader(new ShaderInstance(provider,new ResourceLocation("wuxingxiantu","world_volume"),DefaultVertexFormat.POSITION),s->{
                shader=s;defaults=newDefault;recipe=defaults;
                generation++;failed=false;release();
                reason="ready";System.out.println("[WORLDVOLUME] Shader loaded generation="+generation+" renderer="+GL11.glGetString(GL11.GL_RENDERER));
            });
            event.registerShader(new ShaderInstance(provider,new ResourceLocation("wuxingxiantu","world_volume_composite"),DefaultVertexFormat.POSITION),s->composite=s);
            } catch (Exception e) { shader=null;failed=true;reason="shader-load-failed";release();System.err.println("[WORLDVOLUME] Custom resource load disabled volume pass: "+e); }
        }
        @SubscribeEvent public static void reloadListeners(RegisterClientReloadListenersEvent event){
            event.registerReloadListener((ResourceManagerReloadListener)manager->RenderSystem.recordRenderCall(TribulationClouds::release));
        }
    }
    static void release(){if(snapshot!=null){snapshot.destroyBuffers();snapshot=null;}if(volume!=null){volume.destroyBuffers();volume=null;}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){restoreVanillaClouds();release();frameReady=false;clouds.clear();current=null;}
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide()){restoreVanillaClouds();release();frameReady=false;clouds.clear();current=null;}}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void frame(TickEvent.RenderTickEvent event){
        if(event.phase==TickEvent.Phase.START){
            frameReady=false;
            // Apply before any world renderer reads the cloud option, including shader adapters.
            if(hasClouds())hideVanillaClouds();else restoreVanillaClouds();
        }else restoreVanillaClouds();
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void stage(RenderLevelStageEvent event){
        // BEFORE/AFTER_LEVEL's pose is restored. AFTER_SKY still has the actual
        // level camera basis, including view bob and camera effects.
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_SKY){
            inverseProjection.set(event.getProjectionMatrix()).invert();
            Matrix4f invView=new Matrix4f(event.getPoseStack().last().pose()).invert();
            inverseRotation.set(invView);invView.getTranslation(inverseTranslation);
            frameReady=inverseProjection.isFinite()&&inverseRotation.isFinite()&&inverseTranslation.isFinite();
            if(frameReady)hideVanillaClouds();return;
        }
        // Vanilla renders clouds after particles; keep OFF until RenderTick END.
        // Queue first at this stage; lightning queues afterward and remains visible through clouds.
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        var mc=Minecraft.getInstance();
        if(mc.level==null){clouds.clear();current=null;return;}
        long now=mc.level.getGameTime();clouds.values().removeIf(c->now-c.seen()>60);
        current=clouds.values().stream().min(java.util.Comparator.comparingDouble(c->c.anchor().distanceToSqr(mc.gameRenderer.getMainCamera().getPosition()))).orElse(null);
        if(current==null||failed||shader==null||composite==null||recipe==null||!frameReady){if(!failed)reason="disabled-or-no-camera";return;}
        if(event.getCamera().getFluidInCamera()!=FogType.NONE){reason="camera-submerged";return;}
        var camera=event.getCamera().getPosition();float partial=event.getPartialTick();
        if(!cn.blockforge.generated.generatedmod.client.tribulation.LightningShaderBridge.prepare(()->drawVolume(camera,partial,
                cn.blockforge.generated.generatedmod.client.tribulation.LightningShaderBridge.depthSnapshot())))reason="late-depth-unavailable";
    }
    // Separate adapter: pack depth captured before composite; final color sampled after it.
    // Shared late bridge preserves opaque depth; shader-pack visual validation is pending.
    private static void drawVolume(net.minecraft.world.phys.Vec3 camera,float partial,int externalDepth){
        var mc=Minecraft.getInstance();if(mc.level==null||current==null||shader==null)return;
        RenderTarget main=mc.getMainRenderTarget();
        if(main.getDepthTextureId()<0||GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)!=main.frameBufferId){reason="unexpected-framebuffer";return;}
        State state=new State();

        try(state){
            if(snapshot==null||snapshot.width!=main.width||snapshot.height!=main.height){release();snapshot=new TextureTarget(main.width,main.height,true,Minecraft.ON_OSX);snapshot.setFilterMode(GL11.GL_NEAREST);}
            double scale=Math.min(.5,Math.min(960.0/main.width,540.0/main.height));
            int width=Math.max(1,(int)Math.ceil(main.width*scale)),height=Math.max(1,(int)Math.ceil(main.height*scale));
            if(volume==null||volume.width!=width||volume.height!=height){if(volume!=null)volume.destroyBuffers();volume=new TextureTarget(width,height,false,Minecraft.ON_OSX);volume.setFilterMode(GL11.GL_NEAREST);}
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,snapshot.frameBufferId);
            if(GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Snapshot framebuffer incomplete");
            GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,main.width,main.height,GL11.GL_COLOR_BUFFER_BIT|(externalDepth>=0?0:GL11.GL_DEPTH_BUFFER_BIT),GL11.GL_NEAREST);
            volume.bindWrite(true);
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableBlend();RenderSystem.disableCull();RenderSystem.colorMask(true,true,true,true);
            shader.safeGetUniform("InverseProjection").set(inverseProjection);
            shader.safeGetUniform("InverseViewRotation").set(inverseRotation);
            shader.safeGetUniform("InverseViewTranslation").set(inverseTranslation);
            actualTime=Math.max(0,(mc.level.getGameTime()+partial-current.started())/20f);shader.safeGetUniform("Time").set(actualTime);
            recipe.upload(shader,camera,parameters(actualTime));
            var flash=cn.blockforge.generated.generatedmod.client.tribulation.TribulationEffects.cloudFlash(
                    clouds.entrySet().stream().filter(e->e.getValue()==current).map(java.util.Map.Entry::getKey).findFirst().orElse(null),
                    mc.level.getGameTime()+partial);
            shader.safeGetUniform("LightningFlash").set((float)flash.position().x,(float)flash.position().y,
                    (float)flash.position().z,flash.brightness());
            shader.safeGetUniform("ViewSteps").set((int)Math.min(24,recipe.value("viewSteps")));
            shader.safeGetUniform("LightSteps").set((int)Math.min(2,recipe.value("lightSteps")));
            shader.safeGetUniform("VolumeOnly").set(1);
            shader.setSampler("SceneSampler",snapshot.getColorTextureId());shader.setSampler("DepthSampler",externalDepth>=0?externalDepth:snapshot.getDepthTextureId());
            fullscreen(shader);
            main.bindWrite(true);
            composite.safeGetUniform("InverseProjection").set(inverseProjection);
            composite.safeGetUniform("VolumeSize").set((float)width,(float)height);
            composite.setSampler("SceneSampler",snapshot.getColorTextureId());
            composite.setSampler("VolumeSampler",volume.getColorTextureId());
            composite.setSampler("DepthSampler",externalDepth>=0?externalDepth:snapshot.getDepthTextureId());
            fullscreen(composite);draws++;reason="drawn-half-resolution";

        }catch(Exception e){failed=true;reason="draw-failed";e.printStackTrace();}

    }
    private static void fullscreen(ShaderInstance pass){
        RenderSystem.setShader(()->pass);
        BufferBuilder b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION);
        b.vertex(-1,-1,0).endVertex();b.vertex(3,-1,0).endVertex();b.vertex(-1,3,0).endVertex();
        try{pass.apply();BufferUploader.draw(b.end());}finally{pass.clear();}
    }
    /** Both real GL bindings and Minecraft's texture/program caches are restored. */
    static final class State implements AutoCloseable {
        final int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        final int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        final int[] viewport=new int[4],bound=new int[3],textures=new int[3];
        final int vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),arrayBuffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING),elementBuffer=GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),write=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        final int srcRGB=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRGB=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        final boolean[] color=new boolean[4];final ShaderInstance previous=RenderSystem.getShader();
        State(){GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);var mask=cn.blockforge.generated.generatedmod.client.NativeGlState.colorMask();for(int i=0;i<4;i++)color[i]=mask[i];for(int i=0;i<3;i++){textures[i]=RenderSystem.getShaderTexture(i);GlStateManager._activeTexture(GL13.GL_TEXTURE0+i);bound[i]=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);}GlStateManager._activeTexture(active);}
        public void close(){
            BufferUploader.invalidate();GL30.glBindVertexArray(vao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,arrayBuffer);GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,elementBuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            for(int i=0;i<3;i++){RenderSystem.setShaderTexture(i,textures[i]);GlStateManager._activeTexture(GL13.GL_TEXTURE0+i);GlStateManager._bindTexture(bound[i]);}GlStateManager._activeTexture(active);RenderSystem.setShader(()->previous);GlStateManager._glUseProgram(program);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();RenderSystem.depthMask(write);RenderSystem.blendFuncSeparate(srcRGB,dstRGB,srcA,dstA);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();if(scissor)GL11.glEnable(GL11.GL_SCISSOR_TEST);else GL11.glDisable(GL11.GL_SCISSOR_TEST);RenderSystem.colorMask(color[0],color[1],color[2],color[3]);
        }
        boolean matches(){
            int[] vp=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,vp);
            boolean ok=draw==GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)&&read==GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING)&&active==GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)&&program==GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)&&java.util.Arrays.equals(viewport,vp)&&depth==GL11.glIsEnabled(GL11.GL_DEPTH_TEST)&&write==GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)&&blend==GL11.glIsEnabled(GL11.GL_BLEND)&&cull==GL11.glIsEnabled(GL11.GL_CULL_FACE)&&scissor==GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            ok &= vao==GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)&&arrayBuffer==GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)&&elementBuffer==GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            ok &= srcRGB==GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)&&dstRGB==GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)&&srcA==GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)&&dstA==GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)&&previous==RenderSystem.getShader();
            var mask=cn.blockforge.generated.generatedmod.client.NativeGlState.colorMask();for(int i=0;i<4;i++)ok &=(mask[i])==color[i];
            for(int i=0;i<3;i++){ok &=RenderSystem.getShaderTexture(i)==textures[i];GlStateManager._activeTexture(GL13.GL_TEXTURE0+i);ok &=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==bound[i];}GlStateManager._activeTexture(active);
            return ok;
        }
    }
}
