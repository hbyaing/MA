package cn.blockforge.generated.generatedmod.client.firecoil;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import cn.blockforge.generated.generatedmod.client.firecoil.FireCore.V;
import cn.blockforge.generated.generatedmod.client.TrailShaderBridge;
import net.minecraft.client.*;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Real 3D local ray integration. Reads a separate depth-only copy, never scene color. */
final class VolumeGpu {
 static int program,vao,depthTexture,depthFbo,width,height,generation,draws,renderedWisps,lastGl;
 private static int supportTexture,supportGridTexture;
 private static boolean reportedUnpackState;
 private static final SupportBounds SUPPORT_BOUNDS=new SupportBounds();
 private static final java.nio.ByteBuffer GRID_UPLOAD=org.lwjgl.BufferUtils.createByteBuffer(SupportBounds.GRID_SIZE*SupportBounds.GRID_SIZE*SupportBounds.GRID_SIZE);
 static double coverage,cpuMs,supportCpuMs;
 static int lowFbo,lowTexture,lowWidth,lowHeight,composite;
 private record ColorTarget(int fbo,int texture,int width,int height){}
 // Resolution changes select an existing color target; world depth remains
 // native and is shared by every target. Allocate each used size only once.
 private static final Map<Long,ColorTarget> COLOR_TARGETS=new HashMap<>();
 static int colorTargetAllocations,depthTargetAllocations,integrationPixels;
 static boolean coverageBudgetEnabled=true;
 private static final int COVERAGE_PIXEL_BUDGET=90000;
 private static final double COVERAGE_SCALE_STEP=Math.sqrt(.5);
 static boolean cachedGlowEnabled=true;
 static int glowDivisor=4;
 private static int glowFbo,glowTexture,glowProgram,glowWidth,glowHeight;
 static int deferredComposites;
 static boolean directProbe,shaderPackDetected;
 private static int depthFormat,deferredFbo,deferredTexture,deferredWidth,deferredHeight,deferredProgram;
 private static boolean deferredDirty;
 private static Rect deferredArea;
 private static Matrix4f bodyTransform;
 static double renderScale=1.0;static boolean failed;
 static int stepBudget=512;
 static int integrationSteps(Recipe recipe){return Math.min(stepBudget,recipe.i("segments"));}
 private static final Map<String,Integer> UNIFORMS=new HashMap<>(),COMPOSITE_UNIFORMS=new HashMap<>();
 private static final String[] RECIPE_SCALARS={"Radius","Height","Turns","Width","RotationSpeed","RiseSpeed","WaveAmplitude","WaveSpeed","Turbulence","TongueLength","Erosion","Intensity","Opacity","WeaponTurns","WeaponStrength"};
 private static final String[] RECIPE_KEYS={"radius","height","turns","width","rotationSpeed","riseSpeed","waveAmplitude","waveSpeed","turbulence","tongueLength","erosion","intensity","opacity","weaponTurns","weaponStrength"};
 private static final String[] COLOR_UNIFORMS={"CoreColor","MidColor","EdgeColor"},COLOR_KEYS={"coreColor","midColor","edgeColor"};
 private static final float[] WEAPON_ORIGIN=new float[8],WEAPON_AXIS=new float[8],WEAPON_SIDE=new float[8],CLEAR_COLOR=new float[4];
 private static final FireCore.Particle[] SELECTED_WISPS=new FireCore.Particle[24];
 private static Recipe uploadedRecipe;
 private record BladeProfile(Recipe recipe,float length,float radius){}
 private static final Map<BladeProfile,Recipe> BLADE_PROFILES=new HashMap<>();
 private static final int GLOW_MARGIN=11;
 private static final Vector4f PROJECT_POINT=new Vector4f();
 private static final float[] PROJECT_X=new float[8],PROJECT_Y=new float[8],PROJECT_W=new float[8];
 // Package-private switch lets frozen QA compare against the former full-screen fallback.
 static boolean tightProjection=true;
 private static int compositeLoc(String name){return COMPOSITE_UNIFORMS.computeIfAbsent(name,k->GL20.glGetUniformLocation(composite,k));}
 private static void recipeUniforms(Recipe recipe){
  if(uploadedRecipe==recipe){if(recipe.dynamicSpectrum())for(int i=0;i<COLOR_KEYS.length;i++)GL20.glUniform3fv(loc(COLOR_UNIFORMS[i]),Client.color(recipe,COLOR_KEYS[i]));return;}
  for(int i=0;i<RECIPE_KEYS.length;i++)scalar(RECIPE_SCALARS[i],recipe.d(RECIPE_KEYS[i]));
  for(int i=0;i<COLOR_KEYS.length;i++)GL20.glUniform3fv(loc(COLOR_UNIFORMS[i]),Client.color(recipe,COLOR_KEYS[i]));
  GL20.glUniform1i(loc("Strands"),recipe.i("strands"));GL20.glUniform1i(loc("Seed"),recipe.i("seed"));
  uploadedRecipe=recipe;
 }
 private static final float[] MATRIX=new float[16],WISP_DATA=new float[24*4],WISP_VELOCITY=new float[24*4],WISP_META=new float[24*4];
 private static final String VERTEX="#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}";
 private static String resource(String name)throws Exception{try(var in=VolumeGpu.class.getResourceAsStream("/assets/wuxingxiantu/firecoil/shaders/"+name)){if(in==null)throw new IllegalStateException("Missing volume resource: "+name);return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
 private static int shader(int type,String source){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0){String message=GL20.glGetShaderInfoLog(s);GL20.glDeleteShader(s);throw new IllegalStateException(message);}return s;}
 private static void init()throws Exception{if(program!=0)return;int v=0,f=0,p=GL20.glCreateProgram();try{v=shader(GL20.GL_VERTEX_SHADER,VERTEX);f=shader(GL20.GL_FRAGMENT_SHADER,resource("volume.fsh"));GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));program=p;vao=GL30.glGenVertexArrays();generation++;failed=false;}catch(Exception e){GL20.glDeleteProgram(p);throw e;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}}
 private static int loc(String name){return UNIFORMS.computeIfAbsent(name,k->GL20.glGetUniformLocation(program,k));}
 private static void scalar(String name,double value){GL20.glUniform1f(loc(name),(float)value);}
 private static void vector(String name,V value){GL20.glUniform3f(loc(name),(float)value.x(),(float)value.y(),(float)value.z());}
 private static void supportUniforms(Recipe recipe,float time,boolean body,int weapons){
  long supportBegan=System.nanoTime();
  SUPPORT_BOUNDS.build(recipe,time,body,weapons,WEAPON_ORIGIN,WEAPON_AXIS,WEAPON_SIDE);
  supportCpuMs=(System.nanoTime()-supportBegan)/1e6;
  GlStateManager._activeTexture(GL13.GL_TEXTURE2);
  try(UnpackState unpack=new UnpackState()){
   if(supportTexture==0){supportTexture=GlStateManager._genTexture();GlStateManager._bindTexture(supportTexture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,SupportBounds.TEXTURE_WIDTH,SupportBounds.TEXTURE_HEIGHT,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
   }else GlStateManager._bindTexture(supportTexture);
   if(SUPPORT_BOUNDS.count>0)try(var stack=org.lwjgl.system.MemoryStack.stackPush()){
    var pixels=stack.mallocFloat(SUPPORT_BOUNDS.data.length);pixels.put(SUPPORT_BOUNDS.data).flip();
    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,SupportBounds.TEXTURE_WIDTH,SupportBounds.TEXTURE_HEIGHT,GL11.GL_RGBA,GL11.GL_FLOAT,pixels);
   }
   // Keep the integer sampler complete even when invalid clocks or wisp-only
   // draws select the shader fallback. It always uses a separate texture unit.
   GlStateManager._activeTexture(GL13.GL_TEXTURE3);
   if(supportGridTexture==0){
    supportGridTexture=GlStateManager._genTexture();GlStateManager._bindTexture(supportGridTexture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_R8UI,SupportBounds.GRID_WIDTH,SupportBounds.GRID_HEIGHT,0,GL30.GL_RED_INTEGER,GL11.GL_UNSIGNED_BYTE,(java.nio.ByteBuffer)null);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
   }else GlStateManager._bindTexture(supportGridTexture);
   if(SUPPORT_BOUNDS.pointGridValid){
    GRID_UPLOAD.clear();GRID_UPLOAD.put(SUPPORT_BOUNDS.pointGrid).flip();
    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,SupportBounds.GRID_WIDTH,SupportBounds.GRID_HEIGHT,GL30.GL_RED_INTEGER,GL11.GL_UNSIGNED_BYTE,GRID_UPLOAD);
   }
  }
  GL20.glUniform1i(loc("SupportSpheres"),2);GL20.glUniform1i(loc("SupportSphereCount"),SUPPORT_BOUNDS.count);
  GL20.glUniform1i(loc("PointGrid"),3);GL20.glUniform1i(loc("PointGridSize"),SUPPORT_BOUNDS.pointGridValid?SupportBounds.GRID_SIZE:0);
  GL20.glUniform3f(loc("PointGridMin"),SUPPORT_BOUNDS.pointGridMin[0],SUPPORT_BOUNDS.pointGridMin[1],SUPPORT_BOUNDS.pointGridMin[2]);
  GL20.glUniform3f(loc("PointGridInvVoxel"),SUPPORT_BOUNDS.pointGridInvVoxel[0],SUPPORT_BOUNDS.pointGridInvVoxel[1],SUPPORT_BOUNDS.pointGridInvVoxel[2]);
 }
 /** Vanilla packs view-bob translation into projection. Recover the perspective
  * eye from the complete inverse VP, rather than assuming projection is pure. */
 static V projectionEye(Matrix4f inverse,Matrix4f view){
  var h=inverse.transform(new Vector4f(0,0,-1,0));
  if(Float.isFinite(h.w)&&Math.abs(h.w)>1e-7f)return new V(h.x/h.w,h.y/h.w,h.z/h.w);
  var fallback=new Matrix4f(view).invert().transformPosition(new org.joml.Vector3f());return new V(fallback.x,fallback.y,fallback.z);
 }
 private static V local(V world,V anchor,double yaw){V d=world.sub(anchor);double c=Math.cos(yaw),s=Math.sin(yaw);return new V(c*d.x()+s*d.z(),d.y(),-s*d.x()+c*d.z());}
 private static V direction(V v,double yaw){return local(v,new V(0,0,0),yaw);}
 private static void targets(com.mojang.blaze3d.pipeline.RenderTarget main){targets(main,main.frameBufferId,Rect.full(main.width,main.height));}
 private static void targets(com.mojang.blaze3d.pipeline.RenderTarget main,int source){targets(main,source,Rect.full(main.width,main.height));}
 private static void targets(com.mojang.blaze3d.pipeline.RenderTarget main,int source,Rect area){
  int[] sourceViewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,sourceViewport);
  if(sourceViewport[0]!=0||sourceViewport[1]!=0||sourceViewport[2]!=main.width||sourceViewport[3]!=main.height)throw new IllegalStateException("Unsupported fire coil source viewport: "+Arrays.toString(sourceViewport));
  GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,source);
  int type=GL30.glGetFramebufferAttachmentParameteri(GL30.GL_READ_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
  int name=GL30.glGetFramebufferAttachmentParameteri(GL30.GL_READ_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME),internal;
  if(type==GL11.GL_TEXTURE){GlStateManager._bindTexture(name);internal=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_INTERNAL_FORMAT);}
  else if(type==GL30.GL_RENDERBUFFER){int old=GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER,name);internal=GL30.glGetRenderbufferParameteri(GL30.GL_RENDERBUFFER,GL30.GL_RENDERBUFFER_INTERNAL_FORMAT);GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER,old);}
  else throw new IllegalStateException("Fire coil source framebuffer has no depth: "+source);
  if(width!=main.width||height!=main.height||depthFbo==0||depthFormat!=internal){
  try(UnpackState unpack=new UnpackState()){freeTargets();width=main.width;height=main.height;depthFormat=internal;
  depthTexture=GlStateManager._genTexture();GlStateManager._bindTexture(depthTexture);
  boolean packed=internal==GL30.GL_DEPTH24_STENCIL8||internal==GL30.GL_DEPTH32F_STENCIL8;
  GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,internal,width,height,0,packed?GL30.GL_DEPTH_STENCIL:GL11.GL_DEPTH_COMPONENT,packed?(internal==GL30.GL_DEPTH32F_STENCIL8?GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV:GL30.GL_UNSIGNED_INT_24_8):GL11.GL_FLOAT,(java.nio.ByteBuffer)null);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL14.GL_TEXTURE_COMPARE_MODE,GL11.GL_NONE);
  depthFbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,depthFbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL11.GL_TEXTURE_2D,depthTexture,0);GL11.glDrawBuffer(GL11.GL_NONE);GL11.glReadBuffer(GL11.GL_NONE);if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete depth-copy framebuffer");depthTargetAllocations++;
  }}
  var mc=Minecraft.getInstance();
  boolean adaptive=coverageBudgetEnabled&&!mc.options.getCameraType().isFirstPerson()&&cn.blockforge.generated.generatedmod.HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get();
  long size=integrationSize(width,height,area,renderScale,adaptive);int targetWidth=(int)(size>>>32),targetHeight=(int)size;
  ColorTarget target=COLOR_TARGETS.get(size);
  if(target==null){
   int texture=0,fbo=0;
   try(UnpackState unpack=new UnpackState()){
    texture=GlStateManager._genTexture();GlStateManager._bindTexture(texture);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA16F,targetWidth,targetHeight,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
    fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);
    if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete fire-coil volume target");
    target=new ColorTarget(fbo,texture,targetWidth,targetHeight);COLOR_TARGETS.put(size,target);colorTargetAllocations++;
   }catch(RuntimeException ex){if(texture!=0)GlStateManager._deleteTexture(texture);if(fbo!=0)GL30.glDeleteFramebuffers(fbo);throw ex;}
  }
  lowFbo=target.fbo;lowTexture=target.texture;lowWidth=target.width;lowHeight=target.height;
  integrationPixels=integrationPixels(area.expand(4,width,height),width,height,lowWidth,lowHeight);
 }
 private static long integrationSize(int w,int h,Rect area,double baseScale,boolean adaptive){
  Rect integration=area.expand(4,w,h);double scale=baseScale;int level=0,tw=Math.max(1,(int)Math.ceil(w*scale)),th=Math.max(1,(int)Math.ceil(h*scale));
  if(adaptive)while(integrationPixels(integration,w,h,tw,th)>COVERAGE_PIXEL_BUDGET&&scale>1.0/64){
   ++level;scale=Math.max(1.0/64,baseScale*Math.scalb((level&1)==0?1:COVERAGE_SCALE_STEP,-level/2));tw=Math.max(1,(int)Math.ceil(w*scale));th=Math.max(1,(int)Math.ceil(h*scale));
  }
  return ((long)tw<<32)|(th&0xffffffffL);
 }
 private static int integrationPixels(Rect area,int w,int h,int tw,int th){
  int x0=(int)Math.floor((double)area.x0*tw/w),y0=(int)Math.floor((double)area.y0*th/h),x1=(int)Math.ceil((double)area.x1*tw/w),y1=(int)Math.ceil((double)area.y1*th/h);
  return (x1-x0)*(y1-y0);
 }
 /** Pure dimensions for coverage/rounding tests, independent of GL state. */
 static int[] integrationSizeForTest(int w,int h,int[] area,double scale,boolean adaptive){
  Rect rect=new Rect(area[0],area[1],area[2],area[3]);long size=integrationSize(w,h,rect,scale,adaptive);int tw=(int)(size>>>32),th=(int)size;
  return new int[]{tw,th,integrationPixels(rect.expand(4,w,h),w,h,tw,th)};
 }
 private record Bounds(V lo,V hi){}
 private record Rect(int x0,int y0,int x1,int y1){boolean empty(){return x1<=x0||y1<=y0;}void apply(){GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(x0,y0,x1-x0,y1-y0);}Rect expand(int n,int w,int h){return new Rect(Math.max(0,x0-n),Math.max(0,y0-n),Math.min(w,x1+n),Math.min(h,y1+n));}static Rect full(int w,int h){return new Rect(0,0,w,h);}}
 private static Rect project(Bounds bounds,Matrix4f viewProjection,V camera,int w,int h){
  int outside=15;boolean crossesNear=false,crossesEye=false,allBehind=true;
  float guardX=1f+2f*(GLOW_MARGIN+4)/w,guardY=1f+2f*(GLOW_MARGIN+4)/h;
  for(int i=0;i<8;i++){
   Vector4f p=PROJECT_POINT.set((float)(((i&1)==0?bounds.lo.x():bounds.hi.x())-camera.x()),(float)(((i&2)==0?bounds.lo.y():bounds.hi.y())-camera.y()),(float)(((i&4)==0?bounds.lo.z():bounds.hi.z())-camera.z()),1);
   viewProjection.transform(p);
   if(!Float.isFinite(p.x)||!Float.isFinite(p.y)||!Float.isFinite(p.z)||!Float.isFinite(p.w))return Rect.full(w,h);
   // Integrals start at the eye, including fire before the geometric near
   // plane. Positive-w corners can be projected directly; never z-clip them.
   int mask=(p.x < -p.w*guardX?1:0)|(p.x > p.w*guardX?2:0)|(p.y < -p.w*guardY?4:0)|(p.y > p.w*guardY?8:0);
   outside&=mask;allBehind&=p.w<=0;crossesEye|=p.w<=.0001f;crossesNear|=p.z<=-p.w;
   PROJECT_X[i]=p.x;PROJECT_Y[i]=p.y;PROJECT_W[i]=p.w;
  }
  if(outside!=0||allBehind)return new Rect(0,0,0,0);
  // Crossing the eye remains conservative. For positive w, a linear
  // fractional projection of a convex box reaches extrema at its corners.
  if(crossesEye||crossesNear&&!tightProjection)return Rect.full(w,h);
  double lx=Double.POSITIVE_INFINITY,ly=lx,hx=Double.NEGATIVE_INFINITY,hy=hx;
  for(int i=0;i<8;i++){double x=(double)PROJECT_X[i]/PROJECT_W[i],y=(double)PROJECT_Y[i]/PROJECT_W[i];lx=Math.min(lx,x);ly=Math.min(ly,y);hx=Math.max(hx,x);hy=Math.max(hy,y);}
  int x0=(int)Math.floor(Math.max(-8,Math.min(w+8,(lx+1)*w*.5)))-4,y0=(int)Math.floor(Math.max(-8,Math.min(h+8,(ly+1)*h*.5)))-4;
  int x1=(int)Math.ceil(Math.max(-8,Math.min(w+8,(hx+1)*w*.5)))+4,y1=(int)Math.ceil(Math.max(-8,Math.min(h+8,(hy+1)*h*.5)))+4;
  if(x1<=0||y1<=0||x0>=w||y0>=h)return new Rect(0,0,0,0);
  return new Rect(Math.max(0,x0),Math.max(0,y0),Math.min(w,x1),Math.min(h,y1));
 }
 /** Pure CPU entry point for projection coverage tests; does not touch GL. */
 static int[] projectForTest(float[] lo,float[] hi,Matrix4f vp,float[] camera,int width,int height){
  Rect r=project(new Bounds(new V(lo[0],lo[1],lo[2]),new V(hi[0],hi[1],hi[2])),vp,new V(camera[0],camera[1],camera[2]),width,height);
  return new int[]{r.x0,r.y0,r.x1,r.y1};
 }
 private static Bounds wisps(Recipe recipe,FireCore core,V anchor,double yaw,double partial,double strength){
  // Same support as the approved workbench. The additional detached flames
  // have an exact spherical cutoff sqrt(11.5)*size in the shared density core.
  boolean body=recipe.i("attachment")!=1&&core.ramp()*strength>0;
  double peel=recipe.d("width")*(2.6+recipe.d("tongueLength"))+.1*recipe.d("turbulence");
  double extent=recipe.d("radius")+1.3*recipe.d("waveAmplitude")+peel;
  V lo=body?new V(-extent,-peel,-extent):new V(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
  V hi=body?new V(extent,recipe.d("height")+peel,extent):new V(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
  // Stable age ordering, without building and sorting an ArrayList every draw.
  renderedWisps=0;Arrays.fill(SELECTED_WISPS,null);
  if(strength>0)for(var p:core.pool)if(p.alive&&p.kind==1){
   int index=renderedWisps;while(index>0&&p.age<SELECTED_WISPS[index-1].age)index--;
   if(index>=24)continue;
   for(int j=Math.min(renderedWisps,23);j>index;j--)SELECTED_WISPS[j]=SELECTED_WISPS[j-1];
   SELECTED_WISPS[index]=p;renderedWisps=Math.min(24,renderedWisps+1);
  }
  for(int i=0;i<renderedWisps;i++){
   var p=SELECTED_WISPS[i];V at=local(p.previous.lerp(p.position,partial),anchor,yaw),velocity=direction(p.velocity,yaw);
   double age=Math.max(0,Math.min(1,(p.age-.05+partial*.05)/p.life));int off=i*4;
   WISP_DATA[off]=(float)at.x();WISP_DATA[off+1]=(float)at.y();WISP_DATA[off+2]=(float)at.z();WISP_DATA[off+3]=(float)p.size;
   WISP_VELOCITY[off]=(float)velocity.x();WISP_VELOCITY[off+1]=(float)velocity.y();WISP_VELOCITY[off+2]=(float)velocity.z();WISP_VELOCITY[off+3]=(float)p.seed;
   WISP_META[off]=(float)age;WISP_META[off+1]=(float)strength;WISP_META[off+2]=WISP_META[off+3]=0;
   double support=p.size*3.4+.0001;
   lo=new V(Math.min(lo.x(),at.x()-support),Math.min(lo.y(),at.y()-support),Math.min(lo.z(),at.z()-support));
   hi=new V(Math.max(hi.x(),at.x()+support),Math.max(hi.y(),at.y()+support),Math.max(hi.z(),at.z()+support));
  }
  return body||renderedWisps>0?new Bounds(lo,hi):null;
 }
 private static void copyDepth(int source,Rect area){
  // Glow samples at most 9.9 pixels away. Copy/clear an extra kernel beyond
  // the composite rectangle, including a coarse reconstruction texel, so
  // cached target pixels outside a previous object's rectangle cannot leak.
  Rect copy=area.expand(GLOW_MARGIN*2+8+integrationTexelMargin(),width,height);copy.apply();
  GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,source);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,depthFbo);
  GL30.glBlitFramebuffer(copy.x0,copy.y0,copy.x1,copy.y1,copy.x0,copy.y0,copy.x1,copy.y1,GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
 }
 static void renderAttached(RenderLevelStageEvent e,Recipe recipe,FireCore core,double partial,double strength,Matrix4f transform)throws Exception{
  bodyTransform=transform;try{render(e,recipe,core,new V(0,0,0),0,partial,strength);}finally{bodyTransform=null;}
 }
 static void render(RenderLevelStageEvent e,Recipe recipe,FireCore core,V anchor,double yaw,double partial,double strength)throws Exception{
  draws=renderedWisps=integrationPixels=0;coverage=cpuMs=supportCpuMs=0;
  if(strength<=0||recipe.d("opacity")<=0||core.ramp()<=0&&core.count()==0)return;
  long began=System.nanoTime();
  try{
   var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();if(mc.options.graphicsMode().get()==GraphicsStatus.FABULOUS)return;
   Bounds bounds=wisps(recipe,core,anchor,yaw,partial,strength);if(bounds==null)return;
   var cp=e.getCamera().getPosition();V worldCamera=new V(cp.x,cp.y,cp.z),camera=local(worldCamera,anchor,yaw);
   Matrix4f view=bodyTransform==null?new Matrix4f(e.getPoseStack().last().pose()):new Matrix4f(RenderSystem.getModelViewMatrix()).mul(bodyTransform),projection=new Matrix4f(e.getProjectionMatrix()),vp=new Matrix4f(projection).mul(view);Matrix4f inverse=new Matrix4f(vp).invert();inverse.get(MATRIX);
   if(bodyTransform!=null)camera=projectionEye(inverse,view);
   // Project body-local bounds using the yaw transform; world positions stay double until relative to camera.
   Matrix4f bodyViewProjection=new Matrix4f(vp).rotateY((float)-yaw);Rect area=project(bounds,bodyViewProjection,bodyTransform==null?camera:new V(0,0,0),main.width,main.height);
   if(area.empty()||TrailShaderBridge.shadowPass()){renderedWisps=0;return;}
   int source=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);if(source!=main.frameBufferId&&!postEnabled()){renderedWisps=0;return;}
   coverage=(double)(area.x1-area.x0)*(area.y1-area.y0)/(main.width*(double)main.height);
   try(State state=new State()){
   init();GlStateManager._activeTexture(GL13.GL_TEXTURE0);targets(main,source,area);
   copyDepth(source,area);
   GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main.frameBufferId);RenderSystem.viewport(0,0,width,height);RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);RenderSystem.colorMask(true,true,true,true);GL11.glDisable(GL11.GL_STENCIL_TEST);GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);area.apply();GlStateManager._glUseProgram(program);GL30.glBindVertexArray(vao);
   beginVolume(area);GL20.glUniform2f(loc("Viewport"),lowWidth,lowHeight);GL20.glUniformMatrix4fv(loc("InverseViewProjection"),false,MATRIX);vector("Camera",camera);vector("BoundsMin",bounds.lo);vector("BoundsMax",bounds.hi);scalar("Yaw",yaw);scalar("ModelScale",1);scalar("Time",core.time()+partial*.05);scalar("VolumeStrength",core.ramp()*strength);GL20.glUniform1i(loc("Steps"),integrationSteps(recipe));
   recipeUniforms(recipe);
   GL20.glUniform1i(loc("BodyEnabled"),recipe.i("attachment")==1||core.ramp()*strength<=0?0:1);GL20.glUniform1i(loc("WeaponCount"),0);GL20.glUniform1i(loc("ItemSpace"),bodyTransform==null?0:1);
   supportUniforms(recipe,(float)(core.time()+partial*.05),recipe.i("attachment")!=1&&core.ramp()*strength>0,0);
   GL20.glUniform1i(loc("WispCount"),renderedWisps);GL20.glUniform4fv(loc("Wisps[0]"),WISP_DATA);GL20.glUniform4fv(loc("WispVelocity[0]"),WISP_VELOCITY);GL20.glUniform4fv(loc("WispMeta[0]"),WISP_META);GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(depthTexture);GL20.glUniform1i(loc("SceneDepth"),0);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);finishVolume(main,area);draws=1;lastGl=GL11.glGetError();if(lastGl!=0)throw new IllegalStateException("Volume GL error "+lastGl);
   }
  }catch(Exception ex){failed=true;throw ex;}finally{cpuMs=(System.nanoTime()-began)/1e6;}
 }
 private static void freeTargets(){for(ColorTarget target:COLOR_TARGETS.values()){GlStateManager._deleteTexture(target.texture);GL30.glDeleteFramebuffers(target.fbo);}COLOR_TARGETS.clear();lowTexture=lowFbo=lowWidth=lowHeight=0;if(depthTexture!=0)GlStateManager._deleteTexture(depthTexture);if(depthFbo!=0)GL30.glDeleteFramebuffers(depthFbo);depthTexture=depthFbo=width=height=0;}
 static boolean renderWeapon(Recipe recipe,Matrix4f itemPose,float[] base,float[] axis,float[] side,float length,float radius)throws Exception{
  draws=renderedWisps=integrationPixels=0;coverage=cpuMs=supportCpuMs=0;
  if(recipe.d("weaponStrength")<=0||recipe.d("opacity")<=0)return false;
  long began=System.nanoTime();
  try{
   var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();if(mc.options.graphicsMode().get()==GraphicsStatus.FABULOUS)return false;
   Matrix4f view=new Matrix4f(RenderSystem.getModelViewMatrix()).mul(itemPose),vp=new Matrix4f(RenderSystem.getProjectionMatrix()).mul(view),inverse=new Matrix4f(vp).invert();inverse.get(MATRIX);
   var eye=projectionEye(inverse,view);
   float scale=Math.max(.001f,view.transformDirection(new org.joml.Vector3f(side[0],side[1],side[2])).length());
   var profile=new BladeProfile(recipe,Math.round(length*scale*1000)/1000f,Math.round(radius*scale*1000)/1000f);
   if(BLADE_PROFILES.size()>64)BLADE_PROFILES.clear();
   Recipe sourceRecipe=recipe;
   recipe=BLADE_PROFILES.computeIfAbsent(profile,k->sourceRecipe.forBlade(k.length(),k.radius()));
   float wr=(float)recipe.d("weaponRadius"),ww=(float)recipe.d("weaponWidth");
   float margin=radius+(float)((wr+ww*(4.2+recipe.d("tongueLength"))+.34*recipe.d("waveAmplitude")+.1*recipe.d("turbulence")+.2)/scale);
   V lo=new V(Math.min(base[0],base[0]+axis[0]*length)-margin,Math.min(base[1],base[1]+axis[1]*length)-margin,Math.min(base[2],base[2]+axis[2]*length)-margin),hi=new V(Math.max(base[0],base[0]+axis[0]*length)+margin,Math.max(base[1],base[1]+axis[1]*length)+margin,Math.max(base[2],base[2]+axis[2]*length)+margin);
   Rect area=project(new Bounds(lo,hi),vp,new V(0,0,0),main.width,main.height);if(area.empty()||TrailShaderBridge.shadowPass())return false;
   int source=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);if(source!=main.frameBufferId&&!postEnabled())return false;
   coverage=(double)(area.x1-area.x0)*(area.y1-area.y0)/(main.width*(double)main.height);
   try(State state=new State()){
   init();GlStateManager._activeTexture(GL13.GL_TEXTURE0);targets(main,source,area);
   copyDepth(source,area);
   GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main.frameBufferId);RenderSystem.viewport(0,0,width,height);RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);RenderSystem.colorMask(true,true,true,true);GL11.glDisable(GL11.GL_STENCIL_TEST);GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);GlStateManager._glUseProgram(program);GL30.glBindVertexArray(vao);
   beginVolume(area);GL20.glUniform2f(loc("Viewport"),lowWidth,lowHeight);GL20.glUniformMatrix4fv(loc("InverseViewProjection"),false,MATRIX);vector("Camera",eye);vector("BoundsMin",lo);vector("BoundsMax",hi);scalar("Yaw",0);scalar("ModelScale",scale);scalar("Time",Client.time());scalar("VolumeStrength",1);GL20.glUniform1i(loc("Steps"),integrationSteps(recipe));GL20.glUniform1i(loc("ItemSpace"),1);
   GL20.glUniform1i(loc("BodyEnabled"),0);GL20.glUniform1i(loc("WeaponCount"),1);GL20.glUniform1i(loc("WispCount"),0);
   for(int i=0;i<3;i++){WEAPON_ORIGIN[i]=base[i]*scale;WEAPON_AXIS[i]=axis[i];WEAPON_SIDE[i]=side[i];}
   WEAPON_ORIGIN[3]=length*scale;WEAPON_AXIS[3]=radius*scale;
   GL20.glUniform4fv(loc("WeaponOrigins[0]"),WEAPON_ORIGIN);GL20.glUniform4fv(loc("WeaponAxes[0]"),WEAPON_AXIS);GL20.glUniform4fv(loc("WeaponSides[0]"),WEAPON_SIDE);
   recipeUniforms(recipe);scalar("WeaponRadius",wr);scalar("WeaponWidth",ww);
   supportUniforms(recipe,(float)Client.time(),false,1);
   GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(depthTexture);GL20.glUniform1i(loc("SceneDepth"),0);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);finishVolume(main,area);draws=1;lastGl=GL11.glGetError();if(lastGl!=0)throw new IllegalStateException("Weapon volume GL "+lastGl);
   }
  return true;
  }finally{cpuMs=(System.nanoTime()-began)/1e6;}
 }
 static void release(){BLADE_PROFILES.clear();releaseGlow();freeTargets();freeDeferred();CurveCache.release();if(supportTexture!=0)GlStateManager._deleteTexture(supportTexture);if(supportGridTexture!=0)GlStateManager._deleteTexture(supportGridTexture);supportTexture=supportGridTexture=0;if(program!=0)GL20.glDeleteProgram(program);if(vao!=0)GL30.glDeleteVertexArrays(vao);if(composite!=0)GL20.glDeleteProgram(composite);if(deferredProgram!=0)GL20.glDeleteProgram(deferredProgram);deferredProgram=0;composite=0;program=vao=0;UNIFORMS.clear();COMPOSITE_UNIFORMS.clear();uploadedRecipe=null;Arrays.fill(SELECTED_WISPS,null);failed=false;}
 private static boolean postEnabled(){return !directProbe&&shaderPackDetected;}
 static boolean hasDeferred(){return deferredDirty;}
 static void skipFrame(){draws=renderedWisps=deferredComposites=integrationPixels=0;coverage=cpuMs=supportCpuMs=0;deferredDirty=false;deferredArea=null;shaderPackDetected=false;}
 static void beginFrame(){deferredDirty=false;deferredArea=null;deferredComposites=0;shaderPackDetected=TrailShaderBridge.shaderPackDetected();
  var mc=Minecraft.getInstance();var target=mc.getMainRenderTarget();
  boolean fast=mc.options.getCameraType().isFirstPerson()&&cn.blockforge.generated.generatedmod.HudConfig.EXOTIC_FIRE_FIRST_PERSON_OPTIMIZATION.get();
  stepBudget=fast?192:512;
  renderScale=fast?Math.min(.30,Math.sqrt(90000.0/Math.max(1.0,(double)target.width*target.height))):1.0;
 }
 private static void freeDeferred(){if(deferredTexture!=0)GlStateManager._deleteTexture(deferredTexture);if(deferredFbo!=0)GL30.glDeleteFramebuffers(deferredFbo);deferredTexture=deferredFbo=deferredWidth=deferredHeight=0;deferredDirty=false;deferredArea=null;}
 /** Integrate against the real world/hand depth now; present after shaderpack final. */
 private static void prepareDeferred(){
  if(deferredWidth!=width||deferredHeight!=height||deferredFbo==0){
   freeDeferred();deferredWidth=width;deferredHeight=height;
   try(UnpackState unpack=new UnpackState()){
    GlStateManager._activeTexture(GL13.GL_TEXTURE0);deferredTexture=GlStateManager._genTexture();GlStateManager._bindTexture(deferredTexture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA16F,width,height,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
   }
   deferredFbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,deferredFbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,deferredTexture,0);
   if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete deferred fire coil target");
  }else GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,deferredFbo);
  if(!deferredDirty){GL11.glDisable(GL11.GL_SCISSOR_TEST);RenderSystem.colorMask(true,true,true,true);GL30.glClearBufferfv(GL11.GL_COLOR,0,CLEAR_COLOR);}
 }
 static void flushDeferred()throws Exception{
  if(!deferredDirty)return;
  try(State state=new State()){
   var main=Minecraft.getInstance().getMainRenderTarget();if(main.width!=deferredWidth||main.height!=deferredHeight)return;
   if(deferredProgram==0){int v=0,f=0,p=GL20.glCreateProgram();try{
    v=shader(GL20.GL_VERTEX_SHADER,VERTEX);f=shader(GL20.GL_FRAGMENT_SHADER,"#version 150\nuniform sampler2D Overlay;out vec4 result;void main(){result=texelFetch(Overlay,ivec2(gl_FragCoord.xy),0);}");
    GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));deferredProgram=p;
   }catch(Exception ex){GL20.glDeleteProgram(p);throw ex;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}}
   GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main.frameBufferId);RenderSystem.viewport(0,0,main.width,main.height);deferredArea.apply();
   RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();GL11.glDisable(GL11.GL_STENCIL_TEST);GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);RenderSystem.colorMask(true,true,true,true);
   RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);
   GlStateManager._glUseProgram(deferredProgram);GL30.glBindVertexArray(vao);GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(deferredTexture);GL20.glUniform1i(GL20.glGetUniformLocation(deferredProgram,"Overlay"),0);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
   int error=GL11.glGetError();if(error!=0)throw new IllegalStateException("Deferred fire coil GL "+error);deferredComposites++;
  }finally{deferredDirty=false;deferredArea=null;}
 }
 private static void beginVolume(Rect area){
  GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,lowFbo);RenderSystem.viewport(0,0,lowWidth,lowHeight);
  scaled(area.expand(GLOW_MARGIN*2+4+integrationTexelMargin(),width,height)).apply();GL30.glClearBufferfv(GL11.GL_COLOR,0,CLEAR_COLOR);
  RenderSystem.disableBlend();scaled(area.expand(4,width,height)).apply();
 }
 private static int integrationTexelMargin(){return (int)Math.ceil(Math.max((double)width/lowWidth,(double)height/lowHeight));}
 private static Rect scaled(Rect area){return new Rect(Math.max(0,(int)Math.floor((double)area.x0*lowWidth/width)),Math.max(0,(int)Math.floor((double)area.y0*lowHeight/height)),Math.min(lowWidth,(int)Math.ceil((double)area.x1*lowWidth/width)),Math.min(lowHeight,(int)Math.ceil((double)area.y1*lowHeight/height)));}
 private static void prepareGlow(Rect area)throws Exception{
  if(glowWidth!=(width+glowDivisor-1)/glowDivisor||glowHeight!=(height+glowDivisor-1)/glowDivisor||glowFbo==0){
   freeGlow();glowWidth=(width+glowDivisor-1)/glowDivisor;glowHeight=(height+glowDivisor-1)/glowDivisor;
   try(UnpackState unpack=new UnpackState()){
    GlStateManager._activeTexture(GL13.GL_TEXTURE2);glowTexture=GlStateManager._genTexture();GlStateManager._bindTexture(glowTexture);
    GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,glowWidth,glowHeight,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
    glowFbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,glowFbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,glowTexture,0);
    if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete fire glow target");
   }
  }
  if(glowProgram==0){int v=0,f=0,p=GL20.glCreateProgram();try{
   v=shader(GL20.GL_VERTEX_SHADER,VERTEX);f=shader(GL20.GL_FRAGMENT_SHADER,resource("glow.fsh"));GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);
   if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));glowProgram=p;
  }catch(Exception ex){GL20.glDeleteProgram(p);throw ex;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}}
  GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,glowFbo);RenderSystem.viewport(0,0,glowWidth,glowHeight);
  Rect output=area.expand(GLOW_MARGIN+4,width,height);
  new Rect((int)Math.floor((double)output.x0*glowWidth/width),(int)Math.floor((double)output.y0*glowHeight/height),Math.min(glowWidth,(int)Math.ceil((double)output.x1*glowWidth/width)),Math.min(glowHeight,(int)Math.ceil((double)output.y1*glowHeight/height))).apply();
  RenderSystem.disableBlend();GlStateManager._glUseProgram(glowProgram);
  GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(lowTexture);GL20.glUniform1i(GL20.glGetUniformLocation(glowProgram,"Volume"),0);
  GlStateManager._activeTexture(GL13.GL_TEXTURE1);GlStateManager._bindTexture(depthTexture);GL20.glUniform1i(GL20.glGetUniformLocation(glowProgram,"SceneDepth"),1);
  GL20.glUniform2f(GL20.glGetUniformLocation(glowProgram,"Viewport"),width,height);GL20.glUniform2f(GL20.glGetUniformLocation(glowProgram,"GlowSize"),glowWidth,glowHeight);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
 }
 private static void freeGlow(){if(glowTexture!=0)GlStateManager._deleteTexture(glowTexture);if(glowFbo!=0)GL30.glDeleteFramebuffers(glowFbo);glowTexture=glowFbo=glowWidth=glowHeight=0;}
 private static void releaseGlow(){freeGlow();if(glowProgram!=0)GL20.glDeleteProgram(glowProgram);glowProgram=0;}
 private static void finishVolume(com.mojang.blaze3d.pipeline.RenderTarget main,Rect area)throws Exception{
  if(composite==0){int v=0,f=0,p=GL20.glCreateProgram();try{
   v=shader(GL20.GL_VERTEX_SHADER,VERTEX);f=shader(GL20.GL_FRAGMENT_SHADER,resource("composite.fsh"));
   GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);
   if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));composite=p;
  }catch(Exception ex){GL20.glDeleteProgram(p);throw ex;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}}
  // Small projected flames cost less than the extra draw/target transition.
  boolean cached=cachedGlowEnabled&&lowWidth<width&&(long)(area.x1-area.x0)*(area.y1-area.y0)>=750000;
  if(cached)prepareGlow(area);
  boolean deferred=postEnabled();if(deferred)prepareDeferred();else GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main.frameBufferId);
  RenderSystem.viewport(0,0,width,height);Rect output=area.expand(GLOW_MARGIN,width,height);output.apply();
  RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);
  GlStateManager._glUseProgram(composite);GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(lowTexture);GL20.glUniform1i(compositeLoc("Volume"),0);
  GlStateManager._activeTexture(GL13.GL_TEXTURE1);GlStateManager._bindTexture(depthTexture);GL20.glUniform1i(compositeLoc("SceneDepth"),1);GL20.glUniform2f(compositeLoc("Viewport"),width,height);
  GL20.glUniform1i(compositeLoc("CachedGlow"),cached?1:0);
  GlStateManager._activeTexture(GL13.GL_TEXTURE2);GlStateManager._bindTexture(cached?glowTexture:lowTexture);GL20.glUniform1i(compositeLoc("Glow"),2);GL20.glUniform2f(compositeLoc("GlowSize"),glowWidth,glowHeight);
  GL20.glUniform2f(compositeLoc("IntegrationSize"),lowWidth,lowHeight);GL20.glUniformMatrix4fv(compositeLoc("InverseViewProjection"),false,MATRIX);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
  if(deferred){deferredDirty=true;deferredArea=deferredArea==null?output:new Rect(Math.min(deferredArea.x0,output.x0),Math.min(deferredArea.y0,output.y0),Math.max(deferredArea.x1,output.x1),Math.max(deferredArea.y1,output.y1));}
 }
 /** NativeImage and other renderers may leave row/skip/PBO unpack state active. */
 static final class UnpackState implements AutoCloseable {
  private static final int[] KEYS={GL11.GL_UNPACK_ALIGNMENT,GL11.GL_UNPACK_ROW_LENGTH,GL11.GL_UNPACK_SKIP_PIXELS,GL11.GL_UNPACK_SKIP_ROWS,GL12.GL_UNPACK_IMAGE_HEIGHT,GL12.GL_UNPACK_SKIP_IMAGES,GL11.GL_UNPACK_SWAP_BYTES,GL11.GL_UNPACK_LSB_FIRST};
  private final int[] values=new int[KEYS.length];
  private final int buffer=GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
  UnpackState(){
   boolean unusual=buffer!=0;
   for(int i=0;i<KEYS.length;i++){values[i]=GL11.glGetInteger(KEYS[i]);unusual|=values[i]!=(i==0?4:0);}
   if(unusual&&!reportedUnpackState){reportedUnpackState=true;com.mojang.logging.LogUtils.getLogger().warn("Firecoil isolated ambient pixel unpack state: PBO={}, alignment/rowLength/skipPixels/skipRows/imageHeight/skipImages/swapBytes/lsbFirst={}",buffer,Arrays.toString(values));}
   GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,0);
   for(int i=0;i<KEYS.length;i++)GL11.glPixelStorei(KEYS[i],i==0?4:0);
  }
  public void close(){for(int i=0;i<KEYS.length;i++)GL11.glPixelStorei(KEYS[i],values[i]);GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,buffer);}
 }
 private static final class State implements AutoCloseable {
  final int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),vertexArray=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
  final int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
  final int eqRgb=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB),eqAlpha=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
  final int[] viewport=new int[4],box=new int[4];final boolean[] mask=new boolean[4];final int texture,texture1,texture2,texture3;
  final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),write=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),stencil=GL11.glIsEnabled(GL11.GL_STENCIL_TEST),srgb=GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB);
  State(){GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,box);try(var stack=org.lwjgl.system.MemoryStack.stackPush()){java.nio.ByteBuffer b=stack.malloc(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,b);for(int i=0;i<4;i++)mask[i]=b.get(i)!=0;}GlStateManager._activeTexture(GL13.GL_TEXTURE0);texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GlStateManager._activeTexture(GL13.GL_TEXTURE1);texture1=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GlStateManager._activeTexture(GL13.GL_TEXTURE2);texture2=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GlStateManager._activeTexture(GL13.GL_TEXTURE3);texture3=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GlStateManager._activeTexture(active);}
  private static void flag(int key,boolean value){if(value)GL11.glEnable(key);else GL11.glDisable(key);}
  public void close(){GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);GlStateManager._glUseProgram(program);GL30.glBindVertexArray(vertexArray);GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(texture);GlStateManager._activeTexture(GL13.GL_TEXTURE1);GlStateManager._bindTexture(texture1);GlStateManager._activeTexture(GL13.GL_TEXTURE2);GlStateManager._bindTexture(texture2);GlStateManager._activeTexture(GL13.GL_TEXTURE3);GlStateManager._bindTexture(texture3);GlStateManager._activeTexture(active);if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();RenderSystem.depthMask(write);if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);GL20.glBlendEquationSeparate(eqRgb,eqAlpha);if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();RenderSystem.colorMask(mask[0],mask[1],mask[2],mask[3]);GL11.glScissor(box[0],box[1],box[2],box[3]);flag(GL11.GL_SCISSOR_TEST,scissor);flag(GL11.GL_STENCIL_TEST,stencil);flag(GL30.GL_FRAMEBUFFER_SRGB,srgb);}
 }
}
