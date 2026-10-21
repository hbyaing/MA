#version 150
uniform float EffectTime;
uniform float AdvectionPhase;
uniform float Strength;
in vec2 flowUV;
in vec4 vertexColor;
out vec4 fragColor;
float hash(vec2 p){vec3 q=fract(vec3(p.xyx)*.1031);q+=dot(q,q.yzx+33.33);return fract((q.x+q.y)*q.z);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);}
vec2 gas(vec2 p){
 vec2 warp=vec2(noise(p*.43+vec2(EffectTime*.13,EffectTime*.08)),noise(p*.43+vec2(19.7,7.1)+vec2(-EffectTime*.06,EffectTime*.12)))-.5;
 vec2 q=p+warp*1.8;
 float coarse=noise(q*.72)*.70+noise(q*1.45+vec2(31.4,9.2))*.30;
 float detail=noise(q*1.9+vec2(7.2,21.4)),fine=noise(q*4.1+vec2(12.8,4.6));
 float breakup=smoothstep(.20,.72,coarse+.20*detail);
 float filament=exp(-pow((detail+.20*fine-.62)/.085,2.))*breakup*smoothstep(.25,.75,fine);
 float mist=smoothstep(.18,.76,coarse)*(.21+.47*fine);
 return vec2(mist,filament);
}
void main(){
 vec2 n=flowUV*2.-1.;
 // Independent top, side and bottom masks retain the accepted lower extent.
 float mask=max(smoothstep(.42,.96,abs(n.x)),max(smoothstep(.44,.97,-n.y),smoothstep(.56,.98,n.y)));
 if(mask<.0001)discard;
 float travel=AdvectionPhase;
 // Blend independently advected edge fields, not closed polar contours.
 // Time cannot increase spatial frequency or turn wisps into radial needles.
 float side=smoothstep(-.22,.22,abs(n.x)-abs(n.y));
 vec2 horizontal=vec2(0),vertical=vec2(0);
 if(side>.001)horizontal=gas(n*vec2(3.5,8.)-vec2(sign(n.x)*travel,0));
 if(side<.999)vertical=gas(n*vec2(8.,3.5)-vec2(0,sign(n.y)*travel));
 vec2 field=mix(vertical,horizontal,side);
 float mist=field.x,filament=field.y;
 float density=(mist*.70+filament*.48)*mask;
 float alpha=min(.42,density*Strength*.78);
 vec3 color=mix(vertexColor.rgb,vec3(.88,1.,.98),filament*.23)*(1.+filament*.28);
 fragColor=vec4(color,alpha);
}
