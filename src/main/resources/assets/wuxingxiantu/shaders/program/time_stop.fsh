#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Phase;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec2 uv = (texCoord - 0.5) * vec2(OutSize.x / OutSize.y, 1.0);
    float maxRadius = length(vec2(OutSize.x / OutSize.y, 1.0)) * 0.5;
    float distanceFromCenter = length(uv) / maxRadius;
    float radius = Phase * 1.18;
    float mask = (1.0 - smoothstep(radius - 0.12, radius, distanceFromCenter))
        * smoothstep(0.0, 0.1, Phase);
    float luminance = dot(scene.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 gray = vec3(clamp((luminance - 0.5) * 1.08 + 0.5, 0.0, 1.0));
    float ring = (1.0 - smoothstep(0.0, 0.035, abs(distanceFromCenter - radius + 0.06)))
        * sin(Phase * 3.14159265) * 0.18;
    fragColor = vec4(mix(scene.rgb, gray, mask) + ring, scene.a);
}
