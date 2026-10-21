package cn.blockforge.generated.generatedmod.client.furnacevolume;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.fire.FireCatalog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderLevelStageEvent;
/** Independently owned bottom fire and upper brewing volumes; gameplay/results remain host-native. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FurnaceFireVolume{
    /** Optional isolated-test probe. No query or timing work when unset. */
    public interface DrawProbe{void draw(int program,int vao,int width,int height,String fireId);default void drawUpper(int program,int vao,int width,int height,String fireId){}default double clockSeconds(double gameSeconds){return gameSeconds;}default void composite(Runnable draw){draw.run();}}
    public static DrawProbe drawProbe;
    private static final java.util.Set<Object> CLAIMED=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private static Recipe[] recipes,upperRecipes;
    private static boolean upperFailed,bottomFailed;
    private static final java.util.Set<Object> UPPER_CLAIMED=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    public static boolean claimsUpper(Object furnace){return UPPER_CLAIMED.contains(furnace);}
    public static int upperDraws(){return UPPER_CLAIMED.size();}
    private record Pending(HostAdapter.Furnace furnace,Recipe recipe,org.joml.Matrix4f matrix,net.minecraft.world.phys.Vec3 eye,float partial){}
    private static final java.util.List<Pending> PENDING=new java.util.ArrayList<>();
    public static boolean claims(Object furnace){return CLAIMED.contains(furnace);}
    public static int draws(){return CLAIMED.size();}
    static void reset(){recipes=upperRecipes=null;upperFailed=bottomFailed=false;CLAIMED.clear();UPPER_CLAIMED.clear();PENDING.clear();VolumeGpu.release();}
    private static String resource(String name)throws Exception{
        try(var in=FurnaceFireVolume.class.getResourceAsStream("/assets/wuxingxiantu/shaders/furnacevolume/"+name)){return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
    }
    private static void load()throws Exception{
        var base=com.google.gson.JsonParser.parseString(resource("recipe.json")).getAsJsonObject();
        var palettes=com.google.gson.JsonParser.parseString(resource("nine-flames.json")).getAsJsonObject().getAsJsonArray("palettes");
        var upperBase=com.google.gson.JsonParser.parseString(resource("upper-recipe.json")).getAsJsonObject();
        recipes=new Recipe[10];recipes[0]=Recipe.parse(base.toString());
        upperRecipes=new Recipe[10];upperRecipes[0]=Recipe.parse(upperBase.toString());
        double[] speed={.7,.8,.65,.38,.56,.90,1.20,.80,.45,.95};
        for(int i=1;i<=9;i++){var next=base.deepCopy();var palette=palettes.get(i-1).getAsJsonObject();
            for(String color:new String[]{"fireCore","fireMid","fireEdge"})next.add(color,palette.get(color));
            next.addProperty("flowSpeed",speed[i]);recipes[i]=Recipe.parse(next.toString());
            var upper=upperBase.deepCopy();for(String color:new String[]{"fireCore","fireMid","fireEdge"})upper.add(color,palette.get(color));
            upperRecipes[i]=Recipe.parse(upper.toString());
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        if(net.minecraft.client.Minecraft.getInstance().level==null||bottomFailed)return;
        boolean post=cn.blockforge.generated.generatedmod.client.TrailShaderBridge.shaderPackDetected();
        PENDING.clear();if(!post){CLAIMED.clear();UPPER_CLAIMED.clear();}
        try{if(recipes==null)load();int count=0;
            for(var furnace:HostAdapter.loaded(e.getPartialTick())){
                if(count>=8||!furnace.brewing())continue;
                var fire=FireCatalog.id(furnace.activeFireId());int tier=fire==null?0:fire.tier();
                int index=Math.max(0,Math.min(9,tier));
                var matrix=new org.joml.Matrix4f(e.getProjectionMatrix()).mul(e.getPoseStack().last().pose());
                var bottom=new Pending(furnace,recipes[index],matrix,e.getCamera().getPosition(),e.getPartialTick());
                var upper=new Pending(furnace,upperRecipes[index],matrix,e.getCamera().getPosition(),e.getPartialTick());
                if(post){PENDING.add(bottom);if(!upperFailed)PENDING.add(upper);count++;}
                else {boolean visible=draw(bottom,-1);if(!upperFailed)visible|=draw(upper,-1);if(visible)count++;}
            }
            if(post){
                CLAIMED.removeIf(owner->PENDING.stream().noneMatch(p->!p.recipe.upperOnly()&&p.furnace.entity()==owner));
                UPPER_CLAIMED.removeIf(owner->PENDING.stream().noneMatch(p->p.recipe.upperOnly()&&p.furnace.entity()==owner));
                if(!PENDING.isEmpty())cn.blockforge.generated.generatedmod.client.TrailShaderBridge.captureDepth();
            }
        }catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("Furnace bottom volume failed; legacy fallback",error);reset();bottomFailed=true;}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)post();}
    @SubscribeEvent public static void gui(net.minecraftforge.client.event.RenderGuiEvent.Pre e){if(!PENDING.isEmpty()){e.getGuiGraphics().flush();post();}}
    private static boolean draw(Pending p,int depth)throws Exception{
        try{
            boolean drawn=VolumeGpu.render(p.recipe,p.furnace,p.matrix,p.eye,p.partial,depth);
            if(drawn)(p.recipe.upperOnly()?UPPER_CLAIMED:CLAIMED).add(p.furnace.entity());return drawn;
        }catch(Exception failure){
            if(!p.recipe.upperOnly())throw failure;
            upperFailed=true;UPPER_CLAIMED.clear();
            com.mojang.logging.LogUtils.getLogger().error("Upper alchemy volume disabled until resource reload; legacy brewing fallback",failure);return false;
        }
    }
    private static void post(){
        if(PENDING.isEmpty())return;CLAIMED.clear();UPPER_CLAIMED.clear();int depth=cn.blockforge.generated.generatedmod.client.TrailShaderBridge.capturedDepthTexture();
        try(var state=new cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer.State()){
            if(depth<0)return;net.minecraft.client.Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            for(var p:PENDING)if(!p.recipe.upperOnly()||!upperFailed)draw(p,depth);
        }catch(Exception failure){com.mojang.logging.LogUtils.getLogger().error("Furnace post volume failed; legacy fallback",failure);reset();bottomFailed=true;}
        finally{PENDING.clear();}
    }
    @SubscribeEvent public static void unload(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e){reset();HostAdapter.clear();}
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Resources{
        @SubscribeEvent public static void register(net.minecraftforge.client.event.RegisterClientReloadListenersEvent e){
            e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)manager->com.mojang.blaze3d.systems.RenderSystem.recordRenderCall(FurnaceFireVolume::reset));
        }
    }
}
