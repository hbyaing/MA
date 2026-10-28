package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.alchemy.research.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import java.util.*;
/** Approved V2 layout. All text and specimens are live, not baked examples. */
public final class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final String[] TABS={"鉴定","实验","反推","丹方","谱系","认证"};
    private final List<ResearchTableArt.Button> tabs=new ArrayList<>();private ResearchTableArt.Button inspect,record,previous,next;
    public ResearchTableScreen(ResearchTableMenu m,Inventory i,Component t){super(m,i,t);imageWidth=440;imageHeight=300;}
    @Override protected void init(){super.init();tabs.clear();
        for(int i=0;i<6;i++){final int page=i;tabs.add(addRenderableWidget(new ResearchTableArt.Button(leftPos+13+i*70,topPos+34,66,17,TABS[i],false,b->action(page))));}
        inspect=addRenderableWidget(new ResearchTableArt.Button(leftPos+154,topPos+196,110,19,"开始鉴定",true,b->action(menu.page()==1||menu.page()==2||menu.page()==4||menu.page()==5?menu.primaryAction():menu.page()==3?22:10)));
        record=addRenderableWidget(new ResearchTableArt.Button(leftPos+275,topPos+196,96,19,"记录结果",false,b->action(menu.page()==1||menu.page()==2||menu.page()==4||menu.page()==5?menu.secondaryAction():menu.page()==3?23:11)));
        previous=addRenderableWidget(new ResearchTableArt.Button(leftPos+150,topPos+176,36,14,"上页",false,b->action(20)));
        next=addRenderableWidget(new ResearchTableArt.Button(leftPos+240,topPos+176,36,14,"下页",false,b->action(21)));update();
    }
    private void action(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void update(){for(int i=0;i<tabs.size();i++)tabs.get(i).chosen=menu.page()==i;boolean page=menu.page()==0;inspect.active=page&&menu.unlocked()&&!menu.specimen().isEmpty();record.active=page&&menu.analyzed()&&!menu.recorded()&&menu.supplies()&&menu.records()<64;
        record.setMessage(Component.literal(menu.recorded()?"已记录":"记录结果"));inspect.setMessage(Component.literal("开始鉴定"));
        if(menu.page()==3){inspect.setMessage(Component.literal("研习传承"));record.setMessage(Component.literal(menu.currentTemplateSaved()?"已存模板":"保存模板"));inspect.active=menu.unlocked()&&menu.canStudy();record.active=menu.unlocked()&&menu.canSaveTemplate()&&menu.supplies();}
        boolean investigation=menu.page()==1||menu.page()==2||menu.page()==4||menu.page()==5;
        if(investigation){inspect.setMessage(Component.literal(ResearchWorkspace.actionLabel(menu.primaryAction())));record.setMessage(Component.literal(ResearchWorkspace.actionLabel(menu.secondaryAction())));inspect.active=menu.unlocked()&&menu.primaryEnabled();record.active=menu.unlocked()&&menu.secondaryEnabled()&&(menu.secondaryAction()!=38||menu.supplies());}
        previous.visible=next.visible=menu.page()>=1&&menu.page()<=5;previous.active=next.active=menu.unlocked();
        inspect.setTooltip(Tooltip.create(Component.literal(menu.page()==3?"每次学习一层可理解的概念；火候概念需要实际批次。阅读不增加实践经验。":menu.unlocked()?"验证样本凭证，观察已有公开信息；保留样本。":"先学习炼丹入门。")));
        record.setTooltip(Tooltip.create(Component.literal(menu.page()==3?"理解全部前置后，消耗纸墨各1保存带来源版本的未验证模板；重复保存不扣费。":"消耗纸与墨囊各1，保存个人记录；同一样本不重复扣费，不授予经验或认证。")));
        if(menu.page()==5){inspect.setTooltip(Tooltip.create(Component.literal("冻结后该角色接下来的规定批次全部计入；请先悬停正文查看锁定范围与不可撤回的样本规则。")));record.setTooltip(Tooltip.create(Component.literal("完成规定批次后提交；30批中24批成功不满足95%下界80%的要求。")));}
        else if(investigation){inspect.setTooltip(Tooltip.create(Component.literal(menu.page()==1?"选本人真实批次作基准；严格检验条件，无法隔离的变化不称单因素。":menu.page()==2?"只使用已知观测和已研习模型；改候选不改实物，实际炼制须在丹炉进行。":"选定父方，下一次保存保留祖方基准与累计路径。")));record.setTooltip(Tooltip.create(Component.literal(menu.page()==1?"比较当前批次与基准，不消耗纸墨；两批不能直接证明因果或统计独立。":menu.page()==2?"消耗纸墨各1保存候选；不授经验、成丹或认证，同一内容重复保存不扣费。":"最多选择两个父源；合并保留两支来源，不复制其成功率或认证。")));}}
    @Override protected void containerTick(){super.containerTick();update();}
    private void label(GuiGraphics g,String s,int x,int y,int color){g.drawString(font,s,leftPos+x,topPos+y,color,false);}
    private void centered(GuiGraphics g,String s,int x,int y,int color){label(g,s,x-font.width(s)/2,y,color);}
    private void lines(GuiGraphics g,List<String> text,int x,int y,int width,int color){for(String line:text){var rows=font.split(Component.literal(line),width);for(var row:rows){g.drawString(font,row,leftPos+x,topPos+y,color,false);y+=12;}y+=4;}}
    private void compact(GuiGraphics g,String text,int x,int y,int width,int rows){compact(g,text,x,y,width,rows,12);}
    private void compact(GuiGraphics g,String text,int x,int y,int width,int rows,int step){for(var row:font.split(Component.literal(text),width)){if(rows--<=0)break;g.drawString(font,row,leftPos+x,topPos+y,0x302C22,false);y+=step;}}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){ResearchTableArt.background(g,leftPos,topPos);
        g.pose().pushPose();g.pose().translate(leftPos+220,topPos+11,0);g.pose().scale(1.35F,1.35F,1);g.drawString(font,"丹道研析台",-font.width("丹道研析台")/2,0,0x292317,false);g.pose().popPose();centered(g,"研究样本",73,63,0xE7D19A);centered(g,TABS[menu.page()]+(menu.page()==0?"记录":"研究"),213,63,0xE7D19A);centered(g,"研究线索",361,63,0xE7D19A);
        label(g,"可鉴定",18,120,0xDCD5B4);ItemStack[] examples={new ItemStack(WuXingItems.WASTE_PILL.get()),new ItemStack(WuXingItems.PILL_ASH.get()),new ItemStack(WuXingItems.ALCHEMY_RESIDUE.get())};String[] names={"废丹","丹灰","药渣"};
        for(int i=0;i<3;i++){g.renderItem(examples[i],leftPos+24+i*39,topPos+134);centered(g,names[i],32+i*39,153,0xDCD5B4);}centered(g,"纸",48,202,0xDCD5B4);centered(g,"墨",96,202,0xDCD5B4);label(g,"背包",83,226,0xDCD5B4);
        label(g,"已知条件",309,82,0xE8D5A2);label(g,"待证假设",309,142,0xE8D5A2);
        boolean observed=menu.analyzed();var sample=menu.specimen();var result=PillSnapshot.view(sample);List<String> body;
        if(menu.page()==0)body=observed?List.of("药效："+ResearchView.effect(sample),result==null?"品级：沿用旧制记载":"品级："+result.grade()+"品","药性：尚需深入辨析","来源：样本观测"):List.of("放入成丹或残留物","开始鉴定后查看记录",menu.unlocked()?"基础观察保留样本":"需要学习炼丹入门");
        else body=switch(menu.page()){
            case 1->List.of("个人鉴定记录："+menu.records(),menu.lastRecord().isEmpty()?"尚无已保存记录":"最近："+menu.lastRecord(),"实际炼制仍在丹炉进行","对照研究将在后续开放");
            case 2->List.of("从可见证据提出假设","保留多种可能解释","正式反推将在后续开放","不会显示原始配方");
            case 3->List.of("已掌握传承："+menu.knownFormulas(),"知识与控火技艺分别记录","候选模板将在后续开放");
            case 4->List.of("记录祖方与版本来源","改方需验证有效范围","研究谱系将在后续开放");
            default->List.of("以真实独立批次复验","失败与事故全部计入","独立认证将在后续开放");};
        boolean research=menu.page()>=1&&menu.page()<=5;
        if(research)compact(g,menu.researchText(0),150,89,124,7);else lines(g,body,150,90,124,0x302C22);
        if(research){compact(g,menu.researchText(1),312,100,103,3,10);compact(g,menu.researchText(2),312,158,103,2);}
        else{lines(g,List.of(observed?"样本已通过凭证检查":"尚未完成样本观察","个人记录："+menu.records()),312,100,103,0x302C22);
        label(g,"材料及年份未知",312,158,0x302C22);label(g,"工序条件待研究",312,170,0x302C22);}
        g.pose().pushPose();g.pose().translate(leftPos+314,topPos+182,0);g.pose().scale(.8F,.8F,1);g.drawString(font,"不显示原始配方",0,0,0x514B36,false);g.pose().popPose();
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);
        if((menu.page()>=1&&menu.page()<=5)&&mx>=leftPos+150&&mx<leftPos+278&&my>=topPos+88&&my<topPos+173){String detail=menu.researchText(0)+"\n"+menu.researchText(3);g.renderComponentTooltip(font,List.of(Component.literal(detail)),mx,my);}}
}
