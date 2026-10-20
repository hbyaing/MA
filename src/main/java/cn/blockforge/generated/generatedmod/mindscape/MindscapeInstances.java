package cn.blockforge.generated.generatedmod.mindscape;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** A finite grid with durable leases, including owners who are temporarily offline. */
public final class MindscapeInstances extends SavedData {
    public static final int LIMIT=64, SPACING=128, FLOOR=64, HALF_SIZE=MindscapeArenas.HALF_SIZE;
    private final Map<UUID,Integer> slots=new HashMap<>();
    private final Map<UUID,UUID> owners=new HashMap<>();
    private final BitSet initialized = new BitSet(LIMIT);
    public static MindscapeInstances get(ServerLevel level){return level.getDataStorage().computeIfAbsent(
            MindscapeInstances::load,MindscapeInstances::new,"wuxingxiantu_mindscape_instances");}
    public static MindscapeInstances load(CompoundTag tag){
        var data=new MindscapeInstances();var leases=tag.getList("leases",10);
        for(int i=0;i<Math.min(LIMIT,leases.size());i++){
            var lease=leases.getCompound(i);int slot=lease.getInt("slot");
            if(lease.hasUUID("session")&&lease.hasUUID("owner")&&slot>=0&&slot<LIMIT&&!data.slots.containsValue(slot)){
                data.slots.put(lease.getUUID("session"),slot);data.owners.put(lease.getUUID("session"),lease.getUUID("owner"));
            }
        }
        for (int slot : tag.getIntArray("initialized_v1")) if (slot >= 0 && slot < LIMIT) data.initialized.set(slot);
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag){
        var leases=new net.minecraft.nbt.ListTag();slots.forEach((session,slot)->{
            var lease=new CompoundTag();lease.putUUID("session",session);lease.putUUID("owner",owners.get(session));lease.putInt("slot",slot);leases.add(lease);
        });tag.put("leases",leases);tag.putIntArray("initialized_v1", initialized.stream().toArray());return tag;
    }
    public boolean initialized(int slot) { return initialized.get(slot); }
    public void markInitialized(int slot) { initialized.set(slot); setDirty(); }
    public int allocate(UUID owner,UUID session){
        releaseOwner(owner);
        for(int i=0;i<LIMIT;i++)if(!slots.containsValue(i)){slots.put(session,i);owners.put(session,owner);setDirty();return i;}
        return -1;
    }
    public boolean owns(UUID owner,UUID session,int slot){return owner.equals(owners.get(session))&&Objects.equals(slots.get(session),slot);}
    public void release(UUID session){slots.remove(session);owners.remove(session);setDirty();}
    public void releaseOwner(UUID owner){for(UUID id:new ArrayList<>(owners.keySet()))if(owner.equals(owners.get(id)))release(id);}
    public void resetLeases(){slots.clear();owners.clear();setDirty();}
    public static BlockPos center(int slot){return new BlockPos((slot%8)*SPACING+32,FLOOR,(slot/8)*SPACING+32);}
}
