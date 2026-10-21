"""Export editable native Photon NBT using a checked-in native particle template.

This is offline schema reuse, not proof that the current game rendered the files.
"""
from __future__ import annotations
import copy
import gzip
import io
import json
import sys
import uuid
import math
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'build/vfx-tool-deps'))
import nbtlib as nbt
from nbtlib import Compound as C, List as L, String as S, Float as F, Int as I, Byte as B, IntArray


def constant(value): return C({'_type':S('Constant'),'number':F(value)})


def ident(name):
    raw=uuid.uuid5(uuid.NAMESPACE_URL,'wuxingxiantu-vfx-v1/'+name).bytes
    return IntArray([int.from_bytes(raw[i:i+4],'big',signed=True) for i in range(0,16,4)])


def curve(template, points, lower=0,upper=1):
    out=copy.deepcopy(template)
    out['lower']=F(lower);out['upper']=F(upper);out['min']=F(min(0,lower));out['max']=F(max(1,upper))
    out['curves']=L[L[F]]([L[F]([F(x0),F(y0),F(x0+(x1-x0)/3),F(y0+(y1-y0)/3),F(x0+2*(x1-x0)/3),F(y0+2*(y1-y0)/3),F(x1),F(y1)]) for (x0,y0),(x1,y1) in zip(points,points[1:])])
    return out


