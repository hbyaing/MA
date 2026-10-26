#version 150
in vec3 localPos;uniform float Time;uniform float Kind;uniform float Mode;uniform vec3 Eye;out vec4 fragColor;
const float PI=3.14159265359;
float band(float d,float w){return exp(-abs(d)/w);}
void main(){vec3 jade=vec3(.12,.84,.67),gold=vec3(1.,.73,.23),red=vec3(.88,.055,.1);vec3 tint=Kind>1.5?red:jade;float t=Time;
 if(Mode<.5){vec2 p=localPos.xz;float r=length(p),a=atan(p.y,p.x);float q=band(r-.99,.004)+band(r-.94,.003)+band(r-.72,.003)+band(r-.18,.005);
 float ticks=pow(max(0.,cos((a+t*.075)*64.)),24.)*smoothstep(.86,.89,r)*(1.-smoothstep(.93,.94,r));
 float rune=band(sin((a-t*.09)*24.),.09)*band(r-.79,.035);float spokes=pow(max(0.,cos(a*8.+t*.12)),90.)*smoothstep(.18,.2,r)*(1.-smoothstep(.7,.74,r));
 float mist=exp(-pow((r-.98)/.075,2.))*(.08+.05*sin(a*10.-t));float glow=q*.72+ticks*.42+rune*.55+spokes*.24+mist;
 vec3 c=mix(tint,gold,clamp(rune,0.,1.)*.65)*glow;float alpha=clamp(glow*.72,0.,.9);if(alpha<.006)discard;fragColor=vec4(c,alpha);
 }else{vec3 n=normalize(vec3(localPos.x,localPos.y/.64,localPos.z));float rim=pow(1.-abs(dot(n,normalize(Eye-localPos))),2.3);float scan=pow(.5+.5*sin(localPos.y*36.-t*1.4),30.);float glyph=pow(max(0.,sin(atan(localPos.z,localPos.x)*24.+t*.3)),45.)*pow(.5+.5*cos(localPos.y*28.),18.);float alpha=clamp(.015+rim*.16+scan*.035+glyph*.1,.0,.3);fragColor=vec4(mix(tint,gold,glyph*.5)*(.45+rim*.6+scan*.2),alpha);}
}
