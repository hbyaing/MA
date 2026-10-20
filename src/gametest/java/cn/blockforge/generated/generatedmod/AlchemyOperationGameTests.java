package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyOperationGameTests {
    @GameTest(template="empty")
    public static void manualQualityKeepsRankCountAndFailure(GameTestHelper h) {
        var original=MedicineTier.set(PillData.create(PillCatalog.GATHER_QI,PillData.Grade.MID,PillData.Element.WATER,0),4);
        original.setCount(3);original.getOrCreateTag().putString("custom","preserved");
        var finished=AlchemyOperation.finish(original,7);
        h.assertTrue(PillData.read(finished).grade()==PillData.Grade.HIGH&&MedicineTier.of(finished)==4
                &&finished.getCount()==3&&finished.getTag().getString("custom").equals("preserved"),"Control changed rank, count or metadata");
        h.assertTrue(PillData.read(original).grade()==PillData.Grade.MID,"Pre-rolled job mutated");
        h.assertTrue(PillData.read(AlchemyOperation.finish(original,3)).grade()==PillData.Grade.MID,"Two precise phases gave a bonus");
        h.assertTrue(AlchemyOperation.finish(ItemStack.EMPTY,15).isEmpty(),"Controls bypassed failure");
        var perfect=PillData.create(PillCatalog.AURA,PillData.Grade.PERFECT,PillData.Element.METAL,0);
        h.assertTrue(PillData.read(AlchemyOperation.finish(perfect,15)).grade()==PillData.Grade.PERFECT,"Quality overflow");
        var special=PillData.create(PillCatalog.CALMING,PillData.Grade.LOW,PillData.Element.METAL,0);
        h.assertTrue(!AlchemyOperation.finish(special,15).getOrCreateTag().contains("Grade"),"Special pill gained forbidden quality");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void controlsArePerPhasePerOperatorAndPersistent(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var player=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ControlTester"));
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.setGameMode(GameType.CREATIVE);
        furnace.setItem(8,FireBottleData.fresh(1));
        furnace.startBrewing(1,641,player.getUUID());furnace.enableSpiritFuel();
        var menu=new AlchemyFurnaceMenu(1,player.getInventory(),furnace);player.containerMenu=menu;
        var snapshot=furnace.saveWithoutMetadata();snapshot.putInt("SpiritFuel",furnace.spiritCapacity());snapshot.putInt("PoweredTicks",20);furnace.load(snapshot);
        h.assertTrue(!furnace.controlFire(player),"Unmastered fire accepted a control");
        var data=GeneratedMod.data(player);h.assertTrue(data!=null,"Missing operator capability");data.wuxingxiantu$learnFire(1);
        h.assertTrue(menu.clickMenuButton(player,AlchemyOperation.CONTROL_BUTTON)&&furnace.preciseControls()==1,"Valid control refused or inaccurate");
        h.assertTrue(!menu.clickMenuButton(player,AlchemyOperation.CONTROL_BUTTON),"Repeated control accepted");
        snapshot=furnace.saveWithoutMetadata();furnace.load(snapshot);
        h.assertTrue(!furnace.controlFire(player)&&furnace.controlAttempts()==1,"Reload reset phase allowance");
        snapshot.putInt("PoweredTicks",180);furnace.load(snapshot);
        player.setPos(pos.getX()+30,pos.getY(),pos.getZ());h.assertTrue(!furnace.controlFire(player),"Distant control accepted");
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.setGameMode(GameType.SPECTATOR);h.assertTrue(!furnace.controlFire(player),"Spectator control accepted");player.setGameMode(GameType.CREATIVE);
        player.containerMenu=player.inventoryMenu;h.assertTrue(!furnace.controlFire(player),"Closed menu control accepted");player.containerMenu=menu;
        snapshot.putUUID("BrewingOperator",java.util.UUID.randomUUID());furnace.load(snapshot);
        h.assertTrue(!furnace.controlFire(player),"Other operator altered a batch");
        snapshot.putUUID("BrewingOperator",player.getUUID());furnace.load(snapshot);
        h.assertTrue(furnace.controlFire(player)&&furnace.preciseControls()==3,"Next phase did not unlock control");
        snapshot=furnace.saveWithoutMetadata();snapshot.putInt("PoweredTicks",320);furnace.load(snapshot);
        h.assertTrue(furnace.controlFire(player)&&furnace.controlAttempts()==7&&furnace.preciseControls()==3,"Missed heat still earned precision");
        furnace.finishBrewing(false);h.assertTrue(!furnace.controlFire(player),"Canceled batch accepted control");
        furnace.startBrewing(1,641);furnace.enableSpiritFuel();h.assertTrue(!furnace.controlFire(player)&&furnace.controlAttempts()==0,"Legacy batch adopted an owner or retained controls");
        player.containerMenu=player.inventoryMenu;
        h.succeed();
    }

    @GameTest(template="empty")
    public static void poweredPhasesAndHeatDoNotUseWorldClock(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        furnace.startBrewing(h.getLevel().getGameTime()-10000,h.getLevel().getGameTime()-9360);furnace.enableSpiritFuel();
        var saved=furnace.saveWithoutMetadata();saved.putInt("PoweredTicks",180);furnace.load(saved);
        h.assertTrue(furnace.operationPhase()==1&&furnace.operationHeat()==500,"World clock advanced the powered phase");
        var copy=new AlchemyFurnaceBlockEntity(furnace.getType(),pos,furnace.getBlockState());copy.setLevel(h.getLevel());copy.load(furnace.saveWithoutMetadata());
        h.assertTrue(copy.operationPhase()==1&&copy.operationHeat()==500,"Unload/reload changed timing");
        h.assertTrue(AlchemyOperation.phase(640,640)==3&&AlchemyOperation.phase(0,0)==0,"Boundary phase overflow");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void queuedSettlementUsesOperatedResultExactlyOnce(GameTestHelper h) {
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var owner=java.util.UUID.randomUUID();
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4));
        furnace.setItem(8,FireBottleData.fresh(1));
        var bottleId=FireBottleData.initialize(furnace.getItem(8)).getUUID("BottleId");
        furnace.reserve(FreeAlchemy.recipe(furnace));
        long now=h.getLevel().getGameTime();furnace.startBrewing(now,now+640,owner);furnace.enableSpiritFuel();
        var snapshot=furnace.saveWithoutMetadata();snapshot.putInt("PoweredTicks",640);
        snapshot.putInt("ControlAttempts",7);snapshot.putInt("PreciseControls",7);furnace.load(snapshot);
        var rolled=MedicineTier.set(PillData.create(PillCatalog.GATHER_QI,PillData.Grade.LOW,PillData.Element.METAL,0),2);
        var job=new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.PendingAlchemy(owner,h.getLevel().dimension(),pos,now,
                rolled,furnace.brewingRequestId(),bottleId,1,new double[8],
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","gather_qi_pill"),1,0);
        var jobs=cn.blockforge.generated.generatedmod.network.service.CraftingJobs.get(h.getLevel().getServer());jobs.add(job);
        cn.blockforge.generated.generatedmod.network.service.AlchemyService.tickAlchemy(h.getLevel().getServer());
        h.assertTrue(furnace.hasResult()&&PillData.read(furnace.completedResult()).grade()==PillData.Grade.MID
                &&MedicineTier.of(furnace.completedResult())==2&&furnace.getItem(0).isEmpty(),"Settlement lost quality or duplicated reserved material");
        var result=furnace.completedResult().copy();
        cn.blockforge.generated.generatedmod.network.service.AlchemyService.tickAlchemy(h.getLevel().getServer());
        h.assertTrue(jobs.alchemy(job.key())==null&&ItemStack.matches(result,furnace.completedResult()),"Completed operation settled twice");
        h.succeed();
    }
}
