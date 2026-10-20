package cn.blockforge.generated.generatedmod.client.anvillight;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.client.TrailShaderBridge;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import java.util.*;
import java.io.IOException;

/** Fullbright rune pixels on every baked face, including shader-pack post compositing. */
final class AnvilRuneEmission{
    static ShaderInstance shader;
    private static final MultiBufferSource.BufferSource BUFFERS=MultiBufferSource.immediate(new BufferBuilder(262144));
    private static final BufferBuilder POST_BUILDER=new BufferBuilder(262144);
    private static final Map<BakedModel,List<BakedQuad>> QUADS=new WeakHashMap<>();
    private static BufferBuilder.RenderedBuffer pending;
    private static Matrix4f projection;
    private static VertexBuffer postBuffer;
    private static int pendingQuads,draws;
    static void register(RegisterShadersEvent e){try{e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("wuxingxiantu","anvil_rune_emission"),DefaultVertexFormat.NEW_ENTITY),s->{shader=s;QUADS.clear();clear();});}catch(IOException error){throw new RuntimeException(error);}}
    static void clear(){if(pending!=null)pending.release();pending=null;pendingQuads=0;QUADS.clear();if(postBuffer!=null){postBuffer.close();postBuffer=null;}}
    static void frameStart(){draws=0;}
    static int draws(){return draws;}
    private static int vertices(RenderLevelStageEvent e,List<Vec3> lights,VertexConsumer target){
        var mc=Minecraft.getInstance();var pose=e.getPoseStack();var camera=e.getCamera().getPosition();
        int count=0;
        for(var light:lights){var pos=BlockPos.containing(light);var state=mc.level.getBlockState(pos);if(!state.is(WuXingBlocks.XUANLING_ANVIL.get()))continue;
            var model=mc.getBlockRenderer().getBlockModel(state);
            var quads=QUADS.computeIfAbsent(model,m->{var result=new ArrayList<BakedQuad>();var random=RandomSource.create(42);for(int face=-1;face<6;face++){random.setSeed(42);result.addAll(m.getQuads(state,face<0?null:Direction.values()[face],random));}return result;});
            pose.pushPose();pose.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
            for(var quad:quads){var n=quad.getDirection().getNormal();pose.pushPose();pose.translate(n.getX()*.0005,n.getY()*.0005,n.getZ()*.0005);target.putBulkData(pose.last(),quad,1,1,1,0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}pose.popPose();count+=quads.size();
        }
        return count;
    }
    static void render(RenderLevelStageEvent e,List<Vec3> lights){
        if(shader==null||Minecraft.getInstance().level==null||lights.isEmpty()||TrailShaderBridge.shaderPackDetected()||TrailShaderBridge.shadowPass())return;
        shader.safeGetUniform("SceneDepthEnabled").set(0F);int count=vertices(e,lights,BUFFERS.getBuffer(Material.TYPE));BUFFERS.endBatch(Material.TYPE);if(count>0)draws++;
    }
    static void prepare(RenderLevelStageEvent e,List<Vec3> lights){
        if(pending!=null){pending.release();pending=null;}if(shader==null||lights.isEmpty()||!TrailShaderBridge.shaderPackDetected()||TrailShaderBridge.shadowPass()||!TrailShaderBridge.captureDepth())return;
        POST_BUILDER.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.NEW_ENTITY);pendingQuads=vertices(e,lights,POST_BUILDER);pending=POST_BUILDER.end();projection=new Matrix4f(e.getProjectionMatrix());
    }
    static void post(){
        if(pending==null)return;var mesh=pending;pending=null;int depth=TrailShaderBridge.capturedDepthTexture();if(depth<0){mesh.release();return;}
        try(var state=new cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer.State()){
            if(postBuffer==null)postBuffer=new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();com.mojang.blaze3d.systems.RenderSystem.depthMask(false);com.mojang.blaze3d.systems.RenderSystem.disableCull();
            shader.setSampler("Sampler0",Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getId());shader.setSampler("SceneDepth",depth);shader.safeGetUniform("SceneDepthEnabled").set(1F);shader.safeGetUniform("ModelViewMat").set(new Matrix4f());shader.safeGetUniform("ProjMat").set(projection);shader.safeGetUniform("ColorModulator").set(1F,1F,1F,1F);
            postBuffer.bind();postBuffer.upload(mesh);shader.apply();postBuffer.draw();shader.clear();VertexBuffer.unbind();shader.safeGetUniform("SceneDepthEnabled").set(0F);if(pendingQuads>0)draws++;
        }
    }
    static boolean hasPending(){return pending!=null;}
    private static final class Material extends RenderType{
        private Material(String n,VertexFormat f,VertexFormat.Mode m,int size,boolean a,boolean b,Runnable setup,Runnable clear){super(n,f,m,size,a,b,setup,clear);}
        static final RenderType TYPE=create("anvil_rune_emission",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,262144,false,false,CompositeState.builder().setShaderState(new ShaderStateShard(()->shader)).setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS,false,false)).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(CULL).createCompositeState(false));
    }
}
