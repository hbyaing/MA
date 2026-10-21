#version 150

// Position and Normal have already received the same Java pose transformation.
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 viewPosition;
out vec3 viewNormal;
out vec4 trailData;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    texCoord = UV0;
    vertexColor = Color;
    viewPosition = view.xyz;
    viewNormal = mat3(ModelViewMat) * Normal;
    trailData = vec4(float(UV1.x) / 256.0 - 4.0, float(UV1.y) / 256.0, vec2(UV2) / 16384.0);
}
