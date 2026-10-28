package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.research.ResearchEvidence;
import cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules;
import cn.blockforge.generated.generatedmod.balance.RealmBalance;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;

/** Explicit one-time legacy entitlement import; never fabricates reaction or certification evidence. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class LegacyPillImport {
    public static final String AUTHORITY="legacy-import/1";
    private static final String POLICY=loadPolicy();
    private static String loadPolicy(){try(var in=LegacyPillImport.class.getResourceAsStream("/data/wuxingxiantu/alchemy/legacy-import.json")){if(in==null)throw new IllegalStateException("Missing legacy import policy");return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}catch(java.io.IOException error){throw new IllegalStateException(error);}}

    public record Plan(CompoundTag original,CompoundTag result){public Plan{original=original.copy();result=result.copy();}@Override public CompoundTag original(){return original.copy();}@Override public CompoundTag result(){return result.copy();}}
    public static Plan plan(ItemStack stack){return plan(stack,POLICY);}
    private static Plan plan(ItemStack stack,String policy){
        if(stack.isEmpty()||PillSnapshot.isV5(stack)||stack.getCount()>64)return null;
        var old=PillData.read(stack);boolean creation=stack.is(WuXingItems.CREATION_PILL.get());if(old==null&&!creation)return null;
        if(stack.hasTag()&&stack.getTag().contains("Grade")&&!Set.of("LOW","MID","HIGH","PERFECT").contains(stack.getTag().getString("Grade").toUpperCase(Locale.ROOT)))throw new IllegalArgumentException("Unknown old pill quality; original retained");
        if(stack.hasTag()){var tag=stack.getTag();if(tag.contains(MedicineTier.KEY)&&(tag.getInt(MedicineTier.KEY)<1||tag.getInt(MedicineTier.KEY)>5)||tag.contains("CultivationTier")&&(tag.getInt("CultivationTier")<0||tag.getInt("CultivationTier")>8)||tag.contains("Element")&&!Set.of("METAL","WOOD","WATER","FIRE","EARTH").contains(tag.getString("Element")))throw new IllegalArgumentException("Malformed old pill fields; original retained");}
        var config=cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRuleLoader.document(policy);if(!config.keySet().equals(Set.of("schema","version","referenceHealth","referenceMana","purity","cooldown"))||config.get("schema").getAsInt()!=1||!config.get("version").getAsString().equals(AUTHORITY))throw new IllegalArgumentException("Unknown import policy");
        if(config.get("referenceHealth").getAsLong()<1||config.get("referenceHealth").getAsLong()>4000||config.get("referenceMana").getAsLong()<1||config.get("referenceMana").getAsLong()>1000000||config.get("cooldown").getAsInt()<1||config.get("cooldown").getAsInt()>1200||config.getAsJsonArray("purity").size()!=4)throw new IllegalArgumentException("Import policy bounds");int prior=250000;for(var value:config.getAsJsonArray("purity")){int n=value.getAsInt();if(n<prior||n>1000000)throw new IllegalArgumentException("Import purity bounds");prior=n;}
        int q=creation?4:old.grade().value(),rank=MedicineTier.of(stack),realm=creation?0:ProgressionGate.requiredRealmForPill(stack),cooldown=config.get("cooldown").getAsInt();
        var effects=new ArrayList<Spec>();long health=config.get("referenceHealth").getAsLong(),mana=config.get("referenceMana").getAsLong();
        if(creation)add(effects,Kind.creation_blessing,1000,0,0,cooldown);
        else switch(old.pill()){
            case AURA->add(effects,Kind.mana,mana*250*q,0,realm,cooldown);
            case GOLDEN_WOUND->add(effects,Kind.heal,health*(50+150*q),0,realm,cooldown);
            case BIGU->add(effects,Kind.bigu,1000,18000*q,realm,cooldown);
            case CLEAR_HEART->{add(effects,Kind.cleanse,1000,0,realm,cooldown);add(effects,Kind.dao_heart_clear,1000,0,realm,cooldown);add(effects,Kind.regeneration,Math.max(50,health*25),100*q,realm,cooldown);}
            case CONTINUE_VEIN->{add(effects,Kind.meridian_repair,1000,0,realm,cooldown);add(effects,Kind.regeneration,Math.max(50,health*25),200*q,realm,cooldown);}
            case GATHERING->add(effects,Kind.gathering,q*1000,1200*q,realm,cooldown);
            case FOUNDATION->add(effects,Kind.root_random,q*1000,0,realm,cooldown);
            case ROOT->add(effects,new Kind[]{Kind.root_metal,Kind.root_wood,Kind.root_water,Kind.root_fire,Kind.root_earth}[old.element().ordinal()],q*1000,0,realm,cooldown);
            case CULTIVATION->add(effects,Kind.cultivation,Math.multiplyExact(AlchemyBalance.cultivationGain(old.cultivationTier(),q),1000L),0,realm,cooldown);
            case BREAKTHROUGH->add(effects,Kind.minor_breakthrough,1000,0,realm,1200);
            case TRIBULATION->add(effects,Kind.tribulation,q*1000,1200*q,realm,cooldown);
            case DIAMOND_BODY->{add(effects,Kind.resistance,Math.min(3,q)*1000,600*q,realm,cooldown);add(effects,Kind.shield,q*4000,600*q,realm,cooldown);}
            case BERSERK->{add(effects,Kind.strength,Math.min(3,q)*1000,450*q,realm,cooldown);add(effects,Kind.speed,200,450*q,realm,cooldown);}
            case GALE->{add(effects,Kind.speed,Math.min(350,200*q),600*q,realm,cooldown);add(effects,Kind.jump,Math.min(2,q)*1000,600*q,realm,cooldown);}
            case PROFOUND_WATER->{add(effects,Kind.water_breathing,1000,1200*q,realm,cooldown);add(effects,Kind.night_vision,1000,1200*q,realm,cooldown);add(effects,Kind.haste,Math.min(2,q)*1000,1200*q,realm,cooldown);}
            case CALMING->add(effects,Kind.calming,1000,0,realm,cooldown);
            case PHYSIQUE->{add(effects,Kind.physique_reset,1000,0,realm,cooldown);add(effects,Kind.regeneration,Math.max(50,health*25),200*q,realm,cooldown);}
            case GATHER_QI->add(effects,Kind.mana_regeneration,Math.max(50,mana*25*q/10),20*60*q*(rank+1)/2,realm,cooldown);
            case REJUVENATION->add(effects,Kind.regeneration,Math.max(50,health*25*q/10),20*30*q*(rank+1)/2,realm,cooldown);
            case ORIGIN_CONDENSING->{long cap=RealmBalance.cultivation(realm,0);add(effects,Kind.origin_reserve,Math.min(Math.max(1,cap/2),Math.max(1,cap/20)*q*rank)*1000,0,realm,cooldown);}
            case MERIDIAN_EXPANSION->add(effects,Kind.meridian_expansion,Math.min(5,q+(rank-1)/2)*1000,0,realm,cooldown);
            case BARRIER_PREPARATION->add(effects,Kind.barrier_preparation,(Math.min(2,q-1+(rank-1)/2)+1)*4000,1200*rank,realm,cooldown);
        }
        if(!creation){
            if(stack.hasTag()&&stack.getTag().contains(cn.blockforge.generated.generatedmod.importedcreatures.BeastMaterialEffects.STONE)){var stone=stack.getTag().getCompound(cn.blockforge.generated.generatedmod.importedcreatures.BeastMaterialEffects.STONE);add(effects,Kind.resistance,(Math.max(0,Math.min(2,stone.getInt("Amplifier")))+1)*1000,Math.max(20,Math.min(7200,stone.getInt("Duration"))),realm,cooldown);}
            var auxiliary=cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.read(stack);for(var injection:auxiliary.injections()){int tierRealm=Math.max(realm,injection.tier()-1),strength=Math.max(1,Math.min(3,injection.amplifier()+1)),time=injection.duration();switch(injection.essence()){
                case SPIRIT->add(effects,Kind.mana_regeneration,mana*25*strength/10,time,tierRealm,cooldown);
                case ORIGIN->add(effects,Kind.origin_reserve,1000L*strength*injection.tier(),0,tierRealm,cooldown);
                case VITALITY,WOOD->{add(effects,Kind.regeneration,health*25*strength/10,time,tierRealm,cooldown);if(injection.essence()==cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Essence.WOOD)add(effects,Kind.deep_cleanse,1000,0,tierRealm,cooldown);}
                case PURITY->{add(effects,Kind.deep_cleanse,1000,0,tierRealm,cooldown);add(effects,Kind.resistance,strength*1000,time,tierRealm,cooldown);}
                case BREAK,FLAME,THUNDER->add(effects,Kind.strength,strength*1000,time,tierRealm,cooldown);
                case FROST,STONE->add(effects,Kind.resistance,strength*1000,time,tierRealm,cooldown);
                case METAL->add(effects,Kind.shield,strength*4000,time,tierRealm,cooldown);
                case WIND->add(effects,Kind.speed,Math.min(350,strength*200),time,tierRealm,cooldown);
                case ILLUSION->add(effects,Kind.invisibility,1000,time,tierRealm,cooldown);
                case FORTUNE->add(effects,Kind.luck,strength*1000,time,tierRealm,cooldown);
                case ABUNDANCE->add(effects,Kind.nutrition,1000,0,tierRealm,cooldown);
                case VENOM->{}
            }if(injection.essence()==cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Essence.FROST)add(effects,Kind.frost_strike,1000,time,tierRealm,cooldown);if(injection.essence()==cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Essence.FLAME)add(effects,Kind.flame_strike,1000,time,tierRealm,cooldown);if(injection.essence()==cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Essence.VENOM)add(effects,Kind.venom_strike,1000,time,tierRealm,cooldown);}
            if(auxiliary.clash())add(effects,Kind.confusion,1000,200,realm,cooldown);if(auxiliary.backlash())add(effects,Kind.poison,1000,100,realm,cooldown);
        }
        int purity=creation?1000000:config.getAsJsonArray("purity").get(q-1).getAsInt();double utility=0;for(var spec:effects)utility=Math.max(utility,spec.magnitudeMilli()/(double)(spec.id()==Kind.cultivation?Math.max(10000,Math.min(maximum(Kind.cultivation),AlchemyBalance.cultivationGain(old.cultivationTier(),4)*1000L)):spec.id()==Kind.origin_reserve?Math.max(1000,Math.min(maximum(Kind.origin_reserve),RealmBalance.cultivation(realm,0)/2*1000)):maximum(spec.id())));int potency=(int)Math.min(1000000,Math.floor(utility*1000000));int score=(int)((48L*potency+52L*purity)/100);int grade=grade(score,purity,purity,1000000,0,potency,true);if(grade==0)throw new IllegalArgumentException("No valid V5 import result");
        var result=new Result(grade,stack.getCount(),stack.getCount(),0,1000000,effects.size(),purity,purity,1000000,0,potency,score,hash(policy),effects);
        return new Plan(stack.save(new CompoundTag()),PillSnapshot.write(result,hash(policy),AUTHORITY,"v5-import/1"));
    }
    private static void add(List<Spec> out,Kind kind,long magnitude,int duration,int realm,int cooldown){magnitude=Math.max(minimum(kind),Math.min(maximum(kind),magnitude));if(kind==Kind.shield||kind==Kind.barrier_preparation)magnitude=magnitude/4000*4000;else if(quantized(kind))magnitude=magnitude/1000*1000;for(int i=0;i<out.size();i++){var old=out.get(i);if(old.id()==kind){out.set(i,new Spec(kind,Math.max(old.magnitudeMilli(),magnitude),Math.max(old.duration(),Math.max(0,Math.min(72000,duration))),20,1,Math.max(old.cooldown(),cooldown),Math.max(old.realm(),realm)));return;}}if(out.size()>=9)throw new IllegalArgumentException("Old effect set exceeds nine modules; retain original for review");out.add(new Spec(kind,magnitude,Math.max(0,Math.min(72000,duration)),20,1,cooldown,realm));}
    public static ItemStack preview(ItemStack original){var plan=plan(original);if(plan==null)return ItemStack.EMPTY;var copy=new ItemStack(WuXingItems.V5_PILL.get(),original.getCount());copy.getOrCreateTag().put(PillSnapshot.KEY,plan.result());return copy;}
    public static String fingerprint(CompoundTag original){var copy=original.copy();var tag=copy.getCompound("tag");tag.remove("V5LegacyOriginal");if(tag.isEmpty())copy.remove("tag");return ResearchEvidence.hash(copy);}
    public static void validate(CompoundTag doc){if(!doc.getString("Authority").equals(AUTHORITY)||!doc.hasUUID("Id")||!doc.hasUUID("Owner")||!doc.getString("State").equals("CLAIMED"))throw new IllegalArgumentException("Invalid import authority");var source=ItemStack.of(doc.getCompound("Original"));var expected=plan(source,doc.getString("Policy"));if(expected==null||!expected.result().equals(doc.getCompound("PillResult"))||!fingerprint(doc.getCompound("Original")).equals(doc.getString("SourceHash"))||!ResearchEvidence.hash(doc.getCompound("Original")).equals(doc.getString("ArchiveHash")))throw new IllegalArgumentException("Import provenance mismatch");int quantity=PillSnapshot.read(doc.getCompound("PillResult")).quantity();if(doc.getInt("DosesUsed")<0||doc.getInt("DosesUsed")>quantity)throw new IllegalArgumentException("Import dose budget");if(doc.contains("DoseIntent")){var intent=doc.getCompound("DoseIntent");if(!intent.hasUUID("Id")||!intent.hasUUID("Recipient")||intent.getInt("Dose")!=doc.getInt("DosesUsed")+1||intent.getInt("Dose")>quantity)throw new IllegalArgumentException("Import dose intent");}}
    public static boolean ensure(ServerPlayer player,int slot){
        if(slot<0||slot>=player.getInventory().getContainerSize())return false;var source=player.getInventory().getItem(slot);if(PillSnapshot.isV5(source)){var journal=new BatchJournal(player.server);var pending=journal.importIntent(player.getUUID(),slot);if(!pending.isEmpty()&&source.hasTag()&&source.getTag().hasUUID(V5Batch.KEY)&&source.getTag().getUUID(V5Batch.KEY).equals(pending.getUUID("Id"))){player.server.getPlayerList().save(player);journal.clearImportIntent(player.getUUID(),slot);}return true;}var plan=plan(source);if(plan==null)return false;var journal=new BatchJournal(player.server);var pending=journal.importIntent(player.getUUID(),slot);String fingerprint=fingerprint(plan.original());
        if(pending.isEmpty()){pending=new CompoundTag();pending.putUUID("Id",UUID.randomUUID());pending.putString("SourceHash",fingerprint);pending.put("Original",plan.original());pending.putString("Policy",POLICY);journal.importIntent(player.getUUID(),slot,pending);}else if(!pending.getString("SourceHash").equals(fingerprint))throw new IllegalStateException("Old import slot changed; retain intent for review");
        plan=plan(source,pending.getString("Policy"));UUID id=pending.getUUID("Id");var doc=journal.imported(id);if(doc.isEmpty()){doc=new CompoundTag();doc.putString("Authority",AUTHORITY);doc.putUUID("Id",id);doc.putUUID("Owner",player.getUUID());doc.putString("State","CLAIMED");doc.put("Original",pending.getCompound("Original").copy());doc.putString("SourceHash",fingerprint);doc.putString("ArchiveHash",ResearchEvidence.hash(doc.getCompound("Original")));doc.putString("Policy",pending.getString("Policy"));doc.put("PillResult",plan.result());journal.imported(id,doc);}else validate(doc);
        var converted=PillSnapshot.result(doc);if(source.hasTag()){var retained=source.getTag().copy();for(String key:List.of("Grade",MedicineTier.KEY,"Element","CultivationTier","V5LegacyOriginal"))retained.remove(key);retained.merge(converted.getOrCreateTag());converted.setTag(retained);}converted.getOrCreateTag().putBoolean("V5Imported",true);player.getInventory().setItem(slot,converted);player.getInventory().setChanged();player.server.getPlayerList().save(player);
        // Keep the intent until a saved player snapshot demonstrably contains the same authority.
        var file=player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("playerdata").resolve(player.getUUID()+".dat");try{var saved=NbtIo.readCompressed(file.toFile());boolean durable=false;if(saved!=null)for(var t:saved.getList("Inventory",Tag.TAG_COMPOUND)){var row=(CompoundTag)t;if((row.getByte("Slot")&255)==(slot<36?slot:slot<40?slot+64:150)&&row.getCompound("tag").hasUUID(V5Batch.KEY)&&row.getCompound("tag").getUUID(V5Batch.KEY).equals(id))durable=true;}if(!durable)throw new IllegalStateException("Imported inventory not durable; intent retained");}catch(java.io.IOException e){throw new IllegalStateException("Cannot confirm imported inventory",e);}journal.clearImportIntent(player.getUUID(),slot);return true;
    }
    @SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p))return;var journal=new BatchJournal(p.server);for(int slot=0;slot<p.getInventory().getContainerSize();slot++)try{var pending=journal.importIntent(p.getUUID(),slot);if(pending.isEmpty())continue;UUID id=pending.getUUID("Id");boolean alreadySaved=false;for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(stack.hasTag()&&stack.getTag().hasUUID(V5Batch.KEY)&&stack.getTag().getUUID(V5Batch.KEY).equals(id))alreadySaved=true;}if(alreadySaved){p.server.getPlayerList().save(p);journal.clearImportIntent(p.getUUID(),slot);}else ensure(p,slot);}catch(RuntimeException error){com.mojang.logging.LogUtils.getLogger().error("Import recovery retained at slot {}",slot,error);}}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer p)||p.tickCount%20!=0)return;int remaining=4;for(int slot=0;slot<p.getInventory().getContainerSize()&&remaining>0;slot++)try{if(!PillSnapshot.isV5(p.getInventory().getItem(slot))&&plan(p.getInventory().getItem(slot))!=null){ensure(p,slot);remaining--;}}catch(RuntimeException error){com.mojang.logging.LogUtils.getLogger().error("Old pill retained or imported intent retained at slot {}",slot,error);break;}}
    private LegacyPillImport(){}
}
