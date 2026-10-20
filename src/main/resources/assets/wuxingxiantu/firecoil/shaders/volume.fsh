#version 150
uniform vec2 Viewport;
uniform mat4 InverseViewProjection;
uniform sampler2D SceneDepth;
uniform vec3 Camera, BoundsMin, BoundsMax;
uniform float Yaw, Intensity, Opacity, ModelScale;
uniform int Steps;
uniform int ItemSpace;
out vec4 result;
#define FC_SINGLE_COIL 1
#define FC_POINT_GRID 1

// SHARED_VOLUME_CORE_BEGIN
// Workbench review IV: close-fitting broken flame, short curls and open silhouette.
// Approved close-fitting fire; shared verbatim with the Forge renderer.
// Body-local metres. Density and heat are independent of integration quality.
uniform float Time;
uniform float Radius;
uniform float Height;
uniform float Turns;
uniform float Width;
uniform float RotationSpeed;
uniform float RiseSpeed;
uniform float WaveAmplitude;
uniform float WaveSpeed;
uniform float Turbulence;
uniform float TongueLength;
uniform float Erosion;
uniform float VolumeStrength;
uniform int Strands;
uniform int BodyEnabled;
uniform float WeaponRadius;
uniform float WeaponWidth;
uniform float WeaponTurns;
uniform float WeaponStrength;
uniform int WeaponCount;
// body-local metres, orthonormal axes: xyz=base, w=actual model length.
uniform vec4 WeaponOrigins[2];
uniform vec4 WeaponAxes[2];
uniform vec4 WeaponSides[2];
vec3 DensityStep;
#if defined(FC_POINT_GRID) && !defined(FC_DISABLE_CACHE)
uint fvPointGridMask(vec3 localPoint);
#endif
#if defined(FC_POINT_BINS) && !defined(FC_DISABLE_CACHE)
bool fvPointBinSupported(vec3 localPoint,int strand);
#endif
#if defined(FC_POINT_SUPPORT) && !defined(FC_DISABLE_CACHE)
// Keep the original sample space for the CPU support balls. Weapon fields
// project their own point afterwards, while their support balls are unprojected.
vec3 fvSupportQueryPoint;
bool fvPointMainSupported(vec3 localPoint,float h,float widthInput,float waveInput,float hierarchy,int strand);
#endif
// Per-ray absolute error budget. At most one cutoff is removed for each field;
// the wrapper scales it by path length, emission and the active field count.
float DensityCutoff;
float GaussianExponentLimit;
float GaussianSupportScale;
uniform int Seed;
uniform vec3 CoreColor;
uniform vec3 MidColor;
uniform vec3 EdgeColor;
uniform int WispCount;
uniform vec4 Wisps[24];
uniform vec4 WispVelocity[24];
uniform vec4 WispMeta[24];

const float FC_PI = 3.14159265359;
const float FC_TAU = 6.28318530718;

void fvPrepareCache(float t);
void prepareFireVolume(float t,float rayLength,float intensity,float opacity){
 fvPrepareCache(t);
 float fields=float((BodyEnabled!=0?Strands*3:0)+WeaponCount*(Strands*3+6)+WispCount);
 DensityCutoff=.0002/(max(1.,intensity)*max(1.,opacity*5.)*max(.01,rayLength)*max(1.,fields));
 GaussianExponentLimit=log(max(8.,8.*VolumeStrength*max(1.,WeaponStrength))/max(DensityCutoff,1.e-12));
 GaussianSupportScale=sqrt(GaussianExponentLimit/.6);
}

float fvHash(vec3 p) {
    p = fract(p * vec3(.1031, .1030, .0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.x + p.y) * p.z);
}

float fvNoise(vec3 p) {
    vec3 cell = floor(p), f = fract(p);
    f = f*f*f*(f*(f*6.0-15.0)+10.0);
    return mix(mix(mix(fvHash(cell), fvHash(cell+vec3(1,0,0)), f.x),
                   mix(fvHash(cell+vec3(0,1,0)), fvHash(cell+vec3(1,1,0)), f.x), f.y),
               mix(mix(fvHash(cell+vec3(0,0,1)), fvHash(cell+vec3(1,0,1)), f.x),
                   mix(fvHash(cell+vec3(0,1,1)), fvHash(cell+vec3(1,1,1)), f.x), f.y), f.z);
}

float fvFbm(vec3 p) {
    return .53*fvNoise(p) + .27*fvNoise(p*2.03+vec3(17.1,5.7,31.3))
         + .13*fvNoise(p*4.11+vec3(9.4,23.2,7.8))
         + .07*fvNoise(p*8.17+vec3(27.7,13.6,41.1));
}

// Average a Gaussian fibre over the actual integration segment. This resolves
// thin cores without screen blur, random jitter or brighter sampling beads.
float fvErf(float x){float a=abs(x),t=1.0/(1.0+.3275911*a);float y=1.0-(((((1.061405429*t-1.453152027)*t)+1.421413741)*t-.284496736)*t+.254829592)*t*exp(-a*a);return sign(x)*y;}
float fvFibre(vec2 delta,vec2 segment,float width){
 float lengthSegment=length(segment),w=max(width,.0015);
 if(lengthSegment<.00005)return exp(-dot(delta,delta)/(w*w));
 vec2 direction=segment/lengthSegment;float along=dot(delta,direction),across=max(0.,dot(delta,delta)-along*along);
 float integral=.886226925*w*(fvErf((along+lengthSegment*.5)/w)-fvErf((along-lengthSegment*.5)/w))/lengthSegment;
 return max(0.,integral)*exp(-across/(w*w));
}

// Analytic cyclic flow. Two spatial scales and midpoint backtracing keep local
// curls alive independently of the body's rotation; this is not a fluid solver.
vec3 fvFlow(vec3 p, float phase) {
    vec3 a = p*3.2 + vec3(phase*.37, -phase*.29, phase*.23);
    vec3 b = p*7.1 + vec3(-phase*.41, phase*.31, phase*.53);
    return vec3(sin(a.y)+cos(a.z), sin(a.z)+cos(a.x), sin(a.x)+cos(a.y))*.37
         + vec3(sin(b.y)+cos(b.z), sin(b.z)+cos(b.x), sin(b.x)+cos(b.y))*.13;
}

