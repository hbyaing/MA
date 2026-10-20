package cn.blockforge.generated.generatedmod;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyInfusionGameTests {
    @GameTest(template="empty")
    public static void infusionFailureConsumesReservationAndPersists(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);
        AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var data=GeneratedMod.data(player);
        h.assertTrue(data!=null,"Missing player cultivation capability");
        data.wuxingxiantu$setAura(100);
        data.wuxingxiantu$learnFire(1);
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4));
        furnace.reserve(cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.recipe(furnace));
        furnace.startBrewing(1,101);furnace.enableSpiritFuel();
        h.assertTrue(furnace.injectSpirit(player)==2 && furnace.injectSpirit(player)==0,"Injection not rate limited");
        h.assertTrue(data.wuxingxiantu$getAura()==98 && furnace.spiritFuel()==2,"Transfer not conserved");
        for(int i=0;i<20;i++)furnace.advancePoweredTick();
        h.assertTrue(furnace.spiritFuel()==1 && Math.abs(furnace.brewingProgress()-.2F)<.001F,"Burn consumption incorrect");
        var jobs=cn.blockforge.generated.generatedmod.network.service.CraftingJobs.get(h.getLevel().getServer());
        var job=new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.PendingAlchemy(player.getUUID(),h.getLevel().dimension(),pos,101,
                cn.blockforge.generated.generatedmod.alchemy.PillData.create(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.AURA,
                cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.LOW,cn.blockforge.generated.generatedmod.alchemy.PillData.Element.METAL,0),
                furnace.brewingRequestId(),cn.blockforge.generated.generatedmod.fire.FireBottleData.initialize(furnace.getItem(8)).getUUID("BottleId"),
                1,new double[8],new net.minecraft.resources.ResourceLocation("wuxingxiantu","aura_pill"),1,6);
        jobs.add(job);
        var empty=furnace.saveWithoutMetadata();empty.putInt("SpiritFuel",0);furnace.load(empty);
        furnace.advancePoweredTick();
        h.assertTrue(!furnace.brewing() && furnace.outcome()==2 && furnace.getItem(0).isEmpty()
                && furnace.completedResult().is(WuXingItems.PILL_ASH.get()),"Fuel exhaustion did not consume and produce ash");
        h.assertTrue(jobs.alchemy(job.key())==null,"Failed fuel job remained queued");
        var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        for(int i=0;i<20;i++)furnace.advancePoweredTick();
        h.assertTrue(!furnace.brewing() && furnace.outcome()==2 && furnace.getItem(0).isEmpty(),"Failed save resumed or duplicated materials");
        h.assertTrue(furnace.collectResult(player)==1&&furnace.collectResult(player)==0,"Failure ash did not collect exactly once");
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),2));
        furnace.reserve(cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.recipe(furnace));
        furnace.startBrewing(1,21);furnace.enableSpiritFuel();
        var last=furnace.saveWithoutMetadata();last.putInt("SpiritFuel",1);furnace.load(last);
        for(int i=0;i<20;i++)furnace.advancePoweredTick();
        h.assertTrue(furnace.brewing() && furnace.poweredComplete() && furnace.spiritFuel()==0,"Exact final fuel unit incorrectly failed successful batch");
        h.assertTrue(furnace.complete(cn.blockforge.generated.generatedmod.alchemy.PillData.create(
                cn.blockforge.generated.generatedmod.alchemy.PillCatalog.AURA,cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.LOW,
                cn.blockforge.generated.generatedmod.alchemy.PillData.Element.METAL,0)),"Exact-budget batch did not finish");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void coreUpgradeKeepsMachineInventoryAndFuel(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);
        AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        furnace.setItem(0,new ItemStack(Items.APPLE,4));
        furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));
        furnace.setItem(9,new ItemStack(WuXingBlocks.ALCHEMY_FURNACE_HIGH.get()));
        var saved=furnace.saveWithoutMetadata();saved.putInt("SpiritFuel",57);furnace.load(saved);
        h.assertTrue(!furnace.canPlaceItem(9,new ItemStack(Items.STONE)),"Invalid core accepted");
        h.assertTrue(furnace.upgradeCore(player),"Core upgrade rejected");
        var upgraded=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        h.assertTrue(upgraded.tier()==3 && AlchemyFurnaceBlock.isFormed(upgraded.getBlockState()),"Structure or tier lost");
        h.assertTrue(upgraded.getItem(0).getCount()==4 && upgraded.getItem(8).getCount()==1
                && upgraded.getItem(9).isEmpty() && upgraded.spiritFuel()==57,"Upgrade lost or duplicated machine contents");
        h.assertTrue(!upgraded.upgradeCore(player),"Empty core slot upgraded twice");
        h.succeed();
    }
}
