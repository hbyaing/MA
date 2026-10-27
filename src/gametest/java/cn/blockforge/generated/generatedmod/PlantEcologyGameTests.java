package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.plants.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class PlantEcologyGameTests {
    @GameTest(template="empty")
    public static void coldOceanWeedFindsFloorBeyondSixBlocks(GameTestHelper h) throws Exception {
        var water=net.minecraft.world.level.block.Blocks.WATER.defaultBlockState();
        var gravel=net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState();
        var air=net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        var world=(net.minecraft.world.level.WorldGenLevel)java.lang.reflect.Proxy.newProxyInstance(
            PlantEcologyGameTests.class.getClassLoader(),new Class<?>[]{net.minecraft.world.level.WorldGenLevel.class},(proxy,method,args)->{
                if(method.getName().equals("getMinBuildHeight"))return -64;
                if(method.getName().equals("getMaxBuildHeight"))return 320;
                if(method.getName().equals("getHeight"))return args[0]==net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG?21:64;
                if(method.getName().equals("getBlockState")||method.getName().equals("getFluidState")){
                    int y=((net.minecraft.core.BlockPos)args[0]).getY();
                    var state=y==20?gravel:y>20&&y<64?water:air;
                    return method.getName().equals("getFluidState")?state.getFluidState():state;
                }
                throw new UnsupportedOperationException(method.getName());
            });
        var find=SpiritPlantFeature.class.getDeclaredMethod("findSpot",net.minecraft.world.level.WorldGenLevel.class,
            net.minecraft.util.RandomSource.class,int.class,int.class,SpiritPlantFeature.Rule.class,
            net.minecraft.world.level.block.state.BlockState.class,int.class);
        find.setAccessible(true);
        var found=(net.minecraft.core.BlockPos)find.invoke(null,world,net.minecraft.util.RandomSource.create(7),0,0,
            SpiritPlantFeature.Rule.WATER_BODY,SpiritPlantRegistry.DARK_WATER_WEED.get().defaultBlockState(),6);
        h.assertTrue(found!=null&&found.getY()==21,"Deep cold ocean habitat never reached its seabed: "+found);
        h.succeed();
    }

    @GameTest(template="empty")
    public static void habitatResourcesResolveAndEveryHerbIsReachable(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var biomes=h.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        int checked=0;
        for(var def:SpiritPlantCatalog.ALL) {
            if(def==SpiritPlantCatalog.DRAGON_SCALE)continue; // Independent seven-day cycle.
            var feature=registry.get(ResourceLocation.fromNamespaceAndPath("wuxingxiantu",def.id));
            h.assertTrue(feature!=null&&feature.config() instanceof SpiritPlantFeature.Config,"Missing plant codec: "+def.id);
            var config=(SpiritPlantFeature.Config)feature.config();
            h.assertTrue(config.biomeTag.isPresent(),"Unrestricted plant habitat: "+def.id);
            var tag=TagKey.create(Registries.BIOME,config.biomeTag.orElseThrow());
            var holders=biomes.getTag(tag).orElseThrow();
            h.assertTrue(holders.size()>0,"Empty habitat tag: "+def.id);
            double chance=SpiritPlantEcology.chance(def.id,def.rarity);
            h.assertTrue(chance>0&&chance<=1,"Unreachable species: "+def.id);
            checked++;
        }
        h.assertTrue(checked==31,"Catalogue coverage changed: "+checked);
        h.succeed();
    }

    @GameTest(template="empty")
    public static void probabilityChangesKeepCapsDeterminismAndEndIsolation(GameTestHelper h) {
        var eligible=new TreeMap<String,WildRarity>();
        for(var def:SpiritPlantCatalog.ALL)eligible.put(def.id,def.rarity);
        int common=0,rare=0;
        for(int i=0;i<20000;i++) {
            long seed=i*0x9E3779B97F4A7C15L;
            var plan=SpiritPlantQuota.choose(seed,false,false,eligible);
            h.assertTrue(plan.equals(SpiritPlantQuota.choose(seed,false,false,eligible)),"Quota is nondeterministic");
            h.assertTrue(plan.target()<=3&&plan.limits().values().stream().mapToInt(Integer::intValue).sum()<=plan.target(),"Regional cap exceeded");
            h.assertTrue(!plan.limits().containsKey("dragon_scale_grass"),"End plant leaked into overworld");
            if(plan.limits().containsKey("spirit_sprout_grass"))common++;
            if(plan.limits().containsKey("herb_tianji"))rare++;
            var end=SpiritPlantQuota.choose(seed,true,false,eligible);
            h.assertTrue(end.limits().keySet().stream().allMatch("dragon_scale_grass"::equals),"Overworld plant leaked into End");
        }
        h.assertTrue(common>rare*3&&rare>0,"Ecology lost common/rare ordering or made a species unreachable");
        h.succeed();
    }
}
