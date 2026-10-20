#version 150
in vec3 Position;
in vec2 UV0;
in vec4 Color;
uniform mat4 TrailProjection;
uniform mat4 TrailView;
out vec2 trailCoord;
out vec4 vertexColor;
out vec3 viewPosition;
void main(){
    gl_Position=TrailProjection*vec4(Position,1.0);
    trailCoord=UV0;vertexColor=Color;
    viewPosition=(TrailView*vec4(Position,1.0)).xyz;
}
