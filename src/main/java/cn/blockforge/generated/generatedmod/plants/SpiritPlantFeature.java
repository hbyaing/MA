package cn.blockforge.generated.generatedmod.plants;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * 灵植专用植被 patch 特性：按生态规则挑选合法落点，替代"全生物群系随机刷"。
 * 规则：
 * <ul>
 *   <li>SURFACE_SOIL —— 草/土/沙等地表（配合生物群系列表体现生境差异）；</li>
 *   <li>SURFACE_ROCKY —— 山地岩壁与苔藓；</li>
 *   <li>SURFACE_SANDY —— 沙漠、恶地沙面与陶瓦；</li>
 *   <li>WATER_BODY   —— 任意深度水体：从湖底/海床向上找 1..6 格水；</li>
 *   <li>WATER_SURFACE—— 浮水（清心莲）：水面格；</li>
 *   <li>CAVE_FLOOR   —— 地下洞底（含泥土、苔藓、石头），低亮度要求；</li>
 *   <li>CAVE_WATER   —— 地下水体（玄水藻）；</li>
 *   <li>END_MAIN_ISLAND —— 末地主岛末地石面（含距中心距离窗口，保护龙战斗场）。</li>
 * </ul>
 *
 * <p>所有硬性基质/水体判定统一委托 {@link SpiritPlantDefinition#canPlaceAt}，
 * 与玩家手动种植走同一套规则，野外落点必然可存活。</p>
 */
public final class SpiritPlantFeature extends Feature<SpiritPlantFeature.Config> {

    public enum Rule {
        SURFACE_SOIL, SURFACE_ROCKY, SURFACE_SANDY,
        SURFACE_NEAR_WATER, FOREST_FLOOR, SURFACE_ROCK_EDGE, SURFACE_HIGHLAND,
        WATER_BODY, WATER_SURFACE,
        CAVE_FLOOR, CAVE_WATER,
        END_MAIN_ISLAND
    }

    public static final class Config implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockStateConfiguration.CODEC.fieldOf("place").forGetter(c -> c.place),
                Codec.STRING.xmap(Rule::valueOf, rule -> rule.name()).fieldOf("rule").forGetter(c -> c.rule),
                Codec.intRange(1, 64).fieldOf("retries").orElse(8).forGetter(c -> c.retries),
                Codec.intRange(1, 16).fieldOf("xz_spread").orElse(7).forGetter(c -> c.xzSpread),
                Codec.intRange(1, 24).fieldOf("y_range").orElse(6).forGetter(c -> c.yRange),
                Codec.BOOL.fieldOf("random_age").orElse(true).forGetter(c -> c.randomAge),
                ResourceLocation.CODEC.optionalFieldOf("biome_tag").forGetter(c -> c.biomeTag),
                Codec.intRange(-2032, 2031).optionalFieldOf("min_y", -64).forGetter(c -> c.minY),
                Codec.BOOL.optionalFieldOf("open_sky", false).forGetter(c -> c.openSky)
        ).apply(instance, Config::new));

        public final BlockStateConfiguration place;
        public final Rule rule;
        public final int retries;
        public final int xzSpread;
        public final int yRange;
        public final boolean randomAge;
        public final java.util.Optional<ResourceLocation> biomeTag;
        public final int minY;
        public final boolean openSky;

        Config(BlockStateConfiguration place, Rule rule, int retries, int xzSpread, int yRange, boolean randomAge,
               java.util.Optional<ResourceLocation> biomeTag, int minY, boolean openSky) {
            this.place = place;
            this.rule = rule;
            this.retries = retries;
            this.xzSpread = xzSpread;
            this.yRange = yRange;
            this.randomAge = randomAge;
            this.biomeTag = biomeTag;
            this.minY = minY;
            this.openSky = openSky;
        }
    }

    /** 主岛判定：距 XZ 原点 [120, 860] 视为末地主岛范围（末地外岛自 ~1024 格开始）。 */
    private static final double END_MIN_DIST = 120.0D;
    private static final double END_MAX_DIST = 860.0D;

    public SpiritPlantFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState toPlace = config.place.state;
        SpiritPlantBlock plant = toPlace.getBlock() instanceof SpiritPlantBlock p ? p : null;
        WildRarity rarity = plant == null ? WildRarity.COMMON : plant.definition().rarity;
        if (rarity == WildRarity.SPECIAL && (config.rule != Rule.END_MAIN_ISLAND
                || !level.getLevel().dimension().equals(net.minecraft.world.level.Level.END))) return false;
        if (plant == null) return false;

        for (int attempt = 0; attempt < Math.min(config.retries, rarity.attempts); attempt++) {
            int x = origin.getX() + random.nextInt(config.xzSpread * 2 + 1) - config.xzSpread;
            int z = origin.getZ() + random.nextInt(config.xzSpread * 2 + 1) - config.xzSpread;
            BlockPos pos = findSpot(level, random, x, z, config.rule, toPlace, config.yRange);
            if (pos == null) continue;
            // Patch offsets may cross biome borders or descend below the origin's height.
            if (!matchesHabitat(level, pos, config)) continue;
            SpiritPlantRegions.get(level.getLevel()).offer(pos, config);
            return false; // Deferred until the entire connected region boundary is known.
        }
        return false;
    }

    /** Recheck recorded decoration points without generating or loading additional chunks. */
    static boolean placeRecorded(net.minecraft.server.level.ServerLevel level, BlockPos pos, Config config, int age) {
        for (int dx=-4;dx<=4;dx+=4) for (int dz=-4;dz<=4;dz+=4)
            if (!level.hasChunk((pos.getX()+dx)>>4,(pos.getZ()+dz)>>4)) return false;
        SpiritPlantBlock plant = (SpiritPlantBlock)config.place.state.getBlock();
        BlockState state = candidateState(level,plant,config.place.state,pos);
        if (config.randomAge) state=state.setValue(SpiritPlantBlock.AGE,age % (plant.definition().matureAge+1));
        if (!matchesHabitat(level,pos,config) || !placeableAt(level,state,pos)) return false;
        if (!(level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.WATER))) return false;
        boolean valid = switch(config.rule) {
            case SURFACE_NEAR_WATER -> validSurface(level,pos,state,Rule.SURFACE_SOIL) && nearWaterSource(level,pos);
            case FOREST_FLOOR -> validSurface(level,pos,state,Rule.SURFACE_SOIL);
            case SURFACE_ROCK_EDGE -> validSurface(level,pos,state,Rule.SURFACE_SOIL) && nearRockFace(level,pos);
            case SURFACE_HIGHLAND -> validSurface(level,pos,state,Rule.SURFACE_SOIL);
            case SURFACE_SOIL, SURFACE_ROCKY, SURFACE_SANDY -> validSurface(level,pos,state,config.rule);
            case CAVE_FLOOR -> caveFloorOk(level.getBlockState(pos.below())) && level.getMaxLocalRawBrightness(pos)<=7;
            case CAVE_WATER -> level.getFluidState(pos).is(Fluids.WATER)
                    && level.getFluidState(pos.above()).is(Fluids.WATER)
                    && level.getBlockState(pos.below()).isSolidRender(level,pos.below());
            case WATER_BODY -> level.getFluidState(pos).is(Fluids.WATER)
                    && level.getBlockState(pos.below()).isSolidRender(level,pos.below());
            case WATER_SURFACE -> level.getBlockState(pos).isAir() && level.getFluidState(pos.below()).is(Fluids.WATER);
            case END_MAIN_ISLAND -> level.dimension().equals(net.minecraft.world.level.Level.END)
                    && Math.hypot(pos.getX(),pos.getZ())>=END_MIN_DIST && Math.hypot(pos.getX(),pos.getZ())<=END_MAX_DIST
                    && level.getBlockState(pos.below()).is(Blocks.END_STONE);
        };
        if(!valid||!level.setBlock(pos,state,2))return false;
        HerbCultivationData.get(level).wild(level,pos,plant.definition());
        return true;
    }

    /** 目标格的候选放置状态：水生/半水生灵植按本格真实水重估 WATERLOGGED。 */
    private static BlockState candidateState(WorldGenLevel level, SpiritPlantBlock plant,
                                             BlockState toPlace, BlockPos pos) {
        if (plant == null) return toPlace;
        if (plant.definition().aquaticInCell || plant.definition().semiAquatic) {
            return toPlace.setValue(BlockStateProperties.WATERLOGGED,
                    level.getFluidState(pos).is(Fluids.WATER));
        }
        return toPlace;
    }

    /** 硬性条件统一校验：与 SpiritPlantBlock 手动种植完全同一入口。 */
    private static boolean placeableAt(WorldGenLevel level, BlockState toPlace, BlockPos pos) {
        if (!(toPlace.getBlock() instanceof SpiritPlantBlock plant)) return true;
        return plant.definition().canPlaceAt(level, pos, candidateState(level, plant, toPlace, pos));
    }

    private static BlockPos findSpot(WorldGenLevel level, RandomSource random, int x, int z,
                                     Rule rule, BlockState toPlace, int yRange) {
        switch (rule) {
            case SURFACE_NEAR_WATER:
            case FOREST_FLOOR:
            case SURFACE_ROCK_EDGE: {
                BlockPos pos = forestFloor(level, x, z, toPlace);
                if (pos == null) return null;
                if (rule == Rule.SURFACE_NEAR_WATER && !nearWaterSource(level, pos)) return null;
                if (rule == Rule.SURFACE_ROCK_EDGE && !nearRockFace(level, pos)) return null;
                return pos;
            }
            case SURFACE_HIGHLAND: {
                int y = surfaceY(level, x, z);
                if (y == Integer.MIN_VALUE) return null;
                BlockPos pos = new BlockPos(x, y + 1, z);
                return validSurface(level, pos, toPlace, Rule.SURFACE_SOIL) ? pos : null;
            }
            case SURFACE_SOIL: {
                int y = surfaceY(level, x, z);
                if (y == Integer.MIN_VALUE) return null;
                BlockPos pos = new BlockPos(x, y + 1, z);
                return validSurface(level, pos, toPlace, rule) ? pos : null;
            }
            case SURFACE_ROCKY: {
                int y = surfaceY(level, x, z);
                if (y == Integer.MIN_VALUE) return null;
                // 岩壁：地表上方 0..yRange 找可贴壁的落点（空气 + 邻面是石/土）。
                for (int dy = 1; dy <= yRange + 1; dy++) {
                    BlockPos pos = new BlockPos(x, y + dy, z);
                    if (validSurface(level, pos, toPlace, rule)) return pos;
                }
                return null;
            }
            case SURFACE_SANDY: {
                int y = surfaceY(level, x, z);
                if (y == Integer.MIN_VALUE) return null;
                BlockPos pos = new BlockPos(x, y + 1, z);
                return validSurface(level, pos, toPlace, rule) ? pos : null;
            }
            case WATER_BODY: {
                // 从水面往下扫最多 6 格，找到坐在 solid 上的水格。
                int top = surfaceY(level, x, z);
                if (top == Integer.MIN_VALUE) return null;
                for (int dy = 0; dy <= 6; dy++) {
                    BlockPos pos = new BlockPos(x, top - dy, z);
                    if (!level.getFluidState(pos).is(Fluids.WATER)) continue;
                    if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) continue;
                    if (!placeableAt(level, toPlace, pos)) continue;
                    return pos;
                }
                return null;
            }
            case WATER_SURFACE: {
                // 清心莲：水面之上的空气格，脚下是水源。
                int top = surfaceY(level, x, z);
                if (top == Integer.MIN_VALUE) return null;
                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos waterPos = new BlockPos(x, top - dy, z);
                    if (!level.getFluidState(waterPos).is(Fluids.WATER)) continue;
                    BlockPos above = waterPos.above();
                    if (!level.getBlockState(above).isAir()) continue;
                    if (!placeableAt(level, toPlace, above)) continue;
                    return above;
                }
                return null;
            }
            case CAVE_FLOOR: {
                for (int dy = 0; dy < yRange; dy++) {
                    int y = 8 + random.nextInt(50); // y 8..57
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir()) continue;
                    if (!caveFloorOk(level.getBlockState(pos.below()))) continue;
                    if (level.getMaxLocalRawBrightness(pos) > 7) continue;
                    if (!placeableAt(level, toPlace, pos)) continue;
                    return pos;
                }
                return null;
            }
            case CAVE_WATER: {
                for (int dy = 0; dy < yRange; dy++) {
                    int y = 6 + random.nextInt(44);
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getFluidState(pos).is(Fluids.WATER)) continue;
                    if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) continue;
                    if (!level.getFluidState(pos.above()).is(Fluids.WATER)) continue;
                    if (!placeableAt(level, toPlace, pos)) continue;
                    return pos;
                }
                return null;
            }
            case END_MAIN_ISLAND: {
                if (!level.getLevel().dimension()
                        .equals(net.minecraft.world.level.Level.END)) return null;
                double dist = Math.sqrt((double) x * x + (double) z * z);
                if (dist < END_MIN_DIST || dist > END_MAX_DIST) return null;
                int y = surfaceY(level, x, z);
                if (y == Integer.MIN_VALUE) return null;
                BlockPos pos = new BlockPos(x, y + 1, z);
                if (!level.getBlockState(pos).isAir()) return null;
                if (!level.getBlockState(pos.below()).is(Blocks.END_STONE)) return null;
                if (!placeableAt(level, toPlace, pos)) return null;
                return pos;
            }
            default:
                return null;
        }
    }

    private static int surfaceY(WorldGenLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        return y > level.getMinBuildHeight() && y < level.getMaxBuildHeight() ? y - 1 : Integer.MIN_VALUE;
    }

    private static boolean matchesHabitat(WorldGenLevel level, BlockPos pos, Config config) {
        if (pos.getY() < config.minY) return false;
        if (config.biomeTag.isPresent()) {
            TagKey<Biome> tag = TagKey.create(Registries.BIOME, config.biomeTag.get());
            if (!level.getBiome(pos).is(tag)) return false;
        }
        if (config.openSky) {
            // Check the plant column and a small clearing; no time/weather dependency.
            if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY()) return false;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + dx, pos.getZ() + dz)
                            > pos.getY() + 2) return false;
                }
            }
        }
        return true;
    }

    /** Skip canopy vegetation to reach soil, stopping at the first solid ground. */
    private static BlockPos forestFloor(WorldGenLevel level, int x, int z, BlockState toPlace) {
        int top = surfaceY(level, x, z);
        if (top == Integer.MIN_VALUE) return null;
        int bottom = Math.max(level.getMinBuildHeight(), top - 32);
        for (int y = top; y >= bottom; y--) {
            BlockPos ground = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(ground);
            if (state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)
                    || (state.canBeReplaced() && state.getFluidState().isEmpty())) continue;
            BlockPos pos = ground.above();
            return validSurface(level, pos, toPlace, Rule.SURFACE_SOIL) ? pos : null;
        }
        return null;
    }

    private static boolean nearWaterSource(WorldGenLevel level, BlockPos pos) {
        for (BlockPos probe : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 0, 4))) {
            var fluid = level.getFluidState(probe);
            if (fluid.is(FluidTags.WATER) && fluid.isSource()) return true;
        }
        return false;
    }

    /** Keep the existing soil substrate, requiring an exposed rock face beside it. */
    private static boolean nearRockFace(WorldGenLevel level, BlockPos pos) {
        for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (level.getBlockState(side).is(BlockTags.BASE_STONE_OVERWORLD)
                    || level.getBlockState(side.below()).is(BlockTags.BASE_STONE_OVERWORLD)) return true;
        }
        return false;
    }

    private static boolean validSurface(WorldGenLevel level, BlockPos pos, BlockState toPlace, Rule rule) {
        if (!level.getBlockState(pos).isAir()) return false;
        BlockState floor = level.getBlockState(pos.below());
        if (!placeableAt(level, toPlace, pos)) return false;
        return switch (rule) {
            case SURFACE_SOIL -> floor.is(Blocks.GRASS_BLOCK) || floor.is(Blocks.DIRT)
                    || floor.is(Blocks.COARSE_DIRT) || floor.is(Blocks.PODZOL)
                    || floor.is(Blocks.MOSS_BLOCK) || floor.is(Blocks.SAND);
            case SURFACE_ROCKY -> floor.is(Blocks.STONE) || floor.is(Blocks.GRAVEL)
                    || floor.is(Blocks.MOSS_BLOCK) || floor.is(Blocks.COBBLESTONE)
                    || floor.is(Blocks.GRANITE) || floor.is(Blocks.ANDESITE)
                    || floor.is(Blocks.DIORITE) || floor.is(Blocks.DEEPSLATE)
                    || floor.is(Blocks.GRASS_BLOCK) || floor.is(Blocks.DIRT);
            case SURFACE_SANDY -> floor.is(Blocks.SAND) || floor.is(Blocks.RED_SAND)
                    || floor.is(Blocks.TERRACOTTA) || floor.is(Blocks.WHITE_TERRACOTTA)
                    || floor.is(Blocks.ORANGE_TERRACOTTA);
            default -> true;
        };
    }

    private static boolean caveFloorOk(BlockState floor) {
        return floor.is(Blocks.DIRT) || floor.is(Blocks.COARSE_DIRT) || floor.is(Blocks.GRAVEL)
                || floor.is(Blocks.CLAY) || floor.is(Blocks.MOSS_BLOCK) || floor.is(Blocks.STONE)
                || floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.ANDESITE) || floor.is(Blocks.DIORITE)
                || floor.is(Blocks.GRANITE) || floor.is(Blocks.DEEPSLATE) || floor.is(Blocks.TUFF)
                || floor.is(Blocks.PODZOL) || floor.is(Blocks.MYCELIUM)
                || Substrate.isSpiritSoil(floor);
    }
}
