#version 150
#moj_import <fog.glsl>
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
in vec4 vertexColor;
in vec2 texCoord0;
in float vertexDistance;
out vec4 fragColor;
void main() {
    vec4 texel=texture(Sampler0,texCoord0);
    vec3 source=texel.rgb;
    float alpha=texel.a;
    vec3 rgb=source;
    // Orange reference fire retains source RGB; other equipped fires retain their gameplay palette.
    bool referencePalette=distance(vertexColor.rgb,vec3(1.0,116.0/255.0,32.0/255.0))<=0.025;
    if(!referencePalette) {
        float heat=smoothstep(0.28,0.92,source.g);
        rgb=mix(vertexColor.rgb*max(0.45,source.r),mix(vertexColor.rgb,vec3(1.0),0.78),heat);
    }
    // Separate the hot core from the red fringe without blurring the reference's pixel edges.
    float core=smoothstep(0.58,0.94,source.g)*smoothstep(0.75,1.0,source.r);
    float edge=1.0-smoothstep(0.18,0.48,source.g);
    rgb=mix(rgb,rgb*0.80,edge*0.45);
    vec3 coreColor=referencePalette?vec3(1.0,0.97,0.82):mix(vertexColor.rgb,vec3(1.0),0.86);
    rgb=mix(rgb,coreColor,core*0.32);
    alpha=smoothstep(0.01,0.88,alpha);
    alpha*=vertexColor.a*ColorModulator.a*linear_fog_fade(vertexDistance,FogStart,FogEnd);
    if(alpha<0.02)discard;
    fragColor=vec4(rgb*ColorModulator.rgb,alpha);
}
