#version 150
uniform sampler2D Volume,SceneDepth;
uniform vec2 Viewport;
uniform vec2 IntegrationSize;
uniform mat4 InverseViewProjection;
uniform sampler2D Glow;
uniform vec2 GlowSize;
uniform int CachedGlow;
out vec4 result;
float viewDepth(vec2 uv,float depth){
 vec4 p=InverseViewProjection*vec4(uv*2.-1.,depth*2.-1.,1.);
 return depth>=.999999||p.w<=0.?1.e8:1./p.w;
}
vec4 reconstruct(vec2 uv){
 if(all(equal(IntegrationSize,Viewport)))return texture(Volume,uv);
 float d=viewDepth(uv,texture(SceneDepth,uv).r);
 vec2 grid=uv*IntegrationSize-.5,base=floor(grid),fraction=fract(grid);
 vec4 sum=vec4(0);float total=0.;
 for(int y=0;y<2;y++)for(int x=0;x<2;x++){
  vec2 pixel=clamp(base+vec2(x,y),vec2(0),IntegrationSize-1.);
  vec2 tap=(pixel+.5)/IntegrationSize;
  float other=viewDepth(tap,texture(SceneDepth,tap).r);
  float agreement;
  if(d>=1.e7||other>=1.e7)agreement=d>=1.e7&&other>=1.e7?1.:0.;
  else agreement=1.-smoothstep(max(.025,d*.002),max(.10,d*.008),abs(other-d));
  vec2 weight=mix(1.-fraction,fraction,vec2(x,y));float w=weight.x*weight.y*agreement;
  sum+=texelFetch(Volume,ivec2(pixel),0)*w;total+=w;
 }
 // Premultiplied volume data; never blend a foreground edge with the sky.
 return total>.0001?sum/total:vec4(0);
}
vec3 cachedGlow(vec2 uv){
 float d0=texture(SceneDepth,uv).r;
 vec2 grid=uv*GlowSize-.5,base=floor(grid),fraction=fract(grid);
 vec3 sum=vec3(0);float total=0.;
 for(int y=0;y<2;y++)for(int x=0;x<2;x++){
  vec2 at=clamp(base+vec2(x,y),vec2(0),GlowSize-1.);
  vec4 tap=texelFetch(Glow,ivec2(at),0);
  vec2 bilinear=mix(1.-fraction,fraction,vec2(x,y));
  float weight=bilinear.x*bilinear.y*exp(-abs(tap.a-d0)/max(.000001,1.-d0)*25.);
  sum+=tap.rgb*weight;total+=weight;
 }
 return total>.0001?sum/total:vec3(0);
}
void main(){
 vec2 uv=gl_FragCoord.xy/Viewport, pixel=1.0/Viewport;
 vec4 flame=reconstruct(uv);vec3 glow=vec3(0);float total=0.;
 float d0=texture(SceneDepth,uv).r;
 // Native-sized source: retain the approved workbench's restrained local glow.
 // CPU scissors include the complete 9.9-pixel kernel and its source samples.
 if(CachedGlow==0){
 for(int ring=1;ring<=3;ring++)for(int ray=0;ray<12;ray++){
  float a=float(ray)*6.2831853/12.+float(ring)*.31;
  vec2 at=uv+vec2(cos(a),sin(a))*pixel*float(ring)*3.3;
  float d=texture(SceneDepth,at).r;
  float w=exp(-float(ring*ring)*.24)*exp(-abs(d-d0)/max(.000001,1.-d0)*25.);
  // The existing depth-weighted glow already rejects native depth edges;
  // reserve the four-tap reconstruction for the sharp flame itself.
  glow+=texture(Volume,at).rgb*w;total+=w;
 }
 glow/=max(total,.0001);
 }else glow=cachedGlow(uv);
 vec3 color=flame.rgb+glow*.16;
 float peak=max(color.r,max(color.g,color.b));
 result=vec4(color/(1.+peak*.24),flame.a*.78);
}
