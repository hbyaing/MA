package cn.blockforge.generated.generatedmod.world;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Decoration is deferred until neighboring full chunks exist; never force-load terrain. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class DeferredVeins {
    public static boolean tryGenerate(ServerLevel level,ChunkPos pos) {
        var data=SpiritVeinData.get(level);
        if(!data.pending().contains(pos.toLong()))return false;
        for(int x=pos.x-1;x<=pos.x+1;x++)for(int z=pos.z-1;z<=pos.z+1;z++)
            if(level.getChunkSource().getChunkNow(x,z)==null)return false;
        if(data.grade(pos.x,pos.z)==0)SpiritVeinFeature.generate(level,new BlockPos(pos.x*16+8,0,pos.z*16+8));
        data.finish(pos);return true;
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!(event.level instanceof ServerLevel level)||level.getGameTime()%20!=0)return;
        // At most one expensive cave scan per second, irrespective of candidate count.
        for(long key:SpiritVeinData.get(level).pending())if(tryGenerate(level,new ChunkPos(key)))break;
    }
    private DeferredVeins(){}
}
