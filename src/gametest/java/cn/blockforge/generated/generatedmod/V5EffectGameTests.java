package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import cn.blockforge.generated.generatedmod.alchemy.PillEffects;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.gametest.*;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class V5EffectGameTests {
    private static Snapshot finished(SimulationRules rules,String id,Map<String,Long> stocks){
        var s=AlchemySimulation.begin(rules,id,stocks,0,rules.weight(stocks,true),77,Map.of());
        return new Snapshot(s.batchId(),s.rulesHash(),0,SimulationRules.Phase.COOL,Status.FINISHED,"",s.stocks(),0,0,s.referenceMass(),s.seed(),s.environment(),s.draws(),s.holds(),s.lastSequence(),s.operations(),s.ledger());
    }
    static CompoundTag ready(V5BatchGameTests.Fixture f,Map<String,Long> stocks){
        var d=V5Batch.prepare(f.furnace(),f.player());var p=V5Batch.profile(d.getString("Profile"),d.getInt("Burn"));d.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(p.rules(),finished(p.rules(),d.getUUID("Id").toString(),stocks)));d=V5Batch.seal(d);var journal=new BatchJournal(f.player().server);journal.write(d);journal.link(f.player().serverLevel(),f.pos(),d.getUUID("Id"));f.furnace().restoreV5();return d;
    }
    private static AbilityCompiler.Result compiled(V5BatchGameTests.Fixture f,Map<String,Long> stocks){var d=V5Batch.prepare(f.furnace(),f.player());var rules=V5Batch.profile(d.getString("Profile"),1).rules();return AbilityCompiler.compile(new Policy(d.getString("EffectPolicy")),rules,finished(rules,"sample",stocks),0);}
    private static CompoundTag claim(V5BatchGameTests.Fixture f,Map<String,Long> stocks){var doc=ready(f,stocks);if(f.furnace().collectResult(f.player())!=1)throw new AssertionError("Result not claimable");f.player().getInventory().selected=0;f.player().setGameMode(net.minecraft.world.level.GameType.SURVIVAL);return new BatchJournal(f.player().server).read(doc.getUUID("Id"));}
    private static void eat(GameTestHelper h,V5BatchGameTests.Fixture f,Runnable after){
        f.player().join();f.player().keepTicking(h.getLevel(),f.pos());f.player().setNoGravity(true);f.player().getInventory().selected=0;f.player().getFoodData().setFoodLevel(10);f.player().getFoodData().setSaturation(0);
        h.runAfterDelay(25,()->{f.player().setHealth(5);var stack=f.player().getMainHandItem();h.assertTrue(stack.getItem().use(h.getLevel(),f.player(),InteractionHand.MAIN_HAND).getResult().consumesAction(),"Eating not accepted");h.runAfterDelay(18,after);});
    }
    @GameTest(template="empty") public static void nineBandsAndIndependentHardGates(GameTestHelper h){
        for(int grade=1;grade<=9;grade++){int score=grade*100000;h.assertTrue(AbilityCompiler.grade(score,1000000,1000000,1000000,0,1000000,true)==grade,"Wrong lower boundary "+grade);if(grade<9)h.assertTrue(AbilityCompiler.grade(score+99999,1000000,1000000,1000000,0,1000000,true)==grade,"Wrong half-open upper boundary");}
        h.assertTrue(AbilityCompiler.grade(99999,1000000,1000000,1000000,0,1000000,true)==0,"Failed score clamped to one");
        h.assertTrue(AbilityCompiler.grade(1000000,949999,1000000,1000000,0,1000000,true)==8,"Purity gate masked by score");
        h.assertTrue(AbilityCompiler.grade(1000000,1000000,949999,1000000,0,1000000,true)==8,"Stability gate masked by score");
        h.assertTrue(AbilityCompiler.grade(1000000,1000000,1000000,979999,0,1000000,true)==8,"Structure gate masked by score");
        h.assertTrue(AbilityCompiler.grade(1000000,1000000,1000000,1000000,20001,1000000,true)==8,"Toxicity gate masked by score");
        h.assertTrue(AbilityCompiler.grade(1000000,1000000,1000000,1000000,0,799999,true)==8,"Function gate masked by score");
        h.assertTrue(AbilityCompiler.grade(1000000,1000000,1000000,1000000,0,1000000,false)==0,"Unbalanced ledger succeeded");h.succeed();
    }
    @GameTest(template="empty") public static void scaleChangesYieldNotQualityAndRemaindersAreReal(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var one=compiled(f,Map.of("extract_mu",83L));var two=compiled(f,Map.of("extract_mu",166L));
            h.assertTrue(one.grade()==9&&one.quantity()==2&&two.quantity()==4&&one.effects().equals(two.effects())&&one.score()==two.score(),"Bulk created quality or free strength");
            h.assertTrue(one.boundMass()==80&&one.residualMass()==3&&two.residualMass()==6&&one.spent()<=one.budget(),"Rounded up output or overspent dose");
            var d=ready(f,Map.of("extract_mu",83L));var s=V5Batch.core(d);h.assertTrue(s.ledger().residualMass()==3&&s.stocks().get(AbilityCompiler.BOUND)==80&&s.ledger().lostPotential()==3,"Binding duplicated remainder");V5Batch.validate(d);
        }h.succeed();
    }
    @GameTest(template="empty") public static void moreWeakTagsCannotManufactureUtilityOrUnfundedModules(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var fire=compiled(f,Map.of("extract_huo",80L));h.assertTrue(fire.grade()==0&&fire.quantity()==0,"Material fire tag executed an uncompiled attack");
            var mixed=compiled(f,Map.of("extract_mu",40L,"extract_ling",40L,"extract_ziyang",40L));h.assertTrue(mixed.spent()<=mixed.budget()&&mixed.effects().stream().mapToLong(Spec::cost).sum()==mixed.spent(),"Shared source budget duplicated");
            var tiny=compiled(f,Map.of("extract_mu",39L));h.assertTrue(tiny.grade()==0&&tiny.boundMass()==0&&tiny.residualMass()==39,"Sub-dose mass rounded up");
        }h.succeed();
    }
    @GameTest(template="empty") public static void unknownModulesAndRuleRebindingRejectWithoutOverwrite(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var d=ready(f,Map.of("extract_mu",40L));var journal=new BatchJournal(h.getLevel().getServer());boolean rejected=false;
            try{new Policy(d.getString("EffectPolicy").replace("\"heal\"","\"run_script\""));}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Unknown module accepted");
            var changed=d.copy();changed.putString("EffectPolicy",changed.getString("EffectPolicy").replace("8000","9000"));rejected=false;try{journal.write(changed);}catch(RuntimeException expected){rejected=true;}
            h.assertTrue(rejected&&journal.read(d.getUUID("Id")).equals(d),"Pinned compiled result silently rerolled");
        }h.succeed();
    }
    @GameTest(template="empty") public static void publicStackHasOneGradeAxisAndNoPrivateSnapshot(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var d=ready(f,Map.of("extract_mu",40L));var item=f.furnace().completedResult();var tag=item.getTag();
            h.assertTrue(item.is(WuXingItems.V5_PILL.get())&&PillSnapshot.view(item).grade()==9&&cn.blockforge.generated.generatedmod.alchemy.PillData.read(item)==null,"Two grading systems stacked");
            h.assertTrue(!tag.contains("Grade")&&!tag.contains("MedicineTier")&&!tag.contains("Core")&&!tag.contains("EffectPolicy")&&!tag.contains("ReactionCore"),"Private snapshot or legacy grade leaked");
            h.assertTrue(!f.furnace().getUpdateTag().contains("ReactionCore")&&!f.furnace().getUpdateTag().contains("EffectPolicy"),"Private server data in block update");V5Batch.validate(d);
        }h.succeed();
    }
    @GameTest(template="empty") public static void forgedNbtAndFinishWithoutEatingCannotExecute(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var d=claim(f,Map.of("extract_mu",40L));f.player().setHealth(5);var item=f.player().getMainHandItem();
            item.getItem().finishUsingItem(item,h.getLevel(),f.player());h.assertTrue(f.player().getHealth()==5&&f.player().getMainHandItem().getCount()==1,"Direct finish granted free dose");
            var forged=item.copy();forged.getTag().getCompound(PillSnapshot.KEY).getList("Effects",Tag.TAG_COMPOUND).getCompound(0).putLong("MagnitudeMilli",24000);f.player().setItemInHand(InteractionHand.MAIN_HAND,forged);
            h.assertTrue(!PillRuntime.canUse(f.player(),forged)&&new BatchJournal(h.getLevel().getServer()).read(d.getUUID("Id")).getInt("DosesUsed")==0,"Forged public effect became authority");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=120) public static void realEatingConsumesOnceHealsAndCannotReplay(GameTestHelper h){
        var f=V5BatchGameTests.setup(h);f.player().cleanupAfter(h,f::close);var d=claim(f,Map.of("extract_mu",40L));f.player().setHealth(5);var item=f.player().getMainHandItem().copy();
        eat(h,f,()->{
            var result=PillSnapshot.read(d.getCompound("PillResult"));float expected=5+result.effects().get(0).magnitudeMilli()/1000F;
            h.assertTrue(Math.abs(f.player().getHealth()-expected)<.01&&f.player().getMainHandItem().isEmpty(),"Real eating did not heal/debit exact dose: "+f.player().getHealth());
            var journal=new BatchJournal(h.getLevel().getServer());h.assertTrue(journal.read(d.getUUID("Id")).getInt("DosesUsed")==1,"Dose not durably acknowledged");
            f.player().setItemInHand(InteractionHand.MAIN_HAND,item);h.assertTrue(!PillRuntime.canUse(f.player(),item),"Copied exhausted item granted another dose");item.getItem().finishUsingItem(item,h.getLevel(),f.player());h.assertTrue(Math.abs(f.player().getHealth()-expected)<.01,"Finish replay healed twice");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120) public static void savedDoseReceiptRecoversInterruptedAcknowledgement(GameTestHelper h){
        var f=V5BatchGameTests.setup(h);f.player().cleanupAfter(h,f::close);var d=claim(f,Map.of("extract_mu",40L));f.player().setHealth(5);
        eat(h,f,()->{
            try{
                var file=h.getLevel().getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("playerdata").resolve(f.player().getUUID()+".dat").toFile();var saved=NbtIo.readCompressed(file);var receipts=saved.getCompound("ForgeData").getCompound("V5DoseReceipts");h.assertTrue(receipts.size()==1,"No durable dose receipt");String key=receipts.getAllKeys().iterator().next();
                f.player().getPersistentData().put("V5DoseReceipts",receipts.copy());var journal=new BatchJournal(h.getLevel().getServer());var interrupted=journal.read(d.getUUID("Id"));interrupted.putInt("DosesUsed",0);var intent=new CompoundTag();intent.putUUID("Id",UUID.fromString(key));intent.putUUID("Recipient",f.player().getUUID());intent.putInt("Dose",1);intent.putInt("Slot",0);interrupted.put("DoseIntent",intent);journal.pendingDose(f.player().getUUID(),d.getUUID("Id"),true);journal.write(interrupted);
                float health=f.player().getHealth();PillRuntime.recover(f.player());PillRuntime.recover(f.player());h.assertTrue(f.player().getHealth()==health&&f.player().getMainHandItem().isEmpty()&&journal.read(d.getUUID("Id")).getInt("DosesUsed")==1&&journal.pendingDoses(f.player().getUUID()).isEmpty(),"Recovery duplicated dose or lost ack");h.succeed();
            }catch(java.io.IOException e){throw new AssertionError(e);}
        });
    }
    @GameTest(template="empty") public static void unAppliedDoseIntentRecoversFromActualInventoryExactlyOnce(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var d=claim(f,Map.of("extract_mu",40L));f.player().setHealth(5);var intent=new CompoundTag();intent.putUUID("Id",UUID.randomUUID());intent.putUUID("Recipient",f.player().getUUID());intent.putInt("Dose",1);intent.putInt("Slot",0);d.put("DoseIntent",intent);var journal=new BatchJournal(h.getLevel().getServer());journal.pendingDose(f.player().getUUID(),d.getUUID("Id"),true);journal.write(d);
            PillRuntime.recover(f.player());float health=f.player().getHealth();PillRuntime.recover(f.player());h.assertTrue(health>5&&f.player().getHealth()==health&&f.player().getMainHandItem().isEmpty()&&journal.read(d.getUUID("Id")).getInt("DosesUsed")==1,"Recovery of pre-apply intent not once");
        }h.succeed();
    }
    @GameTest(template="empty") public static void moduleQualificationIndependentOfNineGradeAndExistingShieldProtected(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            claim(f,Map.of("extract_jin",40L));var item=f.player().getMainHandItem();h.assertTrue(!PillRuntime.canUse(f.player(),item),"Shield ignored module realm gate");GeneratedMod.data(f.player()).wuxingxiantu$setRealm(1);h.assertTrue(PillRuntime.canUse(f.player(),item),"Eligible shield unusable");
            f.player().addEffect(new MobEffectInstance(MobEffects.ABSORPTION,400,2));float before=f.player().getAbsorptionAmount();h.assertTrue(!PillRuntime.canUse(f.player(),item)&&f.player().getAbsorptionAmount()==before,"Pill overwrote/refilled existing shield");
        }h.succeed();
    }
    @GameTest(template="empty") public static void finiteShieldOfflineExpiryAndExternalEffectOwnership(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var result=compiled(f,Map.of("extract_jin",40L));PillEffects.applyCompiledDose(f.player(),result);h.assertTrue(f.player().getAbsorptionAmount()>0,"Shield did not execute");
            var timed=f.player().getPersistentData().getCompound("V5TimedPill");timed.putLong("ShieldUntil",h.getLevel().getGameTime()-1);PillRuntime.reconcile(f.player(),true);h.assertTrue(!f.player().hasEffect(MobEffects.ABSORPTION)&&f.player().getAbsorptionAmount()==0,"Offline expired shield survived");
            PillEffects.applyCompiledDose(f.player(),result);f.player().addEffect(new MobEffectInstance(MobEffects.ABSORPTION,400,2));PillRuntime.reconcile(f.player(),true);h.assertTrue(f.player().getEffect(MobEffects.ABSORPTION).getAmplifier()==2,"Pill cleanup stripped another source's shield");
        }h.succeed();
    }
    @GameTest(template="empty") public static void speedIsTransientAndDeathDoesNotLeakModifier(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var r=compiled(f,Map.of("extract_feng",40L));PillEffects.applyCompiledDose(f.player(),r);h.assertTrue(f.player().getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PillRuntime.SPEED)!=null,"Speed module missing");
            PillRuntime.death(new net.minecraftforge.event.entity.living.LivingDeathEvent(f.player(),f.player().damageSources().generic()));h.assertTrue(f.player().getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PillRuntime.SPEED)==null&&!f.player().getPersistentData().contains("V5TimedPill"),"Death leaked temporary attribute");
        }h.succeed();
    }
    @GameTest(template="empty") public static void breakthroughCannotSkipExistingCultivationOrMajorRealm(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            claim(f,Map.of("extract_jingmai",40L));var p=f.player();var data=GeneratedMod.data(p);var item=p.getMainHandItem();h.assertTrue(!PillRuntime.canUse(p,item),"Breakthrough skipped cultivation requirement");data.wuxingxiantu$setCultivation(CultivationLogic.breakthroughRequirement(data));h.assertTrue(PillRuntime.canUse(p,item),"Existing minor-stage gate not reusable");data.wuxingxiantu$setCultivationStage(3);h.assertTrue(!PillRuntime.canUse(p,item)&&data.wuxingxiantu$getRealm()==0,"Nine-grade label bypassed major realm/trial gate");
        }h.succeed();
    }
    @GameTest(template="empty") public static void legacyPillsRemainLegacyAndNoAuthorityFromLooseV5Item(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var legacy=cn.blockforge.generated.generatedmod.alchemy.PillData.create(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.GOLDEN_WOUND,cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.LOW,cn.blockforge.generated.generatedmod.alchemy.PillData.Element.METAL,0);h.assertTrue(!PillSnapshot.isV5(legacy)&&cn.blockforge.generated.generatedmod.alchemy.PillData.read(legacy)!=null,"Old item was implicitly migrated");
            h.assertTrue(!PillRuntime.canUse(f.player(),new ItemStack(WuXingItems.V5_PILL.get())),"Loose creative item acquired unearned official effects");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=120) public static void cooldownIsSharedAcrossCopiesAndVanillaCooldownRemoval(GameTestHelper h){
        var f=V5BatchGameTests.setup(h);f.player().cleanupAfter(h,f::close);var d=claim(f,Map.of("extract_mu",80L));f.player().setHealth(5);
        eat(h,f,()->{var item=f.player().getMainHandItem();h.assertTrue(item.getCount()==1&&!PillRuntime.canUse(f.player(),item),"Split stack bypassed shared cooldown");f.player().getCooldowns().removeCooldown(item.getItem());h.assertTrue(!PillRuntime.canUse(f.player(),item),"Legacy cooldown cleanup bypassed V5 cooldown");f.player().getPersistentData().putLong("V5PillCooldownUntil",h.getLevel().getGameTime()-1);h.assertTrue(PillRuntime.canUse(f.player(),item)&&new BatchJournal(h.getLevel().getServer()).read(d.getUUID("Id")).getInt("DosesUsed")==1,"Cooldown expiry consumed or duplicated another dose");h.succeed();});
    }
    @GameTest(template="empty") public static void poisonCannotFundAdditionalBenefitOrRaiseQuality(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var clean=compiled(f,Map.of("extract_mu",80L));var toxic=compiled(f,Map.of("extract_mu",40L,"extract_du",40L));
            h.assertTrue(toxic.grade()>0&&toxic.toxicity()>0&&toxic.budget()<clean.budget()&&toxic.potency()<clean.potency()&&toxic.score()<clean.score(),"Toxin credited as useful potential");
            PillEffects.applyCompiledDose(f.player(),toxic);h.assertTrue(f.player().hasEffect(MobEffects.POISON),"Declared side effect did not execute");
            h.assertTrue(compiled(f,Map.of("extract_du",80L)).grade()==0,"Pure toxin became a successful pill");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=120) public static void regenerationPulsesAreFiniteAndOfflineDoesNotCatchUp(GameTestHelper h){
        var f=V5BatchGameTests.setup(h);f.player().cleanupAfter(h,f::close);f.player().join();f.player().keepTicking(h.getLevel(),f.pos());f.player().setHealth(5);f.player().getFoodData().setFoodLevel(10);f.player().getFoodData().setSaturation(0);
        var r=compiled(f,Map.of("extract_ziyang",40L));long magnitude=r.effects().get(0).magnitudeMilli();
        h.runAfterDelay(25,()->{f.player().setHealth(5);PillEffects.applyCompiledDose(f.player(),r);h.runAfterDelay(21,()->{h.assertTrue(Math.abs(f.player().getHealth()-(5+magnitude/1000F))<.01,"Regeneration pulse timing/amount wrong");var t=f.player().getPersistentData().getCompound("V5TimedPill");t.putLong("RegenUntil",h.getLevel().getGameTime()-1);t.putLong("NextPulse",0);float before=f.player().getHealth();PillRuntime.reconcile(f.player(),true);PillRuntime.reconcile(f.player(),false);h.assertTrue(f.player().getHealth()==before&&!t.contains("RegenMilli"),"Offline catch-up created unbudgeted healing");h.succeed();});});
    }

    @GameTest(template="empty") public static void registeredSourcesSaturateAndGradeDoesNotMultiplyExecution(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h)){
            var a=compiled(f,Map.of("extract_mu",40L,"extract_huo",40L));var b=compiled(f,Map.of("extract_mu",60L,"extract_huo",20L));var c=compiled(f,Map.of("extract_mu",80L));
            long x=a.effects().get(0).magnitudeMilli(),y=b.effects().get(0).magnitudeMilli(),z=c.effects().get(0).magnitudeMilli();h.assertTrue(x<y&&y<z&&z-y<y-x&&a.spent()<=20,"Saturation or actual source budget violated");
            var low=new AbilityCompiler.Result(1,c.quantity(),c.boundMass(),c.residualMass(),c.budget(),c.spent(),c.purity(),c.stability(),c.structure(),c.toxicity(),c.potency(),c.score(),c.policyHash(),c.effects());
            f.player().setHealth(5);PillEffects.applyCompiledDose(f.player(),c);float high=f.player().getHealth();f.player().setHealth(5);PillEffects.applyCompiledDose(f.player(),low);h.assertTrue(f.player().getHealth()==high,"Nine-grade label multiplied an already compiled effect");
        }h.succeed();
    }
    @GameTest(template="empty") public static void tradedDoseUsesSameGlobalBudgetAndCanBeConsumedByHolder(GameTestHelper h){
        try(var f=V5BatchGameTests.setup(h);var holder=new GameTestPlayer(h.getLevel())){
            var d=claim(f,Map.of("extract_mu",40L));var traded=f.player().getMainHandItem().copy();f.player().setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);holder.setItemInHand(InteractionHand.MAIN_HAND,traded);holder.setHealth(5);h.assertTrue(PillRuntime.canUse(holder,traded),"Trading incorrectly bound effects to original furnace owner");
            var intent=new CompoundTag();intent.putUUID("Id",UUID.randomUUID());intent.putUUID("Recipient",holder.getUUID());intent.putInt("Dose",1);intent.putInt("Slot",0);d.put("DoseIntent",intent);var journal=new BatchJournal(h.getLevel().getServer());journal.pendingDose(holder.getUUID(),d.getUUID("Id"),true);journal.write(d);PillRuntime.recover(holder);
            h.assertTrue(holder.getHealth()>5&&holder.getMainHandItem().isEmpty()&&journal.read(d.getUUID("Id")).getInt("DosesUsed")==1,"Traded dose escaped batch budget");f.player().setHealth(5);f.player().setItemInHand(InteractionHand.MAIN_HAND,traded.copy());h.assertTrue(!PillRuntime.canUse(f.player(),traded),"Old owner's copy spent an exhausted traded dose");
        }h.succeed();
    }

}
