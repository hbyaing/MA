#version 150

in vec2 vUV;in vec3 vLocal;in float vSeed;flat in int vKind;flat in int vID;
uniform float uTime,uSpin,uGoldPower,uBluePower,uFirePower,uWispPower,uGlyph,uSparkPower;
uniform vec3 uGold,uBlue,uFire;
uniform vec3 uEyeLocal;
uniform float uRadius,uHeight;
uniform float uFireTier;
vec3 fireColor(vec3 p){if(uFireTier<8.5)return uFire;float phase=mod(uTime*.625+p.y*.45+atan(p.z,p.x)*.18+4.,4.);vec3 a=phase<1.?vec3(.251,.878,.816):phase<2.?vec3(1.,.439,.722):phase<3.?vec3(.690,.439,1.):vec3(1.,.831,.361);vec3 b=phase<1.?vec3(1.,.439,.722):phase<2.?vec3(.690,.439,1.):phase<3.?vec3(1.,.831,.361):vec3(.251,.878,.816);return mix(a,b,(1.-cos(fract(phase)*3.14159265))*.5);}
uniform sampler2D uPrevDepth;
uniform vec2 uResolution;
uniform int uPeel;
uniform sampler2D uWorldDepth;
out vec4 fragColor;

const float PI=3.14159265359, TAU=6.28318530718;
float hash(float x){return fract(sin(x*127.1+19.19)*43758.5453123);}
float hash3(vec3 p){p=fract(p*.1031);p+=dot(p,p.yzx+33.33);return fract((p.x+p.y)*p.z);}
float noise(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(mix(hash3(i),hash3(i+vec3(1,0,0)),f.x),mix(hash3(i+vec3(0,1,0)),hash3(i+vec3(1,1,0)),f.x),f.y),mix(mix(hash3(i+vec3(0,0,1)),hash3(i+vec3(1,0,1)),f.x),mix(hash3(i+vec3(0,1,1)),hash3(i+vec3(1,1,1)),f.x),f.y),f.z);}
float fbm(vec3 p){return .5333*noise(p)+.2667*noise(p*2.03+13.7)+.1333*noise(p*4.11-9.4)+.0667*noise(p*8.27+4.6);}
mat3 rx(float a){float c=cos(a),s=sin(a);return mat3(1,0,0,0,c,s,0,-s,c);}
mat3 ry(float a){float c=cos(a),s=sin(a);return mat3(c,0,-s,0,1,0,s,0,c);}
mat3 rz(float a){float c=cos(a),s=sin(a);return mat3(c,s,0,-s,c,0,0,0,1);}
float segment(vec2 p,vec2 a,vec2 b){vec2 d=b-a;return length(p-a-d*clamp(dot(p-a,d)/dot(d,d),0.,1.));}
#ifndef VERTEX_STAGE
float ink(float d,float w){float aa=max(fwidth(d),.0006);return 1.-smoothstep(w-aa,w+aa,d);}
#endif
float bell(float x,float s){return exp(-x*x*s);}

