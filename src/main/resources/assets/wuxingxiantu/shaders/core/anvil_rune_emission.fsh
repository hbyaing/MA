#version 150
in vec4 vertexColor;in vec2 uv;
uniform sampler2D Sampler0;uniform vec4 ColorModulator;out vec4 fragColor;
uniform sampler2D SceneDepth;uniform float SceneDepthEnabled;
void main(){
 vec4 tex=texture(Sampler0,uv);
 // Only the blue/cyan rune pixels emit; dark iron and bronze stay normally lit.
 // Shader-pack TAA offsets depth by a fraction of a pixel. On sloping faces,
 // a fixed epsilon rejects alternating coplanar rune fragments as it jitters.
 float depthTolerance=max(.000002,min(.00003,2.*fwidth(gl_FragCoord.z)));
 if(SceneDepthEnabled>.5&&gl_FragCoord.z>texture(SceneDepth,gl_FragCoord.xy/vec2(textureSize(SceneDepth,0))).r+depthTolerance)discard;
 float chroma=max(tex.g,tex.b)-tex.r;
 bool blue=tex.b>=.06&&chroma>=max(.035,tex.b*.40)&&tex.g>=tex.r*1.20;
 // Pale cyan/white-hot rune cores have low chroma, despite being the brightest
 // part of the rune. Keep those without selecting neutral iron or warm bronze.
 bool cyanCore=tex.b>=.70&&tex.g>=.60&&tex.g-tex.r>=.045&&tex.b>=tex.g-.01;
 if(tex.a<.1||(!blue&&!cyanCore))discard;
 vec3 emission=tex.rgb*mix(2.4,1.,smoothstep(.06,.65,tex.b));
 fragColor=vec4(emission,tex.a)*ColorModulator;
}
