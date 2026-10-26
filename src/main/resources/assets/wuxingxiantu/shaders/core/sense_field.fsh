#version 150
uniform sampler2D Sampler0;
uniform vec2 OutputSize;
uniform float ShapeMode;
uniform float EffectTime;
uniform float Progress;
in vec2 uv;
in vec4 tint;
out vec4 fragColor;
const float TAU=6.28318530718;
float band(float x,float center,float width){return exp(-pow((x-center)/width,2.0));}
void main(){
    float scene=texture(Sampler0,gl_FragCoord.xy/OutputSize).r;
    if(gl_FragCoord.z>scene+0.0000001)discard;
    vec2 p=uv*2.0-1.0;float r=length(p),a=atan(p.y,p.x),t=EffectTime;
    float alpha=0.0,hot=0.0;
    if(ShapeMode<0.5){
        // Real expanding sphere: latitude ribbons and staggered longitude echoes.
        float lat=uv.y*3.14159265,lon=uv.x*TAU;
        float ribbon=pow(max(0.0,cos(lat*14.0-t*2.4)),28.0);
        float meridian=pow(max(0.0,cos(lon*18.0+lat*3.0)),42.0);
        float strands=pow(max(0.0,sin(lon*9.0-lat*13.0+t*2.0)),20.0);
        alpha=(ribbon*.36+meridian*.16+strands*.08)*smoothstep(0.02,.18,sin(lat));hot=ribbon;
    }else if(ShapeMode<1.5){
        float arc=.58+.42*sin(a*7.0-t*2.0);
        alpha=band(r,.78,.012)+band(r,.87,.036)*.25+band(r,.62,.009)*arc*.55;
        alpha+=band(r,.82,.075)*.11;hot=band(r,.78,.012);
    }else if(ShapeMode<2.5){
        float sectors=step(.28,abs(sin(a*2.0)));
        float brackets=band(r,.78,.012)*sectors;
        float ticks=band(r,.9,.018)*pow(max(0.0,cos(a*24.0)),34.0);
        float focus=band(r,mix(.68,.36,Progress),.012)*(.55+.45*sin(t*3.0+a*3.0));
        float crossLine=exp(-min(p.x*p.x,p.y*p.y)/.00009)*step(.06,r)*step(r,.16);
        alpha=brackets+ticks*.58+focus*.7+crossLine*.6;hot=brackets;
    }else if(ShapeMode<3.5){
        float wisps=pow(max(0.0,sin(a*5.0+t*1.5+r*14.0)),12.0);
        alpha=band(r,.72,.022)*(.35+.65*wisps)+band(r,.86,.008)*.65;
        alpha+=band(r,.6,.09)*wisps*.24;hot=band(r,.86,.008);
    }else if(ShapeMode<4.5){
        float angle=fract((a+3.14159265)/TAU);
        float segment=pow(max(0.0,cos(angle*TAU*64.0)),18.0);
        alpha=band(r,.79,.016)*segment*(angle<Progress?1.0:.16);
        alpha+=band(r,.88,.01)*(.4+.3*cos(a*3.0-t*.9));hot=segment;
    }else{
        // A flowing, tapered energy thread; the actual quad rises in world space.
        float center=.5+.18*sin(uv.y*5.0-t*2.4);
        float distance=abs(uv.x-center);
        float envelope=smoothstep(0.0,.18,uv.y)*(1.0-smoothstep(.62,1.0,uv.y));
        hot=exp(-distance*distance/.0009);
        alpha=(hot*.9+exp(-distance*distance/.014)*.17)*envelope;
    }
    alpha*=tint.a;if(alpha<.003)discard;
    vec3 color=mix(tint.rgb,vec3(.84,.98,1.0),clamp(hot*.35,0.0,.45));
    fragColor=vec4(color,min(alpha,.9));
}
