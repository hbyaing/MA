package cn.blockforge.generated.generatedmod.formation.client;
import cn.blockforge.generated.generatedmod.formation.carving.*;import cn.blockforge.generated.generatedmod.client.*;import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Inventory;
public final class CarvingScreen extends AbstractContainerScreen<CarvingMenu> {
 private final java.util.List<InkButton> choices=new java.util.ArrayList<>();private InkButton carve,power;
 public CarvingScreen(CarvingMenu m,Inventory inv,Component title){super(m,inv,title);imageWidth=340;imageHeight=246;}
 @Override protected void init(){InventoryResolutionFit.apply(minecraft);width=minecraft.getWindow().getGuiScaledWidth();height=minecraft.getWindow().getGuiScaledHeight();super.init();choices.clear();for(int i=0;i<3;i++){final int type=i;choices.add(addRenderableWidget(new InkButton(leftPos+12,topPos+32+i*27,178,23,Component.literal(CarvingEye.NAMES[i]),b->send(type))));}
 carve=addRenderableWidget(new InkButton(leftPos+12,topPos+120,178,20,Component.literal("确认刻画 · 生成阵眼"),b->send(10)));
 power=addRenderableWidget(new InkButton(leftPos+205,topPos+120,123,20,Component.literal("暂停供能"),b->send(11)));refresh();}
 private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private void refresh(){boolean eye=menu.data.get(0)==1,carried=!menu.getCarried().isEmpty();for(int i=0;i<choices.size();i++){choices.get(i).active=!eye&&!carried;choices.get(i).setMessage(Component.literal((menu.data.get(1)==i?"已选 · ":"")+CarvingEye.NAMES[i]));}carve.active=!eye&&!carried;power.active=eye&&!carried;power.setMessage(Component.literal(menu.data.get(8)==1?"暂停供能":"恢复供能"));}
 @Override public void removed(){try{super.removed();}finally{InventoryResolutionFit.restore(minecraft);}}
 @Override protected void containerTick(){super.containerTick();refresh();}
 @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){WorkstationUi.panel(g,leftPos,topPos,imageWidth,imageHeight,true);for(var slot:menu.slots)if(slot.isActive())WorkstationUi.slot(g,leftPos+slot.x-1,topPos+slot.y-1,18);}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){boolean eye=menu.data.get(0)==1;int kind=Math.max(0,Math.min(2,menu.data.get(1)));g.drawString(font,eye?CarvingEye.NAMES[kind]+" · 阵眼":"阵法刻画核心",12,8,WorkstationUi.GOLD,false);g.drawString(font,"灵石供能",205,32,WorkstationUi.GOLD,false);
 String status=!eye?"刻画后生成阵眼":menu.data.get(9)==0?"供能规则未设置":menu.data.get(8)==0?"已暂停":menu.data.get(2)==1?"法阵运行中":"等待有效灵石";
 g.drawString(font,status,205,48,WorkstationUi.TEXT,false);
 g.drawString(font,"费用："+(menu.cost()>0?menu.cost()+" 下品/分钟":"待配置"),205,91,WorkstationUi.MUTED,false);
 long value=menu.reserve();for(int i=0;i<4;i++){var s=menu.slots.get(i).getItem();var grade=SpiritStoneGrade.of(s.getItem());if(grade!=null&&(menu.data.get(3)&(1<<grade.ordinal()))!=0)value+=(long)s.getCount()*grade.value*1200;}
 String remain=menu.cost()>0?String.valueOf(value/menu.cost()/20)+" 秒":"未配置";g.drawString(font,"余能："+remain,205,105,WorkstationUi.MUTED,false);
 g.drawString(font,"背包",89,148,WorkstationUi.TEXT,false);String allowed="";String[] gradeNames={"下","中","上","极"};for(int i=0;i<4;i++)if((menu.data.get(3)&(1<<i))!=0)allowed+=gradeNames[i];g.drawString(font,"允许："+(allowed.isEmpty()?"待设置":allowed),205,142,WorkstationUi.MUTED,false);
 if(!eye)g.drawString(font,"阵眼固定于核心所在位置",12,142,WorkstationUi.MUTED,false);
 }
 @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
