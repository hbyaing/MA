from pathlib import Path
import json, math, hashlib
from PIL import Image
from anvil_materials import paint_surface, paint_top

ROOT = Path(__file__).resolve().parents[1]
PARTS=[]
GROUPS=['01_玄铁砧体','02_阶梯承托','03_中柱与铜饰','04_重型底座','05_顶部包角','06_底座包角','07_贯穿灵纹','08_中央灵核','09_顶部灵纹']

def box(name, a, b, mat='iron', group='01_玄铁砧体', faces=None, glow=False):
    assert all(b[i]>a[i] for i in range(3)), (name,a,b)
    p=dict(name=name,from_=a,to=b,material=mat,group=group,overrides=faces or {},glow=glow)
    PARTS.append(p)
    return p

def centered(name,w,y0,y1,d,mat='iron',group='01_玄铁砧体'):
    return box(name,[-w/2,y0,-d/2],[w/2,y1,d/2],mat,group)

def mirrorbox(name,xa,xb,ya,yb,za,zb,sx,sz,mat,group):
    x=sorted([sx*xa,sx*xb]);z=sorted([sz*za,sz*zb])
    return box(name,[x[0],ya,z[0]],[x[1],yb,z[1]],mat,group)

# Black iron head, with narrow silver worn arrises and voxel corner cuts.
centered('砧体_中央铸铁',23.5,11.35,15.7,12)
centered('砧体_左右端面',24,11.65,15.45,11.5)
centered('砧面_冷钢细边',23.5,15.7,15.95,11.8,'steel')
top=centered('砧面_灵纹工作面',23.5,15.95,16,11.5)
top['overrides']['up']='top'
centered('下缘_暗铁压边',21.6,10.95,11.45,10.5,'iron_dark')

# Tapered shoulders leave substantial negative space below the head.
for i,(w,d,y) in enumerate([(19.8,9.3,10.45),(17.5,8.2,9.95),(15,7.1,9.45),(12.5,6.1,8.95)]):
    centered('内收托肩_%02d'%i,w,y,y+.53,d,'iron_dark','02_阶梯承托')

centered('中柱_连续玄铁骨',9.2,4.4,9.35,5.4,'iron','03_中柱与铜饰')
for sx in [-1,1]:
    for sz in [-1,1]:
        mirrorbox('柱饰_铜筋',3.1,3.6,4.8,10.2,2.67,2.83,sx,sz,'bronze_dark','03_中柱与铜饰')
        mirrorbox('柱饰_护足',2.5,4.5,4.55,5.7,2.5,3.1,sx,sz,'bronze','03_中柱与铜饰')
        mirrorbox('柱饰_护足顶沿',2.4,4.6,5.7,5.85,2.5,3.14,sx,sz,'bronze','03_中柱与铜饰')
        mirrorbox('侧柱_铜筋',4.58,4.73,5,9.6,1.4,1.85,sx,sz,'bronze_dark','03_中柱与铜饰')
        mirrorbox('侧柱_护足',4.35,4.85,4.55,5.65,1.1,2.45,sx,sz,'bronze','03_中柱与铜饰')

centered('底座_沉重铸铁',18,0,2.9,10,'iron','04_重型底座')
centered('底座_下缘阴影',16.7,0,.35,8.9,'iron_dark','04_重型底座')
centered('底座_磨损台口',17.8,2.9,3.12,9.8,'steel','04_重型底座')
centered('底座_第二阶',16.5,3.12,3.6,8.7,'iron_dark','04_重型底座')
centered('底座_第二阶边线',16.3,3.6,3.72,8.5,'bronze_dark','04_重型底座')
centered('底座_上层承台',14.4,3.72,4.36,7.3,'iron','04_重型底座')
centered('底座_上层磨边',14.2,4.36,4.48,7.1,'steel','04_重型底座')

