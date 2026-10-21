package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CreatureRegistration {
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ImportedCreatures.BLACK_BOAR.get(),BlackBoarEntity.attributes(false).build());
        event.put(ImportedCreatures.BLACK_BOAR_KING.get(),BlackBoarEntity.attributes(true).build());
    }
    @SubscribeEvent public static void placements(SpawnPlacementRegisterEvent event) {
        for(var type:java.util.List.of(ImportedCreatures.BLACK_BOAR.get(),ImportedCreatures.BLACK_BOAR_KING.get()))
            event.register(type,SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    CreatureRegistration::canSpawn,SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
    public static boolean canSpawn(EntityType<BlackBoarEntity> type,ServerLevelAccessor level,MobSpawnType reason,BlockPos pos,RandomSource random) {
        return level.getDifficulty()!=Difficulty.PEACEFUL&&level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                &&level.getRawBrightness(pos,0)>8&&!level.getLevel().isVillage(pos)
                &&pos.distSqr(level.getLevel().getSharedSpawnPos())>1024;
    }
    private CreatureRegistration() {}
}
