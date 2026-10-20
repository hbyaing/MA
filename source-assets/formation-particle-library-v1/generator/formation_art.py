"""Deterministic hard-edged motifs and world-scale formation frames."""
from __future__ import annotations
import math
from PIL import Image,ImageDraw
TAU=math.tau
ROLES=('core','rune','edge','node','spark','shard','dart','arc','sigil','mote','knot','flare')
ROLE_NAMES=dict(zip(ROLES,('阵眼','符文','边界','节点','火花','碎片','飞针','弧片','印记','灵点','结索','星芒')))
STYLES={'five_elements':('五行节点与生克连线',5),'sword':('剑锋、断弧与交错剑轨',7),'sealing':('锁链、方印与封门',8),'teleport':('门扇、刻度与分段轨道',6),'tribulation':('雷叉、放射劫纹与落雷节点',9),'spirit':('灵灯、曲线灵脉与莲叶',6),'alchemy':('鼎足、焰脉与丹丸轨道',3),'heart':('断镜、心结与相对镜面',8),'void':('裂隙、偏心轨道与虚空棱片',7)}
VARIANTS={
 'five_elements':['五行生息','相生环流','五门轮转','逆克星阵'],
 'sword':['七剑归位','双剑回廊','万剑环守','剑潮螺旋'],
 'sealing':['八门封禁','锁链回廊','四方镇印','逆缚囚环'],
 'teleport':['六门跃迁','双轨引渡','折门定位','星轨归程'],
 'tribulation':['九霄落雷','十字劫网','雷环渡劫','螺雷天引'],
 'spirit':['六灯召灵','灵脉回廊','莲瓣引魂','归灵漩涡'],
 'alchemy':['三鼎炼丹','炉脉温养','六火护丹','丹火回旋'],
 'heart':['八镜照心','双心回廊','断镜心牢','心结逆旋'],
 'void':['七隙裂阵','偏轨虚廊','折晶锚阵','虚空坍旋']}
