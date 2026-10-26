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

/** The jing_zhu_tai refinement/temper menu, using approved dark-gold raster fittings. */
public final class ReforgeScreen extends AbstractContainerScreen<EquipmentWorkstationMenu> {
    private static final int GOLD=0xFFE4C783,TEXT=0xFFF0E5C9,MUTED=0xFFB3A48A,WARN=0xFFE2A084;
    private static final String[] STRATEGIES={"稳炼","冒险","护器"},ICONS={"stable","risk","protect"};
    private static final String[] STRATEGY_TIPS={"稳妥保阶","高阶有降阶风险","额外材料护器"};
    private static final Map<String,int[]> SIZES=Map.ofEntries(
        Map.entry("header_title",new int[]{260,86}),Map.entry("header",new int[]{1516,111}),Map.entry("window",new int[]{1516,1000}),
        Map.entry("left_clean",new int[]{343,873}),Map.entry("equipment_clean",new int[]{516,590}),
        Map.entry("preview_clean",new int[]{601,590}),Map.entry("inventory_clean",new int[]{1127,278}),
        Map.entry("tab",new int[]{155,61}),Map.entry("tab_selected",new int[]{164,61}),
        Map.entry("row",new int[]{316,106}),Map.entry("row_selected",new int[]{316,105}),
        Map.entry("confirm",new int[]{447,76}),Map.entry("confirm_fill",new int[]{45,37}),
        Map.entry("row_fill",new int[]{70,28}),Map.entry("row_selected_fill",new int[]{70,36}),
        Map.entry("stable",new int[]{92,83}),Map.entry("risk",new int[]{92,83}),Map.entry("protect",new int[]{92,84}),
        Map.entry("ready",new int[]{43,44}),Map.entry("fire",new int[]{37,50}),Map.entry("alert",new int[]{41,43}),Map.entry("pity",new int[]{23,26}));
    private final List<Control> choices=new ArrayList<>();
    private Control confirm,accept,reject,refineTab,reforgeTab;
    private boolean mode;
    private float scale=1;
    public ReforgeScreen(EquipmentWorkstationMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=512;imageHeight=368;inventoryLabelY=-1000;}
    private static ResourceLocation tex(String n){return ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/reforge_gold/"+n+".png");}
    private static void sprite(GuiGraphics g,String n,int x,int y,int w,int h){int[] s=SIZES.get(n);boolean chrome=n.equals("header")||n.endsWith("_clean")||n.equals("pity");if(chrome)g.setColor(.48f,.46f,.42f,1);g.blit(tex(n),x,y,w,h,0,0,s[0],s[1],s[0],s[1]);if(chrome)g.setColor(1,1,1,1);}
    private static void frame(GuiGraphics g,String n,int x,int y,int w,int h){
        int[] s=SIZES.get(n);int c=16,d=3,e=12;g.fill(x,y,x+w,y+h,0xF0120D07);g.setColor(.48f,.46f,.42f,1);
        for(int row=0;row<2;row++)for(int col=0;col<2;col++)g.blit(tex(n),x+(col==0?0:w-d),y+(row==0?0:h-d),d,d,col==0?0:s[0]-c,row==0?0:s[1]-c,c,c,s[0],s[1]);
        g.blit(tex(n),x+d,y,w-2*d,d,c,0,s[0]-2*c,e,s[0],s[1]);g.blit(tex(n),x+d,y+h-d,w-2*d,d,c,s[1]-e,s[0]-2*c,e,s[0],s[1]);
        g.blit(tex(n),x,y+d,d,h-2*d,0,c,e,s[1]-2*c,s[0],s[1]);g.blit(tex(n),x+w-d,y+d,d,h-2*d,s[0]-e,c,e,s[1]-2*c,s[0],s[1]);g.setColor(1,1,1,1);
    }
    private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.command(action));}
    private boolean pending(){return EquipmentCrafting.read(menu.equipment()).contains("Pending");}
    private boolean reforge(){return pending()||menu.selected()>=30;}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-12f)/imageWidth,(height-12f)/imageHeight));width=(int)(width/scale);height=(int)(height/scale);super.init();
        refineTab=addRenderableWidget(new Control(leftPos+13,topPos+46,50,20,"精炼",b->send(80)));refineTab.tab=true;
        reforgeTab=addRenderableWidget(new Control(leftPos+65,topPos+46,50,20,"重铸",b->send(90)));reforgeTab.tab=true;
        confirm=addRenderableWidget(new Control(leftPos+320,topPos+222,166,26,"锻炼 · 精炼",b->send(100)));
        accept=addRenderableWidget(new Control(leftPos+315,topPos+222,87,26,"采用新胚",b->send(101)));
        reject=addRenderableWidget(new Control(leftPos+406,topPos+222,87,26,"保留原胚",b->send(102)));
        Control close=addRenderableWidget(new Control(leftPos+479,topPos+8,20,20,"关闭",b->onClose()));close.close=true;
        rebuild();
    }
    private void rebuild(){
        for(var b:choices)removeWidget(b);choices.clear();mode=reforge();int count=mode?4:3;
        for(int i=0;i<count;i++){final int n=i;var b=new Control(leftPos+13,topPos+96+i*(mode?35:45),105,mode?31:39,mode?EquipmentCrafting.TEMPER[i]:STRATEGIES[i],w->send((mode?90:80)+n));b.choice=i;choices.add(addRenderableWidget(b));}
    }
    @Override protected void containerTick(){super.containerTick();if(mode!=reforge())rebuild();}
    private void label(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,s,leftPos+x,topPos+y,color,false);}
    private void center(GuiGraphics g,String s,int x,int y,int color){g.drawCenteredString(font,s,leftPos+x,topPos+y,color);}
    private void small(GuiGraphics g,String s,int x,int y,int color){g.pose().pushPose();g.pose().translate(leftPos+x,topPos+y,0);g.pose().scale(.8f,.8f,1);g.drawString(font,s,0,0,color,false);g.pose().popPose();}
    private static String quality(int q){return EquipmentCrafting.QUALITY[Math.max(0,Math.min(3,q))];}
    private List<String> effects(int type,double power){
        boolean spirit=menu.equipment().getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
        return switch(type){
            case 0->List.of(String.format(Locale.ROOT,"伤害 +%.2f%%",4*power),String.format(Locale.ROOT,spirit?"持续耗灵 +%.2f%%":"最大耐久 -%.2f%%",(spirit?4:8)*power));
            case 1->List.of(String.format(Locale.ROOT,"最大耐久 +%.2f%%",12*power));
            case 2->List.of(String.format(Locale.ROOT,"持续耗灵 -%.2f%%",5*power),String.format(Locale.ROOT,"伤害 -%.2f%%",2*power));
            default->List.of(String.format(Locale.ROOT,spirit?"攻速 +%.2f%%":"攻速/步行 +%.2f%%",3*power),String.format(Locale.ROOT,spirit?"持续耗灵 +%.2f%%":"最大耐久 -%.2f%%",(spirit?3:6)*power));
        };
    }
    private record Cost(Item item,int required,String name){}
    private List<Cost> costs(){
        var c=new ArrayList<Cost>();c.add(new Cost(WuXingItems.SPIRIT_STONE.get(),menu.value(3),"灵石"));c.add(new Cost(WuXingItems.DARK_IRON.get(),menu.value(4),"玄铁"));
        if(menu.value(6)>0)c.add(new Cost(WuXingItems.FIVE_ESSENCE.get(),menu.value(6),"五行精华"));
        if(menu.value(7)>0)c.add(new Cost(WuXingItems.GLAZED_CRYSTAL.get(),menu.value(7),"琉璃晶"));
        if((menu.value(11)&1)!=0)c.add(new Cost(WuXingItems.REFINE_SEAL.get(),1,"护炼印"));return c;
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;frame(g,"window",x,y,512,368);sprite(g,"header",x+1,y+1,510,37);sprite(g,"header_title",x+213,y+6,87,29);
        sprite(g,"left_clean",x+5,y+41,119,322);sprite(g,"equipment_clean",x+127,y+41,171,222);sprite(g,"preview_clean",x+301,y+41,205,222);sprite(g,"inventory_clean",x+127,y+266,379,97);
        g.fill(x+8,y+44,x+121,y+360,0x66000000);g.fill(x+130,y+44,x+295,y+260,0x44000000);g.fill(x+304,y+44,x+503,y+260,0x99000000);g.fill(x+130,y+269,x+503,y+360,0x88000000);
        boolean waiting=pending(),isReforge=reforge();var n=EquipmentCrafting.read(menu.equipment());var candidate=n.getCompound("Pending");int selected=waiting?Math.max(0,Math.min(3,candidate.getInt("Type"))):isReforge?Math.max(0,Math.min(3,menu.selected()-30)):Math.max(0,Math.min(2,menu.selected()-20));
        int level=EquipmentCrafting.refine(menu.equipment()),target=Math.min(9,level+1);
        label(g,isReforge?"重铸胚性":"精炼方式",13,78,GOLD);refineTab.selected=!isReforge;reforgeTab.selected=isReforge;refineTab.active=!waiting;reforgeTab.active=!waiting;
        for(var b:choices){b.selected=b.choice==selected;b.active=!waiting;}
        label(g,"待炼之器",140,49,GOLD);InscriptionScreen.frame(g,"equipment_slot",x+192,y+78,40,40);
        center(g,menu.equipment().isEmpty()?"置入装备":font.plainSubstrByWidth(menu.equipment().getHoverName().getString(),150),212,122,TEXT);
        center(g,"精炼 +"+level,212,135,GOLD);center(g,"铭纹 "+n.getCompound("Runes").size()+" / "+EquipmentCrafting.capacity(menu.equipment()),212,149,TEXT);
        label(g,isReforge?"胚性效果上限":"本次提升",141,173,GOLD);
        List<String> detail=isReforge?effects(selected,1):List.of("基础伤害 +1.5% / 阶","护甲 +1% / 阶","最大耐久 +2% / 阶");
        int line=191;for(String text:detail){small(g,text,141,line,TEXT);line+=15;}
        if(isReforge){small(g,"实际加成依灵韵变化",141,232,MUTED);small(g,n.contains("TemperBp")?"当前胚性："+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,n.getInt("Temper")))]:"当前尚无胚性",141,246,MUTED);}
        label(g,waiting?"重铸结果预览":isReforge?"重铸预览":"精炼预览",314,49,GOLD);
        if(waiting){
            int type=Math.max(0,Math.min(3,candidate.getInt("Type")));label(g,"新胚 · "+EquipmentCrafting.TEMPER[type]+" / "+quality(candidate.getInt("Quality")),314,70,GOLD);
            int yy=90;for(String text:effects(type,candidate.getInt("Bp")/10000d)){label(g,text,314,yy,TEXT);yy+=15;}
            label(g,"原胚对照",314,132,GOLD);
            if(n.contains("TemperBp")){
                int old=Math.max(0,Math.min(3,n.getInt("Temper")));small(g,EquipmentCrafting.TEMPER[old]+" / "+quality(n.getInt("TemperQuality")),314,148,TEXT);yy=162;
                for(String text:effects(old,n.getInt("TemperBp")/10000d)){small(g,text,314,yy,MUTED);yy+=13;}
            }else label(g,"原器尚无胚性",314,149,MUTED);
            small(g,"仅在采用后替换原胚性",314,195,TEXT);small(g,"保留原胚也不返还材料",314,208,WARN);
        }else{
            if(isReforge){label(g,"重铸 · "+EquipmentCrafting.TEMPER[selected],314,71,GOLD);small(g,"普通60% · 上佳30%",314,91,TEXT);small(g,"极品9% · 天成1%",314,105,GOLD);}
            else{
                label(g,level>=9?"精炼已满阶 +9":"精炼 +"+level+" → +"+target,314,70,GOLD);label(g,level>=9?"—":menu.value(2)+"%",465,70,GOLD);
                InscriptionScreen.frame(g,"search",x+314,y+86,180,7);int fill=Math.max(0,Math.min(100,menu.value(2)))*176/100;g.fill(x+316,y+88,x+316+fill,y+91,0xFFE5BA55);
                small(g,"当前方式："+STRATEGIES[selected],314,100,TEXT);
            }
            label(g,"所需材料（行囊扣除）",314,119,GOLD);var costs=costs();int step=costs.size()<=3?57:43;
            for(int i=0;i<costs.size();i++){
                var c=costs.get(i);int sx=317+i*step;InscriptionScreen.frame(g,"slot",x+sx,y+135,30,30);g.renderItem(new ItemStack(c.item()),x+sx+7,y+142);
                int owned=c.item()==WuXingItems.SPIRIT_STONE.get()?(int)Math.min(Integer.MAX_VALUE,SpiritCurrency.value(minecraft.player.getInventory().items)):minecraft.player.getInventory().items.stream().filter(s->s.is(c.item())).mapToInt(ItemStack::getCount).sum();
                center(g,owned+"/"+c.required(),sx+15,167,owned>=c.required()?TEXT:WARN);small(g,c.name(),sx+3,179,MUTED);
            }
            if(isReforge){small(g,"极品保底 "+n.getInt("ReforgeRare")+"/10",314,193,TEXT);small(g,"天成保底 "+n.getInt("ReforgeNatural")+"/50",411,193,GOLD);}
            else{
                int fails=n.getCompound("RefinePity").getInt("L"+target),pity=EquipmentCrafting.PITY[target-1];small(g,"灵力消耗 "+menu.value(8),314,193,TEXT);small(g,"连败 "+fails+"/"+pity,390,193,MUTED);
                for(int i=0;i<pity;i++){sprite(g,"pity",x+444+i*6,y+191,5,6);if(i<fails)g.fill(x+445+i*6,y+193,x+448+i*6,y+196,GOLD);}
            }
            String status=menu.value(9)==1?"条件齐备":"条件未齐 · 悬停查看";sprite(g,menu.value(9)==1?"ready":"alert",x+314,y+205,10,10);small(g,status,328,207,menu.value(9)==1?GOLD:WARN);
            int fire=menu.value(11)>>1;sprite(g,"fire",x+444,y+204,8,11);small(g,"异火 "+fire+"阶",455,207,MUTED);
        }
        confirm.visible=!waiting;confirm.active=menu.value(9)==1&&!waiting;confirm.setMessage(Component.literal(isReforge?"开炉 · 重铸":"锻炼 · 精炼"));accept.visible=reject.visible=waiting;accept.active=reject.active=waiting;
        String help=waiting?"采用才会替换原胚性":menu.value(9)!=1?EquipmentCrafting.REASONS.get(Math.max(0,Math.min(EquipmentCrafting.REASONS.size()-1,menu.value(10)))):isReforge?"生成新胚后可选择采用或保留；材料不退还":selected==1?"挑战+7～9失败时30%降一阶，其余保留":"失败消耗材料，精炼等级保留";
        confirm.setTooltip(Tooltip.create(Component.literal(help)));accept.setTooltip(Tooltip.create(Component.literal("替换为新胚性并保留装备")));reject.setTooltip(Tooltip.create(Component.literal("保留原胚性，本次材料不退还")));
        small(g,waiting?"待选结果期间不能再次重铸":isReforge?"结果可弃 · 材料不返":selected==1?"高阶失败可能降阶":"失败耗材 · 不掉阶",314,252,MUTED);
        sprite(g,"alert",x+14,y+248,12,13);small(g,"失败消耗材料",31,249,TEXT);small(g,"装备保留",31,263,MUTED);
        label(g,"行囊",140,274,GOLD);for(int i=2;i<menu.slots.size();i++){var s=menu.getSlot(i);InscriptionScreen.frame(g,"slot",x+s.x-1,y+s.y-1,18,18);}
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        try{renderBackground(g);super.render(g,(int)(mx/scale),(int)(my/scale),partial);
            if(!menu.equipment().isEmpty()&&menu.getCarried().isEmpty()){g.pose().pushPose();g.pose().translate(0,0,250);InscriptionScreen.frame(g,"equipment_slot",leftPos+192,topPos+78,40,40);g.pose().translate(leftPos+198,topPos+84,0);g.pose().scale(1.75f,1.75f,1);g.renderItem(menu.equipment(),0,0);g.pose().popPose();}
        }finally{g.pose().popPose();}renderTooltip(g,mx,my);
    }
    @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){var s=menu.getSlot(0);if(x==s.x&&y==s.y&&w==16&&h==16)return mx>=leftPos+192&&mx<leftPos+232&&my>=topPos+78&&my<topPos+118;return super.isHovering(x,y,w,h,mx,my);}
    @Override public boolean mouseClicked(double x,double y,int b){return super.mouseClicked(x/scale,y/scale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased(x/scale,y/scale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged(x/scale,y/scale,b,dx/scale,dy/scale);}
    @Override public boolean mouseScrolled(double x,double y,double delta){return super.mouseScrolled(x/scale,y/scale,delta);}
    private final class Control extends Button {
        int choice=-1;boolean selected,tab,close;
        Control(int x,int y,int w,int h,String s,OnPress press){super(x,y,w,h,Component.literal(s),press,DEFAULT_NARRATION);}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
            if(close){InscriptionScreen.sprite(g,"close",getX(),getY(),width,height);if(isHoveredOrFocused())g.renderOutline(getX()+1,getY()+1,width-2,height-2,0xFFA28B60);return;}
            String skin=tab?(selected||isHoveredOrFocused()?"tab_selected":"tab"):choice>=0?(selected||isHoveredOrFocused()?"row_selected":"row"):"confirm";
            frame(g,skin,getX(),getY(),width,height);sprite(g,choice>=0||tab?(selected||isHoveredOrFocused()?"row_selected_fill":"row_fill"):"confirm_fill",getX()+3,getY()+3,width-6,height-6);
            if(choice>=0){
                if(mode){Item icon=switch(choice){case 0->Items.IRON_SWORD;case 1->WuXingItems.DARK_IRON.get();case 2->Items.ENDER_PEARL;default->Items.FEATHER;};g.renderItem(new ItemStack(icon),getX()+7,getY()+7);}
                else sprite(g,ICONS[choice],getX()+5,getY()+5,27,27);
                g.drawString(font,getMessage(),getX()+37,getY()+8,active?GOLD:MUTED,false);
                String sub=mode?switch(choice){case 0->"增伤与代价";case 1->"提升耐久";case 2->"减少耗灵";default->"攻速与轻盈";}:STRATEGY_TIPS[choice];
                g.pose().pushPose();g.pose().translate(getX()+37,getY()+21,0);g.pose().scale(.7f,.7f,1);g.drawString(font,sub,0,0,MUTED,false);g.pose().popPose();
            }else g.drawCenteredString(font,getMessage(),getX()+width/2,getY()+(height-9)/2,active?(isHoveredOrFocused()?GOLD:TEXT):MUTED);
            if(!active)g.fill(getX()+3,getY()+3,getX()+width-3,getY()+height-3,0x77000000);
            if(active&&isHoveredOrFocused())g.renderOutline(getX()+1,getY()+1,width-2,height-2,0xFFA28B60);
        }
    }
}
