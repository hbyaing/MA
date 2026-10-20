from pathlib import Path
import json, math, random, hashlib
from PIL import Image, ImageDraw
from refined_metals import paint_metal

ROOT=Path(__file__).resolve().parents[1]
MODEL=json.loads((ROOT/'work/backup-before-texture-v2/玄铁灵砧.bbmodel').read_text(encoding='utf-8'))
GEOM=json.loads((ROOT/'work/geometry.json').read_text(encoding='utf-8'))
DENSITY=8

def rgb(c,delta=0):return tuple(max(0,min(255,round(v+delta))) for v in c[:3])+(255,)
def mix(a,b,t):return tuple(round(a[i]*(1-t)+b[i]*t) for i in range(3))+(255,)
def keyed(p):return (p['name'],tuple(round(v,5) for v in p['from']),tuple(round(v,5) for v in p['to']))
descriptions={keyed(p):p for p in GEOM['parts']}

def paint_energy(w,h,material,seed):
    im=Image.new('RGBA',(w,h));p=im.load();rng=random.Random(seed)
    vertical=h>=w;span=w if vertical else h;length=h if vertical else w
    for y in range(h):
        for x in range(w):
            u=x if vertical else y;v=y if vertical else x
            t=abs((u+.5)/span*2-1)
            if material=='blue':
                c=(150,252,255) if t<.13 else (20,219,255) if t<.37 else (1,156,255) if t<.68 else (5,77,219)
            elif material=='cyan':
                c=(180,255,255) if t<.27 else (65,240,255) if t<.6 else (3,193,245)
            else:
                c=(229,255,255) if t<.55 else (127,244,255)
            flow=math.sin((v+seed%11)*.56)*3
            # Tiny bright seams inside the stream, never a black interruption.
            if (v+seed%5)%16 in (0,1):flow+=8
            p[x,y]=rgb(c,flow+rng.choice([-1,0,0,1]))
    return im

def paint_top(w,h):
    im=paint_metal(w,h,'iron',337);p=im.load();cx=(w-1)/2;cy=(h-1)/2
    r0=26.5
    for y in range(h):
        for x in range(w):
            dx=x-cx;dy=y-cy;r=math.hypot(dx,dy)
            spill=max(math.exp(-abs(dx)/5.0)*.40,math.exp(-abs(dy)/4.5)*.30,math.exp(-r/13)*.5)
            p[x,y]=mix(p[x,y],(9,85,169),spill)
            if abs(r-(r0+4.0))<.65:
                p[x,y]=mix(p[x,y],(87,111,137),.45 if (x+y)%5 else .75)
            if abs(r-(r0+2.8))<.6:p[x,y]=mix(p[x,y],(10,20,33),.75)
            # Etched divisions under the raised metal seal.
            ang=math.atan2(dy,dx)
            if r0-5<r<r0-2 and abs(math.sin(ang*8))<.09:
                p[x,y]=(91,115,137,255)
    d=ImageDraw.Draw(im)
    vx=round(cx);hy=round(cy)
    # The vertical conduit is wider; both channels have dark inset lips.
    d.rectangle((vx-6,0,vx+6,h-1),fill=(12,37,73,255))
    d.rectangle((0,hy-4,w-1,hy+4),fill=(15,38,69,255))
    for x in range(vx-4,vx+5):
        t=abs(x-cx)
        col=(105,247,255) if t<1 else (11,204,255) if t<2.3 else (0,137,248)
        d.line((x,0,x,h-1),fill=rgb(col))
    for y in range(hy-2,hy+3):
        col=(70,237,255) if abs(y-cy)<1 else (0,135,249)
        d.line((0,y,w-1,y),fill=rgb(col))
    # Asymmetric bright fragments suggest the inlaid stream instead of flat tape.
    for yy in [8,18,h-19,h-9]:d.line((vx-1,yy,vx-1,yy+3),fill=(152,255,255,255))
    for xx in [12,27,w-29,w-14]:d.line((xx,hy,xx+4,hy),fill=(104,247,255,255))
    for y in range(max(0,hy-33),min(h,hy+34)):
        for x in range(max(0,vx-33),min(w,vx+34)):
            dx=x-cx;dy=y-cy;r=math.hypot(dx,dy)
            if r0-1.5<=r<=r0+1.1:
                if abs(dx)>3 and abs(dy)>2:
                    lit=max(-1,min(1,(-dx-dy)/(r0*1.4)))
                    p[x,y]=rgb((184,151,109),lit*27+(10 if r>r0+.1 else -8))
            elif r0-2.2<=r<r0-1.5 and abs(dx)>4 and abs(dy)>3:p[x,y]=(61,50,41,255)
            # The seal has a thin blue inset and a white-cyan center.
            if 13.5<r<15.5:p[x,y]=(10,43,94,255)
            elif 11.5<r<=13.5:p[x,y]=(1,99,217,255)
            elif 8.8<r<=11.5:p[x,y]=(0,170,253,255)
            elif 6.5<r<=8.8:p[x,y]=(24,224,255,255)
            elif 3.4<r<=6.5:p[x,y]=(110,245,255,255)
            elif r<=3.4:p[x,y]=(224,255,255,255)
            if 7<r<12 and dx+dy<-5 and (x-y)%7<2:p[x,y]=mix(p[x,y],(167,255,255),.48)
    # Four small paired strokes, an engraved ritual motif kept inside the ring.
    for sx in [-1,1]:
        for sy in [-1,1]:
            xx=round(cx+sx*16);yy=round(cy+sy*16)
            d.line((xx-sx*2,yy,xx,yy),fill=(108,136,160,255))
            d.line((xx,yy,xx,yy-sy*3),fill=(120,151,177,255))
            d.point((xx-sx,yy-sy),fill=(46,100,154,255))
    return im

