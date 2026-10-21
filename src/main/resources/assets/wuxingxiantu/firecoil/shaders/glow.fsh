#version 150
uniform sampler2D Volume,SceneDepth;
uniform vec2 Viewport,GlowSize;
out vec4 result;
void main(){
 vec2 uv=gl_FragCoord.xy/GlowSize,pixel=1./Viewport;
 float d0=texture(SceneDepth,uv).r,total=0.;vec3 glow=vec3(0);
 for(int ring=1;ring<=3;ring++)for(int ray=0;ray<12;ray++){
  float a=float(ray)*6.2831853/12.+float(ring)*.31;
  vec2 at=uv+vec2(cos(a),sin(a))*pixel*float(ring)*3.3;
  float d=texture(SceneDepth,at).r;
  float weight=exp(-float(ring*ring)*.24)*exp(-abs(d-d0)/max(.000001,1.-d0)*25.);
  glow+=texture(Volume,at).rgb*weight;total+=weight;
 }
 // Raw depth needs float32: float16 rounds distant terrain depth to sky.
 result=vec4(glow/max(total,.0001),d0);
}
