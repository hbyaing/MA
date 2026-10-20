package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.WuXingClient;
import cn.blockforge.generated.generatedmod.boss.BossVisualDefinition;
import cn.blockforge.generated.generatedmod.boss.BossVisualEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossBarHud {
    private record Entry(LerpingBossEvent event, BossVisualDefinition definition, Component name) {}
    private record Size(float artScale,float titleScale,float titleCenter,float titleTop,float height) {}
    private static final float ENTRY_SPACING=5,COLUMN_GAP=12;
    private static final float NAME_SCALE_IN_TEXTURE=1.5F;
    private static final Map<UUID, BossVisualDefinition> STYLES = new HashMap<>();
    private static final List<Entry> PENDING = new ArrayList<>();
    private static int vanillaBottom;

    @SubscribeEvent public static void begin(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) return;
        STYLES.clear();
        PENDING.clear();
        vanillaBottom = 8;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        for (var entity : level.entitiesForRendering()) {
            if (entity instanceof BossVisualEntity boss && boss.bossEventId() != null) {
                STYLES.put(boss.bossEventId(), boss.visualDefinition());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void capture(CustomizeGuiOverlayEvent.BossEventProgress event) {
        var definition = STYLES.get(event.getBossEvent().getId());
        if (definition == null) {
            vanillaBottom = Math.max(vanillaBottom, event.getY() + event.getIncrement());
            return;
        }
        // Vanilla still owns the UUID, name, interpolated health, visibility and iteration order.
        // Draw after vanilla so the large artwork does not overlap its unchanged bars.
        Component name=Component.literal(ChatFormatting.stripFormatting(event.getBossEvent().getName().getString()))
                .withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT).withColor(definition.bar().nameColor()));
        PENDING.add(new Entry(event.getBossEvent(), definition, name));
        event.setCanceled(true);
        event.setIncrement(0);
    }

    @SubscribeEvent public static void finish(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id()) || PENDING.isEmpty()) return;
        if (Minecraft.getInstance().level == null) { PENDING.clear(); return; }
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        float top = vanillaBottom + 4;
        float areaLeft=8,areaWidth=screenWidth-16;
        var client=Minecraft.getInstance();
        boolean automatic=HudConfig.BOSS_AUTO.get();
        if(automatic&&client.player!=null&&!client.player.isSpectator()) {
            float right=WuXingClient.hudRight(client)+10,left=WuXingClient.hudLeft(client)-18;
            if(screenWidth-right-8>=144){areaLeft=right;areaWidth=screenWidth-right-8;}
            else if(left>=144){areaWidth=left;}
            else if(WuXingClient.hudTop(client)<110)top=Math.max(top,WuXingClient.hudBottom(client)+6);
        }
        float available=Math.max(1,screenHeight-44-(automatic?top:0));
        int columns=PENDING.size()>3&&totalHeight(areaWidth,1)>available?2:1;
        if(!automatic){
            float desired=(float)PENDING.stream().mapToInt(e->e.definition().bar().screenWidth()).max().orElse(260)*columns+(columns-1)*COLUMN_GAP;
            areaWidth=Math.min(areaWidth,desired);
            areaLeft=8+(float)((screenWidth-16-areaWidth)*HudConfig.BOSS_X.get()/100);
        }
        float columnWidth=(areaWidth-(columns-1)*COLUMN_GAP)/columns;
        float shrink=Math.min(1,available/totalHeight(columnWidth,columns));
        if(!automatic)top=8+(float)(Math.max(0,screenHeight-44-totalHeight(columnWidth,columns)*shrink)*HudConfig.BOSS_Y.get()/100);
        float[] nextY=new float[columns];java.util.Arrays.fill(nextY,top);
        var graphics=event.getGuiGraphics();
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        float[] color=RenderSystem.getShaderColor().clone();var shader=RenderSystem.getShader();
        int texture=RenderSystem.getShaderTexture(0),boundTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();
        try {
            graphics.flush();
            for (int index = 0; index < PENDING.size(); index++) {
                int col=index%columns;Entry entry=PENDING.get(index);Size size=measure(entry,columnWidth);
                float center=areaLeft+columnWidth/2+col*(columnWidth+COLUMN_GAP);
                render(graphics,entry,size,center,nextY[col],shrink);
                nextY[col]+=(size.height()+ENTRY_SPACING)*shrink;
            }
        } finally {
            graphics.flush();
            RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);RenderSystem.setShader(()->shader);
            RenderSystem.setShaderTexture(0,texture);RenderSystem.bindTexture(boundTexture);
            RenderSystem.depthMask(depthMask);
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            PENDING.clear();
        }
    }

    private static float totalHeight(float columnWidth,int columns){
        float[] heights=new float[columns];
        for(int i=0;i<PENDING.size();i++)heights[i%columns]+=measure(PENDING.get(i),columnWidth).height()+ENTRY_SPACING;
        float max=1;for(float height:heights)max=Math.max(max,height);return max;
    }
    private static Size measure(Entry entry,float columnWidth){
        var bar=entry.definition().bar();var font=Minecraft.getInstance().font;
        float artScale=Math.min(bar.screenWidth(),columnWidth)/(float)bar.cropWidth();
        float slotX=(bar.nameAreaX()-bar.cropX())*artScale;
        float slotY=(bar.nameAreaY()-bar.cropY())*artScale;
        float slotWidth=Math.max(1,bar.nameAreaWidth()*artScale);
        float slotHeight=Math.max(1,bar.nameAreaHeight()*artScale);
        // Each texture defines a title slot between its two ornaments, above the fill track.
        // Include shadow pixels when fitting long names and phase suffixes inside that slot.
        float titleScale=Math.min(NAME_SCALE_IN_TEXTURE*artScale,
                Math.min(slotWidth/Math.max(1,font.width(entry.name())+2),slotHeight/(font.lineHeight+1)));
        float titleWidth=(font.width(entry.name())+2)*titleScale;
        float titleHeight=(font.lineHeight+1)*titleScale;
        float horizontalSpace=Math.max(0,(slotWidth-titleWidth)*.5F);
        float offsetX=Mth.clamp(bar.nameOffsetX(),-horizontalSpace,horizontalSpace);
        float titleTop=slotY+Mth.clamp((slotHeight-titleHeight)*.5F+bar.nameOffsetY()+HudConfig.BOSS_NAME_OFFSET_Y.get().floatValue(),0,Math.max(0,slotHeight-titleHeight));
        return new Size(artScale,titleScale,slotX+slotWidth*.5F+offsetX,titleTop,bar.cropHeight()*artScale);
    }
    private static void render(GuiGraphics graphics, Entry entry, Size size, float center, float top, float shrink) {
        // Font batch flushing resets depth/blend; establish the HUD state for every entry.
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);RenderSystem.setShaderColor(1,1,1,1);
        var definition = entry.definition();
        var bar = definition.bar();
        var font = Minecraft.getInstance().font;
        float scale = size.artScale()*shrink;
        float width = bar.cropWidth() * scale;
        float left = center-width/2;
        var textures=Minecraft.getInstance().getTextureManager();
        textures.getTexture(definition.resource(bar.frame())).setFilter(false,false);
        textures.getTexture(definition.resource(bar.fill())).setFilter(false,false);
        var pose = graphics.pose();
        pose.pushPose();
        try {
        pose.translate(left, top, 0);
        pose.scale(scale, scale, 1);
        graphics.blit(definition.resource(bar.frame()), 0, 0, (float) bar.cropX(), (float) bar.cropY(),
                bar.cropWidth(), bar.cropHeight(), bar.textureWidth(), bar.textureHeight());
        int filled = Mth.floor(bar.fillWidth() * Mth.clamp(entry.event().getProgress(), 0, 1));
        if (filled > 0) graphics.blit(definition.resource(bar.fill()), bar.fillX() - bar.cropX(), bar.fillY() - bar.cropY(),
                (float) bar.fillX(), (float) bar.fillY(), filled, bar.fillHeight(), bar.textureWidth(), bar.textureHeight());
        } finally {pose.popPose();}
        float textScale = size.titleScale()*shrink;
        pose.pushPose();
        try {
        pose.translate(left+size.titleCenter()*shrink, top + size.titleTop()*shrink, 0);
        pose.scale(textScale, textScale, 1);
        graphics.drawString(font, entry.name(), -font.width(entry.name()) / 2, 0, bar.nameColor(), bar.nameShadow());
        graphics.flush();
        } finally {pose.popPose();}
    }

    private BossBarHud() {}
}
