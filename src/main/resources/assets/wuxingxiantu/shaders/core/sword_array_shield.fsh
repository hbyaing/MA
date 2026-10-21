#version 150
in vec3 localPos;uniform vec3 Eye;uniform float Time;uniform float Reveal;out vec4 fragColor;
const float Speed=1.;const float Density=1.;const float Power=1.;const int Palette=0;
float h21(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float band(float d,float w){return exp(-abs(d)/max(w,.0001));}
void main(){vec3 p=localPos;float stretch=.80,rr=5.2*.98;
 vec3 un=normalize(vec3(p.x,p.y/stretch,p.z));
 vec3 normal=normalize(vec3(un.x,un.y/stretch,un.z));
 float rim=pow(1.-abs(dot(normal,normalize(Eye-p))),2.4),t=Time*Speed;
 vec3 absn=abs(un);vec2 plane=absn.y>max(absn.x,absn.z)?p.xz:absn.x>absn.z?p.zy:p.xy;
 float dominant=max(absn.x,max(absn.y,absn.z));
 float scan=pow(.5+.5*sin(p.y*8.-t*1.3),22.);
 vec2 cell=plane*2.2;vec2 fid=floor(cell);vec2 loc=fract(cell)-.5;
 float glyphEnabled=step(.68,h21(fid));
 float glyph=(band(abs(loc.x)-.17,.025)*step(abs(loc.y),.28)+band(loc.y,.025)*step(abs(loc.x),.22)+band(abs(loc.y)-.22,.022)*step(abs(loc.x),.14))*glyphEnabled;
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
 float opacity=clamp((.055+rim*.30+scan*.035+glyph*.10+foot*.19+wave*.44+echo*.19+contact*.5)*Density*Power*Reveal*.78,0.,.32);
 vec3 lit=jade*(.13+rim*.72+scan*.13+lattice)+gold*(glyph*.45+foot*.30+wave*1.8+echo*.9+contact*1.4);
 lit*=Power*Reveal*breathing;
fragColor=vec4(lit+gold*(wave*.26+contact*.25),opacity);}
