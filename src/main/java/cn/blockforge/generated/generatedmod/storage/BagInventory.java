package cn.blockforge.generated.generatedmod.storage;

import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Unbounded entry count with long quantities; herbs retain an exact medicinal-year multiset. */
public final class BagInventory {
    public final List<Entry> entries = new ArrayList<>();
    private static ItemStack key(ItemStack source) {
        ItemStack result = source.copyWithCount(1);
        if (HerbAge.definition(source) != null && result.hasTag()) {
            result.getTag().remove(HerbAge.KEY);
            if (result.getTag().isEmpty()) result.setTag(null);
        }
        return result;
    }
    public static final class Entry {
        private final ItemStack base;
        private final boolean herb;
        private long count;
        private final TreeMap<Integer,Long> years = new TreeMap<>();
        private Entry(ItemStack source) {
            base = key(source);
            herb = HerbAge.definition(source) != null;
        }
        public long total() { return count; }
        public boolean matches(ItemStack source) {
            return !source.isEmpty() && ItemStack.isSameItemSameTags(base,key(source));
        }
        private boolean add(ItemStack source,long quantity) {
            if (quantity<=0 || count>Long.MAX_VALUE-quantity) return false;
            if (herb) years.merge(HerbAge.years(source),quantity,Long::sum);
            count += quantity; return true;
        }
        public ItemStack preview(int amount) {
            if (count<=0 || amount<=0) return ItemStack.EMPTY;
            ItemStack stack = base.copy(); long available = count;
            if (herb) {
                var first = years.firstEntry(); HerbAge.setYears(stack,first.getKey()); available = first.getValue();
            }
            stack.setCount((int)Math.min(Math.min(amount,stack.getMaxStackSize()),available)); return stack;
        }
        public ItemStack display() { return preview(1); }
        public ItemStack take(int amount) {
            ItemStack stack = preview(amount);
            if (!stack.isEmpty()) subtract(stack.getCount(),herb?HerbAge.years(stack):0); return stack;
        }
        public ItemStack takeMatching(ItemStack target,int amount) {
            if (!matches(target) || amount<=0) return ItemStack.EMPTY;
            if (!herb) return take(amount);
            int year = HerbAge.years(target); long available = years.getOrDefault(year,0L);
            if (available<=0) return ItemStack.EMPTY;
            ItemStack stack = target.copyWithCount((int)Math.min(Math.min(available,amount),target.getMaxStackSize()));
            subtract(stack.getCount(),year); return stack;
        }
        private void subtract(int amount,int year) {
            count -= amount;
            if (herb) {long remaining = years.get(year)-amount;if (remaining==0) years.remove(year); else years.put(year,remaining);}
        }
        private CompoundTag save() {
            var tag = new CompoundTag(); tag.put("base",base.save(new CompoundTag()));
            if (!herb) tag.putLong("count",count);
            else {
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
            if (HerbAge.definition(base)==null || !tag.contains("years")) inventory.insertCount(base,tag.getLong("count"));
            else {
                int[] years=tag.getIntArray("years"); long[] counts=tag.getLongArray("counts");
                for(int n=0;n<Math.min(years.length,counts.length);n++) {
                    ItemStack aged = base.copy(); HerbAge.setYears(aged,years[n]); inventory.insertCount(aged,counts[n]);
                }
            }
        }
        return inventory;
    }
}