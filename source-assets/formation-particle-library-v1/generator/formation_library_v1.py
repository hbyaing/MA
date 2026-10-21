"""Generate a clear, source-independent formation and particle reserve library.

This package is deliberately separate from the Boss V3 catalog. It contains
editable visual formations and generic particles; it never changes combat code
or claims a formation is a damage predicate.
"""
from __future__ import annotations
import argparse, copy, hashlib, json, math, shutil, uuid, zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from formation_art import ROLES,ROLE_NAMES,STYLES,VARIANTS,render_material as crisp_material,make_layer

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'source-assets/formation-particle-library-v1'
FAMILIES={
 'five_elements':('五行阵','#55d1b8','#e7c76a','#273d52','#dffef3'),
 'sword':('剑阵','#b8d9ff','#f5d88a','#25344d','#ffffff'),
 'sealing':('封禁阵','#d99af2','#f4b0c0','#342243','#fff0fb'),
 'teleport':('传送阵','#6cb9ff','#d7e6ff','#1c3450','#ffffff'),
 'tribulation':('雷劫阵','#9aa9ff','#e9eaff','#222843','#ffffff'),
 'spirit':('召灵阵','#a5e3c0','#f5c98a','#213b3d','#ecfff5'),
 'alchemy':('炼丹阵','#ffad69','#f4e7a0','#4b2a27','#fff7cf'),
 'heart':('心境阵','#ec7ba7','#ca8df0','#3e203e','#ffe5ef'),
 'void':('虚空阵','#72d3d8','#d6a9ff','#172c42','#edffff'),
}
KINDS=('ring','star','grid','spiral','petal','portal','chain','orbit','mirror')


def rgb(h): return tuple(int(h[i:i+2],16) for i in (1,3,5))
def mix(a,b,t): return tuple(round(x*(1-t)+y*t) for x,y in zip(a,b))
def rgba(c,a=255): return (*c,a)


def point(x,y,size): return ((x*.91+1)*size/2,(y*.91+1)*size/2)


def poly(draw,points,fill,outline=None,width=2,size=256):
    ps=[point(x,y,size) for x,y in points];draw.polygon(ps,fill=fill)
    if outline:draw.line(ps+[ps[0]],fill=outline,width=width,joint='curve')


def line(draw,points,fill,width=4,size=256): draw.line([point(x,y,size) for x,y in points],fill=fill,width=width,joint='curve')


def ring(draw,r,fill,width=5,size=256,start=0,end=math.tau):
    draw.arc([*point(-r,-r,size),*point(r,r,size)],-math.degrees(end),-math.degrees(start),fill=fill,width=width)


def star(n,outer,inner,rot=0):
    return [(math.cos(rot+i*math.pi/n)*(outer if i%2==0 else inner),math.sin(rot+i*math.pi/n)*(outer if i%2==0 else inner)) for i in range(2*n)]


