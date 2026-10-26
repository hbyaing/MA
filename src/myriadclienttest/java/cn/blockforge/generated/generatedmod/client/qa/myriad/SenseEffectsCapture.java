package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.progression.*;
import cn.blockforge.generated.generatedmod.divinesurvey.*;
import cn.blockforge.generated.generatedmod.network.packet.ProgressionActionPacket;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.google.gson.GsonBuilder;

/** Real integrated-server actions and S2C receipts, an isolated arena and no pointer capture. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SenseEffectsCapture {
    static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.senseEffectsCapture");
    static final String EXPECTED=System.getProperty("wuxingxiantu.myriadExpectedShaderPack","");
    static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("effects");
    static final String[] CASES={"scan-wide","scan-first-person","identify-channel","identify-complete","opening","opening-interrupted","lock","lock-occluded","survey-start","survey-stopped"};
    static final int[] CAPTURE_TICK={26,26,28,58,58,38,28,48,34,58};
    static final List<Map<String,Object>> evidence=new ArrayList<>();
    static boolean opened,finished,arena,prepared,acted,shaderChecked;static int stage,tick;static int dummyId;static CompletableFuture<?> pending;
    static final long START=System.nanoTime();static Method cameraPosition;static long beforeDraws;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;GLFW.glfwHideWindow(mc.getWindow().getWindow());
            if((System.nanoTime()-START)/1e9>480)throw new AssertionError("Sense VFX timeout stage="+stage);
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses user's loaded world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1600,1200);mc.options.languageCode="zh_cn";mc.getLanguageManager().setSelected("zh_cn");mc.reloadResourcePacks();mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(5);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);mc.options.hideGui=true;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("sense-vfx-"+System.currentTimeMillis(),new LevelSettings("Isolated spiritual sense shader QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7219,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.getOverlay()!=null)return;
            if(!shaderChecked){shader();shaderChecked=true;}
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;}
            if(!arena){arena=true;pending=mc.getSingleplayerServer().submit(()->{
                var server=mc.getSingleplayerServer();var level=server.overworld();level.setDayTime(6000);level.setChunkForced(0,0,true);
                for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++)level.setBlockAndUpdate(new BlockPos(x,199,z),Blocks.SMOOTH_STONE.defaultBlockState());
                for(int y=200;y<205;y++)level.setBlockAndUpdate(new BlockPos(9,y,5),Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.connection.teleport(.5,200,.5,0,0);
                var dummy=EntityType.IRON_GOLEM.create(level);dummy.setNoAi(true);dummy.setNoGravity(true);dummy.setInvulnerable(true);dummy.setPos(.5,200,7.5);dummy.setCustomName(Component.literal("识海测试傀儡"));level.addFreshEntity(dummy);dummyId=dummy.getId();
                for(int y=100;y<103;y++)level.setBlockAndUpdate(new BlockPos(3,y,3),Blocks.DIAMOND_ORE.defaultBlockState());
            });return;}
            if(mc.player.getY()<198||mc.level.getEntity(dummyId)==null)return;
            if(!prepared){
                SenseEffects.clear();mc.mouseHandler.grabMouse();PointerSafety.verify();mc.setScreen(null);mc.options.setCameraType(stage==1?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);beforeDraws=SenseFieldGpu.submitted;
                pending=mc.getSingleplayerServer().submit(()->{var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayer(mc.player.getUUID());
                    if(DivineSurvey.active(p))DivineSurvey.toggle(p);SpiritualSense.login(new PlayerEvent.PlayerLoggedInEvent(p));GeneratedMod.data(p).wuxingxiantu$setRealm(8);
                    var n=PlayerCrafts.get(server).account(p.getUUID());n.putBoolean("SenseOpen",stage!=4&&stage!=5);n.putFloat("Sense",3600);p.connection.teleport(.5,200,.5,0,0);
                    for(int x=-1;x<=1;x++)for(int y=200;y<=204;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,3),Blocks.AIR.defaultBlockState());SpiritualSense.sync(p);
                });prepared=true;acted=false;tick=0;return;
            }
            tick++;
            if(tick==10){String action=stage<=1?"scan":stage<=3?"identify":stage<=5?"open":stage<=7?"lock":"survey";Network.CHANNEL.sendToServer(new ProgressionActionPacket(action));acted=true;}
            if(tick==22&&(stage==5||stage==7||stage==9))pending=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(stage==5)p.connection.teleport(1.5,200,.5,0,0);else if(stage==7){for(int x=-1;x<=1;x++)for(int y=200;y<=204;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,3),Blocks.STONE.defaultBlockState());}else DivineSurvey.toggle(p);});
        }catch(Throwable error){fail(mc,error);}
    }
    static void shader()throws Exception{
        boolean active=TrailShaderBridge.shaderPackDetected();String pack="none";
        if(active)for(String type:new String[]{"net.irisshaders.iris.Iris","net.coderbot.iris.Iris"})try{Object result=Class.forName(type).getMethod("getCurrentPackName").invoke(null);pack=result instanceof Optional<?> optional?String.valueOf(optional.orElse(null)):String.valueOf(result);break;}catch(ClassNotFoundException ignored){}
        if(EXPECTED.isEmpty()?active:!active||!pack.contains(EXPECTED))throw new AssertionError("Unexpected actual shader selection "+pack+" enabled="+active);
        Files.writeString(OUT.resolve("shader.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("active",active,"pack",pack)));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void camera(ViewportEvent.ComputeCameraAngles event){
        if(!ENABLED||finished||!arena||Minecraft.getInstance().player==null)return;
        try{
            if(cameraPosition==null){cameraPosition=Camera.class.getDeclaredMethod("setPosition",Vec3.class);cameraPosition.setAccessible(true);}
            Vec3 target=stage<=1?new Vec3(.5,201,3):stage>=6&&stage<=7?new Vec3(.5,201.4,7.5):new Vec3(.5,201,2.5);
            Vec3 position=stage==1?Minecraft.getInstance().player.getEyePosition():stage==0?new Vec3(6,210,-30):stage>=6&&stage<=7?new Vec3(7,204,-6):new Vec3(6,204,-10);
            Vec3 toward=stage==1?new Vec3(0,0,1):target.subtract(position);cameraPosition.invoke(event.getCamera(),position);event.setYaw((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));event.setPitch((float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance())));event.setRoll(0);
        }catch(Throwable error){fail(Minecraft.getInstance(),error);}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!prepared||!acted||event.phase!=TickEvent.Phase.END||tick<CAPTURE_TICK[stage]||pending!=null)return;var mc=Minecraft.getInstance();
        try{
            PointerSafety.verify();int error=GL11.glGetError();if(error!=0)throw new AssertionError("Sense shader GL error="+error);
            if(!SenseFieldGpu.ready())throw new AssertionError("Sense shader missing");boolean activeCase=stage!=5&&stage!=7&&stage!=9;
            if(activeCase&&SenseFieldGpu.submitted==beforeDraws)throw new AssertionError("No actual shader draws for "+CASES[stage]);
            var state=PlayerProgressionClient.state;
            if(stage==3&&(state.getInt("Channel")!=0||!state.getString("Lock").isEmpty()))throw new AssertionError("Identify did not complete without locking");
            if(stage==5&&state.getInt("Channel")!=0)throw new AssertionError("Movement did not cancel charge");
            if(stage==6&&state.getString("Lock").isEmpty())throw new AssertionError("Real target lock missing");
            if(stage==7&&!state.getString("Lock").isEmpty())throw new AssertionError("Wall did not remove lock");
            if(stage==9&&cn.blockforge.generated.generatedmod.divinesurvey.client.SurveyClient.active())throw new AssertionError("Survey remained active after stop");
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(CASES[stage]+".png"));}
            evidence.add(Map.of("case",CASES[stage],"shaderDraws",SenseFieldGpu.submitted-beforeDraws,"glError",error,"receipt",state.getCompound("SenseVisual").getString("Kind"),"channel",state.getInt("Channel"),"locked",!state.getString("Lock").isEmpty()));
            if(++stage==CASES.length){Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",evidence,"pointer",PointerSafety.snapshot(),"realServerActions",true)));finished=true;mc.stop();}else prepared=false;
        }catch(Throwable error){fail(mc,error);}
    }
    static void fail(Minecraft mc,Throwable error){try{Files.writeString(OUT.resolve("failure.txt"),error.toString());}catch(Exception ignored){}error.printStackTrace();finished=true;mc.stop();}
}
