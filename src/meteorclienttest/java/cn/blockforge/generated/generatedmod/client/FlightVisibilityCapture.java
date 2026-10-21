package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorHeadRenderer;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

/**
 * Opt-in local-player visibility evidence, independent of the old sword/full matrix.
 * Survival double-jump and W inputs run before vanilla input; production client END
 * samplers, pose, shaders and bloom render the actual LocalPlayer. Side views use an
 * explicitly recorded DEV camera offset; front views use vanilla third-person front.
 * This class and its temporary world are excluded from the release JAR.
 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FlightVisibilityCapture {
    private static final boolean FIRST_PERSON=Boolean.getBoolean("wuxingxiantu.realmFirstPersonCapture");
    private static final boolean HOVER=Boolean.getBoolean("wuxingxiantu.realmHoverCapture");
    private static final boolean STABILITY=Boolean.getBoolean("wuxingxiantu.realmFlowStabilityCapture");
    private static double lastFlowPhase=-1,lastFlowStrength,lastFlowTickTime,sectionTravel;
    private static int lastFlowSection=-1;
    private static final boolean SEAM=Boolean.getBoolean("wuxingxiantu.flightSeamCapture");
    private static final boolean ENABLED=SEAM||Boolean.getBoolean("wuxingxiantu.flightVisibilityCapture");
    private static final boolean EXPECT_PACK=Boolean.getBoolean("wuxingxiantu.flightVisibilityExpectedPackActive");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.flightVisibilityOutput",SEAM?"flight-seam":"flight-visibility")).toAbsolutePath();
    private static final long START=System.nanoTime();
    private static final int[] REALMS=SEAM?new int[]{4,8}:new int[]{4,5,8};
    private static final String[] NAMES=SEAM?new String[]{"huashen","dixian"}:new String[]{"huashen","lianxu","dixian"};
    private static final int SECTION_TICKS=SEAM?310:FIRST_PERSON||HOVER?205:185;
    private static final int EXPECTED_IMAGES=SEAM?120:12;
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>(),CHECKS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>(),GL_ERRORS=new ArrayList<>();
    private static CompletableFuture<?> preparation,timeChange;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> latestServer;
    private static Method cameraPosition,cameraRotation;
    private static boolean opened,initialized,requestedSection,sectionReady,finished,sideCamera,localDrawn;
    private static int section,tick,frameHeadStart,frameBloomStart,readinessFrames,actualMovementTicks,seamImages,frameSequence,seamEarlyFrames,seamLateFrames;
    private static float requestedPartial;
    private static String pendingCapture,packName="none";
    private static Vec3 movementStart,lastMotionPosition;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();
        try{
            if((System.nanoTime()-START)/1_000_000_000>300)throw new IllegalStateException("Visibility fixture exceeded 300-second watchdog");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.createDirectories(OUTPUT);
                mc.getWindow().setWindowed(1280,720);mc.options.renderDistance().set(6);mc.options.simulationDistance().set(3);mc.options.framerateLimit().set(SEAM?90:60);
                mc.options.fov().set(65);mc.options.bobView().set(false);mc.options.guiScale().set(2);mc.options.hideGui=true;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel((SEAM?"flight-seam-":"flight-visibility-")+System.currentTimeMillis(),
                    new LevelSettings("Local realm flight visibility verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.screen!=null)mc.setScreen(null);
            collectServerSample();
            if(!initialized){
                if(HOVER){HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(true);HudConfig.REALM_FLIGHT_DEFAULT_CAMERA.set(1);}
                initialized=true;HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);
                cameraPosition=Camera.class.getDeclaredMethod("setPosition",Vec3.class);cameraPosition.setAccessible(true);
                cameraRotation=Camera.class.getDeclaredMethod("setRotation",float.class,float.class);cameraRotation.setAccessible(true);
                if(ModList.get().isLoaded("oculus")){var iris=Class.forName("net.irisshaders.iris.Iris");packName=String.valueOf(iris.getMethod("getCurrentPackName").invoke(null));}
                check("actual_shader_pack",TrailShaderBridge.shaderPackDetected()==EXPECT_PACK,"Expected="+EXPECT_PACK+", actual="+TrailShaderBridge.shaderPackDetected()+", pack="+packName);
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(timeChange!=null){if(!timeChange.isDone())return;timeChange.join();timeChange=null;}
            if(!sectionReady){prepareSection(mc);return;}
            if(pendingCapture!=null&&!SEAM)return;
            tick++;
            if(SEAM){seamTick(mc);return;}
            mc.player.setYRot(-90);mc.player.setXRot(0);
            mc.options.keyJump.setDown(tick==20||tick==24||tick>=30&&tick<=45||HOVER&&(tick==190||tick==194));
            mc.options.keyUp.setDown(tick>=(HOVER?100:50)&&(!HOVER||tick<185));mc.options.keyRight.setDown(false);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(HOVER&&tick>=140&&tick<185);
            if(STABILITY&&tick>=100&&tick<185){
                mc.player.setYRot(-90+(tick-100)*9);
                mc.options.keyJump.setDown(tick>=108&&tick<128);
                mc.options.keyShift.setDown(tick>=154&&tick<172);
                mc.options.keySprint.setDown(tick>=132&&tick<158);
            }
            if(tick==50){movementStart=mc.player.position();lastMotionPosition=movementStart;sectionTravel=0;}
            sideCamera=tick<90||tick>=130&&tick<165;
            if(!HOVER||tick<190)mc.options.setCameraType(tick<50?CameraType.FIRST_PERSON:sideCamera?CameraType.THIRD_PERSON_BACK:CameraType.THIRD_PERSON_FRONT);
            if(FIRST_PERSON&&(!HOVER||tick<190)){mc.options.setCameraType(CameraType.FIRST_PERSON);if(tick==1)HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(true);}
            if(FIRST_PERSON&&!HOVER){
                if(tick==185)HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(false);
                if(tick>=191&&tick<=194)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                if(tick==195)HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(true);
            }
            if(tick==125)setSceneTime(mc,18000);
            String suffix=switch(tick){case 80->"local-day-side";case 115->"local-day-front";case 150->"local-night-side";case 180->"local-night-front";default->null;};
            if(suffix!=null){pendingCapture="realm-"+REALMS[section]+"-"+NAMES[section]+"-"+suffix+".png";readinessFrames=0;}
            if(tick==SECTION_TICKS){
                check("local_actual_W_displacement_realm_"+REALMS[section],movementStart!=null&&(STABILITY?sectionTravel:mc.player.position().subtract(movementStart).horizontalDistance())>15,"Actual movement distance="+(STABILITY?sectionTravel:movementStart==null?null:mc.player.position().subtract(movementStart).horizontalDistance()));
                clearKeys(mc);section++;tick=0;requestedSection=sectionReady=false;latestServer=null;
                if(section==REALMS.length){check("complete_actual_images",CAPTURES.size()==EXPECTED_IMAGES,"Actual PNGs="+CAPTURES.size()+", expected="+EXPECTED_IMAGES);check("no_GL_errors",GL_ERRORS.isEmpty(),GL_ERRORS.toString());finish();}
            }
        }catch(Throwable error){FAILURES.add("Fixture exception: "+error);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_VISIBILITY_CAPTURE",error);finish();}
    }
    /** Tick yaw and keys are the only path driver; no player teleport or direct sampler call while airborne. */
    private static void seamTick(Minecraft mc){
        int turn=Math.max(0,Math.min(180,tick-100));
        float yaw=turn<=45?-90+turn*2.8F:turn<=90?36-(turn-45)*3.5F:turn<=135?-121.5F+(turn-90)*3.2F:22.5F-(turn-135)*2.5F;
        boolean climb=turn>=60&&turn<100,descend=turn>=120&&turn<160;
        mc.player.setYRot(yaw);mc.player.setXRot(climb?-15:descend?15:0);
        mc.options.keyJump.setDown(tick==20||tick==24||tick>=30&&tick<=45||tick>=100&&climb);
        mc.options.keyShift.setDown(tick>=100&&descend);mc.options.keyUp.setDown(tick>=50);
        mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(tick>=80&&tick<=290);
        if(tick==50){movementStart=mc.player.position();lastMotionPosition=movementStart;}
        // Six actual front frames bracket two bends; other frames emphasize the rear neck from the side.
        sideCamera=!(seamImages>=17&&seamImages<=19||seamImages>=47&&seamImages<=49);
        mc.options.setCameraType(tick<50?CameraType.FIRST_PERSON:sideCamera?CameraType.THIRD_PERSON_BACK:CameraType.THIRD_PERSON_FRONT);
        if(seamImages<60&&tick>=100+seamImages*3&&pendingCapture==null){
            requestedPartial=(seamImages&1)==0?.1F:.9F;
            String action=turn<45?"left":turn<90?"right-climb":turn<135?"left-descend":"right-return";
            pendingCapture=String.format(java.util.Locale.ROOT,"realm-%d-%s-turn-%02d-%s-%s-%s.png",REALMS[section],NAMES[section],seamImages,action,sideCamera?"rear-side":"front",requestedPartial<.5?"early":"late");readinessFrames=0;
        }
        if(tick>=SECTION_TICKS&&pendingCapture==null){
            check("complete_turn_sequence_realm_"+REALMS[section],seamImages==60,"Actual turn screenshots="+seamImages+"; expected=60");
            check("actual_tick_boundary_coverage_realm_"+REALMS[section],seamEarlyFrames>=15&&seamLateFrames>=15,"Actual partial<=.15 frames="+seamEarlyFrames+"; partial>=.80 frames="+seamLateFrames+"; every image records its actual partial");
            check("local_actual_W_displacement_realm_"+REALMS[section],movementStart!=null&&mc.player.position().subtract(movementStart).length()>15,"Actual W displacement="+(movementStart==null?null:mc.player.position().subtract(movementStart).length()));
            clearKeys(mc);section++;tick=0;seamImages=seamEarlyFrames=seamLateFrames=0;requestedSection=sectionReady=false;latestServer=null;
            if(section==REALMS.length){check("complete_actual_images",CAPTURES.size()==EXPECTED_IMAGES,"Actual PNGs="+CAPTURES.size()+", expected="+EXPECTED_IMAGES);check("no_GL_errors",GL_ERRORS.isEmpty(),GL_ERRORS.toString());finish();}
        }
    }
    private static void prepareSection(Minecraft mc){
        if(!requestedSection){
            requestedSection=true;clearKeys(mc);sideCamera=false;mc.options.setCameraType(CameraType.FIRST_PERSON);
            UUID id=mc.player.getUUID();int realm=REALMS[section];
            preparation=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)for(int y=131;y<=137;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                p.teleportTo(0,131,0);p.setDeltaMovement(Vec3.ZERO);p.setYRot(-90);p.setXRot(0);p.setInvulnerable(true);p.setNoGravity(false);
                p.getAbilities().flying=false;p.onUpdateAbilities();
                var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(realm);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
                p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));
                level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);level.setWeatherParameters(6000,0,false,false);
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(level.getGameTime(),6000,false));GeneratedMod.sync(p);
            });return;
        }
        if(mc.player.onGround()&&mc.player.getAbilities().mayfly&&!mc.player.getAbilities().flying&&RealmFlightControl.eligible(mc.player)&&GeneratedMod.data(mc.player).wuxingxiantu$getRealm()==REALMS[section]){
            sectionReady=true;check("production_survival_permission_realm_"+REALMS[section],!mc.player.isCreative()&&!mc.player.isSpectator(),"Landed survival player; actual production mayfly=true; no fixture mayfly grant");
        }
    }
    private static void setSceneTime(Minecraft mc,long dayTime){
        UUID id=mc.player.getUUID();timeChange=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();level.setDayTime(dayTime);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(level.getGameTime(),dayTime,false));});
    }
    private static void collectServerSample(){if(serverSample!=null&&serverSample.isDone()){latestServer=serverSample.join();serverSample=null;}}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void telemetry(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||!sectionReady||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        if(tick>=50&&lastMotionPosition!=null){sectionTravel+=mc.player.position().distanceTo(lastMotionPosition);if(mc.player.position().distanceToSqr(lastMotionPosition)>1e-6)actualMovementTicks++;lastMotionPosition=mc.player.position();}
        if(serverSample==null){UUID id=mc.player.getUUID();serverSample=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var row=new LinkedHashMap<String,Object>();row.put("realm",GeneratedMod.data(p).wuxingxiantu$getRealm());row.put("stage",GeneratedMod.data(p).wuxingxiantu$getCultivationStage());row.put("flying",p.getAbilities().flying);row.put("mayfly",p.getAbilities().mayfly);row.put("creative",p.isCreative());row.put("position",List.of(p.getX(),p.getY(),p.getZ()));row.put("dayTime",p.serverLevel().getDayTime());return row;});}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event){
        if(ENABLED&&FIRST_PERSON){event.setPitch(0);return;}
        if(!ENABLED||finished||!sectionReady||!sideCamera||tick<50)return;
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        try{
            // Camera only: actual LocalPlayer and its local view-opacity branch remain intact.
            Vec3 target,position;
            if(SEAM){
                // Camera only follows the input azimuth; looking at the torso-to-history neck.
                double yaw=Math.toRadians(mc.player.getYRot());Vec3 forward=new Vec3(-Math.sin(yaw),0,Math.cos(yaw)),side=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
                target=mc.player.getPosition((float)event.getPartialTick()).add(0,1.0,0).add(forward.scale(-2));
                position=target.add(side.scale(7.5)).add(forward.scale(-4)).add(0,2.2,0);
            }else{target=mc.player.getPosition((float)event.getPartialTick()).add(HOVER?0:-3.5,.9,0);position=target.add(HOVER?-2:-1.5,HOVER?1.2:2.2,HOVER?4:12);}
            cameraPosition.invoke(event.getCamera(),position);Vec3 toward=target.subtract(position);
            float yaw=(float)Math.toDegrees(Math.atan2(-toward.x,toward.z)),pitch=(float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance()));
            cameraRotation.invoke(event.getCamera(),yaw,pitch);event.setYaw(yaw);event.setPitch(pitch);event.setRoll(0);
        }catch(Throwable error){FAILURES.add("Side camera exception: "+error);finish();}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void playerDraw(RenderPlayerEvent.Post event){
        if(ENABLED&&!finished&&event.getEntity()==Minecraft.getInstance().player)localDrawn=true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!initialized)return;
        if(event.phase==TickEvent.Phase.START){localDrawn=false;frameHeadStart=MeteorHeadRenderer.draws;frameBloomStart=MeteorBloomRenderer.draws;return;}
        frameSequence++;
        var mc=Minecraft.getInstance();
        if(STABILITY&&FIRST_PERSON&&sectionReady&&tick>=105&&tick<184&&mc.screen==null){
            check("moving_flow_remains_visible",FirstPersonFlightFlow.vertices==16,"tick="+tick);
            double phase=FirstPersonFlightFlow.advectionPhase,intensity=FirstPersonFlightFlow.strength;
            double tickTime=mc.player.tickCount+mc.getFrameTime();
            if(lastFlowSection==section&&lastFlowPhase>=0){
                check("continuous_flow_phase",phase>=lastFlowPhase-.00001&&phase-lastFlowPhase<=.196*Math.max(0,tickTime-lastFlowTickTime)+.001,"phase step="+(phase-lastFlowPhase)+", simulation tick step="+(tickTime-lastFlowTickTime));
                check("continuous_flow_strength",Math.abs(intensity-lastFlowStrength)<.36,"strength step="+(intensity-lastFlowStrength));
            }
            lastFlowPhase=phase;lastFlowStrength=intensity;lastFlowSection=section;lastFlowTickTime=tickTime;
        }
        if(HOVER&&sectionReady&&mc.player!=null){
            if(tick<50&&mc.player.getAbilities().flying)check("first_takeoff_keeps_view_realm_"+REALMS[section],mc.options.getCameraType().isFirstPerson(),"First-person double jump must not force default third person");
            if(tick==202)check("landing_keeps_view_realm_"+REALMS[section],!mc.player.getAbilities().flying&&mc.options.getCameraType()==(FIRST_PERSON?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_FRONT),"Double-jump flight exit, camera="+mc.options.getCameraType());
        }
        if(FIRST_PERSON&&!HOVER&&Minecraft.getInstance().player!=null&&sectionReady){
            if(tick==190)check("first_person_toggle_off_realm_"+REALMS[section],FirstPersonFlightFlow.vertices==0&&HudConfig.REALM_FLIGHT_EFFECTS.get(),"Own view flow disabled; global effect enabled");
            if(tick==193)check("third_person_unaffected_realm_"+REALMS[section],RealmFlightEffects.renderedVertices>0&&!HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.get(),"Third person world vertices="+RealmFlightEffects.renderedVertices+" while own first-person toggle is off");
        }
        int error;while((error=GL11.glGetError())!=GL11.GL_NO_ERROR)GL_ERRORS.add(section+":"+tick+":"+error);
        if(pendingCapture==null)return;if(mc.screen!=null||mc.getOverlay()!=null)return;
        // Sample both sides of simulation interpolation without modifying the game's clock.
        if(SEAM&&readinessFrames++<12&&(requestedPartial<.5?event.renderTickTime>.15F:event.renderTickTime<.80F))return;
        if(!mc.levelRenderer.isChunkCompiled(mc.player.blockPosition())&&readinessFrames++<120)return;
        try{
            Map<String,Object> metrics;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(pendingCapture));metrics=pixels(image);}
            collectServerSample();var d=GeneratedMod.data(mc.player);var row=new LinkedHashMap<String,Object>();
            row.put("screenshot",pendingCapture);row.put("realm",REALMS[section]);row.put("actualDataRealm",d.wuxingxiantu$getRealm());row.put("actualDataStage",d.wuxingxiantu$getCultivationStage());row.put("clientTick",tick);row.put("localPlayerUUID",mc.player.getUUID());row.put("actualClientFlying",mc.player.getAbilities().flying);row.put("actualClientMayfly",mc.player.getAbilities().mayfly);row.put("actualServer",latestServer);row.put("camera",mc.options.getCameraType().name());row.put("cameraScope",sideCamera?"DEV side-offset camera only; actual LocalPlayer production opacity branch":"Vanilla third-person front camera");
            row.put("renderSequence",frameSequence);row.put("actualPartialTick",event.renderTickTime);row.put("requestedPartialTickBand",SEAM?(requestedPartial<.5?"early <= .15":"late >= .80"):"unrestricted");row.put("partialBandReached",!SEAM||(requestedPartial<.5?event.renderTickTime<=.15F:event.renderTickTime>=.80F));row.put("actualYaw",mc.player.getYRot());row.put("actualPitch",mc.player.getXRot());row.put("actualMotion",List.of(mc.player.getDeltaMovement().x,mc.player.getDeltaMovement().y,mc.player.getDeltaMovement().z));row.put("actualWKey",mc.options.keyUp.isDown());row.put("actualBoostKey",mc.options.keySprint.isDown());row.put("actualAscentKey",mc.options.keyJump.isDown());row.put("actualDescentKey",mc.options.keyShift.isDown());
            Vec3 eye=mc.gameRenderer.getMainCamera().getPosition();row.put("cameraPosition",List.of(eye.x,eye.y,eye.z));row.put("position",List.of(mc.player.getX(),mc.player.getY(),mc.player.getZ()));row.put("actualLocalPlayerDrawn",localDrawn);row.put("actualCompiledLocalSection",mc.levelRenderer.isChunkCompiled(mc.player.blockPosition()));row.put("renderedShells",RealmFlightEffects.renderedShells);row.put("headDrawsInFrame",MeteorHeadRenderer.draws-frameHeadStart);row.put("bloomDrawsInFrame",MeteorBloomRenderer.draws-frameBloomStart);row.put("renderedTrailSegments",RealmFlightEffects.renderedTrailSegments);row.put("renderedVertices",RealmFlightEffects.renderedVertices);row.put("cachedSectionEvaluations",RealmFlightEffects.renderedSectionEvaluations);row.put("previousSectionEvaluations",RealmFlightEffects.uncachedSectionEvaluations);row.put("forwardBodyPoseWeight",RealmFlightVisuals.forwardShellWeight(mc.player,mc.getFrameTime()));row.put("lastPostDrawSucceeded",RealmFlightEffects.lastPostDrawSucceeded);row.put("dayTime",mc.level.getDayTime());row.put("metrics",metrics);row.put("productionFlight",flightDiagnostics(mc.player.getUUID()));CAPTURES.add(row);
            check("actual_survival_flight_"+pendingCapture,!mc.player.isCreative()&&!mc.player.isSpectator()&&mc.player.getAbilities().mayfly&&mc.player.getAbilities().flying&&RealmFlightControl.eligible(mc.player)&&latestServer!=null&&Boolean.TRUE.equals(latestServer.get("flying")),"Real survival double-jump flight client/server="+mc.player.getAbilities().flying+" / "+latestServer);
            check("actual_realm_"+pendingCapture,d.wuxingxiantu$getRealm()==REALMS[section]&&d.wuxingxiantu$getCultivationStage()==3,"Actual data realm/stage="+d.wuxingxiantu$getRealm()+" / "+d.wuxingxiantu$getCultivationStage());
            check("actual_local_model_"+pendingCapture,(FIRST_PERSON?!localDrawn:localDrawn)&&mc.levelRenderer.isChunkCompiled(mc.player.blockPosition()),"First person="+FIRST_PERSON+", RenderPlayer.Post local draw="+localDrawn+", actual section compiled="+mc.levelRenderer.isChunkCompiled(mc.player.blockPosition()));
            check("advanced_independent_shell_removed_"+pendingCapture,RealmFlightEffects.renderedShells==0&&MeteorHeadRenderer.draws-frameHeadStart==0,"Actual shell submissions/head draws="+RealmFlightEffects.renderedShells+" / "+(MeteorHeadRenderer.draws-frameHeadStart));
            if(FIRST_PERSON){
                check("fixed_view_flow_"+pendingCapture,(HOVER&&tick==80?FirstPersonFlightFlow.vertices==0:FirstPersonFlightFlow.vertices>0)&&RealmFlightEffects.renderedVertices==0,"View vertices="+FirstPersonFlightFlow.vertices+", world history vertices="+RealmFlightEffects.renderedVertices);
                row.put("firstPersonFlowVertices",FirstPersonFlightFlow.vertices);
                row.put("firstPersonFlowStrength",FirstPersonFlightFlow.strength);
                if(HOVER&&tick!=80)check("hover_cruise_boost_strength_"+pendingCapture,FirstPersonFlightFlow.strength>=(STABILITY? .44:tick>=140?.75:.44),"Flow strength="+FirstPersonFlightFlow.strength+", tick="+tick);
            }else if(HOVER){
                row.put("hoverVertices",0);
                check("hover_or_move_visible_"+pendingCapture,tick==80?RealmFlightEffects.renderedVertices==0:RealmFlightEffects.renderedVertices>0,"World vertices="+RealmFlightEffects.renderedVertices);
            }else{
                check("actual_production_trail_"+pendingCapture,TrailShaderBridge.meteorReady()&&RealmFlightEffects.renderedTrailSegments>0&&RealmFlightEffects.renderedVertices>0&&RealmFlightVisuals.forwardShellWeight(mc.player,mc.getFrameTime())>(SEAM?.5:.9),"Production shader ready; segments/vertices="+RealmFlightEffects.renderedTrailSegments+" / "+RealmFlightEffects.renderedVertices+"; poseWeight="+RealmFlightVisuals.forwardShellWeight(mc.player,mc.getFrameTime()));
                check("cached_geometry_work_"+pendingCapture,RealmFlightEffects.renderedSectionEvaluations>0&&RealmFlightEffects.renderedSectionEvaluations<RealmFlightEffects.uncachedSectionEvaluations,"section computations cached/previous="+RealmFlightEffects.renderedSectionEvaluations+" / "+RealmFlightEffects.uncachedSectionEvaluations);
                check("actual_bloom_draw_"+pendingCapture,MeteorBloomRenderer.draws-frameBloomStart>0,"Actual emission/bloom draw count="+(MeteorBloomRenderer.draws-frameBloomStart));
            }
            check("actual_scene_time_"+pendingCapture,mc.level.getDayTime()%24000==(pendingCapture.contains("night")?18000:6000),"Actual client day time="+mc.level.getDayTime());
            check("visible_world_"+pendingCapture,!Boolean.TRUE.equals(metrics.get("blackScreenCandidate")),"Pixel statistics are diagnostics only="+metrics);
            com.mojang.logging.LogUtils.getLogger().info("FLIGHT_VISIBILITY_IMAGE {}",row);pendingCapture=null;if(SEAM){seamImages++;if(event.renderTickTime<=.15F)seamEarlyFrames++;if(event.renderTickTime>=.80F)seamLateFrames++;}write();
        }catch(Throwable failure){FAILURES.add("Render capture exception: "+failure);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_VISIBILITY_FRAME",failure);finish();}
    }
    private static Map<String,Object> flightDiagnostics(UUID id)throws ReflectiveOperationException{
        Field flights=RealmFlightEffects.class.getDeclaredField("FLIGHTS");flights.setAccessible(true);Object flight=((Map<?,?>)flights.get(null)).get(id);var row=new LinkedHashMap<String,Object>();if(flight==null)return row;
        for(String name:List.of("trailLength","glow","speedWeight","flowPhase")){Field f=flight.getClass().getDeclaredField(name);f.setAccessible(true);row.put(name,f.get(flight));}
        Field path=flight.getClass().getDeclaredField("path");path.setAccessible(true);var actual=(RealmMeteorPath)path.get(flight);row.put("historySamples",actual.sampleCount());row.put("historySpeed",actual.speed());row.put("historyDirection",actual.direction());
        Field clock=RealmFlightEffects.class.getDeclaredField("clock");clock.setAccessible(true);row.put("productionMonotonicClock",clock.get(null));
        Field orientation=flight.getClass().getDeclaredField("orientation");orientation.setAccessible(true);Object orientationState=orientation.get(flight);var basis=new LinkedHashMap<String,Object>();
        for(String name:List.of("heading","previousHeading","side","previousSide"))basis.put(name,orientationState.getClass().getField(name).get(orientationState));row.put("actualProductionHeadOrientation",basis);
        var mc=Minecraft.getInstance();row.put("actualWorldGameTime",mc.level.getGameTime());
        double partial=mc.getFrameTime(),now=((Number)clock.get(null)).longValue()-1+partial;Vec3 center=mc.player.getPosition((float)partial).add(0,mc.player.getBbHeight()*.5,0);
        Field previousLength=flight.getClass().getDeclaredField("previousTrailLength");previousLength.setAccessible(true);
        double length=net.minecraft.util.Mth.lerp(partial,previousLength.getDouble(flight),((Number)row.get("trailLength")).doubleValue());var frames=actual.renderAdaptive(now,new RealmMeteorPath.Vec(center.x,center.y,center.z),length,cn.blockforge.generated.generatedmod.realmflight.RealmMeteorTuning.TRAIL_LIFETIME_TICKS,0);
        row.put("actualAdaptiveFrames",frames.size());row.put("actualRenderClock",now);
        if(!frames.isEmpty()){
            row.put("actualHeadFrame",frames.get(frames.size()-1));row.put("actualRearNearHeadFrames",frames.subList(Math.max(0,frames.size()-6),frames.size()));
            // Read the actual submitted tube data; do not reconstruct its basis or width for evidence.
            Field tubes=RealmFlightEffects.class.getDeclaredField("TUBE_FRAMES");tubes.setAccessible(true);Object[] submitted=(Object[])tubes.get(null);
            var join=new ArrayList<Map<String,Object>>();
            for(int i=Math.max(0,frames.size()-8);i<Math.min(submitted.length,frames.size()+5);i++){
                var sample=new LinkedHashMap<String,Object>();sample.put("index",i);
                for(String name:List.of("frame","width","behind","opacity")){Field f=submitted[i].getClass().getDeclaredField(name);f.setAccessible(true);sample.put(name,f.get(submitted[i]));}
                join.add(sample);
            }
            row.put("actualSubmittedJoinFrames",join);
        }
        return row;
    }
    private static Map<String,Object> pixels(NativeImage image){
        long sum=0;int dark=0,total=0;for(int y=image.getHeight()/4;y<image.getHeight()*3/4;y+=4)for(int x=image.getWidth()/4;x<image.getWidth()*3/4;x+=4){int p=image.getPixelRGBA(x,y),r=p&255,g=(p>>>8)&255,b=(p>>>16)&255;int v=(r+g+b)/3;sum+=v;if(v<6)dark++;total++;}
        var row=new LinkedHashMap<String,Object>();row.put("width",image.getWidth());row.put("height",image.getHeight());row.put("centralMean",sum/(double)Math.max(1,total));row.put("centralDarkFraction",dark/(double)Math.max(1,total));row.put("blackScreenCandidate",dark/(double)Math.max(1,total)>.95);return row;
    }
    private static void clearKeys(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyShift.setDown(false);}
    private static void check(String name,boolean passed,String details){var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("passed",passed);row.put("details",details);CHECKS.add(row);if(!passed)FAILURES.add(name+": "+details);}
    private static void write()throws java.io.IOException{
        var result=new LinkedHashMap<String,Object>();result.put("fixture",SEAM?"flightSeamCapture":"flightVisibilityCapture");result.put("result",FAILURES.isEmpty()&&GL_ERRORS.isEmpty()&&finished?"PASS":finished?"FAIL":"RUNNING");result.put("expectedImages",EXPECTED_IMAGES);result.put("actualImages",CAPTURES.size());result.put("packName",packName);result.put("actualShaderPackActive",TrailShaderBridge.shaderPackDetected());result.put("scope","Fresh isolated survival world, real local double-space and W input, production END history/pose/shader renderer; no RemotePlayer, no sword, no multiplayer/FPS claim. Side pictures use DEV camera position only; front pictures use vanilla third-person front. Actual source/shader/JAR binding must be supplied by the external delivery verifier. Pixel metrics do not establish aesthetic approval.");result.put("turnSequence",SEAM?"Each realm: 60 actual screenshots over 180 input ticks; continuous left/right yaw, real jump/shift climbing/descent, both interpolation boundary bands; six front frames, remainder rear-side neck camera. Screenshot capture never teleports or directly modifies production history.":null);result.put("outputAbsolutePath",OUTPUT.toString());result.put("actualMovementClientTicks",actualMovementTicks);result.put("captures",CAPTURES);result.put("checks",CHECKS);result.put("failures",FAILURES);result.put("glErrors",GL_ERRORS);Files.writeString(OUTPUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(result));
    }
    private static void finish(){
        if(finished)return;finished=true;try{clearKeys(Minecraft.getInstance());Files.createDirectories(OUTPUT);write();Files.writeString(OUTPUT.resolve("result.txt"),(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL")+": "+CAPTURES.size()+" actual local screenshots; "+CHECKS.size()+" checks; failures="+FAILURES+"; GL="+GL_ERRORS);}catch(Throwable failure){com.mojang.logging.LogUtils.getLogger().error("FLIGHT_VISIBILITY_RESULT",failure);}Minecraft.getInstance().stop();
    }
    private FlightVisibilityCapture(){}
}
