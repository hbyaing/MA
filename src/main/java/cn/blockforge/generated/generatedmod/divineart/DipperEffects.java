package cn.blockforge.generated.generatedmod.divineart;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.network.packet.divineart.DipperVisualPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server footfall authority and compact state broadcasts; no per-point particle packets. */
public final class DipperEffects {
    private static final class Ritual {
        final Vec3 origin; final float yaw; final ResourceKey<Level> dimension; final long startedAt;
        int step,phase=DipperVisualPacket.ACTIVE; long endedAt=-1,lastSent=Long.MIN_VALUE;
        Ritual(ServerPlayer p,BlockPos origin,float yaw) {
            this.origin=DipperPattern.anchor(origin);this.yaw=yaw;
            dimension=p.level().dimension();startedAt=p.serverLevel().getGameTime();
        }
    }
    private static final Map<UUID,Ritual> RITUALS=new HashMap<>();
    private static final String DEADLINE="wuxingxiantu:dipper_deadline";
    private static boolean expired(ServerPlayer player,CultivationData data){
        long deadline=player.getPersistentData().getLong(DEADLINE);
        if(deadline==0||player.serverLevel().getGameTime()<deadline)return false;
        data.wuxingxiantu$setDivineStepOrigin(null);data.wuxingxiantu$setDivineStepIndex(0);clear(player);GeneratedMod.sync(player);return true;
    }
    public static void tickDipper(ServerPlayer player,CultivationData data) {
        if(expired(player,data))return;
        BlockPos origin=data.wuxingxiantu$getDivineStepOrigin();
        int index=data.wuxingxiantu$getDivineStepIndex();
        if(origin==null||index<0||index>=DipperPattern.COUNT)return;
        Vec3 point=DipperPattern.star(DipperPattern.anchor(origin),data.wuxingxiantu$getDivineStepYaw(),index);
        double dx=player.getX()-point.x,dz=player.getZ()-point.z;
        if(dx*dx+dz*dz>1.7)return;
        ensureDipperEffect(player,data);
        Ritual ritual=RITUALS.get(player.getUUID());
        data.wuxingxiantu$setDivineStepIndex(index+1);
        if(ritual!=null)ritual.step=index+1;
        if(index+1>=DipperPattern.COUNT) {
            player.getPersistentData().remove(DEADLINE);
            data.wuxingxiantu$setDivineStepOrigin(null);data.wuxingxiantu$setDivineStepIndex(0);
            if(ritual!=null){ritual.phase=DipperVisualPacket.COMPLETE;ritual.endedAt=player.serverLevel().getGameTime();}
            ServerLevel level=player.serverLevel();
            if(!level.isRaining())level.setWeatherParameters(0,12000,true,false);
            else if(!level.isThundering())level.setWeatherParameters(0,12000,true,true);
            else level.setWeatherParameters(12000,0,false,false);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.dipper_complete"),false);
        }
        if(ritual!=null)broadcast(player,ritual);
        GeneratedMod.sync(player);
    }
    public static void startDipperEffect(ServerPlayer player) {
        CultivationData data=GeneratedMod.data(player);
        if(data==null||data.wuxingxiantu$getDivineStepOrigin()==null)return;
        Ritual ritual=new Ritual(player,data.wuxingxiantu$getDivineStepOrigin(),data.wuxingxiantu$getDivineStepYaw());
        player.getPersistentData().putLong(DEADLINE,player.serverLevel().getGameTime()+DipperPattern.DURATION_TICKS);
        RITUALS.put(player.getUUID(),ritual);broadcast(player,ritual);
    }
    public static void ensureDipperEffect(ServerPlayer player,CultivationData data) {
        if(expired(player,data))return;
        Ritual old=RITUALS.get(player.getUUID());
        if(old!=null&&old.phase==DipperVisualPacket.ACTIVE&&old.dimension.equals(player.level().dimension()))return;
        BlockPos origin=data.wuxingxiantu$getDivineStepOrigin();if(origin==null)return;
        if(!player.getPersistentData().contains(DEADLINE))player.getPersistentData().putLong(DEADLINE,player.serverLevel().getGameTime()+DipperPattern.DURATION_TICKS);
        Ritual ritual=new Ritual(player,origin,data.wuxingxiantu$getDivineStepYaw());
        ritual.step=Math.max(0,Math.min(6,data.wuxingxiantu$getDivineStepIndex()));
        RITUALS.put(player.getUUID(),ritual);broadcast(player,ritual);
    }
    public static void tickDipperEffect(ServerPlayer player) {
        Ritual ritual=RITUALS.get(player.getUUID());if(ritual==null)return;
        long now=player.serverLevel().getGameTime();
        if(!ritual.dimension.equals(player.level().dimension())||!player.isAlive()){clear(player);return;}
        if(ritual.phase==DipperVisualPacket.ACTIVE) {
            CultivationData data=GeneratedMod.data(player);
            if(data!=null&&expired(player,data))return;
            if(data==null||data.wuxingxiantu$getDivineStepOrigin()==null){clear(player);return;}
        } else if(now-ritual.endedAt>=30){RITUALS.remove(player.getUUID());return;}
        // Late observers receive current progress. Duplicate tick callers are coalesced.
        if(now-ritual.lastSent>=20)broadcast(player,ritual);
    }
    private static void broadcast(ServerPlayer player,Ritual ritual) {
        long now=player.serverLevel().getGameTime();ritual.lastSent=now;
        long start=ritual.phase==DipperVisualPacket.ACTIVE?ritual.startedAt:ritual.endedAt;
        int age=(int)Math.min(Integer.MAX_VALUE,Math.max(0,now-start));
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),
            new DipperVisualPacket(player.getUUID(),ritual.dimension.location(),ritual.origin,ritual.yaw,
                ritual.startedAt,ritual.step,ritual.phase,age));
    }
    public static void clear(ServerPlayer player) {
        player.getPersistentData().remove(DEADLINE);
        Ritual ritual=RITUALS.remove(player.getUUID());if(ritual==null)return;
        ritual.phase=DipperVisualPacket.CANCEL;ritual.endedAt=player.serverLevel().getGameTime();broadcast(player,ritual);
    }
    public static void emitCloud(ServerPlayer player){Network.sendCloudVisual(player,true);}
    public static void emitAura(ServerLevel level,ServerPlayer player,String id) {
        if("fa_tian_xiang_di".equals(id))return;
        if("yuan_shen_chu_qiao".equals(id)) {
            level.sendParticles(cn.blockforge.generated.generatedmod.ModParticles.GOLDEN_SOUL_FLAME.get(),
                    player.getX(),player.getY()+1,player.getZ(),8,1.1,1,1.1,.04);
            return;
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,player.getX(),player.getY()+1,player.getZ(),8,1.1,1,1.1,.04);
    }
    private DipperEffects() {}
}
