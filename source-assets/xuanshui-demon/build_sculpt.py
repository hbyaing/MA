"""Reference-aligned, full-volume reconstruction. Reference PNG is used unchanged."""
import math,json,uuid,base64,pathlib,shutil
P=pathlib.Path(__file__).resolve().parent
REF=pathlib.Path(r'C:\Users\Nostzephert\Desktop\新建文件夹\ab0d6511-2eb7-489c-8ae5-3e2a39d7c6e0.png')
S=7.5;E=[];G=[];O=[]
def uid():return str(uuid.uuid4())
def add(a,b):return [a[i]+b[i] for i in range(3)]
def sub(a,b):return [a[i]-b[i] for i in range(3)]
def mul(a,f):return [n*f for n in a]
def norm(a):return mul(a,1/max(1e-9,math.sqrt(sum(n*n for n in a))))
def cross(a,b):return [a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]]
def lerp(a,b,t):return add(mul(a,1-t),mul(b,t))
def w(u,v,x=0):return [x/S,(600-v)/S,(u-780)/S]
def uv(p):return [p[2]*S+780,600-p[1]*S]
def frontuv(p):return [240+p[0]*S,600-p[1]*S]
def grp(name,parent=None,pivot=[0,0,0]):
    g={'uuid':uid(),'name':name,'origin':pivot,'rotation':[0,0,0],'export':True,'visibility':True,'children':[]};G.append(g)
    o={'uuid':g['uuid'],'isOpen':False,'children':[]};(O if parent is None else parent['children']).append(o);return o
ROOT=grp('玄水妖 · 壮硕水兽')
BODY=grp('胸腹 · 连续体块',ROOT,w(765,360))
HEAD=grp('龙首 · 宽颅短吻',ROOT,w(610,325))
BACK=grp('中央背鳍',ROOT,w(780,280))
TAIL=grp('长尾 · 上卷',ROOT,w(945,404))
def mesh(name,vs,polys,parent,mapping=uv,uvs=None,tex=0):
    faces={}
    for i,poly in enumerate(polys):
        subs=[list(range(len(poly)))] if len(poly)<=4 else [[0,j,j+1] for j in range(1,len(poly)-1)]
        for inds in subs:
            faces['f'+str(len(faces))]={'vertices':['v'+str(poly[j]) for j in inds],'uv':{'v'+str(poly[j]):([round(q,3) for q in uvs[i][j]] if uvs else [round(q,3) for q in mapping(vs[poly[j]])]) for j in inds},'texture':tex}
    e={'name':name,'uuid':uid(),'type':'mesh','origin':[0,0,0],'rotation':[0,0,0],'shading':'smooth','export':True,'visibility':True,'vertices':{'v'+str(i):[round(x,5) for x in v] for i,v in enumerate(vs)},'faces':faces};E.append(e);parent['children'].append(e['uuid']);return e
def ringsmesh(name,rings,parent,mapping=uv,uvrings=None):
    n=len(rings[0]);vs=sum(rings,[]);ps=[];us=[]
    for j in range(len(rings)-1):
        for k in range(n):
            ids=[j*n+k,j*n+(k+1)%n,(j+1)*n+(k+1)%n,(j+1)*n+k];ps.append(ids)
            if uvrings:
                flat=sum(uvrings,[]);us.append([flat[ii] for ii in ids])
    for row in [0,len(rings)-1]:
        ids=list(range(row*n,(row+1)*n));ps.append(ids if row else list(reversed(ids)))
        if uvrings:us.append([sum(uvrings,[])[ii] for ii in ps[-1]])
    return mesh(name,vs,ps,parent,mapping,us if uvrings else None)
def loft(name,sections,parent,front=False,n=24):
    rings=[]
    for u,top,bottom,rx in sections:
        cy=(top+bottom)/2;ry=(bottom-top)/2
        rings.append([w(u,cy-ry*math.sin(k*math.tau/n),rx*math.cos(k*math.tau/n)) for k in range(n)])
    return ringsmesh(name,rings,parent)
