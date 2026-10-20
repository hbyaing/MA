#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InverseProjection;
uniform vec2 OutputSize;
uniform float Intensity;
in vec2 texCoord;
out vec4 fragColor;

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
    vec2 uv = clamp(gl_FragCoord.xy / max(OutputSize, vec2(1.0)), vec2(0.0), vec2(1.0));
    float sceneDepth = texture(Sampler1, uv).r;
    vec4 view = InverseProjection * vec4(uv * 2.0 - 1.0, sceneDepth * 2.0 - 1.0, 1.0);
    float destinationDepth = max(-view.z / max(abs(view.w), 0.000001) / 128.0, 0.0);
    vec4 bloom = sampleGlow(uv,destinationDepth+.001);
    if (max(max(bloom.r, bloom.g), bloom.b) < 0.000001) discard;
    // Only the selected meteor's radiance is added; scene alpha is untouched
    // by the caller's RGB-only color mask.
    fragColor = vec4(bloom.rgb * max(Intensity, 0.0), 0.0);
}
