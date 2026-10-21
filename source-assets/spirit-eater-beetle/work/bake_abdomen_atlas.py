"""Bake a continuous abdomen chart from supplied reference pixels (no generated artwork).
The UV seam is placed under the thorax; skin sampling excludes painted armor and legs.
"""
from pathlib import Path
import json
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
ROOT=Path(__file__).resolve().parent.parent
W,H=1024,512
rear=np.asarray(Image.open(ROOT/'背面新参考.png').convert('RGB'),dtype=float)
side=np.asarray(Image.open(ROOT/'reference.png').convert('RGB'),dtype=float)
def sample(a,x,y):
    x=np.clip(x,0,a.shape[1]-1.001);y=np.clip(y,0,a.shape[0]-1.001)
    i=x.astype(int);j=y.astype(int);fx=x-i;fy=y-j
    if a.ndim==3: fx=fx[...,None];fy=fy[...,None]
    return (a[j,i]*(1-fx)+a[j,i+1]*fx)*(1-fy)+(a[j+1,i]*(1-fx)+a[j+1,i+1]*fx)*fy

def mask(shape,polygons):
    im=Image.new('L',(shape[1],shape[0]));d=ImageDraw.Draw(im)
    for poly in polygons:d.polygon(poly,fill=255)
    # Erode first: feather must not pull neighboring chitin into skin.
    return np.asarray(im.filter(ImageFilter.MinFilter(13)).filter(ImageFilter.GaussianBlur(7)),dtype=float)/255
left=[(609,459),(640,451),(650,482),(649,545),(691,553),(696,636),(723,661),(743,735),(752,821),(758,879),(705,857),(625,819),(567,772),(536,720),(520,664),(525,594),(552,526)]
right=[(1536-x,y) for x,y in left]
rm=mask(rear.shape,[left,right])
sm=mask(side.shape,[[(875,526),(909,495),(942,510),(974,548),(999,596),(1020,633),(1017,669),(978,685),(923,687),(867,673),(839,629),(833,587)]])
theta=np.linspace(-np.pi,np.pi,W)[None,:]
phi=np.linspace(0,np.pi,H)[:,None]
x=2.02*np.sin(phi)*np.sin(theta)
y=np.broadcast_to(2.772+2.02*np.cos(phi),(H,W))
z=1.89+2.36*np.sin(phi)*np.cos(theta)
# Mirrored sample of the clear left lobe provides a low-contrast fallback at poles/hidden areas.
px=554+94*(.5+.5*np.sin(theta*2+np.cos(phi)*1.3))
py=622+137*(.5+.5*np.sin(phi*2.2+np.cos(theta)*.7))
base=sample(rear,px,py)*np.array([.90,.94,.95])
ru=768+x/.00715;rv=218+(4.792-y)/(4.04/682)
rc=sample(rear,ru,rv);rw=sample(rm,ru,rv)*np.maximum(0,np.cos(theta))**1.5
su=760+z/.014;sv=790-y/.014
sc=sample(side,su,sv);sw=sample(sm,su,sv)*abs(np.sin(theta))**2.5*(1-rw)
color=rc*rw[...,None]+sc*sw[...,None]+base*(1-rw-sw)[...,None]
# No pole pinwheel; protect UV seam from interpolation differences.
pole=np.clip(np.sin(phi)*7,0,1)[...,None]
pole_color=np.mean(color,axis=1,keepdims=True)
color=color*pole+pole_color*(1-pole)
color[:,0]=(color[:,0]+color[:,-1])/2;color[:,-1]=color[:,0]
Image.fromarray(np.uint8(np.clip(color,0,255))).save(ROOT/'腹部_连续环绕UV_v3.png')
Image.fromarray(np.uint8(rm*255)).save(ROOT/'work/abdomen_reference_mask.png')
(ROOT/'work/abdomen_atlas_provenance.json').write_text(json.dumps({'size':[W,H],'sources':['背面新参考.png','reference.png'],'method':'continuous lat-long bake, manually isolated skin masks, blended reference pixels','added_artwork':False,'uv_seam':'front, under thorax','opaque_skin':True},ensure_ascii=False,indent=2),encoding='utf-8')
print('Saved continuous abdomen atlas',W,H)
