"""Resource integrity checks. Uses only Python's standard library."""
from pathlib import Path
import json,re,sys,unittest,struct,zlib
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
ASSETS=ROOT/'assets/wuxingxiantu'
# NONE deliberately has no status label; the two legacy labels are retained
# as empty compatibility keys. New empty translations must not pass silently.
INTENTIONALLY_EMPTY={'mindscape.wuxingxiantu.state.none',
                     'gui.wuxingxiantu.banner_order_title',
                     'tooltip.wuxingxiantu.sword_role'}
def load(path):
    def unique(pairs):
        result={}
        for key,value in pairs:
            if key in result: raise ValueError(f'{path}: duplicate key {key}')
            result[key]=value
        return result
    return json.loads(path.read_text(encoding='utf-8-sig'),object_pairs_hook=unique)
class Resources(unittest.TestCase):
    def test_furnace_surface_clearance_matches_model(self):
        import hashlib
        folder=ASSETS/'shaders/furnacevolume';meta=load(folder/'surface-clearance.json')
        self.assertEqual(meta['modelSha256'],hashlib.sha256((ASSETS/'models/block/cauldron_structure.obj').read_bytes()).hexdigest(),
                         'Furnace model changed; rerun tools/bake_furnace_surface_clearance.py')
        field=(folder/'surface-clearance.r16f').read_bytes()
        self.assertEqual(len(field),128*128*128*2)
        self.assertEqual(meta['fieldSha256'],hashlib.sha256(field).hexdigest())
        self.assertEqual(meta['nonConvexSkipped'],[])
    def test_cultivation_book_acquisition_coverage(self):
        java=ROOT.parents[2]/'src/main/java/cn/blockforge/generated/generatedmod'
        registry=(java/'WuXingItems.java').read_text(encoding='utf-8-sig')
        item_ids=dict(re.findall(r'RegistryObject<Item> (ART_\w+) = art\("([a-z_]+)"',registry))
        item_ids.update({field:'heart_method_'+item for field,item in
                         re.findall(r'RegistryObject<Item> (HEART_\w+) = heart\("([a-z_]+)"',registry)})
        art_ids=set(re.findall(r'new Entry\("([a-z_]+)"',(java/'ArtCatalog.java').read_text(encoding='utf-8-sig')))
        heart_src=(java/'HeartMethodCatalog.java').read_text(encoding='utf-8-sig')
        heart_ids={'heart_method_'+item for item in re.findall(r'"([a-z_]+)"',
                    heart_src.split('METHOD_IDS = List.of(',1)[1].split(');',1)[0])}
        existing={'art_sword_aura','heart_method_tiangang'}
        for item in ('art_sword_pierce','art_sword_flight','art_sword_sweep','art_sword_guard','art_sword_array',
                     'art_wanjian_guizong','art_jiuxiao_sword_rain'):
            recipe=load(ROOT/f'data/wuxingxiantu/recipes/{item}.json')
            self.assertEqual(recipe['result']['item'],'wuxingxiantu:'+item)
            existing.add(item)
        source=(java/'acquisition/CultivationBookSources.java').read_text(encoding='utf-8-sig')
        rows=re.findall(r'new Book\(WuXingItems\.(\w+), (\d+), (\d+), (\d+)\)',source)
        ids=[item_ids[field] for field,*_ in rows]
        self.assertEqual(len(ids),len(set(ids)),'Duplicate book acquisition rows')
        self.assertEqual(set(ids),(art_ids|heart_ids)-existing,'Missing or unnecessary book acquisition rows')
        self.assertEqual(len(ids),26,'Expected 17 arts and 9 heart methods')
        catalog=load(ASSETS/'item_text_catalog.json')
        for field,level,price,weight in rows:
            self.assertIn(item_ids[field],catalog)
            self.assertTrue(1<=int(level)<=5)
            self.assertTrue(1<=int(price)<=64,'Payment must fit one spirit-stone stack')
            self.assertGreater(int(weight),0,'Book cannot be fished')
        self.assertIn('tooltip.wuxingxiantu.book_source',load(ASSETS/'lang/zh_cn.json'))
        self.assertIn('tooltip.wuxingxiantu.book_source',load(ASSETS/'lang/en_us.json'))

    def test_item_text_catalogue(self):
        catalog=load(ASSETS/'item_text_catalog.json')
        zh=load(ASSETS/'lang/zh_cn.json')
        self.assertEqual(len(catalog),230,'Approved item text coverage changed (includes Funiao Fu fragment)')
        for item,entry in catalog.items():
            with self.subTest(item=item):
                self.assertTrue(any(f'{kind}.wuxingxiantu.{item}' in zh for kind in ('item','block')),
                                'Catalogue refers to an unknown item name')
                self.assertIn(entry['visibility'],('player','internal','creative'))
                self.assertTrue(0<=entry['nameColor']<=0xFFFFFF)
                self.assertTrue(0<=entry['gradeColor']<=0xFFFFFF)
                prefix=f'tooltip.wuxingxiantu.intro.{item}'
                for suffix in ['category','lore']+[f'action.{i}' for i in range(entry['actions'])]:
                    text=zh[prefix+'.'+suffix]
                    self.assertTrue(text.strip())
                    self.assertFalse(re.search(r'§|NBT|tick|源码|审计|未确认获取|配方文件',text,re.I),
                                     'Developer details leaked into player prose')
        self.assertIn('不能用于御剑飞行',zh['tooltip.wuxingxiantu.intro.peach_sword.action.0'])
        self.assertEqual(catalog['fire_seed_nirvana']['actions'],0,'Highest fire tier must use dynamic status')
        self.assertEqual(catalog['alchemy_complete']['visibility'],'creative','Complete alchemy book requires special permission')

    def test_no_legacy_dunxu_whitelist(self):
        self.assertFalse((ROOT/'data/wuxingxiantu/tags/blocks/dunxu_passable.json').exists(),
                         'Obsolete whitelist caused missing minecraft:concrete tag errors')
    def test_plant_stage_textures(self):
        models=list((ASSETS/'models/block').glob('*_low.json'))
        plants=[p for p in models if 'cross' in load(p).get('textures',{}) and (ASSETS/'models/block'/(p.stem[:-4]+'_mid.json')).exists()]
        self.assertEqual(len(plants),13,'Expected thirteen staged plants; blue mushroom has one form')
        for model in plants:
            name=model.stem[:-4]
            images=[]
            for suffix,stage in (('low','young'),('mid','growing')):
                with self.subTest(plant=name,stage=stage):
                    path=ASSETS/'textures/block'/f'{name}_{stage}.png'
                    self.assertEqual(load(ASSETS/'models/block'/f'{name}_{suffix}.json')['textures']['cross'],
                                     f'wuxingxiantu:block/{name}_{stage}')
                    data=path.read_bytes()
                    self.assertEqual(data[:8],b'\x89PNG\r\n\x1a\n')
                    size=32
                    self.assertEqual(struct.unpack('>IIBB',data[16:26]),(size,size,8,6),'Expected native RGBA stage texture')
                    offset=8;compressed=b''
                    while offset<len(data):
                        length=struct.unpack('>I',data[offset:offset+4])[0]
                        kind=data[offset+4:offset+8];chunk=data[offset+8:offset+8+length]
                        self.assertEqual(zlib.crc32(kind+chunk)&0xffffffff,struct.unpack('>I',data[offset+8+length:offset+12+length])[0])
                        if kind==b'IDAT':compressed+=chunk
                        offset+=length+12
                    raw=zlib.decompress(compressed);pixels=[];previous=[0]*(size*4);stride=size*4+1
                    self.assertEqual(len(raw),size*stride)
                    for row in range(size):
                        mode=raw[row*stride];decoded=[]
                        self.assertIn(mode,range(5))
                        for i,value in enumerate(raw[row*stride+1:(row+1)*stride]):
                            a=decoded[i-4] if i>=4 else 0;b=previous[i];c=previous[i-4] if i>=4 else 0
                            prediction=a+b-c
                            paeth=min((a,b,c),key=lambda v:abs(prediction-v))
                            decoded.append((value+(0,a,b,(a+b)//2,paeth)[mode])&255)
                        pixels.extend(decoded);previous=decoded
                    self.assertIn(0,pixels[3::4],'Stage must have transparent background')
                    self.assertGreaterEqual(max(pixels[3::4]),128,'Stage must contain visible cutout pixels')
                    images.append(bytes(pixels))
            self.assertNotEqual(*images,'Young and growing stages must differ')
    def test_spirit_textures_32(self):
        java=ROOT.parents[2]/'src/main/java/cn/blockforge/generated/generatedmod/plants/SpiritPlantCatalog.java'
        plants=set(re.findall(r'\.builder\("([a-z_]+)"',java.read_text(encoding='utf-8')))
        self.assertEqual(len(plants),14)
        checked=0
        for kind in ('block','item'):
            for path in (ASSETS/f'textures/{kind}').glob('*.png'):
                if any(path.stem==name or path.stem.startswith(name+'_') for name in plants):
                    self.assertEqual(struct.unpack('>II',path.read_bytes()[16:24]),(32,32),str(path))
                    checked+=1
        self.assertGreaterEqual(checked,54)
        for base in ('gold_marrow','wood_seed','water_pearl','fire_flame','earth_core'):
            for grade in ('low','mid','high','perfect'):
                name=f'{grade}_{base}'
                self.assertEqual(struct.unpack('>II',(ASSETS/f'textures/item/{name}.png').read_bytes()[16:24]),(32,32))
                self.assertEqual(load(ASSETS/f'models/item/{name}.json')['textures']['layer0'],f'wuxingxiantu:item/{name}')
    def test_particle_texture_references(self):
        for path in (ASSETS/'particles').glob('*.json'):
            for texture in load(path).get('textures',[]):
                if texture.startswith('wuxingxiantu:'):
                    self.assertTrue((ASSETS/'textures/particle'/(texture.split(':',1)[1]+'.png')).is_file(),f'{path}: {texture}')
    def test_locale_keys_and_values(self):
        zh=load(ASSETS/'lang/zh_cn.json');en=load(ASSETS/'lang/en_us.json')
        self.assertEqual(set(zh),set(en),f'zh only: {sorted(zh.keys()-en.keys())}; en only: {sorted(en.keys()-zh.keys())}')
        for language,entries in (('zh_cn',zh),('en_us',en)):
            for key,value in entries.items():
                with self.subTest(language=language,key=key):
                    self.assertIsInstance(value,str)
                    if key not in INTENTIONALLY_EMPTY:
                        self.assertTrue(value.strip(),'Empty translation')
    def test_blockstate_models_exist(self):
        for p in (ASSETS/'blockstates').glob('*.json'):
            data=load(p)
            variants=list(data.get('variants',{}).values())
            variants.extend(part['apply'] for part in data.get('multipart',[]))
            for variant in variants:
                for model in variant if isinstance(variant,list) else [variant]:
                    name=model['model']
                    if name.startswith('wuxingxiantu:'):
                        target=ASSETS/'models'/(name.split(':',1)[1]+'.json')
                        self.assertTrue(target.is_file(),f'{p}: {target}')
    def test_placed_features_exist(self):
        data_root=ROOT/'data/wuxingxiantu'
        for p in (data_root/'worldgen/placed_feature').glob('*.json'):
            feature=load(p)['feature']
            if isinstance(feature,str) and feature.startswith('wuxingxiantu:'):
                target=data_root/'worldgen/configured_feature'/(feature.split(':',1)[1]+'.json')
                self.assertTrue(target.is_file(),f'{p}: {target}')
    def test_json_unique_and_valid(self):
        for p in ROOT.rglob('*.json'):
            with self.subTest(path=p):load(p)
    def test_english_no_chinese_placeholders(self):
        for key,value in load(ASSETS/'lang/en_us.json').items():
            self.assertFalse(re.search('[\u4e00-\u9fff]',value),key)
    def test_locale_placeholder_signatures(self):
        zh=load(ASSETS/'lang/zh_cn.json');en=load(ASSETS/'lang/en_us.json')
        pattern=r'%(?:\d+\$)?[sdf]'
        for key in zh.keys()&en.keys():
            with self.subTest(key=key):
                self.assertEqual(sorted(re.findall(pattern,zh[key])),sorted(re.findall(pattern,en[key])))
    def test_local_models_and_textures_exist(self):
        for p in (ASSETS/'models').rglob('*.json'):
            d=load(p)
            for name in [d.get('parent',''),d.get('model','')]:
                if name.startswith('wuxingxiantu:'):
                    path=name.split(':',1)[1]
                    target=ASSETS/(path if path.startswith('models/') else 'models/'+path+'.json')
                    self.assertTrue(target.exists(),f'{p}: {target}')
            for name in d.get('textures',{}).values():
                if name.startswith('wuxingxiantu:'):
                    self.assertTrue((ASSETS/'textures'/(name.split(':',1)[1]+'.png')).exists(),f'{p}: {name}')
    def test_compendium_resources(self):
        for kind in ('introduction','nourishing','guarding','body','breakthrough','aptitude','complete'):
            self.assertTrue((ASSETS/f'models/item/alchemy_{kind}.json').exists())
            for lang in ('zh_cn','en_us'):
                self.assertIn('item.wuxingxiantu.alchemy_'+kind,load(ASSETS/f'lang/{lang}.json'))
if __name__=='__main__':unittest.main(verbosity=2)

