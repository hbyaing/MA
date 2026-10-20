#version 150
uniform vec2 Viewport;
uniform mat4 InverseLocalProjection;
uniform sampler2D SceneDepth;
uniform vec3 BoundsMin, BoundsMax;
uniform int Steps;
uniform float FireLift;
out vec4 result;

// SHARED_VOLUME_CORE_BEGIN
// Original procedural three-dimensional field. Coordinates in actual furnace metres.
// Colours are density weighted before integration; opacity and intensity occur once.
uniform float Time, Phase;
uniform float Nirvana;
uniform float BottomOnly;
uniform sampler3D SurfaceClearance;uniform float SurfaceMaskEnabled;
uniform sampler3D FireHashLow,FireHashHigh;
uniform vec3 HashOrigin;uniform float HashCacheEnabled;
uniform sampler3D NoiseHashLow,NoiseHashHigh;
uniform int Stage, Seed;
uniform float FireRadius, FireHeight, FireStrength;
uniform float VaporRadius, VaporHeight, VaporSpread, VaporStrength;
uniform float FlowSpeed, Turbulence, FilamentWidth, FilamentStrength;
uniform float OrbitRadius, OrbitSpeed, CoreRadius, CoreStrength;
uniform float BurstRadius, BurstStrength, SigilRadius, SigilStrength;
uniform float Intensity, Opacity;
uniform vec3 FireCore, FireMid, FireEdge, VaporCore, VaporMid, VaporEdge, CoreColor;
const float AV_PI=3.14159265359, AV_TAU=6.28318530718;
const vec3 AV_FIRE=vec3(0,.045,0), AV_MATERIAL=vec3(0,1.38,0);
const vec3 AV_MOUTH=vec3(0,3.2417925,0), AV_RESULT=vec3(0,4.0017925,0);
float avHash(vec3 p){p=fract(p*vec3(.1031,.1030,.0973));p+=dot(p,p.yxz+33.33);return fract((p.x+p.y)*p.z);}
float avNoiseCorner(vec3 p){
#if defined(WUXING_BOTTOM_FIRE) || defined(WUXING_UPPER_ALCHEMY)
 return avHash(mod(p,64.));
#else
 return avHash(p);
#endif
}
float avNoise(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*f*(f*(f*6.-15.)+10.);
#ifdef WUXING_NOISE_CACHE
 ivec3 cell=ivec3(mod(i,64.));
 vec4 low=texelFetch(NoiseHashLow,cell,0),high=texelFetch(NoiseHashHigh,cell,0);
 float a=low.x,b=low.y,c=low.z,d=low.w,e=high.x,j=high.y,g=high.z,h=high.w;
#else
 float a=avNoiseCorner(i),b=avNoiseCorner(i+vec3(1,0,0)),c=avNoiseCorner(i+vec3(0,1,0)),d=avNoiseCorner(i+vec3(1,1,0));
 float e=avNoiseCorner(i+vec3(0,0,1)),j=avNoiseCorner(i+vec3(1,0,1)),g=avNoiseCorner(i+vec3(0,1,1)),h=avNoiseCorner(i+vec3(1,1,1));
#endif
 return mix(mix(mix(a,b,f.x),mix(c,d,f.x),f.y),mix(mix(e,j,f.x),mix(g,h,f.x),f.y),f.z);}
float avFbm(vec3 p){return .53*avNoise(p)+.27*avNoise(p*2.03+vec3(17.1,5.7,31.3))+.13*avNoise(p*4.11+vec3(9.4,23.2,7.8))+.07*avNoise(p*8.17+vec3(27.7,13.6,41.1));}
float avBedFbm(vec3 p,int family){return avFbm(p);}
vec3 avFlow(vec3 p,float t){vec3 a=p*3.1+vec3(t*.37,-t*.29,t*.23),b=p*7.2+vec3(-t*.41,t*.31,t*.53);
 return .38*vec3(sin(a.y)+cos(a.z),sin(a.z)+cos(a.x),sin(a.x)+cos(a.y))+.13*vec3(sin(b.y)+cos(b.z),sin(b.z)+cos(b.x),sin(b.x)+cos(b.y));}
vec3 avWarp(vec3 p,float t,float seed){float k=.075*Turbulence;vec3 v=avFlow(p+seed,t);return p-k*avFlow(p-.5*k*v+seed,t);}
vec3 avThermal(float heat){vec3 c=mix(FireEdge,FireMid,smoothstep(.05,.60,heat));c=mix(c,FireCore,smoothstep(.57,.95,heat));if(Nirvana>.5){vec3 spectral=.5+.5*cos(6.2831853*(Time*.12+heat*.65+vec3(0,.33,.67)));c=mix(spectral*.8+vec3(.12),vec3(1),smoothstep(.80,1.,heat)*.65);}return c;}
vec3 avMedicinal(float heat){vec3 c=mix(VaporEdge,VaporMid,smoothstep(.08,.59,heat));return mix(c,VaporCore,smoothstep(.58,.94,heat));}
void avAdd(inout vec3 light,inout float density,float d,vec3 color){density+=d;light+=d*color;}

