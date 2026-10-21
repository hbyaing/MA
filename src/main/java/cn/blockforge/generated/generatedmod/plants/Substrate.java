package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 灵植的合法种植基质（硬性条件）。所有判定集中在这里，
 * 禁止在各事件类里按具体植物散落的 if-else。
 * 灵壤通过方块标签 wuxingxiantu:spirit_soil 判定，只有一种，无品级。
 */
public enum Substrate {
    /** 普通土地即可：泥土系、草方块、沙砾（野外），灵壤同样可以。 */
    SOIL {
        @Override public boolean floorOk(BlockState floor) {
            return floor.is(BlockTags.DIRT) || floor.is(Blocks.GRASS_BLOCK)
                    || floor.is(Blocks.SAND) || floor.is(Blocks.RED_SAND) || floor.is(Blocks.GRAVEL)
                    || isSpiritSoil(floor);
        }
    },
    /** 只接受土地与灵壤（不认沙砾），用于洞穴菌类等。 */
    SOIL_STRICT {
        @Override public boolean floorOk(BlockState floor) {
            return floor.is(BlockTags.DIRT) || floor.is(Blocks.GRASS_BLOCK) || isSpiritSoil(floor);
        }
    },
    /** 沉水：方块格必须处于水中（水或 waterlogged）。 */
    WATER {
        @Override public boolean floorOk(BlockState floor) {
            return floor.is(BlockTags.DIRT) || floor.is(Blocks.GRASS_BLOCK)
                    || floor.is(Blocks.SAND) || floor.is(Blocks.GRAVEL) || floor.is(Blocks.CLAY)
                    || floor.is(Blocks.STONE) || floor.is(Blocks.COBBLESTONE)
                    || floor.is(Blocks.GRANITE) || floor.is(Blocks.DIORITE) || floor.is(Blocks.ANDESITE)
                    || floor.is(BlockTags.BASE_STONE_OVERWORLD) || isSpiritSoil(floor);
        }
        @Override public boolean cellOk(BlockGetter level, BlockPos pos, BlockState state) {
            return submerged(level, pos);
        }
    },
    /** 浮水或浅水（清心莲）：本格有水，或脚下就是水源。 */
    WATER_OR_ABOVE {
        @Override public boolean floorOk(BlockState floor) {
            return WATER.floorOk(floor);
        }
        @Override public boolean cellOk(BlockGetter level, BlockPos pos, BlockState state) {
            return submerged(level, pos) || level.getFluidState(pos.below()).is(Fluids.WATER);
        }
    },
    /** 末地石或灵壤（龙鳞草）。 */
    END_STONE_OR_SPIRIT {
        @Override public boolean floorOk(BlockState floor) {
            return floor.is(Blocks.END_STONE) || isSpiritSoil(floor);
        }
    },
    /** 岩壁与腐殖层皆可（藤蔓类野外）：石系 + 泥土系 + 灵壤。 */
    ROCKY {
        @Override public boolean floorOk(BlockState floor) {
            return floor.is(BlockTags.DIRT) || floor.is(Blocks.GRASS_BLOCK)
                    || floor.is(Blocks.STONE) || floor.is(Blocks.GRAVEL) || floor.is(Blocks.MOSS_BLOCK)
                    || floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(Blocks.DEEPSLATE)
                    || floor.is(Blocks.TUFF) || floor.is(Blocks.COBBLESTONE) || isSpiritSoil(floor);
        }
    };

    /** 下方方块是否可以扎根。 */
    public abstract boolean floorOk(BlockState floor);

    /** 方块所在格本身是否合法（默认要求空气或可替代）。 */
    public boolean cellOk(BlockGetter level, BlockPos pos, BlockState state) {
        return true;
    }

    /** 灵壤方块标签，只有一种灵壤，无品级。 */
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SPIRIT_SOIL =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    new net.minecraft.resources.ResourceLocation("wuxingxiantu", "spirit_soil"));

    public static boolean isSpiritSoil(BlockState state) {
        return state.is(SPIRIT_SOIL);
    }

    public static boolean isSpiritLiquid(FluidState fluid) {
        return fluid.is(cn.blockforge.generated.generatedmod.registry.ModFluids.SPIRIT_LIQUID_SOURCE.get())
                || fluid.is(cn.blockforge.generated.generatedmod.registry.ModFluids.SPIRIT_LIQUID_FLOWING.get());
    }

    public static boolean isPlantWater(FluidState fluid) {
        return fluid.is(Fluids.WATER) || isSpiritLiquid(fluid);
    }

    /** 本格是否有水（水源、流水或 waterlogged 状态均可）。 */
    public static boolean submerged(BlockGetter level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(Fluids.WATER)) return true;
        BlockState state = level.getBlockState(pos);
        return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
    }
}
