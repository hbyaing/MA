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
    public InscriptionScreen(EquipmentWorkstationMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=512;imageHeight=368;inventoryLabelY=-1000;}
    private static ResourceLocation tex(String name){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/inscription_gold/"+name+".png");}
    private static final Map<String,int[]> SIZES=Map.ofEntries(
        Map.entry("left_clean",new int[]{343,873}),Map.entry("equipment_clean",new int[]{516,623}),Map.entry("preview_clean",new int[]{601,620}),Map.entry("inventory_clean",new int[]{1127,248}),Map.entry("confirm_fill",new int[]{63,48}),Map.entry("row_fill",new int[]{70,55}),Map.entry("row_selected_fill",new int[]{70,52}),
        Map.entry("header",new int[]{1516,111}),Map.entry("window",new int[]{1516,1000}),
        Map.entry("left_panel",new int[]{343,873}),Map.entry("equipment_panel",new int[]{516,623}),
        Map.entry("preview_panel",new int[]{601,620}),Map.entry("inventory_panel",new int[]{1127,244}),
        Map.entry("row",new int[]{316,96}),Map.entry("row_selected",new int[]{316,97}),
        Map.entry("search",new int[]{310,46}),Map.entry("confirm",new int[]{467,88}),
        Map.entry("slot",new int[]{65,52}),Map.entry("equipment_slot",new int[]{128,125}),
        Map.entry("book_slot",new int[]{70,70}),Map.entry("close",new int[]{62,61}),
        Map.entry("pity",new int[]{20,21}),Map.entry("background",new int[]{130,120}),Map.entry("search_icon",new int[]{30,31}),
        Map.entry("guard",new int[]{77,77}),Map.entry("tough",new int[]{77,77}),
        Map.entry("light",new int[]{77,75}),Map.entry("control",new int[]{77,76}),Map.entry("charge",new int[]{77,76}));
    private static void sprite(GuiGraphics g,String name,int x,int y,int w,int h){int[] s=SIZES.get(name);g.blit(tex(name),x,y,w,h,0,0,s[0],s[1],s[0],s[1]);}
    /** Only authored edge strips and corners are stretched; baked labels/items never enter the center. */
    private static void frame(GuiGraphics g,String name,int x,int y,int w,int h){
        int[] s=SIZES.get(name);int corner=16,d=3,edge=12;
        g.fill(x,y,x+w,y+h,0xE314100A);
        // Four independent corners; long spans sample only the outer 4 source pixels, never labels.
        for(int row=0;row<2;row++)for(int col=0;col<2;col++)
            g.blit(tex(name),x+(col==0?0:w-d),y+(row==0?0:h-d),d,d,col==0?0:s[0]-corner,row==0?0:s[1]-corner,corner,corner,s[0],s[1]);
        g.blit(tex(name),x+d,y,w-2*d,d,corner,0,s[0]-2*corner,edge,s[0],s[1]);
        g.blit(tex(name),x+d,y+h-d,w-2*d,d,corner,s[1]-edge,s[0]-2*corner,edge,s[0],s[1]);
        g.blit(tex(name),x,y+d,d,h-2*d,0,corner,edge,s[1]-2*corner,s[0],s[1]);
        g.blit(tex(name),x+w-d,y+d,d,h-2*d,s[0]-edge,corner,edge,s[1]-2*corner,s[0],s[1]);
    }

    private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.command(action));}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-12f)/imageWidth,(height-12f)/imageHeight));
        width=(int)(width/scale);height=(int)(height/scale);super.init();
        String query=search==null?"":search.getValue();
        search=new ForgedSearchBox(font,leftPos+35,topPos+66,77,15,Component.literal("搜索铭纹"));
        search.setTextColor(TEXT);search.setHint(Component.literal("搜索铭纹"));search.setMaxLength(32);search.setValue(query);search.setResponder(s->filter());addRenderableWidget(search);
        choices.clear();
        for(int i=0;i<5;i++){final int rune=i;GoldButton b=new GoldButton(leftPos+12,topPos+87+i*36,106,33,EquipmentCrafting.NAMES[i],w->send(60+rune));b.rune=i;choices.add(addRenderableWidget(b));}
        filter();
        confirm=addRenderableWidget(new GoldButton(leftPos+320,topPos+211,166,27,"落纹 · 刻印",b->send(100)));
        GoldButton close=addRenderableWidget(new GoldButton(leftPos+479,topPos+8,20,20,"关闭",b->onClose()));close.close=true;
        GoldButton convert=addRenderableWidget(new GoldButton(leftPos+187,topPos+171,42,16,"转为凭据",b->send(103)));
        convert.setTooltip(Tooltip.create(Component.literal("旧御体附魔书转换；仅在已有旧书时可用")));
        GoldButton redeem=addRenderableWidget(new GoldButton(leftPos+231,topPos+171,42,16,"兑换",b->send(104)));
        redeem.setTooltip(Tooltip.create(Component.literal("用旧纹凭据为护甲兑换同阶灵御铭纹")));
    }
    private void filter(){String q=search.getValue().trim();int row=0;for(int i=0;i<choices.size();i++){var b=choices.get(i);b.visible=EquipmentCrafting.NAMES[i].contains(q);if(b.visible)b.setY(topPos+87+row++*36);}}
    private void label(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,s,leftPos+x,topPos+y,color,false);}
    private void centered(GuiGraphics g,String s,int x,int y,int color){g.drawCenteredString(font,s,leftPos+x,topPos+y,color);}
    private void small(GuiGraphics g,String s,int x,int y,int color){g.pose().pushPose();g.pose().translate(leftPos+x,topPos+y,0);g.pose().scale(.8f,.8f,1);g.drawString(font,s,0,0,color,false);g.pose().popPose();}
    @Override protected void containerTick(){super.containerTick();search.tick();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        sprite(g,"background",x,y,512,368);frame(g,"window",x,y,512,368);sprite(g,"header",x+1,y+1,510,37);
        sprite(g,"left_clean",x+5,y+41,119,322);sprite(g,"equipment_clean",x+127,y+41,171,222);
        sprite(g,"preview_clean",x+301,y+41,205,222);sprite(g,"inventory_clean",x+127,y+266,379,97);
        g.fill(x+8,y+44,x+121,y+360,0x66000000);g.fill(x+130,y+44,x+295,y+260,0x44000000);g.fill(x+304,y+44,x+503,y+260,0x99000000);g.fill(x+130,y+269,x+503,y+360,0x88000000);
        // Scenic region is deliberately text-free; all copy and changing state are drawn below.
        
        label(g,"铭纹选择",13,49,GOLD);frame(g,"search",x+13,y+65,105,18);sprite(g,"search_icon",x+17,y+69,10,10);
        label(g,"待刻之器",140,49,GOLD);frame(g,"equipment_slot",x+192,y+78,40,40);
        centered(g,menu.equipment().isEmpty()?"置入装备":font.plainSubstrByWidth(menu.equipment().getHoverName().getString(),150),212,122,TEXT);
        centered(g,"精炼 +"+EquipmentCrafting.refine(menu.equipment()),212,135,GOLD);
        centered(g,"铭纹 "+EquipmentCrafting.read(menu.equipment()).getCompound("Runes").size()+" / "+EquipmentCrafting.capacity(menu.equipment()),212,149,TEXT);
        label(g,"旧纹凭据",141,161,GOLD);frame(g,"book_slot",x+162,y+171,22,22);
        small(g,"旧御体附魔书兑换",141,196,MUTED);
        int a=Math.max(0,Math.min(4,menu.selected())),target=EquipmentCrafting.level(menu.equipment(),EquipmentCrafting.RUNES[a])+1;
        label(g,"铭纹详情",140,209,GOLD);sprite(g,ICONS[a],x+139,y+225,26,26);
        label(g,EquipmentCrafting.NAMES[a]+" · "+target+"阶",171,225,GOLD);
        small(g,switch(a){case 0->"护甲减伤 · 防御主纹";case 1->"耐久节省 · 护甲";case 2->"步履轻盈 · 靴子";case 3->"降低耗灵 · 灵剑主纹";default->"蓄势增幅 · 灵剑主纹";},171,240,TEXT);
        label(g,"刻印预览",314,49,GOLD);sprite(g,ICONS[a],x+314,y+65,24,24);label(g,EquipmentCrafting.NAMES[a]+" · "+target+"阶",344,71,GOLD);
        label(g,"成功率",314,96,TEXT);label(g,menu.value(2)+"%",469,96,GOLD);
        frame(g,"search",x+313,y+108,181,7);int fill=Math.max(0,Math.min(100,menu.value(2)))*177/100;g.fill(x+315,y+110,x+315+fill,y+113,0xFFE5BA55);
        small(g,target==1?"首次刻印必成":"失败积累保底 · 旧纹保留",314,119,MUTED);
        label(g,"所需材料（行囊扣除）",314,131,GOLD);
        Item[] MATERIALS=materials();
        for(int i=0;i<4;i++){
            int sx=317+i*45;frame(g,"slot",x+sx,y+145,28,28);g.renderItem(new ItemStack(MATERIALS[i]),x+sx+6,y+151);
            final Item material=MATERIALS[i];int owned=i==0?(int)Math.min(Integer.MAX_VALUE,SpiritCurrency.value(minecraft.player.getInventory().items)):minecraft.player.getInventory().items.stream().filter(s->s.is(material)).mapToInt(ItemStack::getCount).sum();
            centered(g,owned+"/"+menu.value(QUOTE[i]),sx+14,175,owned>=menu.value(QUOTE[i])?TEXT:0xFFE3A28C);
        }
        small(g,"灵石",322,186,MUTED);small(g,"玄铁",367,186,MUTED);small(g,"青金石",408,186,MUTED);small(g,"铭刻水晶",448,186,MUTED);
        small(g,"灵力消耗 "+menu.value(8),314,198,TEXT);
        int fails=EquipmentCrafting.read(menu.equipment()).getCompound("RunePity").getInt(EquipmentCrafting.RUNES[a]+target);
        small(g,"连败 "+fails,411,198,MUTED);
        for(int i=0;i<10;i++){sprite(g,"pity",x+451+i*4,y+198,4,5);if(i<fails)g.fill(x+452+i*4,y+199,x+454+i*4,y+202,GOLD);}
        confirm.active=menu.value(9)==1;
        confirm.setTooltip(Tooltip.create(Component.literal(confirm.active?"灵韵：普通70% / 上佳25% / 极品5%\n失败消耗材料，装备与旧铭纹保留":EquipmentCrafting.REASONS.get(Math.max(0,Math.min(EquipmentCrafting.REASONS.size()-1,menu.value(10)))))));
        small(g,"普通70% · 上佳25% · 极品5%",314,244,MUTED);
        label(g,"行囊",140,274,GOLD);
        for(int i=2;i<menu.slots.size();i++){var s=menu.slots.get(i);frame(g,"slot",x+s.x-1,y+s.y-1,18,18);}
        small(g,"主纹限一道",17,330,GOLD);small(g,"槽位随器阶提升",17,344,MUTED);
        for(var b:choices)b.selected=b.rune==a;
        // The legacy input is deliberately non-insertable in the production menu; never imply an empty slot accepts new books.
        for(var widget:renderables)if(widget instanceof GoldButton b&&!b.close&&b.rune<0&&b!=confirm){b.visible=!menu.book().isEmpty();b.active=!menu.equipment().isEmpty();}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        try{renderBackground(g);super.render(g,(int)(mx/scale),(int)(my/scale),partial);
            if(!menu.equipment().isEmpty()&&menu.getCarried().isEmpty()){
                g.pose().pushPose();g.pose().translate(0,0,250);frame(g,"equipment_slot",leftPos+192,topPos+78,40,40);
                g.pose().translate(leftPos+198,topPos+84,0);g.pose().scale(1.75f,1.75f,1);g.renderItem(menu.equipment(),0,0);g.pose().popPose();
            }
        }finally{g.pose().popPose();}
        renderTooltip(g,mx,my);
    }
    @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){
        var equipment=menu.getSlot(0);
        if(x==equipment.x&&y==equipment.y&&w==16&&h==16)return mx>=leftPos+192&&mx<leftPos+232&&my>=topPos+78&&my<topPos+118;
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
            if(close){sprite(g,"close",getX(),getY(),width,height);if(isHoveredOrFocused())g.renderOutline(getX()+1,getY()+1,width-2,height-2,GOLD);return;}
            String skin=rune>=0?(selected||isHoveredOrFocused()?"row_selected":"row"):"confirm";
            frame(g,skin,getX(),getY(),width,height);
            sprite(g,rune>=0?(selected||isHoveredOrFocused()?"row_selected_fill":"row_fill"):"confirm_fill",getX()+4,getY()+4,width-8,height-8);
            if(selected||active&&isHoveredOrFocused())g.fill(getX()+4,getY()+4,getX()+width-4,getY()+height-4,0x443F2B0B);
            if(rune>=0){sprite(g,ICONS[rune],getX()+5,getY()+4,25,25);g.drawString(Minecraft.getInstance().font,getMessage(),getX()+38,getY()+12,active?GOLD:MUTED,false);}
            else g.drawCenteredString(Minecraft.getInstance().font,getMessage(),getX()+width/2,getY()+(height-9)/2,active?(isHoveredOrFocused()?GOLD:TEXT):MUTED);
            if(active&&isHovered())g.renderOutline(getX()+1,getY()+1,width-2,height-2,GOLD);
            if(!active)g.fill(getX()+3,getY()+3,getX()+width-3,getY()+height-3,0x77000000);
            if(isFocused())g.renderOutline(getX()+2,getY()+2,width-4,height-4,GOLD);
        }
    }
}
