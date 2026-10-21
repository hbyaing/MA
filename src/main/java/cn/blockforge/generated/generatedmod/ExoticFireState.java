package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** 独立异火状态及旧版 minecraft:fire 品阶记录的兼容迁移。 */
public final class ExoticFireState {
    private static final Map<Level, Map<BlockPos, Integer>> FIRES = new WeakHashMap<>();
    private static final Map<Level, Map<BlockPos, Integer>> CLIENT_FIRES = new WeakHashMap<>();
    private static final Map<Level, Map<BlockPos, Integer>> ETERNAL_COALS = new WeakHashMap<>();

    private ExoticFireState() {}

    public static boolean isFire(BlockState state) {
        return state.is(Blocks.FIRE) || state.getBlock() instanceof ExoticFireBlock;
    }

    public static BlockState stateForTier(int tier, BlockState previous) {
        ExoticFireBlock fire = (ExoticFireBlock) WuXingBlocks.EXOTIC_FIRES[
                ExoticFireColorRegistry.clampTier(tier) - 1].get();
        return fire.copyFireState(previous);
    }

    public static int get(Level level, BlockPos pos) {
        // 品阶随方块包同步，不再依赖额外网络包到达顺序或二十格同步半径。
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ExoticFireBlock fire) return fire.tier();
        if (!state.is(Blocks.FIRE)) return 0;
        Map<BlockPos, Integer> fires = level.isClientSide ? CLIENT_FIRES.get(level) : FIRES.get(level);
        return fires == null ? 0 : fires.getOrDefault(pos, 0);
    }

    public static int clientVisualTier(Level level, BlockPos pos) { return get(level, pos); }

    public static Map<BlockPos, Integer> clientSnapshot(Level level) {
        Map<BlockPos, Integer> fires = CLIENT_FIRES.get(level);
        return !level.isClientSide || fires == null ? Map.of() : new HashMap<>(fires);
    }

    public static Set<BlockPos> replaceClient(Level level, Map<BlockPos, Integer> snapshot) {
        if (!level.isClientSide) return Set.of();
        Map<BlockPos, Integer> target = CLIENT_FIRES.computeIfAbsent(level, ignored -> new HashMap<>());
        Set<BlockPos> dirty = new java.util.HashSet<>(target.keySet());
        dirty.addAll(snapshot.keySet());
        dirty.removeIf(pos -> java.util.Objects.equals(target.get(pos), snapshot.get(pos)));
        target.clear();
        target.putAll(snapshot);
        return dirty;
    }

    public static Map<BlockPos, Integer> visibleSnapshot(ServerPlayer player, int radius) {
        Map<BlockPos, Integer> result = new HashMap<>();
        ServerLevel level = player.serverLevel();
        Map<BlockPos, Integer> fires = FIRES.get(level);
        if (fires == null) return result;
        BlockPos center = player.blockPosition();
        for (var entry : fires.entrySet()) {
            BlockPos pos = entry.getKey();
            if (Math.abs(pos.getX() - center.getX()) > radius || Math.abs(pos.getY() - center.getY()) > radius
                    || Math.abs(pos.getZ() - center.getZ()) > radius) continue;
            LevelChunk chunk = loadedChunk(level, pos);
            if (chunk != null && isFire(chunk.getBlockState(pos))) result.put(pos, entry.getValue());
        }
        return result;
    }

    public static void set(Level level, BlockPos pos, int tier) {
        if (level.isClientSide || !isFire(level.getBlockState(pos))) return;
        int safeTier = ExoticFireColorRegistry.clampTier(tier);
        FIRES.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), safeTier);
        ExoticFireSavedData.get(level).put(pos, safeTier);
        BlockState current = level.getBlockState(pos);
        if (!(current.getBlock() instanceof ExoticFireBlock fire) || fire.tier() != safeTier) {
            level.setBlock(pos, stateForTier(safeTier, current), Block.UPDATE_ALL);
        }
    }

    public static void predictClient(Level level, BlockPos pos, int tier) {
        if (!level.isClientSide) return;
        int safeTier = ExoticFireColorRegistry.clampTier(tier);
        CLIENT_FIRES.computeIfAbsent(level, ignored -> new HashMap<>()).put(pos.immutable(), safeTier);
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || isFire(current)) {
            level.setBlock(pos, stateForTier(safeTier,
                    net.minecraft.world.level.block.BaseFireBlock.getState(level, pos)), Block.UPDATE_ALL);
        }
    }

    public static boolean inheritFromNearby(Level level, BlockPos firePos) {
        if (level.isClientSide || !level.getBlockState(firePos).is(Blocks.FIRE)) return false;
        int tier = 0;
        for (Direction direction : Direction.values()) tier = Math.max(tier, get(level, firePos.relative(direction)));
        if (tier == 0) return false;
        set(level, firePos, tier);
        return true;
    }

    /** 蔓延现在由 ExoticFireBlock.tick 在同一 tick 内继承，不再误染附近普通火。 */
    public static Set<BlockPos> inheritNearbyFires(Level level) { return Set.of(); }

    public static void setEternalCoal(Level level, BlockPos coalPos, int tier) {
        if (!level.isClientSide) ETERNAL_COALS.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(coalPos.immutable(), ExoticFireColorRegistry.clampTier(tier));
    }

    /** 区块加载回调中仅恢复索引，等常规世界 tick 再迁移，避免加载过程中递归触碰区块。 */
    public static void restoreChunk(ServerLevel level, LevelChunk chunk) {
        if (chunk.getLevel() != level) return;
        Map<BlockPos, Integer> fires = FIRES.computeIfAbsent(level, ignored -> new HashMap<>());
        Map<BlockPos, Integer> coals = ETERNAL_COALS.computeIfAbsent(level, ignored -> new HashMap<>());
        ExoticFireSavedData data = ExoticFireSavedData.get(level);
        for (var entry : data.snapshot().entrySet()) {
            BlockPos pos = entry.getKey();
            if (!new ChunkPos(pos).equals(chunk.getPos())) continue;
            if (isFire(chunk.getBlockState(pos))) {
                fires.put(pos.immutable(), entry.getValue());
                if (chunk.getBlockState(pos.below()).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) {
                    coals.put(pos.below().immutable(), entry.getValue());
                }
            } else data.remove(pos);
        }
        if (coals.isEmpty()) ETERNAL_COALS.remove(level);
    }

    public static void unloadChunk(ServerLevel level, ChunkPos chunkPos) {
        for (var index : java.util.List.of(FIRES, ETERNAL_COALS)) {
            Map<BlockPos, Integer> entries = index.get(level);
            if (entries == null) continue;
            entries.keySet().removeIf(pos -> new ChunkPos(pos).equals(chunkPos));
            if (entries.isEmpty()) index.remove(level);
        }
    }

    private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        return level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
    }

    public static boolean extinguishEternalFire(Level level, BlockPos clickedPos) {
        BlockPos firePos;
        if (isFire(level.getBlockState(clickedPos))) firePos = clickedPos;
        else if (level.getBlockState(clickedPos).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())
                && isFire(level.getBlockState(clickedPos.above()))) firePos = clickedPos.above();
        else return false;
        BlockPos coalPos = firePos.below();
        if (!level.getBlockState(coalPos).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) return false;
        Map<BlockPos, Integer> coals = ETERNAL_COALS.get(level);
        if (coals != null) {
            coals.remove(coalPos);
            if (coals.isEmpty()) ETERNAL_COALS.remove(level);
        }
        remove(level, firePos);
        level.setBlock(firePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (!level.isClientSide) {
            BlockPos core = RefiningForgeStructure.findCore(level, coalPos);
            if (core != null) RefiningForgeBlock.setStructureLit(level, core, false);
        }
        return true;
    }

    public static void remove(Level level, BlockPos pos) {
        if (!level.isClientSide) ExoticFireSavedData.get(level).remove(pos);
        for (var index : java.util.List.of(FIRES, CLIENT_FIRES)) {
            Map<BlockPos, Integer> entries = index.get(level);
            if (entries == null) continue;
            entries.remove(pos);
            if (entries.isEmpty()) index.remove(level);
        }
    }

    public static void cleanup(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Map<BlockPos, Integer> fires = FIRES.get(level);
        if (fires != null) {
            for (var entry : new HashMap<>(fires).entrySet()) {
                BlockPos pos = entry.getKey();
                LevelChunk chunk = loadedChunk(serverLevel, pos);
                if (chunk == null) continue;
                BlockState state = chunk.getBlockState(pos);
                if (state.is(Blocks.FIRE)) {
                    set(level, pos, entry.getValue()); // 旧存档原位替换，保留品阶与附着面。
                } else if (!isFire(state)) {
                    fires.remove(pos);
                    ExoticFireSavedData.get(serverLevel).remove(pos);
                    if (chunk.getBlockState(pos.below()).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) {
                        BlockPos core = RefiningForgeStructure.findCore(level, pos.below());
                        if (core != null) RefiningForgeBlock.setStructureLit(level, core, false);
                    }
                }
            }
            if (fires.isEmpty()) FIRES.remove(level);
        }
        Map<BlockPos, Integer> coals = ETERNAL_COALS.get(level);
        if (coals != null) {
            coals.keySet().removeIf(pos -> {
                LevelChunk chunk = loadedChunk(serverLevel, pos);
                return chunk != null && !chunk.getBlockState(pos).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get());
            });
            if (coals.isEmpty()) ETERNAL_COALS.remove(level);
        }
    }

    public static void keepEternalFires(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Map<BlockPos, Integer> coals = ETERNAL_COALS.get(level);
        if (coals == null) return;
        for (var entry : new HashMap<>(coals).entrySet()) {
            BlockPos coalPos = entry.getKey();
            LevelChunk chunk = loadedChunk(serverLevel, coalPos);
            if (chunk == null || !chunk.getBlockState(coalPos).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) continue;
            BlockPos firePos = coalPos.above();
            BlockState state = chunk.getBlockState(firePos);
            if (state.isAir()) {
                level.setBlock(firePos, stateForTier(entry.getValue(), Blocks.FIRE.defaultBlockState()), Block.UPDATE_ALL);
            } else if (state.is(Blocks.FIRE)) set(level, firePos, entry.getValue());
        }
    }
}