vec3 fvWarp(vec3 p, float t, float seed) {
    float k = .045*Turbulence;
    vec3 v = fvFlow(p+vec3(seed*1.3,seed*.7,seed*2.1), t*WaveSpeed);
    return p-k*fvFlow(p-.5*k*v+vec3(seed*1.3,seed*.7,seed*2.1), t*WaveSpeed);
}

// Flowing flame crests, with a dominant stream and thinner companions.
// FC_CENTER_ANALYTIC_BEGIN
vec3 fvCenterAnalytic(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 float phase=strand/float(max(strandCount,1))+.09*sin(strand*2.4);
 float wave=s*8.7-t*WaveSpeed*.8+seed*2.1+strand*2.3;
 float sweep=s+.12*sin(s*6.283-t*.31+strand*1.7)+.045*sin(s*13.-t*.47+strand);
 float angle=FC_TAU*(sweep*turnsInput+phase+t*RotationSpeed*.48);
 angle+=.24*sin(wave*.71)+.10*sin(wave*1.63);
 float envelope=max(0.,sin(s*FC_PI));
 float radius=radiusInput*(.84+.16*envelope)+waveInput*(sin(wave)*.85+sin(wave*1.63)*.38)*(.65+.35*envelope);
 return vec3(cos(angle)*radius,.055+s*heightInput+waveInput*.25*sin(wave*1.2),sin(angle)*radius);
}
// FC_CENTER_ANALYTIC_END

// SHARED_CURVE_CACHE_BEGIN
// Native-only per-draw GPU cache. Web keeps the exact analytic path.
// Width=2049, domain=[-.004,1.004], float32 positions, unfiltered texel access.
#ifdef FC_CURVE_CACHE
uniform sampler2D CurveCenters;
vec3 fvCenter(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 // max(0,sin(pi*s)) has a derivative corner at each endpoint. Retain the
 // analytic result in that narrow band and outside the tabulated domain.
 if(s<.004||s>.996)return fvCenterAnalytic(s,strand,t,seed,radiusInput,heightInput,turnsInput,waveInput,strandCount);
 float coordinate=(s+.004)*(2048./1.008);
 int index=int(floor(coordinate)),row=int(strand);float f=coordinate-float(index);
 vec3 p0=texelFetch(CurveCenters,ivec2(index-1,row),0).xyz;
 vec3 p1=texelFetch(CurveCenters,ivec2(index,row),0).xyz;
 vec3 p2=texelFetch(CurveCenters,ivec2(index+1,row),0).xyz;
 vec3 p3=texelFetch(CurveCenters,ivec2(index+2,row),0).xyz;
 // Cubic Lagrange interpolation of four equally spaced analytic samples.
 // Difference form preserves the central sample and limits float cancellation.
 float w0=-f*(f-1.)*(f-2.)/6.;
 float w2=-(f+1.)*f*(f-2.)*.5;
 float w3=(f+1.)*f*(f-1.)/6.;
 return p1+(p0-p1)*w0+(p2-p1)*w2+(p3-p1)*w3;
}
#else
vec3 fvCenter(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 return fvCenterAnalytic(s,strand,t,seed,radiusInput,heightInput,turnsInput,waveInput,strandCount);
}
#endif
// SHARED_CURVE_CACHE_END

#ifndef FC_DISABLE_CACHE
// SHARED_PEEL_CACHE_BEGIN
// Insert after fvCenter, before fvPeel. Compute time-only tongue geometry once
// per fragment, on the GPU, so the hash and trigonometry retain the same float
// implementation as the original density field. No CPU hash approximation.
// Native body and weapon fields are separate draws; keep their cache compact.
#ifdef FC_NATIVE_PEEL_CACHE
// Native draws bake invariant geometry once on the same GPU. Fetching this
// small shared table avoids large dynamically indexed private arrays per ray.
uniform sampler2D CurveCenters;
vec4 fvGetPeelRoot(int i){return texelFetch(CurveCenters,ivec2(i*3,0),0);}
vec4 fvGetPeelAxis(int i){return texelFetch(CurveCenters,ivec2(i*3+1,0),0);}
vec3 fvGetPeelRadial(int i){return texelFetch(CurveCenters,ivec2(i*3+2,0),0).xyz;}
vec4 fvGetTongueRoot(int i){return texelFetch(CurveCenters,ivec2(48+i*3,0),0);}
vec4 fvGetTongueAxis(int i){return texelFetch(CurveCenters,ivec2(49+i*3,0),0);}
vec4 fvGetTongueRadial(int i){return texelFetch(CurveCenters,ivec2(50+i*3,0),0);}
int fvCoilCacheBase(int field){return 0;}
void fvPrepareCache(float t){}
#else
#ifdef FC_SINGLE_COIL
const int FC_PEEL_CAPACITY = 16;
const int FC_TONGUE_CAPACITY = 6;
#else
const int FC_PEEL_CAPACITY = 48;
const int FC_TONGUE_CAPACITY = 12;
#endif

vec4 fvPeelRoots[FC_PEEL_CAPACITY];   // xyz: root, w: life
vec4 fvPeelAxes[FC_PEEL_CAPACITY];    // xyz: axis, w: length
vec3 fvPeelRadials[FC_PEEL_CAPACITY];
vec4 fvTongueRoots[FC_TONGUE_CAPACITY];   // xyz: root, w: length
vec4 fvTongueAxes[FC_TONGUE_CAPACITY];    // xyz: axis, w: life
vec4 fvTongueRadials[FC_TONGUE_CAPACITY]; // xyz: radial, w: random

vec4 fvGetPeelRoot(int i){return fvPeelRoots[i];}
vec4 fvGetPeelAxis(int i){return fvPeelAxes[i];}
vec3 fvGetPeelRadial(int i){return fvPeelRadials[i];}
vec4 fvGetTongueRoot(int i){return fvTongueRoots[i];}
vec4 fvGetTongueAxis(int i){return fvTongueAxes[i];}
vec4 fvGetTongueRadial(int i){return fvTongueRadials[i];}

