package cn.blockforge.generated.generatedmod.client.qa.v5;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.research.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.plants.*;
import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraftforge.event.TickEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Actual menus, vanilla interaction/slot/button packets, two JVMs; render-coordinate QA never moves OS pointer. */
final class ResearchTableAcceptance {
    private static final BlockPos POS=new BlockPos(0,200,0),FURNACE=new BlockPos(5,200,5);
    private static final Path OUT=Path.of(System.getProperty("wuxingxiantu.v5Output")).resolveSibling("research-table-client-validation"),CONTROL=Path.of("research-ui-qa.json");
    private static final boolean RESUME=Boolean.getBoolean("wuxingxiantu.v5Resume");private static final long START=System.nanoTime();
    private static int stage,ticks,frames,violations,page,gui3,gui4;private static boolean opened,done;private static JsonObject control;private static CompletableFuture<?> pending;
    private static final ArrayDeque<String> queued=new ArrayDeque<>();private static final List<String> captures=new ArrayList<>();
    static void tick(TickEvent.ClientTickEvent e){if(done||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();frames++;
        try{Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;if(mc.mouseHandler.isMouseGrabbed()||org.lwjgl.glfw.GLFW.glfwGetInputMode(mc.getWindow().getWindow(),org.lwjgl.glfw.GLFW.GLFW_CURSOR)!=org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL)violations++;check((System.nanoTime()-START)/1e9<240,"Timeout stage="+stage);
            if(!opened&&mc.player!=null)throw new AssertionError("Unrelated world refused");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1440,1080);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                if(RESUME){control=JsonParser.parseString(Files.readString(CONTROL)).getAsJsonObject();String world=control.get("world").getAsString();check(world.startsWith("research-ui-"),"Unrelated save refused");mc.createWorldOpenFlows().loadLevel(new net.minecraft.client.gui.screens.TitleScreen(),world);}
                else{control=new JsonObject();String world="research-ui-"+System.currentTimeMillis();control.addProperty("world",world);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);mc.createWorldOpenFlows().createFreshLevel(world,new LevelSettings("Research UI independent acceptance",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(9219,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);}
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;ticks=0;}ticks++;
            if(stage==0){if(ticks<35)return;mc.mouseHandler.grabMouse();check(V5ClientAcceptance.blockedGrabs>0&&violations==0,"Pointer guard failed");pending=mc.getSingleplayerServer().submit(()->prepare(mc));stage=1;return;}
            if(stage==1){if(ticks<20)return;mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(POS.getCenter(),Direction.UP,POS,false));stage=2;ticks=0;return;}
            if(!(mc.screen instanceof ResearchTableScreen)||!(mc.player.containerMenu instanceof ResearchTableMenu menu)){check(ticks<100,"Physical workstation did not open actual menu");return;}
            if(stage==2){if(ticks<12)return;check(menu.slots.size()==39,"Wrong slot count");if(RESUME){check(menu.records()==1&&!menu.specimen().isEmpty()&&menu.lastRecord().contains("灵丹"),"Restart lost personal note, summary or specimen");action(mc,menu,10);stage=20;ticks=0;return;}
                check(menu.specimen().isEmpty()&&!menu.analyzed(),"Empty fixture incorrect");capture("empty-disabled");clickSlot(mc,118,281);stage=201;ticks=0;return;}
            if(stage==201){if(ticks<8)return;check(menu.getCarried().is(WuXingItems.V5_PILL.get()),"Native inventory coordinate did not pick sample");clickSlot(mc,64,88);stage=202;ticks=0;return;}
            if(stage==202){if(ticks<8)return;check(menu.specimen().getCount()==1&&!menu.getCarried().isEmpty(),"Sample coordinate/max-one limit failed");clickSlot(mc,118,281);stage=3;ticks=0;return;}
            if(stage==3){if(ticks<12)return;check(menu.specimen().is(WuXingItems.V5_PILL.get())&&menu.specimen().getCount()==1&&!menu.analyzed(),"Native shift-click did not reserve one sample");capture("sample-unobserved");action(mc,menu,10);stage=4;ticks=0;return;}
            if(stage==4){if(ticks<12)return;check(menu.analyzed(),"Inspection did not authenticate live sample");capture("observed-normal");stage=41;ticks=0;return;}
            if(stage==41){if(ticks<5)return;capture("observed-hover");stage=42;ticks=0;return;}
            if(stage==42){if(ticks<5)return;var button=mc.screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.AbstractWidget w&&w.getMessage().getString().equals("记录结果")).findFirst().orElseThrow();mc.screen.setFocused(button);capture("observed-focus");stage=43;ticks=0;return;}
            if(stage==43){if(ticks<5)return;action(mc,menu,11);stage=5;ticks=0;return;}
            if(stage==5){if(ticks<12)return;check(menu.recorded()&&menu.records()==1,"Record not confirmed by server");capture("recorded-disabled");action(mc,menu,11);pending=mc.getSingleplayerServer().submit(()->{var t=table(mc);check(t.getItem(0).getCount()==1&&t.getItem(1).getCount()==1&&t.getItem(2).getCount()==1&&FormulaKnowledge.researchRecords(player(mc)).size()==1,"Note replay consumed extra supplies or specimen");});stage=6;return;}
            if(stage==6){if(ticks<8)return;page=1;action(mc,menu,page);stage=7;ticks=0;return;}
            if(stage==7){if(ticks<10)return;check(menu.page()==page,"Page packet lost");capture("page-"+page);stage=71;ticks=0;return;}
            if(stage==71){if(ticks<5)return;if(page<5){action(mc,menu,++page);stage=7;ticks=0;return;}action(mc,menu,0);mc.options.guiScale().set(3);mc.resizeDisplay();stage=8;ticks=0;return;}
            if(stage==8){if(ticks<15)return;gui3=(int)mc.getWindow().getGuiScale();check(gui3==3,"GUI3 not active");capture("approved-gui3");stage=9;ticks=0;return;}
            if(stage==9){if(ticks<6)return;pending=mc.getSingleplayerServer().submit(()->{FormulaKnowledge.reset(player(mc));player(mc).containerMenu.broadcastChanges();});stage=10;return;}
            if(stage==10){if(ticks<10)return;check(!menu.unlocked(),"Knowledge gate not synchronized");capture("knowledge-disabled");mc.getWindow().setWindowed(1920,1440);mc.options.guiScale().set(4);mc.resizeDisplay();stage=11;ticks=0;return;}
            if(stage==11){if(ticks<15)return;gui4=(int)mc.getWindow().getGuiScale();check(gui4==4,"GUI4 not active");capture("fit-gui4");clickSlot(mc,64,88);stage=111;ticks=0;return;}
            if(stage==111){if(ticks<8)return;check(menu.specimen().isEmpty()&&menu.getCarried().is(WuXingItems.V5_PILL.get()),"GUI4 native sample hitbox failed");clickSlot(mc,64,88);stage=112;ticks=0;return;}
            if(stage==112){if(ticks<8)return;check(!menu.specimen().isEmpty()&&menu.getCarried().isEmpty(),"GUI4 sample return failed");capture("fit-gui4-input");stage=12;ticks=0;return;}
            if(stage==12){if(ticks<6)return;pending=mc.getSingleplayerServer().submit(()->{FormulaKnowledge.unlock(player(mc));try{Files.writeString(CONTROL,new GsonBuilder().setPrettyPrinting().create().toJson(control));}catch(Exception ex){throw new RuntimeException(ex);}});stage=99;return;}
            if(stage==20){if(ticks<12)return;check(menu.recorded()&&menu.records()==1,"Saved sample was not deduplicated after restart");capture("restored-notes");action(mc,menu,11);stage=21;ticks=0;return;}
            if(stage==21){if(ticks<12)return;pending=mc.getSingleplayerServer().submit(()->check(table(mc).getItem(1).getCount()==1&&table(mc).getItem(2).getCount()==1,"Restart duplicate note charged again"));stage=99;return;}
            if(stage==99){if(ticks<8)return;finish(mc,null);}
        }catch(Throwable failure){com.mojang.logging.LogUtils.getLogger().error("RESEARCH_UI_ACCEPTANCE",failure);finish(mc,failure.toString());}
    }
    private static net.minecraft.server.level.ServerPlayer player(Minecraft mc){return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());}
    private static ResearchTableEntity table(Minecraft mc){return (ResearchTableEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS);}
    private static void prepare(Minecraft mc){var p=player(mc);var l=p.server.overworld();p.setNoGravity(true);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(l,.5,201,-2.5,0,15);if(RESUME)return;
        p.setGameMode(GameType.CREATIVE);p.teleportTo(l,5.5,201,2.5,0,0);l.setBlockAndUpdate(FURNACE,WuXingBlocks.ALCHEMY_FURNACE.get().defaultBlockState());CoreStructureBuilder.build(p,FURNACE);AlchemyFurnaceStructure.refreshAround(l,FURNACE);var furnace=(AlchemyFurnaceBlockEntity)l.getBlockEntity(FURNACE);var saved=furnace.saveWithoutMetadata();saved.putInt("SpiritFuel",furnace.spiritCapacity());furnace.load(saved);var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4);HerbAge.setYears(herb,1500);furnace.setItem(0,herb);furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));GeneratedMod.data(p).wuxingxiantu$learnFire(1);FormulaKnowledge.unlock(p);check(furnace.startV5Batch(p),"Cannot prepare authenticated specimen fixture");var doc=furnace.v5ServerRecord();var journal=new BatchJournal(p.server);
        for(int i=0;i<64&&doc.getString("State").equals("ACTIVE");i++){doc.putLong("LastWorldTick",l.getGameTime()-20);doc=V5Batch.advance(doc,l,FURNACE);journal.write(doc);}furnace.restoreV5();check(furnace.collectResult(p)==1&&p.getInventory().getItem(0).is(WuXingItems.V5_PILL.get()),"Specimen fixture did not use production compiler/delivery");
        l.setBlockAndUpdate(POS,WuXingBlocks.ALCHEMY_RESEARCH_TABLE.get().defaultBlockState());table(mc).setItem(1,new ItemStack(Items.PAPER,2));table(mc).setItem(2,new ItemStack(Items.INK_SAC,2));p.teleportTo(l,.5,201,-2.5,0,15);p.getInventory().selected=0;p.inventoryMenu.broadcastChanges();
    }
    private static void clickSlot(Minecraft mc,int sx,int sy){int x=(mc.getWindow().getGuiScaledWidth()-440)/2+sx+8,y=(mc.getWindow().getGuiScaledHeight()-300)/2+sy+8;mc.screen.mouseClicked(x,y,0);mc.screen.mouseReleased(x,y,0);}
    private static void action(Minecraft mc,ResearchTableMenu m,int id){mc.gameMode.handleInventoryButtonClick(m.containerId,id);}
    private static void capture(String n){queued.add(n);}private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    static void render(Minecraft mc){if(done)return;try{while(!queued.isEmpty()){var name=queued.remove();if(name.equals("observed-hover")){var g=new net.minecraft.client.gui.GuiGraphics(mc,mc.renderBuffers().bufferSource());double scale=Math.min(1,Math.min((mc.getWindow().getGuiScaledWidth()-12D)/440,(mc.getWindow().getGuiScaledHeight()-12D)/300));int x=(int)((mc.getWindow().getGuiScaledWidth()-440*scale)/2+200*scale),y=(int)((mc.getWindow().getGuiScaledHeight()-300*scale)/2+205*scale);mc.screen.render(g,x,y,0);g.flush();}
                check(org.lwjgl.opengl.GL11.glGetError()==0,"GL error");try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));if(name.equals("approved-gui3")){double scale=mc.getWindow().getGuiScale();int x=(image.getWidth()-(int)(440*scale))/2,y=(image.getHeight()-(int)(300*scale))/2;
                    int[][] boxes={{0,0,80,40},{360,0,80,40},{0,260,80,40},{360,260,80,40},{140,55,149,137},{78,219,263,79}};for(int i=0;i<boxes.length;i++){var b=boxes[i];int w=(int)(b[2]*scale),h=(int)(b[3]*scale);try(var crop=new com.mojang.blaze3d.platform.NativeImage(w,h,false)){for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++)crop.setPixelRGBA(xx,yy,image.getPixelRGBA(x+(int)(b[0]*scale)+xx,y+(int)(b[1]*scale)+yy));crop.writeToFile(OUT.resolve("detail-"+i+".png"));}}
                }}captures.add(name);
            }}catch(Throwable ex){finish(mc,ex.toString());}}
    private static void finish(Minecraft mc,String error){if(done)return;done=true;try{Files.createDirectories(OUT);Files.writeString(OUT.resolve(RESUME?"resume-result.json":"start-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",error==null,"resume",RESUME,"captures",captures,"frames",frames,"captureViolations",violations,"blockedGrabs",V5ClientAcceptance.blockedGrabs,"gui3",gui3,"gui4",gui4,"error",error==null?"":error)));}catch(Exception ex){throw new RuntimeException(ex);}mc.stop();}
}
