package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Text-free state artwork extracted from the navigation reference. */
final class NavigationUiAssets {
    static void draw(GuiGraphics g,String name,int x,int y,int w,int h,int sourceW,int sourceH) {
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/navigation/"+name+".png"),
                x,y,w,h,0,0,sourceW,sourceH,sourceW,sourceH);
        RenderSystem.disableBlend();
    }
    static void surface(GuiGraphics g,int x,int y,int w,int h,boolean selected,boolean hovered,boolean enabled) {
        String state=hovered?"hover":selected?"selected":"normal";
        g.setColor(1,1,1,enabled?1:.5F);
        // Keep the state fill inside the extracted frame's visible inner edge.
        // Reach under the inner gold edge while keeping the notched corners clear.
        draw(g,state+"_background",x+2,y+4,w-4,h-8,128,32);
        draw(g,state+"_background",x+4,y+2,w-8,h-4,128,32);
        draw(g,state+"_frame",x,y,w,h,512,256);
        g.setColor(1,1,1,1);
    }
    static void separator(GuiGraphics g,int x,int y,int w) {
        draw(g,"separator",x,y,w,4,512,32);
    }
}
