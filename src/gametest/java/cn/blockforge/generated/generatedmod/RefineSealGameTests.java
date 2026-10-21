package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class RefineSealGameTests {
    private static GameTestPlayer player(GameTestHelper h){
        var p=new GameTestPlayer(h.getLevel());p.join();var data=GeneratedMod.data(p);
        data.wuxingxiantu$setRealm(8);data.wuxingxiantu$setLearnedFires(new int[]{9});data.wuxingxiantu$setEquippedFire(9);data.wuxingxiantu$setAura(1000);
        var profession=PlayerCrafts.get(p.server).profession(p.getUUID(),"refining");profession.putBoolean("Learned",true);profession.putInt("Rank",8);
        return p;
    }
    private static ItemStack equipment(){var stack=new ItemStack(Items.DIAMOND_SWORD);EquipmentCrafting.write(stack).putInt("Refine",6);return stack;}
    private static void materials(GameTestPlayer p,int crystals){
        p.getInventory().setItem(0,new ItemStack(WuXingItems.SPIRIT_STONE.get(),28));
        p.getInventory().setItem(1,new ItemStack(WuXingItems.DARK_IRON.get(),7));
        p.getInventory().setItem(2,new ItemStack(WuXingItems.GLAZED_CRYSTAL.get(),crystals));
        p.getInventory().setItem(3,new ItemStack(WuXingItems.REFINE_SEAL.get()));
    }
    private static long failureSeed(){for(long seed=0;seed<10000;seed++)if(net.minecraft.util.RandomSource.create(seed).nextInt(100)>=45)return seed;throw new AssertionError("No deterministic failure seed");}

    @GameTest(template="empty") public static void jadeSealReplacesOnlyProtectionCrystalAndKeepsExistingFailureRules(GameTestHelper h){
        try(var p=player(h)){
            materials(p,4);var equipment=equipment();var quote=EquipmentCrafting.quote(p,equipment,22);
            h.assertTrue(quote.ready()&&quote.crystal()==4&&quote.chance()==45&&EquipmentCrafting.usesRefineSeal(p,equipment,22),"Jade alternative changed base cost or chance");
            p.getRandom().setSeed(failureSeed());EquipmentCrafting.attempt(p,equipment,22);
            h.assertTrue(EquipmentCrafting.refine(equipment)==6&&EquipmentCrafting.read(equipment).getCompound("RefinePity").getInt("L7")==1,"Protected failure changed refinement or pity");
            h.assertTrue(p.getInventory().countItem(WuXingItems.REFINE_SEAL.get())==0&&p.getInventory().countItem(WuXingItems.GLAZED_CRYSTAL.get())==0
                    &&p.getInventory().countItem(WuXingItems.DARK_IRON.get())==0&&p.getInventory().countItem(WuXingItems.SPIRIT_STONE.get())==0,"Jade alternative did not charge exactly one seal plus base costs");
            h.assertTrue(!EquipmentCrafting.read(equipment).contains("RefineSeal")&&!equipment.getTag().contains("RefineSeal"),"Jade seal created a second persistent protection buff");
        }h.succeed();
    }
    @GameTest(template="empty") public static void originalExtraCrystalPaymentStillWorksWithoutAlsoSpendingJade(GameTestHelper h){
        try(var p=player(h)){
            materials(p,5);var equipment=equipment();var quote=EquipmentCrafting.quote(p,equipment,22);
            h.assertTrue(quote.ready()&&quote.crystal()==5&&!EquipmentCrafting.usesRefineSeal(p,equipment,22),"Original crystal surcharge was replaced");
            p.getRandom().setSeed(failureSeed());EquipmentCrafting.attempt(p,equipment,22);
            h.assertTrue(EquipmentCrafting.refine(equipment)==6&&EquipmentCrafting.read(equipment).getCompound("RefinePity").getInt("L7")==1
                    &&p.getInventory().countItem(WuXingItems.GLAZED_CRYSTAL.get())==0&&p.getInventory().countItem(WuXingItems.REFINE_SEAL.get())==1,"Original payment stacked jade cost or changed failure rules");
            h.assertTrue(!EquipmentCrafting.usesRefineSeal(p,equipment,21)&&!EquipmentCrafting.usesRefineSeal(p,equipment,20),"Jade affected non-protected actions");
        }h.succeed();
    }
    @GameTest(template="empty") public static void jadeDoesNotReplaceBaseCrystalsAndFailedChangeConsumesNothing(GameTestHelper h){
        try(var p=player(h)){
            materials(p,3);var equipment=equipment();var beforeEquipment=equipment.save(new CompoundTag());
            var before=p.getInventory().items.stream().map(ItemStack::copy).toList();
            h.assertTrue(!EquipmentCrafting.quote(p,equipment,22).ready(),"Jade bypassed missing base crystals");
            EquipmentCrafting.attempt(p,equipment,22);
            for(int i=0;i<36;i++)h.assertTrue(ItemStack.matches(before.get(i),p.getInventory().getItem(i)),"Missing base costs consumed inventory");
            h.assertTrue(beforeEquipment.equals(equipment.save(new CompoundTag())),"Rejected base costs mutated equipment");
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
            p.getInventory().setItem(0,new ItemStack(SpiritStoneGrade.EXQUISITE.item()));
            p.getInventory().setItem(1,new ItemStack(WuXingItems.DARK_IRON.get(),7));
            p.getInventory().setItem(2,new ItemStack(WuXingItems.GLAZED_CRYSTAL.get(),4));
            p.getInventory().setItem(3,new ItemStack(WuXingItems.REFINE_SEAL.get()));
            before=p.getInventory().items.stream().map(ItemStack::copy).toList();float aura=GeneratedMod.data(p).wuxingxiantu$getAura();
            h.assertTrue(EquipmentCrafting.quote(p,equipment,22).ready(),"Full-change fixture did not reach payment");
            String result=EquipmentCrafting.attempt(p,equipment,22);
            h.assertTrue(result.equals("灵石不足或背包无法容纳找零"),"Full change unexpectedly succeeded");
            for(int i=0;i<36;i++)h.assertTrue(ItemStack.matches(before.get(i),p.getInventory().getItem(i)),"Failed exact change consumed or moved materials");
            h.assertTrue(beforeEquipment.equals(equipment.save(new CompoundTag()))&&GeneratedMod.data(p).wuxingxiantu$getAura()==aura,"Failed payment changed equipment or aura");
        }h.succeed();
    }
}
