package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/** Independent first-person pass. Existing two-quad animation and texture parameters are retained. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ExoticBurnOverlay {
    static long renderedFrames;
    @SubscribeEvent public static void gui(RenderGuiEvent.Pre event) {event.getGuiGraphics().flush();render();}
    @SubscribeEvent public static void hiddenHud(RenderLevelStageEvent event) {
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL&&Minecraft.getInstance().options.hideGui)render();
    }
    private static void render() {
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||!mc.options.getCameraType().isFirstPerson()||mc.player.isSpectator())return;
        var burn=ExoticBurnClientState.state(mc.player);if(burn==null)return;
        var first=ExoticBurnRenderer.sprite(burn.sourceFireId(),false);
        var second=ExoticBurnRenderer.sprite(burn.sourceFireId(),true);
        if(first==null||second==null)return;
        long time=mc.level.getGameTime();
        var frame=((time/7)&1)==0?first:second;
        // Own projection and identity view; the pass is unrelated to the vanilla burning screen effect.
        RenderSystem.backupProjectionMatrix();
        var view=RenderSystem.getModelViewStack();view.pushPose();view.setIdentity();RenderSystem.applyModelViewMatrix();
        try {
            RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),
                    (float)mc.getWindow().getWidth()/mc.getWindow().getHeight(),.05F,10F),VertexSorting.DISTANCE_TO_ORIGIN);
            drawFire(new PoseStack(),frame,time+mc.getPartialTick());
            renderedFrames++;
        } finally {
            view.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.restoreProjectionMatrix();
        }
    }
    private static void drawFire(PoseStack poseStack, TextureAtlasSprite sprite, double time) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float[] previousColor=RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float um = (u0 + u1) / 2.0F;
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float vm = (v0 + v1) / 2.0F;
        float shrink = sprite.uvShrinkRatio();
        float ua = Mth.lerp(shrink, u0, um);
        float ub = Mth.lerp(shrink, u1, um);
        float va = Mth.lerp(shrink, v0, vm);
        float vb = Mth.lerp(shrink, v1, vm);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        // 火舌向上跳动：以不同相位微移两片四边形，避免机械的静态火焰。
        float bobA = (float) Math.sin(time * 0.31) * 0.02F;
        float bobB = (float) Math.sin(time * 0.47 + 1.7) * 0.02F;
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            float translateX = (float) (-(i * 2 - 1)) * 0.24F;
            float bob = i == 0 ? bobA : bobB;
            poseStack.translate(translateX, -0.3F + bob, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (i * 2 - 1) * 10.0F));
            Matrix4f matrix4f = poseStack.last().pose();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            // 顶点格式为 POSITION_TEX_COLOR(位置→UV→颜色)，必须先写 uv 再写 color，
            // 否则 BufferBuilder 的 color()/uv() 会因当前元素品名不符而空转，endVertex 报
            // "Not filled all elements of the vertex" 闪退（见 image-170 崩溃分析）。
            builder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).uv(ub, vb).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            builder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).uv(ua, vb).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            builder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).uv(ua, va).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            builder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).uv(ub, va).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(builder.end());
            poseStack.popPose();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
        RenderSystem.setShaderColor(previousColor[0],previousColor[1],previousColor[2],previousColor[3]);
    }
    private ExoticBurnOverlay() {}
}
