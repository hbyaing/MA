#version 150
uniform float EffectTime;
uniform float Mode;
uniform float Layer;
uniform float Opacity;
uniform float FireTier;
uniform vec3 FireColor;
uniform sampler2D Sampler0;
uniform vec2 OutputSize;
uniform float UseSceneDepth;
in vec3 localPos;
in vec2 texCoord;
in vec3 viewRay;
out vec4 fragColor;
float hash(vec3 p){p=fract(p*0.3183099+vec3(.13,.71,.37));p*=17.0;return fract(p.x*p.y*p.z*(p.x+p.y+p.z));}
float noise(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);}
void main(){
    if(UseSceneDepth>.5&&gl_FragCoord.z>texture(Sampler0,gl_FragCoord.xy/OutputSize).r+.0000001)discard;
    vec3 color=FireColor;
    if(FireTier>8.5){
        float phase=mod(EffectTime*20.0+localPos.y*12.0,128.0)/32.0;
        vec3 a=phase<1.0?vec3(.251,.878,.816):phase<2.0?vec3(1,.439,.722):phase<3.0?vec3(.690,.439,1):vec3(1,.831,.361);
        vec3 b=phase<1.0?vec3(1,.439,.722):phase<2.0?vec3(.690,.439,1):phase<3.0?vec3(1,.831,.361):vec3(.251,.878,.816);
        color=mix(a,b,(1.0-cos(fract(phase)*3.14159265))*.5);
    }
    if(Mode>0.5){
        if(Mode>3.5){
            float u=fract(texCoord.x),edge=pow(max(0.,1.-abs(texCoord.y*2.-1.)),1.7);
            float flow=.55+.45*sin(u*25.-EffectTime*3.2);
            vec3 yin=vec3(.12,.27,.44),yang=mix(vec3(1.,.72,.3),color,.3);
            vec3 c=texCoord.x<1.?yang:yin;
            float rim=exp(-abs(texCoord.y-.5)*18.);
            fragColor=vec4(c*(.75+rim*.5),edge*(.45+flow*.35)*sin(u*3.14159265)*Opacity);return;
        }
        if(Mode>2.5){
            float edge=pow(max(0.,1.-abs(texCoord.y*2.-1.)),2.2);
            float lace=noise(vec3(texCoord.x*52.-EffectTime*2.1,texCoord.y*5.,EffectTime*.18));
            float flow=.48+.52*smoothstep(.22,.8,lace);
            float vein=pow(max(0.,cos(texCoord.x*62.-EffectTime*6.+lace*4.)),10.);
            vec3 spirit=mix(vec3(.37,.72,.88),color,.12);
            vec3 pearl=mix(spirit,vec3(1.,.91,.71),vein*.55);
            float dissolve=1.-smoothstep(.92,1.,texCoord.x);
            fragColor=vec4(pearl*(1.+vein*.7),edge*flow*.85*Opacity*dissolve);return;
        }
        if(Mode>1.5){
            float edge=pow(max(0.0,1.0-abs(texCoord.y*2.0-1.0)),1.5);
            float pulse=.65+.35*sin(texCoord.x*12.0-EffectTime*8.0);
            fragColor=vec4(mix(color,vec3(1),.5),edge*texCoord.x*pulse*Opacity);return;
        }
        float edge=smoothstep(0.0,.22,texCoord.y)*(1.0-smoothstep(.78,1.0,texCoord.y));
        float arc=pow(.5+.5*sin(texCoord.x*18.84956-EffectTime*1.5),12.0);
        float ticks=step(.82,fract(texCoord.x*48.0))*smoothstep(.25,.5,texCoord.y);
        float line=exp(-abs(texCoord.y-.5)*24.0);
        float rails=exp(-abs(texCoord.y-.22)*45.0)+exp(-abs(texCoord.y-.78)*45.0);
        float a=edge*(.28+line*.5+arc*.42+ticks*.38+rails*.35)*Opacity;
        fragColor=vec4(mix(color,vec3(1,.86,.46),.65)*(1.1+arc*.5),min(.95,a));return;
    }
    vec3 p=localPos;float t=EffectTime;
    float angle=atan(p.z,p.x);float turn=angle+sin(p.y*3.5-t*1.8)*.6+t*.6;
    vec3 q=vec3(cos(turn),p.y-t*.65,sin(turn))*3.8;
    float n=noise(q+vec3(0,noise(q*.6+t*.15)*1.8,0));
    float detail=noise(q*2.3-vec3(t*.25,t,0));
    float folds=sin(p.y*12.0+angle*3.0-t*4.0+(n-.5)*5.0);
    float vein=pow(1.0-abs(folds),7.0);
    float flame=smoothstep(.34,.67,n*.72+detail*.28+folds*.12);
    vec3 normal=normalize(p);float rim=pow(1.0-abs(dot(normal,normalize(viewRay))),2.0);
    float a=Layer<.5?.93:(Layer<1.5?flame*.66:flame*.34+vein*.15);
    float heat=clamp(vein*.65+flame*.42+(1.0-Layer*.5)*.27,0.0,1.0);
    vec3 rgb=mix(color*.32,color*1.5,heat)+vec3(1.0,.95,.8)*pow(heat,5.0)*.65;
    // Clear the near-facing centre so the workpiece remains visible through the furnace.
    a*=mix(.16,1.0,smoothstep(.12,.75,rim));
    rgb+=color*rim*.6;fragColor=vec4(rgb,a*Opacity);
}
