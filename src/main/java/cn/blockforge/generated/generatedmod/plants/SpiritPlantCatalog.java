package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.blockforge.generated.generatedmod.plants.SpiritPlantDefinition.Preference;

import static cn.blockforge.generated.generatedmod.plants.SpiritPlantDefinition.Preference.*;
import static cn.blockforge.generated.generatedmod.plants.HerbProperty.*;

/**
 * 全部灵植的静态清单。第二批共 14 株：
 * 12 株新灵植 + 龙鳞草（末地主岛特产重做）+ 发光蓝蘑菇（地下特殊菌类）。
 *
 * <p>灵植没有凡/灵/玄/天品阶；差异只体现在稀有度、生境、培养条件、生长
 * 速度与药性上。灵壤只有一种；聚灵阵不参与灵植系统。</p>
 */
public final class SpiritPlantCatalog {

    /** 按注册顺序保存的全部定义，供 worldgen 与文档生成使用。 */
    public static final List<SpiritPlantDefinition> ALL = new ArrayList<>();
    public static final Map<String, SpiritPlantDefinition> BY_ID = new LinkedHashMap<>();

    private static SpiritPlantDefinition register(SpiritPlantDefinition def) {
        ALL.add(def);
        BY_ID.put(def.id, def);
        return def;
    }

    // ===== 定义 =====

    /** 1. 灵芽草：最基础、适应性较强的修仙灵草。骨粉与灵肥都能用。 */
    public static final SpiritPlantDefinition SPIRIT_SPROUT = register(SpiritPlantDefinition
            .builder("spirit_sprout_grass", WildRarity.COMMON)
            .substrate(Substrate.SOIL)
            .growth(8, 4)
            .fertilizer(3)
            .boneMeal()
            .herbs(SPIRIT, WOOD, HARMONY)
            .build());

    /** 2. 凝露花：滋养恢复。邻水加速，缺水不死。 */
    public static final SpiritPlantDefinition DEW_FLOWER = register(SpiritPlantDefinition
            .builder("dew_drop_flower", WildRarity.COMMON)
            .substrate(Substrate.SOIL)
            .growth(8, 4)
            .fertilizer(3)
            .boneMeal()
            .prefers(NEAR_WATER)
            .herbs(WATER, NOURISH, RECOVERY)
            .build());

    /** 3. 清心莲：水生/半水生，清净神魂。不得种在旱地。 */
    public static final SpiritPlantDefinition CLEAR_HEART_LOTUS = register(SpiritPlantDefinition
            .builder("clear_heart_lotus", WildRarity.UNCOMMON)
            .semiAquatic()
            .growth(7, 5)
            .fertilizer(2)
            .herbs(WATER, CLEANSE, SOUL)
            .build());

    /** 4. 聚灵参：修炼聚气核心。人工培养必须灵壤，野外可发现但停止生长。 */
    public static final SpiritPlantDefinition SPIRIT_GINSENG = register(SpiritPlantDefinition
            .builder("spirit_ginseng", WildRarity.UNCOMMON)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(6, 5)
            .fertilizer(2)
            .herbs(SPIRIT, WOOD, GATHER_QI, CULTIVATION)
            .build());

    /** 5. 赤阳草：火阳炼体。灵壤 + 白昼高光照加速；不会点燃灵田。 */
    public static final SpiritPlantDefinition RED_SUN_GRASS = register(SpiritPlantDefinition
            .builder("red_sun_grass", WildRarity.UNCOMMON)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(6, 5)
            .fertilizer(2)
            .prefers(SUN_LIGHT)
            .herbs(FIRE, YANG, BODY_REFINE)
            .build());

    /** 6. 续脉藤：经脉生机。藤本定位以数据记录，表现为普通植株。 */
    public static final SpiritPlantDefinition VEIN_VINE = register(SpiritPlantDefinition
            .builder("vein_mending_vine", WildRarity.RARE)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(5, 6)
            .fertilizer(1)
            .prefers(NEAR_WATER)
            .herbs(WOOD, VITALITY, MERIDIAN)
            .build());

    /** 7. 血灵花：气血爆发。阴生环境偏好，无需浇血机制。 */
    public static final SpiritPlantDefinition BLOOD_FLOWER = register(SpiritPlantDefinition
            .builder("blood_spirit_flower", WildRarity.RARE)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(5, 6)
            .fertilizer(1)
            .herbs(BLOOD, FIRE, QI_BLOOD)
            .build());

