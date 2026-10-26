package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import java.util.*;

/** Approved dark-gold skin; every inventory cell and label remains a live control. */
public final class InscriptionScreen extends AbstractContainerScreen<EquipmentWorkstationMenu> {
    private static final int GOLD=0xFFE4C783, TEXT=0xFFF0E5C9, MUTED=0xFFB3A48A;
    private static final String[] ICONS={"guard","tough","light","control","charge"};
    private static Item[] materials(){return new Item[]{WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.INSCRIPTION_CRYSTAL.get()};}
    private static final int[] QUOTE={3,4,5,7};
    private final List<GoldButton> choices=new ArrayList<>();
    private ForgedSearchBox search;
    private GoldButton confirm;
    private float scale=1;
    public InscriptionScreen(EquipmentWorkstationMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=EquipmentWorkstationLayout.WIDTH;imageHeight=EquipmentWorkstationLayout.INSCRIPTION_HEIGHT;inventoryLabelY=-1000;}
    private static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/inscription_gold/"+name+".png");}
    private static final Map<String,int[]> SIZES=Map.ofEntries(
        Map.entry("left_clean",new int[]{343,873}),Map.entry("equipment_clean",new int[]{516,623}),Map.entry("preview_clean",new int[]{601,620}),Map.entry("inventory_clean",new int[]{1127,248}),Map.entry("confirm_fill",new int[]{63,48}),Map.entry("row_fill",new int[]{70,55}),Map.entry("row_selected_fill",new int[]{70,52}),
        Map.entry("header_title",new int[]{260,86}),Map.entry("header",new int[]{1516,111}),Map.entry("window",new int[]{1516,1000}),
        Map.entry("left_panel",new int[]{343,873}),Map.entry("equipment_panel",new int[]{516,623}),
        Map.entry("preview_panel",new int[]{601,620}),Map.entry("inventory_panel",new int[]{1127,244}),
        Map.entry("row",new int[]{316,96}),Map.entry("row_selected",new int[]{316,97}),
        Map.entry("search",new int[]{310,46}),Map.entry("confirm",new int[]{467,88}),
        Map.entry("slot",new int[]{65,52}),Map.entry("equipment_slot",new int[]{128,125}),
        Map.entry("book_slot",new int[]{70,70}),Map.entry("close",new int[]{62,61}),
        Map.entry("pity",new int[]{20,21}),Map.entry("background",new int[]{130,120}),Map.entry("search_icon",new int[]{30,31}),
        Map.entry("guard",new int[]{77,77}),Map.entry("tough",new int[]{77,77}),
        Map.entry("light",new int[]{77,75}),Map.entry("control",new int[]{77,76}),Map.entry("charge",new int[]{77,76}));
    static void sprite(GuiGraphics g,String name,int x,int y,int w,int h){
        int[] s=SIZES.get(name);boolean chrome=name.equals("header")||name.endsWith("_clean")||name.equals("close")||name.equals("pity")||name.equals("background");
        if(chrome)g.setColor(.48f,.46f,.42f,1);g.blit(tex(name),x,y,w,h,0,0,s[0],s[1],s[0],s[1]);if(chrome)g.setColor(1,1,1,1);
    }
    /** Only authored edge strips and corners are stretched; baked labels/items never enter the center. */
    static void frame(GuiGraphics g,String name,int x,int y,int w,int h){
        int[] s=SIZES.get(name);int corner=16,d=3,edge=12;
        g.fill(x,y,x+w,y+h,0xE314100A);g.setColor(.48f,.46f,.42f,1);
        // Four independent corners; long spans sample the authored edge strips, never labels.
        for(int row=0;row<2;row++)for(int col=0;col<2;col++)
            g.blit(tex(name),x+(col==0?0:w-d),y+(row==0?0:h-d),d,d,col==0?0:s[0]-corner,row==0?0:s[1]-corner,corner,corner,s[0],s[1]);
        g.blit(tex(name),x+d,y,w-2*d,d,corner,0,s[0]-2*corner,edge,s[0],s[1]);
        g.blit(tex(name),x+d,y+h-d,w-2*d,d,corner,s[1]-edge,s[0]-2*corner,edge,s[0],s[1]);
        g.blit(tex(name),x,y+d,d,h-2*d,0,corner,edge,s[1]-2*corner,s[0],s[1]);
        g.blit(tex(name),x+w-d,y+d,d,h-2*d,s[0]-edge,corner,edge,s[1]-2*corner,s[0],s[1]);g.setColor(1,1,1,1);
    }

