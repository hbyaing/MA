"""Package the background-extracted image_gen HUD sprites without changing layout."""
from pathlib import Path
import json,hashlib
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'source-assets/hud-transparent-20261027'
OUT=ROOT/'src/main/resources/assets/wuxingxiantu/textures/gui/hud'
records={}
for name in ['armor','hunger']:
    image=Image.open(SOURCE/(name+'-cutout.png')).convert('RGBA')
    box=image.getchannel('A').point(lambda a:255 if a>16 else 0).getbbox()
    glyph=image.crop(box);glyph.thumbnail((15,15),Image.Resampling.NEAREST)
    sprite=Image.new('RGBA',(15,15));sprite.paste(glyph,((15-glyph.width)//2,(15-glyph.height)//2))
    target=OUT/('icon_'+name+'.png');sprite.save(target)
    records[name]={'source':name+'-cutout.png','crop':box,'runtimeSize':[15,15],'alphaRange':sprite.getchannel('A').getextrema(),'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}
(SOURCE/'manifest.json').write_text(json.dumps({'mode':'built-in image_gen background-extraction','icons':records},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
