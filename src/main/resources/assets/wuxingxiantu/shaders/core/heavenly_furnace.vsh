#version 150
in vec3 Position;
in vec2 UV0;
uniform mat4 EffectMatrix;
uniform vec3 Center;
uniform float Radius;
uniform float EffectTime;
uniform float Mode;
uniform float Layer;
out vec3 localPos;
out vec2 texCoord;
out vec3 viewRay;
void main(){
    vec3 p=Position;
    localPos=(p-Center)/max(Radius,0.01);
    if(Mode<0.5){
        float flow=sin(localPos.y*9.0-EffectTime*3.2+sin(UV0.x*18.8496+EffectTime)*1.8);
        float curl=sin(UV0.x*37.699+localPos.y*8.0-EffectTime*4.0);
        float crest=pow(max(0.0,flow*curl),3.0);
        p+=normalize(localPos)*Radius*(0.035*flow+crest*0.075)*(0.35+Layer*0.325);
        p.y+=Radius*crest*0.12;
    }
    texCoord=UV0;viewRay=p;
    gl_Position=EffectMatrix*vec4(p,1.0);
}
