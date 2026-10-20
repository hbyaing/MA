package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class ProfessionGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=400)
    public static void unemployedVillagerClaimsSpiritPlatform(GameTestHelper h) {
        var level=h.getLevel();
        level.setDayTime(2000);
        for(int x=0;x<7;x++) for(int z=0;z<7;z++) {
            level.setBlock(h.absolutePos(new BlockPos(x,0,z)),Blocks.STONE.defaultBlockState(),3);
            if(x==0 || z==0 || x==6 || z==6) for(int y=1;y<=3;y++)
                level.setBlock(h.absolutePos(new BlockPos(x,y,z)),Blocks.GLASS.defaultBlockState(),3);
        }
        BlockPos job=h.absolutePos(new BlockPos(3,1,3));
        level.setBlock(job,WuXingBlocks.SPIRIT_PLATFORM.get().defaultBlockState(),3);
        var villager=h.spawn(EntityType.VILLAGER,new BlockPos(3,1,2));
        villager.setAge(0);
        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE));
        villager.setVillagerXp(0);
        villager.refreshBrain(level);
        h.succeedWhen(()-> {
            var poi=level.getPoiManager().getType(job);
            h.assertTrue(poi.isPresent() && poi.get().is(PoiTypeTags.ACQUIRABLE_JOB_SITE),"Spirit platform missing from searchable job sites");
            h.assertTrue(villager.getVillagerData().getProfession()==WuXingProfessions.SPIRIT_CULTIVATOR.get(),"Villager has not naturally become a spirit cultivator");
            var claimed=villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
            h.assertTrue(claimed.isPresent() && claimed.get().pos().equals(job),"Villager did not claim this spirit platform");
            h.assertTrue(!villager.getOffers().isEmpty(),"Spirit cultivator has no trades");
        });
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void bothCustomProfessionsRemainSearchable(GameTestHelper h) {
        var registry=h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE);
        var spirit=registry.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE,WuXingProfessions.SPIRIT_POI.getId()));
        var taoist=registry.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE,WuXingProfessions.TAOIST_POI.getId()));
        h.assertTrue(spirit.is(PoiTypeTags.ACQUIRABLE_JOB_SITE),"Spirit POI missing");
        h.assertTrue(taoist.is(PoiTypeTags.ACQUIRABLE_JOB_SITE),"Taoist POI regressed");
        h.assertTrue(WuXingProfessions.SPIRIT_CULTIVATOR.get().heldJobSite().test(spirit),"Spirit profession not bound to platform");
        h.assertTrue(WuXingProfessions.TAOIST.get().heldJobSite().test(taoist),"Taoist profession not bound to drawing platform");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=400)
    public static void shrineTaoistKeepsJobBesideBarrel(GameTestHelper h) {
        var level=h.getLevel();
        level.setDayTime(2000);
        var job=h.absolutePos(new BlockPos(2,1,2));
        level.setBlock(job,Blocks.BARREL.defaultBlockState(),3);
        var villager=h.spawn(EntityType.VILLAGER,new BlockPos(2,1,1));
        villager.setAge(0);
        villager.setVillagerData(villager.getVillagerData().setProfession(WuXingProfessions.TAOIST.get()));
        villager.setVillagerXp(0);
        var offers=villager.getOffers().createTag();
        villager.getPersistentData().putBoolean(TaoistEvents.SHRINE_MERCHANT_TAG,true);
        TaoistEvents.protectShrineMerchant(villager,level);
        villager.refreshBrain(level);
        // No valid drawing platform, with a barrel available to claim after a job reset.
        villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
        villager.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE,
                net.minecraft.core.GlobalPos.of(level.dimension(),job));
        h.runAfterDelay(300,()-> {
            h.assertTrue(villager.getVillagerData().getProfession()==WuXingProfessions.TAOIST.get(),"Shrine Taoist became a fisherman");
            h.assertTrue(villager.getVillagerXp()==1,"Shrine XP floor missing");
            h.assertTrue(villager.getVillagerData().getLevel()==1,"Protection levelled up the merchant");
            h.assertTrue(villager.getOffers().createTag().equals(offers),"Protection changed saved trades");
            h.succeed();
        });
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void unmarkedFishermanIsNotReclassified(GameTestHelper h) {
        var villager=h.spawn(EntityType.VILLAGER,new BlockPos(1,1,1));
        villager.setVillagerData(villager.getVillagerData().setProfession(
                net.minecraft.world.entity.npc.VillagerProfession.FISHERMAN));
        villager.setVillagerXp(0);
        TaoistEvents.protectShrineMerchant(villager,h.getLevel());
        h.assertTrue(villager.getVillagerData().getProfession()==
                net.minecraft.world.entity.npc.VillagerProfession.FISHERMAN,"Unmarked fisherman was changed");
        h.assertTrue(villager.getVillagerXp()==0,"Unmarked fisherman received an XP lock");
        h.succeed();
    }
}