int fvCoilCacheBase(int field) {
#ifdef FC_SINGLE_COIL
    return 0;
#else
    return field*16;
#endif
}

void fvPrepareCoilCache(int cacheBase,float t,float seed,float h,float radius,float widthInput,float turns,float wave,int count) {
    for(int j=0;j<8;j++) {
        if(j>=count)break;
        float strand=float(j),hierarchy=j==0?1.:(j==1?.58:.38);
        float width=widthInput*hierarchy;
        for(int i=0;i<2;i++) {
            int index=cacheBase+j*2+i;
            float k=float(i),clock=t*(.37+RiseSpeed*.15)+k*.413+strand*.193,age=fract(clock),birth=floor(clock);
            float life=smoothstep(0.,.12,age)*(1.-smoothstep(.56,1.,age));
            fvPeelRoots[index].w=life;
            if(life==0.)continue;
            float rootS=.10+.68*fvHash(vec3(k+strand*7.,seed,birth));
            vec3 root=fvCenter(rootS,strand,t,seed,radius,h,turns,wave,count);
            vec3 radial=normalize(vec3(root.x,0,root.z)),side=vec3(-radial.z,0,radial.x);
            float len=width*(1.25+TongueLength*.45)*(.5+.5*sin(age*FC_PI*.5));
            vec3 axis=normalize(vec3(0,1.,0)+radial*(.10+.12*age)+side*.14);
            fvPeelRoots[index]=vec4(root,life);
            fvPeelAxes[index]=vec4(axis,len);
            fvPeelRadials[index]=radial;
        }
    }
}

void fvPrepareWeaponTongues(int weapon,float t,float seed,float lengthBlade,float bladeRadius) {
    for(int j=0;j<6;j++) {
        int index=weapon*6+j;
        float k=float(j),clock=t*.43+k*.167,age=fract(clock),birth=floor(clock);
        float random=fvHash(vec3(k,seed,birth)),a=FC_TAU*random+t*RotationSpeed*.23;
        vec3 radial=vec3(cos(a),0,sin(a)),side=vec3(-sin(a),0,cos(a));
        vec3 root=radial*(bladeRadius+WeaponRadius*.25)+vec3(0,.04+lengthBlade*(.10+k*.13+.05*random),0);
        vec3 axis=normalize(radial*.42+vec3(0,1.0,0)+side*.36);
        float tongueLength=WeaponWidth*(1.9+.7*random)*(.45+.55*age);
        fvTongueRoots[index]=vec4(root,tongueLength);
        fvTongueAxes[index]=vec4(axis,sin(age*FC_PI));
        fvTongueRadials[index]=vec4(radial,random);
    }
}

void fvPrepareCache(float t) {
    float seed=float(Seed%997)*.071;
    if(BodyEnabled!=0)
        fvPrepareCoilCache(fvCoilCacheBase(0),t,seed,max(Height,.2),Radius,Width,Turns,WaveAmplitude,Strands);
    for(int j=0;j<2;j++) {
        if(j>=WeaponCount)break;
        float weaponSeed=seed+float(j)*2.13;
        fvPrepareCoilCache(fvCoilCacheBase(j+1),t,weaponSeed,max(.2,WeaponOrigins[j].w),
            WeaponAxes[j].w+WeaponRadius,WeaponWidth,WeaponTurns,WaveAmplitude*.25,Strands);
        fvPrepareWeaponTongues(j,t,weaponSeed,WeaponOrigins[j].w,WeaponAxes[j].w);
    }
}
#endif
// SHARED_PEEL_CACHE_END
#else
int fvCoilCacheBase(int field){return 0;}
void fvPrepareCache(float t){}
#endif

// Short flames peel upwards from the same moving body stream.
// Rounded, eroded cross sections avoid wide cloth-like sheets and graphic veins.
vec2 fvPeel(vec3 p,float t,float seed,float h,float radius,float width,float turns,float wave,int count,float strand,float strength,int cacheBase){
 float den=0.,hotWeight=0.;
 for(int i=0;i<2;i++){
#ifdef FC_DISABLE_CACHE
  float k=float(i),clock=t*(.37+RiseSpeed*.15)+k*.413+strand*.193,age=fract(clock),birth=floor(clock);
  float life=smoothstep(0.,.12,age)*(1.-smoothstep(.56,1.,age));
  if(life==0.)continue;
  float rootS=.10+.68*fvHash(vec3(k+strand*7.,seed,birth));
  vec3 root=fvCenter(rootS,strand,t,seed,radius,h,turns,wave,count);
  vec3 radial=normalize(vec3(root.x,0,root.z)),side=vec3(-radial.z,0,radial.x);
  float len=width*(1.25+TongueLength*.45)*(.5+.5*sin(age*FC_PI*.5));
  vec3 axis=normalize(vec3(0,1.,0)+radial*(.10+.12*age)+side*.14);
#else
  float k=float(i);int cacheIndex=cacheBase+int(strand)*2+i;
  vec4 rootLife=fvGetPeelRoot(cacheIndex);float life=rootLife.w;
  if(life==0.)continue;
  vec3 root=rootLife.xyz,radial=fvGetPeelRadial(cacheIndex),side=vec3(-radial.z,0,radial.x);
  vec4 cachedAxis=fvGetPeelAxis(cacheIndex);vec3 axis=cachedAxis.xyz;float len=cachedAxis.w;
#endif
  vec3 delta=p-root;
  // Every curved centre lies within .4*width of this segment. The minimum
  // eigenvalue of the radial/side/axis Gaussian metric is greater than .6.
  // Bound the complete density before Newton iteration or four-octave noise.
  vec3 capsuleDelta=delta-axis*clamp(dot(delta,axis),0.,len);
  float maxW=width*.33+.002;
  float support=width*.4+maxW*GaussianSupportScale;
  if(dot(capsuleDelta,capsuleDelta)>support*support)continue;
  float u=clamp(dot(delta,axis)/max(len,.01),0.,1.);
  for(int step=0;step<2;step++){
   vec3 bend=radial*(sin(u*3.8)*width*.18)+side*sin(u*5.-t*.8+k)*width*.22*u;
   vec3 derivative=axis*len+radial*cos(u*3.8)*width*.684+side*width*.22*(sin(u*5.-t*.8+k)+u*5.*cos(u*5.-t*.8+k));
   u=clamp(u+dot(delta-axis*(u*len)-bend,derivative)/max(dot(derivative,derivative),.001),0.,1.);
  }
  if(u>=1.)continue;
  vec3 bend=radial*(sin(u*3.8)*width*.18)+side*sin(u*5.-t*.8+k)*width*.22*u;
  vec3 off=delta-axis*(u*len)-bend;
  float taper=pow(1.-u,.8+.25*smoothstep(.45,.95,u)),w=width*(.20+.13*sin(u*FC_PI))*taper+.002;
  float x=dot(off,radial),z=dot(off,side),axial=dot(off,axis);
  float shape=exp(-x*x/(w*w*.70)-z*z/(w*w)-axial*axial/(w*w));
  if(shape*4.7*life*taper*strength<DensityCutoff)continue;
  float n=fvFbm(vec3(x*24.,u*3.-t*RiseSpeed*1.7,z*24.)+seed+k);
  float lace=smoothstep(.20+Erosion*.20,.57,n);
  float core=pow(shape,2.3)*smoothstep(.46,.72,n);
  float d=(shape*lace*3.1+core*1.6)*life*taper*strength;
  den+=d;hotWeight+=d*clamp(.26+n*.25+core*.39,0.,1.);
 }
 return vec2(den,hotWeight);
}

