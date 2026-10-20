"""Native Photon 1.20.1 authoring scenes in real world units.

Schema comes from the existing native export. RendererSetting.Horizontal rotates
the XY quad by +90 degrees about X. TileParticle uses startSize as HALF extent.
Neither an NBT roundtrip nor this offline export is a Minecraft playback test.
"""
from __future__ import annotations
import copy,hashlib,io,gzip,json,math
from pathlib import Path
from PIL import Image,ImageChops,ImageDraw,ImageFilter
from photon import nbt,C,L,S,F,I,B,constant,ident,curve


def _rgb(value):
    return tuple(int(value[i:i+2],16) for i in (1,3,5))


def profile_color(profile,role='accent'):
    v=profile.get('palette',{}).get(role)
    if isinstance(v,dict):v=v.get('hex',v.get('color'))
    if isinstance(v,list):v=v[0] if v else None
    return v if isinstance(v,str) and v.startswith('#') else ('#E4D3BA' if role=='core' else '#B6A085')


def decal(out,event,profile,material,cache,impact_material=None):
    """Crisp, clipped source-scale footprint; alpha=0 in holes and safety gaps."""
    signature=json.dumps([event['footprint'],profile['id'],material['id']],sort_keys=True)
    digest=hashlib.sha256(signature.encode()).hexdigest()[:18]
    if digest in cache:return cache[digest]
    bb=event['bounds'];padding=.18
    xmin,zmin,xmax,zmax=bb[0]-padding,bb[1]-padding,bb[2]+padding,bb[3]+padding
    extent_x,extent_z=xmax-xmin,zmax-zmin
    resolution=min(96,1536/max(extent_x,extent_z))
    # A zero-direction source cone leaves only the <0.01-block root disc. Keep
    # enough texels for that real tiny footprint instead of enlarging its range.
    width=max(128,math.ceil(extent_x*resolution));height=max(128,math.ceil(extent_z*resolution))
    # Horizontal renderer maps u0 to +X and v0 to +Z. Both texture axes invert.
    def pixel(p):return ((xmax-p[0])/extent_x*(width-1),(zmax-p[1])/extent_z*(height-1))
    mask=Image.new('L',(width,height));md=ImageDraw.Draw(mask)
    for i,line in enumerate(event['footprint']):md.polygon([pixel(p) for p in line],fill=255 if i==0 else 0)
    # Pixel coverage extends past a sampled vertex. Keep the baked art one texel
    # inside the polygon so rounded caps, annular holes and safe gaps stay clear.
    # The exact mathematical boundary remains in the recipe's world-space mesh.
    mask=mask.filter(ImageFilter.MinFilter(3))
    border_pixels=max(2,round(resolution*.14))
    edge=ImageChops.subtract(mask,mask.filter(ImageFilter.MinFilter(2*border_pixels+1)))
    stamp=Image.open(out/material['file']).convert('RGBA')
    motif_layer=Image.new('RGBA',(width,height))
    unit_px=math.sqrt((width/extent_x)*(height/extent_z))
    stamp_size=max(12,round(unit_px*.75));stamp=stamp.resize((stamp_size,stamp_size),Image.Resampling.LANCZOS)
    for x,z,angle in event.get('edgeSamples',[]):
        px,pz=pixel([x,z]);motif_layer.alpha_composite(stamp,(round(px-stamp_size/2),round(pz-stamp_size/2)))
    motif_layer.putalpha(ImageChops.multiply(motif_layer.getchannel('A'),mask))
    impact_layer=Image.new('RGBA',(width,height))
    if impact_material:
        motif=Image.open(out/impact_material['file']).convert('RGBA')
        motif_size=max(16,round(unit_px*1.05));motif=motif.convert('RGBa').resize((motif_size,motif_size),Image.Resampling.LANCZOS).convert('RGBA')
        motif.putalpha(motif.getchannel('A').point(lambda v:round(v*.82)))
        count=min(24,max(3,math.ceil(extent_x*extent_z/16)))
        for index in range(count):
            x=xmin+((index*.6180339+.37)%1)*extent_x
            z=zmin+((index*.4142135+.61)%1)*extent_z
            px,pz=pixel([x,z]);impact_layer.alpha_composite(motif,(round(px-motif_size/2),round(pz-motif_size/2)))
        impact_layer.putalpha(ImageChops.multiply(impact_layer.getchannel('A'),mask))
    paths={}
    for stage in ('warning','active'):
        fill=Image.new('RGBA',(width,height),_rgb(profile_color(profile))+ (0,))
        fill.putalpha(mask.point(lambda v:round(v*(.07 if stage=='warning' else .23))))
        border=Image.new('RGBA',(width,height),_rgb(profile_color(profile,'core' if stage=='active' else 'accent'))+(255,))
        border.putalpha(edge.point(lambda v:round(v*.96)))
        fill.alpha_composite(border);fill.alpha_composite(motif_layer)
        if stage=='active':fill.alpha_composite(impact_layer)
        # A final exact mask prevents anti-aliased/stamped motifs leaking into holes.
        fill.putalpha(ImageChops.multiply(fill.getchannel('A'),mask))
        rel=Path('resourcepack/assets/wuxingxiantu_vfx/textures/ranges_v3')/(digest+'_'+stage+'.png')
        dest=out/rel;dest.parent.mkdir(parents=True,exist_ok=True);fill.save(dest,optimize=True)
        dest.with_suffix('.png.mcmeta').write_text(json.dumps({'texture':{'blur':False,'clamp':True}}),'utf-8')
        paths[stage]='wuxingxiantu_vfx:textures/ranges_v3/'+dest.name
    value=dict(id=digest,center=[(xmin+xmax)/2,.045,(zmin+zmax)/2],extent=[extent_x,extent_z],pixels=[width,height],textures=paths,
               pixelMapping='u=(maxX-worldX)/extentX; v=(maxZ-worldZ)/extentZ',mask='source collision footprint with conservative one-texel inset; exact mesh boundary in recipe; holes transparent',
               conservativeInsetTexels=1,texelSizeBlocks=[extent_x/(width-1),extent_z/(height-1)])
    cache[digest]=value
    return value


