#version 150
in vec2 vUV;out vec4 fragColor;uniform sampler2D uSource;uniform vec2 uDirection;
void main(){vec3 c=texture(uSource,vUV).rgb*.227027;
 c+=(texture(uSource,vUV+uDirection*1.384615).rgb+texture(uSource,vUV-uDirection*1.384615).rgb)*.316216;
 c+=(texture(uSource,vUV+uDirection*3.230769).rgb+texture(uSource,vUV-uDirection*3.230769).rgb)*.070270;
 fragColor=vec4(c,1);}