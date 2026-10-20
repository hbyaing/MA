package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Required tests of the real transformed attack path. Written but not run in the source-only delivery. */
@GameTestHolder(GeneratedMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HeartSwordCombatGameTests {
    private static GameTestPlayer player(GameTestHelper h) {
        var p=new GameTestPlayer(h.getLevel());
        p.cleanupAfter(h,()->{});
        var pos=h.absolutePos(new BlockPos(2,1,2));
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        var data=GeneratedMod.data(p);
        data.wuxingxiantu$setRealm(0);data.wuxingxiantu$setCultivationStage(0);
        data.wuxingxiantu$setMaxMana(10000);data.wuxingxiantu$setAura(10000);
        p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);
        p.join();return p;
    }
    private static void equip(Player p,ItemStack stack) {
        p.getAttributes().removeAttributeModifiers(p.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND));
        p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        p.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));
    }
    private static void timer(Object object,Class<?> type,String name,int value) {
        try {var field=type.getDeclaredField(name);field.setAccessible(true);field.setInt(object,value);}
        catch(ReflectiveOperationException e) {throw new IllegalStateException("Forge 1.20.1 fixture mapping changed: "+name,e);}
    }
    private static float attack(GameTestPlayer p,LivingEntity target,boolean charged) {
        timer(p,LivingEntity.class,"attackStrengthTicker",charged?100:0);
        p.setOnGround(true);p.fallDistance=0;target.invulnerableTime=0;
        float before=target.getHealth();p.attack(target);return before-target.getHealth();
    }
    private static void near(GameTestHelper h,double actual,double expected,String message) {
        h.assertTrue(Math.abs(actual-expected)<.02,message+": expected="+expected+", actual="+actual);
    }
    private static void defense(GameTestHelper h,java.util.function.Consumer<GameTestPlayer> prepare) {
        try(var p=player(h);var target=player(h)) {
            target.setPos(p.getX(),p.getY(),p.getZ()+1);
            equip(p,HeartSwordItem.create(p.getUUID()));prepare.accept(target);
            if(target.isBlocking())h.assertTrue(target.isDamageSourceBlocked(p.damageSources().playerAttack(p)),"Shield does not face the attack");
            double expected=HeartSwordCombatProfile.attackBaseline(p,p.getMainHandItem());
            near(h,attack(p,target,true),expected,"Defense reduced heart damage");h.succeed();
        }
    }
    @GameTest(template="empty") public static void armorAndToughness(GameTestHelper h) {
        defense(h,p->{p.getAttribute(Attributes.ARMOR).setBaseValue(30);p.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(20);});
    }
    @GameTest(template="empty") public static void resistance(GameTestHelper h) {
        defense(h,p->p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,200,4)));
    }
    @GameTest(template="empty") public static void protection(GameTestHelper h) {
        defense(h,p->{var chest=new ItemStack(Items.DIAMOND_CHESTPLATE);chest.enchant(Enchantments.ALL_DAMAGE_PROTECTION,4);p.setItemSlot(EquipmentSlot.CHEST,chest);});
    }
    @GameTest(template="empty") public static void absorptionPreserved(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));target.setAbsorptionAmount(200);
            near(h,attack(p,target,true),HeartSwordCombatProfile.attackBaseline(p,p.getMainHandItem()),"Absorption reduced damage");
            near(h,target.getAbsorptionAmount(),200,"Heart damage consumed absorption");h.succeed();
        }
    }
    @GameTest(template="empty") public static void shield(GameTestHelper h) {
        defense(h,p->{p.setYRot(180);p.yRotO=180;p.setYHeadRot(180);p.yHeadRotO=180;
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));
            p.startUsingItem(InteractionHand.OFF_HAND);
            timer(p,LivingEntity.class,"useItemRemaining",p.getUseItem().getUseDuration()-10);
            h.assertTrue(p.isBlocking(),"Shield fixture is not blocking");});
    }
    @GameTest(template="empty") public static void allRealmAndStageGrowth(GameTestHelper h) {
        try(var p=player(h)) {
            var sword=HeartSwordItem.create(p.getUUID());equip(p,sword);
            for(int realm=0;realm<=8;realm++)for(int stage=0;stage<=3;stage++) {
                var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(realm);d.wuxingxiantu$setCultivationStage(stage);
                near(h,HeartSwordCombatProfile.attackBaseline(p,sword),100*(1.10+.05*realm)*(1+.02*stage),"Growth must not re-add realm attack");
            }
            h.succeed();
        }
    }
    @GameTest(template="empty") public static void forgedUsesMaximumAndSharedTraits(GameTestHelper h) {
        try(var p=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());cn.blockforge.generated.generatedmod.combat.HeartForgeContracts.bind(p,sword);equip(p,sword);
            for(int base:new int[]{1,100})for(int realm:new int[]{0,8}) {
                p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(base);
                GeneratedMod.data(p).wuxingxiantu$setRealm(realm);
                double a=HeartSwordCombatProfile.originalSwordDamage(p,sword),b=HeartSwordCombatProfile.projectHeartSwordDamage(p);
                near(h,HeartSwordCombatProfile.attackBaseline(p,sword),Math.max(a,b),"Forged must use max, not sum");
                h.assertTrue(HeartSwordCombatProfile.traits(p,sword).equals(HeartSwordCombatProfile.traits(p,HeartSwordItem.create(p.getUUID()))),"Traits diverged");
            }
            h.succeed();
        }
    }
    @GameTest(template="empty") public static void unforgePreservesPhysicalItem(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());sword.enchant(Enchantments.SHARPNESS,2);
            var before=sword.copy();cn.blockforge.generated.generatedmod.combat.HeartForgeContracts.bind(p,sword);SpiritSwordItem.clearHeartForge(sword);
            h.assertTrue(ItemStack.matches(sword,before),"Unforge changed original sword or enchantments");
            equip(p,sword);h.assertTrue(HeartSwordCombatProfile.traits(p,sword).isEmpty(),"Traits remain after unforge");
            h.assertTrue(attack(p,target,true)>0,"Physical attack stopped after unforge");h.succeed();
        }
    }
    @GameTest(template="empty") public static void nonownerPureSwordRejected(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(java.util.UUID.randomUUID()));near(h,attack(p,target,true),0,"Foreign pure sword dealt damage");h.succeed();
        }
    }
    @GameTest(template="empty") public static void nonownerForgedRemainsPhysical(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());SpiritSwordItem.setHeartForged(sword,java.util.UUID.randomUUID());equip(p,sword);
            var probe=new Probe(target);MinecraftForge.EVENT_BUS.register(probe);
            try {h.assertTrue(attack(p,target,true)>0&&!probe.heart,"Foreign forge inherited heart damage");}
            finally {MinecraftForge.EVENT_BUS.unregister(probe);}h.succeed();
        }
    }
    @GameTest(template="empty") public static void existingForgeGrowsWithoutNbtSnapshots(GameTestHelper h) {
        try(var p=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());cn.blockforge.generated.generatedmod.combat.HeartForgeContracts.bind(p,sword);equip(p,sword);
            var tag=sword.getTag().copy();double initial=HeartSwordCombatProfile.attackBaseline(p,sword);
            GeneratedMod.data(p).wuxingxiantu$setRealm(8);GeneratedMod.data(p).wuxingxiantu$setCultivationStage(3);
            h.assertTrue(HeartSwordCombatProfile.attackBaseline(p,sword)>initial&&tag.equals(sword.getTag()),"Forge did not grow dynamically");h.succeed();
        }
    }
    @GameTest(template="empty") public static void creativeAndSpectatorProtected(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));
            for(var mode:new GameType[]{GameType.CREATIVE,GameType.SPECTATOR}) {
                target.setGameMode(mode);near(h,attack(p,target,true),0,"Protected player damaged: "+mode);
            }
            h.succeed();
        }
    }
    public static final class Probe {
        final LivingEntity target;int hurts,damages;boolean heart;
        Probe(LivingEntity target) {this.target=target;}
        @SubscribeEvent public void hurt(LivingHurtEvent e) {if(e.getEntity()==target){hurts++;heart|=HeartSwordCombatProfile.isHeartTrue(e.getSource());}}
        @SubscribeEvent public void damage(LivingDamageEvent e) {if(e.getEntity()==target)damages++;}
    }
    @GameTest(template="empty") public static void exactlyOnePrimaryHit(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));var probe=new Probe(target);MinecraftForge.EVENT_BUS.register(probe);
            try {attack(p,target,true);h.assertTrue(probe.hurts==1&&probe.damages==1&&probe.heart,"Expected one heart hurt and damage event, got "+probe.hurts+"/"+probe.damages);}
            finally {MinecraftForge.EVENT_BUS.unregister(probe);}h.succeed();
        }
    }
    @GameTest(template="empty") public static void cooldownStillScalesDamage(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));float full=attack(p,target,true);float weak=attack(p,target,false);
            h.assertTrue(weak>0&&weak<full*.3,"Attack cooldown was bypassed");h.succeed();
        }
    }
    @GameTest(template="empty") public static void unrelatedSpellStaysPhysical(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));target.setAbsorptionAmount(200);
            target.hurt(p.damageSources().indirectMagic(p,p),20);
            near(h,target.getHealth(),1000,"Holding heart sword converted unrelated spell");
            near(h,target.getAbsorptionAmount(),180,"Spell absorption changed");h.succeed();
        }
    }
    @GameTest(template="empty") public static void heartDemonPhaseLockRespected(GameTestHelper h) {
        try(var p=player(h)) {
            var boss=HeartDemonEntity.create(h.getLevel(),p);h.getLevel().addFreshEntity(boss);
            try {
                HeartSwordCombatProfile.applyHeartTrueDamage(p,boss,1000000);
                h.assertTrue(boss.isAlive()&&boss.visualPhase()==HeartDemonEntity.VisualPhase.PHASE_2,"Heart lethal damage skipped phase one");
                h.assertTrue(!HeartSwordCombatProfile.applyHeartTrueDamage(p,boss,1000000)&&boss.isAlive(),"Heart damage bypassed phase transition lock");
            } finally {boss.discard();}h.succeed();
        }
    }
    @GameTest(template="empty") public static void ordinaryInvulnerabilityRespected(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            target.setInvulnerable(true);h.assertTrue(!HeartSwordCombatProfile.applyHeartTrueDamage(p,target,100),"Heart damage bypassed invulnerability");h.succeed();
        }
    }
    @GameTest(template="empty") public static void creationPillRepairsDaoHeartAgain(GameTestHelper h) {
        try(var p=player(h)) {
            p.setGameMode(GameType.CREATIVE);var d=GeneratedMod.data(p);
            var pill=new ItemStack(WuXingItems.CREATION_PILL.get());pill.finishUsingItem(h.getLevel(),p);
            d.wuxingxiantu$setDaoXin(4);d.wuxingxiantu$setHeartDemonInvasionCause("PILL");
            d.wuxingxiantu$getMindscapeData().putString("pending_challenge","stale");
            pill.finishUsingItem(h.getLevel(),p);
            h.assertTrue(d.wuxingxiantu$getDaoXin()==1&&d.wuxingxiantu$getHeartDemonInvasionCause().isEmpty()
                    &&!d.wuxingxiantu$getMindscapeData().contains("pending_challenge"),"Fully granted player could not repair Dao Heart");h.succeed();
        }
    }
    public static final class NumericDefense {
        final LivingEntity target;boolean cancel;
        NumericDefense(LivingEntity target) {this.target=target;}
        @SubscribeEvent public void hurt(LivingHurtEvent e) {
            if(e.getEntity()==target) {e.setAmount(e.getAmount()*.25F);if(cancel)e.setCanceled(true);}
        }
        @SubscribeEvent public void damage(LivingDamageEvent e) {if(e.getEntity()==target)e.setAmount(e.getAmount()*.25F);}
    }
    @GameTest(template="empty") public static void numericReductionBypassedButCancellationRespected(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            equip(p,HeartSwordItem.create(p.getUUID()));var defense=new NumericDefense(target);MinecraftForge.EVENT_BUS.register(defense);
            try {
                near(h,attack(p,target,true),HeartSwordCombatProfile.attackBaseline(p,p.getMainHandItem()),"Numeric reduction leaked");
                defense.cancel=true;near(h,attack(p,target,true),0,"Gameplay event cancellation ignored");
            } finally {MinecraftForge.EVENT_BUS.unregister(defense);}h.succeed();
        }
    }
    @GameTest(template="empty") public static void forgedMeleeKeepsEnchantmentAndTrueDamage(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());sword.enchant(Enchantments.SHARPNESS,2);
            cn.blockforge.generated.generatedmod.combat.HeartForgeContracts.bind(p,sword);equip(p,sword);
            target.getAttribute(Attributes.ARMOR).setBaseValue(30);target.setAbsorptionAmount(200);
            double expected=HeartSwordCombatProfile.attackBaseline(p,sword)
                    +net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(sword,target.getMobType());
            var probe=new Probe(target);MinecraftForge.EVENT_BUS.register(probe);
            try {
                near(h,attack(p,target,true),expected,"Forged melee lost enchantment or true damage");
                h.assertTrue(probe.heart&&probe.hurts==1&&probe.damages==1,"Forged melee duplicated hit");
            } finally {MinecraftForge.EVENT_BUS.unregister(probe);}h.succeed();
        }
    }
    @GameTest(template="empty") public static void rightEquipmentSlotsAligned(GameTestHelper h) {
        try(var p=player(h)) {
            var slots=p.inventoryMenu.slots;
            var robe=slots.stream().filter(s->s instanceof cn.blockforge.generated.generatedmod.robe.EquipmentSlots.EquipmentSlot
                    &&s.getContainerSlot()==0).findFirst().orElseThrow();
            var sword=slots.stream().filter(s->s instanceof cn.blockforge.generated.generatedmod.robe.EquipmentSlots.EquipmentSlot
                    &&s!=robe).findFirst().orElseThrow();
            var offhand=slots.get(45);
            h.assertTrue(robe.x==sword.x&&sword.x==offhand.x&&robe.x==80,"Right equipment column misaligned");
            h.assertTrue(sword.y-robe.y==27&&offhand.y-sword.y==27,"Right equipment rows not evenly spaced");h.succeed();
        }
    }
    private HeartSwordCombatGameTests() {}
}
