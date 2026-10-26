package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import java.util.*;

/** Approved drawing/engraving artwork with live menu items and score data. */
public final class TalismanPlatformScreen extends AbstractContainerScreen<TalismanPlatformMenu> {
    private static final int CW=TalismanCatalog.CANVAS_WIDTH,CH=TalismanCatalog.CANVAS_HEIGHT;
    private static final int ROWS=5;
    private final boolean[][] drawn=new boolean[CH][CW];
    private String selectedId;
    private boolean expanded,painting,draggingScroll,draggingDescription;
    private int descriptionScroll;
    private int lastCol=-1,lastRow=-1,scroll;
    private float uiScale=1;
    private double scrollGrab;
    private ForgedSearchBox search;
    private String query="";
    private List<String> filtered=List.of();
    private final List<ForgedUi.Button> choices=new ArrayList<>(),controls=new ArrayList<>(),inkButtons=new ArrayList<>();
    private record CanvasArea(int x,int y,int cell){}
    /**
     * Offset used to center the visible glyph inside the paper.  The catalog
     * stores logical cells, while the paper is a visual viewport; keeping the
     * offset in one place prevents the guide and the player's stroke from
     * drifting apart when a glyph has asymmetric bounds.
     */
    private record GlyphOffset(int x,int y){}
    private String profile(){return switch(menu.kind()){case DRAW->"draw";case ENGRAVE->"engrave";case RUB->"rub";};}
    private int ux(String element,int fallback){return UiLayoutConfig.x(profile(),element,fallback);}
    private int uy(String element,int fallback){return UiLayoutConfig.y(profile(),element,fallback);}
    private int uw(String element,int fallback){return UiLayoutConfig.width(profile(),element,fallback);}
    private int uh(String element,int fallback){return UiLayoutConfig.height(profile(),element,fallback);}
    public TalismanPlatformScreen(TalismanPlatformMenu menu,Inventory inv,Component title){
        super(menu,inv,title);UiLayoutConfig.reload();imageWidth=menu.kind()==TalismanPlatformBlockEntity.Kind.RUB?240:TalismanPlatformMenu.PANEL_WIDTH;imageHeight=menu.kind()==TalismanPlatformBlockEntity.Kind.RUB?250:TalismanPlatformMenu.PANEL_HEIGHT;
    }
    private boolean drawing(){return menu.kind()!=TalismanPlatformBlockEntity.Kind.RUB;}
    private boolean approved(){return menu.kind()!=TalismanPlatformBlockEntity.Kind.RUB;}
    private CanvasArea canvas(){
        if(!expanded)return approved()?new CanvasArea(leftPos+ux("paper",166),topPos+uy("paper",75),4):new CanvasArea(leftPos+ux("paper",171),topPos+uy("paper",78),4);
        int cell=Math.max(4,Math.min((width-190)/CW,(height-80)/CH));
        return new CanvasArea((width-CW*cell-146)/2+12,(height-CH*cell)/2+8,cell);
    }
    private int expandedActionsY(){return canvas().y+96;}
    private int inkX(int index){return expanded?canvas().x+CW*canvas().cell+30:leftPos+TalismanPlatformMenu.inkX(menu.kind());}
    private int inkY(int index){if(expanded)return canvas().y+index*26;int base=uy("ink",approved()?64:75);int configuredStep=uh("inkStepY",approved()?19:26);int step=Math.max(18,configuredStep<=32?configuredStep:(approved()?19:26));return topPos+base+index*step;}
    @Override protected void init(){
        uiScale=Math.min(1f,Math.min((width-12f)/imageWidth,(height-18f)/imageHeight));
        width=(int)(width/uiScale);height=(int)(height/uiScale);super.init();
        painting=false;lastCol=lastRow=-1;draggingScroll=false;
        var ids=TalismanCatalog.ids();if(selectedId==null&&!ids.isEmpty())selectedId=ids.get(Math.floorMod(menu.selectedIndex(),ids.size()));
        rebuildControls();
    }
    private ForgedUi.Button button(int x,int y,int w,int h,String text,Runnable action){
        var b=(approved()||!drawing())?new TalismanV12Art.Button(x,y,w,h,text,"primary",k->action.run()):new ForgedUi.Button(x,y,w,h,text,k->action.run());controls.add(addRenderableWidget(b));return b;
    }
    private ForgedUi.Button icon(int x,int y,int w,int h,String label,String skin,Runnable action){var b=new TalismanV12Art.Button(x,y,w,h,label,skin,k->action.run());controls.add(addRenderableWidget(b));return b;}
    private void rebuildControls(){
        if(search!=null){query=search.getValue();removeWidget(search);}search=null;
        for(var b:choices)removeWidget(b);choices.clear();for(var b:controls)removeWidget(b);controls.clear();inkButtons.clear();
        if(!drawing()){
            var action=button(leftPos+ux("actions",66),topPos+uy("actions",116),UiLayoutConfig.width(profile(),"actions",108),UiLayoutConfig.height(profile(),"actions",21),"批量拓印",this::rub);action.primary=true;
            int inkButtonX=ux("inkButtons",98),inkButtonY=uy("inkButtons",56),inkButtonStep=Math.max(16,uh("inkButtons",60)/3);
            for(int i=0;i<3;i++){final int ink=i;inkButtons.add(icon(leftPos+inkButtonX,topPos+inkButtonY+i*inkButtonStep,uw("inkButtons",16),uh("inkButtons",16),"选用墨水 "+(i+1),"brush",()->{menu.previewInk(ink);if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100+ink);}));}
            return;
        }
        if(expanded){
            var a=canvas();int x=a.x+CW*a.cell+8,y=expandedActionsY();
            icon(x+2,y+2,12,12,"缩小绘制","collapse",this::toggleExpanded);
            var submit=button(x,y+26,112,23,menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"镌刻印章":"绘制符箓",this::submit);submit.primary=true;
        }else{
            if(drawing()){
                search=new ForgedSearchBox(font,leftPos+ux("search",38),topPos+uy("search",approved()?71:75),uw("search",74),uh("search",14),Component.literal("搜索符箓"));search.setTextColor(ForgedUi.IVORY);search.setHint(Component.literal("搜索符箓…"));search.setMaxLength(64);search.setValue(query);
                search.setResponder(s->{query=s;scroll=0;rebuildChoices();});addRenderableWidget(search);rebuildChoices();
                if(approved())icon(leftPos+ux("expand",243),topPos+uy("expand",49),uw("expand",15),uh("expand",15),"放大绘制","expand",this::toggleExpanded);else button(leftPos+ux("expand",185),topPos+uy("expand",210),uw("expand",80),uh("expand",13),"放大绘制",this::toggleExpanded);
            }
            var action=button(leftPos+ux("actions",approved()?336:289),topPos+uy("actions",approved()?300:299),uw("actions",approved()?54:120),uh("actions",21),drawing()?(menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"镌刻印章":"绘制符箓"):"开始拓印",drawing()?this::submit:this::rub);action.primary=true;
        }
        int start=TalismanPlatformBlockEntity.inkStart(menu.kind());
        if(start>=0)for(int i=0;i<TalismanPlatformBlockEntity.INK_COUNT;i++){
            final int ink=i;Runnable choose=()->{
                menu.previewInk(ink);if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100+ink);
            };inkButtons.add(approved()?icon(inkX(i)-(expanded?22:20),inkY(i),16,16,"选用墨水 "+(i+1),"brush",choose):button(inkX(i)+75,inkY(i),44,18,"选用",choose));
        }
    }
    private void rebuildChoices(){
        for(var b:choices)removeWidget(b);choices.clear();
        String q=query.strip().toLowerCase(Locale.ROOT);
        filtered=TalismanCatalog.ids().stream().filter(id->TalismanCatalog.title(id).getString().toLowerCase(Locale.ROOT).contains(q)).toList();
        scroll=Math.max(0,Math.min(scroll,Math.max(0,filtered.size()-ROWS)));
        int listX=ux("choices",approved()?15:19),listY=uy("choices",approved()?92:99),listW=uw("choices",approved()?101:93),listH=uh("choices",22),configuredStep=uh("choiceStep",23),listStep=Math.max(listH,configuredStep<=32?configuredStep:23);
        for(int i=0;i<ROWS&&scroll+i<filtered.size();i++){
            String id=filtered.get(scroll+i),label=TalismanCatalog.title(id).getString();
            var b=approved()?new TalismanV12Art.Button(leftPos+listX,topPos+listY+i*listStep,listW,listH,label,"choice",k->select(id)):new ForgedUi.Button(leftPos+listX,topPos+listY+i*listStep,listW,listH,label,k->select(id));b.choice=true;b.chosen=id.equals(selectedId);
            choices.add(addRenderableWidget(b));
        }
    }
    private void select(String id){
        if(Objects.equals(id,selectedId))return;selectedId=id;descriptionScroll=0;clearDrawn();
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanSelectPacket(menu.blockPos(),TalismanCatalog.ids().indexOf(id)));
        rebuildChoices();
    }
    private void clearDrawn(){for(var row:drawn)Arrays.fill(row,false);lastCol=lastRow=-1;painting=false;}
    private void toggleExpanded(){expanded=!expanded;painting=false;lastCol=lastRow=-1;rebuildControls();}
    private void submit(){if(selectedId!=null)Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanSubmitPacket(menu.blockPos(),selectedId,TalismanCatalog.encode(drawn)));}
    private void rub(){Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.talisman.TalismanRubPacket(menu.blockPos(),16));}
    private static boolean inRect(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private boolean isOverGrid(double mx,double my){var a=canvas();return drawing()&&inRect(mx,my,a.x,a.y,CW*a.cell,CH*a.cell);}
    private GlyphOffset glyphOffset(String id){
        if(id==null||!TalismanCatalog.has(id)||!drawing())return new GlyphOffset(0,0);
        var target=TalismanCatalog.glyph(id);int minC=CW,maxC=-1,minR=CH,maxR=-1;
        for(int r=0;r<CH;r++)for(int c=0;c<CW;c++)if(target[r][c]){minC=Math.min(minC,c);maxC=Math.max(maxC,c);minR=Math.min(minR,r);maxR=Math.max(maxR,r);}
        if(maxC<0)return new GlyphOffset(0,0);
        return new GlyphOffset((CW-(maxC-minC+1))/2-minC,(CH-(maxR-minR+1))/2-minR);
    }
    private int[] gridAt(double x,double y){
        var a=canvas();int c=(int)Math.floor((x-a.x)/a.cell),r=(int)Math.floor((y-a.y)/a.cell);
        if(c<0||r<0||c>=CW||r>=CH)return null;
        var offset=glyphOffset(selectedId);c-=offset.x();r-=offset.y();
        return c<0||r<0||c>=CW||r>=CH?null:new int[]{c,r};
    }
    private void drawLine(int x0,int y0,int x1,int y1){
        int dx=Math.abs(x1-x0),sx=x0<x1?1:-1,dy=-Math.abs(y1-y0),sy=y0<y1?1:-1,err=dx+dy;
        while(true){if(x0>=0&&y0>=0&&x0<CW&&y0<CH)drawn[y0][x0]=true;if(x0==x1&&y0==y1)break;int e=2*err;if(e>=dy){err+=dy;x0+=sx;}if(e<=dx){err+=dx;y0+=sy;}}
    }
    private int inkColor(){int start=TalismanPlatformBlockEntity.inkStart(menu.kind());if(start>=0){var s=menu.slots.get(start+menu.selectedInk()).getItem();if(s.is(WuXingItems.SPIRIT_INK.get()))return 0xFF000000;}return 0xFFC0392B;}
    private void renderCanvas(GuiGraphics g){
        var a=canvas();
        if(approved()&&expanded){
            int sx=(int)((a.x-3)*uiScale),sy=(int)((a.y-3)*uiScale),ex=(int)((a.x+CW*a.cell+3)*uiScale),ey=(int)((a.y+CH*a.cell+3)*uiScale);
            g.enableScissor(sx,sy,ex,ey);
            TalismanV12Art.image(g,menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"seal_plate_clean":"paper_clean",a.x-3,a.y-3,CW*a.cell+6,CH*a.cell+6,230,422);
        }else if(!approved()){g.fill(a.x,a.y,a.x+CW*a.cell,a.y+CH*a.cell,0xFFD8B25A);g.renderOutline(a.x,a.y,CW*a.cell,CH*a.cell,0xFF8A6A2E);}
        String id=selectedId;boolean[][] marks=drawn;boolean guide=drawing();
        if(!drawing()){ItemStack seal=menu.slots.get(TalismanPlatformBlockEntity.RUB_SEAL).getItem();id=TalismanData.getTalisman(seal);marks=TalismanCatalog.glyph(id);}
        if(id==null||!TalismanCatalog.has(id))return;var target=TalismanCatalog.glyph(id);
        GlyphOffset offset=glyphOffset(id);
        for(int r=0;r<CH;r++)for(int c=0;c<CW;c++){
            boolean mark=marks!=null&&r<marks.length&&marks[r]!=null&&c<marks[r].length&&marks[r][c];
            int drawC=c+(guide?offset.x():0),drawR=r+(guide?offset.y():0);
            if(drawC<0||drawR<0||drawC>=CW||drawR>=CH)continue;
            // Keep a balanced two-cell paper margin on both sides; source
            // glyphs with uneven red guide edges are cropped to this viewport.
            int paperMargin=drawing()?2:0;
            if(drawC<paperMargin||drawC>=CW-paperMargin||drawR<paperMargin||drawR>=CH-paperMargin)continue;
            if(mark||(guide&&target[r][c]))g.fill(a.x+drawC*a.cell,a.y+drawR*a.cell,a.x+drawC*a.cell+Math.max(1,a.cell-1),a.y+drawR*a.cell+Math.max(1,a.cell-1),mark?inkColor():0x64000000);
        }
        if(approved()&&expanded)g.disableScissor();
    }
    private void text(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,s,leftPos+x,topPos+y,color,false);}
    private void port(GuiGraphics g,int slot,String label){var s=menu.slots.get(slot);text(g,label,s.x,s.y-13,ForgedUi.MUTED);}
    private List<net.minecraft.util.FormattedCharSequence> description(){
        if(selectedId==null)return List.of();
        var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();
        lines.addAll(font.split(Component.translatable("talisman.wuxingxiantu."+selectedId+".description"),98));
        lines.addAll(font.split(Component.literal("临摹符文后提交，品级由绘制评分决定。"),98));
        lines.addAll(font.split(Component.literal((menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"镌刻消耗：金锭 × ":"绘制消耗：墨水 × ")+TalismanCatalog.drawCost(selectedId)),98));
        return lines;
    }
    private int descriptionMax(){return Math.max(0,description().size()*13-76);}
    private void thumb(GuiGraphics g,int x,int y,int height,int content,int viewport,int value){
        g.fill(x,y,x+2,y+height,0xFF17272C);int max=Math.max(0,content-viewport);
        int h=max==0?height:Math.max(10,height*viewport/content),travel=height-h;
        g.fill(x,y+(max==0?0:travel*value/max),x+2,y+(max==0?0:travel*value/max)+h,max==0?0xFF444B48:0xFF9A947C);
    }
    private void renderApproved(GuiGraphics g){
        // Use the continuous authored header; an opaque title patch breaks
        // both its color and the outer frame's top edge.
        TalismanV12Art.background(g,menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"engrave_background":"background",leftPos,topPos);
        g.pose().pushPose();g.pose().translate(leftPos+215,topPos+13,0);g.pose().scale(1.5f,1.5f,1);g.drawCenteredString(font,title,0,0,ForgedUi.GOLD);g.pose().popPose();
        g.drawCenteredString(font,"择符 · 运墨 · 成器",leftPos+215,topPos+30,ForgedUi.GOLD);
        boolean engrave=menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE;
        text(g,"符谱",16,50,ForgedUi.GOLD);text(g,engrave?"临摹印纹":"临写符文",137,50,ForgedUi.GOLD);text(g,engrave?"刻刀与材料":"墨水与材料",274,50,ForgedUi.GOLD);
        text(g,engrave?"镌刻评分":"绘制评分",274,engrave?114:132,ForgedUi.GOLD);text(g,"随身物品",136,234,ForgedUi.GOLD);text(g,"结算",313,252,ForgedUi.GOLD);
        if(engrave){text(g,"刻刀",303,68,ForgedUi.GOLD);text(g,"金锭",364,68,ForgedUi.GOLD);}else text(g,"符纸",363,73,ForgedUi.GOLD);text(g,"成品",352,267,ForgedUi.GOLD);
        if(filtered.isEmpty())text(g,"未找到匹配符箓",17,99,ForgedUi.MUTED);
        if(filtered.size()>ROWS){int listX=ux("choices",approved()?15:19),listY=uy("choices",approved()?92:99),trackX=ux("choiceScrollbar",121),trackY=uy("choiceScrollbar",93);thumb(g,leftPos+trackX,topPos+trackY,uh("choiceScrollbar",113),filtered.size(),ROWS,scroll);}
        if(selectedId!=null){
            text(g,font.plainSubstrByWidth(TalismanCatalog.title(selectedId).getString(),102),16,226,ForgedUi.GOLD);
            var lines=description();descriptionScroll=Math.max(0,Math.min(descriptionScroll,descriptionMax()));
            int descX=ux("description",16),descY=uy("description",243),descW=uw("description",101),descH=uh("description",76);
            g.enableScissor((int)((leftPos+descX)*uiScale),(int)((topPos+descY)*uiScale),(int)((leftPos+descX+descW)*uiScale),(int)((topPos+descY+descH)*uiScale));
            for(int i=0;i<lines.size();i++)g.drawString(font,lines.get(i),leftPos+descX,topPos+descY+i*13-descriptionScroll,ForgedUi.IVORY,false);
            g.disableScissor();thumb(g,leftPos+ux("descriptionScrollbar",121),topPos+uy("descriptionScrollbar",242),uh("descriptionScrollbar",77),Math.max(descH,lines.size()*13),descH,descriptionScroll);
        }
        if(engrave)TalismanV12Art.image(g,"seal_plate",leftPos+157,topPos+68,85,143,278,470);
        renderCanvas(g);
        for(var s:menu.slots){if(s.x<0)continue;TalismanV12Art.slot(g,leftPos+s.x-1,topPos+s.y-1);}
        int start=TalismanPlatformBlockEntity.inkStart(menu.kind());
        if(start>=0)for(int i=0;i<3;i++){var item=menu.slots.get(start+i).getItem();if(!item.isEmpty())text(g,font.plainSubstrByWidth(item.getHoverName().getString(),36),323,68+i*19,ForgedUi.MUTED);}
        int total=selectedId==null?0:TalismanCatalog.targetCount(selectedId),hit=0,miss=0;
        if(total>0){var target=TalismanCatalog.glyph(selectedId);for(int r=0;r<CH;r++)for(int c=0;c<CW;c++)if(drawn[r][c]){if(target[r][c])hit++;else miss++;}}
        float base=selectedId==null?0:TalismanCatalog.score(selectedId,drawn);int rank=PlayerProgressionClient.rank("talisman");
        float score=cn.blockforge.generated.generatedmod.progression.PlayerCrafts.talismanScore(base,rank);
        String[] labels={"覆盖率","越界率","基础评分","符师","职业加成","最终评分"};
        String[] values={String.format(Locale.ROOT,"%.1f%%",total==0?0:100f*hit/total),String.format(Locale.ROOT,"%.1f%%",total==0?0:100f*miss/total),String.format(Locale.ROOT,"%.1f",base),rank==0?"未入品":new String[]{"","一","二","三","四","五","六","七","八","九"}[Math.min(rank,9)]+"品",String.format(Locale.ROOT,"+%.1f",score-base),String.format(Locale.ROOT,"%.1f · %s",score,TalismanCatalog.qualityOf(score).title().getString())};
        for(int i=0;i<6;i++){int y=engrave?128+i*14:146+i*13;text(g,labels[i],274,y,ForgedUi.IVORY);text(g,values[i],414-font.width(values[i]),y,i==5?ForgedUi.GOLD:ForgedUi.IVORY);}
        String[] scoreNotes={"基础评分 ≥50 时加成","下品60 · 中品80 · 极品95"};
        g.pose().pushPose();
        g.pose().translate(leftPos+344,topPos+(engrave?223:229),0);
        float noteScale=engrave?1f:.85f;
        g.pose().scale(noteScale,noteScale,1);
        for(int i=0;i<scoreNotes.length;i++)
            g.drawString(font,scoreNotes[i],-font.width(scoreNotes[i])/2,(engrave?12:10)*i,ForgedUi.MUTED,false);
        g.pose().popPose();
    }
    private void renderCompactRub(GuiGraphics g){
        TalismanV12Art.image(g,"rub_compact_background",leftPos,topPos,240,250,993,1131);
        g.drawCenteredString(font,title,leftPos+120,topPos+13,ForgedUi.GOLD);
        text(g,"印章",20,62,ForgedUi.GOLD);text(g,"墨水",118,43,ForgedUi.GOLD);text(g,"符纸",185,62,ForgedUi.GOLD);text(g,"成品",213,62,ForgedUi.GOLD);
        var seal=menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).getItem();String id=TalismanData.getTalisman(seal);boolean valid=TalismanCatalog.has(id);
        var quality=TalismanData.quality(seal);if(quality==null||quality==TalismanCatalog.Quality.WASTE)quality=TalismanCatalog.Quality.LOW;
        text(g,valid?font.plainSubstrByWidth(TalismanCatalog.title(id).getString(),50):"等待印章",42,76,ForgedUi.IVORY);
        text(g,valid?quality.title().getString().replace("符",""):"",42,90,ForgedUi.MUTED);
        for(var slot:menu.slots)if(slot.x>=0)TalismanV12Art.slot(g,leftPos+slot.x-1,topPos+slot.y-1);
        int start=TalismanPlatformBlockEntity.inkStart(menu.kind());
        for(int i=0;i<3;i++){var ink=menu.getSlot(start+i).getItem();if(!ink.isEmpty())text(g,font.plainSubstrByWidth(ink.getHoverName().getString(),36),140,60+i*20,ForgedUi.MUTED);}
        int copies=rubCopies();
        for(var control:controls)if(control.getMessage().getString().equals("批量拓印"))control.active=copies>0;
        String note=copies>0?"本次可拓印 "+copies+" 张 · 每次最多16张":!valid?"请放入已镌刻印章":rubOutputBlocked()?"请先取出成品":"请放入符纸并选用有余量的墨水";
        g.pose().pushPose();g.pose().translate(leftPos+120,topPos+147,0);g.pose().scale(.85f,.85f,1);g.drawString(font,note,-font.width(note)/2,0,ForgedUi.MUTED,false);g.pose().popPose();
    }
    private boolean rubOutputBlocked(){
        var seal=menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).getItem();String id=TalismanData.getTalisman(seal);
        if(!TalismanCatalog.has(id))return false;
        var quality=TalismanData.quality(seal);if(quality==null||quality==TalismanCatalog.Quality.WASTE)quality=TalismanCatalog.Quality.LOW;
        var output=menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).getItem();
        return !output.isEmpty()&&(!ItemStack.isSameItemSameTags(output,TalismanData.create(id,1,quality))||output.getCount()+rubRequestedCopies()>output.getMaxStackSize()||output.getCount()>=output.getMaxStackSize());
    }
    private int rubCopies(){
        var seal=menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).getItem();String id=TalismanData.getTalisman(seal);
        if(!TalismanCatalog.has(id)||rubOutputBlocked())return 0;
        return rubRequestedCopies();
    }
    private int rubRequestedCopies(){
        String id=TalismanData.getTalisman(menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).getItem());
        if(!TalismanCatalog.has(id))return 0;
        var paper=menu.getSlot(TalismanPlatformBlockEntity.RUB_PAPER).getItem();
        var ink=menu.getSlot(TalismanPlatformBlockEntity.inkStart(menu.kind())+menu.selectedInk()).getItem();
        if(!paper.is(WuXingItems.TALISMAN_PAPER.get())||(!ink.is(WuXingItems.SPIRIT_INK.get())&&!ink.is(WuXingItems.CINNABAR_INK.get())))return 0;
        return Math.min(16,Math.min(paper.getCount(),Math.max(0,ink.getMaxDamage()-ink.getDamageValue())/TalismanCatalog.drawCost(id)));
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        if(!drawing()){renderCompactRub(g);return;}
        if(approved()){renderApproved(g);return;}
        ForgedUi.window(g,leftPos+4,topPos+12,imageWidth-8,imageHeight-16,title.getString(),"择符 · 运墨 · 成器");
        for(int[] p:new int[][]{{13,46,112,179},{132,46,142,179},{281,46,136,179},{13,232,261,98},{281,232,136,98}})ForgedUi.panel(g,leftPos+p[0],topPos+p[1],p[2],p[3],false);
        text(g,drawing()?"符谱":"印章信息",22,51,ForgedUi.GOLD);text(g,drawing()?"临写符文":"符纸预览",141,51,ForgedUi.GOLD);
        text(g,menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE?"刻刀与材料":"墨水与材料",290,51,ForgedUi.GOLD);
        if(drawing()){
            ForgedUi.search(g,leftPos+20,topPos+72,98,20);
            // Keep the unselected rows readable against the paper texture and
            // make the list rhythm match the framed title/search area.
            for(int i=0;i<ROWS;i++)
                ForgedUi.divider(g,leftPos+20,topPos+114+i*23,96);
            if(filtered.isEmpty())text(g,"未找到匹配符箓",24,108,ForgedUi.MUTED);
            if(filtered.size()>ROWS){
                int trackX=choiceTrackX(),trackY=choiceTrackY(),trackH=choiceTrackH();
                g.fill(leftPos+trackX,topPos+trackY,leftPos+trackX+4,topPos+trackY+trackH,0xFF17272C);
                int h=Math.max(18,ROWS*trackH/filtered.size()),travel=trackH-h;
                g.fill(leftPos+trackX,topPos+trackY+travel*scroll/(filtered.size()-ROWS),leftPos+trackX+4,topPos+trackY+travel*scroll/(filtered.size()-ROWS)+h,0xFF9A947C);
            }
        }else{
            port(g,TalismanPlatformBlockEntity.RUB_SEAL,"印章");var seal=menu.slots.get(TalismanPlatformBlockEntity.RUB_SEAL).getItem();String id=TalismanData.getTalisman(seal);var quality=TalismanData.quality(seal);
            text(g,id!=null&&TalismanCatalog.has(id)?TalismanCatalog.title(id).getString():"请放入已镌刻印章",24,126,ForgedUi.IVORY);
            text(g,quality==null?"等待印章":"品级："+quality.title().getString(),24,143,ForgedUi.MUTED);
            text(g,"无需重复描画",24,180,ForgedUi.MUTED);
        }
        for(int i=0;i<TalismanPlatformBlockEntity.SIZE;i++)if(TalismanPlatformBlockEntity.isActiveSlot(menu.kind(),i)){var s=menu.slots.get(i);ForgedUi.slot(g,leftPos+s.x-1,topPos+s.y-1,18);}
        if(menu.kind()==TalismanPlatformBlockEntity.Kind.ENGRAVE){port(g,TalismanPlatformBlockEntity.ENGRAVE_KNIFE,"刻刀");port(g,TalismanPlatformBlockEntity.ENGRAVE_GOLD,"金锭");port(g,TalismanPlatformBlockEntity.OUTPUT,"成品");text(g,"沿引导镌刻，消耗金锭。",290,132,ForgedUi.MUTED);}
        else{port(g,menu.kind()==TalismanPlatformBlockEntity.Kind.DRAW?TalismanPlatformBlockEntity.DRAW_PAPER:TalismanPlatformBlockEntity.RUB_PAPER,"符纸");port(g,TalismanPlatformBlockEntity.OUTPUT,"成品");for(int i=0;i<3;i++)text(g,"墨水 "+(i+1),312,80+26*i,ForgedUi.IVORY);text(g,"选中墨水后落笔",290,201,ForgedUi.MUTED);}
        renderCanvas(g);text(g,"随身物品",22,236,ForgedUi.GOLD);
        for(int i=TalismanPlatformBlockEntity.SIZE;i<menu.slots.size();i++){var s=menu.slots.get(i);ForgedUi.slot(g,leftPos+s.x-1,topPos+s.y-1,18);}
        float score=selectedId==null?0:cn.blockforge.generated.generatedmod.progression.PlayerCrafts.talismanScore(TalismanCatalog.score(selectedId,drawn),PlayerProgressionClient.rank("talisman"));
        text(g,drawing()&&selectedId!=null?TalismanCatalog.title(selectedId).getString():"印章拓印",290,239,ForgedUi.GOLD);
        text(g,drawing()?String.format(Locale.ROOT,"评分 %.0f · %s",score,TalismanCatalog.qualityOf(score).title().getString()):"消耗符纸与墨水复制印章。",290,256,ForgedUi.MUTED);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.pose().pushPose();g.pose().scale(uiScale,uiScale,1);int x=(int)(mx/uiScale),y=(int)(my/uiScale);
        try{
            for(int i=0;i<inkButtons.size();i++){var b=inkButtons.get(i);b.setMessage(Component.literal((approved()||!drawing())?"选用墨水 "+(i+1):(menu.selectedInk()==i?"使用中":"选用")));b.chosen=menu.selectedInk()==i;b.choice=b.chosen;}
            // Dim the world uniformly. A rectangular scissor here leaves an
            // opaque slab behind the artwork's cropped bevels and margins.
            renderBackground(g);
            // Keep world terrain, shader noise and UI assets outside the custom frame from leaking into the screen.
            g.fill(0,0,width,height,0xD00A1014);
            if(expanded){
                var a=canvas();int side=a.x+CW*a.cell+20;
                if(approved()){TalismanV12Art.frame(g,a.x-12,a.y-33,CW*a.cell+158,CH*a.cell+48);g.drawString(font,"精绘 · "+TalismanCatalog.title(selectedId).getString(),a.x,a.y-23,ForgedUi.GOLD,false);}else ForgedUi.window(g,a.x-12,a.y-33,CW*a.cell+158,CH*a.cell+48,"精绘 · "+TalismanCatalog.title(selectedId).getString(),"拖动落笔 · Esc返回");renderCanvas(g);
                int start=TalismanPlatformBlockEntity.inkStart(menu.kind());if(start>=0)for(int i=0;i<3;i++){int sx=inkX(i),sy=inkY(i);if(approved())TalismanV12Art.slot(g,sx-1,sy-1);else ForgedUi.slot(g,sx-1,sy-1,18);var item=menu.slots.get(start+i).getItem();g.renderItem(item,sx,sy);g.renderItemDecorations(font,item,sx,sy);if(!item.isEmpty())g.drawString(font,font.plainSubstrByWidth(item.getHoverName().getString(),62),sx+22,sy+5,ForgedUi.IVORY,false);}
                for(var b:controls)b.render(g,x,y,partial);
                if(approved()){
                    int sx=a.x+CW*a.cell+8,sy=expandedActionsY()+62;int rank=PlayerProgressionClient.rank("talisman");float base=selectedId==null?0:TalismanCatalog.score(selectedId,drawn),finalScore=cn.blockforge.generated.generatedmod.progression.PlayerCrafts.talismanScore(base,rank);
                    String[] info={"实时评分",String.format(Locale.ROOT,"基础 %.1f",base),String.format(Locale.ROOT,"职业加成 +%.1f",finalScore-base),String.format(Locale.ROOT,"最终 %.1f · %s",finalScore,TalismanCatalog.qualityOf(finalScore).title().getString()),"Esc 返回，保留笔迹"};
                    for(int i=0;i<info.length;i++)g.drawString(font,info[i],sx,sy+i*14,i==0||i==3?ForgedUi.GOLD:ForgedUi.MUTED,false);
                }
                if(!menu.getCarried().isEmpty()){g.renderItem(menu.getCarried(),x-8,y-8);g.renderItemDecorations(font,menu.getCarried(),x-8,y-8);}
            }else{super.render(g,x,y,partial);renderTooltip(g,x,y);}
        }finally{g.pose().popPose();}
    }
    private int listTop(){return uy("choices",approved()?92:99);}
    private int listX(){return ux("choices",approved()?15:19);}
    private int listWidth(){return uw("choices",approved()?101:93);}
    private int choiceTrackX(){return ux("choiceScrollbar",approved()?118:113);}
    private int choiceTrackY(){return uy("choiceScrollbar",approved()?93:100);}
    private int choiceTrackH(){return uh("choiceScrollbar",115);}
    private boolean scrollbar(double x,double y){return drawing()&&!expanded&&inRect(x,y,leftPos+choiceTrackX(),topPos+choiceTrackY(),9,choiceTrackH())&&filtered.size()>ROWS;}
    private void dragScroll(double y){int h=Math.max(18,ROWS*choiceTrackH()/filtered.size()),travel=choiceTrackH()-h;scroll=(int)Math.round(Math.max(0,Math.min(1,(y-topPos-listTop()-scrollGrab)/travel))*(filtered.size()-ROWS));rebuildChoices();}
    private void dragDescription(double y){int max=descriptionMax();if(max<=0)return;int descH=uh("description",76),h=Math.max(10,descH*descH/(description().size()*13)),travel=descH-h;descriptionScroll=(int)Math.round(Math.max(0,Math.min(1,(y-topPos-uy("description",243)-scrollGrab)/travel))*max);}
    @Override public boolean mouseClicked(double mx,double my,int button){
        double x=mx/uiScale,y=my/uiScale;
        if(button==0&&approved()&&!expanded&&inRect(x,y,leftPos+ux("descriptionScrollbar",118),topPos+uy("descriptionScrollbar",241),9,uh("descriptionScrollbar",79))&&descriptionMax()>0){int descH=uh("description",76),h=Math.max(10,descH*descH/(description().size()*13));double thumb=topPos+uy("descriptionScrollbar",242)+(descH-h)*descriptionScroll/(double)descriptionMax();scrollGrab=y>=thumb&&y<thumb+h?y-thumb:h/2.0;draggingDescription=true;dragDescription(y);return true;}
        if(button==0&&scrollbar(x,y)){int trackH=choiceTrackH(),h=Math.max(18,ROWS*trackH/filtered.size());double thumb=topPos+listTop()+(trackH-h)*scroll/(double)(filtered.size()-ROWS);scrollGrab=y>=thumb&&y<thumb+h?y-thumb:h/2.0;draggingScroll=true;dragScroll(y);return true;}
        if(button==0&&isOverGrid(x,y)){var c=gridAt(x,y);drawn[c[1]][c[0]]=true;lastCol=c[0];lastRow=c[1];painting=true;return true;}
        if(expanded){
            int start=TalismanPlatformBlockEntity.inkStart(menu.kind());if(start>=0)for(int i=0;i<3;i++)if(inRect(x,y,inkX(i),inkY(i),18,18)){slotClicked(menu.slots.get(start+i),start+i,button,net.minecraft.world.inventory.ClickType.PICKUP);return true;}
            for(var b:List.copyOf(controls))if(b.mouseClicked(x,y,button))return true;return true;
        }
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        double x=mx/uiScale,y=my/uiScale;if(draggingDescription){dragDescription(y);return true;}if(draggingScroll){dragScroll(y);return true;}
        if(painting&&button==0){var c=gridAt(x,y);if(c!=null){if(lastCol>=0)drawLine(lastCol,lastRow,c[0],c[1]);else drawn[c[1]][c[0]]=true;lastCol=c[0];lastRow=c[1];}else lastCol=lastRow=-1;return true;}
        return expanded||super.mouseDragged(x,y,button,dx/uiScale,dy/uiScale);
    }
    @Override public boolean mouseReleased(double x,double y,int b){painting=false;lastCol=lastRow=-1;draggingScroll=draggingDescription=false;return expanded||super.mouseReleased(x/uiScale,y/uiScale,b);}
    @Override public boolean mouseScrolled(double mx,double my,double delta){double x=mx/uiScale,y=my/uiScale;if(approved()&&!expanded&&inRect(x,y,leftPos+ux("description",13),topPos+uy("description",242),uw("description",112),uh("description",78))){descriptionScroll=Math.max(0,Math.min(descriptionMax(),descriptionScroll+(delta<0?13:-13)));return true;}if(!expanded&&drawing()&&inRect(x,y,leftPos+listX(),topPos+listTop()-1,listWidth(),choiceTrackH())){scroll+=delta<0?1:-1;rebuildChoices();return true;}return expanded||super.mouseScrolled(x,y,delta);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(expanded&&key==256){toggleExpanded();return true;}if(search!=null&&search.isFocused()&&key!=256&&key!=258){search.keyPressed(key,scan,modifiers);return true;}return super.keyPressed(key,scan,modifiers);}
    @Override public boolean isPauseScreen(){return false;}
}
