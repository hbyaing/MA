package cn.blockforge.generated.generatedmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 七十二变的能力注册表。形态仍是客户端伪装实体，能力由服务端根据形态 ID 决定，
 * 客户端只发送一次“使用能力”意图，绝不提交伤害、坐标或传送结果。
 */
public final class TransformAbilityRegistry {
    private static final long DEFAULT_COOLDOWN = 60L;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> CREEPER_CHARGES = new HashMap<>();
    private static final Map<UUID, Long> ATTACK_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Vec3> TRANSFORM_ABILITY_MOTION = new HashMap<>();
    private static final Map<UUID, Integer> TRANSFORM_ABILITY_MOTION_TICKS = new HashMap<>();
    private static final Map<UUID, Boolean> TRANSFORM_FLIGHT = new HashMap<>();
    /** 变身期间由玩家主动命中过的目标；这些目标允许按原版规则反击。 */
    private static final Map<UUID, Set<UUID>> TRANSFORM_ATTACKED_TARGETS = new HashMap<>();
    /** 低频兜底扫描节流，避免每 tick 扫描周围实体。 */
    private static final Map<UUID, Long> LAST_FACTION_SCAN = new HashMap<>();

    private enum Faction {
        NONE,
        UNDEAD,
        ARTHROPOD,
        ILLAGER,
        AQUATIC,
        MONSTER,
        CREATURE
    }

    private TransformAbilityRegistry() {}

    public static boolean hasAbility(String formId) {
        return abilityId(formId) != null;
    }

    private static void setTransformAbilityMotion(ServerPlayer player, Vec3 motion, int ticks) {
        if (motion == null || !Double.isFinite(motion.x) || !Double.isFinite(motion.y)
                || !Double.isFinite(motion.z) || ticks <= 0) return;
        TRANSFORM_ABILITY_MOTION.put(player.getUUID(), motion);
        TRANSFORM_ABILITY_MOTION_TICKS.put(player.getUUID(), Math.min(8, ticks));
    }

