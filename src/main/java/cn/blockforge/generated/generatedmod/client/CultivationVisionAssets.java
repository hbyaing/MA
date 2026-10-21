package cn.blockforge.generated.generatedmod.client;
final class CultivationVisionAssets {
private record Size(int w,int h,int u,int v,int regionW,int regionH) {
    Size(int w,int h){this(w,h,0,0,w,h);}
}
private static final java.util.Map<String,Size> SIZES=java.util.Map.ofEntries(
java.util.Map.entry("domain",new Size(1254,1254,86,166,1089,879)),
java.util.Map.entry("realm",new Size(1254,1254,59,40,1136,1172)),
java.util.Map.entry("realm_gold",new Size(1254,1254,64,28,1126,1184)),
java.util.Map.entry("cultivation",new Size(1254,1254,227,127,800,982)),
java.util.Map.entry("cultivation_gold",new Size(1254,1254,227,127,800,982)),
java.util.Map.entry("mana",new Size(1254,1254,146,160,963,932)),
java.util.Map.entry("mana_gold",new Size(1254,1254,151,166,952,922)),
java.util.Map.entry("health",new Size(1254,1254,279,131,696,943)),
java.util.Map.entry("health_gold",new Size(1254,1254,279,131,696,943)),
java.util.Map.entry("attack",new Size(1464,1075,101,50,1205,985)),
java.util.Map.entry("attack_gold",new Size(59,51)),
java.util.Map.entry("armor",new Size(1254,1254,46,77,1134,1079)),
java.util.Map.entry("armor_gold",new Size(52,55)),
java.util.Map.entry("heart_method",new Size(1254,1254,88,86,1092,1082)),
java.util.Map.entry("heart_method_gold",new Size(1254,1254,88,84,1094,1085)),
java.util.Map.entry("merit",new Size(1254,1254,16,94,1221,1058)),
java.util.Map.entry("dao",new Size(128,128)),
java.util.Map.entry("dao_gold",new Size(51,56)),
java.util.Map.entry("meridian",new Size(1254,1254,299,181,656,888)),
java.util.Map.entry("meridian_gold",new Size(52,59)),
java.util.Map.entry("physique",new Size(1281,1227,216,169,842,933)),
java.util.Map.entry("physique_gold",new Size(1281,1227,210,167,845,937)),
java.util.Map.entry("purity",new Size(64,64)),
java.util.Map.entry("purity_gold",new Size(53,55)),
java.util.Map.entry("bar_cultivation",new Size(337,24)),
java.util.Map.entry("bar_cultivation_track",new Size(337,24)),
java.util.Map.entry("bar_mana",new Size(337,25)),
java.util.Map.entry("bar_mana_track",new Size(337,25)),
java.util.Map.entry("bar_health",new Size(337,25)),
java.util.Map.entry("bar_health_track",new Size(337,25)),
java.util.Map.entry("root_thunder",new Size(256,256)),
java.util.Map.entry("root_exotic",new Size(256,256)));
static void drawProgress(net.minecraft.client.gui.GuiGraphics g,String name,int x,int y,int w,int h,double ratio){
var s=SIZES.get(name); double fraction=Math.max(0,Math.min(1,ratio));
int filled=(int)Math.round(w*fraction); if(filled<=0)return;
com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
// Crop the source instead of using screen-space scissor inside a translated page.
g.blit(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/vision/components/"+name+".png"),x,y,filled,h,0,0,Math.max(1,(int)Math.round(s.w()*filled/(double)w)),s.h(),s.w(),s.h());
com.mojang.blaze3d.systems.RenderSystem.disableBlend();
}
static void draw(net.minecraft.client.gui.GuiGraphics g,String name,int x,int y,int w,int h){
var s=SIZES.get(name); com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
if(s.u()!=0||s.v()!=0){
    double scale=Math.min(w/(double)s.regionW(),h/(double)s.regionH());
    int fitW=Math.max(1,(int)Math.round(s.regionW()*scale)),fitH=Math.max(1,(int)Math.round(s.regionH()*scale));
    x+=(w-fitW)/2;y+=(h-fitH)/2;w=fitW;h=fitH;
}
g.blit(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/vision/components/"+name+".png"),x,y,w,h,s.u(),s.v(),s.regionW(),s.regionH(),s.w(),s.h());
com.mojang.blaze3d.systems.RenderSystem.disableBlend();
}
public static void drawRootForm(net.minecraft.client.gui.GuiGraphics g,int x,int y,int size,boolean thunder){
    draw(g,thunder?"root_thunder":"root_exotic",x,y,size,size);
}
private static void diamond(net.minecraft.client.gui.GuiGraphics g,int x,int y,int size,int top,int bottom){
    var v=g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());var matrix=g.pose().last().pose();float half=size*.5f;
    v.vertex(matrix,x+half,y,0).color(top>>16&255,top>>8&255,top&255,top>>>24).endVertex();
    v.vertex(matrix,x,y+half,0).color(top>>16&255,top>>8&255,top&255,top>>>24).endVertex();
    v.vertex(matrix,x+half,y+size,0).color(bottom>>16&255,bottom>>8&255,bottom&255,bottom>>>24).endVertex();
    v.vertex(matrix,x+size,y+half,0).color(bottom>>16&255,bottom>>8&255,bottom&255,bottom>>>24).endVertex();
}
}
