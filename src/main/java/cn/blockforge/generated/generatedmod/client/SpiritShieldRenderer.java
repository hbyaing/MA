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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry.*;

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
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,ShatterBurst> SHATTERS=new java.util.WeakHashMap<>();
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,net.minecraft.world.effect.MobEffectInstance> SHATTER_START_EFFECTS=new java.util.WeakHashMap<>();
    private static final java.util.Map<net.minecraft.client.player.AbstractClientPlayer,CompletableFuture<List<Patch>>> PENDING_FRACTURES=new java.util.WeakHashMap<>();
    // Voronoi clipping is CPU work only. Never run it, or a rejected job, on the render thread.
    private static final ThreadPoolExecutor FRACTURE_WORKER=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(16),task->{
                var thread=new Thread(task,"wuxingxiantu-shield-fracture");thread.setDaemon(true);return thread;
            },new ThreadPoolExecutor.AbortPolicy());
    private static final float SHATTER_DURATION=2.4F;
    public record Spark(Vec origin,Vec velocity,double duration) {
        Vec position(double age){return origin.add(velocity.mul(age)).add(new Vec(0,-.9*age*age,0));}
        double alpha(double age){return Math.max(0,1-age/duration);}
    }
    public record ShatterBurst(double startTick,net.minecraft.world.phys.Vec3 origin,float radius,List<Patch> patches,List<Spark> sparks) {}
    /** Only a server-confirmed follow-up contact can break a depleted shield. */
    public static void shatter(int entityId,net.minecraft.world.phys.Vec3 direction) {
        var mc=Minecraft.getInstance();var level=mc.level;
        if(level==null||!(level.getEntity(entityId) instanceof net.minecraft.client.player.AbstractClientPlayer player)
                ||SHATTERS.containsKey(player)||!Double.isFinite(direction.lengthSqr())||direction.lengthSqr()<.001)return;
        var hit=direction.normalize();
        var point=new Vec(hit.x,hit.y,hit.z);
        long seed=level.getGameTime()*31+player.getUUID().hashCode();
        var sparks=new java.util.ArrayList<Spark>();var random=new Random(seed);
        for(int i=0;i<64;i++){
            Vec source=i<30?point:randomDirection(random);
            var velocity=source.mul(.75+random.nextDouble()).add(randomDirection(random).mul(.8)).mul(1.8);
            sparks.add(new Spark(source,velocity,.65+random.nextDouble()*.8));
        }
        var pending=new ShatterBurst(level.getGameTime(),player.position().add(0,player.getBbHeight()*.5,0),
                Math.max(1.15F,player.getBbHeight()*.64F),List.of(),List.copyOf(sparks));
        SHATTERS.put(player,pending);
        SHATTER_START_EFFECTS.put(player,player.getEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()));
        PEAK_POOL.remove(player);EFFECT_DURATIONS.remove(player);
        try{
            var future=CompletableFuture.supplyAsync(()->ShieldFractureGeometry.fracture(
                    ShieldFractureGeometry.unitSphereMesh(),point,110,seed,2.8),FRACTURE_WORKER);
            PENDING_FRACTURES.put(player,future);
            future.whenComplete((patches,error)->mc.execute(()->{
                // A completion belongs to one contact, one entity instance and one client world.
                if(SHATTERS.get(player)!=pending)return;
                if(mc.level!=level){clearShatter(player);return;}
                PENDING_FRACTURES.remove(player);
                var effect=player.getEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get());
                boolean recast=effect!=null&&player.getAbsorptionAmount()>0&&effect!=SHATTER_START_EFFECTS.get(player);
                if(recast||player.isRemoved()||!player.isAlive()
                        ||level.getGameTime()-pending.startTick()>=SHATTER_DURATION*20){clearShatter(player);return;}
                if(error!=null){
                    com.mojang.logging.LogUtils.getLogger().warn("Cannot prepare spirit shield fragments",error);
                    clearShatter(player);return;
                }
                SHATTERS.put(player,new ShatterBurst(pending.startTick(),pending.origin(),pending.radius(),patches,pending.sparks()));
            }));
        }catch(RejectedExecutionException busy){
            // Bound both CPU concurrency and retained geometry when many shields break together.
            com.mojang.logging.LogUtils.getLogger().warn("Spirit shield fracture queue is full; dropping excess visual workload");
            clearShatter(player);
        }
    }
    private static void clearShatter(net.minecraft.client.player.AbstractClientPlayer player){
        var future=PENDING_FRACTURES.remove(player);
        SHATTERS.remove(player);SHATTER_START_EFFECTS.remove(player);
        if(future!=null)future.cancel(false);
    }
    private static void clearWorldState(){
        var futures=List.copyOf(PENDING_FRACTURES.values());
        PENDING_FRACTURES.clear();SHATTERS.clear();SHATTER_START_EFFECTS.clear();
        PEAK_POOL.clear();EFFECT_DURATIONS.clear();HIT_TIMES.clear();HIT_DIRECTIONS.clear();clip=null;camera=null;
        for(var future:futures)future.cancel(false);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){clearWorldState();}
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){
        if(event.getLevel().isClientSide())clearWorldState();
    }
    private static Vec randomDirection(Random random){return new Vec(random.nextDouble()*2-1,random.nextDouble()*2-1,random.nextDouble()*2-1).unit();}
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
            var burst=SHATTERS.get(player);
            if(burst!=null&&effect!=null&&absorption>0&&effect!=SHATTER_START_EFFECTS.get(player)){
                // A new cast immediately replaces the spent shell and its old fragments.
                clearShatter(player);PEAK_POOL.put(player,absorption);burst=null;
            }
            if(burst!=null){
                float age=(float)((now-burst.startTick())/20.0);
                if(age>=SHATTER_DURATION)clearShatter(player);
                else if(player.isAlive()&&!player.isSpectator()&&burst.origin().distanceToSqr(camera)<80*80){
                    if(burst.patches().isEmpty())drawPendingShell(burst);
                    else drawShatter(burst,Math.max(0,age),matrix,(float)((mc.level.getGameTime()%24000+partial)/20.0));
                }
                continue; // Never draw the complete sphere on top of its fragments.
            }
            boolean active=effect!=null;
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
    /** Preserve the spent shell at its frozen origin during the short asynchronous preparation. */
    private static void drawPendingShell(ShatterBurst burst){
        Material.shader.safeGetUniform("Strength").set(1F);Material.shader.safeGetUniform("Integrity").set(0F);
        Material.shader.safeGetUniform("Hit").set(0F);Material.shader.safeGetUniform("HitAge").set(1F);
        var vertices=BUFFERS.getBuffer(Material.TYPE);
        for(var v:UNIT)vertices.vertex(burst.origin().x-camera.x+v[0]*burst.radius(),
                burst.origin().y-camera.y+v[1]*burst.radius(),burst.origin().z-camera.z+v[2]*burst.radius())
                .color(255,255,255,255).normal(v[0],v[1],v[2]).endVertex();
        BUFFERS.endBatch(Material.TYPE);
    }
    private static void drawShatter(ShatterBurst burst,float age,Matrix4f matrix,float time){
        var shader=Material.shardShader;if(shader==null)return;
        shader.safeGetUniform("ClipMat").set(matrix);shader.safeGetUniform("Time").set(time);
        shader.safeGetUniform("ShatterAge").set(age);shader.safeGetUniform("ShatterDuration").set(SHATTER_DURATION);
        shader.safeGetUniform("ShatterFlash").set(1.8F);
        Vec offset=new Vec(burst.origin().x-camera.x,burst.origin().y-camera.y,burst.origin().z-camera.z);
        var sorted=burst.patches().stream().sorted(java.util.Comparator.comparingDouble((Patch patch)->{
            var position=patch.transform(patch.center(),age,3,1.8,1).mul(burst.radius()).add(offset);
            return position.dot(position);
        }).reversed()).toList();
        shader.safeGetUniform("Mode").set(0F);
        var out=BUFFERS.getBuffer(Material.SHARDS);
        for(var patch:sorted)for(var triangle:patch.triangles())for(var vertex:triangle.vertices()){
            var position=patch.transform(vertex.pos(),age,3,1.8,1).mul(burst.radius()).add(offset);
            emitShard(out,position,patch.normal(vertex.normal(),age,3),vertex.pos(),vertex.u(),vertex.v(),1);
        }
        BUFFERS.endBatch(Material.SHARDS);
        shader.safeGetUniform("Mode").set(1F);
        out=BUFFERS.getBuffer(Material.SHARDS);
        for(var patch:sorted){
            float crack=(float)Mth.smoothstep(Mth.clamp((age-patch.delay()+.045)/.045,0,1));
            if(crack<=0)continue;
            for(var edge:patch.edges()){
                var a=patch.transform(edge.a().pos(),age,3,1.8,1).mul(burst.radius()).add(offset);
                var b=patch.transform(edge.b().pos(),age,3,1.8,1).mul(burst.radius()).add(offset);
                var side=b.sub(a).cross(a.add(b).mul(.5)).unit().mul(.0045*burst.radius());
                var normal=patch.normal(edge.a().normal(),age,3);
                emitTrail(out,a,b,side,normal,crack);
            }
        }
        BUFFERS.endBatch(Material.SHARDS);
        shader.safeGetUniform("Mode").set(2F);
        out=BUFFERS.getBuffer(Material.SHARDS);
        for(var spark:burst.sparks()){
            double alpha=spark.alpha(age);if(alpha<=0)continue;
            var a=spark.position(age).mul(burst.radius()).add(offset);
            var b=spark.position(Math.max(0,age-.035)).mul(burst.radius()).add(offset);
            if(a.sub(b).length()<.0001)b=a.sub(spark.velocity().unit().mul(.03));
            var side=a.sub(b).cross(a.add(b).mul(.5)).unit().mul(.005+alpha*.004);
            emitTrail(out,a,b,side,new Vec(0,0,1),alpha);
        }
        BUFFERS.endBatch(Material.SHARDS);
    }
    private static void emitTrail(VertexConsumer out,Vec a,Vec b,Vec side,Vec normal,double alpha){
        var source=new Vec(0,0,1);
        emitShard(out,a.add(side),normal,source,.5,.5,alpha);emitShard(out,b.add(side),normal,source,.5,.5,alpha);
        emitShard(out,a.sub(side),normal,source,.5,.5,alpha);emitShard(out,a.sub(side),normal,source,.5,.5,alpha);
        emitShard(out,b.add(side),normal,source,.5,.5,alpha);emitShard(out,b.sub(side),normal,source,.5,.5,alpha);
    }
    private static void emitShard(VertexConsumer out,Vec p,Vec normal,Vec source,double u,double v,double alpha){
        out.vertex(p.x(),p.y(),p.z()).color((float)(source.x()*.5+.5),(float)(source.y()*.5+.5),(float)(source.z()*.5+.5),(float)alpha)
                .uv((float)u,(float)v).overlayCoords(0,0).uv2(240,240).normal((float)normal.x(),(float)normal.y(),(float)normal.z()).endVertex();
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Material extends RenderType {
        static ShaderInstance shader;
        static ShaderInstance shardShader;
        static final RenderType SHARDS=create("spirit_shield_shards",DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,131072,false,false,CompositeState.builder()
                .setShaderState(new ShaderStateShard(()->shardShader)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                .setOutputState(MAIN_TARGET).createCompositeState(false));
        static final RenderType TYPE=create("spirit_shield_sphere",DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.TRIANGLES,32768,false,false,CompositeState.builder()
                .setShaderState(new ShaderStateShard(()->shader)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                .setOutputState(MAIN_TARGET).createCompositeState(false));
        private Material(String n,VertexFormat f,VertexFormat.Mode m,int size,boolean c,boolean s,Runnable a,Runnable b){super(n,f,m,size,c,s,a,b);}
        @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"spirit_shield_sphere"),
                    DefaultVertexFormat.POSITION_COLOR_NORMAL),s->shader=s);
            event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"spirit_shield_shards"),
                    DefaultVertexFormat.NEW_ENTITY),s->shardShader=s);
        }
    }
    private SpiritShieldRenderer() {}
}
