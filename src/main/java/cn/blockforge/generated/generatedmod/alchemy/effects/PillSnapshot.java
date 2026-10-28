package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import cn.blockforge.generated.generatedmod.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;

/** Pinned compiler policy and immutable result. Only the public result, never seeds, enters an item. */
public final class PillSnapshot {
    public static final String KEY="V5Pill";
    public static boolean isV5(ItemStack stack){return stack.is(WuXingItems.V5_PILL.get())||stack.hasTag()&&stack.getTag().contains(KEY);}
    public static String current(ServerLevel level){
        try(var r=level.getServer().getResourceManager().openAsReader(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","alchemy/effects.json"))){
            var b=new StringBuilder();char[] part=new char[4096];int n;while((n=r.read(part))!=-1){b.append(part,0,n);if(b.length()>32768)throw new IllegalArgumentException("Compiler policy budget");}return new Policy(b.toString()).json;
        }catch(java.io.IOException e){throw new IllegalStateException("Missing compiler policy",e);}
    }
    public static void freeze(CompoundTag doc){
        if(!doc.contains("EffectPolicy")||doc.contains("PillResult"))return;
        var p=V5Batch.profile(doc.getString("Profile"),doc.getInt("Burn"));var core=V5Batch.core(doc);var policy=new Policy(doc.getString("EffectPolicy"));
        var result=AbilityCompiler.compile(policy,p.rules(),core,doc.getInt("StructureDamage"));doc.put("PillResult",write(result,p.rules().hash,p.rules().version,policy.version));
        if(result.grade()>0){doc.putByteArray("ReactionCore",doc.getByteArray("Core"));doc.putByteArray("Core",SimulationSnapshotCodec.encodeServerSnapshot(p.rules(),AbilityCompiler.bind(p.rules(),core,result)));}
    }
    public static void validate(CompoundTag doc){
        if(!doc.contains("EffectPolicy")){if(doc.contains("PillResult")||doc.contains("ReactionCore")||doc.contains("DoseIntent"))throw new IllegalArgumentException("Unbound pill result");return;}
        var policy=new Policy(doc.getString("EffectPolicy"));var p=V5Batch.profile(doc.getString("Profile"),doc.getInt("Burn"));policy.validateResources(p.rules());
        if(!doc.contains("PillResult")){if(!doc.getString("State").equals("ACTIVE")||doc.contains("DoseIntent")||doc.getInt("DosesUsed")!=0)throw new IllegalArgumentException("Missing frozen result");return;}
        if(!doc.getCompound("PillResult").getString("SourceRules").equals(p.rules().hash)||!doc.getCompound("PillResult").getString("SourceVersion").equals(p.rules().version))throw new IllegalArgumentException("Result source mismatch");
        if(!doc.getCompound("PillResult").getString("CompilerVersion").equals(policy.version))throw new IllegalArgumentException("Compiler identity drift");var saved=read(doc.getCompound("PillResult"));var source=doc.contains("ReactionCore")?SimulationSnapshotCodec.decodeServerSnapshot(p.rules(),doc.getByteArray("ReactionCore")):V5Batch.core(doc);
        var expected=AbilityCompiler.compile(policy,p.rules(),source,doc.getInt("StructureDamage"));if(!saved.equals(expected))throw new IllegalArgumentException("Compiled result drift");
        if(saved.grade()>0&&!AbilityCompiler.bind(p.rules(),source,expected).equals(V5Batch.core(doc)))throw new IllegalArgumentException("Pill allocation drift");
        if(doc.getInt("DosesUsed")<0||doc.getInt("DosesUsed")>saved.quantity())throw new IllegalArgumentException("Dose budget exceeded");
        if(doc.contains("DoseIntent")){var intent=doc.getCompound("DoseIntent");if(!intent.hasUUID("Id")||!intent.hasUUID("Recipient")||intent.getInt("Dose")!=doc.getInt("DosesUsed")+1||intent.getInt("Dose")>saved.quantity())throw new IllegalArgumentException("Invalid dose intent");}
    }
    public static ItemStack result(CompoundTag doc){
        if(!doc.contains("PillResult"))return ItemStack.EMPTY;var compiled=read(doc.getCompound("PillResult"));
        if(compiled.grade()==0)return ItemStack.EMPTY;
        var item=new ItemStack(WuXingItems.V5_PILL.get(),compiled.quantity());item.getOrCreateTag().putUUID(V5Batch.KEY,doc.getUUID("Id"));item.getOrCreateTag().put(KEY,doc.getCompound("PillResult").copy());return item;
    }
    public static boolean matches(ItemStack stack,CompoundTag doc){return stack.is(WuXingItems.V5_PILL.get())&&stack.hasTag()&&stack.getTag().hasUUID(V5Batch.KEY)&&doc.hasUUID("Id")&&stack.getTag().getUUID(V5Batch.KEY).equals(doc.getUUID("Id"))&&doc.contains("PillResult")&&stack.getTag().getCompound(KEY).equals(doc.getCompound("PillResult"))&&read(doc.getCompound("PillResult")).grade()>0;}
    public static CompoundTag write(Result r,String rulesHash,String rulesVersion){
        return write(r,rulesHash,rulesVersion,"v5-effects/1");}
    public static CompoundTag write(Result r,String rulesHash,String rulesVersion,String compiler){
        var n=new CompoundTag();n.putString("Family",r.family());n.putString("CompilerVersion",compiler);n.putString("EngineVersion",SimulationRules.ENGINE);n.putString("SourceRules",rulesHash);n.putString("SourceVersion",rulesVersion);n.putInt("Schema",1);n.putInt("Grade",r.grade());n.putInt("Quantity",r.quantity());n.putLong("BoundMass",r.boundMass());n.putLong("ResidueMass",r.residualMass());n.putLong("Budget",r.budget());n.putLong("Spent",r.spent());n.putInt("Purity",r.purity());n.putInt("Stability",r.stability());n.putInt("Structure",r.structure());n.putInt("Toxicity",r.toxicity());n.putInt("Potency",r.potency());n.putInt("Score",r.score());n.putString("PolicyHash",r.policyHash());var list=new ListTag();
        for(var e:r.effects()){var s=new CompoundTag();s.putString("Module",e.id().name());s.putLong("MagnitudeMilli",e.magnitudeMilli());s.putInt("Duration",e.duration());s.putInt("Interval",e.interval());s.putLong("Cost",e.cost());s.putInt("Cooldown",e.cooldown());s.putInt("Realm",e.realm());list.add(s);}n.put("Effects",list);return n;
    }
    public static Result read(CompoundTag n){
        if(!n.getAllKeys().equals(Set.of("Family","CompilerVersion","EngineVersion","SourceRules","SourceVersion","Schema","Grade","Quantity","BoundMass","ResidueMass","Budget","Spent","Purity","Stability","Structure","Toxicity","Potency","Score","PolicyHash","Effects"))||!Set.of("v5-effects/1","v5-effects/2","v5-import/1").contains(n.getString("CompilerVersion"))||!n.getString("EngineVersion").equals(SimulationRules.ENGINE)||n.getString("SourceRules").length()!=64||n.getString("SourceVersion").length()>96||n.getInt("Schema")!=1||n.getString("PolicyHash").length()!=64)throw new IllegalArgumentException("Unknown pill schema");
        var effects=new ArrayList<Spec>();var list=n.getList("Effects",Tag.TAG_COMPOUND);if(list.size()>9)throw new IllegalArgumentException("Effect budget");
        for(var entry:list){var e=(CompoundTag)entry;if(!e.getAllKeys().equals(Set.of("Module","MagnitudeMilli","Duration","Interval","Cost","Cooldown","Realm")))throw new IllegalArgumentException("Unknown effect fields");var kind=Kind.valueOf(e.getString("Module"));if(n.getString("CompilerVersion").equals("v5-effects/1")&&kind.ordinal()>Kind.minor_breakthrough.ordinal()||!n.getString("CompilerVersion").equals("v5-import/1")&&e.getInt("Duration")>12000)throw new IllegalArgumentException("Effect version bounds");effects.add(new Spec(kind,e.getLong("MagnitudeMilli"),e.getInt("Duration"),e.getInt("Interval"),e.getLong("Cost"),e.getInt("Cooldown"),e.getInt("Realm")));}
        var result=new Result(n.getInt("Grade"),n.getInt("Quantity"),n.getLong("BoundMass"),n.getLong("ResidueMass"),n.getLong("Budget"),n.getLong("Spent"),n.getInt("Purity"),n.getInt("Stability"),n.getInt("Structure"),n.getInt("Toxicity"),n.getInt("Potency"),n.getInt("Score"),n.getString("PolicyHash"),effects);if(!result.family().equals(n.getString("Family")))throw new IllegalArgumentException("Family mismatch");return result;
    }
    public static Result view(ItemStack item){try{return item.hasTag()?read(item.getTag().getCompound(KEY)):null;}catch(RuntimeException ignored){return null;}}
    public static Component name(ItemStack stack){var r=view(stack);if(r==null||r.grade()==0)return Component.literal("未认证灵丹（不可服用）");String[] names={"一","二","三","四","五","六","七","八","九"};return Component.literal(names[r.grade()-1]+"品 · "+(r.effects().size()==1?moduleName(r.effects().get(0).id()):"复合灵丹")).withStyle(s->s.withColor(new int[]{0xD8DFD7,0x99D7AF,0x74CACC,0x79ACE9,0xA697E4,0xC28BDD,0xE8BA6A,0xF4CD79,0xFFE7A6}[r.grade()-1]));}
    public static String moduleName(Kind k){return switch(k){case heal->"回春灵丹";case mana->"复灵丹";case regeneration->"温养灵丹";case speed->"轻身灵丹";case resistance->"护体灵丹";case shield->"金身灵丹";case cleanse->"清毒灵丹";case cultivation->"修炼灵丹";case minor_breakthrough->"小境破障丹";default->ExtendedPillEffects.name(k);};}
    public static void tooltip(ItemStack stack,List<Component> lines){var r=view(stack);if(r==null){lines.add(Component.literal("缺少服务端批次凭证，不能服用。"));return;}lines.add(Component.literal("纯度 "+r.purity()/10000D+"% · 稳定 "+r.stability()/10000D+"% · 丹毒 "+r.toxicity()/10000D+"%"));for(var e:r.effects())lines.add(Component.literal(describe(e)+(e.duration()>0?" · "+e.duration()/20D+"秒":"")+" · 预算 "+e.cost()));lines.add(Component.literal("本批 "+r.quantity()+"颗；每颗预算 "+r.spent()+" / "+r.budget()+"。"));lines.add(Component.literal("共用冷却 "+r.effects().stream().mapToInt(Spec::cooldown).max().orElse(20)/20D+"秒；同类效果有上限。"));lines.add(Component.literal("最低境界："+cn.blockforge.generated.generatedmod.items.CultivationLogic.realmName(ProgressionGate.requiredRealmForV5(r))));}
    private static String describe(Spec e){double amount=e.magnitudeMilli()/1000D;return switch(e.id()){
        case heal->"恢复 "+amount+" 生命";case mana->"恢复 "+amount+" 灵力";case regeneration->"每 "+e.interval()/20D+"秒恢复 "+amount+" 生命";
        case speed->"基础移速 +"+amount*100+"%";case resistance->"原版抗性 "+amount+" 级";case shield->"吸收护盾 "+amount+" 生命";
        case cleanse->"清除中毒、虚弱与反胃";case cultivation->"基础修为 "+amount+"（遵守既有规则与上限）";case minor_breakthrough->"尝试既有小境界突破，不跳过资格";default->ExtendedPillEffects.describe(e);};}
    private PillSnapshot(){}
}
