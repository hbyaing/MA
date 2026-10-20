// Native-only per-draw GPU cache. Web keeps the exact analytic path.
// Width=2049, domain=[-.004,1.004], float32 positions, unfiltered texel access.
#ifdef FC_CURVE_CACHE
uniform sampler2D CurveCenters;
vec3 fvCenter(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 // max(0,sin(pi*s)) has a derivative corner at each endpoint. Retain the
 // analytic result in that narrow band and outside the tabulated domain.
 if(s<.004||s>.996)return fvCenterAnalytic(s,strand,t,seed,radiusInput,heightInput,turnsInput,waveInput,strandCount);
 float coordinate=(s+.004)*(2048./1.008);
 int index=int(floor(coordinate)),row=int(strand);float f=coordinate-float(index);
 vec3 p0=texelFetch(CurveCenters,ivec2(index-1,row),0).xyz;
 vec3 p1=texelFetch(CurveCenters,ivec2(index,row),0).xyz;
 vec3 p2=texelFetch(CurveCenters,ivec2(index+1,row),0).xyz;
 vec3 p3=texelFetch(CurveCenters,ivec2(index+2,row),0).xyz;
 // Cubic Lagrange interpolation of four equally spaced analytic samples.
 // Difference form preserves the central sample and limits float cancellation.
 float w0=-f*(f-1.)*(f-2.)/6.;
 float w2=-(f+1.)*f*(f-2.)*.5;
 float w3=(f+1.)*f*(f-1.)/6.;
 return p1+(p0-p1)*w0+(p2-p1)*w2+(p3-p1)*w3;
}
#else
vec3 fvCenter(float s,float strand,float t,float seed,float radiusInput,float heightInput,float turnsInput,float waveInput,int strandCount){
 return fvCenterAnalytic(s,strand,t,seed,radiusInput,heightInput,turnsInput,waveInput,strandCount);
}
#endif
