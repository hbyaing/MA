"""V5: recalibrated side projection, source-pixel-only continuous abdomen atlas."""
from pathlib import Path
import json
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
ROOT=Path(__file__).resolve().parent.parent
W,H=1536,768
rear=np.asarray(Image.open(ROOT/'背面新参考.png').convert('RGB'),dtype=float)
side=np.asarray(Image.open(ROOT/'reference.png').convert('RGB'),dtype=float)
def sample(a,x,y):
 x,y=np.broadcast_arrays(np.clip(x,0,a.shape[1]-1.001),np.clip(y,0,a.shape[0]-1.001))
 i=x.astype(int);j=y.astype(int);fx=x-i;fy=y-j
 if a.ndim==3:fx=fx[...,None];fy=fy[...,None]
 return (a[j,i]*(1-fx)+a[j,i+1]*fx)*(1-fy)+(a[j+1,i]*(1-fx)+a[j+1,i+1]*fx)*fy
def mask(shape,polys,erode=7,blur=3):
 im=Image.new('L',(shape[1],shape[0]));d=ImageDraw.Draw(im)
 for p in polys:d.polygon(p,fill=255)
 return np.asarray(im.filter(ImageFilter.MinFilter(erode)).filter(ImageFilter.GaussianBlur(blur)),float)/255
left=[(609,459),(640,451),(650,482),(649,545),(691,553),(696,636),(723,661),(743,735),(752,821),(758,879),(705,857),(625,819),(567,772),(536,720),(520,664),(525,594),(552,526)]
rm=mask(rear.shape,[left,[(1536-x,y) for x,y in left]])
sm=mask(side.shape,[[(875,526),(909,495),(942,510),(974,548),(999,596),(1016,635),(1012,664),(978,682),(923,684),(870,671),(840,629),(835,587)]])
theta=np.linspace(-np.pi,np.pi,W)[None,:];phi=np.linspace(0,np.pi,H)[:,None]
x=2.02*np.sin(phi)*np.sin(theta);y=np.broadcast_to(2.772+2.02*np.cos(phi),(H,W));z=1.89+2.36*np.sin(phi)*np.cos(theta)
# Sample a broad clean strip without oscillating/repeating source coordinates.
px=560+130*(.5+.5*x/2.02);py=556+252*(.5-.5*(y-2.772)/2.02)
base=sample(rear,px,py)
# Genuine orthographic projections: posterior from the new reference, lateral from the original.
ru=768+x/.00715;rv=218+(4.792-y)/(4.04/682)
su=931+(z-1.89)*(91/2.36);sv=590-(y-2.772)*(94/2.02)
rc=sample(rear,ru,rv);sc=sample(side,su,sv)
rw=sample(rm,ru,rv)*np.maximum(0,np.cos(theta))**4
sw=sample(sm,su,sv)*abs(np.sin(theta))**3
# Fill only uncovered directions; do not dim the actual source glow twice with unused masks.
coverage=np.clip(rw+sw,0,1);total=np.maximum(rw+sw,1e-8)
color=(rc*rw[...,None]+sc*sw[...,None])/total[...,None]*coverage[...,None]+base*(1-coverage[...,None])
# Hide the seam beneath the thorax. Pole averaging limited to a narrow invisible strip.
pole=np.clip(np.sin(phi)*30,0,1)[...,None]
color=color*pole+np.mean(color,axis=1,keepdims=True)*(1-pole)
color[:,0]=(color[:,0]+color[:,-1])/2;color[:,-1]=color[:,0]
rgb=np.uint8(np.clip(color,0,255))
# Blockbench emissive uses inverse alpha as emission strength, not transparency.
luma=(color*np.array([.2126,.7152,.0722])).sum(axis=2)
emission=np.clip((luma-110)/105,0,1)**1.35
alpha=np.uint8(np.clip(242-194*emission,32,255))
Image.fromarray(np.dstack([rgb,alpha]),'RGBA').save(ROOT/'腹部_连续环绕UV_v5.png')
Image.fromarray(rgb).save(ROOT/'work/abdomen_v5_albedo.png')
Image.fromarray(255-alpha).save(ROOT/'work/abdomen_v5_emission_mask.png')
(ROOT/'work/abdomen_atlas_provenance_v5.json').write_text(json.dumps({'size':[W,H],'sources':['背面新参考.png','reference.png'],'method':'recalibrated orthographic side/rear projections; monotonic source patch fallback; smooth normalized directional weights','added_artwork':False,'uv_seam':'anterior beneath thorax','opaque_skin':True,'blockbench_alpha':'inverse alpha controls emissive amount; opaque in emissive mode; do not use as runtime transparency','side_calibration':{'u_center':931,'v_center':590,'u_radius':91,'v_radius':94}},ensure_ascii=False,indent=2),encoding='utf-8')
print('Saved v5 source-derived abdomen atlas')
