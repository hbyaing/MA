package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class DomainSelectionGameTests {
    @GameTest(template = "empty") public static void consumingCreationPillAwakensThunderRoot(GameTestHelper h) {
        try(var player=new cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer(h.getLevel())) {
            player.cleanupAfter(h,()->{});
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            new net.minecraft.world.item.ItemStack(WuXingItems.CREATION_PILL.get()).finishUsingItem(h.getLevel(),player);
            var d=GeneratedMod.data(player);
            h.assertTrue(d.wuxingxiantu$hasThunderRoot()&&d.wuxingxiantu$hasExoticRoot()&&CultivationLogic.thunderRootAvailable(d),"Creation pill failed to awaken special roots");
            h.succeed();
        }
    }
    private static CultivationDataImpl unlocked() {
        var d = new CultivationDataImpl();
        d.wuxingxiantu$setRootMask(31);
        for (int i = 0; i < 5; i++) d.wuxingxiantu$setRootQuality(i, 4);
        return d;
    }
    private static void select(CultivationData d, int root, boolean on) {
        switch (root) {
            case 0 -> d.wuxingxiantu$setGoldDomainEnabled(on);
            case 1 -> d.wuxingxiantu$setWoodDomainEnabled(on);
            case 2 -> d.wuxingxiantu$setWaterDomainEnabled(on);
            case 3 -> d.wuxingxiantu$setFireDomainEnabled(on);
            case 4 -> d.wuxingxiantu$setEarthDomainEnabled(on);
            default -> throw new IllegalArgumentException();
        }
    }
    private static int mask(CultivationData d) {
        return (d.wuxingxiantu$isGoldDomainEnabled() ? 1 : 0)
                | (d.wuxingxiantu$isWoodDomainEnabled() ? 2 : 0)
                | (d.wuxingxiantu$isWaterDomainEnabled() ? 4 : 0)
                | (d.wuxingxiantu$isFireDomainEnabled() ? 8 : 0)
                | (d.wuxingxiantu$isEarthDomainEnabled() ? 16 : 0);
    }
    @GameTest(template = "empty") public static void freshDomainsAreOff(GameTestHelper h) {
        h.assertTrue(mask(new CultivationDataImpl()) == 0, "Fresh player unexpectedly enabled a domain");
        h.succeed();
    }
    @GameTest(template = "empty") public static void legacyCompletedThunderTemperingRestoresSwitching(GameTestHelper h) {
        var tag=unlocked().serializeNBT();
        tag.putInt("Realm",ProgressionGate.THUNDER_ROOT_REALM);
        tag.putInt("ThunderStrikes",CultivationLogic.THUNDER_STRIKES_REQUIRED);
        tag.putBoolean("ThunderUnlocked",false);
        var d=new CultivationDataImpl();d.deserializeNBT(tag);
        h.assertTrue(d.wuxingxiantu$hasThunderRoot()&&CultivationLogic.thunderRootAvailable(d),"Earned legacy thunder root remained locked");
        d.wuxingxiantu$setThunderRootActive(true);
        h.assertTrue(CultivationLogic.isThunderForm(d),"Wood did not switch to thunder");
        d.wuxingxiantu$setThunderRootActive(false);
        h.assertTrue(!CultivationLogic.isThunderForm(d),"Thunder did not switch back to wood");
        var restored=new CultivationDataImpl();restored.deserializeNBT(d.serializeNBT());
        h.assertTrue(CultivationLogic.thunderRootAvailable(restored)&&!CultivationLogic.isThunderForm(restored),"Earned unlock or selected wood form was lost on reload");
        h.succeed();
    }
    @GameTest(template = "empty") public static void unfinishedThunderTemperingStaysLocked(GameTestHelper h) {
        var tag=unlocked().serializeNBT();tag.putInt("Realm",ProgressionGate.THUNDER_ROOT_REALM);
        tag.putInt("ThunderStrikes",CultivationLogic.THUNDER_STRIKES_REQUIRED-1);tag.remove("ThunderUnlocked");
        var d=new CultivationDataImpl();d.deserializeNBT(tag);
        h.assertTrue(!d.wuxingxiantu$hasThunderRoot()&&!CultivationLogic.thunderRootAvailable(d),"Incomplete legacy tempering bypassed unlock");
        h.succeed();
    }
    @GameTest(template = "empty") public static void oldCreationBlessingRestoresSpecialRoots(GameTestHelper h) {
        var tag=unlocked().serializeNBT();tag.putInt("Realm",8);tag.putBoolean("CreationBlessing",true);
        tag.putInt("ThunderStrikes",0);tag.putBoolean("ThunderUnlocked",false);tag.putBoolean("ExoticUnlocked",false);
        var d=new CultivationDataImpl();d.deserializeNBT(tag);
        h.assertTrue(CultivationLogic.thunderRootAvailable(d)&&d.wuxingxiantu$hasExoticRoot(),"Creation pill legacy save lost awakened special roots");
        d.wuxingxiantu$setThunderRootActive(true);h.assertTrue(CultivationLogic.isThunderForm(d),"Restored creation blessing cannot switch to thunder");
        var restored=new CultivationDataImpl();restored.deserializeNBT(d.serializeNBT());
        h.assertTrue(CultivationLogic.isThunderForm(restored),"Restored creation thunder form lost on save");
        h.succeed();
    }
    @GameTest(template = "empty") public static void everyDomainSwitchIsExclusive(GameTestHelper h) {
        var d = unlocked();
        for (int from = 0; from < 5; from++) for (int to = 0; to < 5; to++) {
            select(d, from, true); select(d, to, true);
            h.assertTrue(mask(d) == (1 << to), "Switch left multiple domains enabled");
            select(d, (to + 1) % 5, false);
            h.assertTrue(mask(d) == (1 << to), "Disabling an inactive domain removed the active domain");
            select(d, to, false);
            h.assertTrue(mask(d) == 0, "Active domain did not close");
        }
        h.succeed();
    }
    @GameTest(template = "empty") public static void saveRetainsSingleSelectionAndOff(GameTestHelper h) {
        var d = unlocked();
        for (int root = -1; root < 5; root++) {
            if (root >= 0) select(d, root, true);
            var restored = new CultivationDataImpl(); restored.deserializeNBT(d.serializeNBT());
            h.assertTrue(mask(restored) == (root < 0 ? 0 : 1 << root), "Domain selection changed after load");
        }
        h.succeed();
    }
    @GameTest(template = "empty") public static void oldAllOnSaveKeepsFirstUnlockedDomain(GameTestHelper h) {
        var d = unlocked(); d.wuxingxiantu$setRootQuality(0, 1);
        var tag = d.serializeNBT();
        for (String key : new String[]{"Gold", "Wood", "Water", "Fire", "Earth"}) tag.putBoolean(key + "DomainEnabled", true);
        d.deserializeNBT(tag);
        h.assertTrue(mask(d) == 2, "Legacy migration chose a locked domain or left multiple enabled");
        var again = new CultivationDataImpl(); again.deserializeNBT(d.serializeNBT());
        h.assertTrue(mask(again) == 2, "Migration was not stable across repeated saves");
        h.succeed();
    }
    @GameTest(template = "empty") public static void oldAuraFieldsRespectOffState(GameTestHelper h) {
        var d = unlocked(); d.wuxingxiantu$setRootMask(2);
        var tag = d.serializeNBT();
        tag.remove("WoodDomainEnabled"); tag.putBoolean("WoodAuraEnabled", false);
        d.deserializeNBT(tag);
        h.assertTrue(mask(d) == 0, "Disabled legacy wood aura was enabled by migration");
        tag.putBoolean("WoodAuraEnabled", true); d.deserializeNBT(tag);
        h.assertTrue(mask(d) == 2, "Enabled legacy wood aura did not migrate");
        h.succeed();
    }
    @GameTest(template = "empty") public static void switchingRemovesPriorDomainStatusEffects(GameTestHelper h) {
        var player = new net.minecraftforge.common.util.FakePlayer(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "DomainTest"));
        var d = unlocked();
        var effects = new net.minecraft.world.effect.MobEffect[]{ModEffects.GOLD_DOMAIN.get(), ModEffects.WOOD_DOMAIN.get(),
                ModEffects.WATER_DOMAIN.get(), ModEffects.FIRE_DOMAIN.get(), ModEffects.EARTH_DOMAIN.get(),
                ModEffects.THUNDER_DOMAIN.get(), ModEffects.EXOTIC_DOMAIN.get()};
        for (int root = 0; root < 5; root++) {
            select(d, root, true); CultivationLogic.refreshDomainBuffs(player, d);
            int count = 0; for (var effect : effects) if (player.hasEffect(effect)) count++;
            h.assertTrue(count == 1 && player.hasEffect(effects[root]), "Old domain status effect survived switch");
        }
        select(d, 4, false); CultivationLogic.refreshDomainBuffs(player, d);
        for (var effect : effects) h.assertTrue(!player.hasEffect(effect), "Domain status effect survived close");
        h.succeed();
    }
}
