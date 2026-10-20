package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.GuandingItem;
import cn.blockforge.generated.generatedmod.items.SoulCapturePearlItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class CombatEvents {

    @SubscribeEvent public static void livingDeath(LivingDeathEvent event) {
        // Mindscape encounters settle their own durable progress; projections never yield world loot.
        if(!(event.getEntity() instanceof Player)
                &&event.getEntity().level().dimension()==cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.DIMENSION)return;
        if (cn.blockforge.generated.generatedmod.boss.BossTestCommands.isVisualTest(event.getEntity())) {
            LivingEntity testEntity = event.getEntity();
            if (!testEntity.level().isClientSide) {
                DivineArtLogic.onPossessedEntityDeath(testEntity);
                if (cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(testEntity)) {
                    cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.onServantDeath(testEntity);
                }
            }
            return;
        }
        TribulationManager.onLivingDeath(event);
        SpiritEnchantmentLogic.applyNirvanaKillHeal(event);
        SpiritEnchantmentLogic.applySpiritDrain(event);
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        DivineArtLogic.onPossessedEntityDeath(entity);
        if (cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(entity)) {
            cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.onServantDeath(entity);
        }
        if (entity instanceof ServerPlayer player) {
            GuandingItem.interrupt(player);
            CultivationData data = data(player);
            if (data != null) {
                data.wuxingxiantu$setDaysSinceDeath(0);
                data.wuxingxiantu$setDaoXin(3);
                CultivationBlock.stopMeditation(player, data, false);
                DivineArtLogic.clear(player);
                sync(player);
            }
        } else {
            if (entity.getRandom().nextInt(20) == 0) entity.spawnAtLocation(new ItemStack(WuXingItems.AURA_CRYSTAL.get()));
            ServerPlayer killer = resolveKiller(entity, event.getSource());
            if (killer != null && !(entity instanceof Player)
                    && !cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(entity)) {
                CultivationData killerData = data(killer);
                if (killerData != null) {
                    long meritChange = meritChangeForKill(entity);
                    if (killerData.wuxingxiantu$hasCreationBlessing()) {
                        // 创造丹的无限功德不会因击杀善类而减少，也不会被 long 溢出破坏。
                        killerData.wuxingxiantu$setMerit(Long.MAX_VALUE);
                        meritChange = 0L;
                    } else if (meritChange != 0L) {
                        killerData.wuxingxiantu$setMerit(saturatedAdd(killerData.wuxingxiantu$getMerit(), meritChange));
                    }
                    long cultivationReward = CultivationLogic.killCultivationReward(killerData, entity);
                    float manaReward = CultivationLogic.killManaReward(killerData, entity);
                    long cultivationBefore=killerData.wuxingxiantu$getCultivation();
                    CultivationLogic.gainCultivationAndAura(killer, killerData, cultivationReward, manaReward);
                    cultivationReward=killerData.wuxingxiantu$getCultivation()-cultivationBefore;
                    var rewardMessage = Component.translatable("message.wuxingxiantu.kill_reward",
                            cultivationReward, Math.round(manaReward));
                    if (meritChange != 0L) rewardMessage.append(Component.literal(" | ")).append(Component.translatable(
                            meritChange > 0L ? "message.wuxingxiantu.merit_gain" : "message.wuxingxiantu.merit_loss",
                            Math.abs(meritChange), killerData.wuxingxiantu$getMerit()));
                    killer.displayClientMessage(rewardMessage, true);
                    if (entity instanceof Animal) {
                        long now = killer.level().getGameTime();
                        if (now > killerData.wuxingxiantu$getAnimalKillWindow()) {
                            killerData.wuxingxiantu$setAnimalKills(0);
                            killerData.wuxingxiantu$setAnimalKillWindow(now + 12000L);
                        }
                        killerData.wuxingxiantu$setAnimalKills(killerData.wuxingxiantu$getAnimalKills() + 1);
                        if (killerData.wuxingxiantu$getAnimalKills() >= 20) {
                            killerData.wuxingxiantu$setDaoXin(2);
                            killer.displayClientMessage(Component.translatable("message.wuxingxiantu.dao_dusty"), true);
                            killerData.wuxingxiantu$setAnimalKills(0);
                            killerData.wuxingxiantu$setAnimalKillWindow(now + 12000L);
                        }
                    }
                    if (killer.getRandom().nextInt(10) == 0) {
                        int tier = YaoshouDropRules.coreIndex(entity,
                                killerData.wuxingxiantu$getRealm(), WuXingItems.INNER_CORES.length);
                        if (tier >= 0) entity.spawnAtLocation(new ItemStack(WuXingItems.INNER_CORES[tier].get()));
                    }
                    // 动物等零经验生物不会触发经验掉落事件，这里补掉一颗 1 点灵球，
                    // 保证玩家击杀任意生物都能看到蓝色灵力球（此前只掉怪物灵球，测试世界几乎见不到）。
                    if (entity instanceof net.minecraft.world.entity.Mob && entity.getExperienceReward() <= 0) {
                        SpiritOrbEntity.award(killer.serverLevel(), entity.position(), 1);
                    }
                    sync(killer);
                }
            }
        }
    }

    @SubscribeEvent public static void replaceMonsterXpWithSpiritOrb(
            net.minecraftforge.event.entity.living.LivingExperienceDropEvent event) {
        LivingEntity dead = event.getEntity();
        if (cn.blockforge.generated.generatedmod.boss.BossTestCommands.isVisualTest(dead)) return;
        if (dead.level().isClientSide || dead instanceof Player) return;
        Player attacker = event.getAttackingPlayer();
        if (!(attacker instanceof ServerPlayer killer)) return;
        int amount = event.getDroppedExperience();
        if (amount <= 0) return;
        // 灵力球与原版经验球共存：不再清零原版经验，只额外生成等值灵力球。
        SpiritOrbEntity.award(killer.serverLevel(), dead.position(), amount);
    }

    private static ServerPlayer resolveKiller(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source) {
        Entity attacker = source == null ? null : source.getEntity();
        if (attacker instanceof ServerPlayer player) return player;
        if (attacker instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) return player;

        Entity direct = source == null ? null : source.getDirectEntity();
        if (direct instanceof ServerPlayer player) return player;
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) return player;

        if (victim.getKillCredit() instanceof ServerPlayer player) return player;
        if (victim.getLastHurtByMob() instanceof ServerPlayer player) return player;
        return null;
    }

    private static long meritChangeForKill(LivingEntity entity) {
        if (cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(entity)) return 0L;
        if (entity instanceof Enemy) return 1L;
        if (entity instanceof AbstractVillager) return -5L;
        return 0L;
    }

    private static long saturatedAdd(long value, long change) {
        if (change > 0L && value > Long.MAX_VALUE - change) return Long.MAX_VALUE;
        if (change < 0L && value < Long.MIN_VALUE - change) return Long.MIN_VALUE;
        return value + change;
    }

    @SubscribeEvent public static void livingHurt(LivingHurtEvent event) {
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        // 元素体质倍率先于异火缠绕结算，不施加通用境界伤害压制。
        applyPhysiqueElementalDamage(event);
        if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                &&event.getSource().getEntity() instanceof ServerPlayer fireCaster) {
            event.setAmount(event.getAmount()*cn.blockforge.generated.generatedmod.balance.HeartMethodBalance.fireDamageMultiplier(
                    ProgressionGate.effectiveHeartMethod(fireCaster,data(fireCaster))));
        }
        SpiritEnchantmentLogic.applyExoticFireStrike(event);
    }

    private static void applyPhysiqueElementalDamage(LivingHurtEvent event) {
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        if (attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() != null) attacker = projectile.getOwner();
        if (!(attacker instanceof Player player)) return;
        CultivationData attackerData = data(player);
        if (attackerData == null) return;
        PhysiqueType type = attackerData.wuxingxiantu$getPhysique();
        if ((source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) && type == PhysiqueType.TAIYANG)
                || (source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE) && type == PhysiqueType.TAIYIN)) {
            event.setAmount(event.getAmount() * 1.15F);
        }
    }

