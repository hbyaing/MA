"""Check source drift, model/material assets and native range scenes before delivery."""
from __future__ import annotations
import hashlib,json,sys
from pathlib import Path
import numpy as np
from PIL import Image
from photon import nbt
from range_shapes_v3 import contains

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'source-assets/skill-particle-library-v3'


def check(out=OUT):
    read=lambda name:json.loads((out/name).read_text('utf-8'))
    skills=read('catalog/skills.json');models=read('catalog/models.json');materials=read('catalog/materials.json');geometry=read('catalog/geometry.json');exports=read('photon/exports.json');mapping=read('photon/decal-mapping.json')
    assert len(skills)==77 and len(models)==10 and len(materials)==50
    assert sum(len(s['scenarios']) for s in skills.values())==173
    hashes=geometry['sourceFiles'];source_checked=0
    for item in hashes:
        if isinstance(item,str):continue
        assert hashlib.sha256((ROOT/item['path']).read_bytes()).hexdigest()==item['sha256'],'Source drift '+item['path'];source_checked+=1
    faces=0
    for p in models.values():
        assert (out/p['texture']).exists()
        for item in p['source']:
            assert hashlib.sha256((ROOT/item['path']).read_bytes()).hexdigest()==item['sha256']
        for face in p['mesh']['faces']:
            assert np.isfinite(face['p']).all() and np.isfinite(face['uv']).all()
        faces+=len(p['mesh']['faces'])
    for m in materials:
        im=Image.open(out/m['file']).convert('RGBA');a=np.array(im.getchannel('A'));assert im.size==(128,128)
        assert a.max()==255 and not a[:3].any() and not a[-3:].any() and not a[:,:3].any() and not a[:,-3:].any()
        small=im.convert('RGBa').resize((32,32),Image.Resampling.LANCZOS).convert('RGBA');assert (np.array(small.getchannel('A'))>=192).sum()>=18
    record={(r['skill'],r['scenario']):r for r in exports if 'skill' in r};texture_refs=0;hazards=0;max_emitters=0;checked_native=0;mask_checks=0;mask_mismatches=0;mask_worst=0
    for skill in skills.values():
        for scene in skill['scenarios']:
            r=record[skill['id'],scene['id']];assert r['hazards']==sum(e['kind']=='hazard' and bool(e.get('footprint')) for e in scene['events'])
            assert len(r['boundaries'])==r['hazards'];hazards+=r['hazards']
            eventmap={e['eventId']:e for e in scene['events']}
            for boundary in r['boundaries']:
                raster=mapping[boundary['decal']];event=eventmap[boundary['eventId']];bb=event['bounds'];ex,ez=raster['extent'];cx,_,cz=raster['center']
                # World rectangles retain independent X/Z extents and translated origins.
                assert abs(ex-(bb[2]-bb[0]+.36))<1e-5 and abs(ez-(bb[3]-bb[1]+.36))<1e-5
                assert abs(cx-(bb[0]+bb[2])/2)<1e-5 and abs(cz-(bb[1]+bb[3])/2)<1e-5
                for stage in ('warning','active'):
                    tex=raster['textures'][stage].split(':',1)[1];im=Image.open(out/'resourcepack/assets/wuxingxiantu_vfx'/tex);w,h=im.size;alpha=np.array(im.getchannel('A'))
                    assert alpha.max()>100 and not alpha[0].any() and not alpha[-1].any() and not alpha[:,0].any() and not alpha[:,-1].any(), tex
                    # Sample transparent safe holes/gaps and retained opaque core points.
                    for yy in range(0,h,max(1,h//24)):
                        for xx in range(0,w,max(1,w//24)):
                            x=cx+ex/2-xx/(w-1)*ex;z=cz+ez/2-yy/(h-1)*ez
                            if alpha[yy,xx]>16 and not contains(event,x,z):mask_mismatches+=1
                            mask_checks+=1
    for r in exports:
        runtime=nbt.load(out/r['runtime']);objects=runtime['fx']['mainFX']['fxObjects'];assert len(objects)==r['emitters'];max_emitters=max(max_emitters,len(objects))
        for obj in objects:
            cfg=obj['config'];assert int(cfg['maxParticles'])==1 and not int(cfg['looping'])
            assert cfg['renderer']['renderMode'] in ('Horizontal','Billboard')
            assert float(cfg['emission']['emissionRate']['number'])==0
            burst=cfg['emission']['bursts'][0];assert int(burst['cycles'])==1 and float(burst['count']['number'])==1
            assert not int(cfg['renderer']['bloomEffect'])
            texture=str(cfg['material']['material']['texture']);namespace,path=texture.split(':',1)
            assert (out/'resourcepack/assets'/namespace/path).is_file();texture_refs+=1
        project=nbt.load(out/r['project']);assert len(project['fx']['mainFX']['fxObjects'])==len(objects);checked_native+=2
    assert mask_mismatches==0, f'Baked art leaks into true safety region at {mask_mismatches} sampled texels'
    result={'passed':True,'skills':77,'bossModels':10,'scenarios':173,'events':sum(len(sc['events']) for s in skills.values() for sc in s['scenarios']),
            'modelFaces':faces,'materials':50,'nativeScenes':len(exports),'nativeFilesChecked':checked_native,'textureReferencesChecked':texture_refs,'preservedHazards':hazards,'maxSceneEmitters':max_emitters,
            'sourceHashFilesChecked':source_checked,'rasterPointChecks':mask_checks,'rasterOutsideFootprintSamples':mask_mismatches,
            'rasterPrecisionNotice':'Baked images have finite texel resolution; exact geometry resides in recipe contours and source predicate.','minecraftRendered':False,'productionIntegrated':False}
    report=ROOT/'reports/skill-particle-library-v3/file-validation.json';report.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n','utf-8');print(json.dumps(result,ensure_ascii=False));return result

if __name__=='__main__':check()
