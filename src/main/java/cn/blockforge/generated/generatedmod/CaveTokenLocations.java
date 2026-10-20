package cn.blockforge.generated.generatedmod;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.saveddata.SavedData;

/** 按维度保存洞府令牌，并用固定大小空间分桶提供有界刷怪查询。 */
public final class CaveTokenLocations extends SavedData {
    private static final String DATA_NAME = "wuxingxiantu_cave_tokens";
    private static final String POSITIONS_TAG = "Positions";
    private static final int CELL_SIZE = CaveTokenBlock.PROTECTION_RADIUS;
    private static final int MAX_RECONCILIATIONS_PER_TICK = 1;
    private static final int MAX_RECONCILIATION_RETRIES = 12;
    private static final int MAX_PENDING_CHUNKS = 128;
    private static final Queue<PendingChunk> PENDING_CHUNKS = new ConcurrentLinkedQueue<>();
    private static final Set<PendingKey> PENDING_KEYS = ConcurrentHashMap.newKeySet();

    private final Set<Long> positions = new LinkedHashSet<>();
    private final Map<Long, Set<Long>> positionsByCell = new HashMap<>();
    private final Map<Long, Set<Long>> positionsByChunk = new HashMap<>();

    public static CaveTokenLocations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CaveTokenLocations::load, CaveTokenLocations::new, DATA_NAME);
    }

    private static CaveTokenLocations load(CompoundTag tag) {
        CaveTokenLocations data = new CaveTokenLocations();
        for (long packed : tag.getLongArray(POSITIONS_TAG)) data.addPacked(packed);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] packed = new long[positions.size()];
        int index = 0;
        for (long position : positions) packed[index++] = position;
        tag.putLongArray(POSITIONS_TAG, packed);
        return tag;
    }

    public void add(BlockPos pos) {
        if (addPacked(pos.asLong())) setDirty();
    }

    public void remove(BlockPos pos) {
        if (removePacked(pos.asLong())) setDirty();
    }

    /** 为每个已记录令牌固定加载其所在区块及周围八个区块。 */
    public void ensureAllForcedChunks(ServerLevel level) {
        for (long packed : positions) ensureForcedChunks(level, BlockPos.of(packed));
    }

    public static void ensureForcedChunks(ServerLevel level, BlockPos tokenPos) {
        int centerX = tokenPos.getX() >> 4;
        int centerZ = tokenPos.getZ() >> 4;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                net.minecraftforge.common.world.ForgeChunkManager.forceChunk(
                        level, GeneratedMod.MOD_ID, tokenPos, centerX + offsetX, centerZ + offsetZ, true, true);
            }
        }
    }

    public static void releaseForcedChunks(ServerLevel level, BlockPos tokenPos) {
        int centerX = tokenPos.getX() >> 4;
        int centerZ = tokenPos.getZ() >> 4;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                net.minecraftforge.common.world.ForgeChunkManager.forceChunk(
                        level, GeneratedMod.MOD_ID, tokenPos, centerX + offsetX, centerZ + offsetZ, false, true);
            }
        }
    }

    public boolean protects(BlockPos target) {
        int cellX = Math.floorDiv(target.getX(), CELL_SIZE);
        int cellY = Math.floorDiv(target.getY(), CELL_SIZE);
        int cellZ = Math.floorDiv(target.getZ(), CELL_SIZE);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    Set<Long> bucket = positionsByCell.get(BlockPos.asLong(
                            cellX + offsetX, cellY + offsetY, cellZ + offsetZ));
                    if (bucket == null) continue;
                    for (long packed : bucket) {
                        if (withinProtectionRange(packed, target)) return true;
                    }
                }
            }
        }
        return false;
    }

    /** 旧存档迁移只排队真正含令牌的已生成区块，不在加载回调里扫描方块。 */
    public static void queueReconciliation(ServerLevel level, LevelChunk chunk) {
        if (!mayContainToken(chunk) || PENDING_CHUNKS.size() >= MAX_PENDING_CHUNKS) return;
        PendingKey key = new PendingKey(level, chunk.getPos().toLong());
        if (PENDING_KEYS.add(key)) PENDING_CHUNKS.offer(new PendingChunk(key, 0));
    }

    public static void tickPending(MinecraftServer server) {
        int budget = Math.min(MAX_RECONCILIATIONS_PER_TICK, PENDING_CHUNKS.size());
        for (int processed = 0; processed < budget; processed++) {
            PendingChunk pending = PENDING_CHUNKS.poll();
            if (pending == null) return;
            PendingKey key = pending.key();
            ServerLevel level = key.level();
            if (level.getServer() != server) {
                PENDING_KEYS.remove(key);
                continue;
            }
            ChunkPos chunkPos = new ChunkPos(key.chunkPos());
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null || !chunk.getFullStatus().isOrAfter(FullChunkStatus.FULL)) {
                requeueOrDiscard(pending);
                continue;
            }
            get(level).reconcileChunk(chunk);
            PENDING_KEYS.remove(key);
        }
    }

    public static void clearPending(MinecraftServer server) {
        PENDING_CHUNKS.removeIf(pending -> pending.key().level().getServer() == server);
        PENDING_KEYS.removeIf(key -> key.level().getServer() == server);
    }

    private static void requeueOrDiscard(PendingChunk pending) {
        if (pending.attempts() < MAX_RECONCILIATION_RETRIES) {
            PENDING_CHUNKS.offer(new PendingChunk(pending.key(), pending.attempts() + 1));
        } else {
            PENDING_KEYS.remove(pending.key());
        }
    }

    private static boolean mayContainToken(LevelChunk chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (section.maybeHas(state -> state.is(WuXingBlocks.CAVE_TOKEN.get()))) return true;
        }
        return false;
    }

    private void reconcileChunk(LevelChunk chunk) {
        Set<Long> found = new LinkedHashSet<>();
        chunk.findBlocks(state -> state.is(WuXingBlocks.CAVE_TOKEN.get()),
                (pos, state) -> found.add(pos.asLong()));

        boolean changed = false;
        long chunkKey = chunk.getPos().toLong();
        Set<Long> tracked = positionsByChunk.get(chunkKey);
        if (tracked != null) {
            for (long packed : new LinkedHashSet<>(tracked)) {
                if (!found.contains(packed)) changed |= removePacked(packed);
            }
        }
        for (long packed : found) {
            changed |= addPacked(packed);
            ensureForcedChunks((ServerLevel) chunk.getLevel(), BlockPos.of(packed));
        }
        if (changed) setDirty();
    }

    private boolean addPacked(long packed) {
        if (!positions.add(packed)) return false;
        positionsByCell.computeIfAbsent(cellKey(packed), ignored -> new LinkedHashSet<>()).add(packed);
        positionsByChunk.computeIfAbsent(chunkKey(packed), ignored -> new LinkedHashSet<>()).add(packed);
        return true;
    }

    private boolean removePacked(long packed) {
        if (!positions.remove(packed)) return false;
        removeFromBucket(positionsByCell, cellKey(packed), packed);
        removeFromBucket(positionsByChunk, chunkKey(packed), packed);
        return true;
    }

    private static void removeFromBucket(Map<Long, Set<Long>> buckets, long key, long packed) {
        Set<Long> bucket = buckets.get(key);
        if (bucket == null) return;
        bucket.remove(packed);
        if (bucket.isEmpty()) buckets.remove(key);
    }

    private static long cellKey(long packed) {
        return BlockPos.asLong(
                Math.floorDiv(BlockPos.getX(packed), CELL_SIZE),
                Math.floorDiv(BlockPos.getY(packed), CELL_SIZE),
                Math.floorDiv(BlockPos.getZ(packed), CELL_SIZE));
    }

    private static long chunkKey(long packed) {
        return ChunkPos.asLong(BlockPos.getX(packed) >> 4, BlockPos.getZ(packed) >> 4);
    }

    private static boolean withinProtectionRange(long packed, BlockPos target) {
        int radius = CaveTokenBlock.PROTECTION_RADIUS;
        return Math.abs(target.getX() - BlockPos.getX(packed)) <= radius
                && Math.abs(target.getY() - BlockPos.getY(packed)) <= radius
                && Math.abs(target.getZ() - BlockPos.getZ(packed)) <= radius;
    }

    private record PendingKey(ServerLevel level, long chunkPos) {}
    private record PendingChunk(PendingKey key, int attempts) {}
}
