"""Save unmasked native UI and HUD crops for manual reference comparison."""
from pathlib import Path
import shutil,json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
QA=ROOT/'reports/sword-terrain-stall-20261027'
OUT=ROOT/'reference_assets/sense-footer-ui-20261027/evidence';OUT.mkdir(parents=True,exist_ok=True)
source=QA/'sense-addon-verified/ui'
im=Image.open(source/'sense-normal.png').convert('RGB')
shutil.copy2(source/'sense-normal.png',OUT/'native.png')
shutil.copy2(source/'sense-addon-creation.png',OUT/'creation-open.png')
shutil.copy2(source/'result.json',OUT/'with-creation-result.json')
shutil.copy2(source/'sense-normal.png',ROOT/'deliverables/ui-workbench-20261017/sense-v2/native.png')
parts={'top-left':(316,304,358,346),'top-right':(1242,304,1284,346),
       'bottom-left':(316,854,358,896),'bottom-right':(1242,854,1284,896),
       'top':(350,304,1250,316),'bottom':(350,882,1250,897),
       'left':(316,342,330,860),'right':(1268,342,1284,860),
       'title':(700,306,900,360),'card':(492,486,854,604),'action':(751,551,843,593),
       'status':(492,739,1226,793),'creation':(492,802,924,854),'ascension':(934,802,1226,854)}
for name,box in parts.items():
    part=im.crop(box);part.resize((part.width*3,part.height*3),Image.Resampling.NEAREST).save(OUT/(name+'.png'))
states=['sense-normal','sense-hover','sense-focus','sense-low-power','sense-narrow','sense-addon-hover','sense-addon-focus','sense-addon-disabled']
sheet=Image.new('RGB',(1440,4*450),(8,16,22));draw=ImageDraw.Draw(sheet)
for i,name in enumerate(states):
    scene=Image.open(source/(name+'.png')).convert('RGB')
    if name=='sense-narrow':scene=scene.resize((700,420),Image.Resampling.LANCZOS)
    else:scene=scene.crop((320,308,1280,894)).resize((700,427),Image.Resampling.LANCZOS)
    x=(i%2)*720;y=(i//2)*450;draw.text((x+8,y+3),name,fill='white');sheet.paste(scene,(x+8,y+20))
sheet.save(OUT/'states.png')
hud=ROOT/'source-assets/hud-transparent-20261027/evidence';hud.mkdir(parents=True,exist_ok=True)
for mode in ['native-hud-final','bliss-hud-final']:
    for case in ['light','dark']:
        image=Image.open(QA/mode/('hud-'+case+'.png')).convert('RGB').crop((118,140,312,184))
        image.resize((image.width*4,image.height*4),Image.Resampling.NEAREST).save(hud/(mode+'-'+case+'.png'))
    shutil.copy2(QA/mode/'result.json',hud/(mode+'-result.json'))
print(json.dumps({'nativeCrops':list(parts),'states':states},ensure_ascii=False))
