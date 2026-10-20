from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,shutil,hashlib,base64,io,datetime
R=Path('E:/五行仙途/source-assets/spirit-eater-beetle');D=Path('E:/五行仙途/deliverables/噬灵虫模型');W=R/'work'
font_path='C:/Windows/Fonts/msyh.ttc';f=ImageFont.truetype(font_path,24);small=ImageFont.truetype(font_path,18)
canvas=Image.new('RGB',(1120,470),(28,33,36));draw=ImageDraw.Draw(canvas)
for i,(prefix,title) in enumerate([('before','衔接前'),('after','衔接后')]):
 im=Image.open(W/f'v6_{prefix}_junction_bottom.png').convert('RGBA').crop((285,300,815,660)).rotate(180)
 canvas.paste(im,(20+i*560,58),im);draw.text((22+i*560,18),title,font=f,fill=(213,226,220))
draw.text((22,434),'首节腹甲根部前伸、藏入胸甲底面；两侧青色腹膜保留。',font=small,fill=(176,199,188));canvas.save(W/'胸腹衔接_前后对比_v6.png')
canvas=Image.new('RGB',(1280,1060),(28,33,36));draw=ImageDraw.Draw(canvas)
for i,(name,label) in enumerate([('front','整体'),('side','侧面'),('underside','腹甲分节'),('rear','尾腹')]):
 im=Image.open(W/f'v6_final_{name}.png').convert('RGBA');box=im.getchannel('A').getbbox();im=im.crop(box);im.thumbnail((600,450),Image.Resampling.LANCZOS)
 x=(i%2)*640;y=(i//2)*530;canvas.paste(im,(x+(640-im.width)//2,y+60+(450-im.height)//2),im);draw.text((x+24,y+18),label,font=f,fill=(213,226,220))
canvas.save(W/'腹部优化_四视图_v6.png')
# A dated recoverable copy is created before either canonical model is replaced.
bak=D/'备份_20261007_腹部优化前';bak.mkdir(exist_ok=True,parents=True)
for n in ['噬灵虫.bbmodel','说明.txt']:
 if (D/n).exists() and not (bak/n).exists():shutil.copy2(D/n,bak/n)
if (R/'噬灵虫.bbmodel').exists() and not (R/'before_v6_canonical_sync_20261007.bbmodel').exists():shutil.copy2(R/'噬灵虫.bbmodel',R/'before_v6_canonical_sync_20261007.bbmodel')
final=R/'噬灵虫_腹部优化_v6.bbmodel';model=json.loads(final.read_text('utf8'))
for t in model['textures']:
 assert t.get('source','').startswith('data:image/png;base64,'),t['name'];Image.open(io.BytesIO(base64.b64decode(t['source'].split(',',1)[1]))).verify()
assert len(model['elements'])==102 and len(model['textures'])==9
assert not any(t['name']=='背甲_清晰甲壳_v2.png' for t in model['textures'])
shutil.copy2(final,R/'噬灵虫.bbmodel');shutil.copy2(final,D/'噬灵虫.bbmodel');shutil.copy2(final,D/final.name)
for n in ['胸腹衔接_前后对比_v6.png','腹部优化_四视图_v6.png','abdomen_validation_v6.json','material_provenance_v6.json','junction_report_v6.json']:
 shutil.copy2(W/n,D/n)
for n in ['腹甲_弧缘分节UV_v6.png','腹部_克制微光UV_v6.png']:
 shutil.copy2(R/n,D/n)
for n in ['abdomen_v6_albedo.png','abdomen_v6_emission_mask.png']:shutil.copy2(W/n,D/n)
notes='''噬灵虫 · 腹部优化 v6（2026-10-07）

当前文件：噬灵虫.bbmodel，与 噬灵虫_腹部优化_v6.bbmodel 内容一致。

本轮修改：
- 下腹略收束，保留两侧淡青色腹膜；腹膜极点合并，修正退化面。
- 七节腹甲重新贴体，原参考图甲壳纹理按节独立展开，消除纵向条纹拉伸。
- 第一节腹甲根部局部向前延伸并藏进胸底甲，遮住中央青色断缝。
- 尾腹中央细缝贴合实际表面，腹部使用更克制的局部微光。
- 不改头部、眼睛、触角、口器、六足及既有背甲骨架/分组。
- 不使用用户弃用的“背甲_清晰甲壳_v2.png”。只从项目既有参考素材取样。

检查：102个网格、9张内嵌贴图；UV与贴图引用有效。
七块腹甲为封闭网格；检查了暴露腹甲的表面采样。首节前端为有意嵌入胸甲的隐藏搭接。
验证详情见 abdomen_validation_v6.json，衔接见 胸腹衔接_前后对比_v6.png。
旧模型和说明保存在“备份_20261007_腹部优化前”。其余旧版预览为历史文件；请以带v6名称的预览为准。

注意：这是Blockbench free-mesh源模型，尚未转换或验证GeckoLib/游戏运行时。
Blockbench腹部emissive贴图的alpha是反向发光控制，不是游戏透明度。
独立albedo与emission_mask已另外提供，接入游戏时需自行映射渲染材质。
'''
(D/'说明.txt').write_text(notes,encoding='utf8');(R/'腹部优化_v6_说明.txt').write_text(notes,encoding='utf8')
# Confirm only the first ventral plate changed during the specific junction follow-up.
before=json.loads((R/'before_thorax_junction_v6_20261007.bbmodel').read_text('utf8'))
def canon(e,doc):
 e=json.loads(json.dumps(e))
 for v in e.get('faces',{}).values():
  t=v.get('texture')
  if isinstance(t,int) and not isinstance(t,bool):v['texture']=doc['textures'][t]['uuid']
 return e
B={e['uuid']:e for e in before['elements']};changes=[e['name'] for e in model['elements'] if canon(e,model)!=canon(B[e['uuid']],before)]
assert changes==['贴合腹甲_1'],changes
assert model['outliner']==before['outliner'] and model.get('groups')==before.get('groups')
report={'junction_changed_meshes':changes,'outliner_preserved':True,'groups_preserved':True,'all_textures_embedded_and_decodable':True,'source_sha256':hashlib.sha256(final.read_bytes()).hexdigest(),'delivery_sha256':hashlib.sha256((D/'噬灵虫.bbmodel').read_bytes()).hexdigest(),'mesh_count':102,'texture_count':9}
(D/'junction_delivery_validation_v6.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(report,ensure_ascii=False))