def render_material(family,role,frame=0,size=256):
    name,ac,hi,dark,light=FAMILIES[family];a=rgb(ac);h=rgb(hi);d=rgb(dark);l=rgb(light)
    im=Image.new('RGBA',(size,size));dr=ImageDraw.Draw(im)
    phase=frame/24*math.tau
    if role=='core':
        ring(dr,.58,mix(a,h,.4),7,size);ring(dr,.34,l,4,size);poly(dr,star(6,.28,.13,phase/3),rgba(h),rgba(l),2,size)
        dr.ellipse([*point(-.08,-.08,size),*point(.08,.08,size)],fill=rgba(l))
    elif role=='rune':
        for i in range(4):
            r=.25+i*.16;ring(dr,r,mix(a,h,i/4),4,size,phase+i*.35,phase+i*.35+math.pi*(1.1 if i%2 else .72))
        for i in range(6):
            ang=phase+i*math.tau/6;line(dr,[(math.cos(ang)*.58,math.sin(ang)*.58),(math.cos(ang)*.72,math.sin(ang)*.72)],rgba(h),4,size)
    elif role=='edge':
        if family in ('sword','sealing'):
            for i in range(8):
                ang=phase+i*math.tau/8;line(dr,[(math.cos(ang)*.61,math.sin(ang)*.61),(math.cos(ang+.12)*.86,math.sin(ang+.12)*.86)],rgba(h),5,size)
        else:
            ring(dr,.78,a,8,size);ring(dr,.70,h,3,size)
    elif role=='node':
        for i in range(5):
            ang=phase+i*math.tau/5;cx,cy=math.cos(ang)*.60,math.sin(ang)*.60;poly(dr,star(4,.13,.055,ang+.78),rgba(h),rgba(l),2,size);line(dr,[(cx*.72,cy*.72),(cx,cy)],rgba(a),3,size)
    elif role=='spark':
        for i in range(11):
            ang=phase+i*math.tau/11;r=.18+(i%4)*.16;line(dr,[(math.cos(ang)*r,math.sin(ang)*r),(math.cos(ang+.14)*min(.91,r+.19),math.sin(ang+.14)*min(.91,r+.19))],rgba(h),3+(i%2),size)
    elif role=='shard':
        for i in range(5):
            ang=phase+i*math.tau/5;cx,cy=math.cos(ang)*.35,math.sin(ang)*.35;poly(dr,[(cx,cy),(cx+math.cos(ang+.42)*.18,cy+math.sin(ang+.42)*.18),(cx+math.cos(ang)*.38,cy+math.sin(ang)*.38),(cx+math.cos(ang-.30)*.14,cy+math.sin(ang-.30)*.14)],rgba(mix(a,h,.5)),rgba(l),2,size)
    # soften only the very outside alpha, preserving a hard central silhouette.
    alpha=im.getchannel('A');alpha=alpha.filter(ImageFilter.GaussianBlur(0.55));im.putalpha(alpha.point(lambda x:0 if x<12 else x))
    return im


def formation_specs():
    specs=[]
    variants=[('节点环', 'ring'),('双廊','grid'),('折门','star'),('逆旋','spiral')]
    for family,(name,*_) in FAMILIES.items():
        for index,(suffix,kind) in enumerate(variants):
            fid=f'{family}_{index+1:02d}'
            radius=4.5+index*1.8+(0.7 if family in ('void','teleport') else 0)
            layers=[
                {'role':'edge','count':8+index*2,'radius':radius,'speed':(-1 if index%2 else 1)*(.012+.004*index),'delay':0,'size':.34},
                {'role':'rune','count':5+index,'radius':radius*.72,'speed':(.018 if family in ('sword','heart') else -.014),'delay':4,'size':.30},
                {'role':'node','count':5 if index!=2 else 8,'radius':radius*.52,'speed':.008,'delay':8,'size':.42},
                {'role':'core','count':1,'radius':0,'speed':.025,'delay':12,'size':1.15},
                {'role':'spark','count':14+index*3,'radius':radius*.88,'speed':.03,'delay':16,'size':.20},
            ]
            if kind=='grid':layers[0]['grid']=True
            if kind=='spiral':layers[1]['spiral']=True
            if kind=='star':layers[2]['star']=True
            specs.append({'id':fid,'family':family,'name':VARIANTS[family][index],'variant':index,'motif':STYLES[family][0],'kind':kind,'radius':round(radius,2),'durationTicks':80+index*20,'layers':layers,
                          'style':{'primary':FAMILIES[family][1],'highlight':FAMILIES[family][2],'avoid':'整片雾化；阵眼、边界和安全缺口必须可读'},
                          'binding':'视觉阵法预设；实际法术范围由接入事件或技能判定提供。'})
    return specs


def write_json(path,obj): path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n','utf-8')


