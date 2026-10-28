package cn.blockforge.generated.generatedmod.alchemy.research;
import cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge;
import cn.blockforge.generated.generatedmod.alchemy.PillCatalog;
import cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class ResearchTableMenu extends AbstractContainerMenu {
    public static final int WIDTH=440,HEIGHT=300;
    private static final int TEXT_LIMIT=640;
    private final Container table;private final Player viewer;private final BlockPos pos;private final ContainerData data=new SimpleContainerData(64+4*TEXT_LIMIT);
    private int selection;private ResearchCurriculum curriculum;
    private ResearchWorkspace workspace;
    private ItemStack inspected=ItemStack.EMPTY;
    public ResearchTableMenu(int id,Inventory inv,ResearchTableEntity table){this(id,inv,table,table.getBlockPos());}
    private ResearchTableMenu(int id,Inventory inv,Container table,BlockPos pos){super(cn.blockforge.generated.generatedmod.registry.ModMenus.ALCHEMY_RESEARCH_TABLE.get(),id);this.table=table;this.viewer=inv.player;this.pos=pos;
        addSlot(new Slot(table,0,64,88){public boolean mayPlace(ItemStack s){return ResearchView.sample(s);}public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(table,1,40,177){public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
        addSlot(new Slot(table,2,88,177){public boolean mayPlace(ItemStack s){return s.is(Items.INK_SAC);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,118+col*20,225+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,118+col*20,281));addDataSlots(data);refresh();
    }
    public static ResearchTableMenu fromNetwork(int id,Inventory inv,net.minecraft.network.FriendlyByteBuf b){return new ResearchTableMenu(id,inv,new SimpleContainer(3),b.readBlockPos());}
    public int page(){return data.get(0);}public boolean analyzed(){return data.get(1)>0;}public boolean recorded(){return data.get(1)==2;}public String lastRecord(){var text=new StringBuilder();for(int i=0;i<Math.min(48,data.get(5));i++)text.append((char)(data.get(6+i)&65535));return text.toString();}public int records(){return data.get(2);}public int knownFormulas(){return data.get(3);}public boolean unlocked(){return data.get(4)==1;}
    public ItemStack specimen(){return table.getItem(0);}public boolean supplies(){return table.getItem(1).is(Items.PAPER)&&table.getItem(2).is(Items.INK_SAC);}
    public boolean canStudy(){return data.get(55)==1;}public boolean canSaveTemplate(){return data.get(56)==1;}public boolean currentTemplateSaved(){return data.get(60)==1;}
    public int primaryAction(){return data.get(54);}public int secondaryAction(){return data.get(61);}public boolean primaryEnabled(){return (data.get(62)&1)!=0;}public boolean secondaryEnabled(){return (data.get(62)&2)!=0;}
    private boolean observedServer(){return analyzed()&&!inspected.isEmpty()&&ItemStack.matches(inspected,specimen());}
    private ResearchWorkspace workspace(net.minecraft.server.level.ServerPlayer p){if(workspace==null)workspace=new ResearchWorkspace(p);return workspace;}
    public int practiceCount(){return data.get(57);}public int diversePractice(){return data.get(58);}public int templateNotes(){return data.get(59);}
    public String researchText(int field){if(field<0||field>3)return "";var s=new StringBuilder();for(int i=0;i<TEXT_LIMIT;i++){int c=data.get(64+field*TEXT_LIMIT+i)&65535;if(c==0)break;s.append((char)c);}return s.toString();}
    private void text(int field,String value){for(int i=0;i<TEXT_LIMIT;i++)data.set(64+field*TEXT_LIMIT+i,i<value.length()?value.charAt(i):0);}
    private record Card(ResearchCurriculum.Template template,int section){}
    private java.util.List<Card> cards(net.minecraft.server.level.ServerPlayer p){
        if(curriculum==null)curriculum=ResearchCurriculum.load(p);var cards=new java.util.ArrayList<Card>();
        for(var t:curriculum.available(p))for(int i=0;i<4+t.concepts().size();i++)cards.add(new Card(t,i));return cards;
    }
    private Card selected(net.minecraft.server.level.ServerPlayer p){var cards=cards(p);return cards.isEmpty()?null:cards.get(Math.floorMod(selection,cards.size()));}
    private void researchRefresh(){
        if(!(viewer instanceof net.minecraft.server.level.ServerPlayer p))return;
        var practice=ResearchPractice.records(p);data.set(57,practice.size());data.set(58,ResearchPractice.diverse(p));data.set(59,FormulaKnowledge.understanding(p).getCompound("Templates").size());data.set(55,0);data.set(56,0);
        data.set(60,0);data.set(62,0);for(int i=0;i<4;i++)text(i,"");if(!FormulaKnowledge.unlocked(p)){text(0,"需要学习炼丹入门");return;}
        ResearchWorkspace.Panel panel=null;
        if(page()==1||page()==2||page()==4||page()==5){panel=workspace(p).view(page(),selection,observedServer(),specimen());data.set(54,panel.primary());data.set(61,panel.secondary());data.set(62,panel.flags());
            if(page()!=1){text(0,panel.main());text(1,panel.known());text(2,panel.pending());text(3,panel.detail());return;}}
        if(page()==1){
            if(practice.isEmpty()){text(0,"尚无已结算V5批次\n在丹炉进行真实炼制\n失败与事故也会记录");return;}
            var n=practice.getCompound(Math.floorMod(selection,practice.size()));
            text(0,"实践 "+(Math.floorMod(selection,practice.size())+1)+" / "+practice.size()+"\n结果："+ResearchPractice.outcome(n)+"\n逻辑步："+n.getLong("Steps")+"\n耗灵："+n.getInt("Fuel")+"\n结构损伤："+n.getInt("Damage"));
            text(1,"实践："+practice.size()+" / 128\n差异摘要："+diversePractice());text(2,"含失败，不代表认证\n阅读不增加控火经验");text(3,"批次："+n.getString("Batch").substring(0,8)+"\n前128批展示记录；不作为独立认证样本。\n规则摘要："+n.getString("Rules").substring(0,8));
            if(panel!=null){if(!panel.main().isBlank())text(0,panel.main());text(1,panel.known());text(2,panel.pending());text(3,panel.detail());}return;
        }
        if(page()!=3)return;var card=selected(p);
        if(card==null){text(0,"尚无对应教学丹方\n先学习金疮、聚灵\n或清心丹方传承");text(2,"其它丹方保留旧知识\n全量映射归第12项");return;}
        var t=card.template();boolean complete=t.concepts().stream().allMatch(id->FormulaKnowledge.understands(p,id));
        boolean saved=FormulaKnowledge.understanding(p).getCompound("Templates").contains(t.revision());
        data.set(60,saved?1:0);
        int learned=(int)t.concepts().stream().filter(id->FormulaKnowledge.understands(p,id)).count();
        boolean eligible=curriculum.concepts.stream().anyMatch(n->t.concepts().contains(n.id())&&!FormulaKnowledge.understands(p,n.id())&&n.requires().stream().allMatch(id->FormulaKnowledge.understands(p,id))&&practice.size()>=n.practice());
        data.set(55,eligible?1:0);data.set(56,complete&&!saved&&templateNotes()<32?1:0);
        String body=switch(card.section()){
            case 0->t.title()+"\n理解："+learned+" / "+t.concepts().size()+"\n操作与机理分别学习\n当前模板尚未认证\n不指定必成品级";
            case 1->!FormulaKnowledge.understands(p,"observation")?"硬约束：尚未理解\n先研习辨药与证据":"硬约束：产物目标\n"+PillSnapshot.moduleName(AbilityCompilerKind(t.module()))+"\n"+t.minimumDescription()+"\n"+(t.toxicity()==1000000?"毒性：未额外限制":"毒性上限："+t.toxicity()+"ppm")+"\n仅筛选药效目标";
            case 2,3->!FormulaKnowledge.understands(p,"compatibility")?"建议条件：尚未理解\n先研习配伍概念":String.join("\n",t.advice().subList(Math.min(t.advice().size(),(card.section()-2)*3),Math.min(t.advice().size(),(card.section()-1)*3)));
            default->{String id=t.concepts().get(card.section()-4);var node=curriculum.concepts.stream().filter(c->c.id().equals(id)).findFirst().orElseThrow();boolean understood=FormulaKnowledge.understands(p,id);
                yield node.title()+"\n"+(understood?node.explanation():"尚未理解\n需先理解前置概念\n实践批次要求："+node.practice());}
        };
        String match=!complete?"需先理解约束":!analyzed()?"需先鉴定样本":t.qualifies(cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.view(specimen()))?"样本符合目标":"样本不符合目标";
        text(0,body);text(1,"概念："+learned+" / "+t.concepts().size()+"\n笔记："+(saved?"已保存":"未保存")+"\n"+match);text(2,"年份/比例尚未验证\n建议不会增加药力");
        text(3,"传承："+t.title()+"\n版本："+t.revision().substring(0,8)+"\n未验证教学模板；不是已认证生产丹方。");
    }
    private static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.Kind AbilityCompilerKind(String id){return cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.Kind.valueOf(id);}
    private void refresh(){if(viewer.level().isClientSide)return;if(!ItemStack.matches(inspected,specimen())){data.set(1,0);inspected=ItemStack.EMPTY;if(workspace!=null)workspace.clearTarget();}var notes=FormulaKnowledge.researchRecords(viewer);data.set(2,notes.size());String last=notes.isEmpty()?"":notes.getCompound(notes.size()-1).getString("Summary");data.set(5,Math.min(48,last.length()));for(int i=0;i<48;i++)data.set(6+i,i<last.length()?last.charAt(i):0);data.set(3,(int)java.util.Arrays.stream(PillCatalog.values()).filter(p->FormulaKnowledge.knows(viewer,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu",p.id))).count());data.set(4,FormulaKnowledge.unlocked(viewer)?1:0);}
    @Override public void broadcastChanges(){refresh();researchRefresh();super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return p.level().isClientSide||p.isAlive()&&!p.isSpectator()&&p.level().getBlockEntity(pos)==table&&table.stillValid(p);}
    @Override public boolean clickMenuButton(Player p,int button){if(p.level().isClientSide||!stillValid(p)||p!=viewer)return false;
        if(button>=0&&button<6){data.set(0,button);if(workspace!=null)workspace.navigate();broadcastChanges();return true;}
        if((page()>=1&&page()<=5)&&(button==20||button==21)){selection+=button==20?-1:1;if(workspace!=null)workspace.navigate();broadcastChanges();return true;}
        if(p instanceof net.minecraft.server.level.ServerPlayer researcher&&FormulaKnowledge.unlocked(researcher)&&(page()==1||page()==2||page()==4||page()==5)){
            var w=workspace(researcher);var panel=w.view(page(),selection,observedServer(),specimen());int bit=button==panel.primary()?1:button==panel.secondary()?2:0;
            if(bit==0||(panel.flags()&bit)==0||button==38&&!supplies())return false;
            boolean done=w.action(page(),selection,button,observedServer(),specimen(),supplies());
            if(done&&button==38){table.removeItem(1,1);table.removeItem(2,1);table.setChanged();}if(done&&button==33)selection=0;broadcastChanges();return done;
        }
        if(p instanceof net.minecraft.server.level.ServerPlayer student&&FormulaKnowledge.unlocked(student)&&page()==3){
            var card=selected(student);if(card==null)return false;
            if(button==22){boolean changed=FormulaKnowledge.study(student,curriculum,card.template())>0;broadcastChanges();return changed;}
            if(button==23&&supplies()&&FormulaKnowledge.saveTemplate(student,curriculum,card.template())){
                table.removeItem(1,1);table.removeItem(2,1);table.setChanged();broadcastChanges();return true;
            }return false;
        }
        if(!(p instanceof net.minecraft.server.level.ServerPlayer player)||!FormulaKnowledge.unlocked(player)||page()!=0)return false;
        if(button==10){if(!ResearchView.authenticate(player,specimen()))return false;inspected=specimen().copy();data.set(1,FormulaKnowledge.researchRecords(p).stream().anyMatch(t->((net.minecraft.nbt.CompoundTag)t).getString("Fingerprint").equals(ResearchView.fingerprint(specimen())))?2:1);broadcastChanges();return true;}
        if(button==11&&!recorded()&&analyzed()&&ItemStack.matches(inspected,specimen())&&supplies()&&ResearchView.authenticate(player,specimen())){
            if(!FormulaKnowledge.recordResearch(player,ResearchView.fingerprint(specimen()),ResearchView.effect(specimen())))return false;
            table.removeItem(1,1);table.removeItem(2,1);table.setChanged();data.set(1,2);broadcastChanges();return true;
        }return false;
    }
    @Override public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(i<3){if(!moveItemStackTo(stack,3,39,true))return ItemStack.EMPTY;}
        else{int target=ResearchView.sample(stack)?0:stack.is(Items.PAPER)?1:stack.is(Items.INK_SAC)?2:-1;if(target<0||!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);broadcastChanges();return before;
    }
}
