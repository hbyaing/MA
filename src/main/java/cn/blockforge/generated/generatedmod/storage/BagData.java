package cn.blockforge.generated.generatedmod.storage;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public final class BagData extends SavedData {
 private final Map<UUID,BagInventory> bags=new HashMap<>();
 public static BagData get(ServerLevel level){var storage=level.getServer().overworld().getDataStorage();synchronized(storage){return storage.computeIfAbsent(BagData::load,BagData::new,"wuxingxiantu_storage_bags");}}
 public BagInventory inventory(UUID id){return bags.computeIfAbsent(id,k->new BagInventory());}
 private static BagData load(CompoundTag tag){BagData d=new BagData();ListTag list=tag.getList("bags",10);for(int i=0;i<list.size();i++){CompoundTag b=list.getCompound(i);if(!b.hasUUID("id"))continue;BagInventory inventory;if(b.contains("entries"))inventory=BagInventory.load(b.getList("entries",10));else{inventory=new BagInventory();ListTag legacy=b.getList("items",10);for(int n=0;n<legacy.size();n++)inventory.insert(ItemStack.of(legacy.getCompound(n)));}d.bags.put(b.getUUID("id"),inventory);}return d;}
 @Override public CompoundTag save(CompoundTag tag){ListTag list=new ListTag();bags.forEach((id,inventory)->{CompoundTag b=new CompoundTag();b.putUUID("id",id);b.put("entries",inventory.save());list.add(b);});tag.put("bags",list);return tag;}
}
