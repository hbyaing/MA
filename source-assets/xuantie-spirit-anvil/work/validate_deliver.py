from pathlib import Path
import json, base64, io, shutil, math, zipfile
from PIL import Image

root=Path(__file__).resolve().parents[1]
model=json.loads((root/'玄铁灵砧.bbmodel').read_text(encoding='utf-8'))
textures=model['textures'];elements=model['elements'];errors=[]
texture_ids={t['uuid'] for t in textures}
res=model['resolution'];w,h=res['width'],res['height']
for i,e in enumerate(elements):
    if e.get('type')!='cube':errors.append(f'Unexpected type {i}')
    if any(not math.isfinite(n) for n in e['from']+e['to']):errors.append(f'Nonfinite {i}')
    if any(e['to'][a]<=e['from'][a] for a in range(3)):errors.append(f'Degenerate cube {i}')
    for side,f in e['faces'].items():
        tex=f.get('texture')
        if not (tex in texture_ids or isinstance(tex,int) and 0<=tex<len(textures)):errors.append(f'Missing texture {i} {side}')
        uv=f['uv']
        if not (0<=min(uv[0],uv[2])<=max(uv[0],uv[2])<=w and 0<=min(uv[1],uv[3])<=max(uv[1],uv[3])<=h):errors.append(f'UV outside atlas {i} {side}')
for t in textures:
    assert t['source'].startswith('data:image/png;base64,')
    pic=Image.open(io.BytesIO(base64.b64decode(t['source'].split(',')[1])))
    assert pic.size==(w,h),(t['name'],pic.size)
ids=[e['uuid'] for e in elements]
assert len(set(ids))==len(ids),'Duplicate element UUIDs'
leaves=[]
def walk(nodes):
    for n in nodes:
        if isinstance(n,str):leaves.append(n)
        else:walk(n.get('children',[]))
walk(model['outliner']);assert sorted(leaves)==sorted(ids),'Outliner incomplete'
assert not errors,errors
report={'model':'玄铁灵砧','format':model['meta']['model_format'],'cubes':len(elements),'groups':10,'texture_size':[w,h],'embedded_textures':len(textures),'finite_geometry':True,'uv_in_bounds':True,'complete_outliner':True,'rendered_views':['perspective','front','right','top','back'],'errors':errors}
baseline=root/'work/backup-before-texture-v2/玄铁灵砧.bbmodel'
if baseline.exists():
    old=json.loads(baseline.read_text(encoding='utf-8'))
    keys=['uuid','name','type','from','to','origin','rotation']
    old_geometry=[{k:e.get(k) for k in keys} for e in old['elements']]
    new_geometry=[{k:e.get(k) for k in keys} for e in elements]
    assert old_geometry==new_geometry,'Texture refinement changed geometry'
    assert old['outliner']==model['outliner'],'Texture refinement changed hierarchy'
    report['geometry_unchanged_since_v1']=True
    report['revision']='texture_v2'
(root/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
readme='''玄铁灵砧 · Blockbench 模型

打开「玄铁灵砧.bbmodel」即可编辑，贴图已内嵌，无需重新指定路径。
390 个轴对齐方块，9 个功能分组及 1 个根组；1024×1024 像素贴图。
含厚砧面、四角铜包边、阶梯承托、中柱晶核、层叠底座和贯穿四面的蓝色导纹。

贴图精修 v2：像素密度由每单位 4 像素提高为 8 像素；加入细锻痕、磨损高光、
铜饰接合缝与铆点、灵纹亮芯及晶体斜切面。保留全部几何与分组。

尺寸约 24.12×16.20×12.20（Blockbench 单位，16 单位通常为 1 格）。
采用参考图的俯视长宽比约 2:1；参考图未展示的背面使用对称设计。
模型格式为 Blockbench 通用模型，全部几何均为 Cube，保留逐面 UV。

文件：
玄铁灵砧.bbmodel — 可编辑主工程，含两张内嵌贴图。
xuantie_anvil_atlas.png — 玄铁、铜饰和灵纹的完整颜色图集。
xuantie_anvil_emission.png — 黑底灵纹发光颜色图，供后续渲染接入使用。
xuantie_anvil_glow.png — Blockbench emissive 预览图，Alpha=12，供其原生发光模式使用。
perspective/front/right/top/back.png — 由 Blockbench 实际三维场景渲染的五视图。
validation.json — 几何、纹理、UV 和分组完整性检查。

蓝纹与核心已做 Blockbench 自发光显示；游戏中的泛光、动态光源和配方逻辑需要另行接入。
本次仅制作美术模型，没有修改模组代码或注册游戏内容。
'''
(root/'使用说明.txt').write_text(readme,encoding='utf-8')
dest=root.parents[1]/'deliverables'/'玄铁灵砧模型';dest.mkdir(parents=True,exist_ok=True)
names=['玄铁灵砧.bbmodel','xuantie_anvil_atlas.png','xuantie_anvil_emission.png','xuantie_anvil_glow.png','perspective.png','front.png','right.png','top.png','back.png','validation.json','使用说明.txt']
for name in names:shutil.copy2(root/name,dest/name)
zip_path=dest.parent/'玄铁灵砧模型.zip'
with zipfile.ZipFile(zip_path,'w',zipfile.ZIP_DEFLATED) as z:
    for name in names:z.write(dest/name,'玄铁灵砧模型/'+name)
print(json.dumps({'validation':report,'delivery':str(dest),'zip':str(zip_path)},ensure_ascii=False))
