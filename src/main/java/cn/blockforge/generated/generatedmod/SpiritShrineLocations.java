package cn.blockforge.generated.generatedmod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** 记录已经生成的灵修者结构坐标，供 /locate 查询并跨存档保存。 */
public final class SpiritShrineLocations extends SavedData {
    private static final String DATA_NAME = "wuxingxiantu_spirit_shrines";
    private static final String POSITIONS_TAG = "Positions";
    private final List<Long> positions = new ArrayList<>();

    public static SpiritShrineLocations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SpiritShrineLocations::load, SpiritShrineLocations::new, DATA_NAME);
    }

    private static SpiritShrineLocations load(CompoundTag tag) {
        SpiritShrineLocations data = new SpiritShrineLocations();
        for (long packed : tag.getLongArray(POSITIONS_TAG)) {
            if (!data.positions.contains(packed)) data.positions.add(packed);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] packed = new long[positions.size()];
        for (int i = 0; i < positions.size(); i++) packed[i] = positions.get(i);
        tag.putLongArray(POSITIONS_TAG, packed);
        return tag;
    }

    public void add(BlockPos origin) {
        long packed = origin.asLong();
        if (!positions.contains(packed)) {
            positions.add(packed);
            setDirty();
        }
    }

    public BlockPos nearest(BlockPos from) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (long packed : positions) {
            BlockPos candidate = BlockPos.of(packed);
            double distance = candidate.distSqr(from);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    public int size() {
        return positions.size();
    }
}