# Each L-shaped cap wraps over the top and down both adjacent sides.
for sx in [-1,1]:
    for sz in [-1,1]:
        tag=('右' if sx>0 else '左')+('后' if sz>0 else '前')
        g='05_顶部包角'
        mirrorbox(tag+'_顶包角横臂',9.35,11.77,15.96,16.09,4.5,5.8,sx,sz,'bronze',g)
        mirrorbox(tag+'_顶包角纵臂',10.5,11.77,15.96,16.09,3.1,4.5,sx,sz,'bronze',g)
        mirrorbox(tag+'_正背护肩',9.35,11.75,14.5,15.96,5.78,6.07,sx,sz,'bronze',g)
        mirrorbox(tag+'_侧护肩',11.75,12.06,14.5,15.8,3.1,5.76,sx,sz,'bronze',g)
        mirrorbox(tag+'_正背竖包边',10.6,11.75,11.55,14.5,5.8,6.07,sx,sz,'bronze',g)
        mirrorbox(tag+'_侧竖包边',11.75,12.06,11.65,14.5,4.6,5.78,sx,sz,'bronze',g)
        mirrorbox(tag+'_转角接合',11.74,12.055,11.65,15.8,5.74,6.065,sx,sz,'bronze',g)
        mirrorbox(tag+'_包角底扣',10.2,11.8,11.47,11.72,5.45,6.08,sx,sz,'bronze_dark',g)
        mirrorbox(tag+'_角部蓝嵌片',9.8,10.5,16.005,16.035,3.25,3.8,sx,sz,'cyan','09_顶部灵纹')['glow']=True
        # Base has wider corners at the top and feet at the floor.
        g='06_底座包角'
        mirrorbox(tag+'_底座横包边',6.1,8.98,2.9,3.15,3.8,5.03,sx,sz,'bronze',g)
        mirrorbox(tag+'_底座纵包边',7.8,9.02,2.9,3.15,2.9,3.8,sx,sz,'bronze',g)
        mirrorbox(tag+'_底座正肩',6.1,8.98,1.75,2.92,4.97,5.12,sx,sz,'bronze',g)
        mirrorbox(tag+'_底座正竖边',7.75,8.98,.04,1.75,4.97,5.12,sx,sz,'bronze',g)
        mirrorbox(tag+'_底座侧竖边',8.98,9.13,.04,2.92,3.8,4.98,sx,sz,'bronze',g)
        mirrorbox(tag+'_承台铜端',5.7,7.24,3.7,4.4,2.45,3.69,sx,sz,'bronze',g)

# Conductors are thin geometry with stepped dark blue -> azure -> cyan bands.
def conductor_front(name,z,y0,y1,width=1.3):
    s=1 if z>0 else -1
    for k,(w,mat,offset) in enumerate([(width+1,'socket',0),(width,'blue',.013),(.48,'cyan',.026)]):
        zz=sorted([z+s*offset,z+s*(offset+.024)])
        box(name+'_'+mat,[-w/2,y0,zz[0]],[w/2,y1,zz[1]],mat,'07_贯穿灵纹',glow=k>0)
def conductor_top(name,y,z0,z1,width=1.3):
    for k,(w,mat,dy) in enumerate([(width+.6,'socket',0),(width,'blue',.015),(.48,'cyan',.03)]):
        box(name+'_'+mat,[-w/2,y+dy,z0],[w/2,y+dy+.013,z1],mat,'07_贯穿灵纹',glow=k>0)

