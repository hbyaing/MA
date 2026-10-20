package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fire.ExoticBurnManager;
import cn.blockforge.generated.generatedmod.fire.WeaponFlameState;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticFireTargetingPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.SyncPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Server gameplay regressions for the sender's wrapping-enchantment target preference. */
@GameTestHolder("wuxingxiantu_firetargets")
@PrefixGameTestTemplate(false)
public final class ExoticFireTargetingGameTests {
    private static GameTestPlayer player(GameTestHelper helper){
        var player=new GameTestPlayer(helper.getLevel());player.cleanupAfter(helper,()->{});
        var at=helper.absolutePos(new BlockPos(2,1,2));player.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);player.join();
        var data=GeneratedMod.data(player);data.wuxingxiantu$setRealm(8);
        for(int tier=1;tier<=9;tier++)data.wuxingxiantu$learnFire(tier);
        data.wuxingxiantu$setEquippedFire(1);return player;
    }
    private static CultivationDataImpl data(GameTestPlayer player){return (CultivationDataImpl)GeneratedMod.data(player);}
    private static <T extends Mob>T target(GameTestHelper helper,GameTestPlayer owner,EntityType<T> type,double dx){
        T target=type.create(helper.getLevel());if(target==null)throw new AssertionError("Missing target type "+type);
        target.setNoAi(true);target.setPos(owner.getX()+dx,owner.getY(),owner.getZ()+.5);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);
        helper.getLevel().addFreshEntity(target);return target;
    }
    private static void armor(GameTestPlayer player){
        var armor=new ItemStack(Items.DIAMOND_CHESTPLATE);armor.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),5);
        player.setItemSlot(EquipmentSlot.CHEST,armor);
    }
    private static void sword(GameTestPlayer player,int tier){
        data(player).wuxingxiantu$setEquippedFire(tier);
        var sword=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"dark_iron_sword")));
        sword.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),5);player.setItemSlot(EquipmentSlot.MAINHAND,sword);WeaponFlameState.refresh(player);
    }
    private static LivingHurtEvent strike(GameTestPlayer player,LivingEntity target){
        var event=new LivingHurtEvent(target,player.damageSources().playerAttack(player),10);
        SpiritEnchantmentLogic.applyExoticFireStrike(event);return event;
    }
    private static void attackTarget(Mob mob,LivingEntity target){
        mob.setTarget(target);
        // AbstractPiglin.getTarget reads the Brain instead of Mob.target.
        if(mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin)
            mob.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,target);
        if(mob.getTarget()!=target)throw new AssertionError("Fixture did not set the actual attack target for "+mob.getType());
    }
    private static void servant(Mob target,GameTestPlayer owner){
        var tag=target.getPersistentData();tag.putUUID("HumanEmperorBannerOwner",owner.getUUID());
        tag.putUUID("HumanEmperorBannerId",java.util.UUID.randomUUID());tag.putString("HumanEmperorBannerType","minecraft:zombie");
    }

    @GameTest(template="empty") public static void defaultsPersistAndLegacySavesProtectNonHostile(GameTestHelper helper){
        var data=new CultivationDataImpl();helper.assertTrue(!data.exoticFireHurtsNonHostile(),"New player default harms non-hostiles");
        long version=data.stableSyncVersion();data.setExoticFireHurtsNonHostile(true);
        helper.assertTrue(data.stableSyncVersion()>version,"Setting did not invalidate stable sync");
        var copy=new CultivationDataImpl();copy.deserializeNBT(data.serializeNBT());
        helper.assertTrue(copy.exoticFireHurtsNonHostile(),"Enabled preference was not persisted");
        data.setExoticFireHurtsNonHostile(false);copy.deserializeNBT(data.serializeNBT());
        helper.assertTrue(!copy.exoticFireHurtsNonHostile(),"Disabled preference was not persisted");
        var legacy=data.serializeNBT();legacy.remove("ExoticFireHurtsNonHostile");copy.setExoticFireHurtsNonHostile(true);copy.deserializeNBT(legacy);
        helper.assertTrue(!copy.exoticFireHurtsNonHostile(),"Legacy save did not receive protective default");helper.succeed();
    }
    @GameTest(template="empty") public static void preferencePacketsRoundTripAndRemainSenderScoped(GameTestHelper helper){
        try(var owner=player(helper);var other=player(helper)){
            for(boolean enabled:new boolean[]{true,false}){
                var buffer=new FriendlyByteBuf(Unpooled.buffer());
                try{
                    var packet=new ExoticFireTargetingPacket(enabled);ExoticFireTargetingPacket.encode(packet,buffer);
                    var decoded=ExoticFireTargetingPacket.decode(buffer);helper.assertTrue(decoded.equals(packet)&&buffer.readableBytes()==0,"C2S payload changed");
                    ExoticFireTargetingPacket.apply(owner,decoded.enabled());
                    helper.assertTrue(data(owner).exoticFireHurtsNonHostile()==enabled&&!data(other).exoticFireHurtsNonHostile(),"Preference changed another player");
                }finally{buffer.release();}
                buffer=new FriendlyByteBuf(Unpooled.buffer());
                try{
                    var packet=new SyncPacket(data(owner));SyncPacket.encode(packet,buffer);var decoded=SyncPacket.decode(buffer);
                    helper.assertTrue(decoded.exoticFireHurtsNonHostile()==enabled&&buffer.readableBytes()==0,"S2C stable preference did not round trip");
                    var stable=decoded.stableState();helper.assertTrue(stable[stable.length-1].equals(enabled),"Preference absent from stable snapshot comparison");
                }finally{buffer.release();}
            }
            ExoticFireTargetingPacket.apply(null,true);helper.succeed();
        }
    }
    @GameTest(template="empty") public static void defaultAuraProtectsAnimalsAndIdleNeutralsButBurnsEnemies(GameTestHelper helper){
        try(var owner=player(helper)){
            armor(owner);var cow=target(helper,owner,EntityType.COW,.5);var wolf=target(helper,owner,EntityType.WOLF,-.5);
            var zombie=target(helper,owner,EntityType.ZOMBIE,1.5);var hostileWolf=target(helper,owner,EntityType.WOLF,-1.5);hostileWolf.setTarget(owner);
            SpiritEnchantmentLogic.tick(owner,0);
            helper.assertTrue(!ExoticBurnManager.isBurning(cow)&&!ExoticBurnManager.isBurning(wolf),"Default aura ignited a protected target");
            helper.assertTrue(ExoticBurnManager.isBurning(zombie)&&ExoticBurnManager.isBurning(hostileWolf),"Aura rejected an actual enemy");
            float cowHealth=cow.getHealth(),wolfHealth=wolf.getHealth();for(int i=0;i<40;i++){ExoticBurnManager.tick(cow);ExoticBurnManager.tick(wolf);}
            helper.assertTrue(cow.getHealth()==cowHealth&&wolf.getHealth()==wolfHealth,"Protected target took delayed burn damage");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void enablingAuraAllowsNonHostilesAndDisablingStopsNewIgnition(GameTestHelper helper){
        try(var owner=player(helper)){
            armor(owner);var cow=target(helper,owner,EntityType.COW,.5);data(owner).setExoticFireHurtsNonHostile(true);SpiritEnchantmentLogic.tick(owner,0);
            helper.assertTrue(ExoticBurnManager.isBurning(cow),"Enabled aura still protects non-hostiles");ExoticBurnManager.clear(cow);
            data(owner).setExoticFireHurtsNonHostile(false);SpiritEnchantmentLogic.tick(owner,20);
            helper.assertTrue(!ExoticBurnManager.isBurning(cow),"Disabled aura applied a new burn");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void protectedWeaponHitKeepsOrdinaryAttackWithoutExtraBurnOrDebuff(GameTestHelper helper){
        try(var owner=player(helper)){
            sword(owner,3);var cow=target(helper,owner,EntityType.COW,.5);var event=strike(owner,cow);
            helper.assertTrue(event.getAmount()==10&&!event.isCanceled()&&!ExoticBurnManager.isBurning(cow),"Protected weapon hit changed normal attack or ignited target");
            helper.assertTrue(!cow.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Protected target received cold-bone debuff");
            float before=cow.getHealth();cow.hurt(owner.damageSources().playerAttack(owner),4);
            helper.assertTrue(cow.getHealth()<before&&!ExoticBurnManager.isBurning(cow),"Ordinary active melee was blocked or gained exotic burn");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void weaponEnablesNonHostilesAndAlwaysAllowsActualEnemies(GameTestHelper helper){
        try(var owner=player(helper)){
            sword(owner,1);var cow=target(helper,owner,EntityType.COW,.5);var zombie=target(helper,owner,EntityType.ZOMBIE,1.5);
            var hostileWolf=target(helper,owner,EntityType.WOLF,-1.5);hostileWolf.setTarget(owner);
            var hostileHit=strike(owner,zombie);var neutralHit=strike(owner,hostileWolf);
            helper.assertTrue(hostileHit.getAmount()>10&&neutralHit.getAmount()>10&&ExoticBurnManager.isBurning(zombie)&&ExoticBurnManager.isBurning(hostileWolf),"Enemy weapon effects were suppressed");
            data(owner).setExoticFireHurtsNonHostile(true);var friendlyHit=strike(owner,cow);
            helper.assertTrue(friendlyHit.getAmount()>10&&ExoticBurnManager.isBurning(cow),"Enabled weapon effects rejected non-hostile");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void idleNeutralEnemiesAndFriendlyPiglinsRemainProtected(GameTestHelper helper){
        try(var owner=player(helper);var other=player(helper)){
            sword(owner,1);armor(owner);
            var enderman=target(helper,owner,EntityType.ENDERMAN,.5);var zombified=target(helper,owner,EntityType.ZOMBIFIED_PIGLIN,-.5);
            var piglin=target(helper,owner,EntityType.PIGLIN,1.5);var brute=target(helper,owner,EntityType.PIGLIN_BRUTE,-1.5);
            // Ordinary piglins remain neutral to friendly gold-armored players; brutes are always hostile.
            owner.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.GOLDEN_HELMET));
            SpiritEnchantmentLogic.tick(owner,0);
            for(var neutral:java.util.List.of(enderman,zombified,piglin)){
                helper.assertTrue(!ExoticBurnManager.isBurning(neutral)&&strike(owner,neutral).getAmount()==10,"Idle neutral Enemy incorrectly treated as hostile: "+neutral.getType());
                attackTarget(neutral,other);
                helper.assertTrue(strike(owner,neutral).getAmount()==10,"Aggression at another player counted as aggression at owner");
                attackTarget(neutral,owner);
                helper.assertTrue(strike(owner,neutral).getAmount()>10&&ExoticBurnManager.isBurning(neutral),"Neutral actively attacking owner was protected: "+neutral.getType());
                ExoticBurnManager.clear(neutral);
            }
            helper.assertTrue(ExoticBurnManager.isBurning(brute),"Piglin brute was incorrectly protected as ordinary piglin");
            piglin.setTarget(null);piglin.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET,owner);
            helper.assertTrue(strike(owner,piglin).getAmount()>10,"Brain-only piglin attack target was missed");
            helper.succeed();
        }
    }
    @GameTest(template="empty") public static void persistentNeutralAngerTargetsOnlyItsOwner(GameTestHelper helper){
        try(var owner=player(helper);var other=player(helper)){
            sword(owner,1);var zombified=target(helper,owner,EntityType.ZOMBIFIED_PIGLIN,.5);
            zombified.setTarget(null);zombified.setRemainingPersistentAngerTime(200);zombified.setPersistentAngerTarget(other.getUUID());
            helper.assertTrue(strike(owner,zombified).getAmount()==10&&!ExoticBurnManager.isBurning(zombified),"Foreign persistent anger burned protected neutral");
            zombified.setPersistentAngerTarget(owner.getUUID());
            helper.assertTrue(strike(owner,zombified).getAmount()>10&&ExoticBurnManager.isBurning(zombified),"Owner-specific persistent anger was ignored");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void ownPlayerAndBannerServantsRemainExcludedEvenWhenEnabled(GameTestHelper helper){
        try(var owner=player(helper);var other=player(helper)){
            sword(owner,1);armor(owner);data(owner).setExoticFireHurtsNonHostile(true);var servant=target(helper,owner,EntityType.ZOMBIE,.5);servant(servant,owner);
            var body=cn.blockforge.generated.generatedmod.registry.ModEntities.SOUL_BODY.get().create(helper.getLevel());
            helper.assertTrue(body!=null,"Missing soul body type");body.setOwner(owner.getUUID());body.setPos(owner.getX()-.5,owner.getY(),owner.getZ());helper.getLevel().addFreshEntity(body);
            var selfHit=strike(owner,owner);var playerHit=strike(owner,other);var servantHit=strike(owner,servant);var bodyHit=strike(owner,body);SpiritEnchantmentLogic.tick(owner,0);
            helper.assertTrue(selfHit.getAmount()==10&&playerHit.getAmount()==10&&servantHit.getAmount()==10&&bodyHit.getAmount()==10,"Base excluded target received weapon amplification");
            helper.assertTrue(!ExoticBurnManager.isBurning(owner)&&!ExoticBurnManager.isBurning(other)&&!ExoticBurnManager.isBurning(servant)&&!ExoticBurnManager.isBurning(body),"Base excluded target received aura or weapon burn");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void lightningArcsStillExcludePassiveAnimalsAndBannerServants(GameTestHelper helper){
        try(var owner=player(helper)){
            var struck=target(helper,owner,EntityType.ZOMBIE,.5);var cow=target(helper,owner,EntityType.COW,1.5);
            var servant=target(helper,owner,EntityType.ZOMBIE,-.5);servant(servant,owner);var enemy=target(helper,owner,EntityType.ZOMBIE,-1.5);
            var neutralEnemy=target(helper,owner,EntityType.ENDERMAN,2.5);
            float cowHealth=cow.getHealth(),servantHealth=servant.getHealth(),enemyHealth=enemy.getHealth(),neutralHealth=neutralEnemy.getHealth();
            try{
                var arc=SpiritEnchantmentLogic.class.getDeclaredMethod("arcLightning",net.minecraft.server.level.ServerPlayer.class,LivingEntity.class,float.class);
                arc.setAccessible(true);arc.invoke(null,owner,struck,3F);
            }catch(ReflectiveOperationException error){throw new AssertionError("Arc regression entry failed",error);}
            helper.assertTrue(cow.getHealth()==cowHealth&&servant.getHealth()==servantHealth&&enemy.getHealth()<enemyHealth&&neutralEnemy.getHealth()==neutralHealth,"Lightning arc harmed excluded targets or missed enemy");helper.succeed();
        }
    }
    @GameTest(template="empty") public static void domainIgnitionKeepsItsIndependentTargetRules(GameTestHelper helper){
        try(var owner=player(helper)){
            var cow=target(helper,owner,EntityType.COW,.5);SpiritEnchantmentLogic.igniteByDomain(owner,cow,1);
            helper.assertTrue(ExoticBurnManager.isBurning(cow),"Wrapping preference leaked into domain ignition");helper.succeed();
        }
    }
    private ExoticFireTargetingGameTests(){}
}
