package cn.blockforge.generated.generatedmod.client.qa.v5;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.inputs.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.client.AlchemyScreen;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraftforge.event.TickEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Actual C2S control/claim acceptance in a hidden disposable world; shares NoGrab protection. */
final class V5IncidentAcceptance {
    private static final BlockPos POS=new BlockPos(0,200,0);
    private static final Path OUT=Path.of("../../outputs/v5-incident-client-validation").toAbsolutePath().normalize();
    private static final long START=System.nanoTime();
    private static boolean opened,done;private static int stage,ticks,scenario,violations,frames;
    private static final ArrayDeque<String> queuedCaptures=new ArrayDeque<>();
    private static boolean ventApplied;private static CompletableFuture<?> pending;private static final List<String> captures=new ArrayList<>();
    static void tick(TickEvent.ClientTickEvent event){
        if(done||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;frames++;
            if(mc.mouseHandler.isMouseGrabbed()||org.lwjgl.glfw.GLFW.glfwGetInputMode(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_CURSOR)!=org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL)violations++;
            check((System.nanoTime()-START)/1e9<150,"Timeout stage="+stage+" scenario="+scenario);
            if(!opened&&mc.player!=null)throw new AssertionError("Unrelated world refused");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1440,1080);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.framerateLimit().set(40);mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("v5-incidents-"+System.currentTimeMillis(),new LevelSettings("Independent V5 incident QA",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(7216,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;
            mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;ticks=0;}
            ticks++;if(ticks%40==0)Files.writeString(OUT.resolve("progress.json"),"{\"stage\":"+stage+",\"scenario\":"+scenario+",\"screen\":\""+(mc.screen==null?"none":mc.screen.getClass().getSimpleName())+"\",\"menu\":\""+mc.player.containerMenu.getClass().getSimpleName()+"\"}");
            if(stage==0){if(ticks<35)return;mc.mouseHandler.grabMouse();check(V5ClientAcceptance.blockedGrabs>0&&violations==0,"Pointer protection failed");pending=mc.getSingleplayerServer().submit(()->prepare(mc));stage=1;return;}
            if(!(mc.screen instanceof AlchemyScreen)||!(mc.player.containerMenu instanceof AlchemyFurnaceMenu menu))return;
            if(stage==1){if(ticks<15)return;click(mc,menu,255);stage=2;ticks=0;return;}
            if(stage==2){if(ticks<12||!menu.freeMode())return;check(menu.state()==AlchemyFurnaceMenu.State.READY,"Not ready "+menu.state());
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.alchemy.AlchemyPacket(POS,cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.ID));stage=3;ticks=0;return;}
            if(stage==3){if(ticks<6||!menu.isBrewing())return;click(mc,menu,scenario==2?247:246);stage=4;ticks=0;return;}
            if(stage==4){if(ticks<7)return;if(scenario!=2)click(mc,menu,246);stage=5;ticks=0;return;}
            if(stage==5){
                if(scenario==2){if(ticks<45)return;capture(mc,"paid-warm-hold");pending=mc.getSingleplayerServer().submit(()->{var d=furnace(mc).v5ServerRecord();check(d.getInt("ProcessStep")<=1&&V5Batch.core(d).tick()>=2&&d.getInt("FuelSpent")>10,"Warm hold was free/frozen");check(V5Batch.core(d).stocks().get("moon_essence")<1000,"Warm moon not charged");});stage=6;return;}
                if(scenario==0){if(menu.v5Warning()==0)return;capture(mc,"warning-before-stop");click(mc,menu,246);stage=60;ticks=0;return;}
                if(!menu.furnace().hasResult())return;stage=7;ticks=0;return;
            }
            if(stage==60){if(ticks<6)return;click(mc,menu,245);stage=6;ticks=0;return;}
            if(stage==6){if(ticks<6)return;if(scenario==0&&!ventApplied){pending=mc.getSingleplayerServer().submit(()->{var d=furnace(mc).v5ServerRecord();ventApplied=V5Batch.core(d).ledger().ventedMass()>0;check(ventApplied||d.getString("State").equals("ACTIVE"),"Terminated before queued vent was processed");});return;}click(mc,menu,248);stage=7;ticks=0;return;}
            if(stage==7){if(ticks<20||!menu.furnace().hasResult())return;capture(mc,scenario==0?"emergency-residue":scenario==1?"pressure-blast":scenario==2?"warm-stop":"thermal-ash");
                pending=mc.getSingleplayerServer().submit(()->{
                    var f=furnace(mc);var d=f.v5ServerRecord();var c=V5Batch.core(d);AlchemySimulation.validate(V5Batch.profile(d.getString("Profile"),d.getInt("Burn")).rules(),c);
                    if(scenario==0||scenario==2)check(c.status()==AlchemySimulation.Status.ABORTED&&f.completedResult().is(WuXingItems.ALCHEMY_RESIDUE.get()),"Emergency stop did not produce residue: "+c.status());
                    if(scenario==0)check(c.ledger().ventedMass()>0,"C2S vent never affected resources");
                    if(scenario==1)check(c.status()==AlchemySimulation.Status.EXPLODED&&f.v5DeviceWear()==500,"No actual pressure failure/wear: "+c.status());
                    if(scenario==3)check(c.status()==AlchemySimulation.Status.ASHED&&c.stocks().isEmpty(),"No actual thermal ash: "+c.status());
                    var journal=new BatchJournal(mc.getSingleplayerServer());check(journal.read(d.getUUID("Id")).getString("State").equals("READY"),"Terminal journal missing");
                });stage=8;return;}
            if(stage==8){if(ticks<10)return;click(mc,menu,249);stage=9;ticks=0;return;}
            if(stage==9){if(ticks<15||menu.furnace().hasResult())return;click(mc,menu,249);pending=mc.getSingleplayerServer().submit(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());long count=p.getInventory().items.stream().filter(i->i.hasTag()&&i.getTag().hasUUID(V5Batch.KEY)).mapToLong(ItemStack::getCount).sum();check(count==1,"Claim duplicated result: "+count);
            });stage=10;return;}
            if(stage==10){capture(mc,"claimed-"+scenario);if(++scenario>=4){mc.options.guiScale().set(3);mc.resizeDisplay();stage=11;ticks=0;return;}pending=mc.getSingleplayerServer().submit(()->prepare(mc));stage=1;}
            if(stage==11&&ticks>=12){capture(mc,"gui3-completed");stage=12;ticks=0;return;}
            if(stage==12&&ticks>=5)finish(mc,null);
        }catch(Throwable e){com.mojang.logging.LogUtils.getLogger().error("V5_INCIDENT_QA",e);finish(mc,e.toString());}
    }
    private static AlchemyFurnaceBlockEntity furnace(Minecraft mc){return (AlchemyFurnaceBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS);}
    private static void prepare(Minecraft mc){
        var server=mc.getSingleplayerServer();var level=server.overworld();var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.closeContainer();p.getInventory().clearContent();p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(level,.5,201,-3,0,0);
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<6;y++)level.setBlockAndUpdate(POS.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(POS,WuXingBlocks.ALCHEMY_FURNACE.get().defaultBlockState());CoreStructureBuilder.build(p,POS);AlchemyFurnaceStructure.refreshAround(level,POS);
        var f=furnace(mc);var saved=f.saveWithoutMetadata();saved.putInt("SpiritFuel",f.spiritCapacity());f.load(saved);
        var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),scenario==1?8:4);HerbAge.setYears(herb,1500);f.setItem(0,herb);f.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        if(scenario==2){var moon=new MoonBottleData();moon.collect(new AlchemyEnvironment.Sample("minecraft:overworld",0,18000,Map.of("night",1L,"open_sky",1L,"moon_phase",0L,"moon_input_rate",1000L)));var bottle=new ItemStack(WuXingBlocks.MOON_BOTTLE_ITEM.get());var be=new CompoundTag();be.put("MoonBottle",moon.save());bottle.addTagElement("BlockEntityTag",be);f.setItem(1,bottle);}
        GeneratedMod.data(p).wuxingxiantu$learnFire(1);cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.unlock(p);net.minecraftforge.network.NetworkHooks.openScreen(p,f.menuProvider(),POS);
    }
    private static void click(Minecraft mc,AlchemyFurnaceMenu m,int id){mc.gameMode.handleInventoryButtonClick(m.containerId,id);}
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    private static void capture(Minecraft mc,String name){queuedCaptures.add(name);}
    static void render(Minecraft mc){
        if(done)return;try{while(!queuedCaptures.isEmpty()){
            String name=queuedCaptures.remove();check(org.lwjgl.opengl.GL11.glGetError()==0,"GL error");
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}captures.add(name);
        }}catch(Throwable e){finish(mc,e.toString());}
    }
    private static void finish(Minecraft mc,String error){if(done)return;done=true;try{Files.writeString(OUT.resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",error==null,"scenarios",scenario,"captures",captures,"checkedTicks",frames,"captureViolations",violations,"blockedGrabs",V5ClientAcceptance.blockedGrabs,"error",error==null?"":error)));}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
