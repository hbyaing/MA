"""Extract generated arrow elements onto the existing clean formal button skin.

The arrow silhouettes are imagegen output, not runtime lines or font symbols.
Only the generated gold pixels are retained; source background/alpha noise is
discarded. Both icons reuse the same original border and opaque ink interior.
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'source-assets/talisman-canvas-icons-20261027-v2'
ART = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/gui/talisman_v12'
SIZE = 49


def extract(name):
    source_dir = ROOT / 'source-assets/talisman-collapse-proportion-20261027-v3' if name == 'collapse' else SOURCE
    source = Image.open(source_dir / (name + '-generated.png')).convert('RGB')
    width, height = source.size
    mask = Image.new('L', source.size)
    mask.putdata([255 if (width * .12 < i % width < width * .88
                         and height * .12 < i // width < height * .88
                         and r > 140 and g > 95 and r > b * 1.25) else 0
                  for i, (r, g, b) in enumerate(source.getdata())])
    mask = mask.resize((SIZE, SIZE), Image.Resampling.LANCZOS)
    for hover in [False, True]:
        skin = Image.open(SOURCE / ('expand_hover-original.png' if hover else 'expand-original.png')).convert('RGBA')
        # Keep the existing frame verbatim. Its center becomes solid ink;
        # the generated arrow is composited only inside the frame.
        skin.paste((3, 17, 24, 255), (5, 5, 44, 44))
        gold = Image.new('RGBA', skin.size, (239, 211, 138, 255) if hover else (182, 149, 77, 255))
        skin.paste(gold, (0, 0), mask)
        skin.save(ART / (name + '_v2' + ('_hover' if hover else '') + '.png'))


for icon in ['expand', 'collapse']:
    extract(icon)