def tube(name,points,radii,parent,mapping=uv,n=14,aspect=1,uvprofile=None):
    rings=[];urs=[]
    for j,p in enumerate(points):
        t=norm(sub(points[min(j+1,len(points)-1)],points[max(0,j-1)]));a=norm(cross([1,0,0] if abs(t[0])<.9 else [0,0,1],t));b=norm(cross(t,a));r=radii[j]
        rings.append([add(p,add(mul(a,math.cos(k*math.tau/n)*r),mul(b,math.sin(k*math.tau/n)*r*aspect))) for k in range(n)])
        if uvprofile:urs.append([[uvprofile[j][0],uvprofile[j][1]+math.cos(k*math.tau/n)*2] for k in range(n)])
    return ringsmesh(name,rings,parent,mapping,urs if urs else None)
def ell(name,center,rad,parent,mapping=uv,n=20,rows=12):
    rings=[]
    for j in range(rows+1):
        a=-math.pi/2+j*math.pi/rows;r=max(.035,math.cos(a))
        rings.append([[center[0]+rad[0]*r*math.cos(k*math.tau/n),center[1]+rad[1]*math.sin(a),center[2]+rad[2]*r*math.sin(k*math.tau/n)] for k in range(n)])
    e=ringsmesh(name,rings,parent,mapping)
    for f in e['faces'].values():f['vertices'].reverse()
    return e
def skinpatch(p):
    a=uv(p);return [720+(a[0]-700)*.5,275+(a[1]-300)*.2]
def atlas_skin(e,axes=(0,1),scale=4.0):
    for f in e['faces'].values():
        f['texture']=1
        for key in f['uv']:
            p=e['vertices'][key];f['uv'][key]=[200+p[axes[0]]*scale,220-p[axes[1]]*scale]
    return e
def reference_limb(e,side,cx=150):
    for f in e['faces'].values():
        points=[e['vertices'][k] for k in f['vertices']]
        normal=norm(cross(sub(points[1],points[0]),sub(points[2],points[0])))
        for key in f['uv']:
            p=e['vertices'][key];u,v=uv(p)
            if normal[2]<-.2:
                f['uv'][key]=[max(34,min(185,100+(abs(p[0]*S)-cx)*.73)),max(390,min(542,v))]
            else:f['uv'][key]=[630+(u-645)*.45,474+(v-475)*.40]

# Main barrel is widened and deepened relative to the initial model.
bodysec=[(598,296,423,68),(630,270,443,100),(674,246,456,122),(718,237,465,135),(762,252,474,133),(808,272,477,123),(850,292,479,116),(892,318,478,105),(931,341,468,84),(961,367,462,61)]
bodymesh=loft('厚胸收腰躯干',bodysec,BODY,n=32)
for f in bodymesh['faces'].values():
    for key in f['uv']:
        u,v=f['uv'][key]
        if u<672:f['uv'][key]=[684+(u-598)*.4,280+(v-270)*.6]
    points=[bodymesh['vertices'][k] for k in f['vertices']]
    normal=norm(cross(sub(points[1],points[0]),sub(points[2],points[0])))
    if normal[1]>.70:
        f['texture']=1
        f['uv']={key:[256+bodymesh['vertices'][key][0]*4,250+bodymesh['vertices'][key][2]*3.5] for key in f['vertices']}
# Shaped ventral skin; geometry follows the barrel and carries clean silver scales.
vs=[];ps=[];us=[]
for j,(u,top,bottom,rx) in enumerate(bodysec):
    cy=(top+bottom)/2;ry=(bottom-top)/2
    for k in range(13):
        a=math.pi*(1.09+k*.068)
        vs.append(w(u,cy-ry*1.015*math.sin(a),rx*1.014*math.cos(a)))
