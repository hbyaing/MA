package cn.blockforge.generated.generatedmod.alchemy.inputs;

import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Finite, phase/dimension-attributed moon stock. Storage never asserts a current sky condition. */
public final class MoonBottleData {
    public static final int CAPACITY=24000, MAX_RELEASE=1000, MAX_COLLECT=1000, MAX_SOURCES=16;
    private final TreeMap<String,Long> stock=new TreeMap<>();
    public long total(){return stock.values().stream().mapToLong(Long::longValue).sum();}
    public Map<String,Long> sources(){return Collections.unmodifiableMap(new TreeMap<>(stock));}
    public long collect(AlchemyEnvironment.Sample sample){
        long rate=sample.values().getOrDefault("moon_input_rate",0L);
        if(sample.values().getOrDefault("night",0L)!=1||sample.values().getOrDefault("open_sky",0L)!=1)return 0;
        int phase=Math.toIntExact(sample.values().getOrDefault("moon_phase",-1L));if(phase<0||phase>7)return 0;
        String source=sample.dimension()+"/moon/"+phase;
        if(!stock.containsKey(source)&&stock.size()>=MAX_SOURCES)return 0;
        long added=Math.min(Math.max(0,rate),Math.min(MAX_COLLECT,CAPACITY-total()));
        if(added>0)stock.merge(source,added,Math::addExact);return added;
    }
    /** A single transfer mutation returns exactly what was removed, with bounded provenance. */
    public Map<String,Long> release(long requested){
        if(requested<0||requested>MAX_RELEASE)throw new IllegalArgumentException("Release rate exceeded");
        long remaining=Math.min(requested,total());var result=new TreeMap<String,Long>();
        var iterator=stock.entrySet().iterator();while(iterator.hasNext()&&remaining>0){var entry=iterator.next();long paid=Math.min(remaining,entry.getValue());
            result.put(entry.getKey(),paid);entry.setValue(entry.getValue()-paid);remaining-=paid;if(entry.getValue()==0)iterator.remove();}
        return Collections.unmodifiableMap(result);
    }
    /** Atomic in-memory bottle split/mix. The block owner must mark both saved objects dirty. */
    public long transferTo(MoonBottleData target,long requested){
        Objects.requireNonNull(target);if(requested<0||requested>MAX_RELEASE)throw new IllegalArgumentException("Transfer rate exceeded");
        if(target==this)return 0;
        long remaining=Math.min(requested,Math.min(total(),CAPACITY-target.total()));var plan=new TreeMap<String,Long>();
        var keys=new HashSet<>(target.stock.keySet());
        for(var entry:stock.entrySet()){
            if(remaining==0)break;if(!keys.contains(entry.getKey())&&keys.size()>=MAX_SOURCES)continue;
            long amount=Math.min(remaining,entry.getValue());plan.put(entry.getKey(),amount);keys.add(entry.getKey());remaining-=amount;
        }
        long moved=0;for(var entry:plan.entrySet()){
            long left=stock.get(entry.getKey())-entry.getValue();if(left==0)stock.remove(entry.getKey());else stock.put(entry.getKey(),left);
            target.stock.merge(entry.getKey(),entry.getValue(),Math::addExact);moved+=entry.getValue();
        }return moved;
    }
    public CompoundTag save(){var tag=new CompoundTag();tag.putInt("Schema",1);var sources=new CompoundTag();stock.forEach(sources::putLong);tag.put("Sources",sources);return tag;}
    public static MoonBottleData load(CompoundTag tag){
        var bottle=new MoonBottleData();if(tag.isEmpty())return bottle;
        if(!tag.getAllKeys().equals(Set.of("Schema","Sources"))||!tag.contains("Schema",net.minecraft.nbt.Tag.TAG_INT)||!tag.contains("Sources",net.minecraft.nbt.Tag.TAG_COMPOUND)||tag.getInt("Schema")!=1)throw new IllegalArgumentException("Unknown moon bottle schema");
        var sources=tag.getCompound("Sources");if(sources.size()>MAX_SOURCES)throw new IllegalArgumentException("Too many moon sources");
        for(String key:sources.getAllKeys()){
            if(!key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+/moon/[0-7]")||!sources.contains(key,net.minecraft.nbt.Tag.TAG_LONG))throw new IllegalArgumentException("Invalid moon source");
            long n=sources.getLong(key);if(n<=0||n>CAPACITY)throw new IllegalArgumentException("Invalid moon quantity");bottle.stock.put(key,n);
        }
        if(bottle.total()>CAPACITY)throw new IllegalArgumentException("Moon bottle capacity exceeded");return bottle;
    }
}
