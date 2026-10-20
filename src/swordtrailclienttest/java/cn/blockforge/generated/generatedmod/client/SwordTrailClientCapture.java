package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.network.packet.yujian.SwordFlightInputPacket;
import cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity;
import cn.blockforge.generated.generatedmod.yujian.FlyingSwordConfig;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** Real server-approved sword flight and actual production trail draws, in an opt-in disposable world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SwordTrailClientCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.swordTrailCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.swordTrailOutput","../../reports/sword-trail-client"));
    private static final long START=System.nanoTime();
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>(),TELEMETRY=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final java.lang.reflect.Method CAMERA_POSITION=ObfuscationReflectionHelper.findMethod(Camera.class,"m_90581_",Vec3.class);
    private static CompletableFuture<?> preparation;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> latestServer;
    private static boolean opened,prepared,finished;
    private static int tick;
    private static String pending;
    private static Vec3 movementStart;
    private static int nativeDraws,postDraws;

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUTPUT);
            if((System.nanoTime()-START)/1_000_000_000>300)throw new AssertionError("Sword trail capture watchdog exceeded 300 seconds");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.player!=null)throw new AssertionError("Fixture refuses to adopt an existing world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.deleteIfExists(OUTPUT.resolve("failure.txt"));Files.deleteIfExists(OUTPUT.resolve("complete.txt"));
                mc.getWindow().setWindowed(1280,800);mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwSetWindowTitle(mc.getWindow().getWindow(),"sword-trail-test (isolated)");
                if(!Boolean.getBoolean("wuxingxiantu.swordTrailShowWindow"))org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.cloudStatus().set(CloudStatus.OFF);
                mc.options.framerateLimit().set(60);mc.options.fov().set(60);mc.options.bobView().set(false);mc.options.hideGui=!Boolean.getBoolean("wuxingxiantu.flightHudCapture");
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                FlyingSwordConfig.TRAILS.set(false);FlyingSwordConfig.TRAIL_PATH.set(FlyingSwordConfig.TrailPath.NORMAL_TRAIL_RENDER_PATH);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("sword-trail-test-"+System.currentTimeMillis(),
                        new LevelSettings("Sword trail verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(712309,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null
                    ||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.screen!=null)mc.setScreen(null);
            if(!prepared){
                prepared=true;var id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
                    p.teleportTo(level,0,180,0,0,0);p.setNoGravity(true);p.removeAllEffects();p.setAbsorptionAmount(0);
                    p.getInventory().setItem(0,new ItemStack(Boolean.getBoolean("wuxingxiantu.swordFirstPersonCapture")?WuXingItems.BIG_DIPPER_SWORD.get():WuXingItems.DARK_IRON_SWORD.get()));p.getInventory().selected=0;
                    var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(5);data.wuxingxiantu$learnArt(YujianController.FLIGHT);
                    data.wuxingxiantu$setArtMastery(YujianController.FLIGHT,100);data.wuxingxiantu$setMaxMana(1_000_000);data.wuxingxiantu$setAura(1_000_000);
                    GeneratedMod.sync(p);p.containerMenu.broadcastChanges();
                    if(!YujianController.canCast(p,YujianController.FLIGHT))throw new AssertionError("Actual server rejected equipped sword/learned art preparation");
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            collectServer();
            if(pending!=null){sendInput(false);return;}
            tick++;
            // Production c2s packet supplies held-B/release and forward+sprint inputs; no client entity or history is created here.
            float yaw=tick>=110?(tick-110)*.9F:0;
            sendInput(tick==20);
            mc.player.setYRot(yaw);mc.player.setXRot(0);
            if(tick==55){
                if(!(mc.player.getVehicle() instanceof ControlledSwordEntity sword)||!sword.flightRoot())throw new AssertionError("Real held-B/release server path did not mount a synchronized sword");
                movementStart=sword.position();
            }
            if(tick==62)pending="01-flight-trails-disabled.png";
            if(tick==65){FlyingSwordConfig.TRAILS.set(true);nativeDraws=SwordFlightTrailRenderer.normalDraws;}
            if(tick==95)pending="02-flight-native-trail.png";
            if(tick==105)FlyingSwordConfig.TRAILS.set(false);
            if(tick==120)pending="03-flight-disabled-again.png";
            if(tick==125){FlyingSwordConfig.TRAILS.set(true);FlyingSwordConfig.TRAIL_PATH.set(FlyingSwordConfig.TrailPath.POST_SHADER_TRAIL_RENDER_PATH);postDraws=SwordFlightTrailRenderer.postDraws;}
            if(tick==155)pending="04-flight-post-trail.png";
            if(tick==170){FlyingSwordConfig.TRAIL_PATH.set(FlyingSwordConfig.TrailPath.AUTO);mc.options.cloudStatus().set(CloudStatus.FANCY);}
            if(tick==200)pending="05-flight-auto-cloud-trail.png";
            if(tick==210)FlyingSwordConfig.TRAILS.set(false);
            if(tick==225)pending="06-flight-final-disabled.png";
            if(tick>=55&&mc.player.getVehicle() instanceof ControlledSwordEntity sword){
                var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("swordPosition",sword.position().toString());row.put("speed",sword.flightSpeed());row.put("flightRoot",sword.flightRoot());row.put("state",sword.flightState().name());
                row.put("enabled",FlyingSwordConfig.TRAILS.get());row.put("renderedSegments",FlyingSwordEffects.renderedSegments);row.put("nativeDraws",SwordFlightTrailRenderer.normalDraws);row.put("postDraws",SwordFlightTrailRenderer.postDraws);TELEMETRY.add(row);
            }
            if(tick==250){
                if(!(mc.player.getVehicle() instanceof ControlledSwordEntity sword)||movementStart==null||sword.position().distanceTo(movementStart)<10)throw new AssertionError("Actual synchronized sword did not travel");
                if(!Boolean.getBoolean("wuxingxiantu.swordFirstPersonCapture")&&(SwordFlightTrailRenderer.normalDraws<=nativeDraws||SwordFlightTrailRenderer.postDraws<=postDraws))throw new AssertionError("Native or post production trail shader was never drawn");
                if(CAPTURES.size()!=6)throw new AssertionError("Incomplete screenshots "+CAPTURES.size()+" / 6");
                if(latestServer==null||!Boolean.TRUE.equals(latestServer.get("mounted")))throw new AssertionError("Missing authoritative mounted server sample");
                finish();
            }
        }catch(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("SWORD_TRAIL_CAPTURE",error);finish();}
    }

    private static void sendInput(boolean edge){
        boolean summon=tick>=20&&tick<=31,forward=tick>=50;
        float yaw=tick>=110?(tick-110)*.9F:0;
        Network.CHANNEL.sendToServer(new SwordFlightInputPacket(summon,edge,forward,false,false,false,
                false,false,forward,false,1,0,yaw,0));
    }

    private static void collectServer(){
        var mc=Minecraft.getInstance();
        if(serverSample!=null){if(!serverSample.isDone())return;latestServer=serverSample.join();serverSample=null;}
        if(tick%10!=0)return;
        var id=mc.player.getUUID();serverSample=mc.getSingleplayerServer().submit(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var row=new LinkedHashMap<String,Object>();
            row.put("serverTick",p.tickCount);row.put("mounted",p.getVehicle() instanceof ControlledSwordEntity);row.put("position",p.position().toString());
            row.put("learnedArt",GeneratedMod.data(p).wuxingxiantu$hasLearnedArt(YujianController.FLIGHT));
            if(p.getVehicle() instanceof ControlledSwordEntity sword){row.put("swordId",sword.getId());row.put("flightRoot",sword.flightRoot());row.put("state",sword.flightState().name());row.put("speed",sword.flightSpeed());}
            return row;
        });
    }

    /** Test-only oblique camera follows the real synchronized sword, leaving its motion untouched. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event)throws Exception{
        if(!ENABLED||finished||tick<55)return;
        var mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof ControlledSwordEntity sword))return;
        if(Boolean.getBoolean("wuxingxiantu.swordFirstPersonCapture")){
            mc.options.setCameraType(CameraType.FIRST_PERSON);FlyingSwordConfig.HIDE_HELD.set(true);
            event.setPitch(Boolean.getBoolean("wuxingxiantu.swordDeckCapture")?65:0);return;
        }
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        var target=sword.getPosition((float)event.getPartialTick()).add(0,1,0);
        double yaw=Math.toRadians(sword.getYRot());var forward=new Vec3(-Math.sin(yaw),0,Math.cos(yaw));var side=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        var eye=target.subtract(forward.scale(8)).add(side.scale(8)).add(0,3,0);var delta=target.subtract(eye);
        CAMERA_POSITION.invoke(event.getCamera(),eye);
        event.setYaw((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));event.setPitch((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));event.setRoll(0);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END||pending==null)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.getOverlay()!=null)return;
        try{
            boolean enabled=FlyingSwordConfig.TRAILS.get();
            boolean first=Boolean.getBoolean("wuxingxiantu.swordFirstPersonCapture");
            if(first&&mc.player.getVehicle() instanceof ControlledSwordEntity sword){
                var flight=InterpolatedFlyingSwordTransform.get(sword,mc.getFrameTime());
                double error=mc.gameRenderer.getMainCamera().getPosition().distanceTo(flight.local(0,sword.riderOffset()+mc.player.getEyeHeight(),0));
                if(error>.0001)throw new AssertionError("Eyes drifted away from sword standing frame: "+error);
            }
            if(enabled&&!first&&(!FlyingSwordEffects.normalTrailSubmitted||FlyingSwordEffects.normalTrailVertexCount==0||FlyingSwordEffects.trailDiagnostics().isEmpty()))return;
            if(first&&(FlyingSwordEffects.normalTrailVertexCount!=0||FirstPersonFlightFlow.vertices!=0))throw new AssertionError("Sword first person rendered historical wake or realm flow");
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("OpenGL error "+error);
            if(!enabled&&(FlyingSwordEffects.renderedSegments!=0||!FlyingSwordEffects.trailDiagnostics().isEmpty()))throw new AssertionError("Disabled option retained production trail history/geometry");
            String name=pending;pending=null;try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(name));}
            var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("tick",tick);row.put("enabled",enabled);row.put("path",FlyingSwordEffects.activeTrailRenderPath);
            row.put("renderedSegments",FlyingSwordEffects.renderedSegments);row.put("normalVertices",FlyingSwordEffects.normalTrailVertexCount);row.put("submitted",FlyingSwordEffects.normalTrailSubmitted);
            row.put("nativeDraws",SwordFlightTrailRenderer.normalDraws);row.put("postDraws",SwordFlightTrailRenderer.postDraws);row.put("depth",TrailShaderBridge.depthStatus);row.put("glError",error);
            row.put("history",FlyingSwordEffects.trailDiagnostics());row.put("server",latestServer);row.put("camera",mc.gameRenderer.getMainCamera().getPosition().toString());CAPTURES.add(row);write();
            row.put("firstPersonFlowVertices",FirstPersonFlightFlow.vertices);
        }catch(Throwable error){FAILURES.add(error.toString());finish();}
    }
    private static void write()throws Exception{
        var result=new LinkedHashMap<String,Object>();result.put("test","real-server-sword-trail-toggle");result.put("isolated",true);result.put("input","Production c2s held-B/release and forward+sprint packets; not OS keyboard injection");
        result.put("result",finished?(FAILURES.isEmpty()?"PASS":"FAIL"):"RUNNING");result.put("captures",CAPTURES);result.put("telemetry",TELEMETRY);result.put("server",latestServer);result.put("failures",FAILURES);
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
    private static void finish(){
        if(finished)return;finished=true;
        try{Files.createDirectories(OUTPUT);write();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?"PASS: authoritative equipped/learned-art sword mount/motion, false/true trail options, native/post/auto-cloud production shader draws and six actual GPU screenshots.\n":String.join("\n",FAILURES));}
        catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("SWORD_TRAIL_CAPTURE_REPORT",error);}
        Minecraft.getInstance().stop();
    }
}
