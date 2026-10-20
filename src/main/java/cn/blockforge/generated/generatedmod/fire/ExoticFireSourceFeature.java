package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Small, bounded searches within the decorated column; biome selection is data-driven. */
public final class ExoticFireSourceFeature extends Feature<ExoticFireSourceFeature.Config> {
    public record Config(int tier) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = Codec.intRange(1, 9).fieldOf("tier").xmap(Config::new, Config::tier).codec();
    }
    public ExoticFireSourceFeature() { super(Config.CODEC); }
    @Override public boolean place(FeaturePlaceContext<Config> context) {
        var level = context.level();
        var origin = context.origin();
        int tier = context.config().tier();
        if (tier == 5) {
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin);
            return level.getFluidState(pos).is(FluidTags.WATER) && place(level, pos, tier, true);
        }
        if (tier == 4) {
            BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin);
            for (int y = 0; y < 40 && top.getY() - y > level.getMinBuildHeight(); y++) {
                BlockPos pos = top.below(y);
                if (!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) continue;
                boolean wood = false;
                for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-3, 0, -3), pos.offset(3, 4, 3))) {
                    if (level.getBlockState(nearby).is(BlockTags.LOGS)) { wood = true; break; }
                }
                if (wood) return place(level, pos, tier, false);
            }
            return false;
        }
        if (tier != 1 && tier != 3) return false;
        int ceiling = Math.min(level.getMaxBuildHeight() - 2, tier == 3 ? 110 : 96);
        for (int y = ceiling; y > Math.max(level.getMinBuildHeight() + 1, tier == 3 ? 30 : -54); y--) {
            BlockPos pos = new BlockPos(origin.getX(), y, origin.getZ());
            if (!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            if (tier == 3 && (level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.SOUL_SAND)
                    || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.SOUL_SOIL))) return place(level, pos, tier, false);
            if (tier == 1) {
                for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 0, 3))) {
                    if (level.getFluidState(nearby).is(FluidTags.LAVA)) return place(level, pos, tier, false);
                }
            }
        }
        return false;
    }
    private static boolean place(WorldGenLevel level, BlockPos pos, int tier, boolean water) {
        return level.setBlock(pos, WuXingBlocks.EXOTIC_FIRE_SOURCE.get().defaultBlockState()
                .setValue(ExoticFireSourceBlock.FIRE_TIER, tier).setValue(BlockStateProperties.WATERLOGGED, water), 2);
    }
}
