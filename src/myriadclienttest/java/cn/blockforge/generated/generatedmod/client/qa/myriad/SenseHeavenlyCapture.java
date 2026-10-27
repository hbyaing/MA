package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import java.nio.file.*;
import java.lang.reflect.Field;
import java.util.*;
import com.google.gson.GsonBuilder;

/** Isolated native UI fixtures; mock catalogue/state, production renderers and controls. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SenseHeavenlyCapture {
    static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.senseHeavenlyCapture");
    static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("ui");
    static final String[] CASES={"heavenly-normal","heavenly-hover","heavenly-disabled","heavenly-focus","heavenly-search","heavenly-scale3","heavenly-scale4","heavenly-refining","heavenly-aux-chosen","heavenly-page2","heavenly-no-match","heavenly-many-materials","sense-normal","sense-hover","sense-closed","sense-focus","sense-channel","sense-scale3","sense-scale4","sense-locked","sense-low-power","sense-narrow","sense-survey","sense-cooldown"};
    static boolean opened,prepared,finished;static int stage,tick,auxiliaryState;static final long START=System.nanoTime();
    static Screen screen;static AbstractWidget target;static List<Button> buttons;
    static final List<Map<String,Object>> SCALES=new ArrayList<>();
    static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    static CompoundTag state(){var n=new CompoundTag();n.putBoolean("SenseOpen",stage!=14);n.putFloat("Sense",stage==20?2:3600);n.putInt("Capacity",3600);n.putInt("Radius",64);n.putInt("Realm",8);if(stage==16){n.putInt("Channel",2);n.putInt("Remaining",30);}if(stage==19){n.putString("Lock","玄铁傀儡");}if(stage==23)n.putInt("ScanCooldown",60);return n;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;GLFW.glfwHideWindow(mc.getWindow().getWindow());
            if((System.nanoTime()-START)/1e9>360)throw new AssertionError("UI capture timeout stage="+stage);
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses user's loaded world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1600,1200);mc.options.languageCode="zh_cn";mc.getLanguageManager().setSelected("zh_cn");mc.reloadResourcePacks();mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("sense-heavenly-ui-"+System.currentTimeMillis(),new LevelSettings("Isolated sense and furnace UI QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7217,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.getOverlay()!=null)return;
            if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();mc.getWindow().setWindowed(stage==17?2400:stage==18?3200:1600,stage==17?1440:stage==18?1600:1200);mc.options.guiScale().set(stage==5||stage==17?3:stage==6||stage==18?4:2);mc.resizeDisplay();setup(mc);prepared=true;tick=0;}
            if(stage>=12)PlayerProgressionClient.state=state();
            if(stage==8&&auxiliaryState==2)target.active=false;
            if(stage==22){Class<?> type=cn.blockforge.generated.generatedmod.divinesurvey.client.SurveyClient.class;var active=type.getDeclaredField("active");active.setAccessible(true);active.setBoolean(null,true);var dimension=type.getDeclaredField("dimension");dimension.setAccessible(true);dimension.set(null,mc.level.dimension().location().toString());}
            double scale=stage<12?(Float)field(screen,"uiScale"):1;
            double x=-50,y=-50;if(stage==1||stage==13||(stage==8&&auxiliaryState==0)){x=(target.getX()+target.getWidth()/2d)*scale;y=(target.getY()+target.getHeight()/2d)*scale;if(stage==13){x+=(Integer)field(screen,"contentX");y+=(Integer)field(screen,"contentY")-(Integer)field(screen,"scroll");}}
            for(String axis:new String[]{"xpos","ypos"}){var f=MouseHandler.class.getDeclaredField(axis);f.setAccessible(true);f.setDouble(mc.mouseHandler,axis.equals("xpos")?x*mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth():y*mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight());}
            // Reuse one live screen across incompatible layouts to expose stale geometry/text.
            if(stage==0&&tick==6)((EditBox)field(screen,"search")).setValue("无此丹方");
            if(stage==0&&tick==12)((EditBox)field(screen,"search")).setValue("");
            tick++;
        }catch(Throwable error){fail(mc,error);}
    }
    static CompoundTag catalogue(){var root=new CompoundTag();root.putUUID("Token",UUID.randomUUID());var rows=new ListTag();for(int i=0;i<156;i++){
        var n=new CompoundTag();n.putString("Recipe","wuxingxiantu:qa_"+i);n.putString("Mode",stage==7?"refining":"alchemy");n.putInt("Cost",1188);n.putInt("Duration",1730);n.putInt("Failure",2);n.putString("Reason",stage==2?"背包材料不足。":"");
        var result=new ItemStack(stage==7?Items.DIAMOND_SWORD:WuXingItems.AURA_PILL.get());result.setHoverName(Component.literal(stage==7?"玄铁长剑":new String[]{"上品回灵丹","下品回灵丹","中品回灵丹","极品回灵丹","上品狂暴丹","下品狂暴丹"}[i%6]));n.put("Result",result.save(new CompoundTag()));var ingredients=new ListTag();for(int j=0;j<(stage==11?12:4);j++)ingredients.add(new ItemStack(new Item[]{Items.LILY_OF_THE_VALLEY,Items.BLUE_ORCHID,Items.REDSTONE,Items.WHEAT}[j%4],new int[]{6,6,3,4}[j%4]).save(new CompoundTag()));n.put("Ingredients",ingredients);rows.add(n);
    }root.put("Rows",rows);return root;}
    @SuppressWarnings("unchecked") static void setup(Minecraft mc)throws Exception{
        mc.player.getInventory().clearContent();
        if(stage<12){
            for(int i=0;i<3;i++){var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("wuxingxiantu",new String[]{"herb_xingchen","herb_huanling","herb_fengling"}[i])));HerbAge.setYears(stack,2500);mc.player.getInventory().setItem(i,stack);}
            screen=new HeavenlyFurnaceScreen(catalogue());mc.setScreen(screen);target=(AbstractWidget)field(screen,"start");
            if(stage==4||stage==10){var search=(EditBox)field(screen,"search");search.setValue(stage==4?"上品":"无此丹方");if(stage==4){screen.setFocused(search);search.setFocused(true);search.moveCursorToEnd();search.setHighlightPos(0);}}
            if(stage==8){var aux=HeavenlyFurnaceScreen.class.getDeclaredMethod("cycleAuxiliary",int.class);aux.setAccessible(true);for(int i=0;i<3;i++)aux.invoke(screen,i);int[] slots=(int[])field(screen,"auxiliarySlots");if(Arrays.stream(slots).anyMatch(slot->slot<0)||Arrays.stream(slots).distinct().count()!=3)throw new AssertionError("Auxiliary selection did not use three real inventory slots");target=(AbstractWidget)((List<?>)field(screen,"auxiliaryButtons")).get(0);if(auxiliaryState==1){screen.setFocused(target);target.setFocused(true);}}
            if(stage==9){var next=(Button)field(screen,"next");next.onPress();}
            if(stage==3){screen.setFocused(target);target.setFocused(true);}
        }else{
            PlayerProgressionClient.state=state();
            if(stage==21){screen=new PlayerProgressionPage(true);mc.setScreen(screen);screen.init(mc,276,410);}else{screen=new CultivationMainScreen(mc.player,"sense");mc.setScreen(screen);}
            Object page=stage==21?screen:field(screen,"page");buttons=(List<Button>)field(page,"actions");target=buttons.get(1);
            if(stage==15){((Screen)page).setFocused(target);target.setFocused(true);}
        }
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!prepared||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try{
            if(stage==0&&tick==10){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve("heavenly-live-no-match.png"));}}
            if(tick<22)return;
            if(stage==1&&!target.isHovered())throw new AssertionError("Natural hover missed furnace start");
            if(stage==13&&!target.isHovered())throw new AssertionError("Natural hover missed sense card action");
            SCALES.add(Map.of("case",CASES[stage],"configuredGuiScale",mc.options.guiScale().get(),"actualGuiScale",mc.getWindow().getGuiScale(),"width",mc.getWindow().getWidth(),"height",mc.getWindow().getHeight()));
            if((stage==17||stage==18)&&mc.getWindow().getGuiScale()!=Math.max(1,Math.min(mc.options.guiScale().get(),Math.min(mc.getWindow().getWidth()/800,mc.getWindow().getHeight()/360))))throw new AssertionError("Expected actual GUI scale not applied: stage="+stage+" actual="+mc.getWindow().getGuiScale()+" framebuffer="+mc.getWindow().getWidth()+"x"+mc.getWindow().getHeight());
            int gl=GL11.glGetError();if(gl!=0)throw new AssertionError("GL error "+gl);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve((stage==8&&auxiliaryState>0?"heavenly-aux-"+(auxiliaryState==1?"focus":"disabled"):CASES[stage])+".png"));}
            if(stage==8&&auxiliaryState<2){auxiliaryState++;prepared=false;return;}
            if(++stage==CASES.length){PointerSafety.verify();Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("scales",SCALES,"passed",true,"cases",CASES,"pointer",PointerSafety.snapshot(),"extraCases",List.of("heavenly-aux-focus","heavenly-aux-disabled"),"fixture","Mock data for layout; real gameplay tested separately")));finished=true;mc.stop();}else prepared=false;
        }catch(Throwable error){fail(mc,error);}
    }
    static void fail(Minecraft mc,Throwable error){try{Files.writeString(OUT.resolve("failure.txt"),error.toString());Files.writeString(OUT.resolve("result.json"),new GsonBuilder().create().toJson(Map.of("passed",false,"stage",stage,"error",error.toString())));}catch(Exception ignored){}error.printStackTrace();finished=true;mc.stop();}
}
