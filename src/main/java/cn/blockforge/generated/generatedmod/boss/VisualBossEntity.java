package cn.blockforge.generated.generatedmod.boss;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Existing model IDs serve previews, bounded challenge combat and session-owned echoes. */
public final class VisualBossEntity extends PathfinderMob implements BossVisualEntity {
    private static final EntityDataAccessor<Boolean> COMBAT = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> BAR_ID = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> PREVIEW = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> UNTIL = SynchedEntityData.defineId(VisualBossEntity.class, EntityDataSerializers.LONG);
    private final BossVisualDefinition definition;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final Set<ServerPlayer> tracking = new HashSet<>();
    private final ServerBossEvent bar;
    private cn.blockforge.generated.generatedmod.boss.combat.BossCombatController combat;

    public void beginChallenge(ServerPlayer owner, net.minecraft.core.BlockPos center, UUID session) {
        if(level().isClientSide)return;
        stopCombat();
        entityData.set(COMBAT,true);
        setNoAi(true);setNoGravity(true);setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        combat=new cn.blockforge.generated.generatedmod.boss.combat.BossCombatController(this,owner,center,session);
    }
    public cn.blockforge.generated.generatedmod.boss.combat.BossCombatController combat() { return combat; }
    public int combatPhase() { return entityData.get(PHASE); }
    public void combatPhase(int value) { entityData.set(PHASE,value); }
    public void stopCombat() { if(combat!=null){combat.close();combat=null;} }
    public boolean beginCombat(ServerPlayer owner) {
        if(level().isClientSide||!isAlive()||owner==null||!owner.isAlive()||owner.isSpectator()
                ||owner.level()!=level()||isCombatEcho()||getPersistentData().hasUUID("MindscapeSession")
                ||cn.blockforge.generated.generatedmod.GeneratedMod.data(owner)==null
                ||cn.blockforge.generated.generatedmod.GeneratedMod.data(owner).wuxingxiantu$getMindscapeData().hasUUID("session"))return false;
        stopCombat();entityData.set(COMBAT,true);
        getPersistentData().putBoolean("BossPreviewOnly",false);
        setNoAi(true);setNoGravity(true);getNavigation().stop();setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        combat=new cn.blockforge.generated.generatedmod.boss.combat.BossCombatController(this,owner,
                net.minecraft.core.BlockPos.containing(getX(),getY()-1,getZ()),UUID.randomUUID(),false);
        return true;
    }
    public boolean previewOnly() {
        if(level().isClientSide||isCombatEcho()||getPersistentData().hasUUID("MindscapeSession"))return false;
        stopCombat();entityData.set(COMBAT,false);getPersistentData().putBoolean("BossPreviewOnly",true);
        setNoAi(true);setNoGravity(definition.floating());setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        return true;
    }
    public boolean isCombatEcho() { return getPersistentData().hasUUID("BossEchoParent"); }