for j in range(len(bodysec)-1):
    for k in range(12):
        ps.append([j*13+k,j*13+k+1,(j+1)*13+k+1,(j+1)*13+k]);us.append([(755+k*7,404+j*6),(755+(k+1)*7,404+j*6),(755+(k+1)*7,410+j*6),(755+k*7,410+j*6)])
abmesh=mesh('银灰分节腹甲',vs,ps,BODY,uvs=us)
for f in abmesh['faces'].values():
    f['texture']=1
    f['uv']={key:[250+abmesh['vertices'][key][0]*6,620+abmesh['vertices'][key][2]*2] for key in f['vertices']}

chest=loft('颈下厚胸 · 连续承接肩腹',[(558,344,408,44),(575,335,437,61),(594,318,464,84),(620,296,474,106),(653,285,458,117)],BODY,n=28)
atlas_skin(chest)

# Anatomical head hull with a flat broad muzzle, narrowed chin and cheek volume.
headsec=[(463,344,365,31),(480,326,375,44),(505,307,385,58),(535,293,393,68),(566,283,407,75),(598,273,417,80),(626,280,419,71),(649,303,421,56)]
h=loft('宽楔形头颅',headsec,HEAD,n=28)
# Reproject the forward facing shell from the front reference, preserving real volume.
for f in h['faces'].values():
    points=[h['vertices'][k] for k in f['vertices']];normal=norm(cross(sub(points[1],points[0]),sub(points[2],points[0])))
    avg_u=sum(uv(v)[0] for v in points)/len(points)
    if avg_u<545:
        for key in f['vertices']:
            pt=h['vertices'][key];fu,fv=frontuv(pt);f['uv'][key]=[fu,310+(fv-340)*1.6]
    else:
        f['texture']=1
        f['uv']={key:[256+h['vertices'][key][0]*5,140+h['vertices'][key][2]*2] for key in f['vertices']}
jaw=loft('银色下颌',[(468,364,375,27),(492,369,391,45),(525,377,405,56),(566,389,416,58),(606,400,424,53)],HEAD,n=20)
for f in jaw['faces'].values():
    for key,(u,v) in f['uv'].items():f['uv'][key]=[216+(u-468)*.18,352+(v-364)*.19]
E.remove(jaw);HEAD['children'].remove(jaw['uuid'])
# Broad chest scutes facing the front view.
for i in range(5):
    v=377+i*17;u=550+i*12;wide=60+math.sin(i/4*math.pi)*19-i*4
    pts=[w(u,v,-wide),w(u-8,v+9,0),w(u,v,wide),w(u+7,v+18,wide*.91),w(u-1,v+27,0),w(u+7,v+18,-wide*.91)]
    puv=[[240-wide,v],[240,v+9],[240+wide,v],[240+wide*.91,v+18],[240,v+27],[240-wide*.91,v+18]]
    sc=mesh('胸甲_%02d'%i,pts,[[0,1,4,5],[1,2,3,4]],BODY,uvs=[[puv[q] for q in [0,1,4,5]],[puv[q] for q in [1,2,3,4]]])
    for f in sc['faces'].values():
        f['texture']=1
        for key in f['uv']:
            p=sc['vertices'][key];f['uv'][key]=[256+p[0]*14,530+(600-p[1]*S-v)*6]

