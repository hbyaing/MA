package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import java.util.*;
@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class HeavenlyRefiningGameTests {
    private static GameTestPlayer player(GameTestHelper h){var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(net.minecraft.core.BlockPos.ZERO);p.setPos(pos.getX()+.5,210,pos.getZ()+.5);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(3200);d.wuxingxiantu$learnDivineArt(HeavenlyFurnace.ID);d.wuxingxiantu$learnFire(1);d.wuxingxiantu$setEquippedFire(1);return p;}
    private static RefiningRecipe recipe(GameTestHelper h,String name){return (RefiningRecipe)h.getLevel().getRecipeManager().byKey(new ResourceLocation("wuxingxiantu:refining_"+name)).orElseThrow();}
    private static void materials(GameTestPlayer p,RefiningRecipe r){for(int i=0;i<r.getIngredients().size();i++)p.getInventory().setItem(30+i,r.sample(i).copyWithCount(r.counts().get(i)));}
    @SuppressWarnings("unchecked") private static Object entry(String field,GameTestPlayer p){try{var f=HeavenlyFurnace.class.getDeclaredField(field);f.setAccessible(true);return ((Map<UUID,?>)f.get(null)).get(p.getUUID());}catch(Exception e){throw new AssertionError(e);}}
    private static UUID token(GameTestPlayer p){try{var o=entry("OFFERS",p);var m=o.getClass().getDeclaredMethod("token");m.setAccessible(true);return (UUID)m.invoke(o);}catch(Exception e){throw new AssertionError(e);}}
    @GameTest(template="empty") public static void refiningWorksWithoutAlchemyInheritanceAndEscrowsOnce(GameTestHelper h){try(var p=player(h)){
        var r=recipe(h,"dark_iron_sword");materials(p,r);h.assertTrue(HeavenlyFurnace.open(p),"Refining incorrectly required alchemy inheritance");var id=token(p);float aura=GeneratedMod.data(p).wuxingxiantu$getAura();
        h.assertTrue(HeavenlyFurnace.start(p,id,r.getId()),"Refining start failed");h.assertTrue(!HeavenlyFurnace.start(p,id,r.getId()),"Duplicate start accepted");var saved=(CompoundTag)entry("RUNNING",p);h.assertTrue(saved.getString("Mode").equals("refining"),"Refining mode not saved");h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getAura()==aura-HeavenlyFurnace.cost(r,p),"Wrong refining charge");
        HeavenlyFurnace.cancel(p);h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==7,"Cancel did not return exact materials");HeavenlyFurnace.cancel(p);h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==7,"Repeated cancel duplicated");
    }h.succeed();}
    @GameTest(template="empty") public static void refiningCompletionGrantsOneExactResultAndProfessionReceipt(GameTestHelper h){try(var p=player(h)){
        var r=recipe(h,"dark_iron_sword");materials(p,r);var crafts=cn.blockforge.generated.generatedmod.progression.PlayerCrafts.get(p.server);crafts.learnOrPromote(p,"refining");HeavenlyFurnace.open(p);h.assertTrue(HeavenlyFurnace.start(p,token(p),r.getId()),"Start failed");var saved=(CompoundTag)entry("RUNNING",p);saved.putLong("Start",p.level().getGameTime()-saved.getInt("Duration"));HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
        h.assertTrue(!HeavenlyFurnace.active(p)&&p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==1,"Completion duplicated or lost output");h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameTags(s,r.getResultItem(p.level().registryAccess()))),"Output NBT/type changed");int xp=crafts.profession(p.getUUID(),"refining").getInt("Xp");h.assertTrue(xp>0&&crafts.profession(p.getUUID(),"alchemy").getInt("Xp")==0,"Wrong profession XP");HeavenlyFurnace.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));h.assertTrue(crafts.profession(p.getUUID(),"refining").getInt("Xp")==xp,"XP replayed");
    }h.succeed();}
    @GameTest(template="empty") public static void highFireMissingMaterialAndLowManaDoNotMutateInventory(GameTestHelper h){try(var p=player(h)){
        var low=recipe(h,"dark_iron_sword");var high=recipe(h,"tianque_sword");h.assertTrue(!HeavenlyFurnace.refiningGate(p,high).isEmpty(),"High tier bypassed");materials(p,low);p.getInventory().getItem(31).shrink(1);HeavenlyFurnace.open(p);h.assertTrue(!HeavenlyFurnace.start(p,token(p),low.getId()),"Missing material accepted");h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==6,"Missing material mutated inventory");materials(p,low);GeneratedMod.data(p).wuxingxiantu$setAura(1);HeavenlyFurnace.open(p);h.assertTrue(!HeavenlyFurnace.start(p,token(p),low.getId()),"Low mana accepted");h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==7,"Low mana consumed materials");
    }h.succeed();}
}
