package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyGradeGameTests {
    @GameTest(template="empty")
    public static void healingScalesAndEssencePreviewMatchesReward(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var health=player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        double old=health.getBaseValue();float oldHealth=player.getHealth();
        try {
            health.setBaseValue(1000);
            int[] expected={300,450,600,750};
            for(var grade:PillData.Grade.values()) {
                player.setHealth(100);
                h.assertTrue(PillEffects.apply(player,GeneratedMod.data(player),PillData.create(PillCatalog.GOLDEN_WOUND,grade,PillData.Element.METAL,0)),"Healing rejected");
                h.assertTrue(Math.abs(player.getHealth()-expected[grade.ordinal()])<.01,"Healing lost maximum-health scaling");
            }
        } finally { health.setBaseValue(old);player.setHealth(oldHealth); }
        var bottle=cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1);
        var formula=new net.minecraft.resources.ResourceLocation("wuxingxiantu","balance_test");
        var water=cn.blockforge.generated.generatedmod.fire.FireBottleData.Essence.WATER;
        double predicted=0;double[] contribution=new double[8];contribution[water.ordinal()]=10;
        for(int i=0;i<12;i++) {
            predicted+=10*cn.blockforge.generated.generatedmod.fire.FireBottleData.repeatDecay(i);
            cn.blockforge.generated.generatedmod.fire.FireBottleData.reward(bottle,contribution,formula,1,i*100,"test");
        }
        h.assertTrue(Math.abs(predicted-cn.blockforge.generated.generatedmod.fire.FireBottleData.essence(bottle,water))<.001,"Essence reward differs from preview");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void qualityInvestmentChangesActualDistribution(GameTestHelper h) {
        double previous=0;
        for(var investment:PillData.Grade.values()) {
            var source=PillData.create(PillCatalog.AURA,investment,PillData.Element.METAL,0);
            int total=0;double expectation=0;int[] samples=new int[4];
            for(var grade:PillData.Grade.values()) {
                int chance=AlchemyGradeOdds.percent(source,grade);
                total+=chance;expectation+=chance*grade.value()/100D;
            }
            h.assertTrue(total==100 && expectation>previous,"Investment fails to improve quality");
            previous=expectation;
            var random=RandomSource.create(7100+investment.ordinal());
            for(int i=0;i<10000;i++)samples[PillData.read(AlchemyGradeOdds.roll(source,random)).grade().ordinal()]++;
            for(var grade:PillData.Grade.values())h.assertTrue(
                    Math.abs(samples[grade.ordinal()]-AlchemyGradeOdds.percent(source,grade)*100)<200,
                    "Investment server distribution differs from preview");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void freeAlchemyOddsAndInputRules(GameTestHelper h) {
        var input = new net.minecraft.world.SimpleContainer(9);
        h.assertTrue(FreeAlchemy.recipe(input) == null,"Empty experiment accepted");
        input.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4));
        var recipe = FreeAlchemy.recipe(input);
        h.assertTrue(recipe != null && recipe.matches(input,h.getLevel()) && recipe.counts().get(0) == 4,"Herb plan invalid");
        input.setItem(1,new ItemStack(net.minecraft.world.item.Items.STONE));
        h.assertTrue(FreeAlchemy.recipe(input) == null,"Non-herb experiment accepted");
        var random = RandomSource.create(20261012L);
        var product = recipe.getResultItem(h.getLevel().registryAccess());
        int failures = 0; int[] grades = new int[4];
        for(int i=0;i<20000;i++) {
            var result = FreeAlchemy.roll(product,random);
            if(result.isEmpty()) failures++; else grades[PillData.read(result).grade().ordinal()]++;
        }
        h.assertTrue(Math.abs(failures-15000)<300,"Free success chance incorrect");
        int successes=20000-failures;
        for(var grade:PillData.Grade.values()) h.assertTrue(
                Math.abs(grades[grade.ordinal()]-successes*FreeAlchemy.gradePercent(grade)/100D)<120,
                "Free grade odds differ from UI: "+grade);
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void failedExperimentPersistsAndConsumesOnlyItsReservation(GameTestHelper h) {
        var furnace=new AlchemyFurnaceBlockEntity(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_FURNACE.get(),
                h.absolutePos(net.minecraft.core.BlockPos.ZERO),WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState());
        furnace.setLevel(h.getLevel());
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4));
        var recipe=FreeAlchemy.recipe(furnace);
        furnace.reserve(recipe);furnace.startBrewing(1,100);
        var saved=furnace.saveWithoutMetadata();
        furnace.load(saved);
        h.assertTrue(furnace.failExperiment() && furnace.getItem(0).isEmpty() && furnace.outcome()==2,"Failed batch did not consume saved reservation");
        h.assertTrue(!furnace.failExperiment(),"Failure settled twice");
        h.assertTrue(furnace.completedResult().is(WuXingItems.PILL_ASH.get())&&furnace.completedResult().getCount()==1,"Failure did not leave one ash");
        var emptyResult=furnace.saveWithoutMetadata();emptyResult.remove("CompletedResult");furnace.load(emptyResult);
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),3));
        furnace.reserve(FreeAlchemy.recipe(furnace));furnace.startBrewing(1,100);furnace.finishBrewing(false);
        h.assertTrue(furnace.getItem(0).getCount()==3,"Cancellation consumed herbs");
        var jobs=new cn.blockforge.generated.generatedmod.network.service.CraftingJobs();
        var job=new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.PendingAlchemy(java.util.UUID.randomUUID(),
                h.getLevel().dimension(),furnace.getBlockPos(),100,ItemStack.EMPTY,java.util.UUID.randomUUID(),java.util.UUID.randomUUID(),
                1,new double[8],FreeAlchemy.ID,.25D,100);
        jobs.add(job);
        var restored=cn.blockforge.generated.generatedmod.network.service.CraftingJobs.load(jobs.save(new net.minecraft.nbt.CompoundTag())).alchemy(job.key());
        h.assertTrue(restored!=null && restored.result().isEmpty() && restored.formula().equals(FreeAlchemy.ID),"Failed outcome lost on disk");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void gradeOddsPreserveResultAndSavedQuality(GameTestHelper h) {
        var source = PillData.create(PillCatalog.CULTIVATION, PillData.Grade.LOW, PillData.Element.WATER, 3);
        source.setCount(2);
        source.getOrCreateTag().putString("CustomMarker", "kept");
        var random = RandomSource.create(20261011L);
        int[] counts = new int[4];
        int total = 0;
        for (var grade : PillData.Grade.values()) total += AlchemyGradeOdds.percent(source, grade);
        h.assertTrue(total == 100, "Grade odds do not sum to 100");
        for (int i = 0; i < 10000; i++) {
            var rolled = AlchemyGradeOdds.roll(source, random);
            var data = PillData.read(rolled);
            counts[data.grade().ordinal()]++;
            h.assertTrue(rolled.getCount() == 2 && data.cultivationTier() == 3
                    && rolled.getTag().getString("CustomMarker").equals("kept"), "Result metadata lost");
            var saved = ItemStack.of(rolled.save(new net.minecraft.nbt.CompoundTag()));
            h.assertTrue(PillData.read(saved).grade() == data.grade(), "Saved grade changed");
        }
        for (var grade : PillData.Grade.values()) h.assertTrue(
                Math.abs(counts[grade.ordinal()] - AlchemyGradeOdds.percent(source, grade) * 100) < 200,
                "Seeded distribution differs from displayed odds: " + grade);
        h.assertTrue(PillData.read(source).grade() == PillData.Grade.LOW, "Original recipe mutated");
        var fixed = PillData.create(PillCatalog.CALMING, PillData.Grade.LOW, PillData.Element.METAL, 0);
        h.assertTrue(AlchemyGradeOdds.percent(fixed, PillData.Grade.LOW) == 100
                && PillData.read(AlchemyGradeOdds.roll(fixed, random)).grade() == PillData.Grade.LOW,
                "Fixed-grade pill changed");
        h.succeed();
    }
}
