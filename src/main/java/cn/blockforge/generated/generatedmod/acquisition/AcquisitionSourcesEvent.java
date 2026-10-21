package cn.blockforge.generated.generatedmod.acquisition;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.Event;

/** Each gameplay module registers its provider during an audit/reload; no sampling of random rewards. */
public final class AcquisitionSourcesEvent extends Event {
    private final Set<String> providers=new HashSet<>();
    private final BiConsumer<ResourceLocation,SourceEvidence> sink;
    public AcquisitionSourcesEvent(BiConsumer<ResourceLocation,SourceEvidence> sink){this.sink=sink;}
    public void register(String id,Consumer<AcquisitionSourcesEvent> provider) {
        if(!providers.add(id))throw new IllegalArgumentException("Duplicate acquisition provider: "+id);
        provider.accept(this);
    }
    public void add(ItemLike item,SourceEvidence evidence){sink.accept(BuiltInRegistries.ITEM.getKey(item.asItem()),evidence);}
}
