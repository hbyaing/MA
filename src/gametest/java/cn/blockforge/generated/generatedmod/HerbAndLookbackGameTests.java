package cn.blockforge.generated.generatedmod;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.world.item.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import cn.blockforge.generated.generatedmod.realmflight.*;

@GameTestHolder("wuxingxiantu_herblookback")
@PrefixGameTestTemplate(false)
public final class HerbAndLookbackGameTests {
    private static ItemStack herb(){return new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.PLANT_BLOCKS.get(0).get());}
    private static HerbProcessingTableBlockEntity table(GameTestHelper h){var p=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlock(p,WuXingBlocks.HERB_PROCESSING_TABLE.get().defaultBlockState(),3);return (HerbProcessingTableBlockEntity)h.getLevel().getBlockEntity(p);}
    private static void ticks(GameTestHelper h,HerbProcessingTableBlockEntity t,int n){for(int i=0;i<n;i++)HerbProcessingTableBlockEntity.tick(h.getLevel(),t.getBlockPos(),t.getBlockState(),t);}
    @GameTest(template="empty") public static void processingPersistsAndIsOneForOne(GameTestHelper h){
        var t=table(h);var raw=herb();raw.setCount(2);t.setItem(0,raw);ticks(h,t,50);var saved=t.saveWithoutMetadata();t.load(saved);
        h.assertTrue(t.progress()==50,"Reload lost progress");ticks(h,t,50);
        h.assertTrue(t.getItem(0).getCount()==1&&t.getItem(1).getCount()==1&&HerbPreparation.prepared(t.getItem(1)),"Processing duplicated or lost material");
        var prepared=t.getItem(1).copy();t.setItem(0,prepared);ticks(h,t,200);
        h.assertTrue(t.getItem(0).getCount()==1&&t.getItem(1).getCount()==1&&t.progress()==0,"Processed herb can be upgraded repeatedly");
        h.assertTrue(!t.canPlaceItem(0,new ItemStack(Items.DIRT))&&!t.canPlaceItem(1,raw),"Invalid slots accept items");h.succeed();
    }
    @GameTest(template="empty") public static void blockedOutputAndChangedInputDoNotSpendMaterials(GameTestHelper h){
        var t=table(h);var input=herb();t.setItem(0,input);var full=HerbPreparation.prepare(input);full.setCount(64);t.setItem(1,full);ticks(h,t,200);
        h.assertTrue(t.getItem(0).getCount()==1&&t.progress()==0,"Full output consumed input");t.setItem(1,ItemStack.EMPTY);ticks(h,t,70);
        t.setItem(0,new ItemStack(Items.DIRT));ticks(h,t,40);h.assertTrue(t.progress()==0&&t.getItem(1).isEmpty(),"Input swap completed a stale work item");
        h.assertTrue(t.getSlotsForFace(Direction.DOWN)[0]==1&&!t.canTakeItemThroughFace(0,input,Direction.DOWN)&&t.canPlaceItemThroughFace(0,input,Direction.UP),"Hopper direction is unsafe");h.succeed();
    }
    @GameTest(template="empty") public static void alchemyBonusCountsOnlyConsumedHerbs(GameTestHelper h){
        var recipe=cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.sortedRecipes(h.getLevel()).stream().filter(r->r.getId().getPath().equals("alchemy_aura_pill_low")).findFirst().orElseThrow();
        var input=new net.minecraft.world.SimpleContainer(8);
        // Fill exactly the actual recipe; ingredient test remains vanilla item identity.
        int i=0;for(var ingredient:recipe.getIngredients())input.setItem(i++,ingredient.getItems()[0].copyWithCount(64));
        h.assertTrue(recipe.matches(input,h.getLevel()),"Fixture does not match formula");h.assertTrue(HerbPreparation.bonus(recipe,input)==0,"Raw batch has a bonus");
        for(int slot=0;slot<8;slot++){var s=input.getItem(slot);if(HerbPreparation.raw(s)){var p=HerbPreparation.prepare(s);p.setCount(s.getCount());input.setItem(slot,p);}}
        h.assertTrue(recipe.matches(input,h.getLevel())&&HerbPreparation.bonus(recipe,input)==6,"Prepared item no longer matches or has wrong benefit");
        int base=cn.blockforge.generated.generatedmod.alchemy.AlchemyFailureOdds.percent(recipe,1,1,false);
        int better=cn.blockforge.generated.generatedmod.alchemy.AlchemyFailureOdds.percent(recipe,1,1,false,6);
        h.assertTrue(better==Math.max(2,base-6),"Formal alchemy cap differs");
        h.assertTrue(cn.blockforge.generated.generatedmod.alchemy.AlchemyFailureOdds.percent(recipe,1,1,true,6)==69,"Free alchemy chance differs");
        for(int slot=0;slot<8;slot++){var s=input.getItem(slot);if(HerbPreparation.prepared(s))s.getOrCreateTag().remove(HerbPreparation.KEY);}
        input.setItem(7,HerbPreparation.prepare(herb()));h.assertTrue(HerbPreparation.bonus(recipe,input)==0,"Spare unconsumed herb boosts recipe");h.succeed();
    }
    @GameTest(template="empty") public static void firstPersonOnlyRevealsRearOldTrail(GameTestHelper h){
        var forward=new RealmMeteorPath.Vec(0,0,1);
        h.assertTrue(FirstPersonRealmTrail.viewWeight(forward,forward)==0,"New world effect visible looking forward");
        h.assertTrue(FirstPersonRealmTrail.viewWeight(forward.scale(-1),forward)==1,"Looking back still hides tail");
        h.assertTrue(FirstPersonRealmTrail.historyWeight(0,20,1)==0&&FirstPersonRealmTrail.historyWeight(20,.1,1)==0,"Body or near-camera field restored");
        h.assertTrue(FirstPersonRealmTrail.historyWeight(15,15,1)==1,"Old tail discarded");
        h.assertTrue(FirstPersonRealmTrail.historyWeight(3,3,1)==1,"Turn trail has a multi-block gap");
        h.assertTrue(FirstPersonRealmTrail.centerClearWeight(Math.cos(Math.toRadians(15)))==1,"Center protection removes side turn trail");
        h.assertTrue(FirstPersonRealmTrail.centerClearWeight(1)==0,"Central distant glow reintroduced");
        var path=new RealmMeteorPath();for(int tick=0;tick<30;tick++)path.sample(forward.scale(tick*.5),tick,true);
        var before=path.render(29,new RealmMeteorPath.Vec(0,0,14.5),1,18);int samples=path.sampleCount();
        for(int j=0;j<100;j++)FirstPersonRealmTrail.viewWeight(new RealmMeteorPath.Vec(j%2,0,-1),forward);
        h.assertTrue(path.sampleCount()==samples&&before.equals(path.render(29,new RealmMeteorPath.Vec(0,0,14.5),1,18)),"Camera rotation rewrote history");h.succeed();
    }
}
