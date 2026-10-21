#version 150
in vec2 vUV;out vec4 fragColor;
uniform sampler2D uScene,uBloom0,uBloom1,uBloom2,uBloom3,uBloom4;
uniform float uBloom,uExposure,uVignette,uOpacity;
uniform sampler2D uWorldDepth;uniform mat4 uInvVP;uniform vec3 uEye;
vec3 aces(vec3 x){return clamp((x*(2.51*x+.03))/(x*(2.43*x+.59)+.14),0.,1.);}
void main(){
 float worldZ=texture(uWorldDepth,vUV).r;
 if(worldZ<.999999){vec4 w=uInvVP*vec4(vUV*2.-1.,worldZ*2.-1.,1.);vec3 d=w.xyz/w.w-uEye;float dist=length(d);vec3 rd=d/max(dist,.0001);float b=dot(uEye,rd),h=b*b-dot(uEye,uEye)+12.96;if(h>0.&&dist<max(0.,-b-sqrt(h))){fragColor=vec4(0);return;}}
 vec4 scene=texture(uScene,vUV);vec3 c=scene.rgb;
 vec3 glow=texture(uBloom0,vUV).rgb*.25+texture(uBloom1,vUV).rgb*.28+texture(uBloom2,vUV).rgb*.27+texture(uBloom3,vUV).rgb*.24+texture(uBloom4,vUV).rgb*.19;
 c=pow(aces(c/max(scene.a,.0001)*uExposure),vec3(1./2.2))*scene.a;
 c+=pow(aces(glow*uBloom*uExposure),vec3(1./2.2))*.55;
 vec2 p=(vUV-.5)*2.;c*=1.-uVignette*.28*dot(p,p);
 fragColor=vec4(c*uOpacity,scene.a*uOpacity);
}