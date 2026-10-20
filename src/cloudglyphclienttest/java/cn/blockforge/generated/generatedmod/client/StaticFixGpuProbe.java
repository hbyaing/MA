package cn.blockforge.generated.generatedmod.client;

import java.nio.*;
import java.nio.file.*;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Isolated, opt-in GPU checks; never included in the distribution. */
@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT)
public final class StaticFixGpuProbe {
    private static boolean done;
    private static int failurePhase;
    private static final List<Integer> textures=new ArrayList<>(),programs=new ArrayList<>();
    private static final String VERTEX="#version 150\nout vec2 texCoord;out vec2 uv;void main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));texCoord=p;uv=p;gl_Position=vec4(p*2.-1.,0,1);}";
    @SubscribeEvent public static void failureLatch(TickEvent.RenderTickEvent e) {
        if(!done||failurePhase>=3||!Boolean.getBoolean("wuxingxiantu.staticFixCapture")||e.phase!=TickEvent.Phase.END)return;
        try {
            var cls=cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.class;
            var recipes=cls.getDeclaredField("recipes");recipes.setAccessible(true);
            var failed=cls.getDeclaredField("bottomFailed");failed.setAccessible(true);
            if(failurePhase==0) {
                if(cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.draws()==0)return;
                recipes.set(null,java.lang.reflect.Array.newInstance(recipes.getType().getComponentType(),0));failurePhase=1;return;
            }
            if(!failed.getBoolean(null)||recipes.get(null)!=null)throw new AssertionError("Bottom volume retried after persistent failure");
            if(failurePhase++==1)return; // Check two consecutive frames remain disabled.
            var reset=cls.getDeclaredMethod("reset");reset.setAccessible(true);reset.invoke(null);
            if(failed.getBoolean(null))throw new AssertionError("Reload reset did not re-enable volume");
            var path=Path.of("../../reports/static-fixes-20261015/gpu.json");
            var result=com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            result.addProperty("bottomFailureLatchAndReset",true);Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));
        }catch(Throwable error){failurePhase=3;throw new AssertionError("Failure latch probe",error);}
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e) {
        if(done||!Boolean.getBoolean("wuxingxiantu.staticFixCapture")||e.phase!=TickEvent.Phase.END||Minecraft.getInstance().level==null)return;
        done=true;var rows=new LinkedHashMap<String,Object>();int fbo=0,vao=0;
        try(var saved=new cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer.State()) {
            fbo=GL30.glGenFramebuffers();vao=GL30.glGenVertexArrays();GL30.glBindVertexArray(vao);
            GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glColorMask(true,true,true,true);
            var projection=new Matrix4f().perspective(1F,1F,.1F,1024F);var inverse=new Matrix4f(projection).invert();
            int blur=program(source("energy_blur.fsh")),composite=program(source("realm_meteor_bloom.fsh"));
            int emission=texture(3,GL30.GL_RGBA32F,new float[]{1,0,0,140F/128});
            int output=texture(3,GL30.GL_RGBA32F,new float[]{0,0,0,0});
            for(int p:new int[]{blur,composite})for(float foreground:new float[]{130,160}) {
                int depth=texture(3,GL30.GL_R32F,new float[]{depth(projection,foreground)});
                attach(fbo,output);bind(0,emission);bind(1,depth);GL20.glUseProgram(p);
                uniforms(p,inverse);uniform(p,"Direction",0F,0F);
                GL11.glClearColor(0,0,0,0);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
                float[] rgba=read();boolean visible=foreground>140;
                if(visible?rgba[0]<.8F:rgba[0]>.00001F)throw new AssertionError("Bloom far-depth occlusion: "+Arrays.toString(rgba));
                rows.put((p==blur?"blur":"composite")+"-foreground-"+foreground,Arrays.toString(rgba));
            }
            // Transparent empty texels must not interpolate a sentinel into the surviving glow.
            int edge=texture(3,GL30.GL_RGBA32F,new float[]{0,0,0,1000000});
            bind(0,edge);FloatBuffer bright=org.lwjgl.BufferUtils.createFloatBuffer(4).put(new float[]{1,0,0,140F/128});bright.flip();
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,0,1,1,1,1,GL11.GL_RGBA,GL11.GL_FLOAT,bright);
            int depth=texture(3,GL30.GL_R32F,new float[]{depth(projection,160)});
            attach(fbo,output);bind(0,edge);bind(1,depth);GL20.glUseProgram(composite);uniforms(composite,inverse);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
            if(read()[0]<.9F)throw new AssertionError("Empty sentinel suppressed glow");rows.put("emptySentinel",true);
            int light=program(source("anvil_blue_light.fsh"));
            int old=program(Files.readString(Path.of("../../deliverables/task-20261015-static-fixes/backup/src/main/resources/assets/wuxingxiantu/shaders/core/anvil_blue_light.fsh")));
            int base=texture(3,GL11.GL_RGBA8,new float[]{.2F,.3F,.4F,1});
            int ping=texture(3,GL11.GL_RGBA8,new float[]{0,0,0,1}),pong=texture(3,GL11.GL_RGBA8,new float[]{0,0,0,1});
            int plane=texture(3,GL30.GL_R32F,new float[]{depth(projection,3)});
            for(int count:new int[]{1,2,4}) {
                int input=base;
                for(int i=0;i<count;i++) {
                    int target=i==count-1?output:i%2==0?ping:pong;
                    attach(fbo,target);bind(0,input);bind(1,plane);GL20.glUseProgram(old);lightUniforms(old,inverse);uniform(old,"LightRelative",i*.4F,0F,-1F);
                    GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);input=target;
                }
                float[] reference=read();
                attach(fbo,output);bind(0,base);bind(1,plane);GL20.glUseProgram(light);lightUniforms(light,inverse);GL20.glUniform1i(GL20.glGetUniformLocation(light,"LightCount"),count);
                for(int i=0;i<4;i++)uniform(light,"LightRelative"+i,i*.4F,0F,-1F);
                GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);float[] actual=read();
                for(int i=0;i<3;i++)if(Math.abs(reference[i]-actual[i])>.0041)throw new AssertionError("Batched light differs: "+Arrays.toString(reference)+" / "+Arrays.toString(actual));
                rows.put("lights-"+count,Map.of("old",Arrays.toString(reference),"new",Arrays.toString(actual)));
            }
            for(Vec3 up:new Vec3[]{new Vec3(1,0,0),new Vec3(0,1,0),new Vec3(0,0,1)}) {
                var right=cn.blockforge.generated.generatedmod.client.arts.ArtMaterialParticle.directionRight(up,up);
                if(Math.abs(right.dot(up))>1e-8||right.lengthSqr()<.99)throw new AssertionError("Particle basis degenerate");
            }
            rows.put("particleBasis",true);rows.put("glError",GL11.glGetError());
            if(((Number)rows.get("glError")).intValue()!=0)throw new AssertionError("GPU state error");rows.put("passed",true);
        }catch(Throwable failure){rows.put("passed",false);rows.put("error",failure.toString());com.mojang.logging.LogUtils.getLogger().error("STATIC_FIX_GPU",failure);}
        finally {
            for(int p:programs)GL20.glDeleteProgram(p);for(int t:textures)GL11.glDeleteTextures(t);
            if(fbo!=0)GL30.glDeleteFramebuffers(fbo);if(vao!=0)GL30.glDeleteVertexArrays(vao);
            try{var out=Path.of("../../reports/static-fixes-20261015/gpu.json");Files.createDirectories(out.getParent());Files.writeString(out,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));}catch(Exception error){throw new RuntimeException(error);}
        }
    }
    private static String source(String name)throws Exception {try(var in=StaticFixGpuProbe.class.getResourceAsStream("/assets/wuxingxiantu/shaders/core/"+name)){return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}}
    public static Map<String,Object> anvilOwnerBoundary() throws Exception {
        var result=new LinkedHashMap<String,Object>();int fbo=0,vao=0;
        try(var state=new cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer.State()) {
            fbo=GL30.glGenFramebuffers();vao=GL30.glGenVertexArrays();GL30.glBindVertexArray(vao);
            GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glColorMask(true,true,true,true);
            var projection=new Matrix4f().perspective(1F,1F,.1F,1024F);
            var inverse=new Matrix4f().rotationX(-(float)Math.PI/2).mul(new Matrix4f(projection).invert());
            String current=source("anvil_blue_light.fsh");String old=current.replace("if(!(abs(ownerDelta.x)<.5&&abs(ownerDelta.z)<.5&&abs(ownerDelta.y)<.5))","if(!(abs(delta.x)<.505&&abs(delta.z)<.505&&abs(delta.y)<.54))");
            if(old.equals(current))throw new AssertionError("Boundary reference not constructed");
            int before=program(old),after=program(current),base=texture(3,GL30.GL_RGBA32F,new float[]{.2F,.2F,.2F,1});
            int plane=texture(3,GL30.GL_R32F,new float[]{depth(projection,2)}),output=texture(3,GL30.GL_RGBA32F,new float[]{0,0,0,0});
            float oldBlue=0,newBlue=0;
            for(int p:new int[]{before,after}) {
                attach(fbo,output);bind(0,base);bind(1,plane);GL20.glUseProgram(p);lightUniforms(p,inverse);
                GL20.glUniform1i(GL20.glGetUniformLocation(p,"LightCount"),1);uniform(p,"LightRelative0",0,-1.5F,0);
                GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);float[] pixel=read();if(p==before)oldBlue=pixel[2];else newBlue=pixel[2];
            }
            if(Math.abs(oldBlue-.2F)>.002||newBlue<oldBlue+.15F)throw new AssertionError("Ground not repaired: "+oldBlue+" / "+newBlue+"; GL="+GL11.glGetError());
            result.put("groundOldBlue",oldBlue);result.put("groundNewBlue",newBlue);
            attach(fbo,output);bind(0,base);bind(1,plane);GL20.glUseProgram(after);lightUniforms(after,inverse);
            GL20.glUniform1i(GL20.glGetUniformLocation(after,"LightCount"),1);uniform(after,"LightRelative0",0,-1.8F,0);
            GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);float inside=read()[2];
            if(Math.abs(inside-.2F)>.002)throw new AssertionError("Owner no longer excluded");result.put("ownerBlue",inside);
            int error=GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);result.put("glError",error);result.put("passed",true);
        }finally{for(int p:programs)GL20.glDeleteProgram(p);for(int t:textures)GL11.glDeleteTextures(t);programs.clear();textures.clear();if(fbo!=0)GL30.glDeleteFramebuffers(fbo);if(vao!=0)GL30.glDeleteVertexArrays(vao);}
        return result;
    }
    private static int shader(int type,String source){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new AssertionError(GL20.glGetShaderInfoLog(s));return s;}
    private static int program(String source){int v=shader(GL20.GL_VERTEX_SHADER,VERTEX),f=shader(GL20.GL_FRAGMENT_SHADER,source),p=GL20.glCreateProgram();programs.add(p);GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL20.glLinkProgram(p);GL20.glDeleteShader(v);GL20.glDeleteShader(f);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new AssertionError(GL20.glGetProgramInfoLog(p));return p;}
    private static int texture(int size,int internal,float[] pixel){int t=GL11.glGenTextures();textures.add(t);GL13.glActiveTexture(GL13.GL_TEXTURE0);GL11.glBindTexture(GL11.GL_TEXTURE_2D,t);var data=org.lwjgl.BufferUtils.createFloatBuffer(size*size*pixel.length);for(int i=0;i<size*size;i++)data.put(pixel);data.flip();GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,internal,size,size,0,pixel.length==1?GL11.GL_RED:GL11.GL_RGBA,GL11.GL_FLOAT,data);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);return t;}
    private static void bind(int unit,int texture){GL13.glActiveTexture(GL13.GL_TEXTURE0+unit);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);}
    private static void attach(int fbo,int target){GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,target,0);GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new AssertionError("Probe target incomplete");GL11.glViewport(0,0,3,3);}
    private static float depth(Matrix4f projection,float meters){var p=projection.transform(new Vector4f(0,0,-meters,1));return p.z/p.w*.5F+.5F;}
    private static float[] read(){var data=org.lwjgl.BufferUtils.createFloatBuffer(4);GL11.glReadPixels(1,1,1,1,GL11.GL_RGBA,GL11.GL_FLOAT,data);return new float[]{data.get(0),data.get(1),data.get(2),data.get(3)};}
    private static void uniform(int program,String name,float... values){int location=GL20.glGetUniformLocation(program,name);if(values.length==1)GL20.glUniform1f(location,values[0]);else if(values.length==2)GL20.glUniform2f(location,values[0],values[1]);else GL20.glUniform3f(location,values[0],values[1],values[2]);}
    private static void matrix(int p,String name,Matrix4f value){var data=org.lwjgl.BufferUtils.createFloatBuffer(16);value.get(data);GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(p,name),false,data);}
    private static void uniforms(int p,Matrix4f inverse){GL20.glUniform1i(GL20.glGetUniformLocation(p,"Sampler0"),0);GL20.glUniform1i(GL20.glGetUniformLocation(p,"Sampler1"),1);matrix(p,"InverseProjection",inverse);uniform(p,"OutputSize",3,3);uniform(p,"Intensity",1);}
    private static void lightUniforms(int p,Matrix4f inverse){GL20.glUniform1i(GL20.glGetUniformLocation(p,"Sampler0"),0);GL20.glUniform1i(GL20.glGetUniformLocation(p,"Sampler1"),1);matrix(p,"InverseVP",inverse);uniform(p,"LightColor",.015F,.3F,1);uniform(p,"Radius",4.5F);uniform(p,"Intensity",10);uniform(p,"Texel",1F/3,1F/3);}
}
