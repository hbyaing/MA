package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.EnumSet;

/**
 * 一种灵植的全部静态数据：野外稀有度、种植基质、硬性条件、生长偏好、
 * 灵肥效率与药性。生长事件只读取这里的字段，不允许按具体植物写分支。
 *
 * <p>硬性条件（不满足则不能种/不能长）：substrate + requiresSpiritSoilToGrow。
 * 生长偏好（不满足只减速，绝不致死）：{@link Preference}。</p>
 */
public final class SpiritPlantDefinition {

    /** 生长偏好标记。每种偏好在 {@link #growthOneIn} 内统一换算。 */
    public enum Preference {
        /** 附近（半径2）有水时生长显著加快。 */
        NEAR_WATER,
        /** 白天且天光充足时加快；夜晚减慢。 */
        SUN_LIGHT,
        /** 完全露天（能看到天空）时加快。 */
        OPEN_SKY,
        /** 低光环境（光照<=9）加快；强光减慢，但都不致死。 */
        LOW_LIGHT,
        /** 下雨时加快，雷暴时大幅加快。 */
        THUNDER,
        /** 夜间是主要生长期，白天极慢（不致死）。 */
        NIGHT_BLOOM,
        /** 末地石上正常速度，灵壤等替代基质减半。 */
        END_SPECIALTY
    }

    public final String id;
    /** Explicit acquisition ages, independent of rarity; no medicinal rank or species ceiling. */
    public final int minimumYears, randomMaximumYears;
    public final WildRarity rarity;
    public final Substrate substrate;
    /** 是否必须种在灵壤才能生长（野外可在普通土上被发现但不生长）。 */
    public final boolean requiresSpiritSoilToGrow;
    /** 是否必须浸在水中（玄水藻）。 */
    public final boolean aquaticInCell;
    /** 半水生：本格有水或脚下是水源（清心莲）。 */
    public final boolean semiAquatic;
    /** 每个随机刻推进一次的概率分母基准（数值越大越慢）。 */
    public final int baseGrowthOneIn;
    /** 成熟阶段数（AGE 0..matureAge）。 */
    public final int matureAge;
    /** 灵肥一次推进的阶段数。 */
    public final int fertilizerAdvance;
    /** 普通骨粉是否有效（只允许极少数基础灵植）。 */
    public final boolean boneMealUsable;
    public final EnumSet<Preference> preferences;
    public final EnumSet<HerbProperty> properties;

    private SpiritPlantDefinition(Builder b) {
        this.id = b.id;
        this.minimumYears = b.minimumYears;
        this.randomMaximumYears = b.randomMaximumYears;
        this.rarity = b.rarity;
        this.substrate = b.substrate;
        this.requiresSpiritSoilToGrow = b.requiresSpiritSoilToGrow;
        this.aquaticInCell = b.aquaticInCell;
        this.semiAquatic = b.semiAquatic;
        this.baseGrowthOneIn = b.baseGrowthOneIn;
        this.matureAge = b.matureAge;
        this.fertilizerAdvance = b.fertilizerAdvance;
        this.boneMealUsable = b.boneMealUsable;
        this.preferences = b.preferences.isEmpty()
                ? EnumSet.noneOf(Preference.class) : EnumSet.copyOf(b.preferences);
        this.properties = b.properties;
    }

    /**
     * 种植/存活硬条件统一入口。self 是目标格现有或即将放置的方块状态
     * （放置校验传入假想状态，存活校验传入当前状态）。
     * 偏好不满足永远不返回 false —— 只有硬条件破坏才允许植株脱落。
     */
    public boolean canPlaceAt(BlockGetter level, BlockPos pos, BlockState self) {
        BlockState below = level.getBlockState(pos.below());
        if (aquaticInCell) {
            // 硬性沉水（玄水藻）：只有本格被水浸润才合法。放置时由“替换水”写入 WATERLOGGED，
            // 世界生成时由特性按本格流体水写入；不再接受“仅相邻有水”，
            // 否则玄水藻会被种到干燥岸边，违反“必须完全浸水、出水脱落”的红线。
            return isWaterlogged(self) && substrate.floorOk(below);
        }
        if (semiAquatic) {
            // 浮水/浅水（清心莲）：本格浸润或脚下就是水源；两者皆无即视为旱地拒种。
            // Floating lotus roots require a real liquid surface, never a soil block.
            return Substrate.isPlantWater(below.getFluidState()) && below.getFluidState().isSource();
        }
        return substrate.floorOk(below);
    }

    private static boolean isWaterlogged(BlockState self) {
        return self.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                && self.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
    }

    /** 灵肥硬门槛：基质/水/灵壤必须全部合法，与生长速度无关。 */
    public boolean fertilizerAllowed(BlockGetter level, BlockPos pos) {
        if (!canPlaceAt(level, pos, level.getBlockState(pos))) return false;
        if (requiresSpiritSoilToGrow) {
            BlockState floor = level.getBlockState(pos.below());
            if (!Substrate.isSpiritSoil(floor)) return false;
        }
        return true;
    }

