#version 150

uniform sampler2D Sampler0;
uniform vec2 OutputSize;
uniform float EmissionPass;
uniform float EffectTime;
uniform vec3 VisualTuning;
in vec2 texCoord;
in vec4 vertexColor;
in vec3 viewPosition;
in vec3 viewNormal;
in vec4 trailData;
out vec4 fragColor;

const vec3 WHITE_CORE = vec3(0.995);

float hash3(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}
float realmNoise3(vec3 p) {
    vec3 i=floor(p), f=fract(p); f=f*f*(3.0-2.0*f);
    float a=mix(hash3(i),hash3(i+vec3(1,0,0)),f.x);
    float b=mix(hash3(i+vec3(0,1,0)),hash3(i+vec3(1,1,0)),f.x);
    float c=mix(hash3(i+vec3(0,0,1)),hash3(i+vec3(1,0,1)),f.x);
    float d=mix(hash3(i+vec3(0,1,1)),hash3(i+vec3(1,1,1)),f.x);
    return mix(mix(a,b,f.y),mix(c,d,f.y),f.z);
}
float turbulence(vec3 p) {
    return realmNoise3(p)*0.69+realmNoise3(p*2.07+vec3(17.1,9.2,11.7))*0.31;
}
float gaussian(float distance,float width) {
    float q=distance/max(width,0.001); return exp(-q*q);
}

// U is actual accumulated world-path distance. All layers share the same
// centre; the moving veins below are interior detail in one connected plume.
vec4 trailEnergy() {
    float band=floor(texCoord.y*0.5+0.001);
    float layer=mod(band,4.0);
    float across=clamp(texCoord.y-band*2.0,0.0,1.0);
    float q=across*2.0-1.0, arc=texCoord.x;
    if(layer>2.5) {
        float x=texCoord.x*2.0-1.0;
        float spark=exp(-(x*x+q*q)*5.0);
        return vec4(mix(vertexColor.rgb,WHITE_CORE,0.76),spark*vertexColor.a);
    }
    // Increasing time carries features towards the older (smaller-U) path.
    // Fine veins travel inside the plume rather than forming separate rails.
    float flow=arc+EffectTime*4.2;
    float coarse=turbulence(vec3(flow*0.25,q*1.65,2.31));
    float fine=realmNoise3(vec3(flow*0.67,q*5.4+coarse*1.8,7.13));
    float envelope=pow(max(1.0-q*q,0.0),1.9);
    float drift=(coarse-0.5)*0.42;
    float laneA=0.26*sin(flow*0.59+sin(flow*0.17))+drift;
    float laneB=-0.34+0.19*sin(flow*0.43+1.9)+drift*0.55;
    float laneC=0.40+0.17*sin(flow*0.37+4.1)-drift*0.30;
    float breaks=smoothstep(0.30,0.70,realmNoise3(vec3(flow*0.72,q*2.3,11.7)));
    float veins=gaussian(q-laneA,0.075)*0.78
               +gaussian(q-laneB,0.10)*0.57
               +gaussian(q-laneC,0.065)*0.43;
    float erosion=smoothstep(0.23,0.72,coarse*0.66+fine*0.34);
    float mist=gaussian(q-drift,0.57)*(0.13+erosion*0.31);
    float hot=veins*breaks*smoothstep(0.42,0.79,fine);
    float opacity; vec3 tint=vertexColor.rgb;
    if(layer<0.5) {
        // Only broken interior highlights can turn nearly white.
        opacity=envelope*hot*0.77;
        tint=mix(tint,WHITE_CORE,0.72);
    } else if(layer<1.5) {
        opacity=envelope*(mist+veins*(0.14+breaks*0.55));
        tint=mix(tint,WHITE_CORE,clamp(hot*0.43,0.0,0.45));
    } else {
        // One broad, faint feather surrounds the internal flowing detail.
        opacity=envelope*gaussian(q-drift,0.70)*(0.08+erosion*0.27);
        tint=mix(tint,WHITE_CORE,hot*0.13);
    }
    return vec4(tint,clamp(opacity*vertexColor.a,0.0,0.81));
}


