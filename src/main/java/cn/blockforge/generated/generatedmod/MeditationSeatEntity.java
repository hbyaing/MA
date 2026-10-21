package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** 座位位置就是坐面；模型折腿后的最低点比实体脚底高半格。 */
public final class MeditationSeatEntity extends Entity {
    private static final EntityDataAccessor<Boolean> MEDITATING=SynchedEntityData.defineId(MeditationSeatEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> UNSTABLE_ENTRY=SynchedEntityData.defineId(MeditationSeatEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ENTRY_TICKS=SynchedEntityData.defineId(MeditationSeatEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> SAMPLE_TIME=SynchedEntityData.defineId(MeditationSeatEntity.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> GAIN_TIME=SynchedEntityData.defineId(MeditationSeatEntity.class,EntityDataSerializers.LONG);
    private BlockPos cushionPos = BlockPos.ZERO;
    private int tier;

    public MeditationSeatEntity(EntityType<MeditationSeatEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        setInvisible(true);
    }

    public MeditationSeatEntity(Level level, BlockPos cushionPos, int tier) {
        this(cn.blockforge.generated.generatedmod.registry.ModEntities.MEDITATION_SEAT.get(), level);
        this.cushionPos = cushionPos.immutable();
        this.tier = Math.max(0, Math.min(2, tier));
        double surface = level.getBlockState(cushionPos).getBlock() instanceof CultivationBlock cushion
                ? cushion.meditationSurfaceHeight() : 0.5D;
        setPos(cushionPos.getX() + 0.5D, cushionPos.getY() + surface, cushionPos.getZ() + 0.5D);
    }

    public BlockPos cushionPos() { return cushionPos; }
    public int tier() { return tier; }

    @Override protected void defineSynchedData() {
        entityData.define(MEDITATING,false);entityData.define(UNSTABLE_ENTRY,false);
        entityData.define(ENTRY_TICKS,0);entityData.define(SAMPLE_TIME,0L);entityData.define(GAIN_TIME,-1000L);
    }
    public void setMeditating(boolean active){
        if(level().isClientSide)return;
        entityData.set(MEDITATING,active);
        if(!active){entityData.set(UNSTABLE_ENTRY,false);entityData.set(ENTRY_TICKS,0);}
    }
    public boolean isMeditating(){return entityData.get(MEDITATING);}
    public void entryProgress(boolean unstable,int ticks){
        if(level().isClientSide)return;
        entityData.set(UNSTABLE_ENTRY,unstable);
        entityData.set(ENTRY_TICKS,unstable?net.minecraft.util.Mth.clamp(ticks,0,300):0);
        entityData.set(SAMPLE_TIME,level().getGameTime());
    }
    public float entryTicks(float partialTick){
        if(!isMeditating()||!entityData.get(UNSTABLE_ENTRY))return 0;
        float elapsed=net.minecraft.util.Mth.clamp(level().getGameTime()-entityData.get(SAMPLE_TIME)+partialTick,0,12);
        return Math.min(300,entityData.get(ENTRY_TICKS)+elapsed);
    }
    public void cultivationGain(){if(!level().isClientSide)entityData.set(GAIN_TIME,level().getGameTime());}
    public float gainAge(float partialTick){return level().getGameTime()-entityData.get(GAIN_TIME)+partialTick;}

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if(getFirstPassenger() instanceof net.minecraft.world.entity.player.Player player){
            var data=GeneratedMod.data(player);
            if(data==null||!data.wuxingxiantu$isMeditating()){setMeditating(false);removePassengerAndDiscard();return;}
            if(!data.wuxingxiantu$isDaoHeartUnstable()&&entityData.get(UNSTABLE_ENTRY))entryProgress(false,0);
        }
        if (!(level() instanceof ServerLevel serverLevel)
                || !(serverLevel.getBlockState(cushionPos).getBlock() instanceof CultivationBlock cushion)
                || !cushion.isMeditationCushion()) {
            removePassengerAndDiscard();
            return;
        }
        if (getPassengers().isEmpty()) discard();
    }

    private void removePassengerAndDiscard() {
        Entity passenger = getFirstPassenger();
        if (passenger != null) passenger.stopRiding();
        discard();
    }

    @Override protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        // Folded legs sit half a block above the player's standing-foot origin.
        // Lower only the rider origin so the folded model rests on the cushion.
        moveFunction.accept(passenger, getX(), getY() - 0.5D, getZ());
    }

    @Override public net.minecraft.world.phys.Vec3 getDismountLocationForPassenger(
            net.minecraft.world.entity.LivingEntity passenger) {
        // Restore standing feet to the surface, never to the lowered render anchor.
        var target = new net.minecraft.world.phys.Vec3(getX(), getY() + 0.01D, getZ());
        passenger.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        passenger.fallDistance = 0;
        if (passenger instanceof net.minecraft.server.level.ServerPlayer player) {
            var data = GeneratedMod.data(player);
            if (data != null) CultivationBlock.clearMeditationState(player, data);
            // Synchronize a standing origin before accepting further client movement packets.
            player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
        }
        return target;
    }

    @Override protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof net.minecraft.world.entity.player.Player;
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
