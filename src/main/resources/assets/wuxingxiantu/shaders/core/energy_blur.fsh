#version 150

// Sampler0 is premultiplied emission RGB plus nearest contributing view depth
// in alpha; Sampler1 is the unchanged scene's nonlinear depth snapshot.
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InverseProjection;
uniform vec2 Direction;
uniform vec2 OutputSize;
in vec2 texCoord;
out vec4 fragColor;

float sceneViewDepth(vec2 uv) {
    float depth = texture(Sampler1, uv).r;
    vec4 view = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return max(-view.z / max(abs(view.w), 0.000001) / 128.0, 0.0);
}

vec4 sampleGlow(vec2 uv,float limit) {
    ivec2 size=textureSize(Sampler0,0);vec2 grid=uv*vec2(size)-.5;
    ivec2 base=ivec2(floor(grid));vec2 f=fract(grid);vec3 rgb=vec3(0);float nearest=1000000.0;
    for(int y=0;y<2;y++)for(int x=0;x<2;x++) {
        ivec2 p=clamp(base+ivec2(x,y),ivec2(0),size-ivec2(1));
        vec2 w=mix(vec2(1)-f,f,vec2(x,y));float weight=w.x*w.y;
        vec4 tap=texelFetch(Sampler0,p,0);if(tap.a>limit)continue;rgb+=tap.rgb*weight;
        if(weight>0.000001&&max(max(tap.r,tap.g),tap.b)>0.000001)nearest=min(nearest,tap.a);
    }
    return vec4(rgb,nearest);
}

void main() {
    const float weights[7] = float[7](0.161003374, 0.148624846, 0.116912445,
        0.078368755, 0.044764943, 0.021789437, 0.009037886);
    vec3 emission = vec3(0.0);
    float nearest = 1000000.0;
    float destinationDepth = sceneViewDepth(texCoord) + 0.001;
    vec2 delta = Direction / max(OutputSize, vec2(1.0));
    for (int tap = -6; tap <= 6; ++tap) {
        vec2 uv = clamp(texCoord + delta * float(tap), vec2(0.0), vec2(1.0));
        vec4 source = sampleGlow(uv,destinationDepth);
        if (max(max(source.r, source.g), source.b) > 0.000001
                && source.a <= destinationDepth) {
            emission += source.rgb * weights[abs(tap)];
            nearest = min(nearest, source.a);
        }
    }
    fragColor = vec4(emission, nearest);
}
