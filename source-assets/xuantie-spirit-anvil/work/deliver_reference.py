from pathlib import Path
import json, math, base64, io, shutil, zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'reference-textured'
model=json.loads((OUT/'玄铁灵砧_原图贴图.bbmodel').read_text(encoding='utf-8'))
before=json.loads((ROOT/'玄铁灵砧_贴图精修.bbmodel').read_text(encoding='utf-8'))
geomkeys=['uuid','name','type','from','to','origin','rotation']
assert [{k:e.get(k) for k in geomkeys} for e in model['elements']]==[{k:e.get(k) for k in geomkeys} for e in before['elements']]
W,H=model['resolution']['width'],model['resolution']['height']
for e in model['elements']:
    assert all(math.isfinite(v) for v in e['from']+e['to'])
    for f in e['faces'].values():
        u,v,r,b=f['uv'];assert 0<=min(u,r)<=max(u,r)<=W and 0<=min(v,b)<=max(v,b)<=H
        assert isinstance(f['texture'],int) and 0<=f['texture']<len(model['textures'])
for t in model['textures']:
    assert t['source'].startswith('data:image/png;base64,')
    im=Image.open(io.BytesIO(base64.b64decode(t['source'].split(',')[1])));assert im.size==(W,H)
assert before['outliner']==model['outliner']
report=json.loads((OUT/'extraction_report.json').read_text(encoding='utf-8'))
report['uv_in_bounds']=True;report['embedded_textures']=len(model['textures']);report['validated_geometry_unchanged']=True
(OUT/'extraction_report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
(OUT/'说明.txt').write_text('''玄铁灵砧 · 原图提取贴图版

打开「玄铁灵砧_原图贴图.bbmodel」即可查看与编辑。贴图已内嵌。
模型保留 390 个方块、原几何与分组。1024×1024 图集。

表面来自用户提供的 1672×941 参考图：
顶面：x44..526、y196..430，投射至水平面。
正面砧体、中柱、铜包角、晶核、底座：按各部件分区提取。
侧面：使用参考右视图的对应可见面，按模型深度校正。
隐藏面：复用原图中干净的玄铁、铜与导纹样本，不是原画可见面的唯一还原。

reference_albedo.png 是原图颜色图集，保留图中的阴影、高光与蓝色光晕。
reference_preview.png 是 Blockbench 预览纹理，使用其 emissive 模式减轻光照重复。
top/front/side/base_extracted.png 是未经绘画修改的原图裁区，可直接对照。
perspective/front/right/top.png 来自 Blockbench 真实三维场景。

原图是渲染参考，不是原始展开贴图。因此需先处理表面视角、背景和 UV；
烘焙光照会随贴图保留，不能从这张图完整恢复原始无光照材质。
原图提取版与之前的重绘精修版分别保存。
''',encoding='utf-8')
dest=ROOT.parents[1]/'deliverables'/'玄铁灵砧原图贴图';dest.mkdir(parents=True,exist_ok=True)
names=['玄铁灵砧_原图贴图.bbmodel','reference_albedo.png','reference_preview.png','top_extracted.png','front_extracted.png','side_extracted.png','base_extracted.png','perspective.png','front.png','right.png','top.png','extraction_report.json','说明.txt']
for n in names:shutil.copy2(OUT/n,dest/n)
with zipfile.ZipFile(dest.parent/'玄铁灵砧原图贴图.zip','w',zipfile.ZIP_DEFLATED) as z:
    for n in names:z.write(dest/n,'玄铁灵砧原图贴图/'+n)
print(json.dumps({'validated':True,'cubes':len(model['elements']),'atlas':[W,H],'embedded_textures':len(model['textures'])}))
