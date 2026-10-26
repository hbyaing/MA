package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

/** Text-free elements extracted from the two approved compact references. */
public final class SenseFurnaceArt {
    private static final Map<String,int[]> SIZES=Map.ofEntries(
        Map.entry("frame",new int[]{1377,1074}),Map.entry("panel",new int[]{697,262}),
        Map.entry("button",new int[]{587,73}),Map.entry("selected",new int[]{465,109}),
        Map.entry("row",new int[]{465,109}),Map.entry("slot",new int[]{98,98}));
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/sense_heavenly_v1/"+name+".png");}
    public static void frame(GuiGraphics g,String name,int x,int y,int w,int h){
        int[] size=SIZES.get(name);int sw=size[0],sh=size[1],s=12,d=3;
        int[] u={0,s,sw-s,sw},v={0,s,sh-s,sh},xx={x,x+d,x+w-d,x+w},yy={y,y+d,y+h-d,y+h};
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        for(int r=0;r<3;r++)for(int c=0;c<3;c++)g.blit(texture(name),xx[c],yy[r],xx[c+1]-xx[c],yy[r+1]-yy[r],u[c],v[r],u[c+1]-u[c],v[r+1]-v[r],sw,sh);
        RenderSystem.disableBlend();
    }
    public static void icon(GuiGraphics g,String name,int x,int y,int size){
        int source=name.equals("previous")||name.equals("next")?32:64;
        g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();g.blit(texture(name),x,y,size,size,0,0,source,source,source,source);RenderSystem.disableBlend();
    }
    public static class Button extends InkButton {
        public boolean chosen;
        private final String icon;
        public Button(int x,int y,int w,int h,String text,OnPress action){this(x,y,w,h,text,null,action);}
        public Button(int x,int y,int w,int h,String text,String icon,OnPress action){super(x,y,w,h,Component.literal(text),action);this.icon=icon;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
            float tint=!active?.42f:isHoveredOrFocused()?1f:chosen?.9f:.73f;g.setColor(tint,tint,tint,1);
            frame(g,chosen?"selected":"button",getX(),getY(),getWidth(),getHeight());g.setColor(1,1,1,1);
            if(icon!=null){icon(g,icon,getX()+(getWidth()-9)/2,getY()+(getHeight()-9)/2,9);return;}
            var font=Minecraft.getInstance().font;String text=font.plainSubstrByWidth(getMessage().getString(),getWidth()-8);
            g.drawCenteredString(font,text,getX()+getWidth()/2,getY()+(getHeight()-9)/2,active?CultivationUi.TEXT:CultivationUi.MUTED);
        }
    }
    private SenseFurnaceArt(){}
}
