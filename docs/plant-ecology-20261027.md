# 植物生态重划 · 2026-10-27

覆盖32种灵植及桃树。仅影响新生成区块；保留既有植株、药龄与已决定区域配额。

下表概率为通过区域密度门槛后的物种入选概率，不是每区块实得率。区域门槛仍为普通60%、受限35%、末地8%；每区域最多3株、间距48格，稀有物种竞争一个名额，地形合法性和材料生境会继续降低实际出现率。

|植物 ID|入选概率|落点规则|最低 Y|群系|
|---|---:|---|---:|---|
|spirit_sprout_grass|70.0%|SURFACE_SOIL|-64|plains sunflower_plains meadow forest birch_forest|
|dew_drop_flower|55.0%|SURFACE_NEAR_WATER|-64|river flower_forest swamp mangrove_swamp|
|clear_heart_lotus|30.0%|WATER_SURFACE|-64|river swamp mangrove_swamp|
|spirit_ginseng|28.0%|FOREST_FLOOR|-64|taiga old_growth_pine_taiga old_growth_spruce_taiga old_growth_birch_forest|
|red_sun_grass|25.0%|SURFACE_SOIL|-64|savanna savanna_plateau windswept_savanna|
|vein_mending_vine|12.0%|SURFACE_NEAR_WATER|-64|jungle sparse_jungle bamboo_jungle|
|blood_spirit_flower|9.0%|CAVE_FLOOR|-64|dark_forest swamp dripstone_caves|
|adamantine_vine|10.0%|SURFACE_ROCKY|64|windswept_hills windswept_gravelly_hills stony_peaks|
|gale_grass|13.0%|SURFACE_HIGHLAND|80|meadow windswept_hills windswept_forest|
|dark_water_weed|11.0%|WATER_BODY|-64|cold_ocean deep_cold_ocean frozen_ocean deep_frozen_ocean frozen_river|
|thunder_vein_grass|8.0%|SURFACE_HIGHLAND|96|windswept_hills windswept_forest stony_peaks|
|starlight_flower|3.5%|SURFACE_HIGHLAND|96|meadow cherry_grove|
|glowing_blue_mushroom|36.0%|CAVE_FLOOR|-64|lush_caves taiga old_growth_pine_taiga old_growth_spruce_taiga|
|herb_juling|32.0%|FOREST_FLOOR|-64|forest birch_forest flower_forest|
|herb_huayuan|10.0%|FOREST_FLOOR|-64|old_growth_birch_forest cherry_grove|
|herb_xisui|11.0%|SURFACE_NEAR_WATER|-64|river jungle sparse_jungle|
|herb_pozhang|2.5%|SURFACE_HIGHLAND|112|meadow windswept_forest|
|herb_huichun|34.0%|SURFACE_SOIL|-64|flower_forest birch_forest meadow|
|herb_xingchen|3.0%|SURFACE_HIGHLAND|96|cherry_grove meadow|
|herb_tianji|2.0%|SURFACE_HIGHLAND|112|windswept_forest cherry_grove|
|herb_huanling|7.0%|FOREST_FLOOR|-64|dark_forest old_growth_spruce_taiga|
|herb_fengling|30.0%|SURFACE_SOIL|-64|plains sunflower_plains savanna|
|herb_hanbing|12.0%|SURFACE_SOIL|-64|snowy_taiga snowy_plains grove|
|herb_lieyang|11.0%|SURFACE_SOIL|-64|savanna savanna_plateau windswept_savanna|
|herb_jingang|9.0%|SURFACE_ROCK_EDGE|64|windswept_hills windswept_gravelly_hills|
|herb_duzhen|10.0%|SURFACE_NEAR_WATER|-64|swamp mangrove_swamp jungle|
|herb_jifeng|12.0%|SURFACE_HIGHLAND|80|windswept_hills windswept_forest meadow|
|herb_lingzhi|8.0%|FOREST_FLOOR|-64|dark_forest old_growth_pine_taiga old_growth_spruce_taiga|
|herb_leiteng|7.0%|SURFACE_HIGHLAND|96|windswept_forest windswept_hills|
|herb_yanjia|10.0%|SURFACE_ROCK_EDGE|64|windswept_gravelly_hills windswept_hills|
|herb_lingdao|38.0%|SURFACE_NEAR_WATER|-64|river plains swamp|

龙鳞草保留已确认的独立末地主岛规则：最多3株，每7游戏日随机换点，不叠加区块生成。其配置特征保留兼容，但不注入群系。

桃树：forest、flower_forest、birch_forest、cherry_grove、meadow；每区块1/32次尝试（原1/48），仍需树苗生存条件及空间。

雪地药草选择有土壤的寒带；阳性草药选择稀树草原；灵稻和毒草选择近水；林下药草使用穿透树冠的落点搜索。洞穴物种仍需地下、低光及合法基质。
