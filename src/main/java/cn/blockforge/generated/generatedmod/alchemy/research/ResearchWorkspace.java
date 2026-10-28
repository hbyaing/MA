package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** A real menu session: private comparison baseline, finite solver state and selected parent references. */
public final class ResearchWorkspace {
    public record Panel(String main,String known,String pending,String detail,int primary,int secondary,int flags){}
    private final ServerPlayer player;private final ResearchMethods methods;private final String currentContext;
    private ResearchEvidence.History history;private int historySize=-1;private UUID baseline;
    private String comparison="",message="";private ResearchInference session;
    private final List<String> parents=new ArrayList<>();
    public ResearchWorkspace(ServerPlayer p){player=p;methods=new ResearchMethods(p);currentContext=ResearchEvidence.currentContext(p);}
    public void clearTarget(){session=null;message="";}
    public void navigate(){comparison="";message="";}
    private ResearchEvidence.History history(){int n=ResearchPractice.records(player).size();if(history==null||historySize!=n){history=ResearchEvidence.history(player,methods.known(player));historySize=n;}return history;}
    private ResearchEvidence.Point current(int selection){var rows=ResearchPractice.records(player);if(rows.isEmpty())return null;String id=rows.getCompound(Math.floorMod(selection,rows.size())).getString("Batch");return history().points().stream().filter(p->p.batch().toString().equals(id)).findFirst().orElse(null);}
    private ResearchInference.Candidate candidate(int selection){return session==null?null:session.candidate(Math.floorDiv(selection,5));}
    private CompoundTag revision(int selection){var rows=ResearchLineage.revisions(player);return rows.isEmpty()?new CompoundTag():rows.getCompound(Math.floorMod(Math.floorDiv(selection,4),rows.size()));}
    private boolean alreadySaved(ResearchInference.Candidate c){for(var t:ResearchLineage.revisions(player)){var n=(CompoundTag)t;if(n.getCompound("Plan").equals(c.plan())&&n.getCompound("Scope").equals(c.scope())&&n.getCompound("Goal").equals(session.target)){
            var saved=n.getList("Parents",Tag.TAG_STRING).stream().map(Tag::getAsString).toList();if(parents.isEmpty()||saved.equals(parents))return true;
        }}return false;}
    public Panel view(int page,int selection,boolean analyzed,ItemStack specimen){
        if(page==5){if(!ResearchMethods.qualified(player))return new Panel("先完成配伍、火候与研究前置","尚未具备认证知识","不阻止合法炼制","",50,51,0);var panel=CertificationPanel.view(player,selection);return message.isBlank()?panel:new Panel(message,panel.known(),panel.pending(),panel.detail(),panel.primary(),panel.secondary(),panel.flags());}
        if(!ResearchMethods.qualified(player))return new Panel(page==1?"":"先理解配伍与火候\n完成真实炼制并研习传承","研究前置尚未满足","不影响既有合法炼制","",page==2?32:page==1?30:40,page==2?38:page==1?31:41,0);
        if(page==1){var point=current(selection);String known=baseline==null?"尚未选择对照基准":"基准："+baseline.toString().substring(0,8);return new Panel(comparison,known,"严格检查已测条件\n两批不证明因果",point==null?"旧记录或缺失数据，不能补造开炉环境。":"本人已测投料、开炉环境和操作指令对照；不比较随机种子。",30,31,methods.learned(player)&&point!=null?1+(baseline!=null?2:0):0);}
        if(page==2){
            if(!methods.learned(player))return new Panel("研习公开研究方法\n仅学习已登记基础模型\n不直接读取目标原方","方法尚未研习","不增加控火或药力","更新的方法需重新研习；旧记录不会默改。",32,38,1);
            var c=candidate(selection);int section=Math.floorMod(selection,5),primary=c==null?33:switch(section){case 1->34;case 2->35;case 3->36;case 4->37;default->33;};
            boolean ready=analyzed&&PillSnapshot.view(specimen)!=null;int flags=ready?1:0;
            if(c==null)return new Panel(message.isBlank()?"鉴定真实V5样本后反推\n从本人已知路线筛选\n无已知路线时保留未知":message,"已测记录："+history().points().size(),"不排除未知材料路线","样本只提供公开模块/强度/时长目标，不读取其原始配方。",primary,38,flags);
            String body=switch(section){
                case 0->"候选 "+(Math.floorMod(Math.floorDiv(selection,5),session.candidates.size())+1)+" / "+session.candidates.size()+"\n来源：本人已测路线\n"+ResearchDifficulty.researchText(session.profile);
                case 1->"药龄假设，可减20%\n"+ResearchInference.editingText(c,true);
                case 2->"用量假设，可减一件\n"+ResearchInference.editingText(c,false);
                case 3->"声明药龄范围可扩展\n上下各20%，仅为假设\n已测联合点不会扩张\n"+scopeText(c.scope());
                default->ResearchDifficulty.operationText(c.source.operation());
            };
            var scope=c.scope();var covered=history().points().stream().filter(p->p.context().equals(c.source.context())&&ResearchInference.contains(scope,p.plan())).toList();
            int good=(int)covered.stream().filter(p->ResearchInference.matches(session.target,p.result())).count();
            String known="域内支持："+good+"\n不符/失败："+(covered.size()-good)+"\n父源："+parents.size();
            String detail=ResearchDifficulty.researchText(session.profile)+"\n"+ResearchDifficulty.operationText(c.source.operation())+"\n仅公开研究模型；被改动的条件尚未获得证据，旧成功不自动继承。\n"+ResearchInference.planText(c.plan());
            if(ready&&!alreadySaved(c)&&ResearchLineage.revisions(player).size()<32)flags|=2;
            if(primary==34&&c.editKey(true)==null||primary==35&&c.editKey(false)==null)flags&=~1;
            String pending=c.source.context().equals(currentContext)?"均为待验证假设\n不继承祖方的药效":"规则变化，需复验\n保留原版本证据";
            return new Panel(message.isBlank()?body:message,known,pending,detail,primary,38,flags);
        }
        var node=revision(selection);if(node.isEmpty())return new Panel("尚无研究版本\n先在反推页保存候选","版本上限：32","先声明，再实际复验","",40,41,0);
        int section=Math.floorMod(selection,4);String body=switch(section){
            case 0->"版本："+node.getString("Id").substring(0,8)+"\n祖方："+node.getString("Root").substring(0,8)+"\n直接父源："+node.getList("Parents",Tag.TAG_STRING).size()+"\n"+(node.getString("Context").equals(currentContext)?ResearchLineage.status(node):"规则已变化，须复验");
            case 1->"药龄净偏移："+ResearchLineage.total(node.getCompound("Net"),"age/")+"年\n累计路径："+ResearchLineage.total(node.getCompound("Path"),"age/")+"年\n用量净偏移："+ResearchLineage.total(node.getCompound("Net"),"count/")+"件\n累计用量变化："+ResearchLineage.total(node.getCompound("Path"),"count/")+"件\n目标改动："+node.getInt("GoalChanges")+"\n"+(node.getBoolean("RuleDrift")?"规则变化，需另行复验":"规则版本独立记录");
            case 2->"已测联合点："+node.getCompound("JointPoints").size()+"\n支持批次："+node.getInt("Support")+"\n不符/失败："+node.getInt("Failures")+"\n声明范围仅是假设\n不生成连续有效区域";
            default->"保留全部父源与祖方\n原始基准不可重写\n旧批次仅引用一次\n越出证据域需新实验\n无任何药力衰减惩罚";
        };
        String detail="根源："+node.getString("Root")+"\n父源："+node.getList("Parents",Tag.TAG_STRING)+"\n观测支持只对应保存的联合点；独立验证以认证页为准。\n离散变更路径："+node.getCompound("Path").getCompound("Choices");
        return new Panel(message.isBlank()?body:message,"所选父源："+parents.size()+"\n认证：见认证页","改方须有自身证据\n不按品级推算难度",detail,40,41,methods.learned(player)?3:0);
    }
    public boolean action(int page,int selection,int id,boolean analyzed,ItemStack specimen,boolean supplies){
        if(!ResearchMethods.qualified(player))return false;
        if(page==5){if(id==53&&Math.floorMod(selection,5)==4){message=AlchemyCertification.promote(player);return true;}boolean done=CertificationPanel.action(player,selection,id);if(id==50&&!done){message="冻结条件尚未满足\n检查知识与本人对照\n同条件协议不能重置";return true;}return done;}
        if(page==2&&id==32){boolean changed=methods.study(player);if(changed){history=null;message="已研习公开研究法\n现在可鉴定样本并反推";}return changed;}
        if(!methods.learned(player))return false;
        if(page==1&&(id==30||id==31)){
            var point=current(selection);if(point==null)return false;if(id==30){baseline=point.batch();comparison="已选为对照基准\n批次："+baseline.toString().substring(0,8);return true;}
            var a=history().points().stream().filter(p->p.batch().equals(baseline)).findFirst().orElse(null);if(a==null)return false;comparison=ResearchEvidence.compare(a,point)+"\n基准："+a.result().getInt("Grade")+"品\n当前："+point.result().getInt("Grade")+"品\n未宣称统计独立性";return true;
        }
        if(page==4&&(id==40||id==41)){
            var node=revision(selection);if(node.isEmpty())return false;String revision=node.getString("Id");
            if(id==40){parents.clear();parents.add(revision);message="已选父方\n下一次保存将保留其来源";return true;}
            if(parents.contains(revision)||parents.size()>=2)return false;parents.add(revision);message="已加入第二父源\n合并版本保留两个来源";return true;
        }
        if(page!=2||!analyzed||!ResearchView.authenticate(player,specimen)||PillSnapshot.view(specimen)==null)return false;
        if(id==33){history=null;session=new ResearchInference(ResearchInference.goal(PillSnapshot.view(specimen)),history());message=session.candidates.isEmpty()?"已知范围内暂无可行候选\n不读取目标原始配方\n请补充真实实验":"";return true;}
        var c=candidate(selection);if(c==null)return false;message="";
        if(id==34)return c.edit(true);if(id==35)return c.edit(false);if(id==36)return c.broaden();if(id==37){c.reset();return true;}
        if(id==38&&supplies&&!alreadySaved(c)){
            var node=ResearchLineage.save(player,session,c,parents,methods);if(node.isEmpty())return false;message="已保存候选版本\n"+ResearchLineage.status(node)+"\n去谱系页查看净/累计改动";return true;
        }return false;
    }
    public static String actionLabel(int id){return switch(id){case 30->"选为对照";case 31->"比较两批";case 32->"研习研究法";case 33->"反推候选";case 34->"药龄减20%";case 35->"用量减一件";case 36->"扩展声明";case 37->"恢复原方案";case 38->"保存候选";case 40->"设为父方";case 41->"加入父源";case 50->"冻结后续验证";case 51->"提交验证";case 53->"申请晋阶";default->"研究";};}
    private static String scopeText(CompoundTag scope){var s=new StringBuilder();for(String k:new TreeSet<>(scope.getCompound("Minimum").getAllKeys()))if(k.startsWith("age/"))s.append("药龄 ").append(scope.getCompound("Minimum").getLong(k)).append(" 至 ").append(scope.getCompound("Maximum").getLong(k)).append("年\n");return s.toString();}
}
