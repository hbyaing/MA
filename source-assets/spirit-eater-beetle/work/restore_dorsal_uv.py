import json
from pathlib import Path
from PIL import Image
p=Path('E:/五行仙途/source-assets/spirit-eater-beetle')
old=json.loads((p/'before_shell_antenna_revision.bbmodel').read_text('utf8'))
im=Image.open(p/'reference.png').convert('RGBA')
m=next(e for e in old['elements'] if '腹部_青玉色薄膜' in e['name'])
out=[]
for key,f in m['faces'].items():
    vs=[m['vertices'][k] for k in f['vertices']]
    cy=sum(v[1] for v in vs)/len(vs)
    if cy<1.40: continue
    uv=[f['uv'][k] for k in f['vertices']]
    bad=False
    for j in range(1,len(uv)-1):
        a,b,c=uv[0],uv[j],uv[j+1]
        for v in range(11):
            for w in range(11-v):
                x=round(a[0]*(1-v/10-w/10)+b[0]*v/10+c[0]*w/10)
                y=round(a[1]*(1-v/10-w/10)+b[1]*v/10+c[1]*w/10)
                if im.getpixel((max(0,min(im.width-1,x)),max(0,min(im.height-1,y))))[3]<200: bad=True
    if bad:
        rect=[(771,508),(790,508),(790,527),(771,527)] if cy>3.7 else [(900,597),(932,597),(932,621),(900,621)]
        f['uv']={k:list(rect[i]) for i,k in enumerate(f['vertices'])}
    out.append({'key':key,'uv':f['uv'],'repaired_alpha':bad})
(p/'work/dorsal_uv.json').write_text(json.dumps({'name':m['name'],'faces':out}),encoding='utf8')
print({'faces':len(out),'alpha_repairs':sum(f['repaired_alpha'] for f in out)})
