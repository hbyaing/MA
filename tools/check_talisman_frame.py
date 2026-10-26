"""Reject opaque outer margins on the shared talisman window textures."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
for name in ['background', 'engrave_background', 'frame']:
    path = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/gui/talisman_v12' / (name + '.png')
    image = Image.open(path).convert('RGBA')
    assert image.size == (1408, 1117), (name, image.size)
    alpha = image.getchannel('A')
    for box in [(0, 0, 1408, 17), (0, 1096, 1408, 1117),
                (0, 0, 14, 1117), (1394, 0, 1408, 1117)]:
        assert alpha.crop(box).getextrema() == (0, 0), (name, 'opaque exterior', box)
    for point in [(15, 18), (20, 20), (1392, 18), (1387, 20),
                  (15, 1094), (20, 1090), (1392, 1094), (1387, 1090)]:
        assert alpha.getpixel(point) == 0, (name, 'opaque bevel exterior', point)
    assert alpha.getpixel((700, 500)) == 255, (name, 'missing panel')
    assert alpha.getextrema() == (0, 255), (name, 'missing transparency')
print('Talisman backgrounds: exterior margins and beveled corners are transparent; panels retained')
