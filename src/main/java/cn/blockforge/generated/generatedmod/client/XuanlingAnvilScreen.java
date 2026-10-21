package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.XuanlingAnvilMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;

/** Plain blue interface; native menu slots and rename packets remain intact. */
public final class XuanlingAnvilScreen extends AnvilScreen {
    private static final int TEMPLATE_PREVIEW_X=9;
    private static final int TEMPLATE_ROWS=7, TEMPLATE_HEIGHT=18, TEMPLATE_STRIDE=20;
    private int templatePage;
    private String templateQuery="";
    private net.minecraft.client.gui.components.EditBox templateSearch;
    private final java.util.List<net.minecraft.client.gui.components.AbstractWidget> templateWidgets=new java.util.ArrayList<>();
    private record TemplateEntry(int index,net.minecraft.world.item.ItemStack item,String label){}
    private java.util.List<TemplateEntry> matchingTemplates(){
        var items=((XuanlingAnvilMenu)menu).recordedTemplates();
        var matches=new java.util.ArrayList<TemplateEntry>();
        String query=templateQuery.strip().toLowerCase(java.util.Locale.ROOT);
        for(int i=0;i<items.size();i++){
            var item=items.get(i);var lines=item.getTooltipLines(minecraft.player,net.minecraft.world.item.TooltipFlag.NORMAL);
            String label=lines.size()>1?lines.get(1).getString():item.getHoverName().getString();
            if(query.isEmpty()||label.toLowerCase(java.util.Locale.ROOT).contains(query)
                ||item.getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(query))matches.add(new TemplateEntry(i,item,label));
        }
        return matches;
    }
    public XuanlingAnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);titleLabelX=8;imageHeight+=40;inventoryLabelY+=40;
    }
    @Override protected void subInit(){
        leftPos+=63; // Center the combined 126px sidebar and native 176px panel.
        super.subInit();
        // Keep the selected template separate from the compact operation row.
        // Move the original slots, preserving their inventory/recipe behavior.
        for(int i=0;i<3;i++)menu.getSlot(i).x=49+i*42;
        // Vanilla subInit computes its own centered X instead of using leftPos.
        for(var child:children())if(child instanceof net.minecraft.client.gui.components.EditBox field){field.setX(leftPos+62);field.setY(topPos+24);}
        modeButton(leftPos+8,"修复 / 附魔",0,77);
        var forge=modeButton(leftPos+91,"锻造",1,77);forge.active=!((XuanlingAnvilMenu)menu).recordedTemplates().isEmpty();
        templateWidgets.clear();
        templateSearch=new net.minecraft.client.gui.components.EditBox(font,leftPos-120,topPos+22,114,14,Component.literal("搜索模板")){
            @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial){
                g.fill(getX()-2,getY()-2,getX()+getWidth()+2,getY()+getHeight()+2,0xFF0B1624);
                g.renderOutline(getX()-2,getY()-2,getWidth()+4,getHeight()+4,isFocused()?0xFF80D9F2:0xFF365C7C);
                g.pose().pushPose();
                g.pose().translate(0,getHeight()-font.lineHeight,0);
                super.renderWidget(g,mx,my,partial);
                g.pose().popPose();
            }
        };
        templateSearch.setBordered(false);
        templateSearch.setMaxLength(64);templateSearch.setHint(Component.literal("搜索模板…"));
        templateSearch.setTextColor(0xADD6F0);templateSearch.setTextColorUneditable(0x617E95);
        templateSearch.setValue(templateQuery);
        templateSearch.setResponder(value->{templateQuery=value;templatePage=0;rebuildTemplatePage();});
        addRenderableWidget(templateSearch);
        rebuildTemplateButtons();
    }
    private void rebuildTemplateButtons(){
        var anvil=(XuanlingAnvilMenu)menu;var templates=matchingTemplates();templatePage=Math.min(templatePage,Math.max(0,(templates.size()-1)/TEMPLATE_ROWS));int first=templatePage*TEMPLATE_ROWS;
        for(int row=0;row<TEMPLATE_ROWS&&first+row<templates.size();row++){
            var entry=templates.get(first+row);int index=entry.index();var item=entry.item();String label=entry.label();String compact=font.plainSubstrByWidth(label,85);if(!compact.equals(label))compact=font.plainSubstrByWidth(label,79)+"…";final String rowLabel=compact;
            var templateButton=new net.minecraft.client.gui.components.Button(leftPos-120,topPos+40+row*TEMPLATE_STRIDE,114,TEMPLATE_HEIGHT,Component.literal(label),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,index+2),supplier->supplier.get()){
                @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
                    boolean selected=net.minecraft.world.item.ItemStack.isSameItemSameTags(anvil.selectedTemplate().stack(),item);g.fill(getX(),getY(),getX()+width,getY()+height,selected?0xFF294E70:isHoveredOrFocused()?0xFF203D57:0xFF132B40);g.renderOutline(getX(),getY(),width,height,selected?0xFF80D9F2:0xFF36536E);
                    g.renderItem(item,getX()+3,getY()+1);g.drawString(font,rowLabel,getX()+23,getY()+5,0xADD6F0,false);
                }
            };
            templateButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(label)));
            templateWidgets.add(addRenderableWidget(templateButton));
        }
        if(templates.size()>TEMPLATE_ROWS){
            var previous=pageButton(leftPos-120,"‹",()->{templatePage--;rebuildTemplatePage();});
            previous.active=templatePage>0;
            var next=pageButton(leftPos-34,"›",()->{templatePage++;rebuildTemplatePage();});
            next.active=first+TEMPLATE_ROWS<templates.size();
        }
    }
    private net.minecraft.client.gui.components.Button pageButton(int x,String label,Runnable action){
        var button=new net.minecraft.client.gui.components.Button(x,topPos+imageHeight-24,28,18,Component.literal(label),b->action.run(),supplier->supplier.get()){
            @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
                g.fill(getX(),getY(),getX()+width,getY()+height,!active?0xFF101E2F:isHoveredOrFocused()?0xFF294E70:0xFF19354E);
                g.renderOutline(getX(),getY(),width,height,!active?0xFF2C4358:isHoveredOrFocused()?0xFF80D9F2:0xFF517DA1);
                g.drawCenteredString(font,getMessage(),getX()+width/2,getY()+5,active?0xD8ECFF:0x617E95);
            }
        };
        templateWidgets.add(addRenderableWidget(button));return button;
    }
    private void rebuildTemplatePage(){
        for(var widget:templateWidgets)removeWidget(widget);
        templateWidgets.clear();rebuildTemplateButtons();
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(templateSearch!=null&&templateSearch.isFocused()&&key!=256){
            if(templateSearch.keyPressed(key,scan,modifiers)||templateSearch.canConsumeInput())return true;
        }
        return super.keyPressed(key,scan,modifiers);
    }
    private boolean overTemplatePreview(double x,double y){
        int px=leftPos+TEMPLATE_PREVIEW_X-1,py=topPos+menu.getSlot(0).y-1;
        return x>=px&&x<px+18&&y>=py&&y<py+18;
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        // Display-only: this is not an inventory slot and must never accept items.
        return overTemplatePreview(x,y)||super.mouseClicked(x,y,button);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.render(g,mx,my,partial);
        var template=((XuanlingAnvilMenu)menu).selectedTemplate().stack();
        if(overTemplatePreview(mx,my)&&!template.isEmpty())g.renderTooltip(font,template,mx,my);
    }
    @Override protected boolean hasClickedOutside(double mouseX,double mouseY,int guiLeft,int guiTop,int button){
        // The sidebar is part of this container: clicking its heading must not
        // throw an item currently carried by the mouse onto the ground.
        return mouseX<leftPos-126||mouseX>=leftPos+imageWidth||mouseY<topPos||mouseY>=topPos+imageHeight;
    }
    private net.minecraft.client.gui.components.Button modeButton(int x,String label,int mode,int width){
        return addRenderableWidget(new net.minecraft.client.gui.components.Button(x,topPos+87,width,18,Component.literal(label),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,mode),supplier->supplier.get()){
            @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
                boolean selected=mode==(((XuanlingAnvilMenu)menu).selectedTemplate().stack().isEmpty()?0:1);
                g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),!active?0xFF101E2F:selected?0xFF294E70:isHoveredOrFocused()?0xFF203D57:0xFF19354E);
                g.renderOutline(getX(),getY(),getWidth(),getHeight(),!active?0xFF2C4358:selected?0xFF80D9F2:0xFF517DA1);
                g.drawCenteredString(font,getMessage(),getX()+getWidth()/2,getY()+5,active?0xD8ECFF:0x617E95);
            }
        });
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;
        g.fill(x-126,y,x-4,y+imageHeight,0xFF101E2F);g.renderOutline(x-126,y,122,imageHeight,0xFF38658A);
        g.drawString(font,"已吸收模板",x-118,y+8,0xD8ECFF,false);
        if(!templateQuery.isBlank()&&matchingTemplates().isEmpty())g.drawString(font,"未找到匹配模板",x-118,y+48,0xACC9E1,false);
        if(((XuanlingAnvilMenu)menu).recordedTemplates().isEmpty()){
            g.drawString(font,"手持模板右键",x-118,y+48,0xACC9E1,false);g.drawString(font,"消耗一份吸收",x-118,y+61,0xACC9E1,false);
        }
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFF101E2F);
        g.renderOutline(x,y,imageWidth,imageHeight,0xFF38658A);
        g.fill(x+1,y+1,x+imageWidth-1,y+17,0xFF1B3550);
        g.fill(x+8,y+122,x+imageWidth-8,y+123,0xFF2C4B68);
        g.fill(x+59,y+20,x+168,y+37,0xFF0B1624);
        g.renderOutline(x+59,y+20,109,17,0xFF365C7C);
        g.drawString(font,"名称",x+27,y+24,0xFFACC9E1,false);
        for(var slot:menu.slots){
            if(!slot.isActive())continue;
            g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xFF0A1523);
            g.renderOutline(x+slot.x-1,y+slot.y-1,18,18,0xFF36536E);
        }
        int previewX=x+TEMPLATE_PREVIEW_X,previewY=y+menu.getSlot(0).y;
        g.drawString(font,"模板",previewX+(16-font.width("模板"))/2,previewY+21,0xFFACC9E1,false);
        g.fill(previewX-1,previewY-1,previewX+17,previewY+17,0xFF0A1523);
        g.renderOutline(previewX-1,previewY-1,18,18,0xFF517DA1);
        var selected=((XuanlingAnvilMenu)menu).selectedTemplate().stack();
        if(!selected.isEmpty())g.renderItem(selected,previewX,previewY);
        g.drawString(font,"+",separatorX(0,"+"),y+51,0xFF91BADD,false);
        g.drawString(font,"→",separatorX(1,"→"),y+51,0xFF91BADD,false);
        renderErrorIcon(g,x,y);
    }
    private int separatorX(int leftSlot,String symbol){
        return leftPos+(menu.getSlot(leftSlot).x+16+menu.getSlot(leftSlot+1).x-font.width(symbol))/2;
    }
    @Override protected void renderErrorIcon(GuiGraphics g,int x,int y){
        if((menu.getSlot(0).hasItem()||menu.getSlot(1).hasItem())&&!menu.getSlot(2).hasItem())
            g.drawString(font,"×",separatorX(1,"×"),y+51,0xFFFF9292,false);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xD8ECFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xACC9E1, false);
        XuanlingAnvilMenu anvil = (XuanlingAnvilMenu)menu;
        if (anvil.getCost() <= 0) return;
        Component text;
        int color = 0x8ACDFF;
        if (anvil.damage() >= 3) { text = Component.translatable("gui.wuxingxiantu.anvil_exhausted"); color = 0xFF9292; }
        else if (anvil.getCost() >= 40 && !minecraft.player.getAbilities().instabuild) {
            text = Component.translatable("container.repair.expensive"); color = 0xFF9292;
        } else {
            text = Component.translatable("gui.wuxingxiantu.anvil_price", anvil.manaCost());
            var data = GeneratedMod.data(minecraft.player);
            if (!minecraft.player.getAbilities().instabuild && (data == null || data.wuxingxiantu$getAura() < anvil.manaCost())) color = 0xFF9292;
        }
        graphics.drawString(font, text, imageWidth - 8 - font.width(text), 66, color, false);
    }
}
