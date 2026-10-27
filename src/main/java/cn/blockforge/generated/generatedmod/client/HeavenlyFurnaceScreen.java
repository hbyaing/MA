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
    private final List<HeavenlyFurnaceArt.Button> categories=new ArrayList<>();
    private Button start,previous,next;
    private final int[] auxiliarySlots={-1,-1,-1};
    private final List<InkButton> auxiliaryButtons=new ArrayList<>();
    public HeavenlyFurnaceScreen(CompoundTag tag){
        super(Component.literal("天地为炉 · 炼丹 / 炼器"));UiLayoutConfig.reload();token=tag.getUUID("Token");
        rows=tag.getList("Rows",Tag.TAG_COMPOUND).stream().map(t->((CompoundTag)t).copy()).toList();filtered=rows;
    }
    @Override protected void init(){
        choices.clear();auxiliaryButtons.clear();categories.clear();String query=search==null?"":search.getValue();
        boxWidth=360;boxHeight=284;listWidth=118;visible=6;
        uiScale=Math.min(1f,Math.min((width-12f)/boxWidth,(height-18f)/boxHeight));width=(int)(width/uiScale);height=(int)(height/uiScale);
        left=(width-boxWidth)/2;top=(height-boxHeight)/2;
        search=new ForgedSearchBox(font,left+34,top+38,81,18,Component.literal("搜索丹药 / 器物"));search.setHint(Component.literal("搜索丹药 / 器物"));search.setTextColor(CultivationUi.TEXT);search.setMaxLength(60);search.setValue(query);search.setResponder(v->filterRows());addRenderableWidget(search);
        String[] tabs={"全部","炼丹","炼器"};for(int i=0;i<3;i++){final int value=i-1;var button=new HeavenlyFurnaceArt.Button(left+132+i*74,top+38,70,18,tabs[i],b->{mode=value;filterRows();});categories.add(addRenderableWidget(button));}
        previous=addRenderableWidget(new HeavenlyFurnaceArt.Button(left+14,top+254,22,20,"上一页","previous",b->{page=Math.max(0,page-1);rebuildRows();}));
        next=addRenderableWidget(new HeavenlyFurnaceArt.Button(left+98,top+254,22,20,"下一页","next",b->{page=Math.min(Math.max(0,(filtered.size()-1)/visible),page+1);rebuildRows();}));
        start=addRenderableWidget(new HeavenlyFurnaceArt.Button(left+132,top+254,164,20,"造化开炉",b->{if(selected>=0&&selected<filtered.size()){var row=filtered.get(selected);updateAuxiliaryButtons();Network.CHANNEL.sendToServer(new HeavenlyFurnaceStartPacket(token,new ResourceLocation(row.getString("Recipe")),alchemySelected()?auxiliarySlots:new int[]{-1,-1,-1}));minecraft.setScreen(null);}}));
        addRenderableWidget(new HeavenlyFurnaceArt.Button(left+302,top+254,50,20,"返回",b->onClose()));
        for(int i=0;i<3;i++){final int role=i;auxiliaryButtons.add(addRenderableWidget(new HeavenlyFurnaceArt.Button(left+132+i*74,top+225,70,25,"不选",b->cycleAuxiliary(role)){
            @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
                float tint=!active?.5f:isHoveredOrFocused()?1f:.73f;g.setColor(tint,tint,tint,1);HeavenlyFurnaceArt.frame(g,"button",getX(),getY(),getWidth(),getHeight());g.setColor(1,1,1,1);
                HeavenlyFurnaceArt.centered(g,font,Component.translatable(roleKey(role)),getX()+getWidth()/2,getY()+3,CultivationUi.GOLD);
                String value=getMessage().getString();int slot=auxiliarySlots[role];
                if(slot>=0&&minecraft!=null&&minecraft.player!=null){var herb=minecraft.player.getInventory().getItem(slot);String age=HerbAge.years(herb)+"年";value=font.plainSubstrByWidth(herb.getHoverName().getString(),Math.max(9,getWidth()-8-font.width(age)))+age;}
                HeavenlyFurnaceArt.centered(g,font,value,getX()+getWidth()/2,getY()+14,active?CultivationUi.TEXT:CultivationUi.MUTED);
            }
        }));}
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
            choices.add(addRenderableWidget(new InkButton(left+14,top+62+line*30,106,28,label,b->{selected=index;start.active=row.getString("Reason").isEmpty();updateAuxiliaryButtons();}){
                @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
                    boolean chosen=selected==index;float tint=chosen||isHoveredOrFocused()?1f:.68f;g.setColor(tint,tint,tint,1);HeavenlyFurnaceArt.frame(g,chosen?"selected":"row",getX(),getY(),getWidth(),getHeight());g.setColor(1,1,1,1);
                    HeavenlyFurnaceArt.frame(g,"slot",getX()+4,getY()+4,20,20);g.renderItem(ItemStack.of(row.getCompound("Result")),getX()+6,getY()+6);
                    g.drawString(font,font.plainSubstrByWidth(name(row),74),getX()+28,getY()+6,row.getString("Reason").isEmpty()?CultivationUi.TEXT:CultivationUi.MUTED,false);
                    g.drawString(font,row.getString("Mode").equals("refining")?"器方":"丹方",getX()+28,getY()+17,CultivationUi.MUTED,false);
                }
            }));
        }
        if(previous!=null)previous.active=page>0;if(next!=null)next.active=(page+1)*visible<filtered.size();
        if(start!=null)start.active=selected>=0&&selected<filtered.size()&&filtered.get(selected).getString("Reason").isEmpty();
        updateAuxiliaryButtons();
    }
    private boolean alchemySelected(){return selected>=0&&selected<filtered.size()&&!filtered.get(selected).getString("Mode").equals("refining");}
    private static String roleKey(int role){return "screen.wuxingxiantu.heavenly_aux."+new String[]{"chance","effect","output"}[role];}
    private boolean auxiliaryAvailable(int role,int slot){
        if(minecraft==null||minecraft.player==null||slot<0||slot>=36)return false;
        ItemStack stack=minecraft.player.getInventory().getItem(slot);
        if(stack.isEmpty()||HerbAge.legacyEffectBand(stack)<1||AuxiliaryAlchemy.role(stack)!=AuxiliaryAlchemy.Role.values()[role])return false;
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
            renderBackground(g);HeavenlyFurnaceArt.frame(g,"frame",left,top,boxWidth,boxHeight);
            HeavenlyFurnaceArt.centered(g,font,"天地为炉",left+180,top+10,CultivationUi.GOLD);HeavenlyFurnaceArt.centered(g,font,"炼丹 / 炼器",left+180,top+24,CultivationUi.MUTED);
            for(int i=0;i<categories.size();i++)categories.get(i).chosen=mode==i-1;
            HeavenlyFurnaceArt.frame(g,"search",left+14,top+38,106,18);
            HeavenlyFurnaceArt.icon(g,"search_icon",left+21,top+41,9);
            int x=left+132,y=top+62,w=220;
            if(selected>=0&&selected<filtered.size()){
                boolean alchemy=alchemySelected();var row=filtered.get(selected);HeavenlyFurnaceArt.frame(g,"panel",x,y,w,32);
                g.renderItem(ItemStack.of(row.getCompound("Result")),x+9,y+8);
                g.drawString(font,font.plainSubstrByWidth(name(row),w-40),x+34,y+5,CultivationUi.TEXT,false);
                g.drawString(font,alchemy?"万物为铜 · 丹方":"万物为铜 · 器方",x+34,y+19,CultivationUi.MUTED,false);
                stat(g,x,top+100,70,"所需灵力",Integer.toString(row.getInt("Cost")),CultivationUi.CYAN);
                stat(g,x+74,top+100,70,"炼制时间",String.format(Locale.ROOT,"%.1f秒",row.getInt("Duration")/20f),CultivationUi.GOLD);
                stat(g,x+148,top+100,70,alchemy?"废丹率≤":"炼制失败率",row.getInt("Failure")+"%",0xD7A58E);
                g.drawString(font,"炼制用材",x,top+134,CultivationUi.GOLD,false);
                var ingredients=row.getList("Ingredients",Tag.TAG_COMPOUND);int columns=8;
                for(int i=0;i<ingredients.size();i++){var item=ItemStack.of(ingredients.getCompound(i));int sx=x+(i%columns)*27,sy=top+148+(i/columns)*21;
                    HeavenlyFurnaceArt.frame(g,"slot",sx-1,sy-1,20,20);g.renderItem(item,sx+1,sy+1);g.renderItemDecorations(font,item,sx+1,sy+1);
                    if(mx>=sx&&mx<sx+18&&my>=sy&&my<sy+18)hoveredMaterial=item;
                }
                int reasonY=top+148+Math.max(1,(ingredients.size()+columns-1)/columns)*21+4;
                String reason=row.getString("Reason");g.drawWordWrap(font,Component.literal(reason.isEmpty()?"材料齐备 · 可开炉":reason),x,reasonY,w,reason.isEmpty()?0x9DD2AE:0xED9E8B);
                if(alchemy)g.drawString(font,"副药可选 · 每项1株",x,top+212,CultivationUi.MUTED,false);
                else {String[] flow={"万物入炉","阴阳熔炼","造化铸器"};for(int i=0;i<3;i++){HeavenlyFurnaceArt.frame(g,"panel",x+i*74,top+225,70,25);HeavenlyFurnaceArt.centered(g,font,flow[i],x+i*74+35,top+233,CultivationUi.GOLD);}}
            }else{
                HeavenlyFurnaceArt.frame(g,"panel",x,y,w,188);HeavenlyFurnaceArt.centered(g,font,"未找到配方",x+w/2,y+65,CultivationUi.GOLD);
                g.drawWordWrap(font,Component.literal("更换分类或搜索词，查看已掌握的丹方与器方。"),x+14,y+89,w-28,CultivationUi.MUTED);
            }
            HeavenlyFurnaceArt.centered(g,font,(page+1)+" / "+Math.max(1,(filtered.size()+visible-1)/visible),left+67,top+260,CultivationUi.MUTED);
            super.render(g,mx,my,partial);
        }finally{g.flush();g.pose().popPose();}
        if(!hoveredMaterial.isEmpty())g.renderTooltip(font,hoveredMaterial,screenX,screenY);
    }
    private void stat(GuiGraphics g,int x,int y,int w,String label,String value,int color){
        HeavenlyFurnaceArt.frame(g,"panel",x,y,w,27);HeavenlyFurnaceArt.centered(g,font,label,x+w/2,y+3,CultivationUi.MUTED);HeavenlyFurnaceArt.centered(g,font,value,x+w/2,y+15,color);
    }
    @Override public boolean mouseClicked(double x,double y,int b){return super.mouseClicked(x/uiScale,y/uiScale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased(x/uiScale,y/uiScale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged(x/uiScale,y/uiScale,b,dx/uiScale,dy/uiScale);}
    @Override public boolean mouseScrolled(double x,double y,double d){return super.mouseScrolled(x/uiScale,y/uiScale,d);}
    @Override public boolean isPauseScreen(){return false;}
}
