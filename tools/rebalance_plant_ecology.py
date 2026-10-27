"""Rebuild the 1.20.1 plant habitat resources from an explicit species plan."""
import json
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src/main/resources/data/wuxingxiantu'
# id, conditional regional selection probability, placement rule, minimum height, habitats
PLAN = [
('spirit_sprout_grass', .70, 'SURFACE_SOIL', -64, 'plains sunflower_plains meadow forest birch_forest'),
('dew_drop_flower', .55, 'SURFACE_NEAR_WATER', -64, 'river flower_forest swamp mangrove_swamp'),
('clear_heart_lotus', .30, 'WATER_SURFACE', -64, 'river swamp mangrove_swamp'),
('spirit_ginseng', .28, 'FOREST_FLOOR', -64, 'taiga old_growth_pine_taiga old_growth_spruce_taiga old_growth_birch_forest'),
('red_sun_grass', .25, 'SURFACE_SOIL', -64, 'savanna savanna_plateau windswept_savanna'),
('vein_mending_vine', .12, 'SURFACE_NEAR_WATER', -64, 'jungle sparse_jungle bamboo_jungle'),
('blood_spirit_flower', .09, 'CAVE_FLOOR', -64, 'dark_forest swamp dripstone_caves'),
('adamantine_vine', .10, 'SURFACE_ROCKY', 64, 'windswept_hills windswept_gravelly_hills stony_peaks'),
('gale_grass', .13, 'SURFACE_HIGHLAND', 80, 'meadow windswept_hills windswept_forest'),
('dark_water_weed', .11, 'WATER_BODY', -64, 'cold_ocean deep_cold_ocean frozen_ocean deep_frozen_ocean frozen_river'),
('thunder_vein_grass', .08, 'SURFACE_HIGHLAND', 96, 'windswept_hills windswept_forest stony_peaks'),
('starlight_flower', .035, 'SURFACE_HIGHLAND', 96, 'meadow cherry_grove'),
('dragon_scale_grass', 1.0, 'END_MAIN_ISLAND', -64, 'the_end'),
('glowing_blue_mushroom', .36, 'CAVE_FLOOR', -64, 'lush_caves taiga old_growth_pine_taiga old_growth_spruce_taiga'),
('herb_juling', .32, 'FOREST_FLOOR', -64, 'forest birch_forest flower_forest'),
('herb_huayuan', .10, 'FOREST_FLOOR', -64, 'old_growth_birch_forest cherry_grove'),
('herb_xisui', .11, 'SURFACE_NEAR_WATER', -64, 'river jungle sparse_jungle'),
('herb_pozhang', .025, 'SURFACE_HIGHLAND', 112, 'meadow windswept_forest'),
('herb_huichun', .34, 'SURFACE_SOIL', -64, 'flower_forest birch_forest meadow'),
('herb_xingchen', .03, 'SURFACE_HIGHLAND', 96, 'cherry_grove meadow'),
('herb_tianji', .02, 'SURFACE_HIGHLAND', 112, 'windswept_forest cherry_grove'),
('herb_huanling', .07, 'FOREST_FLOOR', -64, 'dark_forest old_growth_spruce_taiga'),
('herb_fengling', .30, 'SURFACE_SOIL', -64, 'plains sunflower_plains savanna'),
('herb_hanbing', .12, 'SURFACE_SOIL', -64, 'snowy_taiga snowy_plains grove'),
('herb_lieyang', .11, 'SURFACE_SOIL', -64, 'savanna savanna_plateau windswept_savanna'),
('herb_jingang', .09, 'SURFACE_ROCK_EDGE', 64, 'windswept_hills windswept_gravelly_hills'),
('herb_duzhen', .10, 'SURFACE_NEAR_WATER', -64, 'swamp mangrove_swamp jungle'),
('herb_jifeng', .12, 'SURFACE_HIGHLAND', 80, 'windswept_hills windswept_forest meadow'),
('herb_lingzhi', .08, 'FOREST_FLOOR', -64, 'dark_forest old_growth_pine_taiga old_growth_spruce_taiga'),
('herb_leiteng', .07, 'SURFACE_HIGHLAND', 96, 'windswept_forest windswept_hills'),
('herb_yanjia', .10, 'SURFACE_ROCK_EDGE', 64, 'windswept_gravelly_hills windswept_hills'),
('herb_lingdao', .38, 'SURFACE_NEAR_WATER', -64, 'river plains swamp'),
]

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def main():
    species = {row[0] for row in PLAN}
    archive = Path('E:/垃圾/2026-10-27/植物生态旧生成规则')
    # Only retire modifiers whose features are entirely replaced by this plan.
    for path in (DATA / 'forge/biome_modifier').glob('*.json'):
        obj = json.loads(path.read_text(encoding='utf-8'))
        features = obj.get('features', [])
        if isinstance(features, str): features = [features]
        if features and all(f.removeprefix('wuxingxiantu:') in species for f in features):
            if path.stem in {'add_' + s for s in species}: continue
            archive.mkdir(parents=True, exist_ok=True)
            target = archive / path.name
            if target.exists(): raise RuntimeError(f'Backup exists: {target}')
            shutil.move(str(path), str(target))
    for name, chance, rule, min_y, biomes in PLAN:
        # Dragon grass uses the approved three-plant seven-day cycle, not decoration.
        if name == 'dragon_scale_grass': continue
        tag = f'plant_habitats/{name}'
        write(DATA / f'tags/worldgen/biome/{tag}.json', {'replace': False, 'values': ['minecraft:' + b for b in biomes.split()]})
        injection = '#minecraft:is_overworld' if rule == 'CAVE_FLOOR' else '#wuxingxiantu:' + tag
        write(DATA / f'forge/biome_modifier/add_{name}.json', {'type': 'forge:add_features', 'biomes': injection, 'features': ['wuxingxiantu:' + name], 'step': 'vegetal_decoration'})
        path = DATA / f'worldgen/configured_feature/{name}.json'
        obj = json.loads(path.read_text(encoding='utf-8'))
        obj['config'].update(rule=rule, biome_tag='wuxingxiantu:' + tag, min_y=min_y, open_sky=rule == 'SURFACE_HIGHLAND')
        write(path, obj)
    tree_biomes = ['forest', 'flower_forest', 'birch_forest', 'cherry_grove', 'meadow']
    write(DATA / 'forge/biome_modifier/add_peach_trees.json', {'type': 'forge:add_features', 'biomes': ['minecraft:' + b for b in tree_biomes], 'features': 'wuxingxiantu:peach_tree', 'step': 'vegetal_decoration'})
    tree = DATA / 'worldgen/placed_feature/peach_tree.json'
    obj = json.loads(tree.read_text(encoding='utf-8'))
    obj['placement'][0]['chance'] = 32
    write(tree, obj)
    entries = ',\n'.join(f'        Map.entry("{name}", {chance})' for name, chance, *_ in PLAN)
    java = '''package cn.blockforge.generated.generatedmod.plants;

import java.util.Map;

/** Species admission probability after the existing regional density gate.
 * Generated by tools/rebalance_plant_ecology.py; never a per-chunk spawn promise. */
public final class SpiritPlantEcology {
    private static final Map<String,Double> CHANCES = Map.ofEntries(
%s);
    public static double chance(String species, WildRarity fallback) {
        return CHANCES.getOrDefault(species, switch (fallback) {
            case COMMON -> .60; case UNCOMMON -> .25; case RARE -> .08;
            case VERY_RARE -> .02; case SPECIAL -> 1.0;
        });
    }
    private SpiritPlantEcology() {}
}
''' % entries
    (ROOT / 'src/main/java/cn/blockforge/generated/generatedmod/plants/SpiritPlantEcology.java').write_text(java, encoding='utf-8')
    lines = ['# 植物生态重划 · 2026-10-27', '', '覆盖32种灵植及桃树。仅影响新生成区块；保留既有植株、药龄与已决定区域配额。', '', '下表概率为通过区域密度门槛后的物种入选概率，不是每区块实得率。区域门槛仍为普通60%、受限35%、末地8%；每区域最多3株、间距48格，稀有物种竞争一个名额，地形合法性和材料生境会继续降低实际出现率。', '', '|植物 ID|入选概率|落点规则|最低 Y|群系|', '|---|---:|---|---:|---|']
    lines += [f'|{name}|{chance:.1%}|{rule}|{y}|{biomes}|' for name, chance, rule, y, biomes in PLAN if name != 'dragon_scale_grass']
    lines += ['', '龙鳞草保留已确认的独立末地主岛规则：最多3株，每7游戏日随机换点，不叠加区块生成。其配置特征保留兼容，但不注入群系。']
    lines += ['', '桃树：forest、flower_forest、birch_forest、cherry_grove、meadow；每区块1/32次尝试（原1/48），仍需树苗生存条件及空间。', '', '雪地药草选择有土壤的寒带；阳性草药选择稀树草原；灵稻和毒草选择近水；林下药草使用穿透树冠的落点搜索。洞穴物种仍需地下、低光及合法基质。']
    (ROOT / 'docs/plant-ecology-20261027.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')

if __name__ == '__main__': main()
