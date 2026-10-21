"""Preserved fire palettes, reference assets and retired weapon-appearance contracts."""
import json
import math
import re
from pathlib import Path
import unittest
import hashlib
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/wuxingxiantu'
JAVA=ROOT/'src/main/java/cn/blockforge/generated/generatedmod'
ARCHIVE=ROOT/'source-assets/retired-weapon-flame-20260928'
OLD_ASSETS=ARCHIVE/'src/main/resources/assets/wuxingxiantu'
OLD_JAVA=ARCHIVE/'src/main/java/cn/blockforge/generated/generatedmod'
SWORDS={'dark_iron_sword','mingjue_sword','qingming_sword','jixiao_sword','wuxiang_sword','guiyuan_sword','tianque_sword'}
FIRES={'red_flow','molten_gold','cold_ash','azure_wood','cobalt_sea','violet_thunder','sun_saint','nether_void','nirvana'}

def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
PROFILES={p.stem:load(p) for p in (ASSETS/'weapon_flames').glob('*.json')}

class WeaponFlames(unittest.TestCase):
    def test_archived_sprites_match_authored_assets_and_uv_regions(self):
        source=ROOT/'source-assets/weapon-flame-modules-v1'
        manifest=load(source/'manifest.json')
        definitions=(OLD_JAVA/'client/weaponflame/WeaponFlameModules.java').read_text()
        for sprite in manifest['sprites']:
            path=OLD_ASSETS/'textures/effect/modular_v1'/sprite['file']
            self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(),sprite['sha256'])
            self.assertEqual(load(Path(str(path)+'.mcmeta'))['texture'],{'blur':False,'clamp':True})
            with Image.open(path) as image:
                self.assertEqual(image.mode,'RGBA')
                self.assertEqual(list(image.size),sprite['size'])
                region=sprite['region_px_exclusive_max']
                alpha=image.getchannel('A').crop(region)
                self.assertEqual(alpha.getextrema(),(0,255))
                # The renderer's shader discards these low-alpha border pixels.
                strong=alpha.point(lambda a:255 if a>8 else 0).getbbox()
                self.assertTrue(strong[0]>0 and strong[1]>0 and strong[2]<alpha.width and strong[3]<alpha.height)
                values=','.join(str(v) for v in sprite['size']+region)
                self.assertIn('"'+path.stem+'",'+values,definitions)

    def test_exact_weapon_and_fire_coverage(self):
        self.assertEqual({n for n,d in PROFILES.items() if d['type']=='weapon'},SWORDS)
        self.assertEqual({n for n,d in PROFILES.items() if d['type']=='fire'},FIRES)

    def test_profiles_bounded_and_not_rgb_only(self):
        for name in FIRES:
            p=PROFILES[name]
            for v in p['colors'].values():self.assertTrue(0<=int(v.removeprefix('#'),16)<=0xffffff)
            self.assertTrue(.8<=p['shape']['coilTurns']<=1.5)
            self.assertTrue(.05<=p['shape']['flameWidth']<=.5)
            if p.get('schemaVersion',1)==2:
                self.assertTrue(.65<=p['shape']['coilRadius']<=.9)
                self.assertTrue(.2<=p['shape']['flameWidth']<=.35)
                self.assertLessEqual(p['shape']['coilRadius']*1.1,1.0)
            self.assertTrue(2<=p['combat']['hitPeelAmount']<=4)
            self.assertTrue(2<=p['combat']['trailFadeTime']<=5)
            self.assertLessEqual(p['combat']['trailLength'],.3)
        samples=[PROFILES[n] for n in ('red_flow','cobalt_sea','violet_thunder')]
        for key in ('motion','shape','combat'):
            self.assertEqual(len({json.dumps(p[key],sort_keys=True) for p in samples}),3)

    def test_anchors_match_actual_model_bounds(self):
        lengths=[]
        for name in SWORDS:
            a=PROFILES[name];suffix='_flying' if (ASSETS/'models/item'/f'{name}_flying.json').exists() else ''
            model=load(ASSETS/'models/item'/f'{name}{suffix}.json');points=[]
            if 'elements' in model:
                for e in model['elements']:
                    points.extend([[v/16 for v in e[k]] for k in ('from','to')])
            else:
                # Resolve Forge OBJ models and item-parent chains from the asset root.
                model_ref = model.get('model')
                if model_ref:
                    obj_path = ASSETS / model_ref.split(':', 1)[-1]
                    if obj_path.suffix != '.obj': obj_path = obj_path.with_suffix('.obj')
                    for line in obj_path.read_text(encoding='utf-8').splitlines():
                        if line.startswith('v '): points.append(list(map(float, line.split()[1:4])))
                elif model.get('parent'):
                    points = []
            delta=[t-h for t,h in zip(a['tipAnchor'],a['hiltAnchor'])]
            lengths.append(math.sqrt(sum(x*x for x in delta)))
            self.assertAlmostEqual(sum(x*y for x,y in zip(delta,a['widthAxis'])),0)
            self.assertAlmostEqual(sum(x*x for x in a['widthAxis']),1)
            if points:
                for anchor in ('hiltAnchor','tipAnchor'):
                    for i,value in enumerate(a[anchor]):
                        self.assertLessEqual(min(p[i] for p in points)-.01,value)
                        self.assertGreaterEqual(max(p[i] for p in points)+.01,value)
            self.assertTrue(0<a['bladeThickness']<=a['bladeWidth']<lengths[-1])
        self.assertGreater(max(lengths)/min(lengths),2,"Fixtures no longer cover differently sized swords")

    def test_pixel_masks_are_transparent_and_distinct(self):
        flame=Image.open(OLD_ASSETS/'textures/effect/weapon_flame.png')
        band=Image.open(OLD_ASSETS/'textures/effect/weapon_flame_band.png')
        self.assertEqual(flame.size,(64,32));self.assertEqual(band.size,(32,8))
        for im in (flame,band):
            self.assertEqual(im.mode,'RGBA');self.assertEqual(im.getchannel('A').getextrema(),(0,255))
        self.assertEqual(len({flame.crop((i*16,0,(i+1)*16,32)).tobytes() for i in range(4)}),4)

    def test_retired_appearance_keeps_heart_sword_and_protocol_bridge(self):
        renderer=(JAVA/'client/weaponflame/WeaponFlameRenderer.java').read_text()
        for entry in ('renderHeld(', 'renderPosed(', 'afterItem(', 'HeartSwordEffects.afterItem', 'finally {restore(previous);'):
            self.assertIn(entry,renderer)
        self.assertEqual(renderer.count('setWeaponOwner(priorOwner);'),2)
        self.assertIn('firecoil.Client.weapon(',renderer)
        for obsolete in ('HISTORY', 'PixelFlameMesh', 'WeaponFlameHelix', 'getBuffer(', 'WeaponFlameState'):
            self.assertNotIn(obsolete,renderer)
        hit=(JAVA/'client/weaponflame/WeaponFlameHitEffects.java').read_text()
        self.assertIn('accept(WeaponFlameHitPacket packet)',hit)
        for obsolete in ('SubscribeEvent', 'HITS', 'PixelFlameMesh', 'getBuffer('):
            self.assertNotIn(obsolete,hit)
        for name in ('PixelFlameMesh','WeaponFlameHelix','WeaponFlameModules','WeaponFlameRenderTypes'):
            self.assertFalse((JAVA/f'client/weaponflame/{name}.java').exists())
        self.assertFalse((ASSETS/'textures/effect/modular_v1').exists())
        self.assertFalse(list((ASSETS/'textures/effect').glob('weapon_flame*')))
        material=(JAVA/'client/ExoticBurnRenderType.java').read_text()
        self.assertIn('TextureAtlas.LOCATION_BLOCKS',material)
        self.assertNotIn('weaponflame',material)

    def test_reference_materials_have_padding_and_keep_source_colors(self):
        atlas=Image.open(OLD_ASSETS/'textures/effect/weapon_flame_reference.png')
        ribbon=Image.open(OLD_ASSETS/'textures/effect/weapon_flame_ribbon.png')
        self.assertEqual(atlas.mode,'RGBA');self.assertEqual(atlas.size,(512,512))
        for i in range(4):
            tile=atlas.crop((i%2*256,i//2*160,i%2*256+256,i//2*160+160))
            bounds=tile.getchannel('A').getbbox()
            self.assertIsNotNone(bounds)
            self.assertTrue(bounds[0]>0 and bounds[1]>0 and bounds[2]<256 and bounds[3]<160,
                            'Fragment touches UV edge and will be visibly clipped')
            self.assertEqual(tile.getchannel('A').getextrema(),(0,255))
        self.assertEqual(ribbon.mode,'RGBA')
        self.assertEqual(ribbon.getchannel('A').getextrema(),(0,255))
        self.assertFalse(load(OLD_ASSETS/'textures/effect/weapon_flame_ribbon.png.mcmeta')['texture']['clamp'])
        source=Image.open(ROOT/'reference_assets/weapon-flames-0.2/reference-original.png').convert('RGB')
        source_colors=set(source.getdata())
        self.assertTrue(all(pixel[:3] in source_colors for pixel in atlas.getdata() if pixel[3]>0))

if __name__=='__main__':unittest.main(verbosity=2)







