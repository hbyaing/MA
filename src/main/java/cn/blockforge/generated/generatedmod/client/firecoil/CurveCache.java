package cn.blockforge.generated.generatedmod.client.firecoil;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Per-draw float32 peel/tongue geometry, using the production GPU formulas. */
final class CurveCache {
    static final int UNIT=3,WIDTH=66,HEIGHT=1;
    private static int program,vao,texture,fbo;
    private static final Map<String,Integer> UNIFORMS=new HashMap<>();
    private static Key previous;
    static String analyticSha256="";
    private record Key(float time,float radius,float height,float turns,float width,float wave,float rotation,float rise,float waveSpeed,float tongueLength,
                       float weaponRadius,float weaponWidth,float weaponTurns,float bladeLength,float bladeRadius,int strands,int seed,boolean body) {}
    private static String read(String name)throws Exception {
        try(var in=CurveCache.class.getResourceAsStream("/assets/wuxingxiantu/firecoil/shaders/"+name)) {
            if(in==null)throw new IllegalStateException("Missing curve cache resource: "+name);
            return new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
    private static int shader(int type,String source) {
        int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);
        if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0){String e=GL20.glGetShaderInfoLog(s);GL20.glDeleteShader(s);throw new IllegalStateException(e);}return s;
    }
    /** Copy the actual production hash instead of approximating it on the CPU. */
    private static String function(String source,String signature) {
        int first=source.indexOf(signature),brace=first<0?-1:source.indexOf('{',first);
        if(brace<0)throw new IllegalStateException("Production function missing: "+signature);
        int depth=1,at=brace+1;
        while(at<source.length()&&depth>0){char c=source.charAt(at++);if(c=='{')depth++;else if(c=='}')depth--;}
        if(depth!=0)throw new IllegalStateException("Incomplete production function: "+signature);
        return source.substring(first,at);
    }
    private static void init()throws Exception {
        if(program!=0)return;
        String source=read("volume.fsh"),begin="// FC_CENTER_ANALYTIC_BEGIN",end="// FC_CENTER_ANALYTIC_END";
        int first=source.indexOf(begin),last=source.indexOf(end);
        if(first<0||last<=first)throw new IllegalStateException("Production analytic curve markers missing");
        String analytic=source.substring(first+begin.length(),last).strip();
        analyticSha256=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(analytic.getBytes(StandardCharsets.UTF_8)));
        if(!analytic.contains("vec3 fvCenterAnalytic("))throw new IllegalStateException("Production analytic curve signature changed");
        String fragment=read("curve-cache-bake.fsh").replace("__FC_CENTER_ANALYTIC__",analytic)
                .replace("__FC_HASH__",function(source,"float fvHash(vec3 p)"));
        int p=GL20.glCreateProgram(),v=0,f=0;
        try {
            v=shader(GL20.GL_VERTEX_SHADER,"#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}");
            f=shader(GL20.GL_FRAGMENT_SHADER,fragment);GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);
            if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));
            vao=GL30.glGenVertexArrays();texture=GlStateManager._genTexture();GlStateManager._activeTexture(GL13.GL_TEXTURE0+UNIT);GlStateManager._bindTexture(texture);
            try(var unpack=new VolumeGpu.UnpackState()) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,WIDTH,HEIGHT,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
            fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete curve cache framebuffer");
            program=p;
        }catch(Exception e){GL20.glDeleteProgram(p);release();throw e;}
        finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}
    }
    static void prepareBody(Recipe r,double time)throws Exception{prepare(key(r,time,0,0,true));}
    /** Length and radius are the exact model-scaled float uniforms uploaded by VolumeGpu. */
    static void prepareWeapon(Recipe r,double time,float lengthMetres,float bladeRadiusMetres)throws Exception{prepare(key(r,time,lengthMetres,bladeRadiusMetres,false));}
    private static Key key(Recipe r,double time,float length,float radius,boolean body) {
        return new Key((float)time,(float)r.d("radius"),(float)r.d("height"),(float)r.d("turns"),(float)r.d("width"),(float)r.d("waveAmplitude"),
                (float)r.d("rotationSpeed"),(float)r.d("riseSpeed"),(float)r.d("waveSpeed"),(float)r.d("tongueLength"),
                (float)r.d("weaponRadius"),(float)r.d("weaponWidth"),(float)r.d("weaponTurns"),length,radius,r.i("strands"),r.i("seed"),body);
    }
    private static int loc(String name){return UNIFORMS.computeIfAbsent(name,k->GL20.glGetUniformLocation(program,k));}
    private static void scalar(String name,float value){GL20.glUniform1f(loc(name),value);}
    private static void prepare(Key next)throws Exception {
        if(next.equals(previous)&&program!=0)return;
        try(State state=new State()) {
            init();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);RenderSystem.viewport(0,0,WIDTH,HEIGHT);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);RenderSystem.disableBlend();RenderSystem.disableDepthTest();RenderSystem.disableCull();GL11.glDisable(GL11.GL_STENCIL_TEST);RenderSystem.colorMask(true,true,true,true);
            GlStateManager._glUseProgram(program);GL30.glBindVertexArray(vao);
            scalar("Time",next.time);scalar("Radius",next.radius);scalar("Height",next.height);scalar("Turns",next.turns);scalar("Width",next.width);scalar("WaveAmplitude",next.wave);
            scalar("RotationSpeed",next.rotation);scalar("RiseSpeed",next.rise);scalar("WaveSpeed",next.waveSpeed);scalar("TongueLength",next.tongueLength);
            scalar("WeaponRadius",next.weaponRadius);scalar("WeaponWidth",next.weaponWidth);scalar("WeaponTurns",next.weaponTurns);
            scalar("BladeLength",next.bladeLength);scalar("BladeRadius",next.bladeRadius);GL20.glUniform1i(loc("Strands"),next.strands);GL20.glUniform1i(loc("Seed"),next.seed);GL20.glUniform1i(loc("BodyEnabled"),next.body?1:0);
            GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);previous=next;
        }
    }
    /** Called with the production volume program active. Preserves the active texture selector. */
    static void bind(int consumerProgram) {
        if(texture==0)throw new IllegalStateException("Curve cache was not prepared");
        int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);GlStateManager._activeTexture(GL13.GL_TEXTURE0+UNIT);GlStateManager._bindTexture(texture);GlStateManager._activeTexture(active);
        GL20.glUniform1i(GL20.glGetUniformLocation(consumerProgram,"CurveCenters"),UNIT);
    }
    static void release() {
        if(program!=0)GL20.glDeleteProgram(program);if(vao!=0)GL30.glDeleteVertexArrays(vao);if(texture!=0)GlStateManager._deleteTexture(texture);if(fbo!=0)GL30.glDeleteFramebuffers(fbo);
        program=vao=texture=fbo=0;previous=null;UNIFORMS.clear();analyticSha256="";
    }
    private static final class State implements AutoCloseable {
        final int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),binding;
        final int[] viewport=new int[4];final boolean[] mask=new boolean[4];
        final boolean scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),stencil=GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        State(){GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);try(var stack=org.lwjgl.system.MemoryStack.stackPush()){var b=stack.malloc(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,b);for(int i=0;i<4;i++)mask[i]=b.get(i)!=0;}GlStateManager._activeTexture(GL13.GL_TEXTURE0+UNIT);binding=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);GlStateManager._activeTexture(active);}
        public void close(){GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GlStateManager._glUseProgram(program);GL30.glBindVertexArray(vao);RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            flag(GL11.GL_SCISSOR_TEST,scissor);flag(GL11.GL_STENCIL_TEST,stencil);RenderSystem.colorMask(mask[0],mask[1],mask[2],mask[3]);GlStateManager._activeTexture(GL13.GL_TEXTURE0+UNIT);GlStateManager._bindTexture(binding);GlStateManager._activeTexture(active);
        }
        private static void flag(int capability,boolean value){if(value)GL11.glEnable(capability);else GL11.glDisable(capability);}
    }
    private CurveCache(){}
}
