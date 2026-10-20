"""Surface fitting, consistent scale UVs and final sculpt details."""
import build_sculpt as m
import math,json,copy
original={e['name']:copy.deepcopy(e['faces']) for e in m.E}

def atlas(e,scale=7,axes=(2,1)):
    for f in e['faces'].values():
        f['texture']=1
        mid=[sum(e['vertices'][k][a] for k in f['vertices'])/len(f['vertices']) for a in axes]
        shifts=[math.floor((q*scale+220)/490)*490 for q in mid]
        f['uv']={k:[max(5,min(507,220+e['vertices'][k][axes[0]]*scale-shifts[0])),max(5,min(507,220+e['vertices'][k][axes[1]]*scale-shifts[1]))] for k in f['vertices']}

skin_words=['厚胸收腰','颈下厚胸','饱满肩甲','连续肩臂腕','厚前掌','承重趾','厚实后髋','后胫','后掌','后趾','粗长卷尾']
for e in m.E:
    if any(s in e['name'] for s in skin_words):atlas(e)
    if e['name'] in ['厚胸收腰躯干','粗长卷尾'] or any(s in e['name'] for s in ['厚实后髋','饱满肩甲']):
        e['faces']=copy.deepcopy(original[e['name']])
        for f in e['faces'].values():
            for key in f['uv']:
                p=e['vertices'][key];u,v=m.uv(p)
                if e['name']=='厚胸收腰躯干':
                    sec=min(m.bodysec,key=lambda s:abs(s[0]-u));top,bottom=sec[1],sec[2]
                    t=max(0,1-(v-top)/((bottom-top)*.38))**2
                    f['texture']=0
                    if u<690:f['uv'][key]=[707+(u-630)*.14,286+(v-295)*.50]
                    else:f['uv'][key]=[u+p[0]*m.S*.25*t,v+20*t]
                elif '厚实后髋' in e['name']:f['uv'][key]=[881+(u-873)*.66,405+(v-402)*.72]
                elif '饱满肩甲' in e['name']:f['uv'][key]=[700+(u-669)*.48,337+(v-348)*.52]
    if e['name']=='宽楔形头颅':
        for f in e['faces'].values():
            f['texture']=0
            for key in f['uv']:
                p=e['vertices'][key];u,v=m.uv(p)
                f['uv'][key]=[max(171,min(309,240+p[0]*m.S)),min(369,310+(v-340)*1.6)]
    if '连续肩臂腕' in e['name']:
        for f in e['faces'].values():
            f['texture']=0
            for key in f['uv']:
                p=e['vertices'][key];u,v=m.uv(p)
                f['uv'][key]=[min(164,max(36,94+(abs(p[0])*m.S-150)*.60+(u-650)*.10)),min(535,max(389,403+(v-411)*.91))]

def triangles(es):
    ts=[]
    for e in es:
        for f in e['faces'].values():
            vs=[e['vertices'][k] for k in f['vertices']]
            ts.extend([[vs[0],vs[j],vs[j+1]] for j in range(1,len(vs)-1)])
    return ts
def hit(ts,a,b,axes=(0,1),rayaxis=2,extreme=min):
    values=[]
    for tri in ts:
        q=[[v[i] for i in axes] for v in tri]
        den=(q[1][1]-q[2][1])*(q[0][0]-q[2][0])+(q[2][0]-q[1][0])*(q[0][1]-q[2][1])
        if abs(den)<1e-9:continue
        f=((q[1][1]-q[2][1])*(a-q[2][0])+(q[2][0]-q[1][0])*(b-q[2][1]))/den
        g=((q[2][1]-q[0][1])*(a-q[2][0])+(q[0][0]-q[2][0])*(b-q[2][1]))/den
        h=1-f-g
        if min(f,g,h)>=-1e-6:values.append(sum(tri[j][rayaxis]*[f,g,h][j] for j in range(3)))
    return extreme(values) if values else None