def export(out,skills,profiles,materials):
    template=nbt.parse_nbt((Path(__file__).parent/'photon_particle_template.snbt').read_text('utf-8'))
    seed=template['fx']['mainFX']['fxObjects'][0]
    library=C({m['id']:C({'_type':S('TextureMaterial'),'texture':S(m['texture']),'discardThreshold':F(.02)}) for m in materials})
    outdir=out/'resourcepack/assets/wuxingxiantu_vfx/fx/boss_v3';projectdir=out/'photon/projects'
    outdir.mkdir(parents=True,exist_ok=True);projectdir.mkdir(parents=True,exist_ok=True)
    exports=[];cache={};stats={'totalEmitters':0,'maxSceneEmitters':0,'preservedHazards':0,'groundDecals':0}

    def emitter(name,texture,position,size,delay,life,ground=False,linear_path=None):
        obj=copy.deepcopy(seed);cfg=obj['config'];obj['name']=S(name);obj['transform']['id']=ident('v3/'+name)
        obj['transform']['_parentId']=ident('v3/root');obj['transform']['localPosition']=C({k:F(v) for k,v in zip('xyz',position)})
        obj['transform']['localScale']=C({k:F(1) for k in 'xyz'});obj['transform']['localRotation']=C({'w':F(1),'x':F(0),'y':F(0),'z':F(0)})
        cfg['duration']=I(2);cfg['looping']=B(0);cfg['maxParticles']=I(1);cfg['simulationSpace']=S('World')
        cfg['startDelay']=constant(max(0,delay));cfg['startLifetime']=constant(max(1,life));cfg['startSpeed']=constant(0)
        cfg['startColor']=C({'_type':S('Color'),'number':I(-1)})
        cfg['emission']['emissionRate']=constant(0)
        cfg['emission']['bursts']=L[C]([C({'time':I(0),'count':constant(1),'cycles':I(1),'interval':I(1),'probability':F(1)})])
        # TileParticle vertex positions are +/-size: size values are half extents.
        cfg['startSize']=C({'x':constant(size[0]/2),'y':constant(size[1]/2),'z':constant(1)})
        for key in ('physics','trails','subEmitters','noise','sizeBySpeed','rotationBySpeed','colorBySpeed','velocityOverLifetime','forceOverLifetime','inheritVelocity','rotationOverLifetime','sizeOverLifetime','colorOverLifetime','uvAnimation'):
            if key in cfg:cfg[key]['enable']=B(0)
        cfg['shape']['shape']['radius']=F(0);cfg['shape']['shape']['angle']=F(0)
        cfg['renderer']['renderMode']=S('Horizontal' if ground else 'Billboard');cfg['renderer']['bloomEffect']=B(0)
        cfg['material']['material']=C({'_type':S('TextureMaterial'),'texture':S(texture),'discardThreshold':F(.02)})
        cfg['material']['depthMask']=B(0);cfg['material']['depthTest']=B(1);cfg['material']['cull']=B(0)
        cfg['material']['blendMode']['srcColorFactor']=S('SRC_ALPHA');cfg['material']['blendMode']['dstColorFactor']=S('ONE_MINUS_SRC_ALPHA')
        for axis in 'xyz':cfg['startRotation'][axis]=constant(0)
        if linear_path:
            # Sampled source trajectories use world XYZ; zero-length launch intervals are skipped.
            points=[]
            for tick,p in linear_path:
                if points and tick==points[-1][0]:points[-1]=[tick,p]
                else:points.append([tick,p])
            if len(points)>1:
                origin=points[0][1];obj['transform']['localPosition']=C({k:F(v) for k,v in zip('xyz',origin)})
                cfg['velocityOverLifetime']['enable']=B(1)
                frame_template=seed['config']['uvAnimation']['frameOverTime']
                for i,axis in enumerate('xyz'):
                    velocities=[]
                    for j in range(len(points)):
                        lo=max(0,j-1);hi=min(len(points)-1,j+1)
                        velocities.append(20*(points[hi][1][i]-points[lo][1][i])/max(1,points[hi][0]-points[lo][0]))
                    low,high=min(velocities),max(velocities)
                    cfg['velocityOverLifetime']['linear'][axis]=(constant(low) if high-low<1e-8 else curve(frame_template,[(j/(len(points)-1),(v-low)/(high-low)) for j,v in enumerate(velocities)],low,high))
        return obj

    def save(id,objects,description,extra=None):
        root=C({'fx':C({'mainFX':C({'fxObjects':L[C](objects)}),'subFXs':C({})}),'_version':I(1)})
        path=outdir/(id+'.fx');nbt.File(root,gzipped=True).save(path)
        project=C(dict(root));project['resources']=C({'material':copy.deepcopy(library),'curves':C({}),'gradients':C({}),'ldlib.gui.editor.group.colors':C({}),'mesh':C({})})
        dest=projectdir/(id+'.fxproj');nbt.File(project,gzipped=True).save(dest)
        for file in (path,dest):
            loaded=nbt.load(file);buffer=io.BytesIO();loaded.write(buffer)
            if buffer.getvalue()!=gzip.decompress(file.read_bytes()):raise AssertionError('NBT roundtrip '+str(file))
        record=dict(id='wuxingxiantu_vfx:boss_v3/'+id,runtime=path.relative_to(out).as_posix(),project=dest.relative_to(out).as_posix(),emitters=len(objects),description=description,engineVerified=False)
        if extra:record.update(extra)
        exports.append(record);stats['totalEmitters']+=len(objects);stats['maxSceneEmitters']=max(stats['maxSceneEmitters'],len(objects))

    for m in materials:
        save('material__'+m['id'],[emitter('material/'+m['id'],m['texture'],[0,1,0],[1,1],0,100)],'预着色材质检查；1×1格，无辉光，不循环。')
    for skill in skills.values():
        p=profiles[skill['boss']];mats={m['role']:m for m in materials if m['boss']==skill['boss']}
        for scene in skill['scenarios']:
            objects=[];ground_count=0;boundaries=[]
            for i,event in enumerate(scene['events']):
                prefix=skill['id']+'/'+scene['id']+f'/{i}'
                until=min(event.get('cancelAtTick',1e9),event.get('endTick',event['startTick']+event['durationTicks']-(1 if event['kind']=='projectile' and event.get('endInclusive') else 0))+(1 if event.get('endInclusive') else 0))
                if event['kind']=='hazard' and event.get('footprint'):
                    raster=decal(out,event,p,mats['edge'],cache,mats['cut' if event['shape']=='line' else 'impact']);begin=event.get('telegraphStartTick',event['startTick'])
                    warning_end=min(event['startTick'],event.get('cancelAtTick',1e9))
                    if warning_end>begin:
                        objects.append(emitter(prefix+'/warning',raster['textures']['warning'],raster['center'],raster['extent'],begin,warning_end-begin,True))
                    if until>event['startTick']:
                        objects.append(emitter(prefix+'/active',raster['textures']['active'],raster['center'],raster['extent'],event['startTick'],until-event['startTick'],True))
                    ground_count+=1;boundaries.append(dict(eventId=event['eventId'],decal=raster['id'],center=raster['center'],extent=raster['extent']))
                elif event['kind']=='projectile' and until>event['startTick']:
                    path=[[tick,[pos[0],pos[1]+.8,pos[2]]] for tick,pos in event.get('path',[])]
                    objects.append(emitter(prefix+'/projectile',mats['projectile']['texture'],[event['origin'][0],event['origin'][1]+.8,event['origin'][2]],[1.3,1.3],event['startTick'],until-event['startTick'],False,path))
                elif event['shape']=='gaze' and until>event['startTick']:
                    objects.append(emitter(prefix+'/gaze',mats['core']['texture'],[event['origin'][0],p['eyeHeight']+.7,event['origin'][2]],[2,2],event['startTick'],until-event['startTick']))
                elif event['kind']=='node' and until>event['startTick']:
                    objects.append(emitter(prefix+'/node',mats['core']['texture'],[event['origin'][0],1,event['origin'][2]],[1.4,1.4],event['startTick'],until-event['startTick']))
            # Visual charge anchor is distinct from server ground-space collision origin.
            anchor=p.get('castAnchor',[0,p['eyeHeight'],0]);anchor=anchor if isinstance(anchor,list) else anchor.get('position',[0,p['eyeHeight'],0])
            objects.append(emitter(skill['id']+'/'+scene['id']+'/charge',mats['core']['texture'],anchor,[1.2,1.2],0,skill['windupTicks']))
            stats['preservedHazards']+=ground_count
            id=skill['id']+'__'+scene['id']
            save(id,objects,'固定场景源码时序。完整危险边界不抽样；动态目标、遮挡、破招和原生积分须接入后验证。',dict(skill=skill['id'],scenario=scene['id'],hazards=ground_count,boundaries=boundaries,inputs=scene['inputs']))
    stats['groundDecals']=len(cache)
    (out/'photon/exports.json').write_text(json.dumps(exports,ensure_ascii=False,indent=2),'utf-8')
    (out/'photon/decal-mapping.json').write_text(json.dumps(cache,ensure_ascii=False,indent=2),'utf-8')
    return exports,stats
