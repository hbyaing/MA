package cn.blockforge.generated.generatedmod.plants;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 灵植统一注册表。14 株灵植共用 {@link SpiritPlantBlock} + {@link SpiritPlantDefinition}。
 * 全部方块与物品注册在 WuXingBlocks.BLOCKS / WuXingItems.ITEMS 上（创造标签页自动收录）。
 *
 * <p>旧存档兼容：dragon_scale_grass 与 glowing_blue_mushroom 保持原注册 ID，
 * 只重做行为（多阶段生长、生态化 worldgen、灵壤/末地石双基质）。</p>
 */
public final class SpiritPlantRegistry {

    private static final boolean IMPORTED_DEFINITIONS = ImportedHerbs.install();

    public static final Map<String, SpiritPlantDefinition> DEFINITIONS =
            Collections.unmodifiableMap(new LinkedHashMap<>(SpiritPlantCatalog.BY_ID));
    public static final List<RegistryObject<Block>> PLANT_BLOCKS = new ArrayList<>();

    // ===== 12 株新灵植 =====
    public static final RegistryObject<Block> SPIRIT_SPROUT_GRASS =
            block(SpiritPlantCatalog.SPIRIT_SPROUT, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> SPIRIT_SPROUT_GRASS_ITEM =
            herb(SpiritPlantCatalog.SPIRIT_SPROUT, SPIRIT_SPROUT_GRASS);

    public static final RegistryObject<Block> DEW_DROP_FLOWER =
            block(SpiritPlantCatalog.DEW_FLOWER, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> DEW_DROP_FLOWER_ITEM =
            herb(SpiritPlantCatalog.DEW_FLOWER, DEW_DROP_FLOWER);

    public static final RegistryObject<Block> CLEAR_HEART_LOTUS =
            block(SpiritPlantCatalog.CLEAR_HEART_LOTUS, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> CLEAR_HEART_LOTUS_ITEM =
            herb(SpiritPlantCatalog.CLEAR_HEART_LOTUS, CLEAR_HEART_LOTUS);

    public static final RegistryObject<Block> SPIRIT_GINSENG =
            block(SpiritPlantCatalog.SPIRIT_GINSENG, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> SPIRIT_GINSENG_ITEM =
            herb(SpiritPlantCatalog.SPIRIT_GINSENG, SPIRIT_GINSENG);

    public static final RegistryObject<Block> RED_SUN_GRASS =
            block(SpiritPlantCatalog.RED_SUN_GRASS, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> RED_SUN_GRASS_ITEM =
            herb(SpiritPlantCatalog.RED_SUN_GRASS, RED_SUN_GRASS);

    public static final RegistryObject<Block> VEIN_MENDING_VINE =
            block(SpiritPlantCatalog.VEIN_VINE, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> VEIN_MENDING_VINE_ITEM =
            herb(SpiritPlantCatalog.VEIN_VINE, VEIN_MENDING_VINE);

    public static final RegistryObject<Block> BLOOD_SPIRIT_FLOWER =
            block(SpiritPlantCatalog.BLOOD_FLOWER, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> BLOOD_SPIRIT_FLOWER_ITEM =
            herb(SpiritPlantCatalog.BLOOD_FLOWER, BLOOD_SPIRIT_FLOWER);

    public static final RegistryObject<Block> ADAMANTINE_VINE =
            block(SpiritPlantCatalog.ADAMANTINE_VINE, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> ADAMANTINE_VINE_ITEM =
            herb(SpiritPlantCatalog.ADAMANTINE_VINE, ADAMANTINE_VINE);

    public static final RegistryObject<Block> GALE_GRASS =
            block(SpiritPlantCatalog.GALE_GRASS, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> GALE_GRASS_ITEM =
            herb(SpiritPlantCatalog.GALE_GRASS, GALE_GRASS);

    public static final RegistryObject<Block> DARK_WATER_WEED =
            block(SpiritPlantCatalog.DARK_WATER_WEED, SpiritPlantCatalog.plantProperties().noOcclusion());
    public static final RegistryObject<Item> DARK_WATER_WEED_ITEM =
            herb(SpiritPlantCatalog.DARK_WATER_WEED, DARK_WATER_WEED);

    public static final RegistryObject<Block> THUNDER_VEIN_GRASS =
            block(SpiritPlantCatalog.THUNDER_GRASS, SpiritPlantCatalog.plantProperties());
    public static final RegistryObject<Item> THUNDER_VEIN_GRASS_ITEM =
            herb(SpiritPlantCatalog.THUNDER_GRASS, THUNDER_VEIN_GRASS);

    public static final RegistryObject<Block> STARLIGHT_FLOWER =
            block(SpiritPlantCatalog.STARLIGHT_FLOWER,
                    SpiritPlantCatalog.plantProperties().lightLevel(state -> state.getValue(SpiritPlantBlock.AGE) >= 5 ? 4 : 1));
    public static final RegistryObject<Item> STARLIGHT_FLOWER_ITEM =
            herb(SpiritPlantCatalog.STARLIGHT_FLOWER, STARLIGHT_FLOWER);

    // ===== 保留旧 ID 的两株 =====

    /** 龙鳞草：末地主岛特产，末地石或灵壤栽培。物品沿用 DragonScaleGrassItem 的食用行为。 */
    public static final RegistryObject<Block> DRAGON_SCALE_GRASS =
            block(SpiritPlantCatalog.DRAGON_SCALE, SpiritPlantCatalog.plantProperties().noOcclusion());
    public static final RegistryObject<Item> DRAGON_SCALE_GRASS_ITEM = WuXingItems.ITEMS.register(
            "dragon_scale_grass", () -> new cn.blockforge.generated.generatedmod.items.DragonScaleGrassItem(
                    DRAGON_SCALE_GRASS.get(), new Item.Properties().stacksTo(64).fireResistant()));

    /** 发光蓝蘑菇：地下菌类，发光，物品保留食用回灵力行为。 */
    public static final RegistryObject<Block> GLOWING_BLUE_MUSHROOM =
            block(SpiritPlantCatalog.GLOWING_SHROOM,
                    SpiritPlantCatalog.plantProperties().lightLevel(state -> 4));
    public static final RegistryObject<Item> GLOWING_BLUE_MUSHROOM_ITEM = WuXingItems.ITEMS.register(
            "glowing_blue_mushroom", () -> new cn.blockforge.generated.generatedmod.items.GlowingBlueMushroomItem(
                    GLOWING_BLUE_MUSHROOM.get(), new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));

    static {
        for(var def:ImportedHerbs.DEFINITIONS)herb(def,block(def,SpiritPlantCatalog.plantProperties()));
    }

    private static RegistryObject<Block> block(SpiritPlantDefinition def, BlockBehaviour.Properties properties) {
        RegistryObject<Block> registered = WuXingBlocks.BLOCKS.register(def.id,
                () -> def == SpiritPlantCatalog.GLOWING_SHROOM ? new GlowingBlueMushroomBlock(def, properties) : new SpiritPlantBlock(def, properties));
        PLANT_BLOCKS.add(registered);
        return registered;
    }

    private static RegistryObject<Item> herb(SpiritPlantDefinition def, RegistryObject<Block> block) {
        return WuXingItems.ITEMS.register(def.id, () -> new SpiritPlantItem(
                (SpiritPlantBlock) block.get(),
                SpiritPlantCatalog.plantItemProperties(def.rarity)));
    }

    /** 强制类初始化；由 GeneratedMod 构造器在 BLOCKS/ITEMS 的 register(bus) 之后调用一次。 */
    public static void bootstrap() {
        if (PLANT_BLOCKS.size() != SpiritPlantCatalog.ALL.size()) {
            throw new IllegalStateException("灵植注册数量与清单不一致: "
                    + PLANT_BLOCKS.size() + " != " + SpiritPlantCatalog.ALL.size());
        }
    }

    private SpiritPlantRegistry() {}
}
