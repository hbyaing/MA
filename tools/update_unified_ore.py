"""Generate the unified ore loot tables (including legacy registry aliases)."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
p = root / 'src/main/resources/wuxingxiantu.forging.mixins.json'
d = json.loads(p.read_text())
if 'JeiInventoryEffectsMixin' not in d['client']:
    d['client'].append('JeiInventoryEffectsMixin')
p.write_text(json.dumps(d, indent=2) + chr(10))
silk = {'condition': 'minecraft:match_tool', 'predicate': {'enchantments': [
    {'enchantment': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}
entries = [{'type': 'minecraft:item', 'name': 'wuxingxiantu:spirit_stone' + suffix, 'weight': weight}
           for suffix, weight in [('', 60), ('_middle', 25), ('_superior', 10), ('_exquisite', 5)]]
table = {'type': 'minecraft:block', 'pools': [
    {'rolls': 1, 'conditions': [silk], 'entries': [
        {'type': 'minecraft:item', 'name': 'wuxingxiantu:spirit_vein_ore'}]},
    {'rolls': 1, 'conditions': [{'condition': 'minecraft:inverted', 'term': silk}],
     'entries': entries, 'functions': [
         {'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'},
         {'function': 'minecraft:explosion_decay'}]}]}
for suffix in ['', '_middle', '_superior', '_exquisite']:
    p = root / ('src/main/resources/data/wuxingxiantu/loot_tables/blocks/spirit_vein_ore' + suffix + '.json')
    p.write_text(json.dumps(table, indent=2) + chr(10))
