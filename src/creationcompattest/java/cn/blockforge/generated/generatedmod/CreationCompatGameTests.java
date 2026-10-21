package cn.blockforge.generated.generatedmod;

import cn.blockforge.wuxingchuangshi.creation.*;
import cn.blockforge.generated.generatedmod.economy.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

/** Runs with the real addon and Forge capabilities; excluded from release sources. */
@GameTestHolder("wuxingxiantu_creationcompat")
@PrefixGameTestTemplate(false)
public class CreationCompatGameTests {
    @GameTest(template="empty", timeoutTicks=80)
    public static void immortal_rank_save_migration(GameTestHelper h) {
        for(int rank=0;rank<=7;rank++){
            var saved=new CompoundTag();saved.putInt("XianRealm",rank);
            var d=new CreationData();d.deserializeNBT(saved);
            h.assertTrue(!d.reconcileEarthImmortal(7)&&d.getXianRealm()==rank,"Mortal rank changed");
            h.assertTrue(d.reconcileEarthImmortal(8)==(rank==0),"Incorrect migration flag");
            h.assertTrue(d.getXianRealm()==Math.max(1,rank),"Old rank shifted");
            h.assertTrue(!d.reconcileEarthImmortal(8),"Migration not idempotent");
            var loaded=new CreationData();loaded.deserializeNBT(d.serializeNBT());
            h.assertTrue(loaded.getXianRealm()==Math.max(1,rank),"Migration lost after reload");
        }
        h.assertTrue(CreationData.XIAN_REALM_NAMES[0].equals("未登仙"),"Sentinel still a realm");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void immortal_health_is_finite_and_dimension_independent(GameTestHelper h) {
        var p=owner(h);var d=CreationCapability.of(p);var parent=GeneratedMod.data(p);
        parent.wuxingxiantu$setRealm(8);parent.wuxingxiantu$setCultivationStage(0);
        parent.wuxingxiantu$setRootMask(1);
        parent.wuxingxiantu$setEquippedHeartMethod("beast_taming");
        cn.blockforge.generated.generatedmod.items.CultivationLogic.applyHealthBonus(p,parent);
        float base=p.getMaxHealth();float mana=parent.wuxingxiantu$getMaxMana();
        double[] bonuses={0,0,300,600,900,1200,1600,2000};
        for(int rank=0;rank<=7;rank++){
            d.setXianRealm(rank);XianRealmBonus.tick(p);
            h.assertTrue(Math.abs(p.getMaxHealth()-base-bonuses[rank])<.1,"Incorrect HP rank "+rank);
            p.setHealth(p.getMaxHealth()/2);XianRealmBonus.tick(p);
            h.assertTrue(Math.abs(p.getHealth()-p.getMaxHealth()/2)<.1,"Repeated tick healed player");
            h.assertTrue(parent.wuxingxiantu$getMaxMana()==mana,"Addon overwrote native mana");
        }
        d.setXianRealm(1);XianRealmBonus.tick(p);
        h.assertTrue(Math.abs(p.getHealth()-base/2)<.1,"Rank reduction lost health ratio");
        d.setXianRealm(7);parent.wuxingxiantu$setRealm(7);XianRealmBonus.tick(p);
        h.assertTrue(Math.abs(p.getMaxHealth()-base)<.1&&d.getXianRealm()==7,"Dormant rank active or erased");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void legacy_rank_modifiers_are_removed_without_free_healing(GameTestHelper h) {
        var p=owner(h);var d=CreationCapability.of(p);var parent=GeneratedMod.data(p);
        parent.wuxingxiantu$setRealm(8);parent.wuxingxiantu$setRootMask(1);
        parent.wuxingxiantu$setEquippedHeartMethod("beast_taming");
        cn.blockforge.generated.generatedmod.items.CultivationLogic.applyHealthBonus(p,parent);
        float base=p.getMaxHealth();
        var health=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        for(int i=0;i<2;i++)health.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                UUID.randomUUID(),"wuxingchuangshi:xian_realm_life",200,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        p.setHealth(p.getMaxHealth()/2);d.setXianRealm(1);XianRealmBonus.tick(p);
        h.assertTrue(Math.abs(p.getMaxHealth()-base)<.1,"Duplicate Earth Immortal bonus remained");
        h.assertTrue(Math.abs(p.getHealth()-base/2)<.1,"Legacy cleanup changed health fraction");
        d.setXianRealm(7);XianRealmBonus.tick(p);XianRealmBonus.clear(p);XianRealmBonus.tick(p);
        h.assertTrue(Math.abs(p.getMaxHealth()-base-2000)<.1,"Missing transient modifier not restored");
        h.succeed();
    }
    private static net.minecraft.server.level.ServerPlayer owner(GameTestHelper h) {
        var p = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "CreationQA"));
        var d = CreationCapability.of(p);
        h.assertTrue(d != null, "Real addon capability missing");
        int slot = SlotBindings.get(h.getLevel().getServer()).allocate(p.getUUID());
        h.assertTrue(slot > 0, "Cannot allocate isolated owner");
        d.setWorldUnlocked(true); d.setWorldSlot(slot); d.setWorldLevel(1);
        p.getInventory().clearContent();
        return p;
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void high_grade_upgrade_keeps_legacy_units(GameTestHelper h) {
        var p=owner(h);var d=CreationCapability.of(p);
        d.setUpgradeSubmitted(0, 10);
        p.getInventory().items.set(0,new ItemStack(SpiritStoneGrade.SUPERIOR.item(),1));
        WorldManager.submitUpgradeMaterials(p);
        h.assertTrue(d.getUpgradeSubmitted(0)==64,"Did not complete remaining 54 low-equivalent units");
        h.assertTrue(SpiritCurrency.value(p.getInventory().items)==27,"Incorrect exact change");
        WorldManager.submitUpgradeMaterials(p);
        h.assertTrue(SpiritCurrency.value(p.getInventory().items)==27,"Repeated submission charged twice");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void failed_change_is_atomic(GameTestHelper h) {
        var p=owner(h);var d=CreationCapability.of(p);
        for(int i=0;i<p.getInventory().items.size();i++)p.getInventory().items.set(i,new ItemStack(Items.COBBLESTONE,64));
        p.getInventory().items.set(0,new ItemStack(SpiritStoneGrade.EXQUISITE.item(),64));
        long before=SpiritCurrency.value(p.getInventory().items);
        WorldManager.submitUpgradeMaterials(p);
        h.assertTrue(d.getUpgradeSubmitted(0)==0,"Failed change advanced progress");
        h.assertTrue(SpiritCurrency.value(p.getInventory().items)==before,"Failed change debited inventory");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void partial_payment_and_nbt_roundtrip(GameTestHelper h) {
        var p=owner(h);var d=CreationCapability.of(p);
        d.setXianRealm(4);
        p.getInventory().items.set(0,new ItemStack(SpiritStoneGrade.MIDDLE.item(),3));
        WorldManager.submitUpgradeMaterials(p);
        h.assertTrue(d.getUpgradeSubmitted(0)==27,"Partial equivalent payment lost");
        var loaded=new CreationData();loaded.deserializeNBT(d.serializeNBT());
        h.assertTrue(loaded.getUpgradeSubmitted(0)==27&&loaded.getXianRealm()==4,"Saved progression changed");
        h.assertTrue(loaded.getWorldSlot()==d.getWorldSlot()&&loaded.isWorldUnlocked(),"Saved ownership changed");
        var bindings=SlotBindings.load(SlotBindings.get(h.getLevel().getServer()).save(new CompoundTag()));
        h.assertTrue(bindings.slotOf(p.getUUID())==d.getWorldSlot(),"UUID binding roundtrip changed");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void foreign_owner_cannot_pay(GameTestHelper h) {
        var a=owner(h);var b=owner(h);var d=CreationCapability.of(b);
        d.setWorldSlot(CreationCapability.of(a).getWorldSlot());
        b.getInventory().items.set(0,new ItemStack(SpiritStoneGrade.EXQUISITE.item(),1));
        WorldManager.submitUpgradeMaterials(b);
        h.assertTrue(d.getUpgradeSubmitted(0)==0&&SpiritCurrency.value(b.getInventory().items)==729,"Foreign slot charged");
        h.succeed();
    }
}