    /** 形态存档和网络同步的统一白名单：必须是已注册的非玩家生物实体。 */
    public static boolean isValidFormId(String formId) {
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key == null || key.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER))) return false;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
        return type != null && type.getCategory() != net.minecraft.world.entity.MobCategory.MISC;
    }

    public static boolean isVanillaFormId(String formId) {
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key == null || !"minecraft".equals(key.getNamespace())
                || key.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER))) return false;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
        return type != null && type.getCategory() != net.minecraft.world.entity.MobCategory.MISC;
    }

    public static String abilityId(String formId) {
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key == null) return null;
        String id = key.toString();
        return switch (id) {
            case "minecraft:creeper", "minecraft:enderman", "minecraft:blaze", "minecraft:ghast",
                    "minecraft:warden", "minecraft:rabbit", "minecraft:bee", "minecraft:phantom",
                    "minecraft:wither", "minecraft:ender_dragon", "minecraft:guardian",
                    "minecraft:elder_guardian", "minecraft:shulker", "minecraft:illusioner",
            "minecraft:evoker" -> id;
            default -> null;
        };
    }

    public static long remainingCooldown(ServerPlayer player) {
        if (hasInfiniteTechniqueCooldown(player)) return 0L;
        return Math.max(0L, COOLDOWNS.getOrDefault(player.getUUID(), 0L) - player.level().getGameTime());
    }

    public static boolean hasInfiniteTechniqueCooldown(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        return data != null && data.wuxingxiantu$hasCreationBlessing();
    }

    public static boolean isCreeperCharging(ServerPlayer player) {
        return CREEPER_CHARGES.containsKey(player.getUUID());
    }

    public static boolean allowTransformAttack(ServerPlayer player) {
        if (player == null) return false;
        long now = player.level().getGameTime();
        return now >= ATTACK_COOLDOWNS.getOrDefault(player.getUUID(), 0L);
    }

    public static void markTransformAttack(ServerPlayer player) {
        ATTACK_COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + 8L);
    }

    public static boolean use(ServerPlayer player, String formId) {
        String active = activeArt(player);
        // 七十二变和移魂寄魄附身都允许使用已校验宿主形态的生灵技能。
        if (!"qi_shi_er_bian".equals(active) && !"yi_hun_ji_po".equals(active)) return false;
        CultivationData data = GeneratedMod.data(player);
        String expectedForm = DivineArtLogic.activeAbilityForm(player);
        if (data == null || formId == null || !formId.equals(expectedForm)
                || !Arrays.asList(data.wuxingxiantu$getPossessedForms()).contains(formId)) return false;
        String ability = abilityId(formId);
        if (ability == null || remainingCooldown(player) > 0L || isCreeperCharging(player)) return false;
        LivingEntity actor = DivineArtLogic.possessedTarget(player);
        LivingEntity source = actor == null ? player : actor;
        boolean used = switch (ability) {
            case "minecraft:creeper" -> creeperChargeStart(player, source);
            case "minecraft:enderman" -> endermanTeleport(player, source);
            case "minecraft:blaze" -> blazeFireball(source);
            case "minecraft:ghast" -> ghastFireball(source);
            case "minecraft:warden" -> wardenSonicBoom(source);
            case "minecraft:wither" -> witherSkull(source);
            case "minecraft:ender_dragon" -> dragonFireball(source);
            case "minecraft:rabbit" -> rabbitLeap(source);
            case "minecraft:bee" -> beeDash(source);
            case "minecraft:phantom" -> phantomBurst(player, source);
            case "minecraft:guardian" -> guardianBeam(source, false);
            case "minecraft:elder_guardian" -> guardianBeam(source, true);
            case "minecraft:shulker" -> shulkerBullet(source);
            case "minecraft:illusioner" -> illusionerSpell(source);
            case "minecraft:evoker" -> evokerSkill(source);
            default -> false;
        };
        if (used && !hasInfiniteTechniqueCooldown(player)
                && !"minecraft:creeper".equals(ability)) {
            COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + cooldownFor(ability));
        }
        return used;
    }

    /** 在七十二变激活期间施加不需要额外 Mixin 的生物被动能力。 */
    public static void tickPassive(ServerPlayer player, String formId) {
        tickAbilityMotion(player);
        tickCreeperCharge(player);
        if (!"qi_shi_er_bian".equals(activeArt(player))) return;
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key == null) return;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
        if (type == null) return;
        boolean flying = type == EntityType.BAT || type == EntityType.BEE || type == EntityType.PARROT
                || type == EntityType.PHANTOM || type == EntityType.BLAZE || type == EntityType.GHAST
                || type == EntityType.VEX || type == EntityType.ALLAY || type == EntityType.ENDER_DRAGON
                || type == EntityType.WITHER;
        if (flying) {
            if (!TRANSFORM_FLIGHT.containsKey(player.getUUID())) {
                TRANSFORM_FLIGHT.put(player.getUUID(), player.getAbilities().mayfly);
            }
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        } else {
            // C 切换到非飞行形态时立即撤销上一形态授予的飞行，避免技能状态串形态。
            Boolean hadMayFly = TRANSFORM_FLIGHT.remove(player.getUUID());
            if (hadMayFly != null && !hadMayFly && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        if (isDrowningImmune(formId)) player.setAirSupply(player.getMaxAirSupply());
        tickFaction(player, formId);
    }

    private static void tickAbilityMotion(ServerPlayer player) {
        int ticks = TRANSFORM_ABILITY_MOTION_TICKS.getOrDefault(player.getUUID(), 0);
        Vec3 motion = TRANSFORM_ABILITY_MOTION.get(player.getUUID());
        if (ticks <= 0 || motion == null) {
            TRANSFORM_ABILITY_MOTION.remove(player.getUUID());
            TRANSFORM_ABILITY_MOTION_TICKS.remove(player.getUUID());
            return;
        }
        player.setDeltaMovement(motion);
        player.hasImpulse = true;
        TRANSFORM_ABILITY_MOTION.put(player.getUUID(), motion.scale(0.82D));
        TRANSFORM_ABILITY_MOTION_TICKS.put(player.getUUID(), ticks - 1);
    }

    private static void tickCreeperCharge(ServerPlayer player) {
        if (!CREEPER_CHARGES.containsKey(player.getUUID())) return;
        CultivationData data = GeneratedMod.data(player);
        String active = activeArt(player);
        String form = DivineArtLogic.activeAbilityForm(player);
        boolean possession = "yi_hun_ji_po".equals(active);
        if (data == null || (!"qi_shi_er_bian".equals(active) && !possession)
                || !"minecraft:creeper".equals(form)) {
            CREEPER_CHARGES.remove(player.getUUID());
            return;
        }
        // 七十二变：长按 Y 维持膨胀，只有收到松开包才引爆；附身仍走 12 tick 自动引爆。
        if (!possession) return;
        long finish = CREEPER_CHARGES.get(player.getUUID());
        if (player.serverLevel().getGameTime() < finish) {
            player.setSprinting(false);
            LivingEntity chargingSource = DivineArtLogic.possessedTarget(player);
            if (chargingSource instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                creeper.setSwellDir(1);
            }
            if (player.tickCount % 3 == 0 && player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + player.getBbHeight() * 0.6D,
                        player.getZ(), 2, 0.25D, 0.35D, 0.25D, 0.01D);
            }
            return;
        }
        // 附身模式时间到：自动引爆，不撤销苦力怕形态。
        creeperDetonate(player);
    }

    /** 返回形态对应的原版 MobType；它用于被动分类，不会把玩家变成真正的 Mob。 */
    public static MobType inheritedMobType(String formId) {
        if (isOneOf(formId, "minecraft:zombie", "minecraft:husk", "minecraft:drowned",
                "minecraft:skeleton", "minecraft:stray", "minecraft:wither_skeleton",
                "minecraft:zombie_villager", "minecraft:zombified_piglin", "minecraft:zoglin",
                "minecraft:phantom", "minecraft:skeleton_horse", "minecraft:zombie_horse",
                "minecraft:wither")) return MobType.UNDEAD;
        if (isOneOf(formId, "minecraft:spider", "minecraft:cave_spider", "minecraft:silverfish",
                "minecraft:endermite", "minecraft:bee")) return MobType.ARTHROPOD;
        if (isOneOf(formId, "minecraft:evoker", "minecraft:illusioner", "minecraft:pillager",
                "minecraft:ravager", "minecraft:vindicator", "minecraft:vex")) return MobType.ILLAGER;
        if (isOneOf(formId, "minecraft:axolotl", "minecraft:cod", "minecraft:dolphin",
                "minecraft:elder_guardian", "minecraft:glow_squid", "minecraft:guardian",
                "minecraft:pufferfish", "minecraft:salmon", "minecraft:squid", "minecraft:tadpole",
                "minecraft:tropical_fish", "minecraft:turtle")) return MobType.WATER;
        return MobType.UNDEFINED;
    }

    /** 是否处于七十二变；只接受服务端已校验并激活的形态。 */
    public static boolean isActiveTransform(ServerPlayer player) {
        return "qi_shi_er_bian".equals(activeArt(player))
                && isValidFormId(activeForm(player));
    }

    /** 当前七十二变形态，供服务端目标事件读取；无效或未激活时返回空字符串。 */
    public static String activeFormForTargeting(ServerPlayer player) {
        return isActiveTransform(player) ? activeForm(player) : "";
    }

    /** 变身玩家与指定实体是否属于同一逻辑阵营。 */
    public static boolean isAlliedWithTransform(ServerPlayer player, LivingEntity other) {
        if (!isActiveTransform(player) || other == null || other == player) return false;
        Faction transformedFaction = factionForForm(activeForm(player));
        return transformedFaction != Faction.NONE && transformedFaction == factionForEntity(other);
    }

    /**
     * 过滤“主动寻找变身玩家”的目标变化；被玩家主动攻击过的目标不在此处拦截，保留原版反击。
     */
    public static boolean shouldIgnoreMobTarget(Mob mob, ServerPlayer player, String formId) {
        if (mob == null || player == null || !isActiveTransform(player)
                || formId == null || !formId.equals(activeForm(player)) || !mob.isAlive()) return false;
        // 变身只继承形态的原版逻辑阵营，不把“玩家身份”带来的主动仇恨带入阵营关系。
        // 例如僵尸形态与骷髅同属 UNDEAD，普通状态下它们不会主动锁定变身玩家；
        // 但变身牛/鱼不会因此获得对所有敌怪的全局免疫。
        return isAlliedWithTransform(player, mob) && !wasAttackedByTransformedPlayer(player, mob);
    }

    /** 记录七十二变玩家主动命中过的实体，允许该实体继续进行原版反击。 */
    public static void recordTransformedAttack(ServerPlayer player, LivingEntity target) {
        if (!isActiveTransform(player) || target == null || target == player || !target.isAlive()) return;
        TRANSFORM_ATTACKED_TARGETS.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>()).add(target.getUUID());
    }

    public static boolean wasAttackedByTransformedPlayer(ServerPlayer player, LivingEntity target) {
        return player != null && target != null
                && TRANSFORM_ATTACKED_TARGETS.getOrDefault(player.getUUID(), Set.of()).contains(target.getUUID());
    }

    /**
     * 变身玩家受到实体攻击时，通知附近同阵营生物保护它；保护者仍经过原版 canAttack 检查，
     * 不会把同阵营攻击者当成目标。环境伤害没有攻击实体，因此不会凭空制造仇恨。
     */
    public static void protectTransformedPlayer(ServerPlayer player, LivingEntity attacker) {
        if (!isActiveTransform(player) || attacker == null || attacker == player || !attacker.isAlive()
                || isAlliedWithTransform(player, attacker)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        Faction faction = factionForForm(activeForm(player));
        if (faction == Faction.NONE) return;
        for (Mob protector : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0D),
                candidate -> candidate.isAlive() && factionForEntity(candidate) == faction
                        && candidate != attacker && !candidate.isAlliedTo(attacker)
                        && candidate.canAttack(attacker))) {
            protector.setTarget(attacker);
        }
    }

    /** 七十二变开始或切换时清除旧战斗关系，并清掉同阵营 Mob 已有的玩家目标。 */
    public static void onTransformStart(ServerPlayer player, String formId) {
        clearTransformCombatState(player);
        clearFactionTargets(player, formId);
    }

    public static void onTransformChanged(ServerPlayer player, String formId) {
        clearTransformCombatState(player);
        clearFactionTargets(player, formId);
    }

    public static void onTransformStop(ServerPlayer player) {
        clearTransformCombatState(player);
    }

    /** 低频清理未经过目标事件的旧目标，防止原版 AI 在变身期间重新锁定同阵营玩家。 */
    public static void tickFaction(ServerPlayer player, String formId) {
        if (!isActiveTransform(player) || !formId.equals(activeForm(player))) return;
        long now = player.serverLevel().getGameTime();
        long last = LAST_FACTION_SCAN.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now - last < 5L) return;
        LAST_FACTION_SCAN.put(player.getUUID(), now);
        clearFactionTargets(player, formId);
    }

    private static void clearFactionTargets(ServerPlayer player, String formId) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0D),
                candidate -> candidate.isAlive() && shouldIgnoreMobTarget(candidate, player, formId))) {
            if (mob.getTarget() == player) mob.setTarget(null);
            if (mob.getLastHurtByMob() == player) mob.setLastHurtByMob(null);
            if (mob instanceof NeutralMob neutral && player.getUUID().equals(neutral.getPersistentAngerTarget())) {
                neutral.setPersistentAngerTarget(null);
                neutral.setRemainingPersistentAngerTime(0);
            }
        }
    }

    private static String activeForm(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        return data == null ? "" : data.wuxingxiantu$getActivePossessedForm();
    }

    private static Faction factionForForm(String formId) {
        MobType mobType = inheritedMobType(formId);
        if (mobType == MobType.UNDEAD) return Faction.UNDEAD;
        if (mobType == MobType.ARTHROPOD) return Faction.ARTHROPOD;
        if (mobType == MobType.ILLAGER) return Faction.ILLAGER;
        if (mobType == MobType.WATER) return Faction.AQUATIC;
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        EntityType<?> type = key == null ? null : BuiltInRegistries.ENTITY_TYPE.get(key);
        if (type == null) return Faction.NONE;
        if (type.getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) return Faction.MONSTER;
        if (type.getCategory() == net.minecraft.world.entity.MobCategory.CREATURE
                || type.getCategory() == net.minecraft.world.entity.MobCategory.AMBIENT) return Faction.CREATURE;
        return Faction.NONE;
    }

    private static Faction factionForEntity(LivingEntity entity) {
        MobType mobType = entity.getMobType();
        if (mobType == MobType.UNDEAD) return Faction.UNDEAD;
        if (mobType == MobType.ARTHROPOD) return Faction.ARTHROPOD;
        if (mobType == MobType.ILLAGER) return Faction.ILLAGER;
        if (mobType == MobType.WATER) return Faction.AQUATIC;
        if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) return Faction.MONSTER;
        if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE
                || entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.AMBIENT) return Faction.CREATURE;
        return Faction.NONE;
    }

    private static void clearTransformCombatState(ServerPlayer player) {
        cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.clear(player);
        TRANSFORM_ATTACKED_TARGETS.remove(player.getUUID());
        LAST_FACTION_SCAN.remove(player.getUUID());
    }

    public static boolean isFireImmune(String formId) {
        return isOneOf(formId, "minecraft:blaze", "minecraft:ghast", "minecraft:magma_cube",
                "minecraft:strider", "minecraft:zombified_piglin", "minecraft:wither", "minecraft:ender_dragon");
    }

    public static boolean isFallImmune(String formId) {
        return isOneOf(formId, "minecraft:chicken", "minecraft:cat", "minecraft:ocelot",
                "minecraft:parrot", "minecraft:bee", "minecraft:fox", "minecraft:goat");
    }

    public static boolean isDrowningImmune(String formId) {
        return isOneOf(formId, "minecraft:axolotl", "minecraft:cod", "minecraft:dolphin",
                "minecraft:elder_guardian", "minecraft:glow_squid", "minecraft:guardian", "minecraft:ender_dragon",
                "minecraft:pufferfish", "minecraft:salmon", "minecraft:squid", "minecraft:tadpole",
                "minecraft:tropical_fish", "minecraft:turtle");
    }

    public static void clear(ServerPlayer player) {
        COOLDOWNS.remove(player.getUUID());
        CREEPER_CHARGES.remove(player.getUUID());
        ATTACK_COOLDOWNS.remove(player.getUUID());
        TRANSFORM_ABILITY_MOTION.remove(player.getUUID());
        TRANSFORM_ABILITY_MOTION_TICKS.remove(player.getUUID());
        clearTransformCombatState(player);
        Boolean hadMayFly = TRANSFORM_FLIGHT.remove(player.getUUID());
        if (hadMayFly != null && !hadMayFly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static String activeArt(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        return data == null ? "" : data.wuxingxiantu$getActiveDivineArt();
    }

    private static long cooldownFor(String ability) {
        return switch (ability) {
            case "minecraft:creeper" -> 160L;
            case "minecraft:enderman" -> 100L;
            case "minecraft:blaze", "minecraft:ghast" -> 30L;
            case "minecraft:warden" -> 180L;
            case "minecraft:rabbit", "minecraft:bee", "minecraft:phantom" -> 45L;
            case "minecraft:guardian", "minecraft:elder_guardian" -> 40L;
            case "minecraft:shulker", "minecraft:illusioner", "minecraft:evoker" -> 60L;
            case "minecraft:wither" -> 40L;
            case "minecraft:ender_dragon" -> 100L;
            default -> DEFAULT_COOLDOWN;
        };
    }

    private static boolean isOneOf(String formId, String... values) {
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key == null) return false;
        String id = key.toString();
        for (String value : values) if (value.equals(id)) return true;
        return false;
    }

    /** 苦力怕蓄力开始：七十二变长按 Y 维持膨胀，附身则 12 tick 后自动引爆。 */
    private static boolean creeperChargeStart(ServerPlayer player, LivingEntity source) {
        if (CREEPER_CHARGES.containsKey(player.getUUID())) return false;
        boolean possession = "yi_hun_ji_po".equals(activeArt(player));
        CREEPER_CHARGES.put(player.getUUID(),
                possession ? player.level().getGameTime() + 12L : Long.MAX_VALUE);
        if (source instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(1);
        if (source.level() instanceof ServerLevel level) {
            level.playSound(null, source.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.SMOKE, source.getX(), source.getY() + source.getBbHeight() * 0.6D,
                    source.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.01D);
        }
        return true;
    }

    /** 苦力怕引爆：松开 Y（七十二变）或附身蓄力到时后触发；自爆后继续保持苦力怕形态。 */
    public static void creeperDetonate(ServerPlayer player) {
        if (!CREEPER_CHARGES.containsKey(player.getUUID())) return;
        CREEPER_CHARGES.remove(player.getUUID());
        LivingEntity source = DivineArtLogic.possessedTarget(player);
        if (source == null) source = player;
        if (source.level() instanceof ServerLevel level) {
            level.explode(player, source.getX(), source.getY(), source.getZ(), 3.0F, false, ExplosionInteraction.MOB);
            level.playSound(null, source.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + cooldownFor("minecraft:creeper"));
        if (source instanceof net.minecraft.world.entity.monster.Creeper creeper) creeper.setSwellDir(-1);
        // 自爆后保持苦力怕形态：不调用 DivineArtLogic.stop，让玩家继续以苦力怕形态存在。
    }

    private static boolean witherSkull(LivingEntity source) {
        Vec3 direction = source.getLookAngle().normalize();
        WitherSkull skull = new WitherSkull(source.level(), source, direction.x, direction.y, direction.z);
        skull.setPos(source.getX() + direction.x * 1.4D, source.getEyeY() + direction.y * 1.4D,
                source.getZ() + direction.z * 1.4D);
        source.level().addFreshEntity(skull);
        net.minecraft.sounds.SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                new ResourceLocation("minecraft", "entity.wither.shoot"));
        if (sound != null) source.playSound(sound, 1.0F, 1.0F);
        return true;
    }

    private static boolean dragonFireball(LivingEntity source) {
        Vec3 direction = source.getLookAngle().normalize();
        DragonFireball fireball = new DragonFireball(source.level(), source, direction.x, direction.y, direction.z);
        fireball.setPos(source.getX() + direction.x * 3.0D, source.getEyeY() + direction.y * 3.0D,
                source.getZ() + direction.z * 3.0D);
        source.level().addFreshEntity(fireball);
        net.minecraft.sounds.SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(
                new ResourceLocation("minecraft", "entity.ender_dragon.growl"));
        if (sound != null) source.playSound(sound, 1.0F, 1.0F);
        return true;
    }

    private static boolean endermanTeleport(ServerPlayer player, LivingEntity actor) {
        Vec3 look = actor.getLookAngle().normalize();
        double distance = 8.0D + player.getRandom().nextDouble() * 8.0D;
        Vec3 destination = actor.position().add(look.scale(distance));
        boolean success = actor.randomTeleport(destination.x, destination.y, destination.z, true);
        if (success) {
            actor.level().playSound(null, actor.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        return success;
    }

    private static boolean blazeFireball(LivingEntity source) {
        Vec3 direction = source.getLookAngle().normalize();
        SmallFireball fireball = new SmallFireball(source.level(), source,
                direction.x, direction.y, direction.z);
        fireball.setPos(source.getX() + direction.x * 1.2D, source.getEyeY() - 0.15D + direction.y * 1.2D,
                source.getZ() + direction.z * 1.2D);
        source.level().addFreshEntity(fireball);
        source.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
        return true;
    }

    private static boolean ghastFireball(LivingEntity source) {
        Vec3 direction = source.getLookAngle().normalize();
        LargeFireball fireball = new LargeFireball(source.level(), source,
                direction.x, direction.y, direction.z, 1);
        fireball.setPos(source.getX() + direction.x * 1.5D, source.getEyeY() + direction.y * 1.5D,
                source.getZ() + direction.z * 1.5D);
        source.level().addFreshEntity(fireball);
        source.playSound(SoundEvents.GHAST_SHOOT, 1.0F, 1.0F);
        return true;
    }

    private static boolean wardenSonicBoom(LivingEntity source) {
        LivingEntity target = lookTarget(source, 16.0D);
        if (target == null) return false;
        Vec3 start = source.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(start).normalize();
        if (!(source.level() instanceof ServerLevel level)) return false;
        // 先显示一条短促的声波轨迹，即使目标暂时拥有伤害免疫，也不会表现为按键无反应。
        for (int i = 1; i <= 12; i++) {
            Vec3 point = start.add(direction.scale(i * 1.25D));
            level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        boolean hurt = target.hurt(source.damageSources().sonicBoom(source), 10.0F);
        if (hurt) {
            target.knockback(1.2D, source.getX() - target.getX(), source.getZ() - target.getZ());
            source.playSound(SoundEvents.GENERIC_EXPLODE, 0.7F, 1.8F);
        }
        return true;
    }

    private static boolean rabbitLeap(LivingEntity source) {
        Vec3 look = source.getLookAngle();
        Vec3 current = source.getDeltaMovement();
        source.setDeltaMovement(current.x + look.x * 0.9D, 0.75D, current.z + look.z * 0.9D);
        source.hasImpulse = true;
        return true;
    }

    private static boolean beeDash(LivingEntity source) {
        Vec3 look = source.getLookAngle().normalize();
        source.setDeltaMovement(look.scale(1.35D));
        source.hasImpulse = true;
        return true;
    }

    private static boolean phantomBurst(ServerPlayer player, LivingEntity source) {
        Vec3 look = source.getLookAngle().normalize();
        // 附身宿主的普通移动 tick 会主动刹车；把冲刺登记到寄魄状态，确保至少完整执行
        // 一个服务端 tick，而不是刚设置速度就被 tickPossession 覆盖。
        if (DivineArtLogic.isPossessing(player)
                && DivineArtLogic.setPossessionAbilityMotion(player, look.scale(1.8D), 5)) {
            source.hasImpulse = true;
            return true;
        }
        Vec3 burst = look.scale(1.8D);
        setTransformAbilityMotion(player, burst, 5);
        source.setDeltaMovement(burst);
        source.hasImpulse = true;
        return true;
    }

    /** 守卫者灵光：对准星命中的首个生灵发射一道等距灵光并造成伤害；远古守卫者附带挖掘疲劳。 */
    private static boolean guardianBeam(LivingEntity source, boolean elder) {
        LivingEntity target = lookTarget(source, 18.0D);
        if (target == null) return false;
        if (!(source.level() instanceof ServerLevel level)) return false;
        Vec3 start = source.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(start).normalize();
        double distance = start.distanceTo(target.getEyePosition());
        // 连续灵光粒子：从眼睛沿直线铺到目标，展示守卫者的锁定光束。
        int beamSamples = Math.min(24, Math.max(6, (int) (distance * 1.4D)));
        for (int i = 1; i <= beamSamples; i++) {
            float f = (float) i / beamSamples;
            Vec3 point = start.add(direction.scale(distance * f));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        boolean hurt = target.hurt(source.damageSources().indirectMagic(source, source), elder ? 8.0F : 5.0F);
        if (elder) target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 2), source);
        source.playSound(SoundEvents.GUARDIAN_ATTACK, 0.8F, 1.0F);
        return true;
    }

    /** 潜影贝子弹：朝准星目标射出一枚会造成漂浮的潜影贝弹。 */
    private static boolean shulkerBullet(LivingEntity source) {
        LivingEntity target = lookTarget(source, 20.0D);
        if (target == null) return false;
        ShulkerBullet bullet = new ShulkerBullet(source.level(), source, target, Direction.Axis.Y);
        bullet.setPos(source.getX(), source.getEyeY(), source.getZ());
        source.level().addFreshEntity(bullet);
        source.playSound(SoundEvents.SHULKER_SHOOT, 0.8F, 1.0F);
        return true;
    }

    /** 幻术师咒术：对视线目标施放致盲与迟缓的失落咒语。 */
    private static boolean illusionerSpell(LivingEntity source) {
        LivingEntity target = lookTarget(source, 16.0D);
        if (target == null) return false;
        boolean blinded = target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0), source);
        boolean slowed = target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1), source);
        if (!blinded && !slowed) return false;
        if (source.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getEyeY(), target.getZ(),
                    12, 0.3D, 0.2D, 0.3D, 0.02D);
        }
        source.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0F, 1.0F);
        return true;
    }

    /** 唤魔者咒术：沿视线朝目标铺一排唤魔尖牙，咬合造成范围伤害。 */
    private static boolean evokerSkill(LivingEntity source) {
        LivingEntity target = lookTarget(source, 16.0D);
        if (target == null) return false;
        if (!(source.level() instanceof ServerLevel level)) return false;
        Vec3 start = source.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(start).normalize();
        double distance = start.distanceTo(target.getEyePosition());
        int count = Math.min(5, Math.max(3, (int) Math.round(distance / 1.2D)));
        float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        for (int i = 1; i <= count; i++) {
            float f = (float) i / (count + 1);
            Vec3 point = start.add(direction.scale(distance * f));
            net.minecraft.world.entity.projectile.EvokerFangs fangs =
                    new net.minecraft.world.entity.projectile.EvokerFangs(
                            source.level(), point.x, point.y, point.z, yaw, 20, source);
            level.addFreshEntity(fangs);
        }
        net.minecraft.sounds.SoundEvent cast = BuiltInRegistries.SOUND_EVENT.get(
                new ResourceLocation("minecraft", "entity.evoker.cast_spell"));
        if (cast != null) source.playSound(cast, 1.0F, 1.0F);
        return true;
    }

    private static LivingEntity lookTarget(LivingEntity source, double range) {
        Vec3 start = source.getEyePosition();
        Vec3 end = start.add(source.getLookAngle().normalize().scale(range));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(source, start, end,
                source.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living != source && living.isAlive(), range * range);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }
}