def rgba(h,a=255):return tuple(int(h[i:i+2],16) for i in (1,3,5))+(a,)
def motif(d,f,c,r,a,col,hi,width=3,role='sigil'):
 def p(x,y):return(c[0]+r*(x*math.cos(a)-y*math.sin(a)),c[1]+r*(x*math.sin(a)+y*math.cos(a)))
 def ln(ps,cl=col,w=width):d.line([p(*v) for v in ps],fill=cl,width=max(1,w),joint='curve')
 def poly(ps,fill=col):d.polygon([p(*v) for v in ps],fill=fill);ln(ps+[ps[0]],hi,max(1,width//2))
 if f=='five_elements':
  ps=[(math.cos(i*TAU/5-.5*math.pi)*.85,math.sin(i*TAU/5-.5*math.pi)*.85) for i in range(5)];ln(ps+[ps[0]],col);ln([ps[i] for i in [0,2,4,1,3,0]],hi,max(1,width//2))
  for x,y in ps:q=p(x,y);d.ellipse((q[0]-r*.09,q[1]-r*.09,q[0]+r*.09,q[1]+r*.09),fill=hi)
 elif f=='sword':poly([(0,-1),(.14,-.58),(.09,.36),(.36,.45),(.1,.5),(.07,.88),(-.07,.88),(-.1,.5),(-.36,.45),(-.09,.36),(-.14,-.58)]);ln([(0,-.9),(0,.32)],hi)
 elif f=='sealing':
  for off in [-.38,.38]:ln([(-.22,off-.35),(.22,off-.35),(.35,off),(.22,off+.35),(-.22,off+.35),(-.35,off),(-.22,off-.35)],col);ln([(0,-.25),(0,.25)],hi)
 elif f=='teleport':ln([(-.7,.8),(-.7,-.55),(-.4,-.8),(.4,-.8),(.7,-.55),(.7,.8)],col);ln([(-.28,.45),(.25,0),(-.28,-.45)],hi);ln([(-.9,.9),(-.9,.5)],hi);ln([(.9,.9),(.9,.5)],hi)
 elif f=='tribulation':poly([(.25,-1),(-.5,-.05),(-.05,-.15),(-.35,1),(.65,-.2),(.08,-.04)]);ln([(-.15,-.55),(-.8,-.75),(-.6,-.3)],hi)
 elif f=='spirit':
  poly([(0,-1),(.65,-.25),(.42,.52),(0,.9),(-.42,.52),(-.65,-.25)],col);ln([(0,-.85),(.2,-.1),(0,.65),(-.2,-.1),(0,-.85)],hi);ln([(-.8,.8),(0,.35),(.8,.8)],col)
 elif f=='alchemy':
  ln([(-.7,-.48),(.7,-.48),(.58,.28),(.3,.52),(-.3,.52),(-.58,.28),(-.7,-.48)],col);ln([(-.4,.52),(-.6,.85)],hi);ln([(.4,.52),(.6,.85)],hi);poly([(0,-1),(.27,-.55),(0,-.3),(-.25,-.55)],hi)
 elif f=='heart':poly([(0,-.55),(-.38,-.88),(-.85,-.65),(-.82,-.15),(0,.9),(.82,-.15),(.85,-.65),(.38,-.88)],col);ln([(.08,-.6),(-.17,-.1),(.2,.1),(-.1,.65)],hi)
 elif f=='void':
  poly([(-.8,-.8),(.1,-.64),(-.08,-.08),(-.6,.05)],col);poly([(.35,-.42),(.95,-.2),(.64,.5),(.17,.18)],hi);poly([(-.15,.35),(.52,.8),(-.7,.85)],col)
def render_material(family,role,palette,frame=0,size=256):
 im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im);col=rgba(palette[1]);hi=rgba(palette[4]);s=size/2;c=(s,s);r=s*.80;phase=frame/16*TAU
 def ring(rr,a=0,b=TAU,w=4,cl=col):d.arc((s-rr,s-rr,s+rr,s+rr),math.degrees(a),math.degrees(b),fill=cl,width=w)
 if role in ('core','sigil'):motif(d,family,c,r*(.68 if role=='core' else 1),phase*.2,col,hi,5);ring(r,.2+phase,5.3+phase,4);ring(r*.87,phase+3,phase+4.1,2,hi)
 elif role in ('node','mote'):motif(d,family,c,r*(.65 if role=='node' else .45),phase,col,hi,7);ring(r*.88,phase,phase+1.7,5,hi)
 elif role in ('dart','shard'):motif(d,family,c,r,math.pi/4,col,hi,4);d.line([(s-r*.5,s+r*.7),(s-r*.7,s+r*.95)],fill=col,width=3)
 elif role in ('edge','arc'):ring(r,phase+.35,phase+(5.75 if role=='edge' else 2.3),6);ring(r*.87,phase+.45,phase+2.1,3,hi);motif(d,family,(s+r*.65,s-r*.2),r*.25,phase,col,hi,3)
 elif role in ('rune','knot'):n=STYLES[family][1] if role=='rune' else 3
 if role in ('rune','knot'):
  for i in range(n):a=phase+i*TAU/n;motif(d,family,(s+math.cos(a)*r*.68,s+math.sin(a)*r*.68),r*.20,a,col,hi,2)
  ring(r*.44,phase,phase+TAU-.6,3)
 elif role in ('spark','flare'):
  n=7 if role=='spark' else 4
  for i in range(n):a=i*TAU/n+phase;rr=r*(.25+(i%3)*.23);x=s+math.cos(a)*rr;y=s+math.sin(a)*rr;d.line([(x,y),(x+math.cos(a)*r*.2,y+math.sin(a)*r*.2)],fill=hi,width=4);d.line([(x-3,y),(x+3,y)],fill=col,width=2)
 return im
def make_layer(spec,layer,palette,frame,size=512):
 """All art stays inside radius; transparent holes are retained, no fog fill."""
 im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im);R=spec['radius'];extent=2*(R+.18);scale=size/extent;cx=cy=size/2;role=layer['role'];idx=spec['variant'];fam=spec['family'];col=rgba(palette[1]);hi=rgba(palette[2]);light=rgba(palette[4]);phase=frame/16*TAU*(1 if idx%2==0 else -1);n=STYLES[fam][1];r=layer['radius'];w=max(2,round(scale*.065));
 def pt(a,rr):return(cx+math.cos(a)*rr*scale,cy+math.sin(a)*rr*scale)
 def ln(points,c=col,width=w):d.line(points,fill=c,width=width,joint='curve')
 def arc(rr,a,b,c=col,width=w):d.arc((cx-rr*scale,cy-rr*scale,cx+rr*scale,cy+rr*scale),math.degrees(a),math.degrees(b),fill=c,width=width)
 if role=='edge':
  rr=R-.13
  if idx==1:
   # Parallel corridors differ by family count, with open ends.
   for j in [-1,1]:
    y=j*R*.36;reach=math.sqrt(rr*rr-y*y)*.96;ln([(cx-reach*scale,cy+y*scale),(cx+reach*scale,cy+y*scale)],hi);ln([(cx+y*scale,cy-reach*scale),(cx+y*scale,cy+reach*scale)],col)
  elif idx==2:
   pts=[pt(i*TAU/n,rr*(.74 if i%2 else 1)) for i in range(n)];ln(pts+[pts[0]],hi,w+1)
  else:
   for i in range(n):a=i*TAU/n+phase*.08;arc(rr,a+.055,a+TAU/n-.14,col,w+1);arc(rr*.92,a+.12,a+TAU/n-.19,hi,max(1,w//2))
  for i in range(n):a=i*TAU/n;motif(d,fam,pt(a,rr-.28),scale*.22,a,col,light,max(1,w//2))
 elif role=='rune':
  for i in range(n):
   a=phase*.10+i*TAU/n;rr=R*(.49 if idx==0 else .64 if idx==2 else .54)
   if idx==3:rr=R*(.25+.45*i/max(1,n-1));a+=i*.36
   if fam=='void':rr*=1+.1*math.sin(a);a+=.19
   q=pt(a,rr);motif(d,fam,q,scale*(.30+idx*.035),a+phase*.08,col,light,max(2,w//2))
   if fam in ('sword','tribulation','alchemy'):ln([pt(a,rr*.6),pt(a+.14,rr+.32)],hi,max(1,w//2))
   else:arc(rr,a+.15,a+.55,col,max(1,w//2))
 elif role=='node':
  for i in range(n):
   a=i*TAU/n+(-phase*.06 if idx==3 else 0);rr=R*.78;q=pt(a,rr);motif(d,fam,q,scale*.33,a,col,light,w)
   target=pt(a+(TAU/n if idx in (0,2) else math.pi),rr*(.90 if idx!=3 else .45));ln([q,target],rgba(palette[1],130),max(1,w//2))
 elif role=='core':
  if idx==1:
   for off in [-.85,.85]:motif(d,fam,(cx+off*scale,cy),scale*.60,phase*.06,col,light,w)
  else:motif(d,fam,(cx,cy),scale*.72,phase*.10,col,light,w)
  arc(1.12,phase*.12,phase*.12+5.75,hi,w);arc(.92,-phase*.2,-phase*.2+2.6,col,max(1,w//2))
 elif role=='spark':
  for i in range(14+idx*3):
   a=i*TAU/(14+idx*3)+phase*.23;rr=R*(.26+.57*((i*7)%13)/13);q=pt(a,rr);tail=pt(a-.07,rr-.12);ln([tail,q],light,max(1,w//2));x,y=q;d.ellipse((x-1,y-1,x+1,y+1),fill=light)
 # Clip any corner ornament to the visual extent, independent of impact masks.
 mask=Image.new('L',(size,size));md=ImageDraw.Draw(mask);mr=(R-.015)*scale;md.ellipse((cx-mr,cy-mr,cx+mr,cy+mr),fill=255)
 import PIL.ImageChops as ch
 im.putalpha(ch.multiply(im.getchannel('A'),mask));return im
