"""Source-only ventral chitin islands and restrained Blockbench emissive mask."""
from pathlib import Path
import json
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parent.parent
src=Image.open(ROOT/'背面新参考.png').convert('RGB')
patches=[(683,353,757,373),(683,364,757,384),(694,376,751,394),(700,382,755,400),(690,361,751,384),(711,553,755,577),(713,574,756,593)]
atlas=Image.new('RGB',(512,512),(28,37,34))
for i,box in enumerate(patches):
 im=src.crop(box).resize((512,64),Image.Resampling.BICUBIC)
 a=np.asarray(im,dtype=float)
 # Only a subtle source-color adjustment, no invented cracks or added artwork.
 a*=np.array([.95,.97,.95]);atlas.paste(Image.fromarray(np.uint8(np.clip(a,0,255))),(0,i*64))
atlas.save(ROOT/'腹甲_原图分节UV_v6.png')
a=np.asarray(Image.open(ROOT/'work/abdomen_v5_albedo.png').convert('RGB'),dtype=float)
luma=(a*np.array([.2126,.7152,.0722])).sum(axis=2)
emission=np.clip((luma-126)/108,0,1)**1.35
alpha=np.uint8(np.clip(247-183*emission,64,247))
Image.fromarray(np.dstack([a.astype('uint8'),alpha])).save(ROOT/'腹部_柔和灵光UV_v6.png')
Image.fromarray(a.astype('uint8')).save(ROOT/'work/abdomen_v6_albedo.png')
Image.fromarray(255-alpha).save(ROOT/'work/abdomen_v6_emission_mask.png')
(ROOT/'work/material_provenance_v6.json').write_text(json.dumps({'ventral':{'source':'背面新参考.png','crop_rectangles':patches,'atlas_size':[512,512],'rows':7,'row_height':64,'generated_artwork':False},'abdomen':{'base':'work/abdomen_v5_albedo.png','sources':['背面新参考.png','reference.png'],'blockbench_alpha':'inverse alpha is emission strength, not runtime opacity','emission_alpha_range':[int(alpha.min()),int(alpha.max())],'runtime_exports':['work/abdomen_v6_albedo.png','work/abdomen_v6_emission_mask.png']}},ensure_ascii=False,indent=2),encoding='utf-8')
print('V6 source-derived ventral atlas and selective emission saved')
