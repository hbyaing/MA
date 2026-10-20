package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.*;
import net.minecraftforge.event.TickEvent;
import java.util.*;
@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class HeavenlyFurnaceGameTests {
    @GameTest(template="empty") public static void expandedCastingRangeIncludesHeight(GameTestHelper h){
        var origin=new net.minecraft.world.phys.Vec3(10,120,-7);
        h.assertTrue(HeavenlyFurnace.withinCastingRange(origin.add(32,24,0),origin),"Inclusive horizontal/vertical limits");
        h.assertTrue(HeavenlyFurnace.withinCastingRange(origin.add(0,-24,32),origin),"Downward movement allowed");
        h.assertTrue(!HeavenlyFurnace.withinCastingRange(origin.add(32.01,0,0),origin),"Horizontal overflow rejected");
        h.assertTrue(!HeavenlyFurnace.withinCastingRange(origin.add(0,24.01,0),origin),"Vertical overflow rejected");
        h.assertTrue(!HeavenlyFurnace.withinCastingRange(origin.add(24,0,24),origin),"Radius is circular");h.succeed();
    }
    private static AlchemyRecipe recipe(GameTestHelper h){return (AlchemyRecipe)h.getLevel().getRecipeManager().byKey(new ResourceLocation("wuxingxiantu:alchemy_aura_pill_low")).orElseThrow();}
    private static GameTestPlayer player(GameTestHelper h){
        var p=new GameTestPlayer(h.getLevel());p.join();p.setPos(h.absolutePos(net.minecraft.core.BlockPos.ZERO).getX()+.5,210,h.absolutePos(net.minecraft.core.BlockPos.ZERO).getZ()+.5);
        var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(3200);d.wuxingxiantu$learnDivineArt(HeavenlyFurnace.ID);d.wuxingxiantu$learnFire(1);d.wuxingxiantu$setEquippedFire(1);FormulaKnowledge.learnAll(p);return p;
    }
    private static void materials(GameTestPlayer p,AlchemyRecipe r){for(int i=0;i<r.getIngredients().size();i++){var item=r.getIngredients().get(i).getItems()[0].copyWithCount(r.counts().get(i));p.getInventory().setItem(30+i,item);}}
    @SuppressWarnings("unchecked") private static UUID token(GameTestPlayer p){try{
        var f=HeavenlyFurnace.class.getDeclaredField("OFFERS");f.setAccessible(true);Object offer=((Map<UUID,?>)f.get(null)).get(p.getUUID());var m=offer.getClass().getDeclaredMethod("token");m.setAccessible(true);return (UUID)m.invoke(offer);
    }catch(Exception e){throw new AssertionError(e);}}
    @SuppressWarnings("unchecked") private static CompoundTag running(GameTestPlayer p){try{var f=HeavenlyFurnace.class.getDeclaredField("RUNNING");f.setAccessible(true);return ((Map<UUID,CompoundTag>)f.get(null)).get(p.getUUID());}catch(Exception e){throw new AssertionError(e);}}
    private static int count(GameTestPlayer p){return p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();}
    @GameTest(template="empty") public static void matchingReassignsOverlappingIngredientsAndUsesWholeBackpack(GameTestHelper h){
        var in=NonNullList.<Ingredient>create();in.add(Ingredient.of(Items.APPLE,Items.CARROT));in.add(Ingredient.of(Items.APPLE));
        var r=new AlchemyRecipe(new ResourceLocation("wuxingxiantu:test_flow"),in,List.of(2,2),new ItemStack(Items.DIAMOND),0,20,0,false);
        var inventory=new SimpleContainer(36);inventory.setItem(1,new ItemStack(Items.APPLE,2));inventory.setItem(35,new ItemStack(Items.CARROT,2));
        int[] used=HeavenlyFurnace.allocation(r,inventory);h.assertTrue(used!=null&&used[1]==2&&used[35]==2,"Overlapping ingredients not reassigned across entire backpack");
        h.assertTrue(inventory.getItem(1).getCount()==2,"Read-only match mutated inventory");inventory.getItem(35).shrink(1);h.assertTrue(HeavenlyFurnace.allocation(r,inventory)==null,"One missing item accepted");h.succeed();
    }
    @GameTest(template="empty") public static void validStartChargesOnceAndCancellationRefundsExactPreparedItems(GameTestHelper h){
        try(var p=player(h)){
            var r=recipe(h);materials(p,r);p.getInventory().getItem(30).getOrCreateTag().putBoolean(HerbPreparation.KEY,true);
            int count=count(p);float mana=GeneratedMod.data(p).wuxingxiantu$getAura();h.assertTrue(HeavenlyFurnace.open(p),"Art did not open");UUID token=token(p);
            h.assertTrue(HeavenlyFurnace.start(p,token,r.getId()),"Valid brew rejected");h.assertTrue(count(p)==0&&HeavenlyFurnace.active(p),"Materials were not escrowed");
            h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getAura()==mana-HeavenlyFurnace.cost(r,GeneratedMod.data(p)),"Mana charged incorrectly");
            h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId()),"Replay started second brew");
            HeavenlyFurnace.cancel(p);h.assertTrue(count(p)==count&&!HeavenlyFurnace.active(p),"Cancel did not refund exactly once");
            h.assertTrue(p.getInventory().items.stream().anyMatch(HerbPreparation::prepared),"Prepared herb NBT lost");HeavenlyFurnace.cancel(p);h.assertTrue(count(p)==count,"Repeated cancel duplicated materials");
        }h.succeed();
    }
    @GameTest(template="empty") public static void confirmationRechecksKnowledgeInventoryManaAndNonce(GameTestHelper h){
        try(var p=player(h)){
            var r=recipe(h);materials(p,r);h.assertTrue(HeavenlyFurnace.open(p),"Offer failed");var token=token(p);
            h.assertTrue(!HeavenlyFurnace.start(p,UUID.randomUUID(),r.getId()),"Forged nonce accepted");
            p.getInventory().getItem(30).shrink(1);h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId())&&!HeavenlyFurnace.active(p),"Stale ingredient offer accepted");
            materials(p,r);HeavenlyFurnace.open(p);token=token(p);GeneratedMod.data(p).wuxingxiantu$setAura(1);
            h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId())&&count(p)==5,"Low mana mutated materials");
            GeneratedMod.data(p).wuxingxiantu$setAura(3200);FormulaKnowledge.reset(p);h.assertTrue(!HeavenlyFurnace.start(p,token,r.getId()),"Unlearned formula accepted");
        }h.succeed();
    }
    @GameTest(template="empty") public static void completionGivesOneOutcomeAndNeverReturnsConsumedIngredients(GameTestHelper h){
        try(var p=player(h)){
            var r=recipe(h);materials(p,r);HeavenlyFurnace.open(p);h.assertTrue(HeavenlyFurnace.start(p,token(p),r.getId()),"Start failed");
            var saved=running(p);var expected=ItemStack.of(saved.getCompound("Result"));saved.putLong("Start",h.getLevel().getGameTime()-saved.getInt("Duration"));
            HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
            h.assertTrue(!HeavenlyFurnace.active(p)&&count(p)==expected.getCount(),"Completion amount incorrect");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameTags(s,expected)),"Outcome grade/NBT changed");
            HeavenlyFurnace.cancel(p);HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));h.assertTrue(count(p)==expected.getCount(),"Completion was replayed or refunded");
        }h.succeed();
    }
    @GameTest(template="empty") public static void walkingAwayAndReloadRecoveryRefundWithoutDuplicating(GameTestHelper h){
        try(var p=player(h)){
            var r=recipe(h);materials(p,r);HeavenlyFurnace.open(p);HeavenlyFurnace.start(p,token(p),r.getId());p.setPos(p.getX()+HeavenlyFurnace.MOVE_RADIUS+1,p.getY(),p.getZ());
            HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));h.assertTrue(count(p)==5&&!HeavenlyFurnace.active(p),"Leaving cast area did not refund");
            var escrow=new CompoundTag();var list=new ListTag();var item=new ItemStack(Items.APPLE,3);item.getOrCreateTag().putString("Test","restored");list.add(item.save(new CompoundTag()));escrow.put("Items",list);
            p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).put(HeavenlyFurnace.ESCROW,escrow);
            HeavenlyFurnace.login(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));
            HeavenlyFurnace.login(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));h.assertTrue(count(p)==8,"Reload recovery duplicated or lost materials");
        }h.succeed();
    }
    @GameTest(template="empty") public static void progressionAndEquippedFireCannotBeBypassed(GameTestHelper h){
        try(var p=player(h)){
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(2);h.assertTrue(!HeavenlyFurnace.open(p),"Golden Core unlocked art");d.wuxingxiantu$setRealm(3);d.wuxingxiantu$learnFire(9);d.wuxingxiantu$setEquippedFire(9);h.assertTrue(!HeavenlyFurnace.open(p),"Fire above player realm accepted");
            d.wuxingxiantu$setEquippedFire(1);h.assertTrue(HeavenlyFurnace.open(p),"Legal realm and fire rejected");h.assertTrue(HeavenlyFurnace.furnaceTier(3)==4&&HeavenlyFurnace.furnaceTier(4)==5,"Furnace progression wrong");
            var r=recipe(h);h.assertTrue(HeavenlyFurnace.duration(r)==1405&&HeavenlyFurnace.cost(r,d)==946,"Fixed balance changed");
        }h.succeed();
    }
    @GameTest(template="empty") public static void fireballUsesCastHeadingAndValidatesForwardSpace(GameTestHelper h){
        var origin=new net.minecraft.world.phys.Vec3(2,100,3);var front=HeavenlyFurnace.fireballCenter(origin,0);var west=HeavenlyFurnace.fireballCenter(origin,90);
        h.assertTrue(front.distanceTo(origin.add(0,4.5,4.5))<.00001&&west.distanceTo(origin.add(-4.5,4.5,0))<.00001,"Fireball anchor is not forward/up in cast heading");
        try(var p=player(h)){
            p.setYRot(0);var r=recipe(h);materials(p,r);var center=net.minecraft.core.BlockPos.containing(HeavenlyFurnace.fireballCenter(p.position(),0));p.keepTicking(h.getLevel(),center);h.getLevel().getChunkAt(center);
            h.getLevel().setBlock(center,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
            HeavenlyFurnace.open(p);h.assertTrue(!HeavenlyFurnace.start(p,token(p),r.getId())&&count(p)==5,"Blocked forward anchor consumed inventory");
            h.getLevel().setBlock(center,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
        }h.succeed();
    }
}
