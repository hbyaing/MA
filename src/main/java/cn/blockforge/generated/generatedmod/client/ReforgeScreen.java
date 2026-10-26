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
    public ReforgeScreen(EquipmentWorkstationMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=EquipmentWorkstationLayout.WIDTH;imageHeight=EquipmentWorkstationLayout.REFORGE_HEIGHT;inventoryLabelY=-1000;}
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
        refineTab=addRenderableWidget(new Control(leftPos+14,topPos+38,37,16,"精炼",b->send(80)));refineTab.tab=true;
        reforgeTab=addRenderableWidget(new Control(leftPos+55,topPos+38,37,16,"重铸",b->send(90)));reforgeTab.tab=true;
        confirm=addRenderableWidget(new Control(leftPos+198,topPos+232,146,20,"锻炼 · 精炼",b->send(100)));
        accept=addRenderableWidget(new Control(leftPos+198,topPos+232,71,20,"采用新胚",b->send(101)));
        reject=addRenderableWidget(new Control(leftPos+273,topPos+232,71,20,"保留原胚",b->send(102)));
        Control close=addRenderableWidget(new Control(leftPos+334,topPos+5,18,18,"关闭",b->onClose()));close.close=true;
        rebuild();
    }
    private void rebuild(){
        for(var b:choices)removeWidget(b);choices.clear();mode=reforge();int count=mode?4:3;
        for(int i=0;i<count;i++){final int n=i;var b=new Control(leftPos+14,topPos+60+i*(mode?23:30),78,mode?21:26,mode?EquipmentCrafting.TEMPER[i]:STRATEGIES[i],w->send((mode?90:80)+n));b.choice=i;choices.add(addRenderableWidget(b));}
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
    private void compactCentered(GuiGraphics g,String text,int cx,int y,int width,int color){
        float size=Math.min(.8f,width/(float)Math.max(1,font.width(text)));
        g.pose().pushPose();g.pose().translate(leftPos+cx,topPos+y,0);g.pose().scale(size,size,1);
        g.drawString(font,text,-font.width(text)/2,0,color,false);g.pose().popPose();
    }
    private void panel(GuiGraphics g,String skin,int x,int y,int w,int h){
        frame(g,skin,leftPos+x,topPos+y,w,h);g.fill(leftPos+x+3,topPos+y+3,leftPos+x+w-3,topPos+y+h-3,0xFF080907);
    }
    private int owned(Item item){return item==WuXingItems.SPIRIT_STONE.get()?(int)Math.min(Integer.MAX_VALUE,SpiritCurrency.value(minecraft.player.getInventory().items)):minecraft.player.getInventory().items.stream().filter(st->st.is(item)).mapToInt(ItemStack::getCount).sum();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;frame(g,"window",x,y,imageWidth,imageHeight);sprite(g,"header",x+1,y+1,358,27);sprite(g,"header_title",x+148,y+3,64,21);
        panel(g,"left_clean",8,32,90,124);panel(g,"equipment_clean",104,32,90,124);panel(g,"preview_clean",200,32,152,124);
        panel(g,"inventory_clean",8,160,176,98);panel(g,"preview_clean",190,160,162,98);
        boolean waiting=pending(),isReforge=reforge();var n=EquipmentCrafting.read(menu.equipment());var candidate=n.getCompound("Pending");int selected=waiting?Math.max(0,Math.min(3,candidate.getInt("Type"))):isReforge?Math.max(0,Math.min(3,menu.selected()-30)):Math.max(0,Math.min(2,menu.selected()-20));
        int level=EquipmentCrafting.refine(menu.equipment()),target=Math.min(9,level+1);
        refineTab.selected=!isReforge;reforgeTab.selected=isReforge;refineTab.active=!waiting;reforgeTab.active=!waiting;
        for(var button:choices){button.selected=button.choice==selected;button.active=!waiting;}
        label(g,"待炼之器",110,38,GOLD);InscriptionScreen.frame(g,"equipment_slot",x+133,y+55,32,32);
        center(g,menu.equipment().isEmpty()?"置入装备":font.plainSubstrByWidth(menu.equipment().getHoverName().getString(),78),149,93,TEXT);
        center(g,"精炼 +"+level,149,106,GOLD);center(g,"铭纹 "+n.getCompound("Runes").size()+" / "+EquipmentCrafting.capacity(menu.equipment()),149,119,TEXT);
        g.fill(x+112,y+132,x+186,y+133,0xFF54472D);
        compactCentered(g,n.contains("TemperBp")?"胚性："+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,n.getInt("Temper")))]:"尚无胚性",149,140,76,MUTED);
        label(g,waiting?"重铸结果预览":isReforge?"重铸预览":"精炼预览",208,38,GOLD);
        if(waiting){
            int type=Math.max(0,Math.min(3,candidate.getInt("Type")));small(g,"新胚 · "+EquipmentCrafting.TEMPER[type]+" / "+quality(candidate.getInt("Quality")),208,53,GOLD);
            int yy=67;for(String text:effects(type,candidate.getInt("Bp")/10000d)){small(g,text,208,yy,TEXT);yy+=11;}
            label(g,"原胚对照",208,94,GOLD);
            if(n.contains("TemperBp")){
                int old=Math.max(0,Math.min(3,n.getInt("Temper")));small(g,EquipmentCrafting.TEMPER[old]+" / "+quality(n.getInt("TemperQuality")),208,107,TEXT);yy=119;
                for(String text:effects(old,n.getInt("TemperBp")/10000d)){small(g,text,208,yy,MUTED);yy+=11;}
            }else small(g,"原器尚无胚性",208,109,MUTED);
            small(g,"采用后才替换原胚性",208,144,TEXT);
        }else{
            if(isReforge){label(g,"重铸 · "+EquipmentCrafting.TEMPER[selected],208,53,GOLD);small(g,"普通60% · 上佳30%",208,66,TEXT);small(g,"极品9% · 天成1%",208,76,GOLD);}
            else{
                label(g,level>=9?"精炼已满阶 +9":"精炼 +"+level+" → +"+target,208,55,GOLD);String chance=level>=9?"—":menu.value(2)+"%";label(g,chance,344-font.width(chance),55,GOLD);
                InscriptionScreen.frame(g,"search",x+208,y+70,136,6);int fill=Math.max(0,Math.min(100,menu.value(2)))*132/100;g.fill(x+210,y+72,x+210+fill,y+74,0xFFE5BA55);
            }
            label(g,"所需材料（行囊扣除）",208,isReforge?86:83,GOLD);var costs=costs();
            if(costs.size()<=2){
                for(int i=0;i<costs.size();i++){
                    var cost=costs.get(i);int sy=(isReforge?98:95)+i*(isReforge?23:24),available=owned(cost.item());
                    InscriptionScreen.frame(g,"slot",x+208,y+sy,20,20);g.renderItem(new ItemStack(cost.item()),x+210,y+sy+2);frame(g,"row",x+231,y+sy,113,20);
                    label(g,cost.name(),236,sy+5,TEXT);String count=available+"/"+cost.required();compactCentered(g,count,321,sy+6,44,available>=cost.required()?TEXT:WARN);
                }
            }else{
                int step=costs.size()==5?29:costs.size()==4?34:42,total=24+(costs.size()-1)*step,start=200+(152-total)/2,sy=isReforge?99:97;
                for(int i=0;i<costs.size();i++){
                    var cost=costs.get(i);int sx=start+i*step,available=owned(cost.item());InscriptionScreen.frame(g,"slot",x+sx,y+sy,24,24);g.renderItem(new ItemStack(cost.item()),x+sx+4,y+sy+4);
                    compactCentered(g,available+"/"+cost.required(),sx+12,sy+26,step-2,available>=cost.required()?TEXT:WARN);compactCentered(g,cost.name(),sx+12,sy+36,step-2,MUTED);
                }
            }
            if(isReforge){small(g,"极品保底 "+n.getInt("ReforgeRare")+"/10",208,145,TEXT);small(g,"天成 "+n.getInt("ReforgeNatural")+"/50",282,145,GOLD);}
            else{int fails=n.getCompound("RefinePity").getInt("L"+target),pity=EquipmentCrafting.PITY[target-1];small(g,"灵力 "+menu.value(8),208,145,TEXT);small(g,"连败 "+fails+"/"+pity,286,145,MUTED);}
        }
        label(g,isReforge?"胚性效果上限":"本次提升",198,166,GOLD);
        List<String> detail=isReforge?effects(selected,1):List.of("基础伤害 +1.5% / 阶","护甲 +1% / 阶","最大耐久 +2% / 阶");int line=178;
        for(String text:detail){small(g,text,198,line,TEXT);line+=10;}
        if(isReforge)small(g,"实际加成依灵韵变化",198,200,MUTED);
        if(waiting){small(g,"采用后替换 · 材料不返",198,212,WARN);}
        else{
            int fire=menu.value(11)>>1;sprite(g,"fire",x+198,y+209,8,11);small(g,"异火 "+fire+"阶",210,211,MUTED);
            sprite(g,menu.value(9)==1?"ready":"alert",x+267,y+210,9,9);small(g,menu.value(9)==1?"条件齐备":"条件未齐",280,211,menu.value(9)==1?GOLD:WARN);
        }
        small(g,waiting?"待选结果期间不能再次重铸":isReforge?"结果可弃 · 材料不返":selected==1?"高阶失败可能降阶":"失败耗材 · 装备保留",198,223,MUTED);
        confirm.visible=!waiting;confirm.active=menu.value(9)==1&&!waiting;confirm.setMessage(Component.literal(isReforge?"开炉 · 重铸":"锻炼 · 精炼"));accept.visible=reject.visible=waiting;accept.active=reject.active=waiting;
        String help=waiting?"采用才会替换原胚性":menu.value(9)!=1?EquipmentCrafting.REASONS.get(Math.max(0,Math.min(EquipmentCrafting.REASONS.size()-1,menu.value(10)))):isReforge?"生成新胚后可选择采用或保留；材料不退还":selected==1?"挑战+7～9失败时30%降一阶，其余保留":"失败消耗材料，精炼等级保留";
        confirm.setTooltip(Tooltip.create(Component.literal(help)));accept.setTooltip(Tooltip.create(Component.literal("替换为新胚性并保留装备")));reject.setTooltip(Tooltip.create(Component.literal("保留原胚性，本次材料不退还")));
        label(g,"行囊",15,164,GOLD);for(int i=2;i<menu.slots.size();i++){var slot=menu.getSlot(i);InscriptionScreen.frame(g,"slot",x+slot.x-1,y+slot.y-1,18,18);}
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        try{renderBackground(g);super.render(g,(int)(mx/scale),(int)(my/scale),partial);
            if(!menu.equipment().isEmpty()&&menu.getCarried().isEmpty()){g.pose().pushPose();g.pose().translate(0,0,250);InscriptionScreen.frame(g,"equipment_slot",leftPos+133,topPos+55,32,32);g.pose().translate(leftPos+137,topPos+59,0);g.pose().scale(1.5f,1.5f,1);g.renderItem(menu.equipment(),0,0);g.pose().popPose();}
        }finally{g.pose().popPose();}renderTooltip(g,mx,my);
    }
    @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){var s=menu.getSlot(0);if(x==s.x&&y==s.y&&w==16&&h==16)return mx>=leftPos+133&&mx<leftPos+165&&my>=topPos+55&&my<topPos+87;return super.isHovering(x,y,w,h,mx,my);}
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
                if(mode){Item icon=switch(choice){case 0->Items.IRON_SWORD;case 1->WuXingItems.DARK_IRON.get();case 2->Items.ENDER_PEARL;default->Items.FEATHER;};g.renderItem(new ItemStack(icon),getX()+4,getY()+(height-16)/2);}
                else sprite(g,ICONS[choice],getX()+4,getY()+(height-18)/2,18,18);
                g.drawString(font,getMessage(),getX()+26,getY()+(mode?6:4),active?GOLD:MUTED,false);
                String sub=mode?switch(choice){case 0->"增伤与代价";case 1->"提升耐久";case 2->"减少耗灵";default->"攻速与轻盈";}:STRATEGY_TIPS[choice];
                setTooltip(Tooltip.create(Component.literal(sub)));
                if(!mode){g.pose().pushPose();g.pose().translate(getX()+26,getY()+16,0);g.pose().scale(.65f,.65f,1);g.drawString(font,sub,0,0,MUTED,false);g.pose().popPose();}
            }else g.drawCenteredString(font,getMessage(),getX()+width/2,getY()+(height-9)/2,active?(isHoveredOrFocused()?GOLD:TEXT):MUTED);
            if(!active)g.fill(getX()+3,getY()+3,getX()+width-3,getY()+height-3,0x77000000);
            if(active&&isHoveredOrFocused())g.renderOutline(getX()+1,getY()+1,width-2,height-2,0xFFA28B60);
        }
    }
}
