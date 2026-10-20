package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.compat.jei.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class JeiCatalogCapture {
    static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.jeiCatalogCapture");
    static final Path OUT=Path.of(System.getProperty("wuxingxiantu.jeiOutput","../../reports/alchemy-followup-20261014/jei"));
    static final CatalogIngredient[] CASES={new CatalogIngredient(null),new CatalogIngredient(PillCatalog.BERSERK),new CatalogIngredient(PillCatalog.CALMING),new CatalogIngredient(PillCatalog.CULTIVATION),new CatalogIngredient(PillCatalog.ROOT)};
    static final List<Map<String,Object>> rows=new ArrayList<>();static final List<String> failures=new ArrayList<>();
    static final long START=System.nanoTime();static boolean opened,prepared,finished;static int tick,stage;
    static CompletableFuture<?> prepare;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if((System.nanoTime()-START)/1e9>300)throw new AssertionError("JEI capture timeout");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.deleteIfExists(OUT.resolve("failure.txt"));Files.deleteIfExists(OUT.resolve("complete.txt"));
                mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(2);mc.resizeDisplay();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("jei-catalog-"+System.currentTimeMillis(),new LevelSettings("JEI quality catalog acceptance",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||WuXingJeiPlugin.runtime==null)return;
            if(!prepared){prepared=true;var id=mc.player.getUUID();prepare=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setNoGravity(true);p.teleportTo(.5,180,.5);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setAura(1_000_000);GeneratedMod.sync(p);});return;}
            if(prepare!=null){if(!prepare.isDone())return;prepare.join();prepare=null;}
            if(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900){mc.getWindow().setWidth(1280);mc.getWindow().setHeight(900);mc.resizeDisplay();}
            tick++;
            if(tick%35==1){
                var runtime=WuXingJeiPlugin.runtime;var manager=runtime.getIngredientManager();
                var all=manager.getAllIngredients(WuXingJeiPlugin.TYPE);check(all.size()==18&&all.stream().filter(CatalogIngredient::core).count()==1,"One merged core / 17 pills");
                for(var entry:all){var item=manager.getIngredientHelper(WuXingJeiPlugin.TYPE).getCheatItemStack(entry);
                    check(entry.core()?item.is(WuXingItems.INNER_CORES[5].get()):PillData.read(item).grade()==(entry.pill()==PillCatalog.CALMING?PillData.Grade.LOW:PillData.Grade.PERFECT),"Catalog highest grade: "+entry.id());
                    check(entry.tooltip().stream().noneMatch(line->line.getString().contains("作弊")),"Cheat instruction leaked into tooltip");}
                for(var stack:manager.getAllIngredients(VanillaTypes.ITEM_STACK))if(PillData.read(stack)!=null||WuXingJeiPlugin.isCore(stack)){
                    check(!runtime.getIngredientVisibility().isIngredientVisible(VanillaTypes.ITEM_STACK,stack,UidContext.Ingredient),"Duplicate list slot");
                    check(runtime.getIngredientVisibility().isIngredientVisible(VanillaTypes.ITEM_STACK,stack,UidContext.Recipe),"Recipe grade hidden");
                }
                var current=CASES[stage%CASES.length];runtime.getIngredientFilter().setFilterText(current.name().getString());
                mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                var cheat=manager.getIngredientHelper(WuXingJeiPlugin.TYPE).getCheatItemStack(current);
                check(current.core()?cheat.is(WuXingItems.INNER_CORES[5].get()):PillData.read(cheat).grade()==(current.pill()==PillCatalog.CALMING?PillData.Grade.LOW:PillData.Grade.PERFECT),"Cheat is not highest grade");
                check(cheat.getTag()==null||!cheat.getTag().contains("JeiCatalog"),"Display marker leaked to real item");
                mc.gameMode.handleCreativeModeItemAdd(cheat,36); // The same authentic helper stack JEI sends to the server.
                if(current.core()){
                    var factory=runtime.getJeiHelpers().getFocusFactory();var focus=factory.createFocus(RecipeIngredientRole.INPUT,WuXingJeiPlugin.TYPE,current);
                    check(runtime.getRecipeManager().createRecipeCategoryLookup().limitFocus(List.of(focus)).get().findAny().isPresent(),"Merged core lost recipe lookup");
                }
            }
            if(tick%35==25){
                var current=CASES[stage%CASES.length];var runtime=WuXingJeiPlugin.runtime;boolean expanded=stage>=CASES.length;
                check(runtime.getIngredientFilter().getFilteredIngredients(WuXingJeiPlugin.TYPE).size()==1,"Search must have exactly one catalog entry");
                check(runtime.getIngredientFilter().getFilteredIngredients(VanillaTypes.ITEM_STACK).stream().noneMatch(s->PillData.read(s)!=null||WuXingJeiPlugin.isCore(s)),"Visible duplicate vanilla item");
                var server=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).getInventory().getItem(0).copy()).join();
                check(server.getItem()==current.cheat().getItem()&&Objects.equals(server.getTag(),current.cheat().getTag()),"Highest cheat stack did not reach server");
                int gl=org.lwjgl.opengl.GL11.glGetError();check(gl==0,"GL error "+gl);
                var text=current.tooltip(expanded,0).stream().map(Component::getString).toList();
                long detail=text.stream().filter(line->line.matches("^(凡品|下品|中品|上品|极品|仙品)：.*")).count();
                check(detail==(expanded?(current.core()?6:current.pill()==PillCatalog.CALMING?0:4):0),"Shift did not control quality details");
                if(current.pill()==PillCatalog.BERSERK)check(text.contains("以猛烈药力催动气血，短时间换取爆发。")&&text.contains("限时力量与速度"),"Original introduction missing");
                var palette=new ArrayList<Integer>();for(int i=0;i<(current.core()?6:current.pill()==PillCatalog.CALMING?1:4);i++){
                    int color=current.name(i*40.).getStyle().getColor().getValue();palette.add(color);
                    check(current.name(i*40.).getSiblings().isEmpty(),"Per-character spatial gradient returned");
                    int expected=current.core()?new int[]{0xB7B0A3,0x79C58B,0x80D9F2,0xE2A7F5,0xFFC94A,0xFF6B6B}[i]:current.pill()==PillCatalog.CALMING?0xC99CEB:cn.blockforge.generated.generatedmod.text.ItemTextPalette.pillGrade(i);
                    check(color==expected,"Time cycle missed grade color");
                }
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(current.id()+(expanded?"-shift":"")+".png"));}
                var row=new LinkedHashMap<String,Object>();row.put("id",current.id());row.put("expanded",expanded);row.put("listEntries",1);row.put("cheatStack",server.save(new net.minecraft.nbt.CompoundTag()).toString());row.put("tooltip",text);row.put("cycleColors",palette);row.put("glError",gl);rows.add(row);stage++;
                if(stage==CASES.length*2)finish(mc);
            }
        }catch(Throwable e){failures.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("JEI_CATALOG_CAPTURE",e);finish(mc);}
    }
    @SubscribeEvent public static void tooltip(ScreenEvent.Render.Post event){if(!ENABLED||finished||!prepared||stage>=CASES.length*2||WuXingJeiPlugin.runtime==null)return;
        var mc=Minecraft.getInstance();var value=CASES[stage%CASES.length];
        event.getGuiGraphics().renderComponentTooltip(mc.font,stage<CASES.length?WuXingJeiPlugin.runtime.getIngredientManager().getIngredientRenderer(WuXingJeiPlugin.TYPE).getTooltip(value,TooltipFlag.NORMAL):value.tooltip(true,mc.level.getGameTime()+mc.getFrameTime()),12,40);}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void finish(Minecraft mc){if(finished)return;finished=true;try{Files.writeString(OUT.resolve("verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("cases",rows,"failures",failures)));Files.writeString(OUT.resolve(failures.isEmpty()?"complete.txt":"failure.txt"),failures.isEmpty()?"PASS":String.join("\n",failures));}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
