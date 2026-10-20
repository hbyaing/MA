package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.ModEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Isolated input regression. Never sets flying=true to make ordinary entry pass. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class RealmFlightInputCapture {
    private static final long START=System.nanoTime();
    private static final List<Map<String,Object>> ROWS=new ArrayList<>(),CHECKS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final Map<KeyMapping,Boolean> REQUESTED_KEYS=new IdentityHashMap<>();
    private static final Map<String,Vec3> CLIENT_START=new LinkedHashMap<>(),SERVER_START=new LinkedHashMap<>();
    private static final Map<String,Object> INITIAL_OPTIONS=new LinkedHashMap<>();
    private static final Map<String,Object> WATER_SETUP=new LinkedHashMap<>();
    private static final Map<String,Boolean> REPORTED=new LinkedHashMap<>();
    private static CompletableFuture<?> preparation;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> sampledClient,latestServer;
    private static boolean opened,prepared,started,finished;
    private static int tick,capture,waterContactWait;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void input(TickEvent.ClientTickEvent event) {
        if(Boolean.getBoolean("wuxingxiantu.realmFlightDimensionCapture"))return;
        if(finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>360)throw new IllegalStateException("Flight input fixture timed out");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened=true;Files.createDirectories(Path.of("captures"));
                mc.getWindow().setWindowed(1280,720);mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);
                mc.options.framerateLimit().set(60);mc.options.setCameraType(CameraType.FIRST_PERSON);mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("flight-input-verification-"+System.currentTimeMillis(),
                    new LevelSettings("Realm flight input verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(!prepared) {
                prepared=true;mc.setScreen(null);
                INITIAL_OPTIONS.put("sprintKey",mc.options.keySprint.getKey().getValue());INITIAL_OPTIONS.put("crouchKey",mc.options.keyShift.getKey().getValue());
                INITIAL_OPTIONS.put("toggleSprint",mc.options.toggleSprint().get());INITIAL_OPTIONS.put("toggleCrouch",mc.options.toggleCrouch().get());
                mc.options.keySprint.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT));
                mc.options.keyShift.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_CONTROL));KeyMapping.resetMapping();
                mc.options.toggleSprint().set(false);mc.options.toggleCrouch().set(false);
                HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);
                UUID id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    for(int x=-8;x<=640;x++)for(int z=-6;z<=6;z++)level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                    for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=131;y<=138;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                    p.teleportTo(0,131,0);p.setYRot(-90);p.setXRot(0);p.setInvulnerable(true);
                    var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setCultivationStage(3);
                    d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
                    level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);GeneratedMod.sync(p);
                    // The ordinary survival abilities and CultivationTicker grant remain untouched.
                });return;
            }
            if(preparation!=null) {if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(!started) {
                if(!RealmFlightControl.eligible(mc.player)||!mc.player.getAbilities().mayfly||!mc.player.hasEffect(ModEffects.REALM_FLIGHT.get())||!mc.player.onGround())return;
                started=true;check("initial_survival_not_flying",!mc.player.getAbilities().flying,"Flight starts disabled; real permission/buff arrived from server");
            }
            if(serverSample!=null&&serverSample.isDone()) {
                latestServer=serverSample.join();serverSample=null;
                var row=new LinkedHashMap<String,Object>();row.put("client",sampledClient);row.put("server",latestServer);ROWS.add(row);
                int sampleTick=((Number)latestServer.get("fixtureTick")).intValue();
                // The fixture intentionally injects zero on the server in these
                // two setup ticks. Check production recovery again from 681;
                // do not classify the injected pre-physics snapshot as a bug.
                boolean intentionalWaterInjection=sampleTick>=679&&sampleTick<=680;
                if(!intentionalWaterInjection&&Boolean.TRUE.equals(latestServer.get("flying"))&&((Number)latestServer.get("flyingSpeed")).doubleValue()<=0&&!REPORTED.containsKey("server_positive_speed")) {
                    REPORTED.put("server_positive_speed",true);check("server_positive_speed",false,"Server published flyingSpeed="+latestServer.get("flyingSpeed")+" while flying");
                }
            }
            tick++;mc.player.setYRot(-90);mc.player.setXRot(0);
            if(Boolean.getBoolean("wuxingxiantu.flightFlowCapture")&&tick==86){
                check("first_person_flow",FirstPersonFlightFlow.vertices==16&&FirstPersonFlightFlow.strength>.1,
                    "Submitted vertices="+FirstPersonFlightFlow.vertices+", strength="+FirstPersonFlightFlow.strength);
                check("first_person_clear_center",cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects.renderedTrailSegments==0,
                    "Local world-wake segments="+cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects.renderedTrailSegments);
                finish();return;
            }
            // Wait for the teleport/chunk-fluid update before the new water input phase.
            if(tick==679&&(Math.abs(mc.player.getX()-500)>5||!mc.player.isInWater()||latestServer==null||!Boolean.TRUE.equals(latestServer.get("isInWater"))||Boolean.TRUE.equals(latestServer.get("eligible")))){
                if(++waterContactWait>80)throw new IllegalStateException("Water fixture failed actual client/server contact readiness: client="+snapshot(mc.player,true)+", server="+latestServer);
                tick--;return;
            }
            boolean jump=pulse(20,24,90,94,100,104,120,124,130,134,330,334,450,454,540,544)||tick>=60&&tick<=75||tick>=680&&tick<=710;
            boolean forward=tick>=40&&tick<=85||tick>=108&&tick<=115||tick>=140&&tick<=250||tick>=260&&tick<=279||tick>=340&&tick<=380||tick>=460&&tick<=500||tick>=550&&tick<=580||tick>=680&&tick<=710;
            boolean crouch=tick>=280&&tick<=315||tick>=385&&tick<=425||tick>=505&&tick<=530||tick==590||tick==620;
            boolean sprint=tick>=160&&tick<=174||tick>=475&&tick<=494||tick==555||tick==575;
            key(mc.options.keyJump,jump);key(mc.options.keyUp,forward);key(mc.options.keyShift,crouch);key(mc.options.keySprint,sprint);
            key(mc.options.keyDown,false);key(mc.options.keyLeft,false);key(mc.options.keyRight,false);
            if(tick==40)start("realm8_special_ground");
            if(tick==85){movement("realm8_special_ground",8);flight("realm8_special_ground");capture=1;}
            if(tick==98)check("airborne_exit",!mc.player.getAbilities().flying&&!mc.player.onGround(),"Real double-space exit while airborne");
            if(tick==108){flight("airborne_reentry");check("airborne_reentry_above_ground",!mc.player.onGround(),"Reentered with real jump presses before landing");}
            if(tick==116){HudConfig.REALM_FLIGHT_ANIMATION.set(false);RealmFlightVisuals.sendMode();}
            if(tick==140)start("realm8_vanilla_preference");
            if(tick==180){movement("realm8_vanilla_preference",8);flight("realm8_vanilla_preference");capture=2;}
            if(tick==190)mc.setScreen(new InventoryScreen(mc.player));
            if(tick==203)start("inventory_stationary");
            if(tick==214){stationary("inventory_stationary",1.5);check("inventory_does_not_cancel_flight",mc.player.getAbilities().flying,"W held while InventoryScreen suppresses flight input");capture=3;}
            if(tick==215)mc.setScreen(null);
            if(tick==225)start("inventory_closed_resume");
            if(tick==250){movement("inventory_closed_resume",8);flight("inventory_closed_resume");}
            if(tick==260) {
                start("zero_speed_no_state_fallback");injectStoppedZeroSpeed();
            }
            if(tick>=260&&tick<=267)GeneratedMod.data(mc.player).wuxingxiantu$setRealm(2);
            if(tick==267)movement("zero_speed_no_state_fallback",0.2);
            if(tick==268){GeneratedMod.data(mc.player).wuxingxiantu$setRealm(8);start("custom_after_fallback");}
            if(tick==280){movement("custom_after_fallback",2);flight("custom_after_fallback");}
            if(tick==315)landed("realm8_first_landing");
            if(tick==320){HudConfig.REALM_FLIGHT_ANIMATION.set(true);RealmFlightVisuals.sendMode();}
            if(tick==340)start("realm8_ground_reentry");
            if(tick==370) {
                start("non_periodic_zero_speed_fallback");injectStoppedZeroSpeed();
                check("fallback_outside_40_tick_sync",latestServer!=null&&((Number)latestServer.get("entityTick")).intValue()%40!=0,
                    "Latest actual server entityTick="+(latestServer==null?"unavailable":latestServer.get("entityTick"))+"; zero-speed packet injected between periodic ability sync ticks");
            }
            if(tick>=370&&tick<=377)GeneratedMod.data(mc.player).wuxingxiantu$setRealm(2);
            if(tick==377)movement("non_periodic_zero_speed_fallback",0.2);
            if(tick==378){GeneratedMod.data(mc.player).wuxingxiantu$setRealm(8);start("custom_after_non_periodic_fallback");}
            if(tick==380){movement("custom_after_non_periodic_fallback",0.2);flight("custom_after_non_periodic_fallback");}
            if(tick==380){movement("realm8_ground_reentry",8);flight("realm8_ground_reentry");capture=4;}
            if(tick==425)landed("realm8_second_landing");
            if(tick==430) {
                UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setCultivationStage(3);GeneratedMod.sync(p);
                });
            }
            if(tick==448)check("realm3_real_permission",GeneratedMod.data(mc.player).wuxingxiantu$getRealm()==3&&mc.player.getAbilities().mayfly,"Realm3 synchronized and normal server flight grant remains active");
            if(tick==460)start("realm3_ground_entry_remapped_boost");
            if(tick==485)check("remapped_shift_boost",mc.options.keySprint.isDown(),"Sprint is remapped to left Shift, held logical key is down");
            if(tick==500){movement("realm3_ground_entry_remapped_boost",6);flight("realm3_ground_entry_remapped_boost");capture=5;}
            if(tick==530)landed("realm3_remapped_control_landing");
            if(tick==535){mc.options.toggleSprint().set(true);mc.options.toggleCrouch().set(true);}
            if(tick==550)start("realm3_toggle_key_settings");
            if(tick==565)check("toggle_sprint_release_keeps_toggle",mc.options.keySprint.isDown(),"Single remapped Shift press remains toggled after physical release");
            if(tick==580){movement("realm3_toggle_key_settings",6);flight("realm3_toggle_key_settings");}
            if(tick==605)check("toggle_crouch_release_keeps_toggle",mc.options.keyShift.isDown(),"Single remapped Control press remains toggled after release");
            if(tick==625)landed("realm3_toggle_crouch_landing");
            if(tick==660) {
                RealmFlightControl.release(mc.player);mc.player.setDeltaMovement(Vec3.ZERO);
                UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    // Match feet partly submerged in a source-water top layer, with air above.
                    for(int x=498;x<=532;x++)for(int z=-2;z<=2;z++)for(int y=131;y<=145;y++)
                        level.setBlock(new BlockPos(x,y,z),(y<=135?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);
                    var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setActiveDivineArt("");
                    p.teleportTo(500,135.5575,0);
                    try {
                        var update=net.minecraft.world.entity.Entity.class.getDeclaredMethod("updateInWaterStateAndDoFluidPushing");
                        update.setAccessible(true);update.invoke(p);
                    }catch(ReflectiveOperationException error){throw new IllegalStateException("Could not initialize actual server water contact",error);}
                    RealmFlightControl.release(p);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.getAbilities().setFlyingSpeed(.05F);p.setDeltaMovement(Vec3.ZERO);
                    WATER_SETUP.put("serverPreparedState",snapshot(p,false));p.onUpdateAbilities();GeneratedMod.sync(p);
                });
            }
            if(tick==679) {
                UUID id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    if(!p.isInWater()||RealmFlightControl.eligible(p))throw new IllegalStateException("Water injection requires actual in-water, ineligible server player");
                    RealmFlightControl.release(p);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.getAbilities().setFlyingSpeed(0);p.setDeltaMovement(Vec3.ZERO);
                    WATER_SETUP.put("serverInjectedState",snapshot(p,false));p.onUpdateAbilities();
                });
            }
            if(tick==680) {
                start("water_old_save_input");injectStoppedZeroSpeed();
                WATER_SETUP.put("clientInjectedState",snapshot(mc.player,true));
                var serverInjected=(Map<?,?>)WATER_SETUP.get("serverInjectedState");
                check("water_old_save_initial_contact",mc.player.isInWater()&&!RealmFlightControl.eligible(mc.player)
                    &&Boolean.TRUE.equals(serverInjected.get("isInWater"))&&Boolean.FALSE.equals(serverInjected.get("eligible")),
                    "Actual source-water contact="+mc.player.isInWater()+", feetY="+mc.player.getY()+", fluidHeight="+mc.player.getFluidHeight(net.minecraft.tags.FluidTags.WATER));
                check("water_old_save_zero_speed_no_state",mc.player.getAbilities().mayfly&&mc.player.getAbilities().flying&&mc.player.getAbilities().getFlyingSpeed()==0
                    &&!Boolean.TRUE.equals(controller(mc.player,true).get("present"))&&mc.player.getDeltaMovement().lengthSqr()==0,
                    "Client injected mayfly/flying=true, zero acceleration and motion, with cleared controller state before physics");
            }
            if(tick==711) {
                movement("water_old_save_input",0.2);verticalMovement("water_old_save_input",0.2);capture=6;
                check("water_old_save_final_positive_speed",mc.player.getAbilities().getFlyingSpeed()>0&&latestServer!=null&&((Number)latestServer.get("flyingSpeed")).doubleValue()>0,
                    "After W + one held jump, client/server ability speed must recover; final contact="+mc.player.isInWater());
            }
            if(tick==740)finish();
        }catch(Throwable error){FAILURES.add("Fixture exception: "+error);com.mojang.logging.LogUtils.getLogger().error("REALM_FLIGHT_INPUT_CAPTURE",error);finish();}
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void sample(TickEvent.ClientTickEvent event) {
        if(finished||!started||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            if(tick>=260&&tick<=267&&!REPORTED.containsKey("fallback_positive_speed")&&mc.player.getAbilities().getFlyingSpeed()<=0) {
                REPORTED.put("fallback_positive_speed",true);check("fallback_positive_speed",false,"After zero-speed packet + cleared controller state + client realm2, release left flyingSpeed=0");
            }
            if(tick==267&&!REPORTED.containsKey("fallback_positive_speed"))check("fallback_positive_speed",true,"Fallback restored positive ability speed without controller state");
            if(tick>=370&&tick<=377&&!REPORTED.containsKey("non_periodic_fallback_positive_speed")&&mc.player.getAbilities().getFlyingSpeed()<=0) {
                REPORTED.put("non_periodic_fallback_positive_speed",true);check("non_periodic_fallback_positive_speed",false,"Between periodic sync ticks, no-state fallback retained zero ability speed");
            }
            if(tick==377&&!REPORTED.containsKey("non_periodic_fallback_positive_speed"))check("non_periodic_fallback_positive_speed",true,"Between periodic sync ticks, fallback restored positive ability speed");
            if(serverSample==null) {
                sampledClient=snapshot(mc.player,true);UUID id=mc.player.getUUID();
                serverSample=mc.getSingleplayerServer().submit(()->snapshot(mc.getSingleplayerServer().getPlayerList().getPlayer(id),false));
            }
        }catch(Throwable error){FAILURES.add("Telemetry exception: "+error);finish();}
    }
    private static boolean pulse(int... ticks){for(int value:ticks)if(tick==value)return true;return false;}
    private static void injectStoppedZeroSpeed(){
        var mc=Minecraft.getInstance();RealmFlightControl.release(mc.player);
        var zero=new Abilities();zero.mayfly=true;zero.flying=true;zero.setFlyingSpeed(0);zero.setWalkingSpeed(mc.player.getAbilities().getWalkingSpeed());
        mc.getConnection().handlePlayerAbilities(new ClientboundPlayerAbilitiesPacket(zero));
        mc.player.setDeltaMovement(Vec3.ZERO); // Stopped player: subsequent displacement must come from fresh W input.
    }
    private static void key(KeyMapping key,boolean pressed){
        // Screens call releaseAll; refresh ordinary held keys after that reset, while
        // toggle mappings must receive only press transitions or they toggle each tick.
        if(!(key instanceof net.minecraft.client.ToggleKeyMapping)||REQUESTED_KEYS.getOrDefault(key,false)!=pressed)key.setDown(pressed);
        REQUESTED_KEYS.put(key,pressed);
    }
    private static Vec3 serverPosition(){if(latestServer==null)return Vec3.ZERO;return new Vec3(((Number)latestServer.get("x")).doubleValue(),((Number)latestServer.get("y")).doubleValue(),((Number)latestServer.get("z")).doubleValue());}
    private static void start(String name){CLIENT_START.put(name,Minecraft.getInstance().player.position());SERVER_START.put(name,serverPosition());}
    private static double horizontal(Vec3 a,Vec3 b){return a.subtract(b).horizontalDistance();}
    private static void movement(String name,double minimum) {
        Vec3 client=Minecraft.getInstance().player.position(),server=serverPosition();
        double dc=horizontal(client,CLIENT_START.get(name)),ds=horizontal(server,SERVER_START.get(name));
        check(name+"_actual_displacement",dc>=minimum&&ds>=minimum,"client horizontal="+dc+", server horizontal="+ds+", minimum="+minimum);
        check(name+"_position_agreement",horizontal(client,server)<6,"Client/server latest-sampled position discrepancy="+horizontal(client,server)+"; asynchronous sample is usually one tick old");
    }
    private static void stationary(String name,double maximum){double moved=horizontal(Minecraft.getInstance().player.position(),CLIENT_START.get(name));check(name,moved<=maximum,"Horizontal displacement while menu open="+moved);}
    private static void verticalMovement(String name,double minimum){
        double dc=Minecraft.getInstance().player.getY()-CLIENT_START.get(name).y,ds=serverPosition().y-SERVER_START.get(name).y;
        check(name+"_vertical_displacement",dc>=minimum&&ds>=minimum,"client vertical="+dc+", server vertical="+ds+", minimum="+minimum);
    }
    private static void flight(String name){var p=Minecraft.getInstance().player;check(name+"_abilities",p.getAbilities().mayfly&&p.getAbilities().flying&&latestServer!=null&&Boolean.TRUE.equals(latestServer.get("mayfly"))&&Boolean.TRUE.equals(latestServer.get("flying")),"Actual client/server mayfly/flying are enabled");}
    private static void landed(String name){var p=Minecraft.getInstance().player;check(name,p.onGround()&&!p.getAbilities().flying,"Actual onGround="+p.onGround()+", flying="+p.getAbilities().flying+", y="+p.getY());}
    private static void check(String name,boolean pass,String detail){var row=new LinkedHashMap<String,Object>();row.put("tick",tick);row.put("name",name);row.put("pass",pass);row.put("detail",detail);CHECKS.add(row);if(!pass)FAILURES.add(name+": "+detail);com.mojang.logging.LogUtils.getLogger().info("FLIGHT_INPUT_CHECK {}",row);}
    private static Map<String,Object> snapshot(Player player,boolean client) {
        var row=new LinkedHashMap<String,Object>();row.put("fixtureTick",tick);row.put("gameTime",player.level().getGameTime());row.put("entityTick",player.tickCount);
        row.put("x",player.getX());row.put("y",player.getY());row.put("z",player.getZ());row.put("delta",player.getDeltaMovement().toString());row.put("onGround",player.onGround());row.put("isInWater",player.isInWater());row.put("waterHeight",player.getFluidHeight(net.minecraft.tags.FluidTags.WATER));
        row.put("mayfly",player.getAbilities().mayfly);row.put("flying",player.getAbilities().flying);row.put("flyingSpeed",player.getAbilities().getFlyingSpeed());
        row.put("realm",GeneratedMod.data(player).wuxingxiantu$getRealm());row.put("mana",GeneratedMod.data(player).wuxingxiantu$getAura());
        row.put("eligible",RealmFlightControl.eligible(player));row.put("controller",controller(player,client));
        if(client){var mc=Minecraft.getInstance();row.put("specialPreference",HudConfig.REALM_FLIGHT_ANIMATION.get());row.put("screen",mc.screen==null?"none":mc.screen.getClass().getSimpleName());row.put("keyJumpDown",mc.options.keyJump.isDown());row.put("keyForwardDown",mc.options.keyUp.isDown());row.put("keySprintDown",mc.options.keySprint.isDown());row.put("keyCrouchDown",mc.options.keyShift.isDown());row.put("sprintKey",mc.options.keySprint.getKey().getValue());row.put("crouchKey",mc.options.keyShift.getKey().getValue());row.put("toggleSprint",mc.options.toggleSprint().get());row.put("toggleCrouch",mc.options.toggleCrouch().get());}
        return row;
    }
    private static Map<String,Object> controller(Player player,boolean client) {
        var row=new LinkedHashMap<String,Object>();
        try {
            Field field;try{field=RealmFlightControl.class.getDeclaredField(client?"CLIENT_STATES":"SERVER_STATES");}catch(NoSuchFieldException old){field=RealmFlightControl.class.getDeclaredField("STATES");}
            field.setAccessible(true);var states=(Map<?,?>)field.get(null);Object state=states.get(player);row.put("map",field.getName());row.put("present",state!=null);
            if(state!=null)for(String name:new String[]{"input","velocity","received","boost","boostWeight"}){try{Field value=state.getClass().getDeclaredField(name);value.setAccessible(true);row.put(name,String.valueOf(value.get(state)));}catch(ReflectiveOperationException ignored){}}
        }catch(ReflectiveOperationException error){row.put("diagnosticUnavailable",error.toString());}
        return row;
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event){if(finished||event.phase!=TickEvent.Phase.END||capture==0)return;try(var image=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){image.writeToFile(Path.of("captures",String.format(java.util.Locale.ROOT,"input-%02d.png",capture)));capture=0;}catch(Throwable error){FAILURES.add("Screenshot: "+error);finish();}}
    private static void finish() {
        if(finished)return;finished=true;
        try {
            var mc=Minecraft.getInstance();mc.options.toggleSprint().set(false);mc.options.toggleCrouch().set(false);
            for(KeyMapping key:REQUESTED_KEYS.keySet())key.setDown(false);
            var result=new LinkedHashMap<String,Object>();result.put("result",FAILURES.isEmpty()?"PASS":"FAIL");result.put("checks",CHECKS);result.put("failures",FAILURES);result.put("telemetry",ROWS);result.put("initialOptions",INITIAL_OPTIONS);result.put("waterFixture",WATER_SETUP);
            result.put("fixture","Independent generated survival world. Ordinary double-space flight entry and W movement use client KeyMapping input and production controller; flight is never pre-enabled for these paths. Two explicit zero-speed ClientboundPlayerAbilitiesPacket injections plus cleared state/client realm2 model the reported fallback failure. Injection clears previous momentum to test new W movement; second injection records an actual server tick outside the 40-tick sync boundary.");
            result.put("inputScope","Logical KeyMapping press/release states on the isolated client; no native physical keyboard events or GLFW modifier-state certification. Left Shift sprint and left Control crouch are remapped and both hold/toggle settings are exercised.");
            result.put("movementScope","Actual client/server position changes and flight flags, not renderer submission counters. Server snapshots run on the integrated-server thread and can trail client samples by a tick.");
            result.put("waterScope","After the original checks, restore a realm8 idle-flying save condition with no controller state, zero speed/motion and feet135.5575 partly submerged in source water at y135 with air above. W + one continuous jump hold at ticks680..710 tests observed horizontal/up movement; initial water contact is required, later exit from water is allowed and recorded.");
            Files.writeString(Path.of("flight-input-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));Files.writeString(Path.of("flight-input-result.txt"),(FAILURES.isEmpty()?"PASS":"FAIL")+": "+CHECKS.size()+" checks; "+ROWS.size()+" client/server telemetry pairs; "+FAILURES);
        }catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("FLIGHT_INPUT_RESULT",error);}
        Minecraft.getInstance().stop();
    }
    private RealmFlightInputCapture(){}
}
