package cn.blockforge.generated.generatedmod.alchemy.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.*;
import java.util.*;

/** Approved six-page UI, using the existing card and action skins. */
public final class CertificationPanel {
    private static final String[] TYPES={"本人反推验证","恢复基础认证","回灵基础认证","清净基础认证"};
    private static CompoundTag node(ServerPlayer p,int selection){var nodes=ResearchLineage.revisions(p);return nodes.isEmpty()?new CompoundTag():nodes.getCompound(Math.floorMod(Math.floorDiv(selection,5),nodes.size()));}
    public static ResearchWorkspace.Panel view(ServerPlayer p,int selection){
        int kind=Math.floorMod(selection,5),grade=Math.min(9,AlchemyCertification.certifiedRank(p)+1);var node=node(p,selection);
        if(!new ResearchMethods(p).learned(p))return new ResearchWorkspace.Panel("先在反推页研习研究法","尚未研习公开研究模型","知识不直接改变药力","",50,51,0);
        if(kind==4)return new ResearchWorkspace.Panel("正式认证："+AlchemyCertification.certifiedRank(p)+"品\n下一目标："+grade+"品\n一至三：恢复基础\n四至六：另需回灵\n七至九：清净与反推","旧制心得记录保留","偶然出丹不授予品阶","晋阶还需真实知识和既有境界；只接受冻结后正式生存批次的独立认证。",53,51,1);
        if(node.isEmpty())return new ResearchWorkspace.Panel("先保存本人候选版本\n再冻结独立验证方案","尚无可冻结版本","验证不改写物理结果","",50,51,0);
        var protocol=AlchemyCertification.find(p,node.getString("Id"),kind,grade);boolean active=AlchemyCertification.protocols(p).stream().anyMatch(t->((CompoundTag)t).getString("State").equals("COLLECTING"));
        if(protocol.isEmpty())return new ResearchWorkspace.Panel(TYPES[kind]+"\n目标：至少"+grade+"品\n冻结后前"+AlchemyCertification.policy(p).getInt("Count")+"批全计入\n生存炼制，全新原料批次\n允许真实天象变化\n材料设备工序范围锁定",p.isCreative()?"切换生存后可冻结":"点击前查看冻结说明","成败均计入\n旧实验不作留出样本","所选版本："+node.getString("Id").substring(0,8)+"\n"+ResearchInference.planText(node.getCompound("Plan"))+"冻结后不可调门槛或丢弃失败。同一条件复制版本不能重置验证。采用配置冻结的95% Wilson下界≥80%、样本数及材料/天象覆盖门槛。同一原料批次不能重复计独立成功。范围声明不是连续有效域证明。",50,51,!active&&!p.isCreative()&&!p.isSpectator()&&grade<=9?1:0);
        var s=AlchemyCertification.statistics(protocol);String state=switch(protocol.getString("State")){case "CERTIFIED"->"已通过独立验证";case "FAILED"->"最终未通过，保留全部记录";default->"正在采集，门槛已冻结";};
        String body=TYPES[kind]+"\n"+state+"\n成功 "+s.getInt("Pass")+" / 失败 "+s.getInt("Fail")+"\n待完成："+s.getInt("Pending")+"\n下界："+String.format(java.util.Locale.ROOT,"%.1f",s.getDouble("Lower")*100)+"%\n新原料批次："+s.getInt("Lots")+"\n天象组合："+s.getInt("Environments");
        int count=protocol.getList("Trials",Tag.TAG_COMPOUND).size();boolean submit=protocol.getString("State").equals("COLLECTING")&&count==protocol.getCompound("Policy").getInt("Count");
        return new ResearchWorkspace.Panel(body,"已绑定："+count+" / "+protocol.getCompound("Policy").getInt("Count"),"复制/改名不重置\n不筛除事故与中止","版本："+node.getString("Id").substring(0,8)+"\n"+AlchemyCertification.report(protocol)+"\n覆盖门槛：新原料 "+protocol.getCompound("Policy").getInt("MinimumLots")+" / 成功天象 "+protocol.getCompound("Policy").getInt("MinimumEnvironments")+"。规则、目标和样本数已冻结；验证结论仅对应实际留出联合条件，不覆盖整个连续声明域。",50,51,submit?2:0);
    }
    public static boolean action(ServerPlayer p,int selection,int action){var n=node(p,selection);int kind=Math.floorMod(selection,5);if(action==53&&kind==4){AlchemyCertification.promote(p);return true;}if(n.isEmpty()||kind==4)return false;if(action==50)return AlchemyCertification.freeze(p,n.getString("Id"),kind);if(action==51){var protocol=AlchemyCertification.find(p,n.getString("Id"),kind,Math.min(9,AlchemyCertification.certifiedRank(p)+1));return !protocol.isEmpty()&&AlchemyCertification.issue(p,protocol.getUUID("Id"));}return false;}
    private CertificationPanel(){}
}
