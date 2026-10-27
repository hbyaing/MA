package cn.blockforge.generated.generatedmod.alchemy.observation;

import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.function.ToDoubleFunction;

/** Reads only publicly visible input stacks; never predicts reactions or evaluates a grade. */
public final class MaterialObservation {
    public record Tag(String id,String translationKey,List<Integer> sourceSlots,OptionalDouble strength,OptionalDouble budget){}
    public record Snapshot(List<ItemStack> items,List<Tag> tags){}
    private static final Map<String,ToDoubleFunction<Snapshot>> PROVIDERS=new LinkedHashMap<>();
    static {
        register("occupied_slots",s->s.items.stream().filter(x->!x.isEmpty()).count());
        register("item_count",s->s.items.stream().mapToInt(ItemStack::getCount).sum());
        register("herb_count",s->s.items.stream().filter(x->HerbAge.definition(x)!=null).mapToInt(ItemStack::getCount).sum());
        register("minimum_years",s->s.items.stream().filter(x->HerbAge.definition(x)!=null).mapToInt(HerbAge::years).min().orElse(0));
        register("maximum_years",s->s.items.stream().filter(x->HerbAge.definition(x)!=null).mapToInt(HerbAge::years).max().orElse(0));
    }
    /** Extension point for a trusted module; unknown presentation sources are rejected on resource reload. */
    public static synchronized void register(String id,ToDoubleFunction<Snapshot> provider){
        if(!id.matches("[a-z0-9_]+")||PROVIDERS.putIfAbsent(id,Objects.requireNonNull(provider))!=null)throw new IllegalArgumentException("Duplicate observation provider: "+id);
    }
    public static Set<String> providers(){return Set.copyOf(PROVIDERS.keySet());}
    public static double value(String source,Snapshot snapshot){var provider=PROVIDERS.get(source);if(provider==null)throw new IllegalArgumentException("Unknown observation source");double value=provider.applyAsDouble(snapshot);if(!Double.isFinite(value))throw new IllegalArgumentException("Non-finite observation");return value;}
    public static Snapshot read(Container input,int slots,ObservationDefinitions definitions){
        if(slots<0||slots>32||slots>input.getContainerSize())throw new IllegalArgumentException("Invalid input slots");
        var items=new ArrayList<ItemStack>();var sources=new TreeMap<String,List<Integer>>();
        for(int i=0;i<slots;i++){var item=input.getItem(i).copy();items.add(item);var herb=HerbAge.definition(item);
            if(herb!=null)for(var property:herb.properties)sources.computeIfAbsent(property.id(),ignored->new ArrayList<>()).add(i+1);
        }
        var tags=new ArrayList<Tag>();for(var entry:sources.entrySet())tags.add(new Tag(definitions.tags().prefix()+entry.getKey(),definitions.tags().translationPrefix()+entry.getKey(),List.copyOf(entry.getValue()),OptionalDouble.empty(),OptionalDouble.empty()));
        return new Snapshot(List.copyOf(items),List.copyOf(tags));
    }
    private MaterialObservation(){}
}
