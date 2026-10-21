/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.renderer.ShaderInstance
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL30
 */
package cn.blockforge.generated.generatedmod.client.anvillight;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class PassState
implements AutoCloseable {
    final int draw = GL11.glGetInteger((int)36006);
    final int read = GL11.glGetInteger((int)36010);
    final int active = GL11.glGetInteger((int)34016);
    final int[] vp = new int[4];
    final int[] bound = new int[2];
    final int[] shaderTex = new int[2];
    final float[] mask = new float[4];
    final float[] color = (float[])RenderSystem.getShaderColor().clone();
    final int func = GL11.glGetInteger((int)2932);
    final int sr = GL11.glGetInteger((int)32969);
    final int dr = GL11.glGetInteger((int)32968);
    final int sa = GL11.glGetInteger((int)32971);
    final int da = GL11.glGetInteger((int)32970);
    final boolean depth = GL11.glIsEnabled((int)2929);
    final boolean write = GL11.glGetBoolean((int)2930);
    final boolean blend = GL11.glIsEnabled((int)3042);
    final boolean cull = GL11.glIsEnabled((int)2884);
    final boolean scissor = GL11.glIsEnabled((int)3089);
    final ShaderInstance previous = RenderSystem.getShader();

    PassState() {
        GL11.glGetIntegerv((int)2978, (int[])this.vp);
        GL11.glGetFloatv((int)3107, (float[])this.mask);
        for (int i = 0; i < 2; ++i) {
            this.shaderTex[i] = RenderSystem.getShaderTexture((int)i);
            GlStateManager._activeTexture((int)(33984 + i));
            this.bound[i] = GL11.glGetInteger((int)32873);
        }
        GlStateManager._activeTexture((int)this.active);
    }

    @Override
    public void close() {
        GL30.glBindFramebuffer((int)36009, (int)this.draw);
        GL30.glBindFramebuffer((int)36008, (int)this.read);
        RenderSystem.viewport((int)this.vp[0], (int)this.vp[1], (int)this.vp[2], (int)this.vp[3]);
        for (int i = 0; i < 2; ++i) {
            RenderSystem.setShaderTexture((int)i, (int)this.shaderTex[i]);
            GlStateManager._activeTexture((int)(33984 + i));
            GlStateManager._bindTexture((int)this.bound[i]);
        }
        GlStateManager._activeTexture((int)this.active);
        RenderSystem.setShader(() -> this.previous);
        RenderSystem.setShaderColor((float)this.color[0], (float)this.color[1], (float)this.color[2], (float)this.color[3]);
        if (this.depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask((boolean)this.write);
        RenderSystem.depthFunc((int)this.func);
        RenderSystem.blendFuncSeparate((int)this.sr, (int)this.dr, (int)this.sa, (int)this.da);
        if (this.blend) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
        if (this.cull) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }
        if (this.scissor) {
            GL11.glEnable((int)3089);
        } else {
            GL11.glDisable((int)3089);
        }
        RenderSystem.colorMask(((double)this.mask[0] > 0.5 ? 1 : 0) != 0, ((double)this.mask[1] > 0.5 ? 1 : 0) != 0, ((double)this.mask[2] > 0.5 ? 1 : 0) != 0, ((double)this.mask[3] > 0.5 ? 1 : 0) != 0);
    }
}

