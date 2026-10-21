package cn.blockforge.generated.generatedmod.acquisition;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** Unknown realm/repeatability remain explicit; evidence is not proof that every input is reachable. */
public record SourceEvidence(String type,ResourceLocation sourceId,List<String> conditions,
        int minimumRealm,Boolean repeatable,List<String> inputs) {
    public SourceEvidence { conditions=List.copyOf(conditions);inputs=List.copyOf(inputs); }
    public String describe() {
        return type+":"+sourceId+"; conditions="+String.join(", ",conditions)
                +"; realm="+(minimumRealm<0?"UNREVIEWED":minimumRealm)
                +"; repeatable="+(repeatable==null?"UNREVIEWED":repeatable)
                +"; inputs="+String.join(", ",inputs);
    }
}
