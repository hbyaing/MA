from pathlib import Path
import json, math, re
from collections import Counter
import numpy as np
ROOT=Path(__file__).resolve().parent.parent
live=json.loads((ROOT/'work/v6_live_geometry.json').read_text('utf8'))
base=json.loads((ROOT/'before_abdomen_v6_20261007.bbmodel').read_text('utf8'))
cur=json.loads((ROOT/'噬灵虫_腹部优化_v6.bbmodel').read_text('utf8'))
ism=lambda name: bool(re.search(r'腹部_青玉色薄膜|腹部_透明保护膜|^贴合腹甲_[1-7]$|^尾腹_贴体中央细缝$',name))
modified=[m for m in live['meshes'] if ism(m['name'])]
textures={t['uuid']:t for t in live['textures']}
issues=[];topology=[];global_preexisting=[]
for m in live['meshes']:
 V=m['vertices'];edges=Counter();tiny=[];missing=[]
 for fk,f in m['faces'].items():
  ks=f['vertices'];tex=textures.get(f['texture']);p=np.array([V[k] for k in ks])
  area=sum(np.linalg.norm(np.cross(p[i]-p[0],p[i+1]-p[0]))*.5 for i in range(1,len(p)-1))
  if area<1e-10:tiny.append(fk)
  if not tex:missing.append(fk)
  if not np.isfinite(p).all():issues.append([m['name'],fk,'nonfinite vertex'])
  for k in ks:
   uv=f['uv'].get(k)
   if uv is None or not np.isfinite(uv).all():issues.append([m['name'],fk,'invalid UV'])
   elif tex and not(-1e-5<=uv[0]<=tex['uv_width']+1e-5 and -1e-5<=uv[1]<=tex['uv_height']+1e-5):issues.append([m['name'],fk,'UV outside dimensions'])
  for i,a in enumerate(ks):edges[tuple(sorted([a,ks[(i+1)%len(ks)]]))]+=1
 if ism(m['name']):
  topology.append({'name':m['name'],'vertices':len(V),'faces':len(m['faces']),'degenerate_faces':len(tiny),'boundary_edges':sum(n==1 for n in edges.values()),'nonmanifold_edges':sum(n>2 for n in edges.values()),'missing_textures':len(missing)})
  if tiny or missing:issues.append([m['name'],'degenerate or missing texture',tiny,missing])
 else:
  if tiny:global_preexisting.append({'name':m['name'],'degenerate_faces':len(tiny)})

def canonical(e,doc):
 d=json.loads(json.dumps(e))
 for f in d.get('faces',{}).values():
  t=f.get('texture');f['texture']=doc['textures'][t]['uuid'] if isinstance(t,int) and not isinstance(t,bool) else t
 return d
be={e['uuid']:e for e in base['elements']};ce={e['uuid']:e for e in cur['elements']}
changed=[ce[k]['name'] for k in ce if canonical(ce[k],cur)!=canonical(be[k],base)]
unexpected=[n for n in changed if not ism(n)]
if unexpected:issues.append(['unexpected element changes',unexpected])
if base['outliner']!=cur['outliner']:issues.append(['outliner changed'])
if base.get('groups')!=cur.get('groups'):issues.append(['groups changed'])

tris=[]
for m in modified:
 if not re.search('腹部_青玉|腹部_透明保护',m['name']):continue
 for f in m['faces'].values():
  ks=f['vertices']
  for i in range(1,len(ks)-1):tris.append([m['vertices'][ks[j]] for j in [0,i,i+1]])
tris=np.array(tris)
a,b,c=tris[:,0],tris[:,1],tris[:,2]
v0=b[:,[0,2]]-a[:,[0,2]];v1=c[:,[0,2]]-a[:,[0,2]]
det=v0[:,0]*v1[:,1]-v0[:,1]*v1[:,0];valid=abs(det)>1e-12

def skin_y(x,z):
 v2=np.array([x,z])-a[:,[0,2]]
 with np.errstate(divide='ignore',invalid='ignore'):
  u=(v2[:,0]*v1[:,1]-v2[:,1]*v1[:,0])/det
  v=(v0[:,0]*v2[:,1]-v0[:,1]*v2[:,0])/det
 hit=valid&(u>=-1e-6)&(v>=-1e-6)&(u+v<=1+1e-6)
 ys=a[:,1]+u*(b[:,1]-a[:,1])+v*(c[:,1]-a[:,1])
 return float(min(ys[hit])) if hit.any() else None
clearances=[]
for m in modified:
 if not m['name'].startswith('贴合腹甲_'):continue
 ds=[];miss=0;inverted=0
 for f in m['faces'].values():
  ks=f['vertices']
  if any(k.endswith('b') for k in ks):continue
  if f['normal'][1]>=0:inverted+=1
  p=np.array([m['vertices'][k] for k in ks])
  for i in range(1,len(p)-1):
   for u in np.linspace(0,1,6):
    for v in np.linspace(0,1-u,6):
     q=p[0]*(1-u-v)+p[i]*u+p[i+1]*v
     if m['name']=='贴合腹甲_1' and q[2]<-.30:continue
     sy=skin_y(q[0],q[2])
     if sy is None:miss+=1
     else:ds.append(sy-q[1])
 clearances.append({'name':m['name'],'samples':len(ds),'min_clearance_from_outer_membrane':min(ds),'max_clearance_from_outer_membrane':max(ds),'penetrating_samples':int(sum(d<-.001 for d in ds)),'ray_misses':miss,'inverted_outer_faces':inverted})
 if miss or any(d<-.001 for d in ds) or inverted:issues.append([m['name'],'clearance/winding failure',clearances[-1]])
report={'scope':'Blockbench free-mesh source, not game-runtime verification','attachment_sampling_note':'First plate anterior portion z < -0.30 is deliberately embedded in the thorax; abdomen clearance sampling covers its exposed posterior portion.','mesh_count':len(live['meshes']),'modified_meshes':changed,'untouched_meshes':len(ce)-len(changed),'outliner_preserved':base['outliner']==cur['outliner'],'groups_preserved':base.get('groups')==cur.get('groups'),'modified_topology':topology,'ventral_surface_sampling':clearances,'validation_issues':issues,'unmodified_preexisting_degenerate_faces':global_preexisting,'no_disallowed_generated_carapace_texture':all(t['name']!='背甲_清晰甲壳_v2.png' for t in live['textures'])}
(ROOT/'work/abdomen_validation_v6.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({'modified':len(changed),'untouched':report['untouched_meshes'],'issues':issues,'topology':topology,'clearances':clearances,'preexisting_meshes_with_degenerates':len(global_preexisting)},ensure_ascii=False))
if issues:raise SystemExit(1)
