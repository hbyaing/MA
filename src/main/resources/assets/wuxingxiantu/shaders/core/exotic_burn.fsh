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
    // Atlas sprites already contain the shared tier palette; never recolor or multiply it twice.
    vec3 rgb=texel.rgb*vertexColor.rgb;
    float alpha=texel.a*vertexColor.a*ColorModulator.a*linear_fog_fade(vertexDistance,FogStart,FogEnd);
    if(alpha<0.02)discard;
    fragColor=vec4(rgb*ColorModulator.rgb,alpha);
}
