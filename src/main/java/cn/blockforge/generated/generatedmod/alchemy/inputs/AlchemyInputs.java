package cn.blockforge.generated.generatedmod.alchemy.inputs;

import cn.blockforge.generated.generatedmod.HerbPreparation;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.fire.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** Read-only adapters. They NEVER consume items or start a live furnace transaction. */
public final class AlchemyInputs {
    public record Material(String item,int count,int years,boolean prepared,Map<String,Long> stocks,
                           long referenceMass,net.minecraft.nbt.CompoundTag provenance){
        public Material {stocks=SimulationRules.quantities(stocks);provenance=provenance.copy();}
        @Override public net.minecraft.nbt.CompoundTag provenance(){return provenance.copy();}
    }
    public record Batch(List<Material> materials,Map<String,Long> stocks,long referenceMass,String definitionsHash){
        public Batch {materials=List.copyOf(materials);stocks=SimulationRules.quantities(stocks);}
        public AlchemySimulation.Snapshot begin(SimulationRules rules,String batch,long seed,AlchemyEnvironment.Sample environment){
            for(String id:stocks.keySet())if(!rules.resources.containsKey(id))throw new IllegalArgumentException("Missing input resource: "+id);
            return AlchemySimulation.begin(rules,batch,stocks,0,referenceMass,seed,environment.values());
        }
    }
    /** Per-herb mass is finite. Age changes active/inert partition; more tags never create mass. */
    public static Material material(ItemStack stack,InputDefinitions definitions){
        var herb=HerbAge.definition(stack);
        if(stack.isEmpty()||herb==null||stack.getCount()>64)throw new IllegalArgumentException("Unsupported material/count");
        if(stack.hasTag()&&stack.getTag().getBoolean(HerbAge.CREATIVE_TEMPLATE))throw new IllegalArgumentException("Unresolved creative template");
        int age=HerbAge.years(stack);boolean prepared=HerbPreparation.prepared(stack);
        long mass=Math.multiplyExact(definitions.massPerItem,stack.getCount());
        long activePerItem=definitions.baseActive+definitions.ageActive*age/(age+definitions.ageScale);
        if(prepared)activePerItem+=definitions.preparedActive;
        long active=Math.multiplyExact(activePerItem,stack.getCount());
        var weights=definitions.weights(herb);long total=weights.values().stream().mapToLong(Long::longValue).sum();
        var stocks=new TreeMap<String,Long>();long allocated=0;
        for(var entry:weights.entrySet()){
            long units=total==0?0:active*entry.getValue()/total;
            if(units>0){stocks.put("herb_"+entry.getKey(),units);allocated+=units;}
        }
        if(mass>allocated)stocks.put("herb_inert",mass-allocated);
        return new Material(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),stack.getCount(),age,prepared,
            stocks,mass,stack.hasTag()?stack.getTag():new net.minecraft.nbt.CompoundTag());
    }
    public static Batch batch(List<ItemStack> selected,InputDefinitions definitions){
        if(selected.isEmpty()||selected.size()>64)throw new IllegalArgumentException("Input slot budget exceeded");
        var materials=new ArrayList<Material>();var stocks=new TreeMap<String,Long>();long mass=0;
        for(var stack:selected){if(stack.isEmpty())continue;var material=material(stack,definitions);materials.add(material);
            mass=Math.addExact(mass,material.referenceMass());material.stocks().forEach((key,value)->stocks.merge(key,value,Math::addExact));}
        if(mass<1||mass>1000000000)throw new IllegalArgumentException("Invalid reference mass");
        return new Batch(materials,stocks,mass,definitions.hash);
    }
    public record Fire(String id,int tier,Map<String,Double> essence){public Fire{essence=Collections.unmodifiableMap(new TreeMap<>(essence));}}
    /** Read existing fire NBT without initializing, rewarding or rerolling a bottle. */
    public static Fire fire(ItemStack stack){
        int tier=FireBottleData.tier(stack);if(tier<1||stack.getCount()!=1)throw new IllegalArgumentException("Invalid fire bottle");
        var essence=new TreeMap<String,Double>();for(var kind:FireBottleData.Essence.values())essence.put(kind.key(),FireBottleData.essence(stack,kind));
        return new Fire(FireCatalog.tier(tier).key().toString(),tier,essence);
    }
    /** Bound an already paid fuel energy quantity. Fire tier itself never creates fuel energy. */
    public static AlchemySimulation.Operation heat(AlchemySimulation.Snapshot state,long sequence,long paidEnergy,long maximumRate){
        if(paidEnergy<0||maximumRate<0||paidEnergy>maximumRate)throw new IllegalArgumentException("Unpaid or excessive heat");
        return new AlchemySimulation.Operation(sequence,state.tick(),AlchemySimulation.Action.HEAT,"",paidEnergy);
    }
    private AlchemyInputs(){}
}
