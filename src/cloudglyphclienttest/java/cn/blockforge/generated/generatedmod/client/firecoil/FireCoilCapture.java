package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.ExoticFireOrbitRenderer;
import cn.blockforge.generated.generatedmod.fire.WeaponFlameState;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.*;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Native-renderer acceptance fixture; never opens a pre-existing player save. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FireCoilCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.firecoilCapture");
    private static final boolean FIXED_ONLY=Boolean.getBoolean("wuxingxiantu.firecoilFixedOnly");
    private static final boolean SETTINGS_ONLY=Boolean.getBoolean("wuxingxiantu.firecoilSettingsOnly");
    private static final boolean GPU_ONLY=Boolean.getBoolean("wuxingxiantu.firecoilGpuOnly");
    private static final boolean BLADE_ONLY=Boolean.getBoolean("wuxingxiantu.firecoilBladeOnly");
    private static final Path OUT=Path.of(System.getProperty("wuxingxiantu.firecoilOutput",
            "E:/五行仙途/deliverables/task-20261015-firecoil-install/"+(GPU_ONLY?"client-gpu-only":"client")));
    private static final long START=System.nanoTime();
    private static final Vec3 ORIGIN=new Vec3(.5,129,.5);
    private static final String[] SWORDS={"dark_iron_sword","mingjue_sword","qingming_sword",
            "jixiao_sword","wuxiang_sword","guiyuan_sword","tianque_sword"};
    private static final List<Scene> SCENES=scenes();
    private static final List<Map<String,Object>> SHOTS=new ArrayList<>(),GPU=new ArrayList<>();
    private static final List<RemotePlayer> REMOTES=new ArrayList<>();
    private static final List<Double> FRAME_MS=new ArrayList<>(),BODY_MS=new ArrayList<>(),SUPPORT_BUILD_CPU_MS=new ArrayList<>();
    private static boolean opened,prepared,done;
    private static boolean configCaptured,originalWrapVisual,originalFirstPersonVisual,originalFirstPersonOptimization,originalThirdPersonOptimization,originalCoverageBudget;
    private static int sceneIndex=-1,sceneTick,weaponBefore,reloadGeneration,reloadVolumeGeneration,sceneVolumeGeneration;
    private static CompletableFuture<?> preparation,reload;
    private static Scene scene;
    private static Map<String,Object> fixedProbe;
    private static String pending;
    private static Vec3 previousPosition=ORIGIN;
    private static int[] frameQueries;
    private static boolean bodyQueryBegan,bodyQueryEnded;
    private static int frameScene;
    private static double movingDistance;

    // Modes: 0 ordinary, 1 moving, 2 client population, 3 preference off,
    // 4 remove armor, 5 unequip fire, 6 reload, 7 unenchanted, 8 wrong owner, 9 weapon only,
    // 12 native precision, 13 first-person default off, 14 explicit on,
    // 15 third person with the first-person preference off, 16 off again, 17 reload while off.
    private record Scene(String name,int tier,int level,CameraType camera,String sword,
                         boolean offhand,int mode,boolean gpu) {}
    private static List<Scene> scenes() {
        if(SETTINGS_ONLY)return List.of();
        List<Scene> result=new ArrayList<>();
        result.add(new Scene("first-person-default-hidden",5,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,13,false));
        result.add(new Scene("first-person-opt-in-visible",5,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,14,false));
        result.add(new Scene("third-person-with-first-person-off",5,5,CameraType.THIRD_PERSON_FRONT,"guiyuan_sword",false,15,false));
        result.add(new Scene("first-person-hidden-again",5,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,16,false));
        result.add(new Scene("first-person-reload-hidden",5,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,17,false));
        for(int tier=1;tier<=9;tier++)for(int level:new int[]{1,5})
            result.add(new Scene("armor-fire-"+tier+"-level-"+level,tier,level,CameraType.THIRD_PERSON_FRONT,"",false,0,tier==5&&level==5));
        for(int level=2;level<=4;level++)result.add(new Scene("armor-blue-level-"+level,5,level,CameraType.THIRD_PERSON_FRONT,"",false,0,false));
        result.add(new Scene("armor-first-person",5,5,CameraType.FIRST_PERSON,"",false,0,true));
        result.add(new Scene("first-person-native-quality",5,5,CameraType.FIRST_PERSON,"",false,12,false));
        for(String sword:SWORDS)result.add(new Scene("weapon-first-"+sword,5,3,CameraType.FIRST_PERSON,sword,false,0,false));
        result.add(new Scene("weapon-gold-level-V",2,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,0,true));
        result.add(new Scene("weapon-third-person",5,5,CameraType.THIRD_PERSON_FRONT,"tianque_sword",false,0,false));
        result.add(new Scene("weapon-offhand",8,5,CameraType.FIRST_PERSON,"mingjue_sword",true,0,false));
        result.add(new Scene("weapon-unenchanted",5,5,CameraType.FIRST_PERSON,"dark_iron_sword",false,7,false));
        result.add(new Scene("weapon-wrong-owner",5,5,CameraType.FIRST_PERSON,"dark_iron_sword",false,8,false));
        result.add(new Scene("moving-first-person-bob",5,5,CameraType.FIRST_PERSON,"",false,1,false));
        result.add(new Scene("moving-turn",5,5,CameraType.THIRD_PERSON_BACK,"",false,1,false));
        result.add(new Scene("nearby-player-cap-and-distance",5,5,CameraType.THIRD_PERSON_FRONT,"",false,2,true));
        result.add(new Scene("visual-preference-off",5,5,CameraType.FIRST_PERSON,"dark_iron_sword",false,3,false));
        result.add(new Scene("armor-removed",5,5,CameraType.THIRD_PERSON_FRONT,"",false,4,false));
        result.add(new Scene("fire-unequipped",0,5,CameraType.THIRD_PERSON_FRONT,"",false,5,false));
        result.add(new Scene("resource-reload",5,5,CameraType.THIRD_PERSON_FRONT,"guiyuan_sword",false,6,false));
        result.add(new Scene("weapon-without-armor",2,5,CameraType.FIRST_PERSON,"guiyuan_sword",false,9,false));
        for(int level:new int[]{1,3,5})result.add(new Scene("jixiao-refined-level-"+level,5,level,CameraType.FIRST_PERSON,"jixiao_sword",false,9,false));
        result.add(new Scene("jixiao-refined-nirvana",9,5,CameraType.FIRST_PERSON,"jixiao_sword",false,9,false));
        result.add(new Scene("jixiao-refined-violet",6,5,CameraType.FIRST_PERSON,"jixiao_sword",false,9,false));
        result.add(new Scene("nirvana-body-and-sword-phase-a",9,5,CameraType.THIRD_PERSON_FRONT,"guiyuan_sword",false,0,false));
        result.add(new Scene("nirvana-body-and-sword-phase-b",9,5,CameraType.THIRD_PERSON_FRONT,"guiyuan_sword",false,0,false));
        result.add(new Scene("body-attached-horizontal-flight",5,5,CameraType.THIRD_PERSON_FRONT,"",false,10,false));
        result.add(new Scene("third-person-close-low-angle-budget",3,5,CameraType.THIRD_PERSON_BACK,"",false,18,true));
        result.add(new Scene("third-person-close-low-angle-native",3,5,CameraType.THIRD_PERSON_BACK,"",false,19,true));
        if(FIXED_ONLY)return List.of(new Scene("fixed-probe-setup",5,5,CameraType.THIRD_PERSON_FRONT,"",false,0,false));
        if(BLADE_ONLY)return result.stream().filter(s->s.name.startsWith("weapon-")||s.name.startsWith("jixiao-")).toList();
        return GPU_ONLY?result.stream().filter(Scene::gpu).toList():List.copyOf(result);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event) {
        if(!ENABLED||done||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUT);
            if(System.nanoTime()-START>900_000_000_000L)throw new AssertionError("capture watchdog");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                originalWrapVisual=HudConfig.EXOTIC_FIRE_WRAP_VISUAL.get();
                originalFirstPersonVisual=HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.get();
                originalFirstPersonOptimization=HudConfig.EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION.get();
                originalThirdPersonOptimization=HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get();
                originalCoverageBudget=VolumeGpu.coverageBudgetEnabled;
                configCaptured=true;
                if(HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.getDefault())throw new AssertionError("First-person fire visuals must default to false");
                opened=true;mc.getWindow().setWindowed(1280,720);mc.resizeDisplay();
                mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(60);
                mc.options.graphicsMode().set(GraphicsStatus.FANCY);mc.options.cloudStatus().set(CloudStatus.OFF);mc.options.bobView().set(false);mc.options.fov().set(70);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                if(!Boolean.getBoolean("wuxingxiantu.guardGlintShowWindow"))org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("firecoil-install-qa-"+System.currentTimeMillis(),
                        new LevelSettings("Firecoil native QA",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(20261015,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
                return;
            }
            if(Boolean.getBoolean("wuxingxiantu.heavenlySmallWorld")&&opened&&mc.player!=null&&mc.level!=null){
                if(mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)mc.setScreen(null);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            // Hidden native windows can be resized to 1x1 by the Windows compositor.
            int qaWidth=closeCamera(scene)?1920:1280,qaHeight=closeCamera(scene)?1440:720;
            if(mc.getMainRenderTarget().width!=qaWidth||mc.getMainRenderTarget().height!=qaHeight) {
                mc.getWindow().setWidth(qaWidth);mc.getWindow().setHeight(qaHeight);mc.resizeDisplay();
            }
            // Vanilla suppresses first-person hands together with F1's hideGui.
            mc.setScreen(null);mc.options.hideGui=scene==null||scene.sword.isEmpty();
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(!prepared) {
                String swordName=net.minecraft.network.chat.Component.translatable("item.wuxingxiantu.jixiao_sword").getString();
                if(!swordName.equals("霁霄剑")&&!swordName.equals("Jixiao Sword"))throw new AssertionError("Stale sword translation: "+swordName);
                prepared=true;var id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    p.serverLevel().setDayTime(6000);p.serverLevel().setWeatherParameters(6000,0,false,false);
                    for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)p.serverLevel().setBlock(new BlockPos(x,128,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                    p.teleportTo(.5,129,.5);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();
                    var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(8);
                    for(int tier=1;tier<=9;tier++)data.wuxingxiantu$learnFire(tier);
                    GeneratedMod.sync(p);
                    try{SettingsAndInfuserChecks.infuser(p,OUT);}catch(Exception ex){throw new RuntimeException(ex);}
                });return;
            }
            if(SETTINGS_ONLY){
                if(Boolean.getBoolean("wuxingxiantu.anvilReviewQa")){if(AnvilReviewChecks.advance(OUT))finish(null);return;}
                if(Boolean.getBoolean("wuxingxiantu.sidebarGoldQa")){SidebarGoldChecks.run(OUT);finish(null);return;}
                if(Boolean.getBoolean("wuxingxiantu.professionsQa")){if(PlayerProgressionChecks.advance(OUT))finish(null);return;}
                if(Boolean.getBoolean("wuxingxiantu.heavenlyFurnaceQa")){if(HeavenlyFurnaceChecks.advance(OUT))finish(null);return;}
                if(Boolean.getBoolean("wuxingxiantu.talismanUi"))TalismanUiChecks.run(OUT);
                else SettingsAndInfuserChecks.settings(OUT);
                finish(null);return;
            }
            if(scene==null) {
                if(++sceneIndex==SCENES.size()) {
                    if(SHOTS.size()!=SCENES.size()||GPU.size()!=SCENES.stream().filter(Scene::gpu).count()||(!GPU_ONLY&&!FIXED_ONLY&&!BLADE_ONLY&&movingDistance<2))throw new AssertionError("incomplete acceptance: "+SHOTS.size()+" shots, "+GPU.size()+" GPU scenes");
                    fixedProbe=FireCoilFixedProbe.run(OUT);SettingsAndInfuserChecks.settings(OUT);finish(null);return;
                }
                begin(SCENES.get(sceneIndex));return;
            }
            if(pending!=null)return;
            if(scene.sword.isEmpty()&&(!mc.player.getMainHandItem().isEmpty()||!mc.player.getOffhandItem().isEmpty())){weaponBefore=Client.weaponDraws;return;}
            sceneTick++;
            if(sceneTick==8)weaponBefore=Client.weaponDraws; // Exclude asynchronous inventory transition frames.
            mc.options.setCameraType(scene.camera);mc.player.noPhysics=true;mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
            Vec3 next=ORIGIN;float yaw=0;
            if(scene.mode==1) {
                mc.options.bobView().set(true);mc.player.walkDistO=sceneTick*.15F;mc.player.walkDist=mc.player.walkDistO+.15F;mc.player.oBob=mc.player.bob=.05F;
                double angle=sceneTick*.065;next=ORIGIN.add(Math.sin(angle)*1.5,0,1.5-Math.cos(angle)*1.5);yaw=(float)(-angle*180/Math.PI);
                if(sceneTick>1)movingDistance+=next.distanceTo(previousPosition);
            }
            mc.player.setPos(next);mc.player.xo=previousPosition.x;mc.player.yo=previousPosition.y;mc.player.zo=previousPosition.z;
            mc.player.xOld=previousPosition.x;mc.player.yOld=previousPosition.y;mc.player.zOld=previousPosition.z;
            mc.player.setDeltaMovement(next.subtract(previousPosition));mc.player.setYRot(yaw);mc.player.yRotO=yaw;
            mc.player.setXRot(scene.camera.isFirstPerson()?8:0);mc.player.xRotO=mc.player.getXRot();mc.player.yBodyRot=mc.player.yBodyRotO=yaw;
            previousPosition=next;
            if(scene.mode==1){var at=next;var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);if(p!=null)p.setPos(at);});}
            if(scene.mode==2)updateRemotes();
            if(scene.mode==10)updateFlyingRemote();
            if(scene.mode==8)mc.player.getMainHandItem().getOrCreateTag().putUUID("WeaponExoticFireOwner",new UUID(0,1234567));
            if(reloads(scene)) {
                if(sceneTick==4){reloadGeneration=FireRender.generation;reloadVolumeGeneration=VolumeGpu.generation;reload=mc.reloadResourcePacks();return;}
                if(reload!=null){if(!reload.isDone())return;reload.join();reload=null;}
            }
            // Slower GPUs still need enough actual rendered samples, not fabricated repeats.
            int settle=scene.mode==10?64:scene.mode==1?36:28;
            if(scene.gpu&&FRAME_MS.size()<5&&sceneTick<100)return;
            if(sceneTick>=settle)pending=scene.name+".png";
        } catch(Throwable error){finish(error.toString());}
    }

    private static void begin(Scene next) {
        var mc=Minecraft.getInstance();
        for(var p:REMOTES){ExoticFireOrbitRenderer.update(p.getId(),0,0);mc.level.removeEntity(p.getId(),Entity.RemovalReason.DISCARDED);}REMOTES.clear();
        scene=next;sceneTick=0;FRAME_MS.clear();BODY_MS.clear();SUPPORT_BUILD_CPU_MS.clear();previousPosition=ORIGIN;weaponBefore=Client.weaponDraws;sceneVolumeGeneration=VolumeGpu.generation;
        HudConfig.EXOTIC_FIRE_WRAP_VISUAL.set(next.mode!=3);mc.options.setCameraType(next.camera);
        // Every pre-existing positive scene explicitly opts in; these changes are restored at finish.
        HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.set(!firstPersonPreferenceOff(next));
        HudConfig.EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION.set(next.mode!=12);
        HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.set(true);
        VolumeGpu.coverageBudgetEnabled=next.mode!=19;
        var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.teleportTo(.5,129,.5);p.setYRot(0);p.setXRot(next.camera.isFirstPerson()?8:0);
            var data=GeneratedMod.data(p);data.wuxingxiantu$setEquippedFire(next.tier);
            var chest=new ItemStack(Items.NETHERITE_CHESTPLATE);if(next.mode!=4&&next.mode!=9)chest.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),next.level);
            p.setItemSlot(EquipmentSlot.CHEST,next.mode==4?ItemStack.EMPTY:chest);p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.NETHERITE_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.NETHERITE_BOOTS));
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            if(!next.sword.isEmpty()) {
                var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,next.sword));if(item==null||item==Items.AIR)throw new AssertionError("Missing sword "+next.sword);
                var sword=new ItemStack(item);if(next.mode!=7)sword.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),next.level);
                p.setItemInHand(next.offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,sword);
            }
            WeaponFlameState.refresh(p);SpiritEnchantmentLogic.broadcastWrapVisual(p,data);p.inventoryMenu.broadcastChanges();GeneratedMod.sync(p);
        });
        if(next.mode==2) {
            for(int i=0;i<5;i++) {
                var remote=new RemotePlayer(mc.level,new GameProfile(new UUID(20261015,i+1),"FirecoilQA"+i));
                remote.setNoGravity(true);remote.setInvulnerable(true);remote.noPhysics=true;remote.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));
                mc.level.addPlayer(-56790-i,remote);REMOTES.add(remote);
            }updateRemotes();
        }
        if(next.mode==10){
            var remote=new RemotePlayer(mc.level,new GameProfile(new UUID(20261016,10),"AttachedFlightQA"));remote.setNoGravity(true);remote.noPhysics=true;remote.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));
            mc.level.addPlayer(-56795,remote);REMOTES.add(remote);updateFlyingRemote();
        }
        com.mojang.logging.LogUtils.getLogger().info("Firecoil QA scene {}/{}: {}",sceneIndex+1,SCENES.size(),next.name);
    }
    private static boolean firstPersonPreferenceOff(Scene value){return value.mode==13||value.mode==15||value.mode==16||value.mode==17;}
    private static boolean closeCamera(Scene value){return value!=null&&(value.mode==18||value.mode==19);}
    /** Match the reported rear camera pushed down beside the torso, without replacing the rendered player. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void closeCamera(ViewportEvent.ComputeCameraAngles event){
        if(!ENABLED||done||!closeCamera(scene)||Minecraft.getInstance().player==null)return;
        try{
            var p=Minecraft.getInstance().player;
            var move=Camera.class.getDeclaredMethod("setPosition",double.class,double.class,double.class);move.setAccessible(true);
            move.invoke(event.getCamera(),p.getX()+.12,p.getY()+.22,p.getZ()-.65);
            event.setYaw(0);event.setPitch(-63);event.setRoll(0);
        }catch(Throwable failure){finish("close camera fixture: "+failure);}
    }
    private static boolean reloads(Scene value){return value.mode==6||value.mode==17;}
    private static boolean cameraHidden(Scene value){return value.camera.isFirstPerson()&&firstPersonPreferenceOff(value);}
    /** Use the actual legacy orbit generation so a default-off test cannot silently permit fallback rings. */
    private static boolean legacyParticlesWrapping(int playerId)throws ReflectiveOperationException{
        var field=ExoticFireOrbitRenderer.class.getDeclaredField("ORBITS");field.setAccessible(true);
        var orbit=((Map<?,?>)field.get(null)).get(playerId);if(orbit==null)throw new AssertionError("Missing armor orbit packet for hidden legacy-particle check");
        var generation=orbit.getClass().getDeclaredField("generation");generation.setAccessible(true);
        return ExoticFireOrbitRenderer.isWrapping(playerId,generation.getLong(orbit));
    }
    private static void assertNoVolumeTargets()throws ReflectiveOperationException{
        for(String name:new String[]{"program","vao","depthTexture","depthFbo","lowFbo","lowTexture","composite","supportTexture","supportGridTexture","glowFbo","glowTexture","glowProgram","deferredFbo","deferredTexture","deferredProgram"}){
            var field=VolumeGpu.class.getDeclaredField(name);field.setAccessible(true);
            if(field.getInt(null)!=0)throw new AssertionError(scene.name+": hidden renderer allocated "+name);
        }
    }
    private static void updateFlyingRemote(){
        var p=REMOTES.get(0);var at=ORIGIN.add(2.5,1,0);p.setPos(at);p.xo=p.xOld=at.x;p.yo=p.yOld=at.y;p.zo=p.zOld=at.z-2;
        p.setDeltaMovement(0,0,2);p.setYRot(0);p.yRotO=p.yBodyRot=p.yBodyRotO=0;
        ExoticFireOrbitRenderer.update(p.getId(),5,5);
        cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals.receive(new cn.blockforge.generated.generatedmod.realmflight.RealmFlightVisualPacket(p.getUUID(),true,8,0,false,false));
    }
    private static void updateRemotes() {
        double[][] offsets={{-1.7,0},{1.7,0},{0,-2.2},{3.4,0},{0,-50}};
        for(int i=0;i<REMOTES.size();i++) {
            var remote=REMOTES.get(i);var at=ORIGIN.add(offsets[i][0],0,offsets[i][1]);remote.setPos(at);
            remote.xo=remote.xOld=at.x;remote.yo=remote.yOld=at.y;remote.zo=remote.zOld=at.z;remote.setYRot(0);remote.yRotO=remote.yBodyRot=remote.yBodyRotO=0;
            ExoticFireOrbitRenderer.update(remote.getId(),i+2,5);
        }
    }

    // Timestamp queries bracket real production draws without replaying or changing scale.
    // The body measurement includes other AFTER_PARTICLES handlers; the frame includes world/weapon.
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void frameStart(TickEvent.RenderTickEvent event) {
        if(!ENABLED||done||event.phase!=TickEvent.Phase.START||scene==null||!scene.gpu||sceneTick<12||FRAME_MS.size()>=8||frameQueries!=null||preparation!=null||Minecraft.getInstance().getOverlay()!=null)return;
        try {
            if(!GL.getCapabilities().OpenGL33&&!GL.getCapabilities().GL_ARB_timer_query)throw new AssertionError("GPU timestamp queries unavailable");
            frameQueries=new int[4];for(int i=0;i<4;i++)frameQueries[i]=GL15.glGenQueries();frameScene=sceneIndex;bodyQueryBegan=bodyQueryEnded=false;
            GL33.glQueryCounter(frameQueries[0],GL33.GL_TIMESTAMP);
        }catch(Throwable error){finish(error.toString());}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void bodyStart(RenderLevelStageEvent event) {
        if(!ENABLED||done||frameQueries==null||bodyQueryBegan||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        GL33.glQueryCounter(frameQueries[1],GL33.GL_TIMESTAMP);bodyQueryBegan=true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bodyEnd(RenderLevelStageEvent event) {
        if(!ENABLED||done||frameQueries==null||!bodyQueryBegan||bodyQueryEnded||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        GL33.glQueryCounter(frameQueries[2],GL33.GL_TIMESTAMP);bodyQueryEnded=true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void walkingAlignment(RenderLevelStageEvent event) {
        if(!ENABLED||done||scene==null||scene.mode!=1||scene.camera.isFirstPerson()||sceneTick<12
                ||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||VolumeGpu.program==0)return;
        try {
            var mc=Minecraft.getInstance();var actual=BodyAttachment.snapshot(mc.player.getId());
            if(actual==null)throw new AssertionError("Walking torso was not captured");
            float[] inverse=new float[16],expected=new float[16],rendered=new float[16];
            GL20.glGetUniformfv(VolumeGpu.program,GL20.glGetUniformLocation(VolumeGpu.program,"InverseViewProjection"),inverse);
            new org.joml.Matrix4f(event.getProjectionMatrix()).mul(actual).get(expected);
            new org.joml.Matrix4f().set(inverse).invert().get(rendered);
            for(int i=0;i<16;i++)if(Math.abs(expected[i]-rendered[i])>.002)
                throw new AssertionError("Walking fire/model view mismatch at "+i+": "+expected[i]+" vs "+rendered[i]);

        } catch(Throwable failure){finish(failure.toString());}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frameEnd(TickEvent.RenderTickEvent event) {
        if(!ENABLED||done||event.phase!=TickEvent.Phase.END)return;
        try {
            if(frameQueries!=null) {
                try {
                    GL33.glQueryCounter(frameQueries[3],GL33.GL_TIMESTAMP);
                    long end=GL33.glGetQueryObjecti64(frameQueries[3],GL15.GL_QUERY_RESULT),start=GL33.glGetQueryObjecti64(frameQueries[0],GL15.GL_QUERY_RESULT);
                    if(sceneIndex==frameScene&&bodyQueryBegan&&bodyQueryEnded) {
                        long a=GL33.glGetQueryObjecti64(frameQueries[1],GL15.GL_QUERY_RESULT),b=GL33.glGetQueryObjecti64(frameQueries[2],GL15.GL_QUERY_RESULT);
                        FRAME_MS.add((end-start)/1e6);BODY_MS.add((b-a)/1e6);
                        SUPPORT_BUILD_CPU_MS.add(VolumeGpu.supportCpuMs);
                    }
                }finally{for(int query:frameQueries)GL15.glDeleteQueries(query);frameQueries=null;}
            }
            if(scene!=null&&scene.mode==1&&!scene.camera.isFirstPerson()&&sceneTick>=20&&sceneTick<=36&&sceneTick%4==0)
                try(var screenshot=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())){
                    screenshot.writeToFile(OUT.resolve("walk-alignment-"+sceneTick+".png"));
                }
            if(pending==null)return;
            var mc=Minecraft.getInstance();if(Client.failed||VolumeGpu.failed||FireRender.failed)throw new AssertionError(Client.lastError);
            boolean hidden=cameraHidden(scene);
            boolean expectedBody=!hidden&&scene.mode!=3&&scene.mode!=4&&scene.mode!=5&&scene.mode!=9;
            if(Client.owns(mc.player.getId())!=expectedBody)throw new AssertionError(scene.name+": local body claim should be "+expectedBody);
            boolean expectedWeapon=!hidden&&!scene.sword.isEmpty()&&scene.mode!=3&&scene.mode!=7&&scene.mode!=8;
            if((expectedBody||expectedWeapon)&&VolumeGpu.shaderPackDetected&&VolumeGpu.deferredComposites<1)
                throw new AssertionError(scene.name+": shader pack expected a final-frame deferred composite, got "+VolumeGpu.deferredComposites);
            int weaponDelta=Client.weaponDraws-weaponBefore;
            if(expectedWeapon?weaponDelta<=0:weaponDelta!=0)throw new AssertionError(scene.name+": unexpected weapon draw delta "+weaponDelta);
            if(hidden&&(Client.bodyDraws!=0||VolumeGpu.deferredComposites!=0||VolumeGpu.hasDeferred()||legacyParticlesWrapping(mc.player.getId())))
                throw new AssertionError(scene.name+": first-person off left body, deferred work, or legacy particles active");
            if(scene.mode==13||scene.mode==17){
                assertNoVolumeTargets();
                int before=scene.mode==17?reloadVolumeGeneration:sceneVolumeGeneration;
                if(VolumeGpu.generation!=before)throw new AssertionError(scene.name+": hidden renderer compiled a volume shader");
            }
            if(expectedBody&&(ExoticFireOrbitRenderer.visualTier(mc.player.getId())!=scene.tier||ExoticFireOrbitRenderer.visualLevel(mc.player.getId())!=scene.level))throw new AssertionError(scene.name+": actual server armor packet mismatch");
            if(scene.mode==4&&!mc.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty())throw new AssertionError("Removed armor was still equipped");
            List<Boolean> remoteClaims=new ArrayList<>();
            if(scene.mode==2)for(int i=0;i<REMOTES.size();i++){boolean owned=Client.owns(REMOTES.get(i).getId());remoteClaims.add(owned);if(owned!=(i<3))throw new AssertionError("Player budget/distance: "+remoteClaims);}
            // Reload still registers the regular FireRender shader; volume programs stay lazy while hidden.
            // The ordinary third-person reload verifies both paths reconstruct successfully once visible.
            if(scene.mode==6&&(FireRender.generation<=reloadGeneration||VolumeGpu.generation<=reloadVolumeGeneration))throw new AssertionError("Resource reload did not recreate both programs");
            int gl=GL11.glGetError();if(gl!=0)throw new AssertionError("GL error "+gl);
            Map<String,Object> visibility;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                image.writeToFile(OUT.resolve(pending));
                visibility=verifyFinalVisibility(image);
            }
            Map<String,Object> shot=new LinkedHashMap<>();shot.put("file",pending);shot.put("tier",scene.tier);shot.put("level",scene.level);shot.put("camera",scene.camera.name());
            shot.put("bodyClaim",Client.owns(mc.player.getId()));shot.put("bodyDraws",Client.bodyDraws);shot.put("weaponDrawDelta",weaponDelta);shot.put("glError",gl);shot.put("renderScale",VolumeGpu.renderScale);
            shot.put("shaderPackDetected",VolumeGpu.shaderPackDetected);shot.put("deferredCompositesInFinalFrame",VolumeGpu.deferredComposites);shot.put("finalVisibility",visibility);
            shot.put("firstPersonVisualPreference",HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.get());
            shot.put("thirdPersonOptimization",HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get());
            shot.put("coverageBudgetEnabled",VolumeGpu.coverageBudgetEnabled);
            if(hidden){shot.put("hiddenChecks",Map.of("noBodyDraw",Client.bodyDraws==0,"noWeaponDraw",weaponDelta==0,"noDeferredWork",!VolumeGpu.hasDeferred()&&VolumeGpu.deferredComposites==0,"legacyParticlesBlocked",!legacyParticlesWrapping(mc.player.getId())));}
            if(expectedBody||expectedWeapon){
                int steps=GL20.glGetUniformi(VolumeGpu.program,GL20.glGetUniformLocation(VolumeGpu.program,"Steps"));
                int expected=scene.camera.isFirstPerson()&&scene.mode!=12?192:512;
                if(steps!=expected)throw new AssertionError("Actual integration precision mismatch: "+steps+" vs "+expected);
                shot.put("actualGpuSteps",steps);
                shot.put("integrationTarget",List.of(VolumeGpu.lowWidth,VolumeGpu.lowHeight));
                shot.put("projectionCoverage",VolumeGpu.coverage);
                shot.put("integrationPixels",VolumeGpu.integrationPixels);
                if(scene.mode==18&&VolumeGpu.integrationPixels>90000)throw new AssertionError("Close scene exceeded 90000 integration pixels");
                if(scene.mode==19&&(VolumeGpu.lowWidth!=mc.getMainRenderTarget().width||VolumeGpu.lowHeight!=mc.getMainRenderTarget().height))throw new AssertionError("Native close control used a reduced target");
            }
            if(scene.tier==9){
                float[] actual=new float[3];GL20.glGetUniformfv(VolumeGpu.program,GL20.glGetUniformLocation(VolumeGpu.program,"MidColor"),actual);
                if(!Client.recipe(9,scene.level).dynamicSpectrum())throw new AssertionError("Nirvana recipe lost spectrum flag");
                shot.put("nirvanaGpuMidColor",List.of(actual[0],actual[1],actual[2]));shot.put("nirvanaSharedClockTicks",Client.time()*20);
            }
            if(scene.mode==10){
                var matrix=BodyAttachment.snapshot(REMOTES.get(0).getId());if(matrix==null)throw new AssertionError("Actual player layer attachment not captured");
                var up=matrix.transformDirection(new org.joml.Vector3f(0,1,0)).normalize().rotate(mc.gameRenderer.getMainCamera().rotation());
                if(Math.abs(up.y)>.6)throw new AssertionError("Flight fire still vertical: "+up);
                shot.put("flightBodyWorldUp",List.of(up.x,up.y,up.z));shot.put("flightPoseScope","Actual rendered remote torso with client flight-state fixture, not network flight authorization");
            }
            if(expectedBody||expectedWeapon)shot.put("supportBuildCpuMs",VolumeGpu.supportCpuMs);
            shot.put("supportBuildCpuScope","CPU wall time around the last production SUPPORT_BOUNDS.build; not a whole-frame or all-player sum, and separate from GPU timestamps");
            shot.put("viewport",List.of(mc.getMainRenderTarget().width,mc.getMainRenderTarget().height));if(!remoteClaims.isEmpty())shot.put("simulatedClientPlayerClaims",remoteClaims);
            if(scene.tier>0)shot.put("recipeHash",Client.recipe(scene.tier,scene.level).hash());
            if(reloads(scene)){shot.put("shaderGenerationBeforeAfter",List.of(reloadGeneration,FireRender.generation));shot.put("volumeGenerationBeforeAfter",List.of(reloadVolumeGeneration,VolumeGpu.generation));}
            SHOTS.add(shot);
            if(scene.gpu) {
                if(FRAME_MS.size()<5||BODY_MS.size()<5)throw new AssertionError("Insufficient GPU samples: "+FRAME_MS.size());
                Map<String,Object> gpu=new LinkedHashMap<>();gpu.put("scene",scene.name);gpu.put("frameMs",List.copyOf(FRAME_MS));gpu.put("bodyStageMs",List.copyOf(BODY_MS));
                gpu.put("frameMedianMs",median(FRAME_MS));gpu.put("bodyMedianMs",median(BODY_MS));
                gpu.put("scope","Actual frame and AFTER_PARTICLES event GPU timestamps, including other event handlers");gpu.put("renderScale",VolumeGpu.renderScale);gpu.put("steps",VolumeGpu.integrationSteps(Client.recipe(scene.tier,scene.level)));
                gpu.put("supportBuildCpuMs",List.copyOf(SUPPORT_BUILD_CPU_MS));gpu.put("supportBuildCpuMedianMs",median(SUPPORT_BUILD_CPU_MS));
                gpu.put("supportBuildCpuScope","Separately measured CPU wall time around the last production SUPPORT_BOUNDS.build in each sampled frame; not an all-player or whole-frame CPU sum");GPU.add(gpu);
                gpu.put("viewport",List.of(mc.getMainRenderTarget().width,mc.getMainRenderTarget().height));gpu.put("integrationTarget",List.of(VolumeGpu.lowWidth,VolumeGpu.lowHeight));gpu.put("integrationPixels",VolumeGpu.integrationPixels);
            }
            pending=null;scene=null;
        }catch(Throwable error){finish(error.toString());}
    }
    /** Calibrated positive controls on the final scene image, never an isolated FBO. */
    private static Map<String,Object> verifyFinalVisibility(NativeImage image) {
        boolean blue=scene.name.equals("armor-fire-5-level-5");
        boolean gold=scene.name.equals("weapon-gold-level-V")||scene.name.equals("weapon-without-armor");
        Map<String,Object> result=new LinkedHashMap<>();result.put("required",blue||gold);
        if(!blue&&!gold) {
            result.put("scope","Submission/state checks only in this scene; final-pixel positive controls are blue armor V, gold weapon V, and gold weapon without armor");
            return result;
        }
        int[] reference=blue?new int[]{570,385,710,550}:new int[]{800,220,1140,580};
        int x0=reference[0]*image.getWidth()/1280,y0=reference[1]*image.getHeight()/720;
        int x1=reference[2]*image.getWidth()/1280,y1=reference[3]*image.getHeight()/720;
        int visible=0;
        for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++) {
            // Exclude the actual gold blade and its glint. The remaining wide
            // ROI catches flames on either side across their animation phases.
            if(gold) {
                double rx=x*1280.0/image.getWidth(),ry=y*720.0/image.getHeight();
                double along=Math.max(0,Math.min(1,((rx-880)*130+(ry-260)*355)/(130.0*130+355.0*355)));
                double dx=rx-(880+130*along),dy=ry-(260+355*along);
                if(dx*dx+dy*dy<=14*14)continue;
            }
            int rgba=image.getPixelRGBA(x,y),r=rgba&255,g=(rgba>>>8)&255,b=(rgba>>>16)&255;
            if(blue?(b>=128&&b-r>=90&&b-g>=15&&g-r>=10):(r>=210&&g>=150&&r-b>=65&&g-b>=45))visible++;
        }
        int referenceMinimum=blue?100:150;
        int minimum=Math.max(1,(int)Math.ceil(referenceMinimum*(image.getWidth()/1280.0)*(image.getHeight()/720.0)));
        result.put("method","Bright saturated fire-color positive control in a fixed final-scene ROI; not full image equivalence");
        result.put("roi",List.of(x0,y0,x1,y1));result.put("matchingPixels",visible);result.put("minimumMatchingPixels",minimum);
        if(gold)result.put("excludedBladeBandAt1280x720",Map.of("segment",List.of(880,260,1010,615),"radiusPixels",14));
        result.put("colorRule",blue?"B>=128, B-R>=90, B-G>=15, G-R>=10":"R>=210, G>=150, R-B>=65, G-B>=45");
        result.put("calibration","1280x720 accepted native blue armor V: 1112; gold V outside the blade band across saved native rounds: 11344..17993; previous invisible Solas controls: 0");
        result.put("passed",visible>=minimum);
        if(visible<minimum)throw new AssertionError(scene.name+": final image fire visibility failed, "+visible+" matching pixels < "+minimum+" in "+result.get("roi"));
        return result;
    }
    private static double median(List<Double> values){double[] a=values.stream().mapToDouble(Double::doubleValue).sorted().toArray();return(a[(a.length-1)/2]+a[a.length/2])*.5;}
    private static void finish(String error) {
        if(done)return;done=true;
        try {
            if(configCaptured){
                HudConfig.EXOTIC_FIRE_WRAP_VISUAL.set(originalWrapVisual);
                HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.set(originalFirstPersonVisual);
                HudConfig.EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION.set(originalFirstPersonOptimization);
                HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.set(originalThirdPersonOptimization);
                VolumeGpu.coverageBudgetEnabled=originalCoverageBudget;
            }
            Files.createDirectories(OUT);Map<String,Object> result=new LinkedHashMap<>();result.put("passed",error==null);result.put("error",error==null?"":error);result.put("expectedScenes",SCENES.size());result.put("gpuOnly",GPU_ONLY);result.put("fixedOnly",FIXED_ONLY);
            result.put("settingsOnly",SETTINGS_ONLY);
            if(error==null)result.put("pointerSafety",cn.blockforge.generated.generatedmod.client.qa.PointerSafety.verify());
            result.put("scope",SETTINGS_ONLY?"UI-only disposable-world preparation, production settings widgets, real scrolling and screenshots; zero VFX scene/GPU matrix acceptance":"Full production VFX scenes and independent frozen probes");
            result.put("firstPersonVisualDefault",HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.getDefault());
            result.put("testPreferencesRestored",configCaptured&&HudConfig.EXOTIC_FIRE_WRAP_VISUAL.get()==originalWrapVisual&&HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.get()==originalFirstPersonVisual&&HudConfig.EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION.get()==originalFirstPersonOptimization);
            result.put("shots",SHOTS);result.put("gpu",GPU);result.put("fixedProbe",fixedProbe);result.put("gpuRenderer",GL11.glGetString(GL11.GL_RENDERER));result.put("movingDistance",movingDistance);
            result.put("notes",List.of("Actual server equipment/fire packets for the local player","Remote players are client population fixtures, not a network multiplayer test",
                    "Production render scale is never changed; GPU timestamps include event peers","Final-world pixel visibility is required in blue armor V, gold weapon V, and gold weapon without armor; other scenes retain submission/state checks",
                    "The direct black-background fixed probe establishes native image equivalence, not shader-pack final composition","Test sources and temporary save are excluded from the release JAR"));
            Files.writeString(OUT.resolve("verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
        }catch(Exception e){com.mojang.logging.LogUtils.getLogger().error("Cannot write Firecoil QA result",e);}
        if(error!=null)com.mojang.logging.LogUtils.getLogger().error("Firecoil QA failed: {}",error);
        Minecraft.getInstance().stop();
    }
    private FireCoilCapture() {}
}
