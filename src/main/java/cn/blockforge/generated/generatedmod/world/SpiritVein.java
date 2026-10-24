// Adapted from hbyaing/xiantu r115 (ff253fa), retaining the original vein algorithm.
package cn.blockforge.generated.generatedmod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

/** Deterministic candidate locations; only successfully placed veins provide aura or detection. */
public final class SpiritVein {

    /** 每个区块成为灵脉核心的概率（百分比）。 */
    public static final int VEIN_CHANCE_PERCENT = 3;
    /** 灵气场的作用半径（格），超出这个距离就感受不到了。 */
    public static final int AURA_RADIUS = 40;
    /** 灵气场最大浓度。 */
    public static final int MAX_CONCENTRATION = 100;
    /** 罗盘搜索灵脉的最大区块半径（24 区块 = 384 格）。 */
    public static final int SEARCH_CHUNK_RADIUS = 24;
    /** 灵脉可以出现的深度范围。 */
    public static final int MIN_Y = -58;
    public static final int MAX_Y = 24;

    private SpiritVein() {
    }

    /** 把种子与区块坐标混成一个稳定的 64 位散列。 */
    public static long mix(long seed, int x, int z) {
        long h = seed + 0x9E3779B97F4A7C15L;
        h ^= (long) x * 0xBF58476D1CE4E5B9L;
        h ^= (long) z * 0x94D049BB133111EBL;
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        return h;
    }

    /** 这个区块是不是一条灵脉的核心。 */
    public static boolean hasVein(long seed, int chunkX, int chunkZ) {
        return Math.floorMod(chunkX,3)==1 && Math.floorMod(chunkZ,3)==1 && Math.floorMod(mix(seed, chunkX, chunkZ),100L)<27;
    }

    /** 灵脉规模 1~5；规模决定数量和灵气，灵石品级在采掘时抽取。 */
    public static int richness(long seed, int chunkX, int chunkZ) {
        int roll = (int) Math.floorMod(mix(seed ^ 0x5DEECE66DL, chunkX, chunkZ), 100L);
        if (roll < 40) {
            return 1;
        }
        if (roll < 68) {
            return 2;
        }
        if (roll < 86) {
            return 3;
        }
        if (roll < 96) {
            return 4;
        }
        return 5;
    }

    /** 灵脉核心位置（世界坐标），地物与灵气场共用。 */
    public static BlockPos center(long seed, int chunkX, int chunkZ) {
        RandomSource random = RandomSource.create(mix(seed ^ 0xA5A5A5A5L, chunkX, chunkZ));
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        int y = MIN_Y + random.nextInt(MAX_Y - MIN_Y + 1);
        return new BlockPos(x, y, z);
    }

    public static int oreRadius(int size) { return new int[]{2,3,4,5,7}[Math.max(1,Math.min(5,size))-1]; }
    public static int auraRadius(int size) { return new int[]{12,16,22,28,40}[Math.max(1,Math.min(5,size))-1]; }
    public static int peak(int size) { return new int[]{28,45,65,85,100}[Math.max(1,Math.min(5,size))-1]; }
    public static int auraAt(int size, double distance) {
        int radius=auraRadius(size); if(distance>=radius) return 0;
        int core=oreRadius(size);
        if(distance<=core) return peak(size);
        return Math.max(0,(int)Math.floor(peak(size)*(radius-distance)/(radius-core)));
    }
    /** 灵脉的灵气强度，品阶越高越浓。 */
    public static float strength(long seed, int chunkX, int chunkZ) {
        return peak(richness(seed, chunkX, chunkZ));
    }
}
