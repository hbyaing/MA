#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 GuardClipMat;

out vec3 surfacePosition;
out vec3 surfaceNormal;
out vec2 texCoord;
out float surfaceAlpha;

void main() {
    // Java submits the same posed armor vertices as the base draw. HitPosition
    // and HitNormal are transformed by the identical PoseStack, per draw.
    surfacePosition = Position;
    surfaceNormal = Normal;
    texCoord = UV0;
    surfaceAlpha = Color.a;
    gl_Position = GuardClipMat * vec4(Position, 1.0);
    gl_Position.z -= 0.0000005 * gl_Position.w;
}
