package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Matched on/off sprites extracted from the reviewed settings concept. */
final class SettingsUiAssets {
    private static final ResourceLocation TOGGLES=ResourceLocation.fromNamespaceAndPath(
            "wuxingxiantu","textures/gui/wuxia/settings/toggle_jade_v2.png");
    static final int TOGGLE_WIDTH=26,TOGGLE_HEIGHT=12,RIGHT_INSET=5;
    private static final ResourceLocation THUMB=resource("slider_jade_thumb");
    private static final ResourceLocation TRACK=resource("slider_track"),FILL=resource("slider_fill");

    static void toggle(GuiGraphics g,int x,int y,boolean on,boolean enabled) {
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        float tint=enabled?1F:.55F;g.setColor(tint,tint,tint,1);
        // Identical source bounds keep the bezel and bead size steady between
        // states. The full atlas alpha is retained; there is no baked backdrop.
        g.blit(TOGGLES,x,y,TOGGLE_WIDTH,TOGGLE_HEIGHT,710,on?187:460,432,200,1855,848);
        g.setColor(1,1,1,1);RenderSystem.disableBlend();
    }
    static void slider(GuiGraphics g,int x,int y,int width,double value,boolean enabled,boolean focused) {
        // These endpoints are the same x+4 / x+width-4 used by vanilla's
        // AbstractSliderButton mouse mapping, including its keyboard behavior.
        int railX=x+4,railWidth=Math.max(0,width-8),filled=(int)Math.round(railWidth*value);
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        float tint=enabled?1F:.55F;g.setColor(tint,tint,tint,1);
        rail(g,TRACK,railX,y+22,railWidth);
        if(filled>0)rail(g,FILL,railX,y+22,filled);
        tint=enabled?(focused?1F:.88F):.55F;g.setColor(tint,tint,tint,1);
        g.blit(THUMB,railX+filled-4,y+19,8,8,417,417,420,420,1254,1254);
        g.setColor(1,1,1,1);RenderSystem.disableBlend();
    }
    private static void rail(GuiGraphics g,ResourceLocation texture,int x,int y,int width) {
        if(width<=0)return;
        int cap=Math.min(2,width/2);
        if(cap>0)g.blit(texture,x,y,cap,2,0,0,8,8,512,8);
        if(width>2*cap)g.blit(texture,x+cap,y,width-2*cap,2,8,0,496,8,512,8);
        if(cap>0)g.blit(texture,x+width-cap,y,cap,2,504,0,8,8,512,8);
    }
    private static ResourceLocation resource(String name) {
        return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/settings/"+name+".png");
    }
    private SettingsUiAssets() {}
}
