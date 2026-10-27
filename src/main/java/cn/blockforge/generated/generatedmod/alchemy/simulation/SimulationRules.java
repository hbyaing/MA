package cn.blockforge.generated.generatedmod.alchemy.simulation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Immutable, versioned rule snapshot. Physical resource names are data, not a herb enum. */
public final class SimulationRules {
    public static final String ENGINE="alchemy-core/0.1.0", RANDOM="splitmix-fnv-domain/1";
    public enum Destination { ACTIVE, RESIDUE, ASH, VENTED, PILL }
    public enum Phase { WARMUP, EXTRACTION, FUSION, HARMONIZE, CONDENSE, COOL }
    public enum Measure { TEMPERATURE, STOCK, CONCENTRATION_PPM, RATIO_PPM, ENVIRONMENT }
    public record Resource(String id,long mass,long potential,long gas,Destination destination) {
        public Resource {SimulationRules.id(id);if(mass<1||mass>1000000||potential<0||potential>1000000||gas<0||gas>1000000)throw new IllegalArgumentException("Resource units invalid");Objects.requireNonNull(destination);}
    }
    public record Window(Measure measure,String key,String relativeTo,long minimum,long maximum) {
        public Window {Objects.requireNonNull(measure);Objects.requireNonNull(key);if(minimum>maximum)throw new IllegalArgumentException("Invalid window");if(measure!=Measure.TEMPERATURE)id(key);Objects.requireNonNull(relativeTo);if(measure==Measure.RATIO_PPM)id(relativeTo);else if(!relativeTo.isEmpty())throw new IllegalArgumentException("Unexpected ratio reference");}
        public Window(Measure measure,String key,long minimum,long maximum){this(measure,key,"",minimum,maximum);}
    }
    public record Reaction(String id,int priority,Phase phase,Map<String,Long> consume,Map<String,Long> produce,
                           long maximumExtent,long energyCost,long authorizedPotential,int holdSteps,List<Window> windows) {
        public Reaction {
            SimulationRules.id(id);Objects.requireNonNull(phase);consume=quantities(consume);produce=quantities(produce);
            if(consume.isEmpty()||maximumExtent<1||maximumExtent>1000000||energyCost<0||energyCost>1000000000||authorizedPotential<0||authorizedPotential>energyCost||holdSteps<1||holdSteps>10000)throw new IllegalArgumentException("Invalid reaction bounds");
            windows=List.copyOf(windows);if(windows.size()>32)throw new IllegalArgumentException("Too many windows");
        }
    }
    public record Limits(int maxRules,int maxExtent,int maxSteps,int maxEvents,long maxStockUnits,long maxExternalPerEvent,
                         long heatCapacity,long coolingDivisor,long damageTemperature,long maximumDamage,long burstPressure,int controlNoise) {
        public Limits {if(maxRules<1||maxRules>128||maxSteps<1||maxSteps>1000000||maxExtent<1||maxExtent>1000000||maxEvents<1||maxEvents>1024||maxStockUnits<1||maxStockUnits>1000000000L||maxExternalPerEvent<0||maxExternalPerEvent>1000000000L||heatCapacity<1||heatCapacity>1000000||coolingDivisor<1||damageTemperature<0||damageTemperature>1000000000L||maximumDamage>1000000000000000L||maximumDamage<1||burstPressure<1||controlNoise<0||controlNoise>10000)throw new IllegalArgumentException("Invalid limits");}
    }
    public final String version,hash;
    public final Map<String,Resource> resources;
    public final List<Reaction> reactions;
    public final Limits limits;
    public SimulationRules(String version,List<Resource> resources,List<Reaction> reactions,Limits limits){
        id(version);this.version=version;this.limits=Objects.requireNonNull(limits);
        var table=new TreeMap<String,Resource>();for(var r:resources)if(table.putIfAbsent(r.id(),r)!=null)throw new IllegalArgumentException("Duplicate resource");
        if(table.isEmpty()||table.size()>128||reactions.size()>limits.maxRules())throw new IllegalArgumentException("Rule/resource budget exceeded");this.resources=Collections.unmodifiableMap(table);
        var sorted=new ArrayList<>(reactions);sorted.sort(Comparator.comparingInt(Reaction::priority).thenComparing(Reaction::id));var ids=new HashSet<String>();
        for(var rule:sorted){
            if(!ids.add(rule.id()))throw new IllegalArgumentException("Duplicate rule");
            if(weight(rule.consume(),true)!=weight(rule.produce(),true))throw new IllegalArgumentException("Reaction mass mismatch: "+rule.id());
            if(weight(rule.produce(),false)>Math.addExact(weight(rule.consume(),false),rule.authorizedPotential()))throw new IllegalArgumentException("Unfunded potential: "+rule.id());
            for(var w:rule.windows())if(((w.measure()==Measure.STOCK||w.measure()==Measure.CONCENTRATION_PPM||w.measure()==Measure.RATIO_PPM)&&!table.containsKey(w.key()))||(w.measure()==Measure.RATIO_PPM&&!table.containsKey(w.relativeTo())))throw new IllegalArgumentException("Unknown window resource");
        }
        this.reactions=List.copyOf(sorted);
        String canonical=ENGINE+"|"+RANDOM+"|"+version+"|"+limits+"|"+table+"|"+sorted;
        try{hash=java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public long weight(Map<String,Long> stocks,boolean mass){long result=0;for(var e:stocks.entrySet()){var resource=resources.get(e.getKey());if(resource==null)throw new IllegalArgumentException("Unknown resource "+e.getKey());result=Math.addExact(result,Math.multiplyExact(e.getValue(),mass?resource.mass():resource.potential()));}return result;}
    public static Map<String,Long> quantities(Map<String,Long> input){var result=new TreeMap<String,Long>();if(input.size()>128)throw new IllegalArgumentException("Too many resources");for(var e:input.entrySet()){id(e.getKey());if(e.getValue()==null||e.getValue()<0||e.getValue()>1000000000L)throw new IllegalArgumentException("Invalid quantity");if(e.getValue()>0)result.put(e.getKey(),e.getValue());}return Collections.unmodifiableMap(result);}
    public static void id(String id){if(id==null||id.length()>96||!id.matches("[a-z0-9_.:/-]+"))throw new IllegalArgumentException("Invalid id");}
}
