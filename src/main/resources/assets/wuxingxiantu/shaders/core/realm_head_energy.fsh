#version 150

uniform sampler2D Sampler0;
uniform mat4 ViewToHead;
uniform mat4 InverseProjection;
uniform float EffectTime;
uniform vec4 HeadColor;
uniform float Steps;
uniform float FlightStyle;
uniform vec2 FlightMotion;
uniform vec2 OutputSize;
uniform float EmissionPass;
in vec2 texCoord;
out vec4 fragColor;

// Yuanying keeps its sparse field. Higher realms integrate an interior flame;
// the analytic ellipsoid only bounds it and never draws a glass surface.
float hash3(vec3 p) {
    p=fract(p*0.1031);p+=dot(p,p.yzx+33.33);
    return fract((p.x+p.y)*p.z);
}
float headNoise3(vec3 p) {
    vec3 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);
    float a=mix(hash3(i),hash3(i+vec3(1,0,0)),f.x);
    float b=mix(hash3(i+vec3(0,1,0)),hash3(i+vec3(1,1,0)),f.x);
    float c=mix(hash3(i+vec3(0,0,1)),hash3(i+vec3(1,0,1)),f.x);
    float d=mix(hash3(i+vec3(0,1,1)),hash3(i+vec3(1,1,1)),f.x);
    return mix(mix(a,b,f.y),mix(c,d,f.y),f.z);
}
float gaussian(float distance,float width) {
    float v=distance/max(width,0.001);return exp(-v*v);
}
// Use the wake realm hue at the nose, retaining the shared luminance budget.
// Less chroma must not lift luminance by mixing in more bright white.
vec3 frontTint(vec3 realmColor) {
    const vec3 luma=vec3(0.2126,0.7152,0.0722);
    vec3 raw=realmColor;
    float budget=dot(mix(vec3(101.0,217.0,242.0)/255.0,vec3(1.0),0.55),luma);
    // Preserve realm hue instead of raising the dim channels towards white.
    // Bound the strongest channel; saturation does not add more emission.
    return raw*(budget/max(max(raw.r,raw.g),max(raw.b,0.000001)));
}
// Local +Z is flight-forward. Fade across the front half rather than
// clipping a plane at the equator; rear surface and rear wisps contribute zero.
float forwardShellCoverage(float z) {
    return smoothstep(0.0,0.38,z);
}
float inverseRay(float value) {
    return abs(value)<0.00001?(value<0.0?-100000.0:100000.0):1.0/value;
}
vec2 boxHit(vec3 origin,vec3 direction) {
    vec3 inv=vec3(inverseRay(direction.x),inverseRay(direction.y),inverseRay(direction.z));
    vec3 a=(vec3(-1.5,-1.5,-2.5)-origin)*inv;
    vec3 b=(vec3(1.5,1.5,1.25)-origin)*inv;
    vec3 first=min(a,b),last=max(a,b);
    return vec2(max(max(first.x,first.y),first.z),min(min(last.x,last.y),last.z));
}

// Local axes: x sideways, y up, z forward. Positive time in the sampling
// coordinate advects the features towards negative z, into the trailing plume.
// The transverse anchors stay fixed: only small travelling bends move along
// the fibres, rather than making a whole surrounding volume swell sideways.
vec2 legacyDensity(vec3 p) {
    p.xy*=1.0+0.16*sin(p.z*2.1)+0.08*sin(p.z*3.7+EffectTime*1.8);
    float flowZ=p.z+EffectTime*2.65;
    float coarse=headNoise3(vec3(p.xy*3.0,flowZ*1.25));
    float bendA=sin(flowZ*1.65)*0.055+sin(flowZ*3.20+0.8)*0.022;
    float bendB=sin(flowZ*1.90+1.7)*0.049+sin(flowZ*2.80-0.6)*0.019;

    // Uneven, disconnected transverse anchors leave broad gaps around the
    // player. None of these terms describes a radial rim or a closed nose cap.
    float upperRight=gaussian(p.x-0.61-bendA,0.125)
                     *gaussian(p.y-0.25-bendB,0.180);
    float lowerLeft=gaussian(p.x+0.58-bendB,0.140)
                    *gaussian(p.y+0.24-bendA,0.155);
    float lowerReturn=gaussian(p.y+0.60-bendB*0.65,0.115)
                      *gaussian(p.x-0.17-bendA*0.55,0.205);
    float upperWisp=gaussian(p.y-0.60-bendA*0.60,0.105)
                    *gaussian(p.x+0.27-bendB*0.50,0.150);
    float leftWisp=gaussian(p.x+0.66-bendA*0.40,0.105)
                   *gaussian(p.y-0.36-bendB*0.50,0.135);
    float fibres=upperRight+lowerLeft*0.92+lowerReturn*0.68
                 +upperWisp*0.53+leftWisp*0.39;

    float hollow=smoothstep(0.28,0.49,length(p.xy));
    float crossSupport=1.0-smoothstep(1.12,1.48,max(abs(p.x),abs(p.y)));
    float axialSupport=smoothstep(-1.55,-0.94,p.z)
                       *(1.0-smoothstep(0.16,0.62,p.z));
    // Strong erosion cuts visible holes; the dim residual only connects the
    // travelling pieces optically, never into a uniformly filled light shell.
    float erosion=smoothstep(0.32,0.66,coarse);
    float packets=0.22+0.78*smoothstep(-0.35,0.72,
                         sin(flowZ*2.25+p.x*4.1-p.y*2.3));
    float soft=gaussian(p.x,0.88)*gaussian(p.y,0.82)
               *smoothstep(0.43,0.72,coarse)*0.018;
    float density=(fibres*(0.045+erosion*1.42)*packets+soft)
                  *hollow*crossSupport*axialSupport;

    // Sparse short crests travel backwards at the same advection speed.
    // White only appears where an existing filament is both dense and eroded.
    float crest=pow(max(0.0,sin(flowZ*3.8+p.x*5.2-p.y*3.7)),10.0);
    float hot=clamp(crest*smoothstep(0.49,0.76,coarse)
                    *min(fibres,1.0)*0.82,0.0,0.82);
    return vec2(density,hot);
}


