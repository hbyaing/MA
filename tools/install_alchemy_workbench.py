"""Install the versioned alchemy UI workbench into the user's unified local hub."""
from pathlib import Path
import json
import shutil
import argparse

repo = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--hub', type=Path, default=Path.home() / 'Documents/Codex/Workbenches')
args = parser.parse_args()
if not (args.hub / '启动全部工作台.py').is_file():
    raise SystemExit('Open the unified launcher before installing a workbench.')
target = args.hub / 'myriad-ascension-alchemy'
target.mkdir(parents=True, exist_ok=True)
for name in ('index.html', 'workbench.js'):
    shutil.copy2(repo / 'tools/alchemy-workbench' / name, target / name)
assets = target / 'assets'
assets.mkdir(exist_ok=True)
textures = repo / 'src/main/resources/assets/wuxingxiantu/textures/gui'
for asset in (textures / 'alchemy_concept').glob('*.png'):
    shutil.copy2(asset, assets / asset.name)
for name in ('tl', 'top', 'left'):
    shutil.copy2(textures / 'wuxia/outer_frame_v2' / (name + '.png'), assets / ('frame-' + name + '.png'))
preview = repo / 'source-assets/myriad-alchemy-20261020/myriad-ascension-alchemy-controls-preview.png'
shutil.copy2(preview, target / 'approved-preview.png')
(target / 'workbench.json').write_text(json.dumps({
    'name': '万界仙途 · 炼丹工作台',
    'description': '八卦八槽、四阶段控火、独立注灵；正式游戏素材与部件放大检查。'
}, ensure_ascii=False, indent=2), encoding='utf-8')
print(target)
