package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.realmflight.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Actual End exit-portal/respawn and ordinary double-space input, isolated disposable world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class RealmFlightDimensionCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.realmFlightDimensionCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.flightDimensionOutput","../../reports/alchemy-followup-20261014/flight-dimension"));
    private static final List<Map<String,Object>> ROWS=new ArrayList<>(),SAMPLES=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final BlockPos HOME=new BlockPos(0,131,0),PORTAL=new BlockPos(0,131,0);
    private static final int[] REALMS={3,6};
    private static boolean opened,prepared,finished,portalSent;
    private static int phase,tick,caseIndex,waitTicks;
    private static long started=System.nanoTime();
    private static CompletableFuture<?> pending;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static double clientPeak,serverPeak,displacement;
    private static net.minecraft.world.phys.Vec3 previous;
    private static void keys(Minecraft mc,boolean jump,boolean forward){
        mc.options.keyJump.setDown(jump);mc.options.keyUp.setDown(forward);mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);mc.options.keyRight.setDown(false);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(false);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.START)return;var mc=Minecraft.getInstance();
        try{
            if((System.nanoTime()-started)/1e9>240)throw new AssertionError("Dimension flight timeout");
            Files.createDirectories(OUTPUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses existing player save");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.deleteIfExists(OUTPUT.resolve("failure.txt"));Files.deleteIfExists(OUTPUT.resolve("complete.txt"));
                mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(2);mc.resizeDisplay();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("flight-dimension-"+System.currentTimeMillis(),new LevelSettings("End portal flight verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(71231,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;
            if(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=720){mc.getWindow().setWidth(1280);mc.getWindow().setHeight(720);mc.resizeDisplay();}
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;}
            if(!prepared){prepared=true;prepareCase(mc);return;}
            if(mc.screen instanceof net.minecraft.client.gui.screens.WinScreen){
                mc.setScreen(null);mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            }
            if(mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(phase==0){
                keys(mc,false,false);
                if(mc.level.dimension()!=Level.END||GeneratedMod.data(mc.player).wuxingxiantu$getRealm()!=REALMS[caseIndex]||!mc.player.getAbilities().mayfly||!mc.player.onGround()){
                    if(++waitTicks>160)throw new AssertionError("End flight permission did not become ready");return;
                }
                mc.setScreen(null);phase=1;tick=0;waitTicks=0;
                record(mc,"end-ready");
            }
            mc.player.setYRot(0);mc.player.yRotO=0;mc.player.setXRot(0);mc.player.xRotO=0;
            if(phase==1){
                tick++;keys(mc,tick==2||tick==6,tick>=18&&tick<55);
                if(tick==14&&!mc.player.getAbilities().flying)throw new AssertionError("Real End double-space did not enter flight");
                if(tick==45)record(mc,"end-cruise");
                if(tick==55){
                    keys(mc,false,false);portalSent=true;phase=2;tick=0;waitTicks=0;
                    var id=mc.player.getUUID();pending=mc.getSingleplayerServer().submitAsync(()->{
                        var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                        var saved=p.saveWithoutId(new net.minecraft.nbt.CompoundTag());saved.putBoolean("seenCredits",true);p.load(saved);
                        level.setBlock(PORTAL,Blocks.END_PORTAL.defaultBlockState(),3);
                        p.setPos(.5,131.2,.5);
                        Blocks.END_PORTAL.entityInside(Blocks.END_PORTAL.defaultBlockState(),level,PORTAL,p);
                    });return;
                }
            }else if(phase==2){
                keys(mc,false,false);waitTicks++;
                if(mc.level.dimension()==Level.OVERWORLD&&GeneratedMod.data(mc.player).wuxingxiantu$getRealm()==REALMS[caseIndex]){
                    if(!mc.player.getAbilities().mayfly||mc.player.getAbilities().getFlyingSpeed()<=0)throw new AssertionError("Overworld returned without immediate realm permission");
                    if(mc.player.getAbilities().flying)throw new AssertionError("Old flight flag survived portal return");
                    record(mc,"overworld-return-ready");phase=3;tick=0;clientPeak=serverPeak=displacement=0;previous=null;
                }else if(waitTicks>100)throw new AssertionError("Actual End exit portal did not restore Overworld realm data");
            }else if(phase==3){
                tick++;keys(mc,tick==4||tick==8,tick>=18&&tick<=75);
                if(tick==16&&!mc.player.getAbilities().flying)throw new AssertionError("Real double-space failed after End return");
                if(tick==36)record(mc,"overworld-cruise");
                if(tick==76){
                    double cap=RealmFlightBalance.speed(REALMS[caseIndex],3,false)/20;
                    if(clientPeak>cap+.025||serverPeak>cap+.025)throw new AssertionError("Post-portal overspeed: client="+clientPeak+", server="+serverPeak+", cap="+cap);
                    if(displacement<8)throw new AssertionError("Post-portal actual flight did not move");
                    var result=new LinkedHashMap<String,Object>();result.put("realm",REALMS[caseIndex]);result.put("portal",portalSent);result.put("clientPeakPerTick",clientPeak);result.put("serverPeakPerTick",serverPeak);result.put("cruiseLimitPerTick",cap);result.put("actualHorizontalDistance",displacement);result.put("returnReadyTicks",waitTicks);ROWS.add(result);
                    keys(mc,false,false);caseIndex++;
                    if(caseIndex==REALMS.length){finish();return;}prepareCase(mc);
                }
            }
        }catch(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("REALM_FLIGHT_DIMENSION",error);finish();}
    }
    private static void prepareCase(Minecraft mc){
        phase=0;tick=waitTicks=0;portalSent=false;var id=mc.player.getUUID();int realm=REALMS[caseIndex];
        pending=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var server=p.server;var end=server.getLevel(Level.END);
            for(var level:List.of(server.overworld(),end))for(int x=-5;x<=5;x++)for(int z=-5;z<=100;z++){
                level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                for(int y=131;y<=136;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
            }
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(realm);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
            p.setRespawnPosition(Level.OVERWORLD,HOME,0,true,false);p.setInvulnerable(true);p.setNoGravity(false);
            p.teleportTo(end,.5,131,.5,Set.of(),0,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);GeneratedMod.sync(p);
        });
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void sample(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END||phase!=3||tick<18||tick>75)return;var mc=Minecraft.getInstance();
        try{
            var p=mc.player;clientPeak=Math.max(clientPeak,p.getDeltaMovement().horizontalDistance());
            if(previous!=null)displacement+=p.position().subtract(previous).horizontalDistance();previous=p.position();
            if(serverSample!=null&&serverSample.isDone()){
                var row=serverSample.join();serverSample=null;if(((Number)row.get("realm")).intValue()==REALMS[caseIndex]){serverPeak=Math.max(serverPeak,((Number)row.get("speed")).doubleValue());SAMPLES.add(row);}
            }
            if(serverSample==null){var id=p.getUUID();serverSample=mc.getSingleplayerServer().submit(()->{
                var owner=mc.getSingleplayerServer().getPlayerList().getPlayer(id);return Map.of("realm",GeneratedMod.data(owner).wuxingxiantu$getRealm(),"speed",owner.getDeltaMovement().horizontalDistance(),"gameTime",owner.level().getGameTime(),"flying",owner.getAbilities().flying);
            });}
        }catch(Throwable error){FAILURES.add(error.toString());finish();}
    }
    private static void record(Minecraft mc,String name)throws Exception{
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve("realm-"+REALMS[caseIndex]+"-"+name+".png"));}
    }
    private static void finish(){
        if(finished)return;finished=true;var mc=Minecraft.getInstance();keys(mc,false,false);
        try{
            var report=Map.of("test","actual End exit portal, respawn and normal KeyMapping double-space", "cases",ROWS,"serverSamples",SAMPLES,"failures",FAILURES);
            Files.writeString(OUTPUT.resolve("verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
            Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?"PASS":String.join("\n",FAILURES));
        }catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("FLIGHT_DIMENSION_RESULT",error);}mc.stop();
    }
}
