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
    private static final String[] CASES={"selected-empty","selected-missing","selected-wrong","all-prepared","mixed-preparation","selected-materials-removed",
            "phase-warm","phase-extract","phase-combine","phase-condense","control-hover","control-disabled","control-focus","scale-one","scale-three"};
    private static final int[] EXPECTED_BONUS={0,0,0,6,3,0,6,6,6,6,6,6,6,6,6};
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final long START=System.nanoTime();
    private static boolean opened,finished,ready,languageSet;
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
                if(!languageSet){languageSet=true;mc.getLanguageManager().setSelected("zh_cn");mc.options.languageCode="zh_cn";mc.reloadResourcePacks();return;}
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
        var kind=AlchemyScreen.class.getDeclaredMethod("kindKey",AlchemyRecipe.class);kind.setAccessible(true);
        var rankKeys=new java.util.HashSet<String>();
        for(var candidate:choices) {
            var pill=cn.blockforge.generated.generatedmod.alchemy.PillData.read(candidate.getResultItem(mc.level.registryAccess()));
            if(pill!=null&&pill.pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.GATHER_QI)
                rankKeys.add((String)kind.invoke(screen,candidate));
        }
        if(rankKeys.size()!=5)throw new AssertionError("Medicinal ranks collapsed in formula list: "+rankKeys);
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
            default -> fillExact(true);
        }
        view.set(0,(stage>=6?AlchemyFurnaceMenu.State.BREWING:stage==3||stage==4?AlchemyFurnaceMenu.State.READY:AlchemyFurnaceMenu.State.MISSING).ordinal());
        if(stage>=6) {
            int phase=Math.min(3,stage-6);view.set(12,phase);view.set(13,500);view.set(14,stage==11?0:1);
            view.set(15,stage==11?15:0);view.set(16,stage==11?7:0);view.set(3,phase*2500+1250);
            view.set(7,50);
        }
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
            if(stage==13||stage==14) {
                mc.getWindow().setWidth(stage==14?1920:1280);mc.getWindow().setHeight(stage==14?1080:900);
                mc.options.guiScale().set(stage==14?3:1);mc.resizeDisplay();
                screen.init(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
            }
            screen.tick();
            var controlField=AlchemyScreen.class.getDeclaredField("controlFire");controlField.setAccessible(true);
            var control=(net.minecraft.client.gui.components.Button)controlField.get(screen);
            if(stage==12)screen.setFocused(control);
            int pointerX=stage==10?control.getX()+5:0,pointerY=stage==10?control.getY()+5:0;
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
            var model=RenderSystem.getModelViewStack();model.pushPose();
            try{
                model.setIdentity();model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
                RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
                var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,pointerX,pointerY,mc.getFrameTime());gui.flush();
                int error=GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
                for(int slot=0;slot<8;slot++)if(!ItemStack.matches(before.get(slot),input.getItem(slot)))throw new AssertionError("Rendering mutated input slot "+slot);
                var row=new LinkedHashMap<String,Object>();row.put("case",CASES[stage]);row.put("recipe",recipe.getId().toString());
                row.put("completeMaterials",complete);row.put("preparedBonus",bonus);row.put("glError",error);
                row.put("wastePercent",AlchemyFailureOdds.percent(recipe,menu.furnaceTier(),menu.ignitionTier(),false,bonus));
                row.put("width",mc.getMainRenderTarget().width);row.put("height",mc.getMainRenderTarget().height);
                row.put("guiScale",mc.options.guiScale().get());row.put("controlEnabled",control.active);
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                    image.writeToFile(OUTPUT.resolve(CASES[stage]+".png"));
                    if(stage==7) {
                        int scale=(int)mc.getWindow().getGuiScale();
                        int left=(screen.width-AlchemyUiLayout.WIDTH)/2,top=(screen.height-AlchemyUiLayout.HEIGHT)/2;
                        saveCrop(image,"phase-band",left+8,top+216,276,15,scale);
                        saveCrop(image,"control-card",left+290,top+279,122,37,scale);
                        saveCrop(image,"outer-tl",left,top,56,28,scale);
                        saveCrop(image,"outer-tr",left+364,top,56,28,scale);
                        saveCrop(image,"outer-bl",left,top+292,28,28,scale);
                        saveCrop(image,"outer-br",left+392,top+292,28,28,scale);
                        saveCrop(image,"outer-top",left+56,top,308,6,scale);
                        saveCrop(image,"outer-bottom",left+28,top+314,364,6,scale);
                        saveCrop(image,"outer-left",left,top+28,6,264,scale);
                        saveCrop(image,"outer-right",left+414,top+28,6,264,scale);
                    }
                }
                ROWS.add(row);stage++;writeReport();
            }finally{model.popPose();RenderSystem.applyModelViewMatrix();}
            if(stage==CASES.length)finish();
        }catch(Throwable error){fail("RENDER "+CASES[Math.min(stage,CASES.length-1)],error);}
    }

    private static void saveCrop(com.mojang.blaze3d.platform.NativeImage source,String name,int x,int y,int w,int h,int scale)throws Exception {
        try(var crop=new com.mojang.blaze3d.platform.NativeImage(w*4,h*4,false)) {
            for(int cy=0;cy<h*4;cy++)for(int cx=0;cx<w*4;cx++)
                crop.setPixelRGBA(cx,cy,source.getPixelRGBA(x*scale+cx*scale/4,y*scale+cy*scale/4));
            crop.writeToFile(OUTPUT.resolve(name+"-4x.png"));
        }
    }

    private static void writeReport()throws Exception{
        var report=new LinkedHashMap<String,Object>();report.put("test","alchemy-reservation-real-screen-render");
        report.put("fixture","Production AlchemyScreen and AlchemyFurnaceMenu client proxy in a fresh isolated save");
        report.put("captures",ROWS);report.put("failures",FAILURES);report.put("complete",ROWS.size()==CASES.length&&FAILURES.isEmpty());
        if(ROWS.size()==CASES.length)report.put("pointerSafety",cn.blockforge.generated.generatedmod.client.qa.PointerSafety.verify());
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
    private static void fail(String phase,Throwable error){FAILURES.add(phase+": "+error);com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_RESERVATION_CAPTURE "+phase,error);finish();}
    private static void finish(){
        if(finished)return;finished=true;
        try{Files.createDirectories(OUTPUT);writeReport();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),
                FAILURES.isEmpty()?"All fifteen real AlchemyScreen render cases passed; pointer capture blocked\n":String.join("\n",FAILURES));}
        catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_RESERVATION_REPORT",error);}
        Minecraft.getInstance().stop();
    }
}
