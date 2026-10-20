package cn.blockforge.generated.generatedmod;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/** Stationary player-shaped shell. It owns no inventory and never drops copied equipment. */
public final class SoulBodyEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(SoulBodyEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public SoulBodyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level); setNoAi(true); setNoGravity(true); setInvulnerable(true);
    }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(OWNER, Optional.empty()); }
    public Optional<UUID> owner() { return entityData.get(OWNER); }
    public void setOwner(UUID owner) { entityData.set(OWNER, Optional.of(owner)); }
    public boolean returnSoul(net.minecraft.server.level.ServerPlayer player) {
        var data=GeneratedMod.data(player);
        if(data==null||!("yi_hun_ji_po".equals(data.wuxingxiantu$getActiveDivineArt())
                ||"yuan_shen_chu_qiao".equals(data.wuxingxiantu$getActiveDivineArt()))
                ||DivineArtLogic.isPossessing(player)
                ||owner().filter(player.getUUID()::equals).isEmpty()
                ||!player.getPersistentData().hasUUID("wuxingxiantu_soul_body")
                ||!getUUID().equals(player.getPersistentData().getUUID("wuxingxiantu_soul_body"))
                ||player.level()!=level()||player.distanceToSqr(this)>36)return false;
        DivineArtLogic.stop(player,data,true);
        return true;
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); owner().ifPresent(id -> tag.putUUID("Owner", id)); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); if (tag.hasUUID("Owner")) setOwner(tag.getUUID("Owner")); }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 20 == 0) {
            var player = owner().map(id -> getServer().getPlayerList().getPlayer(id)).orElse(null);
            if (player == null || !player.getPersistentData().hasUUID("wuxingxiantu_soul_body")
                    || !getUUID().equals(player.getPersistentData().getUUID("wuxingxiantu_soul_body"))) discard();
        }
    }
    @Override protected void dropAllDeathLoot(net.minecraft.world.damagesource.DamageSource source) {}
    @Override public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
}
