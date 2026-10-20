package cn.blockforge.generated.generatedmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

/** 大乘渡劫末道出现的心魔，属性取渡劫者当前值的十倍。 */
public final class HeartDemonEntity extends Monster implements cn.blockforge.generated.generatedmod.boss.BossVisualEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> VISUAL_TEST = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> BAR_ID = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> PREVIEW = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> PREVIEW_START = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> PREVIEW_UNTIL = SynchedEntityData.defineId(HeartDemonEntity.class, EntityDataSerializers.LONG);
    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animations =
            software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);
    public enum VisualPhase { PHASE_1, PHASE_2 }
    private static final double BOSS_BAR_RANGE = 64.0D;
    private static final String SERVANT_OWNER = "HumanEmperorBannerOwner";
    private static final String OWNER_NAME = "HeartDemonOwnerName";
    private UUID ownerUuid;
    private String ownerName = "";
    private int effectRefresh;
    private boolean stateInitialized;
    private boolean applyingDamage;
    private int transitionTicks;
    private int teleportCooldown;
    private int airAttackCooldown;
    private cn.blockforge.generated.generatedmod.boss.combat.BossCombatController combat;
    public cn.blockforge.generated.generatedmod.boss.combat.BossCombatController combat(){return combat;}
    private void stopCombat(){if(combat!=null){combat.close();combat=null;}}
    public void beginChallenge(ServerPlayer owner,net.minecraft.core.BlockPos center,UUID session) {
        startSkillCombat(owner,center,session,true);
    }
    private void startSkillCombat(ServerPlayer owner,net.minecraft.core.BlockPos center,UUID session,boolean bound) {
        stopCombat();setNoAi(true);getNavigation().stop();setTarget(null);
        setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        combat=new cn.blockforge.generated.generatedmod.boss.combat.BossCombatController(this,owner,center,session,bound);
    }
    // The baseline remains for previews/servants; active skill encounters scale phase-two health.
    private static final int TRANSITION_TICKS = 20;
    private static final int TELEPORT_COOLDOWN = 100;
    private static final double TELEPORT_TRIGGER = 16.0D, TELEPORT_MAX = 32.0D;
    private static final double PHASE_TWO_HEALTH = 200.0D, PHASE_TWO_ATTACK = 30.0D;
    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.translatable("hud.wuxingxiantu.heart_demon"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_20);

    public HeartDemonEntity(EntityType<HeartDemonEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCanPickUpLoot(false);
        setNoGravity(true);
        moveControl = new FlyingMoveControl(this, 20, true);
        setCustomNameVisible(true);
        bossBar.setVisible(false);
        bossBar.setDarkenScreen(true);
        bossBar.setCreateWorldFog(false);
        if (!level.isClientSide) entityData.set(BAR_ID, Optional.of(bossBar.getId()));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    public static HeartDemonEntity create(ServerLevel level, ServerPlayer owner) {
        HeartDemonEntity demon = new HeartDemonEntity(
                cn.blockforge.generated.generatedmod.registry.ModEntities.HEART_DEMON.get(), level);
        demon.bindToOwner(owner);
        demon.moveTo(owner.getX() + 2.0D, owner.getY(), owner.getZ() + 2.0D,
                owner.getYRot() + 180.0F, 0.0F);
        return demon;
    }

    /** 统一给渡劫生成和刷怪蛋生成使用，避免刷怪蛋生成的心魔没有主人数据。 */
    public void bindToOwner(ServerPlayer owner) {
        bindToOwner(owner, false);
    }

    /** 作为人皇幡魂仆绑定时，完整保留玩家效果，避免释放后几秒内效果过期。 */
    public void bindToServantOwner(ServerPlayer owner) {
        bindToOwner(owner, true);
    }

    private void bindToOwner(ServerPlayer owner, boolean persistentEffects) {
        stopCombat();setNoAi(false);
        entityData.set(VISUAL_TEST, false);
        ownerUuid = owner.getUUID();
        entityData.set(OWNER, Optional.of(ownerUuid));
        ownerName = owner.getGameProfile().getName();
        stateInitialized = false;
        effectRefresh = 0;
        if (visualPhase() == VisualPhase.PHASE_1) copyPlayerState(owner, persistentEffects);
        updateDisplayName();
    }

    private void updateDisplayName() {
        Component displayName = visualPhase() == VisualPhase.PHASE_2
                ? Component.translatable("entity.wuxingxiantu.heart_demon_phase2")
                : Component.translatable("entity.wuxingxiantu.heart_demon_phase1");
        setCustomName(displayName);
        bossBar.setName(displayName);
    }

    /** 客户端七十二变伪实体绑定当前玩家，用于复用玩家皮肤和名称。 */
    public void bindClientOwner(UUID ownerId, String name) {
        if (ownerId == null) return;
        ownerUuid = ownerId;
        entityData.set(OWNER, Optional.of(ownerId));
        ownerName = name == null ? "" : name;
        updateDisplayName();
    }

    /** 保存给人皇幡显示和重载世界使用的原始玩家名称。 */
    public String ownerName() {
        return ownerName;
    }

    public UUID ownerUuid() {
        if (ownerUuid != null) return ownerUuid;
        UUID synced = entityData.get(OWNER).orElse(null);
        if (synced != null) return synced;
        return getPersistentData().hasUUID(SERVANT_OWNER)
                ? getPersistentData().getUUID(SERVANT_OWNER) : null;
    }

    public Player owner() {
        UUID id = ownerUuid();
        if (id == null) return null;
        if (level() instanceof ServerLevel level) return level.getServer().getPlayerList().getPlayer(id);
        return null;
    }

    private boolean isSoulServant() {
        return HumanEmperorBannerItem.isServant(this);
    }

    public boolean isOwner(Entity entity) {
        return entity != null && ownerUuid() != null && ownerUuid().equals(entity.getUUID());
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.FLYING_SPEED, 0.4D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER, Optional.empty());
        entityData.define(PHASE, 1);
        entityData.define(VISUAL_TEST, false);
        entityData.define(BAR_ID, Optional.empty());
        entityData.define(PREVIEW, "");
        entityData.define(PREVIEW_START, 0L);
        entityData.define(PREVIEW_UNTIL, 0L);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isVisualTest()) return false;
        if (target == null || target instanceof HeartDemonEntity) return false;
        if (isSoulServant() && isOwner(target)) return false;
        return super.canAttack(target);
    }

    @Override
    public void tick() {
        if (isDeadOrDying() || !isAlive()) {
            bossBar.setVisible(false);
            bossBar.removeAllPlayers();
        }
        if (level().isClientSide) {
            super.tick();
            return;
        }
        if (transitionTicks > 0) {
            transitionTicks--;
            setTarget(null);
            getNavigation().stop();
            setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            super.tick();
            updateBossBar();
            return;
        }
        if (teleportCooldown > 0) teleportCooldown--;
        if (airAttackCooldown > 0) airAttackCooldown--;
        if (isVisualTest()) {
            if (ownerUuid() == null && level().getNearestPlayer(this, 64.0D) instanceof ServerPlayer player) bindVisualOwner(player);
            setTarget(null);
            super.tick();
            updateBossBar();
            return;
        }
        if (ownerUuid() == null && level().getNearestPlayer(this, BOSS_BAR_RANGE) instanceof ServerPlayer player)
            bindToOwner(player);
        Player owner = owner();
        if (owner != null && (!owner.isAlive() || owner.level() != level())) owner = null;
        if(combat!=null&&(isSoulServant()||isVisualTest())){stopCombat();setNoAi(false);}
        if(combat==null&&owner instanceof ServerPlayer server&&!isSoulServant()&&!isNoAi()
                &&!server.isSpectator()&&server.distanceToSqr(this)<=32*32
                &&!getPersistentData().hasUUID("MindscapeSession")&&GeneratedMod.data(server)!=null
                &&!GeneratedMod.data(server).wuxingxiantu$getMindscapeData().hasUUID("session"))
            startSkillCombat(server,net.minecraft.core.BlockPos.containing(getX(),getY()-1,getZ()),UUID.randomUUID(),false);
        if(combat!=null) {
            // The scripted encounter owns attacks and movement, so melee AI cannot hit during a telegraph.
            setTarget(null);getNavigation().stop();setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            if(owner!=null&&visualPhase()==VisualPhase.PHASE_1&&effectRefresh--<=0){effectRefresh=20;copyPlayerState(owner,false);}
            var encounter=combat;
            super.tick();
            if(isAlive()&&!isRemoved()&&combat==encounter)encounter.tick();
            if(combat==encounter&&encounter.closed()){stopCombat();setNoAi(false);}
            updateBossBar();return;
        }
        if (owner != null) {
            if (isSoulServant()) {
                LivingEntity ownerTarget = owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive()
                        ? owner.getLastHurtByMob() : owner.getLastHurtMob();
                if (ownerTarget != null && ownerTarget != this && ownerTarget != owner
                        && !ownerTarget.isAlliedTo(owner) && canAttack(ownerTarget)) {
                    setTarget(ownerTarget);
                } else if (getTarget() == owner || getTarget() instanceof HeartDemonEntity
                        || getTarget() != null && !getTarget().isAlive()) {
                    setTarget(null);
                }
            } else {
                // 刷怪蛋和渡劫生成的是讨伐 Boss，始终主动追击绑定玩家。
                if (canAttack(owner)) setTarget(owner);
                else setTarget(null);
            }
            if (effectRefresh-- <= 0) {
                effectRefresh = 20;
                if (!isSoulServant() && visualPhase() == VisualPhase.PHASE_1) copyPlayerState(owner, false);
            }
        } else {
            setTarget(null);
        }
        super.tick();
        if (visualPhase() == VisualPhase.PHASE_1 && !isNoAi() && isAlive()) tickMirrorMovement();
        updateBossBar();
    }

    private void updateBossBar() {
        if (isDeadOrDying() || !isAlive()) {
            bossBar.setVisible(false);
            bossBar.removeAllPlayers();
            return;
        }
        if (isSoulServant() || !isVisualTest() && ownerUuid() == null) {
            bossBar.setVisible(false);
            bossBar.removeAllPlayers();
            return;
        }
        bossBar.setVisible(true);
        bossBar.setDarkenScreen(!isVisualTest());
        bossBar.setName(visualPhase() == VisualPhase.PHASE_2 || isVisualTest()
                ? Component.translatable("boss.wuxingxiantu." + visualDefinition().id()) : getDisplayName());
        bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / Math.max(1.0F, getMaxHealth()))));
        if (!(level() instanceof ServerLevel level)) return;
        if(!isVisualTest()) {
            ServerPlayer owner=ownerUuid()==null?null:level.getServer().getPlayerList().getPlayer(ownerUuid());
            for(ServerPlayer viewer:java.util.List.copyOf(bossBar.getPlayers()))
                if(viewer!=owner||viewer.level()!=level)bossBar.removePlayer(viewer);
            if(owner!=null&&owner.level()==level)bossBar.addPlayer(owner);
            return;
        }
        if(tickCount%10!=0)return;
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            boolean visible = player.level() == level && player.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE;
            if (visible) bossBar.addPlayer(player);
            else bossBar.removePlayer(player);
        }
    }

    private void copyPlayerState(Player owner, boolean persistentEffects) {
        if (visualPhase() != VisualPhase.PHASE_1) return;
        float previousHealth = getHealth();
        float previousMaxHealth = getMaxHealth();
        AttributeInstance maxHealth = getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.setBaseValue(cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.phaseOneHealth(owner.getMaxHealth()));
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(finiteMirror(owner.getAttributeValue(Attributes.ATTACK_DAMAGE), 3.0D,
                cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.ATTACK_MULTIPLIER));
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(Math.min(1.0D, finiteMirror(owner.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.10D,
                cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.SPEED_MULTIPLIER)));
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(finiteMirror(owner.getAttributeValue(Attributes.ARMOR), 0,
                cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.ARMOR_MULTIPLIER));
        AttributeInstance toughness = getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) toughness.setBaseValue(finiteMirror(owner.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 0,
                cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.TOUGHNESS_MULTIPLIER));
        AttributeInstance knockback = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockback != null) knockback.setBaseValue(Math.min(1.0D,
                finiteMirror(owner.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0,
                        cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.KNOCKBACK_MULTIPLIER)));
        if (!stateInitialized) setHealth(getMaxHealth());
        else setHealth(Math.min(getMaxHealth(), previousHealth / Math.max(1.0F, previousMaxHealth) * getMaxHealth()));
        stateInitialized = true;

        for (MobEffectInstance current : getActiveEffects().toArray(MobEffectInstance[]::new)) {
            removeEffect(current.getEffect());
        }
        for (MobEffectInstance effect : owner.getActiveEffects()) {
            int duration = persistentEffects ? effect.getDuration() : Math.min(120, effect.getDuration());
            addEffect(new MobEffectInstance(effect.getEffect(), duration,
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND) {
                setItemSlot(slot, (slot == EquipmentSlot.MAINHAND
                        ? owner.getMainHandItem() : owner.getOffhandItem()).copy());
            } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                setItemSlot(slot, owner.getItemBySlot(slot).copy());
            }
        }
        setItemInHand(InteractionHand.MAIN_HAND, owner.getMainHandItem().copy());
        setItemInHand(InteractionHand.OFF_HAND, owner.getOffhandItem().copy());
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
    }

    private static double finiteMirror(double value, double minimum, double multiplier) {
        return cn.blockforge.generated.generatedmod.balance.HeartDemonBalance.mirror(value,minimum,multiplier);
    }

    private void tickMirrorMovement() {
        LivingEntity target = getTarget();
        if (!(level() instanceof ServerLevel server) || target == null || !target.isAlive()
                || target.level() != level() || !canAttack(target)) return;
        setNoGravity(true);
        if (distanceToSqr(target) > 4.0D && hasLineOfSight(target)) {
            getMoveControl().setWantedPosition(target.getX(), target.getY() + 0.25D, target.getZ(), 1.25D);
        }
        if (Math.abs(target.getY() - getY()) > 1.0D && distanceToSqr(target) < 6.25D
                && hasLineOfSight(target) && airAttackCooldown == 0) {
            swing(InteractionHand.MAIN_HAND);
            doHurtTarget(target);
            airAttackCooldown = 20;
        }
        double distance = distanceToSqr(target);
        if (isSoulServant() || teleportCooldown > 0 || distance < TELEPORT_TRIGGER * TELEPORT_TRIGGER
                || distance > TELEPORT_MAX * TELEPORT_MAX) return;
        teleportCooldown = TELEPORT_COOLDOWN;
        // Bounded candidates around the target; never load chunks to search for a destination.
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = Math.toRadians(target.getYRot() + 180.0D + attempt * 45.0D);
            var point = target.position().add(-Math.sin(angle) * 3.0D, 0.25D, Math.cos(angle) * 3.0D);
            if (point.distanceToSqr(position()) > TELEPORT_MAX * TELEPORT_MAX) continue;
            var box = getBoundingBox().move(point.subtract(position()));
            if (!safeMirrorLanding(server, box)) continue;
            var from = position();
            getNavigation().stop();
            teleportTo(point.x, point.y, point.z);
            setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            fallDistance = 0;
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, from.x, from.y + 1, from.z, 24, .3, .5, .3, .05);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 24, .3, .5, .3, .05);
            server.playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                    net.minecraft.sounds.SoundSource.HOSTILE, .6F, .8F);
            break;
        }
    }

    private boolean safeMirrorLanding(ServerLevel server, net.minecraft.world.phys.AABB box) {
        if (box.minY < server.getMinBuildHeight() + 2 || box.maxY >= server.getMaxBuildHeight()
                || !server.getWorldBorder().isWithinBounds(box.minX, box.minZ)
                || !server.getWorldBorder().isWithinBounds(box.maxX, box.maxZ)) return false;
        for (int x = net.minecraft.util.Mth.floor(box.minX) >> 4; x <= net.minecraft.util.Mth.floor(box.maxX) >> 4; x++)
            for (int z = net.minecraft.util.Mth.floor(box.minZ) >> 4; z <= net.minecraft.util.Mth.floor(box.maxZ) >> 4; z++)
                if (!server.hasChunk(x, z)) return false;
        return server.noCollision(this, box) && !server.containsAnyLiquid(box);
    }

    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if(combat!=null){if(!combat.accepts(source))return false;amount=combat.incoming(source,amount);}
        return (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY) || transitionTicks == 0) && super.hurt(source, amount);
    }

    @Override protected void actuallyHurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (transitionTicks > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        applyingDamage = !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
        try { super.actuallyHurt(source, amount); }
        finally { applyingDamage = false; }
    }

    @Override public void setHealth(float health) {
        // Observe final damage after armor, absorption and Forge damage hooks, before hurt() can call die().
        if (applyingDamage && health <= 0 && transitionToPhase2()) return;
        super.setHealth(health);
    }

    public boolean transitionToPhase2() {
        if (level().isClientSide || isSoulServant() || visualPhase() != VisualPhase.PHASE_1) return false;
        setPhaseData(2);
        transitionTicks = TRANSITION_TICKS;
        teleportCooldown = airAttackCooldown = effectRefresh = 0;
        stateInitialized = false;
        getNavigation().stop();
        goalSelector.getRunningGoals().forEach(net.minecraft.world.entity.ai.goal.WrappedGoal::stop);
        targetSelector.getRunningGoals().forEach(net.minecraft.world.entity.ai.goal.WrappedGoal::stop);
        setTarget(null);
        setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        setAggressive(false);
        stopUsingItem();
        swinging = false;
        swingTime = 0;
        clearFire();
        removeAllEffects();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            getAttributes().removeAttributeModifiers(getItemBySlot(slot).getAttributeModifiers(slot));
            setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        }
        for (AttributeInstance attribute : getAttributes().getSyncableAttributes()) attribute.removeModifiers();
        for (var attribute : new net.minecraft.world.entity.ai.attributes.Attribute[]{Attributes.MAX_HEALTH,
                Attributes.ATTACK_DAMAGE, Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED, Attributes.ARMOR,
                Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE, Attributes.FOLLOW_RANGE}) {
            AttributeInstance instance = getAttribute(attribute);
            if (instance != null) instance.removeModifiers();
        }
        applyPhaseTwoAttributes();
        setAbsorptionAmount(0);
        setHealth(getMaxHealth());
        deathTime = 0;
        invulnerableTime = TRANSITION_TICKS;
        if(combat!=null)combat.phaseTransition(2);
        previewAnimation("animation.xinmo_wuxiang.phase_two_unmask");
        updateBossBar();
        return true;
    }

    private void applyPhaseTwoAttributes() {
        double health=PHASE_TWO_HEALTH;
        if(combat!=null&&owner() instanceof ServerPlayer player)
            health=Math.min(5_000_000,Math.max(PHASE_TWO_HEALTH,
                    cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.projectHeartSwordDamage(player)
                            *cn.blockforge.generated.generatedmod.boss.combat.HeartDemonSkillPrograms.PROFILE.healthMultiplier()));
        setBase(Attributes.MAX_HEALTH, health);
        setBase(Attributes.ATTACK_DAMAGE, PHASE_TWO_ATTACK);
        setBase(Attributes.MOVEMENT_SPEED, .25D);
        setBase(Attributes.FLYING_SPEED, .4D);
        setBase(Attributes.ARMOR, 0);
        setBase(Attributes.ARMOR_TOUGHNESS, 0);
        setBase(Attributes.KNOCKBACK_RESISTANCE, 0);
        setBase(Attributes.FOLLOW_RANGE, 32);
        boolean floating = visualDefinition().floating();
        setNoGravity(floating);
        moveControl = floating ? new FlyingMoveControl(this, 20, true) : new net.minecraft.world.entity.ai.control.MoveControl(this);
        navigation = floating ? createNavigation(level()) : new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, level());
    }

    private void setBase(net.minecraft.world.entity.ai.attributes.Attribute attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        if (!ownerName.isEmpty()) tag.putString(OWNER_NAME, ownerName);
        tag.putInt("BossVisualPhase", entityData.get(PHASE));
        tag.putBoolean("BossVisualTest", isVisualTest());
        tag.putBoolean("MirrorInitialized", stateInitialized);
        tag.putInt("PhaseTransitionTicks", transitionTicks);
        tag.putInt("MirrorTeleportCooldown", teleportCooldown);
        tag.putBoolean("HeartDemonSkillCombat",combat!=null);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VISUAL_TEST, tag.getBoolean("BossVisualTest"));
        if(tag.getBoolean("HeartDemonSkillCombat")&&!isVisualTest())setNoAi(false);
        setPhaseData(tag.getInt("BossVisualPhase"));
        stateInitialized = tag.contains("MirrorInitialized") ? tag.getBoolean("MirrorInitialized") : tag.contains("Health");
        transitionTicks = Math.max(0, Math.min(TRANSITION_TICKS, tag.getInt("PhaseTransitionTicks")));
        teleportCooldown = Math.max(0, Math.min(TELEPORT_COOLDOWN, tag.getInt("MirrorTeleportCooldown")));
        if (visualPhase() == VisualPhase.PHASE_2) {
            float savedHealth = getHealth();
            double savedMax=getAttributeValue(Attributes.MAX_HEALTH);
            applyPhaseTwoAttributes();
            if(tag.getBoolean("HeartDemonSkillCombat"))setBase(Attributes.MAX_HEALTH,savedMax);
            setHealth(Math.min(savedHealth, getMaxHealth()));
        }
        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
            entityData.set(OWNER, Optional.of(ownerUuid));
        }
        if (tag.contains(OWNER_NAME, net.minecraft.nbt.Tag.TAG_STRING)) {
            ownerName = tag.getString(OWNER_NAME);
            updateDisplayName();
        }
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY) && transitionToPhase2()) return;
        stopCombat();
        super.die(source);
        if (isDeadOrDying()) { bossBar.setVisible(false); bossBar.removeAllPlayers(); }
    }

    @Override
    public void remove(RemovalReason reason) {
        stopCombat();
        bossBar.setVisible(false);
        bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void bindVisualOwner(ServerPlayer player) {
        stopCombat();setNoAi(false);
        entityData.set(VISUAL_TEST, true);
        ownerUuid = player.getUUID();
        entityData.set(OWNER, Optional.of(ownerUuid));
        ownerName = player.getGameProfile().getName();
        if (!stateInitialized && visualPhase() == VisualPhase.PHASE_1) copyPlayerState(player, false);
        updateDisplayName();
    }

    public VisualPhase visualPhase() { return entityData.get(PHASE) == 2 ? VisualPhase.PHASE_2 : VisualPhase.PHASE_1; }
    public void clientTransformPhase(){if(level().isClientSide&&visualPhase()!=VisualPhase.PHASE_2)setPhaseData(2);}
    @Override public void clientMotion(String name,long start,long until){if(level().isClientSide){entityData.set(PREVIEW,name);entityData.set(PREVIEW_START,start);entityData.set(PREVIEW_UNTIL,until);}}
    public void setVisualPhase(int phase) {
        if (level().isClientSide) return;
        if (phase == 2) { transitionToPhase2(); return; }
        setPhaseData(1);
        if(combat!=null)combat.phaseTransition(1);
        else setNoAi(false);
        transitionTicks = teleportCooldown = airAttackCooldown = 0;
        stateInitialized = false;
        setNoGravity(true);
        moveControl = new FlyingMoveControl(this, 20, true);
        navigation = createNavigation(level());
        Player owner = owner();
        if (owner != null && owner.level() == level()) copyPlayerState(owner, isSoulServant());
    }
    private void setPhaseData(int phase) {
        entityData.set(PHASE, phase == 2 ? 2 : 1);
        entityData.set(PREVIEW, "");
        entityData.set(PREVIEW_START, 0L);
        entityData.set(PREVIEW_UNTIL, 0L);
        refreshDimensions();
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (PHASE.equals(key)) refreshDimensions();
    }
    @Override public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        return visualPhase() == VisualPhase.PHASE_2
                ? net.minecraft.world.entity.EntityDimensions.scalable(visualDefinition().width(), visualDefinition().height())
                : super.getDimensions(pose);
    }
    @Override protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return visualDefinition().eyeHeight();
    }
    @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(visualDefinition().renderRadius());
    }
    @Override protected boolean shouldDespawnInPeaceful() { return !isVisualTest() && super.shouldDespawnInPeaceful(); }
    @Override protected boolean shouldDropLoot() { return !isVisualTest() && super.shouldDropLoot(); }
    @Override public int getExperienceReward() { return isVisualTest() ? 0 : super.getExperienceReward(); }
    @Override public cn.blockforge.generated.generatedmod.boss.BossVisualDefinition visualDefinition() {
        return cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.get(
                visualPhase() == VisualPhase.PHASE_2 ? "heart_demon_phase2" : "heart_demon_phase1");
    }
    @Override public UUID bossEventId() { return entityData.get(BAR_ID).orElse(null); }
    @Override public boolean isVisualTest() { return entityData.get(VISUAL_TEST); }
    @Override public String previewAnimation() { return entityData.get(PREVIEW); }
    @Override public long previewStarted() { return entityData.get(PREVIEW_START); }
    @Override public long previewUntil() { return entityData.get(PREVIEW_UNTIL); }
    @Override public void previewAnimation(String name) {
        if (level().isClientSide) return;
        double duration = visualDefinition().clips().getOrDefault(name, 0.0D);
        entityData.set(PREVIEW, duration > 0 ? name : "");
        entityData.set(PREVIEW_START, level().getGameTime());
        entityData.set(PREVIEW_UNTIL, level().getGameTime() + (long) Math.ceil(duration * 20));
    }
    @Override public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar registrar) {
        cn.blockforge.generated.generatedmod.boss.BossAnimations.register(this,
                cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.get("heart_demon_phase2"), registrar);
    }
    @Override public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