float avOctagon(vec2 p){vec2 a=abs(p);return max(max(a.x,a.y),(a.x+a.y)*.70710678);}

// Analytic derivatives of our original quintic-interpolated 3D value noise.
// Curl of three scalar potentials supplies locally irregular vortices, rather
// than repeating a small collection of sinusoidal tongue trajectories.
vec4 avNoiseGradient(vec3 p){
 vec3 i=floor(p),f=fract(p),u=f*f*f*(f*(f*6.-15.)+10.);
 vec3 du=30.*f*f*(f*(f-2.)+1.);
#ifdef WUXING_CORNER_CACHE
 // Validated bottom-fire support and turbulence bounds fit inside this cache,
 // including the four-cell reuse margin. No density or gradient interpolation.
 ivec3 cell=ivec3(i)-ivec3(HashOrigin);
 vec4 low=texelFetch(FireHashLow,cell,0),high=texelFetch(FireHashHigh,cell,0);
 float a=low.x,b=low.y,c=low.z,d=low.w,e=high.x,j=high.y,g=high.z,h=high.w;
#else
 float a=avHash(i),b=avHash(i+vec3(1,0,0)),c=avHash(i+vec3(0,1,0)),d=avHash(i+vec3(1,1,0));
 float e=avHash(i+vec3(0,0,1)),g=avHash(i+vec3(0,1,1)),h=avHash(i+vec3(1,1,1)),j=avHash(i+vec3(1,0,1));
#endif
 float bottom=mix(mix(a,b,u.x),mix(c,d,u.x),u.y),top=mix(mix(e,j,u.x),mix(g,h,u.x),u.y);
 vec3 gradient=du*vec3(mix(mix(b-a,d-c,u.y),mix(j-e,h-g,u.y),u.z),
                       mix(mix(c-a,d-b,u.x),mix(g-e,h-j,u.x),u.z),top-bottom);
 return vec4(mix(bottom,top,u.z),gradient);
}
vec3 avFireCurl(vec3 p,float t,float seed){
 vec3 q=p*2.4+vec3(t*.13,-t*.21,t*.17)+seed;
 // Full three-dimensional curl of a stochastic vector potential, plus an
 // analytic solenoidal flow. Preserve all three midpoint-advection passes.
 vec3 gradient=avNoiseGradient(q+vec3(19.3,7.1,3.4)).yzw;
 return cross(gradient,vec3(.37,.61,.70))*.75+avFlow(p*.3,t*.13)*.18;
}

// Filled, volume fuel bed and continuous folded combustion field. There are no
// fixed angular flame emitters or cylindrical tongues. Each world point has its
// own cooling, flame-sheet thickness and locally advected three-dimensional noise.
vec3 avFireCoordinate(vec3 p,float t,float seed){
 float rise=t*FlowSpeed;
 vec3 q=p;
 float warp=.055+.135*smoothstep(.025,.65,p.y)*Turbulence;
 for(int i=0;i<3;i++){
  vec3 speed=avFireCurl(q,rise,seed);
  q-=warp*avFireCurl(q-speed*warp*.5,rise,seed)/3.;
 }
 q.xz+=vec2(sin(p.y*5.7-rise*.67+seed),cos(p.y*4.8+rise*.43-seed))*.035*Turbulence*smoothstep(.025,.55,p.y);
 return q-vec3(0,rise*.77,0);
}

