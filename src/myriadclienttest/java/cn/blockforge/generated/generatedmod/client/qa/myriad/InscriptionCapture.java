package cn.blockforge.generated.generatedmod.client.qa.myriad;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.InscriptionScreen;
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
public final class InscriptionCapture {
 static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.inscriptionCapture");
 static final Path OUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("inscription");
 static boolean opened,prepared,finished;static int tick,stage;static final long START=System.nanoTime();
 static final String[] CASES={"normal","hover","disabled","focus","search-input","scale3","scale4"};
 static InscriptionScreen screen;static EquipmentWorkstationMenu menu;static ContainerData data;static Button confirm;static EditBox search;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(!ENABLED||finished||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
  try{
   Files.createDirectories(OUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;GLFW.glfwHideWindow(mc.getWindow().getWindow());
   if((System.nanoTime()-START)/1e9>300)throw new AssertionError("Inscription QA timeout");
   if(!opened&&mc.player!=null)throw new AssertionError("Refuses user's loaded world");
   if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
    opened=true;mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
    mc.createWorldOpenFlows().createFreshLevel("inscription-qa-"+System.currentTimeMillis(),new LevelSettings("Inscription isolated visual QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
   }
   if(!opened||mc.player==null||mc.getOverlay()!=null)return;
   if(!prepared){mc.mouseHandler.grabMouse();PointerSafety.verify();setup(mc);prepared=true;}
   tick++;
   if(tick==1){int scale=stage==5?3:stage==6?4:2;mc.options.guiScale().set(scale);mc.resizeDisplay();setup(mc);
    data.set(9,stage==2?0:1);data.set(10,12);search.setValue(stage==4?"灵御":"");
    if(stage==3){screen.setFocused(confirm);confirm.setFocused(true);}
   }
   Field scaleField=InscriptionScreen.class.getDeclaredField("scale");scaleField.setAccessible(true);float uiScale=scaleField.getFloat(screen);
   double px=stage==1?(confirm.getX()+confirm.getWidth()/2d)*uiScale*mc.getWindow().getScreenWidth()/mc.getWindow().getGuiScaledWidth():-50;
   double py=stage==1?(confirm.getY()+confirm.getHeight()/2d)*uiScale*mc.getWindow().getScreenHeight()/mc.getWindow().getGuiScaledHeight():-50;
   for(String axis:new String[]{"xpos","ypos"}){Field cursor=MouseHandler.class.getDeclaredField(axis);cursor.setAccessible(true);cursor.setDouble(mc.mouseHandler,axis.equals("xpos")?px:py);}
  }catch(Throwable t){fail(mc,t);}
 }
 static void setup(Minecraft mc)throws Exception{
  menu=new EquipmentWorkstationMenu(77,mc.player.getInventory(),BlockPos.ZERO,true);menu.getSlot(0).set(new ItemStack(Items.DIAMOND_CHESTPLATE));
  Field f=EquipmentWorkstationMenu.class.getDeclaredField("data");f.setAccessible(true);data=(ContainerData)f.get(menu);data.set(2,100);data.set(3,4);data.set(4,1);data.set(5,4);data.set(7,1);data.set(9,1);
  mc.player.getInventory().setItem(9,new ItemStack(WuXingItems.SPIRIT_STONE.get(),36));mc.player.getInventory().setItem(10,new ItemStack(WuXingItems.DARK_IRON.get(),8));mc.player.getInventory().setItem(11,new ItemStack(Items.LAPIS_LAZULI,12));mc.player.getInventory().setItem(12,new ItemStack(WuXingItems.INSCRIPTION_CRYSTAL.get(),3));
  screen=new InscriptionScreen(menu,mc.player.getInventory(),Component.literal("铭刻台"));mc.setScreen(screen);
  f=InscriptionScreen.class.getDeclaredField("confirm");f.setAccessible(true);confirm=(Button)f.get(screen);f=InscriptionScreen.class.getDeclaredField("search");f.setAccessible(true);search=(EditBox)f.get(screen);
  if(menu.slots.size()!=38)throw new AssertionError("Expected 2 workstation + 36 player slots");
  for(int row=0;row<3;row++)for(int col=0;col<9;col++){var slot=menu.getSlot(2+row*9+col);if(slot.x!=234+col*18||slot.y!=281+row*18)throw new AssertionError("Inventory geometry mismatch");}
 }
 @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e){
  if(!ENABLED||finished||!prepared||e.phase!=TickEvent.Phase.END||tick<16)return;var mc=Minecraft.getInstance();
  try{
   if(stage==1&&!confirm.isHovered())throw new AssertionError("Natural hover fixture did not reach confirm");
   try(var im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(OUT.resolve(CASES[stage]+".png"));}
   if(++stage==CASES.length){PointerSafety.verify();Files.writeString(OUT.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",CASES,"pointer",PointerSafety.snapshot(),"inventorySlots",36,"note","Client visual fixture with controlled quote; gameplay checks are separate")));finished=true;mc.stop();}else tick=0;
  }catch(Throwable t){fail(mc,t);}
 }
 static void fail(Minecraft mc,Throwable t){try{Files.writeString(OUT.resolve("failure.txt"),t.toString());}catch(Exception ignored){}t.printStackTrace();finished=true;mc.stop();}
}
