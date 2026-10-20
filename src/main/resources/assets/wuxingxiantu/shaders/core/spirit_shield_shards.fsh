#version 150
uniform float Time;
uniform float ShatterAge;
uniform float ShatterDuration;
uniform float ShatterFlash;
uniform float Mode;
in vec3 materialPosition;
in vec3 relativePosition;
in vec3 shardNormal;
in float vertexAlpha;
out vec4 fragColor;
float shieldBell(float d, float w) {float q=d/max(w,0.0001);return exp(-q*q);}
float hexEdge(vec2 p) {
    p*=7.0;vec2 cell=vec2(1.73205,3.0);
    vec2 a=mod(p,cell)-cell*.5;
    vec2 b=mod(p-cell*.5,cell)-cell*.5;
    vec2 q=dot(a,a)<dot(b,b)?a:b;
    return smoothstep(.74,.86,max(abs(q.x)*.866025+abs(q.y)*.5,abs(q.y)));
}
void main() {
    float fade=1.0-smoothstep(ShatterDuration*.45,ShatterDuration,ShatterAge);
    vec3 normal=normalize(shardNormal);
    float rim=pow(1.0-abs(dot(normal,normalize(-relativePosition))),2.0);
    vec3 source=normalize(materialPosition),major=abs(source);
    vec2 uv=major.x>major.y&&major.x>major.z?source.yz:(major.y>major.z?source.xz:source.xy);
    // Exclusive source-plane projection, with a small blank boundary band.
    float high=max(max(major.x,major.y),major.z);
    float mid=major.x+major.y+major.z-high-min(min(major.x,major.y),major.z);
    float seam=smoothstep(.015,.070,high-mid);
    float pattern=hexEdge(uv)*seam;
    float flash=exp(-ShatterAge*11.0)*ShatterFlash;
    vec3 warm=vec3(1.0,.15,.08);
    float alpha;
    if(Mode<.5) {
        float flow=.5+.5*sin(source.y*14.0-Time*1.4);
        // Curved fragment faces are clearer than the near-transparent intact
        // shell; their original hex material travels with the rigid piece.
        alpha=(.070+rim*.21+pattern*.045+flow*.008+flash*.05)*fade*vertexAlpha;
        warm=mix(warm,vec3(1.0,.40,.12),pattern*.15+rim*.22+flash*.16);
    } else if(Mode<1.5) {
        alpha=(.66+flash*.08)*fade*vertexAlpha;
        warm=vec3(1.0,.36,.12);
    } else {
        alpha=vertexAlpha*fade;
        warm=mix(vec3(1.0,.26,.04),vec3(1.0,.66,.23),vertexAlpha*.40);
    }
    if(alpha<.004)discard;
    fragColor=vec4(warm,min(alpha,.88));
}
