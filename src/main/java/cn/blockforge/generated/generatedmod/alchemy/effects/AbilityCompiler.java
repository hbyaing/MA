package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import com.google.gson.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation.*;

/** Pure bounded compiler: physical stocks -> immutable paid effects -> nine-grade quality. */
public final class AbilityCompiler {
    public static final int PPM=1000000;
    public static final String BOUND="v5_bound_pill";
    public enum Kind { heal,mana,regeneration,speed,resistance,shield,cleanse,cultivation,minor_breakthrough }
    public record Definition(Kind id,List<String> sources,long maxMilli,int duration,int interval,long costMilli,int cooldown,int realm,int activation){
        public Definition{sources=List.copyOf(sources);}
    }
    public record Spec(Kind id,long magnitudeMilli,int duration,int interval,long cost,int cooldown,int realm){
        public Spec{Objects.requireNonNull(id);if(magnitudeMilli<minimum(id)||magnitudeMilli>maximum(id)||duration<0||duration>12000||interval<1||interval>200||cost<1||cost>1000000||cooldown<1||cooldown>1200||realm<0||realm>8)throw new IllegalArgumentException("Invalid effect specification");}
    }
    public record Result(int grade,int quantity,long boundMass,long residualMass,long budget,long spent,int purity,int stability,int structure,int toxicity,int potency,int score,String policyHash,List<Spec> effects){
        public Result{
            effects=List.copyOf(effects);Objects.requireNonNull(policyHash);
            if(grade<0||grade>9||quantity<0||quantity>64||boundMass<0||residualMass<0||budget<0||budget>1000000||spent<0||spent>budget||effects.size()>9)throw new IllegalArgumentException("Invalid compiled result");
            for(int v:new int[]{purity,stability,structure,toxicity,potency,score})if(v<0||v>PPM)throw new IllegalArgumentException("Invalid quality");
            var ids=new HashSet<Kind>();long sum=0;for(var e:effects){if(!ids.add(e.id()))throw new IllegalArgumentException("Duplicate module");sum=Math.addExact(sum,e.cost());}
            if(sum!=spent||grade>0&&(quantity<1||effects.isEmpty()||boundMass<quantity)||grade==0&&(quantity!=0||boundMass!=0||!effects.isEmpty()))throw new IllegalArgumentException("Unfunded result");
        }
        public String family(){return grade==0?"waste":effects.size()==1?effects.get(0).id().name():"mixed";}
        public int realm(){return effects.stream().mapToInt(Spec::realm).max().orElse(0);}
    }
    public static final class Policy {
        public final String json,hash;public final int mass,maxYield;public final List<Definition> modules;
        public Policy(String text){
            if(text.length()>32768)throw new IllegalArgumentException("Ability policy byte budget");var root=SimulationRuleLoader.document(text);
            keys(root,"schema","version","massPerPill","maxYield","modules");if(integer(root,"schema",1,1)!=1||!root.get("version").getAsString().equals("v5-effects/1"))throw new IllegalArgumentException("Unknown compiler version");
            mass=integer(root,"massPerPill",1,1000000);maxYield=integer(root,"maxYield",1,64);var defs=new ArrayList<Definition>();var seen=new HashSet<Kind>();
            for(var entry:root.getAsJsonArray("modules")){
                var o=entry.getAsJsonObject();keys(o,"id","sources","maxMilli","durationTicks","intervalTicks","costMilli","cooldownTicks","minRealm","activationPpm");
                var id=Kind.valueOf(o.get("id").getAsString());if(!seen.add(id))throw new IllegalArgumentException("Duplicate ability");
                var sources=new ArrayList<String>();for(var v:o.getAsJsonArray("sources")){String source=v.getAsString();SimulationRules.id(source);if(!source.startsWith("extract_")||sources.contains(source))throw new IllegalArgumentException("Invalid module source");sources.add(source);}
                if(sources.isEmpty()||sources.size()>16)throw new IllegalArgumentException("Source budget");
                int duration=integer(o,"durationTicks",0,12000),interval=integer(o,"intervalTicks",1,200);
                if(timed(id)!=(duration>0)||id==Kind.regeneration&&interval!=20)throw new IllegalArgumentException("Unsupported trigger/duration");
                defs.add(new Definition(id,sources,integer(o,"maxMilli",(int)minimum(id),(int)maximum(id)),duration,interval,integer(o,"costMilli",1,1000000),integer(o,"cooldownTicks",1,1200),integer(o,"minRealm",0,8),integer(o,"activationPpm",0,PPM)));
            }
            if(defs.isEmpty()||defs.size()>9)throw new IllegalArgumentException("Ability count budget");modules=List.copyOf(defs);json=root.toString();hash=hash(json);
        }
        public void validateResources(SimulationRules rules){for(var resource:rules.resources.values())if(resource.id().startsWith("extract_")&&(resource.mass()!=1||resource.potential()!=1))throw new IllegalArgumentException("Unsupported extract units");for(var d:modules)for(String id:d.sources()){var r=rules.resources.get(id);if(r==null||r.mass()!=1||r.potential()!=1||r.destination()!=SimulationRules.Destination.ACTIVE)throw new IllegalArgumentException("Unsupported ability resource "+id);}var bound=rules.resources.get(BOUND);if(bound==null||bound.mass()!=1||bound.potential()!=1||bound.destination()!=SimulationRules.Destination.PILL)throw new IllegalArgumentException("Missing bound-pill account");}
    }
    public static Result compile(Policy policy,SimulationRules rules,Snapshot s,int structureDamage){
        AlchemySimulation.validate(rules,s);policy.validateResources(rules);if(structureDamage<0||structureDamage>1000)throw new IllegalArgumentException("Invalid structure damage");
        long remainingMass=rules.weight(s.stocks(),true),extract=0,raw=0,toxic=0;
        for(var e:s.stocks().entrySet()){if(e.getKey().startsWith("extract_"))extract+=e.getValue();if(e.getKey().startsWith("herb_")&&!e.getKey().equals("herb_inert"))raw+=e.getValue();if(e.getKey().equals("extract_du")||e.getKey().equals("extract_huan"))toxic+=e.getValue();}
        int toxicity=ratio(toxic,Math.max(1,extract));int purity=clamp(PPM-ratio(raw,Math.max(1,raw+extract))/4-toxicity*4/5);
        int structure=PPM-structureDamage*1000;
        int thermal=ratio(s.damage(),rules.limits.maximumDamage());int loss=ratio(s.ledger().ventedMass(),Math.max(1,s.ledger().initialMass()));
        int stability=clamp(PPM-Math.max(thermal,Math.max(PPM-structure,loss)));
        int quantity=(int)Math.min(policy.maxYield,extract/policy.mass);long budget=(long)policy.mass*(PPM-toxicity)/PPM;
        var empty=new Result(0,0,0,remainingMass,budget,0,purity,stability,structure,toxicity,0,0,policy.hash,List.of());
        if(s.status()!=Status.FINISHED||quantity==0||purity<250000||stability<250000||structure<750000||budget<1)return empty;
        var credits=new TreeMap<String,Long>();for(var e:s.stocks().entrySet())if(e.getKey().startsWith("extract_"))credits.put(e.getKey(),e.getValue()*policy.mass/Math.max(1,extract));
        double quality=(purity/(double)PPM)*(stability/(double)PPM);long balance=budget;double utility=0;var effects=new ArrayList<Spec>();
        for(var d:policy.modules){
            long source=0;for(String id:d.sources())source+=s.stocks().getOrDefault(id,0L);int concentration=ratio(source,Math.max(1,extract));if(source==0||concentration<d.activation())continue;
            long desired=(long)StrictMath.floor(d.maxMilli()*(1-StrictMath.exp(-3D*concentration/PPM))*quality);
            if(d.id()==Kind.cleanse||d.id()==Kind.minor_breakthrough)desired=1000;
            int duration=d.duration()==0?0:Math.max(20,(int)(d.duration()*quality));long factor=factor(d.id(),duration,d.interval());
            long sourceCredit=0;for(String id:d.sources())sourceCredit+=credits.getOrDefault(id,0L);
            long affordable=Math.min(balance,sourceCredit)*1000000L/(d.costMilli()*factor);long magnitude=Math.min(desired,affordable);
            if(d.id()==Kind.shield)magnitude=magnitude/4000*4000;
            if(d.id()==Kind.resistance||d.id()==Kind.cleanse||d.id()==Kind.minor_breakthrough||d.id()==Kind.cultivation)magnitude=magnitude/1000*1000;
            if(magnitude<minimum(d.id()))continue;long cost=ceil(magnitude*d.costMilli()*factor,1000000);
            var spec=new Spec(d.id(),magnitude,duration,d.interval(),cost,d.cooldown(),d.realm());effects.add(spec);balance-=cost;long debit=cost;for(String id:d.sources()){long used=Math.min(debit,credits.getOrDefault(id,0L));credits.put(id,credits.getOrDefault(id,0L)-used);debit-=used;}if(debit!=0)throw new IllegalStateException("Module source double spend");
            double function=magnitude/(double)d.maxMilli();if(d.duration()>0)function*=duration/(double)d.duration();utility=Math.max(utility,Math.min(1,function));
        }
        int potency=clamp((int)StrictMath.floor(utility*PPM));int score=clamp((int)((48L*potency+28L*purity+24L*stability-22L*toxicity)/100));
        int grade=grade(score,purity,stability,structure,toxicity,potency,true);
        if(effects.isEmpty()||grade==0)return empty;
        return new Result(grade,quantity,(long)quantity*policy.mass,remainingMass-(long)quantity*policy.mass,budget,budget-balance,purity,stability,structure,toxicity,potency,score,policy.hash,effects);
    }
    /** Half-open score bands; nine additionally requires all independent hard gates. */
    public static int grade(int score,int purity,int stability,int structure,int toxicity,int potency,boolean completeLedger){
        for(int v:new int[]{score,purity,stability,structure,toxicity,potency})if(v<0||v>PPM)throw new IllegalArgumentException("Invalid quality input");
        if(!completeLedger||score<100000||purity<250000||stability<250000||structure<750000||potency<1000)return 0;
        int grade=Math.min(9,score/100000);
        if(grade==9&&(purity<950000||stability<950000||structure<980000||toxicity>20000||potency<800000))return 8;
        return grade;
    }
    /** Transfer unbound material to the existing residue/loss accounts. Never change the rules hash. */
    public static Snapshot bind(SimulationRules rules,Snapshot source,Result result){
        AlchemySimulation.validate(rules,source);if(source.status()!=Status.FINISHED||result.grade()==0)throw new IllegalArgumentException("Only actual successful results bind");
        long potential=rules.weight(source.stocks(),false);if(result.boundMass()>potential||result.boundMass()+result.residualMass()!=rules.weight(source.stocks(),true))throw new IllegalArgumentException("Unfunded pill allocation");
        var l=source.ledger();var ledger=new Ledger(l.initialMass(),l.residualMass()+result.residualMass(),l.ashMass(),l.ventedMass(),l.initialPotential(),l.authorizedPotential(),l.lostPotential()+potential-result.boundMass(),l.ventedPotential(),l.initialEnergy(),l.externalEnergy(),l.boundEnergy(),l.lostEnergy(),l.ventedEnergy());
        var bound=new Snapshot(source.batchId(),source.rulesHash(),source.tick(),source.phase(),source.status(),"PILLS_BOUND",Map.of(BOUND,result.boundMass()),source.heat(),source.damage(),source.referenceMass(),source.seed(),source.environment(),source.draws(),source.holds(),source.lastSequence(),source.operations(),ledger);
        AlchemySimulation.validate(rules,bound);return bound;
    }
    public static boolean timed(Kind k){return switch(k){case regeneration,speed,resistance,shield->true;default->false;};}
    public static long minimum(Kind k){return switch(k){case heal,mana->250;case regeneration->50;case speed->10;case shield->4000;default->1000;};}
    public static long maximum(Kind k){return switch(k){case heal->24000;case mana->100000;case regeneration->4000;case speed->350;case resistance->3000;case shield->24000;case cultivation->100000;default->1000;};}
    private static long factor(Kind k,int duration,int interval){return k==Kind.regeneration?Math.max(1,duration/interval):timed(k)?Math.max(1,ceil(duration,20)):1;}
    private static long ceil(long a,long b){return a/b+(a%b==0?0:1);}
    private static int ratio(long a,long b){if(a<0||b<1)throw new IllegalArgumentException("Invalid ratio");return java.math.BigInteger.valueOf(a).multiply(java.math.BigInteger.valueOf(PPM)).divide(java.math.BigInteger.valueOf(b)).min(java.math.BigInteger.valueOf(PPM)).intValueExact();}
    private static int clamp(int n){return Math.max(0,Math.min(PPM,n));}
    public static String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private static int integer(JsonObject o,String k,int min,int max){int n=o.get(k).getAsBigDecimal().intValueExact();if(n<min||n>max)throw new IllegalArgumentException("Invalid "+k);return n;}
    private static void keys(JsonObject o,String... keys){if(!o.keySet().equals(Set.of(keys)))throw new IllegalArgumentException("Unknown ability definition fields");}
    private AbilityCompiler(){}
}