vec4 avFireBed(vec3 p,float t,float seed){
 vec3 local=p-AV_FIRE;
 if(local.y<=0.||local.y>FireHeight*1.22||avOctagon(p.xz)>FireRadius+.75)return vec4(0.);
 float surfaceMask=1.;
 if(SurfaceMaskEnabled>.5){
  vec3 worldPoint=p+vec3(0.,FireLift,0.);
  vec3 surfaceUv=(worldPoint-vec3(-2.8,0.,-2.8))/vec3(5.6,2.65,5.6);
  float clearance=texture(SurfaceClearance,surfaceUv).r;
  // The 3.5cm guard exceeds half a voxel diagonal, excluding interpolation
  // spill into the actual OBJ solids. Fade follows their authored surfaces.
  surfaceMask=smoothstep(.035,.065,clearance);
  if(surfaceMask<=0.)return vec4(0.);
 }
 float h=local.y/max(FireHeight,.1),foot=avOctagon(p.xz);
 // Both gas terms are identically zero under the solid floor above its cutoff.
 // Reject this empty support before evaluating curl or any noise; keep all ray samples.
 // The opening is above the plinth (.3156m) and BELOW the raised floor
 // (.5175m). The solid roof stays in model space when the flame root moves.
 float ceiling=1.-smoothstep(.450,.510,p.y+FireLift);
 float outsideRoof=smoothstep(1.69,1.78,foot);
 if(ceiling==0.&&outsideRoof==0.)return vec4(0.);
 vec3 q=avFireCoordinate(local,t,seed);
 float coarse=avFbm(q*vec3(3.9,2.3,3.9)+vec3(seed,7.1,seed*.4));
 float detail=avFbm(q*vec3(9.8,5.8,9.8)+vec3(19.3,seed*.31,5.7));
 float fine=avNoise(q*vec3(23.,11.,23.)+vec3(seed*.2,13.4,seed));

 // The centre burns through the full disk below the source model's raised floor.
 // That floor's underside is y=.5175; the gas fades before reaching the solid.
 float interior=1.-smoothstep(FireRadius-.23,FireRadius+.025,foot);
 float fuel=interior*(.25+.70*coarse)*exp(-local.y*3.1/max(FireHeight,.1));
 float combustion=smoothstep(.30+h*.19,.57+h*.12,coarse+.24*detail);
 float sheets=exp(-pow((detail-.51)/(.052+.016*(1.-h)),2.));
 float cracks=smoothstep(.17,.44,fine+.25*coarse);
 float baseLife=smoothstep(0.,.018,local.y);
 float patchHeat=avBedFbm(vec3(q.x*2.3,q.z*2.3,t*FlowSpeed*.23+seed),0);
 float innerHeight=min(.43,(.12+.34*smoothstep(.24,.69,patchHeat))*FireHeight/.92);
 float innerCooling=1.-smoothstep(innerHeight-.07,innerHeight+.09,local.y);
 float innerGas=(fuel*.27+interior*sheets*combustion*cracks*.74)*ceiling*baseLife*innerCooling;

 // Turbulent flow born throughout the circumference merges, opens gaps, forks
 // and sheds small curls. Its support leans out beyond the actual metal floor.
 float lean=.65*smoothstep(.025,.82,h);
 float shiftedFoot=avOctagon((local.xz-(q.xz-local.xz)*.55));
 float band=shiftedFoot-(FireRadius+lean);
 float bandWidth=.16+.10*(1.-h);
 float footprint=exp(-band*band/max(bandWidth*bandWidth,.003));
 float heightNoise=avBedFbm(vec3(q.x*2.1,q.z*2.1,t*FlowSpeed*.27+seed),1);
 float tip=.18+.93*smoothstep(.24,.74,heightNoise)+.24*coarse;
 float taper=1.-smoothstep(tip-.16,tip+.09,h);
 float opens=smoothstep(.28+h*.20,.60+h*.17,coarse+.16*detail);
 float folds=exp(-pow((detail-.485)/.045,2.));
 // Only gas outside the raised floor can extend above it.
 float roof=max(ceiling,outsideRoof);
 float outerGas=footprint*taper*baseLife*roof*opens*(.28+folds*cracks*2.35);
 // Continuous noisy root in the narrow seam; real depth masks the gold rim.
 float seamGas=FireLift>0.?exp(-pow((foot-(FireRadius+.07))/.16,2.))
      *(.18+.40*coarse)*(1.-smoothstep(.025,.085,local.y))*baseLife:0.;
 float density=(innerGas+outerGas+seamGas)*FireStrength*surfaceMask;
 float innerHeat=clamp(.18+.22*(1.-h)+.46*sheets*combustion,0.,1.);
 float outerHeat=clamp(.19+.17*(1.-h)+.65*folds*opens,0.,1.);
 vec3 color=(avThermal(innerHeat)*innerGas+avThermal(outerHeat)*outerGas+avThermal(.48)*seamGas)/max(innerGas+outerGas+seamGas,.000001);
 #ifdef WUXING_BOTTOM_FIRE
 // Keep each fire's hue, but give even dark violet gas visible hot streaks.
 color=mix(color,FireCore,.12+.18*max(innerHeat,outerHeat));
#endif
 return vec4(color,density);
}