def world_at(e,face,x,y,w,h):
    a=e['from'];b=e['to'];u=(x+.5)/w;v=(y+.5)/h
    if face=='north':return [b[0]-u*(b[0]-a[0]),b[1]-v*(b[1]-a[1]),a[2]]
    if face=='south':return [a[0]+u*(b[0]-a[0]),b[1]-v*(b[1]-a[1]),b[2]]
    if face=='east':return [b[0],b[1]-v*(b[1]-a[1]),b[2]-u*(b[2]-a[2])]
    if face=='west':return [a[0],b[1]-v*(b[1]-a[1]),a[2]+u*(b[2]-a[2])]
    return [a[0]+u*(b[0]-a[0]),b[1] if face=='up' else a[1],a[2]+v*(b[2]-a[2])]

def add_local_iron(im,e,face,desc):
    p=im.load();w,h=im.size;g=desc['group']
    for y in range(h):
        for x in range(w):
            xx,yy,zz=world_at(e,face,x,y,w,h)
            if face in ('north','south'):dist=abs(xx);front=True
            elif face in ('east','west'):dist=abs(zz);front=False
            else:dist=min(abs(xx),abs(zz));front=False
            glow=max(0,1-dist/2.8)**2*.35
            if '中柱' in g:
                glow=max(glow,max(0,1-dist/2.9)*max(0,1-abs(yy-7.1)/2.8)*.43)
            if '砧体' in g or '底座' in g or '承托' in g or '中柱' in g:
                p[x,y]=mix(p[x,y],(10,101,172),glow)
    return im

def add_copper_craft(im,e,face,desc):
    w,h=im.size
    # Rivets belong to broad visible cap plates, not tiny ring segments or thin edges.
    if min(w,h)<7 or max(w,h)<12 or not any(t in e['name'] for t in ['护肩','正肩','护足','铜端','横臂','纵臂']):return im
    d=ImageDraw.Draw(im)
    pts=[(3,3)] if w<13 or h<12 else [(3,3),(w-4,h-4)]
    for x,y in pts:
        if x+2>=w or y+2>=h:continue
        d.rectangle((x-1,y-1,x+2,y+2),fill=(113,87,60,255))
        d.rectangle((x,y,x+1,y+1),fill=(223,193,146,255))
        d.point((x,y),fill=(255,225,181,255))
        d.point((x+1,y+1),fill=(149,113,76,255))
    return im

def crystal(im,e,face,material):
    w,h=im.size;p=im.load()
    is_side='侧灵核' in e['name']
    for y in range(h):
        for x in range(w):
            pos=world_at(e,face,x,y,w,h)
            xx=pos[2] if is_side else pos[0];yy=pos[1]-7.1
            ridge=xx*.7+yy*.44
            val=14 if -.26<ridge<.14 else -14 if ridge>.6 else 0
            if material=='blue':c=(1,117,239) if xx<0 else (6,81,202)
            elif material=='cyan':c=(21,222,251) if ridge<0 else (8,174,232)
            else:c=(183,255,255) if xx<.02 else (99,239,254)
            if abs(ridge)<.1:val+=26
            if yy>1.0 and xx<.15:val+=15
            p[x,y]=rgb(c,val)
    return im

