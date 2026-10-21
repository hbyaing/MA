"""Verify V3 source footprints, holes, range gaps and event timing independently.

The polygon check uses vectorized even-odd ray casting and distances to polygon
segments, independently of the analytic hit predicate. It checks every source
event; duplicate geometry may share one spatial fixture, never drop events.
"""
from __future__ import annotations
import argparse
import copy
import hashlib
import json
import math
from pathlib import Path
import numpy as np
from boss_geometry_v3 import build_geometry
from range_shapes_v3 import active_at, bounds, contains, footprint, prepare_scenario


def _polygon(points, contours):
    result=np.zeros(len(points),dtype=bool)
    nearest=np.full(len(points),np.inf)
    p=np.asarray(points,dtype=np.float64)
    for contour in contours:
        a=np.asarray(contour,dtype=np.float64)
        b=np.roll(a,-1,axis=0)
        delta=b-a
        crossing=(a[None,:,1]>p[:,None,1])!=(b[None,:,1]>p[:,None,1])
        numerator=(p[:,None,1]-a[None,:,1])*delta[None,:,0]
        xhit=np.divide(numerator,delta[None,:,1],out=np.zeros_like(numerator),where=np.abs(delta[None,:,1])>1e-15)+a[None,:,0]
        result ^= np.count_nonzero(crossing & (p[:,None,0]<xhit),axis=1)%2==1
        d=p[:,None,:]-a[None,:,:]
        length=np.sum(delta*delta,axis=1)
        t=np.divide(np.sum(d*delta[None,:,:],axis=2),length[None,:],out=np.zeros_like(d[:,:,0]),where=length[None,:]>1e-18)
        t=np.clip(t,0,1)
        distance=np.linalg.norm(d-t[:,:,None]*delta[None,:,:],axis=2)
        nearest=np.minimum(nearest,np.min(distance,axis=1))
    return result,nearest


def _spatial(event):
    contours=event['footprint'];bb=bounds(contours)
    if not contours:
        return {'points':0,'edgeDifferences':0,'maxDifferenceDistance':0}
    seed=int(hashlib.sha256(json.dumps([contours,event['collision']],sort_keys=True).encode()).hexdigest()[:16],16)
    rng=np.random.default_rng(seed)
    points=rng.uniform([bb[0]-.1,bb[1]-.1],[bb[2]+.1,bb[3]+.1],size=(160,2)).tolist()
    c=event['collision'];r=c.get('radius',0)
    chord=(r*(1-math.cos(math.pi/48)) if event['shape']=='line' else r*(1-math.cos(math.pi/96)))+3e-6
    for contour in contours:
        for a,b in zip(contour,contour[1:]+contour[:1]):
            d=[b[0]-a[0],b[1]-a[1]];length=math.hypot(*d)
            if length<1e-9:
                continue
            m=[(a[0]+b[0])/2,(a[1]+b[1])/2]
            for offset in (-.7*chord,.7*chord,2*chord):
                points.append([m[0]-d[1]/length*offset,m[1]+d[0]/length*offset])
    poly,nearest=_polygon(points,contours)
    actual=np.array([contains(event,x,z) for x,z in points],dtype=bool)
    different=poly!=actual
    max_error=float(np.max(nearest[different])) if np.any(different) else 0
    assert max_error <= chord+2e-6, (event['shape'],max_error,chord)
    # All curved polygons are inscribed. Any outward error beyond rounding is a bug.
    outside=poly & ~actual
    if np.any(outside):
        assert float(np.max(nearest[outside])) < 5e-6,(event['shape'],'polygon extends outside true collision')
    return {'points':len(points),'edgeDifferences':int(np.sum(different)),
            'maxDifferenceDistance':max_error,'allowedSagitta':chord}


