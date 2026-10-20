package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

/** Original-reference crops; frame centers containing sample values are never drawn. */
final class RefiningUiAssets {
    private record Size(int w,int h) {}
    private static final Map<String,Size> SIZES=Map.ofEntries(
            Map.entry("landscape",new Size(490,232)),Map.entry("panel",new Size(1500,976)),
            Map.entry("title",new Size(665,62)),Map.entry("separator",new Size(1445,23)),
            Map.entry("material_rule",new Size(1266,23)),
            Map.entry("card",new Size(471,88)),Map.entry("output",new Size(201,189)),
            Map.entry("previous",new Size(85,85)),Map.entry("next",new Size(86,85)),
            Map.entry("close",new Size(76,75)),Map.entry("button",new Size(1427,96)),
            Map.entry("button_left",new Size(188,72)),Map.entry("button_right",new Size(184,72)),
            Map.entry("info",new Size(1429,72)),Map.entry("info_tall",new Size(1429,95)),
            Map.entry("aura_icon",new Size(49,56)),Map.entry("time_icon",new Size(40,51)),
            Map.entry("pedestal_icon",new Size(50,64)),Map.entry("fire_icon",new Size(68,83)));
    static void draw(GuiGraphics g,String name,int x,int y,int w,int h) {
        Size s=SIZES.get(name);
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(texture(name),x,y,w,h,0,0,s.w,s.h,s.w,s.h);
    }
    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/refining/"+name+".png");
    }
    static void frame(GuiGraphics g,String name,int x,int y,int w,int h,int sourceEdge,int edge) {
        Size s=SIZES.get(name);
        int[] sx={0,sourceEdge,s.w-sourceEdge,s.w},sy={0,sourceEdge,s.h-sourceEdge,s.h};
        int[] dx={0,edge,w-edge,w},dy={0,edge,h-edge,h};
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        for(int row=0;row<3;row++)for(int col=0;col<3;col++) {
            if(row==1&&col==1)continue;
            g.blit(texture(name),x+dx[col],y+dy[row],dx[col+1]-dx[col],dy[row+1]-dy[row],
                    sx[col],sy[row],sx[col+1]-sx[col],sy[row+1]-sy[row],s.w,s.h);
        }
    }
    static void button(GuiGraphics g,int x,int y,int w,int h,boolean enabled,boolean hovered) {
        button(g,x,y,w,h,enabled,hovered,0);
    }
    /** Clean bevel rules; reference strips include the sample flame at their upper/lower edges. */
    static void infoFrame(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0x9910202B);
        int color=0xFF607A88;
        g.fill(x+2,y,x+w-2,y+1,color);g.fill(x+2,y+h-1,x+w-2,y+h,color);
        g.fill(x,y+2,x+1,y+h-2,color);g.fill(x+w-1,y+2,x+w,y+h-2,color);
        for(int dx:new int[]{1,w-2}) for(int dy:new int[]{1,h-2})g.fill(x+dx,y+dy,x+dx+1,y+dy+1,color);
    }
    static void button(GuiGraphics g,int x,int y,int w,int h,boolean enabled,boolean hovered,float progress) {
        g.fillGradient(x,y,x+w,y+h,enabled?(hovered?0xEE445369:0xEE2E3C4D):0xDD23303E,0xEE14232E);
        int fillW=Math.max(0,Math.min(w-6,Math.round((w-6)*progress)));
        if(fillW>0) g.fillGradient(x+3,y+3,x+3+fillW,y+h-3,0xCCAB8D48,0xBB574326);
        g.renderOutline(x,y,w,h,enabled?0xFFE3BD70:0xFF9D9D8D);
        frame(g,"button",x,y,w,h,22,5);
        g.setColor(1,1,1,enabled?.65F:.25F);
        draw(g,"button_left",x+5,y+5,35,h-10);draw(g,"button_right",x+w-45,y+5,40,h-10);
        g.setColor(1,1,1,1);
    }
}
