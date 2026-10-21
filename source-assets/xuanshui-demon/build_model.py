"""Original editable mesh construction for 玄水妖. Y up, nose -Z."""
import math, json, uuid, base64, pathlib, random
P=pathlib.Path(__file__).resolve().parent
R=random.Random(713)
elements=[]; groups=[]; outline=[]
def uid():return str(uuid.uuid4())
def add(a,b):return [a[i]+b[i] for i in range(3)]
def sub(a,b):return [a[i]-b[i] for i in range(3)]
def mul(a,s):return [v*s for v in a]
def dot(a,b):return sum(a[i]*b[i] for i in range(3))
def cross(a,b):return [a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]]
def norm(a):return mul(a,1/max(1e-9,math.sqrt(dot(a,a))))
def mix(a,b,t):return add(mul(a,1-t),mul(b,t))
def group(name,parent=None,origin=(0,0,0)):
    g={'name':name,'uuid':uid(),'origin':list(origin),'rotation':[0,0,0],'export':True,'visibility':True,'isOpen':False,'children':[]}
    groups.append(g);o={'uuid':g['uuid'],'isOpen':False,'children':[]}
    (parent['children'] if parent else outline).append(o)
    return o
root=group('玄水妖 · 根骨')
torso=group('躯干 · 鳞甲与腹甲',root,(0,24,0))
head=group('头颈 · 玄水龙首',root,(0,29,-28))
fins=group('中轴背鳍',root,(0,36,0))
tail=group('卷尾',root,(0,20,24))
REG={'skin':(4,4,504,504),'ridge':(516,516,248,248),'belly':(4,516,504,248),'claw':(772,516,248,248),'fin':(516,4,504,504)}
def swatch(n):return [n*64+32,832]
def uvmap(region,u,v):
    if isinstance(region,int):return swatch(region)
    x,y,w,h=REG[region];return [x+u*w,y+v*h]
def mesh(name,verts,polys,uvs,parent,region='skin'):
    vid=['v'+str(i) for i in range(len(verts))];faces={}
    for k,poly in enumerate(polys):
        subsets=[list(range(len(poly)))] if len(poly)<=4 else [[0,j,j+1] for j in range(1,len(poly)-1)]
        for inds in subsets:
            faces['f'+str(len(faces))]={'vertices':[vid[poly[j]] for j in inds],'uv':{vid[poly[j]]:uvmap(region,*uvs[k][j]) for j in inds},'texture':0}
    e={'name':name,'uuid':uid(),'type':'mesh','origin':[0,0,0],'rotation':[0,0,0],'shading':'flat','color':0,'export':True,'visibility':True,'vertices':{vid[i]:[round(x,5) for x in v] for i,v in enumerate(verts)},'faces':faces}
    elements.append(e);parent['children'].append(e['uuid']);return e
def grid_mesh(name,rings,parent,region='skin',vscale=1,belly=False):
    n=len(rings[0]);verts=sum(rings,[]);polys=[];uvs=[]
    for j in range(len(rings)-1):
        for k in range(n):
            polys.append([j*n+k,j*n+(k+1)%n,(j+1)*n+(k+1)%n,(j+1)*n+k])
            u0=k/n;u1=(k+1)/n;v0=j/(len(rings)-1)*vscale;v1=(j+1)/(len(rings)-1)*vscale
            uvs.append([(u0,v0),(u1,v0),(u1,v1),(u0,v1)])
    polys.extend([list(reversed(range(n))),list(range((len(rings)-1)*n,len(rings)*n))]);uvs.extend([[(.3,.3)]*n,[(.6,.6)]*n])
    return mesh(name,verts,polys,uvs,parent,region)
def loft(name,sections,parent,region='skin',n=24):
    rings=[]
    for z,y,rx,ry in sections:
        rings.append([[rx*math.cos(2*math.pi*k/n),y+ry*math.sin(2*math.pi*k/n),z] for k in range(n)])
    return grid_mesh(name,rings,parent,region)
def tube(name,points,radii,parent,region='skin',n=10,aspect=1):
    rings=[]
    for j,p in enumerate(points):
        tangent=norm(sub(points[min(j+1,len(points)-1)],points[max(0,j-1)]))
        axis=norm(cross([1,0,0] if abs(tangent[0])<.88 else [0,0,1],tangent));other=norm(cross(tangent,axis))
        r=radii[j] if isinstance(radii,list) else radii
        rings.append([add(p,add(mul(axis,math.cos(k*2*math.pi/n)*r),mul(other,math.sin(k*2*math.pi/n)*r*aspect))) for k in range(n)])
    return grid_mesh(name,rings,parent,region)
