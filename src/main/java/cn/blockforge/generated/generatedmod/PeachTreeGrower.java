package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;

/** 桃树苗使用原版樱花树的完整树形，仅替换为桃木和桃树叶。 */
public final class PeachTreeGrower extends AbstractTreeGrower {
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean bees) {
        return TreeFeatures.CHERRY;
    }

    @Override
    public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos,
                            BlockState sapling, RandomSource random) {
        // 先让原版特征生成樱花树，确保桃树的高度、分枝和冠幅与樱花树完全一致。
        if (!super.growTree(level, generator, pos, sapling, random)) return false;
        replaceCherryBlocks(level, pos);
        return true;
    }

    private static void replaceCherryBlocks(ServerLevel level, BlockPos origin) {
        BlockState peachLog = WuXingBlocks.PEACH_WOOD.get().defaultBlockState();
        BlockState peachLeaves = WuXingBlocks.PEACH_LEAVES.get().defaultBlockState();
        // 樱花树特征的最大范围约为半径 8、高度 20；只替换刚生成的樱花方块。
        for (int x = -8; x <= 8; x++) {
            for (int y = 0; y <= 20; y++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos blockPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(blockPos);
                    if (state.is(Blocks.CHERRY_LOG)) {
                        level.setBlock(blockPos, peachLog.setValue(RotatedPillarBlock.AXIS,
                                state.getValue(RotatedPillarBlock.AXIS)), 3);
                    } else if (state.is(Blocks.CHERRY_LEAVES)) {
                        level.setBlock(blockPos, peachLeaves
                                .setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT))
                                .setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE)), 3);
                    }
                }
            }
        }
    }
}
