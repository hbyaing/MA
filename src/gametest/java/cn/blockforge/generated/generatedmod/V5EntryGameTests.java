package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.alchemy.inputs.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.plants.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.event.TickEvent;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class V5EntryGameTests {
    static AlchemyRecipe recipe(GameTestPlayer p){return (AlchemyRecipe)p.level().getRecipeManager().byKey(ResourceLocation.parse("wuxingxiantu:alchemy_aura_pill_low")).orElseThrow();}
    static void supply(GameTestPlayer p){var r=recipe(p);for(int i=0;i<r.getIngredients().size();i++){var item=r.getIngredients().get(i).getItems()[0].copyWithCount(r.counts().get(i));if(HerbAge.definition(item)!=null)HerbAge.setYears(item,1500);p.getInventory().setItem(30+i,item);}}
    static GameTestPlayer player(GameTestHelper h){var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(BlockPos.ZERO);p.setPos(pos.getX()+.5,210,pos.getZ()+.5);p.setNoGravity(true);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(3200);d.wuxingxiantu$learnDivineArt(HeavenlyFurnace.ID);d.wuxingxiantu$learnFire(1);d.wuxingxiantu$setEquippedFire(1);FormulaKnowledge.learnAll(p);supply(p);return p;}
    @SuppressWarnings("unchecked") static UUID token(GameTestPlayer p){try{var f=HeavenlyFurnace.class.getDeclaredField("OFFERS");f.setAccessible(true);var offer=((Map<UUID,?>)f.get(null)).get(p.getUUID());var m=offer.getClass().getDeclaredMethod("token");m.setAccessible(true);return (UUID)m.invoke(offer);}catch(Exception e){throw new AssertionError(e);}}
    static boolean start(GameTestPlayer p){return HeavenlyFurnace.open(p)&&HeavenlyFurnace.start(p,token(p),recipe(p).getId());}
    static void completePortable(GameTestPlayer p){
        var j=new BatchJournal(p.server);var doc=PortableBatch.current(p);doc.getCompound("Visual").putLong("Start",p.level().getGameTime()-HeavenlyFurnace.GATHER_TICKS);
        for(int i=0;i<64&&doc.getString("State").equals("ACTIVE");i++){doc.putLong("LastWorldTick",p.level().getGameTime()-20);j.write(doc);doc=PortableBatch.advance(p);}
        p.tickCount=0;HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
    }
    @GameTest(template="empty") public static void fixedFormulaCreatesV5WithExactPrimaryAndAuxiliaryCounts(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var r=recipe(f.player());FormulaKnowledge.learnAll(f.player());for(int i=0;i<8;i++)f.furnace().setItem(i,ItemStack.EMPTY);
        for(int i=0;i<r.getIngredients().size();i++){var s=r.getIngredients().get(i).getItems()[0].copyWithCount(r.counts().get(i)+2);if(HerbAge.definition(s)!=null)HerbAge.setYears(s,1500);f.furnace().setItem(i,s);}
        var aux=new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("wuxingxiantu:herb_xingchen")),7);HerbAge.setYears(aux,2500);f.furnace().setItem(5,aux);
        h.assertTrue(f.furnace().startV5Batch(f.player(),r),"Fixed recipe did not enter V5");var d=f.furnace().v5ServerRecord();h.assertTrue(d.getString("Entry").equals("ordinary_formula")&&!d.contains("Result")&&V5Batch.core(d).tick()==0,"Formula pre-rolled legacy output");
        for(int i=0;i<r.getIngredients().size();i++)h.assertTrue(f.furnace().getItem(i).getCount()==2,"Consumed unreserved main ingredients");h.assertTrue(f.furnace().getItem(5).getCount()==6,"Auxiliary not paid exactly once");h.assertTrue(d.getList("Materials",Tag.TAG_COMPOUND).size()==4,"Primary/auxiliary provenance missing");
    }h.succeed();}
    @GameTest(template="empty") public static void inertFormulaIngredientHasMassButNoInventedEffect(GameTestHelper h){var in=AlchemyInputs.batchWithInert(List.of(new ItemStack(Items.REDSTONE,3)),InputDefinitions.current());h.assertTrue(in.stocks().size()==1&&in.stocks().get("herb_inert")==in.referenceMass(),"Non-herb material minted useful resources");h.succeed();}
    @GameTest(template="empty") public static void portableAdmissionIsDurableAndRecoveryDoesNotChargeAgain(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Cannot start portable V5");var d=PortableBatch.current(p);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();h.assertTrue(d.getBoolean("InputCommitted")&&d.contains("EffectPolicy")&&!d.contains("Result"),"Payment or deferred result missing");
        PortableBatch.restore(p);PortableBatch.restore(p);h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getAura()==mana&&p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"Recovery duplicated payment or material");HeavenlyFurnace.cancel(p);
    }h.succeed();}
    @GameTest(template="empty") public static void portablePaymentAckInterruptionUsesSavedPlayerReceipt(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Start rejected");var j=new BatchJournal(p.server);var d=PortableBatch.current(p);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();d.putBoolean("InputCommitted",false);j.write(d);PortableBatch.restore(p);h.assertTrue(PortableBatch.current(p).getBoolean("InputCommitted")&&GeneratedMod.data(p).wuxingxiantu$getAura()==mana,"Acknowledgement retry charged twice");HeavenlyFurnace.cancel(p);}h.succeed();}
    @GameTest(template="empty") public static void portableAdmissionBeforePaymentCanRecoverOnce(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Start rejected");var j=new BatchJournal(p.server);var d=PortableBatch.current(p);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();
        for(var e:d.getList("PlayerInputs",Tag.TAG_COMPOUND)){var row=(CompoundTag)e;p.getInventory().setItem(row.getInt("Slot"),ItemStack.of(row.getCompound("Before")));}
        GeneratedMod.data(p).wuxingxiantu$setAura(mana+d.getInt("PlayerManaCost"));p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).remove("V5EntryReceipts");d.putBoolean("InputCommitted",false);j.write(d);PortableBatch.restore(p);PortableBatch.restore(p);h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getAura()==mana&&p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"Prepayment recovery did not consume exactly once");HeavenlyFurnace.cancel(p);}h.succeed();}
    @GameTest(template="empty") public static void portableFullInventoryRetainsCanonicalResultAndNeverDropsDuplicate(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Start rejected");for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.DIAMOND,64));completePortable(p);var d=PortableBatch.current(p);h.assertTrue(d.getString("State").equals("READY"),"Full inventory discarded output");var expected=V5Batch.result(d);PortableBatch.advance(p);h.assertTrue(PortableBatch.current(p).getString("State").equals("READY"),"Full inventory retried into duplicate");p.getInventory().setItem(0,ItemStack.EMPTY);PortableBatch.advance(p);h.assertTrue(ItemStack.matches(expected,p.getInventory().getItem(0)),"Output changed on retry");int count=p.getInventory().getItem(0).getCount();HeavenlyFurnace.cancel(p);h.assertTrue(p.getInventory().getItem(0).getCount()==count,"Cancel after claim duplicated outcome");}h.succeed();}
    @GameTest(template="empty") public static void ordinaryAndHeavenlyShareIdenticalCoreAndEffectSettlement(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=V5Batch.prepare(f.furnace(),f.player());var b=a.copy();b.putString("Entry","heavenly");var level=h.getLevel();
        for(int i=0;i<64&&a.getString("State").equals("ACTIVE");i++){a.putLong("LastWorldTick",level.getGameTime()-20);b.putLong("LastWorldTick",level.getGameTime()-20);a=V5Batch.advance(a,level,f.pos());b=V5Batch.advance(b,level,f.pos());}
        h.assertTrue(Arrays.equals(a.getByteArray("Core"),b.getByteArray("Core"))&&a.getCompound("PillResult").equals(b.getCompound("PillResult")),"Entry adapter changed shared simulation or nine-grade effects");
    }h.succeed();}
    @GameTest(template="empty") public static void logoutAndLoginKeepSameBatchWithoutRefundOrReroll(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Start rejected");var d=PortableBatch.current(p);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();HeavenlyFurnace.logout(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));HeavenlyFurnace.login(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));var next=PortableBatch.current(p);h.assertTrue(d.getUUID("Id").equals(next.getUUID("Id"))&&Arrays.equals(d.getByteArray("Core"),next.getByteArray("Core"))&&GeneratedMod.data(p).wuxingxiantu$getAura()==mana,"Relog refunded, rerolled or charged again");HeavenlyFurnace.cancel(p);}h.succeed();}
    @GameTest(template="empty") public static void portableActiveIndexPreventsOrdinaryConcurrentStart(GameTestHelper h){try(var p=player(h)){h.assertTrue(start(p),"Start rejected");h.assertTrue(new BatchJournal(p.server).activeCount(p.getUUID())==1,"Portable task absent from shared active index");HeavenlyFurnace.cancel(p);h.assertTrue(new BatchJournal(p.server).activeCount(p.getUUID())==0,"Sealed task kept active count");}h.succeed();}
    @GameTest(template="empty") public static void portableMoonTransferPreservesBottleAndFiniteRemainder(GameTestHelper h){try(var p=player(h)){
        p.getInventory().clearContent();var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4);HerbAge.setYears(herb,1500);p.getInventory().setItem(0,herb);
        var bottle=new ItemStack(WuXingBlocks.MOON_BOTTLE_ITEM.get());var moon=new CompoundTag();moon.putInt("Schema",1);var sources=new CompoundTag();sources.putLong("minecraft:overworld/moon/0",2000);moon.put("Sources",sources);var be=new CompoundTag();be.put("MoonBottle",moon);bottle.addTagElement("BlockEntityTag",be);p.getInventory().setItem(35,bottle);
        var shell=new CompoundTag();shell.putDouble("X",p.getX());shell.putDouble("Y",p.getY());shell.putDouble("Z",p.getZ());shell.putLong("Start",p.level().getGameTime());var used=new int[36];used[0]=4;used[35]=1;
        h.assertTrue(PortableBatch.start(p,recipe(p),used,shell),"Portable moon admission failed");var d=PortableBatch.current(p);h.assertTrue(V5Batch.core(d).stocks().get("moon_essence")==1000,"Moon stock not released exactly once");
        h.assertTrue(p.getInventory().getItem(35).is(WuXingBlocks.MOON_BOTTLE_ITEM.get())&&MoonBottleData.load(BlockItem.getBlockEntityData(p.getInventory().getItem(35)).getCompound("MoonBottle")).total()==1000,"Physical bottle or unused moon lost");
        PortableBatch.restore(p);h.assertTrue(MoonBottleData.load(BlockItem.getBlockEntityData(p.getInventory().getItem(35)).getCompound("MoonBottle")).total()==1000,"Recovery released moon again");HeavenlyFurnace.cancel(p);
    }h.succeed();}
    @GameTest(template="empty") public static void changedUncommittedInputsQuarantineWithoutTakingReplacement(GameTestHelper h){try(var p=player(h)){
        h.assertTrue(start(p),"Start rejected");var d=PortableBatch.current(p);var j=new BatchJournal(p.server);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();d.putBoolean("InputCommitted",false);j.write(d);p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).remove("V5EntryReceipts");p.getInventory().setItem(30,new ItemStack(Items.DIAMOND));
        boolean rejected=false;try{PortableBatch.restore(p);}catch(IllegalStateException expected){rejected=true;}h.assertTrue(rejected&&p.getInventory().getItem(30).is(Items.DIAMOND)&&GeneratedMod.data(p).wuxingxiantu$getAura()==mana,"Unconfirmed input mismatch consumed replacement or charged again");d.putBoolean("InputCommitted",true);j.write(d);HeavenlyFurnace.cancel(p);
    }h.succeed();}

}