// Warp changes each coordinate by at most .045*Turbulence: each analytic
// flow component is a weighted sum of sin/cos with absolute bound one.
// Reject outside the complete unwarped support before evaluating its 24 trig
// operations. The capsule expansion includes both curve bending and warp.
bool fvCoilPossible(vec3 p,float h,float radiusInput,float widthInput,float waveInput,int strandCount,int cacheBase){
 float displacement=.045*abs(Turbulence)+.00001;
 float reach=1.801*abs(widthInput)+.0001;
 float radius=abs(radiusInput)+1.231*abs(waveInput)+reach+displacement*1.415;
 if(abs(p.y-(.055+h*.5))<=h*.5+.25*abs(waveInput)+reach+displacement&&dot(p.xz,p.xz)<=radius*radius)return true;
#ifndef FC_DISABLE_CACHE
 for(int j=0;j<8;j++){
  if(j>=strandCount)break;
  float width=widthInput*(j==0?1.:(j==1?.58:.38));
  float support=width*.4+(width*.33+.002)*GaussianSupportScale+displacement*1.733;
  for(int k=0;k<2;k++){
   int index=cacheBase+j*2+k;
   vec4 rootLife=fvGetPeelRoot(index);if(rootLife.w==0.)continue;
   vec4 cachedAxis=fvGetPeelAxis(index);vec3 delta=p-rootLife.xyz,axis=cachedAxis.xyz;
   delta-=axis*clamp(dot(delta,axis),0.,cachedAxis.w);
   if(dot(delta,delta)<=support*support)return true;
  }
 }
 return false;
#else
 return true;
#endif
}

vec2 fvCoilField(vec3 p,float t,float seed,float h,float radiusInput,float widthInput,float turnsInput,float waveInput,int strandCount,float strength,vec3 segmentStep,int cacheBase){
 if(strength<.0001||strandCount<=0)return vec2(0.);
 if(!fvCoilPossible(p,h,radiusInput,widthInput,waveInput,strandCount,cacheBase))return vec2(0.);
 float density=0.,heatWeight=0.;vec3 q=fvWarp(p,t,seed);
 // Every centre is inside this cylinder for s in [0,1]. The existing main
 // flame cutoff is 2.5*w and w <= .72*width*hierarchy. The rounded-up constants
 // leave float headroom; peeling tongues are evaluated outside this bound too.
 float centerRadius=abs(radiusInput)+1.231*abs(waveInput);
 float yFromMiddle=abs(q.y-(.055+h*.5));
 float radialSquared=dot(q.xz,q.xz);
#if defined(FC_POINT_GRID) && !defined(FC_DISABLE_CACHE)
 // One shared integer lookup covers all main strands at this unwarped point.
 uint pointMask=fvPointGridMask(p);
#endif
 for(int j=0;j<8;j++){
  if(j>=strandCount)break;float strand=float(j);
  float hierarchy=j==0?1.:(j==1?.58:.38);
  float sheetReach=1.801*abs(widthInput)*hierarchy+.0001;
  float sheetRadius=centerRadius+sheetReach;
  if(yFromMiddle<=h*.5+.25*abs(waveInput)+sheetReach&&radialSquared<=sheetRadius*sheetRadius
#if defined(FC_POINT_GRID) && !defined(FC_DISABLE_CACHE)
     &&(pointMask&(1u<<uint(j)))!=0u
#elif defined(FC_POINT_BINS) && !defined(FC_DISABLE_CACHE)
     &&fvPointBinSupported(p,j)
#elif defined(FC_POINT_SUPPORT) && !defined(FC_DISABLE_CACHE)
     &&fvPointMainSupported(p,h,widthInput,waveInput,hierarchy,j)
#endif
  ){
  float s=clamp((q.y-.055)/h,0.,1.);
  vec3 c=fvCenter(s,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount);
  for(int iteration=0;iteration<2;iteration++){
   vec3 tangent=(fvCenter(s+.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount)-fvCenter(s-.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount))/.006;
   s=clamp(s+dot(q-c,tangent)/max(dot(tangent,tangent),.01),0.,1.);
   c=fvCenter(s,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount);
  }
  // At either clamped endpoint ends is exactly zero. Peeling flames below
  // remain active; only the zero-contribution main stream is skipped.
  if(s>0.&&s<1.){
  vec3 tangent=normalize(fvCenter(s+.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount)-fvCenter(s-.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount));
  vec3 radial=normalize(vec3(c.x,0,c.z));radial=normalize(radial-tangent*dot(radial,tangent));
  vec3 across=normalize(cross(tangent,radial)),delta=q-c;
  float x=dot(delta,radial),z=dot(delta,across);
  float along=s*(8.+turnsInput*2.)-t*RiseSpeed*1.25+strand*11.;
  float crest=.5+.5*sin(along*.82+.55*sin(along*1.63+strand));
  float ends=pow(max(0.,sin(s*FC_PI)),.65);
  float w=widthInput*hierarchy*(.30+.42*pow(crest,1.3))*(.42+.58*ends);
  if(length(delta)<w*2.5){
   vec3 flow=vec3(x/max(w,.01)*1.7,along/max(TongueLength,.1),z/max(w,.01)*1.7)+seed;
   float broad=fvFbm(flow);
   float lace=smoothstep(.21+Erosion*.22,.57+Erosion*.10,broad+.05*crest);
   // Gas/fringe require lace; the hot core requires broad above .34. When
   // both gates are zero the finer noise and fibre cannot affect the field.
   if(lace>0.||broad>.34){
   float fine=fvFbm(flow*2.41+vec3(1.7,-t*.47,4.1));
   vec2 bend=vec2(broad-.5,fine-.5)*w*.62;
   vec2 local=vec2(x,z)-bend;
   float shape=exp(-(local.x*local.x*.82+local.y*local.y)/(w*w*.55));
   float breaks=smoothstep(.25,.57,fvNoise(vec3(s*6.7-t*RiseSpeed*.52,seed+strand*7.,strand+3.1)));
   float pulse=.28+.72*breaks;
   float body=shape*lace*pulse*ends;
   // Hot areas are short, advected noise peaks, never periodic painted lines.
   vec2 segment=vec2(dot(segmentStep,radial),dot(segmentStep,across));
   float hotNoise=smoothstep(.47,.72,fine)*smoothstep(.34,.66,broad);
   float core=fvFibre(local,segment,max(.003,w*.27))*hotNoise*ends*pulse;
   float fringe=exp(-(local.x*local.x+local.y*local.y)/(w*w*1.05))*lace*ends*pulse*.04;
   float d=(body*3.8+core*1.7+fringe)*strength;
   float heat=clamp(.24+fine*.23+pow(shape,.8)*.20+core*.28,0.,1.);
   density+=d;heatWeight+=d*heat;
   }
  }
  }
  }
  vec2 peel=fvPeel(q,t,seed,h,radiusInput,widthInput*hierarchy,turnsInput,waveInput,strandCount,strand,strength,cacheBase);
  density+=peel.x;heatWeight+=peel.y;
 }
 return vec2(density,heatWeight);
}

