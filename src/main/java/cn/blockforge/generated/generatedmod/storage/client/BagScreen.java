package cn.blockforge.generated.generatedmod.storage.client;

import cn.blockforge.generated.generatedmod.Network;


import cn.blockforge.generated.generatedmod.plants.HerbAge;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import cn.blockforge.generated.generatedmod.storage.BagFilter;
import cn.blockforge.generated.generatedmod.storage.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/** Original Xiantu bag appearance with the integrated server-owned inventory unchanged. */
public final class BagScreen extends AbstractContainerScreen<BagMenu> {
    private EditBox search;
    private MagnetSwitch magnet;
    private int category;
    private boolean dragging;
    private long pendingAt;
    public BagScreen(BagMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title);imageWidth=300;imageHeight=242;inventoryLabelY=146;
    }
    @Override protected void init() {
        String query=search==null?"":search.getValue();
        super.init();
        search=new EditBox(font,leftPos+26,topPos+26,86,10,Component.literal("搜索"));
        search.setBordered(false);search.setTextColor(XiantuBagUi.TEXT);search.setTextColorUneditable(XiantuBagUi.MUTED);
        search.setMaxLength(80);search.setHint(Component.literal("名称或物品ID"));
        search.setValue(query);
        search.setResponder(value->pendingAt=System.currentTimeMillis()+200);addRenderableWidget(search);
        addRenderableWidget(XiantuBagUi.button(Component.literal("分类："+BagFilter.LABELS[category]),button->{
            category=(category+1)%BagFilter.LABELS.length;button.setMessage(Component.literal("分类："+BagFilter.LABELS[category]));sendFilter();
        }).bounds(leftPos+122,topPos+19,168,20).build());
        addRenderableWidget(XiantuBagUi.button(Component.literal("整理同类物品"),button->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,2)).bounds(leftPos+194,topPos+158,96,20).build());
        magnet=addRenderableWidget(new MagnetSwitch(leftPos+194,topPos+216));
        magnet.setTooltip(Tooltip.create(Component.literal("默认关闭；开启后收取周围9×9范围内允许拾取的物品")));
    }
    private void sendFilter() {pendingAt=0;Network.CHANNEL.sendToServer(new BagFilterPacket(menu.containerId,search.getValue(),category));}
    @Override protected void containerTick() {
        super.containerTick();search.tick();magnet.setMessage(Component.literal(menu.info.get(2)!=0?"磁铁：开启":"磁铁：关闭"));
        if(pendingAt!=0&&System.currentTimeMillis()>=pendingAt)sendFilter();
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY) {
        int x=leftPos,y=topPos;XiantuBagUi.panel(graphics,x,y,300,242);
        graphics.fill(x+8,y+19,x+116,y+39,XiantuBagUi.DEEP);
        XiantuBagUi.frame(graphics,x+8,y+19,108,20,XiantuBagUi.GOLD);
        graphics.blit(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/forged/search_icon.png"),x+13,y+24,9,9,0,0,25,25,25,25);
        for(var slot:menu.slots)XiantuBagUi.slot(graphics,x+slot.x-1,y+slot.y-1);
        graphics.drawString(font,"随身工作台",x+194,y+43,XiantuBagUi.GOLD,false);
        graphics.drawString(font,"滚轮或拖动查看",x+194,y+130,XiantuBagUi.GOLD,false);
        int max=Math.max(0,menu.info.get(1)-1),thumb=Math.max(12,108*6/(max+6));
        int scrollY=y+40+(max==0?0:(108-thumb)*menu.info.get(0)/max);
        graphics.fill(x+176,y+40,x+186,y+148,XiantuBagUi.DEEP);
        graphics.fill(x+177,scrollY,x+185,scrollY+thumb,0xFFB8A273);
        graphics.drawString(font,"Shift：快速存取",x+194,y+188,XiantuBagUi.MUTED,false);
        graphics.drawString(font,"草药保留原年份",x+194,y+203,XiantuBagUi.MUTED,false);
    }
    private void moveScroll(double y) {
        int max=Math.max(0,menu.info.get(1)-1),thumb=Math.max(12,108*6/(max+6));
        double fraction=(y-topPos-40-thumb/2.0)/Math.max(1,108-thumb);
        int row=(int)Math.round(Math.max(0,Math.min(1,fraction))*max);
        Network.CHANNEL.sendToServer(new BagScrollPacket(menu.containerId,row));
    }
    @Override public boolean mouseScrolled(double x,double y,double delta) {
        if(x>=leftPos+7&&x<=leftPos+186&&y>=topPos+39&&y<=topPos+148) {
            int row=Math.max(0,Math.min(Math.max(0,menu.info.get(1)-1),menu.info.get(0)-(int)Math.round(delta*3)));
            Network.CHANNEL.sendToServer(new BagScrollPacket(menu.containerId,row));return true;
        }return super.mouseScrolled(x,y,delta);
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(button==0&&x>=leftPos+176&&x<=leftPos+186&&y>=topPos+40&&y<=topPos+148){dragging=true;moveScroll(y);return true;}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy) {
        if(dragging){moveScroll(y);return true;}return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button) {
        if(dragging){dragging=false;return true;}return super.mouseReleased(x,y,button);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial) {
        renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);
        graphics.pose().pushPose();graphics.pose().translate(0,0,300);
        for(int i=0;i<54;i++)if(menu.totals[i]>1) {
            var slot=menu.slots.get(i+1);String label=countLabel(menu.totals[i]);
            graphics.drawString(font,label,leftPos+slot.x+17-font.width(label),topPos+slot.y+9,0xFFFFFFFF,true);
        }
        graphics.pose().popPose();renderTooltip(graphics,mouseX,mouseY);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY) {
        graphics.drawString(font,title,8,6,XiantuBagUi.GOLD,false);graphics.drawString(font,playerInventoryTitle,8,146,XiantuBagUi.TEXT,false);
    }
    private static String countLabel(long n) {
        if(n<1000)return Long.toString(n);if(n<1000000)return(n/1000)+"K";if(n<1000000000L)return(n/1000000)+"M";
        if(n<1000000000000L)return(n/1000000000)+"B";return n<1000000000000000L?(n/1000000000000L)+"T":">999T";
    }
    @Override protected void renderTooltip(GuiGraphics graphics,int x,int y) {
        if(hoveredSlot!=null&&hoveredSlot.index>=1&&hoveredSlot.index<=54&&hoveredSlot.hasItem()&&menu.getCarried().isEmpty()) {
            var lines=new java.util.ArrayList<Component>(getTooltipFromContainerItem(hoveredSlot.getItem()));
            lines.add(Component.literal("袋内总数量："+menu.totals[hoveredSlot.index-1]));
            if(HerbAge.definition(hoveredSlot.getItem())!=null)lines.add(Component.literal("按年份从小到大取出，保留原年份"));
            graphics.renderComponentTooltip(font,lines,x,y);
        }else super.renderTooltip(graphics,x,y);
    }
    private final class MagnetSwitch extends AbstractButton {
        private final net.minecraft.resources.ResourceLocation sprite=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "wuxingxiantu","textures/gui/wuxia/settings/toggle_jade_v2.png");
        MagnetSwitch(int x,int y) { super(x,y,96,20,Component.literal("磁铁：关闭")); }
        @Override public void onPress() { minecraft.gameMode.handleInventoryButtonClick(menu.containerId,3); }
        @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) { defaultButtonNarrationText(output); }
        @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial) {
            boolean on=menu.info.get(2)!=0;int x=getX(),y=getY();
            g.fill(x,y,x+96,y+20,XiantuBagUi.DEEP);
            XiantuBagUi.frame(g,x,y,96,20,isHoveredOrFocused()?XiantuBagUi.BRIGHT:XiantuBagUi.LINE);
            g.drawString(font,on?"磁铁 开":"磁铁 关",x+5,y+6,active?(on?XiantuBagUi.JADE:XiantuBagUi.TEXT):XiantuBagUi.MUTED,false);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            float tint=active?1F:.55F;g.setColor(tint,tint,tint,1);
            g.blit(sprite,x+64,y+4,26,12,710,on?187:460,432,200,1855,848);
            g.setColor(1,1,1,1);com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if(search.isFocused()&&key!=256){if(search.keyPressed(key,scan,modifiers))return true;return key==69||super.keyPressed(key,scan,modifiers);}
        return super.keyPressed(key,scan,modifiers);
    }
}