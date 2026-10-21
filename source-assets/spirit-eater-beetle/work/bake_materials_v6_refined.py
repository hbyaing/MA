from pathlib import Path
import json
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parent.parent
src=np.asarray(Image.open(ROOT/'背面新参考.png').convert('RGB'),dtype=float)
def sample(x,y):
 x,y=np.broadcast_arrays(x,y);i=x.astype(int);j=y.astype(int);fx=(x-i)[...,None];fy=(y-j)[...,None]
 return (src[j,i]*(1-fx)+src[j,i+1]*fx)*(1-fy)+(src[j+1,i]*(1-fx)+src[j+1,i+1]*fx)*fy
patches=[(89,361,397,27),(81,354,397,25),(82,452,495,25),(77,445,495,24),(71,452,495,22),(57,560,600,18),(49,568,600,16)]
atlas=Image.new('RGB',(512,512),(28,37,34))
t=np.clip((np.arange(512)[None,:]-8)/496,0,1)*2-1
r=(np.arange(64)[:,None]-3)/52
for n,(hw,y0,y1,bend) in enumerate(patches):
 x=768+hw*t;y=y0+(y1-y0)*np.clip(r,0,1)+bend*abs(t)
 y+=np.clip(r-1,0,1)*38
 a=sample(x,y)*np.array([.95,.97,.95]);atlas.paste(Image.fromarray(np.uint8(np.clip(a,0,255))),(0,n*64))
atlas.save(ROOT/'腹甲_弧缘分节UV_v6.png')
a=np.asarray(Image.open(ROOT/'work/abdomen_v5_albedo.png').convert('RGB'),dtype=float)
luma=(a*np.array([.2126,.7152,.0722])).sum(axis=2);emission=np.clip((luma-138)/112,0,1)**1.5
alpha=np.uint8(np.clip(253-120*emission,133,253))
Image.fromarray(np.dstack([a.astype('uint8'),alpha])).save(ROOT/'腹部_克制微光UV_v6.png')
Image.fromarray(a.astype('uint8')).save(ROOT/'work/abdomen_v6_albedo.png')
Image.fromarray(255-alpha).save(ROOT/'work/abdomen_v6_emission_mask.png')
(ROOT/'work/material_provenance_v6.json').write_text(json.dumps({'ventral':{'source':'背面新参考.png','sampling':'bilinear, conformal horizontal bands from original chitin plates; original rim retained','band_parameters_halfwidth_y0_y1_bend':patches,'atlas_size':[512,512],'rows':7,'row_height':64,'generated_artwork':False},'abdomen':{'base':'work/abdomen_v5_albedo.png','sources':['背面新参考.png','reference.png'],'blockbench_alpha':'inverse alpha is emission strength, not runtime opacity','emission_alpha_range':[int(alpha.min()),int(alpha.max())],'runtime_exports':['work/abdomen_v6_albedo.png','work/abdomen_v6_emission_mask.png']}},ensure_ascii=False,indent=2),encoding='utf8')
print('Source-aligned chitin bands and low-intensity emission saved')
