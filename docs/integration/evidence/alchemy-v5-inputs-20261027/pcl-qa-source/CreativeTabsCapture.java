package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.registry.ModTabs;
import cn.blockforge.generated.generatedmod.alchemy.*;
import com.google.gson.GsonBuilder;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import java.nio.file.*;
import java.util.*;

/** Opt-in, isolated native creative-screen acceptance. Never included in production. */
@Mod.EventBusSubscriber(modid="codex_inputs_qa",value=Dist.CLIENT)
public final class CreativeTabsCapture {
    private static final boolean ENABLED=true;
    private static final Path OUT=Path.of("C:/Users/Lenovo/Documents/Codex/2026-10-27/https-github-com-thezinecraftm-rgb-myriad/outputs/pcl-client-validation").resolve("creative-"+System.currentTimeMillis());
    private static final long START=System.nanoTime();
    private static boolean opened,prepared,finished;
    private static int tick,stage;
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static List<CreativeModeTab> tabs;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            
            if((System.nanoTime()-START)/1e9>360)throw new AssertionError("Creative tabs timeout, stage="+stage);
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses an existing user world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(2);mc.resizeDisplay();
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("creative-tabs-"+System.currentTimeMillis(),new LevelSettings("Creative categories isolated QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
            if(!prepared){
                mc.mouseHandler.grabMouse();PointerSafety.verify();
                mc.setScreen(new CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true));
                tabs=CreativeModeTabRegistry.getSortedCreativeModeTabs().stream().filter(t->GeneratedMod.MOD_ID.equals(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(t).getNamespace())).toList();
                check(tabs.size()==10,"Expected ten categories, got "+tabs.size());
                audit();prepared=true;
            }
            tick++;
            if(stage>=20){
                if(stage==20){
                    var herb=new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),64);
                    herb=cn.blockforge.generated.generatedmod.plants.HerbAge.creativeTemplate(herb);
                    mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(36,herb));stage=21;tick=0;return;
                }
                if(tick<30)return;
                var acquired=mc.player.getInventory().getItem(0);
                check(acquired.getCount()==64&&cn.blockforge.generated.generatedmod.plants.HerbAge.definition(acquired)!=null,"Creative packet did not return a herb stack");
                check(acquired.hasTag()&&!acquired.getTag().contains(cn.blockforge.generated.generatedmod.plants.HerbAge.CREATIVE_TEMPLATE),"Production packet left unresolved herb template");
                int years=cn.blockforge.generated.generatedmod.plants.HerbAge.years(acquired);check(years>=0&&years<=10499,"Production random age out of range");
                ROWS.add(Map.of("case","production-creative-packet","years",years,"count",acquired.getCount(),"nbt",acquired.getTag().toString()));
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve("acquired-herb.png"));}
                finish(mc);return;
            }

            int scale=stage<10?2:3;
            if(mc.options.guiScale().get()!=scale){mc.options.guiScale().set(scale);mc.resizeDisplay();}
            if(tick%20==1){
                var screen=(CreativeModeInventoryScreen)mc.screen;var tab=tabs.get(stage%10);
                var pages=CreativeModeInventoryScreen.class.getDeclaredField("pages");pages.setAccessible(true);
                @SuppressWarnings("unchecked") var all=(List<CreativeTabsScreenPage>)pages.get(screen);
                var page=all.stream().filter(p->p.getVisibleTabs().contains(tab)).findFirst().orElseThrow();
                screen.setCurrentPage(page);
                var select=CreativeModeInventoryScreen.class.getDeclaredMethod("m_98560_",CreativeModeTab.class);select.setAccessible(true);select.invoke(screen,tab);
                check(screen.getCurrentPage().getVisibleTabs().contains(tab),"Selected tab missing from current page");
            }
            if(tick%20==15){
                var tab=tabs.get(stage%10);int gl=GL11.glGetError();check(gl==0,"GL error "+gl);
                String id=BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getPath();
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(id+"-scale"+scale+".png"));}
                ROWS.add(Map.of("tab",id,"title",tab.getDisplayName().getString(),"items",tab.getDisplayItems().size(),"guiScale",scale,"glError",gl));
                ++stage;
            }
        }catch(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("CREATIVE_TABS_CAPTURE",error);finish(mc);}
    }
    private static void audit(){
        var counts=new HashMap<String,Integer>();
        for(var stack:ModTabs.HERBS.get().getDisplayItems()){
            var herb=cn.blockforge.generated.generatedmod.plants.HerbAge.definition(stack);if(herb==null)continue;
            counts.merge(herb.id,1,Integer::sum);
            check(cn.blockforge.generated.generatedmod.plants.HerbAge.years(stack)==0&&stack.getTag().getBoolean(cn.blockforge.generated.generatedmod.plants.HerbAge.CREATIVE_TEMPLATE),"Herb creative entry is not a zero-year template");
        }
        check(counts.size()==cn.blockforge.generated.generatedmod.plants.SpiritPlantCatalog.ALL.size()&&counts.values().stream().allMatch(n->n==1),"Duplicated/missing creative herbs: "+counts);

        var union=new HashSet<String>();var items=new HashSet<Item>();
        for(int i=0;i<tabs.size();i++){
            var tab=tabs.get(i);check(!tab.getDisplayItems().isEmpty(),"Empty category "+tab.getDisplayName());
            for(var stack:tab.getDisplayItems()){
                check(!stack.isEmpty()&&stack.getCount()==1,"Invalid creative stack");
                check(union.add(stack.save(new net.minecraft.nbt.CompoundTag()).toString()),"Duplicate item/NBT across tabs: "+stack);
                items.add(stack.getItem());
                check(!tab.getDisplayName().getString().startsWith("itemGroup."),"Missing localized title");
            }
        }
        for(var id:BuiltInRegistries.ITEM.keySet())if(GeneratedMod.MOD_ID.equals(id.getNamespace())){
            var item=BuiltInRegistries.ITEM.get(id);
            if(WuXingItemSources.classify(new ItemStack(item))==WuXingItemSources.Classification.INTERNAL||Set.of("alchemy_formula","talisman").contains(id.getPath()))continue;
            check(items.contains(item),"Registered player-facing item missing: "+id);
        }
        var pillStacks=tabs.stream().flatMap(t->t.getDisplayItems().stream()).filter(s->PillData.read(s)!=null).toList();
        for(var pill:PillCatalog.values())if(MedicineTier.imported(pill))for(int rank=1;rank<=5;rank++){
            final int r=rank;check(pillStacks.stream().filter(s->PillData.read(s).pill()==pill&&MedicineTier.of(s)==r).count()==4,"Missing pill rank/quality variants "+pill+" rank="+r);
        }
        check(items.contains(cn.blockforge.generated.generatedmod.formation.FormationBootstrap.ZHUXIAN_SWORD_CHART.get()),"Missing sword chart");
        for(var sword:cn.blockforge.generated.generatedmod.formation.FormationBootstrap.swords())check(items.contains(sword.get()),"Missing chaos sword");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void finish(Minecraft mc){
        if(finished)return;finished=true;
        try{
            var pointer=PointerSafety.snapshot();if(!Boolean.TRUE.equals(pointer.get("passed")))FAILURES.add("Pointer safety "+pointer);
            Files.writeString(OUT.resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("cases",ROWS,"failures",FAILURES,"pointerSafety",pointer)));
            Files.writeString(OUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?"PASS":String.join("\n",FAILURES));
        }catch(Exception e){throw new RuntimeException(e);}mc.stop();
    }
}
