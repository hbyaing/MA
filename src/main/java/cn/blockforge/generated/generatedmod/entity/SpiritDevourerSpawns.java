package cn.blockforge.generated.generatedmod.entity;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SpiritDevourerSpawns {
    @SubscribeEvent public static void register(SpawnPlacementRegisterEvent e){
        e.register(ModEntities.SPIRIT_DEVOURER.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type,level,reason,pos,random)->level.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL
                    &&level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)<=9
                    &&level.getBlockState(pos.below()).isValidSpawn(level,pos.below(),type)
                    &&!level.getLevel().isVillage(pos)&&pos.distSqr(level.getLevel().getSharedSpawnPos())>1024,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