vec2 fvWeaponTongues(vec3 p,float t,float seed,float lengthBlade,float bladeRadius,int weaponIndex){
 float density=0.,heatWeight=0.;
 float reach=WeaponWidth*4.1+.15;
 if(p.y<-.15||p.y>lengthBlade+reach||length(p.xz)>bladeRadius+WeaponRadius+reach)return vec2(0.);
 for(int j=0;j<6;j++){
#ifdef FC_DISABLE_CACHE
  float k=float(j),clock=t*.43+k*.167,age=fract(clock),birth=floor(clock);
  float random=fvHash(vec3(k,seed,birth)),a=FC_TAU*random+t*RotationSpeed*.23;
  vec3 radial=vec3(cos(a),0,sin(a)),side=vec3(-sin(a),0,cos(a));
  vec3 root=radial*(bladeRadius+WeaponRadius*.25)+vec3(0,.04+lengthBlade*(.10+k*.13+.05*random),0);
  vec3 axis=normalize(radial*.42+vec3(0,1.0,0)+side*.36);
  float tongueLength=WeaponWidth*(1.9+.7*random)*(.45+.55*age);
  float life=sin(age*FC_PI);
#else
  float k=float(j);int cacheIndex=weaponIndex*6+j;
  vec4 rootLength=fvGetTongueRoot(cacheIndex),cachedRadial=fvGetTongueRadial(cacheIndex),cachedAxis=fvGetTongueAxis(cacheIndex);
  vec3 root=rootLength.xyz,radial=cachedRadial.xyz,side=vec3(-radial.z,0,radial.x);
  float random=cachedRadial.w;
  vec3 axis=cachedAxis.xyz;float tongueLength=rootLength.w;
  float life=cachedAxis.w;
#endif
  // Before the 24-trig warp, bound the complete tongue by its straight axis
  // capsule: bend <= .82*width, Gaussian width <= .54*width+.002, and the
  // warp's vector displacement <= sqrt(3)*.045*abs(Turbulence).
  vec3 unwarped=p-root;
  vec3 capsuleDelta=unwarped-axis*clamp(dot(unwarped,axis),0.,tongueLength);
  float maxTongueWidth=WeaponWidth*.54+.002;
  float support=WeaponWidth*.82+sqrt(max(maxTongueWidth*maxTongueWidth,.000012)*GaussianExponentLimit)+.077944*abs(Turbulence)+.001;
  if(dot(capsuleDelta,capsuleDelta)>support*support)continue;
  vec3 delta=fvWarp(unwarped,t*RiseSpeed,seed+k*2.7);
  float s=dot(delta,axis)/max(tongueLength,.02);
  if(s<0.||s>1.)continue;
  vec3 bend=side*sin(s*4.1-t*WaveSpeed+random*13.)*WeaponWidth*.56*s+radial*sin(s*3.7+t*.7)*WeaponWidth*.26*s;
  vec3 crossDelta=delta-axis*(s*tongueLength)-bend;
  float width=WeaponWidth*(.24+.30*sin(s*FC_PI))*(1.-s)+.002;
  float ends=smoothstep(0.,.08,s)*(1.-smoothstep(.58,1.,s));
  if(life==0.||ends==0.)continue;
  float maxStrength=5.1*life*ends*VolumeStrength*WeaponStrength;
  if(dot(crossDelta,crossDelta)>max(width*width,.000012)*GaussianExponentLimit)continue;
  float gasShape=exp(-dot(crossDelta,crossDelta)/(width*width));
  float hotShape=exp(-dot(crossDelta,crossDelta)/max(width*width*.18,.000012));
  if((gasShape+hotShape)*maxStrength<DensityCutoff)continue;
  float noise=fvFbm(delta*vec3(21.,9.,21.)+vec3(seed,-t*RiseSpeed*1.3,k));
  float gas=gasShape*smoothstep(.18+Erosion*.20,.55,noise)*life*ends;
  float hot=hotShape*life*ends*smoothstep(.40,.70,noise);
  float den=(gas*3.4+hot*1.7)*VolumeStrength*WeaponStrength;
  density+=den;heatWeight+=den*clamp(.29+.25*noise+.36*hot,0.,1.);
 }
 return vec2(density,heatWeight);
}

