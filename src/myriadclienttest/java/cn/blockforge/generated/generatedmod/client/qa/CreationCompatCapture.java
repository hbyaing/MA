package cn.blockforge.generated.generatedmod.client.qa;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.client.qa.myriad.PointerSafety;
import com.google.gson.GsonBuilder;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;

/** Production sense page/addon buttons with internal input only and the existing pointer guard. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CreationCompatCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.chuangshiCompatCapture");
    private static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("creation-"+System.currentTimeMillis());
    private static final long START=System.nanoTime();
    private static boolean opened,prepared,finished;
    private static int stage,frames;
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static Object field(Object object,String name)throws Exception {
        var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
    }
    private static void scroll(CultivationMainScreen host,int value)throws Exception {
        var f=host.getClass().getDeclaredField("scroll");f.setAccessible(true);f.set(host,value);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            GLFW.glfwHideWindow(mc.getWindow().getWindow());
            if((System.nanoTime()-START)/1e9>360)throw new AssertionError("Creation UI timeout");
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses an existing user world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(1);mc.resizeDisplay();
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("creation-compat-"+System.currentTimeMillis(),new LevelSettings("Creation isolated QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
            if(!prepared){
                mc.mouseHandler.grabMouse();PointerSafety.verify();
                CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(),true,mc.level.registryAccess());
                var id=new net.minecraft.resources.ResourceLocation("wuxingchuangshi","shanhe_sheji_tu");
                var item=BuiltInRegistries.ITEM.get(id);int count=0;
                for(var tab:BuiltInRegistries.CREATIVE_MODE_TAB) {
                    var key=BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
                    if(!key.getNamespace().equals("wuxingxiantu"))continue;
                    if(tab.getDisplayItems().stream().anyMatch(s->s.is(item))){count++;if(!key.getPath().equals("misc"))throw new AssertionError("Addon leaked into "+key);}
                }
                if(count!=1)throw new AssertionError("Addon misc count "+count);
                mc.setScreen(new CultivationMainScreen(mc.player,"sense"));prepared=true;
            }
            int scale=stage<4?1:2;
            if(mc.options.guiScale().get()!=scale){mc.options.guiScale().set(scale);mc.resizeDisplay();frames=0;}
        }catch(Throwable failure){FAILURES.add(failure.toString());com.mojang.logging.LogUtils.getLogger().error("CREATION_COMPAT_CAPTURE",failure);finish(mc);}
    }
    @SubscribeEvent public static void rendered(ScreenEvent.Render.Post event){
        if(!ENABLED||finished||!prepared||!(event.getScreen() instanceof CultivationMainScreen host))return;
        var mc=Minecraft.getInstance();
        try {
            if(++frames<12)return;
            var page=(PlayerProgressionPage)field(host,"page");
            var buttons=page.children().stream().filter(c->c instanceof AbstractWidget w&&w.getMessage().getString().equals("创世 · 管理小世界")).map(c->(AbstractWidget)c).toList();
            if(buttons.size()!=1)throw new AssertionError("Missing/duplicate creation entry");
            var button=buttons.get(0);
            if(!(button instanceof CultivationUi.Action)||button.getY()!=page.senseFooterY())throw new AssertionError("Unstyled or misplaced entry");
            if(page.height<page.senseFooterY()+56+36)throw new AssertionError("Footer clipped from scroll extent");
            var method=host.getClass().getDeclaredMethod("contentViewportHeight");method.setAccessible(true);
            int viewport=(Integer)method.invoke(host);int offset=Math.max(0,page.height-viewport);scroll(host,offset);
            PlayerProgressionClient.state.putString("Notice","状态提示位于创世入口之后，不应与按钮重叠。");
            int mode=stage%4;button.active=mode!=2;page.setFocused(mode==3?button:null);button.setFocused(mode==3);
            float scale=(Float)field(host,"pageScale");int cx=(Integer)field(host,"contentX"),cy=(Integer)field(host,"contentY");
            int x=mode==1?(int)(cx+(button.getX()+button.getWidth()/2f)*scale):-1000;
            int y=mode==1?(int)(cy+(button.getY()-offset+button.getHeight()/2f)*scale):-1000;
            host.render(event.getGuiGraphics(),x,y,0);event.getGuiGraphics().flush();
            String name="scale"+mc.options.guiScale().get()+"-"+List.of("normal","hover","disabled","focus").get(mode);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name+".png"));}
            double pixels=mc.getWindow().getGuiScale();
            if(pixels!=(stage<4?1:2))throw new AssertionError("Requested GUI scale was fitted to "+pixels);
            ROWS.add(Map.of("name",name,"actualGuiScale",pixels,"footerY",page.senseFooterY(),"pageHeight",page.height,"buttonPixels",List.of((int)((cx+button.getX()*scale)*pixels),(int)((cy+(button.getY()-offset)*scale)*pixels),(int)(button.getWidth()*scale*pixels),(int)(button.getHeight()*scale*pixels))));
            frames=0;if(++stage==8)finish(mc);
        }catch(Throwable failure){FAILURES.add(failure.toString());com.mojang.logging.LogUtils.getLogger().error("CREATION_COMPAT_CAPTURE",failure);finish(mc);}
    }
    private static void finish(Minecraft mc){
        if(finished)return;finished=true;
        try{
            var pointer=PointerSafety.snapshot();if(!Boolean.TRUE.equals(pointer.get("passed")))FAILURES.add("Pointer safety "+pointer);
            Files.writeString(OUT.resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("cases",ROWS,"failures",FAILURES,"pointerSafety",pointer)));
            Files.writeString(OUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?"PASS":String.join("\n",FAILURES));
        }catch(Exception failure){throw new RuntimeException(failure);}mc.stop();
    }
}
