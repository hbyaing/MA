package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.ArtManualItem;
import cn.blockforge.generated.generatedmod.items.CarvingKnifeItem;
import cn.blockforge.generated.generatedmod.items.SealItem;
import cn.blockforge.generated.generatedmod.items.TalismanItem;
import cn.blockforge.generated.generatedmod.items.CreatureCompassItem;
import cn.blockforge.generated.generatedmod.items.WanfaGuizongItem;
import cn.blockforge.generated.generatedmod.items.BreakthroughPillItem;
import cn.blockforge.generated.generatedmod.items.EffectPillItem;
import cn.blockforge.generated.generatedmod.items.EmptySpiritLiquidPotItem;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.items.ExperienceCrystalItem;
import cn.blockforge.generated.generatedmod.items.CreationPillItem;
import cn.blockforge.generated.generatedmod.items.CultivationInsightItem;
import cn.blockforge.generated.generatedmod.items.HeartMethodItem;
import cn.blockforge.generated.generatedmod.items.HuaLingPeiItem;
import cn.blockforge.generated.generatedmod.items.GuandingItem;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import cn.blockforge.generated.generatedmod.items.HeartDemonSpawnEggItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.items.SpiritLiquidBucketItem;
import cn.blockforge.generated.generatedmod.items.SpiritLiquidDrinkItem;
import cn.blockforge.generated.generatedmod.items.SpiritLiquidPotItem;
import cn.blockforge.generated.generatedmod.items.DivineManualItem;
import cn.blockforge.generated.generatedmod.items.SoulBindingPearlItem;
import cn.blockforge.generated.generatedmod.items.SoulCapturePearlItem;
import cn.blockforge.generated.generatedmod.items.SoulPearlItem;
import cn.blockforge.generated.generatedmod.items.SoulUtilityPearlItem;
import cn.blockforge.generated.generatedmod.items.SoulEnergyPearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途物品注册。所有物品都只在 Forge 注册事件中构造。 */
public final class WuXingItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "wuxingxiantu");
    public static final RegistryObject<Item> SEALED_ALCHEMY_BATCH=ITEMS.register("sealed_alchemy_batch",()->new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INSECT_CARAPACE_FRAGMENT=ITEMS.register("insect_carapace_fragment",()->new cn.blockforge.generated.generatedmod.items.InsectMaterialItem("insect_carapace_fragment"));
    public static final RegistryObject<Item> SPIRIT_DEVOURER_SAC=ITEMS.register("spirit_devourer_sac",()->new cn.blockforge.generated.generatedmod.items.InsectMaterialItem("spirit_devourer_sac"));
    public static final RegistryObject<Item> SPIRIT_DEVOURER_SPAWN_EGG=ITEMS.register("spirit_devourer_spawn_egg",()->new net.minecraftforge.common.ForgeSpawnEggItem(
            cn.blockforge.generated.generatedmod.registry.ModEntities.SPIRIT_DEVOURER,0x302E29,0x7DE1BF,new Item.Properties()));
    public static final RegistryObject<Item> DEV_TEST_ROBE=ITEMS.register("dev_test_robe",()->new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPIRIT_STONE = ITEMS.register(
            "spirit_stone", () -> new cn.blockforge.generated.generatedmod.economy.SpiritStoneItem(cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade.LOW, properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> SPIRIT_STONE_MIDDLE = ITEMS.register("spirit_stone_middle", () -> new cn.blockforge.generated.generatedmod.economy.SpiritStoneItem(cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade.MIDDLE, properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPIRIT_STONE_SUPERIOR = ITEMS.register("spirit_stone_superior", () -> new cn.blockforge.generated.generatedmod.economy.SpiritStoneItem(cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade.SUPERIOR, properties(64, Rarity.RARE)));
    public static final RegistryObject<Item> SPIRIT_STONE_EXQUISITE = ITEMS.register("spirit_stone_exquisite", () -> new cn.blockforge.generated.generatedmod.economy.SpiritStoneItem(cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade.EXQUISITE, properties(64, Rarity.EPIC)));
    public static final RegistryObject<Item> SPIRIT_VEIN_COMPASS = ITEMS.register("spirit_vein_compass", () -> new cn.blockforge.generated.generatedmod.items.SpiritVeinCompassItem(properties(1, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RAW_DARK_IRON = ITEMS.register(
            "raw_dark_iron", () -> new Item(properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> DARK_IRON = ITEMS.register(
            "dark_iron", () -> new Item(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPIRIT_COAL = ITEMS.register(
            "spirit_coal", () -> new Item(properties(64, Rarity.COMMON)));
    /** 永燃灵煤：灵蕴台以灵气转化灵煤得到的无尽燃料。 */
    public static final RegistryObject<Item> ETERNAL_SPIRIT_COAL = ITEMS.register(
            "eternal_spirit_coal", () -> new cn.blockforge.generated.generatedmod.items.EternalSpiritCoalItem(
                    properties(64, Rarity.RARE)));
    public static final RegistryObject<Item> GLAZED_CRYSTAL = ITEMS.register(
            "glazed_crystal", () -> new Item(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> REFINE_SEAL = ITEMS.register(
            "refine_seal", () -> new cn.blockforge.generated.generatedmod.items.RefineSealItem(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> QUENCH_ESSENCE = ITEMS.register(
            "quench_essence", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> INSCRIPTION_CRYSTAL = ITEMS.register(
            "ming_jing", () -> new Item(properties(64, Rarity.RARE)));
    public static final RegistryObject<Item> EMPTY_FIRE_SEED_BOTTLE = ITEMS.register(
            "empty_fire_seed_bottle", () -> new FireSeedItem(0, properties(16, Rarity.COMMON)));
    public static final RegistryObject<Item> FIRE_SEED_RED_FLOW = fireSeed("fire_seed_red_flow", 1, Rarity.COMMON);
    public static final RegistryObject<Item> FIRE_SEED_MOLTEN_GOLD = fireSeed("fire_seed_molten_gold", 2, Rarity.UNCOMMON);
    public static final RegistryObject<Item> FIRE_SEED_COLD_ASH = fireSeed("fire_seed_cold_ash", 3, Rarity.UNCOMMON);
    public static final RegistryObject<Item> FIRE_SEED_AZURE_WOOD = fireSeed("fire_seed_azure_wood", 4, Rarity.RARE);
    public static final RegistryObject<Item> FIRE_SEED_COBALT_SEA = fireSeed("fire_seed_cobalt_sea", 5, Rarity.RARE);
    public static final RegistryObject<Item> FIRE_SEED_VIOLET_THUNDER = fireSeed("fire_seed_violet_thunder", 6, Rarity.RARE);
    public static final RegistryObject<Item> FIRE_SEED_SUN_SAINT = fireSeed("fire_seed_sun_saint", 7, Rarity.EPIC);
    public static final RegistryObject<Item> FIRE_SEED_NETHER_VOID = fireSeed("fire_seed_nether_void", 8, Rarity.EPIC);
    public static final RegistryObject<Item> FIRE_SEED_NIRVANA = fireSeed("fire_seed_nirvana", 9, Rarity.EPIC);
    public static final RegistryObject<Item> AURA_CRYSTAL = ITEMS.register(
            "aura_crystal", () -> new Item(properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> EXPERIENCE_CRYSTAL = ITEMS.register(
            "experience_crystal", () -> new ExperienceCrystalItem(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPIRIT_LIQUID_BUCKET = ITEMS.register(
            "spirit_liquid_bucket", () -> new SpiritLiquidBucketItem(new Item.Properties()));
    /** 灵肥：骨粉浸入灵液转化而来，修仙灵植的正式催熟手段。 */
    public static final RegistryObject<Item> SPIRIT_FERTILIZER = ITEMS.register(
            "spirit_fertilizer", () -> new cn.blockforge.generated.generatedmod.plants.SpiritFertilizerItem(
                    properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> SPIRIT_LIQUID_BOTTLE = ITEMS.register(
            "spirit_liquid_bottle", () -> new SpiritLiquidDrinkItem(
                    0.05F, net.minecraft.world.item.Items.GLASS_BOTTLE, properties(64, Rarity.COMMON)));
    // 发光蓝蘑菇与龙鳞草物品已迁移到 SpiritPlantRegistry（保留原注册 ID）。
    public static final RegistryObject<Item> FIVE_ESSENCE = ITEMS.register(
            "five_essence", () -> new Item(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PHYSIQUE_RESET_PILL = unifiedPill("physique_reset_pill");

    public static final RegistryObject<Item> PEACH_BLOSSOM = ITEMS.register(
            "peach_blossom", () -> new Item(properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> PEACH_PETALS = ITEMS.register(
            "peach_petals", () -> new Item(properties(64, Rarity.COMMON)));
    public static final RegistryObject<Item> PEACH_SWORD = sword("peach_sword", net.minecraft.world.item.Tiers.WOOD, 6.0F, "peach_wood", 128, 8, 5, 2, Rarity.UNCOMMON);
    /** 心剑本体：由服务端按 owner UUID 动态凝聚，不参与普通灵剑品阶与配方。 */
    public static final RegistryObject<Item> HEART_SWORD = ITEMS.register("heart_sword",
            () -> new cn.blockforge.generated.generatedmod.items.HeartSwordItem(properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> DARK_IRON_SWORD = sword("dark_iron_sword", net.minecraft.world.item.Tiers.IRON, 7.0F, "dark_iron", 256, 12, 8, 3, Rarity.RARE);
    public static final RegistryObject<Item> AZURE_STEEL_SWORD = sword("mingjue_sword", net.minecraft.world.item.Tiers.DIAMOND, 8.0F, "azure_steel", 512, 16, 12, 3, Rarity.RARE);
    public static final RegistryObject<Item> TIANGANG_SWORD = sword("qingming_sword", net.minecraft.world.item.Tiers.DIAMOND, 9.0F, "tiangang", 1024, 20, 18, 4, Rarity.EPIC);
    public static final RegistryObject<Item> BIG_DIPPER_SWORD = sword("jixiao_sword", net.minecraft.world.item.Tiers.DIAMOND, 10.0F, "big_dipper", 2048, 24, 25, 4, Rarity.EPIC);
    public static final RegistryObject<Item> DEMON_SLAYER_SWORD = sword("wuxiang_sword", net.minecraft.world.item.Tiers.DIAMOND, 11.0F, "demon_slayer", 4096, 28, 35, 5, Rarity.EPIC);
    public static final RegistryObject<Item> PURE_YANG_SWORD = sword("guiyuan_sword", net.minecraft.world.item.Tiers.NETHERITE, 12.0F, "pure_yang", 8192, 32, 50, 6, Rarity.EPIC);
    public static final RegistryObject<Item> XUANYUAN_SWORD = sword("tianque_sword", net.minecraft.world.item.Tiers.NETHERITE, 14.0F, "xuanyuan", 16384, 40, 80, 99, Rarity.EPIC);

    /** 碧血银枪：由 uploads/bixue_yinqiang.bbmodel 转换的长柄武器，基础伤害 15。 */
    public static final RegistryObject<Item> BIXUE_YINQIANG = ITEMS.register(
            "bixue_yinqiang", () -> {
                net.minecraft.world.item.Tier tier = net.minecraft.world.item.Tiers.NETHERITE;
                int modifier = Math.max(0, Math.round(15.0F - 1.0F - tier.getAttackDamageBonus()));
                return new net.minecraft.world.item.SwordItem(
                        tier, modifier, -2.6F, properties(1, Rarity.EPIC).durability(1400));
            });

    public static final RegistryObject<Item> ART_SWORD_AURA = art("art_sword_aura", 1);
    public static final RegistryObject<Item> ART_RENEWAL = art("art_renewal", 1);
    public static final RegistryObject<Item> ART_SPIRIT_SHIELD = art("art_spirit_shield", 1);
    public static final RegistryObject<Item> ART_IMMOBILIZE = art("art_immobilize", 1);
    public static final RegistryObject<Item> ART_LIGHT_BODY = art("art_light_body", 1);
    public static final RegistryObject<Item> ART_GOLDEN_BELL = art("art_golden_bell", 2);
    public static final RegistryObject<Item> ART_ICE_SPIKE = art("art_ice_spike", 2);
    public static final RegistryObject<Item> ART_SPIRIT_GATHERING = art("art_spirit_gathering", 2);
    public static final RegistryObject<Item> ART_ARMOR_BREAK = art("art_armor_break", 2);
    public static final RegistryObject<Item> ART_FLAME_WAVE = art("art_flame_wave", 3);
    public static final RegistryObject<Item> ART_FLASH_STEP = art("art_flash_step", 3);
    public static final RegistryObject<Item> ART_PURIFICATION = art("art_purification", 3);
    public static final RegistryObject<Item> ART_BLOODLUST = art("art_bloodlust", 3);
    public static final RegistryObject<Item> ART_THUNDER = art("art_thunder", 3);
    public static final RegistryObject<Item> ART_MYRIAD_SWORDS = art("art_myriad_swords", 4);
    public static final RegistryObject<Item> ART_MOUNTAIN = art("art_immovable_mountain", 4);
    public static final RegistryObject<Item> ART_HARMONY = art("art_human_harmony", 4);
    public static final RegistryObject<Item> ART_FIVE_ELEMENTS = art("art_five_elements", 4);
    public static final RegistryObject<Item> ART_SWORD_PIERCE = art("art_sword_pierce", 1);
    public static final RegistryObject<Item> ART_SWORD_FLIGHT = art("art_sword_flight", 1);
    public static final RegistryObject<Item> ART_SWORD_SWEEP = art("art_sword_sweep", 2);
    public static final RegistryObject<Item> ART_SWORD_GUARD = art("art_sword_guard", 2);
    public static final RegistryObject<Item> ART_SWORD_ARRAY = art("art_sword_array", 4);

    public static final RegistryObject<Item> HEART_YUAN_HUN = heart("yuan_hun");
    public static final RegistryObject<Item> HEART_GATHERING = heart("gathering");
    public static final RegistryObject<Item> HEART_TIANGANG = heart("tiangang");
    public static final RegistryObject<Item> HEART_XUANSHUI = heart("xuanshui");
    public static final RegistryObject<Item> HEART_NINE_TURNS = heart("nine_turns");
    public static final RegistryObject<Item> HEART_BURNING_HEAVEN = heart("burning_heaven");
    public static final RegistryObject<Item> HEART_THICK_EARTH = heart("thick_earth");
    public static final RegistryObject<Item> HEART_TAIXU = heart("taixu");
    public static final RegistryObject<Item> HEART_WANLING = heart("wanling");
    public static final RegistryObject<Item> HEART_BEAST_TAMING = heart("beast_taming");
    public static final RegistryObject<Item> GUANDING_MANUAL = ITEMS.register(
            "guanding_manual", () -> new GuandingItem(properties(1, Rarity.RARE)));

    public static final RegistryObject<Item>[] GOLD_MATERIALS = materials("gold_marrow");
    public static final RegistryObject<Item>[] WOOD_MATERIALS = materials("wood_seed");
    public static final RegistryObject<Item>[] WATER_MATERIALS = materials("water_pearl");
    public static final RegistryObject<Item>[] FIRE_MATERIALS = materials("fire_flame");
    public static final RegistryObject<Item>[] EARTH_MATERIALS = materials("earth_core");


    public static final RegistryObject<Item> AURA_PILL = unifiedPill("aura_pill");
    public static final RegistryObject<Item> CULTIVATION_PILL = unifiedPill("cultivation_pill");
    public static final RegistryObject<Item> BREAKTHROUGH_PILL = unifiedPill("breakthrough_pill");
    public static final RegistryObject<Item> TRIBULATION_PILL = unifiedPill("tribulation_pill");
    public static final RegistryObject<Item> ROOT_AWAKENING_PILL = unifiedPill("root_awakening_pill");
    public static final RegistryObject<Item> ALCHEMY_FORMULA = ITEMS.register("alchemy_formula",
            () -> new cn.blockforge.generated.generatedmod.alchemy.FormulaItem(properties(1, Rarity.UNCOMMON)));
    public static final java.util.List<RegistryObject<Item>> ALCHEMY_BOOKS = java.util.Arrays.stream(
            cn.blockforge.generated.generatedmod.alchemy.AlchemyCompendium.values())
            .map(book -> ITEMS.<Item>register(book.id, () -> new cn.blockforge.generated.generatedmod.alchemy.CompendiumItem(
                    book, properties(1, Rarity.UNCOMMON))))
            .toList();

    public static final RegistryObject<Item> FOUNDATION_PILL = unifiedPill("foundation_pill");
    public static final RegistryObject<Item> GATHER_QI_PILL = unifiedPill("gather_qi_pill");
    public static final RegistryObject<Item> ORIGIN_CONDENSING_PILL = unifiedPill("origin_condensing_pill");
    public static final RegistryObject<Item> MERIDIAN_EXPANSION_PILL = unifiedPill("meridian_expansion_pill");
    public static final RegistryObject<Item> BARRIER_PREPARATION_PILL = unifiedPill("barrier_preparation_pill");
    public static final RegistryObject<Item> REJUVENATION_PILL = unifiedPill("rejuvenation_pill");




    public static final RegistryObject<Item> HEART_DEMON_SPAWN_EGG = ITEMS.register(
            "heart_demon_spawn_egg", () -> new HeartDemonSpawnEggItem(properties(64, Rarity.EPIC)));
    public static final RegistryObject<Item> MUYAN_SPAWN_EGG = bossEgg("muyan", 0x240C24, 0xCCE4E4);
    public static final RegistryObject<Item> ERMEI_SPAWN_EGG = bossEgg("ermei", 0x243C3C, 0x6CCCB4);
    public static final RegistryObject<Item> BICHEN_SPAWN_EGG = bossEgg("bichen", 0xB49C9C, 0x9C6C6C);
    public static final RegistryObject<Item> SHETAO_SPAWN_EGG = bossEgg("shetao", 0x849C9C, 0x9C3C54);
    public static final RegistryObject<Item> SHENYIN_SPAWN_EGG = bossEgg("shenyin", 0x3C849C, 0xCC6C9C);
    public static final RegistryObject<Item> YIHUO_SPAWN_EGG = bossEgg("yihuo", 0x8454B4, 0xE4E4FC);
    public static final RegistryObject<Item> PENGJU_SPAWN_EGG = bossEgg("pengju", 0x6C6C54, 0xFCE49C);
    public static final RegistryObject<Item> PENGZHI_SPAWN_EGG = bossEgg("pengzhi", 0x543C54, 0xB45484);
    public static final RegistryObject<Item> PENGJIAO_SPAWN_EGG = bossEgg("pengjiao", 0x24243C, 0x24846C);
    public static final java.util.List<RegistryObject<Item>> BOSS_TEST_EGGS = java.util.List.of(
            MUYAN_SPAWN_EGG, ERMEI_SPAWN_EGG, BICHEN_SPAWN_EGG, SHETAO_SPAWN_EGG, SHENYIN_SPAWN_EGG,
            YIHUO_SPAWN_EGG, PENGJU_SPAWN_EGG, PENGZHI_SPAWN_EGG, PENGJIAO_SPAWN_EGG, HEART_DEMON_SPAWN_EGG);

    public static final RegistryObject<Item> CLEAR_HEART_PILL = effect(
            "clear_heart_pill", EffectPillItem.Effect.CLEAR_HEART);
    public static final RegistryObject<Item> CONTINUE_VEIN_PILL = effect(
            "continue_vein_pill", EffectPillItem.Effect.CONTINUE_VEIN);
    public static final RegistryObject<Item> GATHERING_PILL = effect(
            "gathering_pill", EffectPillItem.Effect.GATHERING);
    public static final RegistryObject<Item> CALMING_PILL = effect(
            "calming_pill", EffectPillItem.Effect.CALMING);
    public static final RegistryObject<Item> BIGU_PILL = effect(
            "bigu_pill", EffectPillItem.Effect.BIGU);
    /** 修炼心得右键转化为固定数值的当前阶段修为。 */
    public static final RegistryObject<Item> INSIGHT = ITEMS.register(
            "cultivation_insight", () -> new CultivationInsightItem(properties(16, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DIAMOND_BODY_PILL = effect(
            "diamond_body_pill", EffectPillItem.Effect.DIAMOND_BODY);
    public static final RegistryObject<Item> BERSERK_PILL = effect(
            "berserk_pill", EffectPillItem.Effect.BERSERK);
    public static final RegistryObject<Item> GALE_PILL = effect(
            "gale_pill", EffectPillItem.Effect.GALE);
    public static final RegistryObject<Item> PROFOUND_WATER_PILL = effect(
            "profound_water_pill", EffectPillItem.Effect.PROFOUND_WATER);
    public static final RegistryObject<Item> GOLDEN_WOUND_PILL = effect(
            "golden_wound_pill", EffectPillItem.Effect.GOLDEN_WOUND);

    @SuppressWarnings("unchecked")
    public static final RegistryObject<Item>[] INNER_CORES = new RegistryObject[]{
        ITEMS.register("inner_core_common", () -> new BreakthroughPillItem(
                1, 0, properties(16, Rarity.COMMON), true)),
        ITEMS.register("inner_core_low", () -> new BreakthroughPillItem(
                2, 0, properties(16, Rarity.UNCOMMON), true)),
        ITEMS.register("inner_core_mid", () -> new BreakthroughPillItem(
                3, 0, properties(16, Rarity.RARE), true)),
        ITEMS.register("inner_core_high", () -> new BreakthroughPillItem(
                4, 0, properties(16, Rarity.EPIC), true)),
        ITEMS.register("inner_core_perfect", () -> new BreakthroughPillItem(
                5, 0, properties(16, Rarity.EPIC), true)),
        ITEMS.register("inner_core_immortal", () -> new BreakthroughPillItem(
                6, 20, properties(16, Rarity.EPIC), true))
    };

    public static final RegistryObject<Item> EMPTY_SPIRIT_LIQUID_POT = ITEMS.register(
            "empty_spirit_liquid_pot", () -> new EmptySpiritLiquidPotItem(properties(16, Rarity.COMMON)));
    public static final RegistryObject<Item> LIQUID_POT = ITEMS.register(
            "spirit_liquid_pot", () -> new SpiritLiquidPotItem(
                    EMPTY_SPIRIT_LIQUID_POT.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BINDING_PEARL = ITEMS.register(
            "soul_binding_pearl", () -> new SoulBindingPearlItem(properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> SOUL_CAPTURE_PEARL = ITEMS.register(
            "soul_capture_pearl", () -> new SoulCapturePearlItem(properties(1, Rarity.RARE)));
    public static final RegistryObject<Item> SOUL_PEARL = ITEMS.register(
            "soul_pearl", () -> new SoulPearlItem(properties(16, Rarity.UNCOMMON), false));
    public static final RegistryObject<Item> BROKEN_SOUL_PEARL = ITEMS.register(
            "broken_soul_pearl", () -> new SoulPearlItem(properties(16, Rarity.RARE), true));
    public static final RegistryObject<Item> MYRIAD_SOUL_PEARL = ITEMS.register(
            "myriad_soul_pearl", () -> new SoulUtilityPearlItem(properties(1, Rarity.EPIC), true));
    public static final RegistryObject<Item> CREATIVE_MYRIAD_SOUL_PEARL = ITEMS.register(
            "creative_myriad_soul_pearl", () -> new SoulUtilityPearlItem(
                    properties(1, Rarity.EPIC), true, true));
    public static final RegistryObject<Item> DESTROY_SOUL_PEARL = ITEMS.register(
            "destroy_soul_pearl", () -> new SoulUtilityPearlItem(properties(1, Rarity.RARE), false));
    public static final RegistryObject<Item> SOUL_ENERGY_PEARL = ITEMS.register(
            "soul_energy_pearl", () -> new SoulEnergyPearlItem(properties(1, Rarity.EPIC), false));
    public static final RegistryObject<Item> CREATIVE_SOUL_ENERGY_PEARL = ITEMS.register(
            "creative_soul_energy_pearl", () -> new SoulEnergyPearlItem(properties(1, Rarity.EPIC), true));
    public static final RegistryObject<Item> HUMAN_EMPEROR_BANNER = ITEMS.register(
            "human_emperor_banner", () -> new HumanEmperorBannerItem(properties(1, Rarity.EPIC).durability(256)));
    /** 化灵佩：右键将 10 点灵力转换为 1 点经验，以灵力球的形式呈现；手持时持续发动。 */
    public static final RegistryObject<Item> HUA_LING_PEI = ITEMS.register(
            "hualing_pei", () -> new HuaLingPeiItem(properties(1, Rarity.UNCOMMON)));
    /** 创造丹：一步登天的至高神丹，服用后境界、功法、神通、心法、异火、灵根全部圆满。 */
    public static final RegistryObject<Item> CREATION_PILL = ITEMS.register(
            "creation_pill", () -> new CreationPillItem(properties(1, Rarity.EPIC)));
    /** 岳币：铁砧砸中灵石后产生的彩蛋物品。 */
    public static final RegistryObject<Item> YUE_COIN = ITEMS.register(
            "yue_coin", () -> new Item(properties(64, Rarity.EPIC)));
    public static final RegistryObject<Item> FUNIAO_FU_FRAGMENT = ITEMS.register(
            "funiao_fu_fragment", () -> new DivineManualItem("tian_di_wei_lu", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> DAODE_JING_FRAGMENT = ITEMS.register(
            "daode_jing_fragment", () -> new DivineManualItem("fa_tian_xiang_di", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> GANGDOU_STARMAP = ITEMS.register(
            "gangdou_starmap", () -> new DivineManualItem("ta_gang_bu_dou", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> ZHONGLV_FRAGMENT = ITEMS.register(
            "zhonglv_fragment", () -> new DivineManualItem("yuan_shen_chu_qiao", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> YIN_FU_JING_YI_SHEN_FRAGMENT = ITEMS.register(
            "yin_fu_jing_yi_shen_fragment", () -> new DivineManualItem("yi_hun_ji_po", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> DA_PIN_TIAN_XIAN_JUE_FRAGMENT_ONE = ITEMS.register(
            "da_pin_tian_xian_jue_fragment_one", () -> new DivineManualItem("qi_shi_er_bian", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> DA_PIN_TIAN_XIAN_JUE_FRAGMENT_TWO = ITEMS.register(
            "da_pin_tian_xian_jue_fragment_two", () -> new DivineManualItem("jin_dou_yun", properties(1, Rarity.EPIC)));
    public static final RegistryObject<Item> RI_YUE_TING_LUN_FRAGMENT = ITEMS.register(
            "ri_yue_ting_lun_fragment", () -> new DivineManualItem("ri_yue_ting_lun", properties(1, Rarity.EPIC)));

    // ===== 符箓系统 =====

    /** 刻刀：镌印台的刻字工具，木棍+玄铁锭合成。 */
    public static final RegistryObject<Item> CARVING_KNIFE = ITEMS.register(
            "carving_knife", () -> new CarvingKnifeItem(properties(1, Rarity.UNCOMMON)));
    /** 灵墨：灵煤 + 异火转化所得。一瓶可反复蘸用，耗尽耐久才作废。 */
    public static final RegistryObject<Item> SPIRIT_INK = ITEMS.register(
            "spirit_ink", () -> new Item(inkProperties(32, Rarity.COMMON)));
    public static final RegistryObject<Item> PILL_ASH = ITEMS.register(
            "pill_ash", () -> new cn.blockforge.generated.generatedmod.plants.PillAshItem(properties(64,Rarity.COMMON)));
    public static final RegistryObject<Item> WASTE_PILL=ITEMS.register("waste_pill",()->new Item(properties(64,Rarity.COMMON)));
    /** 朱砂墨：灵墨 + 金粒 + 红石粉合成。耐久更足，是画符拓印的主用墨。 */
    public static final RegistryObject<Item> CINNABAR_INK = ITEMS.register(
            "cinnabar_ink", () -> new Item(inkProperties(64, Rarity.UNCOMMON)));
    /** 符纸：黄色颜料 + 纸合成。 */
    public static final RegistryObject<Item> TALISMAN_PAPER = ITEMS.register(
            "talisman_paper", () -> new Item(properties(64, Rarity.COMMON)));
    /** 印章：镌印台产物，携带符箓符文（NBT），可反复拓印。 */
    public static final RegistryObject<Item> SEAL = ITEMS.register(
            "seal", () -> new SealItem(properties(16, Rarity.UNCOMMON)));
    /** 符咒：刻印/绘制成品，右键触发效果并消耗。 */
    public static final RegistryObject<Item> TALISMAN = ITEMS.register(
            "talisman", () -> new TalismanItem(properties(64, Rarity.UNCOMMON)));
    public static final RegistryObject<Item> TALISMAN_GATHER = talisman("gather");
    public static final RegistryObject<Item> TALISMAN_SPEED = talisman("speed");
    public static final RegistryObject<Item> TALISMAN_THUNDER = talisman("thunder");
    public static final RegistryObject<Item> TALISMAN_WARD = talisman("ward");
    public static final RegistryObject<Item> TALISMAN_THORN = talisman("thorn");
    public static final RegistryObject<Item> TALISMAN_FIRE = talisman("fire");
    public static final RegistryObject<Item> TALISMAN_PURGE = talisman("purge");
    private static RegistryObject<Item> talisman(String id){
        return ITEMS.register("talisman_"+id,()->new TalismanItem(properties(64,Rarity.UNCOMMON),id));
    }
    public static Item talismanItem(String id){
        return switch(id){
            case "gather" -> TALISMAN_GATHER.get(); case "speed" -> TALISMAN_SPEED.get();
            case "thunder" -> TALISMAN_THUNDER.get(); case "ward" -> TALISMAN_WARD.get();
            case "thorn" -> TALISMAN_THORN.get(); case "fire" -> TALISMAN_FIRE.get();
            case "purge" -> TALISMAN_PURGE.get();
            default -> throw new IllegalArgumentException("Unknown craftable talisman: "+id);
        };
    }
    /** 寻真司南：生物指南针，右键输入生物名搜寻最近生物。 */
    public static final RegistryObject<Item> CREATURE_COMPASS = ITEMS.register(
            "creature_compass", () -> new CreatureCompassItem(properties(1, Rarity.RARE)));
    /** 《万法归宗》残卷：解锁画符箓的权限。 */
    public static final RegistryObject<Item> WANFA_GUIZONG = ITEMS.register(
            "wanfa_guizong", () -> new WanfaGuizongItem(properties(1, Rarity.EPIC)));

    private static RegistryObject<Item> fireSeed(String id, int tier, Rarity rarity) {
        return ITEMS.register(id, () -> new FireSeedItem(tier, properties(1, rarity)));
    }

    private static RegistryObject<Item> unifiedPill(String id) {
        return ITEMS.register(id, () -> new cn.blockforge.generated.generatedmod.alchemy.UnifiedPillItem(properties(64, Rarity.COMMON)));
    }

    private static RegistryObject<Item> art(String id, int tier) {
        return ITEMS.register(id, () -> new ArtManualItem(id, tier, properties(1, tier >= 4 ? Rarity.EPIC : tier >= 3 ? Rarity.RARE : tier == 2 ? Rarity.UNCOMMON : Rarity.COMMON)));
    }

    private static RegistryObject<Item> heart(String id) {
        return ITEMS.register("heart_method_" + id, () -> new HeartMethodItem(id, properties(1, id.equals("wanling") ? Rarity.EPIC : Rarity.UNCOMMON)));
    }

    private static RegistryObject<Item> sword(String id, net.minecraft.world.item.Tier tier, float damage, String tierId,
                                              int maxSpirit, int distance, int cost, int pierce, Rarity rarity) {
        return ITEMS.register(id, () -> new SpiritSwordItem(tier, damage, properties(1, rarity),
                tierId, maxSpirit, distance, cost, pierce));
    }

    private static RegistryObject<Item> bossEgg(String id, int primary, int secondary) {
        return ITEMS.register(id + "_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                () -> cn.blockforge.generated.generatedmod.registry.ModEntities.VISUAL_BOSSES.get(id).get(),
                primary, secondary, properties(64, Rarity.EPIC)));
    }

    private static RegistryObject<Item> effect(String id, EffectPillItem.Effect effect) {
        return unifiedPill(id);
    }


    @SuppressWarnings("unchecked")
    private static RegistryObject<Item>[] materials(String base) {
        return new RegistryObject[]{
            ITEMS.register("low_" + base, () -> new Item(properties(64, Rarity.COMMON))),
            ITEMS.register("mid_" + base, () -> new Item(properties(64, Rarity.UNCOMMON))),
            ITEMS.register("high_" + base, () -> new Item(properties(64, Rarity.RARE))),
            ITEMS.register("perfect_" + base, () -> new Item(properties(64, Rarity.EPIC)))
        };
    }

    private static Item.Properties properties(int count, Rarity rarity) {
        return new Item.Properties().stacksTo(count).rarity(rarity);
    }

    /** 墨水瓶：不可堆叠，带耐久；在符箓工作台里消耗的是耐久而不是整瓶。 */
    private static Item.Properties inkProperties(int uses, Rarity rarity) {
        return new Item.Properties().stacksTo(1).rarity(rarity).durability(uses);
    }

    private WuXingItems() {}
}
