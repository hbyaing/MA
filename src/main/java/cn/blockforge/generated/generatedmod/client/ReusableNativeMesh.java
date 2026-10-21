package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;

/** Owns both MemoryTracker's malloc and the GPU mesh. Never free a shared Tesselator buffer. */
public final class ReusableNativeMesh implements AutoCloseable {
    private static final java.lang.reflect.Field STORAGE=ObfuscationReflectionHelper.findField(BufferBuilder.class,"f_85648_");
    private BufferBuilder builder;
    private VertexBuffer gpu;
    private final int initial;
    public int allocations,releases,uploads;
    public ReusableNativeMesh(int initial){this.initial=initial;}
    public BufferBuilder buffer(){RenderSystem.assertOnRenderThread();if(builder==null){builder=new BufferBuilder(initial);allocations++;}return builder;}
    public BufferBuilder begin(VertexFormat.Mode mode,VertexFormat format){
        var b=buffer();b.begin(mode,format);return b;
    }
    public long bytes(){if(builder==null)return 0;try{return ((ByteBuffer)STORAGE.get(builder)).capacity();}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
    public void draw(ShaderInstance shader){
        RenderSystem.assertOnRenderThread();if(gpu==null)gpu=new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        var mesh=builder.end();int vao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),array=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        try{gpu.bind();gpu.upload(mesh);uploads++;shader.apply();try{gpu.draw();}finally{shader.clear();}}
        finally{GL30.glBindVertexArray(vao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,array);}
    }
    @Override public void close(){
        RenderSystem.assertOnRenderThread();if(gpu!=null){gpu.close();gpu=null;}
        if(builder==null)return;
        if(builder.building())builder.end().release();builder.discard();
        try{var memory=(ByteBuffer)STORAGE.get(builder);memory.clear();STORAGE.set(builder,null);MemoryUtil.getAllocator(false).free(MemoryUtil.memAddress(memory));builder=null;releases++;}
        catch(IllegalAccessException e){throw new IllegalStateException("Cannot release owned native mesh",e);}
    }
}
