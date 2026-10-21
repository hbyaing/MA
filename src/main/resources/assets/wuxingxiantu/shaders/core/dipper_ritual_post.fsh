#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 OutputSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main(){
    float sceneDepth=texture(Sampler1,gl_FragCoord.xy/OutputSize).r;
    if(gl_FragCoord.z>sceneDepth+0.0000001)discard;
    vec4 color=texture(Sampler0,texCoord)*vertexColor;
    if(color.a<0.003)discard;
    fragColor=color;
}
