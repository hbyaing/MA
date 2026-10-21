#version 150
uniform float Time,Radius,Height,Turns,Width,WaveAmplitude,RotationSpeed,RiseSpeed,WaveSpeed,TongueLength;
uniform float WeaponRadius,WeaponWidth,WeaponTurns,BladeLength,BladeRadius;
uniform int Strands,Seed,BodyEnabled;
out vec4 result;
const float FC_PI=3.14159265359;
const float FC_TAU=6.28318530718;
// Both functions are extracted from the production shader at load. This is
// an exact parameter bake, not curve interpolation or a CPU hash substitute.
__FC_HASH__
__FC_CENTER_ANALYTIC__

// Texels 0..47: sixteen peel records, root/life + axis/length + radial.
vec4 bakePeel(int index,int part){
 int j=index/2,i=index%2;
 if(j>=Strands)return vec4(0.);
 float t=Time,seed=float(Seed%997)*.071,strand=float(j),hierarchy=j==0?1.:(j==1?.58:.38);
 bool body=BodyEnabled!=0;
 float widthInput=body?Width:WeaponWidth,width=widthInput*hierarchy;
 float h=body?max(Height,.2):max(.2,BladeLength),radius=body?Radius:BladeRadius+WeaponRadius;
 float turns=body?Turns:WeaponTurns,wave=body?WaveAmplitude:WaveAmplitude*.25;
 float k=float(i),clock=t*(.37+RiseSpeed*.15)+k*.413+strand*.193,age=fract(clock),birth=floor(clock);
 float life=smoothstep(0.,.12,age)*(1.-smoothstep(.56,1.,age));
 if(life==0.)return vec4(0.);
 float rootS=.10+.68*fvHash(vec3(k+strand*7.,seed,birth));
 vec3 root=fvCenterAnalytic(rootS,strand,t,seed,radius,h,turns,wave,Strands);
 vec3 radial=normalize(vec3(root.x,0,root.z)),side=vec3(-radial.z,0,radial.x);
 float len=width*(1.25+TongueLength*.45)*(.5+.5*sin(age*FC_PI*.5));
 vec3 axis=normalize(vec3(0,1.,0)+radial*(.10+.12*age)+side*.14);
 if(part==0)return vec4(root,life);
 if(part==1)return vec4(axis,len);
 return vec4(radial,0.);
}

// Texels 48..65: six weapon tongues, root/length + axis/life + radial/random.
vec4 bakeTongue(int index,int part){
 if(BodyEnabled!=0)return vec4(0.);
 float t=Time,seed=float(Seed%997)*.071,k=float(index),clock=t*.43+k*.167,age=fract(clock),birth=floor(clock);
 float random=fvHash(vec3(k,seed,birth)),a=FC_TAU*random+t*RotationSpeed*.23;
 vec3 radial=vec3(cos(a),0,sin(a)),side=vec3(-sin(a),0,cos(a));
 vec3 root=radial*(BladeRadius+WeaponRadius*.25)+vec3(0,.04+BladeLength*(.10+k*.13+.05*random),0);
 vec3 axis=normalize(radial*.42+vec3(0,1.0,0)+side*.36);
 float tongueLength=WeaponWidth*(1.9+.7*random)*(.45+.55*age);
 if(part==0)return vec4(root,tongueLength);
 if(part==1)return vec4(axis,sin(age*FC_PI));
 return vec4(radial,random);
}

void main(){
 int texel=int(floor(gl_FragCoord.x));
 result=texel<48?bakePeel(texel/3,texel%3):bakeTongue((texel-48)/3,(texel-48)%3);
}
