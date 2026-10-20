#version 150

// The saved world pose supplies view-space positions and ellipsoid normals.
in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 TrailProjection;
uniform mat4 TrailView;

out vec2 texCoord;
out vec4 vertexColor;
out vec3 viewPosition;
out vec3 viewNormal;
out vec4 trailData;

void main() {
    gl_Position = TrailProjection * vec4(Position, 1.0);
    texCoord = UV0;
    vertexColor = Color;
    viewPosition = (TrailView * vec4(Position, 1.0)).xyz;
    viewNormal = mat3(TrailView) * Normal;
    trailData = vec4(float(UV1.x) / 256.0 - 4.0, float(UV1.y) / 256.0, vec2(UV2) / 16384.0);
}
