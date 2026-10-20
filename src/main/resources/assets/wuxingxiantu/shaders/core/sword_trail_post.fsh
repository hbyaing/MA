#version 150
uniform sampler2D Sampler0;
uniform vec2 OutputSize;
in vec4 vertexColor;
out vec4 fragColor;
void main(){
  float sceneDepth=texture(Sampler0,gl_FragCoord.xy/OutputSize).r;
  float trailDepth=gl_FragCoord.z;
  if(trailDepth>sceneDepth+0.0000001)discard;
  fragColor=vertexColor;
}
