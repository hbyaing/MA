"""Extract text-free formal skins from the user-approved v2 reference."""
from pathlib import Path
import hashlib
import json
import shutil
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'reference_assets/heavenly-cleanup-20261027/design-v2.png'
OUT = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/gui/heavenly_v2'
OUT.mkdir(parents=True, exist_ok=True)
image = Image.open(SOURCE).convert('RGBA')
manifest = {'approved': True, 'generator': 'built-in image_gen', 'source': str(SOURCE.relative_to(ROOT)), 'elements': {}}
# Every center is made opaque and text-free. No baked labels, numbers, or item glow remain.
for name, box, inset in [
    ('frame', (23, 19, 1386, 1088), 20),
    ('panel', (519, 251, 1348, 382), 12),
    ('button', (520, 975, 1128, 1052), 10),
    ('selected', (66, 250, 487, 364), 12),
    ('row', (70, 373, 485, 477), 8),
    ('slot', (521, 589, 618, 683), 7),
    ('search', (69, 162, 485, 229), 10),
]:
    part = image.crop(box)
    w, h = part.size
    ImageDraw.Draw(part).rectangle((inset, inset, w-inset-1, h-inset-1), fill=(3,17,24,255))
    # Outer corners are genuinely transparent, not a copied dark halo from the mockup.
    mask = Image.new('L', part.size, 255)
    draw = ImageDraw.Draw(mask)
    if name == 'frame':
        draw.polygon([(0,0),(7,0),(0,7)],fill=0)
        draw.polygon([(w-1,0),(w-8,0),(w-1,7)],fill=0)
        draw.polygon([(0,h-1),(7,h-1),(0,h-8)],fill=0)
        draw.polygon([(w-1,h-1),(w-8,h-1),(w-1,h-8)],fill=0)
    part.putalpha(mask)
    target=OUT / (name+'.png'); part.save(target)
    manifest['elements'][name]={'crop':box,'size':[w,h],'slice':inset,'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}
for name in ['previous','next']:
    shutil.copy2(ROOT / f'src/main/resources/assets/wuxingxiantu/textures/gui/sense_heavenly_v1/{name}.png', OUT / (name+'.png'))
shutil.copy2(ROOT/'src/main/resources/assets/wuxingxiantu/textures/gui/forged/search_icon.png', OUT/'search_icon.png')
for name in ['previous','next','search_icon']:
    path=OUT/(name+'.png')
    manifest['elements'][name]={'source':'existing formal control icon','size':list(Image.open(path).size),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
(SOURCE.parent/'component-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# A self-contained page is served through the existing unified UI workbench.
lab=ROOT/'deliverables/ui-workbench-20261017/heavenly-v2'
lab.mkdir(parents=True,exist_ok=True)
shutil.copy2(SOURCE,lab/'reference.png')
for path in OUT.glob('*.png'): shutil.copy2(path,lab/path.name)
shutil.copy2(SOURCE.parent/'component-manifest.json',lab/'component-manifest.json')
html='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>天地为炉 · 第二版素材与实机核对</title>
<style>body{background:#071118;color:#e7dec9;font:16px system-ui;padding:24px}a{color:#70dadb}img{max-width:100%%}main{display:grid;grid-template-columns:1fr 1fr;gap:24px}.asset{display:inline-flex;flex-direction:column;gap:8px;margin:12px}.skin{width:220px;height:60px;object-fit:fill;image-rendering:auto}.slot{width:80px;height:80px}button{background:#0a222c;color:#e7c57c;border:1px solid #b38e47;padding:8px}input{accent-color:#72d9d6}</style>
<h1>天地为炉 · 已批准第二版</h1><p><a href="../index.html">返回统一UI工作台</a> · <a href="component-manifest.json">完整素材坐标与哈希</a></p>
<p>正式素材来自批准图；文字、物品与数量由游戏实时绘制。下面是素材预览，实机结果完成后在右侧更新。</p>
<label>预览比例 <input type="range" min="30" max="100" value="80" oninput="document.querySelector('main').style.zoom=this.value+'%%'"></label>
<main><section><h2>批准参考</h2><img src="reference.png"></section><section><h2>实机复验</h2><img src="native.png" alt="实机验收尚未生成"><h2>完整素材</h2>%s</section></main>'''
html=html % ''.join(f'<div class="asset">{name}<img class="skin {"slot" if name=="slot" else ""}" src="{name}.png"></div>' for name in manifest['elements'])
(lab/'index.html').write_text(html,encoding='utf-8')
index=lab.parent/'index.html'
page=index.read_text(encoding='utf-8')
if 'heavenly-v2/index.html' not in page:
    link='<p><a style="color:#70d8e3" href="heavenly-v2/index.html">天地为炉 · 已批准第二版与实机核对</a></p>'
    index.write_text(page.replace('</header>',link+'</header>',1),encoding='utf-8')