// Four measured diagonal windows. Sources are outside the outer bars (2.0506m).
uniform float FaceHeatStrength, FaceHeatHeight, FaceHeatSpread;
uniform float FaceSparkRate, FaceSparkLife, FaceSparkSize, FaceSparkSpeed;
uniform float AromaStrength, AromaHeight;
uniform float IngredientCount, IngredientSize, IngredientStrength, GatherSpeed;
uniform float EnergyRingRadius, EnergyRingHeight, EnergyRingThickness, EnergyRingStrength, EnergyRingSpeed;
uniform vec3 AromaColor, IngredientColor, EnergyRingColor;

void avWindowEffects(vec3 p,float t,float seed,float power,inout vec3 light,inout float density){
 for(int face=0;face<4;face++){
  float angle=AV_PI*.25+float(face)*AV_PI*.5;
  vec2 normal=vec2(cos(angle),sin(angle)),tangent=vec2(-normal.y,normal.x);
  vec3 source=vec3(normal.x*2.085,1.393125,normal.y*2.085),v=p-source;
  float outwards=dot(v.xz,normal),across=dot(v.xz,tangent);
  // Never create a sheet through the solid back of the window.
  if(outwards<-.025||outwards>max(1.2,FaceSparkSpeed*FaceSparkLife*1.25+.2)||abs(across)>1.4||v.y<-.38||v.y>3.)continue;
  float y=(v.y+.24)/FaceHeatHeight;
  if(y>0.&&y<1.5&&FaceHeatStrength>0.){
   float drift=.10+.22*y,bend=.045*sin(y*6.1-t*FlowSpeed*.6+float(face));
   vec3 q=avWarp(vec3(across-bend,v.y,outwards-drift),t*FlowSpeed,seed+float(face)*7.1);
   float width=FaceHeatSpread*(.75+.75*y);
   float envelope=exp(-q.x*q.x/max(width*width,.001)-q.z*q.z/max(width*width*.72,.001));
   float breakup=smoothstep(.25,.66,avFbm(q*vec3(8.,3.5,8.)-vec3(0,t*FlowSpeed,0)));
   float life=smoothstep(0.,.08,y)*(1.-smoothstep(.6,1.45,y));
   avAdd(light,density,envelope*breakup*life*FaceHeatStrength*power*.38,mix(FireMid,vec3(.52,.50,.45),.8));
  }
  float hy=(v.y+.12)/AromaHeight;
  if(hy>0.&&hy<1.55&&AromaStrength>0.){
   vec3 q=avWarp(vec3(across,v.y,outwards),t*FlowSpeed*.65,seed+float(face)*19.);
   float centerOut=.12+hy*.43,spread=.065+hy*.15;
   float curl=hy*5.9-t*FlowSpeed*.42+float(face)*2.3;
   float distance=length(vec2(q.x-sin(curl)*spread,q.z-centerOut-cos(curl*.87)*spread*.4));
   float width=.025+hy*.034;
   float veil=exp(-distance*distance/(width*width));
   float noise=avFbm(q*vec3(9.,3.1,9.)+vec3(seed,-t*FlowSpeed*.7,13.));
   float soft=exp(-distance*distance/(width*width*8.))*smoothstep(.33,.65,noise)*.15;
   float ends=smoothstep(0.,.09,hy)*(1.-smoothstep(.70,1.5,hy));
   avAdd(light,density,(veil*smoothstep(.23,.57,noise)+soft)*ends*AromaStrength*power,AromaColor);
  }
  if(FaceSparkRate>0.&&FaceSparkSize>0.){
   int count=int(ceil(FaceSparkRate*FaceSparkLife));
   for(int j=0;j<72;j++){
    if(j>=count)break;
    float clock=(t+float(j)/FaceSparkRate)/FaceSparkLife,age=fract(clock)*FaceSparkLife,birth=floor(clock);
    float random=avHash(vec3(float(j)+float(face)*31.,seed,birth));
    float s=age/FaceSparkLife,a=across;
    float cx=(random-.5)*.54+sin(age*4.7+random*19.)*.035*Turbulence;
    float cy=-.17+random*.31+age*(.32+random*.52)-.08*age*age;
    float cz=.025+age*FaceSparkSpeed*(.55+random*.7);
    vec3 delta=vec3(across-cx,v.y-cy,outwards-cz);
    float size=FaceSparkSize*(1.-s*.6),d=length(delta);
    if(d>size*4.)continue;
    float core=exp(-d*d/max(size*size,.00001));
    float tail=exp(-dot(delta/vec3(size,size*2.5,size*1.5),delta/vec3(size,size*2.5,size*1.5)))*.23;
    float life=smoothstep(0.,.06,s)*pow(1.-s,1.35);
    avAdd(light,density,(core*7.+tail)*life*power,avThermal(.78+random*.2));
   }
  }
 }
}

