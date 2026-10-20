package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.robe.RobeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RobeScreen extends AbstractContainerScreen<RobeMenu> {
    public RobeScreen(RobeMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=147;inventoryLabelY=53;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){
        CultivationUi.paper(graphics,leftPos,topPos,imageWidth,imageHeight);
        for(var slot:menu.slots)CultivationUi.slot(graphics,leftPos+slot.x-1,topPos+slot.y-1,18,18,CultivationUi.BORDER);
        if(!menu.getSlot(0).hasItem())graphics.blit(new net.minecraft.resources.ResourceLocation("wuxingxiantu","textures/item/robe_slot_empty.png"),leftPos+53,topPos+30,0,0,16,16,16,16);
        graphics.drawCenteredString(font,Component.translatable("slot.wuxingxiantu.robe"),leftPos+61,topPos+19,0xDED5BA);
        graphics.drawCenteredString(font,Component.translatable("slot.wuxingxiantu.yujian"),leftPos+115,topPos+19,0xDED5BA);
        if(minecraft.player!=null&&cn.blockforge.generated.generatedmod.robe.YujianSlot.locked(minecraft.player))
            graphics.fill(leftPos+107,topPos+47,leftPos+123,topPos+49,0xFFBFA65B);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){renderBackground(graphics);super.render(graphics,mouseX,mouseY,partial);renderTooltip(graphics,mouseX,mouseY);}
}
