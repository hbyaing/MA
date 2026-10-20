package cn.blockforge.generated.generatedmod.client.furnacevolume;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.*;

/** One-third resolution integration with premultiplied, depth-aware reconstruction. */
final class FireComposite {
    private static int texture,fbo,program,width,height;
    private static final String VERTEX="#version 150\nvoid main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(p*2.-1.,0,1);}";
    private static final String FRAGMENT="""
        #version 150
        uniform sampler2D Fire,SceneDepth;
        uniform vec2 Viewport,FireSize;
        uniform mat4 InverseLocalProjection;
        out vec4 result;
        vec3 point(vec2 uv,float depth){vec4 p=InverseLocalProjection*vec4(uv*2.-1.,depth*2.-1.,1.);return p.xyz/p.w;}
        void main(){
            vec2 uv=gl_FragCoord.xy/Viewport;
            float depth=texture(SceneDepth,uv).r;
            vec3 surface=point(uv,depth);
            vec4 eye=InverseLocalProjection*vec4(0.,0.,1.,0.);
            vec3 origin=eye.xyz/eye.w;
            float distanceToSurface=depth>=.999999?0.:length(surface-origin);
            vec2 grid=uv*FireSize-.5,base=floor(grid),fraction=fract(grid);
            vec4 sum=vec4(0);float total=0.;
            for(int y=0;y<2;y++)for(int x=0;x<2;x++){
                vec2 pixel=clamp(base+vec2(x,y),vec2(0),FireSize-1.);
                vec2 tap=(pixel+.5)/FireSize;
                float other=texture(SceneDepth,tap).r;
                // Refuse to spread fire across a foreground/background edge.
                float agreement;
                if(depth>=.999999||other>=.999999)agreement=depth>=.999999&&other>=.999999?1.:0.;
                else {float d=abs(length(point(tap,other)-origin)-distanceToSurface);agreement=1.-smoothstep(.025,.10,d);}
                vec2 weight=mix(1.-fraction,fraction,vec2(x,y));
                float w=weight.x*weight.y*agreement;
                vec4 fire=texelFetch(Fire,ivec2(pixel),0);
                sum+=vec4(fire.rgb*fire.a,fire.a)*w;total+=w;
            }
            if(total<.0001){result=vec4(0);return;}
            sum/=total;result=vec4(sum.rgb/max(sum.a,.00001),sum.a);
        }
        """;
    private static int shader(int type,String source){
        int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
        if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0){String error=GL20.glGetShaderInfoLog(shader);GL20.glDeleteShader(shader);throw new IllegalStateException(error);}return shader;
    }
    static void begin(int fullWidth,int fullHeight){
        int w=(fullWidth+2)/3,h=(fullHeight+2)/3;
        if(fbo==0||w!=width||h!=height){
            freeTarget();width=w;height=h;
            texture=GlStateManager._genTexture();GlStateManager._bindTexture(texture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA16F,w,h,0,GL11.GL_RGBA,GL11.GL_FLOAT,(java.nio.ByteBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
            fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);
            if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete fire integration framebuffer");
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);RenderSystem.viewport(0,0,width,height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL30.glClearBufferfv(GL11.GL_COLOR,0,new float[]{0,0,0,0});
    }
    static int width(){return width;}static int height(){return height;}
    static void composite(int main,int depth,int fullWidth,int fullHeight,float[] inverse){
        if(program==0){
            int v=shader(GL20.GL_VERTEX_SHADER,VERTEX),f=0,p=0;
            try{f=shader(GL20.GL_FRAGMENT_SHADER,FRAGMENT);p=GL20.glCreateProgram();GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);
                if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));program=p;p=0;
            }finally{GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);if(p!=0)GL20.glDeleteProgram(p);}
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main);RenderSystem.viewport(0,0,fullWidth,fullHeight);
        GlStateManager._glUseProgram(program);
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);GlStateManager._bindTexture(texture);
        GlStateManager._activeTexture(GL13.GL_TEXTURE6);GlStateManager._bindTexture(depth);
        GL20.glUniform1i(GL20.glGetUniformLocation(program,"Fire"),0);GL20.glUniform1i(GL20.glGetUniformLocation(program,"SceneDepth"),6);
        GL20.glUniform2f(GL20.glGetUniformLocation(program,"Viewport"),fullWidth,fullHeight);
        GL20.glUniform2f(GL20.glGetUniformLocation(program,"FireSize"),width,height);
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(program,"InverseLocalProjection"),false,inverse);
        RenderSystem.enableBlend();
        if(FurnaceFireVolume.drawProbe==null)GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
        else FurnaceFireVolume.drawProbe.composite(()->GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3));
    }
    private static void freeTarget(){if(texture!=0)GlStateManager._deleteTexture(texture);if(fbo!=0)GL30.glDeleteFramebuffers(fbo);texture=fbo=0;}
    static void release(){freeTarget();if(program!=0)GL20.glDeleteProgram(program);program=width=height=0;}
    private FireComposite(){}
}
