package cn.blockforge.generated.generatedmod.client.qa.v5;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.client.AlchemyScreen;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraftforge.event.TickEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** End-to-end ordinary furnace, native eating packets, then a second JVM checks the spent-dose ledger. */
final class V5EffectAcceptance {
    private static final BlockPos POS=new BlockPos(0,200,0);
    private static final Path OUT=Path.of(System.getProperty("wuxingxiantu.v5Output")).resolveSibling("v5-effect-client-validation");
    private static final Path CONTROL=Path.of("v5-effects-qa.json");
    private static final boolean RESUME=Boolean.getBoolean("wuxingxiantu.v5Resume");
    private static final long START=System.nanoTime();
    private static int stage,ticks,frames,violations,quantity,grade,gui3Scale;private static boolean opened,done,canEat;
    private static CompletableFuture<?> pending;private static JsonObject control;private static float expectedHealth,expectedMana;
    private static final List<String> captures=new ArrayList<>();private static final ArrayDeque<String> queued=new ArrayDeque<>();
    static void tick(TickEvent.ClientTickEvent event){
        if(done||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();frames++;
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(mc.mouseHandler.isMouseGrabbed()||org.lwjgl.glfw.GLFW.glfwGetInputMode(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_CURSOR)!=org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL)violations++;
            check((System.nanoTime()-START)/1e9<240,"Timeout stage="+stage);
            if(!opened&&mc.player!=null)throw new AssertionError("Unrelated world refused");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1440,1080);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                if(RESUME){control=JsonParser.parseString(Files.readString(CONTROL)).getAsJsonObject();String world=control.get("world").getAsString();check(world.startsWith("v5-effects-"),"Unrelated save refused");mc.createWorldOpenFlows().loadLevel(new net.minecraft.client.gui.screens.TitleScreen(),world);}
                else{control=new JsonObject();String world="v5-effects-"+System.currentTimeMillis();control.addProperty("world",world);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(false,null);
                    mc.createWorldOpenFlows().createFreshLevel(world,new LevelSettings("V5 effect independent acceptance",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(7217,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);}
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;ticks=0;}ticks++;
            if(ticks%40==0)Files.writeString(OUT.resolve("progress.json"),"{\"stage\":"+stage+",\"resume\":"+RESUME+",\"screen\":\""+(mc.screen==null?"none":mc.screen.getClass().getSimpleName())+"\"}");
            if(stage==0){if(ticks<35)return;mc.mouseHandler.grabMouse();check(V5ClientAcceptance.blockedGrabs>0&&violations==0,"Pointer guard failed");pending=mc.getSingleplayerServer().submit(()->prepare(mc));stage=RESUME?20:1;return;}
            if(stage==20){if(ticks<15)return;pending=mc.getSingleplayerServer().submit(()->{
                var p=player(mc);var d=new BatchJournal(p.server).read(UUID.fromString(control.get("batch").getAsString()));var stack=p.getMainHandItem();var result=PillSnapshot.read(d.getCompound("PillResult"));quantity=result.quantity();grade=result.grade();
                check(d.getInt("DosesUsed")==1&&stack.getCount()==quantity-1&&PillSnapshot.matches(stack,d),"Restart reset consumption or lost remaining doses");
                check(AbilityCompiler.hash(d.getCompound("PillResult").toString()).equals(control.get("resultHash").getAsString()),"Restart recompiled or rerolled nine-grade manifest");
                var forged=stack.copy();forged.getTag().getCompound(PillSnapshot.KEY).putInt("Grade",grade==9?8:9);check(!PillRuntime.canUse(p,forged),"Forged client grade became authority");setupEating(mc,d);
            });stage=21;return;}
            if(stage==21){if(ticks<5)return;pending=mc.getSingleplayerServer().submit(()->canEat=PillRuntime.canUse(player(mc),player(mc).getMainHandItem()));stage=22;return;}
            if(stage==22){if(!canEat){stage=21;return;}if(ticks<8)return;mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);stage=23;ticks=0;return;}
            if(stage==23){if(ticks>=18)mc.options.keyUse.setDown(false);if(ticks<28)return;pending=mc.getSingleplayerServer().submit(()->checkDose(mc,2));stage=24;return;}
            if(stage==24){capture("resumed-native-eating");mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));stage=25;ticks=0;return;}
            if(stage==25){if(ticks<12)return;capture("resumed-inventory");stage=99;ticks=0;return;}
            if(stage==99){if(ticks<5)return;finish(mc,true,null);return;}
            if(stage==90){finish(mc,false,null);return;}
            if(stage>=1&&stage<=8&&(!(mc.screen instanceof AlchemyScreen)||!(mc.player.containerMenu instanceof AlchemyFurnaceMenu)))return;
            var menu=mc.player.containerMenu instanceof AlchemyFurnaceMenu m?m:null;
            if(stage==1){if(ticks<15)return;click(mc,menu,255);stage=2;ticks=0;return;}
            if(stage==2){if(ticks<12||!menu.freeMode())return;check(menu.state()==AlchemyFurnaceMenu.State.READY,"Free furnace not ready");capture("ready");Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.alchemy.AlchemyPacket(POS,cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.ID));stage=3;ticks=0;return;}
            if(stage==3){if(menu.isBrewing()&&ticks==50)capture("running");if(!menu.furnace().hasResult())return;capture("compiled-nine-grade");pending=mc.getSingleplayerServer().submit(()->{
                var f=furnace(mc);var d=f.v5ServerRecord();V5Batch.validate(d);var result=PillSnapshot.read(d.getCompound("PillResult"));quantity=result.quantity();grade=result.grade();check(grade>0&&quantity>=2&&d.contains("ReactionCore"),"Actual furnace did not compile usable doses");
                check(result.spent()<=result.budget()&&V5Batch.core(d).stocks().get(AbilityCompiler.BOUND)==result.boundMass(),"Output budget not physically bound");
                try{Files.writeString(OUT.resolve("public-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception e){throw new RuntimeException(e);}
                control.addProperty("batch",d.getUUID("Id").toString());control.addProperty("resultHash",AbilityCompiler.hash(d.getCompound("PillResult").toString()));control.addProperty("quantity",quantity);control.addProperty("grade",grade);
                var p=player(mc);for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.DIAMOND,64));p.containerMenu.broadcastChanges();
            });stage=4;return;}
            if(stage==4){if(ticks<12)return;click(mc,menu,249);stage=5;ticks=0;return;}
            if(stage==5){if(ticks<15)return;check(menu.furnace().hasResult(),"Full inventory lost compiled batch");capture("full-inventory-retains-doses");mc.options.guiScale().set(3);mc.resizeDisplay();stage=6;ticks=0;return;}
            if(stage==6){if(ticks<12)return;gui3Scale=(int)mc.getWindow().getGuiScale();check(gui3Scale==3,"GUI3 was not actually selected");capture("compiled-gui3");stage=61;ticks=0;return;}
            if(stage==61){if(ticks<5)return;mc.options.guiScale().set(2);mc.resizeDisplay();pending=mc.getSingleplayerServer().submit(()->{var p=player(mc);p.getInventory().setItem(0,ItemStack.EMPTY);p.containerMenu.broadcastChanges();});stage=7;return;}
            if(stage==7){if(ticks<12)return;click(mc,menu,249);stage=8;ticks=0;return;}
            if(stage==8){if(ticks<18||menu.furnace().hasResult())return;pending=mc.getSingleplayerServer().submit(()->{var p=player(mc);var d=new BatchJournal(p.server).read(UUID.fromString(control.get("batch").getAsString()));check(p.getMainHandItem().getCount()==quantity&&PillSnapshot.matches(p.getMainHandItem(),d),"Partial/incorrect native delivery");setupEating(mc,d);});stage=9;return;}
            if(stage==9){if(ticks<15)return;check(mc.player.getMainHandItem().is(WuXingItems.V5_PILL.get()),"Client inventory did not receive V5 pill");mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);stage=10;ticks=0;return;}
            if(stage==10){if(ticks>=18)mc.options.keyUse.setDown(false);if(ticks<28)return;capture("native-eating-result");pending=mc.getSingleplayerServer().submit(()->{
                checkDose(mc,1);var p=player(mc);check(!PillRuntime.canUse(p,p.getMainHandItem()),"Shared cooldown absent after real eating");
                try{Files.writeString(CONTROL,new GsonBuilder().setPrettyPrinting().create().toJson(control));}catch(Exception e){throw new RuntimeException(e);}
            });stage=11;return;}
            if(stage==11){mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));stage=12;ticks=0;return;}
            if(stage==12){if(ticks<12)return;capture("inventory-after-first-dose");stage=90;ticks=0;return;}
        }catch(Throwable e){com.mojang.logging.LogUtils.getLogger().error("V5_EFFECT_ACCEPTANCE",e);finish(mc,false,e.toString());}
    }
    private static net.minecraft.server.level.ServerPlayer player(Minecraft mc){return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());}
    private static AlchemyFurnaceBlockEntity furnace(Minecraft mc){return (AlchemyFurnaceBlockEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS);}
    private static void prepare(Minecraft mc){
        var p=player(mc);var level=p.server.overworld();p.setNoGravity(true);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(level,.5,201,-3,0,0);
        if(!RESUME){
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<6;y++)level.setBlockAndUpdate(POS.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(POS,WuXingBlocks.ALCHEMY_FURNACE.get().defaultBlockState());CoreStructureBuilder.build(p,POS);AlchemyFurnaceStructure.refreshAround(level,POS);var f=furnace(mc);var n=f.saveWithoutMetadata();n.putInt("SpiritFuel",f.spiritCapacity());f.load(n);
            var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4);HerbAge.setYears(herb,1500);f.setItem(0,herb);f.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));GeneratedMod.data(p).wuxingxiantu$learnFire(1);cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.unlock(p);
            p.setGameMode(GameType.SURVIVAL);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.getInventory().selected=0;net.minecraftforge.network.NetworkHooks.openScreen(p,f.menuProvider(),POS);
        }
    }
    private static void setupEating(Minecraft mc,CompoundTag doc){
        var p=player(mc);p.closeContainer();p.getInventory().selected=0;p.setHealth(3);p.getFoodData().setFoodLevel(10);p.getFoodData().setSaturation(0);GeneratedMod.data(p).wuxingxiantu$setAura(0);GeneratedMod.sync(p);
        var result=PillSnapshot.read(doc.getCompound("PillResult"));expectedMana=result.effects().stream().filter(e->e.id()==AbilityCompiler.Kind.mana).mapToLong(AbilityCompiler.Spec::magnitudeMilli).sum()/1000F;expectedHealth=3+result.effects().stream().filter(e->e.id()==AbilityCompiler.Kind.heal).mapToLong(AbilityCompiler.Spec::magnitudeMilli).sum()/1000F;
        var item=p.getMainHandItem();check(item.getHoverName().getString().contains("品")&&cn.blockforge.generated.generatedmod.alchemy.PillData.read(item)==null,"V5 name stacked legacy ranks");check(item.getTooltipLines(p,TooltipFlag.NORMAL).stream().anyMatch(c->c.getString().contains("预算")),"Missing actual public effect tooltip");p.containerMenu.broadcastChanges();
    }
    private static void checkDose(Minecraft mc,int used){
        var p=player(mc);var journal=new BatchJournal(p.server);var d=journal.read(UUID.fromString(control.get("batch").getAsString()));
        check(d.getInt("DosesUsed")==used&&p.getMainHandItem().getCount()==quantity-used,"Native eating did not debit exactly one dose: used="+d.getInt("DosesUsed")+" count="+p.getMainHandItem().getCount());
        check(Math.abs(p.getHealth()-expectedHealth)<.05F,"Actual continuous heal did not match compiled value: "+p.getHealth()+" expected "+expectedHealth);
        check(GeneratedMod.data(p).wuxingxiantu$getAura()+.01F>=Math.min(expectedMana,GeneratedMod.data(p).wuxingxiantu$getMaxMana()),"Actual continuous mana restoration missing");
        float mp=GeneratedMod.data(p).wuxingxiantu$getAura();float hp=p.getHealth();var item=V5Batch.result(d);item.getItem().finishUsingItem(item,p.serverLevel(),p);PillRuntime.recover(p);PillRuntime.recover(p);check(p.getHealth()==hp&&GeneratedMod.data(p).wuxingxiantu$getAura()==mp&&journal.read(d.getUUID("Id")).getInt("DosesUsed")==used,"Finish/recovery replay granted another effect");check(journal.pendingDoses(p.getUUID()).isEmpty(),"Dose ack remained pending");
        try{Files.writeString(OUT.resolve("consumed-dose-"+used+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("used",used,"remaining",quantity-used,"grade",grade,"healthBefore",3,"healthAfter",p.getHealth(),"expectedHealth",expectedHealth,"expectedMana",expectedMana,"actualMana",mp,"replayUnchanged",true)));}catch(Exception e){throw new RuntimeException(e);}
    }
    private static void click(Minecraft mc,AlchemyFurnaceMenu menu,int id){mc.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void capture(String name){queued.add(name);}
    static void render(Minecraft mc){if(done)return;try{while(!queued.isEmpty()){String name=queued.remove();check(org.lwjgl.opengl.GL11.glGetError()==0,"GL error");try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}captures.add(name);}}catch(Throwable e){finish(mc,false,e.toString());}}
    private static void finish(Minecraft mc,boolean completed,String error){if(done)return;done=true;mc.options.keyUse.setDown(false);try{Files.createDirectories(OUT);var report=new Gson().toJsonTree(Map.of("passed",error==null,"completed",completed,"resume",RESUME,"captures",captures,"checkedTicks",frames,"captureViolations",violations,"blockedGrabs",V5ClientAcceptance.blockedGrabs,"grade",grade,"quantity",quantity,"error",error==null?"":error)).getAsJsonObject();report.addProperty("gui3Scale",gui3Scale);Files.writeString(OUT.resolve(RESUME?"resume-result.json":"start-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
