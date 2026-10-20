"""Check that every registered art has a semantic attack range and V2 material recipe."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
catalog = (ROOT / 'src/main/java/cn/blockforge/generated/generatedmod/ArtCatalog.java').read_text('utf-8-sig')
vfx = (ROOT / 'src/main/java/cn/blockforge/generated/generatedmod/arts/ArtVfxCatalog.java').read_text('utf-8-sig')
ids = re.findall(r'new Entry\("(art_[^"]+)",', catalog)
recipes = set(re.findall(r'case "(art_[^"]+)" -> new Recipe', vfx))
profiles = set(re.findall(r'case "(art_[^"]+)" -> new AttackProfile', catalog))
missing = sorted(set(ids) - recipes)
assert not missing, f'missing V2 recipe: {missing}'
for art in profiles:
    assert f'case "{art}"' in vfx, f'missing VFX case: {art}'
materials = set()
for row in re.findall(r'new Recipe\("([^\"]+)",\s*"([^\"]+)",\s*"([^\"]+)"', vfx):
    materials.update(row)
for material in materials:
    particle = ROOT / 'src/main/resources/assets/wuxingxiantu/particles' / f'art_{material}.json'
    texture = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/particle/arts' / f'{material}.png'
    assert particle.exists() and texture.exists(), f'missing runtime material: {material}'
print(f'Art particle library: {len(ids)} arts, {len(profiles)} semantic attack profiles, {len(materials)} materials')