# Fit the throat scales to the real chest surface along the front-view ray.
chest_tri=triangles([e for e in m.E if any(s in e['name'] for s in ['颈下厚胸','厚胸收腰'])])
for e in m.E:
    if e['name'].startswith('胸甲_'):
        for p in e['vertices'].values():
            z=hit(chest_tri,p[0],p[1])
            if z is not None:p[2]=z-.09

# Replace broad, repeated plate faces with a gently convex fitted continuous chest.
for e in list(m.E):
    if e['name'].startswith('胸甲_'):
        m.E.remove(e);m.BODY['children'].remove(e['uuid'])
for row in range(6):
    vs=[];ps=[];uvs=[]
    widths=[52,58,63,62,54,39,18]
    for j in range(5):
        v=366+row*16+j*4.1
        width=widths[row]*(1-j/4)+widths[row+1]*j/4
        for k in range(13):
            x=(k/6-1)*width;vv=v+(1-abs(k/6-1))*5
            z=hit(chest_tri,x/m.S,(600-vv)/m.S)
            if z is None:z=(561+row*9)/m.S-780/m.S
            vs.append([x/m.S,(600-vv)/m.S,z-.20])
    for j in range(4):
        for k in range(12):
            ids=[j*13+k,j*13+k+1,(j+1)*13+k+1,(j+1)*13+k];ps.append(ids)
            uvs.append([[40+k*35,519+j*14],[40+(k+1)*35,519+j*14],[40+(k+1)*35,533+j*14],[40+k*35,533+j*14]])
    m.mesh('贴体弧面胸甲_%d'%row,vs,ps,m.BODY,uvs=uvs,tex=1)

# Shoulder scute contours follow the bulk, not separate circular armor discs.
# Cyan channels follow the mesh surface and keep a constant narrow thickness.
def trace_side(name,raw,objects,parent,side,width=.20):
    ts=triangles(objects);path=[]
    for u,v in raw:
        p=m.w(u,v)
        x=hit(ts,p[1],p[2],axes=(1,2),rayaxis=0,extreme=max if side>0 else min)
        if x is None:continue
        path.append([x+side*.12,p[1],p[2]])
    if len(path)>1:
        e=m.tube(name,path,[width]*len(path),parent,n=6)
        for f in e['faces'].values():f['texture']=1;f['uv']={k:[160,832] for k in f['vertices']}

body=[e for e in m.E if e['name']=='厚胸收腰躯干']
tails=[e for e in m.E if e['name']=='粗长卷尾']
for side in (-1,1):
    label='左' if side<0 else '右'
    parts=[e for e in m.E if e['name'].startswith(label) and any(s in e['name'] for s in ['饱满肩甲','连续肩臂腕'])]
    parts=[e for e in m.E if e['name'].startswith(label) and '厚实后髋' in e['name']]
    # Front outer forearm streak follows the anterior face at each height.
    parts=[e for e in m.E if e['name'].startswith(label) and '连续肩臂腕' in e['name']]
    ts=triangles(parts);path=[]
    for xx,v in [(135,421),(145,433),(158,447),(168,460),(176,478),(177,494),(177,512)]:
        p=m.w(620,v,xx*side);z=hit(ts,p[0],p[1])
        if z is not None:path.append([p[0],p[1],z-.14])

# Remove stray hidden stripe remnants from the rough pass.
for e in list(m.E):
    if '前臂青色水脉' in e['name']:
        m.E.remove(e)
        def remove(o):
            if e['uuid'] in o['children']:o['children'].remove(e['uuid'])
            for c in o['children']:
                if isinstance(c,dict):remove(c)
        remove(m.ROOT)

for e in m.E:
    if e['name']=='宽楔形头颅' or '鳃扇主体' in e['name']:
        for p in e['vertices'].values():
            v=600-p[1]*m.S
            p[1]=(600-(358+(v-358)*1.10))/m.S

m.build()
