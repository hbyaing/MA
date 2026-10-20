#version 150
uniform sampler2D Sampler0;uniform vec4 ColorModulator;in vec2 uv;in vec4 tint;out vec4 fragColor;
void main(){vec4 c=texture(Sampler0,uv);if(c.a<.1)discard;float grey=dot(c.rgb,vec3(.2126,.7152,.0722));fragColor=vec4(vec3(grey),c.a)*tint*ColorModulator;}