# Full-volume profile extrusion for fins, hands and other irregular contours.
def contour(name,outline,parent,centerx=0,thick=4,uvoutline=None,bulge=0):
    uvoutline=uvoutline or outline
    n=len(outline);vs=[];ps=[];us=[]
    def area(a,b,c):return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    signed=sum(outline[i][0]*outline[(i+1)%n][1]-outline[(i+1)%n][0]*outline[i][1] for i in range(n))
    sign=1 if signed>0 else -1;rest=list(range(n));tris=[]
    while len(rest)>3:
        found=False
        for j in range(len(rest)):
            a,b,c=rest[j-1],rest[j],rest[(j+1)%len(rest)]
            if sign*area(outline[a],outline[b],outline[c])<1e-7:continue
            def inside(q):
                return min(sign*area(outline[a],outline[b],outline[q]),sign*area(outline[b],outline[c],outline[q]),sign*area(outline[c],outline[a],outline[q]))>=-1e-7
            if any(inside(q) for q in rest if q not in [a,b,c]):continue
            tris.append([a,b,c]);rest.pop(j);found=True;break
        if not found:
            tris.extend([[rest[0],rest[j],rest[j+1]] for j in range(1,len(rest)-1)]);rest=[];break
    if len(rest)==3:tris.append(rest)
    for side in (-1,1):
        base=len(vs);vs.extend([w(u,v,centerx+side*thick/2) for u,v in outline])
        for tri in tris:
            order=tri if side*sign>0 else list(reversed(tri))
            ps.append([base+i for i in order]);us.append([uvoutline[i] for i in order])
    for k in range(n):
        ps.append([k,(k+1)%n,n+(k+1)%n,n+k]);us.append([uvoutline[k],uvoutline[(k+1)%n],uvoutline[(k+1)%n],uvoutline[k]])
    return mesh(name,vs,ps,parent,uvs=us)

# Sculpted fan fins: trace the reference edge instead of repeated triangles.
cheekoutline=[(526,340),(559,309),(604,278),(651,262),(637,285),(655,282),(639,304),(646,303),(627,323),(634,323),(615,344),(622,343),(606,361),(611,363),(595,380),(576,371),(550,354)]
for side in (-1,1):
    name='左' if side<0 else '右'
    cg=grp(name+'鳃扇 · 后掠层鳍',HEAD,w(576,329,side*61))
    # x increases towards the back/outside, making a broad swept three-dimensional fan.
    e=contour(name+'鳃扇主体',cheekoutline,cg,side*69,5,bulge=5)
    for key,p in e['vertices'].items():
        u,v=uv(p);p[0]+=side*max(0,u-535)*.14
    # Eyes use reference-aligned polygon surfaces, independent of the skull shell.
    eye_uv=[(500,336),(507,329),(519,327),(515,337),(506,342)]
    eye_verts=[w(u,v,side*(52+(u-500)*.18)) for u,v in eye_uv]
    # Front-visible eye slivers, matching the fierce low brow in front reference.
    front_eye=[[side*31,300],[side*41,286],[side*50,280],[side*48,299],[side*39,310]]
    eyepts=[[(x)/S,(600-v)/S,(495+(v-280)*.1-780)/S] for x,v in front_eye]
    # Four sinuous tapering whiskers, with independent roots and no touching tips.
    for k in range(2):
        bg=grp(name+('内长须' if k==0 else '外短须'),HEAD,w(501,378,side*(24+k*19)))
        raw=[(501+k*9,378),(513+k*9,404),(516+k*6,438),(504+k*8,474),(498+k*11,504),(508+k*11,528),(526+k*11,535)]
        xx=[side*(24+k*19),side*(31+k*20),side*(36+k*23),side*(40+k*24),side*(37+k*25),side*(29+k*25),side*(19+k*28)]
        pts=[w(u,v-(k*10 if j>2 else 0),xx[j]) for j,(u,v) in enumerate(raw)]
        oldr=[.72,.7,.62,.52,.4,.26,.045];sm=[];sr=[]
        for j in range(len(pts)-1):
            p0,p1,p2,p3=pts[max(0,j-1)],pts[j],pts[j+1],pts[min(len(pts)-1,j+2)]
            for q in range(5):
                t=q/5
                sm.append([.5*((2*p1[a])+(-p0[a]+p2[a])*t+(2*p0[a]-5*p1[a]+4*p2[a]-p3[a])*t*t+(-p0[a]+3*p1[a]-3*p2[a]+p3[a])*t*t*t) for a in range(3)])
                sr.append(oldr[j]*(1-t)+oldr[j+1]*t)
        sm.append(pts[-1]);sr.append(oldr[-1])
        tube(name+'弯曲水须_%d'%k,sm,sr,bg,n=9,mapping=lambda p:[89,260])