// Three short, uneven surface streams. They bend and advect on the sphere;
// neither these streams nor player animation determines the sphere's shape.
vec2 sphereArcs(vec3 surface) {
    float speed=clamp(FlightMotion.x,0.0,1.0);
    // Only the streams lean towards the outside of a turn. The sphere surface
    // itself stays analytically complete and independent of pose/bank.
    vec3 p=normalize(surface+vec3(FlightMotion.y*0.15,0.0,0.0)
                     *(0.35+0.65*(1.0-smoothstep(-0.5,0.8,surface.z))));
    float angle=atan(p.y,p.x+0.000001);
    float flow=EffectTime*0.52;
    // Spatial stretching of the advected coordinate changes travel speed
    // without multiplying world time by a changing speed and jumping phase.
    float flowZ=p.z/(1.0+speed*0.85);
    float bend=flowZ*1.35+sin(flowZ*3.4+flow*1.7)*0.19;
    float distanceA=sin((angle-bend-flow)*0.5);
    float distanceB=sin((angle+bend*0.72-flow-2.14)*0.5);
    float distanceC=sin((angle-bend*0.53-flow+2.27)*0.5);
    vec3 narrow=vec3(gaussian(distanceA,0.025),gaussian(distanceB,0.021),gaussian(distanceC,0.024));
    vec3 skirt=vec3(gaussian(distanceA,0.110),gaussian(distanceB,0.092),gaussian(distanceC,0.102));

    // Two short forward bends belong to streams A/B rather than independent
    // rings. Their angular windows attach to the longitudinal stream and
    // keep its white-blue core visible on the forward half from oblique views.
    float capA=0.43+0.065*sin(angle*2.0+flow*0.8);
    float capB=0.72+0.045*sin(angle*3.0-flow*0.65);
    float bendA=(capA/(1.0+speed*0.85))*1.35
               +sin(capA/(1.0+speed*0.85)*3.4+flow*1.7)*0.19;
    float bendB=(capB/(1.0+speed*0.85))*1.35
               +sin(capB/(1.0+speed*0.85)*3.4+flow*1.7)*0.19;
    float windowA=gaussian(sin((angle-bendA-flow)*0.5),0.42);
    float windowB=gaussian(sin((angle+bendB*0.72-flow-2.14)*0.5),0.44);
    narrow.xy=max(narrow.xy,vec2(gaussian(p.z-capA,0.028)*windowA,
                                 gaussian(p.z-capB,0.024)*windowB));
    skirt.xy=max(skirt.xy,vec2(gaussian(p.z-capA,0.075)*windowA,
                               gaussian(p.z-capB,0.066)*windowB));
    float longitudinal=smoothstep(-0.995,-0.88,p.z)
                       *(1.0-smoothstep(0.70,0.995,p.z));
    float irregular=headNoise3(vec3(p.xy*4.7,flowZ*4.7+EffectTime*2.1));
    float continuity=0.72+0.28*smoothstep(0.22,0.68,irregular);
    vec3 weights=vec3(1.0,0.88,0.65);
    float brokenCore=0.20+0.80*smoothstep(0.35,0.70,irregular);
    return clamp(vec2(dot(skirt,weights),dot(narrow,weights)*brokenCore)*longitudinal*continuity,vec2(0.0),vec2(1.0));
}

