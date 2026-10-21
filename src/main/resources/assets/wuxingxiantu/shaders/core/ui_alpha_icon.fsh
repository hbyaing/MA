#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
in vec2 texCoord;
out vec4 fragColor;
void main(){float a=texture(Sampler0,texCoord).a;if(a<0.001)discard;fragColor=vec4(ColorModulator.rgb,a*ColorModulator.a);}