// Advanced flight uses exactly one wake mesh. Detail is nested around one axis,
// never on independent parallel ribbons. U remains actual world-path distance.
vec4 advancedTrail() {
    float band=floor((texCoord.y-8.0)*0.5+0.001);
    float style=floor(band/3.0)+1.0, layer=mod(band,3.0);
    float q=clamp(texCoord.y-(8.0+band*2.0),0.0,1.0)*2.0-1.0;
    float flow=texCoord.x+EffectTime*(5.8+style*0.28);
    float coarse=turbulence(vec3(flow*0.26,q*2.4,12.3));
    float fine=realmNoise3(vec3(flow*0.78,q*6.0+coarse*1.5,4.7));
    float drift=(coarse-0.5)*0.15+0.06*sin(flow*0.41);
    float foldA=0.34*sin(flow*0.36+coarse*1.2)+drift;
    float foldB=-0.40*sin(flow*0.29+1.9)+drift;
    float foldC=0.58*sin(flow*0.22+4.2);
    float gate=smoothstep(0.30,0.69,fine);
    float erosion=smoothstep(0.24,0.68,coarse*0.63+fine*0.37);
    // The broad blue body remains visible between sparse flowing highlights.
    // Three to four moving folds are internal to a single overall silhouette.
    float fibres=gaussian(q-foldA,max(0.072,fwidth(q)*0.55))*(0.20+gate*0.80)
               +gaussian(q-foldB,max(0.082,fwidth(q)*0.55))*erosion*0.74;
    if(style>=3.0)fibres+=gaussian(q-foldC,max(0.066,fwidth(q)*0.5))*gate*0.35;
    float envelope=pow(max(0.0,1.0-q*q),0.90);
    vec3 blue=vertexColor.rgb;
    float coverage,highlight;
    if(layer<0.5) {
        coverage=gaussian(q-drift,0.88)*(0.08+erosion*0.20)+fibres*0.48;
        highlight=fibres*0.16;
        blue=mix(blue,WHITE_CORE,0.08);
    } else if(layer<1.5) {
        coverage=gaussian(q-drift,0.88)*(0.42+erosion*0.38)+fibres*0.58;
        highlight=fibres*0.22;
    } else {
        float coreGate=smoothstep(0.36,0.68,realmNoise3(vec3(flow*0.83,23.7,6.2)));
        coverage=gaussian(q-drift,0.65)*(0.26+erosion*0.30)*(0.35+0.65*coreGate)+fibres*0.32;
        highlight=0.34+fibres*0.22;
        blue=mix(vertexColor.rgb,WHITE_CORE,0.25);
    }
    float alpha=clamp(coverage*envelope*vertexColor.a,0.0,0.72);
    vec3 hue=mix(blue,WHITE_CORE,clamp(highlight,0.0,0.58));
    return vec4(hue*(layer<0.5?1.15:layer>1.5?1.18:1.05),alpha);
}

// Angular distance is periodic, so a filament never tears at the tube seam.
float angularBand(float theta, float centre, float width) {
    return gaussian(sin((theta-centre)*0.5),width);
}

// The first four metres share the same material and energy budget at every
// advanced realm. Colour is allowed to vary, luminance is not a rank bonus.
vec3 realmTint(float layer) {
    vec3 reference=vec3(101.0,217.0,242.0)/255.0;
    if(layer>1.5) {
        float hot=1.0-smoothstep(0.0,26.6,max(trailData.x,0.0));
        reference=mix(reference,vec3(1.0),hot*0.35);
    }
    const vec3 luma=vec3(0.2126,0.7152,0.0722);
    float budget=dot(reference,luma);
    return vertexColor.rgb*(budget/max(max(vertexColor.r,vertexColor.g),max(vertexColor.b,0.000001)));
}

// x: broad interior density; y: short brighter streams. This is the Huashen
// field itself, rather than a differently scaled field for each rank.
vec2 commonFlow(float flow,float theta,float n,float fine) {
    float drift=VisualTuning.y*(3.75*sin(flow*0.42)+2.50*sin(flow*0.19+1.7));
    float phase=theta-drift-(n-0.5)*1.15;
    float a=angularBand(phase,0.85*sin(flow*0.30),0.100);
    float b=angularBand(phase,2.35+0.62*sin(flow*0.23+1.8),0.125);
    float c=angularBand(phase,-1.60+0.50*sin(flow*0.27+4.2),0.090);
    float fibres=(a+b*0.75+c*0.57)*(0.08+0.92*smoothstep(0.30,0.72,fine));
    float erosion=smoothstep(0.22,0.70,n*0.60+fine*0.40);
    return vec2(erosion*erosion,fibres);
}

// Each curl is a finite packet. Adjacent packets meet with zero curl weight,
// while the underlying plume stays continuous; there are no full-length rails.
vec2 curledFlow(float arc,float theta,float flow,float n,vec2 base) {
    float p=(arc+EffectTime*5.9*VisualTuning.z)/6.7;
    float cell=floor(p),u=fract(p),s=u*2.0-1.0;
    float window=sin(u*3.14159265);window*=window;
    float seed=hash3(vec3(cell,2.7,11.3))*6.2831853;
    float turn=seed+2.1*atan(s*2.3)+0.28*sin(flow*0.53);
    float crossA=angularBand(theta,turn,0.105+0.045*window);
    float crossB=angularBand(theta,seed+3.0-2.0*atan(s*2.3),0.12);
    float seam=0.22+0.78*smoothstep(0.22,0.68,n);
    float loops=(crossA+crossB*0.66)*window*seam;
    float inwardFold=angularBand(theta,seed+sin(s*3.1)*1.6,0.31)*window;
    return vec2(0.12+base.x*0.52+inwardFold*0.35,base.y*0.20+loops*0.92);
}