void sphereSurface(float travel,bool nearSurface,vec3 origin,vec3 direction,
                   vec3 viewDirection,float localPerWorld,float sceneLimit,
                   float coverage,inout vec3 light,inout vec3 glow,
                   inout float transmittance,inout float nearestDepth) {
    if(travel<0.0||travel>sceneLimit)return;
    vec3 normal=normalize(origin+direction*travel);
    // Inverse-transpose normals keep the Fresnel correct when the host adds
    // a small speed-dependent longitudinal stretch to the head matrix.
    vec3 viewNormal=normalize(transpose(mat3(ViewToHead))*normal);
    float rim=pow(1.0-clamp(abs(dot(viewNormal,-viewDirection)),0.0,1.0),1.6);
    float front=smoothstep(0.18,0.96,normal.z);
    float rear=1.0-smoothstep(-0.92,-0.25,normal.z);
    vec2 streams=sphereArcs(normal);
    float skirt=streams.x,core=streams.y;
    float speed=clamp(FlightMotion.x,0.0,1.0);
    float rearRim=mix(1.0,0.06,rear);
    float radianceVariation=0.35+0.65*smoothstep(0.30,0.70,
        headNoise3(vec3(normal.xy*3.8,normal.z*3.8+EffectTime*0.65)));
    // The forward hemisphere is an actual luminous volume, not only a few
    // filaments. Noise keeps its surface alive while preserving a continuous
    // front-to-rear gradient.
    float frontFill=smoothstep(-0.05,0.72,normal.z)
                   *(0.42+0.58*radianceVariation);
    // Sparse stream-led coverage avoids a continuous closed glass rim.
    // Rear dimming makes the overlapping broad tail grow out of the shell.
    float opacity=(nearSurface?0.008:0.004)+(nearSurface?0.008:0.004)*rim
                  *mix(0.20,1.0,radianceVariation)*mix(1.0,0.04,rear);
    opacity+=front*(0.012+rim*0.030)*radianceVariation
             +frontFill*(nearSurface?0.11:0.045)
             +(skirt*0.20+core*0.54)*(nearSurface?1.0:0.20);
    opacity*=forwardShellCoverage(normal.z)*clamp(HeadColor.a/0.64,0.0,1.0)*coverage;
    vec3 hue=mix(HeadColor.rgb,vec3(0.647,0.961,1.0),rim*0.66);
    hue=mix(hue,vec3(0.902,1.0,1.0),front*(0.28+speed*0.12)+core*0.46);
    float amount=transmittance*opacity;
    light+=hue*amount*(1.0+frontFill*0.55+rim*0.22*rearRim*radianceVariation+skirt*0.08
                      +core*(1.32+front*0.24)+front*rim*speed*0.20);
    // Bloom selects the rim/streams, never a uniformly glowing opaque disc.
    glow+=hue*amount*(0.10+frontFill*1.15+rim*1.10*rearRim*radianceVariation+skirt*0.26
                     +core*(4.80+front*0.90)+front*(0.16+speed*0.24));
    transmittance*=1.0-opacity;
    if(opacity>0.001) {
        float viewDepth=-viewDirection.z*travel/localPerWorld;
        nearestDepth=min(nearestDepth,max(viewDepth/128.0,0.0));
    }
}

