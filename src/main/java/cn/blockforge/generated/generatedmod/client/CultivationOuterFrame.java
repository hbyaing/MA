package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;

/** Outer window border only; corners stay fixed while the straight edges extend. */
final class CultivationOuterFrame {
    /** Crop backing textures to the same chamfer as the outer gold rail. */
    static void clipBackground(GuiGraphics g,int x,int y,int w,int h,Runnable draw) {
        int cut=Math.min(5,Math.min(w,h)/2);
        g.enableScissor(x,y+cut,x+w,y+h-cut);
        draw.run();
        g.disableScissor();
        for(int row=0;row<cut;row++) {
            int inset=cut-row;
            g.enableScissor(x+inset,y+row,x+w-inset,y+row+1);
            draw.run();g.disableScissor();
            g.enableScissor(x+inset,y+h-row-1,x+w-inset,y+h-row);
            draw.run();g.disableScissor();
        }
    }
    static void draw(GuiGraphics g,int x,int y,int w,int h) {
        // All main windows use the same clean corners; page switches cannot
        // restore the inner brackets removed from the design.
        draw(g,x,y,w,h,true);
    }
    static void drawAlchemy(GuiGraphics g,int x,int y,int w,int h) {
        draw(g,x,y,w,h,true);
    }
    private static void draw(GuiGraphics g,int x,int y,int w,int h,boolean cleanCorners) {
        if(w<=0||h<=0)return;
        int corner=Math.min(13,Math.min(w,h)/2),edge=Math.min(3,corner);
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();g.setColor(1,1,1,1);
        // Every corner comes from the same source, with mirrored UVs (positive
        // geometry). All fittings share the exact 39:13 and 9:3 pixel scale.
        piece(g,"top",x+corner,y,w-2*corner,edge,200,9,false,false);
        piece(g,"top",x+corner,y+h-edge,w-2*corner,edge,200,9,false,true);
        piece(g,"left",x,y+corner,edge,h-2*corner,9,200,false,false);
        piece(g,"left",x+w-edge,y+corner,edge,h-2*corner,9,200,true,false);
        corner(g,x,y,corner,false,false,cleanCorners);
        corner(g,x+w-corner,y,corner,true,false,cleanCorners);
        corner(g,x,y+h-corner,corner,false,true,cleanCorners);
        corner(g,x+w-corner,y+h-corner,corner,true,true,cleanCorners);
        RenderSystem.disableBlend();
    }
    private static void corner(GuiGraphics g,int x,int y,int size,boolean flipX,boolean flipY,boolean clean) {
        if(!clean) { piece(g,"tl",x,y,size,size,39,39,flipX,flipY);return; }
        // The inner bracket is entirely in the lower-right 24x24 source area.
        // Omit that area without redrawing or interrupting either outer rail.
        int cap=Math.round(size*15/39F);
        var texture=ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/outer_frame_v2/tl.png");
        g.blit(texture,x,y+(flipY?size-cap:0),size,cap,flipX?39:0,flipY?15:0,flipX?-39:39,flipY?-15:15,39,39);
        g.blit(texture,x+(flipX?size-cap:0),y+(flipY?0:cap),cap,size-cap,
                flipX?15:0,flipY?39:15,flipX?-15:15,flipY?-24:24,39,39);
    }
    private static void piece(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh,boolean flipX,boolean flipY) {
        if(w<=0||h<=0)return;
        g.blit(ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/outer_frame_v2/"+name+".png"),
                x,y,w,h,flipX?sw:0,flipY?sh:0,flipX?-sw:sw,flipY?-sh:sh,sw,sh);
    }
}
