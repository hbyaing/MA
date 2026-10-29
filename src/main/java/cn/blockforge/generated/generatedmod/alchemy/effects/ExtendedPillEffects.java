package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.MedicineProgress;
import cn.blockforge.generated.generatedmod.items.*;
import cn.blockforge.generated.generatedmod.mindscape.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;

/** V5 effect services consume frozen specifications, never old quality/rank ordinals. */
public final class ExtendedPillEffects {
    public static MobEffect nativeEffect(Kind k){return switch(k){
        case gathering->ModEffects.PILL_GATHERING.get();case strength->MobEffects.DAMAGE_BOOST;case jump->MobEffects.JUMP;
        case water_breathing->MobEffects.WATER_BREATHING;case night_vision->MobEffects.NIGHT_VISION;case haste->MobEffects.DIG_SPEED;
        case tribulation->ModEffects.PILL_TRIBULATION.get();case invisibility->MobEffects.INVISIBILITY;case luck->MobEffects.LUCK;case poison->MobEffects.POISON;case confusion->MobEffects.CONFUSION;default->null;};}
    public static String nativePrefix(MobEffect effect){if(effect==MobEffects.ABSORPTION)return "Shield";if(effect==MobEffects.DAMAGE_RESISTANCE)return "Resistance";for(var k:Kind.values())if(nativeEffect(k)==effect)return "Native_"+k.name();return null;}
    public static boolean negative(Kind k){return k==Kind.poison||k==Kind.confusion;}
    public static int root(Kind k){return switch(k){case root_metal->0;case root_wood->1;case root_water->2;case root_fire->3;case root_earth->4;default->-1;};}
    public static boolean applicable(ServerPlayer p,Spec e){var d=GeneratedMod.data(p);if(d==null)return false;if(PillWorldEffects.replaying(p,e))return true;var n=MedicineProgress.state(p);int root=root(e.id());if(root>=0)return (d.wuxingxiantu$getRootMask()&(1<<root))==0||d.wuxingxiantu$getRootQuality(root)<e.magnitudeMilli()/1000;
        if(nativeEffect(e.id())!=null){if(e.id()==Kind.tribulation&&!TribulationManager.isActive(p))return false;var current=p.getEffect(nativeEffect(e.id()));return current==null||!current.isInfiniteDuration()&&(current.getAmplifier()+1<e.magnitudeMilli()/1000||current.getDuration()<e.duration());}
        return switch(e.id()){
            case bigu->d.wuxingxiantu$getRealm()==0&&d.wuxingxiantu$getBiguRemainTime()<e.duration();
            case dao_heart_clear->d.wuxingxiantu$getDaoXin()!=0&&!d.wuxingxiantu$isDaoHeartUnstable();
            case meridian_repair->d.wuxingxiantu$getMeridianStatus()!=0||p.hasEffect(MobEffects.WEAKNESS)||p.hasEffect(ModEffects.MERIDIAN_DAMAGED.get())||p.hasEffect(ModEffects.MERIDIAN_SHATTERED.get());
            case calming->{var next=MindscapeProgression.chooseNext(p);yield !d.wuxingxiantu$isDaoHeartUnstable()&&p.level().dimension()!=MindscapeManager.DIMENSION&&!d.wuxingxiantu$getMindscapeData().hasUUID("session")&&(next!=null&&next.requiredRealm()<=d.wuxingxiantu$getRealm()||d.wuxingxiantu$getMindscapeData().getBoolean("final_completed"));}
            case root_random->{boolean useful=false;for(int i=0;i<5;i++)useful|=d.wuxingxiantu$getRootQuality(i)<e.magnitudeMilli()/1000;yield useful;}
            case physique_reset,frost_strike,flame_strike,venom_strike->true;
            case creation_blessing->((CreationPillItem)WuXingItems.CREATION_PILL.get()).canBenefit(p,d);
            case nutrition->p.getFoodData().getFoodLevel()<20||p.getFoodData().getSaturationLevel()<20;
            case deep_cleanse->List.of(MobEffects.POISON,MobEffects.CONFUSION,MobEffects.HUNGER,MobEffects.WITHER,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.DIG_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.BLINDNESS).stream().anyMatch(p::hasEffect);
            case mana_regeneration->!PillRuntime.activeManaRegeneration(p);
            case origin_reserve->n.getLong("OriginPower")==0&&MindscapeManager.canGain(p,d,false)&&d.wuxingxiantu$getCultivation()<CultivationLogic.breakthroughRequirement(d);
            case meridian_expansion->n.getInt("ExpansionRealm"+d.wuxingxiantu$getRealm())<2&&d.wuxingxiantu$getAuraBonus()<25;
            case barrier_preparation->d.wuxingxiantu$getCultivationStage()==3&&d.wuxingxiantu$getRealm()<8&&!d.wuxingxiantu$isDaoHeartUnstable()&&!TribulationManager.isActive(p)&&n.getLong("V5PreparedShield")<e.magnitudeMilli();
            default->false;
        };
    }
    public static void apply(ServerPlayer p,Spec e){if(!applicable(p,e))return;var d=GeneratedMod.data(p);var n=MedicineProgress.state(p);var nativeEffect=nativeEffect(e.id());if(nativeEffect!=null){PillRuntime.finiteNative(p,nativeEffect,e.duration(),(int)(e.magnitudeMilli()/1000)-1);return;}
        int root=root(e.id());if(root>=0||e.id()==Kind.root_random){if(root<0){var choices=new ArrayList<Integer>();for(int i=0;i<5;i++)if(d.wuxingxiantu$getRootQuality(i)<e.magnitudeMilli()/1000)choices.add(i);root=choices.get(PillWorldEffects.choice(p,"root",choices.size()));}d.wuxingxiantu$setRootMask(d.wuxingxiantu$getRootMask()|1<<root);d.wuxingxiantu$setRootQuality(root,Math.max(d.wuxingxiantu$getRootQuality(root),(int)e.magnitudeMilli()/1000));CultivationLogic.refreshChaosRoot(p,d);return;}
        switch(e.id()){
            case bigu->d.wuxingxiantu$setBiguRemainTime(Math.max(d.wuxingxiantu$getBiguRemainTime(),e.duration()));
            case meridian_repair->{d.wuxingxiantu$setMeridianStatus(0);p.removeEffect(MobEffects.WEAKNESS);p.removeEffect(ModEffects.MERIDIAN_DAMAGED.get());p.removeEffect(ModEffects.MERIDIAN_SHATTERED.get());}
            case frost_strike,flame_strike,venom_strike->PillWorldEffects.once(p,e,()->cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemyEffects.applyFrozenStrike(p,e.id()==Kind.frost_strike?1:e.id()==Kind.flame_strike?2:4,e.duration()));
            case nutrition->p.getFoodData().eat((int)e.magnitudeMilli()/1000,Math.min(1,e.magnitudeMilli()/5000F));
            case deep_cleanse->{for(var effect:List.of(MobEffects.POISON,MobEffects.CONFUSION,MobEffects.HUNGER,MobEffects.WITHER,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.DIG_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.BLINDNESS)){var active=p.getEffect(effect);if(active!=null&&!active.isInfiniteDuration())p.removeEffect(effect);}}
            case dao_heart_clear->d.wuxingxiantu$setDaoXin(0);
            case calming->MindscapeProgression.requestFromPill(p);
            case physique_reset->{var values=PhysiqueType.values();d.wuxingxiantu$setPhysique(values[PillWorldEffects.choice(p,"physique",Math.min(7,values.length))]);PhysiqueLogic.apply(p,d);}
            case mana_regeneration->PillRuntime.schedule(p,e);
            case origin_reserve->PillWorldEffects.once(p,e,()->{long cap=CultivationLogic.breakthroughRequirement(d);n.putLong("OriginPower",Math.min(cap-d.wuxingxiantu$getCultivation(),e.magnitudeMilli()/1000));n.putInt("OriginRealm",d.wuxingxiantu$getRealm());n.putInt("OriginStage",d.wuxingxiantu$getCultivationStage());});
            case meridian_expansion->PillWorldEffects.expand(p,e);
            case barrier_preparation->PillWorldEffects.once(p,e,()->{n.putLong("V5PreparedShield",e.magnitudeMilli());n.putInt("V5PreparedRealm",d.wuxingxiantu$getRealm());n.putInt("V5PreparedDuration",e.duration());});
            case creation_blessing->((CreationPillItem)WuXingItems.CREATION_PILL.get()).grant(p,d);
            default->throw new IllegalArgumentException("Unknown extended service");
        }PlayerCrafts.get(p.server).setDirty();
    }
    public static String name(Kind k){return switch(k){case frost_strike->"霜击灵丹";case flame_strike->"焰击灵丹";case venom_strike->"毒击灵丹";case invisibility->"隐身灵丹";case luck->"福运灵丹";case nutrition->"饱食灵丹";case poison->"毒性反噬";case confusion->"失衡反噬";case deep_cleanse->"清净灵丹";case bigu->"辟谷灵丹";case dao_heart_clear->"清心灵丹";case meridian_repair->"续脉灵丹";case gathering->"聚气灵丹";case strength->"壮力灵丹";case jump->"腾跃灵丹";case water_breathing->"水息灵丹";case night_vision->"明目灵丹";case haste->"敏采灵丹";case tribulation->"护劫灵丹";case calming->"静心灵丹";case root_metal->"金灵根丹";case root_wood->"木灵根丹";case root_water->"水灵根丹";case root_fire->"火灵根丹";case root_earth->"土灵根丹";case root_random->"筑基灵根丹";case physique_reset->"重塑灵丹";case mana_regeneration->"聚灵温养丹";case origin_reserve->"凝元灵丹";case meridian_expansion->"拓脉灵丹";case barrier_preparation->"破障准备丹";case creation_blessing->"创造丹（管理物品）";default->k.name();};}
    public static String describe(Spec e){return name(e.id())+" · 强度 "+e.magnitudeMilli()/1000D+"（服用时复核原有资格与次数上限）";}
    private ExtendedPillEffects(){}
}
