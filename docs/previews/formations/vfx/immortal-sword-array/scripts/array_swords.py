
import json,math
import numpy as np
from PIL import Image
import moderngl
class ArraySwords:
 def __init__(self,ctx,root):
  self.ctx=ctx;lib=root.parents[1]/'library'
  self.p=ctx.program(vertex_shader="""#version 330
in vec3 position;in vec3 normal;in vec2 texcoord;in vec3 center;in vec3 direction;in float size;
uniform mat4 VP;out vec2 uv;out vec3 n;
void main(){vec3 f=normalize(direction);vec3 helper=abs(f.y)>.95?vec3(0,0,1):vec3(0,1,0);vec3 x=normalize(cross(helper,f));vec3 z=cross(x,f);mat3 basis=mat3(x,f,z);vec3 world=center+basis*position*size;uv=texcoord;n=basis*normal;gl_Position=VP*vec4(world,1.);}
""",fragment_shader="""#version 330
in vec2 uv;in vec3 n;uniform sampler2D Atlas;layout(location=0)out vec4 Color;layout(location=1)out vec4 Bright;
void main(){vec4 t=texture(Atlas,uv);if(t.a<.5)discard;float l=.48+.52*abs(dot(normalize(n),normalize(vec3(-.5,.9,.4))));Color=vec4(t.rgb*l,1);Bright=vec4(t.rgb*max(0.,max(t.r,max(t.g,t.b))-.68)*.12,1);}
""");self.models=[]
  for name in ['zhuxian_sword','xianxian_sword','luxian_sword','juexian_sword','artifact_sword_rank_1']:
   data=[]
   if name.startswith('artifact'):
    mesh=json.loads((lib/'models/item'/f'{name}.json').read_text());facemap={'north':[0,3,2,1],'south':[4,5,6,7],'west':[0,4,7,3],'east':[1,2,6,5],'down':[0,1,5,4],'up':[3,7,6,2]}
    for e in mesh['elements']:
     a=e['from'];b=e['to'];v=[[a[0],a[1],a[2]],[b[0],a[1],a[2]],[b[0],b[1],a[2]],[a[0],b[1],a[2]],[a[0],a[1],b[2]],[b[0],a[1],b[2]],[b[0],b[1],b[2]],[a[0],b[1],b[2]]]
     for side,face in e['faces'].items():
      ids=facemap[side];u0,v0,u1,v1=face.get('uv',[0,0,16,16]);uv=[[u0,v1],[u1,v1],[u1,v0],[u0,v0]]
      for tri in [[0,1,2],[0,2,3]]:
       points=np.array([v[ids[j]] for j in tri]);n=np.cross(points[1]-points[0],points[2]-points[0]);n=n/max(np.linalg.norm(n),1e-8)
       for j,p in zip(tri,points):data.extend([(p[0]-8)/24,(p[1]-18)/24,(p[2]-8)/24,*n,uv[j][0]/16,uv[j][1]/16])
    texpath=lib/'textures/item'/f'{name}.png'
   else:
    mesh=json.loads((lib/f'{name}.bbmodel').read_text(encoding='utf8'))
    for e in mesh['elements']:
     for face in e['faces'].values():
      ids=face['vertices']
      for j in range(1,len(ids)-1):
       tri=[ids[0],ids[j],ids[j+1]];points=np.array([e['vertices'][k]for k in tri]);n=np.cross(points[1]-points[0],points[2]-points[0]);n=n/max(np.linalg.norm(n),1e-8)
       for k,p in zip(tri,points):data.extend([p[0]/15,(50-p[1])/15,p[2]/15,*n,face['uv'][k][0]/256,face['uv'][k][1]/256])
    texpath=lib/f'{name}.png'
   im=Image.open(texpath).convert('RGBA');tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);buf=ctx.buffer(np.asarray(data,dtype='f4').tobytes());inst=ctx.buffer(reserve=2000*28);vao=ctx.vertex_array(self.p,[(buf,'3f 3f 2f','position','normal','texcoord'),(inst,'3f 3f 1f /i','center','direction','size')]);self.models.append((tex,buf,inst,vao))
 def draw(self,d,path_data):
  ctx=self.ctx;w,h=d['width'],d['height'];target=np.array([0,.75,0]);dist=d['distance']*max(1,1.25/(w/h));eye=target+np.array([math.sin(d['yaw'])*math.cos(d['pitch']),math.sin(d['pitch']),math.cos(d['yaw'])*math.cos(d['pitch'])])*dist;z=(eye-target)/dist;x=np.cross([0,1,0],z);x/=np.linalg.norm(x);y=np.cross(z,x);v=np.eye(4);v[:3,:3]=[x,y,z];v[:3,3]=-v[:3,:3]@eye;p=np.zeros((4,4));p[0,0]=2/.85/(w/h);p[1,1]=2/.85;p[2,2]=-100.1/99.9;p[2,3]=-20/99.9;p[3,2]=-1;self.p['VP'].write(np.asarray(p@v,dtype='f4').T.tobytes());self.p['Atlas']=3;ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
  for i,(tex,buf,inst,vao)in enumerate(self.models):
   if i<4:
    a=-i*math.pi/2;r=d['radius']*1.55;items=[[math.cos(a)*r,-.13,math.sin(a)*r,0,1,0,1]]
   else:
    items=[]
    if d['curtain']:
     for j in range(1920):
      q=path_data[j*4:j*4+4];f=path_data[7680+j*4:7680+j*4+3];items.append([*q[:3],*f,q[3]*1.5])
    for j in range(48):
     u=(d['time']*d['speed']*.37+j*.0618)%1;a=j*2.39996+math.floor(d['time']*d['speed']*.37)*.15;ri=d['radius']*1.02+.16;ro=max(ri+.12,d['radius']*1.55-1.38);seed=(math.sin(j*127.1+7*311.7)*43758.5453)%1;r=math.sqrt(ri*ri+(ro*ro-ri*ri)*seed);items.append([math.cos(a)*r,6.3*(1-u)+.05*u,math.sin(a)*r,0,-1,0,.55])
   tex.use(3);inst.write(np.asarray(items,dtype='f4').tobytes());vao.render(instances=len(items))
  ctx.disable(moderngl.DEPTH_TEST)
