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
float hash(vec2 p) { return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453); }
float noise(vec2 p) {
    vec2 cell=floor(p),f=fract(p);
    f=f*f*(3.0-2.0*f);
    return mix(mix(hash(cell),hash(cell+vec2(1,0)),f.x),
               mix(hash(cell+vec2(0,1)),hash(cell+vec2(1,1)),f.x),f.y);
}
void main() {
    vec4 color=texture(Sampler0,texCoord0)*vertexColor*ColorModulator;
    float across=abs(texCoord0.y*2.0-1.0);
    // UV advection is supplied by the local-space mesh, including each fire's flow speed.
    vec2 pixels=floor(texCoord0*vec2(72.0,12.0))/vec2(72.0,12.0);
    float tongues=noise(vec2(pixels.x*19.0,pixels.y*3.0));
    float boundary=mix(0.84,1.0,tongues);
    float fringe=1.0-smoothstep(boundary-0.10,boundary,across);
    float holes=step(0.72,noise(vec2(pixels.x*31.0,pixels.y*9.0)));
    color.a*=fringe*(1.0-holes*smoothstep(0.15,0.8,across));
    color.a*=0.8+0.2*tongues;
    color.a*=linear_fog_fade(vertexDistance,FogStart,FogEnd);
    if(color.a<0.005)discard;
    fragColor=color;
}
