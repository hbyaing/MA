package cn.blockforge.generated.generatedmod.alchemy.auxiliary;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.artifacts.ArtifactEffects;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import static cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Essence.*;

/** Existing target medicine owns recovery/progression; this stores only bounded unique strike lifetime. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class AuxiliaryAlchemyEffects {
    private static final String STRIKES="AuxiliaryStrikes";
    /** Hook after the base pill succeeded and before its one-stack decrement. */
    public static void applyPill(net.minecraft.world.entity.player.Player user,ItemStack pill){
        if(!(user instanceof ServerPlayer player)||!player.isAlive()||player.isSpectator()||ParalysisControl.blocked(player)||!ProgressionGate.canUsePill(player,pill))return;
        var data=GeneratedMod.data(player);if(data==null||PillData.read(pill)==null)return;
        var plan=AuxiliaryAlchemy.read(pill);boolean cleanse=false;int mask=0,duration=0;
        for(var injection:plan.injections()){
            if(!ProgressionGate.isAtLeastRealm(player,injection.tier()-1))continue;
            int quality=Math.max(1,Math.min(4,injection.amplifier()+1));
            switch(injection.essence()){
                case SPIRIT -> MedicineProgress.apply(player,data,PillCatalog.GATHER_QI,quality,injection.tier());
                case ORIGIN -> MedicineProgress.apply(player,data,PillCatalog.ORIGIN_CONDENSING,quality,injection.tier());
                case VITALITY,WOOD -> {MedicineProgress.apply(player,data,PillCatalog.REJUVENATION,quality,injection.tier());cleanse|=injection.essence()==WOOD;}
                case PURITY -> {cleanse=true;buff(player,MobEffects.DAMAGE_RESISTANCE,injection.duration(),Math.min(2,injection.amplifier()));}
                case BREAK,FLAME,THUNDER -> buff(player,MobEffects.DAMAGE_BOOST,injection.duration(),Math.min(2,injection.amplifier()));
                case FROST,STONE -> buff(player,MobEffects.DAMAGE_RESISTANCE,injection.duration(),Math.min(2,injection.amplifier()));
                case METAL -> buff(player,MobEffects.ABSORPTION,injection.duration(),Math.min(2,injection.amplifier()));
                case WIND -> buff(player,MobEffects.MOVEMENT_SPEED,injection.duration(),Math.min(2,injection.amplifier()));
                case ILLUSION -> buff(player,MobEffects.INVISIBILITY,injection.duration(),0);
                case FORTUNE -> buff(player,MobEffects.LUCK,injection.duration(),Math.min(2,injection.amplifier()));
                case ABUNDANCE -> player.getFoodData().eat(1,Math.min(1,.2F*(injection.amplifier()+1)));
                case VENOM -> { /* Only an unbalanced plan has self-poison; strikes use the unique timer below. */ }
            }
            int bit=injection.essence()==FROST?1:injection.essence()==FLAME?2:injection.essence()==VENOM?4:0;
            if(bit!=0){mask|=bit;duration=Math.max(duration,injection.duration());}
        }
        if(cleanse)for(var effect:java.util.List.of(MobEffects.POISON,MobEffects.CONFUSION,MobEffects.HUNGER,MobEffects.WITHER,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.DIG_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.BLINDNESS)){
            var active=player.getEffect(effect);if(active!=null&&!active.isInfiniteDuration())player.removeEffect(effect);
        }
        // Never clear mindscape, DaoHeartUnstable, meridian damage, paralysis or progression gates.
        if(plan.clash())buff(player,MobEffects.CONFUSION,200,0);
        if(plan.backlash())buff(player,MobEffects.POISON,100,0);
        if(mask!=0){
            var medicine=MedicineProgress.state(player);var old=medicine.getCompound(STRIKES);long now=player.level().getGameTime();
            long remaining=Math.max(0,Math.min(7200,old.getLong("Until")-now));var state=new CompoundTag();
            state.putInt("Mask",mask|(remaining>0?old.getInt("Mask")&7:0));state.putLong("Until",now+Math.min(7200,Math.max(remaining,duration)));
            state.putString("Dimension",player.level().dimension().location().toString());medicine.put(STRIKES,state);dirty(player);
        }
        GeneratedMod.sync(player);
    }
    private static void buff(LivingEntity entity,MobEffect effect,int duration,int amplifier){
        var old=entity.getEffect(effect);if(old!=null&&(old.isInfiniteDuration()||old.getAmplifier()>amplifier||old.getAmplifier()==amplifier&&old.getDuration()>=duration))return;
        entity.addEffect(new MobEffectInstance(effect,Math.min(7200,Math.max(1,duration)),Math.max(0,Math.min(2,amplifier)),false,true,true));
    }
    public static boolean enemy(ServerPlayer player,LivingEntity target){
        if(!ArtifactEffects.target(player,target))return false;
        if(target instanceof NeutralMob neutral)return neutral.getTarget()==player;
        return target instanceof Enemy||target instanceof Mob mob&&mob.getTarget()==player;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void hurt(LivingHurtEvent event){
        if(event.isCanceled()||event.getAmount()<=0||!(event.getSource().getEntity() instanceof ServerPlayer player)
                ||event.getSource().getDirectEntity()!=player||!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                ||!player.isAlive()||player.isSpectator()||player.distanceToSqr(event.getEntity())>25
                ||ParalysisControl.blocked(player)||GeneratedMod.isSoulProjection(player)||!enemy(player,event.getEntity()))return;
        var state=MedicineProgress.state(player).getCompound(STRIKES);long now=player.level().getGameTime();
        if(state.getLong("Until")<=now||!state.getString("Dimension").equals(player.level().dimension().location().toString()))return;
        int mask=state.getInt("Mask")&7;var target=event.getEntity();
        if((mask&1)!=0)buff(target,MobEffects.MOVEMENT_SLOWDOWN,100,1);
        if((mask&2)!=0&&target.getRemainingFireTicks()<60)target.setSecondsOnFire(3);
        if((mask&4)!=0)buff(target,MobEffects.POISON,100,0);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%20!=0)return;
        var medicine=MedicineProgress.state(player);if(!medicine.contains(STRIKES))return;var state=medicine.getCompound(STRIKES);
        if(!player.isAlive()||state.getLong("Until")<=player.level().getGameTime()||!state.getString("Dimension").equals(player.level().dimension().location().toString()))clear(player);
    }
    private static void dirty(ServerPlayer player){PlayerCrafts.get(player.server).setDirty();}
    public static void clear(ServerPlayer player){var state=MedicineProgress.state(player);if(state.contains(STRIKES)){state.remove(STRIKES);dirty(player);}}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer player)clear(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player)clear(player);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)clear(player);}
    private AuxiliaryAlchemyEffects(){}
}
