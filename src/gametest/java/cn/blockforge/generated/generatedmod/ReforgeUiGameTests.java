package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class ReforgeUiGameTests {
    @GameTest(template="empty") public static void menuSyncsSealAlternativeAndKeepsPendingChoiceExplicit(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){
            p.join();var pos=h.absolutePos(BlockPos.ZERO);p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.REFORGE_TABLE.get().defaultBlockState());
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setLearnedFires(new int[]{9});d.wuxingxiantu$setEquippedFire(9);d.wuxingxiantu$setAura(1000000);
            var profession=PlayerCrafts.get(p.server).profession(p.getUUID(),"refining");profession.putBoolean("Learned",true);profession.putInt("Rank",8);
            p.getInventory().setItem(20,new ItemStack(WuXingItems.SPIRIT_STONE.get(),64));p.getInventory().setItem(21,new ItemStack(WuXingItems.DARK_IRON.get(),64));p.getInventory().setItem(22,new ItemStack(WuXingItems.GLAZED_CRYSTAL.get(),4));p.getInventory().setItem(23,new ItemStack(WuXingItems.REFINE_SEAL.get(),1));p.getInventory().setItem(24,new ItemStack(WuXingItems.FIVE_ESSENCE.get(),64));
            var menu=new EquipmentWorkstationMenu(84,p.getInventory(),pos,false);var equipment=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());EquipmentCrafting.write(equipment).putInt("Refine",6);menu.getSlot(0).set(equipment);
            h.assertTrue(menu.clickMenuButton(p,menu.command(82)),"Protect strategy did not select");
            h.assertTrue(menu.value(7)==4&&(menu.value(11)&1)==1&&(menu.value(11)>>1)==9,"Seal-backed quote/fire not synchronized");
            p.getInventory().setItem(22,new ItemStack(WuXingItems.GLAZED_CRYSTAL.get(),5));menu.broadcastChanges();
            h.assertTrue(menu.value(7)==5&&(menu.value(11)&1)==0,"Original crystal surcharge must take priority when available");
            h.assertTrue(menu.slots.size()==38&&menu.getSlot(2).x==16&&menu.getSlot(2).y==177&&menu.getSlot(29).y==235,"Live inventory/hotbar geometry differs from skin");
            var n=EquipmentCrafting.write(equipment);n.putInt("Temper",1);n.putInt("TemperQuality",1);n.putInt("TemperBp",8000);n.putInt("ReforgeNatural",50);
            h.assertTrue(menu.clickMenuButton(p,menu.command(90)),"Reforge strategy did not select");
            h.assertTrue(menu.clickMenuButton(p,menu.command(100))&&n.contains("Pending")&&n.getInt("Temper")==1,"Generated result replaced original before acceptance");
            h.assertTrue(menu.value(9)==0&&menu.value(10)==2,"Pending result did not block another action");
            h.assertTrue(menu.clickMenuButton(p,menu.command(102))&&!n.contains("Pending")&&n.getInt("Temper")==1&&n.getInt("TemperBp")==8000,"Keep-original choice changed old temper");
            n.putInt("ReforgeNatural",50);h.assertTrue(menu.clickMenuButton(p,menu.command(100))&&n.contains("Pending"),"Second reforge did not produce candidate");int bp=n.getCompound("Pending").getInt("Bp");
            h.assertTrue(menu.clickMenuButton(p,menu.command(101))&&!n.contains("Pending")&&n.getInt("Temper")==0&&n.getInt("TemperBp")==bp,"Adopt choice did not apply exact candidate");
            menu.removed(p);
        }h.succeed();
    }
    @GameTest(template="empty") public static void compactMenusKeepInventoryAndShiftRouting(GameTestHelper h){
        for(boolean inscription:new boolean[]{true,false})try(var p=new GameTestPlayer(h.getLevel())){
            p.join();p.getInventory().clearContent();var pos=h.absolutePos(BlockPos.ZERO);
            p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            h.getLevel().setBlockAndUpdate(pos,(inscription?WuXingBlocks.INSCRIPTION_TABLE:WuXingBlocks.REFORGE_TABLE).get().defaultBlockState());
            var menu=new EquipmentWorkstationMenu(91,p.getInventory(),pos,inscription);int y=inscription?195:177;
            h.assertTrue(menu.slots.size()==38,"Compact menu lost inventory cells");
            for(int row=0;row<3;row++)for(int col=0;col<9;col++){
                var slot=menu.getSlot(2+row*9+col);h.assertTrue(slot.x==16+col*18&&slot.y==y+row*18,"Storage cells drifted outside compact panel");
            }
            for(int col=0;col<9;col++)h.assertTrue(menu.getSlot(29+col).x==16+col*18&&menu.getSlot(29+col).y==y+58,"Separate hotbar row/gap lost");
            h.assertTrue(menu.getSlot(0).x==141&&menu.getSlot(0).y==63,"Equipment hit slot drifted");
            h.assertTrue(!menu.getSlot(1).mayPlace(new ItemStack(Items.ENCHANTED_BOOK)),"Legacy input became insertable");
            h.assertTrue(inscription?menu.getSlot(1).x==113&&menu.getSlot(1).y==136:menu.getSlot(1).x<0&&menu.getSlot(1).y<0,"Legacy slot moved outside its intended state");
            p.getInventory().setItem(9,new ItemStack(Items.IRON_CHESTPLATE));menu.quickMoveStack(p,2);
            h.assertTrue(menu.equipment().is(Items.IRON_CHESTPLATE)&&p.getInventory().getItem(9).isEmpty(),"Shift click did not route eligible armor to input");
            menu.quickMoveStack(p,0);h.assertTrue(menu.equipment().isEmpty()&&p.getInventory().countItem(Items.IRON_CHESTPLATE)==1,"Shift return lost or duplicated equipment");
            p.getInventory().setItem(9,new ItemStack(Items.COBBLESTONE,12));menu.quickMoveStack(p,2);
            h.assertTrue(p.getInventory().getItem(9).isEmpty()&&p.getInventory().getItem(0).getCount()==12,"Storage to hotbar shift routing broke");
            menu.quickMoveStack(p,29);h.assertTrue(p.getInventory().getItem(0).isEmpty()&&p.getInventory().getItem(9).getCount()==12,"Hotbar to storage shift routing broke");
            menu.removed(p);
        }h.succeed();
    }
}
