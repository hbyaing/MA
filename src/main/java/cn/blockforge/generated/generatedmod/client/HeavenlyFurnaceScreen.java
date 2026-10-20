package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import cn.blockforge.generated.generatedmod.network.packet.HeavenlyFurnaceStartPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Recipe catalogue is supplied by the server; start always revalidates inventory and all gates. */
public final class HeavenlyFurnaceScreen extends Screen {
    private final UUID token;
    private final List<CompoundTag> rows;
    private List<CompoundTag> filtered;
    private int mode=-1;
    private int page,selected=-1,left,top,boxWidth,boxHeight,listWidth,visible;
    private EditBox search;
    private float uiScale=1;
    private final List<ForgedUi.Button> categories=new ArrayList<>();
    private Button start;
    private final int[] auxiliarySlots={-1,-1,-1};
    private final List<InkButton> auxiliaryButtons=new ArrayList<>();
    public HeavenlyFurnaceScreen(CompoundTag tag){
        super(Component.literal("天地为炉 · 炼丹 / 炼器"));UiLayoutConfig.reload();token=tag.getUUID("Token");
        rows=tag.getList("Rows",Tag.TAG_COMPOUND).stream().map(t->((CompoundTag)t).copy()).toList();filtered=rows;
    }
    @Override protected void init(){
        choices.clear();auxiliaryButtons.clear();
        // `window` is the workbench reference frame.  Keep the production
        // furnace canvas fixed so an old screenshot export cannot resize the
        // live controls and make the background art drift away from them.
        String query=search==null?"":search.getValue();boxWidth=430;boxHeight=330;
        uiScale=Math.min(1f,Math.min((width-12f)/boxWidth,(height-18f)/boxHeight));width=(int)(width/uiScale);height=(int)(height/uiScale);categories.clear();
        left=(width-boxWidth)/2;top=(height-boxHeight)/2;listWidth=UiLayoutConfig.width("heavenly","recipes",155);visible=6;
        search=new ForgedSearchBox(font,left+UiLayoutConfig.x("heavenly","search",28),top+UiLayoutConfig.y("heavenly","search",48),listWidth-36,18,Component.literal("搜索丹药 / 器物"));search.setHint(Component.literal("搜索丹药 / 器物"));
        search.setBordered(false);search.setTextColor(0xE7D6B3);search.setMaxLength(60);search.setValue(query);search.setResponder(s->filterRows());addRenderableWidget(search);
        String[] categories={"全部","炼丹","炼器"};
        int categoryX=UiLayoutConfig.x("heavenly","categories",listWidth+12),categoryY=UiLayoutConfig.y("heavenly","categories",46),categoryStep=Math.max(1,UiLayoutConfig.width("heavenly","categories",249)/3);
        for(int i=0;i<3;i++){final int selectedMode=i-1;var button=new ForgedUi.Button(left+categoryX+i*categoryStep,top+categoryY,79,21,categories[i],b->{mode=selectedMode;filterRows();});this.categories.add(addRenderableWidget(button));}
        int actionsY=UiLayoutConfig.y("heavenly","actions",boxHeight-30),actionsX=UiLayoutConfig.x("heavenly","actions",listWidth+12),actionsW=UiLayoutConfig.width("heavenly","actions",boxWidth-listWidth-74);
        addRenderableWidget(new ForgedUi.Button(left+UiLayoutConfig.x("heavenly","previous",12),top+actionsY,24,20,"‹",b->{page=Math.max(0,page-1);rebuildRows();}));
        addRenderableWidget(new ForgedUi.Button(left+UiLayoutConfig.x("heavenly","next",listWidth-36),top+actionsY,24,20,"›",b->{page=Math.min(Math.max(0,(filtered.size()-1)/visible),page+1);rebuildRows();}));
        start=addRenderableWidget(new ForgedUi.Button(left+actionsX,top+actionsY,Math.max(50,actionsW),20,"造化开炉",b->{if(selected>=0){var row=filtered.get(selected);updateAuxiliaryButtons();Network.CHANNEL.sendToServer(new HeavenlyFurnaceStartPacket(token,new ResourceLocation(row.getString("Recipe")),alchemySelected()?auxiliarySlots:new int[]{-1,-1,-1}));minecraft.setScreen(null);}}));
        addRenderableWidget(new ForgedUi.Button(left+UiLayoutConfig.x("heavenly","back",boxWidth-54),top+actionsY,42,20,"返回",b->onClose()));((ForgedUi.Button)start).primary=true;
        int auxiliaryX=left+UiLayoutConfig.x("heavenly","details",listWidth+12);
        int auxiliaryWidth=UiLayoutConfig.width("heavenly","details",boxWidth-listWidth-24),column=(auxiliaryWidth-8)/3;
        for(int i=0;i<3;i++){
            final int role=i;
            auxiliaryButtons.add(addRenderableWidget(new InkButton(auxiliaryX+i*(column+4),top+260,column,28,
                    Component.translatable("screen.wuxingxiantu.heavenly_aux.none"),b->cycleAuxiliary(role))));
        }
        rebuildRows();
    }
    private final List<Button> choices=new ArrayList<>();
    private void filterRows(){String query=search==null?"":search.getValue();filtered=rows.stream().filter(r->mode<0||(mode==1)==r.getString("Mode").equals("refining")).filter(r->(name(r)+(r.getString("Mode").equals("refining")?" 炼器 器方":" 炼丹 丹方")).contains(query)).toList();page=0;selected=-1;rebuildRows();}
    private void rebuildRows(){
        if(!filtered.isEmpty()&&(selected<page*visible||selected>=Math.min(filtered.size(),(page+1)*visible)))selected=page*visible;
        for(var b:choices)removeWidget(b);choices.clear();
        for(int line=0;line<visible;line++){int index=page*visible+line;if(index>=filtered.size())break;var row=filtered.get(index);
            var label=Component.literal(font.plainSubstrByWidth(name(row),listWidth-33));
            label.withStyle(s->s.withColor(row.getString("Reason").isEmpty()?0xC8EBD9:0xAAA496));
            int rowX=UiLayoutConfig.x("heavenly","recipes",12),rowY=UiLayoutConfig.y("heavenly","recipes",75),rowStep=Math.max(1,UiLayoutConfig.height("heavenly","recipes",210)/6);
            choices.add(addRenderableWidget(new InkButton(left+rowX,top+rowY+line*rowStep,listWidth-20,32,label,b->{selected=index;start.active=row.getString("Reason").isEmpty();updateAuxiliaryButtons();},row.getString("Reason").isEmpty()?0xFF8BBEAB:0xFF756D5A){
                @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
                    boolean chosen=selected==index;int edge=chosen?0xFF70D8E3:isHoveredOrFocused()?0xFFDBBC7F:0xFF5C5140;
                    ForgedUi.choice(g,getX(),getY(),getWidth(),getHeight(),chosen);if(isHoveredOrFocused())g.renderOutline(getX()+2,getY()+2,getWidth()-4,getHeight()-4,edge);
                    ForgedUi.slot(g,getX()+4,getY()+5,22);g.renderItem(ItemStack.of(row.getCompound("Result")),getX()+7,getY()+8);
                    g.drawString(font,font.plainSubstrByWidth(name(row),getWidth()-35),getX()+31,getY()+8,row.getString("Reason").isEmpty()?0xDBCBA6:0x8B9187,false);
                    g.drawString(font,row.getString("Mode").equals("refining")?"器方":"丹方",getX()+31,getY()+20,0x96958D,false);if(chosen)g.fill(getX(),getY()+3,getX()+2,getY()+getHeight()-3,0xFF70D8E3);
                }
            }));
        }
        if(start!=null)start.active=selected>=0&&selected<filtered.size()&&filtered.get(selected).getString("Reason").isEmpty();
        updateAuxiliaryButtons();
    }
    private boolean alchemySelected(){return selected>=0&&selected<filtered.size()&&!filtered.get(selected).getString("Mode").equals("refining");}
    private static String roleKey(int role){return "screen.wuxingxiantu.heavenly_aux."+new String[]{"chance","effect","output"}[role];}
    private boolean auxiliaryAvailable(int role,int slot){
        if(minecraft==null||minecraft.player==null||slot<0||slot>=36)return false;
        ItemStack stack=minecraft.player.getInventory().getItem(slot);
        if(stack.isEmpty()||HerbAge.effectiveTier(stack)<1||AuxiliaryAlchemy.role(stack)!=AuxiliaryAlchemy.Role.values()[role])return false;
        for(int other=0;other<3;other++)if(other!=role&&auxiliarySlots[other]==slot)return false;
        return true;
    }
    private void cycleAuxiliary(int role){
        if(!alchemySelected())return;
        var available=new ArrayList<Integer>();available.add(-1);
        for(int slot=0;slot<36;slot++)if(auxiliaryAvailable(role,slot))available.add(slot);
        int index=available.indexOf(auxiliarySlots[role]);
        auxiliarySlots[role]=available.get((index+1)%available.size());
        updateAuxiliaryButtons();
    }
    private void updateAuxiliaryButtons(){
        boolean alchemy=alchemySelected();
        if(!alchemy)Arrays.fill(auxiliarySlots,-1);
        for(int role=0;role<auxiliaryButtons.size();role++){
            if(auxiliarySlots[role]>=0&&!auxiliaryAvailable(role,auxiliarySlots[role]))auxiliarySlots[role]=-1;
            var button=auxiliaryButtons.get(role);button.visible=alchemy;
            boolean any=false;for(int slot=0;slot<36&&!any;slot++)any=auxiliaryAvailable(role,slot);
            button.active=alchemy&&any;
            ItemStack chosen=auxiliarySlots[role]<0||minecraft==null||minecraft.player==null?ItemStack.EMPTY:minecraft.player.getInventory().getItem(auxiliarySlots[role]);
            Component value=chosen.isEmpty()?Component.translatable("screen.wuxingxiantu.heavenly_aux.none"):
                    Component.translatable("screen.wuxingxiantu.heavenly_aux.herb",chosen.getHoverName(),HerbAge.years(chosen));
            button.setMessage(value);
            var tip=Component.translatable(roleKey(role)).append(Component.literal("\n")).append(value).append(Component.literal("\n"))
                    .append(Component.translatable("screen.wuxingxiantu.heavenly_aux.instruction"));
            button.setTooltip(Tooltip.create(tip));
        }
    }
    @Override public void tick(){updateAuxiliaryButtons();}
    private static String name(CompoundTag r){return ItemStack.of(r.getCompound("Result")).getHoverName().getString();}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        int screenX=mx,screenY=my;ItemStack hoveredMaterial=ItemStack.EMPTY;
        g.pose().pushPose();g.pose().scale(uiScale,uiScale,1);mx=(int)(mx/uiScale);my=(int)(my/uiScale);
        try{
        renderBackground(g);ForgedUi.window(g,left,top,boxWidth,boxHeight,"天地为炉","炼丹 / 炼器");
        for(int i=0;i<categories.size();i++)categories.get(i).chosen=mode==i-1;
        int recipePanelX=UiLayoutConfig.x("heavenly","recipes",8),recipePanelY=UiLayoutConfig.y("heavenly","recipes",43),recipePanelW=listWidth-10,recipePanelH=UiLayoutConfig.height("heavenly","recipes",245);
        ForgedUi.panel(g,left+recipePanelX,top+recipePanelY,recipePanelW,recipePanelH,false);
        ForgedUi.search(g,left+UiLayoutConfig.x("heavenly","search",10),top+UiLayoutConfig.y("heavenly","search",45),listWidth-14,22);
        int x=left+UiLayoutConfig.x("heavenly","details",listWidth+12),y=top+UiLayoutConfig.y("heavenly","details",75),w=UiLayoutConfig.width("heavenly","details",boxWidth-listWidth-24);
        if(selected>=0&&selected<filtered.size()){
            boolean alchemy=alchemySelected();int header=alchemy?36:42;
            var row=filtered.get(selected);ForgedUi.panel(g,x,y,w,header,true);
            g.pose().pushPose();g.pose().translate(x+7,y+3,0);g.pose().scale(2,2,1);g.renderItem(ItemStack.of(row.getCompound("Result")),0,0);g.pose().popPose();
            g.drawString(font,font.plainSubstrByWidth(name(row),w-49),x+45,y+8,0xFFF0D2,false);
            g.drawString(font,row.getString("Mode").equals("refining")?"万物为铜 · 器方":"万物为铜 · 丹方",x+45,y+22,0x95AA9E,false);y+=header+(alchemy?4:6);
            int card=(w-8)/3;
            stat(g,x,y,card,"所需灵力",Integer.toString(row.getInt("Cost")),0x8BE3D8);
            stat(g,x+card+4,y,card,"炼制时间",String.format(Locale.ROOT,"%.1f秒",row.getInt("Duration")/20f),0xDCC48E);
            stat(g,x+2*card+8,y,card,row.getString("Mode").equals("refining")?"炼制失败率":"废丹率≤",row.getInt("Failure")+"%",0xC8A79A);y+=alchemy?34:38;
            g.drawString(font,"炼制用材",x,y,0xCABA96,false);y+=13;
            var ingredients=row.getList("Ingredients",Tag.TAG_COMPOUND);int spacing=36,columns=Math.max(1,w/spacing),ingredientRow=alchemy?28:32;
            for(int i=0;i<ingredients.size();i++){var item=ItemStack.of(ingredients.getCompound(i));int sx=x+(i%columns)*spacing,sy=y+(i/columns)*ingredientRow;ForgedUi.slot(g,sx-2,sy-2,26);g.renderItem(item,sx,sy);g.renderItemDecorations(font,item,sx,sy);
                if(mx>=sx&&mx<sx+16&&my>=sy&&my<sy+16)hoveredMaterial=item;}
            y+=((ingredients.size()+columns-1)/columns)*ingredientRow+5;
            String reason=row.getString("Reason");g.drawWordWrap(font,Component.literal(reason.isEmpty()?"材料齐备 · 可开炉":reason),x,y,w,reason.isEmpty()?0x9DD2AE:0xED9E8B);
            if(alchemy){
                g.drawString(font,Component.translatable("screen.wuxingxiantu.heavenly_aux.summary"),x,top+234,0xCABA96,false);
                int column=(w-8)/3;
                for(int role=0;role<3;role++)g.drawCenteredString(font,Component.translatable(roleKey(role)),x+role*(column+4)+column/2,top+248,0xDCC48E);
            }else if(y+20<top+251){
                ForgedUi.label(g,"炼制流程",x,top+251,ForgedUi.GOLD);String[] flow={"万物入炉",row.getString("Mode").equals("refining")?"阴阳熔炼":"阴阳炼化",row.getString("Mode").equals("refining")?"造化铸器":"合气凝丹"};
                int cw=(w-8)/3;for(int i=0;i<3;i++){int fx=x+i*(cw+4);ForgedUi.panel(g,fx,top+265,cw,24,true);ForgedUi.flow(g,i,fx+4,top+270);g.drawCenteredString(font,flow[i],fx+cw/2+7,top+273,ForgedUi.GOLD);}
            }
        }else {
            int cx=x+w/2,cy=y+52;for(int i=0;i<36;i++){double a=i*Math.PI*2/36;g.fill(cx+(int)(Math.cos(a)*34),cy+(int)(Math.sin(a)*25),cx+(int)(Math.cos(a)*34)+2,cy+(int)(Math.sin(a)*25)+2,0xFFAF9967);}
            g.drawCenteredString(font,"天地为炉",cx,cy-4,0xE7C589);
            g.drawWordWrap(font,Component.literal("天地为炉，造化为工，阴阳为炭，万物为铜。择丹方或器方，取背包材料于天地间炼化成形。"),x,y+92,w,0xB9C8BC);
        }
        g.drawCenteredString(font,(page+1)+" / "+Math.max(1,(filtered.size()+visible-1)/visible),left+listWidth/2,top+boxHeight-24,0xB3C7BD);
        super.render(g,mx,my,partial);
        }finally{g.pose().popPose();}
        if(!hoveredMaterial.isEmpty())g.renderTooltip(font,hoveredMaterial,screenX,screenY);
    }
    private void stat(GuiGraphics g,int x,int y,int w,String label,String value,int color){
        boolean compact=boxHeight<280;
        ForgedUi.panel(g,x,y,w,31,true);
        g.drawCenteredString(font,label,x+w/2,y+3,0x8C9E94);g.drawCenteredString(font,value,x+w/2,y+(compact?15:17),color);
    }
    @Override public boolean mouseClicked(double x,double y,int b){return super.mouseClicked(x/uiScale,y/uiScale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased(x/uiScale,y/uiScale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged(x/uiScale,y/uiScale,b,dx/uiScale,dy/uiScale);}
    @Override public boolean mouseScrolled(double x,double y,double d){return super.mouseScrolled(x/uiScale,y/uiScale,d);}
    @Override public boolean isPauseScreen(){return false;}
}