for s in [-1,1]:
    tag='前' if s<0 else '后'
    conductor_front(tag+'_砧面导纹',s*6.005,11.34,15.97)
    for idx,(depth,ya,yb) in enumerate([(5.25,10.95,11.35),(4.65,10.45,10.95),(4.1,9.95,10.45),(3.55,9.45,9.95),(3.05,8.95,9.45)]):
        conductor_front(tag+'_托肩导纹_'+str(idx),s*(depth+.006),ya,yb,1.1)
    for idx,(yy,za,zb) in enumerate([(11.32,5.25,6),(10.92,4.65,5.25),(10.42,4.1,4.65),(9.92,3.55,4.1),(9.42,3.05,3.55),(8.92,2.7,3.05)]):
        zs=sorted([s*za,s*zb]);conductor_top(tag+'_托肩折纹_'+str(idx),yy,*zs,1.1)
    # Small bronze clamps at head bottom, interrupted by the blue channel.
    for sx in [-1,1]:
        mirrorbox(tag+'_纹路铜扣',.8,3.4,11.35,11.7,5.9,6.1,sx,s,'bronze_dark','01_玄铁砧体')
        mirrorbox(tag+'_上缘铜卡',.82,1.55,13.35,13.7,5.93,6.09,sx,s,'bronze','01_玄铁砧体')
    conductor_front(tag+'_中柱导纹',s*2.725,4.48,10.99,1.1)
    conductor_front(tag+'_底座主导纹',s*5.015,.01,3.13)
    conductor_front(tag+'_第二阶导纹',s*4.36,3.12,3.73)
    conductor_front(tag+'_承台导纹',s*3.66,3.72,4.5)
    for y,za,zb,n in [(3.14,4.35,5,'一级'),(3.735,3.65,4.35,'二级'),(4.495,2.7,3.65,'上台')]:
        zs=sorted([s*za,s*zb]);conductor_top(tag+n+'_蓝纹',y,*zs)
    # Thin pedestal crystal socket, a stepped elongated octagon.
    z=s*2.85
    def corepiece(n,w,h,mat,depth):
        zz=sorted([z+s*depth,z+s*(depth+.06)])
        box(tag+'_灵核_'+n,[-w/2,7.1-h/2,zz[0]],[w/2,7.1+h/2,zz[1]],mat,'08_中央灵核',glow=mat in ['blue','cyan','white'])
    corepiece('暗铁竖框',2.4,3.9,'socket',0)
    corepiece('暗铁横框',2.9,2.8,'socket',.005)
    corepiece('蓝晶外竖',1.85,3.65,'blue',.07)
    corepiece('蓝晶外横',2.25,2.9,'blue',.08)
    corepiece('青晶主面',1.0,2.8,'cyan',.15)
    corepiece('晶体肩部',1.4,2.2,'cyan',.145)
    corepiece('亮芯',.42,2.15,'white',.215)
    # Side face repeats the same visual language, scaled to the narrower depth.
    x=s*4.62
    for w,h,mat,dep in [(1.8,3.8,'socket',0),(1.3,3.2,'blue',.045),(.65,2.6,'cyan',.09),(.25,1.9,'white',.14)]:
        xx=sorted([x+s*dep,x+s*(dep+.03)])
        box(tag+'_侧灵核_'+mat,[xx[0],7.15-h/2,-w/2],[xx[1],7.15+h/2,w/2],mat,'08_中央灵核',glow=mat!='socket')

# A very shallow raised stepped ring follows the painted rune on the work surface.
# Side conductors repeat the cross-axis line down the narrower elevation.
def side_vertical(n,x,y0,y1,width=1.0):
    s=1 if x>0 else -1
    for k,(w,mat,dep) in enumerate([(width+.55,'socket',0),(width,'blue',.018),(.34,'cyan',.035)]):
        xx=sorted([x+s*dep,x+s*(dep+.018)])
        box(n+'_'+mat,[xx[0],y0,-w/2],[xx[1],y1,w/2],mat,'07_贯穿灵纹',glow=k>0)
def side_horizontal(n,y,xa,xb):
    for k,(w,mat,dep) in enumerate([(1.55,'socket',0),(1.,'blue',.018),(.34,'cyan',.035)]):
        box(n+'_'+mat,[xa,y+dep,-w/2],[xb,y+dep+.012,w/2],mat,'07_贯穿灵纹',glow=k>0)
for s in [-1,1]:
    n='右侧' if s>0 else '左侧'
    side_vertical(n+'_砧面',s*12.01,11.65,15.45)
    for idx,(xx,ya,yb) in enumerate([(10.8,10.95,11.45),(9.9,10.45,10.95),(8.75,9.95,10.45),(7.5,9.45,9.95),(6.25,8.95,9.45)]):
        side_vertical(n+'_托肩_'+str(idx),s*(xx+.006),ya,yb,.9)
    side_vertical(n+'_砧口',s*11.77,15.45,16.005)
    side_vertical(n+'_底座',s*9.01,.01,3.14)
    side_vertical(n+'_二阶',s*8.26,3.12,3.735)
    side_vertical(n+'_承台',s*7.21,3.72,4.50)
    side_vertical(n+'_中柱',s*4.64,4.48,8.97,.9)
    for y,xa,xb,label in [(16.017,9.7,11.75,'顶面'),(3.145,8.25,9.,'底阶'),(3.745,7.2,8.25,'中阶'),(4.505,4.65,7.2,'上阶')]:
        xx=sorted([s*xa,s*xb]);side_horizontal(n+label,y,*xx)

