package cn.blockforge.generated.generatedmod.storage.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
/** Original Xiantu ink/gold/jade drawing, scoped to the storage bag only. */
public final class XiantuBagUi {
 public static final int INK=0xF5141C20,DEEP=0xFF10171B,GOLD=0xFFB39A66,BRIGHT=0xFFE6C98C,LINE=0xFF665B43,TEXT=0xFFE9DFCB,MUTED=0xFF9BA69D,JADE=0xFF7AB6A2;
 private XiantuBagUi(){}
 public static void frame(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+1,color);g.fill(x,y+h-1,x+w,y+h,color);g.fill(x,y,x+1,y+h,color);g.fill(x+w-1,y,x+w,y+h,color);}
 public static void corner(GuiGraphics g,int x,int y,int dx,int dy){for(int i=0;i<9;i++){g.fill(x+dx*i,y,x+dx*i+1,y+1,BRIGHT);g.fill(x,y+dy*i,x+1,y+dy*i+1,BRIGHT);}for(int i=0;i<4;i++){int a=x+dx*(i+3),b=y+dy*3;g.fill(a,b,a+1,b+1,GOLD);a=x+dx*3;b=y+dy*(i+3);g.fill(a,b,a+1,b+1,GOLD);}}
 /** Three-pixel carved rim grows outward, preserving menu content coordinates. */
 public static void panel(GuiGraphics g,int x,int y,int w,int h){
  g.fill(x+3,y+4,x+w+4,y+h+5,0x80000000);g.fillGradient(x,y,x+w,y+h,0xF51D292B,INK);
  frame(g,x-2,y-2,w+4,h+4,0xFF554632);frame(g,x-1,y-1,w+2,h+2,GOLD);frame(g,x,y,w,h,BRIGHT);frame(g,x+2,y+2,w-4,h-4,0x996F765A);
  corner(g,x,y,1,1);corner(g,x+w-1,y,-1,1);corner(g,x,y+h-1,1,-1);corner(g,x+w-1,y+h-1,-1,-1);
  for(int py=y+24;py<y+h-18;py+=16){meander(g,x+1,py,false);meander(g,x+w-4,py,false);}
  for(int px=x+20;px<x+w-18;px+=16){meander(g,px,y+1,true);meander(g,px,y+h-4,true);}
  g.fill(x+12,y+18,x+w-12,y+19,0x886D644A);
  cloud(g,x+w-60,y+h-10);cloud(g,x+12,y+h-10);
 }
 /** Small repeating hooked fret, confined to the rim. */
 private static void meander(GuiGraphics g,int x,int y,boolean horizontal){
  int[][] pixels={{0,0},{1,0},{2,0},{2,1},{2,2},{1,2},{0,2},{0,3},{0,4},{1,4},{2,4},{2,5},{2,6},{1,6},{0,6}};
  for(int[] p:pixels){int a=x+(horizontal?p[1]:p[0]),b=y+(horizontal?p[0]:p[1]);g.fill(a,b,a+1,b+1,0xFF8C784F);}
 }
 public static void cloud(GuiGraphics g,int x,int y){
  int c=0x886E806C;g.fill(x,y,x+37,y+1,c);g.fill(x+4,y-2,x+33,y-1,c);g.fill(x+9,y-5,x+29,y-4,c);g.fill(x+14,y-8,x+23,y-7,c);
  g.fill(x+8,y-4,x+9,y-1,c);g.fill(x+28,y-4,x+29,y-1,c);g.fill(x+13,y-7,x+14,y-4,c);g.fill(x+23,y-7,x+24,y-4,c);
  g.fill(x+16,y-4,x+22,y-3,c);g.fill(x+16,y-3,x+17,y-1,c);
 }
 public static void slot(GuiGraphics g,int x,int y){g.fillGradient(x,y,x+18,y+18,0xFF0B1216,0xFF27312E);frame(g,x,y,18,18,0xFF64614C);g.fill(x+2,y+2,x+5,y+3,0xFF94855C);g.fill(x+14,y+15,x+16,y+16,0xFF94855C);}
 public static void barFrame(GuiGraphics g,int x,int y,int w,int h){g.fill(x,y,x+w,y+h,DEEP);g.fill(x,y-2,x+w,y,LINE);g.fill(x,y+h,x+w,y+h+2,LINE);g.fill(x,y-1,x+w,y,GOLD);g.fill(x,y+h,x+w,y+h+1,GOLD);g.fill(x-2,y+1,x-1,y+h-1,GOLD);g.fill(x+w+1,y+1,x+w+2,y+h-1,GOLD);g.fill(x,y-1,x+4,y,BRIGHT);g.fill(x+w-4,y+h,x+w,y+h+1,BRIGHT);}
 public static Builder button(Component text,Button.OnPress press){return new Builder(text,press);}
 public static final class Builder {
  private final Component text;private final Button.OnPress press;private int x,y,w,h;
  Builder(Component text,Button.OnPress press){this.text=text;this.press=press;}
  public Builder bounds(int x,int y,int w,int h){this.x=x;this.y=y;this.w=w;this.h=h;return this;}
  public Button build(){return new InkButton(x,y,w,h,text,press);}
 }
 private static final class InkButton extends Button {
  InkButton(int x,int y,int w,int h,Component text,OnPress press){super(x,y,w,h,text,press,DEFAULT_NARRATION);}
  @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial){int x=getX(),y=getY();boolean hover=isHoveredOrFocused();g.fillGradient(x,y,x+width,y+height,active?(hover?0xFF344A43:0xFF253631):0xFF1A2325,0xFF131D20);frame(g,x-1,y-1,width+2,height+2,0xFF554632);frame(g,x,y,width,height,active?(hover?BRIGHT:GOLD):0xFF414943);g.fill(x+3,y+3,x+4,y+height-3,active?GOLD:0xFF414943);var font=Minecraft.getInstance().font;String label=font.plainSubstrByWidth(getMessage().getString(),Math.max(1,width-12));g.drawCenteredString(font,label,x+width/2,y+(height-8)/2,active?TEXT:MUTED);}
 }
}
