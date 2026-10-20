package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorHeadRenderer;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightCamera;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightAppearance;
import cn.blockforge.generated.generatedmod.realmflight.FlightVisualDynamics;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightVisualPacket;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorTuning;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity;
import cn.blockforge.generated.generatedmod.yujian.FlyingSwordState;
import cn.blockforge.generated.generatedmod.yujian.YujianPhase;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

/**
 * Isolated, opt-in actual-render evidence. Controlled remote paths test the
 * production visuals; the final section uses real local movement and vanilla
 * F5 handling. Pixel statistics and draw counters are diagnostics, never an
 * assertion that a design looks good. This class never enters the release JAR.
 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FlightTierCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.flightTierCapture");
    private static final boolean EXPECT_PACK=Boolean.getBoolean("wuxingxiantu.flightTierExpectedPackActive");
    private static final boolean BLOOM_DISABLED=Boolean.getBoolean("wuxingxiantu.flightTierBloomDisabled");
    private static final long START=System.nanoTime();
    private static final UUID SUBJECT_ID=UUID.fromString("73de1886-5114-423e-b932-139a883b4312");
    private static final UUID ALEX_ID=UUID.fromString("ebaef471-9426-49dc-a6b1-5e641320bc37");
    private static final String[] REALM_NAMES={"yuanying","huashen","lianxu","heti","dacheng","dixian"};
    private static final YujianPhase[] SWORD_PHASES={YujianPhase.BASIC,YujianPhase.JIYU,YujianPhase.DUNXU};
    private static final int SECTION_TICKS=120,REALM_END=720,SWORD_END=1080,MOTION_END=1260,REMOTE_END=1620,BASE_FINISH_TICK=1850,CAMERA_PHASE_TICKS=140,LONG_SWORD_BEGIN=BASE_FINISH_TICK+CAMERA_PHASE_TICKS,LONG_SWORD_TICKS=2600,FINISH_TICK=LONG_SWORD_BEGIN+LONG_SWORD_TICKS,EXPECTED_CAPTURES=89;
    private static final int EXPECTED_CAMERA_IMAGES=10,EXPECTED_CAMERA_CHECKS=42;
    private static final int EXPECTED_LONG_SWORD_IMAGES=12,EXPECTED_LONG_SWORD_CHECKS=53;
    private static final int STAGE2_CAPTURES=34,STAGE2_CHECKS=346;
    private static final double ALTITUDE=135,STAGE2_ALTITUDE=ALTITUDE+45;
    private static final Path OUTPUT=Path.of("flight-tiers"),SEQUENCE=OUTPUT.resolve("turn-sequence");
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>(),SEQUENCE_FRAMES=new ArrayList<>(),CHECKS=new ArrayList<>(),F5_EVENTS=new ArrayList<>();
    private static final List<Map<String,Object>> CAMERA_IMAGES=new ArrayList<>(),CAMERA_CHECKS=new ArrayList<>(),CAMERA_F5_EVENTS=new ArrayList<>();
    private static final List<Map<String,Object>> LONG_SWORD_IMAGES=new ArrayList<>(),LONG_SWORD_CHECKS=new ArrayList<>(),LONG_SWORD_TICKS_RECORDED=new ArrayList<>(),LONG_SWORD_RENDER_FRAMES=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>(),GL_ERRORS=new ArrayList<>();
    private static final Map<String,Object> RENDER_DIAGNOSTICS=new LinkedHashMap<>();
    private static final Map<UUID,String> PLAYER_DRAWS=new LinkedHashMap<>();
    private static CompletableFuture<?> preparation;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> latestServer;
    private static RemotePlayer subject;
    private static RemotePlayer secondSubject;
    private static ControlledSwordEntity sword;
    private static Method cameraPosition,cameraRotation;
    private static RealmFlightVisualPacket actualPacket;
    private static RealmFlightVisualPacket actualSecondPacket;
    private static Vec3 lastPosition=new Vec3(0,ALTITUDE,0),cameraOffset=new Vec3(-9,5,31),lookTarget;
    private static Vec3 localMovementStart;
    private static Vec3 motionPosition;
    private static double motionYaw;
    private static Vec3 remotePosition,secondPosition,remoteVelocity=new Vec3(1,0,0);
    private static int remoteStep,stage2CheckStart,stage2CaptureStart,stage2ChecksObserved,stage2CapturesObserved,stage2ReadinessWaitFrames;
    private static int swordReadinessFrames,swordReadinessWaitFrames;
    private static String remoteView="front",remotePath="straight";
    private static boolean opened,prepared,finished,remoteCamera=true,localPreparationRequested,localReady;
    private static int tick,realm=3,swordPhase=-1,frameHeadStart,frameBloomStart,frameSwordNativeStart,frameSwordPostStart;
    private static String pendingCapture,pendingSequence,packName="none",scene="realm";
    private static String pendingCameraCapture;
    private static String pendingLongSwordCapture;
    private static boolean cameraPreparationRequested,cameraReady,cameraCollisionRequested,cameraCollisionReady;
    private static final Vec3 CAMERA_START=new Vec3(40,170,0);
    private static double backBoostDistance,frontBoostDistance;
    private static boolean longSwordPrepared;
    private static boolean longSwordPreflightRequested;
    private static long longSwordPreflightStartedNanos;
    private static int longSwordPreflightIndex,longSwordPreflightWaitTicks,previousLongRenderDistance,previousLongServerViewDistance;
    private static net.minecraft.client.CloudStatus previousLongCloudStatus;
    private static final List<BlockPos> LONG_SWORD_PREFLIGHT_BLOCKS=new ArrayList<>();
    private static final List<Map<String,Object>> LONG_SWORD_PREFLIGHT_SECTIONS=new ArrayList<>();
    private static int longSwordRecordedStep,longSwordReadinessFrames,longSwordReadinessWaitFrames;
    private static double previousLongArc=-1,longSwordAngle=-Math.PI/2;
    private static long lastLongWorldTick=Long.MIN_VALUE,longSwordFirstWorldTick=-1;
    private static long lastLongTrailTick=Long.MIN_VALUE,longSwordFirstTrailTick=-1;
    private static int timeWrapCaptureState;
    private static final int[] longArcCaptureState={0,0};
    private static final double LONG_SWORD_SPEED=12,LONG_SWORD_RADIUS=48;
    private static NativeImage previousLongBoundaryImage;
    private static Map<String,Object> previousLongBoundaryRow,previousLongBoundaryMetrics;
    private static CameraType expectedCamera=CameraType.FIRST_PERSON;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished)return;
        // Remote fixtures move after entity ticks and before the production
        // END history samplers. Real local keys must instead precede vanilla
        // LocalPlayer input and production END double-jump consumption, as in
        // the independently verified CameraCycleCapture. END key injection
        // would toggle our logical flight one tick before vanilla's toggle.
        if(event.phase!=(tick>=REMOTE_END&&tick<LONG_SWORD_BEGIN?TickEvent.Phase.START:TickEvent.Phase.END))return;
        Minecraft mc=Minecraft.getInstance();
        try{
            if((System.nanoTime()-START)/1_000_000_000>600)throw new IllegalStateException("Flight-tier fixture exceeded 600-second watchdog");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.createDirectories(OUTPUT);Files.createDirectories(SEQUENCE);
                mc.getWindow().setWindowed(1280,720);mc.options.renderDistance().set(3);mc.options.simulationDistance().set(3);mc.options.framerateLimit().set(60);
                mc.options.fov().set(65);mc.options.bobView().set(false);mc.options.guiScale().set(2);mc.options.hideGui=true;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("flight-tiers-"+System.currentTimeMillis(),
                    new LevelSettings("Flight tier visual verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.screen!=null)mc.setScreen(null);
            if(!prepared){
                prepared=true;HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);
                if(BLOOM_DISABLED)System.setProperty("wuxingxiantu.meteorBloomDisabled","true");else System.clearProperty("wuxingxiantu.meteorBloomDisabled");
                UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    for(int x=-8;x<=500;x++)for(int z=-6;z<=6;z++)level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                    for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=131;y<=142;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                    p.teleportTo(0,ALTITUDE,-18);p.setInvulnerable(true);p.setNoGravity(true);
                    var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
                    level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);level.setWeatherParameters(6000,0,false,false);GeneratedMod.sync(p);
                    // Observation phase does not pre-enable ordinary flight.
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            collectServerSample();
            if(subject==null){
                subject=new RemotePlayer(mc.level,new GameProfile(SUBJECT_ID,"FlightTierSubject"));subject.setPos(lastPosition);subject.setNoGravity(true);subject.setInvulnerable(true);
                subject.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));subject.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));subject.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));
                mc.level.addPlayer(-56801,subject);cameraPosition=Camera.class.getDeclaredMethod("setPosition",Vec3.class);cameraPosition.setAccessible(true);
                cameraRotation=Camera.class.getDeclaredMethod("setRotation",float.class,float.class);cameraRotation.setAccessible(true);
            }
            // Several game ticks may run between frames. Hold this scene until
            // the real target render has reached RenderTick.END and was saved.
            if(pendingCapture!=null||pendingSequence!=null||pendingCameraCapture!=null||pendingLongSwordCapture!=null&&!scene.equals("sword-long"))return;
            if(tick==REMOTE_END&&!localReady){prepareLocal(mc);return;}
            if(tick==BASE_FINISH_TICK&&!cameraReady){prepareCameraPhase(mc,false);return;}
            if(tick==BASE_FINISH_TICK+120&&!cameraCollisionReady){prepareCameraPhase(mc,true);return;}
            if(tick==LONG_SWORD_BEGIN&&!longSwordPrepared){prepareLongSword(mc);return;}
            tick++;
            if(tick==20){
                if(ModList.get().isLoaded("oculus")){Class<?> iris=Class.forName("net.irisshaders.iris.Iris");packName=String.valueOf(iris.getMethod("getCurrentPackName").invoke(null));}
                check("actual_shader_pack",TrailShaderBridge.shaderPackDetected()==EXPECT_PACK,"Expected="+EXPECT_PACK+", actual="+TrailShaderBridge.shaderPackDetected()+", pack="+packName);
            }
            if(tick<=REALM_END)updateRealm(mc);
            else if(tick<=SWORD_END)updateSword(mc);
            else if(tick<=MOTION_END)updateMotion(mc,tick-SWORD_END);
            else if(tick<=REMOTE_END)updateRemoteFixture(mc,tick-MOTION_END);
            else if(tick<=BASE_FINISH_TICK)updateLocal(mc,tick-REMOTE_END);
            else if(tick<=LONG_SWORD_BEGIN)updateCameraPhase(mc,tick-BASE_FINISH_TICK);
            else updateLongSword(mc,tick-LONG_SWORD_BEGIN);
            if(tick==LONG_SWORD_BEGIN){
                cameraCheck("camera_complete_images",CAMERA_IMAGES.size()==EXPECTED_CAMERA_IMAGES,"Actual camera images="+CAMERA_IMAGES.size()+", expected="+EXPECTED_CAMERA_IMAGES);
                cameraCheck("camera_complete_F5_inputs",CAMERA_F5_EVENTS.size()==5,"Actual separate camera-phase F5 inputs="+CAMERA_F5_EVENTS.size());
            }
        }catch(Throwable error){FAILURES.add("Fixture exception: "+error);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_TIER_CAPTURE",error);finish();}
    }
    private static void updateRealm(Minecraft mc){
        scene="realm";int section=(tick-1)/SECTION_TICKS,step=(tick-1)%SECTION_TICKS;realm=section+3;swordPhase=-1;remoteCamera=true;mc.options.hideGui=true;
        if(step==0)setSceneTime(mc,6000);
        if(step==112)setSceneTime(mc,18000);
        Vec3 position=flightPath(step);moveSubject(position);
        actualPacket=new RealmFlightVisualPacket(SUBJECT_ID,true,realm,3,false,true);RealmFlightVisuals.receive(actualPacket);
        boolean close=step==48||step>=60&&step<=109||step>=114;
        cameraOffset=close?new Vec3(-4,2.2,9):step>=110?new Vec3(-24,12,75):new Vec3(-9,5,31);lookTarget=position.add(close?-1.8:-6,.9,0);
        observeAround(mc,position);
        if(step==36)pendingCapture="realm-"+realm+"-"+REALM_NAMES[section]+"-side.png";
        if(step==48)pendingCapture="realm-"+realm+"-"+REALM_NAMES[section]+"-close-day.png";
        if(step==90)pendingCapture="realm-"+realm+"-"+REALM_NAMES[section]+"-turn.png";
        if(step==110)pendingCapture="realm-"+realm+"-"+REALM_NAMES[section]+"-far.png";
        if(step==116)pendingCapture="realm-"+realm+"-"+REALM_NAMES[section]+"-close-night.png";
        if(step>=70&&step<=108&&step%2==0)pendingSequence="realm-"+realm+"-"+REALM_NAMES[section]+"/frame-"+String.format(Locale.ROOT,"%03d",(step-70)/2)+".png";
    }
    /** Identical 20 Hz straight path followed by a 90-degree arc for every tier. */
    private static Vec3 flightPath(int step){
        if(step<60)return new Vec3(step*.65,ALTITUDE,0);
        double angle=(step-60)*.0275;
        return new Vec3(39+24*Math.sin(angle),ALTITUDE+Math.sin(angle*.7)*.7,24*(1-Math.cos(angle)));
    }
    private static void moveSubject(Vec3 position){
        Vec3 delta=position.subtract(lastPosition);subject.setOldPosAndRot();subject.xOld=lastPosition.x;subject.yOld=lastPosition.y;subject.zOld=lastPosition.z;subject.setPos(position);subject.setDeltaMovement(delta);
        float yaw=delta.horizontalDistance()>1e-5?(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)):-90;
        subject.setYRot(yaw);subject.yBodyRot=yaw;subject.yHeadRot=yaw;subject.setXRot(-5);lastPosition=position;
    }
    private static void observeAround(Minecraft mc,Vec3 position){
        mc.player.setNoGravity(true);
        if(tick%20==0){UUID id=mc.player.getUUID();Vec3 target=position.add(0,0,-18);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);if(p!=null)p.teleportTo(target.x,target.y,target.z);});}
    }
    /** Match the actual observer to the remote camera so vanilla loaded/compiled sections cover the same view. */
    private static void observeRemoteCamera(Minecraft mc){
        // The bounded long run fits the production 64-block local-player
        // distance gate from the center. Keep its chunk observer stationary;
        // only the actual detached fixture camera travels around this path.
        if(scene.startsWith("sword-long")){holdLongSwordObserver(mc);return;}
        Vec3 camera=lookTarget.add(cameraOffset),toward=lookTarget.subtract(camera);
        float yaw=(float)Math.toDegrees(Math.atan2(-toward.x,toward.z)),pitch=(float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance()));
        Vec3 feet=camera.add(0,-mc.player.getEyeHeight(),0);
        mc.player.setNoGravity(true);mc.player.setDeltaMovement(Vec3.ZERO);mc.player.setPos(feet);mc.player.setOldPosAndRot();
        mc.player.xo=feet.x;mc.player.yo=feet.y;mc.player.zo=feet.z;
        mc.player.setYRot(yaw);mc.player.yRotO=yaw;mc.player.yBodyRot=mc.player.yBodyRotO=yaw;mc.player.yHeadRot=mc.player.yHeadRotO=yaw;
        mc.player.setXRot(pitch);mc.player.xRotO=pitch;
        if(scene.equals("sword-long")||tick%20==0){UUID id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);if(p!=null){p.teleportTo(feet.x,feet.y,feet.z);p.setYRot(yaw);p.setXRot(pitch);}});}
    }
    private static void holdLongSwordObserver(Minecraft mc){
        mc.player.setPos(0,205,0);mc.player.setOldPosAndRot();mc.player.xo=0;mc.player.yo=205;mc.player.zo=0;mc.player.setNoGravity(true);mc.player.setDeltaMovement(Vec3.ZERO);mc.player.getAbilities().flying=false;
    }
    private static void setSceneTime(Minecraft mc,long dayTime){
        UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();level.setDayTime(dayTime);
            // Use the real vanilla time packet rather than waiting for its
            // periodic 20-tick broadcast between adjacent diagnostic scenes.
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(level.getGameTime(),dayTime,false));
        });
    }
    private static void updateSword(Minecraft mc){
        scene="sword";int section=(tick-REALM_END-1)/SECTION_TICKS,step=(tick-REALM_END-1)%SECTION_TICKS;swordPhase=section;realm=0;remoteCamera=true;mc.options.hideGui=true;
        if(step==0)setSceneTime(mc,6000);
        if(step==112)setSceneTime(mc,18000);
        actualPacket=new RealmFlightVisualPacket(SUBJECT_ID,false,8,3,false,false);RealmFlightVisuals.receive(actualPacket);
        if(sword==null){
            sword=ModEntities.CONTROLLED_SWORD.get().create(mc.level);if(sword==null)throw new IllegalStateException("Registered controlled sword did not create");
            sword.setUUID(UUID.fromString("747e755d-dd86-4e12-82a6-aa5370fe5a87"));sword.ownerId=SUBJECT_ID;sword.flightRoot(true);sword.flightState(FlyingSwordState.FLYING);sword.setNoGravity(true);
            var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,"dark_iron_sword"));sword.syncFlightDisplay(new ItemStack(item==null?Items.DIAMOND_SWORD:item));
            mc.level.putNonPlayerEntity(-56802,sword);subject.startRiding(sword,true);
        }
        Vec3 position=flightPath(step).add(0,-.8,0),old=sword.position(),delta=position.subtract(old);sword.setOldPosAndRot();sword.xOld=old.x;sword.yOld=old.y;sword.zOld=old.z;sword.setPos(position);sword.setDeltaMovement(delta);
        float yaw=step==0?-90:(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));sword.setYRot(yaw);sword.setXRot(-2);sword.advancedPhase(SWORD_PHASES[section]);sword.flightSpeed(.65F);sword.sprinting(section>0);sword.angularSpeed(step>=60?.0275F:0);sword.roll(step>=60?-.20F:0);sword.bank(step>=60?-.20F:0);
        sword.jinghongStrength(section==2?.5F:0);sword.dunxuStage(section==2?2:0);
        boolean close=step==48||step>=60;
        moveSubject(position.add(0,sword.riderOffset(),0));observeAround(mc,position);cameraOffset=close?new Vec3(-4,2,9):new Vec3(-8,4,24);lookTarget=position.add(close?-1.6:-4,.8,0);
        if(step==36)pendingCapture="sword-"+SWORD_PHASES[section].name().toLowerCase(Locale.ROOT)+"-side.png";
        if(step==48)pendingCapture="sword-"+SWORD_PHASES[section].name().toLowerCase(Locale.ROOT)+"-close-day.png";
        if(step==90)pendingCapture="sword-"+SWORD_PHASES[section].name().toLowerCase(Locale.ROOT)+"-turn.png";
        if(step==116)pendingCapture="sword-"+SWORD_PHASES[section].name().toLowerCase(Locale.ROOT)+"-close-night.png";
        if(step>=70&&step<=108&&step%2==0)pendingSequence="sword-"+SWORD_PHASES[section].name().toLowerCase(Locale.ROOT)+"/frame-"+String.format(Locale.ROOT,"%03d",(step-70)/2)+".png";
    }
    private static void prepareLocal(Minecraft mc){
        if(!localPreparationRequested){
            localPreparationRequested=true;remoteCamera=false;scene="local";mc.options.renderDistance().set(3);mc.options.hideGui=false;mc.options.setCameraType(CameraType.FIRST_PERSON);expectedCamera=CameraType.FIRST_PERSON;mc.player.setNoGravity(false);
            subject.stopRiding();if(sword!=null){mc.level.removeEntity(sword.getId(),Entity.RemovalReason.DISCARDED);sword=null;}RealmFlightVisuals.receive(new RealmFlightVisualPacket(SUBJECT_ID,false,8,3,false,false));mc.level.removeEntity(subject.getId(),Entity.RemovalReason.DISCARDED);
            if(secondSubject!=null){RealmFlightVisuals.receive(new RealmFlightVisualPacket(ALEX_ID,false,8,3,false,false));mc.level.removeEntity(secondSubject.getId(),Entity.RemovalReason.DISCARDED);secondSubject=null;}
            UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.teleportTo(0,131,0);p.setNoGravity(false);p.getAbilities().flying=false;p.onUpdateAbilities();p.serverLevel().setDayTime(6000);GeneratedMod.sync(p);});return;
        }
        mc.player.setNoGravity(false);
        if(mc.player.onGround()&&mc.player.getAbilities().mayfly&&RealmFlightControl.eligible(mc.player)){
            localReady=true;realm=8;check("local_initial_not_flying",!mc.player.getAbilities().flying,"Survival player landed; production mayfly permission present; no fixture flight grant");
        }
    }
    /** Speed changes, both bank directions and the real production stop fade. */
    private static void updateMotion(Minecraft mc,int step){
        scene="motion";realm=4;swordPhase=-1;remoteCamera=true;mc.options.hideGui=true;
        if(step==1){
            subject.stopRiding();if(sword!=null){mc.level.removeEntity(sword.getId(),Entity.RemovalReason.DISCARDED);sword=null;}
            setSceneTime(mc,6000);motionPosition=new Vec3(0,ALTITUDE,0);motionYaw=0;
            moveSubject(motionPosition);
        }
        double cruise=cn.blockforge.generated.generatedmod.realmflight.RealmFlightBalance.speed(4,3,false)/20;
        double fast=cn.blockforge.generated.generatedmod.realmflight.RealmFlightBalance.speed(4,3,true)/20;
        double speed=step<16?0:step<46?cruise:step<66?cruise+(fast-cruise)*(step-45)/20:step<111?fast:step<131?fast*(131-step)/20:0;
        if(step>=81&&step<=95)motionYaw-=.075;
        if(step>=96&&step<=110)motionYaw+=.075;
        motionPosition=motionPosition.add(Math.cos(motionYaw)*speed,0,Math.sin(motionYaw)*speed);
        moveSubject(motionPosition);observeAround(mc,motionPosition);
        actualPacket=new RealmFlightVisualPacket(SUBJECT_ID,step<131,4,3,false,step>=46&&step<111);RealmFlightVisuals.receive(actualPacket);
        cameraOffset=new Vec3(-4,2.2,9);lookTarget=motionPosition.add(-1.8,.9,0);
        String tag=switch(step){case 42->"motion-normal";case 78->"motion-fast";case 93->"motion-left-bank";case 108->"motion-right-bank";case 128->"motion-slow";case 138->"motion-stopping";case 179->"motion-stopped";default->null;};
        if(tag!=null)pendingCapture=tag+".png";
    }
    /** Client-only controlled players in an isolated world, not a LAN/network integration test. */
    private static final class FixtureRemotePlayer extends RemotePlayer {
        private final boolean slim;
        FixtureRemotePlayer(ClientLevel level,UUID id,String name,boolean slim){super(level,new GameProfile(id,name));this.slim=slim;}
        @Override public String getModelName(){return slim?"slim":"default";}
        @Override public ResourceLocation getSkinTextureLocation(){return new ResourceLocation("minecraft",slim?"textures/entity/player/slim/alex.png":"textures/entity/player/wide/steve.png");}
    }
    private static RemotePlayer fixturePlayer(Minecraft mc,UUID id,int entityId,boolean slim,Vec3 position){
        var player=new FixtureRemotePlayer(mc.level,id,slim?"FlightFixtureAlex":"FlightFixtureSteve",slim);
        player.setPos(position);player.setOldPosAndRot();player.setNoGravity(true);player.setInvulnerable(true);
        player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(slim?Items.DIAMOND_HELMET:Items.NETHERITE_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(slim?Items.DIAMOND_CHESTPLATE:Items.NETHERITE_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(slim?Items.DIAMOND_LEGGINGS:Items.NETHERITE_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET,new ItemStack(slim?Items.DIAMOND_BOOTS:Items.NETHERITE_BOOTS));
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(slim?Items.DIAMOND_AXE:Items.NETHERITE_SWORD));
        player.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(slim?Items.TOTEM_OF_UNDYING:Items.SHIELD));
        mc.level.addPlayer(entityId,player);return player;
    }
    private static void moveFixturePlayer(RemotePlayer player,Vec3 position){
        Vec3 old=player.position(),delta=position.subtract(old);player.setOldPosAndRot();player.xOld=old.x;player.yOld=old.y;player.zOld=old.z;
        player.yBodyRotO=player.yBodyRot;player.yHeadRotO=player.yHeadRot;player.setPos(position);player.setDeltaMovement(delta);
        if(delta.lengthSqr()>1e-8){float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));player.setYRot(yaw);player.yBodyRot=yaw;player.yHeadRot=yaw;player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));}
    }
    private static void updateRemoteFixture(Minecraft mc,int stageStep){
        remoteCamera=true;mc.options.hideGui=true;swordPhase=-1;mc.options.fov().set(65);mc.options.renderDistance().set(6);
        if(stageStep==1){stage2CheckStart=CHECKS.size();stage2CaptureStart=CAPTURES.size();setSceneTime(mc,6000);}
        if(stageStep<=240){
            scene="stage2-remote";int section=(stageStep-1)/120;remoteStep=(stageStep-1)%120+1;realm=section==0?4:8;
            if(remoteStep==1){
                RealmFlightVisuals.receive(new RealmFlightVisualPacket(SUBJECT_ID,false,realm,3,false,false));
                mc.level.removeEntity(subject.getId(),Entity.RemovalReason.DISCARDED);
                // Keep all three velocity-relative views above the 130m observation platform, including dives.
                remotePosition=new Vec3(0,STAGE2_ALTITUDE,0);subject=fixturePlayer(mc,SUBJECT_ID,-56801,false,remotePosition);
            }
            // Identical displacement, tick index, camera offset and FOV for both realms.
            double yaw=remoteStep<73?-90:remoteStep<74?179:remoteStep<75?180:remoteStep<85?-179:1;
            double vertical=remoteStep>=31&&remoteStep<51?.34:remoteStep>=51&&remoteStep<71?-.34:0;
            remoteVelocity=new Vec3(-Math.sin(Math.toRadians(yaw))*.75,vertical,Math.cos(Math.toRadians(yaw))*.75);
            remotePosition=remotePosition.add(remoteVelocity);moveFixturePlayer(subject,remotePosition);lastPosition=remotePosition;
            actualPacket=new RealmFlightVisualPacket(SUBJECT_ID,true,realm,3,false,true);RealmFlightVisuals.receive(actualPacket);
            int captureStart=remoteStep>=26&&remoteStep<=28?26:remoteStep>=42&&remoteStep<=44?42:remoteStep>=62&&remoteStep<=64?62:remoteStep>=78&&remoteStep<=80?78:remoteStep>=90&&remoteStep<=92?90:-1;
            remotePath=captureStart==26?"straight":captureStart==42?"climb":captureStart==62?"dive":captureStart==78?"heading-wrap":captureStart==90?"sharp-reversal":"transit";
            remoteView=captureStart<0?"rear":remoteStep-captureStart==0?"front":remoteStep-captureStart==1?"rear":"oblique-rear";
            Vec3 forward=remoteVelocity.normalize(),side=forward.cross(new Vec3(0,1,0)).normalize();
            lookTarget=remotePosition.add(0,subject.getBbHeight()*.5,0);
            // Keep every rear observer beyond the longest possible wake.
            double rearDistance=RealmMeteorTuning.MAX_TRAIL_LENGTH+12;
            cameraOffset=remoteView.equals("front")?forward.scale(40):remoteView.equals("rear")?forward.scale(-rearDistance):forward.scale(-rearDistance).add(side.scale(18)).add(0,5,0);
            observeRemoteCamera(mc);
            if(captureStart>=0)pendingCapture="stage2-realm-"+realm+"-"+remotePath+"-"+remoteView+".png";
        }else{
            scene="stage2-pair";realm=4;remoteStep=stageStep-240;remotePath="independent-fast-slow-stop";remoteView="pair-side-midpoint";
            if(remoteStep==1){
                RealmFlightVisuals.receive(new RealmFlightVisualPacket(SUBJECT_ID,false,4,3,false,false));mc.level.removeEntity(subject.getId(),Entity.RemovalReason.DISCARDED);
                remotePosition=new Vec3(0,STAGE2_ALTITUDE,-6);secondPosition=new Vec3(0,STAGE2_ALTITUDE,6);
                subject=fixturePlayer(mc,SUBJECT_ID,-56801,false,remotePosition);secondSubject=fixturePlayer(mc,ALEX_ID,-56803,true,secondPosition);
            }
            boolean steveActive=remoteStep<61;remoteVelocity=new Vec3(steveActive?1.1:0,0,0);
            remotePosition=remotePosition.add(remoteVelocity);secondPosition=secondPosition.add(.38,.08*Math.sin(remoteStep*.08),0);
            moveFixturePlayer(subject,remotePosition);moveFixturePlayer(secondSubject,secondPosition);lastPosition=remotePosition;
            actualPacket=new RealmFlightVisualPacket(SUBJECT_ID,steveActive,4,3,false,steveActive);actualSecondPacket=new RealmFlightVisualPacket(ALEX_ID,true,8,3,false,false);
            RealmFlightVisuals.receive(actualPacket);RealmFlightVisuals.receive(actualSecondPacket);
            lookTarget=remotePosition.add(secondPosition).scale(.5).add(0,.9,0);
            // Side-on midpoint framing keeps both heads at comparable distances, including the stopped player.
            // The extra 6m covers the nearer lane: even it remains max tail length + 8m from this camera.
            double pairDistance=Math.max(32,Math.max(RealmMeteorTuning.MAX_TRAIL_LENGTH+14,remotePosition.distanceTo(secondPosition)*.8));
            cameraOffset=new Vec3(0,8,pairDistance);observeRemoteCamera(mc);
            String tag=switch(remoteStep){case 43->"both-active";case 59->"distinct-speeds";case 82->"steve-stopping-alex-active";case 112->"steve-stopped-alex-active";default->null;};
            if(tag!=null)pendingCapture="stage2-pair-"+tag+".png";
        }
    }
    private static void updateLocal(Minecraft mc,int step){
        scene="local";realm=8;swordPhase=-1;remoteCamera=false;mc.options.hideGui=false;mc.player.setYRot(-90);mc.player.setXRot(0);
        mc.options.keyJump.setDown(step==20||step==24||step>=30&&step<=50);mc.options.keyUp.setDown(step>=55&&step<=220);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(false);
        if(step==55)localMovementStart=mc.player.position();
        if(step==70||step==100||step==130||step==170||step==200)clickF5(mc);
        if(step==160)setSceneTime(mc,18000);
        if(step==220)check("local_actual_W_motion",mc.player.position().subtract(localMovementStart).horizontalDistance()>8,"Actual local W displacement="+mc.player.position().subtract(localMovementStart).horizontalDistance());
        String tag=switch(step){case 60->"local-day-first";case 90->"local-day-back";case 120->"local-day-front";case 150->"local-day-first-return";case 190->"local-night-back";case 220->"local-night-front";default->null;};
        if(tag!=null)pendingCapture=tag+".png";
    }
    private static void clickF5(Minecraft mc){
        var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("before",mc.options.getCameraType().name());expectedCamera=mc.options.getCameraType().cycle();row.put("expectedAfter",expectedCamera.name());F5_EVENTS.add(row);KeyMapping.click(mc.options.keyTogglePerspective.getKey());
    }
    /** Additional actual local camera evidence; the original local flight phase is unchanged. */
    private static void prepareCameraPhase(Minecraft mc,boolean collision){
        scene="camera";realm=8;swordPhase=-1;remoteCamera=false;mc.options.hideGui=false;
        mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyShift.setDown(false);
        boolean requested=collision?cameraCollisionRequested:cameraPreparationRequested;
        if(!requested){
            if(collision)cameraCollisionRequested=true;else{cameraPreparationRequested=true;mc.options.setCameraType(CameraType.FIRST_PERSON);expectedCamera=CameraType.FIRST_PERSON;}
            UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.teleportTo(CAMERA_START.x,CAMERA_START.y,CAMERA_START.z);p.setDeltaMovement(Vec3.ZERO);p.setYRot(0);p.setXRot(0);
                if(collision)for(int x=-80;x<=120;x++)for(int y=167;y<=177;y++)p.serverLevel().setBlock(new BlockPos(x,y,3),Blocks.STONE.defaultBlockState(),2);
                p.serverLevel().setDayTime(6000);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(p.serverLevel().getGameTime(),6000,false));
            });return;
        }
        if(mc.player.position().distanceTo(CAMERA_START)<1&&mc.player.getAbilities().flying
                &&(!collision||mc.level.getBlockState(new BlockPos(40,171,3)).is(Blocks.STONE))){
            if(collision)cameraCollisionReady=true;else cameraReady=true;
        }
    }
    private static void updateCameraPhase(Minecraft mc,int step){
        scene="camera";realm=8;swordPhase=-1;remoteCamera=false;mc.options.hideGui=false;
        mc.player.setYRot(0);mc.player.setXRot(step>=31&&step<=33?30:step>=83&&step<=85?-30:0);
        mc.options.keyJump.setDown(false);mc.options.keyShift.setDown(false);
        mc.options.keyUp.setDown(step>=2&&step<=104);
        mc.options.keyRight.setDown(step>=121);
        mc.options.keySprint.setDown(step>=13&&step<=35||step>=66&&step<=88||step>=121);
        if(step==1||step==55||step==108||step==119||step==120)clickCameraF5(mc);
        String tag=switch(step){
            case 12->"camera-back-cruise";case 30->"camera-back-boost";case 32->"camera-back-boost-high-angle";case 52->"camera-back-release";
            case 64->"camera-front-cruise";case 82->"camera-front-boost";case 84->"camera-front-boost-high-angle";case 104->"camera-front-release";
            case 118->"camera-first-person";case 138->"camera-front-wall-clipped";default->null;
        };
        if(tag!=null)pendingCameraCapture=tag+".png";
    }
    private static void clickCameraF5(Minecraft mc){
        var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("before",mc.options.getCameraType().name());expectedCamera=mc.options.getCameraType().cycle();row.put("expectedAfter",expectedCamera.name());CAMERA_F5_EVENTS.add(row);KeyMapping.click(mc.options.keyTogglePerspective.getKey());
    }
    /** True 2600-client-tick run; history and EffectTime are never seeded or overwritten. */
    private static void prepareLongSword(Minecraft mc){
        if(!prepareLongSwordSections(mc))return;
        scene="sword-long";realm=0;swordPhase=2;remoteCamera=true;mc.options.hideGui=true;
        mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(false);mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.player.setNoGravity(true);mc.player.getAbilities().flying=false;mc.player.onUpdateAbilities();
        Vec3 start=new Vec3(0,205,-LONG_SWORD_RADIUS);
        subject=new RemotePlayer(mc.level,new GameProfile(SUBJECT_ID,"LongBoostSwordSubject"));subject.setPos(start);subject.setNoGravity(true);subject.setInvulnerable(true);
        subject.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));subject.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));subject.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));mc.level.addPlayer(-56801,subject);lastPosition=start;
        sword=ModEntities.CONTROLLED_SWORD.get().create(mc.level);if(sword==null)throw new IllegalStateException("Long-run registered sword did not create");
        sword.setUUID(UUID.fromString("0f36d55a-e6a3-49d2-a0bf-cb10805b7b17"));sword.ownerId=SUBJECT_ID;sword.flightRoot(true);sword.flightState(FlyingSwordState.FLYING);sword.setNoGravity(true);sword.setPos(start);sword.setYRot(-90);sword.setXRot(0);
        var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,"dark_iron_sword"));sword.syncFlightDisplay(new ItemStack(item==null?Items.DIAMOND_SWORD:item));sword.advancedPhase(YujianPhase.DUNXU);sword.flightSpeed((float)LONG_SWORD_SPEED);sword.sprinting(true);sword.dunxuStage(2);mc.level.putNonPlayerEntity(-56802,sword);subject.startRiding(sword,true);
        lookTarget=start.add(-3,.9,0);cameraOffset=new Vec3(-4,4,24);observeRemoteCamera(mc);longSwordPrepared=true;
    }
    /** Preflight actual path sections; no sword, trail history, arc or clock is seeded. */
    private static boolean prepareLongSwordSections(Minecraft mc){
        scene="sword-long-preflight";remoteCamera=true;mc.options.hideGui=true;
        holdLongSwordObserver(mc);
        mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(false);mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(!longSwordPreflightRequested){
            if(sword!=null)throw new IllegalStateException("Long-run section preflight must precede sword creation");
            longSwordPreflightRequested=true;longSwordPreflightStartedNanos=System.nanoTime();previousLongRenderDistance=mc.options.renderDistance().get();previousLongCloudStatus=mc.options.cloudStatus().get();
            mc.options.renderDistance().set(12);mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
            Vec3 center=new Vec3(0,205,0);mc.player.setPos(center);mc.player.setOldPosAndRot();mc.player.xo=0;mc.player.yo=205;mc.player.zo=0;mc.player.setNoGravity(true);mc.player.setDeltaMovement(Vec3.ZERO);
            var unique=new LinkedHashMap<Long,BlockPos>();double angularStep=2*Math.asin(LONG_SWORD_SPEED/(2*LONG_SWORD_RADIUS));
            for(int step=0;step<=LONG_SWORD_TICKS;step++){
                double angle=-Math.PI/2+step*angularStep;int x=(int)Math.floor(LONG_SWORD_RADIUS*Math.cos(angle)),z=(int)Math.floor(LONG_SWORD_RADIUS*Math.sin(angle));
                int chunkX=Math.floorDiv(x,16),chunkZ=Math.floorDiv(z,16);long key=((long)chunkX<<32)^(chunkZ&0xffffffffL);
                unique.putIfAbsent(key,new BlockPos(chunkX*16+8,205,chunkZ*16+8));
            }
            LONG_SWORD_PREFLIGHT_BLOCKS.addAll(unique.values());UUID id=mc.player.getUUID();
            preparation=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);previousLongServerViewDistance=p.server.getPlayerList().getViewDistance();p.server.getPlayerList().setViewDistance(10);p.teleportTo(0,205,0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);});
            return false;
        }
        if(System.nanoTime()-longSwordPreflightStartedNanos>90_000_000_000L)throw new IllegalStateException("Long-run actual path sections did not compile within90 seconds");
        if(preparation!=null){if(!preparation.isDone())return false;preparation.join();preparation=null;}
        // Repeat the survey if any earlier section lost its actual compiled state.
        if(longSwordPreflightIndex==LONG_SWORD_PREFLIGHT_BLOCKS.size()){
            for(int i=0;i<LONG_SWORD_PREFLIGHT_BLOCKS.size();i++)if(!mc.levelRenderer.isChunkCompiled(LONG_SWORD_PREFLIGHT_BLOCKS.get(i))){longSwordPreflightIndex=i;break;}
            if(longSwordPreflightIndex==LONG_SWORD_PREFLIGHT_BLOCKS.size())return true;
        }
        BlockPos block=LONG_SWORD_PREFLIGHT_BLOCKS.get(longSwordPreflightIndex);lookTarget=Vec3.atCenterOf(block);cameraOffset=new Vec3(-4,4,24);
        boolean present=mc.level.getChunkSource().hasChunk(Math.floorDiv(block.getX(),16),Math.floorDiv(block.getZ(),16));
        if(present&&mc.levelRenderer.isChunkCompiled(block)){
            var row=new LinkedHashMap<String,Object>();row.put("block",List.of(block.getX(),block.getY(),block.getZ()));row.put("clientChunkPresent",true);row.put("compiled",true);row.put("swordNotCreated",sword==null);row.put("scriptTick",tick);row.put("worldTick",mc.level.getGameTime());row.put("productionSamplerTick",FlyingSwordEffects.samplerTick());
            if(longSwordPreflightIndex<LONG_SWORD_PREFLIGHT_SECTIONS.size())LONG_SWORD_PREFLIGHT_SECTIONS.set(longSwordPreflightIndex,row);else LONG_SWORD_PREFLIGHT_SECTIONS.add(row);
            longSwordPreflightIndex++;
        }else longSwordPreflightWaitTicks++;
        return false;
    }
    private static void updateLongSword(Minecraft mc,int step)throws ReflectiveOperationException{
        scene="sword-long";realm=0;swordPhase=2;remoteCamera=true;mc.options.hideGui=true;
        longSwordAngle+=2*Math.asin(LONG_SWORD_SPEED/(2*LONG_SWORD_RADIUS));
        Vec3 position=new Vec3(LONG_SWORD_RADIUS*Math.cos(longSwordAngle),205,LONG_SWORD_RADIUS*Math.sin(longSwordAngle)),old=sword.position(),delta=position.subtract(old);
        float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));sword.setOldPosAndRot();sword.xOld=old.x;sword.yOld=old.y;sword.zOld=old.z;sword.setPos(position);sword.setDeltaMovement(delta);sword.setYRot(yaw);sword.setXRot(0);sword.flightSpeed((float)LONG_SWORD_SPEED);sword.sprinting(true);sword.angularSpeed((float)(LONG_SWORD_SPEED/LONG_SWORD_RADIUS));sword.roll(0);sword.bank(0);sword.jinghongStrength(.5F);sword.dunxuStage(2);
        moveSubject(position.add(0,sword.riderOffset(),0));Vec3 tangent=delta.normalize(),right=new Vec3(-tangent.z,0,tangent.x);lookTarget=position.add(tangent.scale(-4)).add(0,.9,0);cameraOffset=right.scale(24).add(tangent.scale(-4)).add(0,4,0);observeRemoteCamera(mc);
        String name=step==40?"long-sword-start.png":step==1300?"long-sword-middle.png":step==2580?"long-sword-end.png":null;
        if(name!=null)pendingLongSwordCapture=name;
    }
    private static SwordFlightTrailHistory longSwordHistory()throws ReflectiveOperationException{return (SwordFlightTrailHistory)staticMap(FlyingSwordEffects.class,"TRAILS").get(sword);}
    private static FlyingSwordEffects.TrailDiagnostics longSwordDiagnostic(){
        return FlyingSwordEffects.trailDiagnostics().stream().filter(item->sword!=null&&item.swordId()==sword.getId()).findFirst().orElse(null);
    }
    private static Map<String,Object> longSwordRenderDiagnostic(Minecraft mc){
        var row=new LinkedHashMap<String,Object>();float partial=mc.getFrameTime();
        row.put("scriptTick",tick);row.put("longSwordStep",tick-LONG_SWORD_BEGIN);row.put("worldTick",mc.level.getGameTime());row.put("actualTrailTick",FlyingSwordEffects.capturedTrailTick());row.put("productionSamplerTick",FlyingSwordEffects.samplerTick());row.put("partialTick",partial);
        row.put("registeredSwordId",sword.getId());row.put("registeredSwordUUID",sword.getUUID().toString());row.put("registeredSwordLookupKey",sword.getId());row.put("registeredSwordLookupMatches",mc.level.getEntity(sword.getId())==sword);row.put("registeredSwordType",ForgeRegistries.ENTITY_TYPES.getKey(sword.getType()).toString());row.put("actualRoot",sword.flightRoot());row.put("actualRider",subject.getVehicle()==sword);row.put("actualSprint",sword.sprinting());row.put("actualPhase",sword.advancedPhase().name());row.put("syncedSpeed",sword.flightSpeed());
        var transform=InterpolatedFlyingSwordTransform.get(sword,partial);row.put("sprintFactor",transform.sprintFactor());row.put("spiralFactor",transform.spiralFactor());row.put("renderSpeed",transform.speed());
        var diagnostic=longSwordDiagnostic();row.put("productionDiagnosticPresent",diagnostic!=null);
        if(diagnostic!=null){
            row.put("productionDiagnosticSwordId",diagnostic.swordId());
            row.put("historyNodes",diagnostic.historyNodes());row.put("firstArc",diagnostic.firstArc());row.put("headArc",diagnostic.headArc());row.put("origin",diagnostic.origin());row.put("headCoord",diagnostic.headCoord());row.put("effectTime",diagnostic.time());row.put("strength",diagnostic.strength());row.put("widthEnvelope",diagnostic.width());
            row.put("expectedWidthEnvelope",Math.min(.85,(.36+.18*transform.sprintFactor()+.09*transform.spiralFactor())*Math.sqrt(diagnostic.strength())));
            row.put("arcRebaseBucket",(long)Math.floor(diagnostic.firstArc()/SwordTrailPhase.ARC_PERIOD));row.put("arc1024Bucket",(long)Math.floor(diagnostic.firstArc()/1024));
            row.put("expectedOrigin",SwordTrailPhase.origin(diagnostic.firstArc(),sword.getUUID().hashCode()));row.put("expectedHeadCoord",SwordTrailPhase.coordinate(diagnostic.headArc(),diagnostic.origin()));row.put("expectedEffectTime",SwordTrailPhase.time(FlyingSwordEffects.capturedTrailTick(),partial));
        }
        row.put("productionCapturedEffectTime",FlyingSwordEffects.capturedEffectTime());row.put("actualSectionCompiled",mc.levelRenderer.isChunkCompiled(subject.blockPosition()));row.put("actualRiderPostRendered",PLAYER_DRAWS.containsKey(subject.getUUID()));row.put("actualObserverPosition",List.of(mc.player.getX(),mc.player.getY(),mc.player.getZ()));row.put("actualObserverDistanceToSword",sword.distanceTo(mc.player));row.put("actualCloudStatus",mc.options.cloudStatus().get().name());row.put("actualNativeDraws",SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart);row.put("actualPostDraws",SwordFlightTrailRenderer.postDraws-frameSwordPostStart);row.put("actualTrailSubmitted",FlyingSwordEffects.normalTrailSubmitted);row.put("actualTrailVertices",FlyingSwordEffects.normalTrailVertexCount);row.put("actualShaderReady",SwordFlightTrailRenderer.ready());
        return row;
    }
    private static void recordLongSwordRenderFrame(){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||sword==null||subject==null)return;
        var row=longSwordRenderDiagnostic(mc);row.put("renderFrameIndex",LONG_SWORD_RENDER_FRAMES.size());LONG_SWORD_RENDER_FRAMES.add(row);
    }
    /** Save the actual previous/current/next framebuffer around the material origin, not guessed head-arc ticks. */
    private static void captureLongSwordBoundaryFrames(Minecraft mc)throws Exception{
        var diagnostic=longSwordDiagnostic();if(diagnostic==null)return;
        String beforeName=null,nowName=null;boolean cache=false;
        Map<?,?> previous=previousLongBoundaryRow==null?null:(Map<?,?>)previousLongBoundaryRow.get("longSwordRenderEvidence");
        for(int index=0;index<2&&nowName==null;index++){
            double boundary=(index+1)*1024;String prefix="long-sword-arc-"+(int)boundary;
            if(longArcCaptureState[index]==2){nowName=prefix+"-after.png";longArcCaptureState[index]=3;}
            else if(longArcCaptureState[index]==0&&previous!=null&&((Number)previous.get("firstArc")).doubleValue()<boundary&&diagnostic.firstArc()>=boundary){beforeName=prefix+"-before.png";nowName=prefix+"-at.png";longArcCaptureState[index]=2;}
            if(longArcCaptureState[index]==0&&diagnostic.firstArc()<boundary&&boundary-diagnostic.firstArc()<30)cache=true;
        }
        if(nowName==null&&tick-LONG_SWORD_BEGIN>40){
            if(timeWrapCaptureState==2){nowName="long-sword-time-after.png";timeWrapCaptureState=3;}
            else if(timeWrapCaptureState==0&&previous!=null&&((Number)previous.get("effectTime")).doubleValue()>127.8&&diagnostic.time()<.15){beforeName="long-sword-time-before.png";nowName="long-sword-time-at.png";timeWrapCaptureState=2;}
        }
        if(timeWrapCaptureState==0&&diagnostic.time()>127.8)cache=true;
        if(beforeName!=null){
            previousLongBoundaryImage.writeToFile(OUTPUT.resolve(beforeName));previousLongBoundaryRow.put("longSwordScreenshot",beforeName);captureLongSwordEvidence(mc,previousLongBoundaryRow,previousLongBoundaryMetrics,beforeName);LONG_SWORD_IMAGES.add(previousLongBoundaryRow);
        }
        if(nowName!=null){
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){var metrics=pixels(image);var row=metadata(mc,metrics);row.put("longSwordRenderEvidence",longSwordRenderDiagnostic(mc));row.put("longSwordPhaseStep",tick-LONG_SWORD_BEGIN);row.put("longSwordRenderFrameIndex",LONG_SWORD_RENDER_FRAMES.size()-1);row.put("longSwordScreenshot",nowName);image.writeToFile(OUTPUT.resolve(nowName));captureLongSwordEvidence(mc,row,metrics,nowName);LONG_SWORD_IMAGES.add(row);}write();
        }
        if(cache){
            if(previousLongBoundaryImage!=null)previousLongBoundaryImage.close();previousLongBoundaryImage=Screenshot.takeScreenshot(mc.getMainRenderTarget());previousLongBoundaryMetrics=pixels(previousLongBoundaryImage);previousLongBoundaryRow=metadata(mc,previousLongBoundaryMetrics);previousLongBoundaryRow.put("longSwordRenderEvidence",longSwordRenderDiagnostic(mc));previousLongBoundaryRow.put("longSwordPhaseStep",tick-LONG_SWORD_BEGIN);previousLongBoundaryRow.put("longSwordRenderFrameIndex",LONG_SWORD_RENDER_FRAMES.size()-1);
        }else if(previousLongBoundaryImage!=null){previousLongBoundaryImage.close();previousLongBoundaryImage=null;previousLongBoundaryRow=null;previousLongBoundaryMetrics=null;}
    }
    private static void captureLongSwordEvidence(Minecraft mc,Map<String,Object> row,Map<String,Object> metrics,String name){
        @SuppressWarnings("unchecked") var actual=(Map<String,Object>)row.computeIfAbsent("longSwordRenderEvidence",unused->longSwordRenderDiagnostic(mc));row.putIfAbsent("longSwordPhaseStep",tick-LONG_SWORD_BEGIN);row.putIfAbsent("longSwordRenderFrameIndex",LONG_SWORD_RENDER_FRAMES.size()-1);
        row.put("longSwordScreenshotScope","Actual registered client ControlledSwordEntity and attached RemotePlayer on a bounded 12-block/client-tick path; production END sampler and renderer. History arc and EffectTime are never seeded. These are controlled client visuals, not a server-validated speed or multiplayer transport test.");
        longSwordCheck("long_sword_visible_"+name,!Boolean.TRUE.equals(metrics.get("blackScreenCandidate")),"Actual image visibility metrics="+metrics);
        boolean entityBinding=Boolean.TRUE.equals(actual.get("registeredSwordLookupMatches"))&&"0f36d55a-e6a3-49d2-a0bf-cb10805b7b17".equals(actual.get("registeredSwordUUID"))&&actual.get("registeredSwordId").equals(actual.get("productionDiagnosticSwordId"));
        longSwordCheck("long_sword_registered_boost_"+name,entityBinding&&"wuxingxiantu:controlled_sword".equals(actual.get("registeredSwordType"))&&Boolean.TRUE.equals(actual.get("actualRoot"))&&Boolean.TRUE.equals(actual.get("actualRider"))&&"DUNXU".equals(actual.get("actualPhase"))&&Boolean.TRUE.equals(actual.get("actualSprint"))&&Math.abs(((Number)actual.get("syncedSpeed")).doubleValue()-LONG_SWORD_SPEED)<.001&&((Number)actual.get("sprintFactor")).doubleValue()>.99,"Actual registered entity UUID/runtime ID/diagnostic binding, root/rider, phase, sprint and synced speed="+actual);
        longSwordCheck("long_sword_production_draw_"+name,Boolean.TRUE.equals(actual.get("productionDiagnosticPresent"))&&Boolean.TRUE.equals(actual.get("actualShaderReady"))&&Boolean.TRUE.equals(actual.get("actualSectionCompiled"))&&Boolean.TRUE.equals(actual.get("actualRiderPostRendered"))&&((Number)actual.get("actualNativeDraws")).intValue()+((Number)actual.get("actualPostDraws")).intValue()>0&&Boolean.TRUE.equals(actual.get("actualTrailSubmitted"))&&((Number)actual.get("actualTrailVertices")).intValue()>0,"Actual compiled section/rider Post/history/material submission="+actual);
        boolean phase=Boolean.TRUE.equals(actual.get("productionDiagnosticPresent"))&&((Number)actual.get("historyNodes")).intValue()>20&&((Number)actual.get("widthEnvelope")).doubleValue()>=.8&&Math.abs(((Number)actual.get("widthEnvelope")).doubleValue()-((Number)actual.get("expectedWidthEnvelope")).doubleValue())<.00001&&Math.abs(((Number)actual.get("origin")).doubleValue()-((Number)actual.get("expectedOrigin")).doubleValue())<.00001&&Math.abs(((Number)actual.get("headCoord")).doubleValue()-((Number)actual.get("expectedHeadCoord")).doubleValue())<.00001&&Math.abs(((Number)actual.get("effectTime")).doubleValue()-((Number)actual.get("expectedEffectTime")).doubleValue())<.00001&&Math.abs(((Number)actual.get("effectTime")).doubleValue()-((Number)actual.get("productionCapturedEffectTime")).doubleValue())<.00001;
        longSwordCheck("long_sword_history_phase_width_"+name,phase,"Actual continuous-history width and production spatial/time metadata="+actual);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void longSwordTickTelemetry(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END||tick<=LONG_SWORD_BEGIN||tick>FINISH_TICK||!scene.equals("sword-long"))return;
        int step=tick-LONG_SWORD_BEGIN;if(step<=longSwordRecordedStep)return;
        try{
            var mc=Minecraft.getInstance();var history=longSwordHistory();var nodes=history==null?List.<SwordFlightTrailHistory.Node>of():history.nodes();double arc=nodes.isEmpty()?0:nodes.get(nodes.size()-1).arc();long worldTick=mc.level.getGameTime(),trailTick=FlyingSwordEffects.samplerTick();
            boolean continuous=step==1||nodes.size()>20&&arc>previousLongArc+8&&trailTick==lastLongTrailTick+1;
            var row=new LinkedHashMap<String,Object>();row.put("step",step);row.put("worldTick",worldTick);row.put("actualTrailTick",trailTick);row.put("headArc",arc);row.put("historyNodes",nodes.size());row.put("syncedSpeed",sword.flightSpeed());row.put("actualSprint",sword.sprinting());row.put("continuousHistory",continuous);LONG_SWORD_TICKS_RECORDED.add(row);
            previousLongArc=arc;lastLongWorldTick=worldTick;if(longSwordFirstWorldTick<0)longSwordFirstWorldTick=worldTick;longSwordRecordedStep=step;
            lastLongTrailTick=trailTick;if(longSwordFirstTrailTick<0)longSwordFirstTrailTick=trailTick;
            if(step==LONG_SWORD_TICKS){
                longSwordCheck("long_sword_complete_images",LONG_SWORD_IMAGES.size()==EXPECTED_LONG_SWORD_IMAGES,"Actual long-sword images="+LONG_SWORD_IMAGES.size());
                longSwordCheck("long_sword_complete_ticks",LONG_SWORD_TICKS_RECORDED.size()==LONG_SWORD_TICKS,"Recorded actual scripted client ticks="+LONG_SWORD_TICKS_RECORDED.size());
                longSwordCheck("long_sword_two_1024_boundaries",longArcCaptureState[0]==3&&longArcCaptureState[1]==3,"Boundary image states="+java.util.Arrays.toString(longArcCaptureState));
                longSwordCheck("long_sword_actual_128s_wrap",timeWrapCaptureState==3,"Naturally advancing production trail-clock wrap image state="+timeWrapCaptureState);
                longSwordCheck("long_sword_continuous_actual_history",LONG_SWORD_TICKS_RECORDED.stream().allMatch(item->Boolean.TRUE.equals(item.get("continuousHistory")))&&lastLongTrailTick-longSwordFirstTrailTick==LONG_SWORD_TICKS-1,"Actual trail ticks="+longSwordFirstTrailTick+".."+lastLongTrailTick+", raw world ticks="+longSwordFirstWorldTick+".."+lastLongWorldTick+", continuous nodes/arc checked without seeding");
                check("complete_screenshots",CAPTURES.size()==EXPECTED_CAPTURES,"Actual images="+CAPTURES.size()+", expected"+EXPECTED_CAPTURES);
                check("complete_turn_sequences",SEQUENCE_FRAMES.size()==180,"Actual sequence frames="+SEQUENCE_FRAMES.size()+", expected180 (six realm and three sword clips, 20 frames each)");
                check("no_GL_errors",GL_ERRORS.isEmpty(),GL_ERRORS.toString());finish();
            }
        }catch(ReflectiveOperationException error){FAILURES.add("Long-sword history diagnostic exception: "+error);}
    }
    private static void collectServerSample(){if(serverSample!=null&&serverSample.isDone()){latestServer=serverSample.join();serverSample=null;}}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void serverTelemetry(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||!localReady||event.phase!=TickEvent.Phase.END||serverSample!=null)return;
        var mc=Minecraft.getInstance();UUID id=mc.player.getUUID();serverSample=mc.getSingleplayerServer().submit(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var row=new LinkedHashMap<String,Object>();row.put("entityTick",p.tickCount);row.put("x",p.getX());row.put("y",p.getY());row.put("z",p.getZ());row.put("flying",p.getAbilities().flying);row.put("mayfly",p.getAbilities().mayfly);row.put("flyingSpeed",p.getAbilities().getFlyingSpeed());row.put("onGround",p.onGround());row.put("dayTime",p.serverLevel().getDayTime());return row;
        });
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event){
        if(!ENABLED||finished||!remoteCamera||subject==null||cameraPosition==null||lookTarget==null)return;
        try{
            Vec3 target=lookTarget,position=target.add(cameraOffset);cameraPosition.invoke(event.getCamera(),position);Vec3 toward=target.subtract(position);
            float yaw=(float)Math.toDegrees(Math.atan2(-toward.x,toward.z)),pitch=(float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance()));
            cameraRotation.invoke(event.getCamera(),yaw,pitch);event.setYaw(yaw);event.setPitch(pitch);event.setRoll(0);
        }catch(Throwable error){FAILURES.add("Remote camera exception: "+error);finish();}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void world(RenderLevelStageEvent event){
        if(!ENABLED||finished||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var styles=new ArrayList<Integer>();
        var uniforms=new ArrayList<Map<String,Object>>();
        try{
            Field f=RealmFlightEffects.class.getDeclaredField("pendingHeads");f.setAccessible(true);
            for(Object head:(List<?>)f.get(null)){
                if(head instanceof MeteorHeadRenderer.Head actual){
                    styles.add(actual.style());
                    var dimensions=new org.joml.Matrix4f(actual.viewToHead()).invert().getScale(new org.joml.Vector3f());
                    var values=new LinkedHashMap<String,Object>();
                    values.put("style",actual.style());values.put("color",actual.color());values.put("opacity",actual.opacity());
                    values.put("radii",List.of(dimensions.x,dimensions.y,dimensions.z));
                    values.put("speed",actual.speed());values.put("turn",actual.turn());uniforms.add(values);
                }
            }
            RENDER_DIAGNOSTICS.put("actualSubmittedHeadStyles",styles);
            RENDER_DIAGNOSTICS.put("actualHeadUniforms",uniforms);
        }catch(ReflectiveOperationException error){RENDER_DIAGNOSTICS.put("headStyleReadError",error.toString());}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void remotePlayerDraw(RenderPlayerEvent.Post event){
        if(ENABLED&&!finished&&(scene.equals("stage2-remote")||scene.equals("stage2-pair")||scene.equals("sword-long"))&&event.getEntity() instanceof AbstractClientPlayer player)PLAYER_DRAWS.put(player.getUUID(),player.getModelName());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!prepared)return;
        if(event.phase==TickEvent.Phase.START){PLAYER_DRAWS.clear();frameHeadStart=MeteorHeadRenderer.draws;frameBloomStart=MeteorBloomRenderer.draws;frameSwordNativeStart=SwordFlightTrailRenderer.normalDraws;frameSwordPostStart=SwordFlightTrailRenderer.postDraws;return;}
        int error;while((error=GL11.glGetError())!=GL11.GL_NO_ERROR)GL_ERRORS.add(tick+":"+error);
        if(scene.equals("sword-long")&&tick>LONG_SWORD_BEGIN){recordLongSwordRenderFrame();try{captureLongSwordBoundaryFrames(Minecraft.getInstance());}catch(Throwable failure){FAILURES.add("Long-sword adjacent frame capture exception: "+failure);com.mojang.logging.LogUtils.getLogger().error("LONG_SWORD_FRAME",failure);finish();return;}}
        if(pendingCapture==null&&pendingSequence==null&&pendingCameraCapture==null&&pendingLongSwordCapture==null)return;var mc=Minecraft.getInstance();if(mc.screen!=null||mc.getOverlay()!=null)return;
        if(pendingLongSwordCapture!=null&&(!mc.levelRenderer.isChunkCompiled(subject.blockPosition())||FlyingSwordEffects.trailDiagnostics().stream().noneMatch(item->item.swordId()==sword.getId())||SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart==0&&SwordFlightTrailRenderer.postDraws-frameSwordPostStart==0)&&longSwordReadinessFrames++<12){longSwordReadinessWaitFrames++;return;}
        longSwordReadinessFrames=0;
        // Vanilla skips renderEntity for UNCOMPILED sections even when the custom world effect draws.
        // Wait for real compilation; the strict per-player Post assertions below remain unchanged.
        if((scene.equals("stage2-remote")||scene.equals("stage2-pair"))&&(!mc.levelRenderer.isChunkCompiled(subject.blockPosition())
                ||secondSubject!=null&&!mc.levelRenderer.isChunkCompiled(secondSubject.blockPosition()))){stage2ReadinessWaitFrames++;return;}
        // The inherited sword scene can reach its image tick before the
        // renderer's actual submission. Wait at most six frames, then retain
        // the strict draw assertion below; a persistent missing draw fails.
        if(scene.equals("sword")&&SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart==0
                &&SwordFlightTrailRenderer.postDraws-frameSwordPostStart==0&&swordReadinessFrames++<6){
            swordReadinessWaitFrames++;return;
        }
        swordReadinessFrames=0;
        try{
            Map<String,Object> metrics;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                if(pendingCapture!=null)image.writeToFile(OUTPUT.resolve(pendingCapture));
                if(pendingCameraCapture!=null)image.writeToFile(OUTPUT.resolve(pendingCameraCapture));
                if(pendingLongSwordCapture!=null)image.writeToFile(OUTPUT.resolve(pendingLongSwordCapture));
                if(pendingSequence!=null){Path path=SEQUENCE.resolve(pendingSequence);Files.createDirectories(path.getParent());image.writeToFile(path);}
                metrics=pixels(image);
            }
            collectServerSample();var row=metadata(mc,metrics);row.put("screenshot",pendingCapture);row.put("sequenceFrame",pendingSequence);
            if(pendingCameraCapture!=null){row.put("cameraScreenshot",pendingCameraCapture);captureCameraEvidence(mc,row,metrics,pendingCameraCapture);CAMERA_IMAGES.add(row);}
            if(pendingLongSwordCapture!=null){row.put("longSwordScreenshot",pendingLongSwordCapture);captureLongSwordEvidence(mc,row,metrics,pendingLongSwordCapture);LONG_SWORD_IMAGES.add(row);}
            if(pendingCapture!=null){
                CAPTURES.add(row);
                int checksBeforeCapture=CHECKS.size();
                check("visible_world_"+pendingCapture,!Boolean.TRUE.equals(metrics.get("blackScreenCandidate")),"Actual image central ROI statistics="+metrics);
                if(scene.equals("realm")){
                    check("production_remote_state_"+pendingCapture,RealmFlightEffects.transformed(subject)&&actualPacket.realm()==realm,"Actual received visual packet realm="+actualPacket.realm());
                    var styles=RENDER_DIAGNOSTICS.get("actualSubmittedHeadStyles");check("actual_head_style_"+pendingCapture,styles instanceof List<?> list&&list.contains(RealmFlightAppearance.profile(realm).style()),"Submitted styles="+styles+", expected="+RealmFlightAppearance.profile(realm).style());
                    check("actual_head_consumer_"+pendingCapture,RealmFlightEffects.renderedTierHeads[RealmFlightAppearance.profile(realm).style()]>0,"Production style consumer counts="+java.util.Arrays.toString(RealmFlightEffects.renderedTierHeads));
                    check("production_shader_draw_"+pendingCapture,MeteorHeadRenderer.draws-frameHeadStart>0&&RealmFlightEffects.renderedTrailSegments>0,"Head draws="+(MeteorHeadRenderer.draws-frameHeadStart)+", path segments="+RealmFlightEffects.renderedTrailSegments);
                }else if(scene.equals("motion")){
                    if(pendingCapture.equals("motion-stopped.png"))check("motion_complete_stop_cleanup",MeteorHeadRenderer.draws-frameHeadStart==0&&RealmFlightEffects.renderedTrailSegments==0,"Actual stopped frame head/path draws="+(MeteorHeadRenderer.draws-frameHeadStart)+"/"+RealmFlightEffects.renderedTrailSegments);
                    else if(!pendingCapture.equals("motion-stopping.png"))check("motion_active_head",MeteorHeadRenderer.draws-frameHeadStart>0,"Actual moving/hovering head draw="+(MeteorHeadRenderer.draws-frameHeadStart));
                }else if(scene.equals("stage2-remote")){
                    checkRemoteCapture(mc,pendingCapture);
                }else if(scene.equals("stage2-pair")){
                    checkPairCapture(mc,pendingCapture);
                }else if(scene.equals("local")){
                    check("vanilla_F5_view_"+pendingCapture,mc.options.getCameraType()==expectedCamera,"Actual camera="+mc.options.getCameraType()+", expected="+expectedCamera);
                    check("normal_double_space_flight_"+pendingCapture,mc.player.getAbilities().mayfly&&mc.player.getAbilities().flying&&RealmFlightControl.eligible(mc.player)&&latestServer!=null&&Boolean.TRUE.equals(latestServer.get("flying")),"Actual survival double-space client/server flight="+mc.player.getAbilities().flying+" / "+(latestServer==null?"unavailable":latestServer.get("flying"))+"; keys injected START before vanilla input");
                    if(mc.options.getCameraType().isFirstPerson())check("first_person_clear_"+pendingCapture,MeteorHeadRenderer.draws-frameHeadStart==0&&RealmFlightEffects.renderedVertices==0,"No local energy overlay in actual first person");
                }else{
                    check("registered_sword_root_"+pendingCapture,sword!=null&&sword.flightRoot()&&subject.getVehicle()==sword,"Actual controlled sword root and rider attached; phase="+SWORD_PHASES[swordPhase]);
                    check("actual_sword_shader_draw_"+pendingCapture,SwordFlightTrailRenderer.ready()&&(SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart>0||SwordFlightTrailRenderer.postDraws-frameSwordPostStart>0)&&FlyingSwordEffects.normalTrailSubmitted,"Actual material draws in frame native/post="+(SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart)+" / "+(SwordFlightTrailRenderer.postDraws-frameSwordPostStart)+"; submitted="+FlyingSwordEffects.normalTrailSubmitted);
                }
                if(pendingCapture.contains("close-night"))check("actual_night_"+pendingCapture,mc.level.getDayTime()%24000==18000,"Actual client level dayTime="+mc.level.getDayTime()+"; real server time + vanilla packet");
                if(scene.equals("stage2-remote")||scene.equals("stage2-pair")){stage2CapturesObserved++;stage2ChecksObserved+=CHECKS.size()-checksBeforeCapture;}
                com.mojang.logging.LogUtils.getLogger().info("FLIGHT_TIER_IMAGE {}",row);
            }
            if(pendingSequence!=null)SEQUENCE_FRAMES.add(row);pendingCapture=pendingSequence=pendingCameraCapture=pendingLongSwordCapture=null;write();
        }catch(Throwable failure){FAILURES.add("Render capture exception: "+failure);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_TIER_FRAME",failure);finish();}
    }
    private static Object field(Object value,String name)throws ReflectiveOperationException{
        Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);
    }
    private static Map<?,?> staticMap(Class<?> type,String name)throws ReflectiveOperationException{
        Field f=type.getDeclaredField(name);f.setAccessible(true);return (Map<?,?>)f.get(null);
    }
    private record FlightProbe(Object flight,Object poseState,RealmMeteorPath path,double length,double glow,RealmFlightVisualPacket packet) {
        double speed(){return path==null?0:path.speed();}
        int samples(){return path==null?0:path.sampleCount();}
    }
    private static FlightProbe probe(UUID id)throws ReflectiveOperationException{
        Object flight=staticMap(RealmFlightEffects.class,"FLIGHTS").get(id),pose=staticMap(RealmFlightVisuals.class,"STATES").get(id);
        return flight==null?new FlightProbe(null,pose,null,0,0,null):new FlightProbe(flight,pose,(RealmMeteorPath)field(flight,"path"),((Number)field(flight,"trailLength")).doubleValue(),((Number)field(flight,"glow")).doubleValue(),(RealmFlightVisualPacket)field(flight,"state"));
    }
    private static FlightVisualDynamics.Pose actualPose(FlightProbe probe,float partial){return probe.poseState() instanceof FlightVisualDynamics state?state.pose(partial,true):null;}
    private static boolean finitePose(FlightVisualDynamics.Pose pose)throws ReflectiveOperationException{
        if(pose==null)return false;for(var component:pose.getClass().getRecordComponents())if(!Double.isFinite(((Number)component.getAccessor().invoke(pose)).doubleValue()))return false;return true;
    }
    private static boolean actualModel(Minecraft mc,RemotePlayer player,boolean slim)throws ReflectiveOperationException{
        Object renderer=mc.getEntityRenderDispatcher().getRenderer(player);if(!(renderer instanceof PlayerRenderer actual))return false;
        return Boolean.valueOf(slim).equals(field(actual.getModel(),"slim"))&&player.getModelName().equals(slim?"slim":"default")&&mc.getResourceManager().getResource(player.getSkinTextureLocation()).isPresent();
    }
    private static boolean equipped(RemotePlayer player,boolean slim){
        return player.getMainHandItem().is(slim?Items.DIAMOND_AXE:Items.NETHERITE_SWORD)&&player.getOffhandItem().is(slim?Items.TOTEM_OF_UNDYING:Items.SHIELD)
                &&player.getItemBySlot(EquipmentSlot.HEAD).is(slim?Items.DIAMOND_HELMET:Items.NETHERITE_HELMET)&&player.getItemBySlot(EquipmentSlot.CHEST).is(slim?Items.DIAMOND_CHESTPLATE:Items.NETHERITE_CHESTPLATE)
                &&player.getItemBySlot(EquipmentSlot.LEGS).is(slim?Items.DIAMOND_LEGGINGS:Items.NETHERITE_LEGGINGS)&&player.getItemBySlot(EquipmentSlot.FEET).is(slim?Items.DIAMOND_BOOTS:Items.NETHERITE_BOOTS);
    }
    private static double segmentDistance(Vec3 point,Vec3 a,Vec3 b){Vec3 ab=b.subtract(a);double t=ab.lengthSqr()<1e-12?0:Math.max(0,Math.min(1,point.subtract(a).dot(ab)/ab.lengthSqr()));return point.distanceTo(a.add(ab.scale(t)));}
    private static Vec3 vec(RealmMeteorPath.Vec value){return new Vec3(value.x(),value.y(),value.z());}
    private static Map<String,Object> cameraSafety(Minecraft mc,RemotePlayer player,int playerRealm,FlightProbe probe)throws ReflectiveOperationException{
        Vec3 camera=mc.gameRenderer.getMainCamera().getPosition(),head=player.getPosition(mc.getFrameTime()).add(0,player.getBbHeight()*.5,0);var profile=RealmFlightAppearance.profile(playerRealm);
        // Conservative radii include orb stretch and widest tube layer. Distances use the actual smoothed rendered history.
        double orbBound=profile.radius()*RealmMeteorTuning.ORB_STRETCH_MAX*1.08+.25,tubeBound=profile.radius()*RealmMeteorTuning.ROOT_WIDTH_RATIO*1.08+.25,minTube=Double.POSITIVE_INFINITY;
        int pathFrames=0;
        if(probe.path()!=null){
            Field clock=RealmFlightEffects.class.getDeclaredField("clock");clock.setAccessible(true);
            double distance=player.position().distanceTo(camera);int lod=distance>64?2:distance>32?1:0;
            var frames=probe.path().renderAdaptive(((Number)clock.get(null)).doubleValue()-1+mc.getFrameTime(),new RealmMeteorPath.Vec(head.x,head.y,head.z),Math.max(.01,probe.length()),RealmMeteorTuning.TRAIL_LIFETIME_TICKS,lod);pathFrames=frames.size();
            for(int i=1;i<frames.size();i++)minTube=Math.min(minTube,segmentDistance(camera,vec(frames.get(i-1).position()),vec(frames.get(i).position())));
        }
        var row=new LinkedHashMap<String,Object>();row.put("orbDistance",camera.distanceTo(head));row.put("orbBound",orbBound);row.put("outsideOrb",camera.distanceTo(head)>orbBound+2);
        row.put("renderedHistoryFrames",pathFrames);row.put("minimumDistanceToRenderedTubeCenterline",Double.isFinite(minTube)?minTube:null);row.put("tubeBound",tubeBound);row.put("outsideTube",minTube>tubeBound+2);
        if(remoteVelocity.lengthSqr()>1e-8){Vec3 offset=camera.subtract(head),forward=remoteVelocity.normalize();row.put("velocityViewCosine",offset.normalize().dot(forward));row.put("rearAxialDistance",-offset.dot(forward));}
        row.put("actualTrailLength",probe.length());return row;
    }
    private static boolean submittedStyle(int playerRealm){Object styles=RENDER_DIAGNOSTICS.get("actualSubmittedHeadStyles");int style=RealmFlightAppearance.profile(playerRealm).style();return styles instanceof List<?> list&&list.contains(style)&&RealmFlightEffects.renderedTierHeads[style]>0;}
    private static void checkRemoteCapture(Minecraft mc,String name)throws ReflectiveOperationException{
        var probe=probe(SUBJECT_ID);var safety=cameraSafety(mc,subject,realm,probe);Vec3 offset=mc.gameRenderer.getMainCamera().getPosition().subtract(subject.getPosition(mc.getFrameTime()).add(0,subject.getBbHeight()*.5,0));double cosine=offset.normalize().dot(remoteVelocity.normalize()),rear=-offset.dot(remoteVelocity.normalize());
        check("stage2_outside_orb_"+name,Boolean.TRUE.equals(safety.get("outsideOrb")),safety.toString());
        check("stage2_outside_tube_"+name,Boolean.TRUE.equals(safety.get("outsideTube")),safety.toString());
        check("stage2_velocity_camera_"+name,remoteView.equals("front")?cosine>.995:cosine<-.80&&rear>probe.length()+8,"view="+remoteView+", cosine="+cosine+", rear="+rear+", actual trail+8="+(probe.length()+8));
        check("stage2_real_default_player_"+name,mc.level.getPlayerByUUID(SUBJECT_ID)==subject&&PLAYER_DRAWS.containsKey(SUBJECT_ID)&&actualModel(mc,subject,false),"UUID="+subject.getUUID()+", actual rendered players="+PLAYER_DRAWS+", skin="+subject.getSkinTextureLocation());
        check("stage2_real_held_armor_"+name,equipped(subject,false),equipment(subject).toString());
        check("stage2_transformed_"+name,RealmFlightEffects.transformed(subject)&&probe.packet()!=null&&probe.packet().player().equals(SUBJECT_ID)&&probe.packet().realm()==realm,"Actual production state="+probe.packet());
        check("stage2_style_consumer_"+name,submittedStyle(realm),"Actual submitted styles="+RENDER_DIAGNOSTICS.get("actualSubmittedHeadStyles"));
        check("stage2_shader_draw_"+name,MeteorHeadRenderer.draws-frameHeadStart>0&&RealmFlightEffects.renderedTrailSegments>0,"Actual head draws="+(MeteorHeadRenderer.draws-frameHeadStart)+", segments="+RealmFlightEffects.renderedTrailSegments);
        check("stage2_finite_pose_"+name,finitePose(actualPose(probe,mc.getFrameTime())),"Actual production pose="+actualPose(probe,mc.getFrameTime()));
    }
    private static void checkPairCapture(Minecraft mc,String name)throws ReflectiveOperationException{
        var steve=probe(SUBJECT_ID);var alex=probe(ALEX_ID);var steveSafety=cameraSafety(mc,subject,4,steve);var alexSafety=cameraSafety(mc,secondSubject,8,alex);boolean active=remoteStep<61;
        check("stage2_pair_ids_"+name,!SUBJECT_ID.equals(ALEX_ID)&&subject!=secondSubject&&mc.level.getPlayerByUUID(SUBJECT_ID)==subject&&mc.level.getPlayerByUUID(ALEX_ID)==secondSubject,"Two actual client RemotePlayer IDs="+subject.getUUID()+" / "+secondSubject.getUUID());
        check("stage2_pair_models_drawn_"+name,actualModel(mc,subject,false)&&actualModel(mc,secondSubject,true)&&PLAYER_DRAWS.containsKey(SUBJECT_ID)&&PLAYER_DRAWS.containsKey(ALEX_ID),"Actual renderer model flags and player draws="+PLAYER_DRAWS);
        check("stage2_pair_held_armor_"+name,equipped(subject,false)&&equipped(secondSubject,true),"Steve="+equipment(subject)+", Alex="+equipment(secondSubject));
        check("stage2_pair_outside_orbs_"+name,Boolean.TRUE.equals(steveSafety.get("outsideOrb"))&&Boolean.TRUE.equals(alexSafety.get("outsideOrb")),"Steve="+steveSafety+", Alex="+alexSafety);
        check("stage2_pair_outside_tubes_"+name,Boolean.TRUE.equals(steveSafety.get("outsideTube"))&&Boolean.TRUE.equals(alexSafety.get("outsideTube")),"Steve="+steveSafety+", Alex="+alexSafety);
        check("stage2_pair_independent_states_"+name,alex.flight()!=null&&alex.poseState()!=null&&(steve.flight()==null||steve.flight()!=alex.flight()&&steve.path()!=alex.path())&&(steve.poseState()==null||steve.poseState()!=alex.poseState()),"Reflected flight/path/pose objects are per UUID; Steve present="+(steve.flight()!=null)+", Alex present="+(alex.flight()!=null));
        check("stage2_pair_active_alex_draw_"+name,RealmFlightEffects.transformed(secondSubject)&&submittedStyle(8)&&(!active||submittedStyle(4))&&MeteorHeadRenderer.draws-frameHeadStart>0&&RealmFlightEffects.renderedTrailSegments>0,"Actual active Alex and expected styles="+RENDER_DIAGNOSTICS.get("actualSubmittedHeadStyles"));
        check("stage2_pair_alex_history_"+name,alex.samples()>2&&alex.speed()>.25&&alex.packet()!=null&&alex.packet().player().equals(ALEX_ID)&&alex.packet().active()&&finitePose(actualPose(alex,mc.getFrameTime())),"Actual Alex samples="+alex.samples()+", speed="+alex.speed()+", pose="+actualPose(alex,mc.getFrameTime()));
        check("stage2_pair_steve_active_flag_"+name,RealmFlightEffects.transformed(subject)==active&&actualPacket.active()==active,"Actual Steve transformed="+RealmFlightEffects.transformed(subject)+", packet="+actualPacket);
        if(active){
            check("stage2_pair_distinct_speeds_"+name,steve.speed()>alex.speed()*1.7,"Reflected Steve/Alex speed="+steve.speed()+" / "+alex.speed());
            check("stage2_pair_distinct_lengths_"+name,steve.length()>alex.length()+1,"Reflected Steve/Alex dynamic length="+steve.length()+" / "+alex.length());
        }
        if(remoteStep==82)check("stage2_one_stop_keeps_other_history",steve.samples()==0&&alex.samples()>20,"Reflected stopped Steve samples="+steve.samples()+", moving Alex="+alex.samples());
        if(remoteStep==112)check("stage2_one_stop_cleans_only_steve",steve.flight()==null&&steve.poseState()==null&&alex.flight()!=null&&alex.poseState()!=null,"Reflected stopped Steve flight/pose absent; Alex flight/pose retained");
    }
    private static Map<String,Object> equipment(RemotePlayer player){var row=new LinkedHashMap<String,Object>();for(var slot:EquipmentSlot.values())row.put(slot.getName(),ForgeRegistries.ITEMS.getKey(player.getItemBySlot(slot).getItem()).toString());return row;}
    private static Map<String,Object> remoteDiagnostic(Minecraft mc,RemotePlayer player,int playerRealm)throws ReflectiveOperationException{
        var probe=probe(player.getUUID());var row=new LinkedHashMap<String,Object>();row.put("uuid",player.getUUID());row.put("entityId",player.getId());row.put("modelName",player.getModelName());row.put("skin",player.getSkinTextureLocation().toString());row.put("actualRendererModelMatches",actualModel(mc,player,player==secondSubject));row.put("drawnInRealFrame",PLAYER_DRAWS.containsKey(player.getUUID()));row.put("equipment",equipment(player));
        row.put("position",player.position().toString());row.put("actualEntitySectionCompiled",mc.levelRenderer.isChunkCompiled(player.blockPosition()));row.put("actualPacket",probe.packet());row.put("productionTransformed",RealmFlightEffects.transformed(player));row.put("trailLength",probe.length());row.put("speed",probe.speed());row.put("historySamples",probe.samples());row.put("glow",probe.glow());row.put("actualPose",actualPose(probe,mc.getFrameTime()));row.put("cameraSafety",cameraSafety(mc,player,playerRealm,probe));return row;
    }
    private static Map<String,Object> metadata(Minecraft mc,Map<String,Object> metrics){
        var row=new LinkedHashMap<String,Object>();row.put("scriptTick",tick);row.put("actualWorldGameTime",mc.level.getGameTime());row.put("scene",scene);row.put("realm",realm);
        if(realm>=3){var p=RealmFlightAppearance.profile(realm);var profile=new LinkedHashMap<String,Object>();profile.put("style",p.style());profile.put("colorHex",String.format(Locale.ROOT,"#%06X",p.color()));profile.put("length",p.length());profile.put("boostLength",p.boostLength());profile.put("width",p.width());profile.put("radius",p.radius());profile.put("headLength",p.headLength());profile.put("sparkStride",p.sparkStride());row.put("actualProductionProfile",profile);}
        row.put("actualReceivedVisualPacket",actualPacket);row.put("actualSubmittedHeadStyles",RENDER_DIAGNOSTICS.get("actualSubmittedHeadStyles"));row.put("actualHeadUniforms",RENDER_DIAGNOSTICS.get("actualHeadUniforms"));row.put("headDrawsInRealFrame",MeteorHeadRenderer.draws-frameHeadStart);row.put("bloomDrawsInRealFrame",MeteorBloomRenderer.draws-frameBloomStart);
        try{
            Field flights=RealmFlightEffects.class.getDeclaredField("FLIGHTS");flights.setAccessible(true);Object f=((Map<?,?>)flights.get(null)).get(SUBJECT_ID);
            if(f!=null){var diagnostic=new LinkedHashMap<String,Object>();for(String name:new String[]{"trailLength","glow","path"}){Field field=f.getClass().getDeclaredField(name);field.setAccessible(true);Object value=field.get(f);diagnostic.put(name,name.equals("path")?value.getClass().getMethod("speed").invoke(value):value);}row.put("actualDynamicMeteorState",diagnostic);}
            Field states=RealmFlightVisuals.class.getDeclaredField("STATES");states.setAccessible(true);Object poseState=((Map<?,?>)states.get(null)).get(SUBJECT_ID);
            if(poseState!=null)row.put("actualDynamicPose",poseState.getClass().getMethod("pose",float.class,boolean.class).invoke(poseState,mc.getFrameTime(),true));
        }catch(ReflectiveOperationException error){row.put("dynamicDiagnosticError",error.toString());}
        row.put("renderedTierHeads",RealmFlightEffects.renderedTierHeads.clone());row.put("renderedTrailSegments",RealmFlightEffects.renderedTrailSegments);row.put("renderedVertices",RealmFlightEffects.renderedVertices);row.put("lastPostDrawSucceeded",RealmFlightEffects.lastPostDrawSucceeded);row.put("headShaderReady",MeteorHeadRenderer.ready());row.put("meteorShaderReady",TrailShaderBridge.meteorReady());
        row.put("shaderPackActuallyActive",TrailShaderBridge.shaderPackDetected());row.put("shaderPackName",packName);row.put("depthStatus",TrailShaderBridge.depthStatus);row.put("cameraType",mc.options.getCameraType().name());row.put("remoteCameraOverride",remoteCamera);row.put("cameraPosition",mc.gameRenderer.getMainCamera().getPosition().toString());row.put("cameraLook",mc.gameRenderer.getMainCamera().getLookVector().toString());
        row.put("subjectPosition",subject.position().toString());row.put("actualPlayerPosition",mc.player.position().toString());row.put("actualLocalFlying",mc.player.getAbilities().flying);row.put("actualLocalMayfly",mc.player.getAbilities().mayfly);row.put("actualLocalFlyingSpeed",mc.player.getAbilities().getFlyingSpeed());row.put("actualServerPlayer",latestServer);row.put("localInputInjectionPhase","ClientTick START before vanilla LocalPlayer input and production END double-jump");row.put("cameraDistanceToSubject",mc.gameRenderer.getMainCamera().getPosition().distanceTo(subject.position().add(0,.9,0)));row.put("hideGui",mc.options.hideGui);row.put("dayTime",mc.level.getDayTime());row.put("fps",mc.getFps());row.put("pixelMetrics",metrics);
        if(scene.equals("stage2-remote")||scene.equals("stage2-pair")){
            row.put("runtimeScope","Controlled client-only RemotePlayer fixtures in an independent generated integrated-server world; no actual LAN packet transport is asserted.");row.put("comparablePathStep",remoteStep);row.put("pathCase",remotePath);row.put("velocityDerivedView",remoteView);row.put("scriptedVelocity",remoteVelocity.toString());row.put("commonCameraOffset",cameraOffset.toString());row.put("actualFovSetting",mc.options.fov().get());row.put("visualReview","PENDING_HUMAN_REVIEW");
            try{var players=new LinkedHashMap<String,Object>();players.put("steve",remoteDiagnostic(mc,subject,realm));if(secondSubject!=null)players.put("alex",remoteDiagnostic(mc,secondSubject,8));row.put("actualRemotePlayers",players);}catch(ReflectiveOperationException error){row.put("remoteDiagnosticError",error.toString());}
        }
        if(swordPhase>=0){row.put("swordPhase",SWORD_PHASES[swordPhase].name());row.put("actualSwordFlightRoot",sword.flightRoot());row.put("actualRidingControlledSword",subject.getVehicle()==sword);row.put("swordVisualSpeed",sword.renderSpeed(mc.getFrameTime()));row.put("swordNativeDrawsInFrame",SwordFlightTrailRenderer.normalDraws-frameSwordNativeStart);row.put("swordPostDrawsInFrame",SwordFlightTrailRenderer.postDraws-frameSwordPostStart);row.put("swordShaderReady",SwordFlightTrailRenderer.ready());row.put("swordRenderDiagnostics",swordDiagnostics());}
        return row;
    }
    private static Map<String,Object> swordDiagnostics(){
        var row=new LinkedHashMap<String,Object>();
        for(String className:new String[]{"cn.blockforge.generated.generatedmod.client.SwordFlightTrailRenderer","cn.blockforge.generated.generatedmod.client.FlyingSwordEffects"}){
            try{Class<?> type=Class.forName(className);var fields=new LinkedHashMap<String,Object>();for(Field f:type.getFields())if(java.lang.reflect.Modifier.isStatic(f.getModifiers())){Object value=f.get(null);if(value==null||value instanceof Number||value instanceof String||value instanceof Boolean)fields.put(f.getName(),value);}row.put(type.getSimpleName(),fields);}catch(ReflectiveOperationException e){row.put(className,"not present: "+e.getClass().getSimpleName());}
        }return row;
    }
    private static Map<String,Object> pixels(NativeImage image){
        int w=image.getWidth(),h=image.getHeight(),count=0,black=0;double sum=0;
        for(int y=h/5;y<h*4/5;y++)for(int x=w/5;x<w*4/5;x++){int c=image.getPixelRGBA(x,y),r=c&255,g=c>>8&255,b=c>>16&255;count++;sum+=.2126*r+.7152*g+.0722*b;if(Math.max(r,Math.max(g,b))<=3)black++;}
        double mean=sum/count,fraction=black/(double)count;var row=new LinkedHashMap<String,Object>();row.put("width",w);row.put("height",h);row.put("centralMeanLuma255",mean);row.put("centralNearBlackFraction",fraction);row.put("blackScreenCandidate",mean<5&&fraction>.75);return row;
    }
    private static void captureCameraEvidence(Minecraft mc,Map<String,Object> row,Map<String,Object> metrics,String name){
        float partial=mc.getFrameTime();Vec3 eye=mc.player.getEyePosition(partial),camera=mc.gameRenderer.getMainCamera().getPosition(),offset=camera.subtract(eye);double distance=offset.length();
        var diagnostic=RealmFlightCamera.diagnostics(partial);boolean actualBoost=RealmFlightControl.boosting(mc.player),first=mc.options.getCameraType().isFirstPerson();
        row.put("actualLocalEye",List.of(eye.x,eye.y,eye.z));row.put("actualLocalCamera",List.of(camera.x,camera.y,camera.z));row.put("actualLocalCameraDistance",distance);
        row.put("actualCameraDiagnostics",diagnostic);row.put("actualControlBoosting",actualBoost);row.put("actualSprintKey",mc.options.keySprint.isDown());row.put("actualForwardKey",mc.options.keyUp.isDown());row.put("actualStrafeRightKey",mc.options.keyRight.isDown());row.put("cameraPhaseStep",tick-BASE_FINISH_TICK);
        row.put("actualLocalLookPitch",mc.player.getXRot());row.put("actualLocalLookYaw",mc.player.getYRot());
        var ray=mc.level.clip(new ClipContext(eye,camera,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));row.put("eyeToActualCameraClip",ray.getType().name());
        boolean knownWallHit=ray.getType()==HitResult.Type.BLOCK&&ray.getBlockPos().getZ()==3&&ray.getBlockPos().getX()>=-80&&ray.getBlockPos().getX()<=120&&ray.getBlockPos().getY()>=167&&ray.getBlockPos().getY()<=177&&ray.getDirection()==net.minecraft.core.Direction.NORTH&&mc.level.getBlockState(ray.getBlockPos()).is(Blocks.STONE)&&Math.abs(ray.getLocation().z-3)<.00001;
        row.put("actualCameraClipHitBlock",ray.getType()==HitResult.Type.BLOCK?List.of(ray.getBlockPos().getX(),ray.getBlockPos().getY(),ray.getBlockPos().getZ()):null);row.put("actualCameraClipHitPosition",ray.getType()==HitResult.Type.BLOCK?List.of(ray.getLocation().x,ray.getLocation().y,ray.getLocation().z):null);row.put("actualCameraClipHitFace",ray.getType()==HitResult.Type.BLOCK?ray.getDirection().name():null);row.put("actualCameraClipHitKnownWall",knownWallHit);row.put("actualCameraDistanceToKnownWallFace",Math.abs(camera.z-3));row.put("actualCameraZoomReduction",diagnostic.lastRequestedDistance()-distance);
        row.put("wallPlaneZ",name.contains("wall")?3:null);row.put("wallBlockAtEyeHeight",mc.level.getBlockState(new BlockPos((int)Math.floor(eye.x),(int)Math.floor(eye.y),3)).is(Blocks.STONE));
        cameraCheck("camera_visible_"+name,!Boolean.TRUE.equals(metrics.get("blackScreenCandidate")),"Actual image visibility metrics="+metrics);
        cameraCheck("camera_real_local_flight_"+name,mc.getCameraEntity()==mc.player&&mc.options.getCameraType()==expectedCamera&&mc.player.getAbilities().flying&&RealmFlightControl.eligible(mc.player)&&latestServer!=null&&Boolean.TRUE.equals(latestServer.get("flying")),"Actual camera="+mc.options.getCameraType()+", player/server flight="+mc.player.getAbilities().flying+"/"+(latestServer==null?null:latestServer.get("flying")));
        boolean hook=first?!diagnostic.active()&&Math.abs(diagnostic.currentDistance()-4)<.001:diagnostic.active()&&Math.abs(diagnostic.lastRequestedDistance()-diagnostic.interpolatedDistance())<.10&&distance<=diagnostic.lastRequestedDistance()+.15;
        cameraCheck("camera_actual_hook_"+name,hook,"Actual requested/current/interpolated/camera distance="+diagnostic+" / "+distance);
        boolean pass;
        // Vanilla's eight visual rays place this camera against the known z=3
        // block face. Allow its corner-ray margin, and require the ray to hit
        // that wall rather than treating a clipped camera as an unobstructed ray.
        if(name.contains("wall"))pass=actualBoost&&diagnostic.targetDistance()>4.5&&diagnostic.currentDistance()>4.6&&distance<3.10&&offset.z>0&&Math.abs(camera.z-3)<=.10&&knownWallHit&&diagnostic.lastRequestedDistance()-distance>=2&&Boolean.TRUE.equals(row.get("wallBlockAtEyeHeight"));
        else if(first)pass=distance<.20&&Math.abs(diagnostic.currentDistance()-4)<.001&&MeteorHeadRenderer.draws-frameHeadStart==0&&RealmFlightEffects.renderedVertices==0;
        else if(name.contains("boost")){pass=actualBoost&&diagnostic.horizontalSpeed()>diagnostic.cruiseSpeed()*1.04&&diagnostic.currentDistance()>5&&distance>5;if(name.equals("camera-back-boost.png"))backBoostDistance=distance;if(name.equals("camera-front-boost.png"))frontBoostDistance=distance;}
        else if(name.contains("release"))pass=!actualBoost&&diagnostic.targetDistance()<4.001&&distance<(name.contains("back")?backBoostDistance:frontBoostDistance)-.40&&distance>3.8;
        else pass=!actualBoost&&Math.abs(diagnostic.targetDistance()-4)<.001&&Math.abs(distance-4)<.25;
        cameraCheck("camera_case_"+name,pass,"Actual distance="+distance+", offset="+offset+", control boost="+actualBoost+", diagnostics="+diagnostic+", eye-camera clip="+ray.getType());
    }
    private static void cameraCheck(String name,boolean pass,String detail){var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("name",name);row.put("pass",pass);row.put("detail",detail);CAMERA_CHECKS.add(row);if(!pass)FAILURES.add(name+": "+detail);}
    private static void longSwordCheck(String name,boolean pass,String detail){var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("name",name);row.put("pass",pass);row.put("detail",detail);LONG_SWORD_CHECKS.add(row);if(!pass)FAILURES.add(name+": "+detail);}
    private static void check(String name,boolean pass,String detail){var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("name",name);row.put("pass",pass);row.put("detail",detail);CHECKS.add(row);if(!pass)FAILURES.add(name+": "+detail);}
    private static void write()throws Exception{
        var result=new LinkedHashMap<String,Object>();result.put("result",finished?(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL"):"RUNNING");result.put("captures",CAPTURES);result.put("sequenceFrames",SEQUENCE_FRAMES);result.put("checks",CHECKS);result.put("failures",FAILURES);result.put("glErrors",GL_ERRORS);result.put("f5InputEvents",F5_EVENTS);
        result.put("expectedShaderPackActive",EXPECT_PACK);result.put("actualShaderPackActive",TrailShaderBridge.shaderPackDetected());result.put("shaderPackName",packName);result.put("oculusInstalled",ModList.get().isLoaded("oculus"));result.put("embeddiumInstalled",ModList.get().isLoaded("embeddium"));
        result.put("bloomDisabledForCapture",BLOOM_DISABLED);
        result.put("cameraImages",CAMERA_IMAGES);result.put("cameraChecks",CAMERA_CHECKS);result.put("cameraF5InputEvents",CAMERA_F5_EVENTS);result.put("expectedCameraImages",EXPECTED_CAMERA_IMAGES);result.put("expectedCameraChecks",EXPECTED_CAMERA_CHECKS);result.put("baseScriptTicks",BASE_FINISH_TICK);result.put("cameraPhaseTicks",CAMERA_PHASE_TICKS);
        result.put("cameraEvidenceScope","Independent appended actual-local phase. Original 89 images/580 checks/180 turn frames remain intact. Actual W+sprint accelerates and releases in both third-person views; first-person resets; strafe+sprint beside a known front wall retains genuine boosted horizontal displacement while vanilla Camera.getMaxZoom clips the actual view. Camera-eye distance is measured from the actual local interpolated eye and camera positions, not the remote subject. High-angle front/back images are visual evidence only.");
        result.put("longSwordImages",LONG_SWORD_IMAGES);result.put("longSwordChecks",LONG_SWORD_CHECKS);result.put("longSwordTickTelemetry",LONG_SWORD_TICKS_RECORDED);result.put("longSwordRenderFrames",LONG_SWORD_RENDER_FRAMES);result.put("expectedLongSwordImages",EXPECTED_LONG_SWORD_IMAGES);result.put("expectedLongSwordChecks",EXPECTED_LONG_SWORD_CHECKS);result.put("longSwordPhaseStartTick",LONG_SWORD_BEGIN);result.put("longSwordPhaseTicks",LONG_SWORD_TICKS);result.put("longSwordFirstWorldTick",longSwordFirstWorldTick);result.put("longSwordLastWorldTick",lastLongWorldTick);result.put("longSwordElapsedWorldTicks",longSwordFirstWorldTick<0?0:lastLongWorldTick-longSwordFirstWorldTick);result.put("longSwordReadinessWaitFrames",longSwordReadinessWaitFrames);
        result.put("longSwordFirstTrailTick",longSwordFirstTrailTick);result.put("longSwordLastTrailTick",lastLongTrailTick);result.put("longSwordElapsedTrailTicks",longSwordFirstTrailTick<0?0:lastLongTrailTick-longSwordFirstTrailTick);
        int corrected=0,backward=0,duplicate=0;for(int i=1;i<LONG_SWORD_TICKS_RECORDED.size();i++){long delta=((Number)LONG_SWORD_TICKS_RECORDED.get(i).get("worldTick")).longValue()-((Number)LONG_SWORD_TICKS_RECORDED.get(i-1).get("worldTick")).longValue();if(delta!=1)corrected++;if(delta<0)backward++;if(delta==0)duplicate++;}
        result.put("longSwordRawWorldClockCorrectionCount",corrected);result.put("longSwordRawWorldClockBackwardCount",backward);result.put("longSwordRawWorldClockDuplicateCount",duplicate);
        result.put("longSwordPathRadius",LONG_SWORD_RADIUS);result.put("longSwordClientViewDistance",12);result.put("longSwordServerViewDistance",10);result.put("longSwordPreflightSections",LONG_SWORD_PREFLIGHT_SECTIONS);result.put("longSwordPreflightWaitTicks",longSwordPreflightWaitTicks);result.put("longSwordPreflightComplete",longSwordPrepared&&longSwordPreflightIndex==LONG_SWORD_PREFLIGHT_BLOCKS.size());
        result.put("longSwordEvidenceScope","Independent appended2600 consecutive scripted client ticks and exactly2599 naturally elapsed production sampler ticks; raw synchronized world time is retained separately and may repeat or go backward. Registered ControlledSwordEntity root and attached RemotePlayer travel a controlled radius48 circular12-block/client-tick path, with stronger turns than ordinary straight flight. Actual local/server chunk observer stays at center, within production64-block effect distance; detached actual fixture camera follows the moving sword. Before creating the sword, client12/server10 chunk view distances preload every path section and wait actual compilation; no sword history, arc or production clock is seeded or manually sampled. DEV clouds are disabled only for this scene and restored on finish. Every actual RenderTick.END records the real widthEnvelope, history, monotonic sample/captured clock, shader time, compiled section and actual rider RenderPlayer.Post. Long motion continues during pending screenshots. PNGs save actual neighbouring framebuffer images at firstArc/origin1024/2048 material rebases and naturally reached128s production-clock wrap. WidthEnvelope is geometry evidence only; shader density and visible thin boundary continuity require actual frame inspection. Controlled client visuals only; no server speed authorization, LAN transport or aesthetic PASS is claimed.");
        result.put("sequencePlaybackFPS",10);result.put("sequenceTiming","One real saved rendered PNG at each two scripted 20Hz ticks; clock waits for RenderTick.END save. Encode each 20-frame directory at10FPS. Scripted motion evidence, not wall-clock capture or a claimed visual approval.");
        result.put("baseCaptures",55);result.put("baseChecks",234);result.put("expectedExtraCaptures",STAGE2_CAPTURES);result.put("actualExtraCaptures",stage2CapturesObserved);result.put("expectedExtraChecks",STAGE2_CHECKS);result.put("actualExtraChecks",stage2ChecksObserved);result.put("expectedTotalChecks",234+STAGE2_CHECKS);result.put("extraSceneScriptTicks",REMOTE_END-MOTION_END);result.put("extraSceneStartTick",MOTION_END+1);result.put("extraSceneEndTick",REMOTE_END);result.put("extraSceneCaptureIndexStart",stage2CaptureStart);result.put("extraSceneCheckIndexStart",stage2CheckStart);result.put("visualReview","PENDING_HUMAN_REVIEW");
        result.put("extraSceneScope","Two matching 120-tick realm4/8 controlled client-only RemotePlayer paths, followed by 120 ticks with two separate RemotePlayer UUIDs. Deterministic vanilla Steve/default and Alex/slim renderer selection, real held equipment, production receive/state/path/pose/shader consumers. The actual observer is aligned with the remote camera, with6-chunk view distance during these extra scenes; captures wait for the actual entity sections to compile and retain strict per-player Post draw assertions. No actual LAN, dedicated server, or synchronized transport is claimed. The PASS/FAIL result concerns fixture checks and GL errors; screenshot appearance needs human review.");
        result.put("extraEntitySectionReadinessWaitFrames",stage2ReadinessWaitFrames);
        result.put("swordReadinessWaitFrames",swordReadinessWaitFrames);
        result.put("extraCameraPlan","Stage2 subjects and targets start 45m above the base observation altitude so dive views stay above the stone platform. Identical 65-degree FOV and position/velocity script for realm4 and8; front uses unit velocity*40, rear uses unit velocity*-40, oblique rear adds velocity-derived horizontal side*18 and world up*5. Rear axial distance is checked against actual trailLength+8. Actual interpolated head and smoothed rendered path segments are checked against conservative orb/tube radii for every extra image. The pair camera looks side-on at both heads' midpoint with8m elevation and separation-adaptive distance of at least max tail length+14m, preserving a max tail length+8m gap to the nearer lane. Both player IDs and both volumes are checked.");
        result.put("expectedCaptures",EXPECTED_CAPTURES);result.put("swordScenesSkipped",false);result.put("expectedSequenceFrames",180);result.put("scriptTicks",FINISH_TICK);result.put("cameraPlan","Each realm retains wide side (~33m) and far (~80m), adds day/night close (~10.6m), and uses close turn (~10.6m). Each sword phase retains wide side (~26m), adds day/night close (~10m), and uses close turn. All9 turning clips use constant close camera through20 frames. Camera remains outside the bounded energy volume.");
        result.put("scope","Independent generated survival world. Realm3..8 are passed through real production RealmFlightVisuals.receive packets, same controlled remote20Hz straight/turn path and five matching day/night views per tier. Sword scenes use the registered ControlledSwordEntity with flightRoot and actual remote rider, four real production shader views per phase. Extra client-only fixtures add matching realm4/8 velocity-derived front/rear/oblique views and independent Steve/Alex state/history checks; these are not actual LAN transport tests. Final local keys are injected at ClientTick START as in verified CameraCycleCapture, before vanilla LocalPlayer input and production END toggle; production mayfly grant, logical double-space, real W movement and vanilla F5 KeyMapping.click/consumeClick with HUD visible, actual client/server flight flags sampled. No user launcher world or controls touched. Shader selection is observed, never forced. Appearance remains for human screenshot/video review.");
        Files.writeString(Path.of("flight-tier-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
    private static void finish(){
        if(finished)return;finished=true;try{var mc=Minecraft.getInstance();mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyJump.setDown(false);if(longSwordPreflightRequested){mc.options.renderDistance().set(previousLongRenderDistance);mc.options.cloudStatus().set(previousLongCloudStatus);if(mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().setViewDistance(previousLongServerViewDistance));}write();Files.writeString(Path.of("flight-tier-result.txt"),(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL")+": "+CAPTURES.size()+" screenshots; "+SEQUENCE_FRAMES.size()+" actual turning frames; "+CHECKS.size()+" checks; camera="+CAMERA_IMAGES.size()+" images/"+CAMERA_CHECKS.size()+" checks; longSword="+LONG_SWORD_IMAGES.size()+" images/"+LONG_SWORD_CHECKS.size()+" checks/"+LONG_SWORD_TICKS_RECORDED.size()+" real client ticks; failures="+FAILURES+";GL="+GL_ERRORS);}catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("FLIGHT_TIER_RESULT",error);}Minecraft.getInstance().stop();
    }
    private FlightTierCapture(){}
}