def ellipsoid(name,c,rad,parent,region='skin',segments=20,rows=12):
    rings=[]
    for j in range(rows+1):
        a=-math.pi/2+math.pi*j/rows;rr=max(.025,math.cos(a))
        rings.append([[c[0]+rad[0]*rr*math.cos(k*2*math.pi/segments),c[1]+rad[1]*math.sin(a),c[2]+rad[2]*rr*math.sin(k*2*math.pi/segments)] for k in range(segments)])
    return grid_mesh(name,rings,parent,region)
def glow(name,path,parent,width=.23):
    tube(name+' · 灵水亮纹',path,width*1.15,parent,2,n=6)
def fin(name,root_a,root_b,tip,notch,parent,width=.6):
    # Curved thick membrane; serrated trailing outline and an independent structural ray.
    outer=[root_a,tip,mix(tip,notch,.38),add(mix(tip,notch,.4),[0,-.65,-.35]),mix(tip,notch,.66),add(mix(tip,notch,.68),[0,-.6,-.28]),notch,root_b]
    center=mix(mix(root_a,root_b,.5),mix(tip,notch,.5),.46)
    plane=norm(cross(sub(tip,root_a),sub(root_b,root_a)))
    verts=[];polys=[];uvs=[]
    for side in (-1,1):
        offset=mul(plane,width*side/2)
        ci=len(verts);verts.append(add(center,offset))
        verts.extend([add(p,offset) for p in outer])
        for k in range(len(outer)):
            poly=[ci,ci+1+k,ci+1+(k+1)%len(outer)]
            if side<0:poly.reverse()
            def fuv(idx):
                if idx==ci:return (.5,.50)
                q=idx-ci-1
                return [(0,.99),(.05,.025),(.3,.05),(.33,.08),(.58,.065),(.61,.12),(.88,.1),(1,.99)][q]
            polys.append(poly);uvs.append([fuv(i) for i in poly])
    for k in range(8):
        ids=[1+k,1+(k+1)%8,10+(k+1)%8,10+k];polys.append(ids);uvs.append([(.5,.18)]*4)
    mesh(name+' · 冰蓝鳍膜',verts,polys,uvs,parent,'fin')
    ribpath=[root_a,mix(root_a,tip,.5),mix(root_a,tip,.83),tip]
    tube(name+' · 深蓝前缘',ribpath,[width*1.9,width*1.45,width*.8,.09],parent,'ridge',n=7)
    for t in (.32,.65):
        tube(name+' · 鳍脉', [mix(root_a,root_b,t),mix(center,notch,t*.5),mix(tip,notch,t)], [.28,.19,.06],parent,7,n=5)

# Continuous muscular torso; deliberately faceted, not a stack of cubes.
loft('躯干连续主网格',[( -28,28,6,7),(-23,28,11,11),(-16,27,14.5,14),(-7,25,15,13.5),(4,23.5,13,11.5),(14,22,12.8,10),(23,21,10,8),(29,20,6,6)],torso,n=28)
loft('颈部', [(-38,28,6,6),(-33,30,9,8),(-27,29,10.8,9.5),(-20,27,11,11)],head,n=20)

# Ventral lamellae: overlapping V shaped chest to belly, embedded along outer shell.
for i in range(8):
    z=-31+i*7.1;y=25.8-i*2.8 if i<4 else 15.5+(i-4)*.55
    w=5.6+min(i,3)*1.3-(max(0,i-4))*.65
    v=[[-w,y,z],[-w*.68,y-3.1,z-1.1],[0,y-4.6,z-2.8],[w*.68,y-3.1,z-1.1],[w,y,z],[-w*.92,y+.2,z+5],[0,y-3.7,z+4.2],[w*.92,y+.2,z+5]]
    f=[[0,1,2,6,5],[2,3,4,7,6]]
    mesh('腹甲_%02d'%i,v,f,[[(.03,.05),(.15,.8),(.5,.98),(.5,.45),(.03,.03)],[(.5,.98),(.85,.8),(.97,.05),(.97,.03),(.5,.45)]],torso,'belly')

for i in range(6):
    y=24-i*2.8;z=-35+i*2.4;w=6.4+math.sin(i/5*math.pi)*2.5-i*.2
    v=[[-w,y,z],[0,y-1.6,z-1.3],[w,y,z],[-w*.93,y-2.1,z+1.1],[0,y-3.8,z-.1],[w*.93,y-2.1,z+1.1]]
    mesh('胸前V形叠甲_%d'%i,v,[[0,1,4,3],[1,2,5,4]], [[(.06,.06),(.5,.2),(.5,.94),(.06,.8)],[(.5,.2),(.94,.06),(.94,.8),(.5,.94)]],torso,'belly')

