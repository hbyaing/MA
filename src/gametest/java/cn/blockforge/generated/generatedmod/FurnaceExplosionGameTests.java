package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_furnaceexplosion")
@PrefixGameTestTemplate(false)
public final class FurnaceExplosionGameTests {
    @GameTest(template="empty",timeoutTicks=100)
    public static void warningLocksPersistsAndExplodesAfterThreeSeconds(GameTestHelper h) {
        var level=h.getLevel();var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(level,pos);
        var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        furnace.setItem(0,new ItemStack(Items.DIRT,3));
        h.assertTrue(furnace.beginExplosionWarning(null)&&furnace.explosionWarningTicks()==60,"Warning not started");
        h.assertTrue(furnace.getItem(0).isEmpty()&&furnace.busy()&&!furnace.brewing(),"Warning did not consume and lock batch");
        h.assertTrue(!furnace.beginExplosionWarning(null)&&furnace.removeItem(8,1).isEmpty(),"Warning can be retriggered or fire removed");
        var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        h.assertTrue(furnace.explosionWarningTicks()==60,"Reload lost countdown");
        h.runAfterDelay(45,()->h.assertTrue(level.getBlockEntity(pos)==furnace,"Exploded before warning finished"));
        h.runAfterDelay(65,()->{h.assertTrue(level.getBlockState(pos).isAir(),"Warning did not explode");h.succeed();});
    }
    @GameTest(template="empty")
    public static void dismantlingStopsWarning(GameTestHelper h) {
        var level=h.getLevel();var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(level,pos);
        var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));furnace.setItem(0,new ItemStack(Items.DIRT));
        furnace.beginExplosionWarning(null);AlchemyFurnaceStructure.dismantle(level,pos,null);
        h.assertTrue(!furnace.explosionPending()&&!furnace.busy(),"Stopped furnace kept warning");h.succeed();
    }
    @GameTest(template="empty")
    public static void unloadedJobsBackOffAndLoadWakesImmediately(GameTestHelper h) {
        var level=h.getLevel();var server=level.getServer();
        var pos=new BlockPos(1000000,64,1000000);h.assertTrue(!level.hasChunkAt(pos),"Fixture unexpectedly loaded");
        var jobs=cn.blockforge.generated.generatedmod.network.service.CraftingJobs.get(server);
        long now=server.overworld().getGameTime();
        var job=new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.PendingAlchemy(
                java.util.UUID.randomUUID(),level.dimension(),pos,now-1,new ItemStack(Items.APPLE),
                java.util.UUID.randomUUID(),java.util.UUID.randomUUID(),1,new double[8],FreeAlchemy.ID,1,0);
        jobs.add(job);
        try {
            cn.blockforge.generated.generatedmod.network.service.AlchemyService.tickAlchemy(server);
            h.assertTrue(jobs.poll(now+199)==null,"Unloaded job still retries each second");
            cn.blockforge.generated.generatedmod.network.service.AlchemyService.wakeAt(level,pos);
            var due=jobs.poll(now);h.assertTrue(due!=null&&due.key().equals(job.key()),"Load did not wake pending job immediately");
        } finally {jobs.remove(cn.blockforge.generated.generatedmod.network.service.CraftingJobs.Kind.ALCHEMY,job.key());}
        h.succeed();
    }
    @GameTest(template="empty")
    public static void recipeCacheAndOpeningCostFollowReload(GameTestHelper h) {
        var level=h.getLevel();var manager=level.getRecipeManager();
        var all=new java.util.ArrayList<net.minecraft.world.item.crafting.Recipe<?>>(manager.getRecipes());
        var first=FreeAlchemy.sortedRecipes(level);
        h.assertTrue(first==FreeAlchemy.sortedRecipes(level),"Unchanged recipes were re-sorted");
        var recipe=first.stream().filter(r->r.getId().getPath().equals("alchemy_aura_pill_low")).findFirst().orElseThrow();
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var data=GeneratedMod.data(player);
        h.assertTrue(FreeAlchemy.openingCost(recipe,data)==4,"Valid free batch has an unrelated 5% gate");
        try {
            manager.replaceRecipes(all.stream().filter(r->r!=recipe).toList());
            var refreshed=FreeAlchemy.sortedRecipes(level);
            h.assertTrue(refreshed!=first&&!refreshed.contains(recipe),"Same-tick recipe replacement kept stale cache");
        } finally {manager.replaceRecipes(all);}
        h.assertTrue(FreeAlchemy.sortedRecipes(level).contains(recipe),"Restored recipe missing");h.succeed();
    }
    @GameTest(template="empty")
    public static void ashOutcomePersistsWithoutSuccessEffects(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),3));
        furnace.reserve(FreeAlchemy.recipe(furnace));furnace.startBrewing(0,100);
        h.assertTrue(furnace.failExperiment()&&furnace.hasAshResult()&&!furnace.brewing(),"Ash still brewing or missing failure flag");
        var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        h.assertTrue(furnace.hasAshResult(),"Reload lost ash-only rendering state");h.succeed();
    }
    @GameTest(template="empty")
    public static void invalidSubmittedBatchLeavesAshWithoutWarning(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        furnace.setItem(0,new ItemStack(Items.DIRT,3));
        h.assertTrue(furnace.failSubmittedBatch(),"Invalid batch did not settle");
        h.assertTrue(furnace.hasAshResult()&&!furnace.explosionPending()&&!furnace.busy(),"Failure became explosion");
        h.assertTrue(furnace.getItem(0).isEmpty()&&!furnace.getItem(8).isEmpty(),"Batch or ignition consumed incorrectly");
        h.assertTrue(!furnace.failSubmittedBatch(),"Failure settled twice");h.succeed();
    }
    @GameTest(template="empty")
    public static void exactBatchAndUnsafeRecipe(GameTestHelper h) {
        var level=h.getLevel();var input=new net.minecraft.world.SimpleContainer(10);
        var recipes=level.getRecipeManager().getAllRecipesFor(cn.blockforge.generated.generatedmod.registry.ModRecipes.ALCHEMY_TYPE.get());
        for(var recipe:recipes) {
            input.clearContent();
            for(int i=0;i<recipe.getIngredients().size();i++) input.setItem(i,recipe.getIngredients().get(i).getItems()[0].copyWithCount(recipe.counts().get(i)));
            var found=FreeAlchemy.attemptRecipe(input,level);
            h.assertTrue(found!=null,"Registered batch not recognized: "+recipe.getId());
            if(found.furnaceTier()>0)h.assertTrue(FreeAlchemy.unstable(found,found.furnaceTier()-1,9,true,true),"Over-tier batch safe");
            h.assertTrue(!FreeAlchemy.unstable(found,5,found.requiredFire()>0?found.requiredFire():9,true,true),"Valid batch unsafe");
            h.assertTrue(!FreeAlchemy.unstable(found,5,0,true,true) && FreeAlchemy.failedBatch(found,0,true,true),"Wrong fire must fail without exploding");
            h.assertTrue(!FreeAlchemy.unstable(found,5,9,false,true) && FreeAlchemy.failedBatch(found,9,false,true),"Realm failure must not explode");
        }
        input.clearContent();input.setItem(0,new ItemStack(Items.DIRT));
        h.assertTrue(FreeAlchemy.attemptRecipe(input,level)==null&&!FreeAlchemy.unstable(null,5,9,true,true)&&FreeAlchemy.failedBatch(null,9,true,true),"Invalid batch safe");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void explosionDestroysTerrainAndCannotRepeat(GameTestHelper h) {
        var level=h.getLevel();var pos=FurnaceGameTests.build(h);
        AlchemyFurnaceStructure.refreshAround(level,pos);
        var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        h.assertTrue(!FurnaceExplosion.detonate(furnace,null),"Empty furnace exploded");
        furnace.setItem(0,new ItemStack(Items.DIRT,32));
        var earth=pos.above(4);var bedrock=pos.offset(1,4,0);
        level.setBlock(earth,Blocks.DIRT.defaultBlockState(),3);level.setBlock(bedrock,Blocks.BEDROCK.defaultBlockState(),3);
        var rule=net.minecraft.world.level.GameRules.RULE_MOBGRIEFING;
        boolean old=level.getGameRules().getBoolean(rule);
        level.getGameRules().getRule(rule).set(false,level.getServer());
        try {
            h.assertTrue(FurnaceExplosion.detonate(furnace,null),"Did not explode");
            h.assertTrue(level.getBlockEntity(pos)==null&&level.getBlockState(pos).isAir(),"Core survived");
            h.assertTrue(level.getBlockState(earth).isAir(),"Terrain survived with mobGriefing false");
            h.assertTrue(level.getBlockState(bedrock).is(Blocks.BEDROCK),"Bedrock destroyed");
            h.assertTrue(furnace.getItem(0).isEmpty()&&!FurnaceExplosion.detonate(furnace,null),"Materials duplicated or repeated explosion");
        } finally {level.getGameRules().getRule(rule).set(old,level.getServer());}
        h.succeed();
    }
}
