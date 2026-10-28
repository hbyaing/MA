package cn.blockforge.generated.generatedmod.alchemy.batch;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.inputs.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.ContainerHelper;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules.*;

/** Immutable journal transformations. Ingredients are escrowed at commit, never pre-rolled into a pill. */
public final class V5Batch {
    public static final String KEY="V5BatchId";
    public record Profile(String json,SimulationRules rules,int opening,int burn,int[] phases){
        public Profile{phases=phases.clone();}
        @Override public int[] phases(){return phases.clone();}
        public int steps(){return Arrays.stream(phases).sum();}
        public int requiredFuel(){return Math.addExact(opening,Math.multiplyExact(burn,steps()-phases[5]));}
    }
    public static Profile profile(String json,int burn){
        if(burn<1||burn>10000000)throw new IllegalArgumentException("Invalid saved fuel rate");
        var o=SimulationRuleLoader.document(json);
        if(!(o.keySet().equals(Set.of("schema","openingFuel","phaseSteps","rules"))||o.keySet().equals(Set.of("schema","openingFuel","phaseSteps","rules","incidents")))||o.get("schema").getAsBigDecimal().intValueExact()!=1)throw new IllegalArgumentException("Unknown batch profile");
        int opening=o.get("openingFuel").getAsBigDecimal().intValueExact();if(opening<0||opening>100000)throw new IllegalArgumentException("Invalid opening fuel");
        var a=o.getAsJsonArray("phaseSteps");if(a.size()!=6)throw new IllegalArgumentException("Expected six phases");var phases=new int[6];int total=0;
        for(int i=0;i<6;i++){phases[i]=a.get(i).getAsBigDecimal().intValueExact();if(phases[i]<1||phases[i]>10)throw new IllegalArgumentException("Invalid phase duration");total+=phases[i];}
        var rules=SimulationRuleLoader.parse(o.get("rules").toString());if(total>rules.limits.maxSteps())throw new IllegalArgumentException("Insufficient core step budget");
        return new Profile(json,rules,opening,burn,phases);
    }
    public static String currentProfile(ServerLevel level){
        try(var reader=level.getServer().getResourceManager().openAsReader(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","alchemy/batch.json"))){
            var text=new StringBuilder();char[] buffer=new char[4096];int n;while((n=reader.read(buffer))!=-1){text.append(buffer,0,n);if(text.length()>262144)throw new IllegalArgumentException("Profile budget exceeded");}return text.toString();
        }catch(java.io.IOException e){throw new IllegalStateException("Missing V5 batch profile",e);}
    }
    public static NonNullList<ItemStack> inventory(CompoundTag doc){var result=NonNullList.withSize(AlchemyFurnaceBlockEntity.CONTAINER_SIZE,ItemStack.EMPTY);ContainerHelper.loadAllItems(doc.getCompound("Inventory"),result);return result;}
    public static void inventory(CompoundTag doc,NonNullList<ItemStack> items){var n=new CompoundTag();ContainerHelper.saveAllItems(n,items);doc.put("Inventory",n);}
    public static CompoundTag prepare(AlchemyFurnaceBlockEntity furnace,ServerPlayer player){
        var selected=new int[8];for(int i=0;i<8;i++)selected[i]=furnace.getItem(i).getCount();
        return prepare(furnace,player,selected,"ordinary_free","");
    }
    public static CompoundTag prepare(AlchemyFurnaceBlockEntity furnace,ServerPlayer player,int[] selected,String entry,String recipe){
        var items=NonNullList.withSize(AlchemyFurnaceBlockEntity.CONTAINER_SIZE,ItemStack.EMPTY);
        for(int i=0;i<items.size();i++)items.set(i,furnace.getItem(i).copy());
        return prepare((ServerLevel)furnace.getLevel(),furnace.getBlockPos(),player,furnace.tier(),furnace.v5DeviceWear(),furnace.spiritFuel(),items,selected,entry,recipe);
    }
    public static CompoundTag prepare(ServerLevel level,BlockPos pos,ServerPlayer player,int tier,int wear,int fuel,NonNullList<ItemStack> items,int[] selected,String entry,String recipe){
        return prepare(level,pos,player,tier,wear,fuel,items,selected,entry,recipe,List.of());
    }
    public static CompoundTag prepare(ServerLevel level,BlockPos pos,ServerPlayer player,int tier,int wear,int fuel,NonNullList<ItemStack> items,int[] selected,String entry,String recipe,List<ItemStack> additional){
        if(selected.length!=8||items.size()!=AlchemyFurnaceBlockEntity.CONTAINER_SIZE||!Set.of("ordinary_free","ordinary_formula","heavenly").contains(entry))throw new IllegalArgumentException("Invalid entry adapter");
        int burn=cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.burnPerSecond(tier);
        String current=currentProfile(level);var raw=SimulationRuleLoader.document(current);
        if(raw.has("incidents")){var l=raw.getAsJsonObject("rules").getAsJsonObject("limits");l.addProperty("heatCapacity",Math.max(1,(10+tier*5)*(1000-wear)/1000));l.addProperty("burstPressure",3000+tier*1500);current=raw.toString();}
        var profile=profile(current,burn);if(fuel<profile.requiredFuel())throw new IllegalArgumentException("请先注灵，本批次需要储备 "+profile.requiredFuel()+" 点炉内灵力。");
        var defs=InputDefinitions.current();var after=NonNullList.withSize(AlchemyFurnaceBlockEntity.CONTAINER_SIZE,ItemStack.EMPTY);var herbs=new ArrayList<ItemStack>();
        var original=new CompoundTag();var before=NonNullList.withSize(after.size(),ItemStack.EMPTY);
        for(int i=0;i<after.size();i++){after.set(i,items.get(i).copy());before.set(i,items.get(i).copy());}
        ContainerHelper.saveAllItems(original,before);
        var provenance=new ListTag();var moonSources=new CompoundTag();long moon=0;boolean seenBottle=false;
        for(int i=0;i<8;i++){
            var item=after.get(i);int count=selected[i];if(count<0||count>item.getCount())throw new IllegalArgumentException("Invalid material reservation");if(count==0)continue;
            if(item.is(WuXingBlocks.MOON_BOTTLE_ITEM.get())){
                if(seenBottle||item.getCount()!=1)throw new IllegalArgumentException("每批只接入一个蕴月瓶。");seenBottle=true;
                var be=BlockItem.getBlockEntityData(item);var bottle=MoonBottleData.load(be==null?new CompoundTag():be.getCompound("MoonBottle"));
                for(var released:bottle.release(Math.min(MoonBottleData.MAX_RELEASE,bottle.total())).entrySet()){moonSources.putLong(released.getKey(),released.getValue());moon+=released.getValue();}
                var saved=be==null?new CompoundTag():be.copy();saved.put("MoonBottle",bottle.save());item.addTagElement("BlockEntityTag",saved);continue;
            }
            var paid=item.copyWithCount(count);herbs.add(paid);provenance.add(paid.save(new CompoundTag()));item.shrink(count);
        }
        for(var stack:additional){herbs.add(stack.copy());provenance.add(stack.save(new CompoundTag()));}
        var input=entry.equals("ordinary_free")?AlchemyInputs.batch(herbs,defs):AlchemyInputs.batchWithInert(herbs,defs);var stocks=new TreeMap<>(input.stocks());if(moon>0)stocks.put("moon_essence",moon);
        var fire=AlchemyInputs.fire(after.get(AlchemyFurnaceBlockEntity.IGNITION_SLOT));
        var trajectory=new InputTrajectory(defs,fire);var sampled=trajectory.sample(level,pos);
        UUID id=UUID.randomUUID();var core=AlchemySimulation.begin(profile.rules(),id.toString(),stocks,0,input.referenceMass()+moon,level.random.nextLong(),sampled.values());
        var doc=new CompoundTag();doc.putInt("Schema",1);doc.putInt("DeviceWear",wear);doc.putUUID("Id",id);doc.putUUID("Owner",player.getUUID());doc.putString("Dimension",level.dimension().location().toString());doc.putLong("Position",pos.asLong());
        doc.putString("Entry",entry);doc.putString("Recipe",recipe);doc.putString("State","ACTIVE");doc.putString("Profile",profile.json());doc.putString("Inputs",defs.document());doc.putString("InputsHash",defs.hash);doc.putInt("Burn",burn);
        doc.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(profile.rules(),core));doc.put("Original",original);doc.put("Materials",provenance);doc.put("MoonSources",moonSources);
        doc.putInt("FuelBalance",fuel-profile.requiredFuel());doc.putInt("FuelReserved",profile.requiredFuel()-profile.opening());doc.putInt("FuelSpent",profile.opening());doc.putInt("FuelInitial",fuel);
        doc.putLong("LastWorldTick",level.getGameTime());doc.putLong("SampleDayTime",sampled.dayTime());doc.putLong("SampleGameTime",sampled.gameTime());inventory(doc,after);if(profile.rules().resources.containsKey(cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.BOUND))doc.putString("EffectPolicy",cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.current(level));BatchIncidents.initialize(doc);validate(doc);return doc;
    }
    public static Snapshot core(CompoundTag doc){var p=profile(doc.getString("Profile"),doc.getInt("Burn"));return SimulationSnapshotCodec.decodeServerSnapshot(p.rules(),doc.getByteArray("Core"));}
    public static void validate(CompoundTag doc){
        if(doc.getInt("Schema")!=1||!doc.hasUUID("Id")||!doc.hasUUID("Owner")||!Set.of("ACTIVE","READY","DELIVERING","CLAIMED").contains(doc.getString("State")))throw new IllegalArgumentException("Unknown V5 transaction");
        if(doc.contains("Entry")&&!Set.of("ordinary_free","ordinary_formula","heavenly").contains(doc.getString("Entry")))throw new IllegalArgumentException("Unknown entry adapter");
        if(doc.getString("Profile").length()>262144||doc.getString("Inputs").length()>262144)throw new IllegalArgumentException("Snapshot definition budget exceeded");
        var defs=new InputDefinitions(doc.getString("Inputs"));if(!defs.hash.equals(doc.getString("InputsHash")))throw new IllegalArgumentException("Input definition drift");
        if(doc.getInt("DeviceWear")<0||doc.getInt("DeviceWear")>1000)throw new IllegalArgumentException("Invalid device wear");
        BatchIncidents.validate(doc);cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.validate(doc);var s=core(doc);if(!s.batchId().equals(doc.getUUID("Id").toString()))throw new IllegalArgumentException("Batch identity mismatch");
        long balance=doc.getInt("FuelBalance"),reserved=doc.getInt("FuelReserved"),spent=doc.getInt("FuelSpent");
        if(balance<0||reserved<0||spent<0||balance+reserved+spent!=doc.getInt("FuelInitial"))throw new IllegalArgumentException("Fuel conservation mismatch");
        if(doc.getString("State").equals("ACTIVE")&&s.status()!=Status.ACTIVE&&s.status()!=Status.PAUSED)throw new IllegalArgumentException("Transaction/core state mismatch");
    }
    public static CompoundTag advance(CompoundTag original,ServerLevel level,BlockPos pos){
        if(original.contains("IncidentVersion"))return BatchIncidents.advance(original,level,pos);
        validate(original);if(!original.getString("State").equals("ACTIVE")||original.getLong("LastWorldTick")>=level.getGameTime())return original;
        var doc=original.copy();var p=profile(doc.getString("Profile"),doc.getInt("Burn"));var s=core(doc);
        if(s.status()!=Status.ACTIVE)return original;
        int desired=0,total=0;for(int duration:p.phases()){total+=duration;if(s.tick()<total)break;desired++;}
        var controls=new ArrayList<InputTrajectory.Control>();if(s.phase().ordinal()<desired)controls.add(new InputTrajectory.Control(Action.ADVANCE,"",0));
        int paid=desired<5?p.burn():0;if(doc.getInt("FuelReserved")<paid)throw new IllegalArgumentException("Reserved fuel exhausted");
        if(paid>0)controls.add(new InputTrajectory.Control(Action.HEAT,"",Math.min(new InputDefinitions(doc.getString("Inputs")).fireMaximumRate(AlchemyInputs.fire(inventory(doc).get(AlchemyFurnaceBlockEntity.IGNITION_SLOT)).id()),(long)paid*10)));
        if(s.tick()==p.steps()-1)controls.add(new InputTrajectory.Control(Action.FINISH,"",0));
        var fire=AlchemyInputs.fire(inventory(doc).get(AlchemyFurnaceBlockEntity.IGNITION_SLOT));var defs=new InputDefinitions(doc.getString("Inputs"));var trajectory=new InputTrajectory(defs,fire);
        var next=trajectory.step(p.rules(),s,level,pos,controls).snapshot();doc.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(p.rules(),next));
        doc.putInt("FuelReserved",doc.getInt("FuelReserved")-paid);doc.putInt("FuelSpent",doc.getInt("FuelSpent")+paid);doc.putLong("LastWorldTick",level.getGameTime());doc.putLong("SampleGameTime",level.getGameTime());doc.putLong("SampleDayTime",level.getDayTime());
        if(next.status()!=Status.ACTIVE&&next.status()!=Status.PAUSED)ready(doc);validate(doc);return doc;
    }
    public static CompoundTag seal(CompoundTag original){var doc=original.copy();var s=core(doc);var p=profile(doc.getString("Profile"),doc.getInt("Burn"));
        if(s.status()==Status.ACTIVE)s=AlchemySimulation.step(p.rules(),s,List.of(new Operation(s.lastSequence()+1,s.tick(),Action.ABORT,"",0))).snapshot();
        doc.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(p.rules(),s));ready(doc);validate(doc);return doc;
    }
    static void ready(CompoundTag doc){doc.putString("State","READY");doc.putInt("FuelBalance",doc.getInt("FuelBalance")+doc.getInt("FuelReserved"));doc.putInt("FuelReserved",0);cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.freeze(doc);}
    public static ItemStack result(CompoundTag doc){var pill=cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.result(doc);if(!pill.isEmpty())return pill;var item=new ItemStack(BatchIncidents.resultItem(doc));item.getOrCreateTag().putUUID(KEY,doc.getUUID("Id"));
        if(doc.contains("IncidentVersion")){var s=core(doc);var p=profile(doc.getString("Profile"),doc.getInt("Burn"));item.getOrCreateTag().putLong("V5MaterialMass",s.ledger().ashMass()+p.rules().weight(s.stocks(),true));item.getOrCreateTag().putString("V5Outcome",s.status().name());}
        return item;}
    private V5Batch(){}
}
