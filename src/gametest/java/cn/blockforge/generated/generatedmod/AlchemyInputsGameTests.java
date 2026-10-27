package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.inputs.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.storage.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.world.item.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.core.BlockPos;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class AlchemyInputsGameTests {
    @GameTest(template="empty") public static void creativeHerbsHaveExactlyOneZeroYearTemplateEach(GameTestHelper h){
        net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS,true,h.getLevel().registryAccess());
        var counts=new HashMap<String,Integer>();
        for(var stack:cn.blockforge.generated.generatedmod.registry.ModTabs.HERBS.get().getDisplayItems()){
            var herb=HerbAge.definition(stack);if(herb==null)continue;
            counts.merge(herb.id,1,Integer::sum);
            h.assertTrue(HerbAge.years(stack)==0&&stack.getTag().getBoolean(HerbAge.CREATIVE_TEMPLATE),"Catalogue herb is aged or unmarked");
        }
        h.assertTrue(counts.size()==SpiritPlantCatalog.ALL.size()&&counts.values().stream().allMatch(n->n==1),"Missing/duplicated creative herb variants: "+counts);
        var future=SpiritPlantDefinition.builder("future_root_test",WildRarity.SPECIAL).years(12000,13000).build();
        var random=net.minecraft.util.RandomSource.create(321);for(int i=0;i<100;i++){int years=HerbAge.randomYears(future,random);h.assertTrue(years>=12000&&years<=13000,"Explicit minimum age ignored");}
        h.succeed();
    }
    @GameTest(template="empty") public static void inputDefinitionsRejectDriftUnknownAndUnfundedData(GameTestHelper h){
        String source;
        try(var in=AlchemyInputsGameTests.class.getResourceAsStream("/data/wuxingxiantu/alchemy/inputs.json")){source=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
        catch(Exception e){throw new IllegalStateException(e);}
        for(String bad:List.of(source.replace("\"schema\": 1","\"schema\": 1, \"schema\": 1"),source.replace("\"schema\": 1","\"schema\": 2"),
                source.replace("\"massPerItem\": 10000","\"massPerItem\": 100"),source.replace("\"ageScale\": 1000","\"ageScale\": 0"),
                source.replace("\"jin\": 1","\"unknown\": 1"),source.replace("\"red_flow\"","\"unknown_fire\""))){
            // The last replacement may not match namespaced IDs; check separately below.
            if(!bad.equals(source))reject(h,()->new InputDefinitions(bad));
        }
        reject(h,()->new InputDefinitions(source.replace("wuxingxiantu:red_flow","wuxingxiantu:unknown_fire")));
        h.assertTrue(new InputDefinitions(source).hash.equals(InputDefinitions.current().hash),"Stable definition hash changed");h.succeed();
    }
    private static ItemStack herb(int age,int count){var s=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),count);HerbAge.setYears(s,age);s.getOrCreateTag().putString("ThirdParty","keep");return s;}
    private static void reject(GameTestHelper h,Runnable action){boolean rejected=false;try{action.run();}catch(RuntimeException e){rejected=true;}h.assertTrue(rejected,"Invalid input accepted");}
    private static SimulationRules rules(Map<String,Long> stocks,List<Reaction> reactions){
        return new SimulationRules("real-input-test",stocks.keySet().stream().map(id->new Resource(id,1,id.equals("herb_inert")?0:1,0,Destination.ACTIVE)).toList(),reactions,
            new Limits(10,100,1000,256,1000000000,1000000,1,1000000,1000000,1000000,1000000000,0));
    }
    @GameTest(template="empty") public static void creativePacketInitializesOnceAndPreservesOrdinaryZeroAge(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            var template=HerbAge.creativeTemplate(herb(0,64));var original=template.copy();
            player.connection.handleSetCreativeModeSlot(new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(36,template));
            var actual=player.getInventory().getItem(0);
            h.assertTrue(actual.getCount()==64&&actual.hasTag()&&!actual.getTag().contains(HerbAge.CREATIVE_TEMPLATE),"Creative packet left unresolved template");
            int age=HerbAge.years(actual);h.assertTrue(age>=0&&age<=10499,"Creative age out of range");
            h.assertTrue(!HerbAge.initializeTemplate(actual,player.getRandom())&&HerbAge.years(actual)==age,"Resolved herb rerolled");
            var seedling=herb(0,1);h.assertTrue(!HerbAge.initializeTemplate(seedling,player.getRandom())&&HerbAge.years(seedling)==0,"Normal propagule randomized");
            h.assertTrue(original.getTag().getBoolean(HerbAge.CREATIVE_TEMPLATE)&&HerbAge.years(original)==0,"Catalogue copy changed");
            h.assertTrue(actual.getTag().getString("ThirdParty").equals("keep"),"Provenance lost");
        }h.succeed();
    }
    @GameTest(template="empty") public static void allRealHerbsHaveContinuousFinitePartitionedInputs(GameTestHelper h){
        var definitions=InputDefinitions.current();
        for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){
            var stack=new ItemStack(item);if(HerbAge.definition(stack)==null)continue;
            long previous=-1;
            for(int age:new int[]{0,1,499,500,501,1000,10499,1000000000}){
                HerbAge.setYears(stack,age);var material=AlchemyInputs.material(stack,definitions);
                h.assertTrue(material.referenceMass()==10000&&material.stocks().values().stream().mapToLong(Long::longValue).sum()==10000,"Material mass created/lost");
                long active=10000-material.stocks().getOrDefault("herb_inert",0L);h.assertTrue(active>=previous,"Age response decreased");previous=active;
                h.assertTrue(HerbAge.years(stack)==age,"Input adapter mutated specimen");
            }
        }
        var source=herb(1000,3);var prepared=HerbPreparation.prepare(source);prepared.setCount(3);
        var a=AlchemyInputs.material(source,definitions);var b=AlchemyInputs.material(prepared,definitions);
        h.assertTrue(a.stocks().get("herb_inert")>b.stocks().get("herb_inert")&&b.referenceMass()==30000,"Preparation created mass or had no effect");
        reject(h,()->AlchemyInputs.material(new ItemStack(Items.DIRT),definitions));reject(h,()->AlchemyInputs.material(HerbAge.creativeTemplate(source),definitions));h.succeed();
    }
    @GameTest(template="empty") public static void bagMergesAllYearsAndRetainsExactAgeAndOtherNbt(GameTestHelper h){
        var bag=new BagInventory();for(int age:new int[]{0,499,500,2500,8500,1000000})bag.insert(herb(age,2));
        h.assertTrue(bag.entries.size()==1&&bag.entries.get(0).total()==12,"Removed grades still occupy separate entries");
        var saved=BagInventory.load(bag.save());for(int age:new int[]{0,499,500,2500,8500,1000000}){
            var got=saved.entries.get(0).take(64);h.assertTrue(HerbAge.years(got)==age&&got.getCount()==2&&got.getTag().getString("ThirdParty").equals("keep"),"Bag lost exact years or NBT");
        }h.succeed();
    }
    @GameTest(template="empty") public static void realSkySamplesDayNightRoofAndCanReplayIntoKernel(GameTestHelper h){
        var level=h.getLevel();long time=level.getDayTime();boolean raining=level.isRaining(),thundering=level.isThundering();
        var pos=h.absolutePos(new BlockPos(1,3,1)).atY(level.getMaxBuildHeight()-5);
        try{
            level.setWeatherParameters(10000,0,false,false);level.setDayTime(18000);level.updateSkyBrightness();
            var night=AlchemyEnvironment.sample(level,pos);h.assertTrue(night.values().get("night")==1&&night.values().get("moon_input_rate")==1000,"Full moon sample wrong: "+night+" sky="+level.dimensionType().hasSkyLight()+" natural="+level.dimensionType().natural());
            level.setBlockAndUpdate(pos.above(2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            var roof=AlchemyEnvironment.sample(level,pos);h.assertTrue(roof.values().get("open_sky")==0&&roof.values().get("moon_input_rate")==0,"Roof allowed moon input");
            level.removeBlock(pos.above(2),false);level.setDayTime(4*24000+18000);level.updateSkyBrightness();
            var newMoon=AlchemyEnvironment.sample(level,pos);h.assertTrue(newMoon.values().get("new_moon_night")==1&&newMoon.values().get("moon_input_rate")==0,"New moon treated as full moon");
            level.setDayTime(6000);level.updateSkyBrightness();var day=AlchemyEnvironment.sample(level,pos);h.assertTrue(day.values().get("night")==0&&day.values().get("moon_input_rate")==0,"Day retained night input");
            var batch=AlchemyInputs.batch(List.of(herb(1000,2)),InputDefinitions.current());var r=rules(batch.stocks(),List.of());var initial=batch.begin(r,"real-environment",42,night);
            var events=AlchemyEnvironment.changes(initial,day);var changed=AlchemySimulation.step(r,initial,events).snapshot();
            h.assertTrue(changed.environment().equals(day.values())&&changed.stocks().equals(initial.stocks()),"Environment generated or erased material");
            var saved=SimulationSnapshotCodec.decodeServerSnapshot(r,SimulationSnapshotCodec.encodeServerSnapshot(r,changed));
            h.assertTrue(saved.equals(changed),"Actual input checkpoint did not roundtrip");
        }finally{level.setDayTime(time);level.updateSkyBrightness();level.setWeatherParameters(10000,10000,raining,thundering);level.removeBlock(pos.above(2),false);}h.succeed();
    }
    @GameTest(template="empty") public static void environmentInterruptionResetsContinuousWindowWithoutErasingStock(GameTestHelper h){
        var stocks=Map.of("herb_inert",10L);var reaction=new Reaction("night-hold",0,Phase.WARMUP,Map.of("herb_inert",1L),Map.of("herb_inert",1L),1,0,0,2,List.of(new Window(Measure.ENVIRONMENT,"night","",1,1)));
        var r=rules(stocks,List.of(reaction));var s=AlchemySimulation.begin(r,"window",stocks,0,10,99,Map.of("night",1L));
        s=AlchemySimulation.step(r,s,List.of()).snapshot();h.assertTrue(s.holds().getOrDefault("night-hold",0L)==1,"Window did not start");
        s=AlchemySimulation.step(r,s,AlchemyEnvironment.changes(s,new AlchemyEnvironment.Sample("minecraft:overworld",2,6000,Map.of("night",0L)))).snapshot();
        h.assertTrue(s.holds().getOrDefault("night-hold",0L)==0&&s.stocks().equals(stocks),"Day did not reset hold or erased stock");
        s=AlchemySimulation.step(r,s,AlchemyEnvironment.changes(s,new AlchemyEnvironment.Sample("minecraft:overworld",3,18000,Map.of("night",1L)))).snapshot();
        h.assertTrue(s.holds().getOrDefault("night-hold",0L)==1,"Two nights combined into one continuous window");h.succeed();
    }
    @GameTest(template="empty") public static void moonStorageIsFiniteAttributedAndCannotForgeNight(GameTestHelper h){
        var moon=new MoonBottleData();var full=new AlchemyEnvironment.Sample("minecraft:overworld",0,18000,Map.of("night",1L,"open_sky",1L,"moon_phase",0L,"moon_input_rate",1000L));
        for(int i=0;i<100;i++)moon.collect(full);h.assertTrue(moon.total()==MoonBottleData.CAPACITY,"Collection exceeded capacity");
        var saved=MoonBottleData.load(moon.save());var day=new AlchemyEnvironment.Sample("minecraft:overworld",100,6000,Map.of("night",0L,"open_sky",1L,"moon_phase",0L,"moon_input_rate",1000L));
        h.assertTrue(saved.collect(day)==0,"Stored provenance forged night");
        long paid=saved.release(777).values().stream().mapToLong(Long::longValue).sum();h.assertTrue(paid==777&&saved.total()==MoonBottleData.CAPACITY-777,"Release not conserved");
        h.assertTrue(saved.sources().keySet().stream().allMatch(k->k.endsWith("/moon/0")),"Lost lunar origin");
        var target=new MoonBottleData();long before=saved.total()+target.total();long moved=saved.transferTo(target,333);
        h.assertTrue(moved==333&&saved.total()+target.total()==before&&target.sources().keySet().equals(saved.sources().keySet()),"Bottle mixing lost quantity or provenance");
        h.assertTrue(saved.transferTo(saved,333)==0,"Self-transfer changed inventory");reject(h,()->saved.release(1001));
        var invalid=saved.save();invalid.getCompound("Sources").putLong("minecraft:overworld/moon/0",Long.MAX_VALUE);reject(h,()->MoonBottleData.load(invalid));h.succeed();
    }
    @GameTest(template="empty") public static void realFireAdapterDoesNotCreateEssenceOrMutateBottle(GameTestHelper h){
        var fire=cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(3);var original=fire.copy();var input=AlchemyInputs.fire(fire);
        h.assertTrue(input.tier()==3&&ItemStack.matches(original,fire)&&input.essence().values().stream().allMatch(v->v==0D),"Fire adapter changed bottle or invented fuel");
        reject(h,()->AlchemyInputs.fire(new ItemStack(Items.BLAZE_POWDER)));
        var trajectory=new InputTrajectory(InputDefinitions.current(),input);var pos=h.absolutePos(new BlockPos(1,3,1));
        var batch=AlchemyInputs.batch(List.of(herb(1000,1)),InputDefinitions.current());var rules=rules(batch.stocks(),List.of());
        var initial=batch.begin(rules,"fire-trajectory",123,trajectory.sample(h.getLevel(),pos));
        var heated=trajectory.step(rules,initial,h.getLevel(),pos,List.of(new InputTrajectory.Control(AlchemySimulation.Action.HEAT,"",1200))).snapshot();
        h.assertTrue(heated.ledger().externalEnergy()==1200&&heated.environment().get("fire_tier")==3,"Real fire/control trajectory lost inputs");
        reject(h,()->trajectory.step(rules,initial,h.getLevel(),pos,List.of(new InputTrajectory.Control(AlchemySimulation.Action.HEAT,"",2000),new InputTrajectory.Control(AlchemySimulation.Action.HEAT,"",2000))));
        reject(h,()->trajectory.step(rules,initial,h.getLevel(),pos,List.of(new InputTrajectory.Control(AlchemySimulation.Action.ENVIRONMENT,"night",1))));h.succeed();
    }
    @GameTest(template="empty") public static void placedMoonBottleLootPreservesStorageWhenReplaced(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));var next=pos.east();
        var state=WuXingBlocks.MOON_BOTTLE.get().defaultBlockState();level.setBlockAndUpdate(pos,state);
        var bottle=(MoonBottleBlockEntity)level.getBlockEntity(pos);
        bottle.contents().collect(new AlchemyEnvironment.Sample("minecraft:overworld",0,18000,Map.of("night",1L,"open_sky",1L,"moon_phase",0L,"moon_input_rate",1000L)));
        var drops=net.minecraft.world.level.block.Block.getDrops(state,level,pos,bottle);
        h.assertTrue(drops.size()==1&&drops.get(0).getCount()==1&&drops.get(0).is(WuXingBlocks.MOON_BOTTLE_ITEM.get()),"Moon bottle drop missing or duplicated");
        level.removeBlock(pos,false);level.setBlockAndUpdate(next,state);
        h.assertTrue(BlockItem.updateCustomBlockEntityTag(level,null,next,drops.get(0)),"Bottle drop did not restore block NBT");
        var restored=(MoonBottleBlockEntity)level.getBlockEntity(next);
        h.assertTrue(restored.contents().total()==1000&&restored.contents().sources().equals(bottle.contents().sources()),"Bottle placement lost lunar stock/provenance");h.succeed();
    }
}