bellysections=[(-27,28,10.8,9.6),(-20,27,13.6,13.4),(-10,25.5,15,13.9),(0,24.1,14.2,12.5),(10,22.7,13,10.9),(20,21.5,11.5,9.3),(27,20.3,7.5,7.1)]
bv=[];bp=[];bu=[]
for z,y,rx,ry in bellysections:
    for k in range(9):
        a=math.pi*(1.08+k*.105)
        bv.append([rx*1.012*math.cos(a),y+ry*1.012*math.sin(a),z])
for j in range(6):
    for k in range(8):
        bp.append([j*9+k,j*9+k+1,(j+1)*9+k+1,(j+1)*9+k]);bu.append([(k/8,j/6),((k+1)/8,j/6),((k+1)/8,(j+1)/6),(k/8,(j+1)/6)])
mesh('躯干腹部连续银甲',bv,bp,bu,torso,'belly')

# Wedge skull, tapered snout, lower jaw, mouth seam.
loft('宽楔形龙首',[(-52,25.3,3.5,2.1),(-48,27.5,6,4.5),(-42,30,8.5,6.7),(-35,31.5,9.4,7.7),(-29,31.3,7.8,7)],head,n=20)
loft('口缝',[(-52.3,23.9,3.5,.24),(-47,23.8,5.9,.5),(-40,24,7,.6),(-34,25.5,6.4,.4)],head,0,n=14)
loft('下颌银甲',[(-52,23.3,3.3,.75),(-47,22.6,5.7,1.7),(-40,23.2,6.4,2.4),(-33,25.1,5.7,2)],head,'belly',n=16)
for side in (-1,1):
    label='左' if side<0 else '右'
    # Slanted almond eyes follow the diagonal cheek, visible from front and side.
    pts=[[side*6.2,28.4,-47.8],[side*8.4,32.5,-43.6],[side*9.45,32.4,-39.9],[side*8.9,28.8,-41.7]]
    mesh(label+'眼窝',pts,[[0,1,2,3]],[[(0,0),(1,0),(1,1),(0,1)]],head,1)
    c=[sum(p[j] for p in pts)/4 for j in range(3)]
    eye=[add(mix(c,p,.67),[side*.13,.05,-.1]) for p in pts]
    mesh(label+'青瞳',eye,[[0,1,2,3]],[[(0,0),(1,0),(1,1),(0,1)]],head,2)
    ec=[sum(p[j] for p in eye)/4 for j in range(3)]
    ellipsoid(label+'瞳光',add(ec,[side*.18,.07,-.1]),[.35,.66,.5],head,3,segments=8,rows=5)
    tube(label+'压眉',[pts[0],add(pts[1],[0,.55,0]),add(pts[2],[0,.45,0]),[side*8.5,33,-36]], [.65,1.15,1.35,.3],head,'ridge',n=7)
    ellipsoid(label+'鼻孔',[side*2.6,26.25,-51.1],[.8,.32,.43],head,0,segments=8,rows=4)
    for k in range(6):
        z=-48+k*2.15;x=side*(4.9+k*.25);y=24.2+max(0,k-3)*.4
        tube(label+'短牙_%d'%k,[[x,y,z],[x,y-1.3,z-.18]], [.55,.04],head,'belly',n=5)
    # Seven spread cheek rays, sculpted in three dimensions.
    cheek=group(label+'鳃扇',head,(side*7.8,29,-34))
    edges=[(15,42,-29),(19,38,-28),(21,33.2,-27),(20.5,28.3,-26),(18.5,23.5,-26.5),(15.8,20,-28),(12.2,18.7,-30.5)]
    for k in range(6):
        e=edges[k];ne=edges[k+1]
        a=[side*8,31.5-k*.65,-37+k*.45];b=[side*7.7,30.7-k*.95,-34+k*.7]
        t=[side*e[0],e[1],e[2]];nt=[side*ne[0],ne[1],ne[2]]
        fin(label+'颊鳍_%d'%k,a,b,t,mix(t,nt,.84),cheek,.43)
    # Four independently grouped tapering barbels, inner pair longer.
    for k in range(2):
        barbel=group(label+('长须' if k==0 else '短须'),head,(side*(2.5+k*2.3),22,-46))
        pts=[[side*(2.4+k*2.5),22.5,-46+k*1.5],[side*(3.7+k*2.7),18,-46.5],[side*(4.4+k*2.9),12,-48],[side*(5.5+k*2.4),6+k*2.5,-48.5],[side*(7.1+k*1.8),3.8+k*3,-46],[side*(7.5+k*1.5),4.2+k*3,-42.8]]
        tube(label+'须蓝缘',pts,[.83,.82,.72,.55,.35,.03],barbel,7,n=8)
        tube(label+'须白芯',[add(p,[0,.14,-.35]) for p in pts],[.43,.46,.4,.28,.13,.015],barbel,3,n=7)
    glow(label+'额侧灵纹',[[side*3.6,36.25,-39],[side*4.8,37.2,-35],[side*5.4,37.8,-31]],head,.18)

