package cn.blockforge.generated.generatedmod.alchemy.simulation;

import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules.*;

/** Fixed logical steps; does not create ItemStacks, grant grades, consume player resources or send seeds. */
public final class AlchemySimulation {
    public enum Status { ACTIVE, PAUSED, FINISHED, ABORTED, ASHED, EXPLODED }
    public enum Action { ADVANCE, HEAT, VENT, ENVIRONMENT, ABORT, FINISH }
    public record Operation(long sequence,long tick,Action action,String resource,long amount) {
        public Operation {if(sequence<0||tick<0||amount>1000000000L||amount< -1000000000L||(action!=Action.ENVIRONMENT&&amount<0))throw new IllegalArgumentException("Invalid operation");Objects.requireNonNull(action);Objects.requireNonNull(resource);if(!resource.isEmpty())id(resource);}
    }
    /** Three different accounts: material mass, medicinal potential, thermal/external energy. */
    public record Ledger(long initialMass,long residualMass,long ashMass,long ventedMass,
                         long initialPotential,long authorizedPotential,long lostPotential,long ventedPotential,
                         long initialEnergy,long externalEnergy,long boundEnergy,long lostEnergy,long ventedEnergy) {
        public Ledger {for(long v:new long[]{initialMass,residualMass,ashMass,ventedMass,initialPotential,authorizedPotential,lostPotential,ventedPotential,initialEnergy,externalEnergy,boundEnergy,lostEnergy,ventedEnergy})if(v<0)throw new IllegalArgumentException("Negative ledger");}
    }
    /** SERVER PRIVATE. Encoding contains the official seed and must never be used as a client DTO. */
    public record Snapshot(String batchId,String rulesHash,long tick,Phase phase,Status status,String reason,Map<String,Long> stocks,
                           long heat,long damage,long referenceMass,long seed,Map<String,Long> environment,Map<String,Long> draws,Map<String,Long> holds,
                           long lastSequence,List<Operation> operations,Ledger ledger) {
        public Snapshot {id(batchId);Objects.requireNonNull(rulesHash);Objects.requireNonNull(phase);Objects.requireNonNull(status);Objects.requireNonNull(reason);stocks=quantities(stocks);environment=environmentCopy(environment);draws=quantities(draws);holds=quantities(holds);operations=List.copyOf(operations);Objects.requireNonNull(ledger);if(tick<0||heat<0||damage<0||referenceMass<1||referenceMass>1000000000L||heat>1000000000000L||lastSequence< -1||reason.length()>96||operations.size()>256)throw new IllegalArgumentException("Invalid snapshot");}
    }
    public record ReactionTrace(String rule,long extent,long potentialLost,long energyBound){}
    public record Step(Snapshot snapshot,List<ReactionTrace> reactions,long peakTemperature,java.math.BigInteger peakPressure){}
    public static Snapshot begin(SimulationRules rules,String batchId,Map<String,Long> input,long heat,long referenceMass,long seed,Map<String,Long> environment){
        // Environment is recorded at creation; changes require explicit events included in history.
        validateEnvironment(environment);input=quantities(input);long mass=rules.weight(input,true),potential=rules.weight(input,false);
        var state=new Snapshot(batchId,rules.hash,0,Phase.WARMUP,Status.ACTIVE,"",input,heat,0,referenceMass,seed,environment,Map.of(),Map.of(),-1,List.of(),new Ledger(mass,0,0,0,potential,0,0,0,heat,0,0,0,0));validate(rules,state);return state;
    }
    public static void validate(SimulationRules rules,Snapshot s){
        if(!s.rulesHash().equals(rules.hash))throw new IllegalArgumentException("Rule snapshot mismatch; retain original batch and bind its rules");
        long count=0;for(long q:s.stocks().values())count=Math.addExact(count,q);if(count>rules.limits.maxStockUnits()||s.operations().size()>rules.limits.maxEvents())throw new IllegalArgumentException("Batch budget exceeded");
        var knownRules=new HashMap<String,Reaction>();for(var rule:rules.reactions)knownRules.put(rule.id(),rule);
        for(var e:s.holds().entrySet())if(!knownRules.containsKey(e.getKey())||e.getValue()>knownRules.get(e.getKey()).holdSteps())throw new IllegalArgumentException("Invalid reaction checkpoint");
        for(String domain:s.draws().keySet())if(!domain.equals("control"))throw new IllegalArgumentException("Unknown random domain");
        long sequence=-1,previousTick=-1;for(var op:s.operations()){if(op.sequence()<=sequence||op.tick()>=s.tick()||op.tick()<previousTick)throw new IllegalArgumentException("Invalid history");sequence=op.sequence();previousTick=op.tick();}
        if(sequence!=s.lastSequence())throw new IllegalArgumentException("Operation history mismatch");
        var l=s.ledger();if(l.initialMass()!=Math.addExact(rules.weight(s.stocks(),true),Math.addExact(l.residualMass(),Math.addExact(l.ashMass(),l.ventedMass()))))throw new IllegalArgumentException("Mass ledger mismatch");
        if(Math.addExact(l.initialPotential(),l.authorizedPotential())!=Math.addExact(rules.weight(s.stocks(),false),Math.addExact(l.lostPotential(),l.ventedPotential())))throw new IllegalArgumentException("Potential ledger mismatch");
        if(Math.addExact(l.initialEnergy(),l.externalEnergy())!=Math.addExact(s.heat(),Math.addExact(l.boundEnergy(),Math.addExact(l.lostEnergy(),l.ventedEnergy()))))throw new IllegalArgumentException("Energy ledger mismatch");
    }
    private static void validateEnvironment(Map<String,Long> environment){if(environment.size()>32)throw new IllegalArgumentException("Environment budget exceeded");for(var e:environment.entrySet()){id(e.getKey());if(e.getValue()==null||(e.getValue()< -1000000000L||e.getValue()>1000000000L))throw new IllegalArgumentException("Invalid environment value");}}
    private static Map<String,Long> environmentCopy(Map<String,Long> input){validateEnvironment(input);return Collections.unmodifiableMap(new TreeMap<>(input));}
    private static java.math.BigInteger pressure(SimulationRules r,Map<String,Long> stocks,long energy){long gas=0;for(var e:stocks.entrySet())gas=Math.addExact(gas,Math.multiplyExact(e.getValue(),r.resources.get(e.getKey()).gas()));return java.math.BigInteger.valueOf(gas).multiply(java.math.BigInteger.valueOf(energy)).divide(java.math.BigInteger.valueOf(r.limits.heatCapacity()).multiply(java.math.BigInteger.valueOf(1000)));}
    private static boolean windows(SimulationRules r,Snapshot s,Map<String,Long> environment,Reaction rule){
        for(var w:rule.windows()){
            if(w.measure()==Measure.ENVIRONMENT&&!environment.containsKey(w.key()))return false;
            if(w.measure()==Measure.TEMPERATURE){if(!ratio(s.heat(),r.limits.heatCapacity(),1,w.minimum(),w.maximum()))return false;continue;}
            if(w.measure()==Measure.CONCENTRATION_PPM){if(!ratio(s.stocks().getOrDefault(w.key(),0L),s.referenceMass(),1000000,w.minimum(),w.maximum()))return false;continue;}
            if(w.measure()==Measure.RATIO_PPM){if(!ratio(s.stocks().getOrDefault(w.key(),0L),s.stocks().getOrDefault(w.relativeTo(),0L),1000000,w.minimum(),w.maximum()))return false;continue;}
            long value=switch(w.measure()){
                case TEMPERATURE -> s.heat()/r.limits.heatCapacity();
                case STOCK -> s.stocks().getOrDefault(w.key(),0L);
                case CONCENTRATION_PPM, RATIO_PPM -> throw new IllegalStateException("Ratio already checked");
                case ENVIRONMENT -> {if(!environment.containsKey(w.key()))yield Long.MIN_VALUE;yield environment.get(w.key());}
            };
            if(value<w.minimum()||value>w.maximum())return false;
        }return true;
    }
    private static boolean ratio(long numerator,long denominator,long scale,long min,long max){
        if(denominator<=0)return false;
        var value=java.math.BigInteger.valueOf(numerator).multiply(java.math.BigInteger.valueOf(scale));var d=java.math.BigInteger.valueOf(denominator);
        return value.compareTo(java.math.BigInteger.valueOf(min).multiply(d))>=0&&value.compareTo(java.math.BigInteger.valueOf(max).multiply(d))<=0;
    }
    /** Counter-based domain stream; observing other domains cannot advance the control stream. */
    public static long sample(long seed,String domain,long counter){id(domain);if(counter<0)throw new IllegalArgumentException("Negative draw counter");long hash=0xcbf29ce484222325L;for(byte b:domain.getBytes(java.nio.charset.StandardCharsets.UTF_8))hash=(hash^(b&255))*0x100000001b3L;long z=seed^hash;z+=0x9e3779b97f4a7c15L*(counter+1);z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;z=(z^(z>>>27))*0x94d049bb133111ebL;return z^(z>>>31);}
    public static Step step(SimulationRules r,Snapshot original,List<Operation> events){
        validate(r,original);var environment=new TreeMap<>(original.environment());
        if(original.status()!=Status.ACTIVE)return new Step(original,List.of(),original.heat()/r.limits.heatCapacity(),pressure(r,original.stocks(),original.heat()));
        if(original.tick()>=r.limits.maxSteps())return paused(original,"STEP_BUDGET");
        if(events.size()>32)throw new IllegalArgumentException("Per-step operation budget exceeded");
        var sorted=new ArrayList<>(events);sorted.sort(Comparator.comparingLong(Operation::sequence));
        var history=new ArrayList<>(original.operations());if(history.size()+sorted.size()>r.limits.maxEvents())return paused(original,"OPERATION_BUDGET");
        long last=original.lastSequence(),heat=original.heat(),external=0,ventedMass=0,ventedPotential=0,ventedEnergy=0;var phase=original.phase();var status=Status.ACTIVE;boolean advanced=false;
        var stocks=new TreeMap<>(original.stocks());var draws=new TreeMap<>(original.draws());
        for(int i=0;i<sorted.size();i++){
            var op=sorted.get(i);if(op.tick()!=original.tick()||op.sequence()<=last||status!=Status.ACTIVE)throw new IllegalArgumentException("Stale/out-of-phase operation");
            last=op.sequence();history.add(op);
            switch(op.action()){
                case ADVANCE -> {if(op.amount()!=0||!op.resource().isEmpty()||phase==Phase.COOL||advanced)throw new IllegalArgumentException("Invalid stage transition");phase=Phase.values()[phase.ordinal()+1];advanced=true;}
                case HEAT -> {
                    if(!op.resource().isEmpty()||op.amount()>r.limits.maxExternalPerEvent())throw new IllegalArgumentException("Unauthorized external energy");
                    long actual=op.amount();if(actual>0&&r.limits.controlNoise()>0){long n=draws.getOrDefault("control",0L);long batchSeed=sample(original.seed(),original.batchId(),0);long noise=Math.floorMod(sample(batchSeed,"control",n),2L*r.limits.controlNoise()+1)-r.limits.controlNoise();draws.put("control",Math.addExact(n,1));actual=Math.max(0,Math.addExact(actual,noise));}
                    heat=Math.addExact(heat,actual);external=Math.addExact(external,actual);
                }
                case VENT -> {
                    var resource=r.resources.get(op.resource());if(resource==null||resource.gas()==0||op.amount()>stocks.getOrDefault(op.resource(),0L))throw new IllegalArgumentException("Invalid vent resource");
                    stocks.put(op.resource(),stocks.getOrDefault(op.resource(),0L)-op.amount());ventedMass=Math.addExact(ventedMass,Math.multiplyExact(op.amount(),resource.mass()));ventedPotential=Math.addExact(ventedPotential,Math.multiplyExact(op.amount(),resource.potential()));
                }
                case ENVIRONMENT -> {if(!environment.containsKey(op.resource()))throw new IllegalArgumentException("Unknown environmental event key");environment.put(op.resource(),op.amount());}
                case ABORT -> {if(op.amount()!=0||!op.resource().isEmpty())throw new IllegalArgumentException("Invalid abort");status=Status.ABORTED;}
                case FINISH -> {if(phase!=Phase.COOL||op.amount()!=0||!op.resource().isEmpty())throw new IllegalArgumentException("Finish requires cooling stage");status=Status.FINISHED;}
            }
        }
        var old=original.ledger();var ledger=new Ledger(old.initialMass(),old.residualMass(),old.ashMass(),Math.addExact(old.ventedMass(),ventedMass),old.initialPotential(),old.authorizedPotential(),old.lostPotential(),Math.addExact(old.ventedPotential(),ventedPotential),old.initialEnergy(),Math.addExact(old.externalEnergy(),external),old.boundEnergy(),old.lostEnergy(),Math.addExact(old.ventedEnergy(),ventedEnergy));
        var ready=new Snapshot(original.batchId(),r.hash,original.tick(),phase,status,"",stocks,heat,original.damage(),original.referenceMass(),original.seed(),environment,draws,original.holds(),last,history,ledger);
        var holds=new TreeMap<String,Long>();var remaining=new TreeMap<>(stocks);var delta=new TreeMap<String,Long>();var traces=new ArrayList<ReactionTrace>();
        long potentialLost=0,potentialAdded=0,bound=0;int budget=r.limits.maxExtent();boolean exhausted=false;
        if(status==Status.ACTIVE)for(var rule:r.reactions){
            boolean eligible=phase==rule.phase()&&windows(r,ready,environment,rule);long held=eligible?Math.min(rule.holdSteps(),original.holds().getOrDefault(rule.id(),0L)+1):0;holds.put(rule.id(),held);
            if(!eligible||held<rule.holdSteps())continue;
            long extent=rule.maximumExtent();for(var need:rule.consume().entrySet())extent=Math.min(extent,remaining.getOrDefault(need.getKey(),0L)/need.getValue());
            if(rule.energyCost()>0)extent=Math.min(extent,(heat-bound)/rule.energyCost());
            if(extent>budget){extent=budget;exhausted=true;}if(extent==0)continue;budget-=Math.toIntExact(extent);
            for(var need:rule.consume().entrySet()){long q=Math.multiplyExact(need.getValue(),extent);remaining.put(need.getKey(),remaining.get(need.getKey())-q);delta.merge(need.getKey(),-q,Math::addExact);}
            for(var out:rule.produce().entrySet())delta.merge(out.getKey(),Math.multiplyExact(out.getValue(),extent),Math::addExact);
            long loss=Math.multiplyExact(Math.subtractExact(Math.addExact(r.weight(rule.consume(),false),rule.authorizedPotential()),r.weight(rule.produce(),false)),extent),energy=Math.multiplyExact(rule.energyCost(),extent);
            potentialLost=Math.addExact(potentialLost,loss);potentialAdded=Math.addExact(potentialAdded,Math.multiplyExact(rule.authorizedPotential(),extent));bound=Math.addExact(bound,energy);holds.put(rule.id(),0L);traces.add(new ReactionTrace(rule.id(),extent,loss,energy));
        }
        for(var e:delta.entrySet()){long q=Math.addExact(stocks.getOrDefault(e.getKey(),0L),e.getValue());if(q<0)throw new IllegalStateException("Resource double spend");stocks.put(e.getKey(),q);}
        // Capture the step peak after emergency operations, before reactions can hide hazardous gas/heat.
        long peakHeat=heat;var beforePressure=pressure(r,ready.stocks(),heat);
        heat-=bound;long temperature=peakHeat/r.limits.heatCapacity();var pressure=beforePressure.max(pressure(r,stocks,heat));
        long damage=Math.addExact(original.damage(),Math.max(0,peakHeat-Math.multiplyExact(r.limits.damageTemperature(),r.limits.heatCapacity())));
        long cooling=heat/r.limits.coolingDivisor();heat-=cooling;
        ledger=new Ledger(ledger.initialMass(),ledger.residualMass(),ledger.ashMass(),ledger.ventedMass(),ledger.initialPotential(),Math.addExact(ledger.authorizedPotential(),potentialAdded),Math.addExact(ledger.lostPotential(),potentialLost),ledger.ventedPotential(),ledger.initialEnergy(),ledger.externalEnergy(),Math.addExact(ledger.boundEnergy(),bound),Math.addExact(ledger.lostEnergy(),cooling),ledger.ventedEnergy());
        String reason="";
        if(pressure.compareTo(java.math.BigInteger.valueOf(r.limits.burstPressure()))>=0){
            status=Status.EXPLODED;reason="PRESSURE";
        }else if(damage>=r.limits.maximumDamage()){
            status=Status.ASHED;reason="THERMAL_DAMAGE";
        }else if(exhausted){status=Status.PAUSED;reason="REACTION_BUDGET";}
        if(status==Status.ABORTED){
            ledger=new Ledger(ledger.initialMass(),ledger.residualMass(),ledger.ashMass(),ledger.ventedMass(),ledger.initialPotential(),ledger.authorizedPotential(),ledger.lostPotential(),ledger.ventedPotential(),ledger.initialEnergy(),ledger.externalEnergy(),ledger.boundEnergy(),ledger.lostEnergy(),Math.addExact(ledger.ventedEnergy(),heat));heat=0;
        }
        if(status==Status.EXPLODED||status==Status.ASHED){
            long mass=r.weight(stocks,true),potential=r.weight(stocks,false),vent=status==Status.EXPLODED?mass/4:0;
            ledger=new Ledger(ledger.initialMass(),ledger.residualMass(),Math.addExact(ledger.ashMass(),mass-vent),Math.addExact(ledger.ventedMass(),vent),ledger.initialPotential(),ledger.authorizedPotential(),Math.addExact(ledger.lostPotential(),potential),ledger.ventedPotential(),ledger.initialEnergy(),ledger.externalEnergy(),ledger.boundEnergy(),Math.addExact(ledger.lostEnergy(),heat),ledger.ventedEnergy());stocks.clear();heat=0;
        }
        return checked(r,with(r,ready,stocks,heat,damage,status,reason,holds,ledger),List.copyOf(traces),temperature,pressure);
    }
    private static Snapshot with(SimulationRules r,Snapshot s,Map<String,Long> stocks,long heat,long damage,Status status,String reason,Map<String,Long> holds,Ledger ledger){return new Snapshot(s.batchId(),r.hash,Math.addExact(s.tick(),1),s.phase(),status,reason,stocks,heat,damage,s.referenceMass(),s.seed(),s.environment(),s.draws(),holds,s.lastSequence(),s.operations(),ledger);}
    private static Step checked(SimulationRules r,Snapshot s,List<ReactionTrace> traces,long temp,java.math.BigInteger pressure){validate(r,s);return new Step(s,traces,temp,pressure);}
    private static Step paused(Snapshot s,String reason){return new Step(new Snapshot(s.batchId(),s.rulesHash(),s.tick(),s.phase(),Status.PAUSED,reason,s.stocks(),s.heat(),s.damage(),s.referenceMass(),s.seed(),s.environment(),s.draws(),s.holds(),s.lastSequence(),s.operations(),s.ledger()),List.of(),0,java.math.BigInteger.ZERO);}
    private AlchemySimulation(){}
}
