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
vec3 fvCenter(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 float phase=strand/float(max(strandCount,1))+.09*sin(strand*2.4);
 float wave=s*8.7-t*WaveSpeed*.8+seed*2.1+strand*2.3;
 float sweep=s+.12*sin(s*6.283-t*.31+strand*1.7)+.045*sin(s*13.-t*.47+strand);
 float angle=FC_TAU*(sweep*turnsInput+phase+t*RotationSpeed*.48);
 angle+=.24*sin(wave*.71)+.10*sin(wave*1.63);
 float envelope=max(0.,sin(s*FC_PI));
 float radius=radiusInput*(.84+.16*envelope)+waveInput*(sin(wave)*.85+sin(wave*1.63)*.38)*(.65+.35*envelope);
 return vec3(cos(angle)*radius,.055+s*heightInput+waveInput*.25*sin(wave*1.2),sin(angle)*radius);
}

// Short flames peel upwards from the same moving body stream.
// Rounded, eroded cross sections avoid wide cloth-like sheets and graphic veins.
vec2 fvPeel(vec3 p,float t,float seed,float h,float radius,float width,float turns,float wave,int count,float strand,float strength){
 float den=0.,hotWeight=0.;
 for(int i=0;i<2;i++){
  float k=float(i),clock=t*(.37+RiseSpeed*.15)+k*.413+strand*.193,age=fract(clock),birth=floor(clock);
  float life=smoothstep(0.,.12,age)*(1.-smoothstep(.56,1.,age));
  if(life==0.)continue;
  float rootS=.10+.68*fvHash(vec3(k+strand*7.,seed,birth));
  vec3 root=fvCenter(rootS,strand,t,seed,radius,h,turns,wave,count);
  vec3 radial=normalize(vec3(root.x,0,root.z)),side=vec3(-radial.z,0,radial.x);
  float len=width*(1.25+TongueLength*.45)*(.5+.5*sin(age*FC_PI*.5));
  vec3 axis=normalize(vec3(0,1.,0)+radial*(.10+.12*age)+side*.14);
  vec3 delta=p-root;float u=clamp(dot(delta,axis)/max(len,.01),0.,1.);
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
  if(shape==0.)continue;
  float n=fvFbm(vec3(x*24.,u*3.-t*RiseSpeed*1.7,z*24.)+seed+k);
  float lace=smoothstep(.20+Erosion*.20,.57,n);
  float core=pow(shape,2.3)*smoothstep(.46,.72,n);
  float d=(shape*lace*3.1+core*1.6)*life*taper*strength;
  den+=d;hotWeight+=d*clamp(.26+n*.25+core*.39,0.,1.);
 }
 return vec2(den,hotWeight);
}