    public VisualBossEntity(EntityType<? extends VisualBossEntity> type, Level level, String id) {
        super(type, level);
        definition = BossVisualRegistry.get(id);
        bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        if (!level.isClientSide) entityData.set(BAR_ID, Optional.of(bar.getId()));
        setPersistenceRequired();
        setCanPickUpLoot(false);
        if (definition.floating()) {
            setNoGravity(true);
            moveControl = new FlyingMoveControl(this, 20, true);
            FlyingPathNavigation flying = new FlyingPathNavigation(this, level);
            flying.setCanFloat(true);
            navigation = flying;
            goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 0.6D));
        } else {
            goalSelector.addGoal(0, new FloatGoal(this));
            goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        }
        refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.FLYING_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COMBAT,true);
        entityData.define(PHASE,1);
        entityData.define(BAR_ID, Optional.empty());
        entityData.define(PREVIEW, "");
        entityData.define(START, 0L);
        entityData.define(UNTIL, 0L);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if(combat!=null&&isAlive())combat.tick();
        if(isCombatEcho()){bar.removeAllPlayers();bar.setVisible(false);return;}
        if(combat!=null&&combat.closed()) {
            stopCombat();setNoAi(false);setNoGravity(definition.floating());
        }
        if(combat==null&&isAlive()&&!isVisualTest()&&!isNoAi()&&!getPersistentData().hasUUID("MindscapeSession")&&tickCount%20==0) {
            var level=(net.minecraft.server.level.ServerLevel)level();
            ServerPlayer nearest=null;double nearestDistance=24*24;
            for(ServerPlayer candidate:level.players()) {
                var data=cn.blockforge.generated.generatedmod.GeneratedMod.data(candidate);
                if(!candidate.isAlive()||candidate.isCreative()||candidate.isSpectator()||data==null||data.wuxingxiantu$getMindscapeData().hasUUID("session"))continue;
                double distance=distanceToSqr(candidate);
                if(distance<nearestDistance&&hasLineOfSight(candidate)){nearest=candidate;nearestDistance=distance;}
            }
            if(nearest!=null)beginCombat(nearest);
        }
        bar.setName(combat==null?getDisplayName():getDisplayName().copy().append(" · "+switch(combatPhase()){case 2->"II";case 3->"III";default->"I";}));
        bar.setProgress(Math.max(0, Math.min(1, getHealth() / getMaxHealth())));
        bar.setVisible(isAlive());
        if(tickCount%10!=0)return; // Health remains immediate; only distance membership is throttled.
        tracking.removeIf(player -> player.isRemoved() || player.level() != level());
        for (ServerPlayer player : Set.copyOf(bar.getPlayers())) {
            if (!tracking.contains(player) || !isAlive() || player.distanceToSqr(this) > 4096) bar.removePlayer(player);
        }
        for (ServerPlayer player : tracking) {
            if (isAlive() && player.distanceToSqr(this) <= 4096) bar.addPlayer(player);
        }
    }

    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); tracking.add(player); if(isAlive()&&player.distanceToSqr(this)<=4096)bar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); tracking.remove(player); bar.removePlayer(player); }
    @Override public void remove(RemovalReason reason) { stopCombat(); if (bar != null) bar.removeAllPlayers(); tracking.clear(); super.remove(reason); }
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount) {
        if(!level().isClientSide&&isCombatEcho()) {
            var parent=((net.minecraft.server.level.ServerLevel)level()).getEntity(getPersistentData().getUUID("BossEchoParent"));
            if(parent instanceof VisualBossEntity root&&root.combat()!=null)root.combat().hitEcho(this,source,amount);
            else if(parent instanceof ServerPlayer caster){var c=cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.encounter(caster);if(c!=null)c.hitEcho(this,source,amount);}
            return false;
        }
        if(!level().isClientSide&&combat==null&&!isVisualTest()&&source.getEntity() instanceof ServerPlayer player)
            beginCombat(player);
        if(combat!=null) {
            if(!combat.accepts(source))return false;
            amount=combat.incoming(source,amount);
        }
        return super.hurt(source,amount);
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.putBoolean("BossChallengeCombat",entityData.get(COMBAT));
        tag.putBoolean("BossPreviewOnly",isVisualTest());
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(COMBAT,!tag.getBoolean("BossPreviewOnly"));
        // A loaded challenge is aborted by MindscapeManager's durable session recovery.
        if(getPersistentData().hasUUID("MindscapeSession")||isVisualTest()){setNoAi(true);setNoGravity(true);}
        else if(tag.getBoolean("BossChallengeCombat")){setNoAi(false);setNoGravity(definition.floating());}
    }
    @Override protected boolean shouldDropLoot() { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return definition == null ? super.getStandingEyeHeight(pose, dimensions) : definition.eyeHeight();
    }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(definition.renderRadius()); }
    @Override public BossVisualDefinition visualDefinition() { return definition; }
    @Override public UUID bossEventId() { return entityData.get(BAR_ID).orElse(null); }
    @Override public boolean isVisualTest() { return !entityData.get(COMBAT); }
    @Override public String previewAnimation() { return entityData.get(PREVIEW); }
    @Override public long previewStarted() { return entityData.get(START); }
    @Override public long previewUntil() { return entityData.get(UNTIL); }
    @Override public void previewAnimation(String name) {
        if (level().isClientSide) return;
        double duration = definition.clips().getOrDefault(name, 0.0D);
        entityData.set(PREVIEW, duration > 0 ? name : "");
        entityData.set(START, level().getGameTime());
        entityData.set(UNTIL, level().getGameTime() + (long) Math.ceil(duration * 20));
    }
    @Override public void clientMotion(String name,long start,long until){if(level().isClientSide){entityData.set(PREVIEW,name);entityData.set(START,start);entityData.set(UNTIL,until);}}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) { BossAnimations.register(this, definition, registrar); }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
