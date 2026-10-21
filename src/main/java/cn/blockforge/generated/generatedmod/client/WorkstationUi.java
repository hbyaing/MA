package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * UI primitives extracted from the approved engraving/forge reference.
 * All dimensions are logical pixels; callers scale the complete screen uniformly.
 */
public final class WorkstationUi {
    public static final int PANEL=0xB80A1116, PANEL_SOFT=0x8E0A1116,
            LINE=0xFF71828A, LINE_DARK=0xFF263840, GOLD=0xFFE0B85D,
            TEXT=0xFFE9F1F0, MUTED=0xFF9BA9AC, CYAN=0xFF54E4EA;
    private WorkstationUi() {}

    public static void window(GuiGraphics g,int x,int y,int w,int h,String title,String subtitle){
        g.fill(x,y,x+w,y+h,0xC30A1218);
        targetFrame(g,"gold",x,y,w,h);
        int cx=x+w/2;
        CultivationUi.rule(g,x+90,y+32,Math.max(24,w-180));
        g.pose().pushPose();g.pose().translate(cx,y+7,0);g.pose().scale(1.55f,1.55f,1);g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,title,0,0,TEXT);g.pose().popPose();
        g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,subtitle,cx,y+25,GOLD);
    }
    public static void panel(GuiGraphics g,int x,int y,int w,int h,boolean accent){
        g.fill(x,y,x+w,y+h,PANEL);
        targetFrame(g,"ink",x,y,w,h);
        if(accent)CultivationUi.rule(g,x+12,y+19,Math.max(12,w-24));
    }
    private static void corner(GuiGraphics g,int x,int y,boolean right,boolean bottom){
        int c=GOLD;g.fill(x+(right?7:0),y,x+(right?8:1),y+8,c);g.fill(x,y+(bottom?7:0),x+8,y+(bottom?8:1),c);
    }
    /** Draw the extracted border at its authored line thickness; only long spans are tiled. */
    private static void targetFrame(GuiGraphics g,String kind,int x,int y,int w,int h){
        // The reference uses a continuous one-pixel cool line with tiny gold corner ticks.
        // Do not draw the 16px source corner canvases directly: at logical GUI scale they
        // become opaque black wedges and break the panel silhouette.
        int line=kind.equals("gold")?0xFFD2D7D2:LINE;
        g.renderOutline(x,y,w,h,line);g.renderOutline(x+2,y+2,w-4,h-4,LINE_DARK);
        int c=Math.min(7,Math.min(w,h)/4), gold=GOLD;
        g.fill(x,y,x+c,y+1,gold);g.fill(x,y,x+1,y+c,gold);
        g.fill(x+w-c,y,x+w,y+1,gold);g.fill(x+w-1,y,x+w,y+c,gold);
        g.fill(x,y+h-1,x+c,y+h,gold);g.fill(x,y+h-c,x+1,y+h,gold);
        g.fill(x+w-c,y+h-1,x+w,y+h,gold);g.fill(x+w-1,y+h-c,x+w,y+h,gold);
    }
    private static ResourceLocation borderTex(String name){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/reference/borders/"+name+".png");}
    public static void divider(GuiGraphics g,int x,int y,int w){
        CultivationUi.rule(g,x,y+2,w);
    }
    public static void slot(GuiGraphics g,int x,int y,int size){
        g.fill(x,y,x+size,y+size,0xA8081116);g.renderOutline(x,y,size,size,GOLD);g.renderOutline(x+2,y+2,size-4,size-4,LINE_DARK);
        g.fill(x,y,x+4,y+1,GOLD);g.fill(x,y,x+1,y+4,GOLD);g.fill(x+size-4,y+size-1,x+size,y+size,GOLD);g.fill(x+size-1,y+size-4,x+size,y+size,GOLD);
    }
    public static void search(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,PANEL_SOFT);g.renderOutline(x,y,w,h,LINE);g.renderOutline(x+2,y+2,w-4,h-4,LINE_DARK);
        g.blit(new net.minecraft.resources.ResourceLocation("wuxingxiantu","textures/gui/forged/search_icon.png"),x+7,y+3,9,9,0,0,25,25,25,25);
    }
    public static void choice(GuiGraphics g,int x,int y,int w,int h,boolean chosen){
        g.fill(x,y,x+w,y+h,chosen?0xA51A6670:PANEL_SOFT);g.renderOutline(x,y,w,h,chosen?CYAN:LINE);g.renderOutline(x+2,y+2,w-4,h-4,chosen?0xFF2B8F99:LINE_DARK);
        if(chosen){g.fill(x+4,y+h-3,x+w-4,y+h-2,CYAN);g.fill(x+4,y+2,x+w-4,y+3,CYAN);}
    }
    public static void scrollTrack(GuiGraphics g,int x,int y,int h,int thumbY,int thumbH){
        g.fill(x,y,x+5,y+h,0xA8071116);g.renderOutline(x,y,5,h,LINE_DARK);g.fill(x+1,thumbY,x+4,thumbY+thumbH,GOLD);g.fill(x+2,thumbY+2,x+3,thumbY+thumbH-2,0xFFFFE2A0);
    }
    public static class Button extends ForgedUi.Button {
        public Button(int x,int y,int w,int h,String text,OnPress press){super(x,y,w,h,text,press);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
            boolean hover=active&&isHoveredOrFocused();g.fill(getX(),getY(),getX()+width,getY()+height,hover?0xA52B5962:PANEL_SOFT);
            g.renderOutline(getX(),getY(),width,height,active?(hover?CYAN:LINE):LINE_DARK);g.renderOutline(getX()+2,getY()+2,width-4,height-4,LINE_DARK);
            g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,getMessage(),getX()+width/2,getY()+(height-8)/2,active?(hover?TEXT:GOLD):MUTED);
        }
    }
}
