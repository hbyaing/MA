package cn.blockforge.generated.generatedmod.client.furnacevolume;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.util.*;
/** Pack all eight hashes of a lattice cell into two exact RGBA32F fetches. */
final class FireCornerCache{
    static final class Entry{int low,high;final int[] origin=new int[3];boolean ready;}
    private static final Map<Recipe,Entry> ENTRIES=new IdentityHashMap<>();private static int program;private static boolean unavailable;
    static double buildGpuMs,buildCpuMs;
    private static Entry noise;
    private static boolean noiseUnavailable;
    private static final String SOURCE="""
        #version 430
        layout(local_size_x=8,local_size_y=8,local_size_z=8)in;
        layout(rgba32f,binding=0)writeonly uniform image3D Low;
        layout(rgba32f,binding=1)writeonly uniform image3D High;
        uniform ivec3 Origin;uniform int Periodic;
        float avHash(vec3 p){if(Periodic!=0)p=mod(p,64.);p=fract(p*vec3(.1031,.1030,.0973));p+=dot(p,p.yxz+33.33);return fract((p.x+p.y)*p.z);}
        void main(){ivec3 cell=ivec3(gl_GlobalInvocationID);vec3 i=vec3(cell+Origin);
         float a=avHash(i),b=avHash(i+vec3(1,0,0)),c=avHash(i+vec3(0,1,0)),d=avHash(i+vec3(1,1,0));
         float e=avHash(i+vec3(0,0,1)),g=avHash(i+vec3(0,1,1)),h=avHash(i+vec3(1,1,1)),j=avHash(i+vec3(1,0,1));
         imageStore(Low,cell,vec4(a,b,c,d));imageStore(High,cell,vec4(e,j,g,h));}
        """;
    private static void init(){if(program!=0)return;int shader=GL20.glCreateShader(GL43.GL_COMPUTE_SHADER),candidate=0;try{GL20.glShaderSource(shader,SOURCE);GL20.glCompileShader(shader);if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0)throw new IllegalStateException(GL20.glGetShaderInfoLog(shader));candidate=GL20.glCreateProgram();GL20.glAttachShader(candidate,shader);GL20.glLinkProgram(candidate);if(GL20.glGetProgrami(candidate,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(candidate));program=candidate;candidate=0;}finally{GL20.glDeleteShader(shader);if(candidate!=0)GL20.glDeleteProgram(candidate);}}
    private static int texture(){int t=GL11.glGenTextures();GL11.glBindTexture(GL12.GL_TEXTURE_3D,t);GL12.glTexImage3D(GL12.GL_TEXTURE_3D,0,GL30.GL_RGBA32F,64,64,96,0,GL11.GL_RGBA,GL11.GL_FLOAT,(ByteBuffer)null);int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR){GL11.glDeleteTextures(t);throw new IllegalStateException("Fire corner-cache allocation GL error 0x"+Integer.toHexString(error));}GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);return t;}
    static Entry prepare(Recipe recipe,float time){
        if(unavailable||!recipe.bottomOnly()||!GL.getCapabilities().OpenGL43)return null;
        float seed=(recipe.i("seed")%997)*.071f,rise=time*(float)recipe.d("flowSpeed");float[] shift={seed+rise*.13f,seed-rise*.21f,seed+rise*.17f};for(float x:shift)if(!Float.isFinite(x)||Math.abs(x)>8_000_000)return null;
        int[] origin={(int)Math.floor(shift[0])-16,(int)Math.floor(shift[1])-16,(int)Math.floor(shift[2])-16};var entry=ENTRIES.computeIfAbsent(recipe,r->new Entry());
        boolean covered=entry.ready;for(int i=0;i<3;i++)covered&=Math.abs(origin[i]-entry.origin[i])<=4;
        if(!covered){long start=System.nanoTime();int oldProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),query=0;int[][] image=new int[2][6];int[] keys={GL42.GL_IMAGE_BINDING_NAME,GL42.GL_IMAGE_BINDING_LEVEL,GL42.GL_IMAGE_BINDING_LAYERED,GL42.GL_IMAGE_BINDING_LAYER,GL42.GL_IMAGE_BINDING_ACCESS,GL42.GL_IMAGE_BINDING_FORMAT};for(int n=0;n<2;n++)for(int k=0;k<6;k++)image[n][k]=GL30.glGetIntegeri(keys[k],n);
            try{init();GlStateManager._activeTexture(GL13.GL_TEXTURE1);if(entry.low==0)entry.low=texture();if(entry.high==0)entry.high=texture();
                GlStateManager._glUseProgram(program);GL20.glUniform1i(GL20.glGetUniformLocation(program,"Periodic"),0);GL20.glUniform3i(GL20.glGetUniformLocation(program,"Origin"),origin[0],origin[1],origin[2]);GL42.glBindImageTexture(0,entry.low,0,true,0,GL15.GL_WRITE_ONLY,GL30.GL_RGBA32F);GL42.glBindImageTexture(1,entry.high,0,true,0,GL15.GL_WRITE_ONLY,GL30.GL_RGBA32F);
                if(FurnaceFireVolume.drawProbe!=null){query=GL15.glGenQueries();GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,query);}GL43.glDispatchCompute(8,8,12);GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT|GL42.GL_TEXTURE_FETCH_BARRIER_BIT);if(query!=0){GL15.glEndQuery(GL33.GL_TIME_ELAPSED);buildGpuMs=GL33.glGetQueryObjectui64(query,GL15.GL_QUERY_RESULT)/1e6;}
                int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR)throw new IllegalStateException("Fire corner-cache build GL error 0x"+Integer.toHexString(error));System.arraycopy(origin,0,entry.origin,0,3);entry.ready=true;
            }catch(RuntimeException error){unavailable=true;release();com.mojang.logging.LogUtils.getLogger().warn("Fire corner cache unavailable; keep original full precision field",error);return null;}
            finally{if(query!=0)GL15.glDeleteQueries(query);for(int n=0;n<2;n++)GL42.glBindImageTexture(n,image[n][0],image[n][1],image[n][2]!=0,image[n][3],image[n][4],image[n][5]);GlStateManager._glUseProgram(oldProgram);buildCpuMs=(System.nanoTime()-start)/1e6;}
        }
        GlStateManager._activeTexture(GL13.GL_TEXTURE1);GL11.glBindTexture(GL12.GL_TEXTURE_3D,entry.low);GlStateManager._activeTexture(GL13.GL_TEXTURE2);GL11.glBindTexture(GL12.GL_TEXTURE_3D,entry.high);return entry;
    }
    // A failed allocation stays disabled until client restart. Render/resource
    // reset can release VRAM, but must not create a per-frame allocation retry loop.
    static void release(){for(var e:ENTRIES.values()){if(e.low!=0)GL11.glDeleteTextures(e.low);if(e.high!=0)GL11.glDeleteTextures(e.high);}ENTRIES.clear();if(noise!=null){if(noise.low!=0)GL11.glDeleteTextures(noise.low);if(noise.high!=0)GL11.glDeleteTextures(noise.high);noise=null;}if(program!=0)GL20.glDeleteProgram(program);program=0;}
    /** Shared finite lattice. Store original corner hashes, retaining quintic interpolation and all FBM octaves. */
    static Entry prepareNoise(){
        if(unavailable||noiseUnavailable||!GL.getCapabilities().OpenGL43)return null;
        if(noise==null){
            int oldProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);int[][] image=new int[2][6];
            int[] keys={GL42.GL_IMAGE_BINDING_NAME,GL42.GL_IMAGE_BINDING_LEVEL,GL42.GL_IMAGE_BINDING_LAYERED,GL42.GL_IMAGE_BINDING_LAYER,GL42.GL_IMAGE_BINDING_ACCESS,GL42.GL_IMAGE_BINDING_FORMAT};
            for(int n=0;n<2;n++)for(int k=0;k<6;k++)image[n][k]=GL30.glGetIntegeri(keys[k],n);
            Entry candidate=new Entry();
            try{
                init();GlStateManager._activeTexture(GL13.GL_TEXTURE3);candidate.low=noiseTexture();
                GlStateManager._activeTexture(GL13.GL_TEXTURE4);candidate.high=noiseTexture();
                GlStateManager._glUseProgram(program);GL20.glUniform1i(GL20.glGetUniformLocation(program,"Periodic"),1);
                GL20.glUniform3i(GL20.glGetUniformLocation(program,"Origin"),0,0,0);
                GL42.glBindImageTexture(0,candidate.low,0,true,0,GL15.GL_WRITE_ONLY,GL30.GL_RGBA32F);
                GL42.glBindImageTexture(1,candidate.high,0,true,0,GL15.GL_WRITE_ONLY,GL30.GL_RGBA32F);
                GL43.glDispatchCompute(8,8,8);GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT|GL42.GL_TEXTURE_FETCH_BARRIER_BIT);
                int error=GL11.glGetError();if(error!=0)throw new IllegalStateException("Shared fire noise cache GL error "+error);
                noise=candidate;
            }catch(RuntimeException error){
                noiseUnavailable=true;if(candidate.low!=0)GL11.glDeleteTextures(candidate.low);if(candidate.high!=0)GL11.glDeleteTextures(candidate.high);
                com.mojang.logging.LogUtils.getLogger().warn("Shared fire noise unavailable; retain full formula",error);return null;
            }finally{
                for(int n=0;n<2;n++)GL42.glBindImageTexture(n,image[n][0],image[n][1],image[n][2]!=0,image[n][3],image[n][4],image[n][5]);GlStateManager._glUseProgram(oldProgram);
            }
        }
        GlStateManager._activeTexture(GL13.GL_TEXTURE3);GL11.glBindTexture(GL12.GL_TEXTURE_3D,noise.low);
        GlStateManager._activeTexture(GL13.GL_TEXTURE4);GL11.glBindTexture(GL12.GL_TEXTURE_3D,noise.high);return noise;
    }
    private static int noiseTexture(){
        int texture=GL11.glGenTextures();GL11.glBindTexture(GL12.GL_TEXTURE_3D,texture);
        GL12.glTexImage3D(GL12.GL_TEXTURE_3D,0,GL30.GL_RGBA32F,64,64,64,0,GL11.GL_RGBA,GL11.GL_FLOAT,(ByteBuffer)null);
        int error=GL11.glGetError();if(error!=0){GL11.glDeleteTextures(texture);throw new IllegalStateException("Shared fire noise allocation GL error "+error);}
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);return texture;
    }
}
