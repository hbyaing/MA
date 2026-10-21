/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferBuilder$RenderedBuffer
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL30
 */
package cn.blockforge.generated.generatedmod.client.anvillight;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class Gpu {
    Gpu() {
    }

    static void register(RegisterShadersEvent e, String name, Consumer<ShaderInstance> callback) {
        try {
            e.registerShader(new ShaderInstance(e.getResourceProvider(), new ResourceLocation("wuxingxiantu", name), DefaultVertexFormat.POSITION_TEX), callback);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static TextureTarget target(int w, int h, boolean depth) {
        TextureTarget t = new TextureTarget(Math.max(1, w), Math.max(1, h), depth, Minecraft.ON_OSX);
        t.setFilterMode(9729);
        return t;
    }

    static void clear(TextureTarget t) {
        GL11.glDisable((int)3089);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.depthMask((boolean)true);
        t.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        t.clear(Minecraft.ON_OSX);
        t.bindWrite(true);
        if (GL30.glCheckFramebufferStatus((int)36009) != 36053) {
            throw new IllegalStateException("Incomplete target");
        }
    }

    static void blit(RenderTarget from, RenderTarget to, int bits) {
        GL30.glBindFramebuffer((int)36008, (int)from.frameBufferId);
        GL30.glBindFramebuffer((int)36009, (int)to.frameBufferId);
        GL30.glBlitFramebuffer((int)0, (int)0, (int)from.width, (int)from.height, (int)0, (int)0, (int)to.width, (int)to.height, (int)bits, (int)9728);
    }

    static void full(ShaderInstance s, int tex0, int tex1) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.setShader(() -> s);
        RenderSystem.setShaderTexture((int)0, (int)tex0);
        RenderSystem.setShaderTexture((int)1, (int)tex1);
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1.0, -1.0, 0.0).uv(0.0f, 0.0f).endVertex();
        b.vertex(3.0, -1.0, 0.0).uv(2.0f, 0.0f).endVertex();
        b.vertex(-1.0, 3.0, 0.0).uv(0.0f, 2.0f).endVertex();
        BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)b.end());
    }

    static void count() {
    }
}

