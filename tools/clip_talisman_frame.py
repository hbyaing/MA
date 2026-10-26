"""Losslessly remove ink outside the authored gold frame, retaining all frame pixels."""
from pathlib import Path
import hashlib
import json
import shutil
from datetime import datetime
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'src/main/resources/assets/wuxingxiantu/textures/gui/talisman_v12'

def contour(image):
    width, height = image.size
    rows = {}
    for y in range(height):
        gold = []
        for x in range(width):
            if 50 <= y < height - 50 and 50 <= x < width - 50:
                continue
            r, g, b, a = image.getpixel((x, y))
            if a and r >= 15 and r > g > b:
                gold.append(x)
        if gold:
            rows[y] = (min(gold), max(gold))
    assert rows and len(rows) == max(rows) - min(rows) + 1, 'Broken outer frame'
    return rows

def main():
    backup = Path('E:/垃圾') / datetime.now().strftime('%Y-%m-%d/talisman-frame-alpha-%H%M%S')
    backup.mkdir(parents=True, exist_ok=False)
    records = []
    for name in ['background', 'engrave_background', 'frame']:
        path = ART / (name + '.png')
        original = path.read_bytes()
        saved = backup / path.name
        saved.write_bytes(original)
        assert saved.read_bytes() == original
        before = Image.open(path).convert('RGBA')
        assert before.size == (1408, 1117)
        rows = contour(before)
        after = before.copy()
        removed = 0
        for y in range(before.height):
            bounds = rows.get(y)
            for x in range(before.width):
                if bounds is None or x < bounds[0] or x > bounds[1]:
                    r, g, b, a = before.getpixel((x, y))
                    if a:
                        after.putpixel((x, y), (r, g, b, 0))
                        removed += 1
        # Every pixel within the traced frame keeps its exact original RGBA;
        # every exterior pixel must be transparent, including all four corners.
        for y in range(before.height):
            bounds = rows.get(y)
            for x in range(before.width):
                inside = bounds is not None and bounds[0] <= x <= bounds[1]
                assert after.getpixel((x, y)) == before.getpixel((x, y)) if inside else after.getpixel((x, y))[3] == 0
        after.save(path, compress_level=9)
        records.append({'asset': path.name, 'removedOpaquePixels': removed,
                        'oldSHA256': hashlib.sha256(original).hexdigest(),
                        'newSHA256': hashlib.sha256(path.read_bytes()).hexdigest()})
    report = {'backup': str(backup), 'assets': records, 'interiorRGBAUnchanged': True}
    (backup / 'manifest.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    (ROOT / 'reports/talisman-alpha-assets.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(records, indent=2))

if __name__ == '__main__':
    main()