    private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.command(action));}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-12f)/imageWidth,(height-12f)/imageHeight));
        width=(int)(width/scale);height=(int)(height/scale);super.init();
        String query=search==null?"":search.getValue();
        search=new ForgedSearchBox(font,leftPos+29,topPos+50,59,15,Component.literal("搜索铭纹"));
        search.setTextColor(TEXT);search.setHint(Component.literal("搜索铭纹"));search.setMaxLength(32);search.setValue(query);search.setResponder(s->filter());addRenderableWidget(search);
        choices.clear();
        for(int i=0;i<5;i++){final int rune=i;GoldButton b=new GoldButton(leftPos+14,topPos+72+i*20,78,18,EquipmentCrafting.NAMES[i],w->send(60+rune));b.rune=i;choices.add(addRenderableWidget(b));}
        filter();
        confirm=addRenderableWidget(new GoldButton(leftPos+198,topPos+246,146,22,"落纹 · 刻印",b->send(100)));
        GoldButton close=addRenderableWidget(new GoldButton(leftPos+334,topPos+5,18,18,"关闭",b->onClose()));close.close=true;
        GoldButton convert=addRenderableWidget(new GoldButton(leftPos+111,topPos+157,40,14,"转为凭据",b->send(103)));
        convert.setTooltip(Tooltip.create(Component.literal("旧御体附魔书转换；仅在已有旧书时可用")));
        GoldButton redeem=addRenderableWidget(new GoldButton(leftPos+155,topPos+157,32,14,"兑换",b->send(104)));
        redeem.setTooltip(Tooltip.create(Component.literal("用旧纹凭据为护甲兑换同阶灵御铭纹")));
    }
    private void filter(){String q=search.getValue().trim();int row=0;for(int i=0;i<choices.size();i++){var b=choices.get(i);b.visible=EquipmentCrafting.NAMES[i].contains(q);if(b.visible)b.setY(topPos+72+row++*20);}}
    private void label(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,s,leftPos+x,topPos+y,color,false);}
    private void centered(GuiGraphics g,String s,int x,int y,int color){g.drawCenteredString(font,s,leftPos+x,topPos+y,color);}
    private void small(GuiGraphics g,String s,int x,int y,int color){g.pose().pushPose();g.pose().translate(leftPos+x,topPos+y,0);g.pose().scale(.8f,.8f,1);g.drawString(font,s,0,0,color,false);g.pose().popPose();}
    @Override protected void containerTick(){super.containerTick();search.tick();}
    private void compactCentered(GuiGraphics g,String text,int cx,int y,int width,int color){
        float size=Math.min(.8f,width/(float)Math.max(1,font.width(text)));
        g.pose().pushPose();g.pose().translate(leftPos+cx,topPos+y,0);g.pose().scale(size,size,1);
        g.drawString(font,text,-font.width(text)/2,0,color,false);g.pose().popPose();
    }
    private void panel(GuiGraphics g,String skin,int x,int y,int w,int h){
        frame(g,skin,leftPos+x,topPos+y,w,h);g.fill(leftPos+x+3,topPos+y+3,leftPos+x+w-3,topPos+y+h-3,0xFF080907);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        frame(g,"window",x,y,imageWidth,imageHeight);sprite(g,"header",x+1,y+1,358,27);sprite(g,"header_title",x+148,y+3,64,21);
        panel(g,"left_clean",8,32,90,142);panel(g,"equipment_clean",104,32,90,142);panel(g,"preview_clean",200,32,152,142);
        panel(g,"inventory_clean",8,178,176,98);panel(g,"preview_clean",190,178,162,98);
        label(g,"铭纹选择",14,38,GOLD);frame(g,"search",x+14,y+50,78,16);sprite(g,"search_icon",x+18,y+54,8,8);
        if(choices.stream().noneMatch(button->button.visible))small(g,"无匹配铭纹",18,78,MUTED);
        label(g,"待刻之器",110,38,GOLD);frame(g,"equipment_slot",x+133,y+55,32,32);
        centered(g,menu.equipment().isEmpty()?"置入装备":font.plainSubstrByWidth(menu.equipment().getHoverName().getString(),78),149,93,TEXT);
        centered(g,"精炼 +"+EquipmentCrafting.refine(menu.equipment()),149,106,GOLD);
        centered(g,"铭纹 "+EquipmentCrafting.read(menu.equipment()).getCompound("Runes").size()+" / "+EquipmentCrafting.capacity(menu.equipment()),149,119,TEXT);
        frame(g,"book_slot",x+111,y+134,20,20);small(g,"旧纹凭据",136,139,GOLD);
        int a=Math.max(0,Math.min(4,menu.selected())),target=EquipmentCrafting.level(menu.equipment(),EquipmentCrafting.RUNES[a])+1;
        label(g,"铭纹详情",198,184,GOLD);sprite(g,ICONS[a],x+198,y+198,20,20);
        label(g,EquipmentCrafting.NAMES[a]+" · "+target+"阶",224,199,GOLD);
        small(g,switch(a){case 0->"护甲减伤 · 防御主纹";case 1->"耐久节省 · 护甲";case 2->"步履轻盈 · 靴子";case 3->"降低耗灵 · 灵剑主纹";default->"蓄势增幅 · 灵剑主纹";},224,212,TEXT);
        small(g,"主纹限一道 · 槽位随器阶",198,224,MUTED);small(g,"普通70% · 上佳25% · 极品5%",198,235,MUTED);
        label(g,"刻印预览",208,38,GOLD);sprite(g,ICONS[a],x+208,y+52,16,16);label(g,EquipmentCrafting.NAMES[a]+" · "+target+"阶",230,55,GOLD);
        label(g,"成功率",208,73,TEXT);String chance=menu.value(2)+"%";label(g,chance,344-font.width(chance),73,GOLD);
        frame(g,"search",x+208,y+85,136,6);int fill=Math.max(0,Math.min(100,menu.value(2)))*132/100;g.fill(x+210,y+87,x+210+fill,y+89,0xFFE5BA55);
        small(g,target==1?"首次刻印必成":"失败积累保底 · 旧纹保留",208,94,MUTED);
        label(g,"所需材料（行囊扣除）",208,103,GOLD);Item[] costs=materials();String[] names={"灵石","玄铁","青金石","铭刻水晶"};
        for(int i=0;i<4;i++){
            int sx=213+i*34;frame(g,"slot",x+sx,y+114,24,24);g.renderItem(new ItemStack(costs[i]),x+sx+4,y+118);
            final Item material=costs[i];int owned=i==0?(int)Math.min(Integer.MAX_VALUE,SpiritCurrency.value(minecraft.player.getInventory().items)):minecraft.player.getInventory().items.stream().filter(st->st.is(material)).mapToInt(ItemStack::getCount).sum();
            compactCentered(g,owned+"/"+menu.value(QUOTE[i]),sx+12,140,32,owned>=menu.value(QUOTE[i])?TEXT:0xFFE3A28C);
            compactCentered(g,names[i],sx+12,152,32,MUTED);
        }
        small(g,"灵力 "+menu.value(8),208,164,TEXT);
        int fails=EquipmentCrafting.read(menu.equipment()).getCompound("RunePity").getInt(EquipmentCrafting.RUNES[a]+target);small(g,"连败 "+fails,281,164,MUTED);
        for(int i=0;i<10;i++){sprite(g,"pity",x+313+i*3,y+164,3,4);if(i<fails)g.fill(x+313+i*3,y+165,x+315+i*3,y+167,GOLD);}
        confirm.active=menu.value(9)==1;
        confirm.setTooltip(Tooltip.create(Component.literal(confirm.active?"灵韵：普通70% / 上佳25% / 极品5%\n失败消耗材料，装备与旧铭纹保留":EquipmentCrafting.REASONS.get(Math.max(0,Math.min(EquipmentCrafting.REASONS.size()-1,menu.value(10)))))));
        label(g,"行囊",15,182,GOLD);for(int i=2;i<menu.slots.size();i++){var slot=menu.getSlot(i);frame(g,"slot",x+slot.x-1,y+slot.y-1,18,18);}
        for(var button:choices)button.selected=button.rune==a;
        // Retain the legacy controls visibly, but never imply that a new book can be inserted.
        for(var widget:renderables)if(widget instanceof GoldButton button&&!button.close&&button.rune<0&&button!=confirm){button.visible=true;button.active=!menu.book().isEmpty()&&!menu.equipment().isEmpty();}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        try{renderBackground(g);super.render(g,(int)(mx/scale),(int)(my/scale),partial);
            if(!menu.equipment().isEmpty()&&menu.getCarried().isEmpty()){
                g.pose().pushPose();g.pose().translate(0,0,250);frame(g,"equipment_slot",leftPos+133,topPos+55,32,32);
                g.pose().translate(leftPos+137,topPos+59,0);g.pose().scale(1.5f,1.5f,1);g.renderItem(menu.equipment(),0,0);g.pose().popPose();
            }
        }finally{g.pose().popPose();}
        renderTooltip(g,mx,my);
    }
    @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){
        var equipment=menu.getSlot(0);
        if(x==equipment.x&&y==equipment.y&&w==16&&h==16)return mx>=leftPos+133&&mx<leftPos+165&&my>=topPos+55&&my<topPos+87;
        return super.isHovering(x,y,w,h,mx,my);
    }
    @Override public boolean mouseClicked(double x,double y,int b){return super.mouseClicked(x/scale,y/scale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased(x/scale,y/scale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged(x/scale,y/scale,b,dx/scale,dy/scale);}
    @Override public boolean mouseScrolled(double x,double y,double delta){return super.mouseScrolled(x/scale,y/scale,delta);}
    @Override public boolean keyPressed(int key,int scan,int mods){if(search.isFocused()&&key!=256&&key!=258){return search.keyPressed(key,scan,mods);}return super.keyPressed(key,scan,mods);}
    private static final class GoldButton extends Button {
        int rune=-1;boolean selected,close;
        GoldButton(int x,int y,int w,int h,String s,OnPress press){super(x,y,w,h,Component.literal(s),press,DEFAULT_NARRATION);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
            if(close){sprite(g,"close",getX(),getY(),width,height);if(isHoveredOrFocused())g.renderOutline(getX()+1,getY()+1,width-2,height-2,0xFFA28B60);return;}
            String skin=rune>=0?(selected||isHoveredOrFocused()?"row_selected":"row"):"confirm";
            frame(g,skin,getX(),getY(),width,height);
            sprite(g,rune>=0?(selected||isHoveredOrFocused()?"row_selected_fill":"row_fill"):"confirm_fill",getX()+4,getY()+4,width-8,height-8);
            if(selected||active&&isHoveredOrFocused())g.fill(getX()+4,getY()+4,getX()+width-4,getY()+height-4,0x443F2B0B);
            if(rune>=0){sprite(g,ICONS[rune],getX()+2,getY()+1,16,16);g.drawString(Minecraft.getInstance().font,getMessage(),getX()+24,getY()+(height-9)/2,active?GOLD:MUTED,false);}
            else g.drawCenteredString(Minecraft.getInstance().font,getMessage(),getX()+width/2,getY()+(height-9)/2,active?(isHoveredOrFocused()?GOLD:TEXT):MUTED);
            if(active&&isHovered())g.renderOutline(getX()+1,getY()+1,width-2,height-2,0xFFA28B60);
            if(!active)g.fill(getX()+3,getY()+3,getX()+width-3,getY()+height-3,0x77000000);
            if(isFocused())g.renderOutline(getX()+2,getY()+2,width-4,height-4,0xFFA28B60);
        }
    }
}
