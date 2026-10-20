#version 150
in vec3 Position;in vec3 Normal;in vec4 Color;in vec2 UV0;
uniform mat4 ModelViewMat;uniform mat4 ProjMat;
out vec3 viewPos;out vec3 viewNormal;out vec4 vertexColor;out vec2 uv;
void main(){vec4 p=ModelViewMat*vec4(Position,1.);viewPos=p.xyz;viewNormal=mat3(transpose(inverse(ModelViewMat)))*Normal;vertexColor=Color;uv=UV0;gl_Position=ProjMat*p;}
