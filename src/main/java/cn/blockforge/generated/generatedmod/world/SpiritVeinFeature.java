// Adapted from hbyaing/xiantu r115 (ff253fa), retaining the original vein algorithm.
package cn.blockforge.generated.generatedmod.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 灵脉地物：仅替换地下矿洞侧壁上的岩石，不生成埋藏矿团。
 * <p>是否成脉、脉心在哪、多粗，全部由 {@link SpiritVein} 按种子算出来，
 * 成功生成后登记，供灵气场和探测器共用。</p>
 */
public class SpiritVeinFeature extends Feature<NoneFeatureConfiguration> {

    public SpiritVeinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        ChunkPos candidate = new ChunkPos(origin);
        SpiritVeinData.get(level.getLevel()).markDecorationChunk(candidate);
        if (!SpiritVein.hasVein(level.getSeed(), candidate.x, candidate.z)) return false;
        SpiritVeinData.get(level.getLevel()).queue(candidate);
        return true;
    }
    public static boolean generate(WorldGenLevel level, BlockPos origin) {
        long seed = level.getSeed();
        ChunkPos chunk = new ChunkPos(origin);
        if (!SpiritVein.hasVein(seed, chunk.x, chunk.z)) {
            return false;
        }

        var data=SpiritVeinData.get(level.getLevel());
        if(!data.canDecorate(chunk))return false;
        int requested = SpiritVein.richness(seed, chunk.x, chunk.z);
        java.util.Set<BlockPos> walls = new java.util.HashSet<>();
        for (int x=chunk.getMinBlockX()-15;x<=chunk.getMaxBlockX()+15;x++)
            for(int z=chunk.getMinBlockZ()-15;z<=chunk.getMaxBlockZ()+15;z++)
                for(int y=Math.max(SpiritVein.MIN_Y,level.getMinBuildHeight()+1);y<=SpiritVein.MAX_Y;y++) {
                    BlockPos p=new BlockPos(x,y,z);
                    if(!data.canDecorate(new ChunkPos(p)))continue;
                    BlockState state=level.getBlockState(p);
                    if((state.is(BlockTags.STONE_ORE_REPLACEABLES)||state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) && isCaveWall(level,p)) walls.add(p);
                }
        // 只选六面相邻的最大连续洞壁；BFS前缀本身也保持连通。
        java.util.List<BlockPos> largest=ConnectedVeinShape.largest(walls,
                p -> java.util.Arrays.stream(Direction.values()).map(p::relative).toList(),
                java.util.Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ()));
        int minimum=requested==5?256:64;
        if(largest.size()<minimum)return false;
        int target=new int[]{96,144,192,255,512}[requested-1];
        java.util.List<BlockPos> ores=largest.subList(0,Math.min(target,largest.size()));
        int actual=requested==5?5:ores.size()>=192?4:ores.size()>=144?3:ores.size()>=96?2:1;
        for(int i=0;i<ores.size();i++) {
            level.setBlock(ores.get(i),cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_VEIN_ORE.get().defaultBlockState(),2);
        }
        // 脉心取实际矿带内、靠近质心的一块矿，不再指向随机岩层坐标。
        double ax=ores.stream().mapToInt(BlockPos::getX).average().orElseThrow(),ay=ores.stream().mapToInt(BlockPos::getY).average().orElseThrow(),az=ores.stream().mapToInt(BlockPos::getZ).average().orElseThrow();
        BlockPos center=ores.stream().min(java.util.Comparator.comparingDouble(p->(p.getX()-ax)*(p.getX()-ax)+(p.getY()-ay)*(p.getY()-ay)+(p.getZ()-az)*(p.getZ()-az))).orElseThrow();
        SpiritVeinData.get(level.getLevel()).register(chunk.x,chunk.z,actual,center,ores.size());
        return true;
    }

    /** 必须是地表下的侧壁，水平邻接至少两格高的干燥洞内空气。 */
    private static boolean isCaveWall(WorldGenLevel level, BlockPos pos) {
        if (level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) - pos.getY() < 8) return false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos air = pos.relative(side);
            if (level.getBlockState(air).isAir() && level.getBlockState(air.above()).isAir()) return true;
        }
        return false;
    }

}
