package cn.blockforge.generated.generatedmod.client.qa.myriad;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.TalismanPlatformScreen;

import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.lang.reflect.Field;
import com.google.gson.GsonBuilder;
import java.util.*;

/** Native visual-state fixture, no OS pointer input and no release packaging. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class TalismanCapture {
 static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.talismanCapture");
 static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("talisman");
 static boolean opened,prepared,finished;static int tick,stage;static final long START=System.nanoTime();
 static final String[] CASES={"draw-normal","draw-hover","draw-disabled","draw-focus","draw-search","draw-scale3","draw-scale4","engrave-normal","engrave-hover","engrave-disabled","engrave-focus","engrave-search","engrave-scale3","engrave-scale4","draw-bright-edge","engrave-bright-edge","draw-expanded","engrave-expanded","rub-empty","rub-ready","rub-hover","rub-scale3","rub-scale4","rub-blocked-output","expanded-collapse-hover","expanded-collapse-focus","expanded-scale3","expanded-scale4","expanded-return","engrave-expanded-scale3","expand-icon-hover","expand-icon-focus","expand-icon-disabled","collapse-icon-disabled"};
 static TalismanPlatformScreen screen;static TalismanPlatformMenu menu;static Button confirm,collapse;static EditBox search;
 static int mode(){return stage>=14?0:stage%7;}
 @SubscribeEvent public static void backdrop(ScreenEvent.BackgroundRendered event){
  if(!ENABLED||stage<14||event.getScreen()!=screen)return;
  var g=event.getGuiGraphics();
  for(int y=0;y<screen.height;y+=16)for(int x=0;x<screen.width;x+=16)
   g.fill(x,y,x+16,y+16,((x+y)/16%2)==0?0xFFFFFFFF:0xFFFF5BC4);
 }
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(!ENABLED||finished||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
  try{
   Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;GLFW.glfwHideWindow(mc.getWindow().getWindow());
   if((System.nanoTime()-START)/1e9>300)throw new AssertionError("Talisman QA timeout");
   if(!opened&&mc.player!=null)throw new AssertionError("Refuses user's loaded world");
   if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
    opened=true;mc.getWindow().setWindowed(1600,1200);mc.options.languageCode="zh_cn";mc.getLanguageManager().setSelected("zh_cn");mc.reloadResourcePacks();mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
    mc.createWorldOpenFlows().createFreshLevel("talisman-qa-"+System.currentTimeMillis(),new LevelSettings("Talisman isolated visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
   }
   if(!opened||mc.player==null||mc.getOverlay()!=null)return;
   if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();setup(mc);prepared=true;}
   tick++;
   if(tick==1){int scale=stage==21||stage==26||stage==29||mode()==5?3:stage==22||stage==27||mode()==6?4:2;mc.options.guiScale().set(scale);mc.resizeDisplay();setup(mc);
    if(stage<18||stage>23)confirm.active=mode()!=2;if(search!=null)search.setValue(mode()==4?"聚灵":"");
    if(stage==25||stage==31){screen.setFocused(collapse);collapse.setFocused(true);}
    if(stage==32||stage==33)collapse.active=false;
    if(mode()==3){screen.setFocused(confirm);confirm.setFocused(true);}
   }
   Field scaleField=TalismanPlatformScreen.class.getDeclaredField("uiScale");scaleField.setAccessible(true);float uiScale=scaleField.getFloat(screen);
   Button pointerTarget=(stage==24||stage==30)?collapse:confirm;
   double px=(mode()==1||stage==20||stage==24||stage==30)?(pointerTarget.getX()+pointerTarget.getWidth()/2d)*uiScale*mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth():-50;
   double py=(mode()==1||stage==20||stage==24||stage==30)?(pointerTarget.getY()+pointerTarget.getHeight()/2d)*uiScale*mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight():-50;
   for(String axis:new String[]{"xpos","ypos"}){Field cursor=MouseHandler.class.getDeclaredField(axis);cursor.setAccessible(true);cursor.setDouble(mc.mouseHandler,axis.equals("xpos")?px:py);}
  }catch(Throwable t){fail(mc,t);}
 }
 static void setup(Minecraft mc)throws Exception{
  boolean rub=stage>=18&&stage<=23;boolean engrave=(stage>=7&&stage<14)||stage==15||stage==17||stage==29;
  var kind=rub?TalismanPlatformBlockEntity.Kind.RUB:engrave?TalismanPlatformBlockEntity.Kind.ENGRAVE:TalismanPlatformBlockEntity.Kind.DRAW;
  var block=rub?WuXingBlocks.RUBBING_PLATFORM.get():engrave?WuXingBlocks.ENGRAVING_PLATFORM.get():WuXingBlocks.DRAWING_PLATFORM.get();
  var entity=new TalismanPlatformBlockEntity(BlockPos.ZERO,block.defaultBlockState(),kind);entity.setLevel(mc.level);
  menu=new TalismanPlatformMenu(77,mc.player.getInventory(),entity);
  mc.player.getInventory().setItem(9,new ItemStack(Items.DIRT,64));mc.player.getInventory().setItem(10,new ItemStack(Items.STONE,4));
  if(rub&&stage!=18){var seal=new ItemStack(WuXingItems.SEAL.get());TalismanData.setTalisman(seal,TalismanCatalog.ids().get(0));TalismanData.setQuality(seal,TalismanCatalog.Quality.LOW);menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).set(seal);menu.getSlot(TalismanPlatformBlockEntity.RUB_PAPER).set(new ItemStack(WuXingItems.TALISMAN_PAPER.get(),12));menu.getSlot(TalismanPlatformBlockEntity.inkStart(kind)).set(new ItemStack(WuXingItems.SPIRIT_INK.get()));if(stage==23)menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).set(new ItemStack(Items.DIRT,64));}
  screen=new TalismanPlatformScreen(menu,mc.player.getInventory(),block.getName());mc.setScreen(screen);
  Field f=TalismanPlatformScreen.class.getDeclaredField("controls");f.setAccessible(true);
  var controls=(List<?>)f.get(screen);confirm=(Button)controls.get(rub?0:1);
  f=TalismanPlatformScreen.class.getDeclaredField("search");f.setAccessible(true);search=(EditBox)f.get(screen);
  if(stage==16||stage==17||(stage>=24&&stage<=29)||stage==33){
   var expand=TalismanPlatformScreen.class.getDeclaredMethod("toggleExpanded");expand.setAccessible(true);expand.invoke(screen);
   controls=(List<?>)fControls(screen);collapse=(Button)controls.get(0);confirm=(Button)controls.get(1);
   if(collapse.getWidth()!=20||collapse.getHeight()!=20||!collapse.getMessage().getString().equals("缩小画布"))throw new AssertionError("Collapse button is not the paired square icon control");
   var canvas=TalismanPlatformScreen.class.getDeclaredMethod("canvas");canvas.setAccessible(true);var area=canvas.invoke(screen);var cy=area.getClass().getDeclaredMethod("y");var cell=area.getClass().getDeclaredMethod("cell");cy.setAccessible(true);cell.setAccessible(true);
   int bottom=(int)cy.invoke(area)+TalismanCatalog.CANVAS_HEIGHT*(int)cell.invoke(area)+15;
   if(confirm.getY()+99!=bottom-8)throw new AssertionError("Expanded scoring/action group is not anchored to the bottom");
   if(stage==28){Field drawn=TalismanPlatformScreen.class.getDeclaredField("drawn");drawn.setAccessible(true);((boolean[][])drawn.get(screen))[0][0]=true;collapse.onPress();Field expanded=TalismanPlatformScreen.class.getDeclaredField("expanded");expanded.setAccessible(true);if(expanded.getBoolean(screen)||!((boolean[][])drawn.get(screen))[0][0])throw new AssertionError("Collapse lost strokes or did not return");}
  }
  if(stage>=30&&stage<=32)collapse=(Button)((List<?>)fControls(screen)).get(0);
  if(rub){var output=menu.getSlot(TalismanPlatformBlockEntity.OUTPUT);if(output.x!=212||output.y!=70)throw new AssertionError("Rubbing output still lies below operation row");for(int i=0;i<TalismanPlatformBlockEntity.SIZE;i++)if(menu.getSlot(i).y>107)throw new AssertionError("Machine slot overlaps inventory");}
 }

 static Object fControls(TalismanPlatformScreen target)throws Exception{var f=TalismanPlatformScreen.class.getDeclaredField("controls");f.setAccessible(true);return f.get(target);}
 @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e){
  if(!ENABLED||finished||!prepared||e.phase!=TickEvent.Phase.END||tick<16)return;var mc=Minecraft.getInstance();
  try{
   if((mode()==1||stage==20)&&!confirm.isHovered())throw new AssertionError("Natural hover fixture did not reach confirm");
   if((stage==24||stage==30)&&!collapse.isHovered())throw new AssertionError("Collapse hover target missed");
   try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(CASES[stage]+".png"));}
   if(++stage==CASES.length){PointerSafety.verify();Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",CASES,"pointer",PointerSafety.snapshot(),"inventorySlots",36,"note","Client visual fixture with controlled quote; gameplay checks are separate")));finished=true;mc.stop();}else tick=0;
  }catch(Throwable t){fail(mc,t);}
 }
 static void fail(Minecraft mc,Throwable t){try{Files.writeString(OUT.resolve("failure.txt"),t.toString());}catch(Exception ignored){}t.printStackTrace();finished=true;mc.stop();}
}
