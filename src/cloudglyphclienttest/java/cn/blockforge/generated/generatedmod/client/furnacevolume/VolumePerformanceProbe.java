package cn.blockforge.generated.generatedmod.client.furnacevolume;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

/** Test-only paired GPU queries and float framebuffer comparison, identical uniforms/depth. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class VolumePerformanceProbe implements FurnaceFireVolume.DrawProbe{
    private static final String REFERENCE=System.getProperty("wuxingxiantu.perfBaselineShader","");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.perfOutput","../../reports/performance-20261014"));
    private static final double FIXED_TIME=Double.parseDouble(System.getProperty("wuxingxiantu.perfTimeSeconds","NaN"));
    @Override public double clockSeconds(double gameSeconds){return Double.isFinite(FIXED_TIME)?FIXED_TIME:gameSeconds;}
    private static final VolumePerformanceProbe INSTANCE=new VolumePerformanceProbe();
    private final Set<String> seen=new HashSet<>();private final List<Map<String,Object>> rows=new ArrayList<>();private int reference,fieldProbe;private boolean compositePending;
    private float densityAt(int production,float y)throws Exception{
        return densityAt(production,y,-1.55F);
    }
    private float densityAt(int production,float y,float z)throws Exception{
        if(fieldProbe==0){
            String source=Files.readString(Path.of(REFERENCE));source=source.substring(0,source.indexOf("// SHARED_VOLUME_CORE_END"));
            source=source.replace("#version 150","#version 150\n#define WUXING_BOTTOM_FIRE 1\n#define WUXING_CORNER_CACHE 1\n#define WUXING_NOISE_CACHE 1");
            source+="\nuniform vec3 ProbePoint;void main(){result=avFireBed(ProbePoint-vec3(0.,FireLift,0.),Time,float(Seed%997)*.071);}";
            int v=shader(GL20.GL_VERTEX_SHADER,"#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}");
            if(Boolean.getBoolean("wuxingxiantu.perfBaselineNoise"))source=source.replace("#define WUXING_CORNER_CACHE 1","#define WUXING_CORNER_CACHE 1\n#define WUXING_NOISE_CACHE 1");int f=shader(GL20.GL_FRAGMENT_SHADER,source);fieldProbe=GL20.glCreateProgram();GL20.glAttachShader(fieldProbe,v);GL20.glAttachShader(fieldProbe,f);GL30.glBindFragDataLocation(fieldProbe,0,"result");GL20.glLinkProgram(fieldProbe);GL20.glDeleteShader(v);GL20.glDeleteShader(f);
            if(GL20.glGetProgrami(fieldProbe,GL20.GL_LINK_STATUS)==0)throw new AssertionError(GL20.glGetProgramInfoLog(fieldProbe));
        }
        int[] scissor=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissor);
        try{copyUniforms(production,fieldProbe);GL20.glUniform3f(GL20.glGetUniformLocation(fieldProbe,"ProbePoint"),0,y,z);GL11.glScissor(0,0,1,1);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
            FloatBuffer pixel=org.lwjgl.BufferUtils.createFloatBuffer(4);GL11.glReadPixels(0,0,1,1,GL11.GL_RGBA,GL11.GL_FLOAT,pixel);return pixel.get(3);
        }finally{GL11.glScissor(scissor[0],scissor[1],scissor[2],scissor[3]);GlStateManager._glUseProgram(production);}
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(!REFERENCE.isEmpty()&&e.phase==TickEvent.Phase.START)FurnaceFireVolume.drawProbe=INSTANCE;}
    private static int shader(int type,String text){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,text);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new AssertionError(GL20.glGetShaderInfoLog(s));return s;}
    private void init()throws Exception{if(reference!=0)return;int v=shader(GL20.GL_VERTEX_SHADER,"#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}");String source=Files.readString(Path.of(REFERENCE));if(Boolean.getBoolean("wuxingxiantu.perfBaselineCorners"))source=source.replace("#version 150","#version 150\n#define WUXING_BOTTOM_FIRE 1\n#define WUXING_CORNER_CACHE 1");if(Boolean.getBoolean("wuxingxiantu.perfBaselineNoise"))source=source.replace("#define WUXING_CORNER_CACHE 1","#define WUXING_CORNER_CACHE 1\n#define WUXING_NOISE_CACHE 1");int f=shader(GL20.GL_FRAGMENT_SHADER,source);
        reference=GL20.glCreateProgram();GL20.glAttachShader(reference,v);GL20.glAttachShader(reference,f);GL30.glBindFragDataLocation(reference,0,"result");GL20.glLinkProgram(reference);GL20.glDeleteShader(v);GL20.glDeleteShader(f);if(GL20.glGetProgrami(reference,GL20.GL_LINK_STATUS)==0)throw new AssertionError(GL20.glGetProgramInfoLog(reference));}
    static void copyUniforms(int from,int to){
        GlStateManager._glUseProgram(to);
        try(var stack=MemoryStack.stackPush()){
            IntBuffer size=stack.mallocInt(1),type=stack.mallocInt(1);FloatBuffer values=stack.mallocFloat(16);
            for(int i=0;i<GL20.glGetProgrami(from,GL20.GL_ACTIVE_UNIFORMS);i++){
                String name=GL20.glGetActiveUniform(from,i,256,size,type);int src=GL20.glGetUniformLocation(from,name),dst=GL20.glGetUniformLocation(to,name);if(dst<0)continue;
                switch(type.get(0)){
                    case GL11.GL_INT,GL20.GL_SAMPLER_2D,GL20.GL_SAMPLER_3D->GL20.glUniform1i(dst,GL20.glGetUniformi(from,src));
                    case GL11.GL_FLOAT,GL20.GL_FLOAT_VEC2,GL20.GL_FLOAT_VEC3,GL20.GL_FLOAT_MAT4->{values.clear();GL20.glGetUniformfv(from,src,values);
                        if(type.get(0)==GL11.GL_FLOAT)GL20.glUniform1f(dst,values.get(0));
                        else if(type.get(0)==GL20.GL_FLOAT_VEC2)GL20.glUniform2f(dst,values.get(0),values.get(1));
                        else if(type.get(0)==GL20.GL_FLOAT_VEC3)GL20.glUniform3f(dst,values.get(0),values.get(1),values.get(2));
                        else GL20.glUniformMatrix4fv(dst,false,values);}
                    default->throw new AssertionError("Unexpected uniform type "+name+"/"+type.get(0));
                }
            }
        }
    }
    private static double measure(int shader,int query){GlStateManager._glUseProgram(shader);GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,query);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);GL15.glEndQuery(GL33.GL_TIME_ELAPSED);return GL33.glGetQueryObjectui64(query,GL15.GL_QUERY_RESULT)/1e6;}
    private static double measureFullReference(int shader,int query,int w,int h){
        var main=net.minecraft.client.Minecraft.getInstance().getMainRenderTarget();
        int[] box=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,box);
        GlStateManager._glUseProgram(shader);
        GL20.glUniform2f(GL20.glGetUniformLocation(shader,"Viewport"),main.width,main.height);
        GL11.glViewport(0,0,main.width,main.height);
        int x=box[0]*main.width/w,y=box[1]*main.height/h;
        GL11.glScissor(x,y,Math.min(main.width-x,(box[2]*main.width+w-1)/w),Math.min(main.height-y,(box[3]*main.height+h-1)/h));
        try{return measure(shader,query);}
        finally{GL11.glViewport(0,0,w,h);GL11.glScissor(box[0],box[1],box[2],box[3]);GL20.glUniform2f(GL20.glGetUniformLocation(shader,"Viewport"),w,h);}
    }
    private static double median(double[] values){double[] sorted=values.clone();Arrays.sort(sorted);return (sorted[sorted.length/2]+sorted[(sorted.length-1)/2])*.5;}
    @Override public void composite(Runnable draw){
        if(!compositePending){draw.run();return;}
        compositePending=false;int query=GL15.glGenQueries();
        try{
            GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,query);draw.run();GL15.glEndQuery(GL33.GL_TIME_ELAPSED);
            double ms=GL33.glGetQueryObjectui64(query,GL15.GL_QUERY_RESULT)/1e6;
            var row=rows.get(rows.size()-1);row.put("compositeGpuMs",ms);
            row.put("totalDrawGpuMs",((Number)row.get("optimizedGpuMs")).doubleValue()+ms);
            Files.writeString(OUTPUT.resolve("paired-gpu.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("renderer",GL11.glGetString(GL11.GL_RENDERER),"measurement","paired full-resolution baseline/integration medians plus single actual composite draw; RGBA32F field comparison at identical rays","cases",rows)));
        }catch(Exception error){throw new AssertionError(error);}
        finally{GL15.glDeleteQueries(query);}
    }
    public void draw(int program,int vao,int w,int h,String id){
        compositePending=false;if(id.endsWith("#upper")||!seen.add(id))return;
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int fbo=0,color=0,q=0;boolean blend=GL11.glIsEnabled(GL11.GL_BLEND);float[] clear=new float[4];GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE,clear);
        try{init();copyUniforms(program,reference);
            color=GL11.glGenTextures();GlStateManager._bindTexture(color);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,net.minecraft.client.Minecraft.getInstance().getMainRenderTarget().width,net.minecraft.client.Minecraft.getInstance().getMainRenderTarget().height,0,GL11.GL_RGBA,GL11.GL_FLOAT,(ByteBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
            fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,color,0);GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new AssertionError("Comparison FBO incomplete");
            GlStateManager._bindTexture(texture);GL11.glDisable(GL11.GL_BLEND);q=GL15.glGenQueries();double[] before=new double[24],after=new double[24];
            for(int i=-6;i<24;i++){double a,b;if((i&1)==0){a=measureFullReference(reference,q,w,h);b=measure(program,q);}else{b=measure(program,q);a=measureFullReference(reference,q,w,h);}if(i>=0){before[i]=a;after[i]=b;}}
            GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glClearColor(0,0,0,0);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);GL11.glEnable(GL11.GL_SCISSOR_TEST);GlStateManager._glUseProgram(reference);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
            FloatBuffer baseline=org.lwjgl.BufferUtils.createFloatBuffer(w*h*4),optimized=org.lwjgl.BufferUtils.createFloatBuffer(w*h*4);GL11.glReadPixels(0,0,w,h,GL11.GL_RGBA,GL11.GL_FLOAT,baseline);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);GL11.glEnable(GL11.GL_SCISSOR_TEST);GlStateManager._glUseProgram(program);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);GL11.glReadPixels(0,0,w,h,GL11.GL_RGBA,GL11.GL_FLOAT,optimized);
            double max=0,sum=0;int active=0;for(int i=0;i<w*h*4;i++){double error=Math.abs((double)baseline.get(i)-optimized.get(i));if(!Double.isFinite(error))throw new AssertionError("Nonfinite pixel");max=Math.max(max,error);sum+=error*error;if(optimized.get(i)!=0)active++;}
            if(active==0){seen.remove(id);return;}
            double visibleError=0;for(int pixel=0;pixel<w*h;pixel++){float a=baseline.get(pixel*4+3),b=optimized.get(pixel*4+3);for(int channel=0;channel<3;channel++)visibleError=Math.max(visibleError,Math.abs(baseline.get(pixel*4+channel)*a-optimized.get(pixel*4+channel)*b)+Math.abs(a-b));}System.out.println("FURNACE_NEAR_QUALITY raw="+max+" composite="+visibleError);double visibleLimit=Double.parseDouble(System.getProperty("wuxingxiantu.perfVisibleErrorLimit","0.00392156862745098"));if(visibleError>visibleLimit)throw new AssertionError("Visible composite difference exceeds configured limit: "+visibleError);
            int opaqueBedPixels=0;
            if(Boolean.getBoolean("wuxingxiantu.verifyFireBed")){
                GlStateManager._glUseProgram(reference);
                GL20.glUniform1f(GL20.glGetUniformLocation(reference,"FireStrength"),0);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
                FloatBuffer bedPixels=org.lwjgl.BufferUtils.createFloatBuffer(w*h*4);GL11.glReadPixels(0,0,w,h,GL11.GL_RGBA,GL11.GL_FLOAT,bedPixels);
                for(int pixel=0;pixel<w*h;pixel++)if(bedPixels.get(pixel*4+3)>=.99999F)opaqueBedPixels++;
                if(opaqueBedPixels==0)throw new AssertionError("Charcoal support did not cover any background pixels with gas disabled");
                copyUniforms(program,reference);GlStateManager._glUseProgram(program);
            }
            HostAdapter.loaded(0);BaselineHostScan.loaded(0);double[] oldCpu=new double[100],newCpu=new double[100];
            for(int i=0;i<100;i++){long begin=System.nanoTime();var old=BaselineHostScan.loaded(0);oldCpu[i]=(System.nanoTime()-begin)/1e6;begin=System.nanoTime();var current=HostAdapter.loaded(0);newCpu[i]=(System.nanoTime()-begin)/1e6;if(old.size()!=current.size())throw new AssertionError("Furnace discovery mismatch");for(int j=0;j<old.size();j++)if(old.get(j).entity()!=current.get(j).entity())throw new AssertionError("Furnace order mismatch");}
            var row=new LinkedHashMap<String,Object>();row.put("fire",id);row.put("width",w);row.put("height",h);row.put("baselineWidth",net.minecraft.client.Minecraft.getInstance().getMainRenderTarget().width);row.put("baselineHeight",net.minecraft.client.Minecraft.getInstance().getMainRenderTarget().height);row.put("samplesPerVersion",24);row.put("baselineGpuMs",median(before));row.put("optimizedGpuMs",median(after));row.put("bottomSpecialization",true);row.put("cornerCacheEnabled",VolumeGpu.cornerCaching());row.put("cacheBuildGpuMs",FireCornerCache.buildGpuMs);row.put("cacheBuildCpuMs",FireCornerCache.buildCpuMs);row.put("baselineDiscoveryCpuMs",median(oldCpu));row.put("optimizedDiscoveryCpuMs",median(newCpu));row.put("maxFloatError",max);row.put("maxCompositeError",visibleError);row.put("visibleErrorLimit",visibleLimit);row.put("rmse",Math.sqrt(sum/(w*h*4)));row.put("activeChannels",active);row.put("glError",GL11.glGetError());rows.add(row);compositePending=true;
            row.put("noiseCacheEnabled",VolumeGpu.noiseCaching());if(Boolean.getBoolean("wuxingxiantu.verifyFireBed"))row.put("opaqueBedPixelsWithGasDisabled",opaqueBedPixels);
            row.put("timeSeconds",GL20.glGetUniformf(program,GL20.glGetUniformLocation(program,"Time")));
            if(Boolean.getBoolean("wuxingxiantu.verifyFireSeam")){
                float seam=densityAt(program,.38F),below=densityAt(program,.31F),roof=densityAt(program,.55F);
                if(!(seam>0&&below==0&&roof==0))throw new AssertionError("Fire must originate in measured seam: "+seam+" / "+below+" / "+roof);
                row.put("seamDensity",seam);row.put("belowPlinthDensity",below);row.put("solidRoofDensity",roof);
                float solid=densityAt(program,.38F,-1.7F);if(solid!=0)throw new AssertionError("Fire penetrated actual metal support: "+solid);row.put("solidSupportDensity",solid);
            }
            Files.createDirectories(OUTPUT);Files.writeString(OUTPUT.resolve("paired-gpu.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("renderer",GL11.glGetString(GL11.GL_RENDERER),"measurement","paired draw-only GL_TIME_ELAPSED: full-resolution baseline versus integration target; field RGBA32F comparison at identical rays; composite excluded","cases",rows)));
        }catch(Exception e){throw new AssertionError("Performance probe failed",e);}
        finally{GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GlStateManager._bindTexture(texture);GlStateManager._glUseProgram(program);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);if(blend)GL11.glEnable(GL11.GL_BLEND);else GL11.glDisable(GL11.GL_BLEND);GL11.glClearColor(clear[0],clear[1],clear[2],clear[3]);if(q!=0)GL15.glDeleteQueries(q);if(fbo!=0)GL30.glDeleteFramebuffers(fbo);if(color!=0)GL11.glDeleteTextures(color);}
    }
}
