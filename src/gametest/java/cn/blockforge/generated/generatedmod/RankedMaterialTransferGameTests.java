package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class RankedMaterialTransferGameTests {
    private static ItemStack herb(String id,int years,int count) {
        var stack=new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath("wuxingxiantu",id)),count);
        HerbAge.setYears(stack,years);return stack;
    }
    private static Inventory bag(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new GameProfile(UUID.randomUUID(),"RankedTransfer")).getInventory();
    }
    private static AlchemyRecipe recipe() {
        var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(herb("herb_juling",0,1).getItem()));
        return new AlchemyRecipe(ResourceLocation.fromNamespaceAndPath("wuxingxiantu","qa_rank_transfer"),ingredients,List.of(3),
                MedicineTier.set(PillData.create(PillCatalog.GATHER_QI,PillData.Grade.LOW,PillData.Element.METAL,0),3),3,100,1,true);
    }
    private static List<ItemStack> snapshot(Inventory bag,SimpleContainer machine) {
        var result=new ArrayList<ItemStack>();for(int i=0;i<36;i++)result.add(bag.getItem(i).copy());
        for(int i=0;i<10;i++)result.add(machine.getItem(i).copy());return result;
    }
    private static boolean unchanged(List<ItemStack> before,Inventory bag,SimpleContainer machine) {
        var after=snapshot(bag,machine);
        for(int i=0;i<before.size();i++)if(!ItemStack.matches(before.get(i),after.get(i)))return false;
        return true;
    }
    @GameTest(template="empty")
    public static void mixedAgeAutofillUsesExactlyOneAgedGuide(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);var recipe=recipe();
        bag.setItem(0,herb("herb_juling",500,20));bag.setItem(1,herb("herb_juling",4500,3));
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.OK,"Mixed-age fill failed");
        h.assertTrue(recipe.matches(machine,h.getLevel())&&bag.getItem(0).getCount()==18&&bag.getItem(1).getCount()==2,"Did not conserve two young plus one aged herb");
        var before=snapshot(bag,machine);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.OK&&unchanged(before,bag,machine),"Repeated fill spent extra guide");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void completeYoungBatchSwapsOneSpecimenAndPreservesNbt(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);var recipe=recipe();
        var young=herb("herb_juling",500,3);young.getOrCreateTag().putString("lineage","kept");machine.setItem(0,young);
        bag.setItem(5,herb("herb_juling",4500,1));
        machine.setItem(8,new ItemStack(Items.BLAZE_POWDER));machine.setItem(9,new ItemStack(Items.DIAMOND));
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.OK&&recipe.matches(machine,h.getLevel()),"Existing batch did not exchange guide");
        int returned=0;for(int i=0;i<36;i++)if(HerbAge.years(bag.getItem(i))==500&&bag.getItem(i).getOrCreateTag().getString("lineage").equals("kept"))returned+=bag.getItem(i).getCount();
        h.assertTrue(returned==1&&machine.getItem(0).getCount()==2&&machine.getItem(8).is(Items.BLAZE_POWDER)&&machine.getItem(9).is(Items.DIAMOND),"Exchange changed tags or special slots");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void missingOrUnrelatedAgedHerbFailsAtomically(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);var recipe=recipe();
        bag.setItem(0,herb("herb_juling",500,20));bag.setItem(1,herb("herb_tianji",8500,1));
        var before=snapshot(bag,machine);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.AGE_MISSING&&unchanged(before,bag,machine),"Unrelated guide passed or failed transfer mutated inventories");
        machine.setItem(0,herb("herb_juling",500,3));machine.setItem(1,herb("herb_tianji",8500,1));
        h.assertTrue(recipe.tryIngredientReservation(machine).isPresent()&&recipe.tryReservation(machine).isEmpty(),"Idle high-age herb bypassed actual recipe allocation");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void fullFurnaceCannotLoseItemsDuringGuideSwap(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);
        machine.setItem(0,herb("herb_juling",500,64));for(int i=1;i<8;i++)machine.setItem(i,new ItemStack(Items.COBBLESTONE,64));
        bag.setItem(0,herb("herb_juling",4500,1));var before=snapshot(bag,machine);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe(),false)==AlchemyMaterialTransfer.Result.SLOTS_FULL&&unchanged(before,bag,machine),"Full furnace lost or partially moved herbs");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void spentGuideFreesBagSlotForReturnedYoungHerb(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);
        for(int i=0;i<36;i++)bag.setItem(i,new ItemStack(Items.STONE,64));
        bag.setItem(0,herb("herb_juling",4500,1));machine.setItem(0,herb("herb_juling",500,3));
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe(),false)==AlchemyMaterialTransfer.Result.OK&&recipe().matches(machine,h.getLevel()),"Using last guide did not free its bag slot");
        h.assertTrue(HerbAge.years(bag.getItem(0))==500&&bag.getItem(0).getCount()==1,"Returned young herb was lost");h.succeed();
    }
    @GameTest(template="empty")
    public static void occupiedGuideStackCannotDestroyReturnedYoungHerb(GameTestHelper h) {
        var bag=bag(h);var machine=new SimpleContainer(10);
        for(int i=0;i<36;i++)bag.setItem(i,new ItemStack(Items.STONE,64));
        bag.setItem(0,herb("herb_juling",4500,2));machine.setItem(0,herb("herb_juling",500,3));var before=snapshot(bag,machine);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe(),false)==AlchemyMaterialTransfer.Result.INVENTORY_FULL&&unchanged(before,bag,machine),"Failed return modified full inventories");h.succeed();
    }
}