void avGatherEffects(vec3 p,float t,float seed,float gather,float power,inout vec3 light,inout float density){
 // Material lights stay inside the actual vessel, above its .56125m floor.
 if(length(p.xz)<1.32&&p.y>.58&&p.y<2.28){
  vec3 target=AV_MATERIAL+vec3(0,gather*.28,0);
  float breathe=.035*sin(t*GatherSpeed*1.7);
  for(int j=0;j<12;j++){
   if(float(j)>=IngredientCount)break;
   float k=float(j),theta=t*GatherSpeed*.68+k*AV_TAU/max(IngredientCount,1.)+seed*.1;
   float radius=OrbitRadius*(.65+.21*sin(k*2.7+seed))*(1.-gather*.94);
   vec3 center=target+vec3(cos(theta)*radius,(.22*sin(theta*1.3+k)+breathe)*(1.-gather),sin(theta)*radius);
   float size=IngredientSize*(.7+.3*avHash(vec3(k,seed,3.)));
   float d=length(p-center),orb=exp(-d*d/max(size*size,.0001));
   avAdd(light,density,orb*IngredientStrength*power*3.,IngredientColor);
   // A curved 3D intake stream leads each orbiting material towards the centre.
   vec3 delta=p-target;float r=length(delta.xz),s=clamp(r/max(radius,.04),0.,1.);
   float aa=atan(delta.z,delta.x),path=theta-(1.-s)*1.6;
   float ad=atan(sin(aa-path),cos(aa-path));
   float z=delta.y-center.y+target.y;
   float width=max(.012,FilamentWidth*.75),distance=length(vec2(ad*max(r,.08),delta.y-(center.y-target.y)*s-.05*sin(s*AV_PI)));
   float thread=exp(-distance*distance/(width*width))*smoothstep(.03,.12,s)*(1.-smoothstep(.85,1.05,s));
   float flow=.22+.78*smoothstep(.1,.75,avNoise(vec3(s*9.-t*GatherSpeed*2.,k,seed)));
   avAdd(light,density,thread*flow*IngredientStrength*power*.42,mix(IngredientColor,EnergyRingColor,s));
  }
 }
 // Broken, helical currents around the vessel have real depth and changing height.
 vec3 q=avWarp(p,t*FlowSpeed*.38,seed+41.);
 float radius=length(q.xz),theta=atan(q.z,q.x)-t*EnergyRingSpeed*AV_TAU;
 if(abs(radius-EnergyRingRadius)<.42&&abs(q.y-EnergyRingHeight)<.7){
  for(int j=0;j<3;j++){
   float k=float(j),height=EnergyRingHeight+.14*sin(theta*2.+k*2.1)+.09*sin(theta*5.1+k-t*FlowSpeed*.3);
   float rr=EnergyRingRadius+.06*sin(theta*3.+k*2.4),width=EnergyRingThickness*(.7+.3*avNoise(vec3(theta*2.,k,t*.17)));
   float d=length(vec2(radius-rr,q.y-height-k*.10));
   float fragments=smoothstep(.28,.62,avNoise(vec3(theta*2.7-t*EnergyRingSpeed*4.,k*5.1,seed)));
   float flow=.25+.75*smoothstep(-.6,.6,sin(theta*2.7-k*2.1));
   avAdd(light,density,exp(-d*d/max(width*width,.00005))*fragments*flow*EnergyRingStrength*power*1.2,EnergyRingColor);
  }
 }
}

