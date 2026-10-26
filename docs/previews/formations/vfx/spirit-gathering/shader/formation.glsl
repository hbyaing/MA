// Original spirit-gathering formation preview. World units: blocks; deterministic seconds.
uniform vec2 Resolution;
uniform sampler2D FlowNoise;
uniform float Time,Power,Speed,Radius,Density,Yaw,Pitch,Distance,Reveal;
uniform int Palette,Steps;
const float PI=3.14159265359;
float h21(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float n3(vec3 p){vec2 uv=p.xz*.083+vec2(p.y*.071,p.y*.053);return texture(FlowNoise,uv).g*.65+texture(FlowNoise,uv*2.03+vec2(.27,.61)).r*.35;}
vec3 element(int i){if(Palette==1)return vec3(.28,.93,.79);if(Palette==2)return vec3(1.,.72,.25);if(i==0)return vec3(1.,.80,.36);if(i==1)return vec3(.29,.92,.55);if(i==2)return vec3(.24,.67,1.);if(i==3)return vec3(1.,.33,.16);return vec3(.89,.59,.27);}
vec2 node(int i){float a=float(i)*PI*.4+PI*.5;return vec2(cos(a),sin(a))*Radius*.86;}
float band(float d,float w){return exp(-abs(d)/max(w,.0001));}
float segment(vec2 p,vec2 a,vec2 b){vec2 v=b-a;return length(p-a-v*clamp(dot(p-a,v)/dot(v,v),0.,1.));}
vec2 boxHit(vec3 ro,vec3 rd,vec3 lo,vec3 hi){vec3 inv=1./(rd+vec3(.000001));vec3 a=(lo-ro)*inv,b=(hi-ro)*inv;vec3 n=min(a,b),f=max(a,b);return vec2(max(max(n.x,n.y),n.z),min(min(f.x,f.y),f.z));}
float sphereHit(vec3 ro,vec3 rd,vec3 center,float radius){vec3 q=ro-center;float b=dot(q,rd),d=b*b-dot(q,q)+radius*radius;return d<0.?1e4:-b-sqrt(d);}
vec3 stoneNormal(vec3 q,vec3 halfSize){vec3 d=abs(q/halfSize);if(d.y>max(d.x,d.z))return vec3(0,sign(q.y),0);if(d.x>d.z)return vec3(sign(q.x),0,0);return vec3(0,0,sign(q.z));}
vec3 floorSurface(vec3 p,out vec3 glow){
 vec2 q=p.xz;float r=length(q),rn=r/Radius,a=atan(q.y,q.x),t=Time*Speed;
 vec2 grid=abs(fract(q*.5-.5)-.5)/max(fwidth(q*.5),vec2(.001));float line=1.-min(min(grid.x,grid.y),1.);
 vec3 base=mix(vec3(.026,.044,.047),vec3(.038,.061,.063),line*.5);
 float disk=1.-smoothstep(.96,1.02,rn);base=mix(base,vec3(.045,.075,.076),disk*.55);
 float aa=max(fwidth(rn),.0005);float seals=band(rn-.97,.002+aa*.25)+band(rn-.91,.0014+aa*.25)+band(rn-.73,.0013+aa*.25)+band(rn-.68,.0013+aa*.25)+band(rn-.21,.002+aa*.3)+band(rn-.18,.001+aa*.25);
 float marks=step(.68,.5+.5*cos(a*80.))*band(rn-.94,.006);float innerTicks=step(.83,.5+.5*cos((a+t*.018)*50.))*band(rn-.705,.011);
 seals+=marks*.65+innerTicks*.7;
 float links=0.,streams=0.;vec3 colored=vec3(0.);
 for(int i=0;i<5;i++){
  vec2 nn=node(i),unitN=normalize(nn);float d=segment(q,nn*.82,vec2(0));float travel=pow(.5+.5*sin(length(q-nn)*3.8-t*2.3+float(i)*1.2),9.);float channel=band(d,.018);
  links+=channel*.26+band(segment(q,nn*.78,node((i+2)%5)*.78),.012)*.21;
  streams+=channel*travel*.9;
  float nr=length(q-nn);float circle=band(nr-.36,.012)+band(nr-.44,.007);colored+=element(i)*(circle*.8+exp(-nr*nr*5.)*.08);
  // Short seal strokes are geometric talismans, not pseudo-text painted over the scene.
  vec2 uv=vec2(dot(q-unitN*Radius*.8,unitN),dot(q,vec2(-unitN.y,unitN.x)));float rune=band(uv.y,.008)*step(abs(uv.x),.12)+band(abs(uv.x)-.08,.008)*step(abs(uv.y),.11);colored+=element(i)*rune*.35;
 }
 float trigrams=0.;
 for(int i=0;i<8;i++){float angle=float(i)*PI*.25+PI*.125;vec2 axis=vec2(cos(angle),sin(angle)),tangent=vec2(-axis.y,axis.x);vec2 local=q-axis*Radius*.81;float x=dot(local,tangent),y=dot(local,axis);for(int row=0;row<3;row++){float solidLine=mod(floor(float(i)/pow(2.,float(row))),2.);float gap=mix(smoothstep(.022,.042,abs(x)),1.,solidLine);trigrams+=band(y-(float(row)-1.)*.085,.009)*(1.-smoothstep(.15,.17,abs(x)))*gap;}}
 colored+=vec3(.86,.68,.34)*trigrams*.55;
 float center=band(rn-.1,.0015)+band(rn-.14,.0015)*(.5+.5*cos(a*8.-t*.18));
 float activation=Power*Reveal;
 glow=(vec3(.37,.91,.77)*(seals*.58+links*.50+center*.8)+vec3(.97,.78,.39)*(marks*.25+innerTicks*.25+streams*.65)+colored*.72)*activation;
 return base+glow;
}
float gasField(vec3 p,out vec3 color){
 float r=length(p.xz),y=p.y,t=Time*Speed;float a=atan(p.z,p.x);
 color=vec3(.34,.85,.75);if(y<0.||y>4.4||r>Radius+1.)return 0.;
 // Incoming wisps follow spiral lanes near the array, with packets travelling toward its center.
 float outerMask=smoothstep(.3,.9,r)*(1.-smoothstep(Radius*.88,Radius+1.,r));
 float lane=pow(.5+.5*cos(a*5.+r*.92+t*.24),11.);
 float height=.22+.13*sin(a*3.-r*1.2+t*.4);
 float mist=exp(-pow((y-height)/.16,2.))*outerMask;
 float adv=n3(vec3(p.x+sin(a)*.2,p.y-t*.3,p.z+cos(a)*.2));
 float packets=.38+.62*pow(.5+.5*sin(r*3.5+t*1.6+adv*2.4),3.);
 float gather=mist*(.025+lane*.60)*packets*(.25+adv*.8);
 // Helical annular gas is sampled in 3D, not a camera-facing swirl sprite.
 float rise=clamp(y/4.,0.,1.),columnR=.40+.17*y;
 float twist=a*2.-y*3.8+t*.85+adv*3.;
 float fold=.55+.45*sin(twist);float shell=exp(-pow((r-columnR-.14*sin(twist))/.25,2.));
 float topFade=1.-smoothstep(2.2,4.2,y);float bottomFade=smoothstep(.12,.55,y);
 float column=shell*(.10+.30*fold)*(.25+adv)*topFade*bottomFade;
 float core=exp(-r*r*7.)*exp(-y*1.8)*.09;
 color=mix(vec3(.20,.68,.61),vec3(.74,.95,.81),rise*.65+fold*.15);
 if(Palette==2)color=mix(vec3(.82,.55,.20),vec3(1.,.91,.66),rise*.6+fold*.2);
 return (gather+column+core)*Density*Power*Reveal;
}
void formation(vec2 uv,out vec3 scene,out vec3 emission){
 vec3 target=vec3(0,.75,0);float cd=Distance*max(1.,1.25/(Resolution.x/Resolution.y));vec3 ro=target+vec3(sin(Yaw)*cos(Pitch),sin(Pitch),cos(Yaw)*cos(Pitch))*cd;
 vec3 forward=normalize(target-ro),right=normalize(cross(forward,vec3(0,1,0))),up=cross(right,forward);vec2 screen=(uv-.5)*vec2(Resolution.x/Resolution.y,1.);vec3 rd=normalize(forward+right*screen.x*.85+up*screen.y*.85);
 scene=mix(vec3(.015,.030,.034),vec3(.035,.061,.061),clamp(uv.y,0.,1.));emission=vec3(0.);float solid=1e4;int hit=-1;vec3 hitCenter=vec3(0),halfSize=vec3(.19,.30,.19);bool crystal=false;
 if(rd.y<-.0001){solid=-ro.y/rd.y;vec3 point=ro+rd*solid;scene=floorSurface(point,emission);float fog=1.-exp(-solid*.027);scene=mix(scene,vec3(.020,.041,.045),fog*.65);emission*=1.-fog*.45;}
 for(int i=0;i<5;i++){
  vec2 n=node(i);vec3 c=vec3(n.x,.30,n.y);vec2 h=boxHit(ro,rd,c-halfSize,c+halfSize);
  if(h.x>0.&&h.x<h.y&&h.x<solid){solid=h.x;hit=i;hitCenter=c;crystal=false;}
  float ball=sphereHit(ro,rd,vec3(n.x,.80,n.y),.15);if(ball>0.&&ball<solid){solid=ball;hit=i;hitCenter=vec3(n.x,.80,n.y);crystal=true;}
 }
 // Low central jade dais anchors the vortex in world space.
 vec3 hc=vec3(0,.10,0),hs=vec3(.57,.10,.57);vec2 dh=boxHit(ro,rd,hc-hs,hc+hs);
 if(dh.x>0.&&dh.x<dh.y&&dh.x<solid){solid=dh.x;hit=5;hitCenter=hc;crystal=false;halfSize=hs;}
 if(hit>=0){vec3 pos=ro+rd*solid,q=pos-hitCenter;vec3 normal=crystal?normalize(q):stoneNormal(q,halfSize);float light=.32+.68*max(0.,dot(normal,normalize(vec3(-.5,.9,.4))));
  scene=vec3(.075,.12,.12)*light;emission=vec3(0.);
  if(crystal){float rim=pow(1.-max(0.,dot(normal,-rd)),2.);scene=element(hit)*(.13+.55*light);emission=element(hit)*(.38+rim*.7)*Power*Reveal;scene+=emission;}
  else{vec3 c=abs(q/halfSize);float edge=step(.84,min(max(c.x,c.z),max(c.y,c.z)));float stripe=1.-smoothstep(.015,.026,abs(fract((q.y+.3)*5.)-.5));scene+=vec3(.28,.23,.13)*edge*.25;emission=(hit==5?vec3(.37,.91,.77):element(hit))*stripe*.10*Power*Reveal;scene+=emission;}
 }
 vec2 bound=boxHit(ro,rd,vec3(-Radius-1.,.02,-Radius-1.),vec3(Radius+1.,4.4,Radius+1.));float begin=max(.02,bound.x),end=min(solid,bound.y);
 if(end>begin){float stepSize=(end-begin)/float(Steps);float trans=1.;vec3 gas=vec3(0.);
  for(int i=0;i<64;i++){if(i>=Steps)break;float dist=begin+(float(i)+.5)*stepSize;vec3 p=ro+rd*dist,col;float rho=gasField(p,col);float alpha=1.-exp(-rho*stepSize*1.8);gas+=col*alpha*trans*1.12;trans*=1.-alpha;if(trans<.025)break;}
  scene=scene*trans+gas;emission=emission*trans+gas*.55;
 }
 // Deterministic particles converge inward, then rise out of the array eye.
 for(int i=0;i<55;i++){float seed=h21(vec2(float(i),13.)),u=fract(Time*Speed*(.075+seed*.035)+seed);float a=float(i)*2.39996+u*1.5,r=Radius*pow(1.-u,1.25);vec3 p=vec3(cos(a)*r,.18+pow(u,5.)*3.6+sin(u*12.+seed*9.)*.06,sin(a)*r);vec3 q=p-ro;float along=dot(q,rd);if(along<0.||along>solid)continue;vec3 near=q-rd*along;float s=.012+.016*seed;float dotLight=exp(-dot(near,near)/(s*s));vec3 col=Palette==0?element(i%5):element(0);vec3 spark=col*dotLight*(.6+.7*seed)*Power*Reveal;scene+=spark;emission+=spark;}
}