vec2 fvCoilField(vec3 p,float t,float seed,float h,float radiusInput,float widthInput,float turnsInput,float waveInput,int strandCount,float strength,vec3 segmentStep){
 if(strength<.0001||strandCount<=0)return vec2(0.);
 float density=0.,heatWeight=0.;vec3 q=fvWarp(p,t,seed);
 // Every centre is inside this cylinder for s in [0,1]. The existing main
 // flame cutoff is 2.5*w and w <= .72*width*hierarchy. The rounded-up constants
 // leave float headroom; peeling tongues are evaluated outside this bound too.
 float centerRadius=abs(radiusInput)+1.231*abs(waveInput);
 float yFromMiddle=abs(q.y-(.055+h*.5));
 float radialSquared=dot(q.xz,q.xz);
 for(int j=0;j<8;j++){
  if(j>=strandCount)break;float strand=float(j);
  float hierarchy=j==0?1.:(j==1?.58:.38);
  float sheetReach=1.801*abs(widthInput)*hierarchy+.0001;
  float sheetRadius=centerRadius+sheetReach;
  if(yFromMiddle<=h*.5+.25*abs(waveInput)+sheetReach&&radialSquared<=sheetRadius*sheetRadius){
  float s=clamp((q.y-.055)/h,0.,1.);
  vec3 c=fvCenter(s,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount);
  for(int iteration=0;iteration<2;iteration++){
   vec3 tangent=(fvCenter(s+.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount)-fvCenter(s-.003,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount))/.006;
   s=clamp(s+dot(q-c,tangent)/max(dot(tangent,tangent),.01),0.,1.);
   c=fvCenter(s,strand,t,seed,radiusInput,h,turnsInput,waveInput,strandCount);
  }
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
   float broad=fvFbm(flow),fine=fvFbm(flow*2.41+vec3(1.7,-t*.47,4.1));
   vec2 bend=vec2(broad-.5,fine-.5)*w*.62;
   vec2 local=vec2(x,z)-bend;
   float shape=exp(-(local.x*local.x*.82+local.y*local.y)/(w*w*.55));
   float lace=smoothstep(.21+Erosion*.22,.57+Erosion*.10,broad+.05*crest);
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
  vec2 peel=fvPeel(q,t,seed,h,radiusInput,widthInput*hierarchy,turnsInput,waveInput,strandCount,strand,strength);
  density+=peel.x;heatWeight+=peel.y;
 }
 return vec2(density,heatWeight);
}

vec2 fvWeaponTongues(vec3 p,float t,float seed,float lengthBlade,float bladeRadius){
 float density=0.,heatWeight=0.;
 float reach=WeaponWidth*4.1+.15;
 if(p.y<-.15||p.y>lengthBlade+reach||length(p.xz)>bladeRadius+WeaponRadius+reach)return vec2(0.);
 for(int j=0;j<6;j++){
  float k=float(j),clock=t*.43+k*.167,age=fract(clock),birth=floor(clock);
  float random=fvHash(vec3(k,seed,birth)),a=FC_TAU*random+t*RotationSpeed*.23;
  vec3 radial=vec3(cos(a),0,sin(a)),side=vec3(-sin(a),0,cos(a));
  vec3 root=radial*(bladeRadius+WeaponRadius*.25)+vec3(0,.04+lengthBlade*(.10+k*.13+.05*random),0);
  vec3 axis=normalize(radial*.42+vec3(0,1.0,0)+side*.36);
  float tongueLength=WeaponWidth*(1.9+.7*random)*(.45+.55*age);
  vec3 delta=fvWarp(p-root,t*RiseSpeed,seed+k*2.7);
  float s=dot(delta,axis)/max(tongueLength,.02);
  if(s<0.||s>1.)continue;
  vec3 bend=side*sin(s*4.1-t*WaveSpeed+random*13.)*WeaponWidth*.56*s+radial*sin(s*3.7+t*.7)*WeaponWidth*.26*s;
  vec3 crossDelta=delta-axis*(s*tongueLength)-bend;
  float width=WeaponWidth*(.24+.30*sin(s*FC_PI))*(1.-s)+.002;
  float life=sin(age*FC_PI),ends=smoothstep(0.,.08,s)*(1.-smoothstep(.58,1.,s));
  if(life==0.||ends==0.)continue;
  float gasShape=exp(-dot(crossDelta,crossDelta)/(width*width));
  float hotShape=exp(-dot(crossDelta,crossDelta)/max(width*width*.18,.000012));
  if(gasShape==0.&&hotShape==0.)continue;
  float noise=fvFbm(delta*vec3(21.,9.,21.)+vec3(seed,-t*RiseSpeed*1.3,k));
  float gas=gasShape*smoothstep(.18+Erosion*.20,.55,noise)*life*ends;
  float hot=hotShape*life*ends*smoothstep(.40,.70,noise);
  float den=(gas*3.4+hot*1.7)*VolumeStrength*WeaponStrength;
  density+=den;heatWeight+=den*clamp(.29+.25*noise+.36*hot,0.,1.);
 }
 return vec2(density,heatWeight);
}

vec2 sampleFireVolume(vec3 p, float t) {
    float seed = float(Seed%997)*.071;
    float density = 0.0, heatWeight = 0.0;
    float h = max(Height,.2);

    if(BodyEnabled!=0){
        vec2 body=fvCoilField(p,t,seed,h,Radius,Width,Turns,WaveAmplitude,Strands,VolumeStrength,DensityStep);
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
            modelRadius+WeaponRadius,WeaponWidth,WeaponTurns,WaveAmplitude*.25,Strands,VolumeStrength*WeaponStrength,vec3(dot(DensityStep,side),dot(DensityStep,axis),dot(DensityStep,other)));
        // New blade art only; this frozen oracle retains its independent
        // uncached, unculled density implementation for equivalence checks.
        float bladeAlong=localPoint.y/max(.2,WeaponOrigins[j].w);
        float opening=smoothstep(-.40,.28,sin(bladeAlong*FC_TAU*2.35-t*1.65+seed));
        float rootFade=smoothstep(0.,.015+WeaponWidth*.30,localPoint.y);
        weapon*=mix(.10,1.,opening)*rootFade;
        density+=weapon.x;heatWeight+=weapon.y;
        vec2 tongues=fvWeaponTongues(localPoint,t,seed+float(j)*2.13,WeaponOrigins[j].w,modelRadius);
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
        float n=fvFbm(vec3(lateral*2.9,y*2.0-flow*.7)+ws*37.0);
        float gas=exp(-dot(lateral,lateral)/(r*r))*profile;
        float holes=smoothstep(.18+Erosion*.28,.49+Erosion*.18,n);
        float hot=exp(-dot(lateral,lateral)/(.026+.019*profile))*profile*holes;
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
