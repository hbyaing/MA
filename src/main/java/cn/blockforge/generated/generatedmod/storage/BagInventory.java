package cn.blockforge.generated.generatedmod.storage;

import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Unbounded entry count with long quantities; herbs retain an exact medicinal-year multiset. */
public final class BagInventory {
    public final List<Entry> entries = new ArrayList<>();
    private static String lotKey(ItemStack source){return HerbAge.definition(source)!=null?HerbAge.LOT:cn.blockforge.generated.generatedmod.importedcreatures.BeastMaterials.blood(source)?cn.blockforge.generated.generatedmod.importedcreatures.BeastMaterials.LOT:null;}
    private static ItemStack key(ItemStack source) {
        ItemStack result = source.copyWithCount(1);
        if (lotKey(source)!=null && result.hasTag()) {
            if(HerbAge.definition(source)!=null)result.getTag().remove(HerbAge.KEY);result.getTag().remove(lotKey(source));
            if (result.getTag().isEmpty()) result.setTag(null);
        }
        return result;
    }
    public static final class Entry {
        private final ItemStack base;
        private final boolean herb;
        private final String lotKey;
        private long count;
        private final TreeMap<Integer,Long> years = new TreeMap<>();
        private final TreeMap<Integer,LinkedHashMap<String,Long>> origins=new TreeMap<>();
        private Entry(ItemStack source) {
            base = key(source);
            herb = HerbAge.definition(source) != null;lotKey=lotKey(source);
        }
        public long total() { return count; }
        public boolean matches(ItemStack source) {
            return !source.isEmpty() && ItemStack.isSameItemSameTags(base,key(source));
        }
        private boolean add(ItemStack source,long quantity) {
            if (quantity<=0 || count>Long.MAX_VALUE-quantity) return false;
            if (lotKey!=null){int year=herb?HerbAge.years(source):0;years.merge(year,quantity,Long::sum);String lot=source.hasTag()&&source.getTag().hasUUID(lotKey)?source.getTag().getUUID(lotKey).toString():"";origins.computeIfAbsent(year,k->new LinkedHashMap<>()).merge(lot,quantity,Long::sum);}
            count += quantity; return true;
        }
        public ItemStack preview(int amount) {
            if (count<=0 || amount<=0) return ItemStack.EMPTY;
            ItemStack stack = base.copy(); long available = count;
            if (lotKey!=null) {
                var first = years.firstEntry(); if(herb)HerbAge.setYears(stack,first.getKey()); var origin=origins.get(first.getKey()).entrySet().iterator().next();if(!origin.getKey().isEmpty())stack.getOrCreateTag().putUUID(lotKey,UUID.fromString(origin.getKey()));available=origin.getValue();
            }
            stack.setCount((int)Math.min(Math.min(amount,stack.getMaxStackSize()),available)); return stack;
        }
        public ItemStack display() { return preview(1); }
        public ItemStack take(int amount) {
            ItemStack stack = preview(amount);
            if (!stack.isEmpty()) subtract(stack.getCount(),herb?HerbAge.years(stack):0,lotKey!=null&&stack.hasTag()&&stack.getTag().hasUUID(lotKey)?stack.getTag().getUUID(lotKey).toString():""); return stack;
        }
        public ItemStack takeMatching(ItemStack target,int amount) {
            if (!matches(target) || amount<=0) return ItemStack.EMPTY;
            if (lotKey==null) return take(amount);
            int year = herb?HerbAge.years(target):0; long available = years.getOrDefault(year,0L);
            if (available<=0) return ItemStack.EMPTY;
            var bucket=origins.get(year);String requested=target.hasTag()&&target.getTag().hasUUID(lotKey)?target.getTag().getUUID(lotKey).toString():bucket.keySet().iterator().next();if(!bucket.containsKey(requested))return ItemStack.EMPTY;ItemStack stack=base.copy();if(herb)HerbAge.setYears(stack,year);if(!requested.isEmpty())stack.getOrCreateTag().putUUID(lotKey,UUID.fromString(requested));stack.setCount((int)Math.min(Math.min(bucket.get(requested),amount),stack.getMaxStackSize()));
            subtract(stack.getCount(),year,requested); return stack;
        }
        private void subtract(int amount,int year,String lot) {
            count -= amount;
            if (lotKey!=null) {var bucket=origins.get(year);long rest=bucket.get(lot)-amount;if(rest==0)bucket.remove(lot);else bucket.put(lot,rest);if(bucket.isEmpty())origins.remove(year);long remaining = years.get(year)-amount;if (remaining==0) years.remove(year); else years.put(year,remaining);}
        }
        private CompoundTag save() {
            var tag = new CompoundTag(); tag.put("base",base.save(new CompoundTag()));
            if (lotKey==null) tag.putLong("count",count);
            else {
                var sourceLots=new ListTag();for(var entry:origins.entrySet())for(var lot:entry.getValue().entrySet()){var n=new CompoundTag();n.putInt("year",entry.getKey());n.putString("lot",lot.getKey());n.putLong("count",lot.getValue());sourceLots.add(n);}tag.put("origins",sourceLots);
                tag.putIntArray("years",years.keySet().stream().mapToInt(Integer::intValue).toArray());
                tag.putLongArray("counts",years.values().stream().mapToLong(Long::longValue).toArray());
            }
            return tag;
        }
    }
    private boolean insertCount(ItemStack stack,long quantity) {
        if (stack.isEmpty() || stack.getItem() instanceof StorageBagItem || quantity<=0) return false;
        for (Entry entry:entries) if (entry.matches(stack)) return entry.add(stack,quantity);
        Entry entry = new Entry(stack); if (!entry.add(stack,quantity)) return false;
        entries.add(entry); return true;
    }
    public boolean insert(ItemStack stack) {
        if (!insertCount(stack,stack.getCount())) return false; stack.setCount(0); return true;
    }
    public void compact() { entries.removeIf(entry->entry.total()==0); }
    public void sort() { compact();entries.sort(Comparator.comparing(entry->entry.display().getHoverName().getString())); }
    public ListTag save() {
        ListTag list = new ListTag(); for (Entry entry:entries) if (entry.total()>0) list.add(entry.save()); return list;
    }
    public static BagInventory load(ListTag list) {
        BagInventory inventory = new BagInventory();
        for (int i=0;i<list.size();i++) {
            CompoundTag tag = list.getCompound(i); ItemStack base = ItemStack.of(tag.getCompound("base"));
            if (base.isEmpty()) continue;
            if (lotKey(base)==null || !tag.contains("years")) inventory.insertCount(base,tag.getLong("count"));
            else if(tag.contains("origins",Tag.TAG_LIST)){for(var t:tag.getList("origins",Tag.TAG_COMPOUND)){var n=(CompoundTag)t;var stack=base.copy();if(HerbAge.definition(stack)!=null)HerbAge.setYears(stack,n.getInt("year"));try{if(!n.getString("lot").isEmpty())stack.getOrCreateTag().putUUID(lotKey(base),UUID.fromString(n.getString("lot")));}catch(IllegalArgumentException invalid){}inventory.insertCount(stack,n.getLong("count"));}}else {
                int[] years=tag.getIntArray("years"); long[] counts=tag.getLongArray("counts");
                for(int n=0;n<Math.min(years.length,counts.length);n++) {
                    ItemStack aged = base.copy(); if(HerbAge.definition(aged)!=null)HerbAge.setYears(aged,years[n]); inventory.insertCount(aged,counts[n]);
                }
            }
        }
        return inventory;
    }
}