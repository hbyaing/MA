package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** 在地表浅层生成灵液湖，并在湖岸草方块上生成发光蓝蘑菇。 */
public final class SpiritLiquidLakeFeature extends Feature<NoneFeatureConfiguration> {
    public SpiritLiquidLakeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int centerX = origin.getX() + random.nextInt(16);
        int centerZ = origin.getZ() + random.nextInt(16);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG,
                new BlockPos(centerX, 0, centerZ));
        int centerY = surface.getY() - 1;
        if (centerY < 10 || centerY > 30) return false;

        int radiusX = 2 + random.nextInt(5);
        int radiusZ = 2 + random.nextInt(5);
        int depth = 1 + random.nextInt(4);
        BlockState liquid = ModFluids.SPIRIT_LIQUID_SOURCE.get().defaultFluidState().createLegacyBlock();
        boolean placed = false;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int z = -radiusZ; z <= radiusZ; z++) {
                double ellipse = x * x / (double) (radiusX * radiusX)
                        + z * z / (double) (radiusZ * radiusZ);
                if (ellipse > 1.0D) continue;
                int columnDepth = 1 + (int) Math.floor((1.0D - ellipse) * (depth - 1));
                for (int y = centerY; y > centerY - columnDepth; y--) {
                    BlockPos pos = new BlockPos(centerX + x, y, centerZ + z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.STONE)
                            && !state.is(Blocks.DIRT) && !state.is(Blocks.GRAVEL)) continue;
                    // 仅替换湖心范围内的地表，湖岸及范围外的草方块保持不动。
                    level.setBlock(pos, liquid, 2);
                    placed = true;
                }
            }
        }
        if (!placed) return false;

        // 发光蓝蘑菇的地表装饰已移除：它的正式自然来源是大型洞穴与地下水附近（SpiritPlantFeature CAVE 规则）。
        return true;
    }
}
