package cn.blockforge.generated.generatedmod;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Loads the authored island only after its entire footprint is available. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritShrineManager {
    public static final ResourceLocation TEMPLATE=new ResourceLocation(GeneratedMod.MOD_ID,"spirit_shrine_island");
    public static final BlockPos ENTRANCE=new BlockPos(53,44,34);
    private static final int SIZE_X=103, SIZE_Y=74, SIZE_Z=86;
    public static final int GENERATION_Y=220;
    private static final int SPACING=80, SEPARATION=20;
    private static final Queue<PendingShrine> PENDING=new ConcurrentLinkedQueue<>();
    private static final Set<PendingKey> KEYS=ConcurrentHashMap.newKeySet();
    private SpiritShrineManager() {}

    @SubscribeEvent public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getChunk() instanceof LevelChunk chunk)
                || !(chunk.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) return;
        ChunkPos pos=chunk.getPos();
        if (!isCandidate(level.getSeed(),pos) || PENDING.size()>=64) return;
        PendingKey key=new PendingKey(level,pos.toLong());
        if (KEYS.add(key)) PENDING.offer(new PendingShrine(key,pos.getMiddleBlockX()-SIZE_X/2,
                pos.getMiddleBlockZ()-SIZE_Z/2,0));
    }

    static boolean isCandidate(long seed,ChunkPos pos) {
        int regionX=Math.floorDiv(pos.x,SPACING),regionZ=Math.floorDiv(pos.z,SPACING);
        return pos.equals(candidateChunk(seed,regionX,regionZ));
    }

    static ChunkPos candidateChunk(long seed,int regionX,int regionZ) {
        var random=new net.minecraft.world.level.levelgen.WorldgenRandom(new net.minecraft.world.level.levelgen.LegacyRandomSource(0));
        random.setLargeFeatureWithSalt(seed,regionX,regionZ,10387319);
        int bound=SPACING-SEPARATION;
        int x=regionX*SPACING+(random.nextInt(bound)+random.nextInt(bound))/2;
        int z=regionZ*SPACING+(random.nextInt(bound)+random.nextInt(bound))/2;
        return new ChunkPos(x,z);
    }

    public record LocateResult(BlockPos position,boolean generated) {}

    /** Seed-based search; never loads or generates chunks as a command side effect. */
    public static LocateResult locate(ServerLevel level,BlockPos from) {
        if (!level.dimension().equals(Level.OVERWORLD)) return null;
        BlockPos recorded=SpiritShrineLocations.get(level).nearest(from);
        double best=recorded==null ? Double.MAX_VALUE : horizontalDistanceSquared(from,recorded);
        int rx=Math.floorDiv(from.getX(),16*SPACING),rz=Math.floorDiv(from.getZ(),16*SPACING);
        var candidates=new java.util.ArrayList<BlockPos>();
        for (int x=rx-4;x<=rx+4;x++) for (int z=rz-4;z<=rz+4;z++) {
            ChunkPos chunk=candidateChunk(level.getSeed(),x,z);
            BlockPos origin=new BlockPos(chunk.getMiddleBlockX()-SIZE_X/2,GENERATION_Y,
                    chunk.getMiddleBlockZ()-SIZE_Z/2);
            if (level.getWorldBorder().isWithinBounds(origin)
                    && level.getWorldBorder().isWithinBounds(origin.offset(SIZE_X-1,0,SIZE_Z-1))) candidates.add(origin);
        }
        candidates.sort(java.util.Comparator.comparingDouble(p->horizontalDistanceSquared(from,p.offset(ENTRANCE))));
        var generator=level.getChunkSource().getGenerator();
        var randomState=level.getChunkSource().randomState();
        for (BlockPos origin:candidates) {
            BlockPos entrance=origin.offset(ENTRANCE);
            if (horizontalDistanceSquared(from,entrance)>=best) break;
            if (origin.getY()+SIZE_Y>level.getMaxBuildHeight()) continue;
            // Coarse terrain check for unexplored candidates. Actual placement still checks
            // every column and every occupied block once the complete footprint is loaded.
            boolean clear=true;
            for (int dx:new int[]{0,SIZE_X/2,SIZE_X-1}) {
                for (int dz:new int[]{0,SIZE_Z/2,SIZE_Z-1}) {
                    if (generator.getBaseHeight(origin.getX()+dx,origin.getZ()+dz,
                            Heightmap.Types.WORLD_SURFACE_WG,level,randomState)+20>GENERATION_Y) {
                        clear=false;break;
                    }
                }
                if (!clear) break;
            }
            if (!clear) continue;
            if (footprintLoaded(level,origin.getX(),origin.getZ())) {
                for (BlockPos pos:BlockPos.betweenClosed(origin,origin.offset(SIZE_X-1,SIZE_Y-1,SIZE_Z-1))) {
                    if (!level.getBlockState(pos).isAir()) { clear=false;break; }
                }
            }
            if (clear) return new LocateResult(entrance,false);
        }
        return recorded==null ? null : new LocateResult(recorded,true);
    }

    private static double horizontalDistanceSquared(BlockPos a,BlockPos b) {
        double dx=(double)a.getX()-b.getX(),dz=(double)a.getZ()-b.getZ();
        return dx*dx+dz*dz;
    }

    public static void tickPending(MinecraftServer server) {
        PendingShrine p=PENDING.poll();
        if (p==null) return;
        ServerLevel level=p.key.level;
        if (level.getServer()!=server) { KEYS.remove(p.key);return; }
        // No synchronous chunk generation or tickets: retry while neighboring chunks arrive.
        if (!footprintLoaded(level,p.x,p.z)) {
            if (p.attempts<1200) PENDING.offer(new PendingShrine(p.key,p.x,p.z,p.attempts+1));
            else KEYS.remove(p.key);
            return;
        }
        KEYS.remove(p.key);
        BlockPos center=new BlockPos(p.x+ENTRANCE.getX(),128,p.z+ENTRANCE.getZ());
        BlockPos nearest=SpiritShrineLocations.get(level).nearest(center);
        if (nearest!=null && Math.abs(nearest.getX()-center.getX())<160 && Math.abs(nearest.getZ()-center.getZ())<144) return;
        int surface=level.getMinBuildHeight();
        for (int x=p.x;x<p.x+SIZE_X;x++) for (int z=p.z;z<p.z+SIZE_Z;z++)
            surface=Math.max(surface,level.getHeight(Heightmap.Types.WORLD_SURFACE,x,z));
        if (surface+20>GENERATION_Y) return;
        BlockPos origin=new BlockPos(p.x,GENERATION_Y,p.z);
        if (origin.getY()+SIZE_Y>level.getMaxBuildHeight()) return;
        if (placeIsland(level,origin)) {
            BlockPos entrance=origin.offset(ENTRANCE);
            SpiritShrineLocations.get(level).add(entrance);
            for (var player:level.getPlayers(player->player.blockPosition().distSqr(entrance)<=256*256))
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.shrine_generated"),false);
        }
    }

    static boolean footprintLoaded(ServerLevel level,int x,int z) {
        for (int cx=Math.floorDiv(x,16);cx<=Math.floorDiv(x+SIZE_X-1,16);cx++)
            for (int cz=Math.floorDiv(z,16);cz<=Math.floorDiv(z+SIZE_Z-1,16);cz++)
                if (level.getChunkSource().getChunkNow(cx,cz)==null) return false;
        return true;
    }

    static boolean placeIsland(ServerLevel level,BlockPos origin) {
        if (origin.getY()<level.getMinBuildHeight() || origin.getY()+SIZE_Y>level.getMaxBuildHeight()
                || !footprintLoaded(level,origin.getX(),origin.getZ())) return false;
        // The supplied template contains air. Never erase terrain or an existing build.
        for (BlockPos pos:BlockPos.betweenClosed(origin,origin.offset(SIZE_X-1,SIZE_Y-1,SIZE_Z-1)))
            if (!level.getBlockState(pos).isAir()) return false;
        StructureTemplate template=level.getStructureManager().get(TEMPLATE).orElse(null);
        if (template==null || !template.getSize().equals(new net.minecraft.core.Vec3i(SIZE_X,SIZE_Y,SIZE_Z))) return false;
        // Preserve authored villagers, inventories and trades; do not spawn another merchant.
        return template.placeInWorld(level,origin,origin,new StructurePlaceSettings()
                .setIgnoreEntities(false).setFinalizeEntities(false),level.random,Block.UPDATE_CLIENTS);
    }

    public static void clearPending(MinecraftServer server) {
        PENDING.removeIf(p->p.key.level.getServer()==server);
        KEYS.removeIf(key->key.level.getServer()==server);
    }
    private record PendingKey(ServerLevel level,long chunk) {}
    private record PendingShrine(PendingKey key,int x,int z,int attempts) {}
}
