"""Extract approved raster controls; runtime text and item slots remain live."""
from pathlib import Path
from PIL import Image, ImageDraw
import json,hashlib,shutil
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'source-assets/inscription-dark-gold-v2/reference.png'
OUT=ROOT/'source-assets/inscription-dark-gold-v2/components'
OUT.mkdir(parents=True,exist_ok=True)
im=Image.open(SOURCE).convert('RGBA')
regions={
'header':(10,8,1526,119),'window':(10,8,1526,1008),
'left_panel':(28,126,371,999),'equipment_panel':(382,126,898,749),
'preview_panel':(907,126,1508,746),'inventory_panel':(381,756,1508,1000),
'row':(41,365,357,461),'row_selected':(41,260,357,357),
'search':(45,199,355,245),'confirm':(972,596,1439,684),
'slot':(984,770,1049,822),'equipment_slot':(575,224,703,349),
'book_slot':(486,486,556,556),'close':(1434,31,1496,92),
'progress':(928,281,1485,303),'pity':(1280,519,1300,540),
'background':(1330,790,1460,910),'search_icon':(61,209,91,240),
'guard':(53,270,130,347),'tough':(53,375,130,452),
'light':(53,478,130,553),'control':(53,580,130,656),
'charge':(53,681,130,757),'sigil':(462,164,805,403)}
for name,box in regions.items():im.crop(box).save(OUT/(name+'.png'))
manifest={'source':'reference.png','sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'source_size':im.size,'regions':regions,'logical_size':[512,368],'inventory':{'columns':9,'rows':3,'hotbar_rows':1,'step':18},'note':'Nine-slice centers exclude baked text; runtime draws labels/items. Sigil raw region retained for reference only.'}
(SOURCE.parent/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
board=Image.new('RGB',(1000,((len(regions)+4)//5)*150),(35,30,22));d=ImageDraw.Draw(board)
for i,(name,box) in enumerate(regions.items()):
 tile=im.crop(box);tile.thumbnail((186,118));x=(i%5)*200;y=(i//5)*150;board.paste(tile,(x,y+20),tile);d.text((x+4,y+3),name,fill='white')
board.save(SOURCE.parent/'contact-sheet.png')
print('Extracted',len(regions),'approved UI elements')

# The approved screenshot hides parts of the sigil behind equipment. The saved
# imagegen restoration supplies clean panel art; runtime labels remain separate.
clean=Image.open(SOURCE.parent/'clean-background-generated.png').convert('RGBA')
clean_regions={'left_clean':(28,126,371,999),'equipment_clean':(382,126,898,749),'preview_clean':(907,126,1508,746),'inventory_clean':(381,756,1508,1004)}
fills={'confirm_fill':(1007,614,1070,662),'row_fill':(265,384,335,439),'row_selected_fill':(265,278,335,330)}
for name,box in clean_regions.items():clean.crop(box).save(OUT/(name+'.png'))
for name,box in fills.items():im.crop(box).save(OUT/(name+'.png'))
manifest['clean_regions']=clean_regions;manifest['fill_regions']=fills
manifest['restoration']='clean-background-generated.png (built-in image_gen; remove baked text, items and controls, restore unobscured sigil)'
manifest['controls']={'choice':['normal','selected','hover','disabled','keyboard-focus'],'confirm':['normal','hover','disabled','keyboard-focus'],'search':['empty-hint','input','caret','selection'],'close':['authored-icon'],'inventory':'3x9 plus 1x9 hotbar; source grid is not runtime geometry'}
manifest['runtime_note']='Panel art from restored background; control corners/edges from approved source. Labels, quantities, item sprites, button state overlays and search input remain live.'
(SOURCE.parent/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
RUNTIME=ROOT/'src/main/resources/assets/wuxingxiantu/textures/gui/inscription_gold'
RUNTIME.mkdir(parents=True,exist_ok=True)
for file in OUT.glob('*.png'):
    shutil.copy2(file,RUNTIME/file.name)
    (RUNTIME/(file.name+'.mcmeta')).write_text(json.dumps({'texture':{'blur':True,'clamp':True}})+'\n',encoding='utf-8')
print('Prepared',len(list(OUT.glob('*.png'))),'skin elements and runtime metadata')

# User correction: background only. Existing geometry, icons and controls stay identical.
flat_path=SOURCE.parent/'flat-background-generated.png'
if flat_path.exists():
 flat=Image.open(flat_path).convert('RGBA')
 for name,box in {**clean_regions,'header':(10,8,1526,119),'background':(1330,790,1460,910)}.items():flat.crop(box).save(OUT/(name+'.png'))
 im.crop((640,24,900,110)).save(OUT/'header_title.png')
 manifest['background_only']={'file':flat_path.name,'title_crop':[640,24,900,110],'geometry_changed':False,'controls_changed':False}
 (SOURCE.parent/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
 for file in OUT.glob('*.png'):
  shutil.copy2(file,RUNTIME/file.name);(RUNTIME/(file.name+'.mcmeta')).write_text(json.dumps({'texture':{'blur':True,'clamp':True}})+'\n',encoding='utf-8')

# Final contact sheet reflects background-only overrides, not the superseded scenic crop.
component_root=ROOT/"source-assets/inscription-dark-gold-v2"/'components'
files=sorted(component_root.glob('*.png'));board=Image.new('RGB',(1000,((len(files)+4)//5)*145),(27,23,17));draw=ImageDraw.Draw(board)
for i,file in enumerate(files):
 tile=Image.open(file).convert('RGBA');tile.thumbnail((185,115));x=i%5*200;y=i//5*145;draw.text((x+3,y+2),file.stem,fill='white');board.paste(tile,(x,y+22),tile)
board.save(component_root.parent/'contact-sheet.png')

manifest['resource_files']={file.name:hashlib.sha256(file.read_bytes()).hexdigest() for file in sorted(component_root.glob('*.png'))}
(component_root.parent/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
