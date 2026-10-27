package cn.blockforge.generated.generatedmod;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.*;
import cn.blockforge.generated.generatedmod.alchemy.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyKnowledgeMenuGameTests {
    @GameTest(template="empty")
    public static void formulaKnowledgeSurvivesSignedShortMenuTransport(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        var saved=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy();
        try {
            FormulaKnowledge.reset(player);FormulaKnowledge.unlock(player);
            var furnace=new AlchemyFurnaceBlockEntity(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_FURNACE.get(),
                    h.absolutePos(net.minecraft.core.BlockPos.ZERO),WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState());
            furnace.setLevel(h.getLevel());
            var server=new AlchemyFurnaceMenu(1,player.getInventory(),furnace);
            int[] packet=new int[14];
            server.addSlotListener(new ContainerListener() {
                public void slotChanged(AbstractContainerMenu menu,int slot,ItemStack stack) {}
                public void dataChanged(AbstractContainerMenu menu,int index,int value) {packet[index]=(short)value;}
            });
            for(var learned:PillCatalog.values()) {
                FormulaKnowledge.reset(player);FormulaKnowledge.unlock(player);
                var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu",learned.id);
                FormulaKnowledge.learn(player,id,false);server.broadcastChanges();
                var client=new AlchemyFurnaceMenu(2,player.getInventory(),furnace);
                for(int i=0;i<packet.length;i++)client.setData(i,packet[i]);
                // Client display must use the transported snapshot, not local persistent data.
                FormulaKnowledge.reset(player);
                h.assertTrue(client.formulaUnlocked(),"Unlocked flag lost in transport");
                for(var candidate:PillCatalog.values()) {
                    var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(Items.APPLE));
                    var candidateId=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu",candidate.id);
                    var recipe=new AlchemyRecipe(candidateId,ingredients,java.util.List.of(1),new ItemStack(Items.APPLE),0,20,1,false,
                            candidateId,1,0,0,1,1);
                    h.assertTrue(client.knowsFormula(recipe)==(candidate==learned),"Formula bit changed: "+candidate);
                }
            }
            h.succeed();
        } finally { player.getPersistentData().put(Player.PERSISTED_NBT_TAG,saved); }
    }
}
