package cn.blockforge.generated.generatedmod.client.furnacevolume;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

/** Test-only actual-pass timing and independently isolated material-field probes. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FurnaceUpperProbe implements FurnaceFireVolume.DrawProbe {
    private static final FurnaceUpperProbe INSTANCE=new FurnaceUpperProbe();
    private final Set<String> seen=new HashSet<>();
    private final List<Map<String,Object>> rows=new ArrayList<>();
    private final Map<Boolean,Integer> fields=new HashMap<>();
    private Map<String,Object> pending;
    private static final String[] STRENGTHS={"FaceHeatStrength","FaceSparkRate","AromaStrength","IngredientStrength","EnergyRingStrength","VaporStrength","CoreStrength","FilamentStrength","BurstStrength","SigilStrength"};
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.START&&Boolean.getBoolean("wuxingxiantu.furnaceUpperCapture"))FurnaceFireVolume.drawProbe=INSTANCE;}
    @Override public void draw(int program,int vao,int w,int h,String id){measure(program,w,h,id,false);}
    @Override public void drawUpper(int program,int vao,int w,int h,String id){measure(program,w,h,id,true);}
    private static int shader(int type,String source){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0)throw new AssertionError(GL20.glGetShaderInfoLog(s));return s;}
    private int field(boolean cached)throws Exception{
        if(fields.containsKey(cached))return fields.get(cached);
        String source;
        try(var in=FurnaceUpperProbe.class.getResourceAsStream("/assets/wuxingxiantu/shaders/furnacevolume/volume.fsh")){source=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
        source=source.substring(0,source.indexOf("// SHARED_VOLUME_CORE_END"));
        source=source.replace("#version 150","#version 150\n#define WUXING_UPPER_ALCHEMY 1"+(cached?"\n#define WUXING_NOISE_CACHE 1":""));
        source+="""
            uniform int ProbeLayer;
            void main(){
                int index=int(gl_FragCoord.x),face=index/64,k=index%64;
                float a=.78539816339+float(face)*1.57079632679;
                vec2 normal=vec2(cos(a),sin(a)),tangent=vec2(-normal.y,normal.x);
                vec3 p;
                if(ProbeLayer<3){
                    float across=(float(k%8)-3.5)*.075,y=1.25+float(k/8)*.18;
                    float outwards=ProbeLayer==2?.35:.13;
                    p=vec3(normal.x*(2.085+outwards)+tangent.x*across,y,normal.y*(2.085+outwards)+tangent.y*across);
                    if(ProbeLayer==1){
                        int count=max(1,int(ceil(FaceSparkRate*FaceSparkLife))),j=k%count;
                        float seed=float(Seed%997)*.071,clock=(Time+float(j)/max(FaceSparkRate,.001))/FaceSparkLife;
                        float age=fract(clock)*FaceSparkLife,random=avHash(vec3(float(j)+float(face)*31.,seed,floor(clock)));
                        float cx=(random-.5)*.54+sin(age*4.7+random*19.)*.035*Turbulence;
                        float cy=-.17+random*.31+age*(.32+random*.52)-.08*age*age;
                        float cz=.025+age*FaceSparkSpeed*(.55+random*.7);
                        p=vec3(normal.x*(2.085+cz)+tangent.x*cx,1.393125+cy,normal.y*(2.085+cz)+tangent.y*cx);
                    }
                }else if(ProbeLayer==3){
                    float angle=float(index)*.17,radius=.05+float(k%8)*.10;
                    p=vec3(cos(angle)*radius,1.18+float(k/8)*.08,sin(angle)*radius);
                }else if(ProbeLayer==4){
                    float angle=float(index)*6.2831853/256.;
                    p=vec3(cos(angle)*EnergyRingRadius,EnergyRingHeight+.16*sin(angle*2.),sin(angle)*EnergyRingRadius);
                }else if(ProbeLayer==5){
                    float angle=float(index)*.31,radius=float(k%8)*.045;
                    p=vec3(cos(angle)*radius,3.30+float(k/8)*.20,sin(angle)*radius);
                }else{
                    float gather=Stage==2?smoothstep(0.,1.,Phase):0.;
                    p=AV_MATERIAL+vec3(0,gather*.28,0);
                }
                result=sampleAlchemy(p,Time);
            }
            """;
        int v=shader(GL20.GL_VERTEX_SHADER,"#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}"),f=shader(GL20.GL_FRAGMENT_SHADER,source),program=GL20.glCreateProgram();
        GL20.glAttachShader(program,v);GL20.glAttachShader(program,f);GL30.glBindFragDataLocation(program,0,"result");GL20.glLinkProgram(program);GL20.glDeleteShader(v);GL20.glDeleteShader(f);
        if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new AssertionError(GL20.glGetProgramInfoLog(program));fields.put(cached,program);return program;
    }
    private void measure(int program,int w,int h,String id,boolean upper){
        pending=null;int stage=GL20.glGetUniformi(program,GL20.glGetUniformLocation(program,"Stage"));String key=id+"/"+stage+"/"+upper;if(!seen.add(key))return;
        int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),fbo=0,color=0,query=0;
        int[] box=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,box);boolean blend=GL11.glIsEnabled(GL11.GL_BLEND);
        try{
            color=GlStateManager._genTexture();GlStateManager._bindTexture(color);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,Math.max(w,256),h,0,GL11.GL_RGBA,GL11.GL_FLOAT,(ByteBuffer)null);
            fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,color,0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new AssertionError("Upper QA target incomplete");GlStateManager._bindTexture(texture);GL11.glDisable(GL11.GL_BLEND);query=GL15.glGenQueries();
            double[] times=new double[12];for(int i=-3;i<12;i++){GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,query);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);GL15.glEndQuery(GL33.GL_TIME_ELAPSED);double ms=GL33.glGetQueryObjectui64(query,GL15.GL_QUERY_RESULT)/1e6;if(i>=0)times[i]=ms;}Arrays.sort(times);
            var row=new LinkedHashMap<String,Object>();row.put("fire",id);row.put("stage",stage);row.put("upper",upper);row.put("gpuMs",(times[5]+times[6])*.5);row.put("width",w);row.put("height",h);
            if(upper){
                int field=field(VolumeGpu.noiseCaching());var probes=new LinkedHashMap<String,Object>();
                for(int layer=0;layer<7;layer++){
                    VolumePerformanceProbe.copyUniforms(program,field);
                    for(String strength:STRENGTHS)GL20.glUniform1f(GL20.glGetUniformLocation(field,strength),0);
                    String enabled=STRENGTHS[layer];GL20.glUniform1f(GL20.glGetUniformLocation(field,enabled),GL20.glGetUniformf(program,GL20.glGetUniformLocation(program,enabled)));
                    GL20.glUniform1i(GL20.glGetUniformLocation(field,"ProbeLayer"),layer);GL11.glViewport(0,0,256,1);GL11.glScissor(0,0,256,1);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
                    FloatBuffer pixels=org.lwjgl.BufferUtils.createFloatBuffer(256*4);GL11.glReadPixels(0,0,256,1,GL11.GL_RGBA,GL11.GL_FLOAT,pixels);
                    double[] maximum=new double[layer<3?4:1];for(int pixel=0;pixel<256;pixel++){float d=pixels.get(pixel*4+3);if(!Float.isFinite(d))throw new AssertionError("Nonfinite upper field");maximum[layer<3?pixel/64:0]=Math.max(maximum[layer<3?pixel/64:0],d);}
                    boolean expected=layer<3||layer==5||((layer==3||layer==4)&&stage>0)||layer==6&&stage==2;
                    if(expected)for(double max:maximum)if(max<=0)throw new AssertionError("Missing "+enabled+" stage="+stage+"/"+Arrays.toString(maximum));probes.put(enabled,maximum);
                }
                row.put("isolatedDensities",probes);
                for(String strength:STRENGTHS)GL20.glUniform1f(GL20.glGetUniformLocation(field,strength),0);
                GL20.glUniform1i(GL20.glGetUniformLocation(field,"ProbeLayer"),3);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
                FloatBuffer disabled=org.lwjgl.BufferUtils.createFloatBuffer(256*4);GL11.glReadPixels(0,0,256,1,GL11.GL_RGBA,GL11.GL_FLOAT,disabled);
                for(int i=0;i<256*4;i++)if(disabled.get(i)!=0)throw new AssertionError("Disabled upper effects still emit");
                row.put("allStrengthsDisabledZero",true);
                row.put("ingredientCount",GL20.glGetUniformf(program,GL20.glGetUniformLocation(program,"IngredientCount")));
            }
            row.put("glError",GL11.glGetError());if(((Number)row.get("glError")).intValue()!=0)throw new AssertionError("Upper probe GL error");rows.add(row);pending=row;write();
        }catch(Exception failure){throw new AssertionError("Upper QA failed",failure);}
        finally{GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GlStateManager._bindTexture(texture);GlStateManager._glUseProgram(program);GL11.glViewport(0,0,w,h);GL11.glScissor(box[0],box[1],box[2],box[3]);if(blend)GL11.glEnable(GL11.GL_BLEND);else GL11.glDisable(GL11.GL_BLEND);if(query!=0)GL15.glDeleteQueries(query);if(fbo!=0)GL30.glDeleteFramebuffers(fbo);if(color!=0)GlStateManager._deleteTexture(color);}
    }
    @Override public void composite(Runnable draw){if(pending==null){draw.run();return;}int q=GL15.glGenQueries();try{GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,q);draw.run();GL15.glEndQuery(GL33.GL_TIME_ELAPSED);double ms=GL33.glGetQueryObjectui64(q,GL15.GL_QUERY_RESULT)/1e6;pending.put("compositeGpuMs",ms);pending.put("totalGpuMs",((Number)pending.get("gpuMs")).doubleValue()+ms);write();}catch(Exception e){throw new AssertionError(e);}finally{pending=null;GL15.glDeleteQueries(q);}}
    private void write()throws Exception{Path output=Path.of(System.getProperty("wuxingxiantu.guardGlintOutput"));Files.createDirectories(output);Files.writeString(output.resolve("upper-gpu.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("renderer",GL11.glGetString(GL11.GL_RENDERER),"cases",rows)));}
}
