#version 150
in vec3 Position;
in vec4 Color;
in vec3 Normal;
uniform mat4 ClipMat;
out vec3 sphereNormal;
out vec3 relativePosition;
void main(){sphereNormal=Normal;relativePosition=Position;gl_Position=ClipMat*vec4(Position,1.0);}