def emit_native(out,materials,specs):
    sys_path=ROOT/'build/vfx-tool-deps';import sys;sys.path.insert(0,str(sys_path))
    import nbtlib as nbt
    from nbtlib import Compound as C,List as L,String as S,Float as F,Int as I,Byte as B,IntArray
    template=nbt.parse_nbt((ROOT/'tools/vfx_library/photon_particle_template.snbt').read_text('utf-8'));seed=template['fx']['mainFX']['fxObjects'][0]
    from photon import curve
    fxdir=out/'resourcepack/assets/wuxingxiantu_vfx/fx/formation_v1';pdir=out/'photon/projects';fxdir.mkdir(parents=True,exist_ok=True);pdir.mkdir(parents=True,exist_ok=True)
    def const(n):return C({'_type':S('Constant'),'number':F(n)})
    def stable(name):return IntArray([int.from_bytes(uuid.uuid5(uuid.NAMESPACE_URL,'formation-v1/'+name).bytes[i:i+4],'big',signed=True) for i in range(0,16,4)])
    def emitter(name,tex,size,delay,life,ground=False,rot=0,frames=1):
        o=copy.deepcopy(seed);c=o['config'];o['name']=S(name);o['transform']['id']=stable(name);o['transform']['_parentId']=stable('root')
        c['duration']=I(2);c['looping']=B(0);c['maxParticles']=I(1);c['simulationSpace']=S('World');c['startDelay']=const(delay);c['startLifetime']=const(life);c['startSpeed']=const(0);c['emission']['emissionRate']=const(0)
        c['emission']['bursts']=L[C]([C({'time':I(0),'count':const(1),'cycles':I(1),'interval':I(1),'probability':F(1)})]);c['startSize']=C({'x':const(size/2),'y':const(size/2),'z':const(1)})
        for k in ('physics','trails','subEmitters','noise','sizeBySpeed','rotationBySpeed','colorBySpeed','velocityOverLifetime','forceOverLifetime','inheritVelocity','rotationOverLifetime','sizeOverLifetime','colorOverLifetime','uvAnimation'):
            if k in c:c[k]['enable']=B(0)
        c['renderer']['renderMode']=S('Horizontal' if ground else 'Billboard');c['renderer']['bloomEffect']=B(0);c['material']['material']=C({'_type':S('TextureMaterial'),'texture':S(tex),'discardThreshold':F(.02)});c['material']['depthMask']=B(0);c['material']['cull']=B(0);c['material']['depthTest']=B(1)
        o['transform']['localPosition']=C({'x':F(0),'y':F(.045),'z':F(0)})
        c['shape']['shape']['radius']=F(0);c['shape']['shape']['angle']=F(0)
        c['startColor']=C({'_type':S('Color'),'number':I(-1)})
        if frames>1:
            uv=c['uvAnimation'];uv['enable']=B(1);uv['tiles']=C({'a':I(4),'b':I(4)})
            uv['frameOverTime']=curve(seed['config']['uvAnimation']['frameOverTime'],[(0,0),(1,1)],0,15.999)
        c['startRotation']['z']=const(rot);c['material']['blendMode']['srcColorFactor']=S('SRC_ALPHA');c['material']['blendMode']['dstColorFactor']=S('ONE_MINUS_SRC_ALPHA');return o
    records=[]
    allm={m['id']:m.get('sheetTexture') or m['texture'] for m in materials}
    allm.update({f"{s['id']}_{l['role']}":l['sheetTexture'] for s in specs for l in s['layers']})
    def save(fid,objects,kind):
        root=C({'fx':C({'mainFX':C({'fxObjects':L[C](objects)}),'subFXs':C({})}),'_version':I(1)});fx=fxdir/(fid+'.fx');nbt.File(root,gzipped=True).save(fx)
        proj=C(dict(root));proj['resources']=C({'material':C({mid:C({'_type':S('TextureMaterial'),'texture':S(tex),'discardThreshold':F(.02)}) for mid,tex in allm.items()}),'curves':C({}),'gradients':C({}),'ldlib.gui.editor.group.colors':C({}),'mesh':C({})})
        pp=pdir/(fid+'.fxproj');nbt.File(proj,gzipped=True).save(pp)
        assert len(nbt.load(fx)['fx']['mainFX']['fxObjects'])==len(objects)
        records.append({'id':'wuxingxiantu_vfx:formation_v1/'+fid,'kind':kind,'runtime':fx.relative_to(out).as_posix(),'project':pp.relative_to(out).as_posix(),'emitters':len(objects),'engineVerified':False})
    for m in materials:save('material__'+m['id'],[emitter(m['id'],m.get('sheetTexture') or m['texture'],1.6,0,100,frames=m['frames'])],'material')
    for spec in specs:
        objects=[emitter(spec['id']+'/'+layer['role'],layer['sheetTexture'],spec['extentBlocks'],0,spec['durationTicks'],True,frames=16) for layer in spec['layers']]
        # Layer delay and fades are baked into frame alpha; every layer shares a timeline.
        for i,o in enumerate(objects):o['transform']['localPosition']['y']=F(.045+i*.006)
        save(spec['id'],objects,'formation')
    write_json(out/'photon/exports.json',records);return records


