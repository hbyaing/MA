package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;


/** Fixed-reference workstation UI: logical pixels stay stable and scale uniformly with input coordinates. */
public final class EquipmentWorkstationScreen extends AbstractContainerScreen<EquipmentWorkstationMenu> {
    private static final int GOLD=WorkstationUi.GOLD,MUTED=WorkstationUi.MUTED;

    private Button confirm,accept,reject,convert,credit;private final List<Button> selectors=new ArrayList<>();
    private WorkstationUi.Button refineTab,reforgeTab;private EditBox search;
    private boolean reforge,draggingScroll;private int scroll;private float uiScale=1;private double scrollGrab;
    private List<Integer> filtered=new ArrayList<>();
    private static final String[] DISPLAY_NAMES={"灵御","坚韧","轻身","御器","破邪","生机","焚魂","聚灵"};
    private String profile(){return menu.inscription?"inscription":"reforge";}
    private int ux(String element,int fallback){return UiLayoutConfig.x(profile(),element,fallback);}
    private int uy(String element,int fallback){return UiLayoutConfig.y(profile(),element,fallback);}
    /** The skin and container use a fixed logical canvas.  The workbench's
     * `window` entry describes the reference frame and must not resize the
     * live screen (older exports stored the source-image bounds there). */
    public EquipmentWorkstationScreen(EquipmentWorkstationMenu menu,Inventory inv,Component title){super(menu,inv,title);UiLayoutConfig.reload();imageWidth=430;imageHeight=330;reforge=menu.selected()>=30;}
    private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.command(action));}
    private WorkstationUi.Button button(String text,int x,int y,int w,int h,int action){
        return addRenderableWidget(new WorkstationUi.Button(leftPos+x,topPos+y,w,h,text,b->send(action)));
    }
    @Override protected void init(){
        uiScale=Math.min(1f,Math.min((width-12f)/imageWidth,(height-18f)/imageHeight));width=(int)(width/uiScale);height=(int)(height/uiScale);
        super.init();selectors.clear();
        if(menu.inscription){
            String query=search==null?"":search.getValue();search=new ForgedSearchBox(font,leftPos+ux("search",36),topPos+uy("search",61),UiLayoutConfig.width(profile(),"search",64),UiLayoutConfig.height(profile(),"search",14),Component.literal("搜索铭纹"));
            search.setBordered(false);search.setTextColor(ForgedUi.IVORY);search.setHint(Component.literal("搜索铭纹"));search.setMaxLength(64);search.setValue(query);
            search.setResponder(s->{scroll=0;rebuildChoices();});addRenderableWidget(search);
        }else{
            int modesX=ux("modes",12),modesY=uy("modes",51);refineTab=addRenderableWidget(new WorkstationUi.Button(leftPos+modesX,topPos+modesY,45,18,"精炼",b->{reforge=false;rebuildChoices();send(80);}));
            reforgeTab=addRenderableWidget(new WorkstationUi.Button(leftPos+modesX+50,topPos+modesY,45,18,"重铸",b->{reforge=true;rebuildChoices();send(90);}));
        }
        rebuildChoices();
        String actionElement=menu.inscription?"confirm":"actions";
        int actionX=ux(actionElement,menu.inscription?150:256),actionY=uy(actionElement,menu.inscription?168:298),actionW=UiLayoutConfig.width(profile(),actionElement,menu.inscription?90:154),actionH=UiLayoutConfig.height(profile(),actionElement,menu.inscription?23:22);
        confirm=button(menu.inscription?"落纹 · 刻印":"锻炼 · 精炼",actionX,actionY,actionW,actionH,100);
        if(!menu.inscription){int ax=ux("accept",256),ay=uy("accept",298);accept=button("采用",ax,ay,UiLayoutConfig.width(profile(),"accept",73),UiLayoutConfig.height(profile(),"accept",22),101);reject=button("保留原胚",ux("reject",334),uy("reject",ay),UiLayoutConfig.width(profile(),"reject",76),UiLayoutConfig.height(profile(),"reject",22),102);}
    }
    private void choice(String name,int value,int command,int y){
        int listX=ux(menu.inscription?"runes":"modes",12),listW=UiLayoutConfig.width(profile(),menu.inscription?"runes":"modes",menu.inscription?90:89);
        int rowH=value>=20&&value<=22?32:Math.max(18,Math.min(27,UiLayoutConfig.height(profile(),"runes",232)/8));
        var b=new WorkstationUi.Button(leftPos+listX,topPos+y,listW,rowH,name,w->{if(command>=0)send(command);}){
            @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){chosen=menu.selected()==value;if(menu.inscription){var label=getMessage();setMessage(Component.empty());super.renderWidget(g,mx,my,dt);setMessage(label);ForgedUi.rune(g,Math.min(value,7),getX()+3,getY()+3,21);if(value>=EquipmentCrafting.NAMES.length)g.fill(getX()+3,getY()+3,getX()+24,getY()+24,0x99060A0D);g.drawString(font,label,getX()+28,getY()+9,value>=EquipmentCrafting.NAMES.length?WorkstationUi.MUTED:ForgedUi.IVORY,false);}else if(value>=20&&value<=22){var label=getMessage();setMessage(Component.empty());super.renderWidget(g,mx,my,dt);setMessage(label);ForgedUi.strategy(g,value-20,getX()+4,getY()+5);g.drawString(font,label,getX()+27,getY()+5,ForgedUi.IVORY,false);String detail=value==20?"稳妥保阶":value==21?"高阶降阶风险":"额外材料护器";g.drawString(font,detail,getX()+27,getY()+17,ForgedUi.MUTED,false);}else super.renderWidget(g,mx,my,dt);}
        };b.choice=true;selectors.add(addRenderableWidget(b));
    }
    private void rebuildChoices(){
        for(var b:selectors)removeWidget(b);selectors.clear();
        if(menu.inscription){
            filtered=new ArrayList<>();String q=search==null?"":search.getValue().trim();
            for(int i=0;i<DISPLAY_NAMES.length;i++)if(DISPLAY_NAMES[i].contains(q))filtered.add(i);
            scroll=Math.max(0,Math.min(scroll,Math.max(0,filtered.size()-8)));
            int listY=uy("runes",83),listStep=Math.max(1,UiLayoutConfig.height(profile(),"runes",232)/8);
            for(int row=0;row<8&&row+scroll<filtered.size();row++){int i=filtered.get(row+scroll);choice(DISPLAY_NAMES[i],i,i<EquipmentCrafting.NAMES.length?60+i:-1,listY+row*listStep);}
        }else if(reforge){int listY=uy("modes",77);for(int i=0;i<4;i++)choice(EquipmentCrafting.TEMPER[i],30+i,90+i,listY+i*31);}
        else{int listY=uy("modes",77);choice("稳炼",20,80,listY);choice("冒险",21,81,listY+38);choice("护器",22,82,listY+76);}
    }
    private void text(GuiGraphics g,String s,int x,int y,int c){ForgedUi.label(g,s,leftPos+x,topPos+y,c);}
    private void center(GuiGraphics g,String s,int x,int y,int c){g.drawCenteredString(font,s,leftPos+x,topPos+y,c);}
    private void title(GuiGraphics g,String s,int x,int y,int c){g.pose().pushPose();g.pose().translate(leftPos+x,topPos+y,0);g.pose().scale(1.45f,1.45f,1);g.drawCenteredString(font,s,0,0,c);g.pose().popPose();}
    private void quantity(GuiGraphics g,String s,int x,int y,int color){float scale=Math.min(1,29f/Math.max(1,font.width(s)));g.pose().pushPose();g.pose().translate(leftPos+x,topPos+y,0);g.pose().scale(scale,scale,1);g.drawCenteredString(font,s,0,0,color);g.pose().popPose();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        if(menu.inscription) EquipmentUiAssets.drawInscription(g,x,y); else EquipmentUiAssets.draw(g,x,y);
        // Reference hierarchy: centered title and subtitle sit above the three-panel work area.
        if(menu.inscription)title(g,"铭刻台",215,8,GOLD);else center(g,"精铸台",215,10,GOLD);
        center(g,menu.inscription?"刻印 / 养器":"锻骨 / 重塑",215,28,WorkstationUi.TEXT);
        // 参考稿的硬布局：左侧完整列表；中上预览；右上材料；中下详情；右下独立物品栏。

        if(menu.inscription)text(g,"铭纹选择",18,50,GOLD);
        if(menu.inscription){
            int sx=ux("search",12),sy=uy("search",60),sw=UiLayoutConfig.width(profile(),"search",91),sh=UiLayoutConfig.height(profile(),"search",16);
            WorkstationUi.search(g,x+sx,y+sy,sw,sh);
            int track=listTrack(),max=Math.max(0,filtered.size()-8),thumb=filtered.size()<=8?track:Math.max(24,track*8/filtered.size());
            int ty=y+listY()+(max==0?0:(track-thumb)*scroll/max);WorkstationUi.scrollTrack(g,x+sx+sw+2,y+listY(),track,ty,thumb);
            if(filtered.isEmpty())text(g,"无匹配铭纹",20,96,MUTED);
        }else{refineTab.chosen=!reforge;reforgeTab.chosen=reforge;}
        var s=menu.equipment();var n=EquipmentCrafting.read(s);int action=menu.selected();boolean pending=n.contains("Pending");

        int equipmentX=menu.slots.get(0).x,equipmentY=menu.slots.get(0).y,equipmentSize=UiLayoutConfig.width(profile(),"equipmentSlot",20);center(g,menu.inscription?"待刻之器":"待炼之器",ux("equipmentTitle",179),uy("equipmentTitle",51),GOLD);WorkstationUi.slot(g,x+equipmentX,y+equipmentY,equipmentSize);
        center(g,s.isEmpty()?"置入装备":font.plainSubstrByWidth(s.getHoverName().getString(),100),ux("equipmentName",179),uy("equipmentName",125),WorkstationUi.TEXT);
        center(g,"精炼 +"+EquipmentCrafting.refine(s),ux("equipmentRefine",179),uy("equipmentRefine",140),GOLD);center(g,"铭纹 "+n.getCompound("Runes").size()+" / "+EquipmentCrafting.capacity(s),ux("equipmentCapacity",179),uy("equipmentCapacity",156),MUTED);
        if(menu.inscription)WorkstationUi.slot(g,x+ux("book",129),y+uy("book",170),18);
        String op=action<5?EquipmentCrafting.NAMES[Math.max(0,action)]+" · "+(EquipmentCrafting.level(s,EquipmentCrafting.RUNES[Math.max(0,action)])+1)+"阶":action<=22?"精炼 · +"+(EquipmentCrafting.refine(s)+1):"重铸 · "+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,action-30))];
        int rightX=ux("summary",menu.inscription?248:256);
        text(g,op,rightX,53,GOLD);
        if(action>=30){text(g,"普通60% · 上佳30%",255,78,0xA6C9C4);text(g,"极品9% · 天成1%",255,92,0xB9A1CC);}
        else{text(g,"成功率",rightX,76,MUTED);int rateRight=menu.inscription?406:414;g.drawString(font,menu.value(2)+"%",x+rateRight-font.width(menu.value(2)+"%"),y+76,WorkstationUi.CYAN,false);g.fill(x+rightX,y+88,x+rateRight,y+94,0xFF292A2C);g.fill(x+rightX,y+88,x+rightX+158*Math.min(100,Math.max(0,menu.value(2)))/100,y+94,0xFF54E4EA);}
        text(g,"所需材料",rightX,104,GOLD);
        Item[] mats=menu.inscription?new Item[]{WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.FIVE_ESSENCE.get(),WuXingItems.INSCRIPTION_CRYSTAL.get()}:new Item[]{WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.FIVE_ESSENCE.get(),WuXingItems.GLAZED_CRYSTAL.get()};
        int materialX=ux("materials",menu.inscription?250:258),materialY=uy("materials",116),materialStep=Math.max(18,UiLayoutConfig.width(profile(),"materials",mats.length*34)/Math.max(1,mats.length));
        for(int i=0;i<mats.length;i++){int sx=x+materialX+i*materialStep,sy=y+materialY;WorkstationUi.slot(g,sx-2,sy-2,28);g.renderItem(new ItemStack(mats[i]),sx+4,sy+4);int required=menu.value(3+i),owned=0;if(i==0)owned=(int)cn.blockforge.generated.generatedmod.economy.SpiritCurrency.value(minecraft.player.getInventory().items);else for(var it:minecraft.player.getInventory().items)if(it.is(mats[i]))owned+=it.getCount();quantity(g,owned+"/"+required,materialX+10+i*materialStep,uy("materialCounts",149),owned>=required?0xA5CBB1:0xE6A18F);}
        text(g,"灵力 "+menu.value(8),ux("power",rightX),uy("power",162),WorkstationUi.CYAN);
        String pity=action<5?Integer.toString(n.getCompound("RunePity").getInt(EquipmentCrafting.RUNES[Math.max(0,action)]+(EquipmentCrafting.level(s,EquipmentCrafting.RUNES[Math.max(0,action)])+1))):action<=22?Integer.toString(n.getCompound("RefinePity").getInt("L"+(EquipmentCrafting.refine(s)+1))):n.getInt("ReforgeRare")+"/10 · "+n.getInt("ReforgeNatural")+"/50";
        int failureCount;
        if(action<5)failureCount=n.getCompound("RunePity").getInt(EquipmentCrafting.RUNES[Math.max(0,action)]+(EquipmentCrafting.level(s,EquipmentCrafting.RUNES[Math.max(0,action)])+1));
        else if(action<=22)failureCount=n.getCompound("RefinePity").getInt("L"+(EquipmentCrafting.refine(s)+1));
        else failureCount=n.getInt("ReforgeRare");
        text(g,action>=30?"保底":"连败",ux("pity",350),uy("pity",162),0xB4AAC3);
        int lit=Math.max(0,Math.min(10,failureCount));
        int pityX=ux("pityDots",256),pityY=uy("pityDots",174),pityStep=Math.max(11,UiLayoutConfig.width(profile(),"pityDots",150)/10);
        for(int i=0;i<10;i++){int sx=x+pityX+i*pityStep,sy=y+pityY;WorkstationUi.slot(g,sx,sy,11);if(i<lit)g.fill(sx+3,sy+3,sx+8,sy+8,0xFFE0B85D);}
        if(menu.inscription){
            text(g,"铭纹详情",124,212,GOLD);
            ForgedUi.rune(g,Math.max(0,Math.min(4,action)),x+121,y+228,22);
            text(g,action<5?EquipmentCrafting.NAMES[action]:"未选择铭纹",146,229,WorkstationUi.CYAN);
            text(g,action<5?"防御类铭纹":"选择左侧铭纹查看详情",146,244,MUTED);
            text(g,"铭纹效果",124,261,GOLD);
            text(g,action==0?"伤害降低 8%":"选择后显示实际加成",124,275,ForgedUi.IVORY);
            // Keep the equipment applicability copy inside the narrow details
            // card; the previous single line ran into the inventory card at
            // the panel seam on larger GUI scales.
            text(g,"适用装备",124,291,MUTED);
            text(g,"胸甲 / 法衣 / 盾牌",124,306,ForgedUi.IVORY);
            // Lift the inventory heading away from the first slot row so the
            // glyph bottoms do not touch the slot frames at higher GUI scales.
            text(g,"物品栏",240,214,GOLD);
        }else { text(g,"物品栏",18,214,GOLD); text(g,"风险说明",258,214,GOLD); }
        for(int i=2;i<menu.slots.size();i++){var slot=menu.slots.get(i);g.fill(x+slot.x-2,y+slot.y-2,x+slot.x+18,y+slot.y+18,0xFF07090B);g.renderOutline(x+slot.x-2,y+slot.y-2,20,20,0xFF595750);}
        int info=menu.inscription?239:206;
        if(pending&&!menu.inscription){var c=n.getCompound("Pending");text(g,"重铸结果预览",259,232,GOLD);text(g,"新胚 · "+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,c.getInt("Type")))]+" / "+EquipmentCrafting.QUALITY[Math.max(0,Math.min(3,c.getInt("Quality")))],259,249,0xD6BCE0);text(g,n.contains("TemperBp")?"原胚 · "+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,n.getInt("Temper")))]: "原胚 · 尚无胚性",259,265,MUTED);}
        else if(!menu.inscription){
            if(menu.inscription)text(g,"铭刻说明",259,info+8,GOLD);
            String risk=menu.inscription?"旧纹保留 · 失败耗材":action==21?"高阶失败有概率降一阶":action>=30?"结果可弃 · 材料不返":"失败耗材 · 不掉阶";text(g,risk,259,info+25,0xC9B693);
            if(menu.inscription)text(g,"连败积累保底；材料从行囊扣除",259,info+41,MUTED);
            else {text(g,"当前精炼等级  "+EquipmentCrafting.refine(s)+" / 9",259,info+41,MUTED);text(g,"失败保留装备，保底进度累计",259,info+57,ForgedUi.IVORY);}
            text(g,menu.value(9)==1?"材料与条件齐备":"条件未齐 · 悬停查看",259,menu.inscription?info+58:info+73,menu.value(9)==1?0x9FC4AD:MUTED);
        }
        confirm.visible=!pending;confirm.active=menu.value(9)==1&&!pending;confirm.setMessage(Component.literal(menu.inscription?"落纹 · 刻印":action>=30?"开炉 · 重铸":"锻炼 · 精炼"));
        if(accept!=null){accept.visible=pending;reject.visible=pending;accept.active=pending;reject.active=pending;}
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.pose().pushPose();g.pose().scale(uiScale,uiScale,1);int mx=(int)(x/uiScale),my=(int)(y/uiScale);
        try{renderBackground(g);g.fill(0,0,width,height,0xD00A1014);super.render(g,mx,my,partial);
            if(menu.getCarried().isEmpty()){
                int ex=menu.slots.get(0).x,ey=menu.slots.get(0).y,es=UiLayoutConfig.width(profile(),"equipmentSlot",20);WorkstationUi.slot(g,leftPos+ex,topPos+ey,es);if(!menu.equipment().isEmpty()){g.pose().pushPose();g.pose().translate(leftPos+ex+2,topPos+ey+2,150);g.renderItem(menu.equipment(),0,0);g.pose().popPose();
                }
                int bx=menu.slots.get(1).x,by=menu.slots.get(1).y;WorkstationUi.slot(g,leftPos+bx,topPos+by,18);if(!menu.book().isEmpty()){g.renderItem(menu.book(),leftPos+bx+1,topPos+by+1);g.renderItemDecorations(font,menu.book(),leftPos+bx+1,topPos+by+1);}
            }
            }finally{g.pose().popPose();}
        renderTooltip(g,x,y);details(g,mx,my,x,y);
    }
    private void details(GuiGraphics g,int x,int y,int screenX,int screenY){
        if(over(confirm,x,y)){
            var tips=new ArrayList<Component>();if(menu.value(9)!=1)tips.add(Component.literal(EquipmentCrafting.REASONS.get(Math.max(0,Math.min(EquipmentCrafting.REASONS.size()-1,menu.value(10))))));int action=menu.selected();
            if(action<5){int l=EquipmentCrafting.level(menu.equipment(),EquipmentCrafting.RUNES[action])+1;tips.add(Component.literal("灵韵：普通70% / 上佳25% / 极品5%"));tips.add(Component.literal(l==1?"首次刻印100%成功":"连续"+l+"次失败后，下次必成"));tips.add(Component.literal("升级保留较高灵韵；材料从行囊扣除"));}
            else if(action<=22){int l=EquipmentCrafting.refine(menu.equipment())+1;if(l<=9)tips.add(Component.literal("每败+10个百分点，最多"+EquipmentCrafting.PITY[l-1]+"败后下次必成"));tips.add(Component.literal("基础伤害+1.5% / 护甲+1% / 耐久+2%每阶"));tips.add(Component.literal(action==21?"挑战+7～9失败时30%降一阶，其余保留":"失败扣材料但不掉阶"));}
            else{tips.add(Component.literal("普通60% / 上佳30% / 极品9% / 天成1%"));tips.add(Component.literal("10次未极品则下次极品以上；50次未天成则下次天成"));tips.addAll(temperTips(action-30,1));}g.renderComponentTooltip(font,tips,screenX,screenY);
        }else if(over(accept,x,y)){var n=EquipmentCrafting.read(menu.equipment());var c=n.getCompound("Pending");var tips=new ArrayList<Component>();tips.add(Component.literal("新胚性"));tips.addAll(temperTips(c.getInt("Type"),c.getInt("Bp")/10000d));if(n.contains("TemperBp")){tips.add(Component.literal("原胚性"));tips.addAll(temperTips(n.getInt("Temper"),n.getInt("TemperBp")/10000d));}g.renderComponentTooltip(font,tips,screenX,screenY);}
        Item[] mats=menu.inscription?new Item[]{WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.FIVE_ESSENCE.get(),WuXingItems.INSCRIPTION_CRYSTAL.get()}:new Item[]{WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.FIVE_ESSENCE.get(),WuXingItems.GLAZED_CRYSTAL.get()};
        int mx=ux("materials",menu.inscription?250:258),my=uy("materials",116),step=Math.max(18,UiLayoutConfig.width(profile(),"materials",mats.length*34)/Math.max(1,mats.length));
        for(int i=0;i<mats.length;i++)if(x>=leftPos+mx+i*step-2&&x<leftPos+mx+i*step+26&&y>=topPos+my-2&&y<topPos+my+26)g.renderTooltip(font,Component.literal(new ItemStack(mats[i]).getHoverName().getString()+" × "+menu.value(3+i)),screenX,screenY);
    }
    private static boolean over(Button b,int x,int y){return b!=null&&b.visible&&x>=b.getX()&&x<b.getX()+b.getWidth()&&y>=b.getY()&&y<b.getY()+b.getHeight();}
    private List<Component> temperTips(int type,double power){boolean spirit=menu.equipment().getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem;String text=switch(type){case 0->String.format(Locale.ROOT,spirit?"伤害+%.2f%%；持续耗灵+%.2f%%":"伤害+%.2f%%；最大耐久-%.2f%%",4*power,(spirit?4:8)*power);case 1->String.format(Locale.ROOT,"最大耐久+%.2f%%",12*power);case 2->String.format(Locale.ROOT,"持续耗灵-%.2f%%；伤害-%.2f%%",5*power,2*power);default->String.format(Locale.ROOT,spirit?"攻速+%.2f%%；持续耗灵+%.2f%%":"攻速/步行+%.2f%%；耐久-%.2f%%",3*power,(spirit?3:6)*power);};return List.of(Component.literal(text));}
    private int listX(){return ux("runes",12);}
    private int listY(){return uy("runes",83);}
    private int listTrack(){return Math.max(24,UiLayoutConfig.height(profile(),"runes",212));}
    private boolean scrollbar(double x,double y){return menu.inscription&&x>=leftPos+listX()+UiLayoutConfig.width(profile(),"runes",90)&&x<leftPos+listX()+UiLayoutConfig.width(profile(),"runes",90)+10&&y>=topPos+listY()&&y<=topPos+listY()+listTrack();}
    private int thumbSize(){return filtered.size()<=8?listTrack():Math.max(24,listTrack()*8/filtered.size());}
    private void drag(double y){int max=Math.max(0,filtered.size()-8),travel=listTrack()-thumbSize();if(max==0||travel<=0)return;scroll=(int)Math.round(Math.max(0,Math.min(1,(y-topPos-listY()-scrollGrab)/travel))*max);rebuildChoices();}
    private boolean equipmentFrame(double x,double y){var slot=menu.slots.get(0);return x>=leftPos+slot.x&&x<leftPos+slot.x+16&&y>=topPos+slot.y&&y<topPos+slot.y+16;}
    @Override public boolean mouseClicked(double x,double y,int b){x/=uiScale;y/=uiScale;
        if(b==0&&menu.inscription&&x>=leftPos+listX()+UiLayoutConfig.width(profile(),"runes",90)&&x<leftPos+listX()+UiLayoutConfig.width(profile(),"runes",90)+10){
            if(y>=topPos+listY()-8&&y<topPos+listY()){scroll--;rebuildChoices();return true;}
            if(y>=topPos+listY()+listTrack()&&y<=topPos+listY()+listTrack()+10){scroll++;rebuildChoices();return true;}
            if(scrollbar(x,y)){int max=Math.max(0,filtered.size()-8),size=thumbSize();double thumb=topPos+83+(max==0?0:(212-size)*scroll/(double)max);scrollGrab=y>=thumb&&y<=thumb+size?y-thumb:size/2.0;draggingScroll=max>0;drag(y);return true;}
        }
        return super.mouseClicked(x,y,b);}
    @Override public boolean mouseReleased(double x,double y,int b){draggingScroll=false;return super.mouseReleased(x/uiScale,y/uiScale,b);}
    @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){var slot=menu.slots.get(0);return x==slot.x&&y==slot.y&&w==16&&h==16?equipmentFrame(mx,my):super.isHovering(x,y,w,h,mx,my);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){if(draggingScroll){drag(y/uiScale);return true;}return super.mouseDragged(x/uiScale,y/uiScale,b,dx/uiScale,dy/uiScale);}
    @Override public boolean mouseScrolled(double x,double y,double delta){x/=uiScale;y/=uiScale;if(menu.inscription&&x>=leftPos+listX()&&x<leftPos+listX()+UiLayoutConfig.width(profile(),"runes",100)&&y>=topPos+listY()&&y<=topPos+listY()+listTrack()){scroll+=delta<0?1:-1;rebuildChoices();return true;}return super.mouseScrolled(x,y,delta);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(search!=null&&search.isFocused()&&key!=org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE&&key!=org.lwjgl.glfw.GLFW.GLFW_KEY_TAB){search.keyPressed(key,scan,modifiers);return true;}return super.keyPressed(key,scan,modifiers);}
}















