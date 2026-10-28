package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import net.minecraft.nbt.*;
import java.util.*;

/** Finite empirical solver. Its entire input is authorized observations and a public effect target. */
public final class ResearchInference {
    public static CompoundTag goal(AbilityCompiler.Result result){
        if(result==null||result.grade()==0)throw new IllegalArgumentException("No measured effect target");var n=new CompoundTag();var list=new ListTag();
        result.effects().stream().sorted(Comparator.comparing(e->e.id().name())).forEach(e->{var t=new CompoundTag();t.putString("Module",e.id().name());t.putLong("Magnitude",e.magnitudeMilli());t.putInt("Duration",e.duration());t.putInt("Interval",e.interval());t.putInt("Cooldown",e.cooldown());t.putInt("Realm",e.realm());list.add(t);});n.put("Effects",list);n.putInt("MaximumToxicity",result.toxicity());return n;
    }
    public static boolean matches(CompoundTag target,CompoundTag result){
        var actual=PillSnapshot.read(result);if(actual.grade()==0||actual.toxicity()>target.getInt("MaximumToxicity"))return false;var goal=target.getList("Effects",Tag.TAG_COMPOUND);if(goal.isEmpty()||goal.size()>9)return false;
        return goal.stream().allMatch(t->{var n=(CompoundTag)t;return actual.effects().stream().anyMatch(e->e.id().name().equals(n.getString("Module"))&&e.magnitudeMilli()>=n.getLong("Magnitude")&&e.duration()>=n.getInt("Duration")&&e.interval()<=n.getInt("Interval")&&e.cooldown()<=n.getInt("Cooldown")&&e.realm()<=n.getInt("Realm"));});
    }
    public static final class Candidate {
        public final ResearchEvidence.Point source;public final List<ResearchEvidence.Point> measurements;
        private CompoundTag plan,scope;
        public Candidate(ResearchEvidence.Point source,List<ResearchEvidence.Point> measurements){this.source=source;this.measurements=List.copyOf(measurements);reset();}
        public CompoundTag plan(){return plan.copy();}public CompoundTag scope(){return scope.copy();}
        public void reset(){plan=source.plan();scope=exact(plan);}
        public String editKey(boolean age){var numbers=plan.getCompound("Numbers");return new TreeSet<>(numbers.getAllKeys()).stream().filter(k->k.startsWith(age?"age/":"count/")&&numbers.getLong(k)>(age?0:1)).findFirst().orElse(null);}
        public boolean edit(boolean age){
            var numbers=plan.getCompound("Numbers");var key=editKey(age);if(key==null)return false;
            long old=numbers.getLong(key);numbers.putLong(key,age?old-Math.max(1,old/5):old-1);scope=exact(plan);return true;
        }
        public boolean broaden(){var numbers=plan.getCompound("Numbers");var key=new TreeSet<>(numbers.getAllKeys()).stream().filter(k->k.startsWith("age/")&&numbers.getLong(k)>0).findFirst().orElse(null);if(key==null)return false;long v=numbers.getLong(key),delta=Math.max(1,v/5);scope.getCompound("Minimum").putLong(key,Math.max(0,v-delta));scope.getCompound("Maximum").putLong(key,Math.min(cn.blockforge.generated.generatedmod.plants.HerbAge.MAX,v+delta));return true;}
    }
    public static CompoundTag exact(CompoundTag plan){var n=new CompoundTag();n.put("Minimum",plan.getCompound("Numbers").copy());n.put("Maximum",plan.getCompound("Numbers").copy());n.put("Choices",plan.getCompound("Choices").copy());return n;}
    public static boolean contains(CompoundTag scope,CompoundTag plan){
        if(!scope.getCompound("Choices").equals(plan.getCompound("Choices")))return false;var values=plan.getCompound("Numbers");var min=scope.getCompound("Minimum");var max=scope.getCompound("Maximum");if(!values.getAllKeys().equals(min.getAllKeys())||!values.getAllKeys().equals(max.getAllKeys()))return false;
        for(String k:values.getAllKeys())if(values.getLong(k)<min.getLong(k)||values.getLong(k)>max.getLong(k))return false;return true;
    }
    public final CompoundTag target,profile;public final List<Candidate> candidates;public final int rejected;
    public ResearchInference(CompoundTag target,ResearchEvidence.History history){
        this.target=target.copy();var groups=new TreeMap<String,List<ResearchEvidence.Point>>();for(var p:history.points())groups.computeIfAbsent(p.context()+ResearchEvidence.hash(p.plan()),k->new ArrayList<>()).add(p);
        var candidates=new ArrayList<Candidate>();int rejected=0,conflicts=0,controls=0,overflow=0;
        for(var group:groups.values()){
            var good=group.stream().filter(p->matches(target,p.result())).toList();if(good.isEmpty()){rejected++;continue;}if(good.size()!=group.size())conflicts++;
            if(candidates.size()<32)candidates.add(new Candidate(good.get(0),group));else overflow++;
        }
        for(int i=0;i<history.points().size();i++)for(int j=i+1;j<history.points().size();j++){var a=history.points().get(i);var b=history.points().get(j);if(a.context().equals(b.context())&&ResearchEvidence.differences(a.plan(),b.plan()).size()==1)controls++;}
        this.rejected=rejected;this.candidates=List.copyOf(candidates);profile=ResearchDifficulty.inference(candidates.size(),conflicts,controls,history.unavailable()+overflow);profile.putInt("TruncatedRoutes",overflow);
    }
    public Candidate candidate(int index){return candidates.isEmpty()?null:candidates.get(Math.floorMod(index,candidates.size()));}
    public static String materialName(CompoundTag plan,String index){String identity=plan.getCompound("Choices").getString("material/"+index);int split=identity.lastIndexOf('/');var id=net.minecraft.resources.ResourceLocation.tryParse(split<0?identity:identity.substring(0,split));if(id==null)return "原料"+index;return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id).getDescription().getString();}
    public static String planText(CompoundTag plan){var s=new StringBuilder();var nums=plan.getCompound("Numbers");for(String k:new TreeSet<>(nums.getAllKeys()))if(k.startsWith("age/")){String i=k.substring(4);s.append(materialName(plan,i)).append("：").append(nums.getLong(k)).append("年 / ").append(nums.getLong("count/"+i)).append("件\n");}return s.toString();}
    public static String editingText(Candidate c,boolean age){String key=c.editKey(age);if(key==null)return "没有可降低的此类参数\n悬停查看完整原料";String i=key.substring(key.indexOf('/')+1);var p=c.plan();return materialName(p,i)+"\n药龄："+p.getCompound("Numbers").getLong("age/"+i)+"年\n用量："+p.getCompound("Numbers").getLong("count/"+i)+"件\n仅改此原料的"+(age?"药龄":"用量")+"\n悬停查看全部原料";}
}
