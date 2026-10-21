package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.EquipmentBond;
import cn.blockforge.generated.generatedmod.event.PlayerLifecycleEvents;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.*;
import java.util.UUID;

/** Integration data boundaries; these do not assert a complete disk/world migration. */
@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class IntegrationDataGameTests {
    @GameTest(template="empty")
    public static void legacyManaLoadsOnceAndCurrentFieldWins(GameTestHelper h) {
        var old=new CompoundTag();old.putInt("Realm",2);old.putFloat("Aura",37.5F);
        var data=new CultivationDataImpl();data.deserializeNBT(old);
        h.assertTrue(data.getCurrentMana()==37.5F,"Legacy aura lost");
        data.wuxingxiantu$setAura(12.5F);
        h.assertTrue(data.getCurrentMana()==12.5F,"Aura alias created a second balance");
        var saved=data.serializeNBT();saved.putFloat("Aura",999);
        var restored=new CultivationDataImpl();restored.deserializeNBT(saved);
        h.assertTrue(restored.getCurrentMana()==12.5F,"Legacy field overrode current mana");
        var twice=new CultivationDataImpl();twice.deserializeNBT(restored.serializeNBT());
        h.assertTrue(twice.getCurrentMana()==12.5F,"Repeated load added mana");h.succeed();
    }

    @GameTest(template="empty")
    public static void playerClonePreservesProgressAndDoesNotDuplicateEquipment(GameTestHelper h) {
        boolean keep=h.getLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        try(var original=new GameTestPlayer(h.getLevel());var replacement=new GameTestPlayer(h.getLevel())) {
            h.getLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false,h.getLevel().getServer());
            var data=GeneratedMod.data(original);data.wuxingxiantu$setRealm(2);
            data.wuxingxiantu$setCultivation(123);data.setCurrentMana(31.5F);
            data.wuxingxiantu$setYujianStack(new ItemStack(Items.DIAMOND_SWORD));
            original.invalidateCaps();
            PlayerLifecycleEvents.clonePlayer(new PlayerEvent.Clone(replacement,original,true));
            var restored=GeneratedMod.data(replacement);
            h.assertTrue(restored.wuxingxiantu$getRealm()==2&&restored.wuxingxiantu$getCultivation()==123,"Clone lost progression");
            h.assertTrue(restored.getCurrentMana()==31.5F,"Clone changed mana");
            h.assertTrue(restored.wuxingxiantu$getYujianStack().isEmpty(),"Death without keepInventory duplicated equipped sword");
        } finally {h.getLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(keep,h.getLevel().getServer());}
        h.succeed();
    }

    @GameTest(template="empty")
    public static void professionAndSenseReloadKeepOwnersIsolated(GameTestHelper h) {
        var first=UUID.randomUUID();var second=UUID.randomUUID();var data=new PlayerCrafts();
        data.account(first).putBoolean("SenseOpen",true);data.account(first).putFloat("Sense",73.5F);
        var craft=data.profession(first,"alchemy");craft.putBoolean("Learned",true);craft.putInt("Rank",5);craft.putInt("Xp",1234);
        data.profession(second,"alchemy").putInt("Xp",7);
        var snapshot=data.save(new CompoundTag());var restored=PlayerCrafts.load(snapshot);
        h.assertTrue(restored.account(first).getFloat("Sense")==73.5F&&restored.account(first).getBoolean("SenseOpen"),"Sense reset on reload");
        h.assertTrue(restored.profession(first,"alchemy").getInt("Rank")==5&&restored.profession(first,"alchemy").getInt("Xp")==1234,"Career migrated twice or lost XP");
        restored.profession(first,"alchemy").putInt("Xp",55);
        h.assertTrue(restored.profession(second,"alchemy").getInt("Xp")==7,"Accounts shared mutable career data");
        h.assertTrue(snapshot.getCompound("Accounts").getCompound(first.toString()).getCompound("alchemy").getInt("Xp")==1234,"Reload mutated saved snapshot");h.succeed();
    }

    @GameTest(template="empty")
    public static void boundEquipmentReloadPreservesOwnerAndRejectsOtherPlayer(GameTestHelper h) {
        try(var owner=new GameTestPlayer(h.getLevel());var stranger=new GameTestPlayer(h.getLevel())) {
            var stack=new ItemStack(Items.DIAMOND_SWORD);stack.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,owner.getUUID());
            var restored=ItemStack.of(stack.save(new CompoundTag()));
            h.assertTrue(owner.getUUID().equals(EquipmentBond.owner(restored)),"Owner UUID lost on item reload");
            h.assertTrue(EquipmentBond.canUse(owner,restored)&&!EquipmentBond.canUse(stranger,restored),"Reload bypassed ownership");
            restored.getOrCreateTag().putString(HeartSwordItem.OWNER_TAG,"invalid");
            h.assertTrue(!EquipmentBond.canUse(owner,restored),"Malformed owner became unbound equipment");
        }h.succeed();
    }
}
