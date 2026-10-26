"""Extract all formal elements of the approved sense/furnace UI references."""
from pathlib import Path
import hashlib
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'source-assets/sense-heavenly-ui-20261027-v1'
OUTPUT = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/gui/sense_heavenly_v1'
OUTPUT.mkdir(parents=True, exist_ok=True)
reference = {name: Image.open(SOURCE / (name + '-reference.png')).convert('RGBA')
             for name in ['sense', 'heavenly']}
manifest = {'approved': True, 'generator': 'built-in image_gen', 'elements': {}}


def save(name, image, source, box):
    path = OUTPUT / (name + '.png')
    image.save(path)
    manifest['elements'][name] = {'source': source, 'crop': box,
        'size': image.size, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}


def frame(name, source, box, inset):
    image = reference[source].crop(box)
    w, h = image.size
    # Retain the authored border; remove every baked label/icon from its center.
    ImageDraw.Draw(image).rectangle((inset, inset, w-inset-1, h-inset-1), fill=(3,17,24,255))
    mask = Image.new('L', image.size)
    ImageDraw.Draw(mask).polygon([(5,0),(w-6,0),(w-1,5),(w-1,h-6),
                                 (w-6,h-1),(5,h-1),(0,h-6),(0,5)], fill=255)
    image.putalpha(mask)
    save(name, image, source, box)


frame('frame', 'heavenly', (18,20,1395,1094), 15)
frame('panel', 'sense', (50,277,747,539), 14)
frame('button', 'heavenly', (558,979,1145,1052), 12)
frame('selected', 'heavenly', (50,264,515,373), 12)
frame('row', 'heavenly', (50,383,515,492), 12)
frame('slot', 'heavenly', (573,592,671,690), 12)
for name, box in [('scan',(90,326,226,466)),('identify',(805,326,945,466)),
                  ('lock',(90,610,226,753)),('survey',(805,610,945,753))]:
    image = reference['sense'].crop(box).convert('RGBA')
    # Extract the cyan glyph, excluding frame gold and opaque background.
    image.putalpha(Image.eval(image.getchannel('A'), lambda a: 0))
    pixels = []
    for r,g,b,a in image.getdata():
        alpha = int(255*min(1,max(0,(max(g,b)-r-12)/48))) if b>r*1.05 and g>45 else 0
        pixels.append((r,g,b,alpha))
    image.putdata(pixels)
    save(name, image.resize((64,64),Image.Resampling.LANCZOS), 'sense', box)
for name, box in [('previous',(94,1001,127,1038)),('next',(431,1001,466,1038))]:
    image = reference['heavenly'].crop(box)
    image.putalpha(Image.eval(image.convert('L'),lambda a: 255 if a>115 else 0))
    save(name, image.resize((32,32),Image.Resampling.LANCZOS),'heavenly',box)
(SOURCE/'component-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