vec2 sampleFireVolume(vec3 p, float t) {
#if defined(FC_POINT_SUPPORT) && !defined(FC_DISABLE_CACHE)
    fvSupportQueryPoint=p;
#endif
    float seed = float(Seed%997)*.071;
    float density = 0.0, heatWeight = 0.0;
    float h = max(Height,.2);

    if(BodyEnabled!=0){
        vec2 body=fvCoilField(p,t,seed,h,Radius,Width,Turns,WaveAmplitude,Strands,VolumeStrength,DensityStep,fvCoilCacheBase(0));
        // Leave the face open while side/back shoulder flames remain visible.
        float face=smoothstep(1.34,1.49,p.y)*(1.-smoothstep(.16,.30,abs(p.x)))*smoothstep(-.02,.18,p.z);
        body*=1.-face*.94;
        density+=body.x;heatWeight+=body.y;
    }
    for(int j=0;j<2;j++){
        if(j>=WeaponCount)break;
        vec3 delta=p-WeaponOrigins[j].xyz;
        vec3 axis=WeaponAxes[j].xyz,side=WeaponSides[j].xyz,other=cross(side,axis);
        vec3 localPoint=vec3(dot(delta,side),dot(delta,axis),dot(delta,other));
        float modelRadius=WeaponAxes[j].w;
        vec2 weapon=fvCoilField(localPoint,t,seed+float(j)*2.13,max(.2,WeaponOrigins[j].w),
            modelRadius+WeaponRadius,WeaponWidth,WeaponTurns,WaveAmplitude*.25,Strands,VolumeStrength*WeaponStrength,vec3(dot(DensityStep,side),dot(DensityStep,axis),dot(DensityStep,other)),fvCoilCacheBase(j+1));
        // Broken travelling flame groups leave the blade silhouette readable.
        // This only removes density, so the conservative support bounds remain valid.
        float bladeAlong=localPoint.y/max(.2,WeaponOrigins[j].w);
        float opening=smoothstep(-.40,.28,sin(bladeAlong*FC_TAU*2.35-t*1.65+seed));
        float rootFade=smoothstep(0.,.015+WeaponWidth*.30,localPoint.y);
        weapon*=mix(.10,1.,opening)*rootFade;
        density+=weapon.x;heatWeight+=weapon.y;
        vec2 tongues=fvWeaponTongues(localPoint,t,seed+float(j)*2.13,WeaponOrigins[j].w,modelRadius,j);
        tongues*=rootFade;
        density+=tongues.x;heatWeight+=tongues.y;
    }

    for(int j=0;j<24;j++) {
        if(j>=WispCount) break;
        float size=max(Wisps[j].w,.01);
        vec3 d=p-Wisps[j].xyz;
        if(dot(d,d)>size*size*11.5) continue;
        float age=clamp(WispMeta[j].x,0.0,1.0), strength=WispMeta[j].y;
        float life=smoothstep(0.0,.11,age)*pow(max(0.0,1.0-age),1.05);
        if(life==0.||strength==0.)continue;
        vec3 axis=normalize(WispVelocity[j].xyz*.85+vec3(0,.65,0));
        vec3 side=normalize(cross(axis,abs(axis.y)>.9?vec3(1,0,0):vec3(0,1,0)));
        vec3 other=cross(axis,side);
        float ws=WispVelocity[j].w;
        vec3 v=vec3(dot(d,side),dot(d,axis),dot(d,other))/size;
        float flow=t*RiseSpeed+ws*19.7;
        v=fvWarp(v*.34,flow,ws*7.1)/.34;
        float stretch=.85+TongueLength*.71;
        float y=v.y/stretch;
        vec2 bend=vec2(sin(y*2.7-flow*.8+ws*7.0),cos(y*3.1+flow*.67))*Turbulence*.24;
        vec2 lateral=v.xz-bend*(.7+.3*y);
        float profile=exp(-y*y*1.5);
        float r=.21+.25*profile;
        float gas=exp(-dot(lateral,lateral)/(r*r))*profile;
        // Holes is in [0,1], so these un-eroded fields bound all wisp noise.
        float hotBound=exp(-dot(lateral,lateral)/(.026+.019*profile))*profile;
        if((gas*.65+hotBound*2.8)*life*strength<DensityCutoff)continue;
        float n=fvFbm(vec3(lateral*2.9,y*2.0-flow*.7)+ws*37.0);
        float holes=smoothstep(.18+Erosion*.28,.49+Erosion*.18,n);
        float hot=hotBound*holes;
        float den=(gas*.65*holes+hot*2.8)*life*strength;
        float heat=clamp(.22+n*.24+pow(hot,.61)*.70,0.0,1.0);
        density+=den;
        heatWeight+=den*heat;
    }
    return vec2(density,heatWeight/max(density,.00001));
}

vec3 fireVolumeColor(float heat) {
    vec3 color=mix(EdgeColor,MidColor,smoothstep(.15,.59,heat));
    return mix(color,CoreColor,smoothstep(.48,.80,heat));
}

