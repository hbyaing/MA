package cn.blockforge.generated.generatedmod;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
@GameTestHolder("wuxingxiantu_tribulation")
@PrefixGameTestTemplate(false)
public final class TribulationIntegrationTests {
 @GameTest(template="empty",timeoutTicks=80) public static void paralysisFallsAndLandsOnRealTerrain(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(2,2,2));var level=h.getLevel();
  for(var q:BlockPos.betweenClosed(pos.offset(-2,-1,-2),pos.offset(2,-1,2)))level.setBlock(q,Blocks.STONE.defaultBlockState(),3);
  var mob=new net.minecraft.world.entity.monster.Zombie(level);mob.setPos(pos.getX()+.5,pos.getY()+6,pos.getZ()+.5);mob.setNoGravity(true);level.addFreshEntity(mob);
  double start=mob.getY();ParalysisControl.apply(mob,200);
  h.runAfterDelay(10,()->{h.assertTrue(mob.getY()<start-1&&!mob.isNoGravity(),"paralysis allows actual gravity-driven falling");h.assertTrue(Math.abs(mob.getX()-pos.getX()-.5)<.01&&Math.abs(mob.getZ()-pos.getZ()-.5)<.01,"falling cannot steer horizontally");});
  h.runAfterDelay(35,()->{h.assertTrue(mob.onGround()&&Math.abs(mob.getY()-pos.getY())<.05,"paralyzed entity lands on collision surface");mob.discard();h.succeed();});
 }
 @GameTest(template="empty") public static void finalFieldDamagesForThirtyTicksAndPersists(GameTestHelper h){
  var p=player(h);TribulationManager.startTest(p,0,true);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;trial.strikesDone=trial.totalStrikes()-1;
  float before=p.getHealth(),budget=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.damage(0,trial.totalStrikes());
  TribulationManager.tick(p.server);h.assertTrue(trial.finalTicks==29&&p.getHealth()<before,"final begins with a partial damage pulse");
  h.assertTrue(before-p.getHealth()<budget/2&&p.getEffect(ModEffects.PARALYSIS.get()).getDuration()>=30,"final locks movement without frontloading damage");
  var restored=TribulationState.Trial.load(trial.save());h.assertTrue(restored.finalTicks==29,"save/load preserves the consumed pulse");TribulationState.get(p.server).trials.put(p.getUUID(),restored);
  for(int t=1;t<30;t++)TribulationManager.tick(p.server);
  h.assertTrue(restored.finalTicks==0&&!restored.settled(),"final field lasts exactly thirty ticks");
  h.assertTrue(Math.abs(before-p.getHealth()-budget)<.03,"six pulses preserve the final damage budget");
  TribulationManager.tick(p.server);h.assertTrue(restored.settled(),"settlement follows the sustained field");h.succeed();
 }
 @GameTest(template="empty") public static void lateImpactBreaksStoneBeyondEarlyPit(GameTestHelper h) throws Exception {
  var p=player(h);var center=p.blockPosition();var level=h.getLevel();
  for(var q:BlockPos.betweenClosed(center.offset(-4,-5,-4),center.offset(4,-1,4)))level.setBlock(q,Blocks.DEEPSLATE.defaultBlockState(),3);
  level.setBlock(center.below(),Blocks.STONE.defaultBlockState(),3);
  TribulationManager.startTest(p,3,true);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.strikesDone=1;
  var destroy=TribulationManager.class.getDeclaredMethod("destroyStrikeBlocks",net.minecraft.server.level.ServerLevel.class,BlockPos.class,TribulationState.Trial.class);destroy.setAccessible(true);destroy.invoke(null,level,center,trial);
  h.assertTrue(!level.getBlockState(center.below()).is(Blocks.STONE)&&level.getBlockState(center.below(2)).is(Blocks.DEEPSLATE),"early strike breaks stone but leaves harder deep layers");
  trial.strikesDone=trial.totalStrikes();destroy.invoke(null,level,center,trial);
  h.assertTrue(!level.getBlockState(center.below(2)).is(Blocks.DEEPSLATE)&&!level.getBlockState(center.offset(2,-1,0)).is(Blocks.DEEPSLATE),"late strike reaches deepslate, deeper layers and a wider area");TribulationManager.stopTest(p);h.succeed();
 }
 @GameTest(template="empty") public static void lateStrikeDigsDeeperAndIgnitesRing(GameTestHelper h) throws Exception {
  var p=player(h);var center=p.blockPosition();var level=h.getLevel();
  for(var q:BlockPos.betweenClosed(center.offset(-4,-4,-4),center.offset(4,-1,4)))level.setBlock(q,Blocks.DIRT.defaultBlockState(),3);
  TribulationManager.startTest(p,0,true);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.strikesDone=trial.totalStrikes();
  var destroy=TribulationManager.class.getDeclaredMethod("destroyStrikeBlocks",net.minecraft.server.level.ServerLevel.class,BlockPos.class,TribulationState.Trial.class);
  destroy.setAccessible(true);destroy.invoke(null,level,center,trial);
  h.assertTrue(level.getBlockState(center.below(2)).isAir(),"last strike reaches two blocks below impact");
  h.assertTrue(level.getBlockState(center.below(3)).is(Blocks.DIRT),"depth stops at realm limit");
  boolean fire=false;for(var q:BlockPos.betweenClosed(center.offset(-4,-3,-4),center.offset(4,2,4)))if(level.getBlockState(q).is(Blocks.FIRE))fire=true;
  h.assertTrue(fire,"grounded real strike ignites surrounding surfaces");TribulationManager.stopTest(p);h.succeed();
 }
 @GameTest(template="empty") public static void paralysisLocksAndReleasesMovement(GameTestHelper h){
  var p=player(h);var origin=p.position();
  p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.PARALYSIS.get(),100));
  ParalysisControl.player(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
  p.setPos(origin.add(3,-2,0));p.setDeltaMovement(1,-.42,1);
  ParalysisControl.player(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
  h.assertTrue(Math.abs(p.getX()-origin.x)<.001&&Math.abs(p.getZ()-origin.z)<.001&&Math.abs(p.getY()-origin.y+2)<.001&&Math.abs(p.getDeltaMovement().y+.42)<.001&&p.getDeltaMovement().horizontalDistanceSqr()==0,"paralysis locks horizontal movement and preserves falling");
  p.removeEffect(ModEffects.PARALYSIS.get());
  ParalysisControl.player(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
  p.setPos(origin.add(1,0,0));
  ParalysisControl.player(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
  h.assertTrue(p.position().distanceToSqr(origin)>.5,"movement resumes after removal");
  h.assertTrue(h.getLevel().damageSources().lightningBolt().is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING),"tribulation source carries the lightning tag");h.succeed();
 }
 @GameTest(template="empty") public static void soulProjectionLeavesBodyAndCleansUp(GameTestHelper h) throws Exception {
  var p=player(h);var origin=p.position();var data=GeneratedMod.data(p);
  data.wuxingxiantu$setActiveDivineArt("yuan_shen_chu_qiao");
  var start=DivineArtLogic.class.getDeclaredMethod("startSoul",net.minecraft.server.level.ServerPlayer.class,CultivationData.class,boolean.class);
  start.setAccessible(true);start.invoke(null,p,data,true);
  h.assertTrue(p.isSpectator(),"departing soul enters spectator mode");
  h.assertTrue(p.getPersistentData().hasUUID("wuxingxiantu_soul_body"),"projection registers a physical body");
  var body=h.getLevel().getEntity(p.getPersistentData().getUUID("wuxingxiantu_soul_body"));
  h.assertTrue(body instanceof SoulBodyEntity && body.position().distanceToSqr(origin)<.001,"body remains at launch position");
  p.setPos(origin.add(4,2,0));
  h.assertTrue(body.position().distanceToSqr(origin)<.001,"body does not follow the soul");
  DivineArtLogic.stop(p,data,true);
  h.assertTrue(body.isRemoved()&&!p.getPersistentData().hasUUID("wuxingxiantu_soul_body"),"return removes the shell and its ownership");
  h.assertTrue(p.position().distanceToSqr(origin)<.001,"soul returns to the physical body position");h.succeed();
 }
 @GameTest(template="empty") public static void airborneStrikeHasNoGroundImpactButStillDamages(GameTestHelper h){
  var p=player(h);var level=h.getLevel();var ground=p.blockPosition().below();level.setBlock(ground,Blocks.DIRT.defaultBlockState(),3);
  h.assertTrue(TribulationManager.impactGround(p)!=null,"standing dirt provides a real impact surface");
  p.setPos(p.getX(),p.getY()+8,p.getZ());p.setOnGround(true);
  h.assertTrue(TribulationManager.impactGround(p)==null,"stale onGround cannot create a floating impact");
  TribulationManager.startTest(p,0,true);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;
  float hp=p.getHealth();TribulationManager.tick(p.server);
  h.assertTrue(p.getHealth()<hp,"an airborne player still receives tribulation damage");
  h.assertTrue(level.getBlockState(ground).is(Blocks.DIRT),"airborne hit does not destroy distant terrain");
  TribulationManager.stopTest(p);h.succeed();
 }
 @GameTest(template="empty") public static void flightSweepFastPathKeepsWallsAndUnloadedChunksSafe(GameTestHelper h){
  var p=player(h);var pos=h.absolutePos(new BlockPos(2,12,2));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
  var box=p.getBoundingBox();var velocity=new net.minecraft.world.phys.Vec3(3,0,0);
  var clear=cn.blockforge.generated.generatedmod.yujian.FlightCollision.sweep(p,box,velocity,false);
  h.assertTrue(!clear.blocked()&&clear.movement().equals(velocity),"empty sky accepts the full displacement");
  var wall=pos.offset(2,0,0);h.getLevel().setBlock(wall,Blocks.STONE.defaultBlockState(),3);h.getLevel().setBlock(wall.above(),Blocks.STONE.defaultBlockState(),3);
  var blocked=cn.blockforge.generated.generatedmod.yujian.FlightCollision.sweep(p,box,velocity,false);
  h.assertTrue(blocked.blocked()&&blocked.movement().x>0&&blocked.movement().x<3,"obstacle retains swept collision prefix");
  var phased=cn.blockforge.generated.generatedmod.yujian.FlightCollision.sweep(p,box,velocity,true);
  h.assertTrue(!phased.blocked()&&phased.movement().equals(velocity),"Dunxu still crosses allowed stone");
  var unloaded=cn.blockforge.generated.generatedmod.yujian.FlightCollision.sweep(p,box,new net.minecraft.world.phys.Vec3(100000,0,0),false);
  h.assertTrue(unloaded.blocked()&&!unloaded.loaded(),"unloaded route is rejected without forcing chunk creation");h.succeed();
 }
 @GameTest(template="empty") public static void commandDefaultsToRealAndPreviewIsExplicit(GameTestHelper h){
  var p=player(h);var source=p.createCommandSourceStack().withPermission(2);
  p.server.getCommands().performPrefixedCommand(source,"tribulationtest 1");
  var trial=TribulationState.get(p.server).trials.get(p.getUUID());
  h.assertTrue(trial!=null&&!trial.harmless,"ordinary command starts a real trial");
  TribulationManager.stopTest(p);
  p.server.getCommands().performPrefixedCommand(source,"tribulationtest 1 预览");
  trial=TribulationState.get(p.server).trials.get(p.getUUID());
  h.assertTrue(trial!=null&&trial.harmless,"explicit preview remains harmless");
  TribulationManager.stopTest(p);h.succeed();
 }
 @GameTest(template="empty") public static void lightningAppliesScaledParalysisAndPreviewDoesNot(GameTestHelper h){var p=player(h);TribulationManager.startTest(p,0,true);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;TribulationManager.tick(p.server);h.assertTrue(p.hasEffect(ModEffects.PARALYSIS.get()),"real lightning applies paralysis");h.assertTrue(p.getEffect(ModEffects.PARALYSIS.get()).getDuration()==ParalysisControl.duration(0,0),"first strike duration matches strength");TribulationManager.stopTest(p);p.removeEffect(ModEffects.PARALYSIS.get());TribulationManager.startTest(p,7,false);trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;TribulationManager.tick(p.server);h.assertTrue(!p.hasEffect(ModEffects.PARALYSIS.get()),"visual preview cannot paralyze");TribulationManager.stopTest(p);h.succeed();}
 @GameTest(template="empty") public static void paralysisInterruptsAndRefreshesWithoutStacking(GameTestHelper h){var p=player(h);var data=GeneratedMod.data(p);data.wuxingxiantu$setActiveDivineArt("fa_tian_xiang_di");p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.FA_TIAN_XIANG_DI.get(),100));p.getAbilities().flying=true;ParalysisControl.apply(p,30);h.assertTrue(data.wuxingxiantu$getActiveDivineArt().isBlank()&&!p.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())&&!p.getAbilities().flying,"paralysis interrupts giant and flight");ParalysisControl.apply(p,10);h.assertTrue(p.getEffect(ModEffects.PARALYSIS.get()).getDuration()==30,"weaker refresh cannot shorten duration");ParalysisControl.apply(p,40);h.assertTrue(p.getEffect(ModEffects.PARALYSIS.get()).getDuration()==40,"refresh replaces duration without adding");var event=new net.minecraftforge.event.entity.player.AttackEntityEvent(p,p);ParalysisControl.attack(event);h.assertTrue(event.isCanceled(),"attack is rejected on server");for(int realm=0;realm<8;realm++){h.assertTrue(ParalysisControl.duration(realm,1)>ParalysisControl.duration(realm,0),"later strikes paralyze longer");if(realm>0)h.assertTrue(ParalysisControl.duration(realm,1)>ParalysisControl.duration(realm-1,1),"higher realm duration increases");}p.removeEffect(ModEffects.PARALYSIS.get());h.succeed();}
 @GameTest(template="empty") public static void stormFadesInBeforeCloudWarnings(GameTestHelper h){var p=player(h);var level=h.getLevel();level.setRainLevel(0);level.setThunderLevel(0);TribulationManager.startTest(p,0,false);var trial=TribulationState.get(p.server).trials.get(p.getUUID());h.assertTrue(level.getRainLevel(1)==0&&level.getThunderLevel(1)==0,"trial starts without an instant storm");for(int t=0;t<30;t++)TribulationManager.tick(p.server);h.assertTrue(Math.abs(level.getRainLevel(1)-.5f)<.02&&Math.abs(level.getThunderLevel(1)-.25f)<.02,"storm is halfway after 1.5 seconds");h.assertTrue(trial.skyDelay==30,"warning wait survives the weather transition");var restored=TribulationState.Trial.load(trial.save());h.assertTrue(restored.skyDelay==trial.skyDelay,"warning wait persists across save/load");for(int t=0;t<30;t++)TribulationManager.tick(p.server);h.assertTrue(level.getRainLevel(1)==1&&level.getThunderLevel(1)==1&&trial.skyDelay==0,"full storm after three seconds, before the first warning");TribulationManager.tick(p.server);h.assertTrue(trial.skyDelay==35,"first complete warning group starts only after the storm");for(int t=0;t<35;t++)TribulationManager.tick(p.server);h.assertTrue(trial.skyDelay==0,"a complete warning and gap precede the next group");TribulationManager.stopTest(p);h.assertTrue(level.getRainLevel(1)==0&&!level.isRaining(),"stop still clears the weather");h.succeed();}
 private static GameTestPlayer player(GameTestHelper h){var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(new BlockPos(2,2,2));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(0);data.wuxingxiantu$setCultivationStage(3);cn.blockforge.generated.generatedmod.items.CultivationLogic.applyHealthBonus(p,data);p.setHealth(p.getMaxHealth());p.cleanupAfter(h,()->TribulationManager.stopTest(p));return p;}
 @GameTest(template="empty") public static void realmDamageCraterRestoreAndSun(GameTestHelper h){
  var p=player(h);var level=h.getLevel();BlockPos ground=p.blockPosition().below();level.setBlock(ground,Blocks.DIRT.defaultBlockState(),3);
  h.assertTrue(TribulationManager.startTest(p,0,true),"start harmful fixture");var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;
  float before=p.getHealth();TribulationManager.tick(p.server);
  h.assertTrue(Math.abs(before-p.getHealth()-2.5)<.02,"realm base damage is 2.5 HP");h.assertTrue(level.isEmptyBlock(ground),"actual crater removed dirt");h.assertTrue(level.isRaining()&&level.isThundering(),"active test owns storm");
  var journal=TribulationState.get(p.server);var entry=journal.craters.get(new TribulationState.CraterKey(level.dimension(),ground));h.assertTrue(entry!=null,"original block journaled");journal.retry(entry,p.server.overworld().getGameTime());TribulationManager.tick(p.server);h.assertTrue(level.getBlockState(ground).is(Blocks.DIRT),"deadline restored dirt");
  TribulationManager.stopTest(p);h.assertTrue(!level.isRaining()&&!level.isThundering(),"stop turns weather clear");h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getRealm()==0,"test did not advance realm");h.succeed();
 }
 @GameTest(template="empty") public static void previewNeverHurtsOrBreaks(GameTestHelper h){var p=player(h);var level=h.getLevel();var ground=p.blockPosition().below();level.setBlock(ground,Blocks.DIRT.defaultBlockState(),3);TribulationManager.startTest(p,7,false);var trial=TribulationState.get(p.server).trials.get(p.getUUID());trial.countdownTicks=0;float hp=p.getHealth();TribulationManager.tick(p.server);h.assertTrue(p.getHealth()==hp,"preview never damages");h.assertTrue(level.getBlockState(ground).is(Blocks.DIRT),"preview never breaks blocks");TribulationManager.stopTest(p);h.succeed();}
}
