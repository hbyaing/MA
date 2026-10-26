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
 static final String[] CASES={"draw-normal","draw-hover","draw-disabled","draw-focus","draw-search","draw-scale3","draw-scale4","engrave-normal","engrave-hover","engrave-disabled","engrave-focus","engrave-search","engrave-scale3","engrave-scale4","draw-bright-edge","engrave-bright-edge"};
 static TalismanPlatformScreen screen;static TalismanPlatformMenu menu;static Button confirm;static EditBox search;
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
    opened=true;mc.getWindow().setWindowed(1280,900);mc.options.languageCode="zh_cn";mc.getLanguageManager().setSelected("zh_cn");mc.reloadResourcePacks();mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
    mc.createWorldOpenFlows().createFreshLevel("talisman-qa-"+System.currentTimeMillis(),new LevelSettings("Talisman isolated visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
   }
   if(!opened||mc.player==null||mc.getOverlay()!=null)return;
   if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();setup(mc);prepared=true;}
   tick++;
   if(tick==1){int scale=mode()==5?3:mode()==6?4:2;mc.options.guiScale().set(scale);mc.resizeDisplay();setup(mc);
    confirm.active=mode()!=2;search.setValue(mode()==4?"聚灵":"");
    if(mode()==3){screen.setFocused(confirm);confirm.setFocused(true);}
   }
   Field scaleField=TalismanPlatformScreen.class.getDeclaredField("uiScale");scaleField.setAccessible(true);float uiScale=scaleField.getFloat(screen);
   double px=mode()==1?(confirm.getX()+confirm.getWidth()/2d)*uiScale*mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth():-50;
   double py=mode()==1?(confirm.getY()+confirm.getHeight()/2d)*uiScale*mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight():-50;
   for(String axis:new String[]{"xpos","ypos"}){Field cursor=MouseHandler.class.getDeclaredField(axis);cursor.setAccessible(true);cursor.setDouble(mc.mouseHandler,axis.equals("xpos")?px:py);}
  }catch(Throwable t){fail(mc,t);}
 }
 static void setup(Minecraft mc)throws Exception{
  boolean engrave=(stage>=7&&stage<14)||stage==15;
  var kind=engrave?TalismanPlatformBlockEntity.Kind.ENGRAVE:TalismanPlatformBlockEntity.Kind.DRAW;
  var block=engrave?WuXingBlocks.ENGRAVING_PLATFORM.get():WuXingBlocks.DRAWING_PLATFORM.get();
  var entity=new TalismanPlatformBlockEntity(BlockPos.ZERO,block.defaultBlockState(),kind);entity.setLevel(mc.level);
  menu=new TalismanPlatformMenu(77,mc.player.getInventory(),entity);
  mc.player.getInventory().setItem(9,new ItemStack(Items.DIRT,64));mc.player.getInventory().setItem(10,new ItemStack(Items.STONE,4));
  screen=new TalismanPlatformScreen(menu,mc.player.getInventory(),block.getName());mc.setScreen(screen);
  Field f=TalismanPlatformScreen.class.getDeclaredField("controls");f.setAccessible(true);
  var controls=(List<?>)f.get(screen);confirm=(Button)controls.get(1);
  f=TalismanPlatformScreen.class.getDeclaredField("search");f.setAccessible(true);search=(EditBox)f.get(screen);
 }

 @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e){
  if(!ENABLED||finished||!prepared||e.phase!=TickEvent.Phase.END||tick<16)return;var mc=Minecraft.getInstance();
  try{
   if(mode()==1&&!confirm.isHovered())throw new AssertionError("Natural hover fixture did not reach confirm");
   try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(CASES[stage]+".png"));}
   if(++stage==CASES.length){PointerSafety.verify();Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",CASES,"pointer",PointerSafety.snapshot(),"inventorySlots",36,"note","Client visual fixture with controlled quote; gameplay checks are separate")));finished=true;mc.stop();}else tick=0;
  }catch(Throwable t){fail(mc,t);}
 }
 static void fail(Minecraft mc,Throwable t){try{Files.writeString(OUT.resolve("failure.txt"),t.toString());}catch(Exception ignored){}t.printStackTrace();finished=true;mc.stop();}
}
