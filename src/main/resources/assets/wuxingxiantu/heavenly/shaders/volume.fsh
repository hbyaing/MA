#version 150

in vec2 vUV;out vec4 fragColor;
uniform vec2 uResolution;
uniform vec3 uEye,uRight,uUp,uForward;
uniform float uTime,uAspect,uTanFov,uRadius,uHeight,uSpin,uSmoke,uMist,uFirePower,uSwirl,uBackdrop;
uniform vec3 uFire,uGold;
uniform float uFireTier;
vec3 fireColor(vec3 p){if(uFireTier<8.5)return uFire;float phase=mod(uTime*.625+p.y*.45+atan(p.z,p.x)*.18+4.,4.);vec3 a=phase<1.?vec3(.251,.878,.816):phase<2.?vec3(1.,.439,.722):phase<3.?vec3(.690,.439,1.):vec3(1.,.831,.361);vec3 b=phase<1.?vec3(1.,.439,.722):phase<2.?vec3(.690,.439,1.):phase<3.?vec3(1.,.831,.361):vec3(.251,.878,.816);return mix(a,b,(1.-cos(fract(phase)*3.14159265))*.5);}
uniform int uSteps;
uniform mat4 uInvVP;
uniform sampler2D uWorldDepth;
uniform float uCoreStrength;
vec3 unproject(vec2 uv,float z){vec4 p=uInvVP*vec4(uv*2.-1.,z*2.-1.,1.);return p.xyz/p.w;}
uniform vec4 uLayers;
uniform sampler2D uPeelColor0,uPeelDepth0;
uniform sampler2D uPeelColor1,uPeelDepth1;
uniform sampler2D uPeelColor2,uPeelDepth2;
uniform sampler2D uPeelColor3,uPeelDepth3;
uniform sampler2D uPeelColor4,uPeelDepth4;
uniform sampler2D uPeelColor5,uPeelDepth5;
uniform sampler2D uPeelColor6,uPeelDepth6;
uniform sampler2D uPeelColor7,uPeelDepth7;

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

