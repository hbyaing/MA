package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class FireLightningEvents {
    @SubscribeEvent public static void lightning(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof LightningBolt bolt) || bolt.getCause() != null
                || bolt.getPersistentData().getBoolean("wuxingxiantu_artificial_lightning")
                || !level.dimension().equals(Level.OVERWORLD) || !level.isThundering()) return;
        var pos = bolt.blockPosition();
        if (pos.getY() < 120 || !level.getBiome(pos).is(BiomeTags.IS_MOUNTAIN)
                || !level.canSeeSky(pos) || !level.isEmptyBlock(pos)
                || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                || level.random.nextInt(16) != 0) return;
        level.setBlock(pos, WuXingBlocks.EXOTIC_FIRE_SOURCE.get().defaultBlockState()
                .setValue(ExoticFireSourceBlock.FIRE_TIER, 6), 3);
    }
    private FireLightningEvents() {}
}
