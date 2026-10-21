package cn.blockforge.generated.generatedmod.event;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class AlchemyEvents {

    @SubscribeEvent public static void refiningForgePlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
        if (!RefiningForgeStructure.isComponent(event.getPlacedBlock())) return;
        RefiningForgeStructure.refreshAround(level, event.getPos());
    }

    @SubscribeEvent public static void alchemyFurnacePlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
        if (AlchemyFurnaceStructure.isComponent(event.getPlacedBlock()))
            AlchemyFurnaceStructure.markDirtyAround(level, event.getPos());
    }

    @SubscribeEvent public static void alchemyFurnaceNeighbor(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
        AlchemyFurnaceStructure.markDirtyAround(level,event.getPos());
    }

    @SubscribeEvent public static void refiningForgeNeighbor(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
        if (!RefiningForgeStructure.hasComponentNear(level, event.getPos())) return;
        RefiningForgeStructure.refreshAround(level, event.getPos());
    }
    private AlchemyEvents() {}
}
