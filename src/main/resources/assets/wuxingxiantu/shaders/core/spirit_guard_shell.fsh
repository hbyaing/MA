#version 150
in vec3 viewPos;in vec3 viewNormal;in vec4 vertexColor;in vec2 uv;
uniform vec4 ColorModulator;out vec4 fragColor;
void main(){
 float rim=pow(clamp(1.-abs(dot(normalize(viewNormal),normalize(-viewPos))),0.,1.),1.6);
 float age=clamp(uv.y,0.,1.);
 float wave=exp(-pow((uv.x-age*2.2)/.075,2.))*(1.-age);
 float echo=exp(-pow((uv.x-age*2.2+.18)/.06,2.))*(1.-age)*.4;
 // Only impact rings: no continuous blue body coating.
 float a=wave*.55+echo*.2;
 if(a<.008)discard;
 vec3 blue=mix(vec3(.40,.72,1.),vec3(.70,.92,1.),rim);
 blue+=vec3(.25,.45,.55)*(wave+echo);
 fragColor=vec4(blue,min(a,.75))*ColorModulator;
}
