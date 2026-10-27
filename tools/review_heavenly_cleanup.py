"""Produce unmasked native crops for manual comparison to the approved reference."""
from pathlib import Path
import json
import shutil
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
NATIVE=ROOT/'reports/heavenly-v2-final/ui'
EVIDENCE=ROOT/'reference_assets/heavenly-cleanup-20261027/evidence'
EVIDENCE.mkdir(parents=True,exist_ok=True)
im=Image.open(NATIVE/'heavenly-normal.png').convert('RGB')
shutil.copy2(NATIVE/'heavenly-normal.png',EVIDENCE/'native.png')
shutil.copy2(NATIVE/'heavenly-normal.png',ROOT/'deliverables/ui-workbench-20261017/heavenly-v2/native.png')
# Preserve world pixels outside each edge: these are QA crops, not artwork masks.
parts={
'top-left':(436,312,482,358),'top-right':(1118,312,1164,358),
'bottom-left':(436,842,482,888),'bottom-right':(1118,842,1164,888),
'top-title':(650,310,948,390),'bottom':(436,872,1164,888),
'left':(436,350,450,850),'right':(1150,350,1164,850),
'slots':(699,605,910,656),'search':(464,388,684,432),
}
for name,box in parts.items():
    part=im.crop(box)
    part.resize((part.width*3,part.height*3),Image.Resampling.NEAREST).save(EVIDENCE/(name+'.png'))
# Readable state sheet with real render output, including the same-screen clear transition.
names=['heavenly-normal','heavenly-live-no-match','heavenly-hover','heavenly-disabled','heavenly-focus','heavenly-search','heavenly-aux-chosen','heavenly-many-materials']
sheet=Image.new('RGB',(1440,4*600),(10,15,20));d=ImageDraw.Draw(sheet)
for i,name in enumerate(names):
    source=Image.open(NATIVE/(name+'.png')).convert('RGB')
    crop=source.crop((440,316,1160,884)).resize((700,552),Image.Resampling.LANCZOS)
    x=(i%2)*720;y=(i//2)*600
    d.text((x+8,y+5),name,fill='white');sheet.paste(crop,(x+8,y+25))
sheet.save(EVIDENCE/'states.png')
shutil.copy2(NATIVE/'result.json',EVIDENCE/'native-result.json')
print(json.dumps({'crops':list(parts),'nativeResult':json.loads((NATIVE/'result.json').read_text())['passed']},ensure_ascii=False))
