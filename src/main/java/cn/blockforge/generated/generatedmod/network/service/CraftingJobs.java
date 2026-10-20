package cn.blockforge.generated.generatedmod.network.service;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Position-owned persistent transactions. Owner/deadline indexes are rebuilt after loading. */
public final class CraftingJobs extends SavedData {
    public enum Kind { ALCHEMY, REFINING }
    public enum Status { RUNNING, READY }
    public record JobKey(ResourceKey<Level> dimension,BlockPos pos) {
        public JobKey { pos=pos.immutable(); }
    }
    record WorkKey(Kind kind,JobKey key) {}
    public record Due(Kind kind,JobKey key,long time) {}
    public record PendingAlchemy(UUID owner,ResourceKey<Level> dimension,BlockPos furnacePos,long finishTick,ItemStack result,
            UUID requestId,UUID bottleId,int fireTier,double[] essence,ResourceLocation formula,double multiplier,int aura) {
        public JobKey key(){return new JobKey(dimension,furnacePos);}
        public Status status(long now){return now<finishTick?Status.RUNNING:Status.READY;}
    }
    public record PendingRefining(UUID owner,ResourceKey<Level> dimension,BlockPos corePos,long finishTick,ItemStack result,
            List<ItemStack> materials,int aura) {
        public JobKey key(){return new JobKey(dimension,corePos);}
        public Status status(long now){return now<finishTick?Status.RUNNING:Status.READY;}
    }
    private final Map<JobKey,PendingAlchemy> alchemy=new HashMap<>();
    private final Map<JobKey,PendingRefining> refining=new HashMap<>();
    private final Map<UUID,Set<WorkKey>> owners=new HashMap<>();
    private final Map<WorkKey,Due> scheduled=new HashMap<>();
    private final NavigableSet<Due> due=new TreeSet<>(Comparator.comparingLong(Due::time)
            .thenComparing(d->d.kind().ordinal()).thenComparing(d->d.key().dimension().location().toString())
            .thenComparingLong(d->d.key().pos().asLong()));
    public final Map<UUID,Float> refunds=new HashMap<>();
    public static CraftingJobs get(MinecraftServer server){
        return server.overworld().getDataStorage().computeIfAbsent(CraftingJobs::load,CraftingJobs::new,"wuxingxiantu_crafting_jobs");
    }
    public PendingAlchemy alchemy(JobKey key){return alchemy.get(key);}
    public PendingRefining refining(JobKey key){return refining.get(key);}
    public void add(PendingAlchemy job) {
        if(alchemy.putIfAbsent(job.key(),job)!=null)throw new IllegalStateException("Alchemy position occupied");
        index(job.owner(),Kind.ALCHEMY,job.key(),job.finishTick());setDirty();
    }
    public void add(PendingRefining job) {
        if(refining.putIfAbsent(job.key(),job)!=null)throw new IllegalStateException("Refining position occupied");
        index(job.owner(),Kind.REFINING,job.key(),job.finishTick());setDirty();
    }
    /** One optional coolant joins the same saved job escrow as recipe inputs. */
    public boolean reserveRefiningQuench(JobKey key,UUID owner,ItemStack held) {
        var job=refining.get(key);
        if(job==null||!job.owner().equals(owner)||held.isEmpty()
                ||!held.is(cn.blockforge.generated.generatedmod.WuXingItems.QUENCH_ESSENCE.get())
                ||!cn.blockforge.generated.generatedmod.artifacts.ArtifactData.artifact(job.result())
                ||cn.blockforge.generated.generatedmod.artifacts.ArtifactData.quenchConduct(job.result())!=0)return false;
        var result=job.result().copy();
        cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.write(result).putInt("ArtifactQuenchConduct",3);
        var materials=new ArrayList<ItemStack>();for(var item:job.materials())materials.add(item.copy());
        materials.add(held.copyWithCount(1));
        // Keep the original owner, deadline and indexes; only the escrow/result change.
        refining.put(key,new PendingRefining(job.owner(),job.dimension(),job.corePos(),job.finishTick(),result,materials,job.aura()));
        held.shrink(1);setDirty();return true;
    }
    private void index(UUID owner,Kind kind,JobKey key,long time) {
        owners.computeIfAbsent(owner,id->new HashSet<>()).add(new WorkKey(kind,key));
        schedule(kind,key,time);
    }
    public void schedule(Kind kind,JobKey key,long time) {
        if(kind==Kind.ALCHEMY?!alchemy.containsKey(key):!refining.containsKey(key))return;
        var work=new WorkKey(kind,key);var next=new Due(kind,key,time);var old=scheduled.put(work,next);
        if(old!=null)due.remove(old);due.add(next);
    }
    public Due poll(long now) {
        if(due.isEmpty()||due.first().time()>now)return null;
        var entry=due.pollFirst();scheduled.remove(new WorkKey(entry.kind(),entry.key()));return entry;
    }
    public void remove(Kind kind,JobKey key) {
        UUID owner;
        if(kind==Kind.ALCHEMY){var job=alchemy.remove(key);if(job==null)return;owner=job.owner();}
        else {var job=refining.remove(key);if(job==null)return;owner=job.owner();}
        var work=new WorkKey(kind,key);var old=scheduled.remove(work);if(old!=null)due.remove(old);
        var index=owners.get(owner);if(index!=null){index.remove(work);if(index.isEmpty())owners.remove(owner);}
        setDirty();
    }
    public int activeCount(UUID owner,Kind kind,long now) {
        int count=0;
        for(var work:owners.getOrDefault(owner,Set.of())) {
            if(work.kind()!=kind)continue;
            long finish=kind==Kind.ALCHEMY?alchemy.get(work.key()).finishTick():refining.get(work.key()).finishTick();
            if(finish>now)count++;
        }
        return count;
    }
    public String describe(UUID owner,long now) {
        var text=new StringBuilder();
        for(var work:owners.getOrDefault(owner,Set.of())) {
            long finish=work.kind()==Kind.ALCHEMY?alchemy.get(work.key()).finishTick():refining.get(work.key()).finishTick();
            text.append(work.kind()).append(" ").append(work.key().dimension().location()).append(" ")
                    .append(work.key().pos().toShortString()).append(finish>now?" 剩余 "+((finish-now+19)/20)+" 秒":" 已到期，等待原区块结算").append("; ");
        }
        return text.toString();
    }
    public void retryOwner(UUID owner,long now) {
        for(var work:owners.getOrDefault(owner,Set.of()))schedule(work.kind(),work.key(),now);
    }
    public void refund(UUID owner,int amount){if(amount>0){refunds.merge(owner,(float)amount,Float::sum);setDirty();}}
    private static CompoundTag base(UUID owner,ResourceKey<Level> dimension,BlockPos pos,long finish,ItemStack result,int aura){
        var tag=new CompoundTag();tag.putUUID("Owner",owner);tag.putString("Dimension",dimension.location().toString());
        tag.putLong("Pos",pos.asLong());tag.putLong("Finish",finish);tag.put("Result",result.save(new CompoundTag()));tag.putInt("Aura",aura);return tag;
    }
    @Override public CompoundTag save(CompoundTag tag){
        tag.putInt("Schema",2);
        var brews=new ListTag();
        alchemy.values().forEach(j->{
            var t=base(j.owner(),j.dimension(),j.furnacePos(),j.finishTick(),j.result(),j.aura());
            t.putUUID("Request",j.requestId());t.putUUID("Bottle",j.bottleId());t.putInt("FireTier",j.fireTier());
            var essence=new ListTag();for(double value:j.essence())essence.add(DoubleTag.valueOf(value));t.put("Essence",essence);
            t.putString("Formula",j.formula().toString());t.putDouble("Multiplier",j.multiplier());brews.add(t);
        });tag.put("Alchemy",brews);
        var forges=new ListTag();
        refining.values().forEach(j->{
            var t=base(j.owner(),j.dimension(),j.corePos(),j.finishTick(),j.result(),j.aura());
            var materials=new ListTag();for(var stack:j.materials())materials.add(stack.save(new CompoundTag()));
            t.put("Materials",materials);forges.add(t);
        });tag.put("Refining",forges);
        var refundTags=new ListTag();refunds.forEach((owner,amount)->{var t=new CompoundTag();t.putUUID("Owner",owner);t.putFloat("Aura",amount);refundTags.add(t);});
        tag.put("Refunds",refundTags);return tag;
    }
    private static ResourceKey<Level> dimension(CompoundTag tag){
        return ResourceKey.create(Registries.DIMENSION,new ResourceLocation(tag.getString("Dimension")));
    }
    public static CraftingJobs load(CompoundTag tag){
        var data=new CraftingJobs();
        // V1 already stored owner/dimension/position per entry; rebuilding keys migrates it without losing jobs.
        for(var element:tag.getList("Alchemy",Tag.TAG_COMPOUND)){
            var t=(CompoundTag)element;var list=t.getList("Essence",Tag.TAG_DOUBLE);double[] essence=new double[list.size()];
            for(int i=0;i<essence.length;i++)essence[i]=list.getDouble(i);
            data.add(new PendingAlchemy(t.getUUID("Owner"),dimension(t),BlockPos.of(t.getLong("Pos")),t.getLong("Finish"),
                    ItemStack.of(t.getCompound("Result")),t.getUUID("Request"),t.getUUID("Bottle"),t.getInt("FireTier"),essence,
                    new ResourceLocation(t.getString("Formula")),t.getDouble("Multiplier"),t.getInt("Aura")));
        }
        for(var element:tag.getList("Refining",Tag.TAG_COMPOUND)){
            var t=(CompoundTag)element;var materials=new ArrayList<ItemStack>();
            for(var entry:t.getList("Materials",Tag.TAG_COMPOUND))materials.add(ItemStack.of((CompoundTag)entry));
            data.add(new PendingRefining(t.getUUID("Owner"),dimension(t),BlockPos.of(t.getLong("Pos")),t.getLong("Finish"),
                    ItemStack.of(t.getCompound("Result")),materials,t.getInt("Aura")));
        }
        for(var element:tag.getList("Refunds",Tag.TAG_COMPOUND)){var t=(CompoundTag)element;data.refunds.put(t.getUUID("Owner"),t.getFloat("Aura"));}
        return data;
    }
}
