package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.mindscape.MindscapeManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class EntryUiGameTests {
    @GameTest(template = "empty")
    public static void freshRootsHaveRandomCountAndQuality(GameTestHelper helper) {
        int seen = 0;
        var random = RandomSource.create(20260927L);
        for (int sample = 0; sample < 1000; sample++) {
            var data = new CultivationDataImpl();
            PlayerInitialization.initializeIfNeeded(data, random);
            int mask = data.wuxingxiantu$getRootMask();
            helper.assertTrue(Integer.bitCount(mask) >= 1 && Integer.bitCount(mask)<=5 && mask <= 31, "Invalid fresh root");
            for (int i = 0; i < 5; i++) {
                helper.assertTrue(((mask&(1<<i))==0 ? data.wuxingxiantu$getRootQuality(i)==0 : data.wuxingxiantu$getRootQuality(i)>=1 && data.wuxingxiantu$getRootQuality(i)<=4),
                        "Quality inconsistent with selected elements");
            }
            helper.assertTrue(data.wuxingxiantu$isInitialized(), "Initialization not marked complete");
            seen |= mask;
        }
        helper.assertTrue(seen == 31, "All five roots must be reachable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void savedRootsDoNotReroll(GameTestHelper helper) {
        var data = new CultivationDataImpl();
        PlayerInitialization.initializeIfNeeded(data, RandomSource.create(42));
        var saved = data.serializeNBT();
        var restored = new CultivationDataImpl();
        restored.deserializeNBT(saved);
        PlayerInitialization.initializeIfNeeded(restored, RandomSource.create(99));
        helper.assertTrue(restored.wuxingxiantu$isInitialized(), "Initialization flag lost after saving");
        helper.assertTrue(data.wuxingxiantu$getRootMask() == restored.wuxingxiantu$getRootMask(), "Root rerolled");
        helper.assertTrue(data.wuxingxiantu$getPhysique() == restored.wuxingxiantu$getPhysique(), "Physique rerolled");
        for (int i = 0; i < 5; i++) helper.assertTrue(data.wuxingxiantu$getRootQuality(i)
                == restored.wuxingxiantu$getRootQuality(i), "Quality changed after loading");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyRootsRetainProgress(GameTestHelper helper) {
        var data = new CultivationDataImpl();
        data.wuxingxiantu$setRootMask(5);
        data.wuxingxiantu$setRootQuality(0, 3);
        data.wuxingxiantu$setRootQuality(2, 4);
        data.wuxingxiantu$setPhysique(PhysiqueType.JIUQIAO);
        PlayerInitialization.initializeIfNeeded(data, RandomSource.create(3));
        helper.assertTrue(data.wuxingxiantu$isInitialized() && data.wuxingxiantu$getRootMask() == 5,
                "Legacy roots were replaced");
        helper.assertTrue(data.wuxingxiantu$getRootQuality(0) == 3 && data.wuxingxiantu$getRootQuality(2) == 4,
                "Legacy quality was downgraded");
        helper.assertTrue(data.wuxingxiantu$getPhysique() == PhysiqueType.JIUQIAO, "Legacy physique changed");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void exactProbabilityBucketsAndDistribution(GameTestHelper h) {
        var counts=new int[5];var qualities=new int[4];
        for(int i=0;i<100;i++) {counts[cn.blockforge.generated.generatedmod.balance.RootBalance.countForRoll(i)-1]++;qualities[cn.blockforge.generated.generatedmod.balance.RootBalance.qualityForRoll(i)-1]++;}
        h.assertTrue(java.util.Arrays.equals(counts,new int[]{45,30,17,7,1}),"Count probabilities changed");
        h.assertTrue(java.util.Arrays.equals(qualities,new int[]{65,25,9,1}),"Quality probabilities changed");
        int[] elements=new int[5],observed=new int[5],grades=new int[4];int total=0;
        var rng=RandomSource.create(20261016);
        for(int i=0;i<20000;i++) {
            var d=new CultivationDataImpl();PlayerInitialization.initializeIfNeeded(d,rng);
            observed[Integer.bitCount(d.wuxingxiantu$getRootMask())-1]++;
            for(int j=0;j<5;j++)if((d.wuxingxiantu$getRootMask()&(1<<j))!=0){elements[j]++;grades[d.wuxingxiantu$getRootQuality(j)-1]++;total++;}
        }
        double[] cp={.45,.30,.17,.07,.01},qp={.65,.25,.09,.01};
        for(int i=0;i<5;i++){h.assertTrue(Math.abs(observed[i]/20000D-cp[i])<.012,"Count distribution biased");h.assertTrue(Math.abs(elements[i]/(double)total-.2)<.012,"Element selection biased");}
        for(int i=0;i<4;i++)h.assertTrue(Math.abs(grades[i]/(double)total-qp[i])<.012,"Quality distribution biased");h.succeed();
    }
    private static CultivationDataImpl roots(int count,int grade) {
        var d=new CultivationDataImpl();d.wuxingxiantu$setEquippedHeartMethod("thick_earth");d.wuxingxiantu$setRootMask((1<<count)-1);
        for(int i=0;i<count;i++)d.wuxingxiantu$setRootQuality(i,grade);return d;
    }
    private static void near(GameTestHelper h,double a,double b,String message){h.assertTrue(Math.abs(a-b)<.001,message+": "+a+" vs "+b);}
    @GameTest(template="empty")
    public static void rootTradeoffAndRecoveryDoNotDoubleDip(GameTestHelper h) {
        var one=roots(1,1);var five=roots(5,1);
        near(h,100,cn.blockforge.generated.generatedmod.items.CultivationLogic.calculateMaxMana(one),"Single-root capacity");
        near(h,172,cn.blockforge.generated.generatedmod.items.CultivationLogic.calculateMaxMana(five),"Five-root capacity");
        near(h,.57,cn.blockforge.generated.generatedmod.balance.RootBalance.training(five),"Five-root training");
        near(h,cn.blockforge.generated.generatedmod.items.CultivationLogic.manaRegenPerSecond(one),cn.blockforge.generated.generatedmod.items.CultivationLogic.manaRegenPerSecond(five),"Count increased recovery");
        var high=roots(5,4);
        near(h,.912,cn.blockforge.generated.generatedmod.balance.RootBalance.training(high),"Quality average stacked");
        near(h,172,cn.blockforge.generated.generatedmod.items.CultivationLogic.calculateMaxMana(high),"Quality increased capacity");
        near(h,1.8,cn.blockforge.generated.generatedmod.items.CultivationLogic.manaRegenPerSecond(high)/cn.blockforge.generated.generatedmod.items.CultivationLogic.manaRegenPerSecond(five),"Quality recovery missing");
        high.wuxingxiantu$setThunderRoot(true);high.wuxingxiantu$setThunderRootActive(true);high.wuxingxiantu$setExoticRoot(true);high.wuxingxiantu$setExoticRootActive(true);
        h.assertTrue(cn.blockforge.generated.generatedmod.balance.RootBalance.count(high)==5,"Variant added sixth root");
        var mixed=roots(2,1);mixed.wuxingxiantu$setRootQuality(1,3);
        near(h,.99875,cn.blockforge.generated.generatedmod.balance.RootBalance.training(mixed),"Mixed quality average");h.succeed();
    }
    @GameTest(template="empty")
    public static void fractionalTrainingAndDirectRewardsUseSeparateRoutes(GameTestHelper h) {
        try(var p=new cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer(h.getLevel())) {
            var d=GeneratedMod.data(p);d.wuxingxiantu$setEquippedHeartMethod("thick_earth");d.wuxingxiantu$setRealm(1);d.wuxingxiantu$setRootMask(31);
            for(int i=0;i<5;i++)d.wuxingxiantu$setRootQuality(i,1);
            for(int i=0;i<100;i++)cn.blockforge.generated.generatedmod.items.CultivationLogic.gainTrainingCultivation(p,d,1);
            near(h,57,d.wuxingxiantu$getCultivation(),"Small training gains rounded away");
            cn.blockforge.generated.generatedmod.items.CultivationLogic.gainCultivation(p,d,100);
            near(h,157,d.wuxingxiantu$getCultivation(),"Direct reward received root penalty");
            cn.blockforge.generated.generatedmod.items.CultivationLogic.gainTrainingCultivation(p,d,10000);
            near(h,cn.blockforge.generated.generatedmod.items.CultivationLogic.breakthroughRequirement(d),d.wuxingxiantu$getCultivation(),"Training exceeded cap");
            near(h,0,p.getPersistentData().getDouble("WuxingRootTrainingRemainder"),"Cap banked excess training");
        }h.succeed();
    }

    @GameTest(template = "empty")
    public static void dedicatedServerLoadsMindscape(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getServer().getLevel(MindscapeManager.DIMENSION) != null,
                "Mindscape dimension missing on dedicated server");
        helper.succeed();
    }
}
