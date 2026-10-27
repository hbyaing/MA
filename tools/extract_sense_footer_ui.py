"""Extract the approved sense cards, formal icons, status and addon controls."""
from pathlib import Path
import hashlib,json,shutil
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'reference_assets/sense-footer-ui-20261027'
OUT=ROOT/'src/main/resources/assets/wuxingxiantu/textures/gui/sense_v2'
OUT.mkdir(parents=True,exist_ok=True)
image=Image.open(SOURCE/'design-v2.png').convert('RGBA')
manifest={'approved':True,'generator':'built-in image_gen','elements':{}}
def save(name,part,box,kind):
    path=OUT/(name+'.png');part.save(path)
    manifest['elements'][name]={'crop':box,'size':list(part.size),'kind':kind,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
for name,box,inset in [
    ('panel',(289,280,888,486),12),('status',(289,731,1518,812),12),
    ('button',(675,401,860,464),12),('creation',(406,831,973,924),14),
    ('ascension',(1003,831,1384,925),14)]:
    part=image.crop(box);w,h=part.size
    if name in ('creation','ascension'):
        # The reference island/mountain touches the inner border. Rebuild only
        # straight edge spans from clean authored edge samples, preserving corners.
        part.paste(part.crop((w//2,0,w//2+1,inset)).resize((w-2*inset,inset)),(inset,0))
        part.paste(part.crop((w//2,h-inset,w//2+1,h)).resize((w-2*inset,inset)),(inset,h-inset))
        part.paste(part.crop((0,inset+2,inset,inset+3)).resize((inset,h-2*inset)),(0,inset))
        part.paste(part.crop((w-inset,inset+2,w,inset+3)).resize((inset,h-2*inset)),(w-inset,inset))
    ImageDraw.Draw(part).rectangle((inset,inset,w-inset-1,h-inset-1),fill=(3,17,24,255))
    # Cut the tiny corner exterior; preserve the authored bevel and gold trim.
    mask=Image.new('L',part.size,255);draw=ImageDraw.Draw(mask)
    for pts in [[(0,0),(5,0),(0,5)],[(w-1,0),(w-6,0),(w-1,5)],[(0,h-1),(5,h-1),(0,h-6)],[(w-1,h-1),(w-6,h-1),(w-1,h-6)]]:draw.polygon(pts,fill=0)
    part.putalpha(mask);save(name,part,box,'text-free formal skin')
for name,box,mode in [
    ('scan',(316,307,412,398),'cyan'),('identify',(941,303,1058,396),'cyan'),
    ('lock',(313,529,412,623),'cyan'),('survey',(940,529,1051,628),'cyan'),
    ('cost',(319,422,350,460),'cyan'),('creation_icon',(418,838,602,917),'colored'),
    ('ascension_icon',(1013,843,1117,918),'colored'),('arrow',(1290,861,1320,894),'gold')]:
    part=image.crop(box);pixels=[]
    for r,g,b,a in part.getdata():
        if mode=='cyan':alpha=int(255*min(1,max(0,(max(g,b)-r-12)/45))) if g>45 and b>r*1.05 else 0
        elif mode=='gold':alpha=255 if r>100 and g>65 and r>b*1.25 else 0
        else:alpha=int(255*min(1,max(0,(max(r,g,b)-35)/40)))
        pixels.append((r,g,b,alpha))
    part.putdata(pixels);save(name,part,box,'formal extracted icon')
(SOURCE/'component-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
lab=ROOT/'deliverables/ui-workbench-20261017/sense-v2';lab.mkdir(parents=True,exist_ok=True)
shutil.copy2(SOURCE/'design-v2.png',lab/'reference.png')
shutil.copy2(SOURCE/'component-manifest.json',lab/'component-manifest.json')
for path in OUT.glob('*.png'):shutil.copy2(path,lab/path.name)
html='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>识海 · 已批准第二版与创世入口</title><style>body{background:#06131a;color:#dcd5c3;font:16px system-ui;padding:24px}a{color:#70d9de}main{display:grid;grid-template-columns:1fr 1fr;gap:20px}img{max-width:100%}.skin{width:250px;height:70px;object-fit:fill}.glyph{width:64px;height:64px;object-fit:contain}.asset{display:inline-flex;flex-direction:column;gap:8px;margin:10px}</style><h1>识海 · 已批准第二版</h1><p><a href="../index.html">统一UI工作台</a> · <a href="component-manifest.json">完整素材坐标与哈希</a></p><main><section><h2>批准参考</h2><img src="reference.png"></section><section><h2>实机复验</h2><img src="native.png" alt="等待本轮实机结果"><h2>正式素材</h2>'''
html+=''.join(f'<div class="asset">{name}<img class="{"glyph" if "icon" in name or name in ["scan","identify","lock","survey","cost","arrow"] else "skin"}" src="{name}.png"></div>' for name in manifest['elements'])
(lab/'index.html').write_text(html+'</section></main>',encoding='utf-8')
index=lab.parent/'index.html';page=index.read_text(encoding='utf-8')
if 'sense-v2/index.html' not in page:index.write_text(page.replace('</header>','<p><a style="color:#70d8e3" href="sense-v2/index.html">识海第二版 · 创世入口与实机核对</a></p></header>',1),encoding='utf-8')