PATCHES={};assign=[];unknown=[]
for e in MODEL['elements']:
    desc=descriptions.get(keyed(e))
    if not desc:unknown.append(e['name']);continue
    size=[e['to'][k]-e['from'][k] for k in range(3)]
    face_records={}
    for face,axes in [('north',(0,1)),('south',(0,1)),('east',(2,1)),('west',(2,1)),('up',(0,2)),('down',(0,2))]:
        w,h=[max(2,round(size[a]*DENSITY)) for a in axes]
        mat=desc['material'];is_top=e['name']=='砧面_灵纹工作面' and face=='up'
        special_iron=mat.startswith('iron') and min(w,h)>10
        special_bronze=mat.startswith('bronze') and min(w,h)>6
        special_gem='灵核' in e['name'] and mat in ('blue','cyan','white')
        context=e['uuid']+face if special_iron or special_bronze or special_gem else ''
        variant=int(hashlib.sha1(e['name'].encode()).hexdigest()[:4],16)%5
        key=(w,h,'top' if is_top else mat,variant,context)
        if key not in PATCHES:
            seed=int(hashlib.sha1(str(key[:4]).encode()).hexdigest()[:8],16)
            if is_top:im=paint_top(w,h)
            elif mat in ('blue','cyan','white'):im=paint_energy(w,h,mat,seed)
            else:im=paint_metal(w,h,mat,seed)
            if special_iron and not is_top:im=add_local_iron(im,e,face,desc)
            if special_bronze:im=add_copper_craft(im,e,face,desc)
            if special_gem:im=crystal(im,e,face,mat)
            PATCHES[key]={'image':im,'mat':key[2]}
        face_records[face]={'key':key,'glow':desc['glow']}
    assign.append({'uuid':e['uuid'],'name':e['name'],'faces':face_records})
assert not unknown,unknown

# Four-pixel gutters protect the original pixels during filtering.
W=1024;x=y=4;row=0
for key,v in sorted(PATCHES.items(),key=lambda kv:(-kv[0][1],-kv[0][0])):
    w,h=key[:2]
    if x+w+4>W:x=4;y+=row+8;row=0
    v['uv']=[x,y,x+w,y+h];x+=w+8;row=max(row,h)
H=2**math.ceil(math.log2(y+row+4))
atlas=Image.new('RGBA',(W,H),(24,26,34,255));emission=Image.new('RGBA',(W,H),(0,0,0,255))
for key,v in PATCHES.items():
    u,t,r,b=v['uv'];im=v['image'];atlas.paste(im,(u,t))
    atlas.paste(im.crop((0,0,1,im.height)).resize((4,im.height)),(u-4,t))
    atlas.paste(im.crop((im.width-1,0,im.width,im.height)).resize((4,im.height)),(r,t))
    atlas.paste(im.crop((0,0,im.width,1)).resize((im.width,4)),(u,t-4))
    atlas.paste(im.crop((0,im.height-1,im.width,im.height)).resize((im.width,4)),(u,b))
    if v['mat'] in ('blue','cyan','white'):emission.paste(im,(u,t))
    elif v['mat']=='top':
        ep=Image.new('RGBA',im.size,(0,0,0,255));px=ep.load()
        for yy in range(im.height):
            for xx in range(im.width):
                c=im.getpixel((xx,yy))
                if c[2]>160 and c[1]>100 and c[2]>c[0]*1.2:px[xx,yy]=c
        emission.paste(ep,(u,t))
for a in assign:
    for face,d in a['faces'].items():d['uv']=PATCHES[d.pop('key')]['uv']
atlas.save(ROOT/'xuantie_anvil_atlas.png');emission.save(ROOT/'xuantie_anvil_emission.png')
glow=atlas.copy();glow.putalpha(12);glow.save(ROOT/'xuantie_anvil_glow.png')
(ROOT/'work/refined_uv.json').write_text(json.dumps({'resolution':[W,H],'assignments':assign},ensure_ascii=False),encoding='utf-8')
print(json.dumps({'cubes':len(assign),'patches':len(PATCHES),'texture_size':[W,H],'pixel_density':DENSITY}))
