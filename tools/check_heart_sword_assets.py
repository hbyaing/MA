"""Asset-only checks; does not launch Gradle or Minecraft."""
import base64
import json
import math
import hashlib
from pathlib import Path
import unittest
from PIL import Image, ImageChops

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/wuxingxiantu'

def canonical_bytes(path: Path) -> bytes:
    """Hash text geometry independent of Git's platform line-ending checkout."""
    data = path.read_bytes()
    if path.suffix.lower() in {'.obj', '.mtl'}:
        return data.replace(b'\r\n', b'\n')
    return data

class HeartSwordAssets(unittest.TestCase):
    def test_inventory_icon_size_and_alpha(self):
        after=Image.open(ASSETS/'textures/item/heart_sword_inventory.png').convert('RGBA')
        self.assertEqual(after.size,(64,64))
        self.assertEqual(after.getchannel('A').getextrema(),(0,255))
        self.assertIsNotNone(after.getbbox())

    def test_blockbench_texture_matches_runtime(self):
        source=json.loads((ROOT/'source-assets/heart_sword.bbmodel').read_text(encoding='utf-8-sig'))
        report=json.loads((ROOT/'source-assets/contracts/heart_sword_v2_import.json').read_text(encoding='utf-8'))
        self.assertEqual(len(source['textures']),len(report['materials']))
        for material in report['materials']:
            self.assertEqual(base64.b64decode(source['textures'][material['index']]['source'].split(',',1)[1]),
                             (ROOT/material['path']).read_bytes())

    def test_grip_anchor_and_larger_held_transforms(self):
        model=json.loads((ASSETS/'models/item/heart_sword.json').read_text(encoding='utf-8'))
        for hand in ('righthand','lefthand'):
          for view,scale in (('firstperson',round(1.4/1.875,9)),('thirdperson',round(1.4*1.3/1.875,9))):
            first=model['display'][view+'_'+hand]
            self.assertEqual(first['scale'],[scale]*3)
            # Apply the authored model transform to the actual OBJ grip, not the sword origin.
            v=[(g-.5)*s for g,s in zip((0,-.2,0),first['scale'])]
            for axis in (2,1,0):
                angle=first['rotation'][axis]*(-1 if hand=='lefthand' and axis>0 else 1)
                a=math.radians(angle);i,j=((1,2),(2,0),(0,1))[axis]
                v[i],v[j]=v[i]*math.cos(a)-v[j]*math.sin(a),v[i]*math.sin(a)+v[j]*math.cos(a)
            translation=first['translation'].copy()
            if hand=='lefthand':translation[0]*=-1
            for position,translation in zip(v,translation):
                self.assertAlmostEqual(position+translation/16,0,places=6)
    def test_model_geometry_matches_import(self):
        contract=json.loads((ROOT/'source-assets/contracts/heart_sword_v2_import.json').read_text(encoding='utf-8'))
        for path,digest in contract['output_sha256'].items():
            self.assertEqual(hashlib.sha256(canonical_bytes(ROOT/path)).hexdigest(),digest)

if __name__=='__main__':
    unittest.main(verbosity=2)
