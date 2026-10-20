/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.opengl.GL11
 */
package cn.blockforge.generated.generatedmod.client.anvillight;

import cn.blockforge.generated.generatedmod.client.anvillight.Gpu;
import cn.blockforge.generated.generatedmod.client.anvillight.PassState;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;

final class Lighting {
    static TextureTarget snapshot;
    static ShaderInstance shader;
    private static Matrix4f pendingInverse;
    private static java.util.List<Vec3> pendingLights=java.util.List.of();

    Lighting() {
    }

    static void register(RegisterShadersEvent e) {
        Gpu.register(e, "anvil_blue_light", s -> {
            shader = s;
        });
    }

    static void release() {
        pendingInverse=null;pendingLights=java.util.List.of();
        if (snapshot != null) {
            snapshot.destroyBuffers();
        }
        snapshot = null;
    }

    private static Matrix4f safeInverseViewProjection(Matrix4fc projection, Matrix4fc pose) {
        if (projection == null || pose == null) return null;
        try {
            Matrix4f combined = new Matrix4f(projection).mul(pose);
            if (!combined.isFinite()) return null;
            float determinant = combined.determinant();
            if (!Float.isFinite(determinant) || Math.abs(determinant) < 1.0e-8f) return null;
            Matrix4f inverse = combined.invert();
            return inverse.isFinite() ? inverse : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static void render(RenderLevelStageEvent e, java.util.List<Vec3> lights) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS || shader == null || lights.isEmpty()
                || cn.blockforge.generated.generatedmod.client.TrailShaderBridge.shaderPackDetected()) {
            return;
        }
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (GL11.glGetInteger((int)36006) != main.frameBufferId || main.getDepthTextureId() < 0 || Minecraft.useShaderTransparency()) {
            return;
        }
        try (PassState state = new PassState();){
            Matrix4f inverse = safeInverseViewProjection(e.getProjectionMatrix(), e.getPoseStack().last().pose());
            if (inverse == null) return;
            if (snapshot == null || Lighting.snapshot.width != main.width || Lighting.snapshot.height != main.height) {
                Lighting.release();
                snapshot = Gpu.target(main.width, main.height, true);
            }
            Gpu.blit(main, (RenderTarget)snapshot, 16640);
            main.bindWrite(true);
            shader.safeGetUniform("InverseVP").set(inverse);
            int count=Math.min(4,lights.size());shader.safeGetUniform("LightCount").set(count);
            for(int i=0;i<4;i++) {
                Vec3 center=i<count?lights.get(i).subtract(e.getCamera().getPosition()):Vec3.ZERO;
                shader.safeGetUniform("LightRelative"+i).set((float)center.x,(float)center.y,(float)center.z);
            }
            shader.safeGetUniform("LightColor").set(0.015f, 0.3f, 1.0f);
            shader.safeGetUniform("Radius").set(4.5f);
            shader.safeGetUniform("Intensity").set(10.0f);
            shader.safeGetUniform("Texel").set(1.0f / (float)main.width, 1.0f / (float)main.height);
            Gpu.full(shader, snapshot.getColorTextureId(), snapshot.getDepthTextureId());
            Gpu.count();
        }
    }
    static void prepare(RenderLevelStageEvent e,java.util.List<Vec3> lights){
        pendingInverse=null;pendingLights=java.util.List.of();
        if(shader==null||lights.isEmpty()||!cn.blockforge.generated.generatedmod.client.TrailShaderBridge.shaderPackDetected()
                ||cn.blockforge.generated.generatedmod.client.TrailShaderBridge.shadowPass()
                ||!cn.blockforge.generated.generatedmod.client.TrailShaderBridge.captureDepth())return;
        pendingInverse=safeInverseViewProjection(e.getProjectionMatrix(), e.getPoseStack().last().pose());
        if(pendingInverse==null)return;
        var camera=e.getCamera().getPosition();pendingLights=lights.stream().limit(4).map(p->p.subtract(camera)).toList();
    }
    static void post(){
        if(pendingInverse==null)return;
        var inverse=pendingInverse;var lights=pendingLights;pendingInverse=null;pendingLights=java.util.List.of();
        int depth=cn.blockforge.generated.generatedmod.client.TrailShaderBridge.capturedDepthTexture();if(depth<0)return;
        var main=Minecraft.getInstance().getMainRenderTarget();
        try(PassState state=new PassState()){
            if(snapshot==null||snapshot.width!=main.width||snapshot.height!=main.height){release();snapshot=Gpu.target(main.width,main.height,true);}
            // Shader packs own their world FBO. At HUD time, sample the final
            // color with the matching world-depth snapshot captured earlier.
            Gpu.blit(main,snapshot,GL11.GL_COLOR_BUFFER_BIT);main.bindWrite(true);
            shader.safeGetUniform("InverseVP").set(inverse);shader.safeGetUniform("LightCount").set(lights.size());
            for(int i=0;i<4;i++){var center=i<lights.size()?lights.get(i):Vec3.ZERO;shader.safeGetUniform("LightRelative"+i).set((float)center.x,(float)center.y,(float)center.z);}
            shader.safeGetUniform("LightColor").set(.015f,.3f,1f);shader.safeGetUniform("Radius").set(4.5f);shader.safeGetUniform("Intensity").set(10f);shader.safeGetUniform("Texel").set(1f/main.width,1f/main.height);
            Gpu.full(shader,snapshot.getColorTextureId(),depth);
        }
    }
}



