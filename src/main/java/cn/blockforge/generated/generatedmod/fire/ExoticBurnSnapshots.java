package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** Client snapshot ordering/prediction, separated from rendering so it can be tested on a server. */
public final class ExoticBurnSnapshots {
    private record Entry(ExoticBurnPacket packet,long expiresAt) {}
    private final Map<Integer,Entry> entries=new HashMap<>();
    public void accept(ExoticBurnPacket p,ResourceLocation dimension,long now) {
        if(!dimension.equals(p.dimension()))return;
        var previous=entries.get(p.entityId());
        if(previous!=null&&previous.packet.sequence()>=p.sequence())return;
        entries.put(p.entityId(),new Entry(p,now+p.duration()));
    }
    public ExoticBurnPacket get(int entityId,UUID uuid,long now) {
        var e=entries.get(entityId);
        return e!=null&&e.expiresAt>now&&e.packet.duration()>0&&e.packet.entityUuid().equals(uuid)?e.packet:null;
    }
    public int remaining(int entityId,UUID uuid,long now) {
        var p=get(entityId,uuid,now);return p==null?0:(int)Math.max(0,entries.get(entityId).expiresAt-now);
    }
    public void remove(int entityId,UUID uuid) {var e=entries.get(entityId);if(e!=null&&e.packet.entityUuid().equals(uuid))entries.remove(entityId);}
    public void tick(long now) {entries.entrySet().removeIf(e->e.getValue().expiresAt+200<now);}
    public void clear() {entries.clear();}
    public java.util.List<ExoticBurnPacket> active(long now) {
        return entries.values().stream().filter(e->e.expiresAt>now&&e.packet.duration()>0).map(Entry::packet).toList();
    }
    /** Unknown IDs deliberately have no visual; never substitute a Minecraft fire profile. */
    public static FireCatalog.Definition visual(String fireId) {return FireCatalog.id(fireId);}
}
