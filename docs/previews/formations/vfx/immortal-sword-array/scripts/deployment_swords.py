from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
root=ROOT
src=(root/'scripts/array_swords.py').read_text(encoding='utf8')
src=src.replace("a=-i*math.pi/2;r=d['radius']*1.55;items=[[math.cos(a)*r,-.13,math.sin(a)*r,0,1,0,1]]", "a=-i*math.pi/2;r=d['radius']*1.55;items=[] if d.get('stage',4)==1 else [[math.cos(a)*r,(-.13+max(0.,1-(d['time']-2)/2)**2*12 if d.get('stage',4)==2 else -.13),math.sin(a)*r,0,1,0,1]]")
src=src.replace("if d['curtain']:","if d['curtain'] and d['time']>=9:")
src=src.replace('for j in range(1920):',"for j in range(int(1920*min(1.,max(0.,(d['time']-9)/3)))):")
src=src.replace('for j in range(48):','for j in range(0):')
src=src.replace("tex.use(3);inst.write",'''if i==4 and d.get('stage',4)==4 and d['time']<12:
    for arm in range(4):
     angle=-arm*math.pi/2;origin=np.array([math.cos(angle)*d['radius']*1.55,3.1,math.sin(angle)*d['radius']*1.55])
     for step in range(max(1,int(75*min(1.,max(0.,(d['time']-6)/3))))):
      u=step/74;phase=u*math.pi*3+d['time']*.6
      pos=origin+np.array([(math.cos(phase)-math.cos(d['time']*.6))*(.55*u),u*3.3,(math.sin(phase)-math.sin(d['time']*.6))*(.55*u)])
      tangent=np.array([-math.sin(phase)*2,3.3,math.cos(phase)*2]);tangent/=np.linalg.norm(tangent)
      for lane in [-1,0,1]:items.append([pos[0]+lane*.12,pos[1],pos[2],*tangent,.42*min(1.,max(0.,(12-d['time'])/1.2))])
   if not items:continue
   tex.use(3);inst.write''')
src=src.replace('reserve=2000*28','reserve=4000*28')

ns={};exec(src,ns)
Base=ns['ArraySwords']
class ArraySwords(Base):
 def draw(self,d,paths):
  t=d['time'];d=dict(d);d['stage']=1 if t<2 else 2 if t<4 else 3 if t<6 else 4
  super().draw(d,paths)

