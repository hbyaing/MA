package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.io.IOException;

/** A true sphere, composed after water without writing holes into its depth buffer. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SpiritShieldRenderer {
    private static Matrix4f clip;
    private static net.minecraft.world.phys.Vec3 camera;
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,Integer> EFFECT_DURATIONS=new java.util.WeakHashMap<>();
    private static final float[][] UNIT=mesh();
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,Float> PEAK_POOL=new java.util.WeakHashMap<>();
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,Double> HIT_TIMES=new java.util.WeakHashMap<>();
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,net.minecraft.world.phys.Vec3> HIT_DIRECTIONS=new java.util.WeakHashMap<>();
    public static void impact(int entityId,net.minecraft.world.phys.Vec3 direction) {
        var level=Minecraft.getInstance().level;
        if(level==null || !(level.getEntity(entityId) instanceof net.minecraft.client.player.AbstractClientPlayer player))return;
        HIT_TIMES.put(player,(double)level.getGameTime());
        HIT_DIRECTIONS.put(player,direction);
    }
    private static final MultiBufferSource.BufferSource BUFFERS=MultiBufferSource.immediate(new BufferBuilder(32768));
    private static float[] point(int row,int column) {
        double latitude=Math.PI*row/24,longitude=Math.PI*2*column/48;
        return new float[]{(float)(Math.sin(latitude)*Math.cos(longitude)),(float)Math.cos(latitude),
                (float)(Math.sin(latitude)*Math.sin(longitude))};
    }
    private static float[][] mesh() {
        var vertices=new java.util.ArrayList<float[]>();
        for(int y=0;y<24;y++)for(int x=0;x<48;x++) {
            var a=point(y,x);var b=point(y+1,x);var c=point(y+1,x+1);var d=point(y,x+1);
            java.util.Collections.addAll(vertices,a,b,c,a,c,d);
        }
        return vertices.toArray(float[][]::new);
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            clip=new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix())
                    .mul(event.getPoseStack().last().pose());
            camera=event.getCamera().getPosition();return;
        }
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL || clip==null)return;
        var matrix=clip;clip=null;
        var mc=Minecraft.getInstance();
        if(mc.level==null || Material.shader==null)return;
        float partial=event.getPartialTick();
        Material.shader.safeGetUniform("ClipMat").set(matrix);
        Material.shader.safeGetUniform("Time").set((mc.level.getGameTime()%24000+partial)/20F);
        // Far shields first for ordinary alpha compositing.
        var players=new java.util.ArrayList<>(mc.level.players());
        players.sort(java.util.Comparator.comparingDouble((net.minecraft.client.player.AbstractClientPlayer p)->p.distanceToSqr(camera)).reversed());
        for(var player:players) {
            var effect=player.getEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get());
            double now=mc.level.getGameTime()+partial;
            float absorption=player.getAbsorptionAmount();
            double hitAt=HIT_TIMES.getOrDefault(player,-1000D);
            boolean active=effect!=null&&absorption>0;
            if(effect!=null){
                if(effect.getDuration()>EFFECT_DURATIONS.getOrDefault(player,effect.getDuration())+10)PEAK_POOL.put(player,absorption);
                EFFECT_DURATIONS.put(player,effect.getDuration());
                PEAK_POOL.put(player,Math.max(absorption,PEAK_POOL.getOrDefault(player,absorption)));
            }else EFFECT_DURATIONS.remove(player);
            float integrity=absorption/Math.max(1,PEAK_POOL.getOrDefault(player,absorption));
            if(effect==null)PEAK_POOL.remove(player);
            float hitAge=Mth.clamp((float)((now-hitAt)/24.0),0,1);
            float impact=1-hitAge;
            if((!active&&impact<=0) || !player.isAlive() || player.isSpectator()
                    || player.distanceToSqr(camera)>64*64)continue;
            float fade=active?Mth.clamp((effect.getDuration()-partial)/6F,0,1):impact;
            Material.shader.safeGetUniform("Strength").set(fade);
            Material.shader.safeGetUniform("Integrity").set(Mth.clamp(integrity,0,1));
            Material.shader.safeGetUniform("Hit").set(impact);
            Material.shader.safeGetUniform("HitAge").set(hitAge);
            var hitDirection=HIT_DIRECTIONS.getOrDefault(player,new net.minecraft.world.phys.Vec3(0,1,0));
            Material.shader.safeGetUniform("HitDirection").set((float)hitDirection.x,(float)hitDirection.y,(float)hitDirection.z);
            double x=Mth.lerp(partial,player.xOld,player.getX())-camera.x;
            double y=Mth.lerp(partial,player.yOld,player.getY())+player.getBbHeight()*.5-camera.y;
            double z=Mth.lerp(partial,player.zOld,player.getZ())-camera.z;
            float radius=Math.max(1.15F,player.getBbHeight()*.64F);
            var vertices=BUFFERS.getBuffer(Material.TYPE);
            for(var v:UNIT)vertices.vertex(x+v[0]*radius,y+v[1]*radius,z+v[2]*radius)
                    .color(255,255,255,255).normal(v[0],v[1],v[2]).endVertex();
            BUFFERS.endBatch(Material.TYPE);
        }
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Material extends RenderType {
        static ShaderInstance shader;
        static final RenderType TYPE=create("spirit_shield_sphere",DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.TRIANGLES,32768,false,false,CompositeState.builder()
                .setShaderState(new ShaderStateShard(()->shader)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                .setOutputState(MAIN_TARGET).createCompositeState(false));
        private Material(String n,VertexFormat f,VertexFormat.Mode m,int size,boolean c,boolean s,Runnable a,Runnable b){super(n,f,m,size,c,s,a,b);}
        @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"spirit_shield_sphere"),
                    DefaultVertexFormat.POSITION_COLOR_NORMAL),s->shader=s);
        }
    }
    private SpiritShieldRenderer() {}
}
