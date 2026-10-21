package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * 跨重启保存异火位置和品阶。位置按维度分别保存在该维度的 level data 中，
 * 避免把不同维度的坐标混在一起，也避免依赖易失的静态 Map。
 */
public final class ExoticFireSavedData extends SavedData {
    private static final String DATA_NAME = "wuxingxiantu_exotic_fires";
    private static final String FIRES = "Fires";
    private static final String POS = "Pos";
    private static final String TIER = "Tier";

    private final Map<BlockPos, Integer> fires = new HashMap<>();

    public static ExoticFireSavedData load(CompoundTag tag) {
        ExoticFireSavedData data = new ExoticFireSavedData();
        ListTag list = tag.getList(FIRES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.contains(POS, Tag.TAG_COMPOUND)) continue;
            BlockPos pos = NbtUtils.readBlockPos(entry.getCompound(POS));
            int tier = entry.getInt(TIER);
            if (tier >= 1 && tier <= 9) data.fires.put(pos.immutable(), tier);
        }
        return data;
    }

    public static ExoticFireSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ExoticFireSavedData::load,
                ExoticFireSavedData::new, DATA_NAME);
    }

    public static ExoticFireSavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("ExoticFireSavedData is server-side only");
        }
        return get(serverLevel);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Integer> entry : fires.entrySet()) {
            CompoundTag fire = new CompoundTag();
            fire.put(POS, NbtUtils.writeBlockPos(entry.getKey()));
            fire.putInt(TIER, ExoticFireColorRegistry.clampTier(entry.getValue()));
            list.add(fire);
        }
        tag.put(FIRES, list);
        return tag;
    }

    public Map<BlockPos, Integer> snapshot() {
        return new HashMap<>(fires);
    }

    public void put(BlockPos pos, int tier) {
        int safeTier = ExoticFireColorRegistry.clampTier(tier);
        Integer previous = fires.put(pos.immutable(), safeTier);
        if (previous == null || previous != safeTier) setDirty();
    }

    public void remove(BlockPos pos) {
        if (fires.remove(pos) != null) setDirty();
    }

    public void removeIf(java.util.function.Predicate<BlockPos> predicate) {
        if (fires.keySet().removeIf(predicate)) setDirty();
    }
}
