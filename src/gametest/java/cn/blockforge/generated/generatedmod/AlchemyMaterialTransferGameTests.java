package cn.blockforge.generated.generatedmod;

import net.minecraft.gametest.framework.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyMaterialTransferGameTests {
    @GameTest(template="empty")
    public static void fillAndClearConserveItemsAndRejectPartialTransfers(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        var bag=player.getInventory();
        for(int i=0;i<36;i++)bag.setItem(i,ItemStack.EMPTY);
        var machine=new SimpleContainer(10);
        machine.setItem(8,new ItemStack(Items.BLAZE_POWDER));machine.setItem(9,new ItemStack(Items.DIAMOND));
        var inputs=NonNullList.<Ingredient>create();inputs.add(Ingredient.of(Items.APPLE));inputs.add(Ingredient.of(Items.REDSTONE));
        var recipe=new AlchemyRecipe(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","transfer_test"),inputs,
                java.util.List.of(4,2),new ItemStack(Items.GOLDEN_APPLE),0,100,5,false);
        machine.setItem(0,new ItemStack(Items.APPLE,1));
        var tagged=new ItemStack(Items.APPLE,8);tagged.getOrCreateTag().putString("marker","kept");
        bag.setItem(0,tagged);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.MISSING
                &&bag.getItem(0).getCount()==8&&machine.getItem(0).getCount()==1,"Missing ingredient partially moved items");
        bag.setItem(1,new ItemStack(Items.REDSTONE,8));
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.OK&&recipe.matches(machine,h.getLevel()),"Fill failed");
        h.assertTrue(bag.getItem(0).getCount()==5&&bag.getItem(1).getCount()==6,"Fill moved more than one batch");
        h.assertTrue(machine.getItem(1).getTag().getString("marker").equals("kept"),"Fill lost stack NBT");
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,recipe,false)==AlchemyMaterialTransfer.Result.OK&&bag.getItem(0).getCount()==5,"Repeated fill duplicated or overfilled");
        for(int i=0;i<36;i++)bag.setItem(i,new ItemStack(Items.STONE,64));
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,null,true)==AlchemyMaterialTransfer.Result.INVENTORY_FULL&&recipe.matches(machine,h.getLevel()),"Full inventory lost materials");
        for(int i=0;i<36;i++)bag.setItem(i,ItemStack.EMPTY);
        h.assertTrue(AlchemyMaterialTransfer.move(machine,bag,null,true)==AlchemyMaterialTransfer.Result.OK,"Clear failed");
        for(int i=0;i<8;i++)h.assertTrue(machine.getItem(i).isEmpty(),"Clear left materials");
        h.assertTrue(machine.getItem(8).is(Items.BLAZE_POWDER)&&machine.getItem(9).is(Items.DIAMOND),"Clear moved ignition or core");
        int apples=0,redstone=0;for(int i=0;i<36;i++){if(bag.getItem(i).is(Items.APPLE))apples+=bag.getItem(i).getCount();if(bag.getItem(i).is(Items.REDSTONE))redstone+=bag.getItem(i).getCount();}
        h.assertTrue(apples==4&&redstone==2,"Clear duplicated or lost materials");
        h.succeed();
    }
}
