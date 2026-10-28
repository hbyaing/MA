package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.alchemy.research.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class ResearchKnowledgeGameTests {
    private static ResearchCurriculum teach(GameTestPlayer p){FormulaKnowledge.unlock(p);FormulaKnowledge.learn(p,ResourceLocation.parse("wuxingxiantu:golden_wound_pill"),false);return ResearchCurriculum.load(p);}
    @GameTest(template="empty") public static void readingHasPrerequisitesDeduplicatesAndNeverTrainsPractice(GameTestHelper h){try(var f=ResearchTableGameTests.setup(h)){
        var c=teach(f.p());var t=c.templates.get(0);var profession=PlayerCrafts.get(f.p().server).profession(f.p().getUUID(),"alchemy").copy();
        h.assertTrue(FormulaKnowledge.study(f.p(),c,t)==1&&!FormulaKnowledge.understands(f.p(),"conservation"),"Reading skipped prerequisite layer");
        h.assertTrue(FormulaKnowledge.study(f.p(),c,t)==1&&FormulaKnowledge.study(f.p(),c,t)==1,"Theory layers not understood");
        h.assertTrue(FormulaKnowledge.study(f.p(),c,t)==0&&!FormulaKnowledge.understands(f.p(),"thermal"),"Missing real practice bypassed");
        h.assertTrue(profession.equals(PlayerCrafts.get(f.p().server).profession(f.p().getUUID(),"alchemy")),"Reading changed practice XP/rank/works");
        for(int i=0;i<10;i++)h.assertTrue(FormulaKnowledge.study(f.p(),ResearchCurriculum.load(f.p()),t)==0,"Reload/duplicate granted more learning");
    }h.succeed();}
    @GameTest(template="empty") public static void unownedSourcesAndForgedClientFlagsCannotLearnOrSave(GameTestHelper h){try(var f=ResearchTableGameTests.setup(h)){
        var c=ResearchCurriculum.load(f.p());h.assertTrue(FormulaKnowledge.study(f.p(),c,c.templates.get(0))==0,"Unowned formula read");
        f.menu().clickMenuButton(f.p(),3);f.menu().setData(55,1);f.menu().setData(56,1);
        h.assertTrue(!f.menu().clickMenuButton(f.p(),22)&&!f.menu().clickMenuButton(f.p(),23)&&f.table().getItem(1).getCount()==3,"Forged menu view wrote knowledge or charged supplies");
        teach(f.p());f.menu().broadcastChanges();h.assertTrue(!f.menu().researchText(0).contains("最低治疗"),"Unlearned body leaked constraints");
        f.menu().clickMenuButton(f.p(),21);h.assertTrue(f.menu().researchText(0).contains("尚未理解")&&!f.menu().researchText(0).contains("0.25"),"Partial understanding bypassed projection");
    }h.succeed();}
    @GameTest(template="empty") public static void allTerminalOutcomesRecordedOnceInExistingAccount(GameTestHelper h){try(var f=V5BatchGameTests.setup(h);var other=new GameTestPlayer(h.getLevel())){
        var journal=new BatchJournal(f.player().server);var active=V5Batch.prepare(f.furnace(),f.player());journal.write(active);h.assertTrue(ResearchPractice.records(f.player()).isEmpty(),"Active batch counted as practice");
        var failed=V5Batch.seal(active);journal.write(failed);journal.write(failed);h.assertTrue(ResearchPractice.records(f.player()).size()==1&&ResearchPractice.records(f.player()).getCompound(0).getInt("Grade")==0,"Failure omitted or replay duplicated");
        V5EffectGameTests.ready(f,Map.of("extract_mu",80L));h.assertTrue(ResearchPractice.records(f.player()).size()==2&&ResearchPractice.records(other).isEmpty(),"Success omitted or other player sees practice");
        var store=PlayerCrafts.get(f.player().server);var restored=PlayerCrafts.load(store.save(new CompoundTag()));h.assertTrue(restored.profession(f.player().getUUID(),"alchemy").getList("V5Practice",Tag.TAG_COMPOUND).size()==2,"Practice lost on world NBT roundtrip");
    }h.succeed();}
    @GameTest(template="empty") public static void templateSaveConsumesOnlyOnceAndRetainsUnverifiedProvenance(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var c=teach(f.player());V5EffectGameTests.ready(f,Map.of("extract_mu",80L));for(int i=0;i<3;i++)FormulaKnowledge.study(f.player(),c,c.templates.get(0));
        var pos=f.pos().offset(3,0,0);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.ALCHEMY_RESEARCH_TABLE.get().defaultBlockState());var table=(ResearchTableEntity)h.getLevel().getBlockEntity(pos);f.player().setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        table.setItem(1,new ItemStack(Items.PAPER,2));var menu=new ResearchTableMenu(1,f.player().getInventory(),table);menu.clickMenuButton(f.player(),3);
        h.assertTrue(!menu.clickMenuButton(f.player(),23)&&table.getItem(1).getCount()==2,"Missing ink consumed paper");table.setItem(2,new ItemStack(Items.INK_SAC,2));
        h.assertTrue(menu.clickMenuButton(f.player(),23)&&!menu.clickMenuButton(f.player(),23)&&table.getItem(1).getCount()==1&&table.getItem(2).getCount()==1,"Template save replay/cost incorrect");
        var notes=FormulaKnowledge.understanding(f.player()).getCompound("Templates");var n=notes.getCompound(c.templates.get(0).revision());
        h.assertTrue(n.getString("Status").equals("UNVERIFIED")&&n.getString("Source").equals(c.templates.get(0).formula())&&n.getString("Module").equals("heal")&&n.contains("Advice"),"Source/constraints lost or certification invented");h.getLevel().removeBlock(pos,false);
    }h.succeed();}
    @GameTest(template="empty") public static void knowledgeDoesNotChangePhysicalSnapshotOrRandomSequence(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var d=V5Batch.prepare(f.furnace(),f.player());d.putLong("LastWorldTick",h.getLevel().getGameTime()-20);var before=V5Batch.advance(d,h.getLevel(),f.pos());var c=teach(f.player());for(int i=0;i<6;i++)FormulaKnowledge.study(f.player(),c,c.templates.get(0));
        var after=V5Batch.advance(d,h.getLevel(),f.pos());h.assertTrue(V5Batch.core(before).tick()>V5Batch.core(d).tick(),"Physical invariance test did not execute a real step");h.assertTrue(before.equals(after),"Knowledge changed same pinned input/operation/seed result");
        h.assertTrue(!FormulaKnowledge.understanding(f.player()).contains("Xp"),"Theory masquerades as practice XP");
    }h.succeed();}
    @GameTest(template="empty") public static void understandingClonesWithoutSharingCertification(GameTestHelper h){try(var f=ResearchTableGameTests.setup(h);var clone=new GameTestPlayer(h.getLevel());var stranger=new GameTestPlayer(h.getLevel())){
        var c=teach(f.p());FormulaKnowledge.study(f.p(),c,c.templates.get(0));FormulaKnowledge.clone(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(clone,f.p(),true));
        h.assertTrue(FormulaKnowledge.understands(clone,"observation")&&!FormulaKnowledge.understands(stranger,"observation"),"Death clone lost knowledge or leaked to other player");
        teach(stranger);h.assertTrue(!FormulaKnowledge.understands(stranger,"observation")&&FormulaKnowledge.understanding(stranger).getCompound("Templates").isEmpty(),"Sharing formula inherited author understanding/certification");
    }h.succeed();}
    @GameTest(template="empty") public static void malformedCurriculumFailsClosedWithoutPlayerMutation(GameTestHelper h){try(var f=ResearchTableGameTests.setup(h)){
        var c=ResearchCurriculum.load(f.p());String json;try(var r=f.p().server.getResourceManager().openAsReader(ResourceLocation.parse("wuxingxiantu:alchemy/research.json"))){json=new java.io.BufferedReader(r).lines().collect(java.util.stream.Collectors.joining("\n"));}catch(Exception e){throw new RuntimeException(e);}
        var before=FormulaKnowledge.understanding(f.p());var cyclic=cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRuleLoader.document(json);cyclic.getAsJsonArray("concepts").get(0).getAsJsonObject().getAsJsonArray("requires").add("thermal");var unknown=cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRuleLoader.document(json);unknown.getAsJsonArray("templates").get(0).getAsJsonObject().addProperty("module","run_script");for(String bad:List.of(cyclic.toString(),unknown.toString(),json.replace("\"schema\": 1","\"schema\": 2"),json.replace("\"schema\": 1","\"schema\": 1.5"),json.replace("\"schema\": 1","\"schema\": 1, \"schema\": 1"))){boolean rejected=false;try{new ResearchCurriculum(bad);}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Invalid/cyclic curriculum accepted");}
        h.assertTrue(before.equals(FormulaKnowledge.understanding(f.p()))&&c.templates.size()==26,"Bad definition mutated player");
    }h.succeed();}
    @GameTest(template="empty") public static void targetConstraintsEvaluateActualEffectsWithoutAssigningGrade(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var c=ResearchCurriculum.load(f.player());var d=V5EffectGameTests.ready(f,Map.of("extract_mu",80L));var actual=PillSnapshot.read(d.getCompound("PillResult"));
        h.assertTrue(c.templates.get(0).qualifies(actual)&&!c.templates.get(1).qualifies(actual),"Hard target predicate ignored real effect");
        var before=d.copy();c.templates.forEach(t->t.qualifies(actual));h.assertTrue(d.equals(before),"Template evaluation changed actual result");
    }h.succeed();}
}
