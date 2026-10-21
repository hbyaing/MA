package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
/** Independently extracted reference elements for the 精铸台 skin. */
final class EquipmentUiAssets {
 private static final String ROOT="textures/gui/equipment/";
 private static ResourceLocation r(String n){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu",ROOT+n+".png");}
 private static void p(GuiGraphics g,ResourceLocation t,int x,int y,int w,int h,int sw,int sh){g.blit(t,x,y,w,h,0f,0f,sw,sh,sw,sh);}
 static void draw(GuiGraphics g,int x,int y){
  // Forge layout pass: keep only the authored geometry. The former raster
  // skin contained several overlapping frames, ornaments and gold rules that
  // obscured the live controls and made the screen read as double-bordered.
  g.fill(x,y,x+430,y+330,0xD0070A0D);
  g.renderOutline(x,y,430,330,0xFFE0B85D);
  WorkstationUi.panel(g,x+10,y+38,105,155,false);
  WorkstationUi.panel(g,x+119,y+38,116,155,false);
  WorkstationUi.panel(g,x+246,y+38,174,155,false);
  WorkstationUi.panel(g,x+5,y+204,245,122,false);
  WorkstationUi.panel(g,x+255,y+204,165,122,false);
 }
 static void drawInscription(GuiGraphics g,int x,int y){
  // Layout pass: raster inscription skin remains intentionally absent.
  g.fill(x,y,x+430,y+330,0xD0070A0D);
  g.renderOutline(x,y,430,330,0xFFE0B85D);
  // The rune list occupies the full left rail.  Keeping the panel at the
  // former upper-card height left the lower four entries and the scrollbar
  // floating on the window background instead of inside the framed rail.
  WorkstationUi.panel(g,x+10,y+38,105,288,false);
  // Preview and materials share one continuous work surface. This removes
  // the artificial seam while preserving the authored content coordinates.
  WorkstationUi.panel(g,x+119,y+38,301,164,false);
  WorkstationUi.panel(g,x+119,y+204,116,122,false);
  WorkstationUi.panel(g,x+235,y+204,185,122,false);
 }
}
