package cn.blockforge.generated.generatedmod.plants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Three tracked wild plants; no repeated per-chunk worldgen lottery. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class DragonGrassCycle extends SavedData{
    private long next=-1;private final java.util.List<BlockPos> positions=new java.util.ArrayList<>();
    private final java.util.Set<BlockPos> cultivated=new java.util.HashSet<>();
    public static boolean mainIsland(BlockPos p){return (long)p.getX()*p.getX()+(long)p.getZ()*p.getZ()<=128L*128;}
    public static DragonGrassCycle load(CompoundTag t){var d=new DragonGrassCycle();d.next=t.getLong("Next");for(long p:t.getLongArray("Positions"))d.positions.add(BlockPos.of(p));for(long p:t.getLongArray("Cultivated"))d.cultivated.add(BlockPos.of(p));return d;}
    @Override public CompoundTag save(CompoundTag t){t.putLong("Next",next);t.putLongArray("Positions",positions.stream().mapToLong(BlockPos::asLong).toArray());t.putLongArray("Cultivated",cultivated.stream().mapToLong(BlockPos::asLong).toArray());return t;}
    @SubscribeEvent public static void placed(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){
        if(e.getLevel() instanceof ServerLevel l&&l.dimension()==Level.END&&e.getEntity() instanceof net.minecraft.world.entity.player.Player&&e.getPlacedBlock().is(SpiritPlantRegistry.DRAGON_SCALE_GRASS.get())){
            var d=l.getDataStorage().computeIfAbsent(DragonGrassCycle::load,DragonGrassCycle::new,"wuxing_dragon_grass");d.cultivated.add(e.getPos().immutable());d.setDirty();
        }
    }
    @SubscribeEvent public static void broken(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(!e.isCanceled()&&e.getLevel() instanceof ServerLevel l&&l.dimension()==Level.END&&e.getState().is(SpiritPlantRegistry.DRAGON_SCALE_GRASS.get())){
            var d=l.getDataStorage().computeIfAbsent(DragonGrassCycle::load,DragonGrassCycle::new,"wuxing_dragon_grass");if(d.cultivated.remove(e.getPos()))d.setDirty();
        }
    }
    @SubscribeEvent public static void migrate(net.minecraftforge.event.level.ChunkEvent.Load e){
        if(!(e.getLevel() instanceof ServerLevel level)||level.dimension()!=Level.END||!(e.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk))return;
        var d=level.getDataStorage().computeIfAbsent(DragonGrassCycle::load,DragonGrassCycle::new,"wuxing_dragon_grass");
        // Old worldgen had no provenance. Preserve cultivated plants on spirit soil,
        // retire untracked end-stone wild plants as their chunks naturally load.
        var sections=chunk.getSections();for(int s=0;s<sections.length;s++){
            var section=sections[s];if(!section.maybeHas(state->state.is(SpiritPlantRegistry.DRAGON_SCALE_GRASS.get())))continue;
            int bottom=chunk.getMinBuildHeight()+s*16;
            for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++){
                if(!section.getBlockState(x,y,z).is(SpiritPlantRegistry.DRAGON_SCALE_GRASS.get()))continue;
                var p=new BlockPos(chunk.getPos().getMinBlockX()+x,bottom+y,chunk.getPos().getMinBlockZ()+z);
                if(!d.positions.contains(p)&&!d.cultivated.contains(p)&&(!mainIsland(p)||chunk.getBlockState(p.below()).is(Blocks.END_STONE)))chunk.setBlockState(p,Blocks.AIR.defaultBlockState(),false);
            }
        }
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.level instanceof ServerLevel level)||level.dimension()!=Level.END||level.getGameTime()%200!=0||level.players().isEmpty())return;
        var d=level.getDataStorage().computeIfAbsent(DragonGrassCycle::load,DragonGrassCycle::new,"wuxing_dragon_grass");
        d.refresh(level,level.getGameTime());
    }
    public boolean refresh(ServerLevel level,long now){
        if(level.dimension()!=Level.END||next>=0&&now<next)return false;
        for(var p:positions)if(level.getBlockState(p).is(SpiritPlantRegistry.DRAGON_SCALE_GRASS.get()))level.removeBlock(p,false);
        positions.clear();
        for(int attempt=0;attempt<160&&positions.size()<3;attempt++){
            int x=level.random.nextInt(193)-96,z=level.random.nextInt(193)-96;
            // Height queries return the minimum height for unloaded chunks. Load the
            // chosen main-island site before asking its surface height.
            level.getChunk(x >> 4,z >> 4);
            var ground=level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(x,0,z));
            if(!mainIsland(ground)||ground.getY()<40||!level.getBlockState(ground.below()).is(Blocks.END_STONE)||!level.isEmptyBlock(ground)||positions.contains(ground))continue;
            level.setBlock(ground,SpiritPlantRegistry.DRAGON_SCALE_GRASS.get().defaultBlockState(),3);positions.add(ground.immutable());
        }
        // Failed sites retry later without creating more than three tracked plants.
        next=positions.size()==3?now+7L*24000:now+200;setDirty();return true;
    }
}
