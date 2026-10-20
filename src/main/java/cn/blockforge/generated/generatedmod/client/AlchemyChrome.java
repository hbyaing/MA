package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;

/** Coordinate-authored fittings. Fixed corners preserve line weight at every size. */
final class AlchemyChrome {
    private static final java.util.Map<String,ResourceLocation> TEXTURES=new java.util.HashMap<>();
    static final int GOLD=0xFFEED08A, EDGE=0xFF877047, INK=0xFF0C1B24;
    static void surface(GuiGraphics g,int x,int y,int w,int h,int edge,int top,int bottom) {
        g.fillGradient(x+1,y+2,x+w-1,y+h-2,top,bottom);
        g.fill(x+2,y+1,x+w-2,y+2,top);
        g.fill(x+2,y+h-2,x+w-2,y+h-1,bottom);
        g.fill(x+2,y,x+w-2,y+1,edge);
        g.fill(x+2,y+h-1,x+w-2,y+h,edge);
        g.fill(x,y+2,x+1,y+h-2,edge);
        g.fill(x+w-1,y+2,x+w,y+h-2,edge);
        for(int dx:new int[]{1,w-2})for(int dy:new int[]{1,h-2})g.fill(x+dx,y+dy,x+dx+1,y+dy+1,edge);
    }
    static void panel(GuiGraphics g,int x,int y,int w,int h) {
        g.fillGradient(x+1,y+1,x+w-1,y+h-1,0xE3102732,0xF0081720);
        nine(g,"panel",x,y,w,h,128,128,32,8);
    }
    static void slot(GuiGraphics g,int x,int y,boolean machine) {
        texture(g,machine?"material_slot":"inventory_slot",x,y,18,18,72,72);
    }
    static void button(GuiGraphics g,int x,int y,int w,int h,boolean active,boolean selected,boolean hover,boolean primary,boolean pressed,boolean tab) {
        boolean gold=primary&&active||tab&&selected;
        String name=gold?"button_gold":selected||active&&hover?"button_selected":"button";
        if(!active&&!selected)g.setColor(.66F,.69F,.69F,1);
        nine(g,name,x,y,w,h,256,96,16,4);
        g.setColor(1,1,1,1);
        if(pressed)g.fill(x+4,y+h-3,x+w-4,y+h-2,0xFF90DCE6);
    }
    static void bar(GuiGraphics g,int x,int y,int w,int h,float progress,boolean spirit,boolean danger) {
        nine(g,"bar_track",x,y,w,h,256,48,12,3);
        int fill=Math.round((w-2)*Math.max(0,Math.min(1,progress)));
        if(fill>0)nine(g,danger?"bar_danger":spirit?"bar_spirit":"bar_gold",x+1,y+1,fill,h-2,256,48,12,2);
        // The shared rail covers fill edges at both zero and full capacity.
        nine(g,"bar_border",x,y,w,h,256,48,12,3);
    }
    static void progressBacking(GuiGraphics g,int x,int y,int w,int h) {
        nine(g,"progress_backing",x,y,w,h,256,48,12,3);
    }
    static void rule(GuiGraphics g,int x,int y,int w) {
        CultivationUi.softDivider(g,x,y,w,false,0xA4B59A65);
    }
    static void formation(GuiGraphics g,int cx,int cy) { texture(g,"formation",cx-64,cy-64,128,128,512,512); }
    static void clouds(GuiGraphics g) {
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();g.setColor(.8F,.85F,.86F,.62F);
        g.blit(id("cloud_pair"),140,174,136,20,0,240,2172,320,2172,724);
        g.setColor(1,1,1,1);RenderSystem.disableBlend();
    }
    static void preview(GuiGraphics g,int x,int y) { texture(g,"preview",x,y,28,28,112,112); }
    static void headerBacking(GuiGraphics g) {
        texture(g,"header_shadow",120,5,180,18,720,72);
    }
    static void headerCorners(GuiGraphics g) {
        headerOrnament(g,"header_corner",3,2,48,23,192,92,false);
        headerOrnament(g,"header_corner",369,2,48,23,192,92,true);
    }
    static void headerFlourishes(GuiGraphics g,int titleHalfWidth) {
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(id("divider"),210-titleHalfWidth-6,20,2*titleHalfWidth+12,6,32,0,448,24,512,24);
        RenderSystem.disableBlend();
        headerOrnament(g,"header_flourish",210-titleHalfWidth-26,17,22,7,88,28,false);
        headerOrnament(g,"header_flourish",210+titleHalfWidth+4,17,22,7,88,28,true);
    }
    private static void headerOrnament(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh,boolean mirror) {
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(id(name),x,y,w,h,mirror?sw:0,0,mirror?-sw:sw,sh,sw,sh);
        RenderSystem.disableBlend();
    }
    private static ResourceLocation id(String name) { return TEXTURES.computeIfAbsent(name,n -> ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/alchemy_concept/"+n+".png")); }
    private static void texture(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh) {
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(id(name),x,y,w,h,0,0,sw,sh,sw,sh);
        RenderSystem.disableBlend();
    }
    private static void strip(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh,int sourceCap,int cap) {
        cap=Math.min(cap,w/2);
        int[] xs={x,x+cap,x+w-cap,x+w},us={0,sourceCap,sw-sourceCap,sw};
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        for(int col=0;col<3;col++)if(xs[col+1]>xs[col])
            g.blit(id(name),xs[col],y,xs[col+1]-xs[col],h,us[col],0,us[col+1]-us[col],sh,sw,sh);
        RenderSystem.disableBlend();
    }
    private static void nine(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh,int sourceCap,int cap) {
        cap=Math.min(cap,Math.min(w,h)/2);
        int[] xs={x,x+cap,x+w-cap,x+w},ys={y,y+cap,y+h-cap,y+h};
        int[] us={0,sourceCap,sw-sourceCap,sw},vs={0,sourceCap,sh-sourceCap,sh};
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        for(int row=0;row<3;row++)for(int col=0;col<3;col++) {
            if(xs[col+1]<=xs[col]||ys[row+1]<=ys[row])continue;
            g.blit(id(name),xs[col],ys[row],xs[col+1]-xs[col],ys[row+1]-ys[row],
                    us[col],vs[row],us[col+1]-us[col],vs[row+1]-vs[row],sw,sh);
        }
        RenderSystem.disableBlend();
    }
    private AlchemyChrome() {}
}
