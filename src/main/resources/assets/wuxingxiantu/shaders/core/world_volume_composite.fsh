#version 150
uniform sampler2D SceneSampler;
uniform sampler2D VolumeSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseProjection;
uniform vec2 VolumeSize;
in vec2 screenUV;
out vec4 fragColor;
vec3 decode(vec3 c){return mix(c/12.92,pow((c+.055)/1.055,vec3(2.4)),step(vec3(.04045),c));}
vec3 encode(vec3 c){c=max(c,vec3(0));return mix(c*12.92,1.055*pow(c,vec3(1.0/2.4))-.055,step(vec3(.0031308),c));}
float distanceAt(vec2 uv){
    float depth=texture(DepthSampler,uv).r;if(depth>=.999999)return 100000.0;
    vec4 p=InverseProjection*vec4(uv*2.0-1.0,depth*2.0-1.0,1.0);
    return length(p.xyz/p.w);
}
void main(){
    vec2 pixel=screenUV*VolumeSize-.5,base=floor(pixel),f=fract(pixel);
    float center=distanceAt(screenUV),sum=0.0,best=1e30;vec4 volume=vec4(0),nearest=vec4(0,0,0,1);
    for(int y=0;y<2;y++)for(int x=0;x<2;x++){
        vec2 uv=clamp((base+vec2(x,y)+.5)/VolumeSize,.5/VolumeSize,1.0-.5/VolumeSize);
        float delta=abs(distanceAt(uv)-center);
        vec4 sampleVolume=texture(VolumeSampler,uv);
        if(delta<best){best=delta;nearest=sampleVolume;}
        float weight=(x==0?1.0-f.x:f.x)*(y==0?1.0-f.y:f.y);
        weight*=exp(-delta/max(1.0,center*.025));
        volume+=sampleVolume*weight;sum+=weight;
    }
    volume=sum>1e-5?volume/sum:nearest;
    vec3 scene=decode(texture(SceneSampler,screenUV).rgb);
    fragColor=vec4(encode(scene*volume.a+volume.rgb),1.0);
}
