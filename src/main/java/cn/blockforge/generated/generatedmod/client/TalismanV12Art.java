package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Complete elements extracted from the approved v12 artwork; no baked UI text. */
public final class TalismanV12Art {
    private TalismanV12Art(){}
    public static void image(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh){
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/talisman_v12/"+name+".png"),x,y,w,h,0,0,sw,sh,sw,sh);
        RenderSystem.disableBlend();
    }
    /** Alpha is clipped in the shared texture itself, covering every draw path. */
    public static void background(GuiGraphics g,String name,int x,int y){
        image(g,name,x,y,430,340,1408,1117);
        // The authored header also contains a baked title-shaped patch.
        // Give the entire header the panel ink, leaving both frame strokes
        // and the beveled corners intact; restore only the authored dividers.
        int ink=0xFF031118;
        g.fill(x+11,y+9,x+419,y+11,ink);
        g.fill(x+8,y+11,x+422,y+41,ink);
        var texture=ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/talisman_v12/"+name+".png");
        g.blit(texture,x+8,y+33,166,3,26,108,544,10,1408,1117);
        g.blit(texture,x+256,y+33,166,3,838,108,544,10,1408,1117);
    }
    public static void slot(GuiGraphics g,int x,int y){image(g,"slot",x,y,18,18,60,60);}
    public static void frame(GuiGraphics g,int x,int y,int w,int h){
        int[] us={0,40,1368,1408},vs={0,40,1077,1117},xs={x,x+10,x+w-10,x+w},ys={y,y+10,y+h-10,y+h};
        var texture=ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/talisman_v12/frame.png");
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        for(int r=0;r<3;r++)for(int c=0;c<3;c++)g.blit(texture,xs[c],ys[r],xs[c+1]-xs[c],ys[r+1]-ys[r],us[c],vs[r],us[c+1]-us[c],vs[r+1]-vs[r],1408,1117);
        RenderSystem.disableBlend();
    }
    public static final class Button extends ForgedUi.Button {
        private final String skin;
        public Button(int x,int y,int w,int h,String text,String skin,OnPress action){super(x,y,w,h,text,action);this.skin=skin;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
            boolean hovered=active&&isHoveredOrFocused();
            if(!active)g.setColor(.48f,.48f,.48f,1);
            switch(skin){
                case "brush" -> image(g,chosen||hovered?"brush_on":"brush_off",getX(),getY(),getWidth(),getHeight(),52,52);
                case "expand" -> image(g,hovered?"expand_hover":"expand",getX(),getY(),getWidth(),getHeight(),49,49);
                case "collapse" -> image(g,hovered?"collapse_small_hover":"collapse_small",getX(),getY(),getWidth(),getHeight(),16,16);
                case "choice" -> {if(chosen||hovered)image(g,"choice",getX(),getY(),getWidth(),getHeight(),335,67);}
                default -> CultivationUi.backpackButton(g,getX(),getY(),getWidth(),getHeight(),active,chosen,hovered,false);
            }
            g.setColor(1,1,1,1);
            if(!skin.equals("brush")&&!skin.equals("expand")&&!skin.equals("collapse"))
                g.drawCenteredString(Minecraft.getInstance().font,getMessage(),getX()+getWidth()/2,getY()+(getHeight()-9)/2,active?CultivationUi.TEXT:CultivationUi.MUTED);
        }
    }
}
