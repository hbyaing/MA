package cn.blockforge.generated.generatedmod.client.firecoil;

/** Conservative support balls for the unchanged sampled main flame curves. */
final class SupportBounds {
 static final int SEGMENTS=32, TEXTURE_WIDTH=256, TEXTURE_HEIGHT=3;
 static final int POINT_BIN_BASE=256,POINT_META_BASE=512;
 static final int GRID_SIZE=32,GRID_WIDTH=GRID_SIZE*GRID_SIZE,GRID_HEIGHT=GRID_SIZE;
 final float[] data=new float[TEXTURE_WIDTH*TEXTURE_HEIGHT*4];
 final byte[] pointGrid=new byte[GRID_SIZE*GRID_SIZE*GRID_SIZE];
 final float[] pointGridMin=new float[3],pointGridInvVoxel=new float[3];
 boolean pointGridValid;
 private final double[] centerA=new double[3],centerB=new double[3];
 private final double[] localSpheres=new double[8*SEGMENTS*4];
 int count;
 void build(Recipe r,float time,boolean body,int weapons,float[] origins,float[] axes,float[] sides){
  count=0;
  pointGridValid=false;
  java.util.Arrays.fill(data,POINT_META_BASE*4,(POINT_META_BASE+8)*4,0f);
  if(!Float.isFinite(time)||Math.abs(time)>86400){count=-1;return;}
  // The production renderer draws one body or one weapon at a time. Multiple
  // fields keep the original full sphere table and leave point bins disabled.
  boolean pointBins=(body?1:0)+weapons==1;
  double seed=(float)((r.i("seed")%997)*.071f);
  if(body)coil(r,time,seed,Math.max(.2,(float)r.d("height")),(float)r.d("radius"),(float)r.d("width"),(float)r.d("turns"),(float)r.d("waveAmplitude"),-1,origins,axes,sides,pointBins);
  for(int j=0;j<weapons;j++){
   int at=j*4;
   coil(r,time,(float)((float)seed+j*2.13f),Math.max(.2,origins[at+3]),axes[at+3]+(float)r.d("weaponRadius"),(float)r.d("weaponWidth"),(float)r.d("weaponTurns"),(float)r.d("waveAmplitude")*.25f,j,origins,axes,sides,pointBins);
  }
  if(pointBins&&count>0&&count<=POINT_BIN_BASE)buildPointGrid(count);
 }
 private void coil(Recipe r,float time,double seed,double height,double radius,double width,double turns,double wave,int weapon,float[] origins,float[] axes,float[] sides,boolean pointBins){
  int strands=r.i("strands");double rotation=(float)r.d("rotationSpeed"),waveSpeed=(float)r.d("waveSpeed"),rise=(float)r.d("riseSpeed");
  double a1=14.70*Math.abs(turns)+2.902,a2=77.56*Math.abs(turns)+29.28;
  double ra=Math.abs(radius),wa=Math.abs(wave),r0=ra+1.231*wa,r1=.503*ra+14.14*wa,r2=1.58*ra+173.2*wa;
  double second=Math.hypot(r2+r0*a1*a1,2*r1*a1+r0*a2)+27.26*wa;
  double chordError=second/(8*SEGMENTS*SEGMENTS),warp=.077944*(float)r.d("turbulence"),ds=1.0/SEGMENTS;
  // CPU double support encloses the GPU float curve, including growing phase
  // roundoff. Very old effect clocks use the unculled shader fallback above.
  double roundoff=.001+Math.abs(time)*1.e-5*(Math.abs(rotation)*r0+Math.abs(waveSpeed)*wa+Math.abs(rise*width)+Math.abs(turns)*r0*.12);
  for(int j=0;j<strands;j++){
   double hierarchy=j==0?1:j==1?.58:.38;
   double[] previous=centerA,next=centerB;
   center(0,j,time,seed,radius,height,turns,wave,strands,rotation,waveSpeed,previous);
   for(int k=0;k<SEGMENTS;k++){
    double s0=k*ds,s1=(k+1)*ds,mid=(s0+s1)*.5;
    center(s1,j,time,seed,radius,height,turns,wave,strands,rotation,waveSpeed,next);
    double along=mid*(8+turns*2)-time*rise*1.25+j*11;
    double crest=.5+.5*Math.sin(along*.82+.55*Math.sin(along*1.63+j));
    double crestUpper=Math.min(1,crest+.85826*Math.abs(8+turns*2)*ds*.5);
    double peakS=Math.max(s0,Math.min(s1,.5)),ends=Math.pow(Math.max(0,Math.sin(peakS*Math.PI)),.65);
    double maxWidth=width*hierarchy*(.30+.42*Math.pow(Math.max(0,crestUpper),1.3))*(.42+.58*ends);
    double dx=next[0]-previous[0],dy=next[1]-previous[1],dz=next[2]-previous[2];
    double support=.5*Math.sqrt(dx*dx+dy*dy+dz*dz)+chordError+warp+2.5*maxWidth+roundoff;
    double x=(previous[0]+next[0])*.5,y=(previous[1]+next[1])*.5,z=(previous[2]+next[2])*.5;
    if(pointBins){int local=(j*SEGMENTS+k)*4;localSpheres[local]=x;localSpheres[local+1]=y;localSpheres[local+2]=z;localSpheres[local+3]=support;}
    if(weapon>=0){int at=weapon*4;double ox=sides[at+1]*axes[at+2]-sides[at+2]*axes[at+1],oy=sides[at+2]*axes[at]-sides[at]*axes[at+2],oz=sides[at]*axes[at+1]-sides[at+1]*axes[at];
     double ss=sides[at]*sides[at]+sides[at+1]*sides[at+1]+sides[at+2]*sides[at+2],aa=axes[at]*axes[at]+axes[at+1]*axes[at+1]+axes[at+2]*axes[at+2],sa=sides[at]*axes[at]+sides[at+1]*axes[at+1]+sides[at+2]*axes[at+2],det=Math.max(1.e-8,ss*aa-sa*sa);
     double stretch=1.00001/Math.sqrt(Math.max(1.e-8,Math.min(det,.5*(ss+aa-Math.hypot(ss-aa,2*sa))))),sx=aa*x-sa*y,ay=ss*y-sa*x;
     put(origins[at]+(sides[at]*sx+axes[at]*ay+ox*z)/det,origins[at+1]+(sides[at+1]*sx+axes[at+1]*ay+oy*z)/det,origins[at+2]+(sides[at+2]*sx+axes[at+2]*ay+oz*z)/det,support*stretch);
    }else put(x,y,z,support);
    double[] swap=previous;previous=next;next=swap;
   }
  }
 }
 /** One bit per strand; every cell intersecting a support sphere is occupied. */
 private void buildPointGrid(int spheres){
  java.util.Arrays.fill(pointGrid,(byte)0);
  double xMin=Double.POSITIVE_INFINITY,yMin=xMin,zMin=xMin,xMax=Double.NEGATIVE_INFINITY,yMax=xMax,zMax=xMax;
  for(int sphere=0;sphere<spheres;sphere++){
   int at=sphere*4;double r=localSpheres[at+3];
   xMin=Math.min(xMin,localSpheres[at]-r);xMax=Math.max(xMax,localSpheres[at]+r);
   yMin=Math.min(yMin,localSpheres[at+1]-r);yMax=Math.max(yMax,localSpheres[at+1]+r);
   zMin=Math.min(zMin,localSpheres[at+2]-r);zMax=Math.max(zMax,localSpheres[at+2]+r);
  }
  pointGridMin[0]=Math.nextDown((float)(xMin-.002));pointGridMin[1]=Math.nextDown((float)(yMin-.002));pointGridMin[2]=Math.nextDown((float)(zMin-.002));
  pointGridInvVoxel[0]=GRID_SIZE/(Math.nextUp((float)(xMax+.002))-pointGridMin[0]);
  pointGridInvVoxel[1]=GRID_SIZE/(Math.nextUp((float)(yMax+.002))-pointGridMin[1]);
  pointGridInvVoxel[2]=GRID_SIZE/(Math.nextUp((float)(zMax+.002))-pointGridMin[2]);
  // Derive cells from the exact float uniforms used by the shader. The half
  // diagonal bounds every point in a cell; .002 also covers bin-edge rounding.
  double dx=1.0/pointGridInvVoxel[0],dy=1.0/pointGridInvVoxel[1],dz=1.0/pointGridInvVoxel[2];
  double halfDiagonal=.5*Math.sqrt(dx*dx+dy*dy+dz*dz)+.002;
  for(int sphere=0;sphere<spheres;sphere++){
   int at=sphere*4,bit=1<<(sphere/SEGMENTS);double x=localSpheres[at],y=localSpheres[at+1],z=localSpheres[at+2],r=localSpheres[at+3];
   double expanded=r+halfDiagonal,squared=expanded*expanded;
   int loX=Math.max(0,(int)Math.floor((x-r-.002-pointGridMin[0])*pointGridInvVoxel[0]));
   int hiX=Math.min(GRID_SIZE-1,(int)Math.floor((x+r+.002-pointGridMin[0])*pointGridInvVoxel[0]));
   int loY=Math.max(0,(int)Math.floor((y-r-.002-pointGridMin[1])*pointGridInvVoxel[1]));
   int hiY=Math.min(GRID_SIZE-1,(int)Math.floor((y+r+.002-pointGridMin[1])*pointGridInvVoxel[1]));
   int loZ=Math.max(0,(int)Math.floor((z-r-.002-pointGridMin[2])*pointGridInvVoxel[2]));
   int hiZ=Math.min(GRID_SIZE-1,(int)Math.floor((z+r+.002-pointGridMin[2])*pointGridInvVoxel[2]));
   for(int iz=loZ;iz<=hiZ;iz++){
    double zc=pointGridMin[2]+(iz+.5)*dz-z,zSquared=zc*zc;
    for(int iy=loY;iy<=hiY;iy++){
     double yc=pointGridMin[1]+(iy+.5)*dy-y,yzSquared=zSquared+yc*yc;
     if(yzSquared>squared)continue; // Entire row lies outside this sphere.
     int row=(iz*GRID_SIZE+iy)*GRID_SIZE;
     for(int ix=loX;ix<=hiX;ix++){
      int index=row+ix;if((pointGrid[index]&bit)!=0)continue;
      double xc=pointGridMin[0]+(ix+.5)*dx-x;
      if(yzSquared+xc*xc<=squared)pointGrid[index]|=(byte)bit;
     }
    }
   }
  }
  pointGridValid=true;
 }
 /** Local, pre-warp sphere unions reduced to conservative xz bounds per y slab. */
 private void pointBins(int strand){
  double bottom=Double.POSITIVE_INFINITY,top=Double.NEGATIVE_INFINITY;
  for(int segment=0;segment<SEGMENTS;segment++){int at=segment*4;double y=localSpheres[at+1],r=localSpheres[at+3];bottom=Math.min(bottom,y-r);top=Math.max(top,y+r);}
  // Use the very float metadata the shader reads, then pad each slab for its
  // float multiply/floor at a bin boundary. Final xz values round outwards too.
  float yMin=Math.nextDown((float)(bottom-.001)),yMax=Math.nextUp((float)(top+.001));
  float inverse=SEGMENTS/(yMax-yMin);
  int meta=(POINT_META_BASE+strand)*4;data[meta]=yMin;data[meta+1]=inverse;data[meta+2]=yMax;data[meta+3]=1f;
  for(int bin=0;bin<SEGMENTS;bin++){
   double slabLo=yMin+bin/(double)inverse-.001,slabHi=yMin+(bin+1)/(double)inverse+.001;
   double xMin=Double.POSITIVE_INFINITY,xMax=Double.NEGATIVE_INFINITY,zMin=Double.POSITIVE_INFINITY,zMax=Double.NEGATIVE_INFINITY;
   for(int segment=0;segment<SEGMENTS;segment++){
    int at=segment*4;double y=localSpheres[at+1],r=localSpheres[at+3],dy=Math.max(0,Math.max(slabLo-y,y-slabHi));
    if(dy>r)continue;
    double reach=Math.sqrt(Math.max(0,r*r-dy*dy));
    xMin=Math.min(xMin,localSpheres[at]-reach);xMax=Math.max(xMax,localSpheres[at]+reach);
    zMin=Math.min(zMin,localSpheres[at+2]-reach);zMax=Math.max(zMax,localSpheres[at+2]+reach);
   }
   int at=(POINT_BIN_BASE+strand*SEGMENTS+bin)*4;
   if(xMin>xMax){data[at]=1f;data[at+1]=-1f;data[at+2]=1f;data[at+3]=-1f;}
   else{data[at]=Math.nextDown((float)(xMin-.001));data[at+1]=Math.nextUp((float)(xMax+.001));data[at+2]=Math.nextDown((float)(zMin-.001));data[at+3]=Math.nextUp((float)(zMax+.001));}
  }
 }
 private void put(double x,double y,double z,double radius){int at=count++*4;data[at]=(float)x;data[at+1]=(float)y;data[at+2]=(float)z;data[at+3]=(float)radius;}
 private static void center(double s,int strand,float t,double seed,double radius,double height,double turns,double wave,int count,double rotation,double waveSpeed,double[] output){
  double phase=(double)strand/Math.max(count,1)+.09*Math.sin(strand*2.4),w=s*8.7-t*waveSpeed*.8+seed*2.1+strand*2.3;
  double sweep=s+.12*Math.sin(s*6.283-t*.31+strand*1.7)+.045*Math.sin(s*13-t*.47+strand);
  double angle=6.28318530718*(sweep*turns+phase+t*rotation*.48)+.24*Math.sin(w*.71)+.10*Math.sin(w*1.63);
  double envelope=Math.max(0,Math.sin(s*3.14159265359));
  double rr=radius*(.84+.16*envelope)+wave*(Math.sin(w)*.85+Math.sin(w*1.63)*.38)*(.65+.35*envelope);
  output[0]=Math.cos(angle)*rr;output[1]=.055+s*height+wave*.25*Math.sin(w*1.2);output[2]=Math.sin(angle)*rr;
 }
}
