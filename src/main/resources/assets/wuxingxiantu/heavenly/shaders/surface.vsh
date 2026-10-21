#version 150
#define VERTEX_STAGE

in vec2 aUV;
uniform mat4 uVP;
uniform float uTime,uRadius,uHeight,uTilt,uSpin,uSwirl,uWisp,uSparks;
uniform vec3 uRight,uUp;
uniform int uKind,uOffset;
out vec2 vUV;out vec3 vLocal;out float vSeed;flat out int vKind;flat out int vID;

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

mat3 orbit(float id){return ry(.25+uTime*.045*uSpin)*rx((id<.5?1.:-1.)*uTilt);}
void main(){
 float id=float(gl_InstanceID+uOffset),u=aUV.x,v=aUV.y,t=uTime*uSpin;vec3 p=vec3(0);vSeed=hash(id+float(uKind)*57.);vUV=aUV;vKind=uKind;vID=gl_InstanceID+uOffset;
 if(uKind==12){
  // Close each annulus: the existing horizontal face, its opposite cap,
  // and the inner/outer cylindrical walls form a four-face ring column.
  float a=u*TAU+t*(id<.5?-.09:.08);
  float r=(2.13+(v-.5)*.29)*uRadius;
  p=vec3(cos(a)*r,(id<.5?1.65:-1.5)*uHeight+(id<.5?.14:-.14),sin(a)*r);
  vKind=0;
 }else if(uKind==11){
  // Match the lower disc: vertical faces belong to its inner/outer edges,
  // not to the middle of the horizontal glyph band.
  float a=u*TAU-t*.09,r=(2.13+(id<.5?.145:-.145))*uRadius;
  p=vec3(cos(a)*r,1.65*uHeight+v*.14,sin(a)*r);
 }else if(uKind==10){
  float r=(2.13+(id<.5?.145:-.145))*uRadius,a=u*TAU+t*.08;
  p=vec3(cos(a)*r,-1.5*uHeight-v*.14,sin(a)*r);
 }else if(uKind==0||uKind==8){
  float ringID=uKind==8?floor(id/3.):id;
  float rail=uKind==8?mod(id,3.):0.;
  float r=(ringID<1.5?2.13:1.96)*uRadius;
  float width=(ringID<1.5?.29:.24)*uRadius;
  float a=u*TAU+t*(ringID<.5?-.09:ringID<1.5?.08:ringID<2.5?.15:-.13);
  if(uKind==8){
   // Disc faces occupy radius 1.985..2.275. The original inner rail at 2.16
   // was coplanar with their glyphs; equal-depth fragments cannot be peeled.
   r+=(ringID<1.5?.19+rail*.10:(rail-1.)*.18+.21)*uRadius;
   width=.017*uRadius;
  }
  // The blue hoops are belts: their broad inner face points at the furnace.
  // The top/bottom discs retain radial width; the hoops use axial width.
  if(ringID>=1.5&&uKind==0)p=vec3(cos(a)*r,(v-.5)*width,sin(a)*r);
  else p=vec3(cos(a)*(r+(v-.5)*width),0,sin(a)*(r+(v-.5)*width));
  if(ringID<1.5)p.y=(ringID<.5?1.65:-1.5)*uHeight;
  else p=orbit(ringID-2.)*p;
  if(uKind==8)vUV=vec2(u,v);
 }else if(uKind==1){
  float arm=mod(id,3.),braid=floor(id/3.);
  float r=(.045+pow(1.-u,1.1)*1.13)*uSwirl;
  float a=u*TAU*1.35+t*.85+arm*TAU/3.+braid*.17;
  float width=(.035+.19*sin(PI*u))*(1.-braid*.12)*uSwirl;
  r+=(v-.5)*width;
  r+=sin(a*3.+u*9.-t*1.2)*.035*sin(PI*u);
  float ripple=sin(a*5.-t*2.+u*11.)*.045*sin(PI*u);
  float shell=sqrt(max(0.,1.24*1.24*uSwirl*uSwirl-r*r))*.68;
  p=vec3(cos(a)*r,sin(a)*r,(braid<.5?1.:-1.)*shell+ripple);
  if(braid>1.5)p=ry(PI*.5)*p;
  p=ry(t*.075)*rx(.16)*p;p.y+=.06;
 }else if(uKind==2){
  float family=floor(id/10.),strand=mod(id,10.);
  float a=u*TAU+t*(family<.5?.28:-.24)+strand*1.49;
  float radial=1.96+(.07+.11*hash(strand+4.))*sin(a*3.-t*.8+strand*2.3);
  radial+=.055*(strand-4.5);
  float width=(.03+.055*hash(strand+6.))*pow(max(0.,sin(PI*u)),.35);
  p=vec3(cos(a)*(radial+(v-.5)*width),.055*sin(a*5.+t+strand),sin(a)*(radial+(v-.5)*width))*uRadius;
  p=orbit(family)*p;
 }else if(uKind==3){
  bool dark=id>=18.;float seed=dark?id-18.:id;
  float a=seed*TAU/18.+sin(u*5.-t*.7+seed)*.16+u*(dark?1.6:-1.1)+t*(dark?.12:-.07);
  float r=(dark?mix(1.5,.58,pow(u,.8)):mix(1.65,.15,pow(u,1.4)))*uRadius;
  r+=sin(u*11.+seed*2.-t*.8)*.1*sin(PI*u);
  float w=(dark?.32:.16)*uWisp*sin(PI*clamp(u*.95+.025,0.,1.))*(.7+.5*sin(seed+u*7.));
  p=vec3(cos(a)*r,(dark?(-1.5+u*1.6):(1.65-u*1.6))*uHeight,sin(a)*r);
  p+=vec3(cos(a+.7),.13*sin(u*8.+seed),sin(a+.7))*(v-.5)*w;
 }else if(uKind==6){
  float a=id*TAU/14.+u*.65+sin(u*5.-t*.3+id)*.2;
  float r=(1.15+.35*sin(id+u*4.+t*.2)+u*.4)*uRadius;
  float w=.36*uWisp*pow(max(0.,sin(PI*u)),.6);
  p=vec3(cos(a)*(r+(v-.5)*w),(1.65+u*1.2)*uHeight,sin(a)*(r+(v-.5)*w));
 }else if(uKind==4){
  float seed=id+21.,family=mod(id,4.);vec3 q;float phase=fract(hash(seed*7.)+uTime*(.025+.035*hash(seed)));
  float a=hash(seed+2.)*TAU+t*(.05+.08*hash(seed+1.));
  if(family<.5){float r=sqrt(hash(seed+9.))*2.08*uRadius;q=vec3(cos(a)*r,(1.9-phase*3.6)*uHeight,sin(a)*r);}
  else if(family<1.5){float r=(1.85+hash(seed+7.)*.5)*uRadius;q=orbit(mod(id,2.))*vec3(cos(a)*r,sin(a*4.+seed)*.16,sin(a)*r);}
  else if(family<2.5){float r=(.2+hash(seed+3.))*uSwirl;float aa=a+t*.65+r*5.;q=vec3(cos(aa)*r,sin(aa)*r,.3*(hash(seed+8.)-.5));}
  else{float r=(1.8+hash(seed+4.)*.65)*uRadius;q=vec3(cos(a)*r,(hash(seed+5.)*3.6-1.8)*uHeight,sin(a)*r);}
  float size=(.006+pow(hash(seed+11.),9.)*.033)*uSparks;
  p=q+(uRight*(u-.5)+uUp*(v-.5))*size*4.;
  vSeed=seed;
 }else if(uKind==5){
  float a=hash(id+8.)*TAU,r=(.35+hash(id+42.)*1.65)*uRadius;
  float lengthY=(.4+hash(id+14.)*1.9)*uHeight;
  p=vec3(cos(a)*r+(v-.5)*.004,1.65*uHeight-u*lengthY,sin(a)*r);
  vSeed=id;
 }else if(uKind==7){
  float a=u*TAU,b=v*PI;float r=.125*uSwirl*(1.+.07*sin(a*9.+t*4.)*sin(b*5.-t*3.));
  p=vec3(sin(b)*cos(a),cos(b),sin(b)*sin(a))*r+vec3(0,.06,0);
 }
 vLocal=p;gl_Position=uVP*vec4(p,1);
}