#ifndef FC_DISABLE_CACHE
// SHARED_RAY_SUPPORT_BEGIN
// Conservative ray support. The original fixed integration indices, midpoint
// positions and step lengths remain unchanged; an empty bit skips a zero field.
uniform sampler2D SupportSpheres;
uniform int SupportSphereCount;
#ifdef FC_POINT_GRID
uniform usampler2D PointGrid;
uniform vec3 PointGridMin,PointGridInvVoxel;
uniform int PointGridSize;
// Every occupied voxel includes every strand support sphere intersecting its
// full cell box. An integer texel keeps all strand bits; no filtering is used.
uint fvPointGridMask(vec3 localPoint){
 if(SupportSphereCount<0||PointGridSize<=0||any(lessThanEqual(PointGridInvVoxel,vec3(0.))))return 0xffu;
 vec3 voxel=(localPoint-PointGridMin)*PointGridInvVoxel;
 if(any(lessThan(voxel,vec3(0.)))||any(greaterThanEqual(voxel,vec3(float(PointGridSize)))))return 0u;
 ivec3 cell=ivec3(floor(voxel));
 return texelFetch(PointGrid,ivec2(cell.x+cell.y*PointGridSize,cell.z),0).r;
}
#endif
#ifdef FC_POINT_BINS
// Each native single-field draw stores a conservative local support AABB for
// every strand/y bin. Metadata and bounds live after its at-most-256 balls.
// These boxes already include warp, so query the unwarped local field point.
bool fvPointBinSupported(vec3 localPoint,int strand){
 if(SupportSphereCount<32*(strand+1)||SupportSphereCount>256)return true;
 vec4 meta=texelFetch(SupportSpheres,ivec2(strand,2),0);
 if(meta.w<.5||meta.y<=0.)return true;
 if(localPoint.y<meta.x||localPoint.y>meta.z)return false;
 int bin=clamp(int(floor((localPoint.y-meta.x)*meta.y)),0,31);
 vec4 bounds=texelFetch(SupportSpheres,ivec2(strand*32+bin,1),0);
 return localPoint.x>=bounds.x&&localPoint.x<=bounds.y&&localPoint.z>=bounds.z&&localPoint.z<=bounds.w;
}
#endif
#ifdef FC_POINT_SUPPORT
// Main density can be nonzero only within 2.5*w of its final curve point.
// w<=.72*width*hierarchy, curve y differs from .055+s*h by at most
// .25*abs(wave), and warp changes y by at most .045*abs(Turbulence).
// This y interval therefore includes every segment that could contribute.
bool fvPointMainSupported(vec3 localPoint,float h,float widthInput,float waveInput,float hierarchy,int strand){
 // Negative clocks or an incomplete table retain the unculled analytic path.
 if(SupportSphereCount<32*(strand+1))return true;
 float reach=1.801*abs(widthInput)*hierarchy+.25*abs(waveInput)+.045*abs(Turbulence)+.001;
 int lo=max(0,int(floor((localPoint.y-.055-reach)/h*32.))-1);
 int hi=min(31,int(floor((localPoint.y-.055+reach)/h*32.))+1);
 for(int segment=lo;segment<=hi;segment++){
  int index=strand*32+segment;
  vec4 sphere=texelFetch(SupportSpheres,ivec2(index%256,index/256),0);
  vec3 delta=fvSupportQueryPoint-sphere.xyz;
  if(dot(delta,delta)<=sphere.w*sphere.w)return true;
 }
 return false;
}
#endif
uint fvRayMask[16];
vec3 fvSupportOrigin,fvSupportDirection;
float fvSupportFirst,fvSupportLast,fvSupportStep;
int fvSupportSteps;

void fvMarkRayInterval(float enter,float leave){
 if(leave<fvSupportFirst||enter>fvSupportLast)return;
 // An extra sample on each side protects rounding at interval endpoints.
 int lo=max(0,int(floor((max(enter,fvSupportFirst)-fvSupportFirst)/fvSupportStep-.5))-1);
 int hi=min(fvSupportSteps-1,int(ceil((min(leave,fvSupportLast)-fvSupportFirst)/fvSupportStep-.5))+1);
 int firstWord=lo/32,lastWord=hi/32;
 for(int word=firstWord;word<=lastWord;word++){
  uint bits=0xffffffffu;
  if(word==firstWord)bits&=0xffffffffu<<uint(lo&31);
  if(word==lastWord)bits&=0xffffffffu>>uint(31-(hi&31));
  fvRayMask[word]|=bits;
 }
}

void fvMarkSupportSphere(vec3 center,float radius){
 vec3 delta=center-fvSupportOrigin;
 float along=dot(delta,fvSupportDirection);
 float squared=radius*radius-(dot(delta,delta)-along*along);
 if(squared<0.)return;
 float reach=sqrt(max(0.,squared));
 fvMarkRayInterval(along-reach,along+reach);
}

vec3 fvSupportWeaponPoint(vec3 p,int weapon){
 vec3 side=WeaponSides[weapon].xyz,axis=WeaponAxes[weapon].xyz;
 float ss=dot(side,side),aa=dot(axis,axis),sa=dot(side,axis),det=max(1.e-8,ss*aa-sa*sa);
 return WeaponOrigins[weapon].xyz+(side*(aa*p.x-sa*p.y)+axis*(ss*p.y-sa*p.x)+cross(side,axis)*p.z)/det;
}

float fvSupportWeaponStretch(int weapon){
 vec3 side=WeaponSides[weapon].xyz,axis=WeaponAxes[weapon].xyz;
 float ss=dot(side,side),aa=dot(axis,axis),sa=dot(side,axis),det=ss*aa-sa*sa;
 return 1.00001/sqrt(max(1.e-8,min(det,.5*(ss+aa-length(vec2(ss-aa,2.*sa))))));
}

void fvMarkPeelSupport(int cacheBase,int weapon,float widthInput){
 float warp=.077944*Turbulence+.001;
 float stretch=weapon<0?1.:fvSupportWeaponStretch(weapon);
 for(int j=0;j<8;j++){
  if(j>=Strands)break;
  float hierarchy=j==0?1.:(j==1?.58:.38),width=widthInput*hierarchy;
  float support=width*.4+(width*.33+.002)*sqrt(GaussianExponentLimit/.6)+warp;
  for(int k=0;k<2;k++){
   int index=cacheBase+j*2+k;
   vec4 rootLife=fvGetPeelRoot(index);if(rootLife.w==0.)continue;
   vec4 cachedAxis=fvGetPeelAxis(index);float len=cachedAxis.w;
   vec3 center=rootLife.xyz+cachedAxis.xyz*(len*.5);
   if(weapon>=0)center=fvSupportWeaponPoint(center,weapon);
   fvMarkSupportSphere(center,(len*.5+support)*stretch);
  }
 }
}

