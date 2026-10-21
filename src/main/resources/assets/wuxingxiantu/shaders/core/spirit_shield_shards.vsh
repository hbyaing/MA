#version 150
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
uniform mat4 ClipMat;
out vec3 materialPosition;
out vec3 relativePosition;
out vec3 shardNormal;
out float vertexAlpha;
void main() {
    // Material position belongs to the frozen original sphere; lighting normal
    // and Position follow the independently rotating rigid fragment.
    materialPosition = Color.rgb * 2.0 - 1.0;
    relativePosition = Position;
    shardNormal = Normal;
    vertexAlpha = Color.a;
    gl_Position = ClipMat * vec4(Position, 1.0);
}
