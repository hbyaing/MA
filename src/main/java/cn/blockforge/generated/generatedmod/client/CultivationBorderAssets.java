package cn.blockforge.generated.generatedmod.client;
final class CultivationBorderAssets {
private record Size(int w,int h) {}
private static final java.util.Map<String,Size> SIZES=java.util.Map.ofEntries(
java.util.Map.entry("rule_gold",new Size(213,12)),
java.util.Map.entry("rule_gold_vertical",new Size(12,213)),
java.util.Map.entry("rule_ink",new Size(178,12)),
java.util.Map.entry("gold_tl",new Size(16,16)),
java.util.Map.entry("gold_tr",new Size(16,16)),
java.util.Map.entry("gold_bl",new Size(16,16)),
java.util.Map.entry("gold_br",new Size(16,16)),
java.util.Map.entry("gold_top",new Size(429,3)),
java.util.Map.entry("gold_bottom",new Size(429,3)),
java.util.Map.entry("gold_left",new Size(3,792)),
java.util.Map.entry("gold_right",new Size(3,792)),
java.util.Map.entry("ink_tl",new Size(16,16)),
java.util.Map.entry("ink_tr",new Size(16,16)),
java.util.Map.entry("ink_bl",new Size(16,16)),
java.util.Map.entry("ink_br",new Size(16,16)),
java.util.Map.entry("ink_top",new Size(411,3)),
java.util.Map.entry("ink_bottom",new Size(411,3)),
java.util.Map.entry("ink_left",new Size(3,436)),
java.util.Map.entry("ink_right",new Size(3,436)));
static void draw(net.minecraft.client.gui.GuiGraphics g,String name,int x,int y,int w,int h){
    if(w<=0||h<=0)return;
    var s=SIZES.get(name);
    var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/reference/borders/"+name+".png");
    com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    if(name.equals("rule_gold_vertical")) {
        vertical(g,x+w/2F,y,h);
    } else g.blit(texture,x,y,w,h,0,0,s.w(),s.h(),s.w(),s.h());
    com.mojang.blaze3d.systems.RenderSystem.disableBlend();
}
/** Center the original horizontal rule on an exact background boundary. */
static void drawVertical(net.minecraft.client.gui.GuiGraphics g,int boundaryX,int y,int height) {
    if(height<=0)return;
    g.flush();g.setColor(1,1,1,1);com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    vertical(g,boundaryX,y,height);
    g.flush();com.mojang.blaze3d.systems.RenderSystem.disableBlend();
}
private static void vertical(net.minecraft.client.gui.GuiGraphics g,float center,int y,int height) {
    var rule=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/reference/borders/rule_gold.png");
    int cap=Math.min(4,height/2);
    g.pose().pushPose();g.pose().translate(center,y,0);
    g.pose().mulPose(new org.joml.Quaternionf().rotationZ((float)Math.PI/2));
    // Same 12:4 stroke scale as CultivationUi.rule; keep the hollow ends fixed.
    g.blit(rule,0,-2,cap,4,0,0,12,12,213,12);
    if(height>2*cap)g.blit(rule,cap,-2,height-2*cap,4,12,0,189,12,213,12);
    g.blit(rule,height-cap,-2,cap,4,201,0,12,12,213,12);
    g.pose().popPose();
}
static void frame(net.minecraft.client.gui.GuiGraphics g,String k,int x,int y,int w,int h){int e=5;draw(g,k+"_tl",x,y,e,e);draw(g,k+"_tr",x+w-e,y,e,e);draw(g,k+"_bl",x,y+h-e,e,e);draw(g,k+"_br",x+w-e,y+h-e,e,e);draw(g,k+"_top",x+e,y,w-2*e,1);draw(g,k+"_bottom",x+e,y+h-1,w-2*e,1);draw(g,k+"_left",x,y+e,1,h-2*e);draw(g,k+"_right",x+w-1,y+e,1,h-2*e);}
}