void fvPrepareRaySupport(vec3 ro,vec3 rd,float first,float last,float stepLength,int steps){
 if(SupportSphereCount<0){for(int word=0;word<16;word++)fvRayMask[word]=0xffffffffu;return;}
 for(int word=0;word<16;word++)fvRayMask[word]=0u;
 fvSupportOrigin=ro;fvSupportDirection=rd;
 fvSupportFirst=first;fvSupportLast=last;fvSupportStep=max(stepLength,1.e-8);fvSupportSteps=steps;
 for(int j=0;j<768;j++){
  if(j>=SupportSphereCount)break;
  vec4 sphere=texelFetch(SupportSpheres,ivec2(j%256,j/256),0);
  fvMarkSupportSphere(sphere.xyz,sphere.w);
 }
 if(BodyEnabled!=0)fvMarkPeelSupport(fvCoilCacheBase(0),-1,Width);
 for(int weapon=0;weapon<2;weapon++){
  if(weapon>=WeaponCount)break;
  fvMarkPeelSupport(fvCoilCacheBase(weapon+1),weapon,WeaponWidth);
  float width=WeaponWidth*.54+.002;
  float support=WeaponWidth*.82+sqrt(max(width*width,.000012)*GaussianExponentLimit)+.077944*Turbulence+.001;
  float stretch=fvSupportWeaponStretch(weapon);
  for(int j=0;j<6;j++){
   int index=weapon*6+j;
   vec4 cachedAxis=fvGetTongueAxis(index);if(cachedAxis.w==0.)continue;
   vec4 rootLength=fvGetTongueRoot(index);float len=rootLength.w;
   vec3 center=rootLength.xyz+cachedAxis.xyz*(len*.5);
   fvMarkSupportSphere(fvSupportWeaponPoint(center,weapon),(len*.5+support)*stretch);
  }
 }
 for(int j=0;j<24;j++){
  if(j>=WispCount)break;
  fvMarkSupportSphere(Wisps[j].xyz,max(Wisps[j].w,.01)*3.391166+.001);
 }
}

bool fvRaySampleSupported(int index){return (fvRayMask[index/32]&(1u<<uint(index&31)))!=0u;}
bool fvRayWordEmpty(int index){return fvRayMask[index/32]==0u;}
bool fvRayAnySupported(){
 uint anyBits=0u;for(int word=0;word<16;word++)anyBits|=fvRayMask[word];
 return anyBits!=0u;
}
// SHARED_RAY_SUPPORT_END
#else
void fvPrepareRaySupport(vec3 ro,vec3 rd,float first,float last,float stepLength,int steps){}
bool fvRaySampleSupported(int index){return true;}
bool fvRayAnySupported(){return true;}
bool fvRayWordEmpty(int index){return false;}
#endif


// SHARED_VOLUME_CORE_END

vec3 bodyDirection(vec3 v){float c=cos(Yaw),s=sin(Yaw);return vec3(c*v.x+s*v.z,v.y,-s*v.x+c*v.z);}
vec2 volumeBoxHit(vec3 ro,vec3 rd){
 vec3 safe=sign(rd)*max(abs(rd),vec3(.00001));safe+=vec3(equal(safe,vec3(0)))*.00001;
 vec3 a=(BoundsMin-ro)/safe,b=(BoundsMax-ro)/safe,mn=min(a,b),mx=max(a,b);
 return vec2(max(mn.x,max(mn.y,mn.z)),min(mx.x,min(mx.y,mx.z)));
}
void main(){
 vec2 uv=gl_FragCoord.xy/Viewport;
 // Iris compresses hand clip-z to +/-0.125. Clip z=1 then unprojects
 // behind the eye; z=0 is inside both ordinary and hand projections.
 vec4 farPoint=InverseViewProjection*vec4(uv*2.0-1.0,ItemSpace!=0?0.0:1.0,1.0);
 vec3 rd=ItemSpace!=0?normalize(farPoint.xyz/farPoint.w-Camera):bodyDirection(normalize(farPoint.xyz/farPoint.w));
 vec2 hit=volumeBoxHit(Camera,rd);
 float depth=texture(SceneDepth,uv).r;
 vec4 surface=InverseViewProjection*vec4(uv*2.0-1.0,depth*2.0-1.0,1.0);
 float sceneDistance=depth>=.999999||surface.w<=0.0?10000.0:(ItemSpace!=0?length(surface.xyz/surface.w-Camera):length(surface.xyz/surface.w));
 float first=max(0.0,hit.x),last=min(hit.y,sceneDistance);
 if(last<=first){result=vec4(0);return;}
 float ds=(last-first)/float(Steps),transmittance=1.0;
 vec3 light=vec3(0);DensityStep=rd*(ds*ModelScale);
 prepareFireVolume(Time,(last-first)*ModelScale,Intensity,Opacity);
#ifndef FC_DISABLE_RAY_SUPPORT
 fvPrepareRaySupport(Camera*ModelScale,rd,first*ModelScale,last*ModelScale,ds*ModelScale,Steps);
 if(!fvRayAnySupported()){result=vec4(0);return;}
#endif
 // Stable midpoint integration; temporal random dithering would shimmer in motion.
 for(int i=0;i<512;i++){
  if(i>=Steps||transmittance<.008)break;
#ifndef FC_DISABLE_RAY_SUPPORT
  if(fvRayWordEmpty(i)){i=(i/32+1)*32-1;continue;}
  if(!fvRaySampleSupported(i))continue;
#endif
  vec3 p=Camera+rd*(first+(float(i)+.5)*ds);
  // Actual item transforms retain their animation; the shared field is always
  // evaluated in metres, including noise, curl and analytic fibre integration.
  vec2 field=sampleFireVolume(p*ModelScale,Time);
  float alpha=1.0-exp(-max(0.0,field.x)*(ds*ModelScale)*Opacity*5.0);
  light+=transmittance*fireVolumeColor(field.y)*Intensity*alpha;
  transmittance*=1.0-alpha;
 }
 float alpha=1.0-transmittance;
 // Same premultiplied emission as the approved native-resolution workbench.
 result=vec4(light,alpha);
}
