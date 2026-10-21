package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.realmflight.*;
import cn.blockforge.generated.generatedmod.realmflight.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Isolated actual-client first-person, machine-menu and terrain-light verification. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class LookbackAndHerbCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.lookbackCapture");
    private static final Path OUT=Path.of("E:/五行仙途/deliverables/task-20261015-soul-herb-lookback/client");
    private static final long START=System.nanoTime();
    private static final List<Map<String,Object>> captures=new ArrayList<>();
    private static boolean opened,prepared,done,gpuDone;private static int tick;
    private static CompletableFuture<?> work;private static String pending;private static BlockPos machine,anvil;
    private static Vec3 previous;private static java.lang.reflect.Field lightField;
    private static net.minecraft.client.renderer.ShaderInstance currentLight,oldLight;
    private static boolean savedAnimation,savedEffects;private static int savedRealm;
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Shaders {
        @SubscribeEvent public static void register(RegisterShadersEvent e)throws Exception {
            if(ENABLED)e.registerShader(new net.minecraft.client.renderer.ShaderInstance(e.getResourceProvider(),
                    new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"qa_anvil_before"),com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION),s->oldLight=s);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void tick(TickEvent.ClientTickEvent e){
        if(!ENABLED||done||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUT);if((System.nanoTime()-START)>240_000_000_000L)throw new AssertionError("Capture watchdog");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1280,900);mc.resizeDisplay();mc.options.guiScale().set(2);
                mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(60);mc.options.graphicsMode().set(GraphicsStatus.FANCY);
                mc.options.cloudStatus().set(CloudStatus.OFF);mc.options.fov().set(70);mc.options.bobView().set(false);
                org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("lookback-herb-"+System.currentTimeMillis(),new LevelSettings("Lookback and herbal processing QA",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(20261015,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);return;
            }
            if(!opened||mc.player==null||mc.level==null||mc.getOverlay()!=null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(!prepared){prepared=true;savedAnimation=HudConfig.REALM_FLIGHT_ANIMATION.get();savedEffects=HudConfig.REALM_FLIGHT_EFFECTS.get();savedRealm=HudConfig.REALM_FLIGHT_VISUAL_REALM.get();HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_EFFECTS.set(true);HudConfig.REALM_FLIGHT_VISUAL_REALM.set(8);
                var id=mc.player.getUUID();work=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
                    p.setInvulnerable(true);p.teleportTo(p.serverLevel(),.5,135,.5,0,0);p.setNoGravity(true);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();
                    p.serverLevel().setDayTime(6000);p.serverLevel().setWeatherParameters(6000,0,false,false);p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);GeneratedMod.sync(p);
                });return;
            }
            if(work!=null){if(!work.isDone())return;work.join();work=null;}
            if(GeneratedMod.data(mc.player).wuxingxiantu$getRealm()!=8)return;
            if(pending!=null)return;
            tick++;mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setCameraEntity(mc.player);
            if(tick<=150){
                mc.setScreen(null);mc.options.hideGui=true;mc.player.getAbilities().mayfly=true;mc.player.getAbilities().flying=true;mc.player.setNoGravity(true);
                double bend= tick<65?0:Math.min(35,tick-65)*.035;
                Vec3 p=tick<65?new Vec3(.5,135,.5+tick*.7):new Vec3(.5+20*(1-Math.cos(bend)),135,46+20*Math.sin(bend)+Math.max(0,tick-100)*.7);
                if(previous==null)previous=p;mc.player.setOldPosAndRot();mc.player.xOld=previous.x;mc.player.yOld=previous.y;mc.player.zOld=previous.z;
                mc.player.setPos(p.x,p.y,p.z);mc.player.setDeltaMovement(p.subtract(previous));mc.player.setYRot(0);mc.player.setXRot(0);mc.player.yBodyRot=mc.player.yBodyRotO=0;previous=p;
                RealmFlightVisuals.receive(new RealmFlightVisualPacket(mc.player.getUUID(),true,8,3,false,true));
                var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var server=mc.getSingleplayerServer().getPlayerList().getPlayer(id);if(server!=null){server.teleportTo(p.x,p.y,p.z);server.getAbilities().mayfly=true;server.getAbilities().flying=true;}});
                if(tick==55)pending="01-first-person-forward.png";if(tick==100)pending="02-first-person-look-back.png";if(tick==145)pending="03-first-person-forward-again.png";
            }else if(tick==155){
                mc.options.hideGui=false;var id=mc.player.getUUID();machine=mc.player.blockPosition().offset(1,-1,1);anvil=machine.offset(3,0,0);
                work=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();p.getAbilities().flying=false;p.onUpdateAbilities();p.setNoGravity(true);
                    for(int x=-3;x<=7;x++)for(int z=-4;z<=4;z++)level.setBlock(machine.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
                    level.setBlock(machine,WuXingBlocks.HERB_PROCESSING_TABLE.get().defaultBlockState(),3);level.setBlock(anvil,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);level.setBlock(machine.offset(-2,0,0),WuXingBlocks.SOUL_HEALING_ROOM.get().defaultBlockState(),3);
                    var table=(HerbProcessingTableBlockEntity)level.getBlockEntity(machine);table.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.PLANT_BLOCKS.get(0).get(),2));
                    net.minecraftforge.network.NetworkHooks.openScreen(p,table.menuProvider(),machine);
                });
            }else {
                mc.player.getAbilities().flying=false;mc.player.setDeltaMovement(Vec3.ZERO);
                if(tick==190){if(!(mc.screen instanceof HerbProcessingTableScreen)||!(mc.player.containerMenu instanceof HerbProcessingTableMenu m)||m.progress()<1)throw new AssertionError("Production processing menu did not open/progress");pending="04-herb-processing-menu.png";}
                if(tick==285){if(!(mc.player.containerMenu instanceof HerbProcessingTableMenu m)||!HerbPreparation.prepared(m.getSlot(1).getItem()))throw new AssertionError("Production menu did not receive processed herb");pending="05-herb-processing-output.png";}
                if(tick==300){var id=mc.player.getUUID();work=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.closeContainer();p.teleportTo(anvil.getX()+3.7,anvil.getY()+1.3,anvil.getZ()+4.1);});mc.options.hideGui=true;}
                if(tick==335){lightField=Class.forName("cn.blockforge.generated.generatedmod.client.anvillight.Lighting").getDeclaredField("shader");lightField.setAccessible(true);currentLight=(net.minecraft.client.renderer.ShaderInstance)lightField.get(null);lightField.set(null,oldLight);}
                if(tick==345)pending="06-anvil-ground-before.png";
                if(tick==350)lightField.set(null,currentLight);
                if(tick==365)pending="07-anvil-ground-after.png";
                if(tick==375)finish(null);
            }
        }catch(Throwable error){finish(error.toString());}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void camera(ViewportEvent.ComputeCameraAngles e){
        if(!ENABLED||done||!prepared)return;
        if(tick<=150){e.setYaw(tick>=70&&tick<=115?180:0);e.setPitch(0);e.setRoll(0);}
        else if(tick>=300&&anvil!=null){var toward=Vec3.atCenterOf(anvil).subtract(e.getCamera().getPosition());e.setYaw((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));e.setPitch((float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance())));e.setRoll(0);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent e){
        if(!ENABLED||done||e.phase!=TickEvent.Phase.END||Minecraft.getInstance().level==null)return;
        try{
            var mc=Minecraft.getInstance();if(!gpuDone&&tick>=30){var result=StaticFixGpuProbe.anvilOwnerBoundary();Files.writeString(OUT.resolve("anvil-boundary-gpu.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));gpuDone=true;
                var state=WuXingBlocks.SOUL_HEALING_ROOM.get().defaultBlockState();var model=mc.getBlockRenderer().getBlockModel(state);var faces=Map.of(Direction.NORTH,"front",Direction.SOUTH,"back",Direction.EAST,"left",Direction.WEST,"right",Direction.UP,"top",Direction.DOWN,"bottom");
                for(var face:faces.entrySet()){var q=model.getQuads(state,face.getKey(),net.minecraft.util.RandomSource.create(0));if(q.size()!=1||!q.get(0).getSprite().contents().name().toString().equals("wuxingxiantu:block/soul_healing_room_"+face.getValue()))throw new AssertionError("Soul face mismatch");}
            }
            if(pending==null||mc.getOverlay()!=null)return;String name=pending;
            if(name.startsWith("01-")||name.startsWith("03-")){if(RealmFlightEffects.renderedVertices!=0||RealmFlightEffects.renderedShells!=0)throw new AssertionError("New world effect added to front view; weight="+RealmFlightEffects.firstPersonViewWeight);}
            if(name.startsWith("02-")){if(RealmFlightEffects.renderedShells!=0)throw new AssertionError("First-person head restored");}
            int gl=org.lwjgl.opengl.GL11.glGetError();if(gl!=0)throw new AssertionError("GL "+gl);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name));}
            captures.add(Map.of("name",name,"tick",tick,"worldTrailVertices",RealmFlightEffects.renderedVertices,"worldTrailSegments",RealmFlightEffects.renderedTrailSegments,"heads",RealmFlightEffects.renderedShells,"existingEdgeVertices",FirstPersonFlightFlow.vertices,"glError",gl));pending=null;
        }catch(Throwable error){finish(error.toString());}
    }
    private static void finish(String error){
        if(done)return;done=true;try{if(lightField!=null&&currentLight!=null)lightField.set(null,currentLight);HudConfig.REALM_FLIGHT_ANIMATION.set(savedAnimation);HudConfig.REALM_FLIGHT_EFFECTS.set(savedEffects);HudConfig.REALM_FLIGHT_VISUAL_REALM.set(savedRealm);
            Files.writeString(OUT.resolve("verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",error==null&&captures.size()==7,"error",error==null?"":error,"captures",captures,"gpuBoundary",gpuDone)));
        }catch(Exception ex){com.mojang.logging.LogUtils.getLogger().error("QA save",ex);}Minecraft.getInstance().stop();
    }
}
