package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.artifacts.*;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.*;
import java.util.UUID;

/** Exercises the registered gameplay event handlers, not independent copies of effect calculations. */
@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class ArtifactEffectsGameTests {
    private static GameTestPlayer player(GameTestHelper h){
        var p=new GameTestPlayer(h.getLevel());var pos=h.absolutePos(new BlockPos(2,4,2));
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);return p;
    }
    private static ItemStack sword(ArtifactAux aux,int material){
        var stack=new ItemStack(ArtifactBootstrap.ARTIFACTS.get(ArtifactKind.SWORD).get());
        EquipmentCrafting.write(stack).putInt("ArtifactMaterial",material);ArtifactData.infuse(stack,aux);return stack;
    }
    private static <T extends Mob>T target(GameTestHelper h,GameTestPlayer p,EntityType<T> type){
        var mob=type.create(h.getLevel());if(mob==null)throw new AssertionError("Missing target type");
        mob.setNoAi(true);mob.setPos(p.getX()+1,p.getY(),p.getZ());
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);mob.setHealth(200);return mob;
    }
    private static LivingHurtEvent strike(GameTestPlayer p,LivingEntity target,float amount){
        var event=new LivingHurtEvent(target,p.damageSources().playerAttack(p),amount);ArtifactEffects.hurt(event);return event;
    }
    private static void receive(GameTestPlayer p){ArtifactEffects.hurt(new LivingHurtEvent(p,p.damageSources().generic(),4));}
    private static void tick(GameTestPlayer p,TickEvent.Phase phase){ArtifactEffects.tick(new TickEvent.PlayerTickEvent(phase,p));}

    @GameTest(template="empty") public static void bloodthirstAndCrystalSlowUseTheActualDamageEvent(GameTestHelper h){
        try(var p=player(h)){
            var victim=target(h,p,EntityType.ZOMBIE);p.setHealth(5);
            p.setItemInHand(InteractionHand.MAIN_HAND,sword(ArtifactAux.LONGXUE,2));
            var event=strike(p,victim,10);
            h.assertTrue(event.getAmount()==10&&Math.abs(p.getHealth()-6.8F)<.0001F,"Bloodthirst did not heal eighteen percent of the hurt-event amount");
            var slow=victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            h.assertTrue(slow!=null&&slow.getDuration()==40&&slow.getAmplifier()==0,"Crystal artifact lost its actual target debuff");
            float health=p.getHealth();strike(p,victim,0);
            h.assertTrue(p.getHealth()==health,"A zero-damage event healed the attacker");
        }h.succeed();
    }

    @GameTest(template="empty") public static void thunderProcUsesConductAndDoesNotBecomeAnAlwaysOnBonus(GameTestHelper h){
        try(var p=player(h)){
            var victim=target(h,p,EntityType.ZOMBIE);var stack=sword(ArtifactAux.LEIJING,2);
            EquipmentCrafting.setRune(stack,"control",2,0);EquipmentCrafting.write(stack).putInt("ArtifactQuenchConduct",3);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            // Minecraft's legacy RandomSource matches these fixed Java LCG seeds.
            p.getRandom().setSeed(4096);var proc=strike(p,victim,10);
            h.assertTrue(Math.abs(proc.getAmount()-16.5F)<.0001F,"The seeded thunder proc lost quality or the single conduct authority");
            p.getRandom().setSeed(0);var ordinary=strike(p,victim,10);
            h.assertTrue(ordinary.getAmount()==10,"Thunder became a permanent flat damage bonus");
        }h.succeed();
    }

    @GameTest(template="empty") public static void foreignOwnersPetsAndBannerServantsCannotFarmArtifactEffects(GameTestHelper h){
        try(var p=player(h)){
            var stack=sword(ArtifactAux.LONGXUE,2);p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.setHealth(5);
            var pet=target(h,p,EntityType.WOLF);pet.setOwnerUUID(p.getUUID());pet.setTame(true);
            var servant=target(h,p,EntityType.ZOMBIE);var tag=servant.getPersistentData();
            tag.putUUID("HumanEmperorBannerOwner",p.getUUID());tag.putUUID("HumanEmperorBannerId",UUID.randomUUID());tag.putString("HumanEmperorBannerType","minecraft:zombie");
            for(var protectedTarget:new LivingEntity[]{pet,servant}){
                var event=strike(p,protectedTarget,10);
                h.assertTrue(event.getAmount()==10&&p.getHealth()==5&&!protectedTarget.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"A protected target granted artifact healing or a debuff");
            }
            stack.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());var enemy=target(h,p,EntityType.ZOMBIE);
            var event=strike(p,enemy,10);
            h.assertTrue(event.getAmount()==10&&p.getHealth()==5&&!enemy.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"A foreign-bound artifact still supplied attack effects");
        }h.succeed();
    }

    @GameTest(template="empty") public static void shieldSharesVanillaAbsorptionAndHonorsTheTwoHundredTickCooldown(GameTestHelper h){
        try(var p=player(h)){
            p.setItemInHand(InteractionHand.MAIN_HAND,sword(ArtifactAux.HUXINYU,1));
            p.setItemInHand(InteractionHand.OFF_HAND,sword(ArtifactAux.TAIYIN,1));receive(p);
            var absorption=p.getEffect(MobEffects.ABSORPTION);long first=p.getPersistentData().getLong("MyriadArtifactShield");
            h.assertTrue(absorption!=null&&absorption.getDuration()==200&&absorption.getAmplifier()==0&&p.getAbsorptionAmount()==4,"Two equipped shields stacked a second pool instead of vanilla absorption");
            p.removeEffect(MobEffects.ABSORPTION);receive(p);
            h.assertTrue(!p.hasEffect(MobEffects.ABSORPTION)&&p.getPersistentData().getLong("MyriadArtifactShield")==first,"Removing absorption bypassed its cooldown");
            p.getPersistentData().putLong("MyriadArtifactShield",p.level().getGameTime()-199);receive(p);
            h.assertTrue(!p.hasEffect(MobEffects.ABSORPTION),"Shield refreshed before two hundred ticks");
            p.getPersistentData().putLong("MyriadArtifactShield",p.level().getGameTime()-200);receive(p);
            h.assertTrue(p.hasEffect(MobEffects.ABSORPTION)&&p.getAbsorptionAmount()==4,"Shield did not refresh at the cooldown boundary");
            p.removeEffect(MobEffects.ABSORPTION);p.getPersistentData().remove("MyriadArtifactShield");
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getMainHandItem().getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());receive(p);
            h.assertTrue(!p.hasEffect(MobEffects.ABSORPTION)&&!p.getPersistentData().contains("MyriadArtifactShield"),"Foreign equipment granted a shield or started a cooldown");
        }h.succeed();
    }

    @GameTest(template="empty") public static void conductRestorationUsesTheServerTickGateCapacityAndOwner(GameTestHelper h){
        try(var p=player(h)){
            var stack=sword(ArtifactAux.LONGXUE,0);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var data=(CultivationDataImpl)GeneratedMod.data(p);data.setMaxMana(100);data.wuxingxiantu$setAura(99.9F);
            p.tickCount=19;tick(p,TickEvent.Phase.END);h.assertTrue(data.wuxingxiantu$getAura()==99.9F,"Conduct restored aura on a non-second tick");
            p.tickCount=20;tick(p,TickEvent.Phase.START);h.assertTrue(data.wuxingxiantu$getAura()==99.9F,"Conduct restored aura in both tick phases");
            tick(p,TickEvent.Phase.END);h.assertTrue(data.wuxingxiantu$getAura()==100,"Conduct exceeded capacity or lost copper restoration");
            data.wuxingxiantu$setAura(10);stack.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());tick(p,TickEvent.Phase.END);
            h.assertTrue(data.wuxingxiantu$getAura()==10,"Foreign-bound equipment supplied conduct restoration");
        }h.succeed();
    }
}
