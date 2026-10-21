package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.*;
import com.mojang.authlib.GameProfile;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class MyriadMedicineGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){
        var p=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"MedicineQA"));
        var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(0);d.wuxingxiantu$setCultivationStage(0);d.wuxingxiantu$restoreDaoXin(0);return p;
    }
    private static ItemStack herb(String id,int years,int count){
        var stack=new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:"+id)),count);
        HerbAge.setYears(stack,years);return stack;
    }
    private static ItemStack pill(PillCatalog pill,int rank){return MedicineTier.set(PillData.create(pill,PillData.Grade.LOW,PillData.Element.METAL,0),rank);}
    @GameTest(template="empty")
    public static void rankedRecipeSelectsAnAgedGuideAndConsumesThatSameAllocation(GameTestHelper h){
        var guide=herb("herb_xingchen",6500,1);var young=herb("herb_xingchen",500,1);
        var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(guide.getItem()));
        var recipe=new AlchemyRecipe(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:qa_ranked"),ingredients,List.of(1),pill(PillCatalog.GATHER_QI,4),4,100,1,true);
        var input=new SimpleContainer(8);input.setItem(0,young);input.setItem(1,guide);
        h.assertTrue(recipe.matches(input,h.getLevel()),"A valid aged guide was hidden behind a younger identical species");
        var allocated=recipe.reservation(input);h.assertTrue(allocated[0]==0&&allocated[1]==1,"Wrong specimen allocated");
        var consumed=recipe.consume(input);h.assertTrue(consumed.size()==1&&HerbAge.years(consumed.get(0))==6500,"Consumption differed from preview");
        h.assertTrue(input.getItem(0).getCount()==1&&!recipe.matches(input,h.getLevel()),"Young herb satisfied high-rank formula");h.succeed();
    }
    @GameTest(template="empty")
    public static void oldFormulasStillAcceptUnagedHerbs(GameTestHelper h){
        var stack=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get());
        var ingredients=NonNullList.<Ingredient>create();ingredients.add(Ingredient.of(stack.getItem()));
        var recipe=new AlchemyRecipe(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:qa_legacy"),ingredients,List.of(1),PillData.create(PillCatalog.AURA,PillData.Grade.LOW,PillData.Element.METAL,0),0,100,1,false);
        var input=new SimpleContainer(8);input.setItem(0,stack);
        h.assertTrue(recipe.matches(input,h.getLevel()),"New age gate broke old formulas");h.succeed();
    }
    @GameTest(template="empty")
    public static void dataRecipesCarryRankAcrossNetworkAndKeepAllGates(GameTestHelper h){
        var manager=h.getLevel().getRecipeManager();
        for(String id:List.of("gather_qi_pill","origin_condensing_pill","meridian_expansion_pill","barrier_preparation_pill","rejuvenation_pill"))for(int tier=1;tier<=5;tier++){
            var recipe=(AlchemyRecipe)manager.byKey(net.minecraft.resources.ResourceLocation.tryParse("wuxingxiantu:myriad_"+id+"_tier_"+tier)).orElseThrow();
            var out=recipe.getResultItem(h.getLevel().registryAccess());
            h.assertTrue(MedicineTier.of(out)==tier&&recipe.minimumRealm()==tier-1,"Data recipe rank/gate lost");
            h.assertTrue(recipe.furnaceTier()==(tier==1?0:tier)&&recipe.minFireTier()==2*tier-1,"Furnace/fire restriction weakened");
            var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try{var serializer=new AlchemyRecipe.Serializer();serializer.toNetwork(buf,recipe);var copy=serializer.fromNetwork(recipe.getId(),buf);
                h.assertTrue(MedicineTier.of(copy.getResultItem(h.getLevel().registryAccess()))==tier,"Network stripped medicine rank");}finally{buf.release();}
        }
        var item=herb("herb_tianji",8500,1);
        h.assertTrue(HerbAge.effectiveTier(item)==5&&HerbCultivationData.cultivationCap(HerbAge.definition(item),true,100)==5,"No species can reach immortal medicinal rank");h.succeed();
    }
    @GameTest(template="empty")
    public static void originMedicineRequiresMeditationAndDoesNotCrossStages(GameTestHelper h){
        var p=player(h);var d=GeneratedMod.data(p);d.wuxingxiantu$setCultivation(0);d.wuxingxiantu$setMeditating(false);
        h.assertTrue(PillEffects.apply(p,d,pill(PillCatalog.ORIGIN_CONDENSING,1)),"Origin reserve rejected");
        long power=MedicineProgress.state(p).getLong("OriginPower");
        MedicineProgress.second(p);h.assertTrue(d.wuxingxiantu$getCultivation()==0,"Origin medicine gave instant cultivation");
        h.assertTrue(!PillEffects.apply(p,d,pill(PillCatalog.ORIGIN_CONDENSING,1)),"Origin reserves stacked");
        d.wuxingxiantu$setMeditating(true);MedicineProgress.second(p);
        h.assertTrue(d.wuxingxiantu$getCultivation()>0&&MedicineProgress.state(p).getLong("OriginPower")<power,"Meditation did not refine reserve");
        int stage=d.wuxingxiantu$getCultivationStage();d.wuxingxiantu$setCultivation(CultivationLogic.breakthroughRequirement(d));MedicineProgress.second(p);
        h.assertTrue(d.wuxingxiantu$getCultivationStage()==stage,"Origin medicine crossed a stage");
        d.wuxingxiantu$setCultivationStage(stage+1);MedicineProgress.second(p);
        h.assertTrue(MedicineProgress.state(p).getLong("OriginPower")==0,"Old-stage reserve crossed breakthrough");h.succeed();
    }
    @GameTest(template="empty")
    public static void permanentCapacitySharesTargetCeilingAndHasPerRealmLimit(GameTestHelper h){
        var p=player(h);var d=GeneratedMod.data(p);d.wuxingxiantu$setAuraBonus(0);
        h.assertTrue(PillEffects.apply(p,d,pill(PillCatalog.MERIDIAN_EXPANSION,1)),"First expansion rejected");
        h.assertTrue(PillEffects.apply(p,d,pill(PillCatalog.MERIDIAN_EXPANSION,1)),"Second expansion rejected");
        h.assertTrue(!PillEffects.apply(p,d,pill(PillCatalog.MERIDIAN_EXPANSION,1))&&d.wuxingxiantu$getAuraBonus()==2,"Per-realm cap bypassed");
        var loaded=new CultivationDataImpl();loaded.deserializeNBT(d.serializeNBT());
        h.assertTrue(loaded.wuxingxiantu$getAuraBonus()==2,"Permanent expansion lost on capability reload");
        d.wuxingxiantu$setRealm(1);d.wuxingxiantu$setAuraBonus(25);
        h.assertTrue(!PillEffects.apply(p,d,pill(PillCatalog.MERIDIAN_EXPANSION,1)),"Shared 25% target limit bypassed");h.succeed();
    }
    @GameTest(template="empty")
    public static void recuperationAndManaRecoveryAreGradualRatherThanInstant(GameTestHelper h){
        var p=player(h);var d=GeneratedMod.data(p);p.setHealth(5);d.wuxingxiantu$setAura(0);
        h.assertTrue(PillEffects.apply(p,d,pill(PillCatalog.REJUVENATION,1))&&PillEffects.apply(p,d,pill(PillCatalog.GATHER_QI,1)),"Recovery medicines rejected");
        h.assertTrue(p.getHealth()==5&&d.wuxingxiantu$getAura()==0,"Long-effect medicine became instant recovery");
        h.assertTrue(!PillEffects.apply(p,d,pill(PillCatalog.GATHER_QI,1)),"Equal mana effects stacked");
        MedicineProgress.second(p);h.assertTrue(p.getHealth()>5&&d.wuxingxiantu$getAura()>0,"Recovery did not tick");
        float before=p.getHealth();h.assertTrue(PillEffects.apply(p,d,PillData.create(PillCatalog.GOLDEN_WOUND,PillData.Grade.LOW,PillData.Element.METAL,0))&&p.getHealth()>before+1,"Original emergency healing changed");h.succeed();
    }
    @GameTest(template="empty")
    public static void preparationCannotSkipTribulationRequirements(GameTestHelper h){
        var p=player(h);var d=GeneratedMod.data(p);d.wuxingxiantu$setCultivationStage(3);d.wuxingxiantu$setCultivation(0);d.wuxingxiantu$setAura(0);
        h.assertTrue(PillEffects.apply(p,d,pill(PillCatalog.BARRIER_PREPARATION,1)),"Peak preparation rejected");
        h.assertTrue(!TribulationManager.start(p)&&d.wuxingxiantu$getRealm()==0,"Preparation skipped cultivation, catalyst or mana gate");
        h.assertTrue(MedicineProgress.state(p).getInt("PreparedBarrier")==1,"Rejected trial consumed preparation");h.succeed();
    }
}