vec3 sky(vec3 rd){
 float horizon=smoothstep(-.70,-.05,rd.y);
 vec3 col=mix(vec3(.25,.065,.035),vec3(.006,.017,.048),horizon);
 vec2 p=rd.xz/max(.12,abs(rd.y)+.35);
 float clouds=fbm(vec3(p*2.5,rd.y*3.+uTime*.004));
 float thin=fbm(vec3(p*6.+clouds*2.,rd.y*7.));
 float cloud=smoothstep(.47,.72,clouds+thin*.18);
 vec3 cloudCol=mix(vec3(.30,.10,.04),vec3(.012,.020,.046),smoothstep(-.45,.03,rd.y));
 col=mix(col,cloudCol,cloud*.58);
 col+=vec3(.025,.008,.004)*pow(max(0.,dot(rd,normalize(vec3(-.7,-.18,-.4)))),12.);
 vec2 s=rd.xy/(.5+abs(rd.z))*290.;vec2 cell=floor(s);vec2 pt=fract(s)-.5;
 float st=hash3(vec3(cell,3.));col+=vec3(.35,.43,.65)*step(.997,st)*exp(-dot(pt,pt)*100.)*smoothstep(.1,.7,rd.y);
 return mix(vec3(.006,.011,.028),col,uBackdrop);
}
vec4 medium(vec3 p){
 float t=uTime*uSpin;vec3 q=p/vec3(uRadius,uHeight,uRadius);float r=length(q.xz),a=atan(q.z,q.x);
 float density=0.;vec3 light=vec3(0);
 if(q.y<.35&&q.y>-1.8&&r<2.){
  float h=clamp((q.y+1.55)/1.9,0.,1.);
  float winding=a*7.+h*8.-t*.7+sin(h*5.-t*.3)*1.3;
  float target=1.32-h*.63+.10*sin(a*3.+h*11.-t*.7);
  float field=bell(r-target,19.)*pow(.5+.5*sin(winding),2.);
  float n=fbm(q*5.+vec3(sin(a+t*.3),-t*.85,cos(a+t*.3)));
  float fold=pow(1.-abs(sin(winding+n*7.)),10.);
  float d=field*smoothstep(.20,.62,n)*pow(max(0.,sin(PI*h)),.6)*uSmoke*uLayers.z;
  density+=d*5.4;
  light+=d*(vec3(.006,.007,.016)+vec3(.12,.12,.22)*fold*.8);
 }
 if(q.y>.1&&q.y<1.95&&r<2.5){
  float h=clamp((q.y-.1)/1.85,0.,1.);
  float target=.2+h*1.65;
  float swirl=a*5.-q.y*7.+t*.5;
  float n=fbm(q*4.8+vec3(0,t*.5,0));
  float d=bell(r-target,18.)*pow(.5+.5*sin(swirl+n*4.),4.)*smoothstep(.38,.72,n)*uMist*uLayers.y;
  d*=1.-smoothstep(1.65,1.95,q.y);
  float cap=bell(q.y-1.75-sin(a*3.+r*4.+t*.5)*.1,55.)*smoothstep(.2,1.6,r)*(1.-smoothstep(1.65,2.25,r))*.04;
  d+=cap*pow(n,2.)*uMist*uLayers.y;
  density+=d*.85;light+=d*uGold*(.25+pow(n,4.)*2.);
 }
 if(uLayers.x>.5){
  vec3 c=rx(-.16)*ry(-t*.075)*(p-vec3(0,.06,0));
  c/=uSwirl;float radius=length(c),rr=length(c.xy),angle=atan(c.y,c.x);
  if(radius<1.38){
   float n=fbm(c*7.+vec3(t*.3,-t*.5,t*.1));
   float latitude=acos(clamp(c.z/max(radius,.001),-1.,1.));
   float phase=angle*3.+latitude*5.2-t*2.2+n*2.;
   float arm=pow(max(0.,.5+.5*sin(phase)),9.);
   float shell=exp(-abs(radius-(.82+(n-.5)*.22))*34.);
   float inner=exp(-radius*radius*3.)*.10;
   float d=shell*(.10+arm*2.4)+inner;
   float filament=pow(1.-abs(sin(phase*2.+n*5.)),14.);
   density+=d*2.6*uCoreStrength;
   vec3 fire=fireColor(p);vec3 red=fire*.35;
   vec3 hot=mix(fire,vec3(1.),.08)*3.4*(arm+filament*.9);
   light+=d*(red+hot)*uFirePower*uCoreStrength;
   float glow=exp(-dot(c,c)*55.);density+=glow*2.*uCoreStrength;light+=glow*(fire*9.+vec3(.25))*uFirePower*uCoreStrength;
  }
 }
 if(isnan(density)||isinf(density)||any(isnan(light))||any(isinf(light)))return vec4(0);
 return vec4(light,max(0.,density));
}
void main(){
 vec3 rd=normalize(unproject(vUV,1.)-uEye);
 vec3 bg=vec3(0);
 vec4 surfaces[8];float surfaceDistance[8];
 surfaces[0]=texture(uPeelColor0,vUV);{float z=texture(uPeelDepth0,vUV).r;surfaceDistance[0]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[1]=texture(uPeelColor1,vUV);{float z=texture(uPeelDepth1,vUV).r;surfaceDistance[1]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[2]=texture(uPeelColor2,vUV);{float z=texture(uPeelDepth2,vUV).r;surfaceDistance[2]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[3]=texture(uPeelColor3,vUV);{float z=texture(uPeelDepth3,vUV).r;surfaceDistance[3]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[4]=texture(uPeelColor4,vUV);{float z=texture(uPeelDepth4,vUV).r;surfaceDistance[4]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[5]=texture(uPeelColor5,vUV);{float z=texture(uPeelDepth5,vUV).r;surfaceDistance[5]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[6]=texture(uPeelColor6,vUV);{float z=texture(uPeelDepth6,vUV).r;surfaceDistance[6]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
surfaces[7]=texture(uPeelColor7,vUV);{float z=texture(uPeelDepth7,vUV).r;surfaceDistance[7]=z>.999999?1e8:dot(unproject(vUV,z)-uEye,rd);}
 int surfaceIndex=0;
 float bound=max(3.6*max(uRadius,uHeight),1.7*uSwirl);
 float b=dot(uEye,rd),c=dot(uEye,uEye)-bound*bound,h=b*b-c;
 if(h<0.){fragColor=vec4(0);return;}
 float lo=max(0.,-b-sqrt(h)),hi=-b+sqrt(h);
 float worldZ=texture(uWorldDepth,vUV).r;
 if(worldZ<.999999)hi=min(hi,dot(unproject(vUV,worldZ)-uEye,rd));
 if(hi<=lo){fragColor=vec4(0);return;}
 float dt=(hi-lo)/float(uSteps),dist=lo+dt*.5,trans=1.;vec3 col=vec3(0);
 for(int i=0;i<768;i++){
  if(i>=uSteps)break;
  vec4 m=medium(uEye+rd*dist);float extinction=1.-exp(-m.a*dt*2.3);
  col+=trans*(m.rgb/max(m.a,.0001))*extinction;
  trans*=1.-extinction;
  for(int j=0;j<8;j++){
   if(surfaceIndex>=8)break;
   if(surfaceDistance[surfaceIndex]>dist+dt*.5)break;
   vec4 s=surfaces[surfaceIndex];col+=trans*s.rgb*s.a;trans*=1.-s.a;surfaceIndex++;
  }
  dist+=dt;
 }
 fragColor=vec4(col,1.-trans);
}