// Unequal packet speeds make broad thin folds catch and merge. Max blending
// keeps the overlap from becoming a series of bright transverse rings.
vec2 layeredFlow(float arc,float theta,float flow,float n,vec2 base) {
    float slow=(arc+EffectTime*3.9*VisualTuning.z)*0.73;
    float fast=(arc+EffectTime*7.2*VisualTuning.z)*0.91;
    float packetA=pow(0.5+0.5*cos(slow),3.0);
    float packetB=pow(0.5+0.5*cos(fast+1.7),4.0);
    float foldA=angularBand(theta,0.6*sin(slow*0.8)+n*1.5,0.31);
    float foldB=angularBand(theta,2.6+0.9*sin(fast*0.65),0.25);
    float merged=max(packetA*foldA,packetB*foldB);
    float thinA=angularBand(theta,0.6*sin(slow*0.8)+n*1.5+0.37,0.068);
    float thinB=angularBand(theta,2.6+0.9*sin(fast*0.65)-0.31,0.075);
    float strands=max(thinA*packetA,thinB*packetB);
    return vec2(0.13+base.x*0.33+merged*0.72,base.y*0.18+strands*0.75);
}

vec2 turnFlow(float flow,float theta,vec2 circle,vec2 base) {
    float curve=length(trailData.zw);
    vec2 inward=trailData.zw/max(curve,0.0001);
    float outside=max(-dot(circle,inward),0.0);
    float turn=smoothstep(0.02,0.36,curve);
    float oldWake=smoothstep(2.0,7.0,trailData.y)*(1.0-smoothstep(13.0,23.0,trailData.y));
    float haze=turbulence(vec3(flow*0.13,circle*1.8+vec2(2.1,4.7)));
    float fold=angularBand(theta,1.1*sin(flow*0.23)+haze*2.0,0.32);
    float soft=0.17+base.x*0.40+fold*(0.20+haze*0.20);
    soft+=outside*turn*oldWake*haze*0.32;
    return vec2(soft,base.y*0.42+fold*smoothstep(0.42,0.74,haze)*0.28);
}

vec4 tubeTrail() {
    float band=floor((texCoord.y-64.0)*0.5+0.001);
    float style=floor(band/3.0)+1.0,layer=mod(band,3.0);
    float theta=(texCoord.y-(64.0+band*2.0))*6.2831853;
    float flow=texCoord.x+EffectTime*5.24*VisualTuning.z;
    vec2 circle=vec2(cos(theta),sin(theta));
    float n=turbulence(vec3(flow*0.30,circle*2.2));
    float fine=realmNoise3(vec3(flow*0.78,circle*4.6+vec2(n*1.1)));
    vec2 base=commonFlow(flow,theta,n,fine),field=base;
    float advanced=smoothstep(4.0,10.0,trailData.x);
    if(style>1.5&&advanced>0.0) {
        vec2 variant;
        if(style<2.5) {
            variant=curledFlow(texCoord.x,theta,flow,n,base);
        } else if(style<3.5) {
            variant=layeredFlow(texCoord.x,theta,flow,n,base);
        } else if(style<4.5) {
            variant=turnFlow(flow,theta,circle,base);
        } else {
            vec2 largeCurl=curledFlow(texCoord.x*0.57,theta,flow*0.65,n,base);
            vec2 middleFold=layeredFlow(texCoord.x,theta,flow,n,base);
            float quickFlow=texCoord.x+EffectTime*9.6*VisualTuning.z;
            float quick=angularBand(theta,1.3*sin(quickFlow*0.52)+n*2.2,0.058);
            quick*=smoothstep(0.36,0.72,fine);
            variant=vec2(largeCurl.x*0.45+middleFold.x*0.40+base.x*0.15,
                         largeCurl.y*0.40+middleFold.y*0.34+quick*0.30);
        }
        field=mix(base,variant,advanced);
    }
    // The flow skin now supplies the entire leading wrap: no separate orb.
    // Bound coincident layer peaks, but keep broad moving energy visible in
    // daylight. The join budget fades smoothly into the historical wake.
    float headBudget=1.0-smoothstep(2.0,6.0,trailData.x);
    // Match the translucent leading volume without dimming its moving crests.
    // The historical wake beyond six metres retains its full material.
    field.x*=mix(1.0,0.90,headBudget);
    field.y=mix(field.y,field.y/(1.0+field.y*0.65),headBudget);
    float facing=clamp(abs(dot(normalize(viewNormal),normalize(-viewPosition))),0.0,1.0);
    float pulse=1.0+VisualTuning.x*sin(flow*0.32+theta+n*2.0);
    float breakup=smoothstep(0.23,0.68,realmNoise3(vec3(flow*0.55,circle*3.1)));
    float density=(field.x*0.55+field.y*1.18)*pulse;
    vec3 tint=realmTint(layer);
    const vec3 luma=vec3(0.2126,0.7152,0.0722);
    // Keep the leading join in the same realm hue as its wake.
    float white=clamp(field.y*0.36,0.0,0.55);
    if(layer<0.5) {density=field.x*0.12+field.y*0.52;tint=mix(tint,WHITE_CORE,0.08);}
    if(layer>1.5) {density=(field.x*0.30+field.y*1.02)*pulse;white=0.24+field.y*0.22;tint=mix(tint,WHITE_CORE,0.12);}
    // Never add Fresnel brightness: it outlines the volume as a glass ring.
    float feather=smoothstep(0.025,0.40,facing);
    float silhouette=layer<0.5?feather:layer<1.5?mix(0.24,1.0,feather):mix(0.30,1.0,feather);
    float alpha=clamp(density*vertexColor.a*silhouette*(0.50+0.50*breakup),0.0,0.60);
    float alphaBudget=layer<0.5?0.18:layer<1.5?0.40:0.34;
    float boundedAlpha=alphaBudget*(1.0-exp(-alpha/alphaBudget));
    alpha=mix(alpha,boundedAlpha,headBudget);
    // The geometric hemisphere closes at its nose. Do not slice its front
    // off here: its flow spreads around the pole into the equator and wake.
    // Keep broad moving coverage between thin streams without a glass rim.
    float leading=1.0-smoothstep(0.0,2.0,trailData.x);
    alpha=mix(alpha,alpha*(0.90+0.10*breakup),leading);
    float gain=1.35+field.y*0.48;
    float boundedGain=1.35+0.15*(1.0-exp(-field.y*0.48/0.15));
    gain=mix(gain,boundedGain,headBudget);
    // Preserve distant highlights; the head overlap carries realm colour.
    return vec4(mix(tint,WHITE_CORE,min(white,0.18)*(1.0-headBudget))*gain,alpha);
}