vec4 sampleAlchemy(vec3 p,float t){
#ifdef WUXING_UPPER_ALCHEMY
 float radial=length(p.xz);
 bool windows=p.y>.85&&p.y<3.95&&radial>1.85&&radial<3.6;
 bool chamber=p.y>.57&&p.y<2.3&&radial<1.35;
 bool circulation=p.y>.35&&p.y<2.35&&radial>2.10&&radial<3.5;
 bool chimney=p.y>3.24&&p.y<6.5&&radial<1.50;
 if(!(windows||chamber||circulation||chimney))return vec4(0.);
#endif
 float seed=float(Seed%997)*.071,phase=clamp(Phase,0.,1.),density=0.;vec3 light=vec3(0);
 float firePower=Stage==0?.18+.72*smoothstep(0.,1.,phase):Stage==1?1.:Stage==2?1.-.65*phase:max(0.,1.-phase*7.5);
 if(FireStrength>0.&&firePower>.001){vec4 fireField=avFireBed(p,t,seed);avAdd(light,density,fireField.a*firePower,fireField.rgb);}
 // All other layers have exactly zero strength for the host bottom-fire recipe.
 // Keep the same fire field, integration positions, precision and output formula.
#ifdef WUXING_BOTTOM_FIRE
 return vec4(light/max(density,.000001)*Intensity,density*Opacity);
#else
 if(BottomOnly>.5)return vec4(light/max(density,.000001)*Intensity,density*Opacity);

 // The source is only the measured chimney opening, never the entire 5m body.
 float vaporPower=Stage==0?.08+.17*phase:Stage==1?.62:Stage==2?.62*(1.-phase*.76):max(0.,.8-phase*1.05);
 vec3 v=p-AV_MOUTH;float vy=v.y/max(VaporHeight,.1);
 if(vy>0.&&vy<1.65&&vaporPower>.001){
  float bendWeight=smoothstep(.03,.42,vy);
  vec2 bend=vec2(sin(vy*3.4+t*FlowSpeed*.41+seed),cos(vy*4.1-t*FlowSpeed*.33+seed*.71))*VaporSpread*.27*bendWeight;
  vec3 q=avWarp(vec3(v.x-bend.x,v.y,v.z-bend.y),t*FlowSpeed,seed)*vec3(1,1.35,1);
  float radius=VaporRadius+VaporSpread*pow(max(vy,0.),.85);
  float n=avFbm(q*vec3(6.5,3.2,6.5)+vec3(seed,-t*FlowSpeed*1.8,seed*.71));
  float profile=exp(-dot(q.xz,q.xz)/max(radius*radius*.64,.0001));
  float ends=smoothstep(0.,.035,vy)*(1.-smoothstep(.28,1.12,vy));
  float smoke=profile*ends*smoothstep(.26,.66,n)*vaporPower*VaporStrength*.75;
  avAdd(light,density,smoke,avMedicinal(.25+n*.24));
  for(int j=0;j<3;j++){
   float k=float(j),angle=vy*(5.1+k*.7)-t*FlowSpeed*.9+k*AV_TAU/3.+seed;
   vec2 at=radius*(.38+.2*sin(vy*7.1+k))*vec2(cos(angle),sin(angle));
   float d=length(q.xz-at),width=FilamentWidth*(1.+vy*.65);
   float veil=exp(-d*d/max(width*width,.000025))*ends;
   veil*=smoothstep(.22,.63,avNoise(vec3(vy*7.-t*FlowSpeed,seed+k*6.4,t*.15)));
   avAdd(light,density,veil*FilamentStrength*vaporPower*.9,avMedicinal(.82));
  }
  // Independently born, expanding and curling three-dimensional wisps.
  for(int j=0;j<12;j++){
   float k=float(j),clock=t*FlowSpeed*.38+k/12.,age=fract(clock),birth=floor(clock);
   float random=avHash(vec3(k,seed,birth));float angle=random*AV_TAU+age*(1.3+random)*Turbulence;
   vec3 center=vec3(cos(angle)*VaporSpread*pow(age,1.25),age*VaporHeight*1.38,sin(angle)*VaporSpread*pow(age,1.25));
   center.xz+=bend;
   float width=VaporRadius*(.28+age*.55),height=width*(2.2+Turbulence*.5);
   vec3 local=avWarp(v-center,t*FlowSpeed+random*7.,seed+k*2.1);
   vec3 ellipsoid=local/vec3(width,height,width);float shape=exp(-dot(ellipsoid,ellipsoid)*1.45);
   if(shape>.002){float detail=avFbm(local/max(width,.03)*2.5+vec3(random*71.,-t*FlowSpeed*1.1,seed));
    float life=smoothstep(0.,.14,age)*pow(1.-age,1.15);
    float d=shape*life*smoothstep(.21,.57,detail)*VaporStrength*vaporPower*1.5;
    avAdd(light,density,d,avMedicinal(.43+detail*.48));}
  }
 }

 // Refining threads stay at the source model's material ring.
 float gather=Stage==2?smoothstep(0.,1.,phase):Stage==3?1.:0.;
 float orbitPower=Stage==0?0.:Stage==1?1.:Stage==2?1.-gather*.6:0.;
 vec3 m=avWarp(p-AV_MATERIAL,t*FlowSpeed,seed+.7);
 float r=length(m.xz),angle=atan(m.z,m.x),orbit=OrbitRadius*(1.-gather*.88);
 if(orbitPower>.001&&r<OrbitRadius+.35&&abs(m.y)<.45){
  for(int j=0;j<3;j++){
   float k=float(j),theta=angle-t*OrbitSpeed*AV_TAU+k*2.094;
   float height=.12*sin(theta*2.+k*1.7),radius=orbit*(.85+.14*cos(theta*3.+k));
   float width=FilamentWidth*(1.1+.6*sin(theta+k)*sin(theta+k));
   float d=length(vec2(r-radius,m.y-height));
   float thread=exp(-d*d/max(width*width,.00004))*(.35+.65*smoothstep(-.7,.65,sin(theta*2.1+k)));
   avAdd(light,density,thread*FilamentStrength*orbitPower*2.,mix(VaporCore,CoreColor,gather*.8));
  }
 }
 vec3 core=AV_MATERIAL+vec3(0,gather*.28,0);float corePower=Stage==2?gather:Stage==3?1.:0.;
 if(Stage==3)core=mix(AV_MOUTH,AV_RESULT,smoothstep(.18,.80,phase));
 float coreDistance=length(p-core),coreSize=CoreRadius*(Stage==3?1.:.6+.4*gather);
 if(corePower>.001){
  float heart=exp(-coreDistance*coreDistance/max(coreSize*coreSize*.48,.00001));
  float halo=exp(-coreDistance*coreDistance/max(coreSize*coreSize*4.,.0001));
  avAdd(light,density,(heart*9.+halo*.32)*CoreStrength*corePower,CoreColor);
 }

 if(Stage==3&&phase>.12&&phase<.82){
  float x=clamp((phase-.13)/.17,0.,1.),pulse=sin(x*AV_PI),radius=.10+BurstRadius*x;
  vec3 b=p-AV_MOUTH;float ring=exp(-pow((length(b.xz)-radius)/.055,2.)-pow((b.y-.07)/.065,2.));
  avAdd(light,density,ring*pulse*BurstStrength*3.,CoreColor);
  float rise=smoothstep(.18,.40,phase)*(1.-smoothstep(.60,.82,phase));
  float y=b.y;if(y>0.&&y<2.3){
   for(int j=0;j<5;j++){
    float k=float(j),h=y/2.3,a=h*5.3+k*AV_TAU/5.+t*OrbitSpeed*AV_TAU;
    float spread=.21+BurstRadius*h*.42;
    vec2 at=spread*vec2(cos(a),sin(a));float d=length(b.xz-at);
    float width=FilamentWidth*(1.2+h*.9),w=exp(-d*d/max(width*width,.000025));
    w*=sin(h*AV_PI)*rise*BurstStrength*FilamentStrength;
    avAdd(light,density,w*2.2,mix(CoreColor,VaporCore,k/5.));
   }
  }
 }

 // Thin, true-volume energy rings on the floor, sized to the 5m cauldron.
 if(abs(p.y-.035)<.13&&Stage<3){
  float r=length(p.xz),a=atan(p.z,p.x)+t*OrbitSpeed*.5;
  float ring=exp(-pow((r-SigilRadius)/.026,2.)-pow((p.y-.035)/.027,2.));
  float inner=exp(-pow((r-(SigilRadius-.14))/.016,2.)-pow((p.y-.035)/.025,2.));
  float segments=smoothstep(-.48,.12,sin(a*24.));
  float power=Stage==0?.2+.8*phase:Stage==1?1.:1.-phase*.7;
  avAdd(light,density,(ring+inner*segments*.7)*SigilStrength*power*3.,CoreColor);
 }
 if(p.y<4.4)avWindowEffects(p,t,seed,firePower,light,density);
 if(p.y<2.6)avGatherEffects(p,t,seed,gather,orbitPower,light,density);
 return vec4(light/max(density,.000001)*Intensity,density*Opacity);
#endif
}