def verify(root:Path) -> dict:
    geometry=build_geometry(root)
    original_event_count=sum(len(sc['events']) for s in geometry['skills'].values() for sc in s['scenarios'])
    skills=copy.deepcopy(geometry['skills'])
    spatial_cache={};all_events=[];prepared_count=0;scenario_count=0;ring_count=0
    for skill in skills.values():
        for scenario in skill['scenarios']:
            original=copy.deepcopy(scenario['events'])
            prepare_scenario(scenario);scenario_count+=1
            assert len(scenario['events'])==len(original)
            assert len(scenario['targetPath'])==scenario['durationTicks']+1
            assert all(t==i for i,(t,p) in enumerate(scenario['targetPath']))
            for before,event in zip(original,scenario['events']):
                assert all(event[k]==v for k,v in before.items()),'Source event modified during preparation'
                all_events.append(event);prepared_count+=1
                if event['kind']!='hazard':
                    continue
                if event['shape']=='gaze':
                    assert event['footprint']==[] and event['nominalFootprint']==[]
                    assert event['bounds'] is None
                    continue
                assert event['footprint'],'Damaging geometry missing'
                key=json.dumps([event['footprint'],event['collision'],event['shape'],event['origin'],event['target']],sort_keys=True)
                if key not in spatial_cache:
                    spatial_cache[key]=_spatial(event)
                if event['shape']=='ring' and event['collision']['innerRadius']>0:
                    ring_count+=1
                    assert len(event['footprint'])==2
                    assert not contains(event,event['origin'][0],event['origin'][2])
                    inner_result,_=_polygon([[event['origin'][0],event['origin'][2]]],event['footprint'])
                    assert not inner_result[0],'Ring hole is filled'
    assert prepared_count==original_event_count==1649

    checks=[]
    def record(name,condition):
        assert condition,name
        checks.append(name)
    def hazards(skill,index=0):
        return [e for e in skills[skill]['scenarios'][index]['events'] if e['kind']=='hazard']
    for skill,nominal in [('ermei_05',7),('yihuo_05',6)]:
        scenario=skills[skill]['scenarios'][0];e=hazards(skill);gap=scenario['inputs']['target'][2]
        clear=nominal-2.3;tick=min(v['startTick'] for v in e)
        hit=lambda z:any(contains(v,22,z,tick=tick) for v in e)
        record(skill+' gap center is safe',not hit(gap))
        record(skill+' nominal gap loses rounded endcaps',hit(gap+nominal/2-.5))
        record(skill+' exact safe width '+str(clear),not hit(gap+clear/2-.002) and hit(gap+clear/2+.002))
        record(skill+' lower safe boundary',not hit(gap-clear/2+.002) and hit(gap-clear/2-.002))
        record(skill+' no whole-wall replacement',len(e)==20)

    water=hazards('pengjiao_04');last=max(e['startTick'] for e in water)
    hit_angle=lambda degrees:any(contains(e,-6*math.sin(math.radians(degrees)),6*math.cos(math.radians(degrees)),tick=last) for e in water)
    record('gap ring preserves five chords per wave',len(water)==25)
    record('gap ring +Z center axis safe',not hit_angle(0))
    record('gap ring 24 degree point safe',not hit_angle(24))
    record('gap ring 30 degree point hits real round cap',hit_angle(30))
    record('gaze never obtains 30 block footprint',all(not e.get('footprint') for e in all_events if e['shape']=='gaze'))
    real=hazards('yihuo_01')
    record('yihuo fake lines excluded from damage footprints',len(real)==1 and real[0]['target']==[0,0,24])
    line=hazards('pengjiao_02')[0]
    record('source line width includes padding',line['width']==1.5 and line['collision']['width']==2)
    record('round cap extends beyond endpoint',contains(line,0,34.999) and not contains(line,0,35.001))
    record('hazard active end is inclusive',active_at(line,line['startTick']+line['durationTicks']) and not active_at(line,line['startTick']+line['durationTicks']+1))
    cancel=next(e for e in hazards('shetao_05') if e['shape']=='line')
    record('interrupt cancels before same tick hit',contains(cancel,0,5,tick=126) and not contains(cancel,0,5,tick=127))
    wave=next(e for e in hazards('ermei_02') if e['shape']=='ring')
    r=wave['collision']['radius']-.1
    record('jumpable wave obeys source height',contains(wave,r,0,player_y=.5) and not contains(wave,r,0,player_y=.7))
    record('gaze conditions cannot be replaced by XZ point containment',not contains(hazards('muyan_05')[0],0,0))
    shot=next(e for e in skills['ermei_03']['scenarios'][0]['events'] if e['kind']=='projectile')
    record('projectile endTick authoritative',active_at(shot,shot['endTick']) and not active_at(shot,shot['endTick']+1))
    cone=hazards('ermei_01')[0]
    record('cone root includes source subcentimeter disc',contains(cone,0,-.009) and _polygon([[0,-.009]],cone['footprint'])[0][0])
    record('cone does not extend behind beyond root tolerance',not contains(cone,0,-.02))
    points=sum(v['points'] for v in spatial_cache.values())
    return {'passed':True,'skills':len(skills),'scenarios':scenario_count,'originalEvents':original_event_count,'preparedEvents':prepared_count,
            'removedOrSampledEvents':0,'spatiallyCheckedUniqueGeometries':len(spatial_cache),'polygonPredicateSamples':points,
            'hollowRingEventsChecked':ring_count,'polygonDifferencesRestrictedTo':'conservative arc chords (outer inscribed, inner hole circumscribed) plus coordinate rounding (<= 5e-6 outward)',
            'maxObservedEdgeDifferenceBlocks':max(v['maxDifferenceDistance'] for v in spatial_cache.values()),
            'keyChecks':checks,'sourceDigest':geometry['extraction']['sourceDigest'],
            'limitations':['有限点集几何回归，不等于连续空间形式证明。','未加载Minecraft/Photon；真实深度、网络、碰撞与运行期动态接入待实测。']}


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2]);p.add_argument('--output',type=Path)
    args=p.parse_args();result=verify(args.root)
    out=args.output or args.root/'reports/skill-particle-library-v3/range-validation.json';out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'output':str(out),**{k:v for k,v in result.items() if k not in ('keyChecks','limitations')}},ensure_ascii=False))