void advancedSphere(vec3 origin,vec3 direction,vec3 viewDirection,
                    float localPerWorld,float sceneLimit) {
    float closestTravel=-dot(origin,direction);
    vec3 closest=origin+direction*closestTravel;
    // Computing closest-point distance avoids subtracting two huge squared
    // camera distances. Radius is exactly one in the uniformly scaled space.
    float discriminant=1.0-dot(closest,closest);
    float edgeWidth=max(fwidth(discriminant),0.00005);
    if(discriminant<-edgeWidth)discard;
    float root=sqrt(max(discriminant,0.0));
    float coverage=smoothstep(-edgeWidth,edgeWidth,discriminant);
    vec3 light=vec3(0.0),glow=vec3(0.0);
    float transmittance=1.0,nearestDepth=1000000.0;
    // The analytic outer shell is intentionally omitted. The front energy is
    // carried by the interior volume below, so there is no glass-ball contour.
    // Thin advected wisps occupy the outer interior of the sphere. A bounded
    // integration gives flowing energy depth without filling the actor's face
    // with a uniformly bright disc or stacking additional closed shells.
    float first=max(closestTravel-root,0.0),last=min(closestTravel+root,sceneLimit);
    float stepSize=max(0.0,last-first)/12.0;
    for(int sample=0;sample<12;sample++) {
        float travel=first+(float(sample)+0.5)*stepSize;
        vec3 p=origin+direction*travel;
        float radius=length(p);
        if(radius<0.0001)continue;
        vec2 streams=sphereArcs(p/max(radius,0.0001));
        float localNoise=headNoise3(vec3(p.xy*6.5,p.z*5.2+EffectTime*1.6));
        float envelope=exp(-radius*radius*2.0)*(1.0-smoothstep(0.72,1.0,radius));
        float frontAxis=forwardShellCoverage(p.z/max(radius,0.0001));
        float shoulder=smoothstep(0.18,0.86,p.z/max(radius,0.0001));
        float noiseGate=smoothstep(0.25,0.75,localNoise);
        float detail=noiseGate*smoothstep(0.10,0.65,streams.y+streams.x*streams.x*0.55);
        // The broad medium is transparent; flowing crests carry the light.
        // Lowering only chroma would leave the same opaque white fog.
        float field=envelope*(0.18+localNoise*0.18
                    +(streams.x*streams.x*0.52+streams.y*0.70)*noiseGate)
                    *frontAxis*(0.62+0.38*shoulder);
        float opacity=1.0-exp(-field*stepSize/localPerWorld
                             *clamp(HeadColor.a/0.64,0.0,1.0)*coverage);
        vec3 hue=frontTint(HeadColor.rgb);
        float amount=transmittance*opacity;
        light+=hue*amount*(1.10+0.65*detail);
        glow+=hue*amount*(0.55+1.65*detail);
        transmittance*=1.0-opacity;
        if(opacity>0.001)nearestDepth=min(nearestDepth,
            max(-viewDirection.z*travel/localPerWorld/128.0,0.0));
    }
    // No rear cap: the volume fades continuously before reaching the back.
    float opacity=1.0-transmittance;
    if(opacity<0.002)discard;
    if(EmissionPass>0.5)fragColor=vec4(glow,nearestDepth);
    else fragColor=vec4(light/max(opacity,0.000001),opacity);
}

void main() {
    vec2 uv=clamp(gl_FragCoord.xy/max(OutputSize,vec2(1.0)),vec2(0.0),vec2(1.0));
    vec4 rayH=InverseProjection*vec4(uv*2.0-1.0,1.0,1.0);
    vec3 viewDirection=normalize(rayH.xyz/max(abs(rayH.w),0.000001));
    vec3 origin=(ViewToHead*vec4(0,0,0,1)).xyz;
    vec3 localDirection=mat3(ViewToHead)*viewDirection;
    float localPerWorld=max(length(localDirection),0.000001);
    vec3 direction=localDirection/localPerWorld;
    // Clip the integration interval against the captured opaque scene, even
    // when a foreground body or block lies inside the projected flame bounds.
    float sceneDepth=texture(Sampler0,uv).r;
    vec4 sceneH=InverseProjection*vec4(uv*2.0-1.0,sceneDepth*2.0-1.0,1.0);
    vec3 sceneView=sceneH.xyz/max(abs(sceneH.w),0.000001);
    vec3 sceneLocal=(ViewToHead*vec4(sceneView,1.0)).xyz;
    float sceneLimit=dot(sceneLocal-origin,direction)-0.0003;
    if(HeadColor.a<0.001)discard;
    if(FlightStyle>0.5) {
        advancedSphere(origin,direction,viewDirection,localPerWorld,sceneLimit);
        return;
    }
    vec2 span=boxHit(origin,direction);
    float first=max(span.x,0.0),last=min(span.y,sceneLimit);
    if(last<=first||HeadColor.a<0.001)discard;

    float samples=floor(clamp(Steps,8.0,24.0)+0.5);
    float stepSize=(last-first)/samples;
    float metres=stepSize/localPerWorld;
    vec3 light=vec3(0.0);
    float transmittance=1.0,nearestDepth=1000000.0;
    for(int sample=0;sample<24;sample++) {
        if(float(sample)>=samples||transmittance<0.07)break;
        float travel=first+(float(sample)+0.5)*stepSize;
        vec3 at=origin+direction*travel;
        vec2 field=legacyDensity(at);
        float mass=field.x*metres*HeadColor.a*3.25;
        float opacity=1.0-exp(-mass);
        vec3 hue=mix(HeadColor.rgb,vec3(0.95,1.0,1.0),field.y);
        light+=transmittance*hue*opacity;
        transmittance*=1.0-opacity;
        if(opacity>0.001) {
            float viewDepth=-viewDirection.z*travel/localPerWorld;
            nearestDepth=min(nearestDepth,max(viewDepth/128.0,0.0));
        }
    }
    float totalOpacity=min(1.0-transmittance,0.76);
    if(totalOpacity<0.003)discard;
    if(EmissionPass>0.5) {
        fragColor=vec4(light*0.78,nearestDepth);
    } else {
        vec3 hue=light/max(1.0-transmittance,0.000001);
        fragColor=vec4(hue,totalOpacity);
    }
}