// SHARED_VOLUME_CORE_END

vec2 boxHit(vec3 ro,vec3 rd){
 vec3 safe=sign(rd)*max(abs(rd),vec3(.00001));safe+=vec3(equal(safe,vec3(0)))*.00001;
 vec3 a=(BoundsMin-ro)/safe,b=(BoundsMax-ro)/safe,mn=min(a,b),mx=max(a,b);
 return vec2(max(mn.x,max(mn.y,mn.z)),min(mx.x,min(mx.y,mx.z)));
}
#ifdef WUXING_BOTTOM_FIRE
void fireSlab(float origin,float direction,float radius,inout vec2 interval){
 if(direction==0.){if(abs(origin)>radius)interval=vec2(1.,0.);return;}
 vec2 t=vec2(-radius-origin,radius-origin)/direction;
 interval.x=max(interval.x,min(t.x,t.y));interval.y=min(interval.y,max(t.x,t.y));
}
vec2 fireOctagonHit(vec3 ro,vec3 rd,float radius){
 vec2 interval=vec2(-1e20,1e20);
 fireSlab(ro.x,rd.x,radius,interval);fireSlab(ro.z,rd.z,radius,interval);
 fireSlab((ro.x+ro.z)*.70710678,(rd.x+rd.z)*.70710678,radius,interval);
 fireSlab((ro.x-ro.z)*.70710678,(rd.x-rd.z)*.70710678,radius,interval);
 return interval;
}
#endif
void main(){
 vec2 uv=gl_FragCoord.xy/Viewport;
 vec4 eye=InverseLocalProjection*vec4(0.,0.,1.,0.);
 vec3 ro=eye.xyz/eye.w;
 vec4 farPoint=InverseLocalProjection*vec4(uv*2.-1.,1.,1.);
 vec3 rd=normalize(farPoint.xyz/farPoint.w-ro);
 vec2 hit=boxHit(ro,rd);
 float depth=texture(SceneDepth,uv).r;
 vec4 surface=InverseLocalProjection*vec4(uv*2.-1.,depth*2.-1.,1.);
 float sceneDistance=depth>=.999999?10000.:dot(surface.xyz/surface.w-ro,rd);
 float first=max(0.,hit.x),last=min(hit.y,sceneDistance);
 bool emberHit=false;vec3 ember=vec3(0.);
#ifdef WUXING_BOTTOM_FIRE
 // An opaque charcoal support is distinct from the translucent gas. It sits
 // at the measured root and is clipped against real foreground geometry.
 if(rd.y<-.00001){
  float bedDistance=(FireLift+.045-ro.y)/rd.y;
  vec3 bedPoint=ro+rd*bedDistance;
  if(bedDistance>=first&&bedDistance<last&&avOctagon(bedPoint.xz)<min(FireRadius+.24,1.78)){
   emberHit=true;last=bedDistance;
   float coals=avNoise(vec3(bedPoint.xz*14.,float(Seed)*.071));
   float heat=avNoise(vec3(bedPoint.xz*3.7,Time*.15));
   float cracks=exp(-pow((coals-.50)/.045,2.));
   ember=vec3(.025,.018,.026)+FireEdge*.13+FireMid*(.09+.24*cracks)*(.5+.5*heat);
  }
 }
#endif
 if(last<=first){result=vec4(0);return;}
 int marchSteps=Steps;
#ifdef WUXING_BOTTOM_FIRE
 // Opaque geometry can leave millimetres of visible gas. Preserve the original
 // integration spacing and budget; pixel cost is bounded by the render target.
 marchSteps=min(Steps,max(48,int(ceil((last-first)/.001))));
#endif
 float ds=(last-first)/float(marchSteps),transmittance=1.;vec3 light=vec3(0);
 int begin=0,end=marchSteps,emptyBegin=1,emptyEnd=0;
#ifdef WUXING_BOTTOM_FIRE
 // Preserve first/ds and every contributing midpoint. Only skip samples in
 // conservative, exactly empty octagon corners or beneath the opaque floor.
 float pad=.0005+max(abs(ro.x),max(abs(ro.y),abs(ro.z)))*.000005;
 vec2 support=fireOctagonHit(ro,rd,FireRadius+.75+pad);
 begin=int(clamp(floor((max(first,support.x)-first)/ds-.5)-2.,0.,float(marchSteps)));
 end=int(clamp(ceil((min(last,support.y)-first)/ds-.5)+3.,0.,float(marchSteps)));
 vec2 hole=fireOctagonHit(ro,rd,max(0.,1.69-pad));
 float y0=.510+pad,y1=BoundsMax.y+pad;
 fireSlab(ro.y-(y0+y1)*.5,rd.y,max(0.,(y1-y0)*.5),hole);
 if(hole.y>=hole.x){
  emptyBegin=int(clamp(ceil((max(first,hole.x)-first)/ds-.5)+2.,0.,float(marchSteps)));
  emptyEnd=int(clamp(floor((min(last,hole.y)-first)/ds-.5)-2.,-1.,float(marchSteps-1)));
 }
#endif
 // Stable world-space midpoint integration, independent of frame number.
 for(int i=begin;i<512;i++){
  if(i>=end)break;
  if(i>=emptyBegin&&i<=emptyEnd){i=emptyEnd;continue;}
  vec3 p=ro+rd*(first+(float(i)+.5)*ds);
  vec4 field=sampleAlchemy(p-vec3(0.,FireLift,0.),Time);
  if(field.a<=0.)continue;
  float alpha=1.-exp(-max(0.,field.a)*ds*8.);
  light+=transmittance*field.rgb*alpha;transmittance*=1.-alpha;
  // Intensity is validated <=3 and each field colour <=1 before intensity.
  // Residual premultiplied colour <=3e-6, alpha <=1e-6: well below one 8-bit step.
  if(transmittance<=.000001)break;
 }
 if(emberHit){result=vec4(light+transmittance*ember,1.);return;}
 float alpha=1.-transmittance;result=vec4(light/max(.00001,alpha),alpha);
}
