package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.*;

/** Opt-in real local-player flight, vanilla F5 handling and framebuffer/image diagnostics. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CameraCycleCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.cameraCycleCapture")&&!Boolean.getBoolean("wuxingxiantu.flightTierCapture");
    private static final boolean EXPECT_PACK=Boolean.getBoolean("wuxingxiantu.cameraExpectedPackActive");
    private static final long START=System.nanoTime();
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>(),CHECKS=new ArrayList<>(),F5_EVENTS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>(),GL_ERRORS=new ArrayList<>();
    private static final Map<String,Object> STAGES=new LinkedHashMap<>();
    private static CompletableFuture<?> preparation;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> latestServer;
    private static Vec3 movementStart,serverMovementStart;
    private static boolean opened,prepared,started,finished;
    private static int tick;
    private static String pendingCapture,packName="none";
    private static CameraType expectedCamera=CameraType.FIRST_PERSON;
    private static final Path OUTPUT=Path.of("camera-cycle");

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>360)throw new IllegalStateException("Camera cycle fixture timed out");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.createDirectories(OUTPUT);mc.getWindow().setWindowed(1280,720);
                mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(60);
                mc.options.fov().set(65);mc.options.bobView().set(false);mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.options.hideGui=false;mc.resizeDisplay();mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("camera-cycle-"+System.currentTimeMillis(),
                    new LevelSettings("Flight camera cycle verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(!prepared){
                prepared=true;mc.setScreen(null);HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);System.clearProperty("wuxingxiantu.meteorBloomDisabled");
                UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    for(int x=-8;x<=500;x++)for(int z=-6;z<=6;z++)level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                    for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=131;y<=142;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                    p.teleportTo(0,131,0);p.setInvulnerable(true);
                    var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
                    level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);level.setWeatherParameters(6000,0,false,false);GeneratedMod.sync(p);
                    // Flight permission/entry is entirely the normal survival production path.
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(!started){
                if(!mc.player.onGround()||!mc.player.getAbilities().mayfly||!RealmFlightControl.eligible(mc.player))return;
                started=true;check("initial_not_flying",!mc.player.getAbilities().flying,"No fixture flying/mayfly grant; production permission arrived");
            }
            if(serverSample!=null&&serverSample.isDone()){latestServer=serverSample.join();serverSample=null;}
            // Minecraft can consume several ticks between rendered frames.
            // Keep each requested F5 view until its real screenshot is saved.
            if(pendingCapture!=null)return;
            tick++;mc.player.setYRot(-90);mc.player.setXRot(0);
            mc.options.keyJump.setDown(tick==20||tick==24||tick>=30&&tick<=55);
            mc.options.keyUp.setDown(tick>=70&&tick<=178||tick>=480&&tick<=585);
            mc.options.keyDown.setDown(false);mc.options.keyLeft.setDown(false);mc.options.keyRight.setDown(false);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(false);
            if(tick==60){
                boolean active=TrailShaderBridge.shaderPackDetected();
                if(ModList.get().isLoaded("oculus")){Class<?> iris=Class.forName("net.irisshaders.iris.Iris");packName=String.valueOf(iris.getMethod("getCurrentPackName").invoke(null));}
                check("actual_shader_pack_mode",active==EXPECT_PACK,"oculusInstalled="+ModList.get().isLoaded("oculus")+", actualPackActive="+active+", expected="+EXPECT_PACK+", packName="+packName);
                check("natural_double_space_entry",mc.player.getAbilities().flying&&latestServer!=null&&Boolean.TRUE.equals(latestServer.get("flying")),"Actual normal double-jump entry enabled both client/server flight");
            }
            if(tick==70||tick==480){movementStart=mc.player.position();serverMovementStart=serverPosition();}
            if(tick==178||tick==585){check("actual_W_movement_"+tick,mc.player.position().subtract(movementStart).horizontalDistance()>8&&serverPosition().subtract(serverMovementStart).horizontalDistance()>8,"Client/server actual W displacement="+mc.player.position().subtract(movementStart).horizontalDistance()+" / "+serverPosition().subtract(serverMovementStart).horizontalDistance());}
            if(pulse(100,130,160,200,240,280,300,302,304,306,308,310,320,480,520,560,610,650,690,720,722,724,726,728,730,930,960))clickF5(mc);
            if(tick==350||tick==760){HudConfig.REALM_FLIGHT_ANIMATION.set(false);RealmFlightVisuals.sendMode();}
            if(tick==390||tick==800){HudConfig.REALM_FLIGHT_ANIMATION.set(true);RealmFlightVisuals.sendMode();}
            if(tick==430||tick==840)System.setProperty("wuxingxiantu.meteorBloomDisabled","true");
            if(tick==460){
                System.clearProperty("wuxingxiantu.meteorBloomDisabled");
                preparation=mc.getSingleplayerServer().submitAsync(()->mc.getSingleplayerServer().overworld().setDayTime(18000));
            }
            if(tick==880)System.clearProperty("wuxingxiantu.meteorBloomDisabled");
            if(tick==900)mc.options.hideGui=true;
            String tag=switch(tick){
                case 80->"day-moving-first";case 110->"day-moving-back";case 140->"day-moving-front";case 170->"day-moving-first-return";
                case 220->"day-hover-back";case 260->"day-hover-front";case 290->"day-hover-first";
                case 301,303,305,307,309,311->"day-rapid-"+tick;
                case 340->"day-back-effects-on";case 374->"day-back-cosmetic-off-24ticks";case 414->"day-back-effects-restored";case 454->"day-back-bloom-off";
                case 490->"night-moving-front";case 530->"night-moving-first";case 570->"night-moving-back";
                case 630->"night-hover-front";case 670->"night-hover-first";case 710->"night-hover-back";
                case 721,723,725,727,729,731->"night-rapid-"+tick;
                case 750->"night-back-effects-on";case 784->"night-back-cosmetic-off-24ticks";case 824->"night-back-effects-restored";case 864->"night-back-bloom-off";
                case 920->"night-no-gui-back";case 950->"night-no-gui-front";case 980->"night-no-gui-first";default->null;
            };
            if(tag!=null){if(pendingCapture!=null)throw new IllegalStateException("Previous capture was not rendered");pendingCapture=tag;}
            if(tick==1000){check("complete_capture_set",CAPTURES.size()==36,"Real screenshots="+CAPTURES.size()+", expected36");finish();}
        }catch(Throwable error){FAILURES.add("Fixture exception: "+error);com.mojang.logging.LogUtils.getLogger().error("CAMERA_CYCLE",error);finish();}
    }
    private static boolean pulse(int... ticks){for(int value:ticks)if(tick==value)return true;return false;}
    private static void clickF5(Minecraft mc){
        var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("before",mc.options.getCameraType().name());
        expectedCamera=mc.options.getCameraType().cycle();row.put("expectedAfter",expectedCamera.name());row.put("boundKey",mc.options.keyTogglePerspective.getKey().getValue());F5_EVENTS.add(row);
        KeyMapping.click(mc.options.keyTogglePerspective.getKey());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void serverTelemetry(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||!started||event.phase!=TickEvent.Phase.END||serverSample!=null)return;
        var mc=Minecraft.getInstance();UUID id=mc.player.getUUID();serverSample=mc.getSingleplayerServer().submit(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var row=new LinkedHashMap<String,Object>();row.put("x",p.getX());row.put("y",p.getY());row.put("z",p.getZ());row.put("flying",p.getAbilities().flying);row.put("mayfly",p.getAbilities().mayfly);row.put("flyingSpeed",p.getAbilities().getFlyingSpeed());return row;
        });
    }
    private static Vec3 serverPosition(){return latestServer==null?Vec3.ZERO:new Vec3(((Number)latestServer.get("x")).doubleValue(),((Number)latestServer.get("y")).doubleValue(),((Number)latestServer.get("z")).doubleValue());}
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void beforeParticles(RenderLevelStageEvent event){if(ENABLED&&!finished&&started&&event.getStage()==RenderLevelStageEvent.Stage.AFTER_PARTICLES)STAGES.put("particlesBeforeProduction",glState("particlesBefore"));}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void afterWorld(RenderLevelStageEvent event){
        if(!ENABLED||finished||!started)return;
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_PARTICLES){
            var after=glState("particlesAfter");
            var before=(Map<?,?>)STAGES.get("particlesBeforeProduction");
            var changes=new ArrayList<String>();
            if(before==null)changes.add("missingBeforeSample");
            else {
                // This bracket includes other subscribers. The sword renderer
                // binds its WHITE texture and clears depth even with no sword.
                for(String field:new String[]{"drawFramebuffer","readFramebuffer","framebufferStatus","blend","blendSrcRgb","blendDstRgb","blendSrcAlpha","blendDstAlpha","blendEquationRgb","blendEquationAlpha","blendCacheIdentity","blendCacheOpaque"})
                    if(!java.util.Objects.equals(before.get(field),after.get(field)))changes.add(field);
                for(String field:new String[]{"viewport","scissorBox","colorWriteMask"})
                    if(!java.util.Arrays.equals((int[])before.get(field),(int[])after.get(field)))changes.add(field);
                if(!java.util.Arrays.equals((float[])before.get("shaderColor"),(float[])after.get("shaderColor")))changes.add("shaderColor");
            }
            after.put("restoredComparedToBefore",changes.isEmpty());after.put("stateDifferences",changes);
            STAGES.put("particlesAfterProduction",after);
        }
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL)STAGES.put("afterLevelBeforeDeferredComposite",glState("afterLevel"));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void gui(RenderGuiEvent.Pre event){if(ENABLED&&!finished&&started)STAGES.put("guiPreAfterHighestPriorityListeners",glState("guiPre"));}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!started||event.phase!=TickEvent.Phase.END)return;
        try {
            STAGES.put("renderEndAfterDeferredComposite",glState("renderEnd"));
            if(pendingCapture==null)return;var mc=Minecraft.getInstance();if(mc.screen!=null||mc.getOverlay()!=null)return;
            if(!scopeTested){scopeTested=true;scopeChecks();}
            String name=String.format(java.util.Locale.ROOT,"%02d-%s.png",CAPTURES.size()+1,pendingCapture);pendingCapture=null;
            Map<String,Object> metrics;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                image.writeToFile(OUTPUT.resolve(name));metrics=pixels(image);
            }
            var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("tick",tick);row.put("cameraType",mc.options.getCameraType().name());row.put("expectedCameraType",expectedCamera.name());
            var camera=mc.gameRenderer.getMainCamera();row.put("cameraPosition",camera.getPosition().toString());row.put("cameraLook",camera.getLookVector().toString());row.put("cameraDetached",camera.isDetached());row.put("cameraFluid",camera.getFluidInCamera().name());
            row.put("playerPosition",mc.player.position().toString());row.put("actualServer",latestServer);row.put("flying",mc.player.getAbilities().flying);row.put("mayfly",mc.player.getAbilities().mayfly);row.put("flyingSpeed",mc.player.getAbilities().getFlyingSpeed());row.put("onGround",mc.player.onGround());row.put("isInWater",mc.player.isInWater());row.put("eligible",RealmFlightControl.eligible(mc.player));
            row.put("forwardKey",mc.options.keyUp.isDown());row.put("dayTime",mc.level.getDayTime());row.put("hideGui",mc.options.hideGui);row.put("specialPreference",HudConfig.REALM_FLIGHT_ANIMATION.get());row.put("bloomDisabled",Boolean.getBoolean("wuxingxiantu.meteorBloomDisabled"));row.put("oculusInstalled",ModList.get().isLoaded("oculus"));row.put("embeddiumInstalled",ModList.get().isLoaded("embeddium"));row.put("actualPackActive",TrailShaderBridge.shaderPackDetected());row.put("packName",packName);
            row.put("pixelMetrics",metrics);row.put("renderStages",new LinkedHashMap<>(STAGES));row.put("mainFramebuffer",mc.getMainRenderTarget().frameBufferId);row.put("depthBridgeStatus",TrailShaderBridge.depthStatus);row.put("diagnosticGeometryOnly",RealmFlightEffects.renderedVertices);row.put("fps",mc.getFps());CAPTURES.add(row);
            check("view_"+name,mc.options.getCameraType()==expectedCamera,"Actual vanilla F5 camera="+mc.options.getCameraType().name());
            check("visible_world_"+name,!Boolean.TRUE.equals(metrics.get("blackScreenCandidate")),"Central world pixels: "+metrics);
            var restored=(Map<?,?>)STAGES.get("particlesAfterProduction");
            check("particle_blend_cache_restored_"+name,restored!=null&&Boolean.TRUE.equals(restored.get("restoredComparedToBefore")),
                "Multi-subscriber bracket: blend/cache/framebuffer differences="+(restored==null?"missingAfterSample":restored.get("stateDifferences"))+"; depth/texture/GL_PROGRAM are observations only");
            check("flying_"+name,mc.player.getAbilities().flying&&mc.player.getAbilities().mayfly,"Actual flight remains enabled through camera/cosmetic changes");write();
        }catch(Throwable error){FAILURES.add("Capture exception: "+error);finish();}
    }
    private static Map<String,Object> glState(String phase){
        var row=new LinkedHashMap<String,Object>();int[] viewport=new int[4],scissorBox=new int[4],colorWriteMask=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissorBox);
        var mask=org.lwjgl.BufferUtils.createByteBuffer(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,mask);for(int i=0;i<4;i++)colorWriteMask[i]=mask.get(i)!=0?1:0;
        row.put("tick",tick);row.put("drawFramebuffer",GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING));row.put("readFramebuffer",GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING));row.put("framebufferStatus",GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER));row.put("viewport",viewport);
        row.put("program",GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM));row.put("activeTexture",GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE));row.put("activeTextureBinding2d",GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));row.put("blend",GL11.glIsEnabled(GL11.GL_BLEND));row.put("depthTest",GL11.glIsEnabled(GL11.GL_DEPTH_TEST));row.put("depthWrite",GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK));row.put("cull",GL11.glIsEnabled(GL11.GL_CULL_FACE));row.put("scissor",GL11.glIsEnabled(GL11.GL_SCISSOR_TEST));row.put("scissorBox",scissorBox);row.put("colorWriteMask",colorWriteMask);row.put("shaderColor",RenderSystem.getShaderColor().clone());
        row.put("blendSrcRgb",GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB));row.put("blendDstRgb",GL11.glGetInteger(GL14.GL_BLEND_DST_RGB));row.put("blendSrcAlpha",GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA));row.put("blendDstAlpha",GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA));row.put("blendEquationRgb",GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB));row.put("blendEquationAlpha",GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA));
        var cache=BlendMode.lastApplied;row.put("blendCacheIdentity",cache==null?0:System.identityHashCode(cache));row.put("blendCacheOpaque",cache==null?null:cache.isOpaque());
        float[] clear=new float[4];GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE,clear);row.put("clearColor",clear);
        int[] textures=new int[2],shaderTextures=new int[2];int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        for(int i=0;i<2;i++){GlStateManager._activeTexture(GL13.GL_TEXTURE0+i);textures[i]=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);shaderTextures[i]=RenderSystem.getShaderTexture(i);}
        GlStateManager._activeTexture(active);row.put("textures01",textures);row.put("shaderTextures01",shaderTextures);
        int error;while((error=GL11.glGetError())!=GL11.GL_NO_ERROR){String value=tick+":"+phase+":"+error;GL_ERRORS.add(value);}
        return row;
    }
    private static boolean scopeTested;
    private static final class InjectedScopeFailure extends RuntimeException {}
    private static void scopeChecks(){
        int sentinel=GL11.glGenTextures();
        try(var outer=new MeteorBloomRenderer.State()){
            GlStateManager._activeTexture(GL13.GL_TEXTURE0+2);
            int originalUnit2=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GlStateManager._bindTexture(sentinel);
            try{
                for(int fault=0;fault<2;fault++){
                    var before=glState("scopeBefore");
                    try(var scope=new MeteorBloomRenderer.State()){
                        RenderSystem.viewport(1,2,37,29);RenderSystem.enableScissor(1,1,8,8);
                        RenderSystem.enableBlend();new BlendMode(GL11.GL_ONE,GL11.GL_ONE,org.lwjgl.opengl.GL14.GL_FUNC_ADD).apply();
                        RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();
                        RenderSystem.colorMask(false,true,false,true);RenderSystem.setShaderColor(.23F,.44F,.51F,.6F);
                        RenderSystem.clearColor(.1F,.2F,.3F,.4F);
                        RenderSystem.bindTexture(sentinel);RenderSystem.setShaderTexture(0,sentinel);RenderSystem.setShaderTexture(1,sentinel);
                        // Exercise an actual shader apply, including its program/cache.
                        TrailShaderBridge.meteorPostShader().apply();
                        if(fault==1)throw new InjectedScopeFailure();
                    }catch(InjectedScopeFailure expected){}
                    var after=glState("scopeAfter");var differences=new ArrayList<String>();
                    for(String field:new String[]{"drawFramebuffer","readFramebuffer","framebufferStatus","program","activeTexture","activeTextureBinding2d","blend","depthTest","depthWrite","cull","scissor","blendSrcRgb","blendDstRgb","blendSrcAlpha","blendDstAlpha","blendEquationRgb","blendEquationAlpha","blendCacheIdentity","blendCacheOpaque"})
                        if(!java.util.Objects.equals(before.get(field),after.get(field)))differences.add(field);
                    for(String field:new String[]{"viewport","scissorBox","colorWriteMask","textures01","shaderTextures01"})
                        if(!java.util.Arrays.equals((int[])before.get(field),(int[])after.get(field)))differences.add(field);
                    for(String field:new String[]{"shaderColor","clearColor"})
                        if(!java.util.Arrays.equals((float[])before.get(field),(float[])after.get(field)))differences.add(field);
                    check("exact_scope_restore_"+(fault==0?"normal":"exception_finally"),differences.isEmpty(),"Entry unit2 sentinel; actual shader apply; precise State boundary differences="+differences);
                }
            }finally{GlStateManager._activeTexture(GL13.GL_TEXTURE0+2);GlStateManager._bindTexture(originalUnit2);}
        }finally{RenderSystem.deleteTexture(sentinel);}
    }
    private static Map<String,Object> pixels(com.mojang.blaze3d.platform.NativeImage image){
        int w=image.getWidth(),h=image.getHeight(),count=0,black=0,min=255,max=0;double sum=0,sumSquares=0;
        for(int y=h/5;y<h*4/5;y++)for(int x=w/5;x<w*4/5;x++){
            int c=image.getPixelRGBA(x,y),r=c&255,g=c>>8&255,b=c>>16&255;double l=.2126*r+.7152*g+.0722*b;
            count++;sum+=l;sumSquares+=l*l;if(Math.max(r,Math.max(g,b))<=3)black++;min=Math.min(min,Math.min(r,Math.min(g,b)));max=Math.max(max,Math.max(r,Math.max(g,b)));
        }
        double mean=sum/count,fraction=black/(double)count;var result=new LinkedHashMap<String,Object>();result.put("width",w);result.put("height",h);result.put("roi","central60% width/height; excludes most GUI overlays");result.put("sampledPixels",count);result.put("meanLuma255",mean);result.put("lumaStdDev",Math.sqrt(Math.max(0,sumSquares/count-mean*mean)));result.put("nearBlackFractionRgbLe3",fraction);result.put("minimumChannel",min);result.put("maximumChannel",max);result.put("blackScreenCriterion","meanLuma255<5 AND nearBlackRgbLe3Fraction>0.75 in this open, bright platform test world");result.put("blackScreenCandidate",fraction>.75&&mean<5);return result;
    }
    private static void check(String name,boolean pass,String detail){var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("name",name);row.put("pass",pass);row.put("detail",detail);CHECKS.add(row);if(!pass)FAILURES.add(name+": "+detail);com.mojang.logging.LogUtils.getLogger().info("CAMERA_CYCLE_CHECK {}",row);}
    private static void write()throws Exception{
        var result=new LinkedHashMap<String,Object>();result.put("result",finished?(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL"):"RUNNING");result.put("captures",CAPTURES);result.put("checks",CHECKS);result.put("failures",FAILURES);result.put("glErrors",GL_ERRORS);result.put("f5InputEvents",F5_EVENTS);
        result.put("expectedPackActive",EXPECT_PACK);result.put("oculusInstalled",ModList.get().isLoaded("oculus"));result.put("embeddiumInstalled",ModList.get().isLoaded("embeddium"));result.put("actualPackActive",TrailShaderBridge.shaderPackDetected());result.put("packName",packName);
        result.put("scope","Independent survival world; normal production mayfly grant, logical double-space entry and W motion. F5 uses vanilla KeyMapping.click/consumeClick. No forced camera position, remote avatar, forced shader route or pre-enabled ordinary flight. Pixel candidate is a diagnostic proxy backed by saved actual screenshots, not a draw-count success claim. No native GLFW physical-key certification.");
        result.put("glStageScope","AFTER_PARTICLES brackets multiple subscribers and asserts blend/cache/framebuffer/color state only. Sword renderer legitimately binds WHITE/depth false even with no swords; texture/depth/program across that entire event are observations. Two separate exact MeteorBloomRenderer.State boundary checks use active unit2 sentinel plus actual shader apply, normal close and injected exception finally, and compare all saved GL/cache/program/texture/clearColor state. GUI/no-GUI screenshots cover real deferred composites; sample ticks distinguish stale GUI-pre observations.");
        Files.writeString(Path.of("camera-cycle-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
    private static void finish(){if(finished)return;finished=true;try{var mc=Minecraft.getInstance();mc.options.keyUp.setDown(false);mc.options.keyJump.setDown(false);System.clearProperty("wuxingxiantu.meteorBloomDisabled");write();Files.writeString(Path.of("camera-cycle-result.txt"),(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL")+": "+CAPTURES.size()+" screenshots, "+CHECKS.size()+" checks; failures="+FAILURES+"; GL="+GL_ERRORS);}catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("CAMERA_CYCLE_RESULT",error);}Minecraft.getInstance().stop();}
    private CameraCycleCapture(){}
}
