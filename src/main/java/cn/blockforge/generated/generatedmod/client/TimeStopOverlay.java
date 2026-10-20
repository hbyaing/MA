package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Desaturate the final shader-pack composition before HUD rendering. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class TimeStopOverlay {
    private static PostChain chain;
    private static PostPass grayscale;
    private static Object resources;
    private static boolean failed;
    private static float amount, previousAmount;
    private static int width, height;
    private static Object renderTarget;
    private static boolean renderedThisFrame;

    private TimeStopOverlay() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            amount = previousAmount = 0;
            close();
            failed = false;
            return;
        }
        if (client.isPaused()) return;
        var data = GeneratedMod.data(client.player);
        boolean active = data != null && "ri_yue_ting_lun".equals(data.wuxingxiantu$getActiveDivineArt());
        previousAmount = amount;
        amount = active ? Math.min(1, amount + 0.125F) : Math.max(0, amount - 0.1F);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void beforeHud(RenderGuiEvent.Pre event) {
        event.getGuiGraphics().flush();
        render(Minecraft.getInstance().getPartialTick());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void frame(TickEvent.RenderTickEvent event) {
        if(event.phase==TickEvent.Phase.START)renderedThisFrame=false;
        else if(Minecraft.getInstance().options.hideGui)render(event.renderTickTime);
    }

    private static void render(float partialTick) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || renderedThisFrame) return;
        float phase = net.minecraft.util.Mth.lerp(partialTick, previousAmount, amount);
        if (phase <= 0) return;
        if (resources != client.getResourceManager()) {
            close();
            resources = client.getResourceManager();
            failed = false;
        }
        if (failed) return;
        renderedThisFrame=true;
        int oldDraw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int oldRead=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int[] scissorBox=new int[4];GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissorBox);
        int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var previousShader=RenderSystem.getShader();
        int texture=RenderSystem.getShaderTexture(0);
        float[] color=RenderSystem.getShaderColor().clone();
        try {
            var target = client.getMainRenderTarget();
            if(renderTarget!=target){close();renderTarget=target;}
            if (chain == null) {
                chain = new PostChain(client.getTextureManager(), client.getResourceManager(), target,
                        new ResourceLocation(GeneratedMod.MOD_ID, "shaders/post/time_stop.json"));
                grayscale = chain.addPass("wuxingxiantu:time_stop", target, chain.getTempTarget("swap"));
                chain.addPass("blit", chain.getTempTarget("swap"), target);
                width = height = -1;
            }
            if (width != target.width || height != target.height) {
                width = target.width;
                height = target.height;
                chain.resize(width, height);
            }
            grayscale.getEffect().safeGetUniform("Phase").set(phase);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableScissor();
            chain.process(partialTick);
        } catch (Exception ex) {
            LogUtils.getLogger().error("Could not render time-stop scene effect", ex);
            close();
            failed = true;
        } finally {
            RenderSystem.setShader(()->previousShader);RenderSystem.setShaderTexture(0,texture);
            RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
            RenderSystem.depthMask(depthMask);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            if(scissor)RenderSystem.enableScissor(scissorBox[0],scissorBox[1],scissorBox[2],scissorBox[3]);
            else RenderSystem.disableScissor();
            RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,oldDraw);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,oldRead);
            RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
        }
    }

    public static void close() {
        if (chain != null) chain.close();
        chain = null;
        grayscale = null;
        renderTarget = null;
    }

    public static void reload() {
        close();
        failed = false;
    }
}