// A short-lived, low-density turn residue, carried by the old path and driven
// by its curvature. It is never a second full-length trail or a bright shell.
vec4 turnMist() {
    float band=floor((texCoord.y-128.0)*0.5+0.001);
    float theta=(texCoord.y-(128.0+band*2.0))*6.2831853;
    vec2 circle=vec2(cos(theta),sin(theta));
    float curve=length(trailData.zw);
    float outside=max(-dot(circle,trailData.zw/max(curve,0.0001)),0.0);
    float flow=texCoord.x+EffectTime*2.1*VisualTuning.z;
    float haze=turbulence(vec3(flow*0.22,circle*2.9));
    float interior=smoothstep(0.025,0.40,abs(dot(normalize(viewNormal),normalize(-viewPosition))));
    // Age, behind-distance and turn-strength gates were already applied by
    // the CPU. Repeating them here would erase normal-radius turn residue.
    float alpha=vertexColor.a*(0.18+0.62*haze*haze)*interior;
    alpha*=0.12+0.88*outside;
    return vec4(mix(realmTint(0.0),WHITE_CORE,0.14),min(alpha,0.18));
}

vec4 advancedSpark() {
    float style=floor((texCoord.y-40.0)*0.5+0.001)+1.0;
    vec2 p=vec2(texCoord.x*2.0-1.0,(texCoord.y-(40.0+(style-1.0)*2.0))*2.0-1.0);
    float glow=exp(-dot(p,p)*7.0);
    float star=(gaussian(p.x,0.065)*gaussian(p.y,0.42)+gaussian(p.y,0.065)*gaussian(p.x,0.42));
    float alpha=clamp((glow+star*(style>=4.0?0.40:0.12))*vertexColor.a,0.0,0.9);
    return vec4(WHITE_CORE,alpha);
}

vec4 energy() {return texCoord.y>=128.0?turnMist():texCoord.y>=64.0?tubeTrail():texCoord.y>=40.0?advancedSpark():texCoord.y>=8.0?advancedTrail():trailEnergy();}

void main(){
 float sceneDepth=texture(Sampler0,gl_FragCoord.xy/OutputSize).r;
 if(gl_FragCoord.z>sceneDepth+0.0000001)discard;
 fragColor=energy();if(fragColor.a<0.003)discard;
 if(EmissionPass>0.5)fragColor=vec4(fragColor.rgb*fragColor.a*(texCoord.y>=64.0?0.65:1.0),max(-viewPosition.z/128.0,0.0));
}
