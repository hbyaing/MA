package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;

/** Exercises the enlarged equipment hit box and untouched inventory hit cells. */
final class CompactWorkstationChecks {
 static void verify(AbstractContainerScreen<EquipmentWorkstationMenu> screen,EquipmentWorkstationMenu menu,boolean inscription)throws Exception{
  var left=AbstractContainerScreen.class.getDeclaredField("leftPos");left.setAccessible(true);
  var top=AbstractContainerScreen.class.getDeclaredField("topPos");top.setAccessible(true);int x=left.getInt(screen),y=top.getInt(screen);
  var hit=screen.getClass().getDeclaredMethod("isHovering",int.class,int.class,int.class,int.class,double.class,double.class);hit.setAccessible(true);var equipment=menu.getSlot(0);
  if(!(Boolean)hit.invoke(screen,equipment.x,equipment.y,16,16,x+134d,y+56d))throw new AssertionError("Visible equipment corner is not clickable");
  if(!(Boolean)hit.invoke(screen,equipment.x,equipment.y,16,16,x+164.5,y+86.5))throw new AssertionError("Equipment lower corner is not clickable");
  if((Boolean)hit.invoke(screen,equipment.x,equipment.y,16,16,x+165d,y+70d))throw new AssertionError("Equipment hit box extends beyond frame");
  for(int i=2;i<38;i++){var slot=menu.getSlot(i);if(!(Boolean)hit.invoke(screen,slot.x,slot.y,16,16,x+slot.x+8d,y+slot.y+8d))throw new AssertionError("Inventory cell is not clickable: "+i);}
  int height=inscription?284:266;
  for(var child:screen.children())if(child instanceof AbstractWidget w&&w.visible){if(w.getX()<x||w.getY()<y||w.getX()+w.getWidth()>x+360||w.getY()+w.getHeight()>y+height)throw new AssertionError("Control outside compact window: "+w.getMessage());}
 }
 private CompactWorkstationChecks(){}
}