# This is a continuous pixel-circle; each horizontal run remains editable.
ring_r=3.35;unit=.25
for zi in range(-14,14):
    z=(zi+.5)*unit
    xs=[]
    for xi in range(-14,14):
        x=(xi+.5)*unit;r=math.hypot(x,z)
        if ring_r-.22<=r<=ring_r+.06: xs.append(xi)
    runs=[]
    for xi in xs:
        if not runs or xi>runs[-1][-1]+1:runs.append([xi])
        else:runs[-1].append(xi)
    for run in runs:
        box('顶环_%s_%s'%(zi,run[0]),[run[0]*unit,16.012,zi*unit],[(run[-1]+1)*unit,16.073,(zi+1)*unit],'bronze_dark','09_顶部灵纹')

# Small stepped luminous crystal in the middle of the horizontal rune.
for w,d,y0,y1,mat in [(2,1,16.025,16.11,'blue'),(1,2,16.025,16.11,'blue'),(1.45,.85,16.11,16.16,'cyan'),(.85,1.45,16.11,16.16,'cyan'),(.65,.65,16.16,16.2,'white')]:
    centered('顶面灵眼_'+mat,w,y0,y1,d,mat,'09_顶部灵纹')['glow']=True

# Atlas packing: repeated sizes share a patch; every face uses integer UVs.
PATCHES={}
def patch(w,h,mat,variant=0):
    key=(w,h,mat,variant)
    if key not in PATCHES:
        seed=int(hashlib.md5(str(key).encode()).hexdigest()[:8],16)
        pic=paint_top(w,h) if mat=='top' else paint_surface(w,h,mat,seed)
        PATCHES[key]={'image':pic,'mat':mat}
    return key
for i,p in enumerate(PARTS):
    sz=[p['to'][a]-p['from_'][a] for a in range(3)]
    p['faces']={}
    for face,axes in [('north',(0,1)),('south',(0,1)),('east',(2,1)),('west',(2,1)),('up',(0,2)),('down',(0,2))]:
        w,h=[max(2,round(sz[a]*4)) for a in axes]
        mat=p['overrides'].get(face,p['material'])
        if mat=='top':w,h=96,48
        key=patch(w,h,mat,0 if mat in ['blue','cyan','white','socket','top'] else i%4)
        p['faces'][face]={'key':key,'glow':p['glow']}

W=512;x=y=2;row=0
for key,v in sorted(PATCHES.items(),key=lambda kv:-kv[0][1]):
    w,h=key[:2]
    if x+w+2>W:x=2;y+=row+4;row=0
    v['uv']=[x,y,x+w,y+h];x+=w+4;row=max(row,h)
H=2**math.ceil(math.log2(y+row+4))
atlas=Image.new('RGBA',(W,H),(20,22,29,255));emission=Image.new('RGBA',(W,H),(0,0,0,255))
for key,v in PATCHES.items():
    u,t,r,b=v['uv'];im=v['image'];atlas.paste(im,(u,t))
    # Expand edge pixels into a two-pixel gutter to keep mipmaps from bleeding.
    atlas.paste(im.crop((0,0,1,im.height)).resize((2,im.height)),(u-2,t))
    atlas.paste(im.crop((im.width-1,0,im.width,im.height)).resize((2,im.height)),(r,t))
    atlas.paste(im.crop((0,0,im.width,1)).resize((im.width,2)),(u,t-2))
    atlas.paste(im.crop((0,im.height-1,im.width,im.height)).resize((im.width,2)),(u,b))
    if v['mat'] in ['blue','cyan','white']:emission.paste(im,(u,t))
    elif v['mat']=='top':
        ep=Image.new('RGBA',im.size,(0,0,0,255));pix=ep.load()
        for yy in range(im.height):
            for xx in range(im.width):
                c=im.getpixel((xx,yy))
                if c[2]>100 and c[2]>c[0]*1.8:pix[xx,yy]=c
        emission.paste(ep,(u,t))
for p in PARTS:
    for f,d in p['faces'].items():d['uv']=PATCHES[d.pop('key')]['uv']
    p['from']=p.pop('from_');p.pop('overrides')
atlas.save(ROOT/'xuantie_anvil_atlas.png');emission.save(ROOT/'xuantie_anvil_emission.png')
glow_preview=emission.copy();glow_preview.putalpha(16);glow_preview.save(ROOT/'xuantie_anvil_glow.png')
(ROOT/'work'/'geometry.json').write_text(json.dumps({'parts':PARTS,'groups':GROUPS,'texture_size':[W,H]},ensure_ascii=False),encoding='utf-8')
print(json.dumps({'cubes':len(PARTS),'groups':len(GROUPS),'atlas':[W,H],'patches':len(PATCHES)}))
