package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.event.SpiritShieldImpactEvents;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.gametest.*;
@GameTestHolder("wuxingxiantu_shield")
@PrefixGameTestTemplate(false)
public final class SpiritShieldGameTests {
 private static void activate(GameTestPlayer p,float pool){p.addEffect(new MobEffectInstance(ModEffects.SPIRIT_SHIELD_VISUAL.get(),400));p.setAbsorptionAmount(pool);}
 @GameTest(template="empty") public static void meleeConsumesShield(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var z=EntityType.ZOMBIE.create(h.getLevel());activate(p,8);float hp=p.getHealth();
   p.hurt(p.damageSources().mobAttack(z),3);
   h.assertTrue(p.getHealth()==hp,"Melee leaked through active shield");
   h.assertTrue(Math.abs(p.getAbsorptionAmount()-5)<.001,"Melee must charge shield once: pool="+p.getAbsorptionAmount());h.succeed();
  }
 }
 @GameTest(template="empty") public static void excessAndExhaustedDamage(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var z=EntityType.ZOMBIE.create(h.getLevel());activate(p,2);float hp=p.getHealth();
   p.hurt(p.damageSources().mobAttack(z),5);
   h.assertTrue(Math.abs(p.getHealth()-(hp-3))<.001,"Excess melee damage lost or charged twice: HP="+p.getHealth()+" pool="+p.getAbsorptionAmount());
   h.assertTrue(p.getAbsorptionAmount()==0&&!SpiritShieldImpactEvents.active(p),"Exhausted shield still active");
   h.assertTrue(p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),"Exhausting strike broke the shell too early");
   p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(z),2);
   h.assertTrue(Math.abs(p.getHealth()-(hp-5))<.001,"Exhausted shield grants immunity");
   h.assertTrue(!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),"Next strike did not break the depleted shell");h.succeed();
  }
 }

 @GameTest(template="empty") public static void depletedShellBreaksOnceAndCanRecast(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var z=EntityType.ZOMBIE.create(h.getLevel());activate(p,2);float hp=p.getHealth();
   p.hurt(p.damageSources().mobAttack(z),2);
   h.assertTrue(p.getAbsorptionAmount()==0&&p.getHealth()==hp&&p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Pool depletion must retain the visual shell without leaking damage");
   p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(z),2);
   h.assertTrue(p.getHealth()==hp-2&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Next hit must break the depleted shell and follow ordinary damage");
   p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(z),2);
   h.assertTrue(p.getHealth()==hp-4&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Further damage must not restore or intercept with the already broken shell");
   activate(p,6);p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(z),2);
   h.assertTrue(p.getAbsorptionAmount()==4&&p.getHealth()==hp-4&&p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Recast shield must reset its pool and protect a new hit");h.succeed();
  }
 }

 @GameTest(template="empty") public static void creativeArrowDepletesThenBreaksOnNextContact(GameTestHelper h){
  try(var p=new GameTestPlayer(h.getLevel())){
   p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);activate(p,4);float hp=p.getHealth();
   var first=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),0,0,0);
   first.setBaseDamage(2);first.setDeltaMovement(0,0,2);
   var firstEvent=new net.minecraftforge.event.entity.ProjectileImpactEvent(first,new net.minecraft.world.phys.EntityHitResult(p));
   SpiritShieldImpactEvents.projectile(firstEvent);
   h.assertTrue(firstEvent.isCanceled()&&first.isRemoved()&&p.getAbsorptionAmount()==0
     &&p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),"Exhausting creative arrow must retain the shell");
   var next=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),0,0,0);next.setDeltaMovement(0,0,2);
   var nextEvent=new net.minecraftforge.event.entity.ProjectileImpactEvent(next,new net.minecraft.world.phys.EntityHitResult(p));
   SpiritShieldImpactEvents.projectile(nextEvent);
   h.assertTrue(!nextEvent.isCanceled()&&!next.isRemoved()&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get())
     &&p.getAbsorptionAmount()==0&&p.getHealth()==hp,"Next creative contact must break once and retain vanilla handling");
   SpiritShieldImpactEvents.projectile(nextEvent);
   h.assertTrue(!nextEvent.isCanceled()&&!next.isRemoved()&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Repeat contact must not re-intercept the shattered shell");next.discard();h.succeed();
  }
 }

 @GameTest(template="empty") public static void exhaustingArrowExcessDoesNotBreakItsOwnShell(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   activate(p,2);float hp=p.getHealth();
   var first=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),0,0,0);
   first.setBaseDamage(2);first.setDeltaMovement(0,0,2);
   var event=new net.minecraftforge.event.entity.ProjectileImpactEvent(first,new net.minecraft.world.phys.EntityHitResult(p));
   SpiritShieldImpactEvents.projectile(event);
   h.assertTrue(event.isCanceled()&&first.isRemoved()&&p.getAbsorptionAmount()==0&&p.getHealth()==hp-2
    &&p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),"One exhausting arrow's excess hurt event broke its own shell");
   var next=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),0,0,0);next.setDeltaMovement(0,0,2);
   var nextEvent=new net.minecraftforge.event.entity.ProjectileImpactEvent(next,new net.minecraft.world.phys.EntityHitResult(p));
   SpiritShieldImpactEvents.projectile(nextEvent);
   h.assertTrue(!nextEvent.isCanceled()&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
    "Following distinct arrow must break the shell");next.discard();h.succeed();
  }
 }

 @GameTest(template="empty") public static void hostileDepletionAndMeleeAreOneInterception(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var pos=h.absolutePos(new BlockPos(3,3,3));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
   var z=EntityType.ZOMBIE.create(h.getLevel());z.setNoAi(true);z.setTarget(p);z.setPos(p.getX()+.1,p.getY(),p.getZ());h.getLevel().addFreshEntity(z);
   try{
    float damage=(float)z.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);activate(p,damage);float hp=p.getHealth();
    SpiritShieldImpactEvents.repel(p);p.hurt(p.damageSources().mobAttack(z),damage);
    h.assertTrue(p.getAbsorptionAmount()==0&&p.getHealth()==hp&&p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
      "Exhausting collision plus its melee event must leave one depleted shell");
    p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(z),1);
    h.assertTrue(p.getHealth()==hp-1&&!p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()),
      "A later melee hit must break the depleted contact shell");h.succeed();
   }finally{z.discard();}
  }
 }
 @GameTest(template="empty") public static void overlappingMobSeparated(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var pos=h.absolutePos(new BlockPos(3,3,3));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);activate(p,8);
   var z=EntityType.ZOMBIE.create(h.getLevel());z.setNoAi(true);z.setPos(p.getX()+.1,p.getY(),p.getZ());h.getLevel().addFreshEntity(z);
   try{
    for(int i=0;i<8;i++)SpiritShieldImpactEvents.repel(p);
    double d=z.position().subtract(p.position()).multiply(1,0,1).length();
    h.assertTrue(d>=1.15+z.getBbWidth()*.5,"Overlapping mob not separated");
    h.assertTrue(z.getDeltaMovement().x>0,"Mob not pushed outward");
    p.setAbsorptionAmount(0);z.setPos(p.getX()+.1,p.getY(),p.getZ());SpiritShieldImpactEvents.repel(p);
    h.assertTrue(z.distanceToSqr(p)<.02,"Exhausted shield still repels");h.succeed();
   }finally{z.discard();}
  }
 }

 @GameTest(template="empty") public static void creativeArrowCostsShield(GameTestHelper h){
  try(var p=new GameTestPlayer(h.getLevel())){
   p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);activate(p,8);float hp=p.getHealth();
   var arrow=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),0,0,0);
   arrow.setBaseDamage(2);arrow.setDeltaMovement(0,0,2);
   var event=new net.minecraftforge.event.entity.ProjectileImpactEvent(arrow,new net.minecraft.world.phys.EntityHitResult(p));
   SpiritShieldImpactEvents.projectile(event);
   h.assertTrue(event.isCanceled()&&arrow.isRemoved(),"Arrow was not intercepted");
   h.assertTrue(p.getAbsorptionAmount()==4&&p.getHealth()==hp,"Creative interception did not consume exactly four shield points");h.succeed();
  }
 }
 @GameTest(template="empty") public static void hostileContactConsumesOnce(GameTestHelper h){
  try(var p=new GameTestPlayer(h.getLevel())){
   var pos=h.absolutePos(new BlockPos(3,3,3));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);activate(p,8);
   var z=EntityType.ZOMBIE.create(h.getLevel());z.setNoAi(true);z.setTarget(p);z.setPos(p.getX()+.1,p.getY(),p.getZ());h.getLevel().addFreshEntity(z);
   try{float damage=(float)z.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
    SpiritShieldImpactEvents.repel(p);h.assertTrue(p.getAbsorptionAmount()==8-damage,"Hostile contact did not charge shield");
    z.setPos(p.getX()+.1,p.getY(),p.getZ());SpiritShieldImpactEvents.repel(p);
    h.assertTrue(p.getAbsorptionAmount()==8-damage,"Same contact repeatedly drained shield");
    h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
    float hp=p.getHealth();p.hurt(p.damageSources().mobAttack(z),damage);
    h.assertTrue(p.getAbsorptionAmount()==8-damage&&p.getHealth()==hp,"Contact and melee charged the same interception twice");h.succeed();
   }finally{z.discard();}
  }
 }

 @GameTest(template="empty") public static void strongerContactAttackUsesRemainingPool(GameTestHelper h){
  h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  try(var p=new GameTestPlayer(h.getLevel())){
   var pos=h.absolutePos(new BlockPos(3,3,3));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);activate(p,8);
   var z=EntityType.ZOMBIE.create(h.getLevel());z.setNoAi(true);z.setTarget(p);z.setPos(p.getX()+.1,p.getY(),p.getZ());h.getLevel().addFreshEntity(z);
   try{float damage=(float)z.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);float hp=p.getHealth();
    SpiritShieldImpactEvents.repel(p);p.hurt(p.damageSources().mobAttack(z),damage+1.5F);
    h.assertTrue(Math.abs(p.getAbsorptionAmount()-(8-damage-1.5F))<.001&&p.getHealth()==hp,"Stronger melee contact leaked past remaining shield");h.succeed();
   }finally{z.discard();}
  }
 }
}