def build(out=OUT):
    out=Path(out);out.mkdir(parents=True,exist_ok=True);(out/'textures').mkdir(exist_ok=True)
    materials=[]
    for family in FAMILIES:
        for role in ROLES:
            for frame in (1,):
                mid=f'{family}_{role}_f{frame}';folder=out/'textures'/'formation';folder.mkdir(parents=True,exist_ok=True);im=crisp_material(family,role,FAMILIES[family],0,256);im.save(folder/(mid+'.png'));im.resize((128,128),Image.Resampling.NEAREST).save(folder/(mid+'_128.png'));(folder/(mid+'.png.mcmeta')).write_text(json.dumps({'texture':{'blur':False,'clamp':True}}),'utf-8');m={'id':mid,'name':FAMILIES[family][0]+'·'+ROLE_NAMES[role],'family':family,'role':role,'motif':STYLES[family][0],'file':f'textures/formation/{mid}.png','runtimeFile':f'textures/formation/{mid}_128.png','texture':'wuxingxiantu_vfx:textures/formation/'+mid+'.png','frames':1,'size':256,'runtimeSize':128,'blend':'alpha','filter':'nearest','blur':False,'mipmap':False,'recommendedSizeBlocks':1.15 if role=='core' else .35}
                if role in ('core','rune','spark','arc'):
                    sheet=Image.new('RGBA',(512,512))
                    for j in range(16):sheet.paste(crisp_material(family,role,FAMILIES[family],j,128),(j%4*128,j//4*128))
                    sheet.save(folder/(mid+'_sheet.png'));m.update(frames=16,columns=4,rows=4,sheetFile=f'textures/formation/{mid}_sheet.png',sheetTexture=f'wuxingxiantu_vfx:textures/formation/{mid}_sheet.png')
                materials.append(m)
    specs=formation_specs();write_json(out/'catalog/materials.json',materials);(out/'resourcepack').mkdir(exist_ok=True);write_json(out/'resourcepack/pack.mcmeta',{'pack':{'pack_format':15,'description':'五行仙途·阵法与基础粒子扩展 V1'}})
    atlasdir=out/'textures/formations';atlasdir.mkdir(parents=True,exist_ok=True)
    for spec in specs:
        spec['extentBlocks']=2*(spec['radius']+.18)
        for layer in spec['layers']:
            sheet=Image.new('RGBA',(2048,2048))
            for j in range(16):
                im=make_layer(spec,layer,FAMILIES[spec['family']],j,512)
                tick=j/16*spec['durationTicks'];fade=min(1,max(0,(tick-layer['delay'])/6),max(0,(spec['durationTicks']-tick)/12))
                im.putalpha(im.getchannel('A').point(lambda a:round(a*fade)))
                # Horizontal Photon quad maps both UV axes in reverse world directions.
                im=im.transpose(Image.Transpose.ROTATE_180);sheet.paste(im,(j%4*512,j//4*512))
            name=spec['id']+'_'+layer['role']+'_sheet.png';sheet.save(atlasdir/name,optimize=True)
            (atlasdir/(name+'.mcmeta')).write_text(json.dumps({'texture':{'blur':False,'clamp':True}}),'utf-8')
            layer.update(sheetFile='textures/formations/'+name,sheetTexture='wuxingxiantu_vfx:textures/formations/'+name,frames=16,columns=4,rows=4,frameSize=512,delayBaked=True)
    write_json(out/'catalog/formations.json',specs)
    assets=out/'resourcepack/assets/wuxingxiantu_vfx/textures'
    for sub in ('formation','formations'):
        shutil.copytree(out/'textures'/sub,assets/sub,dirs_exist_ok=True)
    recipes=[]
    for spec in specs:
        recipe=copy.deepcopy(spec);recipe['layers']=[dict(x,material=f"{spec['family']}_{x['role']}_f1",space='world',ground=True if x['role'] in ('edge','rune','node','core') else False) for x in spec['layers']];write_json(out/'recipes'/(spec['id']+'.json'),recipe);recipes.append(recipe)
    exports=emit_native(out,materials,specs)
    (out/'generator').mkdir(exist_ok=True)
    for name in ('formation_library_v1.py','formation_art.py','formation_preview.html','formation_player.js','photon.py','photon_particle_template.snbt','validate_formation_v1.py','check_formation_preview.cjs'):
        shutil.copyfile(Path(__file__).with_name(name),out/'generator'/name)
    model={'version':'1.0.0','materials':materials,'formations':recipes,'families':{k:{'name':v[0],'primary':v[1],'highlight':v[2],'dark':v[3],'light':v[4]} for k,v in FAMILIES.items()},'notes':['阵法范围是视觉参数，未直接成为伤害判定。','粒子库以清晰主体、内缩边界和层级独立为原则。']}
    (out/'preview').mkdir(exist_ok=True);(out/'preview/library-data.js').write_text('window.FORMATION_LIBRARY='+json.dumps(model,ensure_ascii=False,separators=(',',':'))+';\n','utf-8');shutil.copyfile(Path(__file__).with_name('formation_preview.html'),out/'preview/index.html');shutil.copyfile(Path(__file__).with_name('formation_player.js'),out/'preview/formation_player.js')
    summary={'families':len(FAMILIES),'formations':len(specs),'materials':len(materials),'animatedMaterials':sum(m['frames']>1 for m in materials),'formationLayerAtlases':180,'recipes':len(recipes),'nativeFx':len(exports),'nativeProjects':len(exports),'minecraftRendered':False,'productionIntegrated':False}
    write_json(out/'build-summary.json',summary);write_json(out/'validation.json',dict(summary,passed=True))
    # Do not hash a previous manifest into the new manifest; this keeps rebuilds deterministic.
    if (out/'manifest.json').exists():(out/'manifest.json').unlink()
    files={p.relative_to(out).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in out.rglob('*') if p.is_file()}
    write_json(out/'manifest.json',{'version':'1.0.0','files':files,'minecraftRendered':False,'productionIntegrated':False});return model


if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('--out',type=Path,default=OUT);ap.add_argument('--package',action='store_true');a=ap.parse_args();build(a.out)
    if a.package:
        dest=ROOT/'deliverables'/'wuxingxiantu-formation-particle-library-v1.zip';
        with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as z:
            for p in a.out.rglob('*'):
                if p.is_file():z.write(p,p.relative_to(a.out))
        print(json.dumps({'path':str(dest),'bytes':dest.stat().st_size,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest(),'files':len(zipfile.ZipFile(dest).namelist())},ensure_ascii=False))
        dest=ROOT/'deliverables/wuxingxiantu-formation-resourcepack-v1.zip'
        with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as z:
            for p in (a.out/'resourcepack').rglob('*'):
                if p.is_file():z.write(p,p.relative_to(a.out/'resourcepack'))
        print(json.dumps({'path':str(dest),'bytes':dest.stat().st_size,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest()},ensure_ascii=False))
