package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance;
import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance.Domain;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("wuxingxiantu_domainbalance")
@PrefixGameTestTemplate(false)
public final class DamageDomainGameTests {
    @GameTest(template="empty") public static void exoticIgnitesBeforeDomainDamage(GameTestHelper h){
        try(var s=new Scene(h,9)){
            s.data.wuxingxiantu$setRealm(8);s.data.wuxingxiantu$learnFire(9);s.data.wuxingxiantu$setEquippedFire(9);
            s.data.setCurrentMana(s.data.getMaxMana());
            var target=s.target(1,0);boolean[] saw={false};
            java.util.function.Consumer<net.minecraftforge.event.entity.living.LivingHurtEvent> observer=event->{
                if(event.getEntity()==target&&event.getSource() instanceof cn.blockforge.generated.generatedmod.combat.DomainDamageSource){
                    saw[0]=cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.state(target)!=null;
                }
            };
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(observer);
            try{s.pulse(Domain.EXOTIC,4);}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(observer);}
            h.assertTrue(saw[0],"Domain damage arrived before exotic ignition");h.succeed();
        }
    }
    private static final class Scene implements AutoCloseable {
        final GameTestPlayer player;
        final List<Husk> targets=new ArrayList<>();
        final GameTestHelper helper;
        final CultivationData data;
        Scene(GameTestHelper h,int ordinal){
            helper=h;player=new GameTestPlayer(h.getLevel());
            var pos=h.absolutePos(new BlockPos(1,0,1));
            // Isolate concurrent fields vertically, beyond their maximum radius.
            player.setPos(pos.getX()+.5,180+ordinal*12,pos.getZ()+.5);
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            data=GeneratedMod.data(player);data.wuxingxiantu$setRealm(2);
            data.wuxingxiantu$setMaxMana(1000);data.wuxingxiantu$setAura(1000);
        }
        Husk target(double x,double z){
            var mob=EntityType.HUSK.create(helper.getLevel());
            mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.getX()+x,player.getY(),player.getZ()+z);
            helper.getLevel().addFreshEntity(mob);mob.removeAllEffects();
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);
            mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
            targets.add(mob);return mob;
        }
        void pulse(Domain domain,int quality){
            try{
                String name=switch(domain){case FIRE->"fireAura";case THUNDER->"thunderDomain";case EXOTIC->"exoticFireDomain";case TIDE->"tideDomain";case GRAVITY->"gravityDomain";};
                var method=domain==Domain.TIDE||domain==Domain.GRAVITY
                        ?CultivationLogic.class.getDeclaredMethod(name,net.minecraft.world.entity.player.Player.class,int.class)
                        :CultivationLogic.class.getDeclaredMethod(name,net.minecraft.world.entity.player.Player.class,CultivationData.class,int.class);
                method.setAccessible(true);
                if(domain==Domain.TIDE||domain==Domain.GRAVITY)method.invoke(null,player,quality);else method.invoke(null,player,data,quality);
            }catch(ReflectiveOperationException error){throw new AssertionError("Production domain pulse failed",error);}
        }
        int quote(Domain d,int q,int count,boolean pulse){return CultivationLogic.domainManaCost(player,data,d,q,count,pulse,false);}
        public void close(){CultivationLogic.clearDomainEffects(helper.getLevel(),player);targets.forEach(Husk::discard);player.close();}
    }
    private static void near(GameTestHelper h,double actual,double expected,String why){h.assertTrue(Math.abs(actual-expected)<.005,why+": "+actual+" versus "+expected);}

    @GameTest(template="empty") public static void liveAttackModifierAndMana(GameTestHelper h){
        try(var s=new Scene(h,0)){
            var mob=s.target(1,0);int cost=s.quote(Domain.FIRE,3,1,true);
            s.pulse(Domain.FIRE,3);near(h,1000-mob.getHealth(),6.4,"Base live attack ignored");near(h,s.data.wuxingxiantu$getAura(),1000-cost,"First pulse debit");
            mob.invulnerableTime=0;mob.removeAllEffects();mob.setHealth(1000);
            s.player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(UUID.randomUUID(),"live weapon/buff",20,AttributeModifier.Operation.ADDITION));
            int stronger=s.quote(Domain.FIRE,3,1,true);float before=s.data.wuxingxiantu$getAura();
            s.pulse(Domain.FIRE,3);near(h,1000-mob.getHealth(),10.8,"Changed equipment/buff attack was not read live");near(h,s.data.wuxingxiantu$getAura(),before-stronger,"Stronger damage debit");
            h.assertTrue(stronger>cost,"Higher output was free");h.succeed();
        }
    }
    @GameTest(template="empty") public static void everyDamagingDomainPaysItsPulse(GameTestHelper h){
        for(var domain:Domain.values())try(var s=new Scene(h,1)){
            var mob=s.target(.5,0);s.player.tickCount=40;
            int cost=s.quote(domain,4,1,true);s.pulse(domain,4);
            near(h,1000-mob.getHealth(),CultivationLogic.domainDamage(s.player,domain,4),"Wrong actual damage for "+domain);
            near(h,s.data.wuxingxiantu$getAura(),1000-cost,"Wrong actual debit for "+domain);
            h.assertTrue(mob.getLastHurtByMob()==s.player,"Domain lost caster attribution: "+domain);
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void insufficientManaDoesNotPartiallyRelease(GameTestHelper h){
        for(var domain:Domain.values())try(var s=new Scene(h,2)){
            var mob=s.target(.5,0);s.player.tickCount=40;
            int cost=s.quote(domain,4,1,true);s.data.wuxingxiantu$setAura(cost-1);
            double speed=mob.getAttributeValue(Attributes.MOVEMENT_SPEED);s.pulse(domain,4);
            near(h,mob.getHealth(),1000,"Unpaid domain damaged target: "+domain);
            near(h,s.data.wuxingxiantu$getAura(),cost-1,"Unpaid domain consumed partial mana: "+domain);
            near(h,mob.getAttributeValue(Attributes.MOVEMENT_SPEED),speed,"Unpaid control was applied: "+domain);
            h.assertTrue(!mob.isOnFire()&&!mob.hasEffect(ModEffects.PARALYSIS.get()),"Unpaid domain effect leaked");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void emptyAndOutsideSphereAreFree(GameTestHelper h){
        try(var s=new Scene(h,3)){
            var corner=s.target(6,6);s.pulse(Domain.FIRE,4);
            near(h,corner.getHealth(),1000,"Box corner outside visible sphere was damaged");near(h,s.data.wuxingxiantu$getAura(),1000,"Empty sphere charged upkeep");
            corner.discard();for(var d:Domain.values())s.pulse(d,4);
            near(h,s.data.wuxingxiantu$getAura(),1000,"Empty domains drained mana");h.succeed();
        }
    }
    @GameTest(template="empty") public static void areaBudgetCapsOutputAndCostTogether(GameTestHelper h){
        try(var s=new Scene(h,4)){
            for(int i=0;i<17;i++)s.target(.8+i*.07,0);
            int quote=s.quote(Domain.FIRE,4,12,true);s.pulse(Domain.FIRE,4);
            long hit=s.targets.stream().filter(mob->mob.getHealth()<1000).count();
            h.assertTrue(hit==12,"Field hit "+hit+" targets instead of 12");near(h,s.data.wuxingxiantu$getAura(),1000-quote,"Charged uncapped target count");h.succeed();
        }
    }
    @GameTest(template="empty") public static void thunderPaysOnlyThreeNearestStrikes(GameTestHelper h){
        try(var s=new Scene(h,5)){
            for(int i=1;i<=6;i++)s.target(i*.7,0);
            int quote=s.quote(Domain.THUNDER,4,3,true);s.pulse(Domain.THUNDER,4);
            for(int i=0;i<6;i++)h.assertTrue((s.targets.get(i).getHealth()<1000)==(i<3),"Thunder chose wrong nearest target");
            near(h,s.data.wuxingxiantu$getAura(),1000-quote,"Thunder charge did not match emitted strikes");
            h.assertTrue(s.targets.get(0).getEffect(ModEffects.PARALYSIS.get()).getDuration()==10,"Thunder stun can overlap permanently");h.succeed();
        }
    }
    @GameTest(template="empty") public static void tideAlternatesControlAndPaidDamage(GameTestHelper h){
        try(var s=new Scene(h,6)){
            var mob=s.target(.5,0);s.player.tickCount=20;int quiet=s.quote(Domain.TIDE,3,1,false);
            s.pulse(Domain.TIDE,3);near(h,mob.getHealth(),1000,"Tide wrong half-second phase");near(h,s.data.wuxingxiantu$getAura(),1000-quiet,"Control-only quote");
            s.player.tickCount=40;int active=s.quote(Domain.TIDE,3,1,true);s.pulse(Domain.TIDE,3);
            near(h,1000-mob.getHealth(),1.9,"Tide attack ratio");near(h,s.data.wuxingxiantu$getAura(),1000-quiet-active,"Tide damage phase charged twice");h.succeed();
        }
    }
    @GameTest(template="empty") public static void controlledTargetStillTakesPaidDomainDamage(GameTestHelper h){
        try(var s=new Scene(h,7)){
            var mob=s.target(.5,0);mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,10));
            s.pulse(Domain.GRAVITY,4);near(h,1000-mob.getHealth(),3.8,"Domain control made victim immune to paid pulse");h.succeed();
        }
    }
}
