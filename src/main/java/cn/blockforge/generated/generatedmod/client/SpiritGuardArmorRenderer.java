package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.SpiritEnchantment;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Reuses actual armor vertices and UVs instead of drawing a second player skin. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SpiritGuardArmorRenderer {
    private static MultiBufferSource.BufferSource EFFECT_BUFFERS;
    private static final ReusableNativeMesh EFFECT_MESH=new ReusableNativeMesh(16384);
    private static final List<Draw> WORLD_DRAWS=new ArrayList<>();
    private static boolean worldRendering;
    private static final ReusableNativeMesh POST_MESH=new ReusableNativeMesh(16384);
    private static Object worldIdentity;
    public static void release(){
        if(!RenderSystem.isOnRenderThread()){RenderSystem.recordRenderCall(SpiritGuardArmorRenderer::release);return;}
        WORLD_DRAWS.clear();worldRendering=false;POST_MESH.close();EFFECT_MESH.close();EFFECT_BUFFERS=null;
    }
    @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){release();worldIdentity=null;}
    @SubscribeEvent public static void shutdown(net.minecraftforge.event.GameShuttingDownEvent event){release();}
    static int worldDraws,guiDraws,capturedVertices,nativeFoilVertices;
    static String bufferClass="none";

    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST) public static void frame(TickEvent.RenderTickEvent event){
        if(event.phase==TickEvent.Phase.END){drawPost();return;}
        if(event.phase==TickEvent.Phase.START){
            var level=Minecraft.getInstance().level;if(worldIdentity!=level){release();worldIdentity=level;}
            WORLD_DRAWS.clear();worldRendering=false;
            worldDraws=guiDraws=capturedVertices=nativeFoilVertices=0;
        }
    }
    @SubscribeEvent public static void world(RenderLevelStageEvent event){
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_SKY)worldRendering=true;
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        worldRendering=false;
        if(WORLD_DRAWS.isEmpty())return;
        // Iris owns the global projection during its shadow pass; use the real
        // level-stage projection once the scene has finished rendering.
        if(TrailShaderBridge.shaderPackDetected())for(int i=0;i<WORLD_DRAWS.size();i++){
            var d=WORLD_DRAWS.get(i);var clip=new Matrix4f(event.getProjectionMatrix()).scale(.99975586F);
            WORLD_DRAWS.set(i,new Draw(d.capture,clip,d.impact,d.aura,d.seconds,d.hitAge,true));
        }
        if(TrailShaderBridge.shaderPackDetected()){TrailShaderBridge.captureDepth();return;}
        // Oculus owns the entity batch; its endBatch(RenderType) is a no-op.
        // At this stage base armor has reached the world depth target.
        try(var saved=new MeteorBloomRenderer.State()){
            for(var draw:WORLD_DRAWS){draw.capture.draw(draw);worldDraws++;}
        }finally{WORLD_DRAWS.clear();}
    }
    @SubscribeEvent public static void post(net.minecraftforge.client.event.RenderGuiEvent.Pre event){
        if(WORLD_DRAWS.isEmpty())return;
        event.getGuiGraphics().flush();
        drawPost();
    }
    private static void drawPost(){
        if(WORLD_DRAWS.isEmpty())return;
        try(var saved=new MeteorBloomRenderer.State()){
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            for(var draw:WORLD_DRAWS){draw.capture.draw(draw);worldDraws++;}
        }finally{WORLD_DRAWS.clear();}
    }
    private record Draw(Capture capture,Matrix4f clip,Contact impact,boolean aura,float seconds,float hitAge,boolean world){}

    static boolean canRender(AbstractClientPlayer player,float partial) {
        if(TrailShaderBridge.shadowPass())return false;
        var camera=Minecraft.getInstance().getCameraEntity();
        if(!HudConfig.SPIRIT_GUARD_VISUAL.get()||SpiritGuardArmorRenderType.shader()==null
                ||player.isSpectator()||camera==null||camera.distanceToSqr(player)>48*48)return false;
        boolean enchanted=false;
        for(var slot:EquipmentSlot.values())if(slot.getType()==EquipmentSlot.Type.ARMOR
                &&cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.guardLevel(player.getItemBySlot(slot))>0){
            enchanted=true;break;
        }
        if(!enchanted)return false;
        var data=GeneratedMod.data(player);
        return data!=null&&(data.wuxingxiantu$getAura()>0||SpiritGuardLayer.hitAge(player,partial)>=0);
    }

    static final class Capture implements MultiBufferSource {
        private final MultiBufferSource original;
        private final AbstractClientPlayer player;
        private final List<Piece> pieces=new ArrayList<>();
        private final LinkedHashSet<RenderType> baseTypes=new LinkedHashSet<>();
        private Piece current;

        Capture(MultiBufferSource original,AbstractClientPlayer player){this.original=original;this.player=player;bufferClass=original.getClass().getName();}

        void begin(ItemStack stack,EquipmentSlot slot,boolean vanillaUv,boolean hidden){
            int tier=hidden?0:cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.guardLevel(stack);
            current=new Piece(tier,vanillaUv);pieces.add(current);
        }
        void texture(ResourceLocation texture){if(current!=null)current.texture=texture;}

        @Override public VertexConsumer getBuffer(RenderType type){
            // Preserve native foil; cloud glyphs and contact ripples are an additional pass.
            baseTypes.add(type);
            var target=original.getBuffer(type);
            if(current!=null&&current.tier>0
                    &&(type==RenderType.armorEntityGlint()||type==RenderType.armorGlint()))
                return new RecordedVertices(target,null);
            if(current!=null&&current.tier>0&&!current.captured&&current.texture!=null
                    &&type==RenderType.armorCutoutNoCull(current.texture)){
                current.captured=true;
                return new RecordedVertices(target,current.vertices);
            }
            return target;
        }

        void finish(PoseStack pose,float partial){
            if(pieces.stream().noneMatch(piece->!piece.vertices.isEmpty()))return;
            float hitAge=SpiritGuardLayer.hitAge(player,partial);
            var impact=hitAge>=0?contact(pose,partial):new Contact(new Vector3f(),new Vector3f(0,0,1));
            var data=GeneratedMod.data(player);
            boolean aura=data!=null&&data.wuxingxiantu$getAura()>0;
            // 4320 seconds contain whole cycles for all four levels (4.8/4/3.6/3 seconds).
            float seconds=(player.level().getGameTime()%86400+partial)/20F;
            // ArmorCutoutNoCull uses vanilla's 0.99975586 view-scale layering.
            // Match that exact depth transform; a large clip-space bias can expose
            // a high-tier leggings body through a lower-tier chestplate.
            var clip=new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix()).scale(.99975586F);
            boolean perspective=TrailShaderBridge.shaderPackDetected()?Minecraft.getInstance().screen==null||worldRendering:Math.abs(RenderSystem.getProjectionMatrix().m33())<.5F;
            var draw=new Draw(this,clip,impact,aura,seconds,hitAge,perspective);
            capturedVertices+=pieces.stream().mapToInt(piece->piece.vertices.size()).sum();
            // Iris may skip Forge's AFTER_SKY and may render the armor in a shadow
            // pass first. Classify the actual projection, not that optional stage.
            if(perspective){
                if(WORLD_DRAWS.size()<128)WORLD_DRAWS.add(draw);
                return;
            }
            if(Minecraft.getInstance().screen==null||org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER_BINDING)!=Minecraft.getInstance().getMainRenderTarget().frameBufferId)return; // Shadow camera, not an inventory preview.
            // Inventory previews use an ordinary immediate source, independent of world batches.
            if(original instanceof MultiBufferSource.BufferSource source)
                for(var type:baseTypes)source.endBatch(type);
            try(var saved=new MeteorBloomRenderer.State()){draw(draw);guiDraws++;}
        }

        private void draw(Draw draw){
            for(var piece:pieces){
                if(piece.vertices.isEmpty()||piece.texture==null)continue;
                int level=Mth.clamp(piece.tier,1,4);
                var type=SpiritGuardArmorRenderType.forTexture(piece.texture,level);
                var shader=SpiritGuardArmorRenderType.shader();
                int depth=draw.world&&TrailShaderBridge.shaderPackDetected()?TrailShaderBridge.capturedDepthTexture():-1;
                shader.safeGetUniform("SceneDepthEnabled").set(depth>=0?1F:0F);
                if(depth>=0)shader.setSampler("SceneDepth",depth);
                shader.safeGetUniform("GuardClipMat").set(draw.clip);
                shader.safeGetUniform("EffectTime").set(draw.seconds);
                shader.safeGetUniform("HitAge").set(draw.hitAge);
                shader.safeGetUniform("HitPosition").set(draw.impact.position.x,draw.impact.position.y,draw.impact.position.z);
                shader.safeGetUniform("HitNormal").set(draw.impact.normal.x,draw.impact.normal.y,draw.impact.normal.z);
                shader.safeGetUniform("GuardLevel").set((float)level);
                shader.safeGetUniform("Intensity").set(switch(level){case 1->.85F;case 2->.89F;case 3->.94F;default->1.02F;});
                shader.safeGetUniform("CloudEnabled").set(piece.vanillaUv&&draw.aura?1F:0F);
                shader.safeGetUniform("ColorModulator").set(1F,1F,1F,1F);
                if(depth>=0){
                    // Bypass Iris' entity RenderType program substitution after its final composite.
                    var mc=Minecraft.getInstance();
                    shader.setSampler("Sampler0",mc.getTextureManager().getTexture(SpiritGuardArmorRenderType.mask(level)).getId());
                    shader.setSampler("Sampler1",mc.getTextureManager().getTexture(piece.texture).getId());
                    var builder=POST_MESH.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY);
                    for(var vertex:piece.vertices)vertex.write(builder);
                    try{
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA,org.lwjgl.opengl.GL11.GL_ONE);
                        com.mojang.blaze3d.systems.RenderSystem.disableCull();com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                        POST_MESH.draw(shader);
                    }finally{
                        if(builder.building())builder.end().release();
                    }
                    continue;
                }
                if(EFFECT_BUFFERS==null)EFFECT_BUFFERS=MultiBufferSource.immediate(EFFECT_MESH.buffer());
                var output=EFFECT_BUFFERS.getBuffer(type);
                for(var vertex:piece.vertices)vertex.write(output);
                // Per-piece uniforms are consumed now, never after another player changes them.
                EFFECT_BUFFERS.endBatch(type);
            }
        }

        private Contact contact(PoseStack pose,float partial){
            var direction=SpiritGuardLayer.hitDirection(player);
            float yaw=Mth.rotLerp(partial,player.yBodyRotO,player.yBodyRot)*Mth.DEG_TO_RAD;
            float c=Mth.cos(yaw),s=Mth.sin(yaw);
            var local=new Vector3f((float)(direction.x*c+direction.z*s),-(float)direction.y,
                    (float)(direction.x*s-direction.z*c));
            var normal=pose.last().normal().transform(local).normalize();
            var origin=pose.last().pose().transformPosition(new Vector3f(0,.5F,0));
            Contact result=null;float farthest=-1;
            for(var piece:pieces)for(int i=0;i+3<piece.vertices.size();i+=4){
                var a=piece.vertices.get(i);var b=piece.vertices.get(i+1);
                var d=piece.vertices.get(i+3);var third=piece.vertices.get(i+2);
                var faceNormal=new Vector3f(a.nx,a.ny,a.nz);
                if(faceNormal.dot(normal)<.1F)continue;
                float distance=Math.max(rayTriangle(origin,normal,a,b,third),rayTriangle(origin,normal,a,third,d));
                if(distance>farthest&&distance>=0){
                    farthest=distance;result=new Contact(new Vector3f(normal).mul(distance).add(origin),faceNormal.normalize());
                }
            }
            if(result!=null)return result;
            // Sparse armor can miss the torso ray. Select the nearest point on a facing real quad.
            var expected=new Vector3f(normal).mul(.35F).add(origin);
            float closest=Float.POSITIVE_INFINITY;
            for(var piece:pieces)for(int i=0;i+3<piece.vertices.size();i+=4){
                var a=piece.vertices.get(i);var b=piece.vertices.get(i+1);var d=piece.vertices.get(i+3);
                var faceNormal=new Vector3f(a.nx,a.ny,a.nz);
                if(faceNormal.dot(normal)<.1F)continue;
                var av=a.position();var u=b.position().sub(av);var v=d.position().sub(av);
                float uLength=u.lengthSquared(),vLength=v.lengthSquared();
                if(uLength<.000001F||vLength<.000001F)continue;
                var offset=new Vector3f(expected).sub(av);
                var point=new Vector3f(av).fma(Mth.clamp(offset.dot(u)/uLength,0,1),u)
                        .fma(Mth.clamp(offset.dot(v)/vLength,0,1),v);
                float distance=point.distanceSquared(expected);
                if(distance<closest){closest=distance;result=new Contact(point,faceNormal.normalize());}
            }
            return result!=null?result:new Contact(expected,normal);
        }
    }

    private static float rayTriangle(Vector3f origin,Vector3f direction,Vertex a,Vertex b,Vertex c){
        var edge1=b.position().sub(a.position());var edge2=c.position().sub(a.position());
        var p=new Vector3f(direction).cross(edge2);
        float determinant=edge1.dot(p);if(Math.abs(determinant)<.000001F)return -1;
        float inverse=1/determinant;var t=new Vector3f(origin).sub(a.position());
        float u=t.dot(p)*inverse;if(u<0||u>1)return -1;
        var q=new Vector3f(t).cross(edge1);float v=direction.dot(q)*inverse;
        if(v<0||u+v>1)return -1;
        float distance=edge2.dot(q)*inverse;return distance>=0?distance:-1;
    }
    private record Contact(Vector3f position,Vector3f normal){}
    private static final class Piece {
        final int tier;final boolean vanillaUv;final List<Vertex> vertices=new ArrayList<>(144);
        ResourceLocation texture;boolean captured;
        Piece(int tier,boolean vanillaUv){this.tier=tier;this.vanillaUv=vanillaUv;}
    }
    private record Vertex(double x,double y,double z,int r,int g,int b,int a,float u,float v,
            int overlayU,int overlayV,int lightU,int lightV,float nx,float ny,float nz){
        Vector3f position(){return new Vector3f((float)x,(float)y,(float)z);}
        void write(VertexConsumer output){output.vertex(x,y,z).color(r,g,b,a).uv(u,v).overlayCoords(overlayU,overlayV)
                .uv2(lightU,lightV).normal(nx,ny,nz).endVertex();}
    }
    private static final class RecordedVertices implements VertexConsumer {
        final VertexConsumer original;final List<Vertex> vertices;
        double x,y,z;int r=255,g=255,b=255,a=255,overlayU,overlayV,lightU,lightV;float u,v,nx,ny,nz;
        RecordedVertices(VertexConsumer original,List<Vertex> vertices){this.original=original;this.vertices=vertices;}
        public VertexConsumer vertex(double x,double y,double z){this.x=x;this.y=y;this.z=z;original.vertex(x,y,z);return this;}
        public VertexConsumer color(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;original.color(r,g,b,a);return this;}
        public VertexConsumer uv(float u,float v){this.u=u;this.v=v;original.uv(u,v);return this;}
        public VertexConsumer overlayCoords(int u,int v){overlayU=u;overlayV=v;original.overlayCoords(u,v);return this;}
        public VertexConsumer uv2(int u,int v){lightU=u;lightV=v;original.uv2(u,v);return this;}
        public VertexConsumer normal(float x,float y,float z){nx=x;ny=y;nz=z;original.normal(x,y,z);return this;}
        public void endVertex(){
            // Custom models cannot allocate an unbounded per-frame copy through this optional overlay.
            if(vertices==null)nativeFoilVertices++;
            else if(vertices.size()<65536)vertices.add(new Vertex(x,y,z,r,g,b,a,u,v,overlayU,overlayV,lightU,lightV,nx,ny,nz));
            original.endVertex();
        }
        public void defaultColor(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;original.defaultColor(r,g,b,a);}
        public void unsetDefaultColor(){original.unsetDefaultColor();}
    }
    private SpiritGuardArmorRenderer(){}
}
