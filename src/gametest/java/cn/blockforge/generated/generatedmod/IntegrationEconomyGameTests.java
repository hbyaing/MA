package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.economy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class IntegrationEconomyGameTests {
    @GameTest(template="empty")
    public static void registeredExchangeRecipesConserveAllFourCurrencyValues(GameTestHelper h) {
        var grades=SpiritStoneGrade.values();
        for(int i=1;i<grades.length;i++) for(String operation:List.of("compress","split")) {
            var id=ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"spirit_stone_"+grades[i].id()+"_"+operation);
            var recipe=h.getLevel().getRecipeManager().byKey(id).orElseThrow();
            long input=0;
            for(var ingredient:recipe.getIngredients()) {
                var choices=ingredient.getItems();h.assertTrue(choices.length==1,"Exchange accepts unexpected alternative currency: "+id);
                var grade=SpiritStoneGrade.of(choices[0].getItem());h.assertTrue(grade!=null,"Exchange consumes noncurrency: "+id);input+=grade.value;
            }
            var output=recipe.getResultItem(h.getLevel().registryAccess());
            h.assertTrue(SpiritCurrency.value(List.of(output))==input,"Exchange creates/destroys value: "+id);
            h.assertTrue(operation.equals("compress")?recipe.getIngredients().size()==9&&output.is(grades[i].item())&&output.getCount()==1
                :recipe.getIngredients().size()==1&&output.is(grades[i-1].item())&&output.getCount()==9,"Exchange ratio changed: "+id);
        }h.succeed();
    }

    @GameTest(template="empty")
    public static void mixedPaymentsConserveValueAndPreserveUnrelatedTaggedItems(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var inventory=player.getInventory();var random=new Random(20261021);
            for(int attempt=0;attempt<128;attempt++) {
                inventory.clearContent();var preserved=new ItemStack(Items.DIAMOND_SWORD);
                preserved.getOrCreateTag().putString("integration_marker","unchanged");inventory.setItem(0,preserved.copy());
                int slot=1;for(var grade:SpiritStoneGrade.values())inventory.setItem(slot++,new ItemStack(grade.item(),1+random.nextInt(16)));
                long before=SpiritCurrency.value(inventory.items);long cost=attempt==0?before:random.nextInt((int)before+1);
                h.assertTrue(SpiritCurrency.pay(inventory,cost),"Solvent payment with free change slots failed");
                h.assertTrue(SpiritCurrency.value(inventory.items)==before-cost,"Mixed payment changed total value");
                h.assertTrue(ItemStack.matches(inventory.getItem(0),preserved),"Payment changed unrelated tagged item");
                var snapshot=inventory.items.stream().map(ItemStack::copy).toList();
                h.assertTrue(!SpiritCurrency.pay(inventory,before-cost+1),"Insolvent payment succeeded");
                for(int i=0;i<snapshot.size();i++)h.assertTrue(ItemStack.matches(snapshot.get(i),inventory.getItem(i)),"Failed payment changed inventory");
            }
        }h.succeed();
    }
}
