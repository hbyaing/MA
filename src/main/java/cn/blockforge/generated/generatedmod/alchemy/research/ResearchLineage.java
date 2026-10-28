package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Immutable owner-local revision DAG in the existing world-owned profession account. */
public final class ResearchLineage {
    public static ListTag revisions(ServerPlayer p){return PlayerCrafts.get(p.server).profession(p.getUUID(),"alchemy").getList("ResearchRevisions",Tag.TAG_COMPOUND).copy();}
    public static CompoundTag find(ListTag list,String id){for(var t:list){var n=(CompoundTag)t;if(n.getString("Id").equals(id))return n.copy();}return new CompoundTag();}
    private static ListTag strings(Collection<String> values){var n=new ListTag();values.forEach(s->n.add(StringTag.valueOf(s)));return n;}
    public static CompoundTag offset(CompoundTag a,CompoundTag b){
        var n=new CompoundTag();var nums=new CompoundTag();var choices=new CompoundTag();var keys=new TreeSet<>(a.getCompound("Numbers").getAllKeys());keys.addAll(b.getCompound("Numbers").getAllKeys());
        for(String k:keys){long v=Math.abs(Math.subtractExact(a.getCompound("Numbers").getLong(k),b.getCompound("Numbers").getLong(k)));if(v>0)nums.putLong(k,v);}
        var ids=new TreeSet<>(a.getCompound("Choices").getAllKeys());ids.addAll(b.getCompound("Choices").getAllKeys());for(String k:ids)if(!Objects.equals(a.getCompound("Choices").get(k),b.getCompound("Choices").get(k)))choices.putLong(k,1);
        n.put("Numbers",nums);n.put("Choices",choices);return n;
    }
    public static CompoundTag accumulate(CompoundTag prior,CompoundTag step){var n=prior.copy();for(String group:List.of("Numbers","Choices")){var total=n.getCompound(group);var increment=step.getCompound(group);for(String k:increment.getAllKeys())total.putLong(k,Math.addExact(total.getLong(k),increment.getLong(k)));n.put(group,total);}return n;}
    private static void maximum(CompoundTag into,CompoundTag value){for(String group:List.of("Numbers","Choices")){var n=into.getCompound(group);var v=value.getCompound(group);for(String k:v.getAllKeys())n.putLong(k,Math.max(n.getLong(k),v.getLong(k)));into.put(group,n);}}
    public static CompoundTag save(ServerPlayer p,ResearchInference session,ResearchInference.Candidate candidate,List<String> selectedParents,ResearchMethods methods){
        if(!methods.learned(p)||session==null||candidate==null||!session.candidates.contains(candidate))return new CompoundTag();
        var current=ResearchEvidence.history(p,methods.known(p));var source=current.points().stream().filter(e->e.batch().equals(candidate.source.batch())).findFirst().orElse(null);
        if(source==null||!source.plan().equals(candidate.source.plan())||!source.context().equals(candidate.source.context()))return new CompoundTag();
        var plan=candidate.plan();ResearchEvidence.validatePlan(plan);var original=source.plan();
        if(!plan.getCompound("Choices").equals(original.getCompound("Choices"))||!plan.getCompound("Numbers").getAllKeys().equals(original.getCompound("Numbers").getAllKeys()))return new CompoundTag();
        for(String key:plan.getCompound("Numbers").getAllKeys())if(plan.getCompound("Numbers").getLong(key)!=original.getCompound("Numbers").getLong(key)&&!key.startsWith("age/")&&!key.startsWith("count/"))return new CompoundTag();
        var all=revisions(p);if(all.size()>=32)return new CompoundTag();var parents=new ArrayList<>(new LinkedHashSet<>(selectedParents));if(parents.size()>2)return new CompoundTag();
        if(parents.isEmpty())for(var t:all){var n=(CompoundTag)t;if(n.getString("Context").equals(source.context())&&n.getCompound("Plan").equals(original)&&(!plan.equals(original)||!n.getCompound("Goal").equals(session.target))){parents.add(n.getString("Id"));break;}}
        var parentDocs=new ArrayList<CompoundTag>();for(String id:parents){var n=find(all,id);if(n.isEmpty())return new CompoundTag();parentDocs.add(n);}
        var identity=new CompoundTag();identity.putUUID("Author",p.getUUID());identity.put("Plan",plan);identity.put("Scope",candidate.scope());identity.put("Goal",session.target.copy());identity.putString("Context",source.context());identity.put("Parents",strings(parents));String id=ResearchEvidence.hash(identity);
        if(!find(all,id).isEmpty())return new CompoundTag();var node=identity.copy();node.putString("Id",id);node.putString("Engine",SimulationRules.ENGINE);node.putString("SourceBatch",source.batch().toString());node.putString("Rules",source.rules());
        var roots=new LinkedHashSet<String>();for(var parent:parentDocs)for(var r:parent.getList("Roots",Tag.TAG_STRING))roots.add(r.getAsString());if(roots.isEmpty())roots.add(id);String root=roots.iterator().next();node.put("Roots",strings(roots));node.putString("Root",root);node.putString("Formula",root);
        var baseline=root.equals(id)?plan:find(all,root).getCompound("Plan");node.put("RootBaseline",baseline.copy());node.put("Net",offset(baseline,plan));var path=new CompoundTag();int goalChanges=0;boolean drift=false;
        for(var parent:parentDocs){maximum(path,accumulate(parent.getCompound("Path"),offset(parent.getCompound("Plan"),plan)));goalChanges=Math.max(goalChanges,parent.getInt("GoalChanges")+(parent.getCompound("Goal").equals(session.target)?0:1));drift|=!parent.getString("Context").equals(source.context());}
        node.put("Path",path);node.putInt("GoalChanges",goalChanges);node.putBoolean("RuleDrift",drift);
        var evidence=new ListTag();var points=new CompoundTag();int support=0,failures=0;
        for(var point:current.points())if(point.context().equals(source.context())&&ResearchInference.contains(candidate.scope(),point.plan())){
            boolean met=ResearchInference.matches(session.target,point.result());var e=new CompoundTag();String key=ResearchEvidence.hash(point.plan());e.putString("Batch",point.batch().toString());e.putString("Point",key);e.putBoolean("MeetsGoal",met);evidence.add(e);points.put(key,point.plan());if(met)support++;else failures++;
        }
        node.put("Evidence",evidence);node.put("JointPoints",points);node.putInt("Support",support);node.putInt("Failures",failures);node.putInt("Unavailable",current.unavailable());node.putString("Status",!source.context().equals(ResearchEvidence.currentContext(p))?"NEEDS_RETEST":support==0?"CANDIDATE":failures>0?"CONFLICTING":"OBSERVED_SUPPORT");
        node.putBoolean("Certified",false);node.putString("ScopeMeaning","DECLARATION_ONLY_NO_INTERPOLATION");all.add(node);if(ResearchEvidence.canonical(all).length()>1048576)return new CompoundTag();
        var store=PlayerCrafts.get(p.server);store.profession(p.getUUID(),"alchemy").put("ResearchRevisions",all);store.setDirty();return node.copy();
    }
    public static long total(CompoundTag offset,String prefix){long n=0;for(String k:offset.getCompound("Numbers").getAllKeys())if(k.startsWith(prefix))n=Math.addExact(n,offset.getCompound("Numbers").getLong(k));return n;}
    public static String status(CompoundTag node){return switch(node.getString("Status")){case "OBSERVED_SUPPORT"->"已有观测支持，未认证";case "CONFLICTING"->"存在失败或矛盾，需复验";case "NEEDS_RETEST"->"原规则证据，需复验";default->"候选，缺少自身证据";};}
    private ResearchLineage(){}
}
