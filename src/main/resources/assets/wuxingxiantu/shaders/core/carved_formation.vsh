#version 150
in vec3 Position;in vec4 Color;uniform mat4 ModelViewMat;uniform mat4 ProjMat;uniform mat4 LocalFromPosition;out vec3 localPos;
void main(){localPos=(LocalFromPosition*vec4(Position,1.)).xyz;gl_Position=ProjMat*ModelViewMat*vec4(Position,1.);}
