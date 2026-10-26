"""Extract the approved jing_zhu_tai artwork. Never use the unrelated recipe table."""
from pathlib import Path
from PIL import Image,ImageDraw
import shutil,json,hashlib
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'source-assets/reforge-dark-gold-concept-20261026/reforge-refine-reference.png'
OUT=ROOT/'source-assets/reforge-dark-gold-v1';OUT.mkdir(parents=True,exist_ok=True)
COMP=OUT/'components';COMP.mkdir(exist_ok=True)
im=Image.open(SOURCE).convert('RGBA');shutil.copy2(SOURCE,OUT/'reference.png')
regions={'header':(10,8,1526,119),'window':(10,8,1526,1008),'tab_selected':(40,134,204,195),'tab':(210,134,365,195),'row_selected':(42,286,358,391),'row':(42,405,358,511),'row_protect':(42,528,358,635),'confirm':(991,607,1438,683),'slot':(890,739,963,791),'equipment_slot':(576,224,703,348),'stable':(53,300,145,383),'risk':(53,418,145,501),'protect':(53,541,145,625),'ready':(957,566,1000,610),'fire':(1370,514,1407,564),'alert':(51,669,92,712),'pity':(1248,513,1271,539),'confirm_fill':(1020,625,1065,662),'row_fill':(265,425,335,453),'row_selected_fill':(265,304,335,340)}
for name,box in regions.items():im.crop(box).save(COMP/(name+'.png'))
clean_path=OUT/'clean-background-generated.png'
if clean_path.exists():
 clean=Image.open(clean_path).convert('RGBA')
 for name,box in {'left_clean':(28,126,371,999),'equipment_clean':(382,126,898,716),'preview_clean':(907,126,1508,716),'inventory_clean':(381,726,1508,1004)}.items():
  clean.crop(box).save(COMP/(name+'.png'));regions[name]=box
manifest={'source':'reference.png','sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'regions':regions,'logical_size':[512,368],'inventory':{'columns':9,'rows':3,'hotbar':9,'origin':[234,281],'step':18,'hotbar_gap':4},'scope':'jing_zhu_tai / REFORGE_TABLE only','note':'Text and items stay live. 4 temper choices use official item sprites; no temporary drawn symbols.'}
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
board=Image.new('RGB',(1000,((len(regions)+4)//5)*145),(27,23,17));d=ImageDraw.Draw(board)
for i,n in enumerate(regions):
 tile=Image.open(COMP/(n+'.png')).convert('RGBA');tile.thumbnail((185,115));x=i%5*200;y=i//5*145;d.text((x+3,y+2),n,fill='white');board.paste(tile,(x,y+22),tile)
board.save(OUT/'contact-sheet.png')
RUNTIME=ROOT/'src/main/resources/assets/wuxingxiantu/textures/gui/reforge_gold';RUNTIME.mkdir(parents=True,exist_ok=True)
for file in COMP.glob('*.png'):
 shutil.copy2(file,RUNTIME/file.name);(RUNTIME/(file.name+'.mcmeta')).write_text(json.dumps({'texture':{'blur':True,'clamp':True}})+'\n',encoding='utf-8')
print('Extracted',len(regions),'reforge elements')

flat_path=OUT/'flat-background-generated.png'
if flat_path.exists():
 flat=Image.open(flat_path).convert('RGBA')
 for name,box in {'header':(10,8,1526,119),'left_clean':(28,126,371,999),'equipment_clean':(382,126,898,716),'preview_clean':(907,126,1508,716),'inventory_clean':(381,726,1508,1004)}.items():flat.crop(box).save(COMP/(name+'.png'))
 im.crop((640,24,900,110)).save(COMP/'header_title.png')
 manifest['background_only']={'file':flat_path.name,'title_crop':[640,24,900,110],'geometry_changed':False,'controls_changed':False}
 (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
 for file in COMP.glob('*.png'):
  shutil.copy2(file,RUNTIME/file.name);(RUNTIME/(file.name+'.mcmeta')).write_text(json.dumps({'texture':{'blur':True,'clamp':True}})+'\n',encoding='utf-8')

# Final contact sheet reflects background-only overrides, not the superseded scenic crop.
component_root=ROOT/"source-assets/reforge-dark-gold-v1"/'components'
files=sorted(component_root.glob('*.png'));board=Image.new('RGB',(1000,((len(files)+4)//5)*145),(27,23,17));draw=ImageDraw.Draw(board)
for i,file in enumerate(files):
 tile=Image.open(file).convert('RGBA');tile.thumbnail((185,115));x=i%5*200;y=i//5*145;draw.text((x+3,y+2),file.stem,fill='white');board.paste(tile,(x,y+22),tile)
board.save(component_root.parent/'contact-sheet.png')

manifest['resource_files']={file.name:hashlib.sha256(file.read_bytes()).hexdigest() for file in sorted(component_root.glob('*.png'))}
(component_root.parent/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
