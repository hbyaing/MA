package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 供奉异火的供品：由炼器台在承华台与炉心之间生成，沿弧线飞向中央异火，
 * 抵达后短暂保留以绘制入焰光屑。纯视觉载体——素材在服务端点击“炼制”时即已扣除，
 * 因此这里禁用拾取、碰撞与伤害，也不做原版物品实体的重力与合并逻辑。
 */
public final class OfferingItemEntity extends ItemEntity implements net.minecraftforge.entity.IEntityAdditionalSpawnData {
    private static final int MAX_FLIGHT_TICKS = 140;

    private BlockPos target = BlockPos.ZERO;
    private boolean hasTarget;
    private int flightTicks;
    private Vec3 origin=Vec3.ZERO;
    private long startedAt;
    private int duration=24,fireTier=1;

    public OfferingItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        noPhysics=true;
        setNoGravity(true);
        setNeverPickUp();
    }

    public OfferingItemEntity(Level level, double x, double y, double z, ItemStack stack, BlockPos target) {
        this(cn.blockforge.generated.generatedmod.registry.ModEntities.OFFERING_ITEM.get(),level);
        setPos(x,y,z);
        setItem(stack);
        origin=new Vec3(x,y,z);
        startedAt=level.getGameTime();
        this.target = target.immutable();
        this.hasTarget = true;
        duration=net.minecraft.util.Mth.clamp((int)(origin.distanceTo(destination())*4)+12,18,32);
        fireTier=Math.max(1,ExoticFireState.get(level,target));
        this.setNoGravity(true);
        this.setNeverPickUp();
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** Both sides follow the same spawn-synchronized path; only the server removes the visual carrier. */
    @Override public void tick() {
        xo=getX();yo=getY();zo=getZ();
        this.baseTick();
        if (!this.hasTarget) {
            if(!level().isClientSide)this.discard();
            return;
        }
        setPos(path(progress(0)));
        setDeltaMovement(Vec3.ZERO);
        if(!level().isClientSide&&(age(0)>=duration+12||++flightTicks>MAX_FLIGHT_TICKS))discard();
    }

    public float age(float partial){return level().getGameTime()-startedAt+partial;}
    public float progress(float partial){return net.minecraft.util.Mth.clamp(age(partial)/duration,0,1);}
    public float impactAge(float partial){return age(partial)-duration;}
    public int fireTier(){return fireTier;}
    @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(1.5);}
    public Vec3 destination(){return Vec3.atCenterOf(target).add(0,.18,0);}
    public Vec3 path(float progress){
        double p=net.minecraft.util.Mth.clamp(progress,0,1),ease=p*p*(3-2*p);
        return origin.lerp(destination(),ease).add(0,Math.sin(p*Math.PI)*.65,0);
    }
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(){
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf buf){
        buf.writeBlockPos(target);buf.writeDouble(origin.x);buf.writeDouble(origin.y);buf.writeDouble(origin.z);
        buf.writeLong(startedAt);buf.writeVarInt(duration);buf.writeVarInt(fireTier);
    }
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf buf){
        target=buf.readBlockPos();origin=new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble());
        startedAt=buf.readLong();duration=net.minecraft.util.Mth.clamp(buf.readVarInt(),18,32);fireTier=buf.readVarInt();hasTarget=true;
        setNoGravity(true);setNeverPickUp();
    }

    @Override public void playerTouch(net.minecraft.world.entity.player.Player player) {}

    @Override public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override public boolean isPickable() {
        return false;
    }

    @Override public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasTarget", this.hasTarget);
        tag.putInt("TargetX", this.target.getX());
        tag.putInt("TargetY", this.target.getY());
        tag.putInt("TargetZ", this.target.getZ());
        tag.putInt("FlightTicks", this.flightTicks);
        tag.putDouble("OriginX",origin.x);tag.putDouble("OriginY",origin.y);tag.putDouble("OriginZ",origin.z);
        tag.putLong("StartedAt",startedAt);tag.putInt("Duration",duration);tag.putInt("FireTier",fireTier);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasTarget = tag.getBoolean("HasTarget");
        this.target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        this.flightTicks = tag.getInt("FlightTicks");
        origin=new Vec3(tag.getDouble("OriginX"),tag.getDouble("OriginY"),tag.getDouble("OriginZ"));
        startedAt=tag.getLong("StartedAt");duration=net.minecraft.util.Mth.clamp(tag.getInt("Duration"),18,32);
        fireTier=Math.max(1,tag.getInt("FireTier"));hasTarget&=tag.contains("StartedAt");
    }
}