    /** 8. 金刚藤：防御炼体。基础生长速度偏慢。 */
    public static final SpiritPlantDefinition ADAMANTINE_VINE = register(SpiritPlantDefinition
            .builder("adamantine_vine", WildRarity.RARE)
            .substrate(Substrate.ROCKY)
            .needsSpiritSoil()
            .growth(4, 6)
            .fertilizer(1)
            .herbs(METAL, EARTH, TOUGHNESS, BODY_REFINE)
            .build());

    /** 9. 疾风草：身法。露天加速。 */
    public static final SpiritPlantDefinition GALE_GRASS = register(SpiritPlantDefinition
            .builder("gale_grass", WildRarity.RARE)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(5, 6)
            .fertilizer(1)
            .prefers(OPEN_SKY)
            .herbs(WIND, WOOD, MOVEMENT)
            .build());

    /** 10. 玄水藻：硬性水生，离开水体脱落。寒水偏好。 */
    public static final SpiritPlantDefinition DARK_WATER_WEED = register(SpiritPlantDefinition
            .builder("dark_water_weed", WildRarity.RARE)
            .aquatic()
            .growth(6, 5)
            .fertilizer(1)
            .herbs(WATER, YIN, COLD)
            .build());

    /** 11. 雷纹草：雷淬炼。普通天气可缓慢生长，雷雨显著加速，不需要遭雷劈。 */
    public static final SpiritPlantDefinition THUNDER_GRASS = register(SpiritPlantDefinition
            .builder("thunder_vein_grass", WildRarity.RARE)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(5, 6)
            .fertilizer(1)
            .prefers(Preference.THUNDER, OPEN_SKY)
            .herbs(HerbProperty.THUNDER, YANG, TEMPER)
            .build());

    /** 12. 星辉花：1.0 后期奇珍。夜生昼歇；白天灵肥效率降低。 */
    public static final SpiritPlantDefinition STARLIGHT_FLOWER = register(SpiritPlantDefinition
            .builder("starlight_flower", WildRarity.VERY_RARE)
            .substrate(Substrate.SOIL)
            .needsSpiritSoil()
            .growth(4, 7)
            .fertilizer(2)
            .prefers(NIGHT_BLOOM, OPEN_SKY)
            .herbs(STAR, SPIRIT, SOUL)
            .build());

    /** 13. 龙鳞草：末地主岛特产。末地石正常速度，灵壤培养稍慢。 */
    public static final SpiritPlantDefinition DRAGON_SCALE = register(SpiritPlantDefinition
            .builder("dragon_scale_grass", WildRarity.SPECIAL)
            .substrate(Substrate.END_STONE_OR_SPIRIT)
            .growth(4, 6)
            .fertilizer(1)
            .prefers(END_SPECIALTY)
            .herbs(DRAGON, VITALITY, BODY_REFINE, SPIRIT)
            .build());

    /** 14. 发光蓝蘑菇：地下菌类，低光偏好，发光。 */
    public static final SpiritPlantDefinition GLOWING_SHROOM = register(SpiritPlantDefinition
            .builder("glowing_blue_mushroom", WildRarity.UNCOMMON)
            .substrate(Substrate.SOIL_STRICT)
            .growth(7, 4)
            .fertilizer(2)
            .prefers(LOW_LIGHT)
            .herbs(YIN, SOUL, GHOST)
            .build());

    /** 灵植统一方块属性：草状碰撞、随机刻、无遮剔。 */
    public static BlockBehaviour.Properties plantProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .instabreak()
                .randomTicks()
                .sound(SoundType.GRASS);
    }

    /** 灵植物品属性：可堆叠 64，稀有度按野外定位给展示层级。 */
    public static Item.Properties plantItemProperties(WildRarity rarity) {
        Item.Properties properties = new Item.Properties().stacksTo(64);
        return switch (rarity) {
            case COMMON, UNCOMMON -> properties.rarity(net.minecraft.world.item.Rarity.COMMON);
            case RARE -> properties.rarity(net.minecraft.world.item.Rarity.UNCOMMON);
            case VERY_RARE -> properties.rarity(net.minecraft.world.item.Rarity.RARE);
            case SPECIAL -> properties.rarity(net.minecraft.world.item.Rarity.EPIC);
        };
    }

    private SpiritPlantCatalog() {}
}
