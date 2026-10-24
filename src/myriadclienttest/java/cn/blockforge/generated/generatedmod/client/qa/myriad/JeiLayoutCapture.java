package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.CultivationMainScreen;
import cn.blockforge.generated.generatedmod.compat.jei.WuXingJeiPlugin;
import com.google.gson.GsonBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.library.plugins.vanilla.gui.InventoryEffectRendererGuiHandler;
import net.minecraft.client.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;

/** Production JEI handler invocation with pointer protection and an isolated disposable world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class JeiLayoutCapture {
    static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.jeiLayoutCapture");
    static final Path OUT=Path.of("../../reports/jei-layout-20261025");
    static final List<Object> rows=new ArrayList<>();
    static final List<String> failures=new ArrayList<>();
    static boolean opened,prepared,finished;static int tick,stage;
    static final long START=System.nanoTime();
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if((System.nanoTime()-START)/1e9>300)throw new AssertionError("JEI layout timeout");
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses existing world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1600,1000);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("jei-layout-"+System.currentTimeMillis(),new LevelSettings("JEI isolated QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getOverlay()!=null||WuXingJeiPlugin.runtime==null)return;
            if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();prepared=true;}
            tick++;
            if(tick%30==1){
                mc.options.guiScale().set(stage/4+1);mc.resizeDisplay();
                mc.player.removeAllEffects();
                if(stage%4>=2)for(var effect:List.of(MobEffects.MOVEMENT_SPEED,MobEffects.DAMAGE_BOOST,MobEffects.REGENERATION))mc.player.addEffect(new MobEffectInstance(effect,12000));
                mc.setScreen(new CultivationMainScreen(mc.player,stage%2==0?"inventory":"vision"));
                WuXingJeiPlugin.runtime.getIngredientFilter().setFilterText("");
            }
            if(tick%30==25){
                var screen=(CultivationMainScreen)mc.screen;
                var areas=new InventoryEffectRendererGuiHandler<net.minecraft.world.inventory.InventoryMenu>().getGuiExtraAreas(screen);
                check(areas.size()==(stage%4==2?3:0),"Phantom effect exclusions on stage "+stage+": "+areas.size());
                for(var area:areas)check(area.getX()<screen.getGuiLeft()+screen.getXSize(),"Effects incorrectly anchored outside the full frame");
                var rt=WuXingJeiPlugin.runtime;
                for(var stack:rt.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK)){
                    var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if(id.getNamespace().equals("wuxingxiantu")&&id.getPath().startsWith("spirit_vein_ore"))
                        check(rt.getIngredientVisibility().isIngredientVisible(VanillaTypes.ITEM_STACK,stack,UidContext.Ingredient)==id.getPath().equals("spirit_vein_ore"),"Legacy ore visible in JEI: "+id);
                }
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve("stage-"+stage+".png"));}
                rows.add(Map.of("stage",stage,"configuredScale",stage/4+1,"actualScale",mc.getWindow().getGuiScale(),"effects",areas.size()));
                if(++stage==12)finish(mc);
            }
        }catch(Throwable e){failures.add(e.toString());finish(mc);}
    }
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static void finish(Minecraft mc){
        finished=true;
        try{Files.writeString(OUT.resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("cases",rows,"failures",failures,"pointerSafety",PointerSafety.snapshot())));}catch(Exception e){throw new RuntimeException(e);}
        mc.stop();
    }
}
