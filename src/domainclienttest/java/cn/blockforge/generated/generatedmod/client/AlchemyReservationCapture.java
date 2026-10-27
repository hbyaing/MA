package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.AlchemyFailureOdds;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/** Opt-in real AlchemyScreen rendering in a fresh, isolated development save; never in the release JAR. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class AlchemyReservationCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.alchemyReservationCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.alchemyReservationOutput","captures"))
            .resolve("run-"+System.currentTimeMillis());
    private static final String[] CASES={"selected-empty","selected-missing","selected-wrong","all-prepared","mixed-preparation","selected-materials-removed","observation-open","observation-tags-scroll","observation-focus","observation-gui4"};
    private static final int[] EXPECTED_BONUS={0,0,0,6,3,0,6,6,6,6};
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final long START=System.nanoTime();
    private static boolean opened,finished,ready;
    private static int settleTicks,stage;
    private static AlchemyFurnaceBlockEntity input;
    private static AlchemyFurnaceMenu menu;
    private static AlchemyScreen screen;
    private static AlchemyRecipe recipe;
    private static ContainerData view;

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUTPUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if((System.nanoTime()-START)/1_000_000_000L>300)throw new AssertionError("Alchemy reservation capture timeout");
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses to adopt an existing save");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(2);mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("alchemy-reservation-test-"+System.currentTimeMillis(),
                        new LevelSettings("Alchemy reservation acceptance",GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||mc.screen!=null)return;
            if(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900){
                mc.getWindow().setWidth(1280);mc.getWindow().setHeight(900);mc.resizeDisplay();
            }
            ready=++settleTicks>=30;
        }catch(Throwable error){fail("SETUP",error);}
    }

    private static void initialize(Minecraft mc)throws Exception{
        // Local menu proxy isolates inventory changes from the live server. It uses the production menu and screen.
        input=new AlchemyFurnaceBlockEntity(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_FURNACE.get(),
                new BlockPos(123456,179,123456),WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState().setValue(AlchemyFurnaceBlock.FORMED,true));
        input.setLevel(mc.level);
        input.setItem(AlchemyFurnaceBlockEntity.IGNITION_SLOT,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        menu=new AlchemyFurnaceMenu(0,mc.player.getInventory(),input);
        var choices=menu.recipes();int index=-1;
        for(int i=0;i<choices.size();i++)if(choices.get(i).getId().getPath().equals("alchemy_aura_pill_low")){index=i;break;}
        if(index<0)throw new AssertionError("Regression recipe is absent");recipe=choices.get(index);
        var field=AlchemyFurnaceMenu.class.getDeclaredField("viewData");field.setAccessible(true);view=(ContainerData)field.get(menu);
        view.set(0,AlchemyFurnaceMenu.State.MISSING.ordinal());view.set(1,0);view.set(2,1);view.set(4,index+1);
        view.set(6,0);view.set(8,1);view.set(9,0x7fff);view.set(10,0x7fff);
        screen=new AlchemyScreen(menu,mc.player.getInventory(),Component.literal("炼丹预留回归验收"));
        screen.init(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
    }

    private static void clearMaterials(){for(int slot=0;slot<AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT;slot++)input.setItem(slot,ItemStack.EMPTY);}
    private static void fillExact(boolean allPrepared){
        clearMaterials();boolean firstHerb=true;
        for(int slot=0;slot<recipe.getIngredients().size();slot++){
            var stack=recipe.getIngredients().get(slot).getItems()[0].copyWithCount(recipe.counts().get(slot));
            if(HerbPreparation.herb(stack)){
                if(allPrepared||firstHerb){int count=stack.getCount();stack=HerbPreparation.prepare(stack);stack.setCount(count);}
                firstHerb=false;
            }
            input.setItem(slot,stack);
        }
    }
    private static void configureCase(){
        switch(stage){
            case 0 -> clearMaterials();
            case 1 -> {fillExact(true);input.getItem(0).shrink(1);}
            case 2 -> {clearMaterials();input.setItem(0,new ItemStack(Items.DIRT,64));}
            case 3 -> fillExact(true);
            case 4 -> fillExact(false);
            case 5 -> clearMaterials(); // Keep the same selected recipe/menu after a valid batch loses all input.
            case 6,7,8,9 -> fillExact(true);
            default -> throw new AssertionError("Unknown capture stage");
        }
        view.set(0,(stage==3||stage==4||stage>=6?AlchemyFurnaceMenu.State.READY:AlchemyFurnaceMenu.State.MISSING).ordinal());
    }

    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!ready||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||mc.screen!=null)return;
        try{
            if(screen==null)initialize(mc);configureCase();
            if(menu.selectedRecipe()!=recipe)throw new AssertionError("Selected recipe changed during material edits");
            boolean complete=recipe.tryReservation(input).isPresent();
            if(complete!=(stage==3||stage==4||stage>=6))throw new AssertionError("Unexpected fixture material availability");
            int bonus=HerbPreparation.bonus(recipe,input);
            if(bonus!=EXPECTED_BONUS[stage])throw new AssertionError("Wrong preparation bonus: "+bonus);
            var before=new ArrayList<ItemStack>();for(int slot=0;slot<8;slot++)before.add(input.getItem(slot).copy());
            screen.tick();
            if(stage==6){
                var control=screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("记录")).map(c->(net.minecraft.client.gui.components.Button)c).findFirst().orElseThrow();
                control.onPress();if(AlchemyObservationView.rows(menu).size()<=5)throw new AssertionError("Missing parameter/tag observations");
            }
            if(stage==7)screen.mouseScrolled(screen.getGuiLeft()+300,screen.getGuiTop()+180,-1);
            if(stage==8)screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("药力")).findFirst().orElseThrow().setFocused(true);
            if(stage==9){mc.options.guiScale().set(4);mc.resizeDisplay();screen.init(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());}
            Class.forName("cn.blockforge.generated.generatedmod.client.qa.myriad.PointerSafety").getMethod("verify").invoke(null);
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
            var model=RenderSystem.getModelViewStack();model.pushPose();
            try{
                model.setIdentity();model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
                RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
                var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,0,0,mc.getFrameTime());gui.flush();
                int error=GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
                for(int slot=0;slot<8;slot++)if(!ItemStack.matches(before.get(slot),input.getItem(slot)))throw new AssertionError("Rendering mutated input slot "+slot);
                var row=new LinkedHashMap<String,Object>();row.put("case",CASES[stage]);row.put("recipe",recipe.getId().toString());
                row.put("completeMaterials",complete);row.put("preparedBonus",bonus);row.put("glError",error);
                row.put("wastePercent",AlchemyFailureOdds.percent(recipe,menu.furnaceTier(),menu.ignitionTier(),false,bonus));
                row.put("width",mc.getMainRenderTarget().width);row.put("height",mc.getMainRenderTarget().height);
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(CASES[stage]+".png"));}
                ROWS.add(row);stage++;writeReport();
            }finally{model.popPose();RenderSystem.applyModelViewMatrix();}
            if(stage==CASES.length)finish();
        }catch(Throwable error){fail("RENDER "+CASES[Math.min(stage,CASES.length-1)],error);}
    }

    private static void writeReport()throws Exception{
        var report=new LinkedHashMap<String,Object>();report.put("test","alchemy-reservation-real-screen-render");
        report.put("fixture","Production AlchemyScreen and AlchemyFurnaceMenu client proxy in a fresh isolated save");
        report.put("pointer",Class.forName("cn.blockforge.generated.generatedmod.client.qa.myriad.PointerSafety").getMethod("snapshot").invoke(null));
        report.put("captures",ROWS);report.put("failures",FAILURES);report.put("complete",ROWS.size()==CASES.length&&FAILURES.isEmpty());
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
    private static void fail(String phase,Throwable error){FAILURES.add(phase+": "+error);com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_RESERVATION_CAPTURE "+phase,error);finish();}
    private static void finish(){
        if(finished)return;finished=true;
        try{Files.createDirectories(OUTPUT);writeReport();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),
                FAILURES.isEmpty()?"All ten real AlchemyScreen render cases passed\n":String.join("\n",FAILURES));}
        catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_RESERVATION_REPORT",error);}
        Minecraft.getInstance().stop();
    }
}
