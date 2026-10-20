package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** 独立于经验球的灵力掉落，避免被原版经验球合并后变成经验。 */
public class SpiritOrbEntity extends Entity {
    private static final EntityDataAccessor<Integer> VALUE = SynchedEntityData.defineId(
            SpiritOrbEntity.class, EntityDataSerializers.INT);
    private int age;
    private int health = 5;
    private int count = 1;
    private Player followingPlayer;
    /** 弹跳窗口：新掉落的灵球先散开弹跳约 0.5 秒再吸附拾取，保证客户端来得及收到生成包并显示。 */
    private int pickupDelay = 10;

    public SpiritOrbEntity(EntityType<? extends SpiritOrbEntity> type, Level level) {
        super(type, level);
    }

    public SpiritOrbEntity(Level level, double x, double y, double z, int value) {
        this(ModEntities.SPIRIT_ORB.get(), level);
        setPos(x, y, z);
        entityData.set(VALUE, Math.max(1, value));
        setYRot(random.nextFloat() * 360.0F);
        setDeltaMovement((random.nextDouble() * 0.2D - 0.1D) * 2.0D,
                random.nextDouble() * 0.4D, (random.nextDouble() * 0.2D - 0.1D) * 2.0D);
    }

    public static void award(ServerLevel level, Vec3 pos, int total) {
        while (total > 0) {
            int split = ExperienceOrb.getExperienceValue(total);
            total -= split;
            level.addFreshEntity(new SpiritOrbEntity(level, pos.x, pos.y, pos.z, split));
        }
    }

    @Override protected void defineSynchedData() { entityData.define(VALUE, 1); }
    public int getValue() { return entityData.get(VALUE); }

    public int getIcon() {
        int value = getValue();
        int[] thresholds = {3, 7, 17, 37, 73, 149, 307, 617, 1237, 2477};
        int icon = 0;
        while (icon < thresholds.length && value >= thresholds[icon]) icon++;
        return icon;
    }

    @Override public void tick() {
        super.tick();
        if (pickupDelay > 0) pickupDelay--;
        Vec3 motion = getDeltaMovement();
        if (isEyeInFluid(FluidTags.WATER)) {
            setDeltaMovement(motion.x * 0.99D, Math.min(motion.y + 0.0005D, 0.06D), motion.z * 0.99D);
        } else if (!isNoGravity()) {
            setDeltaMovement(motion.add(0, -0.03D, 0));
        }
        if (isInLava()) {
            setDeltaMovement((random.nextDouble() - random.nextDouble()) * 0.2D,
                    0.2D, (random.nextDouble() - random.nextDouble()) * 0.2D);
        }
        if (tickCount % 20 == 1 || followingPlayer == null) {
            followingPlayer = level().getNearestPlayer(this, 8.0D);
            if (!level().isClientSide && tickCount % 20 == 1) mergeNearby();
        }
        if (followingPlayer != null && (followingPlayer.isSpectator() || !followingPlayer.isAlive()
                || distanceToSqr(followingPlayer) > 64.0D)) followingPlayer = null;
        if (followingPlayer != null && pickupDelay == 0) {
            Vec3 pull = new Vec3(followingPlayer.getX() - getX(),
                    followingPlayer.getY() + followingPlayer.getEyeHeight() / 2.0D - getY(),
                    followingPlayer.getZ() - getZ());
            double distance = pull.length();
            if (distance > 0.0001D && distance < 8.0D) {
                double strength = 1.0D - distance / 8.0D;
                setDeltaMovement(getDeltaMovement().add(pull.scale(strength * strength * 0.1D / distance)));
            }
        }
        move(MoverType.SELF, getDeltaMovement());
        float friction = 0.98F;
        if (onGround()) friction = level().getBlockState(getBlockPosBelowThatAffectsMyMovement())
                .getFriction(level(), getBlockPosBelowThatAffectsMyMovement(), this) * 0.98F;
        setDeltaMovement(getDeltaMovement().multiply(friction, 0.98D, friction));
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        if (!level().isClientSide && pickupDelay == 0) {
            for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.5D))) {
                playerTouch(player);
                if (isRemoved()) break;
            }
            if (++age >= 6000) discard();
        }
    }

    private void mergeNearby() {
        for (SpiritOrbEntity other : level().getEntitiesOfClass(SpiritOrbEntity.class,
                getBoundingBox().inflate(0.5D), orb -> orb != this && !orb.isRemoved()
                        && orb.getValue() == getValue() && (orb.getId() - getId()) % 40 == 0)) {
            count += other.count;
            age = Math.min(age, other.age);
            other.discard();
        }
    }

    public void playerTouch(Player player) {
        if (level().isClientSide || isRemoved() || player.isSpectator() || !player.isAlive()) return;
        // 单独记录冷却，经验和灵力能够在同一刻拾取。
        long now = level().getGameTime();
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains("SpiritOrbPickup") && now - persistent.getLong("SpiritOrbPickup") < 2L) return;
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return;
        float maxMana = data.wuxingxiantu$getMaxMana();
        // 灵力已满时依旧吸收并消耗灵球：数值由 Math.min 封顶在上限，多出的部分当作浪费。
        data.wuxingxiantu$setCurrentMana(Math.min(maxMana, data.wuxingxiantu$getAura() + getValue()));
        persistent.putLong("SpiritOrbPickup", now);
        player.take(this, 1);
        level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.1F, (random.nextFloat() - random.nextFloat()) * 0.35F + 0.9F);
        if (player instanceof ServerPlayer serverPlayer) GeneratedMod.sync(serverPlayer);
        if (--count <= 0) discard();
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide) {
            health -= (int) amount;
            if (health <= 0) discard();
        }
        return true;
    }
    @Override public boolean isAttackable() { return false; }
    @Override public SoundSource getSoundSource() { return SoundSource.AMBIENT; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(VALUE, Math.max(1, tag.getInt("Value")));
        age = tag.getInt("Age");
        count = tag.contains("Count") ? Math.max(1, tag.getInt("Count")) : 1;
        health = tag.contains("Health") ? tag.getInt("Health") : 5;
        pickupDelay = 0;
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Value", getValue());
        tag.putInt("Age", age);
        tag.putInt("Count", count);
        tag.putInt("Health", health);
    }
}