# Cranial sail and dorsal sawtooth: central, continuous, graded to tail.
fin('额顶竖鳍',[0,33,-46],[0,36,-27],[0,52,-30],[0,44,-24],fins,.8)
for i,(z,y,h,length) in enumerate([(-27,36,19,17),(-13,39,20,18),(1,36,18,17),(14,31,15,16),(27,26,12,15),(40,21,10,13),(51,23,9,12)]):
    fin('背鳍_%02d'%i,[0,y-3,z],[0,y-4,z+length],[0,y+h*.9,z+length*.65],[0,y+h*.44,z+length*.9],fins,.85 if i<4 else .55)

# Limbs: shoulder, forearm, five broad toes, recurve claws, paired outer fins.
for side in (-1,1):
    label='左' if side<0 else '右'
    front=group(label+'前肢',root,(side*12,27,-19))
    ellipsoid(label+'肩甲',[side*12.8,26,-18],[7.2,11,10],front,segments=20,rows=12)
    tube(label+'前上臂',[[side*13,24,-18],[side*15.6,17,-19],[side*17,12,-24]],[6.4,6.0,5.1],front,n=14)
    tube(label+'前臂',[[side*17,14,-23],[side*18.5,9,-29],[side*19,5,-35]],[5.2,4.8,4.7],front,n=14)
    ellipsoid(label+'前掌',[side*19,3.8,-36.5],[6.7,3.4,6.5],front,segments=18,rows=8)
    for k in range(5):
        xx=side*(13.8+k*2.65);zz=-40.4+abs(k-2)*1.25
        tube(label+'前趾_%d'%k,[[xx,3.9,zz+3.7],[xx+side*.3,2.7,zz],[xx+side*.35,1.9,zz-2.2]],[1.72,1.59,1.0],front,n=9)
        tube(label+'前爪_%d'%k,[[xx+side*.35,2.5,zz-1.4],[xx+side*.4,1.9,zz-3.9],[xx+side*.35,.45,zz-5.6]],[1.25,.86,.03],front,'claw',n=7)
    fin(label+'肘鳍',[side*20,16,-23],[side*21,9,-28],[side*25,27,-17],[side*26,15,-17],front,.55)
    fin(label+'腕鳍',[side*22,8,-30],[side*23,3,-35],[side*28,17,-24],[side*28,5,-24],front,.55)
    glow(label+'肩纹',[[side*14.5,35,-24],[side*16.6,32,-26],[side*18.5,28,-26],[side*19.8,24,-24],[side*19.4,20,-22]],front,.3)
    glow(label+'臂纹',[[side*18.8,16,-27],[side*19.7,13.1,-30],[side*20.7,9.6,-32.5],[side*20,7.1,-35.6]],front,.26)
    hind=group(label+'后肢',root,(side*11,21,17))
    ellipsoid(label+'髋甲',[side*12.5,20,17],[7.8,10.4,9.2],hind,segments=20,rows=12)
    tube(label+'后小腿',[[side*14.2,15.5,19],[side*18.7,10,24],[side*19.5,4.4,22.2]],[5.0,4.1,3.5],hind,n=14)
    ellipsoid(label+'后掌',[side*19.5,3.1,18.8],[5.6,2.7,6.0],hind,segments=16,rows=8)
    for k in range(4):
        xx=side*(15.2+k*2.75);zz=14+abs(k-1.5)*.9
        tube(label+'后趾_%d'%k,[[xx,3.2,zz+2.4],[xx,2.2,zz-1.4]],[1.6,1.15],hind,n=8)
        tube(label+'后爪_%d'%k,[[xx,2.7,zz-.7],[xx,1.6,zz-3.4],[xx,.5,zz-4.5]],[1.1,.65,.03],hind,'claw',n=7)
    fin(label+'后胫鳍',[side*21.4,12,23],[side*22,3,22],[side*26,20,27],[side*27,4,30],hind,.55)
    glow(label+'髋纹',[[side*15,28,12],[side*18,25.6,12.1],[side*19.6,21,13],[side*19.7,17.8,15]],hind,.25)

