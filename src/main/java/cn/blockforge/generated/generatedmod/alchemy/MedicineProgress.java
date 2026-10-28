package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One target-owned medicinal state; no imported capability or competing cultivation ladder. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class MedicineProgress {
    public static CompoundTag state(ServerPlayer player){
        var account=PlayerCrafts.get(player.server).account(player.getUUID());
        if(!account.contains("MyriadMedicine"))account.put("MyriadMedicine",new CompoundTag());
        return account.getCompound("MyriadMedicine");
    }
    private static void dirty(ServerPlayer player){PlayerCrafts.get(player.server).setDirty();}
    public static boolean apply(ServerPlayer player,CultivationData data,PillCatalog pill,int quality){
        return apply(player,data,pill,quality,1);
    }
    public static boolean apply(ServerPlayer player,CultivationData data,PillCatalog pill,int quality,int rank){
        var n=state(player);quality=Math.max(1,Math.min(4,quality));rank=Math.max(1,Math.min(5,rank));
        switch(pill){
            case GATHER_QI -> {
                if(n.getInt("QiSeconds")>0&&(n.getInt("QiQuality")>quality||n.getInt("QiQuality")==quality&&n.getInt("QiRank")>=rank))return false;
                n.putInt("QiSeconds",60*quality*(rank+1)/2);n.putInt("QiQuality",quality);n.putInt("QiRank",rank);
            }
            case REJUVENATION -> {
                if(player.getHealth()>=player.getMaxHealth()||n.getInt("RecoverySeconds")>0&&(n.getInt("RecoveryQuality")>quality||n.getInt("RecoveryQuality")==quality&&n.getInt("RecoveryRank")>=rank))return false;
                n.putInt("RecoverySeconds",30*quality*(rank+1)/2);n.putInt("RecoveryQuality",quality);n.putInt("RecoveryRank",rank);
            }
            case ORIGIN_CONDENSING -> {
                if(n.getLong("OriginPower")>0||!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,true))return false;
                long cap=CultivationLogic.breakthroughRequirement(data),current=data.wuxingxiantu$getCultivation();
                if(current>=cap)return false;
                n.putLong("OriginPower",Math.min(cap-current,Math.min(Math.max(1,cap/2),Math.max(1,cap/20)*quality*rank)));
                n.putInt("OriginRealm",data.wuxingxiantu$getRealm());n.putInt("OriginStage",data.wuxingxiantu$getCultivationStage());
            }
            case MERIDIAN_EXPANSION -> {
                String key="ExpansionRealm"+data.wuxingxiantu$getRealm();int used=Math.max(0,n.getInt(key));
                int before=data.wuxingxiantu$getAuraBonus();
                if(used>=2||before>=25)return false;
                data.wuxingxiantu$setAuraBonus(Math.min(25,before+Math.min(5,quality+(rank-1)/2)));n.putInt(key,used+1);
                CultivationLogic.refreshMaxMana(data);
            }
            case BARRIER_PREPARATION -> {
                if(data.wuxingxiantu$getCultivationStage()!=3||data.wuxingxiantu$getRealm()>=8||TribulationManager.isActive(player)
                        ||data.wuxingxiantu$isDaoHeartUnstable()||n.getInt("PreparedBarrier")>quality
                        ||n.getInt("PreparedBarrier")==quality&&n.getInt("PreparedBarrierRank")>=rank)return false;
                n.putInt("PreparedBarrier",quality);n.putInt("PreparedBarrierRank",rank);n.putInt("BarrierRealm",data.wuxingxiantu$getRealm());
            }
            default -> {return false;}
        }
        dirty(player);return true;
    }
    /** A preparation cannot start a trial or waive any target requirement. */
    public static void beginTrial(ServerPlayer player){
        var n=state(player);var d=GeneratedMod.data(player);int quality=n.getInt("PreparedBarrier");
        if(d==null)return;
        if(n.getLong("V5PreparedShield")>0){if(n.getInt("V5PreparedRealm")==d.wuxingxiantu$getRealm())cn.blockforge.generated.generatedmod.alchemy.effects.PillRuntime.finiteNative(player,MobEffects.ABSORPTION,Math.max(20,Math.min(72000,n.getInt("V5PreparedDuration"))),(int)Math.min(5,n.getLong("V5PreparedShield")/4000-1));n.remove("V5PreparedShield");dirty(player);}
        if(quality<=0)return;
        if(n.getInt("BarrierRealm")==d.wuxingxiantu$getRealm()) {
            int rank=Math.max(1,Math.min(5,n.getInt("PreparedBarrierRank")));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,1200*rank,Math.min(2,quality-1+(rank-1)/2),false,true,true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200*quality,0,false,true,true));
        }
        n.remove("PreparedBarrier");dirty(player);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase==TickEvent.Phase.END&&event.player instanceof ServerPlayer player&&player.tickCount%20==0)second(player);
    }
    public static void second(ServerPlayer player){
        if(!player.isAlive()||player.isSpectator())return;var data=GeneratedMod.data(player);if(data==null)return;
        var n=state(player);boolean changed=false;
        int seconds=n.getInt("QiSeconds");if(seconds>0){
            n.putInt("QiSeconds",seconds-1);CultivationLogic.restoreMana(data,CultivationLogic.maxAura(data)*.0025F*Math.max(1,Math.min(4,n.getInt("QiQuality"))));changed=true;
        }
        seconds=n.getInt("RecoverySeconds");if(seconds>0){
            n.putInt("RecoverySeconds",seconds-1);player.heal(player.getMaxHealth()*.0025F*Math.max(1,Math.min(4,n.getInt("RecoveryQuality"))));changed=true;
        }
        long power=Math.max(0,n.getLong("OriginPower"));
        if(power>0){
            if(data.wuxingxiantu$getRealm()!=n.getInt("OriginRealm")||data.wuxingxiantu$getCultivationStage()!=n.getInt("OriginStage")){
                n.putLong("OriginPower",0);changed=true;
            }else if(data.wuxingxiantu$isMeditating()&&cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,false)){
                long cap=CultivationLogic.breakthroughRequirement(data),current=Math.max(0,Math.min(cap,data.wuxingxiantu$getCultivation()));
                long gain=Math.min(power,Math.min(cap-current,Math.max(1,cap/1200)));
                if(gain>0){data.wuxingxiantu$setCultivation(current+gain);n.putLong("OriginPower",power-gain);changed=true;}
            }
        }
        if(changed){dirty(player);GeneratedMod.sync(player);}
    }
    @SubscribeEvent public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        var n=state(player);for(String key:java.util.List.of("QiSeconds","RecoverySeconds","OriginPower","PreparedBarrier","V5PreparedShield"))n.remove(key);
        dirty(player);
    }
    private MedicineProgress(){}
}
