#version 150
in vec3 localPos;uniform float Time;uniform float Reveal;out vec4 fragColor;
const float PI=3.14159265359;const float Radius=5.2;const float Speed=1.;const float Power=1.;
vec3 element(int i){if(i%4==0)return vec3(.08,.85,.80);if(i%4==1)return vec3(.95,.06,.035);if(i%4==2)return vec3(.17,.11,.15);return vec3(.62,.14,.95);}
vec2 node(int i){float a=-float(i)*PI*.5;return vec2(cos(a),sin(a))*Radius*1.55;}
float band(float d,float w){return exp(-abs(d)/max(w,.0001));}
float segment(vec2 p,vec2 a,vec2 b){vec2 v=b-a;return length(p-a-v*clamp(dot(p-a,v)/dot(v,v),0.,1.));}
vec3 floorSurface(vec3 p,out vec3 glow){
 vec2 q=p.xz;float r=length(q),rn=r/Radius,a=atan(q.y,q.x),t=Time*Speed;
 vec2 grid=abs(fract(q*.5-.5)-.5)/max(fwidth(q*.5),vec2(.001));float line=1.-min(min(grid.x,grid.y),1.);
 vec3 base=mix(vec3(.020,.012,.018),vec3(.038,.020,.027),line*.5);
 float disk=1.-smoothstep(.96,1.02,rn);base=mix(base,vec3(.033,.013,.023),disk*.55);
 // Counter-rotating runic bands; subsidiary centers and anchoring channels stay fixed.
 a+=t*(rn<1.02?.085:-.055);
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
 for(int i=0;i<8;i++){float angle=float(i)*PI*.25+PI*.125+t*.085;vec2 axis=vec2(cos(angle),sin(angle)),tangent=vec2(-axis.y,axis.x);vec2 local=q-axis*Radius*.81;float x=dot(local,tangent),y=dot(local,axis);for(int row=0;row<3;row++){float solidLine=mod(floor(float(i)/pow(2.,float(row))),2.);float gap=mix(smoothstep(.022,.042,abs(x)),1.,solidLine);trigrams+=band(y-(float(row)-1.)*.085,.009)*(1.-smoothstep(.15,.17,abs(x)))*gap;}}
 colored+=vec3(.65,.055,.07)*trigrams*.55;
 float center=band(rn-.1,.0015)+band(rn-.14,.0015)*(.5+.5*cos(a*8.-t*.18));
 for(int i=0;i<4;i++){
 vec2 local=q-node(i);float nr=length(local),na=atan(local.y,local.x)+t*(i%2==0?.16:-.16);
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
 float angle=-float(k)*PI*.5;vec2 axis=vec2(cos(angle),sin(angle)),tanAxis=vec2(-axis.y,axis.x);
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
 float angle=float(k)*PI/6.+PI/12.-t*.055;vec2 axis=vec2(cos(angle),sin(angle)),tangent=vec2(-axis.y,axis.x);
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

void main(){vec3 glow;floorSurface(vec3(localPos.x,0.,localPos.z),glow);float energy=max(glow.r,max(glow.g,glow.b));float alpha=clamp(energy*1.1,0.,.86);if(alpha<.006)discard;fragColor=vec4(glow/max(alpha,.001),alpha);}