def export(output,materials,recipes,palettes):
    template=nbt.parse_nbt((Path(__file__).parent/'photon_particle_template.snbt').read_text('utf-8'))
    by_id={m['id']:m for m in materials}
    fxdir=output/'resourcepack/assets/wuxingxiantu_vfx/fx';fxdir.mkdir(parents=True,exist_ok=True)
    projectdir=output/'photon/projects';projectdir.mkdir(parents=True,exist_ok=True)
    material_library=C({m['id']:C({'_type':S('TextureMaterial'),'texture':S(m['sheetTexture'] or m['texture']),'discardThreshold':F(.001)}) for m in materials})
    exports=[]

    def emitter(name, layer, color, loop=False):
        obj=copy.deepcopy(template['fx']['mainFX']['fxObjects'][0]);cfg=obj['config']
        obj['name']=S(layer['material']); obj['transform']['id']=ident(name)
        # Root parent IDs in the native export point to a transient preview root.
        # Retain that format while giving every independent export a stable ID.
        obj['transform']['_parentId']=ident(name.split('/')[0]+'/root')
        for key in ('localPosition','localScale'):
            obj['transform'][key]=C({axis:F(1 if key=='localScale' else 0) for axis in 'xyz'})
        obj['transform']['localRotation']=C({axis:F(1 if axis=='w' else 0) for axis in 'wxyz'})
        mat=by_id[layer['material']]
        cfg['material']['material']=copy.deepcopy(material_library[mat['id']])
        cfg['material']['depthMask']=B(0);cfg['material']['depthTest']=B(1);cfg['material']['cull']=B(0)
        cfg['material']['blendMode']['srcColorFactor']=S('SRC_ALPHA')
        cfg['material']['blendMode']['dstColorFactor']=S('ONE' if mat['blend']=='add' else 'ONE_MINUS_SRC_ALPHA')
        cfg['looping']=B(loop);cfg['duration']=I(60 if loop else 2)
        cfg['simulationSpace']=S('World')
        cfg['maxParticles']=I(min(96,max(8,layer['count']*2)))
        cfg['startDelay']=constant(layer['delayTicks'])
        cfg['startLifetime']=constant(layer['lifetimeTicks'])
        cfg['emission']['emissionRate']=constant(.20 if loop else layer['count']/2)
        cfg['emission']['bursts']=L[C]([])
        cfg['startColor']=C({'_type':S('Color'),'number':I((0xFF000000|int(color[1:],16))-2**32)})
        for module in ('trails','subEmitters','physics','noise','sizeBySpeed','rotationBySpeed','colorBySpeed','velocityOverLifetime','forceOverLifetime','inheritVelocity'):
            if module in cfg: cfg[module]['enable']=B(0)
        cfg['startSize']=C({axis:constant(layer['sizeBlocks']) for axis in 'xyz'})
        cfg['shape']['shape']['radius']=F(.01 if layer['motion'] in ('center','ground','ring','column') else .40)
        cfg['shape']['shape']['angle']=F(8 if layer['motion']=='rise' else 30)
        cfg['startSpeed']=constant(.022 if layer['motion']=='rise' else 0 if layer['motion'] in ('center','ground','ring','column','suspend') else .045)
        cfg['renderer']['renderMode']=S('Billboard');cfg['renderer']['bloomEffect']=B(0)
        cfg['colorOverLifetime']['enable']=B(1)
        cfg['colorOverLifetime']['color']['a']=L[F]([F(v) for v in [0,0,.08,1,.68,.8,1,0]])
        for channel in 'rgb': cfg['colorOverLifetime']['color'][channel]=L[F]([F(0),F(1),F(1),F(1)])
        uv=cfg['uvAnimation'];uv['enable']=B(mat['frames']>1)
        uv['tiles']=C({'a':I(mat['columns']),'b':I(mat['rows'])})
        uv['frameOverTime']=curve(uv['frameOverTime'],[(0,0),(1,1)],0,max(.999,mat['frames']-.001))
        cfg['sizeOverLifetime']['enable']=B(1)
        for axis in 'xyz': cfg['sizeOverLifetime']['size'][axis]=curve(uv['frameOverTime'],[(0,.25),(.18,.78),(.8,1),(1,.5)],0,1.2)
        if 'nativePath' in layer:
            path=layer['nativePath'];life=layer['lifetimeTicks'];samples=len(path)
            def values_curve(values,label):
                low=min(values);high=max(values)
                if high-low<1e-7:return constant(low)
                result=curve(uv['frameOverTime'],[(j/(len(values)-1),(v-low)/(high-low)) for j,v in enumerate(values)],low,high)
                result['yAxis']=S(label);return result
            def velocities(axis,sign):
                positions=[pt[axis]*sign for pt in path]
                # Photon VelocityOverLifetimeSetting multiplies linear by 0.05:
                # export blocks/second, not blocks/tick (upstream 1.20.1 source).
                return [20*(positions[min(j+1,samples-1)]-positions[max(0,j-1)])/(life*(min(j+1,samples-1)-max(0,j-1))/(samples-1)) for j in range(samples)]
            obj['transform']['localPosition']=C({'x':F(path[0][0]),'y':F(-path[0][1]),'z':F(0)})
            cfg['shape']['shape']['radius']=F(0);cfg['startSpeed']=constant(0)
            cfg['velocityOverLifetime']['enable']=B(1)
            cfg['velocityOverLifetime']['linear']=C({'x':values_curve(velocities(0,1),'velocity'),'y':values_curve(velocities(1,-1),'velocity'),'z':constant(0)})
            cfg['startRotation']['z']=constant(-math.degrees(path[0][2]))
            cfg['rotationOverLifetime']['enable']=B(1)
            cfg['rotationOverLifetime']['roll']=values_curve([-math.degrees(pt[2]-path[0][2]) for pt in path],'rotation')
            cfg['sizeOverLifetime']['size']['x']=values_curve([pt[3]*pt[5] for pt in path],'size')
            cfg['sizeOverLifetime']['size']['y']=values_curve([pt[4]*pt[5] for pt in path],'size')
            cfg['sizeOverLifetime']['size']['z']=constant(1)
        return obj

    def save(id,objects,description):
        root=C({'fx':C({'mainFX':C({'fxObjects':L[C](objects)}),'subFXs':C({})}),'_version':I(1)})
        path=fxdir/(id+'.fx');path.parent.mkdir(parents=True,exist_ok=True)
        nbt.File(root,gzipped=True).save(path)
        proj=C(dict(root));proj['resources']=C({'curves':C({}),'material':copy.deepcopy(material_library),'gradients':C({}),'ldlib.gui.editor.group.colors':C({}),'mesh':C({})})
        ppath=projectdir/(id.replace('/','__')+'.fxproj')
        nbt.File(proj,gzipped=True).save(ppath)
        for saved in (path,ppath):
            decoded=nbt.load(saved);buffer=io.BytesIO();decoded.write(buffer)
            assert buffer.getvalue()==gzip.decompress(saved.read_bytes()), 'NBT changed on roundtrip'
        exports.append(dict(id='wuxingxiantu_vfx:'+id,runtime=path.relative_to(output).as_posix(),project=ppath.relative_to(output).as_posix(),emitters=len(objects),description=description,verification='NBT roundtrip + schema/reference checks only; not rendered in Minecraft'))

    for mat in materials:
        layer=dict(material=mat['id'],motion='center',count=4,sizeBlocks=1.2,delayTicks=0,lifetimeTicks=30)
        save('materials/'+mat['id'],[emitter('material-'+mat['id'],layer,'#FFFFFF',True)],'单材质循环样片；用于编辑器检查透明度、帧序和染色。')
    for recipe in recipes:
        palette=palettes[recipe['defaultPalette']]
        if recipe['layers'] and 'paths' in recipe['layers'][0]:
            layers=recipe['layers'];total=sum(l['count'] for l in layers)
            allocations=[max(1,min(l['count'],int(l['count']*32/total))) for l in layers]
            while sum(allocations)>32:
                i=max(range(len(allocations)),key=lambda i:allocations[i]);allocations[i]-=1
            objs=[]
            def mix_color(left,right,amount):
                a=[int(left[i:i+2],16) for i in (1,3,5)];b=[int(right[i:i+2],16) for i in (1,3,5)]
                return '#'+''.join(f'{round(x*(1-amount)+y*amount):02X}' for x,y in zip(a,b))
            for i,(ly,n) in enumerate(zip(layers,allocations)):
                for j in range(n):
                    index=min(ly['count']-1,round((j+.5)/n*ly['count']-.5))
                    part=dict(ly,count=1,nativePath=ly['paths'][index],delayTicks=ly['delayTicks']+index*ly.get('staggerTicks',0))
                    role=ly.get('colorRole','body')
                    color=(palettes[['metal','wood','water','fire','earth'][index%5]][1] if role=='elements' else
                           palettes[role][1] if role in ('metal','wood','water','fire','earth') else
                           mix_color(palette[1],palette[2],.85) if role=='glow' else
                           mix_color(palette[1],'#FFFFFF',.72) if role=='core' else
                           mix_color(palette[1],palette[3],.34) if role=='edge' else palette[1])
                    objs.append(emitter(recipe['id']+f'/{i}-{j}',part,color))
            description='V2 独立配方；统一路径采样导出位置、速度、旋转、大小和错峰，最多32个发射器。引擎积分近似，未实机验收。'
        else:
            objs=[emitter(recipe['id']+'/'+str(i),ly,palette[1]) for i,ly in enumerate(recipe['layers'])]
            description='组合材质样片；位置/发射/淡出可编辑。完整轨迹与服务端技能时序见配方，不是技能实装。'
        save('recipes/'+recipe['id'],objs,description)
    (output/'photon/exports.json').write_text(json.dumps(exports,ensure_ascii=False,indent=2),'utf-8')
    return exports
