package cn.blockforge.generated.generatedmod.client.furnacevolume;

import org.lwjgl.opengl.*;
import com.mojang.blaze3d.platform.GlStateManager;

/** Static authored-model clearance, shared by every furnace. No per-frame voxel bake. */
final class FurnaceSurfaceClearance {
    private static int texture;
    static void bind()throws java.io.IOException {
        GlStateManager._activeTexture(GL13.GL_TEXTURE5);
        if(texture==0){
            byte[] data;try(var stream=FurnaceSurfaceClearance.class.getResourceAsStream("/assets/wuxingxiantu/shaders/furnacevolume/surface-clearance.r16f")){
                if(stream==null)throw new java.io.IOException("Missing furnace surface clearance");data=stream.readAllBytes();
            }
            if(data.length!=128*128*128*2)throw new java.io.IOException("Invalid furnace clearance lattice");
            int candidate=GL11.glGenTextures();try{
                GL11.glBindTexture(GL12.GL_TEXTURE_3D,candidate);
                var buffer=org.lwjgl.BufferUtils.createByteBuffer(data.length);buffer.put(data).flip();
                GL12.glTexImage3D(GL12.GL_TEXTURE_3D,0,GL30.GL_R16F,128,128,128,0,GL11.GL_RED,GL30.GL_HALF_FLOAT,buffer);
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL12.GL_TEXTURE_3D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                for(int axis:new int[]{GL11.GL_TEXTURE_WRAP_S,GL11.GL_TEXTURE_WRAP_T,GL12.GL_TEXTURE_WRAP_R})GL11.glTexParameteri(GL12.GL_TEXTURE_3D,axis,GL12.GL_CLAMP_TO_EDGE);
                int error=GL11.glGetError();if(error!=0)throw new java.io.IOException("Furnace clearance upload GL error "+error);
                texture=candidate;candidate=0;
            }finally{if(candidate!=0)GL11.glDeleteTextures(candidate);}
        }
        GL11.glBindTexture(GL12.GL_TEXTURE_3D,texture);
    }
    static void release(){if(texture!=0)GL11.glDeleteTextures(texture);texture=0;}
    private FurnaceSurfaceClearance(){}
}
