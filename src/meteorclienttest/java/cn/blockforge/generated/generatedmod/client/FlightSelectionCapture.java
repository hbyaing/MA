package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals;
import cn.blockforge.generated.generatedmod.realmflight.*;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

/** DEV only: real inventory widgets, mouse input, persistence and packet round trips. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FlightSelectionCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.flightSelectionCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.flightSelectionOutput","flight-selection")).toAbsolutePath();
    private static final long START=System.nanoTime();
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>(),CAPTURES=new ArrayList<>(),INPUTS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>(),GL_ERRORS=new ArrayList<>();
    private static CompletableFuture<?> serverWork;
    private static CompletableFuture<Map<String,Object>> serverSample;
    private static Map<String,Object> latestServer;
    private static boolean opened,initialized,finished,restored,savedAnimation,savedHorizontal;
    private static int savedPreference,stage,stageTicks,totalTicks,flightTicks;
    private static String pending;
    private static Method serverSnapshot;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event) {
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>300)throw new IllegalStateException("Selection fixture 300-second watchdog");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened=true;Files.createDirectories(OUTPUT);
                mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(2);mc.options.renderDistance().set(6);mc.options.simulationDistance().set(3);mc.options.framerateLimit().set(60);mc.options.hideGui=false;mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("flight-selection-"+System.currentTimeMillis(),new LevelSettings("Realm effect settings verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            totalTicks++;stageTicks++;
            if(serverSample!=null&&serverSample.isDone()){latestServer=serverSample.join();serverSample=null;}
            sampleServer(mc);
            if(!initialized) {
                initialized=true;savedAnimation=HudConfig.REALM_FLIGHT_ANIMATION.get();savedHorizontal=HudConfig.REALM_FLIGHT_HORIZONTAL.get();savedPreference=HudConfig.REALM_FLIGHT_VISUAL_REALM.get();
                HudConfig.REALM_FLIGHT_ANIMATION.set(true);HudConfig.REALM_FLIGHT_HORIZONTAL.set(true);HudConfig.REALM_FLIGHT_VISUAL_REALM.set(0);
                serverSnapshot=RealmFlightTracking.class.getDeclaredMethod("packet",ServerPlayer.class);serverSnapshot.setAccessible(true);
                clearKeys(mc);mc.setScreen(null);setRealm(mc,8,true);return;
            }
            if(serverWork!=null){if(!serverWork.isDone())return;serverWork.join();serverWork=null;}
            if(pending!=null)return;
            if(stageTicks>200)throw new IllegalStateException("Selection stage timed out: "+stage+", actual server="+latestServer);
            switch(stage) {
                case 0 -> {if(dataRealm(mc)==8&&mc.player.getAbilities().mayfly&&mc.player.onGround()){openInventory(mc);advance();}}
                case 1 -> {
                    String[] pages={"背包","内视","功法","灵剑","成就","设置"};
                    if(uiPageIndex<pages.length){clickNavigation(mc,pages[uiPageIndex]);capture("layout-page-"+uiPageIndex+".png");uiPageIndex++;}
                    else advance();
                }
                case 2 -> {clickPage(mc,"遁光");advance();}
                case 3 -> {checkMenu(mc,8);capture("dixian-settings-top.png");advance();}
                case 4 -> {scrollBottom(mc);capture("dixian-settings-bottom.png");advance();}
                case 5 -> {clickChoice(mc,4);check("real_click_saved_huashen",HudConfig.REALM_FLIGHT_VISUAL_REALM.get()==4,"After production mouseClicked config="+HudConfig.REALM_FLIGHT_VISUAL_REALM.get());capture("dixian-selected-huashen.png");advance();}
                case 6 -> {if(roundTripReady(mc,8,4,4)){checkRoundTrip(mc,8,4);checkSavedConfig(mc,4);closeScreen(mc);flightTicks=0;advance();}}
                case 7 -> {driveFlight(mc,true);if(flightTicks>=90&&actualFlight(mc,8,4)){capture("dixian-flight-huashen-selected.png");clearKeys(mc);advance();}}
                case 8 -> {openSettings(mc);advance();}
                case 9 -> {clickChoice(mc,8);capture("dixian-selected-dixian.png");advance();}
                case 10 -> {if(roundTripReady(mc,8,8,8)){checkRoundTrip(mc,8,8);closeScreen(mc);flightTicks=0;advance();}}
                case 11 -> {driveFlight(mc,false);if(flightTicks>=60&&actualFlight(mc,8,8)){capture("dixian-flight-dixian-selected.png");clearKeys(mc);advance();}}
                case 12 -> {openSettings(mc);advance();}
                case 13 -> {clickChoice(mc,0);capture("dixian-auto-selected.png");advance();}
                case 14 -> {if(roundTripReady(mc,8,8,0)){checkRoundTrip(mc,8,8);checkSavedConfig(mc,0);closeScreen(mc);setRealm(mc,4,false);advance();}}
                case 15 -> {if(dataRealm(mc)==4&&latestServer!=null&&((Number)latestServer.get("realm")).intValue()==4){openSettings(mc);advance();}}
                case 16 -> {checkMenu(mc,4);capture("huashen-settings-top.png");advance();}
                case 17 -> {scrollBottom(mc);capture("huashen-settings-bottom.png");advance();}
                case 18 -> {clickChoice(mc,3);capture("huashen-selected-yuanying.png");advance();}
                case 19 -> {if(roundTripReady(mc,4,3,3)){checkRoundTrip(mc,4,3);Network.CHANNEL.sendToServer(new RealmFlightModePacket(true,8));INPUTS.add(Map.of("action","actual_untrusted_packet","requestedPreference",8,"actualRealm",4));advance();}}
                case 20 -> {if(serverEquals(4,4,4)){check("actual_server_rejects_locked_effect",true,"Requested 地仙 8 at actual 化神 4, server stores 4 and snapshot visual 4");clickChoice(mc,0);capture("huashen-auto-after-untrusted.png");advance();}}
                case 21 -> {if(roundTripReady(mc,4,4,0)){checkRoundTrip(mc,4,4);checkSavedConfig(mc,0);closeScreen(mc);openSettings(mc);advance();}}
                case 22 -> {check("preference_after_reopen",HudConfig.REALM_FLIGHT_VISUAL_REALM.get()==0,"Reopened production page preserves auto");capture("huashen-reopened-auto.png");advance();}
                case 23 -> {clearKeys(mc);closeScreen(mc);check("complete_actual_images",CAPTURES.size()==18,"Actual screenshots="+CAPTURES.size()+"; expected=18");finish();}
                default -> throw new IllegalStateException("Unknown stage "+stage);
            }
        } catch(Throwable failure) {FAILURES.add("Fixture exception: "+failure);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_SELECTION_CAPTURE",failure);finish();}
    }
    private static int uiPageIndex;
    private static int dataRealm(Minecraft mc){return GeneratedMod.data(mc.player).wuxingxiantu$getRealm();}
    private static void advance(){stage++;stageTicks=0;latestServer=null;}
    private static void setRealm(Minecraft mc,int realm,boolean initial) {
        UUID id=mc.player.getUUID();serverWork=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);var level=p.serverLevel();
            d.wuxingxiantu$setRealm(realm);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);
            p.setInvulnerable(true);p.getAbilities().flying=false;p.onUpdateAbilities();
            if(initial) {
                for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)level.setBlock(new BlockPos(x,130,z),Blocks.STONE.defaultBlockState(),2);
                for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)for(int y=131;y<=137;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                p.teleportTo(0,131,0);p.setDeltaMovement(Vec3.ZERO);p.setYRot(-90);p.setXRot(0);level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);level.setWeatherParameters(6000,0,false,false);
            }
            GeneratedMod.sync(p);
        });
    }
    private static void sampleServer(Minecraft mc) {
        if(serverSample!=null||serverSnapshot==null)return;UUID id=mc.player.getUUID();
        serverSample=mc.getSingleplayerServer().submit(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var row=new LinkedHashMap<String,Object>();
            try {var packet=(RealmFlightVisualPacket)serverSnapshot.invoke(null,p);row.put("realm",packet.realm());row.put("visualRealm",RealmFlightSelection.resolve(packet.realm(),packet.visualRealm()));row.put("packetPreference",packet.visualRealm());row.put("active",packet.active());row.put("preferredRealm",p.getPersistentData().getInt(RealmFlightModePacket.VISUAL_REALM));row.put("flying",p.getAbilities().flying);row.put("mana",GeneratedMod.data(p).wuxingxiantu$getCurrentMana());}
            catch(Exception error){throw new IllegalStateException(error);}return row;
        });
    }
    private static boolean serverEquals(int realm,int visual,int preferred){return latestServer!=null&&((Number)latestServer.get("realm")).intValue()==realm&&((Number)latestServer.get("visualRealm")).intValue()==visual&&((Number)latestServer.get("preferredRealm")).intValue()==preferred;}
    private static boolean roundTripReady(Minecraft mc,int realm,int visual,int preferred)throws Exception {
        if(!serverEquals(realm,visual,preferred))return false;
        var packet=state(mc);return packet==null||packet.realm()==realm&&packet.visualRealm()==preferred;
    }
    private static void openInventory(Minecraft mc) {
        clearKeys(mc);mc.options.hideGui=false;mc.setScreen(new InventoryScreen(mc.player));
        check("production_inventory_replacement_stage_"+stage,mc.screen instanceof CultivationMainScreen,"Opened exact vanilla InventoryScreen, Forge Open event returned "+mc.screen.getClass().getName());
    }
    private static void openSettings(Minecraft mc)throws Exception {openInventory(mc);clickNavigation(mc,"设置");clickPage(mc,"遁光");}
    private static CultivationMainScreen main(Minecraft mc){if(!(mc.screen instanceof CultivationMainScreen screen))throw new IllegalStateException("Expected production inventory");return screen;}
    private static CultivationPage page(Minecraft mc)throws Exception{return (CultivationPage)field(CultivationMainScreen.class,main(mc),"page");}
    @SuppressWarnings("unchecked")
    private static void clickNavigation(Minecraft mc,String label)throws Exception {
        var widgets=(List<AbstractWidget>)field(CultivationMainScreen.class,main(mc),"navButtons");
        var button=widgets.stream().filter(w->w.getMessage().getString().equals(label)).findFirst().orElseThrow();
        click(mc,button.getX()+button.getWidth()/2.0,button.getY()+button.getHeight()/2.0,label);
    }
    private static AbstractWidget pageWidget(Minecraft mc,String label,boolean choice)throws Exception {
        for(var child:page(mc).children())if(child instanceof AbstractWidget w) {
            String text=w.getMessage().getString();
            if(choice?text.contains(label)&&!(w instanceof CultivationUi.Tab):text.equals(label))return w;
        }
        throw new IllegalStateException("Missing actual production widget: "+label+", labels="+labels(mc));
    }
    private static void clickPage(Minecraft mc,String label)throws Exception{clickPageWidget(mc,pageWidget(mc,label,false),label);}
    private static void clickChoice(Minecraft mc,int realm)throws Exception{String label=RealmFlightSelection.label(realm);clickPageWidget(mc,pageWidget(mc,label,true),label);}
    private static void clickPageWidget(Minecraft mc,AbstractWidget widget,String label)throws Exception {
        var screen=main(mc);int contentX=(Integer)field(CultivationMainScreen.class,screen,"contentX"),contentY=(Integer)field(CultivationMainScreen.class,screen,"contentY"),contentH=(Integer)field(CultivationMainScreen.class,screen,"contentH");
        float scale=(Float)field(CultivationMainScreen.class,screen,"pageScale");
        for(int attempts=0;attempts<30;attempts++) {
            int scroll=(Integer)field(CultivationMainScreen.class,screen,"scroll");double top=contentY+(widget.getY()-scroll)*scale,bottom=top+widget.getHeight()*scale;
            if(top>=contentY+1&&bottom<=contentY+contentH-1){click(mc,contentX+(widget.getX()+widget.getWidth()/2.0)*scale,top+widget.getHeight()*scale/2,label);return;}
            screen.mouseScrolled(contentX+20,contentY+contentH/2.0,top<contentY?1:-1);INPUTS.add(Map.of("action","actual_mouseScrolled","delta",top<contentY?1:-1,"stage",stage));
        }
        throw new IllegalStateException("Widget cannot be brought fully into viewport: "+label);
    }
    private static void click(Minecraft mc,double x,double y,String label) {
        boolean accepted=main(mc).mouseClicked(x,y,0);main(mc).mouseReleased(x,y,0);
        INPUTS.add(Map.of("action","actual_screen_mouseClicked","label",label,"x",x,"y",y,"accepted",accepted,"stage",stage));
        if(!accepted)throw new IllegalStateException("Production widget click rejected: "+label);
    }
    private static void scrollBottom(Minecraft mc)throws Exception {
        var screen=main(mc);int x=(Integer)field(CultivationMainScreen.class,screen,"contentX"),y=(Integer)field(CultivationMainScreen.class,screen,"contentY");
        screen.mouseScrolled(x+30,y+80,-100);INPUTS.add(Map.of("action","actual_mouseScrolled","delta",-100,"stage",stage));
    }
    private static List<String> labels(Minecraft mc)throws Exception {var labels=new ArrayList<String>();if(page(mc)==null)return labels;for(var child:page(mc).children())if(child instanceof AbstractWidget w)labels.add(w.getMessage().getString());return labels;}
    private static void checkMenu(Minecraft mc,int actualRealm)throws Exception {
        var labels=labels(mc);check("auto_choice_realm_"+actualRealm,labels.stream().anyMatch(t->t.contains("随当前境界")),labels.toString());
        for(int realm=3;realm<=8;realm++){final String name=RealmFlightSelection.label(realm);boolean exists=labels.stream().anyMatch(t->t.contains(name));check("unlocked_choice_"+actualRealm+"_"+realm,exists==(realm<=actualRealm),"Expected "+(realm<=actualRealm?"available":"absent")+" "+name+"; labels="+labels);}
        var options=new ArrayList<AbstractWidget>();for(var child:page(mc).children())if(child instanceof AbstractWidget w&&!(w instanceof CultivationUi.Tab))options.add(w);
        boolean noOverlap=true;for(int a=0;a<options.size();a++)for(int b=a+1;b<options.size();b++){var wa=options.get(a);var wb=options.get(b);if(wa.getX()<wb.getX()+wb.getWidth()&&wa.getX()+wa.getWidth()>wb.getX()&&wa.getY()<wb.getY()+wb.getHeight()&&wa.getY()+wa.getHeight()>wb.getY())noOverlap=false;}
        check("actual_controls_no_overlap_realm_"+actualRealm,noOverlap,"Read-only production widget bounds, same page space; scrolling captures top/bottom");
    }
    private static void checkSavedConfig(Minecraft mc,int preference)throws Exception {
        Path config=mc.gameDirectory.toPath().resolve("config/wuxingxiantu-client.toml");
        if(!Files.isRegularFile(config)) {
            try(var paths=Files.list(mc.gameDirectory.toPath().resolve("config"))){config=paths.filter(p->p.getFileName().toString().contains("wuxing")&&p.getFileName().toString().endsWith(".toml")).filter(p->{try{return Files.readString(p).contains("realmFlightVisualRealm");}catch(Exception ignored){return false;}}).findFirst().orElseThrow();}
        }
        String text=Files.readString(config);boolean persisted=text.lines().anyMatch(line->line.strip().equals("realmFlightVisualRealm = "+preference));
        check("actual_config_saved_stage_"+stage,persisted,"Read saved production config "+config+"; realmFlightVisualRealm="+preference);
    }
    private static void checkRoundTrip(Minecraft mc,int actualRealm,int visual)throws Exception {
        check("actual_server_round_trip_stage_"+stage,dataRealm(mc)==actualRealm&&serverEquals(actualRealm,visual,HudConfig.REALM_FLIGHT_VISUAL_REALM.get()),"Actual realm unchanged "+dataRealm(mc)+"; config="+HudConfig.REALM_FLIGHT_VISUAL_REALM.get()+"; actual integrated-server snapshot="+latestServer);
        var packet=state(mc);if(packet!=null)check("received_visual_packet_stage_"+stage,packet.realm()==actualRealm&&RealmFlightSelection.resolve(packet.realm(),packet.visualRealm())==visual,"Read actual FLIGHTS state after production S2C delivery="+packet);
    }
    private static void driveFlight(Minecraft mc,boolean takeoff) {
        flightTicks++;mc.player.setYRot(-90);mc.player.setXRot(0);mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);mc.options.hideGui=true;
        mc.options.keyJump.setDown(takeoff&&(flightTicks==20||flightTicks==24||flightTicks>=30&&flightTicks<=45));mc.options.keyUp.setDown(flightTicks>=50||!takeoff);mc.options.keySprint.setDown(false);mc.options.keyShift.setDown(false);
    }
    private static boolean actualFlight(Minecraft mc,int actualRealm,int visual)throws Exception {
        var packet=state(mc);if(packet==null||!mc.player.getAbilities().flying||!roundTripReady(mc,actualRealm,visual,HudConfig.REALM_FLIGHT_VISUAL_REALM.get())||!Boolean.TRUE.equals(latestServer.get("active"))||RealmFlightEffects.renderedTrailSegments==0)return false;
        var resolver=RealmFlightEffects.class.getDeclaredMethod("visualRealm",net.minecraft.world.entity.player.Player.class,RealmFlightVisualPacket.class);resolver.setAccessible(true);int chosen=(Integer)resolver.invoke(null,mc.player,packet);
        check("actual_render_selection_stage_"+stage,chosen==visual&&dataRealm(mc)==actualRealm&&packet.realm()==actualRealm,"Read actual production renderer resolver="+chosen+"; state="+packet+"; flying="+mc.player.getAbilities().flying+"; actual trail segments="+RealmFlightEffects.renderedTrailSegments);
        return chosen==visual;
    }
    private static RealmFlightVisualPacket state(Minecraft mc)throws Exception {
        var flights=(Map<?,?>)field(RealmFlightEffects.class,null,"FLIGHTS");Object flight=flights.get(mc.player.getUUID());return flight==null?null:(RealmFlightVisualPacket)field(flight.getClass(),flight,"state");
    }
    private static Object field(Class<?> owner,Object instance,String name)throws Exception{Field field=owner.getDeclaredField(name);field.setAccessible(true);return field.get(instance);}
    private static void closeScreen(Minecraft mc){if(mc.screen!=null)mc.screen.onClose();mc.setScreen(null);}
    private static void clearKeys(Minecraft mc){mc.options.keyUp.setDown(false);mc.options.keyRight.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyShift.setDown(false);mc.options.keySprint.setDown(false);}
    private static void capture(String name){pending=name;}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event) {
        if(!ENABLED||finished||!initialized||event.phase!=TickEvent.Phase.END)return;
        int error;while((error=GL11.glGetError())!=GL11.GL_NO_ERROR)GL_ERRORS.add(stage+":"+error);
        if(pending==null)return;var mc=Minecraft.getInstance();if(mc.getOverlay()!=null)return;
        try {
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(pending));}
            var row=new LinkedHashMap<String,Object>();row.put("screenshot",pending);row.put("stage",stage);row.put("actualRealm",dataRealm(mc));row.put("configPreference",HudConfig.REALM_FLIGHT_VISUAL_REALM.get());row.put("actualServer",latestServer);row.put("actualFlightPacket",state(mc));row.put("screenClass",mc.screen==null?null:mc.screen.getClass().getName());row.put("actualFlying",mc.player.getAbilities().flying);row.put("trailSegments",RealmFlightEffects.renderedTrailSegments);
            if(mc.screen instanceof CultivationMainScreen){row.put("actualProductionWidgetLabels",labels(mc));row.put("actualScroll",field(CultivationMainScreen.class,mc.screen,"scroll"));row.put("contentBounds",List.of(field(CultivationMainScreen.class,mc.screen,"contentX"),field(CultivationMainScreen.class,mc.screen,"contentY"),field(CultivationMainScreen.class,mc.screen,"contentW"),field(CultivationMainScreen.class,mc.screen,"contentH")));}
            CAPTURES.add(row);com.mojang.logging.LogUtils.getLogger().info("FLIGHT_SELECTION_IMAGE {}",row);pending=null;write();
        }catch(Throwable failure){FAILURES.add("Capture exception: "+failure);com.mojang.logging.LogUtils.getLogger().error("FLIGHT_SELECTION_FRAME",failure);finish();}
    }
    private static void check(String name,boolean passed,String details){CHECKS.add(Map.of("name",name,"passed",passed,"details",details));if(!passed)FAILURES.add(name+": "+details);}
    private static void write()throws Exception {
        var result=new LinkedHashMap<String,Object>();result.put("fixture","flightSelectionCapture");result.put("result",finished?(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL"):"RUNNING");result.put("scope","Isolated local DEV survival save; exact vanilla inventory Open event, actual production navigation/category/selection mouseClicked and mouseScrolled. Reflection only reads widget coordinates, server snapshot and actual renderer state. Real production network validates one deliberately locked selection. No remote multiplayer/FPS/shader-pack claim.");result.put("captures",CAPTURES);result.put("actualInputEvents",INPUTS);result.put("checks",CHECKS);result.put("failures",FAILURES);result.put("glErrors",GL_ERRORS);result.put("configRestored",restored);result.put("ticks",totalTicks);Files.writeString(OUTPUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(result));
    }
    private static void finish(){if(finished)return;finished=true;try{var mc=Minecraft.getInstance();clearKeys(mc);if(initialized){HudConfig.REALM_FLIGHT_ANIMATION.set(savedAnimation);HudConfig.REALM_FLIGHT_HORIZONTAL.set(savedHorizontal);HudConfig.REALM_FLIGHT_VISUAL_REALM.set(savedPreference);HudConfig.REALM_FLIGHT_ANIMATION.save();HudConfig.REALM_FLIGHT_HORIZONTAL.save();HudConfig.REALM_FLIGHT_VISUAL_REALM.save();restored=HudConfig.REALM_FLIGHT_ANIMATION.get()==savedAnimation&&HudConfig.REALM_FLIGHT_HORIZONTAL.get()==savedHorizontal&&HudConfig.REALM_FLIGHT_VISUAL_REALM.get()==savedPreference;}check("no_GL_errors",GL_ERRORS.isEmpty(),GL_ERRORS.toString());check("DEV_preferences_restored",restored,"Original animation/horizontal/preferred restored");Files.createDirectories(OUTPUT);write();Files.writeString(OUTPUT.resolve("result.txt"),(FAILURES.isEmpty()&&GL_ERRORS.isEmpty()?"PASS":"FAIL")+": "+CAPTURES.size()+" real screenshots, "+CHECKS.size()+" checks, "+INPUTS.size()+" actual mouse/packet inputs; failures="+FAILURES);}catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("FLIGHT_SELECTION_RESULT",error);}Minecraft.getInstance().stop();}
    private FlightSelectionCapture(){}
}
