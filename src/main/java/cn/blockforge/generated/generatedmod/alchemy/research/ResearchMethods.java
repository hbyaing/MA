package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge;
import cn.blockforge.generated.generatedmod.alchemy.batch.V5Batch;
import cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Explicitly taught public model, not permission to inspect every private reaction. */
public final class ResearchMethods {
    private final CompoundTag definitions;
    public final String hash;
    public ResearchMethods(ServerPlayer p){
        try(var reader=p.server.getResourceManager().openAsReader(ResourceLocation.parse("wuxingxiantu:alchemy/research-methods.json"))){
            var s=new StringBuilder();char[] b=new char[2048];int n;while((n=reader.read(b))!=-1){s.append(b,0,n);if(s.length()>16384)throw new IllegalArgumentException("Method budget");}
            var root=SimulationRuleLoader.document(s.toString());if(!root.keySet().equals(Set.of("schema","publicRules"))||root.get("schema").getAsBigDecimal().intValueExact()!=1)throw new IllegalArgumentException("Method schema");
            var ids=new HashSet<String>();for(var e:root.getAsJsonArray("publicRules"))if(!ids.add(e.getAsString())||ids.size()>64)throw new IllegalArgumentException("Duplicate/method budget");
            var rules=V5Batch.profile(V5Batch.currentProfile(p.serverLevel()),1).rules();definitions=new CompoundTag();for(var r:rules.reactions)if(ids.remove(r.id()))definitions.putString(r.id(),AbilityCompiler.hash(r.toString()));
            if(!ids.isEmpty()||definitions.isEmpty())throw new IllegalArgumentException("Unknown public process");hash=ResearchEvidence.hash(definitions);
        }catch(java.io.IOException e){throw new IllegalStateException("Missing public research methods",e);}
    }
    public static boolean qualified(ServerPlayer p){return FormulaKnowledge.unlocked(p)&&FormulaKnowledge.understands(p,"compatibility")&&FormulaKnowledge.understands(p,"thermal");}
    public boolean learned(ServerPlayer p){return qualified(p)&&FormulaKnowledge.understanding(p).getString("ResearchMethodHash").equals(hash);}
    public boolean study(ServerPlayer p){return qualified(p)&&!learned(p)&&FormulaKnowledge.studyResearchMethods(p,hash,definitions);}
    public Set<String> known(ServerPlayer p){return learned(p)?Set.copyOf(definitions.getAllKeys()):Set.of();}
    private ResearchMethods(){throw new UnsupportedOperationException();}
}
