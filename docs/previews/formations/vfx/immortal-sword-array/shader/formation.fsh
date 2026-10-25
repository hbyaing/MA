#version 150
in vec2 uv;out vec4 FragColor;out vec4 BrightColor;
// Original spirit-gathering formation preview. World units: blocks; deterministic seconds.
uniform vec2 Resolution;
uniform sampler2D FlowNoise;
uniform sampler2D SwordTexture;
uniform sampler2D SwordPaths;
uniform float Time,Power,Speed,Radius,Density,Yaw,Pitch,Distance,Reveal;
uniform int Palette,Steps;
uniform int Curtain;
const float PI=3.14159265359;
float h21(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float n3(vec3 p){vec2 uv=p.xz*.083+vec2(p.y*.071,p.y*.053);return texture(FlowNoise,uv).g*.65+texture(FlowNoise,uv*2.03+vec2(.27,.61)).r*.35;}
vec3 element(int i){if(Palette==1)return vec3(.95,.075,.035);if(Palette==2)return vec3(.78,.32,1.);if(i%4==0)return vec3(1.,.13,.055);if(i%4==1)return vec3(.76,.22,1.);if(i%4==2)return vec3(1.,.69,.20);return vec3(.25,.72,1.);}
vec2 node(int i){float a=float(i)*PI*.5+PI*.25;return vec2(cos(a),sin(a))*Radius*1.55;}
float band(float d,float w){return exp(-abs(d)/max(w,.0001));}
float segment(vec2 p,vec2 a,vec2 b){vec2 v=b-a;return length(p-a-v*clamp(dot(p-a,v)/dot(v,v),0.,1.));}
vec2 boxHit(vec3 ro,vec3 rd,vec3 lo,vec3 hi){vec3 inv=1./(rd+vec3(.000001));vec3 a=(lo-ro)*inv,b=(hi-ro)*inv;vec3 n=min(a,b),f=max(a,b);return vec2(max(max(n.x,n.y),n.z),min(min(f.x,f.y),f.z));}
float sphereHit(vec3 ro,vec3 rd,vec3 center,float radius){vec3 q=ro-center;float b=dot(q,rd),d=b*b-dot(q,q)+radius*radius;return d<0.?1e4:-b-sqrt(d);}
vec3 stoneNormal(vec3 q,vec3 halfSize){vec3 d=abs(q/halfSize);if(d.y>max(d.x,d.z))return vec3(0,sign(q.y),0);if(d.x>d.z)return vec3(sign(q.x),0,0);return vec3(0,0,sign(q.z));}
vec3 floorSurface(vec3 p,out vec3 glow){
 vec2 q=p.xz;float r=length(q),rn=r/Radius,a=atan(q.y,q.x),t=Time*Speed;
 vec2 grid=abs(fract(q*.5-.5)-.5)/max(fwidth(q*.5),vec2(.001));float line=1.-min(min(grid.x,grid.y),1.);
 vec3 base=mix(vec3(.020,.012,.018),vec3(.038,.020,.027),line*.5);
 float disk=1.-smoothstep(.96,1.02,rn);base=mix(base,vec3(.033,.013,.023),disk*.55);
 float aa=max(fwidth(rn),.0005);float seals=band(rn-.97,.002+aa*.25)+band(rn-.91,.0014+aa*.25)+band(rn-.73,.0013+aa*.25)+band(rn-.68,.0013+aa*.25)+band(rn-.21,.002+aa*.3)+band(rn-.18,.001+aa*.25);
 float marks=step(.68,.5+.5*cos(a*80.))*band(rn-.94,.006);float innerTicks=step(.83,.5+.5*cos((a+t*.018)*50.))*band(rn-.705,.011);
 seals+=marks*.65+innerTicks*.7;
 float links=0.,streams=0.;vec3 colored=vec3(0.);
 for(int i=0;i<4;i++){
  vec2 nn=node(i),unitN=normalize(nn);float d=segment(q,nn*.82,vec2(0));float travel=pow(.5+.5*sin(length(q-nn)*3.8-t*2.3+float(i)*1.2),9.);float channel=band(d,.018);
  links+=channel*.26+band(segment(q,nn*.78,node((i+1)%4)*.78),.012)*.21;
  streams+=channel*travel*.9;
  float nr=length(q-nn);float circle=band(nr-.36,.012)+band(nr-.44,.007);colored+=element(i)*(circle*.8+exp(-nr*nr*5.)*.08);
  // Short seal strokes are geometric talismans, not pseudo-text painted over the scene.
  vec2 uv=vec2(dot(q-unitN*Radius*.8,unitN),dot(q,vec2(-unitN.y,unitN.x)));float rune=band(uv.y,.008)*step(abs(uv.x),.12)+band(abs(uv.x)-.08,.008)*step(abs(uv.y),.11);colored+=element(i)*rune*.35;
 }
 float trigrams=0.;
 for(int i=0;i<8;i++){float angle=float(i)*PI*.25+PI*.125;vec2 axis=vec2(cos(angle),sin(angle)),tangent=vec2(-axis.y,axis.x);vec2 local=q-axis*Radius*.81;float x=dot(local,tangent),y=dot(local,axis);for(int row=0;row<3;row++){float solidLine=mod(floor(float(i)/pow(2.,float(row))),2.);float gap=mix(smoothstep(.022,.042,abs(x)),1.,solidLine);trigrams+=band(y-(float(row)-1.)*.085,.009)*(1.-smoothstep(.15,.17,abs(x)))*gap;}}
 colored+=vec3(.65,.055,.07)*trigrams*.55;
 float center=band(rn-.1,.0015)+band(rn-.14,.0015)*(.5+.5*cos(a*8.-t*.18));
 for(int i=0;i<4;i++){
 vec2 local=q-node(i);float nr=length(local),na=atan(local.y,local.x);
 float ring=band(nr-1.25,.018)+band(nr-1.08,.012)+band(nr-.78,.010);
 float marks=band(nr-1.17,.035)*step(.5,cos(na*32.));
 float spokes=band(segment(local,vec2(-.7,0),vec2(.7,0)),.012)+band(segment(local,vec2(0,-.7),vec2(0,.7)),.012);
 colored+=element(i)*(ring+marks*.55+spokes*.55);
 }
 // Outer enclosing formation clears the entire radius of all four subsidiary seals.
 float outerR=Radius*1.55+2.25;
 float outer=band(r-outerR,.021)+band(r-(outerR-.20),.012)+band(r-(outerR-.45),.010);
 outer+=band(r-(outerR-.10),.035)*step(.52,cos(a*128.))*.65;
 outer+=band(r-(outerR-.33),.025)*pow(.5+.5*cos(a*32.-t*.10),12.)*.7;
 colored+=vec3(.95,.12,.055)*outer;
 // Annular script and circuit veins occupy the space between the main and outer arrays.
 float middle=(Radius+outerR)*.5;
 float midmask=smoothstep(Radius*1.02,Radius*1.12,r)*(1.-smoothstep(outerR-.52,outerR-.30,r));
 float orbit=band(r-middle,.010)+band(r-(middle-.25),.008)+band(r-(middle+.25),.008);
 float symbolAngle=a+t*.012;
 float sector=floor((symbolAngle+PI)/(2.*PI)*40.);
 float localAngle=(fract((symbolAngle+PI)/(2.*PI)*40.)-.5)*(2.*PI/40.);
 vec2 glyphPos=vec2(localAngle*r,(r-middle));
 float rune=band(glyphPos.x,.014)*step(abs(glyphPos.y),.19);
 rune+=band(abs(glyphPos.y)-.14,.012)*step(abs(glyphPos.x),.095);
 rune+=band(glyphPos.y-.025*sin(sector*2.),.011)*step(abs(glyphPos.x),.13);
 rune+=band(abs(glyphPos.x)-.095,.010)*step(abs(glyphPos.y+.08),.07)*step(.4,fract(sector*.37));
 float veins=0.;
 for(int k=0;k<4;k++){
 float angle=float(k)*PI*.5+PI*.25;vec2 axis=vec2(cos(angle),sin(angle)),tanAxis=vec2(-axis.y,axis.x);
 float axial=dot(q,axis),lateral=dot(q,tanAxis);
 float lane=band(abs(lateral)-.22,.014)*smoothstep(Radius*.96,Radius*1.10,axial)*(1.-smoothstep(outerR-.45,outerR-.20,axial));
 float pulse=pow(.5+.5*sin(axial*5.-t*2.),8.);
 veins+=lane*(.45+pulse*.8);
 }
 float lattice=band(sin(a*16.+r*3.),.026)*band(sin(a*16.-r*3.),.20)*.22;
 // Keep the subsidiary discs readable instead of drawing text across their centers.
 float clearance=1.;for(int k=0;k<4;k++)clearance*=smoothstep(1.30,1.52,length(q-node(k)));
 colored+=vec3(.96,.30,.10)*(orbit*.30+rune*.63+lattice)*midmask*clearance;
 colored+=vec3(1.,.43,.16)*veins*clearance;
 // Larger talisman clusters fill the otherwise empty annular panels.
 float panelRunes=0.;
 for(int k=0;k<12;k++){
 float angle=float(k)*PI/6.+PI/12.;vec2 axis=vec2(cos(angle),sin(angle)),tangent=vec2(-axis.y,axis.x);
 vec2 v=q-axis*(middle+.02);vec2 g=vec2(dot(v,tangent),dot(v,axis));
 float extent=min(.43,(outerR-Radius)*.28);
 float body=band(g.x,.014)*step(abs(g.y),extent);
 body+=band(g.y-extent*.68,.013)*step(abs(g.x),.23);
 body+=band(g.y+extent*.56,.013)*step(abs(g.x),.18);
 body+=band(abs(g.x)-.18,.013)*step(abs(g.y-extent*.10),extent*.42);
 body+=band(g.y-g.x*.65,.013)*step(abs(g.x),.26)*step(abs(g.y),extent*.50);
 body+=band(abs(g.x)-.085,.011)*step(abs(g.y+extent*.80),.07);
 float diamond=band(abs(g.x)+abs(g.y-extent-.15)-.13,.011)*step(abs(g.y-extent-.15),.14);
 panelRunes+=body+diamond*.70;
 }
 // Two offset rows of smaller seals create a rhythm between the large talismans.
 for(int row=0;row<2;row++){
 float ringR=mix(Radius+.42,outerR-.72,float(row));
 float aa=a+float(row)*.10,sector=floor((aa+PI)/(2.*PI)*48.);
 vec2 g=vec2((fract((aa+PI)/(2.*PI)*48.)-.5)*(2.*PI/48.)*r,r-ringR);
 float rune=band(g.x,.010)*step(abs(g.y),.11)+band(abs(g.y)-.08,.010)*step(abs(g.x),.075);
 rune+=band(g.y-g.x*.5,.009)*step(abs(g.x),.09)*step(abs(g.y),.09);
 panelRunes+=rune*.48;
 }
 colored+=vec3(1.,.23,.075)*panelRunes*.70*midmask*clearance;
 float activation=Power*Reveal;
 glow=(vec3(.80,.035,.07)*(seals*.58+links*.50+center*.8)+vec3(1.,.18,.065)*(marks*.25+innerTicks*.25+streams*.65)+colored*.72)*activation;
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
 color=vec3(.07,.012,.025); color=vec3(.55,.08,.055);return gather*.18*Density*Power*Reveal;
}

// Solid pixel extrusion: traverse the texture's occupied cells through a finite thickness.
// Front, back and every alpha silhouette edge are closed surfaces, including the guard and grip.
void swordRay(vec3 ro,vec3 rd,vec3 center,vec3 axis,float size,vec3 tint,inout float closest,inout vec3 color,inout vec3 glow){
 vec3 yy=normalize(axis),xx=normalize(cross(abs(yy.y)>.95?vec3(0,0,1):vec3(0,1,0),yy)),zz=cross(xx,yy);
 vec3 r=vec3(dot(ro-center,xx),dot(ro-center,yy),dot(ro-center,zz))/size;
 vec3 d=vec3(dot(rd,xx),dot(rd,yy),dot(rd,zz))/size;
 vec3 o=vec3(.5+(r.y+r.x)*.353553,.5-(r.y-r.x)*.353553,r.z);
 vec3 v=vec3((d.y+d.x)*.353553,-(d.y-d.x)*.353553,d.z);
 float thick=.075;vec2 bounds=boxHit(o,v,vec3(0,0,-thick),vec3(1,1,thick));
 float t=max(0.,bounds.x),end=min(closest,bounds.y);if(end<=t)return;
 float light=abs(v.z)>.00001&&abs(abs((o+v*t).z)-thick)<.0001?.95:.64;
 ivec2 dims=textureSize(SwordTexture,0);
 for(int stepIndex=0;stepIndex<36;stepIndex++){
 vec3 q=o+v*(t+.00001);ivec2 cell=ivec2(floor(q.xy*vec2(dims)));
 if(any(lessThan(cell,ivec2(0)))||any(greaterThanEqual(cell,dims)))break;
 vec4 texel=texelFetch(SwordTexture,cell,0);
 if(texel.a>.5){closest=t;glow=texel.rgb*.055*Power*Reveal;color=texel.rgb*light+glow;break;}
 vec2 boundary=(vec2(cell)+step(vec2(0),v.xy))/vec2(dims);
 vec2 next=vec2(1e10);if(abs(v.x)>.0000001)next.x=(boundary.x-o.x)/v.x;if(abs(v.y)>.0000001)next.y=(boundary.y-o.y)/v.y;
 float advance=min(next.x,next.y);if(advance>=end||advance<t-.00001)break;t=advance;light=next.x<next.y?.62:.78;
 }
}
void formation(vec2 uv,out vec3 scene,out vec3 emission){
 vec3 target=vec3(0,.75,0);float cd=Distance*max(1.,1.25/(Resolution.x/Resolution.y));vec3 ro=target+vec3(sin(Yaw)*cos(Pitch),sin(Pitch),cos(Yaw)*cos(Pitch))*cd;
 vec3 forward=normalize(target-ro),right=normalize(cross(forward,vec3(0,1,0))),up=cross(right,forward);vec2 screen=(uv-.5)*vec2(Resolution.x/Resolution.y,1.);vec3 rd=normalize(forward+right*screen.x*.85+up*screen.y*.85);
 scene=mix(vec3(.012,.007,.016),vec3(.033,.014,.027),clamp(uv.y,0.,1.));emission=vec3(0.);float solid=1e4;int hit=-1;vec3 hitCenter=vec3(0),halfSize=vec3(.19,.30,.19);bool crystal=false;
 if(rd.y<-.0001){solid=-ro.y/rd.y;vec3 point=ro+rd*solid;scene=floorSurface(point,emission);float fog=1.-exp(-solid*.027);scene=mix(scene,vec3(.026,.012,.021),fog*.65);emission*=1.-fog*.45;}
 // Low central jade dais anchors the vortex in world space.
 vec3 hc=vec3(0,.10,0),hs=vec3(.57,.10,.57);vec2 dh=boxHit(ro,rd,hc-hs,hc+hs);
 if(dh.x>0.&&dh.x<dh.y&&dh.x<solid){solid=dh.x;hit=5;hitCenter=hc;crystal=false;halfSize=hs;}
 if(hit>=0){vec3 pos=ro+rd*solid,q=pos-hitCenter;vec3 normal=crystal?normalize(q):stoneNormal(q,halfSize);float light=.32+.68*max(0.,dot(normal,normalize(vec3(-.5,.9,.4))));
  scene=vec3(.045,.025,.035)*light;emission=vec3(0.);
  if(crystal){float rim=pow(1.-max(0.,dot(normal,-rd)),2.);scene=element(hit)*(.13+.55*light);emission=element(hit)*(.38+rim*.7)*Power*Reveal;scene+=emission;}
  else{vec3 c=abs(q/halfSize);float edge=step(.84,min(max(c.x,c.z),max(c.y,c.z)));float stripe=1.-smoothstep(.015,.026,abs(fract((q.y+.3)*5.)-.5));scene+=vec3(.28,.23,.13)*edge*.25;emission=(hit==5?vec3(.80,.035,.07):element(hit))*stripe*.10*Power*Reveal;scene+=emission;}
 }

 for(int i=0;i<4;i++){vec2 nn=node(i);vec3 col=scene,em=emission;float before=solid;
 swordRay(ro,rd,vec3(nn.x,1.75,nn.y),vec3(0,-1,0),1.8,element(i),solid,col,em);
 if(solid<before){scene=col;emission=em;}}
 // Direct original JS module results: count(longyuan,0)=384 currents, five swords each.
 for(int i=0;i<1920;i++){
 if(Curtain==0)break;
 vec4 point=texelFetch(SwordPaths,ivec2(i,0),0);vec3 center=point.xyz,f=texelFetch(SwordPaths,ivec2(i,1),0).xyz;
 vec3 delta=center-ro;float along=dot(delta,rd);if(along<0.||along>solid+.5||length(delta-rd*along)>point.w*1.6)continue;
 float before=solid;vec3 col=scene,em=emission;swordRay(ro,rd,center,f,point.w*.82,vec3(.7,.9,1.),solid,col,em);
 if(solid<before){scene=col;emission=em;}
 }
 // Rain occupies the continuous annulus outside the main dome and inside the subsidiary centers.
 for(int i=0;i<48;i++){
 float u=fract(Time*Speed*.37+float(i)*.0618),a=float(i)*2.39996+floor(Time*Speed*.37)*.15;
 float innerR=Radius*1.02+.16,outerRainR=Radius*1.55-1.38;
 outerRainR=max(innerR+.12,outerRainR);
 float rr=sqrt(mix(innerR*innerR,outerRainR*outerRainR,h21(vec2(float(i),7.))));
 vec3 c=vec3(cos(a)*rr,mix(6.3,.05,u),sin(a)*rr);
 float before=solid;vec3 col=scene,em=emission;swordRay(ro,rd,c,vec3(0,-1,0),.32,element(i%4),solid,col,em);
 if(solid<before){scene=col;emission=em;}
 }
 vec2 bound=boxHit(ro,rd,vec3(-Radius-1.,.02,-Radius-1.),vec3(Radius+1.,4.4,Radius+1.));float begin=max(.02,bound.x),end=min(solid,bound.y);
 if(end>begin){float stepSize=(end-begin)/float(Steps);float trans=1.;vec3 gas=vec3(0.);
  for(int i=0;i<64;i++){if(i>=Steps)break;float dist=begin+(float(i)+.5)*stepSize;vec3 p=ro+rd*dist,col;float rho=gasField(p,col);float alpha=1.-exp(-rho*stepSize*1.8);gas+=col*alpha*trans*1.12;trans*=1.-alpha;if(trans<.025)break;}
  scene=scene*trans+gas;emission=emission*trans+gas*.55;
 }

 // Actual ellipsoidal dome intersections, back shell then front shell, clipped by opaque world geometry.
 vec3 ec=vec3(0.,0.,0.);float rr=Radius*.98,stretch=.80;
 vec3 er=(ro-ec)/vec3(1.,stretch,1.),ed=rd/vec3(1.,stretch,1.);
 float qa=dot(ed,ed),qb=dot(er,ed),qc=dot(er,er)-rr*rr,disc=qb*qb-qa*qc;
 if(disc>0.){for(int side=0;side<2;side++){
 float ds=(-qb+(side==0?1.:-1.)*sqrt(disc))/qa;
 vec3 p=ro+rd*ds;
 if(ds<0.||ds>solid||p.y<.025)continue;
 vec3 un=normalize(vec3(p.x,p.y/stretch,p.z));
 vec3 normal=normalize(vec3(un.x,un.y/stretch,un.z));
 float rim=pow(1.-abs(dot(normal,-rd)),2.4),t=Time*Speed;
 vec3 absn=abs(un);vec2 plane=absn.y>max(absn.x,absn.z)?p.xz:absn.x>absn.z?p.zy:p.xy;
 float dominant=max(absn.x,max(absn.y,absn.z));
 float scan=pow(.5+.5*sin(p.y*8.-t*1.3),22.);
 vec2 cell=plane*2.2;vec2 fid=floor(cell);vec2 loc=fract(cell)-.5;
 float active=step(.68,h21(fid));
 float glyph=(band(abs(loc.x)-.17,.025)*step(abs(loc.y),.28)+band(loc.y,.025)*step(abs(loc.x),.22)+band(abs(loc.y)-.22,.022)*step(abs(loc.x),.14))*active;
 glyph*=smoothstep(.58,.76,dominant)*(.45+.55*sin(t+fid.y*.8)*sin(t+fid.y*.8));
 float lattice=band(abs(loc.x)-.47,.012)*.08+band(abs(loc.y)-.47,.012)*.08;
 float age=mod(Time,6.);vec3 impact=normalize(vec3(.72,.42,.56));
 float arc=acos(clamp(dot(un,impact),-1.,1.))*rr;
 float wave=band(arc-age*3.2,.075)*exp(-age*.85)*step(.02,age);
 float echo=band(arc-age*2.65,.045)*exp(-age*1.25)*step(.06,age);
 float contact=exp(-arc*arc*3.)*exp(-age*6.);
 float foot=exp(-p.y*8.);float breathing=.88+.12*sin(t*.8);
 vec3 jade=Palette==2?vec3(1.,.68,.23):Palette==1?vec3(.30,.93,.72):vec3(.48,.055,.035);
 vec3 gold=vec3(1.,.53,.18);
 float opacity=clamp((.015+rim*.24+scan*.025+glyph*.07+foot*.17+wave*.40+echo*.16+contact*.5)*Density*Power*Reveal,0.,.85);
 vec3 lit=jade*(.13+rim*.72+scan*.13+lattice)+gold*(glyph*.45+foot*.30+wave*1.8+echo*.9+contact*1.4);
 lit*=Power*Reveal*breathing;
 scene=scene*(1.-opacity)+lit*opacity+gold*(wave*.26+contact*.25)*Power*Reveal;
 emission=emission*(1.-opacity)+lit*opacity*.60+gold*(wave*.32+contact*.25)*Power*Reveal;
 }}
 // Deterministic particles converge inward, then rise out of the array eye.
 for(int i=0;i<55;i++){float seed=h21(vec2(float(i),13.)),u=fract(Time*Speed*(.075+seed*.035)+seed);float a=float(i)*2.39996+u*1.5,r=Radius*pow(1.-u,1.25);vec3 p=vec3(cos(a)*r,.18+pow(u,5.)*3.6+sin(u*12.+seed*9.)*.06,sin(a)*r);vec3 q=p-ro;float along=dot(q,rd);if(along<0.||along>solid)continue;vec3 near=q-rd*along;float s=.012+.016*seed;float dotLight=exp(-dot(near,near)/(s*s));vec3 col=Palette==0?element(i%5):element(0);vec3 spark=col*dotLight*(.6+.7*seed)*Power*Reveal;scene+=spark;emission+=spark;}
}

void main(){vec3 c,e;formation(uv,c,e);FragColor=vec4(c,1.);BrightColor=vec4(max(e-vec3(.09),vec3(0.)),1.);}