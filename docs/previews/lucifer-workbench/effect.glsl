precision highp float;
uniform vec2 resolution;
uniform float time, intensity, density, tilt;
uniform float radius, rotationSpeed, flowSpeed, mistSpeed, thickness, rayLength, pulseStrength, openingDuration, glowGain;
uniform vec3 ringColor, runeColor, rayColor, mistColor, coreColor, backgroundColor;
uniform vec4 layers;
uniform float mistWidth, mistIntensity, mistTurbulence;
out vec4 fragColor;
#define PI 3.14159265359
float hash(float x){return fract(sin(x*127.1+13.7)*43758.5453);}
float line(float d,float w){return exp(-abs(d)/(w*thickness));}
float ring(float r,float x,float w){return line(r-x,w);}
float seg(vec2 p,vec2 a,vec2 b,float w){vec2 v=b-a;return line(length(p-a-v*clamp(dot(p-a,v)/dot(v,v),0.,1.)),w);}
mat2 rot(float a){return mat2(cos(a),-sin(a),sin(a),cos(a));}
float mistHash(vec3 p){p=fract(p*.1031);p+=dot(p,p.yzx+33.33);return fract((p.x+p.y)*p.z);}
float mistNoise(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);
 return mix(mix(mix(mistHash(i),mistHash(i+vec3(1,0,0)),f.x),mix(mistHash(i+vec3(0,1,0)),mistHash(i+vec3(1,1,0)),f.x),f.y),mix(mix(mistHash(i+vec3(0,0,1)),mistHash(i+vec3(1,0,1)),f.x),mix(mistHash(i+vec3(0,1,1)),mistHash(i+vec3(1,1,1)),f.x),f.y),f.z);}
float mistFbm(vec3 p){float sum=0.,weight=.55;for(int i=0;i<4;i++){sum+=weight*mistNoise(p);p=p*2.03+vec3(9.2,3.7,5.1);weight*=.48;}return sum;}
void main(){
 vec2 uv=(gl_FragCoord.xy-.5*resolution)/resolution.y;
 vec3 col=backgroundColor+backgroundColor*.5*max(0.,1.-length(uv));
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
 // Continuous flowing emissive mist: broad soft density, not a polyline or lightning.
 float mist=0.;
 if(abs(r-1.13)<mistWidth*4.+.18 && layers.w>.0){
   float mt=t*mistSpeed;
   vec2 flow=rot(mt*.19)*q;
   vec3 field=vec3(flow*3.4,mt*.22);
   float coarse=mistFbm(field);
   float warp=mistFbm(field*1.28+vec3(3.8,7.1,-mt*.12));
   float center=1.13+(coarse-.5)*.22*mistTurbulence;
   float d=(r-center)/max(.012,mistWidth);
   float veil=exp(-d*d*1.25);
   float detail=mistFbm(vec3(flow*11.+vec2(warp*1.4,coarse),mt*.37));
   float folds=.35+.9*smoothstep(.22,.76,coarse+detail*.25);
   float gauze=pow(max(.0,detail),1.5)*1.4;
   mist=veil*folds*(.35+gauze)*mistIntensity;
   // Diffuse translucent skirt, fading continuously in both radial directions.
   mist+=exp(-d*d*.24)*(.12+.12*coarse)*mistIntensity;
 }

 float inner=exp(-r*r*7.);
 float burst=pow(max(0.,sin(t*1.3)),22.)*.65;
 col+=(ringColor*rings*layers.x+runeColor*red*layers.y+rayColor*glow*layers.z+mistColor*mist*layers.w)*intensity*unfold*pulse*glowGain;
 col+=coreColor*exp(-r*r*4.)*(.055+burst*pulseStrength)*unfold*intensity;

 col=1.-exp(-col*1.15);col=pow(col,vec3(.88));fragColor=vec4(col,1.);
}