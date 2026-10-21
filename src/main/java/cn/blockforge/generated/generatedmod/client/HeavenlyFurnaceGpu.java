package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reviewed workbench renderer: full resolution HDR, eight depth peels, procedural volumes.
 * Owns every GL resource it creates and restores raw state without changing Minecraft caches. */
public final class HeavenlyFurnaceGpu {
    public static final float WORLD_SCALE=1.6f;
    private static final List<Target> TARGETS=new ArrayList<>();
    private static final List<Mesh> MESHES=new ArrayList<>();
    private static Program surface,volume,down,blur,composite;
    private static Mesh ring,ribbon,wisp,spark,curtain,sphere;
    private static Target scene;
    private static final Target[] peels=new Target[8];
    private static final Target[][] blooms=new Target[5][2];
    private static int empty,width,height;
    public static int generation,draws,targetAllocations;
    public static String failure="";

    public static void release(){
        if(!RenderSystem.isOnRenderThread()){RenderSystem.recordRenderCall(HeavenlyFurnaceGpu::release);return;}
        freeTargets();
        for(var m:MESHES){GL30.glDeleteVertexArrays(m.vao);GL15.glDeleteBuffers(m.vbo);GL15.glDeleteBuffers(m.ibo);}MESHES.clear();
        for(var p:new Program[]{surface,volume,down,blur,composite})if(p!=null)GL20.glDeleteProgram(p.id);
        surface=volume=down=blur=composite=null;
        if(empty!=0)GL30.glDeleteVertexArrays(empty);empty=0;failure="";
    }
    private static void freeTargets(){for(var t:TARGETS){GL30.glDeleteFramebuffers(t.fbo);GL11.glDeleteTextures(t.texture);if(t.depth!=0)GL11.glDeleteTextures(t.depth);}TARGETS.clear();width=height=0;}
    private static String source(String name)throws Exception{
        var id=new ResourceLocation("wuxingxiantu","heavenly/shaders/"+name);
        try(var in=Minecraft.getInstance().getResourceManager().open(id)){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}
    }
    private static int compile(int type,String source){int id=GL20.glCreateShader(type);GL20.glShaderSource(id,source);GL20.glCompileShader(id);if(GL20.glGetShaderi(id,GL20.GL_COMPILE_STATUS)==0){String error=GL20.glGetShaderInfoLog(id);GL20.glDeleteShader(id);throw new IllegalStateException(error);}return id;}
    private static Program program(String vertex,String fragment)throws Exception{
        int v=0,f=0,p=GL20.glCreateProgram();try{
            v=compile(GL20.GL_VERTEX_SHADER,source(vertex));f=compile(GL20.GL_FRAGMENT_SHADER,source(fragment));
            GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL20.glBindAttribLocation(p,0,"aUV");GL30.glBindFragDataLocation(p,0,"fragColor");GL20.glLinkProgram(p);
            if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(fragment+": "+GL20.glGetProgramInfoLog(p));
            return new Program(p);
        }catch(Exception ex){GL20.glDeleteProgram(p);throw ex;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}
    }
    private static void init()throws Exception{
        if(surface!=null)return;
        if(GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS)<17)throw new IllegalStateException("Heavenly furnace requires 17 fragment texture units");
        try{
            surface=program("surface.vsh","surface.fsh");volume=program("fullscreen.vsh","volume.fsh");down=program("fullscreen.vsh","down.fsh");blur=program("fullscreen.vsh","blur.fsh");composite=program("fullscreen.vsh","composite.fsh");
            ring=mesh(512,3);ribbon=mesh(320,10);wisp=mesh(192,12);spark=mesh(1,1);curtain=mesh(24,1);sphere=mesh(64,32);empty=GL30.glGenVertexArrays();generation++;
        }catch(Exception ex){release();throw ex;}
    }
    private static Mesh mesh(int nx,int ny){
        FloatBuffer vertices=BufferUtils.createFloatBuffer((nx+1)*(ny+1)*2);IntBuffer indices=BufferUtils.createIntBuffer(nx*ny*6);
        for(int y=0;y<=ny;y++)for(int x=0;x<=nx;x++)vertices.put(x/(float)nx).put(y/(float)ny);vertices.flip();
        for(int y=0;y<ny;y++)for(int x=0;x<nx;x++){int i=y*(nx+1)+x;indices.put(i).put(i+1).put(i+nx+2).put(i).put(i+nx+2).put(i+nx+1);}indices.flip();
        int vao=GL30.glGenVertexArrays(),vbo=GL15.glGenBuffers(),ibo=GL15.glGenBuffers();GL30.glBindVertexArray(vao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);GL15.glBufferData(GL15.GL_ARRAY_BUFFER,vertices,GL15.GL_STATIC_DRAW);GL20.glEnableVertexAttribArray(0);GL20.glVertexAttribPointer(0,2,GL11.GL_FLOAT,false,8,0L);GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,ibo);GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER,indices,GL15.GL_STATIC_DRAW);
        var mesh=new Mesh(vao,vbo,ibo,nx*ny*6);MESHES.add(mesh);return mesh;
    }
    private static int texture(int w,int h,boolean depth){
        int id=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,depth?GL30.GL_DEPTH_COMPONENT24:GL30.GL_RGBA16F,w,h,0,depth?GL11.GL_DEPTH_COMPONENT:GL11.GL_RGBA,depth?GL11.GL_UNSIGNED_INT:GL30.GL_HALF_FLOAT,(ByteBuffer)null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,depth?GL11.GL_NEAREST:GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,depth?GL11.GL_NEAREST:GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL14.GL_TEXTURE_COMPARE_MODE,GL11.GL_NONE);return id;
    }
    private static Target target(int w,int h,boolean depth){
        int color=texture(w,h,false),d=depth?texture(w,h,true):0,fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,color,0);if(d!=0)GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_DEPTH_ATTACHMENT,GL11.GL_TEXTURE_2D,d,0);
        var t=new Target(fbo,color,d,w,h);TARGETS.add(t);targetAllocations++;
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Heavenly HDR framebuffer incomplete");return t;
    }
    private static void resize(int w,int h){
        if(width==w&&height==h)return;freeTargets();width=w;height=h;scene=target(w,h,false);
        for(int i=0;i<8;i++)peels[i]=target(w,h,true);
        for(int i=0;i<5;i++)for(int j=0;j<2;j++)blooms[i][j]=target(Math.max(1,w>>(i+1)),Math.max(1,h>>(i+1)),false);
    }
    private static void bind(Target t){GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,t.fbo);GL11.glViewport(0,0,t.width,t.height);}
    private static void tex(int unit,int texture){GL13.glActiveTexture(GL13.GL_TEXTURE0+unit);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);}
    private static void full(Program p,Target t){if(t!=null)bind(t);p.use();GL30.glBindVertexArray(empty);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);}
    private static void draw(int kind,Mesh mesh,int count,int offset){surface.i("uKind",kind);surface.i("uOffset",offset);GL30.glBindVertexArray(mesh.vao);GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES,mesh.count,GL11.GL_UNSIGNED_INT,0L,count);}
    private static void surfaces(float ignite){
        draw(3,wisp,18,18);draw(3,wisp,18,0);draw(6,wisp,14,0);draw(5,curtain,96,0);
        if(ignite>0){draw(1,ribbon,9,0);draw(7,sphere,1,0);}
        draw(2,ribbon,20,0);draw(0,ring,2,2);draw(8,ring,6,6);
        draw(0,ring,2,0);draw(12,ring,2,0);draw(10,ring,2,0);draw(11,ring,2,0);
        draw(8,ring,6,0);draw(4,spark,1700,0);
    }
    /** Called only after the final world composite; world depth comes from the existing bridge. */
    public static void render(RenderLevelStageEvent e,Vec3 center,float yaw,float age,float ignite,float shrink){
        render(e,center,yaw,age,ignite,shrink,1);
    }
    public static void render(RenderLevelStageEvent e,Vec3 center,float yaw,float age,float ignite,float shrink,int fireTier){
        if(!failure.isEmpty())return;
        try(State saved=new State()){
            for(int i=0;i<17;i++)GL33.glBindSampler(i,0);
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,0);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,1);GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS,0);
            init();var target=Minecraft.getInstance().getMainRenderTarget();resize(target.viewWidth,target.viewHeight);
            float scale=WORLD_SCALE*(1-shrink*.94f);Vec3 rel=center.subtract(e.getCamera().getPosition());
            var view=new Matrix4f(e.getPoseStack().last().pose()).translate((float)rel.x,(float)rel.y,(float)rel.z).rotateY((float)Math.toRadians(-yaw)).scale(scale);
            var vp=new Matrix4f(e.getProjectionMatrix()).mul(view);var inverse=new Matrix4f(vp).invert();var iv=new Matrix4f(view).invert();
            var eyeH=inverse.transform(new Vector4f(0,0,-1,0));Vector3f eye;
            if(Float.isFinite(eyeH.w)&&Math.abs(eyeH.w)>1e-7f)eye=new Vector3f(eyeH.x,eyeH.y,eyeH.z).div(eyeH.w);else eye=iv.transformPosition(new Vector3f());
            var right=iv.transformDirection(new Vector3f(1,0,0)).normalize();var up=iv.transformDirection(new Vector3f(0,1,0)).normalize();
            float time=age/20f;
            GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);GL11.glColorMask(true,true,true,true);GL11.glDepthMask(true);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LESS);GL11.glClearDepth(1);GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,0);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,1);GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS,0);
            tex(16,TrailShaderBridge.capturedDepthTexture());
            surface.use();surface.m("uVP",vp);surface.v("uRight",right);surface.v("uUp",up);surface.v("uEyeLocal",iv.transformPosition(new Vector3f()));surface.i("uWorldDepth",16);surface.v2("uResolution",width,height);
            for(var p:new Program[]{surface,volume}){
                p.use();p.f("uTime",time);p.f("uFireTier",fireTier);p.f("uRadius",1);p.f("uHeight",1);p.f("uTilt",(float)Math.toRadians(31));p.f("uSpin",1);p.f("uSwirl",1);p.f("uWisp",1);p.f("uSparks",1);p.f("uGoldPower",1.8f);p.f("uBluePower",2);p.f("uFirePower",2*ignite);p.f("uWispPower",1);p.f("uGlyph",1);p.f("uSparkPower",1.2f);p.color("uGold",0xffe0ba);p.color("uBlue",0x2164ff);p.color("uFire",cn.blockforge.generated.generatedmod.SpiritFirePalette.base(fireTier));
            }
            surface.use();
            for(int i=0;i<8;i++){bind(peels[i]);GL11.glClearColor(0,0,0,0);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);tex(0,peels[(i+7)%8].depth);surface.i("uPeel",i);surface.i("uPrevDepth",0);surfaces(ignite);}
            GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);volume.use();
            for(int i=0;i<8;i++){tex(i*2,peels[i].texture);tex(i*2+1,peels[i].depth);volume.i("uPeelColor"+i,i*2);volume.i("uPeelDepth"+i,i*2+1);}
            tex(16,TrailShaderBridge.capturedDepthTexture());volume.i("uWorldDepth",16);volume.m("uInvVP",inverse);volume.v("uEye",eye);volume.i("uSteps",256);volume.f("uSmoke",1.5f);volume.f("uMist",1.25f);volume.f("uCoreStrength",ignite);GL20.glUniform4f(volume.loc("uLayers"),ignite>0?1:0,1,1,0);full(volume,scene);
            Target source=scene;
            for(int i=0;i<5;i++){
                Target a=blooms[i][0],b=blooms[i][1];tex(0,source.texture);down.use();down.i("uSource",0);down.v2("uTexel",1f/source.width,1f/source.height);down.f("uThreshold",i==0?.75f:0);full(down,a);
                tex(0,a.texture);blur.use();blur.i("uSource",0);blur.v2("uDirection",1.2f/a.width,0);full(blur,b);tex(0,b.texture);blur.v2("uDirection",0,1.2f/a.height);full(blur,a);source=a;
            }
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,target.frameBufferId);GL11.glViewport(0,0,width,height);GL11.glEnable(GL11.GL_BLEND);GL14.glBlendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD,GL14.GL_FUNC_ADD);
            tex(0,scene.texture);tex(16,TrailShaderBridge.capturedDepthTexture());composite.use();composite.i("uScene",0);composite.i("uWorldDepth",16);composite.m("uInvVP",inverse);composite.v("uEye",eye);for(int i=0;i<5;i++){tex(i+1,blooms[i][0].texture);composite.i("uBloom"+i,i+1);}composite.f("uBloom",.85f);composite.f("uExposure",.95f);composite.f("uVignette",0);composite.f("uOpacity",Mth.clamp(age/30f,0,1)*(1-shrink*.3f));full(composite,null);draws++;
        }catch(Exception ex){failure=ex.toString();com.mojang.logging.LogUtils.getLogger().error("天地为炉着色器绘制失败",ex);}
    }
    private record Target(int fbo,int texture,int depth,int width,int height){}
    private record Mesh(int vao,int vbo,int ibo,int count){}
    private static final class Program {
        final int id;final Map<String,Integer> uniforms=new HashMap<>();final float[] matrix=new float[16];
        Program(int id){this.id=id;}void use(){GL20.glUseProgram(id);}int loc(String name){return uniforms.computeIfAbsent(name,n->GL20.glGetUniformLocation(id,n));}
        void i(String n,int v){GL20.glUniform1i(loc(n),v);}void f(String n,float v){GL20.glUniform1f(loc(n),v);}void v2(String n,float x,float y){GL20.glUniform2f(loc(n),x,y);}void v(String n,Vector3f v){GL20.glUniform3f(loc(n),v.x,v.y,v.z);}void m(String n,Matrix4f m){GL20.glUniformMatrix4fv(loc(n),false,m.get(matrix));}
        void color(String n,int rgb){GL20.glUniform3f(loc(n),linear(rgb>>16&255),linear(rgb>>8&255),linear(rgb&255));}float linear(int v){float c=v/255f;return c<=.04045f?c/12.92f:(float)Math.pow((c+.055f)/1.055f,2.4);}
    }
    private static final class State implements AutoCloseable {
        final int program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),buffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING),draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),unpack=GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING),alignment=GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT),row=GL11.glGetInteger(GL11.GL_UNPACK_ROW_LENGTH),skipRow=GL11.glGetInteger(GL11.GL_UNPACK_SKIP_ROWS),skipPixel=GL11.glGetInteger(GL11.GL_UNPACK_SKIP_PIXELS);
        final boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),srgb=GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB),mask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        final int depthFunc=GL11.glGetInteger(GL11.GL_DEPTH_FUNC),src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),eq=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB),eqA=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        final int[] viewport=new int[4],textures=new int[17],samplers=new int[17];final float[] clear=new float[4];final ByteBuffer colors=BufferUtils.createByteBuffer(4);final double clearDepth=GL11.glGetDouble(GL11.GL_DEPTH_CLEAR_VALUE);
        State(){GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE,clear);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,colors);for(int i=0;i<textures.length;i++){GL13.glActiveTexture(GL13.GL_TEXTURE0+i);textures[i]=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);samplers[i]=GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);}}
        public void close(){
            for(int i=0;i<textures.length;i++){GL13.glActiveTexture(GL13.GL_TEXTURE0+i);GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[i]);GL33.glBindSampler(i,samplers[i]);}GL13.glActiveTexture(active);GL20.glUseProgram(program);GL30.glBindVertexArray(vao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,buffer);GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,unpack);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,alignment);GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH,row);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS,skipRow);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS,skipPixel);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GL11.glViewport(viewport[0],viewport[1],viewport[2],viewport[3]);GL11.glClearColor(clear[0],clear[1],clear[2],clear[3]);GL11.glClearDepth(clearDepth);GL11.glColorMask(colors.get(0)!=0,colors.get(1)!=0,colors.get(2)!=0,colors.get(3)!=0);GL11.glDepthMask(mask);GL11.glDepthFunc(depthFunc);GL14.glBlendFuncSeparate(src,dst,srcA,dstA);GL20.glBlendEquationSeparate(eq,eqA);flag(GL11.GL_BLEND,blend);flag(GL11.GL_CULL_FACE,cull);flag(GL11.GL_DEPTH_TEST,depth);flag(GL11.GL_SCISSOR_TEST,scissor);flag(GL30.GL_FRAMEBUFFER_SRGB,srgb);
        }
        void flag(int flag,boolean enabled){if(enabled)GL11.glEnable(flag);else GL11.glDisable(flag);}
    }
    private HeavenlyFurnaceGpu(){}
}
