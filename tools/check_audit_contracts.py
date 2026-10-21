"""Check stable protocol IDs and the canonical divine registry/resource boundary."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/cn/blockforge/generated/generatedmod'
source = (JAVA / 'network/NetworkChannel.java').read_text(encoding='utf-8-sig')
imports = dict((name.rsplit('.', 1)[-1], name) for name in re.findall(r'import ([\w.]+);', source))
actual = []
for direction, number, name in re.findall(r'\b(c2s|s2c)\((\d+),\s*([\w.]+)\.class', source):
    actual.append({'id': int(number), 'type': imports.get(name, name), 'direction': direction.upper()})
expected = json.loads((ROOT / 'tools/network-message-ids.json').read_text(encoding='utf-8-sig'))
assert sorted(actual, key=lambda row: row['id']) == expected, 'Protocol table changed; review IDs and version'
assert len({row['id'] for row in actual}) == len(actual), 'Duplicate packet ID'
legacy = 'guang' + '_yin_suo'
for path in (ROOT / 'src/main').rglob('*'):
    if not path.is_file():
        continue
    assert legacy not in path.name, f'Legacy resource filename: {path}'
    if path.suffix in {'.java', '.json'} and path.name != 'LegacyDivineMigration.java':
        assert legacy not in path.read_text(encoding='utf-8-sig'), f'Legacy active ID: {path}'
for locale in ('zh_cn', 'en_us'):
    lang = json.loads((ROOT / f'src/main/resources/assets/wuxingxiantu/lang/{locale}.json').read_text(encoding='utf-8-sig'))
    for art in ('fa_tian_xiang_di', 'ta_gang_bu_dou', 'yuan_shen_chu_qiao', 'yi_hun_ji_po', 'qi_shi_er_bian', 'jin_dou_yun', 'ri_yue_ting_lun'):
        assert '%1$s' in lang[f'divine.wuxingxiantu.function.{art}'], f'Hardcoded divine cost: {locale}/{art}'
print(f'Audit contracts: {len(actual)} directed packet IDs; canonical divine resources and dynamic costs')