# Body-side stream lines follow the elliptical shell instead of floating in air.
for side in (-1,1):
    glow(('左' if side<0 else '右')+'躯体水脉',[[side*8,37,-20],[side*9,38,-16],[side*11,35,-9],[side*12.8,31,-1],[side*12.1,28,4],[side*11.0,28,10],[side*10.2,28.4,16],[side*8.8,25.9,23],[side*6.8,24,29]],torso,.26)

# Continuous curling tail, underside band, and side water channels.
tail_pts=[[0,20,24],[0,18,33],[0,15.5,43],[0,14.5,53],[0,17,63],[0,22,73],[0,30,81],[0,39,85],[0,48,83],[0,54,78]]
tail_r=[7.3,6.8,5.8,4.9,4.0,3.25,2.7,2.25,1.8,.8]
tube('尾身连续曲面',tail_pts,tail_r,tail,'skin',n=20,aspect=1.05)
for side in (-1,1):
    path=[add(p,[side*r*1.01,r*.12,0]) for p,r in zip(tail_pts,tail_r)]
    glow(('左' if side<0 else '右')+'尾水脉',path,tail,.24)
# Underside is a fitted ribbon on the tube.
rings=[]
for j,(p,r) in enumerate(zip(tail_pts,tail_r)):
    t=norm(sub(tail_pts[min(j+1,len(tail_pts)-1)],tail_pts[max(0,j-1)]));a=norm(cross([1,0,0],t));b=norm(cross(t,a))
    rings.append([add(p,add(mul(a,math.cos(k*math.pi/6)*r*1.015),mul(b,math.sin(k*math.pi/6)*r*1.05*1.015))) for k in range(-2,3)])
verts=sum(rings,[]);polys=[];uvs=[]
for j in range(9):
    for k in range(4):
        polys.append([j*5+k,j*5+k+1,(j+1)*5+k+1,(j+1)*5+k]);uvs.append([(k/4,j/9),((k+1)/4,j/9),((k+1)/4,(j+1)/9),(k/4,(j+1)/9)])
mesh('尾腹银蓝鳞带',verts,polys,uvs,tail,'belly')
caudal=group('尾端 · 扇形分叉鳍',tail,(0,34,81))
fin('尾上叶',[0,31,79],[0,52,80],[0,68,72],[0,57,90],caudal,.9)
fin('尾中叶',[0,27,77],[0,49,82],[0,59,96],[0,40,100],caudal,.85)
fin('尾下叶',[0,26,77],[0,41,85],[0,27,100],[0,18,97],caudal,.75)
# Paired lower fins broaden the caudal silhouette in front/top views.
for side in (-1,1):
    fin(('左' if side<0 else '右')+'尾侧鳍',[side*1.4,31,81],[side*1,44,84],[side*13,47,93],[side*13.8,28,96],caudal,.7)
    fin(('左' if side<0 else '右')+'尾腹鳍',[side*1.6,25,75],[side*1.8,29,83],[side*8.5,18,87],[side*10,16,91],caudal,.6)

def build():
    atlas=P/'xuanshui_atlas.png'
    tex={'name':'玄水妖 · 鳞甲与冰鳍.png','uuid':uid(),'id':'0','path':str(atlas),'width':1024,'height':1024,'uv_width':1024,'uv_height':1024,'render_mode':'default','render_sides':'double','internal':True,'saved':True,'source':'data:image/png;base64,'+base64.b64encode(atlas.read_bytes()).decode()}
    model={'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':'玄水妖','model_identifier':'xuanshui_demon','resolution':{'width':1024,'height':1024},'elements':elements,'groups':groups,'outliner':outline,'textures':[tex],'animations':[]}
    target=P/'玄水妖.bbmodel';target.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    allv=[v for e in elements for v in e['vertices'].values()]
    report={'name':model['name'],'meshes':len(elements),'groups':len(groups),'vertices':len(allv),'faces':sum(len(e['faces']) for e in elements),'bounds':{'min':[min(v[i] for v in allv) for i in range(3)],'max':[max(v[i] for v in allv) for i in range(3)]},'format':'free','texture':[1024,1024]}
    (P/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':build()
