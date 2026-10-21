package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.util.*;

/** Deterministic server-time choreography, bounded surface shaders, no vanilla particle fallback. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class HeavenlyFurnaceVisuals {
    private static final Map<UUID,CompoundTag> EFFECTS=new LinkedHashMap<>();
    private static Object world;
    static ShaderInstance shader;
    public static CompoundTag active(UUID owner){var t=EFFECTS.get(owner);var mc=Minecraft.getInstance();return t!=null&&mc.level!=null&&mc.level.getGameTime()>=t.getLong("Start")&&mc.level.getGameTime()<t.getLong("Start")+t.getInt("Duration")?t:null;}
    private static RenderLevelStageEvent pendingFrame;
    private static boolean compositing;
    private static com.mojang.blaze3d.pipeline.TextureTarget materialTarget;
    private static int materialDepthRead;
    public static void releaseMaterials(){
        if(!RenderSystem.isOnRenderThread()){RenderSystem.recordRenderCall(HeavenlyFurnaceVisuals::releaseMaterials);return;}
        if(materialTarget!=null){materialTarget.destroyBuffers();materialTarget=null;}
        if(materialDepthRead!=0){org.lwjgl.opengl.GL30.glDeleteFramebuffers(materialDepthRead);materialDepthRead=0;}
    }
    static {ClientPacketSink.heavenlyFurnace=HeavenlyFurnaceVisuals::receive;}
    public static void receive(CompoundTag tag){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        if(world!=mc.level){world=mc.level;EFFECTS.clear();pendingFrame=null;HeavenlyFurnaceGpu.release();releaseMaterials();}
        switch(tag.getString("Kind")){
            case "open" -> {if(tag.hasUUID("Token"))mc.setScreen(new HeavenlyFurnaceScreen(tag));}
            case "start" -> {if(tag.hasUUID("Owner")&&tag.getString("Dimension").equals(mc.level.dimension().location().toString())){
                if(EFFECTS.size()>=32&&!EFFECTS.containsKey(tag.getUUID("Owner")))return;
                var state=tag.copy();
                if(tag.contains("Elapsed",Tag.TAG_LONG))state.putLong("Start",mc.level.getGameTime()-Math.max(0,Math.min(tag.getInt("Duration"),tag.getLong("Elapsed"))));
                EFFECTS.put(tag.getUUID("Owner"),state);}}
            case "end" -> {if(tag.hasUUID("Owner"))EFFECTS.remove(tag.getUUID("Owner"));}
            case "complete" -> {if(tag.hasUUID("Owner")){var effect=EFFECTS.get(tag.getUUID("Owner"));if(effect!=null)effect.put("Preview",tag.getCompound("Result"));}}
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(world!=mc.level){world=mc.level;EFFECTS.clear();pendingFrame=null;HeavenlyFurnaceGpu.release();releaseMaterials();}
        if(mc.level!=null)EFFECTS.values().removeIf(t->mc.level.getGameTime()>t.getLong("Start")+t.getInt("Duration")+24);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||EFFECTS.isEmpty()||shader==null||TrailShaderBridge.shadowPass())return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;Vec3 camera=e.getCamera().getPosition();
        // Small-world star ceilings are drawn at the later world-border stage.
        // Composite after the world even without Oculus, otherwise that opaque sky replaces the fireball.
        boolean defer=!compositing;
        if(defer){
            var pose=new PoseStack();pose.last().pose().set(e.getPoseStack().last().pose());
            pendingFrame=TrailShaderBridge.captureDepth()?new RenderLevelStageEvent(e.getStage(),e.getLevelRenderer(),pose,new Matrix4f(e.getProjectionMatrix()),e.getRenderTick(),e.getPartialTick(),e.getCamera(),e.getFrustum()):null;
        }
        int rendered=0;
        for(var tag:EFFECTS.values()){
            Vec3 origin=new Vec3(tag.getDouble("X"),tag.getDouble("Y"),tag.getDouble("Z"));
            Vec3 center=HeavenlyFurnace.fireballCenter(tag);
            if(origin.distanceToSqr(camera)>48*48)continue;
            if(!e.getFrustum().isVisible(new net.minecraft.world.phys.AABB(origin,center).inflate(6)))continue;
            if(rendered++>=8)break;
            float age=(float)(mc.level.getGameTime()-tag.getLong("Start"))+e.getPartialTick();if(age<0)continue;
            int duration=tag.getInt("Duration");float shrink=Mth.clamp((age-(duration-HeavenlyFurnace.CONDENSE_TICKS))/HeavenlyFurnace.CONDENSE_TICKS,0,1);
            var pose=e.getPoseStack();
            if(defer)continue;
            if(age>=duration)continue;
            float ignite=Mth.clamp((age-HeavenlyFurnace.GATHER_TICKS+12)/24,0,1);
            HeavenlyFurnaceGpu.render(e,center,tag.getFloat("Yaw"),age,ignite,shrink,tag.getInt("Fire"));
            var previous=RenderSystem.getShader();var oldBlend=BlendMode.lastApplied;
            boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            int sr=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dr=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),sa=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),da=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
            try{
                RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();
                if(compositing)RenderSystem.disableDepthTest();else RenderSystem.enableDepthTest();RenderSystem.depthMask(false);
                RenderSystem.setShader(()->shader);
                var matrix=new Matrix4f(e.getProjectionMatrix());
                shader.safeGetUniform("EffectMatrix").set(matrix.mul(pose.last().pose()));
                shader.safeGetUniform("EffectTime").set(age/20f);shader.safeGetUniform("FireTier").set((float)tag.getInt("Fire"));
                int color=tag.getInt("Fire")==9?SpiritFirePalette.spectrum((long)age):SpiritFirePalette.base(tag.getInt("Fire"));
                shader.safeGetUniform("FireColor").set((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);
                shader.safeGetUniform("Mode").set(3f);shader.safeGetUniform("Opacity").set((1-shrink)*Mth.clamp(age/15,0,1)*.68f);
                Vec3 fallback=origin.add(0,1.4,0).add(center.subtract(origin.add(0,1.4,0)).normalize().scale(.6));
                var caster=mc.level.getPlayerByUUID(tag.getUUID("Owner"));
                if(caster!=null)fallback=caster.getPosition(e.getPartialTick()).add(0,1.6,0);
                for(boolean left:new boolean[]{false,true}){
                    Vec3 palm=HeavenlyFurnacePose.palm(tag.getUUID("Owner"),left,fallback.add(left?-.35:.35,0,0)).subtract(camera);
                    if(mc.player!=null&&tag.getUUID("Owner").equals(mc.player.getUUID())&&mc.options.getCameraType().isFirstPerson()){
                        var hand=new org.joml.Vector4f(left?-.52f:.52f,-.20f,.8f,1);new Matrix4f(matrix).invert().transform(hand);hand.div(hand.w);palm=new Vec3(hand.x,hand.y,hand.z);
                    }
                    fuel(palm,center.subtract(camera),age+(left?7:0));
                }
                if(age<HeavenlyFurnace.GATHER_TICKS){
                    shader.safeGetUniform("Mode").set(2f);shader.safeGetUniform("Opacity").set(.65f);
                    gatheringThreads(tag,age,origin,center,camera);
                }
            }finally{
                RenderSystem.setShader(()->previous);RenderSystem.depthMask(mask);if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
                if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();RenderSystem.blendFuncSeparate(sr,dr,sa,da);
                if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();BlendMode.lastApplied=oldBlend;
            }
        }
    }
    private static void materials(RenderLevelStageEvent frame){
        if(EFFECTS.isEmpty()||TrailShaderBridge.shadowPass())return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        // Draw real items after temporal shader-pack passes; copy scene depth into an owned target.
        int w=mc.getMainRenderTarget().viewWidth,h=mc.getMainRenderTarget().viewHeight;
        boolean resized=materialTarget==null||materialTarget.width!=w||materialTarget.height!=h;
        if(materialTarget==null)materialTarget=new com.mojang.blaze3d.pipeline.TextureTarget(w,h,true,Minecraft.ON_OSX);
        else if(resized)materialTarget.resize(w,h,Minecraft.ON_OSX);
        if(resized){
            // Depth blits require identical formats; the bridge owns a DEPTH_COMPONENT32F texture.
            com.mojang.blaze3d.platform.GlStateManager._bindTexture(materialTarget.getDepthTextureId());
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT32F,w,h,0,GL11.GL_DEPTH_COMPONENT,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
        }
        materialTarget.setClearColor(0,0,0,0);materialTarget.clear(Minecraft.ON_OSX);
        if(materialDepthRead==0)materialDepthRead=org.lwjgl.opengl.GL30.glGenFramebuffers();
        org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER,materialDepthRead);
        org.lwjgl.opengl.GL30.glFramebufferTexture2D(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER,org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT,GL11.GL_TEXTURE_2D,TrailShaderBridge.capturedDepthTexture(),0);
        GL11.glReadBuffer(GL11.GL_NONE);
        org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER,materialTarget.frameBufferId);
        org.lwjgl.opengl.GL30.glBlitFramebuffer(0,0,w,h,0,0,w,h,GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
        materialTarget.bindWrite(true);
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();
        var previous=RenderSystem.getShader();int texture=RenderSystem.getShaderTexture(0);
        var model=RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(frame.getProjectionMatrix(),com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.enableDepthTest();RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(1,1,1,1);
            int count=0;for(var tag:EFFECTS.values()){
                float age=mc.level.getGameTime()-tag.getLong("Start")+frame.getPartialTick();if(age<0||age>tag.getInt("Duration")+24)continue;
                var origin=new Vec3(tag.getDouble("X"),tag.getDouble("Y"),tag.getDouble("Z"));
                if(origin.distanceToSqr(frame.getCamera().getPosition())>48*48)continue;if(count++>=8)break;
                renderItems(tag,age,origin,HeavenlyFurnace.fireballCenter(tag),frame.getCamera().getPosition(),frame.getPoseStack(),frame.getPartialTick());
            }
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().identity(),VertexSorting.ORTHOGRAPHIC_Z);model.setIdentity();RenderSystem.applyModelViewMatrix();
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);RenderSystem.setShaderTexture(0,materialTarget.getColorTextureId());RenderSystem.setShaderColor(1,1,1,1);
            var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);b.vertex(-1,-1,0).uv(0,0).endVertex();b.vertex(1,-1,0).uv(1,0).endVertex();b.vertex(1,1,0).uv(1,1).endVertex();b.vertex(-1,1,0).uv(0,1).endVertex();BufferUploader.drawWithShader(b.end());
        }finally{model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);RenderSystem.setShader(()->previous);RenderSystem.setShaderTexture(0,texture);}
    }
    @SubscribeEvent public static void post(RenderGuiEvent.Pre e){
        if(pendingFrame==null)return;e.getGuiGraphics().flush();submit();
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void frame(TickEvent.RenderTickEvent e){
        if(e.phase==TickEvent.Phase.START)pendingFrame=null;
        else if(Minecraft.getInstance().options.hideGui)submit();
    }
    private static void submit(){
        var frame=pendingFrame;pendingFrame=null;if(frame==null||EFFECTS.isEmpty()||shader==null)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        int draw=GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);int texture=RenderSystem.getShaderTexture(0);
        boolean scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);int[] box=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,box);
        boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int sr=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dr=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),sa=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),da=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        try{
            mc.getMainRenderTarget().bindWrite(true);RenderSystem.disableScissor();RenderSystem.setShaderTexture(0,TrailShaderBridge.capturedDepthTexture());
            shader.safeGetUniform("OutputSize").set((float)mc.getMainRenderTarget().viewWidth,(float)mc.getMainRenderTarget().viewHeight);
            materials(frame);
            RenderSystem.setShaderTexture(0,TrailShaderBridge.capturedDepthTexture());shader.safeGetUniform("UseSceneDepth").set(1f);compositing=true;render(frame);
        }finally{
            compositing=false;shader.safeGetUniform("UseSceneDepth").set(0f);RenderSystem.setShaderTexture(0,texture);
            RenderSystem.depthMask(mask);if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();RenderSystem.blendFuncSeparate(sr,dr,sa,da);if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            com.mojang.blaze3d.platform.GlStateManager._glBindFramebuffer(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER,draw);
            com.mojang.blaze3d.platform.GlStateManager._glBindFramebuffer(org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER,read);
            RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);if(scissor)RenderSystem.enableScissor(box[0],box[1],box[2],box[3]);
        }
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.options.hideGui||mc.screen!=null)return;
        var tag=EFFECTS.get(mc.player.getUUID());if(tag==null)return;
        float age=(float)(mc.level.getGameTime()-tag.getLong("Start"))+mc.getFrameTime();int duration=tag.getInt("Duration");if(age<0||age>=duration)return;
        int x=mc.getWindow().getGuiScaledWidth()/2-70,y=mc.getWindow().getGuiScaledHeight()-90;
        boolean refining=tag.getString("Mode").equals("refining");
        String phase=age<HeavenlyFurnace.GATHER_TICKS?"万物入炉":age>=duration-40?(refining?"造化铸器":"合气凝丹"):"阴阳炼化";
        var g=e.getGuiGraphics();g.fill(x,y,x+140,y+7,0xC01B2424);g.fill(x+1,y+1,x+1+(int)(138*age/duration),y+6,0xFF81CDB9);
        g.drawCenteredString(mc.font,"天地为炉 · "+phase+" · "+(int)Math.ceil((duration-age)/20)+"秒",x+70,y-12,0xEBD7A1);
    }
    private static void renderItems(CompoundTag tag,float age,Vec3 origin,Vec3 center,Vec3 camera,PoseStack pose,float partial){
        var mc=Minecraft.getInstance();var items=tag.getList("Items",Tag.TAG_COMPOUND);var buffers=mc.renderBuffers().bufferSource();
        if(age<HeavenlyFurnace.GATHER_TICKS){
            int n=Math.min(12,items.size());for(int i=0;i<n;i++){
                float gather=gatherProgress(age,i,n);if(gather>=1)continue;
                Vec3 position=materialPosition(origin,center,tag.getFloat("Yaw"),age,i,n);
                item(ItemStack.of(items.getCompound(i)),position.subtract(camera),.72f*(1-gather*.3f),age*.8f+i*39,pose,buffers,partial);
            }
        }else if(age<tag.getInt("Duration")-12){
            float progress=Mth.clamp((age-HeavenlyFurnace.GATHER_TICKS)/Math.max(1,tag.getInt("Duration")-HeavenlyFurnace.GATHER_TICKS),0,1);
            float scale=tag.getString("Mode").equals("refining")?.85f+progress*.65f:.4f+progress*.4f;
            item(ItemStack.of(tag.getCompound("Preview")),center.subtract(camera),scale,age*.35f,pose,buffers,partial);
        }else if(age>=tag.getInt("Duration")-12){
            float fade=Mth.clamp((tag.getInt("Duration")+24-age)/18,0,1);
            item(ItemStack.of(tag.getCompound("Preview")),center.add(0,(age-tag.getInt("Duration"))*.008,0).subtract(camera),(tag.getString("Mode").equals("refining")?1.5f:.8f)*fade,age,pose,buffers,partial);
        }
        buffers.endBatch();
    }
    private static void item(ItemStack stack,Vec3 p,float scale,float angle,PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,float partial){
        pose.pushPose();pose.translate(p.x,p.y,p.z);pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(angle*.025f)*12));pose.scale(scale,scale,scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.FIXED,0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,Minecraft.getInstance().level,0);pose.popPose();
    }
    private static void gatheringThreads(CompoundTag tag,float age,Vec3 origin,Vec3 center,Vec3 camera){
        var items=tag.getList("Items",Tag.TAG_COMPOUND);int n=Math.min(12,items.size());
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        for(int i=0;i<n;i++){
            float gather=gatherProgress(age,i,n);if(gather>=1)continue;
            double angle=i*Math.PI*2/Math.max(1,n)+age*.045;
            for(int j=0;j<24;j++)for(int k=0;k<4;k++){
                float u=(j+(k==1||k==2?1:0))/24f,v=k>=2?1:0;
                double a=angle-(1-u)*.9;Vec3 orbit=origin.add(Math.cos(a)*1.45,1.15+Math.sin(a*2)*.15,Math.sin(a)*1.45);
                Vec3 point=materialPosition(origin,center,tag.getFloat("Yaw"),age-(1-u)*8,i,n).subtract(camera);
                Vec3 side=point.cross(new Vec3(Math.cos(a),0,Math.sin(a))).normalize().scale((v-.5)*.04*u);
                b.vertex(point.x+side.x,point.y+side.y,point.z+side.z).uv(u,v).endVertex();
            }
        }BufferUploader.drawWithShader(b.end());
    }
    public static float gatherProgress(float age,int index,int count){
        float gap=(HeavenlyFurnace.GATHER_TICKS-60-44-16)/(float)Math.max(1,count-1);
        return Mth.clamp((age-60-index*gap)/44,0,1);
    }
    public static Vec3 materialPosition(Vec3 origin,Vec3 center,float yaw,float age,int index,int count){
        float t=gatherProgress(age,index,count);double a=index*Math.PI*2/Math.max(1,count)+age*.04+Math.toRadians(yaw);
        Vec3 orbit=origin.add(Math.cos(a)*1.65,1.35+Math.sin(a*2)*.2,Math.sin(a)*1.65);
        if(t<=0)return orbit;
        // Elevated cubic arc retains visible item size until it reaches the gathering sphere.
        Vec3 start=origin.add(Math.cos(a)*1.65,1.35+Math.sin(a*2)*.2,Math.sin(a)*1.65);
        Vec3 c1=start.add(0,1.7,0),c2=center.add(0,.8,0);
        double s=1-t;return start.scale(s*s*s).add(c1.scale(3*s*s*t)).add(c2.scale(3*s*t*t)).add(center.scale(t*t*t));
    }
    private static void sphere(Vec3 c,float radius){
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        int lon=40,lat=24;
        for(int y=0;y<lat;y++)for(int x=0;x<lon;x++)for(int k=0;k<4;k++){
            float u=(x+(k==1||k==2?1:0))/(float)lon,v=(y+(k>=2?1:0))/(float)lat;
            double a=u*Math.PI*2,p=v*Math.PI;
            b.vertex(c.x+radius*Math.sin(p)*Math.cos(a),c.y+radius*Math.cos(p),c.z+radius*Math.sin(p)*Math.sin(a)).uv(u,v).endVertex();
        }BufferUploader.drawWithShader(b.end());
    }
    private static void ring(Vec3 c,float radius,float angle){
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        for(int i=0;i<128;i++)for(int k=0;k<4;k++){
            float u=(i+(k==1||k==2?1:0))/128f,v=k>=2?1:0;double a=u*Math.PI*2+angle;float r=radius+(v-.5f)*.24f;
            b.vertex(c.x+Math.cos(a)*r,c.y,c.z+Math.sin(a)*r).uv(u,v).endVertex();
        }BufferUploader.drawWithShader(b.end());
    }
    private static void fuel(Vec3 start,Vec3 end,float age){
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        Vec3 tangent=end.subtract(start).normalize(),side=tangent.cross(new Vec3(0,1,0)).normalize();if(side.lengthSqr()<.01)side=new Vec3(1,0,0);
        Vec3 up=tangent.cross(side).normalize();double reach=start.distanceTo(end);
        for(int strand=0;strand<5;strand++)for(int j=0;j<80;j++)for(int k=0;k<4;k++){
            float u=(j+(k==1||k==2?1:0))/80f,v=k>=2?1:0;double t=1-u,fan=Math.sin(u*Math.PI);
            Vec3 p=start.scale(t*t*t).add(start.add(0,Math.min(2,reach*.24),0).scale(3*t*t*u)).add(end.add(0,-Math.min(2.5,reach*.3),0).scale(3*t*u*u)).add(end.scale(u*u*u));
            double twist=u*11-age*.035+strand*Math.PI*2/5,radius=(strand==0?.025:.16)*fan*(.7+.3*Math.sin(u*17-age*.025));
            p=p.add(side.scale(Math.cos(twist)*radius)).add(up.scale(Math.sin(twist)*radius));
            Vec3 across=side.scale(Math.cos(twist)).add(up.scale(Math.sin(twist)));
            double width=strand==0?.10:.045; p=p.add(across.scale((v-.5)*width*(.5+fan)));
            b.vertex(p.x,p.y,p.z).uv(u,v).endVertex();
        }BufferUploader.drawWithShader(b.end());
    }
    private static void yinYang(Vec3 center,float radius,float age){
        var b=Tesselator.getInstance().getBuilder();b.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
        for(int stream=0;stream<2;stream++)for(int i=0;i<96;i++)for(int k=0;k<4;k++){
            float u=(i+(k==1||k==2?1:0))/96f,v=k>=2?1:0;double a=u*Math.PI*3.4+age*.018+stream*Math.PI;
            float envelope=Mth.sin(u*(float)Math.PI),r=radius*(.75f+.25f*envelope);
            double width=(v-.5)*.2*(.3+envelope);double x=Math.cos(a)*(r+width),z=Math.sin(a)*(r+width),y=(u-.5)*2.3*radius;
            b.vertex(center.x+x,center.y+y,center.z+z).uv(u+stream,v).endVertex();
        }BufferUploader.drawWithShader(b.end());
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent public static void register(RegisterShadersEvent e)throws java.io.IOException{
            HeavenlyFurnaceGpu.release();
            releaseMaterials();
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"heavenly_furnace"),DefaultVertexFormat.POSITION_TEX),s->shader=s);
        }
    }
    private HeavenlyFurnaceVisuals(){}
}