// Angular meander, constructed from distances to straight line segments.
float glyph(vec2 p,float variant){
 if(mod(variant,2.)>.5)p.x=1.-p.x;
 float d=segment(p,vec2(.10,.16),vec2(.90,.16));
 d=min(d,segment(p,vec2(.90,.16),vec2(.90,.83)));
 d=min(d,segment(p,vec2(.90,.83),vec2(.27,.83)));
 d=min(d,segment(p,vec2(.27,.83),vec2(.27,.39)));
 d=min(d,segment(p,vec2(.27,.39),vec2(.69,.39)));
 d=min(d,segment(p,vec2(.69,.39),vec2(.69,.61)));
 d=min(d,segment(p,vec2(.69,.61),vec2(.47,.61)));
 return ink(d,.043*uGlyph);
}
// Test only this ring's shell, so a transparent glyph does not expose another
// face's glyph band. Other effects and the world remain visible through holes.
bool hiddenRingFace(vec3 p,float id){
 vec3 d=p-uEyeLocal;float distanceToFace=length(d);
 float limit=1.-.001/max(distanceToFace,.001);
 float inner=1.985*uRadius,outer=2.275*uRadius;
 float lo=id<.5?1.65*uHeight:-1.5*uHeight-.14;
 float hi=lo+.14;
 if(abs(d.y)>1e-7)for(int i=0;i<2;i++){
  float t=((i==0?lo:hi)-uEyeLocal.y)/d.y;
  float r=length((uEyeLocal+d*t).xz);
  if(t>0.&&t<limit&&r>=inner&&r<=outer)return true;
 }
 float a=dot(d.xz,d.xz),b=dot(uEyeLocal.xz,d.xz);
 if(a>1e-8)for(int i=0;i<2;i++){
  float r=i==0?inner:outer,c=dot(uEyeLocal.xz,uEyeLocal.xz)-r*r,disc=b*b-a*c;
  if(disc<0.)continue;
  float root=sqrt(disc);
  for(int j=0;j<2;j++){
   float t=(-b+(j==0?-root:root))/a,y=uEyeLocal.y+d.y*t;
   if(t>0.&&t<limit&&y>=lo&&y<=hi)return true;
  }
 }
 return false;
}
void main(){
 if(gl_FragCoord.z>texture(uWorldDepth,gl_FragCoord.xy/uResolution).r+.0000001)discard;
 if(uPeel>0&&gl_FragCoord.z<=texture(uPrevDepth,gl_FragCoord.xy/uResolution).r+.00000012)discard;
 float t=uTime*uSpin,u=vUV.x,v=vUV.y;vec3 color=vec3(0);float alpha=0.;
 if(vKind==0||vKind==10||vKind==11){
  float id=vKind==10?1.:vKind==11?0.:float(vID),cells=id<1.5?32.:28.;
  if(id<1.5&&hiddenRingFace(vLocal,id))discard;
  vec2 p=vec2(fract(u*cells),clamp((v-.23)/.54,0.,1.));
  float cell=floor(u*cells);
  float g=glyph(p,cell)*step(.23,v)*step(v,.77);
  // Eight larger square seals sit at the cardinal and diagonal divisions.
  float largeCell=u*8.,sealLocal=fract(largeCell);
  float marker=1.-smoothstep(.048,.056,abs(sealLocal-.5));
  vec2 s=vec2((sealLocal-.5)*8.5+.5,(v-.16)/.68);
  float seal=glyph(s,0.)*step(0.,s.x)*step(s.x,1.)*step(0.,s.y)*step(s.y,1.);
  g=mix(g,seal,marker);
  float rails=ink(abs(v-.10),.014)+ink(abs(v-.90),.014)+ink(abs(v-.20),.008)+ink(abs(v-.80),.008);
  float edge=smoothstep(0.,.045,v)*(1.-smoothstep(.955,1.,v));
  float traveling=pow(.5+.5*sin(u*TAU*3.-t*.9),24.);
  vec3 tint=id<.5?uGold:id<1.5?vec3(.61,.65,.76):uBlue;
  float power=id<.5?uGoldPower:id<1.5?.65*uGoldPower:uBluePower;
  float emission=rails*2.2+g*2.5;
  color=tint*(.12+emission*(1.+traveling*.7))*power+vec3(1)*pow(emission*.3,3.)*power*.28;
  alpha=edge*(id>.5&&id<1.5?.94:clamp(.12+g*.88+rails*.85,0.,1.));
  // The four faces of a solid annular column must occlude one another.
  // Fading their borders exposes the opposite cap/wall through the seam,
  // superimposing several independent glyph bands even on the black ring.
  if(id<1.5)alpha=1.;
  if(id<.5){
   // User-approved polarity: the original bright glyphs become apertures;
   // the former clear negative space becomes the white luminous material.
   float opening=clamp(max(g,rails),0.,1.);
   alpha=1.-opening;
   color=vec3(1.)*(1.4+traveling*.6)*uGoldPower;
  }
  if(id>.5&&id<1.5){
   // Ink-black body. Symbols reflect cold light instead of emitting white bloom.
   color=vec3(.0007,.0009,.0022)+vec3(.13,.15,.21)*(g*.48+rails*.12);
   color+=vec3(.15,.18,.25)*seal*marker*.42;
   alpha=1.;
  }
 }else if(vKind==8){
  float ring=floor(float(vID)/3.),rail=mod(float(vID),3.);
  float pulse=pow(.5+.5*sin(u*TAU*7.-t*.5+rail),16.);
  float edge=pow(max(0.,1.-abs(v*2.-1.)),1.4);
  vec3 tint=ring<.5?uGold:ring<1.5?vec3(.008,.009,.016):uBlue;
  color=tint*(1.1+pulse*2.)*(ring<1.5?uGoldPower:uBluePower);
  alpha=edge*(rail<.5?.55:rail<1.5?.32:.17);
  if(rail>1.5)alpha*=smoothstep(.2,.65,sin(u*TAU*53.));
 }else if(vKind==1){
  vec3 q=vec3(u*22.-t*.9,v*4.5,float(vID)*2.3);
  float n=fbm(q),curl=noise(q*2.5+vec3(0,t*.5,0));
  float edge=pow(max(0.,1.-abs((v-.5)*2.+(n-.5)*.55)),1.1);
  float l=abs(sin(v*PI*4.+n*5.+u*12.-t*1.3));
  float vein=pow(1.-l,9.);
  float stria=pow(1.-abs(sin(v*21.+u*85.+n*8.-t*2.)),18.);
  float hot=vein+stria*.75;
  float broken=smoothstep(.14,.66,n+curl*.25);
  vec3 fire=fireColor(vLocal);color=mix(fire*.18,fire*1.6,broken)*uFirePower;
  color+=mix(fire,vec3(1.),.12)*hot*3.2*uFirePower;
  alpha=edge*(.08+broken*.35+hot*.65)*smoothstep(0.,.07,u)*(1.-smoothstep(.98,1.,u));
 }else if(vKind==2){
  float n=fbm(vec3(u*18.-t*.3,v*3.,vSeed*13.));
  float edge=pow(max(0.,1.-abs(v*2.-1.+(n-.5)*.3)),1.7);
  float filament=pow(1.-abs(sin(v*13.+u*37.+n*5.-t*2.)),12.);
  float split=pow(1.-abs(sin(v*25.+n*9.-u*52.+t)),19.);
  color=(uBlue*(1.+filament*2.)+vec3(.48,.73,1)*split*3.)*uBluePower;
  alpha=edge*(.10+filament*.6+split*.5)*pow(max(0.,sin(PI*u)),.5);
 }else if(vKind==3||vKind==6){
  bool dark=vID>=18;float n=fbm(vec3(u*10.-t*.75,v*3.,vSeed*11.));
  float w=sin(v*12.+u*19.-t+5.*n),thread=pow(1.-abs(w),10.);
  float micro=pow(1.-abs(sin(v*31.-u*46.+n*8.+t)),20.);
  float edge=pow(max(0.,1.-abs(v*2.-1.+(n-.5)*.45)),1.3);
  float ends=pow(max(0.,sin(PI*u)),.4);
  if(dark){color=vec3(.001,.002,.007)+vec3(.15,.17,.30)*(thread*.9+micro*.6)*uWispPower;alpha=edge*ends*(.68+n*.65)*uWispPower;}
  else{color=(uGold*.56+vec3(.38,.43,.56))*(.7+thread*1.8+micro*1.6)*uWispPower;alpha=edge*ends*(.05+n*.09+thread*.35+micro*.25);}
 }else if(vKind==4){
  vec2 p=(vUV-.5)*2.;float d=length(p),core=exp(-d*d*30.);
  float star=exp(-abs(p.x)*95.-abs(p.y)*5.)+exp(-abs(p.y)*95.-abs(p.x)*5.);
  float sparkle=.25+.75*pow(.5+.5*sin(uTime*(1.+hash(vSeed)*3.)+vSeed),3.);
  float family=mod(float(vID),4.);
  color=(family<.5?uGold:family<1.5?uBlue:family<2.5?fireColor(vLocal):vec3(.30,.35,.63))*3.*uSparkPower;
  alpha=(core+star*.75+exp(-d*d*7.)*.12)*sparkle;
 }else if(vKind==5){
  float phase=fract(u*1.6+uTime*.18+hash(vSeed));
  float pulse=pow(1.-phase,16.);
  float edge=pow(max(0.,1.-abs(v*2.-1.)),2.);
  color=uGold*(.35+pulse*4.)*uGoldPower;
  alpha=edge*(.045+pulse*.75)*(1.-smoothstep(.65,1.,u));
 }else if(vKind==7){
  float n=fbm(vLocal*55.+vec3(uTime,-uTime*2.,0));
  color=(fireColor(vLocal)*(3.+4.*n)+vec3(.25))*uFirePower;alpha=.95;
 }
 if(alpha<.002)discard;
 fragColor=vec4(color,clamp(alpha,0.,1.));
}