# Thick limbs with deep palms and long weight-bearing digits.
for side in (-1,1):
    name='左' if side<0 else '右';front=grp(name+'前肢 · 重爪',ROOT,w(673,353,side*110))
    def shoulderuv(p):
        u,v=uv(p);return [704+(u-669)*.55,338+(v-348)*.65]
    ell(name+'饱满肩甲',w(669,348,side*106),[7.8,12.7,10.9],front,shoulderuv)
    def armuv(p):
        u,v=uv(p);return [645+(u-642)*.50,469+(v-478)*.42]
    limb=tube(name+'连续肩臂腕',[w(670,340,side*110),w(680,373,side*120),w(681,410,side*132),w(670,443,side*141),w(650,474,side*151),w(628,504,side*158),w(614,530,side*161),w(603,545,side*162)],[6.3,8.2,8.4,7.7,7.2,6.9,6.9,5.8],front,armuv,n=22)
    reference_limb(limb,side)
    # Hand shape carries the original scale and cyan detail, broadened towards toes.
    def palmuv(p):
        u,v=uv(p);return [642+(u-626)*.28,510+(v-535)*.25]
    pm=ell(name+'厚前掌',w(610,543,side*161),[8.7,4.5,6.4],front,palmuv,n=20,rows=10);atlas_skin(pm,axes=(0,2))
    for k in range(5):
        xx=side*(108+k*27);tipu=557+abs(k-2)*7
        points=[w(613,541,xx),w(590,554,xx+side*4),w(tipu+12,568,xx+side*5)]
        # Individual toes use a clean front-foot strip from the front view.
        def toeuv(p,k=k,side=side):
            a=uv(p);return [691+k*3+(p[0]*S/side-(108+k*27))*.16,350+(a[1]-541)*.22]
        toe=tube(name+'承重趾_%d'%k,points,[2.35,2.18,1.75],front,toeuv,n=10);atlas_skin(toe,axes=(0,2),scale=9)
        cp=[w(tipu+19,563,xx+side*5),w(tipu+2,578,xx+side*5),w(tipu-5,593,xx+side*5)]
        tube(name+'弯钩爪_%d'%k,cp,[1.82,1.3,.07],front,n=8,mapping=lambda p:[53,550])
    forefin=[(675,461),(703,433),(734,412),(750,410),(741,440),(763,431),(752,457),(771,452),(754,487),(738,507),(763,505),(749,531),(727,553),(752,555),(727,568),(678,558),(655,541)]
    contour(name+'前臂外鳍',forefin,front,side*190,5,bulge=3)
    upperfin=[(681,413),(704,383),(725,342),(739,331),(735,369),(747,357),(748,388),(759,386),(749,418),(720,433)]
    contour(name+'肘尖鳍',upperfin,front,side*169,4,bulge=3)
    tube(name+'前臂青色水脉',[w(637,453,side*179),w(617,472,side*188),w(599,494,side*198),w(584,516,side*197)],[.22,.26,.25,.12],front,n=6,mapping=lambda p:[1090,459])
    hind=grp(name+'后肢 · 蹲踞',ROOT,w(873,405,side*100))
    ell(name+'厚实后髋',w(873,402,side*104),[9.5,12.7,10.5],hind,n=24,rows=14)
    tube(name+'后胫',[w(879,439,side*125),w(909,471,side*139),w(929,496,side*147),w(928,525,side*151),w(925,548,side*151)],[7.4,7.2,6.8,6.1,5.8],hind,n=18)
    ell(name+'后掌',w(916,559,side*156),[7.9,3.8,6.4],hind,n=18,rows=9)
    for k in range(4):
        xx=side*(116+k*28);u=875+abs(k-1.5)*6
        tube(name+'后趾_%d'%k,[w(923,554,xx),w(u+12,573,xx),w(u,580,xx)],[2.3,2,1.3],hind,n=9,uvprofile=[(955,543),(955,558),(954,571)])
        tube(name+'后爪_%d'%k,[w(u+7,574,xx),w(u-7,587,xx),w(u-12,597,xx)],[1.6,1.0,.04],hind,n=7,mapping=lambda p:[53,550])
    hindfin=[(919,477),(941,449),(958,426),(976,416),(974,450),(983,445),(985,474),(1000,469),(993,507),(1006,518),(1001,537),(1021,549),(1017,574),(995,580),(957,568),(940,540)]
    contour(name+'后踝鳍',hindfin,hind,side*174,5,bulge=3)

