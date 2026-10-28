package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class IntegrationAlchemyGameTests {
    private static AlchemyRecipe recipe(GameTestHelper h){return (AlchemyRecipe)h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("wuxingxiantu:alchemy_aura_pill_low")).orElseThrow();}
    private static ItemStack herb(String id){var s=new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"herb_"+id)),7);HerbAge.setYears(s,2500);s.getOrCreateTag().putBoolean(HerbPreparation.KEY,true);return s;}
    private static GameTestPlayer player(GameTestHelper h){
        var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(BlockPos.ZERO);p.setPos(pos.getX()+.5,210,pos.getZ()+.5);
        var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(3200);d.wuxingxiantu$learnDivineArt(HeavenlyFurnace.ID);d.wuxingxiantu$learnFire(1);d.wuxingxiantu$setEquippedFire(1);FormulaKnowledge.learnAll(p);return p;
    }
    private static void materials(GameTestPlayer p,AlchemyRecipe r){for(int i=0;i<r.getIngredients().size();i++)p.getInventory().setItem(30+i,r.getIngredients().get(i).getItems()[0].copyWithCount(r.counts().get(i)));p.getInventory().setItem(0,herb("xingchen"));p.getInventory().setItem(1,herb("hanbing"));p.getInventory().setItem(2,herb("lingdao"));p.getInventory().setItem(3,herb("tianji"));}
    @SuppressWarnings("unchecked") private static UUID token(GameTestPlayer p){try{var field=HeavenlyFurnace.class.getDeclaredField("OFFERS");field.setAccessible(true);var offer=((Map<UUID,?>)field.get(null)).get(p.getUUID());var method=offer.getClass().getDeclaredMethod("token");method.setAccessible(true);return (UUID)method.invoke(offer);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private static List<ItemStack> snapshot(GameTestPlayer p){return p.getInventory().items.stream().map(ItemStack::copy).toList();}
    private static void unchanged(GameTestHelper h,GameTestPlayer p,List<ItemStack> saved){for(int i=0;i<saved.size();i++)h.assertTrue(ItemStack.matches(saved.get(i),p.getInventory().getItem(i)),"Inventory changed at slot "+i);}

    private static void sameContents(GameTestHelper h,GameTestPlayer p,List<ItemStack> saved){
        h.assertTrue(saved.stream().mapToInt(ItemStack::getCount).sum()==p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum(),"Refund changed total item count");
        for(var expected:saved)if(!expected.isEmpty()){
            int count=saved.stream().filter(s->ItemStack.isSameItemSameTags(s,expected)).mapToInt(ItemStack::getCount).sum();
            int actual=p.getInventory().items.stream().filter(s->ItemStack.isSameItemSameTags(s,expected)).mapToInt(ItemStack::getCount).sum();
            h.assertTrue(count==actual,"Refund lost item identity, years or prepared NBT");
        }
    }
    @GameTest(template="empty")
    public static void realHeavenlyCastRejectsInvalidAuxiliariesAtomicallyAndSealsOnce(GameTestHelper h){
        try(var p=player(h)){try{
            var r=recipe(h);materials(p,r);var before=snapshot(p);float mana=GeneratedMod.data(p).getCurrentMana();
            h.assertTrue(HeavenlyFurnace.open(p),"Offer unavailable");var token=token(p);
            for(var invalid:List.of(new int[]{0,0,2},new int[]{30,1,2},new int[]{-2,1,2},new int[]{0,1,36})){
                h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId(),invalid),"Forged auxiliary selection accepted");unchanged(h,p,before);
                h.assertTrue(!HeavenlyFurnace.active(p)&&GeneratedMod.data(p).getCurrentMana()==mana,"Rejected request charged mana or created a cast");
            }
            h.assertTrue(HeavenlyFurnace.start(p,token,r.getId(),new int[]{0,1,2}),"Valid selected auxiliaries rejected");
            for(int i=0;i<3;i++)h.assertTrue(p.getInventory().getItem(i).getCount()==6,"Cast did not consume one plant per role");
            h.assertTrue(ItemStack.matches(p.getInventory().getItem(3),before.get(3)),"Unselected herb consumed");
            var paid=snapshot(p);float charged=GeneratedMod.data(p).getCurrentMana();
            h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId(),new int[]{0,1,2}),"Replayed offer started twice");unchanged(h,p,paid);
            HeavenlyFurnace.cancel(p);var sealed=snapshot(p);h.assertTrue(p.getInventory().items.stream().anyMatch(x->x.is(WuXingItems.ALCHEMY_RESIDUE.get())),"No sealed residue");HeavenlyFurnace.cancel(p);unchanged(h,p,sealed);
            h.assertTrue(GeneratedMod.data(p).getCurrentMana()==charged&&charged<mana,"Cancellation changed the existing spent-mana rule");
        }finally{HeavenlyFurnace.cancel(p);}}h.succeed();
    }

    @GameTest(template="empty")
    public static void fullInventoryCancellationDropsExactTaggedEscrowWithoutDuplication(GameTestHelper h){
        try(var p=player(h)){try{
            var r=recipe(h);materials(p,r);String marker=UUID.randomUUID().toString();
            for(var stack:p.getInventory().items)if(!stack.isEmpty())stack.getOrCreateTag().putString("IntegrationEscrow",marker);
            int before=p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();h.assertTrue(HeavenlyFurnace.open(p),"Offer unavailable");
            h.assertTrue(HeavenlyFurnace.start(p,token(p),r.getId(),new int[]{0,1,2}),"Cast rejected");
            int paid=before-p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
            HeavenlyFurnace.cancel(p);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(8),e->marker.equals(e.getItem().getOrCreateTag().getString("IntegrationEscrow")));
            h.assertTrue(drops.isEmpty(),"V5 cancellation refunded consumed inputs");
            var doc=cn.blockforge.generated.generatedmod.alchemy.batch.PortableBatch.current(p);h.assertTrue(doc.getString("State").equals("READY"),"Full inventory did not retain sealed result");
            HeavenlyFurnace.cancel(p);h.assertTrue(cn.blockforge.generated.generatedmod.alchemy.batch.PortableBatch.current(p).getString("State").equals("READY"),"Repeated cancel lost pending result");
            p.getInventory().setItem(0,ItemStack.EMPTY);HeavenlyFurnace.cancel(p);h.assertTrue(p.getInventory().getItem(0).is(WuXingItems.ALCHEMY_RESIDUE.get()),"Retained result not delivered when space became available");
            drops.forEach(ItemEntity::discard);
        }finally{HeavenlyFurnace.cancel(p);}}h.succeed();
    }

    @GameTest(template="empty")
    public static void failedOrdinaryBatchConsumesOneAuxiliaryPerRoleAndCannotSettleAgain(GameTestHelper h){
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);var r=recipe(h);
        for(int i=0;i<r.getIngredients().size();i++)furnace.setItem(i,r.getIngredients().get(i).getItems()[0].copyWithCount(r.counts().get(i)));
        furnace.setItem(3,herb("xingchen"));furnace.setItem(4,herb("hanbing"));furnace.setItem(5,herb("lingdao"));
        furnace.reserve(r);furnace.startBrewing(1,21);var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        h.assertTrue(furnace.failExperiment()&&furnace.completedResult().is(WuXingItems.PILL_ASH.get()),"Failure did not settle into residue");
        for(int i=3;i<=5;i++)h.assertTrue(furnace.getItem(i).getCount()==6,"Failure consumed wrong auxiliary count");
        h.assertTrue(!furnace.failExperiment()&&!furnace.complete(r.getResultItem(h.getLevel().registryAccess())),"Failed batch produced a second outcome");
        furnace.load(furnace.saveWithoutMetadata());h.assertTrue(furnace.completedResult().getCount()==1&&!furnace.failExperiment(),"Reload settled failed batch again");h.succeed();
    }
}
