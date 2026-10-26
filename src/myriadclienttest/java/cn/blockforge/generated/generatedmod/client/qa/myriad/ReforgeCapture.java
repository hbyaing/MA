package cn.blockforge.generated.generatedmod.client.qa.myriad;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.ReforgeScreen;
import cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.lang.reflect.Field;
import com.google.gson.GsonBuilder;
import java.util.*;

/** Native visual-state fixture, no OS pointer input and no release packaging. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ReforgeCapture {
 static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.reforgeCapture");
 static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("reforge");
 static boolean opened,prepared,finished;static int tick,stage;static final long START=System.nanoTime();
 static final String[] CASES={"normal","hover","disabled","focus","adventure","protected-seal","reforge-sharp","reforge-tough","reforge-spirit","reforge-light","pending","scale3","scale4","max-level","empty","small-viewport-hover","two-materials","four-materials"};
 static ReforgeScreen screen;static EquipmentWorkstationMenu menu;static ContainerData data;static Button confirm;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(!ENABLED||finished||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
  try{
   Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;GLFW.glfwHideWindow(mc.getWindow().getWindow());
   if((System.nanoTime()-START)/1e9>300)throw new AssertionError("Reforge QA timeout");
   if(!opened&&mc.player!=null)throw new AssertionError("Refuses user's loaded world");
   if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
    opened=true;mc.getWindow().setWindowed(1600,1200);mc.options.languageCode="zh_cn";mc.getLanguageManager().setSelected("zh_cn");mc.reloadResourcePacks();mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
    mc.createWorldOpenFlows().createFreshLevel("reforge-qa-"+System.currentTimeMillis(),new LevelSettings("Reforge isolated visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
   }
   if(!opened||mc.player==null||mc.getOverlay()!=null)return;
   if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();setup(mc);prepared=true;}
   tick++;
   if(tick==1){mc.getWindow().setWindowed(stage==15?1280:1600,stage==15?900:1200);int guiScale=stage==11?3:stage==12||stage==15?4:2;mc.options.guiScale().set(guiScale);mc.resizeDisplay();setup(mc);
    if(stage==3){screen.setFocused(confirm);confirm.setFocused(true);}
   }
   Field scaleField=ReforgeScreen.class.getDeclaredField("scale");scaleField.setAccessible(true);float uiScale=scaleField.getFloat(screen);
   double px=(stage==1||stage==15)?(confirm.getX()+confirm.getWidth()/2d)*uiScale*mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth():-50;
   double py=(stage==1||stage==15)?(confirm.getY()+confirm.getHeight()/2d)*uiScale*mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight():-50;
   for(String axis:new String[]{"xpos","ypos"}){Field cursor=MouseHandler.class.getDeclaredField(axis);cursor.setAccessible(true);cursor.setDouble(mc.mouseHandler,axis.equals("xpos")?px:py);}
  }catch(Throwable t){fail(mc,t);}
 }
 static void setup(Minecraft mc)throws Exception{
  menu=new EquipmentWorkstationMenu(77,mc.player.getInventory(),BlockPos.ZERO,false);
  var sword=new ItemStack(cn.blockforge.generated.generatedmod.WuXingItems.DARK_IRON_SWORD.get());
  var tag=cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.write(sword);tag.putInt("Refine",stage==16?0:stage==4||stage==5?6:stage==13?9:3);
  if(stage==10){tag.putInt("Temper",1);tag.putInt("TemperQuality",1);tag.putInt("TemperBp",8000);var pending=new net.minecraft.nbt.CompoundTag();pending.putInt("Type",0);pending.putInt("Quality",2);pending.putInt("Bp",9300);tag.put("Pending",pending);}
  if(stage!=14)menu.getSlot(0).set(sword);
  Field f=EquipmentWorkstationMenu.class.getDeclaredField("data");f.setAccessible(true);data=(ContainerData)f.get(menu);
  data.set(1,stage>=6&&stage<=10?30+Math.min(3,stage-6):stage==4?21:stage==5?22:20);
  data.set(2,stage==16?100:stage==4||stage==5?45:stage==13?0:80);data.set(3,stage>=6&&stage<=10?12:stage==4||stage==5?28:24);data.set(4,stage>=6&&stage<=10?4:stage==4||stage==5?7:6);data.set(6,stage==17?1:stage==5?2:stage>=6&&stage<=10?1:0);data.set(7,stage==16?0:stage>=6&&stage<=10?0:stage==4||stage==5?4:2);data.set(9,stage==2||stage==10||stage==13||stage==14?0:1);data.set(10,stage==13?7:stage==14?1:12);data.set(11,(3<<1)|(stage==5?1:0));
  mc.player.getInventory().setItem(9,new ItemStack(WuXingItems.SPIRIT_STONE.get(),36));mc.player.getInventory().setItem(10,new ItemStack(WuXingItems.DARK_IRON.get(),8));mc.player.getInventory().setItem(11,new ItemStack(WuXingItems.GLAZED_CRYSTAL.get(),stage==4||stage==5?4:3));mc.player.getInventory().setItem(12,new ItemStack(WuXingItems.FIVE_ESSENCE.get(),3));mc.player.getInventory().setItem(13,new ItemStack(WuXingItems.REFINE_SEAL.get()));
  screen=new ReforgeScreen(menu,mc.player.getInventory(),Component.literal("精铸台"));mc.setScreen(screen);
  f=ReforgeScreen.class.getDeclaredField("confirm");f.setAccessible(true);confirm=(Button)f.get(screen);
  CompactWorkstationChecks.verify(screen,menu,false);
  if(menu.slots.size()!=38)throw new AssertionError("Expected 2 workstation + 36 player slots");
  for(int row=0;row<3;row++)for(int col=0;col<9;col++){var slot=menu.getSlot(2+row*9+col);if(slot.x!=16+col*18||slot.y!=177+row*18)throw new AssertionError("Inventory geometry mismatch");}
 }
 @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e){
  if(!ENABLED||finished||!prepared||e.phase!=TickEvent.Phase.END||tick<16)return;var mc=Minecraft.getInstance();
  try{
   if((stage==1||stage==15)&&!confirm.isHovered())throw new AssertionError("Natural hover fixture did not reach confirm");
   try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(CASES[stage]+".png"));}
   if(++stage==CASES.length){PointerSafety.verify();Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",CASES,"pointer",PointerSafety.snapshot(),"inventorySlots",36,"note","Client visual fixture with controlled quote; gameplay checks are separate")));finished=true;mc.stop();}else tick=0;
  }catch(Throwable t){fail(mc,t);}
 }
 static void fail(Minecraft mc,Throwable t){try{Files.writeString(OUT.resolve("failure.txt"),t.toString());}catch(Exception ignored){}t.printStackTrace();finished=true;mc.stop();}
}
