package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer;
import cn.blockforge.generated.generatedmod.client.realmflight.MeteorHeadRenderer;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightVisualPacket;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
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
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

/** Independent Forge client fixture. Neither this class nor its world enters the release JAR. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID, value=Dist.CLIENT)
public final class MeteorClientCapture {
    private static final long START = System.nanoTime();
    private static final UUID SUBJECT_ID = UUID.fromString("3c98ab4d-d5fa-422b-b60c-e6e61b6337b3");
    private static final boolean EXPECT_EXTERNAL_SHADERS = Boolean.getBoolean("wuxingxiantu.meteorExternalShaders");
    private static final boolean CAMERA_CYCLE = Boolean.getBoolean("wuxingxiantu.cameraCycleCapture") || Boolean.getBoolean("wuxingxiantu.flightTierCapture") || Boolean.getBoolean("wuxingxiantu.flightVisibilityCapture") || Boolean.getBoolean("wuxingxiantu.flightSelectionCapture");
    private static final double ALTITUDE = 135;
    private static final Path OUTPUT = Path.of("captures");
    private static final Path SEQUENCE = Path.of("turn-sequence");
    private static final List<Map<String,Object>> CAPTURES = new ArrayList<>();
    private static final List<String> GL_ERRORS = new ArrayList<>();
    private static final List<RemotePlayer> EXTRA_SUBJECTS = new ArrayList<>();
    private static final Map<String,List<Integer>> FPS_SAMPLES = new LinkedHashMap<>();
    private static boolean opened, prepared, finished, overrideCamera = true, localObliqueCamera, forcedPost, compositePrepared, firstPersonPassed;
    private static int tick, reloadStability, shaderIdentity, lastSequenceTick=-1, sequenceFrames, disabledBloomDraws, headDrawsAtFrameStart;
    private static boolean wallPlaced;
    private static final List<BlockPos> WALL = new ArrayList<>();
    private static String requestedCapture;
    private static RemotePlayer subject;
    private static Vec3 lateFlightOrigin;
    private static CompletableFuture<?> serverPreparation, reload;
    private static Vec3 cameraOffset = new Vec3(5,2,8), lastPosition = new Vec3(0,ALTITUDE,0);
    private static String externalPackName = "none";
    private static Method cameraPosition;
    private static Object originalIrisApi;
    private static Method originalShaderEnabled;
    private static boolean originalSearched;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event) {
        // Move after ClientLevel entity ticks, before the production END-phase history/pose samplers.
        if (CAMERA_CYCLE || finished || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if ((System.nanoTime()-START)/1_000_000_000 > 360) throw new IllegalStateException("Meteor client capture timed out");
            mc.setWindowActive(true);
            mc.options.pauseOnLostFocus = false;
            if (!opened && mc.getOverlay() == null && (mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    || mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened = true;
                Files.createDirectories(OUTPUT);
                Files.createDirectories(SEQUENCE);
                mc.getWindow().setWindowed(1280,720);
                mc.options.renderDistance().set(3);
                mc.options.simulationDistance().set(3);
                mc.options.framerateLimit().set(60);
                mc.options.fov().set(65);
                mc.options.bobView().set(false);
                mc.options.guiScale().set(2);
                mc.options.hideGui = true;
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                LevelSettings settings = new LevelSettings("Meteor flight verification", GameType.SURVIVAL,
                        false, net.minecraft.world.Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel("meteor-verification-"+System.currentTimeMillis(), settings,
                        new WorldOptions(712309L,false,false), net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null
                    || mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen) return;
            if (mc.screen != null) mc.setScreen(null);
            if (!prepared) {
                prepared = true;
                HudConfig.REALM_FLIGHT_ANIMATION.set(true);
                HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);
                UUID local = mc.player.getUUID();
                serverPreparation = mc.getSingleplayerServer().submitAsync(()->{
                    var p = mc.getSingleplayerServer().getPlayerList().getPlayer(local);
                    var d = GeneratedMod.data(p);
                    d.wuxingxiantu$setRealm(8);
                    d.wuxingxiantu$setCultivationStage(3);
                    d.wuxingxiantu$setMaxMana(1_000_000);
                    d.wuxingxiantu$setAura(1_000_000);
                    p.teleportTo(0,ALTITUDE,-18);
                    p.setInvulnerable(true);
                    p.getAbilities().mayfly = true;
                    p.getAbilities().flying = true;
                    p.onUpdateAbilities();
                    p.serverLevel().setDayTime(11000);
                    p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);
                    p.serverLevel().setWeatherParameters(6000,0,false,false);
                    GeneratedMod.sync(p);
                });
                return;
            }
            if (serverPreparation != null) {
                if (!serverPreparation.isDone()) return;
                serverPreparation.join(); serverPreparation = null;
            }
            if (subject == null) {
                subject = new RemotePlayer(mc.level, new GameProfile(SUBJECT_ID,"MeteorSubject"));
                subject.setPos(lastPosition.x,lastPosition.y,lastPosition.z);
                subject.xOld=lastPosition.x; subject.yOld=lastPosition.y; subject.zOld=lastPosition.z;
                subject.setNoGravity(true);
                subject.setInvulnerable(true);
                subject.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));
                subject.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));
                subject.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));
                mc.level.addPlayer(-56781,subject);
                cameraPosition = Camera.class.getDeclaredMethod("setPosition",Vec3.class);
                cameraPosition.setAccessible(true);
            }
            if (reload != null) {
                if (!reload.isDone() || mc.getOverlay()!=null) { reloadStability=0; return; }
                reload.join();
                if (++reloadStability < 20) return;
                if (!TrailShaderBridge.meteorReady() || !MeteorHeadRenderer.ready()) throw new IllegalStateException("Meteor trail/head shaders unavailable after resource reload");
                if (shaderIdentity == System.identityHashCode(TrailShaderBridge.meteorShader())) throw new IllegalStateException("Resource reload did not replace meteor shader");
                requestedCapture = "09-after-resource-reload.png";
                reload = null;
            }
            tick++;
            if (tick==60 && EXPECT_EXTERNAL_SHADERS) {
                if (!TrailShaderBridge.shaderPackDetected()) throw new IllegalStateException("Requested external shader pack is not active");
                Class<?> iris=Class.forName("net.irisshaders.iris.Iris");
                externalPackName=(String)iris.getMethod("getCurrentPackName").invoke(null);
                if (!externalPackName.contains("Solas")) throw new IllegalStateException("Expected Solas, active pack="+externalPackName);
            }
            updateScene(mc);
            if (tick==70) requestedCapture="01-hover-three-dimensional-shell.png";
            if (tick==155) requestedCapture="02-cruise-body-and-shader-trail.png";
            if (tick==245) requestedCapture="03-boost-long-meteor-trail.png";
            if (tick==342) requestedCapture="04-turn-curved-history.png";
            if (tick==420) requestedCapture="05-distant-observer.png";
            if (tick==500) {
                if (!RealmFlightControl.eligible(mc.player) || !mc.player.getAbilities().flying) throw new IllegalStateException("Local first-person fixture is not actually realm flying");
                firstPersonPassed=true;
                requestedCapture="06-first-person-clear-view.png";
            }
            if (tick==570) requestedCapture="07-stopped-and-expired-control.png";
            if (tick==660) requestedCapture=EXPECT_EXTERNAL_SHADERS?"08-solas-depth-composite-route.png":"08-forced-depth-composite-route.png";
            if (tick==681) {
                shaderIdentity=System.identityHashCode(TrailShaderBridge.meteorShader());
                reload=mc.reloadResourcePacks(); reloadStability=0;
            }
            if (tick==730) {mc.getWindow().setWindowed(1600,900);mc.resizeDisplay();}
            if (tick==775) requestedCapture="10-resized-depth-composite.png";
            if (tick==801) {disabledBloomDraws=MeteorBloomRenderer.draws;System.setProperty("wuxingxiantu.meteorBloomDisabled","true");}
            if (tick==850) requestedCapture="11-no-bloom-single-plume-side.png";
            if (tick==900) requestedCapture="12-no-bloom-rear-width.png";
            if (tick==940) requestedCapture="13-no-bloom-front-open-head.png";
            if (tick==980) requestedCapture="14-no-bloom-48-block-observer.png";
            if (tick==991) {System.clearProperty("wuxingxiantu.meteorBloomDisabled");HudConfig.REALM_FLIGHT_ANIMATION.set(false);}
            if (tick==1040) requestedCapture="15-remote-visible-with-local-vanilla-preference.png";
            if (tick==1051) {
                HudConfig.REALM_FLIGHT_ANIMATION.set(true);
                UUID local=mc.player.getUUID();
                serverPreparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(local);
                    GeneratedMod.data(p).wuxingxiantu$setRealm(2);
                    p.getAbilities().flying=false;p.setNoGravity(true);p.onUpdateAbilities();GeneratedMod.sync(p);
                });
            }
            if (tick==1105) requestedCapture="16-realm2-inactive-clear-control.png";
            if (tick==1121) {
                UUID local=mc.player.getUUID();
                serverPreparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(local);
                    GeneratedMod.data(p).wuxingxiantu$setRealm(8);
                    p.getAbilities().flying=true;p.getAbilities().mayfly=true;p.onUpdateAbilities();GeneratedMod.sync(p);
                });
            }
            if (tick==1125) {
                var center=BlockPos.containing(lastPosition).offset(0,0,4);
                UUID local=mc.player.getUUID();
                serverPreparation=mc.getSingleplayerServer().submitAsync(()->{
                    var level=mc.getSingleplayerServer().getPlayerList().getPlayer(local).serverLevel();
                    for(int x=-6;x<=6;x++)for(int y=-3;y<=5;y++) {
                        var at=center.offset(x,y,0);level.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());WALL.add(at);
                    }
                });wallPlaced=true;
            }
            if (tick==1180) requestedCapture="17-stone-wall-occlusion-with-energy.png";
            if (tick==1210) requestedCapture="18-stone-wall-no-energy-control.png";
            if (tick==1241) {
                overrideCamera=false;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                mc.player.setYRot(-90);mc.player.setXRot(6);mc.options.keyUp.setDown(true);
            }
            if (tick==1290) {
                if (!RealmFlightControl.eligible(mc.player)||!mc.player.getAbilities().flying) throw new IllegalStateException("Local third-person fixture is not actually realm flying");
                requestedCapture="19-local-third-person-body-and-flight.png";
            }
            if (tick==1291) {
                mc.options.keyUp.setDown(false);mc.options.setCameraType(CameraType.FIRST_PERSON);
                lateFlightOrigin=lastPosition;
            }
            if (tick==1400) requestedCapture="20-four-observed-flying-players.png";
            if (tick==1411) {
                for(var extra:EXTRA_SUBJECTS) {
                    RealmFlightVisuals.receive(new RealmFlightVisualPacket(extra.getUUID(),false,8,3,false,true));
                    mc.level.removeEntity(extra.getId(),net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
                EXTRA_SUBJECTS.clear();
                UUID local=mc.player.getUUID();
                serverPreparation=mc.getSingleplayerServer().submitAsync(()->{
                    var level=mc.getSingleplayerServer().getPlayerList().getPlayer(local).serverLevel();level.setDayTime(18000);
                });
            }
            if (tick==1490) requestedCapture="21-night-meteor-flight.png";
            if (tick==1511) {
                localObliqueCamera=true;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                mc.player.setYRot(-90);mc.player.setXRot(6);mc.options.keyUp.setDown(true);
            }
            if (tick==1540) {
                if(!RealmFlightControl.eligible(mc.player)||!mc.player.getAbilities().flying)throw new IllegalStateException("Oblique local flight fixture is not actually realm flying");
                requestedCapture="22-local-third-person-oblique-side.png";
            }
            if (tick==1550) {
                if (CAPTURES.size()!=22) throw new IllegalStateException("Expected twenty-two captures, got "+CAPTURES.size());
                if (sequenceFrames<10) throw new IllegalStateException("Expected ten actual turning frames, got "+sequenceFrames);
                if (!GL_ERRORS.isEmpty()) throw new IllegalStateException("Render frames reported OpenGL errors: "+GL_ERRORS);
                finish("PASS: isolated actual Forge rendering, twenty-two diagnostic captures, turning frame sequence, Bloom OFF shape views, local vanilla preference with remote effects, realm2 inactive, local first/third-person flight and oblique side view, four visible flying players, night flight, stopped fade, stone-wall depth occlusion, resource reload and resize. External shader pack="+externalPackName+"; forced route fixture="+forcedPost+".");
            }
        } catch (Throwable error) {
            com.mojang.logging.LogUtils.getLogger().error("METEOR_CLIENT_CAPTURE",error);
            finish("FAIL: "+error);
        }
    }

    private static void updateScene(Minecraft mc) throws Exception {
        boolean active=true, boost=false;
        Vec3 position;
        if (tick<=85) {
            position=new Vec3(0,ALTITUDE,0);cameraOffset=new Vec3(5,2,8);
        } else if (tick<=180) {
            position=new Vec3((tick-85)*.5,ALTITUDE,0);cameraOffset=new Vec3(-5,3,11);
        } else if (tick<=270) {
            position=new Vec3(47.5+(tick-180)*.95,ALTITUDE,0);cameraOffset=new Vec3(-4,2,8);boost=true;
        } else if (tick<=360) {
            double angle=-Math.PI/2+(tick-270)*.04;
            position=new Vec3(133+20*Math.cos(angle),ALTITUDE+Math.sin((tick-270)*.02)*.6,20+20*Math.sin(angle));
            cameraOffset=new Vec3(-6,4,12);boost=true;
        } else if (tick<=445) {
            Vec3 start=turnEnd();position=start.add((tick-360)*.65,0,0);
            cameraOffset=new Vec3(-48,10,72);
        } else if (tick<=515) {
            position=lastPosition;active=false;overrideCamera=false;mc.options.hideGui=false;
            if (tick==446) {
                serverPreparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    p.teleportTo(133,ALTITUDE,-10);p.setYRot(-90);p.setXRot(6);
                    p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();
                });
            }
            mc.player.setYRot(-90);mc.player.setXRot(6);
        } else if (tick<=580) {
            position=lastPosition;active=false;overrideCamera=true;mc.options.hideGui=true;cameraOffset=new Vec3(5,2,8);
        } else if (tick<=1050) {
            overrideCamera=true;mc.options.hideGui=true;boost=true;
            if (!compositePrepared) {
                if (!TrailShaderBridge.shaderPackDetected()) forceCompositeRoute();
                compositePrepared=true;
            }
            // Stay near loaded terrain while maintaining a real curved emitter history.
            double angle=(tick-581)*.045;
            position=new Vec3(145+18*Math.cos(angle),ALTITUDE+Math.sin(angle*.6)*1.5,15+18*Math.sin(angle));
            cameraOffset=new Vec3(-4,3,10);
            Vec3 direction=new Vec3(-Math.sin(angle),0,Math.cos(angle));
            if (tick>=871&&tick<=910) cameraOffset=direction.scale(-8).add(0,2,.05);
            else if (tick>=911&&tick<=950) cameraOffset=direction.scale(8).add(0,2,.05);
            else if (tick>=951&&tick<=990) cameraOffset=new Vec3(-25,6,42);
        } else if(tick<=1290) {
            position=lastPosition;boost=false;overrideCamera=true;mc.options.hideGui=true;
            active=tick>1120&&tick<1190;
            cameraOffset=tick>1120?new Vec3(0,2,8):new Vec3(5,2,8);
            if(tick>=1241) {overrideCamera=false;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
        } else {
            if(lateFlightOrigin==null)lateFlightOrigin=lastPosition;
            position=lateFlightOrigin.add((tick-1291)*.60,0,0);boost=true;
            overrideCamera=true;mc.options.hideGui=true;cameraOffset=new Vec3(6,5,24);
            if(tick<=1410)updateExtraSubjects(mc,position);
            if(tick>=1511) {active=false;localObliqueCamera=true;cameraOffset=new Vec3(-5,2,5);}
        }
        Vec3 delta=position.subtract(lastPosition);
        subject.setOldPosAndRot();
        subject.xOld=lastPosition.x;subject.yOld=lastPosition.y;subject.zOld=lastPosition.z;
        subject.setPos(position.x,position.y,position.z);
        subject.setDeltaMovement(delta);
        float yaw=delta.horizontalDistance()>1e-5?(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)):-90;
        subject.setYRot(yaw);subject.yBodyRot=yaw;subject.yHeadRot=yaw;subject.setXRot(-5);
        lastPosition=position;
        if (tick%20==0 && overrideCamera && active) {
            UUID local=mc.player.getUUID();Vec3 observer=position.add(0,0,-18);
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(local);
                if (p!=null) p.teleportTo(observer.x,observer.y,observer.z);
            });
        }
        int realm=tick>=1051&&tick<=1120?2:8;
        RealmFlightVisuals.receive(new RealmFlightVisualPacket(SUBJECT_ID,active,realm,realm==2?0:3,false,boost));
        if (active && !RealmFlightEffects.transformed(subject)) throw new IllegalStateException("Remote visual state rejected");
    }

    private static void updateExtraSubjects(Minecraft mc,Vec3 leaderPosition) {
        Vec3[] offsets={new Vec3(-5,3,-5),new Vec3(-7,-2,6),new Vec3(3,1,6)};
        for(int i=0;i<offsets.length;i++) {
            Vec3 position=leaderPosition.add(offsets[i]);RemotePlayer extra;
            if(EXTRA_SUBJECTS.size()<=i) {
                UUID id=UUID.nameUUIDFromBytes(("meteor-extra-"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                extra=new RemotePlayer(mc.level,new GameProfile(id,"MeteorExtra"+i));
                extra.setPos(position.x,position.y,position.z);extra.setNoGravity(true);extra.setInvulnerable(true);
                extra.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));
                mc.level.addPlayer(-56782-i,extra);EXTRA_SUBJECTS.add(extra);
            }else extra=EXTRA_SUBJECTS.get(i);
            Vec3 old=extra.position();extra.setOldPosAndRot();extra.xOld=old.x;extra.yOld=old.y;extra.zOld=old.z;
            extra.setPos(position.x,position.y,position.z);extra.setDeltaMovement(position.subtract(old));
            extra.setYRot(-90);extra.yBodyRot=-90;extra.yHeadRot=-90;extra.setXRot(-5);
            RealmFlightVisuals.receive(new RealmFlightVisualPacket(extra.getUUID(),true,8,3,false,true));
        }
    }

    private static Vec3 turnEnd() {
        double angle=-Math.PI/2+90*.04;
        return new Vec3(133+20*Math.cos(angle),ALTITUDE+Math.sin(90*.02)*.6,20+20*Math.sin(angle));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if (CAMERA_CYCLE || finished || !overrideCamera || subject==null || cameraPosition==null) return;
        try {
            var actor=localObliqueCamera?Minecraft.getInstance().player:subject;
            if(actor==null)return;
            Vec3 target=actor.getPosition((float)event.getPartialTick()).add(0,.9,0);
            Vec3 position=target.add(cameraOffset);
            cameraPosition.invoke(event.getCamera(),position);
            Vec3 toward=target.subtract(position);
            event.setYaw((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));
            event.setPitch((float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance())));
            event.setRoll(0);
        } catch (Throwable error) {finish("FAIL: camera fixture: "+error);}
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event) {
        if (CAMERA_CYCLE || finished || subject==null) return;
        if (event.phase==TickEvent.Phase.START) {headDrawsAtFrameStart=MeteorHeadRenderer.draws;return;}
        String fpsGroup=tick>=1260&&tick<=1290?"singleLocalThirdPerson":tick>=1370&&tick<=1400?"fourRemotePlayers":null;
        if(fpsGroup!=null)FPS_SAMPLES.computeIfAbsent(fpsGroup,key->new ArrayList<>()).add(Minecraft.getInstance().getFps());
        int gl=GL11.glGetError();if (gl!=GL11.GL_NO_ERROR) GL_ERRORS.add(tick+":"+gl);
        if (tick>=280&&tick<=360&&tick%4==0&&lastSequenceTick!=tick&&Minecraft.getInstance().getOverlay()==null) {
            try (var frame=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
                frame.writeToFile(SEQUENCE.resolve(String.format(java.util.Locale.ROOT,"frame-%03d.png",sequenceFrames++)));lastSequenceTick=tick;
            } catch (Throwable error) {finish("FAIL: actual frame sequence: "+error);return;}
        }
        if (requestedCapture==null) return;
        Minecraft mc=Minecraft.getInstance();
        if (mc.screen!=null || mc.getOverlay()!=null) return;
        try {
            if (!TrailShaderBridge.meteorReady() || !MeteorHeadRenderer.ready()) throw new IllegalStateException("Meteor trail/head shaders were not registered at capture");
            String name=requestedCapture;requestedCapture=null;
            boolean clear=name.startsWith("06-")||name.startsWith("07-")||name.startsWith("16-")||name.startsWith("18-");
            boolean bloomDisabled=Boolean.getBoolean("wuxingxiantu.meteorBloomDisabled");
            int frameHeadDraws=MeteorHeadRenderer.draws-headDrawsAtFrameStart;
            if (clear && RealmFlightEffects.renderedVertices!=0) throw new IllegalStateException("First-person/expired control retained effect geometry: "+name);
            if (clear && frameHeadDraws!=0) throw new IllegalStateException("First-person/expired control retained energy head draws: "+name);
            if (!clear && (RealmFlightEffects.renderedVertices==0 || RealmFlightEffects.renderedShells==0)) throw new IllegalStateException("Active flight did not submit a three-dimensional shell: "+name);
            if (!clear && frameHeadDraws==0) throw new IllegalStateException("Active flight did not draw its procedural three-dimensional head: "+name);
            if (!clear && !name.startsWith("01-") && !name.startsWith("17-") && RealmFlightEffects.renderedTrailSegments==0) throw new IllegalStateException("Moving flight did not submit its actual path: "+name);
            if((name.startsWith("19-")||name.startsWith("22-"))&&(mc.options.getCameraType()!=CameraType.THIRD_PERSON_BACK||!RealmFlightEffects.transformed(mc.player)))throw new IllegalStateException("Local third-person capture is not actually enabled realm flight");
            if(name.startsWith("22-")&&!localObliqueCamera)throw new IllegalStateException("Local oblique camera fixture is not enabled");
            if(name.startsWith("20-")&&(EXTRA_SUBJECTS.size()!=3||RealmFlightEffects.renderedShells!=4))throw new IllegalStateException("Expected four visible active energy heads");
            if(name.startsWith("21-")&&mc.level.getDayTime()%24000!=18000)throw new IllegalStateException("Night capture did not enter actual night");
            if (!clear && RealmFlightEffects.renderedTrailSegments>0 && TrailShaderBridge.shaderPackDetected() && !RealmFlightEffects.lastPostDrawSucceeded) throw new IllegalStateException("Trail depth-composite draw did not complete: "+name);
            if (!clear && !bloomDisabled && MeteorBloomRenderer.draws==0) throw new IllegalStateException("Selective emission bloom did not draw: "+name+"; "+MeteorBloomRenderer.status);
            if (bloomDisabled && MeteorBloomRenderer.draws!=disabledBloomDraws) throw new IllegalStateException("Bloom OFF diagnostic still drew bloom: "+name);
            try (var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {frame.writeToFile(OUTPUT.resolve(name));}
            Map<String,Object> row=new LinkedHashMap<>();row.put("name",name);row.put("tick",tick);
            row.put("width",mc.getMainRenderTarget().viewWidth);row.put("height",mc.getMainRenderTarget().viewHeight);
            row.put("shaderReady",TrailShaderBridge.meteorReady());row.put("forcedCompositeFixture",forcedPost);
            row.put("headShaderReady",MeteorHeadRenderer.ready());row.put("proceduralHeadDrawsInFrame",frameHeadDraws);
            row.put("externalShaderPack",externalPackName);row.put("shaderPackActuallyActive",TrailShaderBridge.shaderPackDetected()&&!forcedPost);
            row.put("depthCompositeRouteActive",TrailShaderBridge.shaderPackDetected());
            row.put("selectiveBloomDraws",MeteorBloomRenderer.draws);row.put("selectiveBloomStatus",MeteorBloomRenderer.status);
            row.put("bloomDisabledForShapeProof",bloomDisabled);row.put("framesPerSecond",mc.getFps());
            row.put("cameraType",mc.options.getCameraType().name());row.put("dayTime",mc.level.getDayTime());
            row.put("cameraTargetsActualLocalPlayer",localObliqueCamera);
            row.put("extraRemoteSubjects",EXTRA_SUBJECTS.size());
            row.put("localSpecialFlightPreference",HudConfig.REALM_FLIGHT_ANIMATION.get());row.put("occlusionWallPlaced",wallPlaced);
            row.put("depthStatus",TrailShaderBridge.depthStatus);row.put("cameraPosition",eventCameraPosition(mc));
            row.put("subjectPosition",subject.position().toString());row.put("localFlight",mc.player.getAbilities().flying);
            for (String field:new String[]{"renderedVertices","renderedShells","renderedTrailSegments","lastPostDrawSucceeded","renderedShellVertices","renderedTrailVertices","renderedTrailPoints","renderedSegments","renderedPlayers","lastVertexCount"}) {
                try {Field f=RealmFlightEffects.class.getDeclaredField(field);f.setAccessible(true);row.put(field,f.get(null));}catch(ReflectiveOperationException ignored){}
            }
            CAPTURES.add(row);
            com.mojang.logging.LogUtils.getLogger().info("METEOR_CAPTURE {}",row);
            writeResult("RUNNING");
        } catch (Throwable error) {finish("FAIL: screenshot: "+error);}
    }

    private static String eventCameraPosition(Minecraft mc) {return mc.gameRenderer.getMainCamera().getPosition().toString();}

    /** Deliberately tests the depth route without pretending an external shader pack is installed. */
    public static final class CompositeFixture {public boolean isShaderPackInUse(){return true;}}
    private static void forceCompositeRoute() throws Exception {
        Field api=TrailShaderBridge.class.getDeclaredField("irisApi"), enabled=TrailShaderBridge.class.getDeclaredField("shaderEnabled"), searched=TrailShaderBridge.class.getDeclaredField("searched");
        api.setAccessible(true);enabled.setAccessible(true);searched.setAccessible(true);
        originalIrisApi=api.get(null);originalShaderEnabled=(Method)enabled.get(null);originalSearched=searched.getBoolean(null);
        api.set(null,new CompositeFixture());enabled.set(null,CompositeFixture.class.getMethod("isShaderPackInUse"));searched.setBoolean(null,true);
        forcedPost=true;
    }
    private static void restoreCompositeRoute() throws Exception {
        if (!forcedPost) return;
        Field api=TrailShaderBridge.class.getDeclaredField("irisApi"), enabled=TrailShaderBridge.class.getDeclaredField("shaderEnabled"), searched=TrailShaderBridge.class.getDeclaredField("searched");
        api.setAccessible(true);enabled.setAccessible(true);searched.setAccessible(true);
        api.set(null,originalIrisApi);enabled.set(null,originalShaderEnabled);searched.setBoolean(null,originalSearched);
    }
    private static void writeResult(String message) throws Exception {
        Map<String,Object> result=new LinkedHashMap<>();result.put("result",message);result.put("captures",CAPTURES);
        result.put("firstPersonActuallyFlying",firstPersonPassed);result.put("glErrors",GL_ERRORS);
        result.put("expectedExternalShaders",EXPECT_EXTERNAL_SHADERS);result.put("externalShaderPack",externalPackName);
        result.put("turnSequenceFrames",sequenceFrames);result.put("turnSequenceFramesPerSecond",5);
        Map<String,Object> fpsSummary=new LinkedHashMap<>();
        for(var entry:FPS_SAMPLES.entrySet()) {
            var values=entry.getValue();Map<String,Object> summary=new LinkedHashMap<>();
            summary.put("samples",values.size());summary.put("meanFPS",values.stream().mapToInt(Integer::intValue).average().orElse(0));
            summary.put("minimumFPS",values.stream().mapToInt(Integer::intValue).min().orElse(0));
            summary.put("maximumFPS",values.stream().mapToInt(Integer::intValue).max().orElse(0));fpsSummary.put(entry.getKey(),summary);
        }
        result.put("observedFrameRateSamples",fpsSummary);result.put("performanceScope","Real whole-client reported FPS, 60 FPS cap, isolated world; not a GPU timer or universal benchmark.");
        result.put("fixture","Independent generated world; remote avatar follows fixed 20 Hz path; actual production renderer and shaders. Composite forced only for its explicit captures.");
        Files.writeString(Path.of("meteor-client-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
    private static void finish(String message) {
        if (finished) return;finished=true;
        try {Minecraft.getInstance().options.keyUp.setDown(false);System.clearProperty("wuxingxiantu.meteorBloomDisabled");restoreCompositeRoute();writeResult(message);Files.writeString(Path.of("meteor-client-result.txt"),message);}
        catch (Throwable error) {com.mojang.logging.LogUtils.getLogger().error("METEOR_CAPTURE_RESULT",error);}
        Minecraft.getInstance().stop();
    }
    private MeteorClientCapture(){}
}