    /** 本随机刻是否允许推进生长（含灵壤门槛）。返回概率分母，&lt;=0 表示禁止。 */
    public int growthOneIn(Level level, BlockPos pos) {
        if (!canPlaceAt(level, pos, level.getBlockState(pos))) return 0;
        if (requiresSpiritSoilToGrow && !Substrate.isSpiritSoil(level.getBlockState(pos.below()))) return 0;
        int oneIn = baseGrowthOneIn;
        if (Substrate.isSpiritSoil(level.getBlockState(pos.below()))
                || (semiAquatic && Substrate.isSpiritLiquid(level.getFluidState(pos.below())))) {
            oneIn = Math.max(1, oneIn / 2);
        }
        boolean day = level.isDay();
        if (preferences.contains(Preference.NEAR_WATER) && hasWaterNearby(level, pos)) {
            oneIn = Math.max(2, oneIn / 2);
        }
        if (preferences.contains(Preference.SUN_LIGHT)) {
            int sky = level.getMaxLocalRawBrightness(pos);
            if (day && sky >= 12) oneIn = Math.max(2, oneIn / 2);
            else if (!day) oneIn = oneIn * 2;
        }
        if (preferences.contains(Preference.OPEN_SKY) && canSeeSky(level, pos)) {
            oneIn = Math.max(2, oneIn / 2);
        }
        if (preferences.contains(Preference.LOW_LIGHT)) {
            int sky = level.getMaxLocalRawBrightness(pos);
            if (sky <= 9) oneIn = Math.max(2, oneIn / 2);
            else oneIn = oneIn * 2;
        }
        if (preferences.contains(Preference.THUNDER) && level.isRaining() && canSeeSky(level, pos)) {
            oneIn = level.isThundering() ? Math.max(1, oneIn / 3) : Math.max(2, oneIn / 2);
        }
        if (preferences.contains(Preference.NIGHT_BLOOM)) {
            if (level.isNight()) oneIn = Math.max(2, oneIn / 2);
            else if (day) oneIn = oneIn * 4; // 白天极慢，但绝不致死
        }
        if (preferences.contains(Preference.END_SPECIALTY)
                && !level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.END_STONE)) {
            oneIn = oneIn * 2;
        }
        return oneIn;
    }

    /** 灵肥单次推进阶段数（已应用夜生/末地特产的减速修正）。0 表示无效。 */
    public int fertilizerAdvanceAt(Level level, BlockPos pos) {
        if (!fertilizerAllowed(level, pos)) return 0;
        int adv = fertilizerAdvance;
        if (preferences.contains(Preference.NIGHT_BLOOM) && !level.isNight()) {
            adv = Math.max(1, adv - 1); // 白天效率降低但仍可少量推进
        }
        if (preferences.contains(Preference.END_SPECIALTY)
                && !level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.END_STONE)) {
            adv = Math.max(1, adv - 1);
        }
        return adv;
    }

    private static boolean hasWaterNearby(LevelReader level, BlockPos pos) {
        // 半径 2 的同层与下一层浅扫描：仅 randomTick 触发，成本受控。
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 0; y++) {
                    if (level.getFluidState(pos.offset(x, y, z)).is(Fluids.WATER)) return true;
                }
            }
        }
        return false;
    }

    /** 露天判定用 MOTION_BLOCKING 高度图，不依赖当前亮度，成本为一次高度图查询。 */
    private static boolean canSeeSky(LevelReader level, BlockPos pos) {
        return pos.getY() >= level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                pos.getX(), pos.getZ());
    }

    public static Builder builder(String id, WildRarity rarity) {
        return new Builder(id, rarity);
    }

    /** 流式构造器：所有灵植数据都在 SpiritPlantCatalog 一处声明。 */
    public static final class Builder {
        private final String id;
        private final WildRarity rarity;
        private int minimumYears = 0, randomMaximumYears = 10499;
        private Substrate substrate = Substrate.SOIL;
        private boolean requiresSpiritSoilToGrow;
        private boolean aquaticInCell;
        private boolean semiAquatic;
        private int baseGrowthOneIn = 8;
        private int matureAge = 4;
        private int fertilizerAdvance = 1;
        private boolean boneMealUsable;
        private final EnumSet<Preference> preferences = EnumSet.noneOf(Preference.class);
        private EnumSet<HerbProperty> properties = EnumSet.noneOf(HerbProperty.class);

        Builder(String id, WildRarity rarity) {
            this.id = id;
            this.rarity = rarity;
        }

        /** Future innate roots declare a sourced minimum here; no such species is invented by default. */
        public Builder years(int minimum, int randomMaximum) {
            if(minimum<0||randomMaximum<minimum||randomMaximum>HerbAge.MAX)throw new IllegalArgumentException("Invalid year range");
            this.minimumYears=minimum;this.randomMaximumYears=randomMaximum;return this;
        }
        public Builder substrate(Substrate s) { this.substrate = s; return this; }
        public Builder needsSpiritSoil() { this.requiresSpiritSoilToGrow = true; return this; }
        public Builder aquatic() { this.aquaticInCell = true; this.substrate = Substrate.WATER; return this; }
        public Builder semiAquatic() { this.semiAquatic = true; this.substrate = Substrate.WATER_OR_ABOVE; return this; }
        public Builder growth(int oneIn, int matureAge) { this.baseGrowthOneIn = oneIn; this.matureAge = matureAge; return this; }
        public Builder fertilizer(int stages) { this.fertilizerAdvance = stages; return this; }
        public Builder boneMeal() { this.boneMealUsable = true; return this; }
        public Builder prefers(Preference... prefs) { this.preferences.addAll(java.util.Arrays.asList(prefs)); return this; }
        public Builder herbs(HerbProperty... props) { this.properties = EnumSet.noneOf(HerbProperty.class); this.properties.addAll(java.util.Arrays.asList(props)); return this; }
        public SpiritPlantDefinition build() { return new SpiritPlantDefinition(this); }
    }
}
