package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Server-owned teaching content. Descriptive constraints never enter the reaction engine. */
public final class ResearchCurriculum {
    public record Concept(String id,String title,List<String> requires,int practice,String explanation,String hash) {}
    public record Template(String id,String title,String formula,String module,long minimum,int toxicity,
                           List<String> concepts,List<String> advice,String revision) {
        public boolean qualifies(AbilityCompiler.Result result) {
            return result!=null&&result.grade()>0&&result.toxicity()<=toxicity&&result.effects().stream()
                    .anyMatch(e->e.id().name().equals(module)&&e.magnitudeMilli()>=minimum);
        }
        public String minimumDescription(){return switch(module){case "heal"->"最低治疗："+minimum/1000D+"点";case "mana"->"最低回灵："+minimum/1000D+"点";case "cleanse"->"最低净化："+minimum/1000+"次";default->"最低药力："+minimum/1000D;};}
    }
    public final List<Concept> concepts;
    public final List<Template> templates;
    public final String hash;
    public ResearchCurriculum(String json) {
        if(json.length()>65536)throw new IllegalArgumentException("Curriculum size budget");
        var root=cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRuleLoader.document(json);keys(root,"schema","concepts","templates");
        if(number(root,"schema")!=1)throw new IllegalArgumentException("Unknown curriculum");
        var nodes=new ArrayList<Concept>();var seen=new HashSet<String>();
        for(var entry:root.getAsJsonArray("concepts")) {
            var n=entry.getAsJsonObject();keys(n,"id","title","requires","practice","explanation");String id=id(n.get("id").getAsString());
            if(!seen.add(id)||nodes.size()>=16)throw new IllegalArgumentException("Duplicate/concept budget");
            var requires=strings(n,"requires",8);if(!seen.containsAll(requires)||requires.contains(id))throw new IllegalArgumentException("Cyclic/unknown concept prerequisite");
            int practice=Math.toIntExact(number(n,"practice"));if(practice<0||practice>128)throw new IllegalArgumentException("Practice prerequisite");
            nodes.add(new Concept(id,text(n,"title",24),requires,practice,text(n,"explanation",128),AbilityCompiler.hash(n.toString())));
        }
        var forms=new ArrayList<Template>();var ids=new HashSet<String>();
        for(var entry:root.getAsJsonArray("templates")) {
            var t=entry.getAsJsonObject();keys(t,"id","title","formula","module","minimumMilli","maximumToxicityPpm","concepts","advice");
            String id=id(t.get("id").getAsString()),formula=t.get("formula").getAsString(),module=t.get("module").getAsString();
            if(!ids.add(id)||forms.size()>=32||!FormulaKnowledge.valid(ResourceLocation.tryParse(formula)))throw new IllegalArgumentException("Unknown/duplicate formula");
            var kind=AbilityCompiler.Kind.valueOf(module);long min=number(t,"minimumMilli");int toxicity=Math.toIntExact(number(t,"maximumToxicityPpm"));
            if(min<AbilityCompiler.minimum(kind)||min>AbilityCompiler.maximum(kind)||toxicity<0||toxicity>1000000)throw new IllegalArgumentException("Invalid constraint");
            var prerequisites=strings(t,"concepts",16);if(prerequisites.isEmpty()||!seen.containsAll(prerequisites))throw new IllegalArgumentException("Unknown concept");
            forms.add(new Template(id,text(t,"title",24),formula,module,min,toxicity,prerequisites,strings(t,"advice",8),AbilityCompiler.hash(t.toString())));
        }
        if(nodes.isEmpty()||forms.isEmpty())throw new IllegalArgumentException("Empty curriculum");concepts=List.copyOf(nodes);templates=List.copyOf(forms);hash=AbilityCompiler.hash(root.toString());
    }
    public List<Template> available(ServerPlayer p){return templates.stream().filter(t->FormulaKnowledge.knows(p,ResourceLocation.tryParse(t.formula()))).toList();}
    public static ResearchCurriculum load(ServerPlayer p){
        try(var r=p.server.getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath("wuxingxiantu","alchemy/research.json"))){
            var s=new StringBuilder();char[] chars=new char[2048];int n;while((n=r.read(chars))!=-1){s.append(chars,0,n);if(s.length()>65536)throw new IllegalArgumentException("Curriculum budget");}return new ResearchCurriculum(s.toString());
        }catch(java.io.IOException e){throw new IllegalStateException("Missing research curriculum",e);}
    }
    private static long number(JsonObject n,String key){var v=n.get(key);if(!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("Expected integer");return v.getAsBigDecimal().longValueExact();}
    private static List<String> strings(JsonObject n,String key,int max){var result=new ArrayList<String>();for(var v:n.getAsJsonArray(key)){if(!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isString())throw new IllegalArgumentException("Expected string");String s=v.getAsString();if(s.isBlank()||s.length()>128||result.contains(s)||result.size()>=max)throw new IllegalArgumentException("Invalid list");result.add(s);}return List.copyOf(result);}
    private static String id(String s){if(!s.matches("[a-z][a-z0-9_]{0,47}"))throw new IllegalArgumentException("Invalid research id");return s;}
    private static String text(JsonObject n,String key,int max){String s=n.get(key).getAsString();if(s.isBlank()||s.length()>max||s.indexOf('\n')>=0)throw new IllegalArgumentException("Invalid research text");return s;}
    private static void keys(JsonObject n,String... keys){if(!n.keySet().equals(Set.of(keys)))throw new IllegalArgumentException("Unknown research fields");}
}
