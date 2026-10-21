package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.economy.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.world.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class MyriadFoundationGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){
        return FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"MyriadQA"));
    }
    @GameTest(template="empty")
    public static void gradedPaymentReturnsExactChangeAndKeepsLegacyCurrency(GameTestHelper h){
        var inventory=player(h).getInventory();
        inventory.setItem(0,new ItemStack(WuXingItems.SPIRIT_STONE_SUPERIOR.get()));
        inventory.setItem(1,new ItemStack(WuXingItems.SPIRIT_STONE.get(),2));
        h.assertTrue(SpiritCurrency.value(inventory.items)==83,"Legacy stone must equal one low unit");
        h.assertTrue(SpiritCurrency.pay(inventory,12),"Mixed-grade payment rejected");
        h.assertTrue(SpiritCurrency.value(inventory.items)==71,"Payment lost or created value");
        h.assertTrue(SpiritCurrency.pay(inventory,71)&&SpiritCurrency.value(inventory.items)==0,"Exact remaining payment failed");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void failedPaymentNeverConsumesCurrencyOrOtherItems(GameTestHelper h){
        var inventory=player(h).getInventory();
        for(int slot=0;slot<inventory.items.size();slot++)inventory.setItem(slot,new ItemStack(Items.COBBLESTONE,64));
        inventory.setItem(0,new ItemStack(WuXingItems.SPIRIT_STONE_EXQUISITE.get(),2));
        var before=inventory.items.stream().map(ItemStack::copy).toList();
        h.assertTrue(!SpiritCurrency.pay(inventory,1),"Full inventory cannot receive exact change");
        h.assertTrue(!SpiritCurrency.pay(inventory,2000),"Insufficient funds accepted");
        for(int slot=0;slot<before.size();slot++)h.assertTrue(ItemStack.matches(before.get(slot),inventory.getItem(slot)),"Failed payment mutated inventory");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void veinDiscoveryRequiresSuccessfulPlacementAndSurvivesReload(GameTestHelper h){
        var data=new SpiritVeinData();var pos=new BlockPos(100,10,200);var chunk=new ChunkPos(6,12);
        data.queue(chunk);
        h.assertTrue(data.nearest(pos,384).isEmpty()&&data.auraAt(pos)==0,"Pending candidate produced phantom aura");
        var pending=SpiritVeinData.load(data.save(new CompoundTag()));
        h.assertTrue(pending.pending().contains(chunk.toLong()),"Pending decoration lost on restart");
        pending.register(6,12,3,pos,192);pending.finish(chunk);
        var restored=SpiritVeinData.load(pending.save(new CompoundTag()));
        h.assertTrue(restored.grade(6,12)==3&&restored.auraAt(pos)==65,"Generated vein did not persist");
        h.assertTrue(restored.nearest(pos,1).orElseThrow().center().equals(pos),"Detector center differs from actual vein");
        restored.queue(chunk);
        h.assertTrue(restored.pending().isEmpty(),"Completed vein was scheduled again");h.succeed();
    }
    @GameTest(template="empty")
    public static void continuousVeinPrefixDoesNotJoinDisconnectedCaves(GameTestHelper h){
        var cells=List.of(1,2,3,4,20,21);
        var shape=ConnectedVeinShape.largest(cells,p->List.of(p-1,p+1),Comparator.naturalOrder());
        h.assertTrue(shape.equals(List.of(1,2,3,4)),"Disconnected walls joined or ordering unstable");
        for(int size=1;size<=shape.size();size++){
            var prefix=shape.subList(0,size);
            h.assertTrue(prefix.stream().mapToInt(Integer::intValue).max().orElseThrow()-prefix.get(0)==size-1,"Truncated vein disconnected");
        }
        h.succeed();
    }
    @GameTest(template="empty")
    public static void importedHerbsUseTargetProcessingAndDoNotReplaceGaleGrass(GameTestHelper h){
        h.assertTrue(ImportedHerbs.DEFINITIONS.size()==18&&SpiritPlantRegistry.PLANT_BLOCKS.size()==32,"Herbs missing from target registry");
        for(var def:ImportedHerbs.DEFINITIONS){
            var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(GeneratedMod.MOD_ID+":"+def.id));
            var stack=new ItemStack(item);HerbAge.setYears(stack,2500);
            h.assertTrue(HerbPreparation.herb(stack)&&HerbAge.years(HerbPreparation.prepare(stack))==2500,"Processing lost herb years: "+def.id);
        }
        h.assertTrue(SpiritPlantRegistry.GALE_GRASS.getId().getPath().equals("gale_grass")&&SpiritPlantCatalog.BY_ID.containsKey("herb_jifeng"),"Wind plants collided");h.succeed();
    }
    @GameTest(template="empty")
    public static void medicinalAgeCannotPromoteOrdinarySpeciesToImmortal(GameTestHelper h){
        h.assertTrue(HerbAge.tier(499)==0&&HerbAge.tier(500)==1&&HerbAge.tier(2499)==1&&HerbAge.tier(2500)==2,"Year thresholds shifted");
        h.assertTrue(HerbCultivationData.cultivationCap(SpiritPlantCatalog.SPIRIT_SPROUT,true,100)==1,"Common grass became immortal");
        h.assertTrue(HerbCultivationData.cultivationCap(SpiritPlantCatalog.STARLIGHT_FLOWER,false,100)==1,"Vein bypassed soil requirement");
        h.assertTrue(HerbCultivationData.cultivationCap(SpiritPlantCatalog.STARLIGHT_FLOWER,true,0)==2,"High tier needs vein");
        var stack=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get());HerbAge.setYears(stack,10499);
        h.assertTrue(HerbAge.effectiveTier(stack)==1,"Item NBT bypassed species limit");h.succeed();
    }
    @GameTest(template="empty")
    public static void herbYearsSurvivePlayerHarvestAndReplant(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));
        level.setBlockAndUpdate(pos.below(),Blocks.DIRT.defaultBlockState());
        var plant=(SpiritPlantBlock)SpiritPlantRegistry.GALE_GRASS.get();
        var specimen=new ItemStack(plant.asItem());HerbAge.setYears(specimen,2500);
        level.setBlockAndUpdate(pos,plant.defaultBlockState());
        HerbCultivationData.get(level).plant(level,pos,plant.definition(),specimen);
        var saved=HerbCultivationData.load(HerbCultivationData.get(level).save(new CompoundTag()));
        h.assertTrue(saved.years(pos)==2500,"Planted age lost on reload");
        var fake=player(h);fake.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(fake.gameMode.destroyBlock(pos),"Player harvest failed");
        var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(1));
        var harvested=drops.stream().map(net.minecraft.world.entity.item.ItemEntity::getItem).filter(s->s.is(plant.asItem())).findFirst().orElseThrow();
        h.assertTrue(HerbAge.years(harvested)==2500,"onRemove erased years before player drops");
        level.setBlockAndUpdate(pos,plant.defaultBlockState());
        plant.setPlacedBy(level,pos,plant.defaultBlockState(),fake,harvested);
        h.assertTrue(HerbCultivationData.get(level).years(pos)==2500,"Replant lost years");h.succeed();
    }
}
