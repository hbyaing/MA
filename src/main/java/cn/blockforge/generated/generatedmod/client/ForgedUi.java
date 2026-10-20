package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Reusable elements extracted from the approved iron-and-gold UI artwork. */
public final class ForgedUi {
    public static final int GOLD=0xD8BA7D,IVORY=0xE8DECA,MUTED=0x96958D,CYAN=0x70D8E3;
    private static ResourceLocation art(String name){return new ResourceLocation("wuxingxiantu","textures/gui/forged/"+name+".png");}
    public static void rune(GuiGraphics g,int type,int x,int y,int size){String name=switch(type){case 0->"rune_guard";case 1->"rune_tough";case 2->"rune_light";case 3->"rune_control";case 4->"rune_banish";case 5->"rune_vitality";case 6->"rune_soul";case 7->"rune_gather";default->"rune_tough";};g.blit(art(name),x,y,size,size,0,0,53,53,53,53);}
    public static void flow(GuiGraphics g,int type,int x,int y){String name=type==0?"flow_gather":type==1?"flow_heat":"flow_finish";int w=type==0?31:type==1?39:42,h=type==0?37:type==1?40:41;g.blit(art(name),x,y,13,14,0,0,w,h,w,h);}
    private static void corners(GuiGraphics g,String name,int x,int y,int w,int h,int size,int source){
        for(int i=0;i<4;i++){boolean right=i%2!=0,bottom=i>=2;g.blit(art(name),right?x+w-size:x,bottom?y+h-size:y,size,size,right?source:0,bottom?source:0,right?-source:source,bottom?-source:source,source,source);}
    }
    public static void panel(GuiGraphics g,int x,int y,int w,int h,boolean ornate){
        g.fill(x,y,x+w,y+h,0xFF101114);g.blit(art("metal"),x+1,y+1,w-2,h-2,0,0,60,45,60,45);g.fill(x+1,y+1,x+w-1,y+h-1,0xBB0B0C0F);
        g.renderOutline(x,y,w,h,0xFF514735);g.renderOutline(x+2,y+2,w-4,h-4,0xFF242528);
        if(ornate)corners(g,"corner",x,y,w,h,11,33);
    }
    public static void window(GuiGraphics g,int x,int y,int w,int h,String title,String subtitle){
        g.fill(x-3,y-3,x+w+3,y+h+3,0xD0060709);panel(g,x,y,w,h,false);
        g.renderOutline(x,y,w,h,0xFF514735);g.renderOutline(x+1,y+1,w-2,h-2,0xFFC3A363);g.renderOutline(x+3,y+3,w-6,h-6,0xFF79623E);
        corners(g,"corner",x,y,w,h,11,33);
        int tw=170,tx=x+(w-tw)/2;g.blit(art("title_plaque"),tx,y-5,tw,39,0,0,385,89,385,89);
        var f=Minecraft.getInstance().font;g.pose().pushPose();g.pose().translate(x+w/2f,y+3,0);g.pose().scale(1.5f,1.5f,1);g.drawCenteredString(f,title,0,0,IVORY);g.pose().popPose();g.drawCenteredString(f,subtitle,x+w/2,y+21,GOLD);
    }
    public static void slot(GuiGraphics g,int x,int y,int size){g.fill(x,y,x+size,y+size,0xFF07090B);g.renderOutline(x,y,size,size,0xFF85734C);corners(g,"slot_corner",x,y,size,size,7,18);}
    public static void label(GuiGraphics g,String s,int x,int y,int color){g.drawString(Minecraft.getInstance().font,s,x,y,color,false);}
    public static void divider(GuiGraphics g,int x,int y,int w){g.fill(x,y,x+w,y+1,0xFF514735);int c=x+w/2;g.fill(c-2,y-1,c+2,y+2,0xFFB89C60);}
    public static void strategy(GuiGraphics g,int type,int x,int y){String name=type==0?"strategy_stable":type==1?"strategy_risk":"strategy_protect";g.blit(art(name),x,y,18,18,0,0,48,48,48,48);}
    public static void equipment(GuiGraphics g,int x,int y,int size){nine(g,"equipment_frame",x,y,size,size,200,171,18,14);}
    private static void nine(GuiGraphics g,String name,int x,int y,int w,int h,int sw,int sh,int cap,int edge){
        int dx=Math.min(cap/3,w/3),dy=Math.min(edge/2,h/3);int[] us={0,cap,sw-cap,sw},vs={0,edge,sh-edge,sh},xs={x,x+dx,x+w-dx,x+w},ys={y,y+dy,y+h-dy,y+h};
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)g.blit(art(name),xs[col],ys[row],xs[col+1]-xs[col],ys[row+1]-ys[row],us[col],vs[row],us[col+1]-us[col],vs[row+1]-vs[row],sw,sh);
    }
    public static void search(GuiGraphics g,int x,int y,int w,int h){nine(g,"search_frame",x,y,w,h,258,44,12,8);g.blit(art("search_icon"),x+7,y+3,9,9,0,0,25,25,25,25);}
    public static void choice(GuiGraphics g,int x,int y,int w,int h,boolean chosen){nine(g,chosen?"button_selected":"button_choice",x,y,w,h,chosen?236:234,chosen?66:60,chosen?44:14,9);}
    public static void scrollArrow(GuiGraphics g,int x,int y,boolean down){g.blit(art("scroll_arrow"),x,y,7,7,0,down?17:0,16,down?-17:17,16,17);}
    public static void scrollThumb(GuiGraphics g,int x,int y,int w,int h,boolean enabled){g.fill(x,y,x+w,y+h,enabled?0xFF8D7042:0xFF49463D);g.fill(x+1,y+2,x+w-1,y+h-2,enabled?0xFFD1B274:0xFF6B6556);g.fill(x+2,y+3,x+3,y+h-3,enabled?0xFFF0D89C:0xFF817969);}
    public static class Button extends InkButton {
        public boolean chosen,primary,choice;
        public Button(int x,int y,int w,int h,String text,OnPress press){super(x,y,w,h,Component.literal(text),press);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
            boolean hovered=active&&isHoveredOrFocused();
            CultivationUi.backpackButton(g,getX(),getY(),getWidth(),getHeight(),active,chosen,hovered,false);
            g.drawCenteredString(Minecraft.getInstance().font,getMessage(),getX()+getWidth()/2,getY()+(getHeight()-8)/2,
                    !active?CultivationUi.MUTED:chosen?CultivationUi.GOLD:CultivationUi.TEXT);
        }
    }
}
