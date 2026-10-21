#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 texel = texture(Sampler0, texCoord0);
    // The authored light/gem PNGs use 10/255 and 24/255 alpha. Amplify only these
    // two materials before the discard threshold; their source images stay intact.
    float alpha = min(texel.a * 7.0, 0.68) * vertexColor.a * ColorModulator.a;
    if (alpha < 0.004) discard;
    vec3 color = texel.rgb * vertexColor.rgb * ColorModulator.rgb;
    // Alpha blending retains the ice-blue tint when several filaments overlap.
    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
