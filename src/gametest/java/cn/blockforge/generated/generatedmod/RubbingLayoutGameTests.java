package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class RubbingLayoutGameTests {
 @GameTest(template="empty") public static void outputStaysWithInputsAndShiftReturnsResult(GameTestHelper h){
  try(var p=new GameTestPlayer(h.getLevel())){
   p.join();p.getInventory().clearContent();var pos=h.absolutePos(BlockPos.ZERO);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.RUBBING_PLATFORM.get().defaultBlockState());
   var be=(TalismanPlatformBlockEntity)h.getLevel().getBlockEntity(pos);var menu=new TalismanPlatformMenu(97,p.getInventory(),be);
   h.assertTrue(menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).x==212&&menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).y==70,"Output did not follow its top label");
   for(int i=0;i<TalismanPlatformBlockEntity.SIZE;i++)h.assertTrue(menu.getSlot(i).y<=91,"Machine slot leaked into inventory area");
   h.assertTrue(menu.slots.size()==42&&menu.getSlot(6).x==40&&menu.getSlot(6).y==166&&menu.getSlot(33).y==224,"Rubbing storage/hotbar grid changed");
   be.setItem(TalismanPlatformBlockEntity.OUTPUT,new ItemStack(Items.PAPER,2));menu.quickMoveStack(p,TalismanPlatformBlockEntity.OUTPUT);
   h.assertTrue(menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).getItem().isEmpty()&&p.getInventory().countItem(Items.PAPER)==2,"Output shift pickup lost or duplicated items");menu.removed(p);
  }h.succeed();
 }
}