@SubscribeEvent public static void livingHeal(LivingHealEvent event) {
        SpiritEnchantmentLogic.reduceNetherHealing(event);
    }

    @SubscribeEvent public static void livingDamage(LivingDamageEvent event) {
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (TimeStopManager.blocksDamage(event.getEntity(), event.getSource().getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity().getPersistentData().getBoolean("wuxingxiantu_tribulation_strike")) return;
        if (cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.isHeartTrue(event.getSource())) {
            if(event.getEntity() instanceof ServerPlayer target&&data(target)!=null&&data(target).wuxingxiantu$isMeditating())
                CultivationBlock.stopMeditation(target,data(target),false);
            return;
        }
        SpiritEnchantmentLogic.applySpiritGuard(event);
        if(event.getAmount()>0 && event.getSource().getDirectEntity() instanceof ServerPlayer caster
                &&event.getSource().getEntity()==caster && !event.getEntity().isAlliedTo(caster)) {
            int seconds=cn.blockforge.generated.generatedmod.balance.HeartMethodBalance.igniteSeconds(
                    ProgressionGate.effectiveHeartMethod(caster,data(caster)));
            if(seconds>0)event.getEntity().setSecondsOnFire(seconds);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && event.getEntity() instanceof HeartDemonEntity) {
            CultivationData attackerData = data(attacker);
            if (attackerData != null)
                event.setAmount(event.getAmount() * CultivationLogic.daoXinHeartDemonDamageMultiplier(attackerData.wuxingxiantu$getDaoXin()));
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getEntity().level().isClientSide) return;
        CultivationData data = data(player);
        if (data == null) return;
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING))
            event.setAmount(event.getAmount() * CultivationLogic.daoXinLightningMultiplier(data.wuxingxiantu$getDaoXin()));
        if (player.level().dimension() == cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.DIMENSION)
            event.setAmount(event.getAmount() * CultivationLogic.daoXinDamageTakenMultiplier(data.wuxingxiantu$getDaoXin(), true));
        // 万雷领域：天雷灵根显化期间免疫一切闪电伤害；否则闪电为木灵根玩家累积天雷淬体计数。
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING)) {
            if (CultivationLogic.isThunderDomainActive(data)) {
                event.setCanceled(true);
                // 原版闪电在伤害之外还会附加燃烧，免疫时一并清除，做到真正的“天雷不侵”。
                player.clearFire();
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.thunder_immune"), true);
                return;
            }
            // 有效雷击来源：玩家自己引来的雷（御雷法器、万雷领域、命令刷雷）不计入淬体，
            // 自然天雷或其他实体的雷击才算；境界与品质门槛由 ProgressionGate 复核。
            net.minecraft.world.entity.Entity striker = event.getSource().getEntity();
            boolean validSource = striker == null || !player.getUUID().equals(striker.getUUID());
            CultivationLogic.addThunderStrike(player, data, validSource);
        }
        if (data.wuxingxiantu$isMeditating()) CultivationBlock.stopMeditation(player, data, false);
        PhysiqueType physique = data.wuxingxiantu$getPhysique();
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                && physique == PhysiqueType.TAIYIN) event.setAmount(event.getAmount() * .80F);
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FREEZE)
                && physique == PhysiqueType.TAIYANG) event.setAmount(event.getAmount() * .80F);
        // 心法减伤按“实际生效心法”结算：境界不足的高阶心法保留学习记录但不产生效果。
        event.setAmount(event.getAmount()*cn.blockforge.generated.generatedmod.balance.HeartMethodBalance.damageTakenMultiplier(
                ProgressionGate.effectiveHeartMethod(player,data)));
        if (hasRoot(data, 2) && data.wuxingxiantu$getRootQuality(2) >= 3 && player.isUnderWater()) event.setAmount(event.getAmount() * (data.wuxingxiantu$getRootQuality(2) >= 4 ? 0.60F : 0.80F));
        // No universal mana-based death prevention; active shields use absorption.

    }

    @SubscribeEvent public static void livingAttack(LivingAttackEvent event) {
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();
        if (TimeStopManager.blocksDamage(event.getEntity(), sourceEntity) || TimeStopManager.isStopped(sourceEntity)) {
            event.setCanceled(true);
            return;
        }
        ServerPlayer transformedAttacker = sourceEntity instanceof ServerPlayer player ? player
                : directEntity instanceof ServerPlayer player ? player
                : DivineArtLogic.possessingPlayer(sourceEntity);
        if (transformedAttacker != null) {
            // 附身宿主由宿主作为伤害源，反查控制玩家；普通七十二变则直接使用玩家伤害源。
            TransformAbilityRegistry.recordTransformedAttack(transformedAttacker, event.getEntity());
        }
        if (event.getEntity() instanceof ServerPlayer player) GuandingItem.interrupt(player);
        if (event.getSource().getEntity() instanceof ServerPlayer player && isSoulProjection(player)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player) GuandingItem.interrupt(player);
        Entity attacker = event.getSource().getEntity();
        if (event.getEntity() instanceof ServerPlayer player
                && attacker instanceof LivingEntity livingAttacker
                && !isSoulProjection(player)) {
            TransformAbilityRegistry.protectTransformedPlayer(player, livingAttacker);
        }
        if (attacker != null
                && cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(attacker)
                && attacker.getPersistentData().hasUUID("HumanEmperorBannerOwner")
                && attacker.getPersistentData().getUUID("HumanEmperorBannerOwner").equals(event.getEntity().getUUID())) {
            event.setCanceled(true);
            if (attacker instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == event.getEntity()) {
                mob.setTarget(null);
            }
            return;
        }
        // A domain's own control proc must not turn its victim immune to the
        // next paid pulse. Keep all other frozen-combat restrictions intact.
        if (CultivationLogic.isFrozen(event.getEntity())
                && !(event.getSource() instanceof cn.blockforge.generated.generatedmod.combat.DomainDamageSource)
                || attacker instanceof LivingEntity livingAttacker
                && CultivationLogic.isFrozen(livingAttacker)) event.setCanceled(true);
    }

    @SubscribeEvent public static void attackEntity(AttackEntityEvent event) {
        if (TimeStopManager.isStopped(event.getEntity())) {
             event.setCanceled(true);
             return;
         }
        if (event.getEntity() instanceof ServerPlayer player
                && DivineArtLogic.isPossessing(player)) {
            // 旁观本体不应触发原版攻击；宿主攻击由 PossessionInputPacket 统一执行。
            event.setCanceled(true);
            return;
        }
        if (CultivationLogic.isFrozen(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player
                && player.getMainHandItem().getItem() instanceof SoulCapturePearlItem capture) {
            event.setCanceled(true);
            if (SoulPearlData.hasEntity(player.getMainHandItem())) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.soul_pearl_locked"), true);
            }
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CultivationData data = data(player);
    }

    @SubscribeEvent public static void fallDamage(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CultivationData data = data(player);
            if (data != null && (data.wuxingxiantu$getRealm() >= 2
                    || TransformAbilityRegistry.isFallImmune(data.wuxingxiantu$getActivePossessedForm()))) {
                event.setDistance(0.0F);
            }
        }
    }

    @SubscribeEvent public static void transformDamageImmunity(LivingAttackEvent event) {
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CultivationData data = data(player);
        if (data == null || !"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())) return;
        String form = data.wuxingxiantu$getActivePossessedForm();
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                && TransformAbilityRegistry.isFireImmune(form)) event.setCanceled(true);
        else if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_DROWNING)
                && TransformAbilityRegistry.isDrowningImmune(form)) event.setCanceled(true);
    }
    private CombatEvents() {}
}
