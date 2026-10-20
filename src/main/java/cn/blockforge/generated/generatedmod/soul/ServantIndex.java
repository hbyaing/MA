package cn.blockforge.generated.generatedmod.soul;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Index loaded servants only; unloading never retains entities or levels. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class ServantIndex {
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, Set<UUID>> IDS = new HashMap<>();
    @SubscribeEvent public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && HumanEmperorBannerItem.isServant(event.getEntity()))
            IDS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            var ids = IDS.get(level.dimension()); if (ids != null) ids.remove(event.getEntity().getUUID());
        }
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { IDS.clear(); }
    public static List<Entity> loaded(ServerLevel level) {
        var result = new ArrayList<Entity>();
        for (UUID id : IDS.getOrDefault(level.dimension(), Set.of())) {
            Entity entity = level.getEntity(id);
            if (entity != null && !entity.isRemoved() && HumanEmperorBannerItem.isServant(entity)) result.add(entity);
        }
        return result;
    }
    private ServantIndex() {}
}
