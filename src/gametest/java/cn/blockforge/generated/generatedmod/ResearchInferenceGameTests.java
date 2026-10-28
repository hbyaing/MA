package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.alchemy.research.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class ResearchInferenceGameTests {
    // Canonical journal fixtures use the production compiler. Live time/controls are tested separately by the client.
    static CompoundTag terminal(V5BatchGameTests.Fixture f,int years,int count,boolean success){
        var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),count);HerbAge.setYears(herb,years);f.furnace().setItem(0,herb);
        var d=V5Batch.prepare(f.furnace(),f.player());var profile=V5Batch.profile(d.getString("Profile"),d.getInt("Burn"));var initial=V5Batch.core(d);
        var stocks=success?Map.of("extract_mu",80L):Map.of("herb_inert",80L);var s=AlchemySimulation.begin(profile.rules(),d.getUUID("Id").toString(),stocks,0,80,initial.seed(),initial.environment());
        s=new Snapshot(s.batchId(),s.rulesHash(),0,SimulationRules.Phase.COOL,Status.FINISHED,"",s.stocks(),0,0,s.referenceMass(),s.seed(),s.environment(),s.draws(),s.holds(),s.lastSequence(),s.operations(),s.ledger());
        d.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(profile.rules(),s));d=V5Batch.seal(d);new BatchJournal(f.player().server).write(d);return d;
    }
    static ResearchMethods educate(GameTestPlayer p){FormulaKnowledge.learn(p,ResourceLocation.parse("wuxingxiantu:golden_wound_pill"),false);var c=ResearchCurriculum.load(p);for(int i=0;i<3;i++)FormulaKnowledge.study(p,c,c.templates.get(0));var methods=new ResearchMethods(p);if(!methods.study(p)&&!methods.learned(p))throw new AssertionError("Research prerequisites not achieved");return methods;}
    private static ResearchInference solve(GameTestPlayer p,CompoundTag d,ResearchMethods m){return new ResearchInference(ResearchInference.goal(PillSnapshot.read(d.getCompound("PillResult"))),ResearchEvidence.history(p,m.known(p)));}
    @GameTest(template="empty") public static void foreignAndIncompleteHistoryCannotBecomeResearchEvidence(GameTestHelper h){try(var f=V5BatchGameTests.setup(h);var other=new GameTestPlayer(h.getLevel())){
        var d=terminal(f,100,4,true);var m=educate(f.player());boolean rejected=false;try{ResearchEvidence.read(other,d,m.known(f.player()));}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Other player's private journal imported");
        var old=d.copy();old.remove("ResearchStart");new BatchJournal(f.player().server).write(old);var history=ResearchEvidence.history(f.player(),m.known(f.player()));h.assertTrue(history.points().isEmpty()&&history.unavailable()==1,"Missing initial environment silently invented");
        h.assertTrue(!PillSnapshot.result(d).getTag().contains("ResearchStart"),"Private research metadata leaked to item");
    }h.succeed();}
    @GameTest(template="empty") public static void controlledComparisonRejectsConfoundingAndSelfComparison(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);var b=terminal(f,80,4,true);var c=terminal(f,60,3,true);var m=educate(f.player());var x=ResearchEvidence.read(f.player(),a,m.known(f.player()));var y=ResearchEvidence.read(f.player(),b,m.known(f.player()));var z=ResearchEvidence.read(f.player(),c,m.known(f.player()));
        h.assertTrue(ResearchEvidence.differences(x.plan(),y.plan()).equals(List.of("age/0"))&&ResearchEvidence.compare(x,y).contains("单因素"),"One age intervention not isolated");
        h.assertTrue(ResearchEvidence.compare(x,z).contains("不能作单因素")&&ResearchEvidence.compare(x,x).contains("同一批次"),"Confounded/self evidence accepted");
    }h.succeed();}
    @GameTest(template="empty") public static void empiricalSolverKeepsEquivalentRoutesAndContradictoryFailures(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);terminal(f,80,4,true);terminal(f,100,4,false);var m=educate(f.player());var s=solve(f.player(),a,m);
        h.assertTrue(s.candidates.size()==2&&s.profile.getBoolean("Ambiguous")&&s.profile.getInt("ConflictingRoutes")==1,"Equivalent route or failure discarded");
        h.assertTrue(s.candidates.stream().anyMatch(c->c.measurements.size()==2),"Same-route contradiction omitted");
        var node=ResearchLineage.save(f.player(),s,s.candidates.stream().filter(c->c.measurements.size()==2).findFirst().orElseThrow(),List.of(),m);
        h.assertTrue(node.getInt("Support")==1&&node.getInt("Failures")==1&&node.getString("Status").equals("CONFLICTING")&&!node.getBoolean("Certified"),"Cherry-picked reliability or certification invented");
    }h.succeed();}
    @GameTest(template="empty") public static void cumulativePathCannotBeWashedBySmallEditsOrReturningToRoot(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);var m=educate(f.player());var s=solve(f.player(),a,m);var c=s.candidate(0);var root=ResearchLineage.save(f.player(),s,c,List.of(),m);c.edit(true);var b=ResearchLineage.save(f.player(),s,c,List.of(root.getString("Id")),m);c.edit(true);var next=ResearchLineage.save(f.player(),s,c,List.of(b.getString("Id")),m);c.reset();var returned=ResearchLineage.save(f.player(),s,c,List.of(next.getString("Id")),m);
        h.assertTrue(!root.isEmpty()&&!returned.isEmpty()&&ResearchLineage.total(returned.getCompound("Net"),"age/")==0&&ResearchLineage.total(returned.getCompound("Path"),"age/")==72,"100→80→64→100 path washed away");
        h.assertTrue(returned.getString("Root").equals(root.getString("Id"))&&b.getInt("Support")==0&&next.getInt("Support")==0,"Off-domain children inherited evidence");
        var all=ResearchLineage.revisions(f.player());h.assertTrue(ResearchLineage.find(all,root.getString("Id")).equals(root),"Ancestor mutated during branching");
    }h.succeed();}
    @GameTest(template="empty") public static void jointObservationsDoNotCreateAnUntestedRectangle(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);terminal(f,80,3,true);var m=educate(f.player());var s=solve(f.player(),a,m);var c=s.candidates.stream().filter(v->v.source.plan().getCompound("Numbers").getLong("age/0")==100).findFirst().orElseThrow();c.broaden();var wide=ResearchLineage.save(f.player(),s,c,List.of(),m);
        h.assertTrue(wide.getCompound("JointPoints").size()==1&&wide.getInt("Support")==1,"Independent age/count extrema created a fake tested box");
        c.edit(true);var child=ResearchLineage.save(f.player(),s,c,List.of(wide.getString("Id")),m);h.assertTrue(child.getInt("Support")==0&&child.getCompound("JointPoints").isEmpty(),"80-year/4-item point inherited untested evidence");
    }h.succeed();}
    @GameTest(template="empty") public static void branchMergePreservesBothRootsWithoutDoubleCountingEvidence(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);terminal(f,80,3,true);var m=educate(f.player());var s=solve(f.player(),a,m);var x=s.candidates.get(0);var y=s.candidates.get(1);var r1=ResearchLineage.save(f.player(),s,x,List.of(),m);var r2=ResearchLineage.save(f.player(),s,y,List.of(),m);var merged=ResearchLineage.save(f.player(),s,x,List.of(r1.getString("Id"),r2.getString("Id")),m);
        h.assertTrue(merged.getList("Parents",Tag.TAG_STRING).size()==2&&merged.getList("Roots",Tag.TAG_STRING).size()==2&&merged.getInt("Support")==1,"Merge washed provenance or duplicated support");
        h.assertTrue(ResearchLineage.save(f.player(),s,x,List.of("forged-parent"),m).isEmpty(),"Unknown/imported parent accepted");
        var store=PlayerCrafts.get(f.player().server);h.assertTrue(PlayerCrafts.load(store.save(new CompoundTag())).profession(f.player().getUUID(),"alchemy").getList("ResearchRevisions",Tag.TAG_COMPOUND).equals(ResearchLineage.revisions(f.player())),"World reload changed DAG");
    }h.succeed();}
    private static SimulationRules physical(boolean narrow){var resources=List.of(new SimulationRules.Resource("herb",1,1,1,SimulationRules.Destination.ACTIVE),new SimulationRules.Resource("extract",1,1,1,SimulationRules.Destination.ACTIVE));var window=narrow?List.of(new SimulationRules.Window(SimulationRules.Measure.TEMPERATURE,"",5,5)):List.<SimulationRules.Window>of();return new SimulationRules("difficulty_fixture",resources,List.of(new SimulationRules.Reaction("known",0,SimulationRules.Phase.EXTRACTION,Map.of("herb",1L),Map.of("extract",1L),10,0,0,narrow?2:1,window)),new SimulationRules.Limits(4,100,30,64,1000,1000,10,20,100,10000,1000,0));}
    @GameTest(template="empty") public static void fourDifficultyCombinationsComeFromAmbiguityAndActualWindows(GameTestHelper h){
        for(boolean ambiguous:List.of(false,true))for(boolean narrow:List.of(false,true)){
            var research=ResearchDifficulty.inference(ambiguous?3:1,0,1,0);var operation=ResearchDifficulty.operation(physical(narrow),Set.of("known"),new int[]{3,6,3,3,3,3},10,10);
            h.assertTrue(research.getBoolean("Ambiguous")==ambiguous&&operation.getBoolean("Demanding")==narrow,"Independent difficulty quadrant collapsed");
        }
        var unknown=ResearchDifficulty.operation(physical(true),Set.of(),new int[]{3,6,3,3,3,3},10,10);h.assertTrue(!unknown.getBoolean("Complete")&&unknown.getInt("KnownWindows")==0&&unknown.getLong("TemperatureSpan")==-1,"Unknown rule window leaked");h.succeed();
    }
    @GameTest(template="empty") public static void unavailableStageHoldIsReportedWithoutFabricatedSuccessProbability(GameTestHelper h){var rules=physical(true);var profile=ResearchDifficulty.operation(rules,Set.of("known"),new int[]{3,1,3,3,3,3},10,10);h.assertTrue(profile.getInt("UnreachableRules")==1&&!profile.contains("SuccessProbability")&&!profile.contains("Grade"),"Impossible stage hidden behind score/grade");h.succeed();}
    @GameTest(template="empty") public static void metadataAndResearchDoNotChangePaidPhysicsOrCompileResults(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var d=V5Batch.prepare(f.furnace(),f.player());d.putLong("LastWorldTick",h.getLevel().getGameTime()-20);var legacy=d.copy();legacy.remove("ResearchStart");var old=V5Batch.advance(legacy,h.getLevel(),f.pos());var now=V5Batch.advance(d,h.getLevel(),f.pos());
        h.assertTrue(V5Batch.core(old).equals(V5Batch.core(now))&&old.getInt("FuelSpent")==now.getInt("FuelSpent"),"Research observation metadata changed physics");
        var a=terminal(f,100,4,true);var before=f.furnace().saveWithoutMetadata();var m=educate(f.player());var s=solve(f.player(),a,m);var c=s.candidate(0);c.edit(true);ResearchLineage.save(f.player(),s,c,List.of(),m);
        h.assertTrue(f.furnace().saveWithoutMetadata().equals(before)&&new BatchJournal(f.player().server).read(a.getUUID("Id")).equals(a),"Candidate edit changed physical furnace or authoritative pill");
    }h.succeed();}
    @GameTest(template="empty") public static void manualInterventionLogIsBoundedAndNeverConfusedWithAutomaticOutcomes(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var d=V5Batch.prepare(f.furnace(),f.player());var changed=BatchIncidents.control(d,246,100);var replay=BatchIncidents.control(changed,246,101);h.assertTrue(changed.getList("ResearchControls",Tag.TAG_COMPOUND).size()==1&&changed.equals(replay),"Duplicate command became a new intervention");
        var noHistory=d.copy();noHistory.putBoolean("ResearchControlsIncomplete",true);var sealed=V5Batch.seal(noHistory);new BatchJournal(f.player().server).write(sealed);boolean rejected=false;try{ResearchEvidence.read(f.player(),sealed,Set.of());}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Truncated controls treated as complete experiment");
        var budgets=ResearchDifficulty.stageBudgets(new int[]{3,6,3,3,3,3},List.of(new Operation(0,5,Action.ADVANCE,"",0),new Operation(1,13,Action.ADVANCE,"",0)),20);h.assertTrue(budgets[0]==5&&budgets[1]==8,"Observed warming extensions ignored when diagnosing phase feasibility");
    }h.succeed();}
    @GameTest(template="empty") public static void liveMenuRejectsStaleActionsAndChargesCandidateOnce(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var d=terminal(f,100,4,true);educate(f.player());var journal=new BatchJournal(f.player().server);h.assertTrue(BatchDelivery.claim(f.player(),d.getUUID("Id")),"Canonical sample delivery failed");
        var pos=f.pos().offset(3,0,0);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.ALCHEMY_RESEARCH_TABLE.get().defaultBlockState());var table=(cn.blockforge.generated.generatedmod.alchemy.research.ResearchTableEntity)h.getLevel().getBlockEntity(pos);f.player().setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);table.setItem(0,PillSnapshot.result(journal.read(d.getUUID("Id"))).copyWithCount(1));table.setItem(1,new ItemStack(Items.PAPER,2));table.setItem(2,new ItemStack(Items.INK_SAC,2));
        var menu=new cn.blockforge.generated.generatedmod.alchemy.research.ResearchTableMenu(3,f.player().getInventory(),table);h.assertTrue(menu.clickMenuButton(f.player(),10),"Inspection failed");menu.clickMenuButton(f.player(),2);menu.setData(54,34);menu.setData(62,3);
        h.assertTrue(!menu.clickMenuButton(f.player(),34),"Forged visible action/old card accepted");h.assertTrue(menu.clickMenuButton(f.player(),33)&&menu.clickMenuButton(f.player(),38),"Genuine inference/save failed");
        h.assertTrue(!menu.clickMenuButton(f.player(),38)&&table.getItem(1).getCount()==1&&table.getItem(2).getCount()==1&&ResearchLineage.revisions(f.player()).size()==1,"Replay charged or duplicated revision");
        menu.clickMenuButton(f.player(),21);h.assertTrue(menu.clickMenuButton(f.player(),34)&&menu.researchText(1).contains("域内支持：0"),"Edited hypothesis still displayed original route's evidence count");
        table.setItem(0,new ItemStack(Items.DIAMOND));menu.setData(1,1);h.assertTrue(!menu.clickMenuButton(f.player(),33),"Changed/forged sample retained observation rights");
        FormulaKnowledge.reset(f.player());menu.setData(4,1);h.assertTrue(!menu.clickMenuButton(f.player(),33)&&table.getItem(1).getCount()==1,"Client unlocked flag overrode real knowledge");h.getLevel().removeBlock(pos,false);
    }h.succeed();}
    @GameTest(template="empty") public static void splittingIdenticalHerbStacksDoesNotInventAnotherRoute(GameTestHelper h){try(var f=V5BatchGameTests.setup(h)){
        var a=terminal(f,100,4,true);var second=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),2);HerbAge.setYears(second,100);f.furnace().setItem(1,second);var b=terminal(f,100,2,true);var m=educate(f.player());var x=ResearchEvidence.read(f.player(),a,m.known(f.player()));var y=ResearchEvidence.read(f.player(),b,m.known(f.player()));
        h.assertTrue(x.plan().equals(y.plan())&&ResearchEvidence.compare(x,y).contains("同条件复现"),"Splitting one physical input changed experimental factors");
        var s=solve(f.player(),a,m);h.assertTrue(s.candidates.size()==1&&s.candidate(0).measurements.size()==2,"Slot arrangement created a fake equivalent route");
    }h.succeed();}
}