# Gradually tapering curved tail; radius and reference position vary independently.
raw=[(934,402,46),(980,422,47),(1030,440,43),(1080,453,44),(1130,465,42),(1180,487,42),(1220,506,35),(1260,502,34),(1300,475,30),(1335,440,28),(1366,400,25),(1384,358,23),(1394,313,21),(1388,274,18),(1372,242,13),(1348,211,8)]
tp=[w(u,v) for u,v,r in raw];tr=[r/S for u,v,r in raw]
tube('粗长卷尾',tp,tr,TAIL,n=24,aspect=1.2)
# Trace data supplied independently from the same reference.
profiles=json.loads((P/'work'/'fin_profiles.json').read_text(encoding='utf-8'))
for item in profiles['dorsal']:
    contour(item['name'],item['outline'],BACK,0,5,bulge=1.5)
for item in profiles['caudal']:
    if item['name'] in ['caudal_inner_fan','caudal_outer_edge_fan']:
        for side in (-1,1):
            e=contour(('左' if side<0 else '右')+item['name'],item['outline'],TAIL,side*10,5,bulge=2)
            for key,p in e['vertices'].items():
                u,v=uv(p);p[0]+=side*max(0,440-v)*.043
    else:
        contour(item['name'],item['outline'],TAIL,0,9,bulge=2.0)
# Forehead median crest uses the frontal reference on its thick front edge.
crest=[(475,342),(501,301),(529,261),(556,225),(580,198),(603,191),(602,211),(623,195),(618,227),(591,265),(565,291),(524,320)]
contour('额头中央水鳍',crest,HEAD,0,8,bulge=2)

def build():
    dst=P/'玄水妖_参考原图.png'
    if not dst.exists():shutil.copyfile(REF,dst)
    tex={'name':'玄水妖 · 参考原画UV','uuid':uid(),'id':'0','path':str(dst),'width':1448,'height':1086,'uv_width':1448,'uv_height':1086,'render_mode':'default','render_sides':'double','internal':True,'saved':True,'source':'data:image/png;base64,'+base64.b64encode(dst.read_bytes()).decode()}
    at=P/'xuanshui_atlas_v2.png'
    atlas={'name':'玄水妖 · 衔接鳞甲','uuid':uid(),'id':'1','path':str(at),'width':1024,'height':1024,'uv_width':1024,'uv_height':1024,'render_mode':'default','render_sides':'double','internal':True,'saved':True,'source':'data:image/png;base64,'+base64.b64encode(at.read_bytes()).decode()}
    model={'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':'玄水妖','model_identifier':'xuanshui_demon','resolution':{'width':1448,'height':1086},'elements':E,'groups':G,'outliner':O,'textures':[tex,atlas],'animations':[]}
    (P/'玄水妖.bbmodel').write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    report={'name':'玄水妖','revision':'壮硕体态重建','meshes':len(E),'groups':len(G),'vertices':sum(len(e['vertices']) for e in E),'faces':sum(len(e['faces']) for e in E),'texture_size':[1448,1086],'format':'free'}
    (P/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':build()
