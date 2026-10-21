package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.plants.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class MyriadResidueGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ResidueQA"));
    }
    @GameTest(template="empty")
    public static void ashUsesAreBoundedAcrossHarvestReplantAndReload(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));var p=player(h);
        var plant=(SpiritPlantBlock)SpiritPlantRegistry.GALE_GRASS.get();
        level.setBlockAndUpdate(pos.below(),WuXingBlocks.SPIRIT_SOIL.get().defaultBlockState());
        level.setBlockAndUpdate(pos,plant.defaultBlockState());
        var specimen=new ItemStack(plant.asItem());HerbAge.setYears(specimen,2500);
        plant.setPlacedBy(level,pos,plant.defaultBlockState(),p,specimen);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(WuXingItems.PILL_ASH.get(),5));
        var ctx=new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        for(int i=0;i<3;i++)h.assertTrue(WuXingItems.PILL_ASH.get().useOn(ctx).consumesAction(),"Legal ash use failed");
        h.assertTrue(!WuXingItems.PILL_ASH.get().useOn(ctx).consumesAction()&&p.getMainHandItem().getCount()==2,"Ash feeding was unlimited or consumed on failure");
        var data=HerbCultivationData.get(level);
        h.assertTrue(data.years(pos)==2500&&HerbCultivationData.load(data.save(new CompoundTag())).ashUses(pos)==3,"Ash changed years or lost its limit on reload");
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));p.gameMode.destroyBlock(pos);
        var dropped=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(1)).stream()
            .map(net.minecraft.world.entity.item.ItemEntity::getItem).filter(s->s.is(plant.asItem())&&HerbAge.years(s)==2500).findFirst().orElseThrow();
        h.assertTrue(dropped.getTag().getInt(HerbAge.ASH_USES)==3,"Harvest erased substrate use count");
        level.setBlockAndUpdate(pos,plant.defaultBlockState());plant.setPlacedBy(level,pos,plant.defaultBlockState(),p,dropped);
        h.assertTrue(!plant.applyAsh(level,pos,plant.defaultBlockState()),"Replant reset the ash limit");h.succeed();
    }
    @GameTest(template="empty")
    public static void ashCannotBypassSpiritSoilOrMaturity(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));
        var plant=(SpiritPlantBlock)SpiritPlantRegistry.GALE_GRASS.get();
        level.setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos,plant.defaultBlockState());
        h.assertTrue(!plant.applyAsh(level,pos,plant.defaultBlockState()),"Ash bypassed spirit soil");
        h.assertTrue(HerbCultivationData.get(level).ashUses(pos)==0,"Rejected feeding spent specimen allowance");
        level.setBlockAndUpdate(pos.below(),WuXingBlocks.SPIRIT_SOIL.get().defaultBlockState());
        var mature=plant.defaultBlockState().setValue(SpiritPlantBlock.AGE,6);level.setBlockAndUpdate(pos,mature);
        h.assertTrue(!plant.applyAsh(level,pos,mature),"Ash fed an already mature plant");h.succeed();
    }
    @GameTest(template="empty")
    public static void rankedFailureProducesOneRecoverableWasteAndCannotSettleTwice(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));
        level.setBlockAndUpdate(pos,WuXingBlocks.ALCHEMY_FURNACE.get().defaultBlockState());
        var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);
        var input=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4);HerbAge.setYears(input,500);
        furnace.setItem(0,input);var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(input.getItem()));
        var recipe=new AlchemyRecipe(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:qa_waste"),ingredients,List.of(4),MedicineTier.set(PillData.create(PillCatalog.GATHER_QI,PillData.Grade.LOW,PillData.Element.METAL,0),1),0,20,1,false);
        furnace.reserve(recipe);furnace.startBrewing(level.getGameTime(),level.getGameTime()+20);furnace.rankedFailure(true);
        var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        h.assertTrue(furnace.failExperiment(),"Ranked failure did not settle");
        h.assertTrue(furnace.getItem(0).isEmpty()&&furnace.completedResult().is(WuXingItems.WASTE_PILL.get())&&furnace.completedResult().getCount()==1,"Failure duplicated inputs or lost waste product");
        h.assertTrue(!furnace.failExperiment()&&furnace.completedResult().getCount()==1,"Failure settled twice");
        var recovery=(net.minecraft.world.item.crafting.SmeltingRecipe)level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:waste_pill_to_ash")).orElseThrow();
        var result=recovery.getResultItem(level.registryAccess());
        h.assertTrue(result.is(WuXingItems.PILL_ASH.get())&&result.getCount()==1&&recovery.getExperience()==0&&recovery.getCookingTime()>0,"Lossy recovery minted herbs, XP or extra residue");h.succeed();
    }
    @GameTest(template="empty")
    public static void higherMedicinalRanksImproveBoundedEffects(GameTestHelper h){
        var p=player(h);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(4);d.wuxingxiantu$restoreDaoXin(0);d.wuxingxiantu$setAuraBonus(0);
        var low=MedicineTier.set(PillData.create(PillCatalog.GATHER_QI,PillData.Grade.LOW,PillData.Element.METAL,0),1);
        var high=MedicineTier.set(low.copy(),5);
        h.assertTrue(PillEffects.apply(p,d,low),"Yellow medicine rejected");int duration=MedicineProgress.state(p).getInt("QiSeconds");
        h.assertTrue(PillEffects.apply(p,d,high)&&MedicineProgress.state(p).getInt("QiSeconds")>duration,"High rank had no benefit");
        h.assertTrue(!PillEffects.apply(p,d,low),"Weaker rank overwrote stronger medicine");
        var expansion=MedicineTier.set(PillData.create(PillCatalog.MERIDIAN_EXPANSION,PillData.Grade.LOW,PillData.Element.METAL,0),5);
        h.assertTrue(PillEffects.apply(p,d,expansion)&&d.wuxingxiantu$getAuraBonus()==3,"Ranked expansion did not improve bounded capacity");
        var origin=MedicineTier.set(PillData.create(PillCatalog.ORIGIN_CONDENSING,PillData.Grade.PERFECT,PillData.Element.METAL,0),5);
        d.wuxingxiantu$setCultivation(0);h.assertTrue(PillEffects.apply(p,d,origin),"Immortal origin medicine rejected");
        h.assertTrue(MedicineProgress.state(p).getLong("OriginPower")<=cn.blockforge.generated.generatedmod.items.CultivationLogic.breakthroughRequirement(d)/2,"High rank overflowed bounded reserve");h.succeed();
    }
}
