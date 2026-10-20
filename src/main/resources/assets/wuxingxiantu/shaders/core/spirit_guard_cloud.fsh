#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D SceneDepth;
uniform float SceneDepthEnabled;
uniform vec4 ColorModulator;
uniform float EffectTime;
uniform float HitAge;
uniform vec3 HitPosition;
uniform vec3 HitNormal;
uniform float Intensity;
uniform float CloudEnabled;
uniform float GuardLevel;

in vec3 surfacePosition;
in vec3 surfaceNormal;
in vec2 texCoord;
in float surfaceAlpha;
out vec4 fragColor;

float cloudBell(float distance, float width) {
    float q = distance / max(width, 0.00001);
    return exp(-q * q);
}

void main() {
    if(SceneDepthEnabled>.5){
        vec2 pixel=gl_FragCoord.xy/vec2(textureSize(SceneDepth,0));
        if(gl_FragCoord.z>texture(SceneDepth,pixel).r+0.000002)discard;
    }
    // Alpha follows the actual armor texture, including leather/custom cutouts.
    float armorAlpha = texture(Sampler1, texCoord).a * surfaceAlpha;
    if (armorAlpha < 0.1) discard;

    vec4 glyph = texture(Sampler0, texCoord);
    float coverage = glyph.r;
    // Progress and cluster phase are coverage-premultiplied data, preventing
    // texture filtering from changing the animation at thin brush edges.
    float progress = clamp(glyph.g / max(coverage, 0.00001), 0.0, 1.0);
    float offset = clamp(glyph.b / max(coverage, 0.00001), 0.0, 1.0);
    float level = clamp(floor(GuardLevel + 0.5), 1.0, 4.0);
    float cycle = level < 1.5 ? 4.8 : (level < 2.5 ? 4.0 : (level < 3.5 ? 3.6 : 3.0));
    float phase = fract(EffectTime / cycle + offset);
    float writeHead = clamp(phase / 0.38, 0.0, 1.15);
    float eraseHead = clamp((phase - 0.60) / 0.34, 0.0, 1.18);
    float written = 1.0 - smoothstep(writeHead - 0.045, writeHead + 0.025, progress);
    float retained = smoothstep(eraseHead - 0.04, eraseHead + 0.055, progress);
    float cycleFade = 1.0 - smoothstep(0.94, 0.995, phase);
    float head = cloudBell(progress - writeHead, 0.065)
               * (1.0 - smoothstep(0.36, 0.42, phase));
    float movingLight = 0.82 + 0.18 * sin(progress * 12.0 - phase * 6.2831853 + offset * 6.2831853);
    float crispCoverage = smoothstep(0.10, 0.80, coverage);
    float innerCore = smoothstep(0.72, 0.98, coverage);
    // A quiet complete trace prevents lower levels from becoming isolated curls
    // during the write/erase cycle. The brighter head still visibly travels.
    float cloud = crispCoverage * mix(0.25, 1.0, written * retained * cycleFade) * CloudEnabled;
    float alpha = cloud * (0.61 * movingLight + innerCore * 0.08 + head * 0.22);
    // The highest level carries a second narrow light pulse inside the strokes.
    // Coverage still clips it to the actual glyph; the metal is never recolored.
    float escort = cloudBell(progress - fract(phase + 0.46), 0.028);
    alpha += level > 3.5 ? cloud * innerCore * escort * 0.11 : 0.0;
    vec3 bodyColor = level < 1.5 ? vec3(.12, .48, .78)
                   : level < 2.5 ? vec3(.08, .68, .82)
                   : level < 3.5 ? vec3(.21, .49, 1.0)
                   : vec3(.43, .58, 1.0);
    vec3 coreColor = level < 1.5 ? vec3(.30, .66, .90)
                   : level < 2.5 ? vec3(.26, .88, .97)
                   : level < 3.5 ? vec3(.49, .76, 1.0)
                   : vec3(.72, .84, 1.0);
    vec3 blue = mix(bodyColor, coreColor, innerCore * .34 + head * .40);

    // Only the incoming-facing portion of the actual armor receives a hit ring.
    // Direction/position are per draw, never a shared current-player global.
    if (HitAge >= 0.0 && HitAge <= 0.8) {
        vec3 hitNormal = normalize(HitNormal);
        vec3 delta = surfacePosition - HitPosition;
        float planeDistance = dot(delta, hitNormal);
        vec3 tangent = delta - planeDistance * hitNormal;
        float radial = length(tangent);
        float facing = pow(max(dot(normalize(surfaceNormal), hitNormal), 0.0), 4.0);
        float localMask = facing * cloudBell(planeDistance, 0.20)
                        * (1.0 - smoothstep(0.36, 0.64, length(delta)));
        float life = clamp(HitAge / 0.8, 0.0, 1.0);
        float fade = sin(life * 3.1415927) * (1.0 - life);
        float ringRadius = 0.035 + life * (0.29 + level * 0.065);
        float ring = smoothstep(0.12, 0.86, cloudBell(radial - ringRadius, 0.012 + life * 0.012));
        float echo = level > 1.5 ? cloudBell(radial - ringRadius + 0.07, 0.013) * (0.07 + level * 0.05) : 0.0;
        echo += level > 3.5 ? cloudBell(radial - ringRadius + 0.13, 0.010) * 0.12 : 0.0;
        float point = cloudBell(length(delta), 0.085) * (1.0 - life);
        float hit = (ring + echo + point * 0.5) * localMask * fade;
        alpha += hit * (0.57 + level * 0.16);
        blue = mix(blue, vec3(0.75, 0.92, 1.0), clamp(hit * 2.0, 0.0, 0.65));
    }

    alpha *= armorAlpha * clamp(Intensity, 0.0, 1.5);
    if (alpha < 0.006) discard;
    // Additive glint preserves the original material draw and diffuse color.
    fragColor = vec4(blue, min(alpha, 0.82)) * ColorModulator;
}
