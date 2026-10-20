package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Borderless vanilla fields draw at y rather than centering; align all field contents together. */
public final class ForgedSearchBox extends EditBox {
    private final Font font;
    public ForgedSearchBox(Font font,int x,int y,int w,int h,Component label){super(font,x,y,w,h,label);this.font=font;setBordered(false);}
    @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial){
        int offset=Math.max(0,getHeight()-font.lineHeight-2);
        g.pose().pushPose();g.pose().translate(0,offset,0);
        try{super.renderWidget(g,mx,my-offset,partial);}finally{g.pose().popPose();}
    }
}
