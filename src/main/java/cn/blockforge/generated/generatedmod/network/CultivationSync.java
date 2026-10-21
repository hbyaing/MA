package cn.blockforge.generated.generatedmod.network;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;

/** Version gates avoid copying slow arrays on ordinary vitals flushes; full heartbeats retain recovery. */
public final class CultivationSync {
    private record Snapshot(Object[] stable,CultivationVitalsPacket vitals,Object[] divine,long fullAt,long stableVersion,long divineVersion){}
    private static final Map<ServerPlayer,Snapshot> LAST=new WeakHashMap<>();
    private static final Map<ServerPlayer,Long> LOSS_LAST=new WeakHashMap<>();
    public static void forget(ServerPlayer player){LAST.remove(player);LOSS_LAST.remove(player);SyncMetrics.forget(player.getUUID());}
    public static void clear(){LAST.clear();LOSS_LAST.clear();SyncMetrics.reset();}
    public static void publish(ServerPlayer player){
        var data=GeneratedMod.data(player);if(data==null)return;
        var old=LAST.get(player);long now=player.server.overworld().getGameTime();
        SyncMetrics.observe(player.getUUID());
        boolean heartbeat=old==null||now-old.fullAt()>=200||now<old.fullAt();
        var tracked=data instanceof CultivationDataImpl impl?impl:null;
        long stableVersion=tracked==null?0:tracked.stableSyncVersion();
        long divineVersion=tracked==null?0:tracked.divineSyncVersion();
        boolean readStable=heartbeat||tracked==null||stableVersion!=old.stableVersion();
        SyncPacket packet=null;Object[] stable=old==null?null:old.stable();
        if(readStable){packet=new SyncPacket(data);stable=packet.stableState();SyncMetrics.snapshotAllocated();}
        var vitals=new CultivationVitalsPacket(data.getCurrentMana(),data.getMaxMana(),data.wuxingxiantu$getBiguRemainTime(),
                data.wuxingxiantu$getCultivation(),data.wuxingxiantu$getMerit());
        boolean readDivine=heartbeat||tracked==null||divineVersion!=old.divineVersion();
        Object[] divine=readDivine?new Object[]{data.wuxingxiantu$getLearnedDivineArts(),data.wuxingxiantu$getEquippedDivineArts(),data.wuxingxiantu$getActiveDivineArt(),
                data.wuxingxiantu$getDivineStepIndex(),data.wuxingxiantu$getPossessedForms(),data.wuxingxiantu$getActivePossessedForm(),
                data.wuxingxiantu$getLearnedFires(),data.wuxingxiantu$getEquippedFire()}:old.divine();
        boolean full=heartbeat||readStable&&!Arrays.deepEquals(stable,old.stable());
        boolean flushVitals=full||now%5==0;
        if(flushVitals&&tracked!=null&&!java.util.Objects.equals(LOSS_LAST.get(player),tracked.manaLossVersion())){Network.sendToPlayer(player,new ManaLossPacket(tracked.manaLossRows(),tracked.manaLossEnabled()));LOSS_LAST.put(player,tracked.manaLossVersion());}
        if(full)Network.sendToPlayer(player,packet);
        else if(flushVitals&&!vitals.equals(old.vitals()))Network.sendToPlayer(player,vitals);
        if(heartbeat||readDivine&&!Arrays.deepEquals(divine,old.divine()))Network.sendDivineSync(player);
        LAST.put(player,new Snapshot(stable,flushVitals?vitals:old.vitals(),divine,full?now:old.fullAt(),stableVersion,divineVersion));
    }
    private CultivationSync(){}
}
