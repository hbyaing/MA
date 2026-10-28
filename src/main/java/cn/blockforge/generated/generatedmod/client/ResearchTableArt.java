package cn.blockforge.generated.generatedmod.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
/** Text-free approved artwork; UV elements shared with the layout workbench. */
public final class ResearchTableArt {
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/research_v2/background.png");
    public static void part(GuiGraphics g,int x,int y,int w,int h,int u,int v,int sw,int sh){g.blit(TEXTURE,x,y,w,h,u*1519/440f,v*1035/300f,Math.max(1,sw*1519/440),Math.max(1,sh*1035/300),1519,1035);}
    public static void background(GuiGraphics g,int x,int y){part(g,x,y,440,300,0,0,440,300);part(g,x+17,y+119,38,13,11,128,38,13);
        // Rebuild the inventory using actual 36-slot menu coordinates, independent of generated grids.
        part(g,x+78,y+219,263,79,11,128,120,28);
        part(g,x+84,y+219,251,3,84,219,251,3);part(g,x+84,y+297,251,2,84,289,251,1);
        part(g,x+78,y+225,6,67,78,225,6,59);part(g,x+335,y+225,6,67,335,225,6,59);
        part(g,x+78,y+219,8,8,78,219,8,8);part(g,x+333,y+219,8,8,333,219,8,8);
        part(g,x+78,y+291,8,8,78,282,8,8);part(g,x+333,y+291,8,8,333,282,8,8);
        for(int row=0;row<4;row++)for(int col=0;col<9;col++)part(g,x+117+col*20,y+(row==3?280:224+row*18),18,18,33,171,30,29);
    }
    public static class Button extends InkButton {
        private final boolean primary;public boolean chosen;
        public Button(int x,int y,int w,int h,String text,boolean primary,OnPress a){super(x,y,w,h,net.minecraft.network.chat.Component.literal(text),a);this.primary=primary;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){float tint=!active?.48F:isHoveredOrFocused()?1F:.84F;g.setColor(tint,tint,tint,1);
            if(height<=17)part(g,getX(),getY(),width,height,chosen?13:84,34,chosen?66:65,17);
            else part(g,getX(),getY(),width,height,primary?154:275,196,primary?110:96,19);
            g.setColor(1,1,1,1);var font=net.minecraft.client.Minecraft.getInstance().font;var text=font.plainSubstrByWidth(getMessage().getString(),width-10);g.drawString(font,text,getX()+(width-font.width(text))/2,getY()+(height-9)/2,active?0xE8D5A2:0x8D917F,false);
        }
    }
    private ResearchTableArt(){}
}
