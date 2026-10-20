package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.HerbProcessingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Code-native interface: no new unreviewed texture. */
public final class HerbProcessingTableScreen extends AbstractContainerScreen<HerbProcessingTableMenu> {
    public HerbProcessingTableScreen(HerbProcessingTableMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=188;inventoryLabelY=94;}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;g.fill(x-1,y-1,x+177,y+189,0xFF547C80);g.fill(x,y,x+176,y+188,0xFF10232C);
        slot(g,x+44,y+38);slot(g,x+116,y+38);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+8+col*18,y+106+row*18);
        for(int col=0;col<9;col++)slot(g,x+8+col*18,y+164);
        g.fill(x+69,y+44,x+107,y+48,0xFF28404B);g.fill(x+69,y+44,x+69+38*menu.progress()/100,y+48,0xFF79D4A4);
        g.drawString(font,Component.translatable("gui.wuxingxiantu.herb_processing_table.input"),x+37,y+25,0xFFD1E9DD,false);
        g.drawString(font,Component.translatable("gui.wuxingxiantu.herb_processing_table.output"),x+108,y+25,0xFFD1E9DD,false);
        g.drawString(font,Component.translatable("gui.wuxingxiantu.herb_processing_table.time"),x+22,y+65,0xFF9CBDB5,false);
        g.drawString(font,Component.translatable("gui.wuxingxiantu.herb_processing_table.bonus"),x+10,y+80,0xFF9CBDB5,false);
    }
    private void slot(GuiGraphics g,int x,int y){g.fill(x-1,y-1,x+17,y+17,0xFF547C80);g.fill(x,y,x+16,y+16,0xFF07171E);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);super.render(g,x,y,partial);renderTooltip(g,x,y);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xD8ECE6,false);g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xA8C6C0,false);}
}
