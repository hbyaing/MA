#version 150
in vec2 vUV;out vec4 fragColor;uniform sampler2D uSource;uniform vec2 uTexel;uniform float uThreshold;
void main(){
 vec3 c=texture(uSource,vUV).rgb*.20;
 c+=(texture(uSource,vUV+uTexel*vec2(1,1)).rgb+texture(uSource,vUV+uTexel*vec2(-1,1)).rgb+texture(uSource,vUV+uTexel*vec2(1,-1)).rgb+texture(uSource,vUV+uTexel*vec2(-1,-1)).rgb)*.10;
 c+=(texture(uSource,vUV+uTexel*vec2(2,0)).rgb+texture(uSource,vUV+uTexel*vec2(-2,0)).rgb+texture(uSource,vUV+uTexel*vec2(0,2)).rgb+texture(uSource,vUV+uTexel*vec2(0,-2)).rgb)*.10;
 float br=max(c.r,max(c.g,c.b));float contribution=max(br-uThreshold,0.)/max(br,.001);fragColor=vec4(c*(uThreshold>0.?contribution:1.),1);
}