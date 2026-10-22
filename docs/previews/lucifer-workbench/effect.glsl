precision highp float;
uniform vec2 resolution;
uniform float time, intensity, density, tilt;
uniform float radius, rotationSpeed, flowSpeed, arcSpeed, thickness, rayLength, pulseStrength, openingDuration, showFigure, glowGain;
uniform vec3 ringColor, runeColor, rayColor, arcColor, coreColor, backgroundColor;
uniform vec4 layers;
out vec4 fragColor;
#define PI 3.14159265359
float hash(float x){return fract(sin(x*127.1+13.7)*43758.5453);}
float line(float d,float w){return exp(-abs(d)/(w*thickness));}
float ring(float r,float x,float w){return line(r-x,w);}
float seg(vec2 p,vec2 a,vec2 b,float w){vec2 v=b-a;return line(length(p-a-v*clamp(dot(p-a,v)/dot(v,v),0.,1.)),w);}
mat2 rot(float a){return mat2(cos(a),-sin(a),sin(a),cos(a));}
void main(){
 vec2 uv=(gl_FragCoord.xy-.5*resolution)/resolution.y;
 vec3 col=backgroundColor+backgroundColor*.5*max(0.,1.-length(uv));
 float ground=uv.y+.38;col+=vec3(.035,.02,.065)*exp(-abs(ground)*18.);
 float grid=pow(max(0.,cos(uv.x*36.)),45.)*exp(-abs(ground)*8.)*.016;col+=vec3(.3,.18,.5)*grid;
 float t=time;float unfold=smoothstep(0.,openingDuration,t);float pulse=1.-pulseStrength*.5+pulseStrength*.5*sin(t*2.4);
 vec2 q=uv-vec2(0.,.055);q.x/=max(.45,cos(tilt));q=rot(.025*sin(t*.2))*q;
 float scale=radius*max(.03,unfold);q/=scale;
 float r=length(q);float a=atan(q.y,q.x);vec3 violet=vec3(.48,.12,1.), pink=vec3(1.,.13,.52), ice=vec3(.7,.5,1.);
 float glow=0.;float red=0.;
 glow+=ring(r,.79,.007)*1.8+ring(r,.83,.004)+ring(r,.87,.008)*1.4;
 glow+=ring(r,1.,.008)*1.8+ring(r,1.08,.004)+ring(r,1.15,.009)*1.5;
 glow+=ring(r,1.18,.025)*.45+ring(r,1.18,.1)*.10;
 red+=ring(r,.58,.006)*1.8+ring(r,.63,.008)*1.2+ring(r,.69,.007);
 float teeth=pow(max(0.,cos((a+t*.16*rotationSpeed)*80.)),15.);red+=teeth*smoothstep(.68,.69,r)*(1.-smoothstep(.75,.76,r))*1.2;
 float ticks=pow(max(0.,cos((a-t*.08*rotationSpeed)*120.)),22.);glow+=ticks*smoothstep(.95,.96,r)*(1.-smoothstep(1.015,1.025,r));
 // Counter-rotating angular seals, built from thin broken engraved strokes.
 for(int k=0;k<3;k++){
  float kk=float(k);float ang=a+t*rotationSpeed*(mod(kk,2.)<.5?.13:-.17);float cell=floor((ang+PI)*16./(2.*PI));
  float local=fract((ang+PI)*16./(2.*PI));float rr=.54+kk*.09;
  float glyph=line(local-.24,.018)*step(rr-.025,r)*step(r,rr+.025);
  glyph+=line(r-rr,.003)*step(.24,local)*step(local,.69);
  glyph+=line(local-.69,.016)*step(rr-.025,r)*step(r,rr+.008);
  red+=glyph*(.6+.4*hash(cell+kk*17.));
 }
 float rings=glow;glow=0.;
 // Stable indexed radial circuit paths, with stepped lateral offsets and outward pulses.
 float lanes=144.*density;float sector=(a+PI)*lanes/(2.*PI);float id=floor(sector);float local=fract(sector)-.5;
 float rnd=hash(id);float reach=1.18+rayLength*(.17+1.05*hash(id+7.));
 float offset=.15*sin(floor(r*8.+rnd*4.)*2.+id)*smoothstep(1.17,1.4,r);
 float dist=abs(local-offset)*r*2.*PI/lanes;
 float mask=smoothstep(1.13,1.17,r)*(1.-smoothstep(reach-.15,reach,r));
 float travel=pow(max(0.,sin(r*13.-t*4.*flowSpeed+rnd*20.)),12.);
 float thin=line(dist,.0023)*mask;glow+=thin*(.3+travel*1.8);
 glow+=line(dist,.013)*mask*.13;
 float knot=ring(r,reach-.06,.007)*line(dist,.009)*1.2;glow+=knot;
 // Long restrained filament arcs, topology seeded by angle and fixed temporal intervals.
 float frame=floor(t*7.*arcSpeed);float lightning=0.;
 for(int i=0;i<5;i++){
  float fi=float(i);float start=hash(fi+frame*3.);float aa=a-start*6.283;
  float arcR=1.12+.045*sin(a*17.+frame)+.025*sin(a*43.+fi);
  float gate=smoothstep(.04,.14,abs(sin(aa*.5)))*(1.-smoothstep(.38,.65,abs(sin(aa*.5))));
  lightning+=ring(r,arcR,.003)*gate*(.3+.7*hash(fi+frame));
 }

 float inner=exp(-r*r*7.);
 float burst=pow(max(0.,sin(t*1.3)),22.)*.65;
 col+=(ringColor*rings*layers.x+runeColor*red*layers.y+rayColor*glow*layers.z+arcColor*lightning*1.3*layers.w)*intensity*unfold*pulse*glowGain;
 col+=coreColor*exp(-r*r*4.)*(.055+burst*pulseStrength)*unfold*intensity;
 col+=arcColor*lightning*.6*unfold*layers.w*intensity;
 // Separate suspended humanoid scale reference in front of the halo.
 vec2 h=uv-vec2(0.,-.015);float body=seg(h,vec2(0.,-.09),vec2(0.,.07),.009);
 body=max(body,line(length(h-vec2(0.,.105))-.023,.003));
 body=max(body,seg(h,vec2(-.005,-.08),vec2(-.035,-.22),.006));body=max(body,seg(h,vec2(.005,-.08),vec2(.035,-.22),.006));
 body=max(body,seg(h,vec2(-.005,.055),vec2(-.088,.015),.005));body=max(body,seg(h,vec2(.005,.055),vec2(.088,.015),.005));
 body*=showFigure;float silhouette=smoothstep(.13,.85,body);col=mix(col,vec3(.018,.012,.04),silhouette*.88);col+=vec3(.8,.46,1.)*body*.40*unfold;
 col+=vec3(.34,.12,.8)*exp(-length(uv-vec2(0.,-.37))*14.)*.15;
 col=1.-exp(-col*1.15);col=pow(col,vec3(.88));fragColor=vec4(col,1.);
}