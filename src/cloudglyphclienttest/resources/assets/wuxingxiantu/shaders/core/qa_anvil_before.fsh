#version 150
uniform sampler2D Sampler0;uniform sampler2D Sampler1;uniform mat4 InverseVP;uniform vec3 LightRelative0,LightRelative1,LightRelative2,LightRelative3;uniform int LightCount;uniform vec3 LightColor;uniform float Radius;uniform float Intensity;uniform vec2 Texel;in vec2 uv;out vec4 fragColor;
vec3 position(vec2 st,float depth){vec4 p=InverseVP*vec4(st*2.0-1.0,depth*2.0-1.0,1.0);return p.xyz/p.w;}
void main(){
 vec4 base=texture(Sampler0,uv);float d=texture(Sampler1,uv).r;vec3 p=position(uv,d);
 // Neighbor with smaller depth discontinuity on each axis; samples belong to the same view.
 float dl=texture(Sampler1,uv-vec2(Texel.x,0)).r,dr=texture(Sampler1,uv+vec2(Texel.x,0)).r;
 float db=texture(Sampler1,uv-vec2(0,Texel.y)).r,dt=texture(Sampler1,uv+vec2(0,Texel.y)).r;
 vec3 dx=abs(dl-d)<abs(dr-d)?p-position(uv-vec2(Texel.x,0),dl):position(uv+vec2(Texel.x,0),dr)-p;
 vec3 dy=abs(db-d)<abs(dt-d)?p-position(uv-vec2(0,Texel.y),db):position(uv+vec2(0,Texel.y),dt)-p;
 vec3 n=cross(dx,dy);float len=length(n);n/=max(len,1e-7);if(dot(n,-p)<0.0)n=-n;
 if(d>=.999999||len<1e-7){fragColor=base;return;}
 vec3 lights[4]=vec3[4](LightRelative0,LightRelative1,LightRelative2,LightRelative3);
 vec3 lit=base.rgb;
 for(int i=0;i<4;i++) {
  if(i>=LightCount)break;
  vec3 delta=lights[i]-p;
  // Each source excludes itself; other lights retain their normal contribution.
  // Classify just inside the visible surface. The ground top lies exactly on
  // the owner's lower boundary; a padded box incorrectly excluded that ground.
  // Moving inward along its own normal puts ground in the block below, while
  // anvil faces remain inside their owner. No extra light or intensity change.
  vec3 ownerDelta=lights[i]-(p-n*.004);
  if(!(abs(delta.x)<.505&&abs(delta.z)<.505&&abs(delta.y)<.54)) {
   float dist=length(delta);float fall=pow(max(1.0-pow(dist/Radius,2.0),0.0),2.0)/(1.0+dist*dist);
   float diffuse=max(dot(n,delta/max(dist,.001)),0.0);
   lit+=lit*LightColor*Intensity*fall*diffuse;
  }
  // The old per-light snapshot used RGBA8; preserve its intermediate rounding.
  if(i+1<LightCount)lit=floor(clamp(lit,0.0,1.0)*255.0+.5)/255.0;
 }
 fragColor=vec4(lit,base.a);
}
