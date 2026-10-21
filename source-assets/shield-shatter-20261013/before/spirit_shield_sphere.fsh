#version 150
uniform float Time;
uniform float Integrity;
uniform float Strength;
uniform float Hit;
uniform float HitAge;
uniform vec3 HitDirection;
in vec3 sphereNormal;
in vec3 relativePosition;
out vec4 fragColor;
float hexEdge(vec2 p){
    p*=7.0;vec2 cell=vec2(1.73205,3.0);
    vec2 a=mod(p,cell)-cell*.5;
    vec2 b=mod(p-cell*.5,cell)-cell*.5;
    vec2 q=dot(a,a)<dot(b,b)?a:b;
    float edge=max(abs(q.x)*.866025+abs(q.y)*.5,abs(q.y));
    return smoothstep(.74,.86,edge);
}
void main(){
    vec3 n=normalize(sphereNormal),dominant=abs(n);
    float rim=pow(1.0-abs(dot(n,normalize(-relativePosition))),2.5);
    vec2 uv=dominant.x>dominant.y && dominant.x>dominant.z?n.yz:(dominant.y>dominant.z?n.xz:n.xy);
    float pattern=hexEdge(uv);
    float flow=.5+.5*sin(n.y*14.0-Time*1.4);
    float angle=acos(clamp(dot(n,normalize(HitDirection)),-1.0,1.0));
    float age=clamp(HitAge,0.0,1.0);
    float radius=age*1.65;
    float fade=1.0-smoothstep(.58,1.0,age);
    float width=mix(.025,.045,age);
    float ring=exp(-pow((angle-radius)/width,2.0))*fade;
    float halo=exp(-pow((angle-radius)/(width*2.8),2.0))*fade*.12;
    float echoRadius=max(0.0,age-.16)*1.65;
    float echo=exp(-pow((angle-echoRadius)/(width*.8),2.0))
            *smoothstep(.16,.23,age)*fade*.32;
    float contact=exp(-pow(angle/.14,2.0))*pow(Hit,8.0);
    float wave=(ring+halo+echo)*Hit;
    // The impact stays localized; no full-sphere white flash or great-circle band.
    float alpha=Strength*(.006+.15*rim+.008*pattern*rim+.003*flow*rim
            +wave*.48+contact*.32);
    vec3 color=mix(vec3(.18,.62,.96),vec3(.62,.90,1.0),clamp(rim*.45+ring*.75+contact,0.0,1.0));
    float wear=1.0-clamp(Integrity,0.0,1.0);
    vec3 warm=mix(vec3(.60,.28,.92),vec3(1.0,.12,.08),smoothstep(.4,1.0,wear));
    color=mix(color,warm,smoothstep(.08,.90,wear));
    fragColor=vec4(color,clamp(alpha,0.0,.58));
}
