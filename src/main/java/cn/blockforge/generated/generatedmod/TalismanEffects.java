package cn.blockforge.generated.generatedmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-authoritative talismans; rays stop at blocks and never edit terrain. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class TalismanEffects {
    private TalismanEffects() {}
    private static final ThreadLocal<Boolean> REDUCING=ThreadLocal.withInitial(()->false);
    public static boolean purifiable(MobEffect effect) {
        return effect==MobEffects.MOVEMENT_SLOWDOWN||effect==MobEffects.WEAKNESS
            ||effect==MobEffects.POISON||effect==MobEffects.HUNGER||effect==MobEffects.BLINDNESS
            ||effect==MobEffects.CONFUSION||effect==MobEffects.DIG_SLOWDOWN;
    }
    private static boolean evil(MobEffect effect) {
        return effect==MobEffects.WEAKNESS||effect==MobEffects.BLINDNESS||effect==MobEffects.CONFUSION;
    }
    /** Only the explicit ordinary debuff list is shortened. Boss/custom effects are untouched. */
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void resist(MobEffectEvent.Applicable event) {
        LivingEntity entity=event.getEntity();MobEffectInstance incoming=event.getEffectInstance();
        if(entity.level().isClientSide||REDUCING.get()||incoming==null||event.getResult()==Event.Result.DENY)return;
        boolean ward=entity.hasEffect(ModEffects.TALISMAN_WARD.get())&&evil(incoming.getEffect());
        boolean purge=entity.hasEffect(ModEffects.TALISMAN_PURGE.get())&&purifiable(incoming.getEffect());
        if(!ward&&!purge||incoming.isInfiniteDuration())return;
        event.setResult(Event.Result.DENY);
        // Reapply the weaker instance; scoped guard prevents recursive event handling.
        REDUCING.set(true);
        try { entity.addEffect(new MobEffectInstance(incoming.getEffect(),Math.max(1,incoming.getDuration()/2),
                Math.max(0,incoming.getAmplifier()-(ward?1:0)),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon())); }
        finally {REDUCING.remove();}
    }
    public static boolean activate(TalismanCatalog.Effect effect,Player player,ItemStack stack) {
        if(!(player.level() instanceof ServerLevel level))return false;
        if(player.getCooldowns().isOnCooldown(stack.getItem()))return false;
        boolean applied;
        switch(effect) {
            case GATHER -> { player.addEffect(new MobEffectInstance(ModEffects.TALISMAN_GATHERING.get(),1200,0));applied=true; }
            case SPEED -> { player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,600,1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP,600,0));applied=true; }
            case WARD,PURGE -> {
                for(var instance:java.util.List.copyOf(player.getActiveEffects())) {
                    if(!instance.isInfiniteDuration()&&(effect==TalismanCatalog.Effect.PURGE?purifiable(instance.getEffect()):evil(instance.getEffect())))
                        player.removeEffect(instance.getEffect());
                }
                player.addEffect(new MobEffectInstance(effect==TalismanCatalog.Effect.WARD
                        ?ModEffects.TALISMAN_WARD.get():ModEffects.TALISMAN_PURGE.get(),effect==TalismanCatalog.Effect.WARD?600:200,0));
                applied=true;
            }
            case FIRE,THUNDER,THORN -> applied=attack(effect,player,level);
            // Existing saved talismans retain their old benign effects.
            case SHIELD -> {player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,600,2));applied=true;}
            case HEAL -> {player.heal(8);player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,120,1));applied=true;}
            default -> applied=false;
        }
        if(applied){
            // Retain the shared one-second cooldown after separating item IDs.
            player.getCooldowns().addCooldown(WuXingItems.TALISMAN.get(),20);
            for(String id:TalismanCatalog.ids())player.getCooldowns().addCooldown(WuXingItems.talismanItem(id),20);
            level.sendParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+1,player.getZ(),12,.35,.6,.35,.03);}
        else player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.talisman_no_target"),true);
        return applied;
    }
    private static boolean enemy(Player caster,LivingEntity entity) {
        if(entity==caster||!entity.isAlive()||entity.isInvulnerable()||entity.isAlliedTo(caster))return false;
        if(entity instanceof Player p&&(p.isCreative()||p.isSpectator()||!caster.canHarmPlayer(p)))return false;
        if(entity instanceof net.minecraft.world.entity.TamableAnimal pet&&caster.getUUID().equals(pet.getOwnerUUID()))return false;
        return true;
    }
    private static boolean visible(ServerLevel level,Vec3 origin,LivingEntity target) {
        return level.clip(new ClipContext(origin,target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,target)).getType()==HitResult.Type.MISS;
    }
    private static boolean attack(TalismanCatalog.Effect effect,Player player,ServerLevel level) {
        Vec3 from=player.getEyePosition(),end=from.add(player.getLookAngle().scale(12));
        var block=level.clip(new ClipContext(from,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        Vec3 hit=block.getLocation();double distance=from.distanceToSqr(hit);LivingEntity nearest=null;
        for(var candidate:level.getEntitiesOfClass(LivingEntity.class,new AABB(from,hit).inflate(1),e->enemy(player,e))) {
            var intersection=candidate.getBoundingBox().inflate(.15).clip(from,hit);
            if(intersection.isPresent()&&from.distanceToSqr(intersection.get())<distance) {
                distance=from.distanceToSqr(intersection.get());hit=intersection.get();nearest=candidate;
            }
        }
        if(effect==TalismanCatalog.Effect.THUNDER) {
            if(nearest==null)return false;
            nearest.hurt(level.damageSources().indirectMagic(player,player),8);
            nearest.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(),20,0));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,hit.x,hit.y,hit.z,28,.25,.6,.25,.03);
            level.playSound(null,hit.x,hit.y,hit.z,SoundEvents.LIGHTNING_BOLT_IMPACT,SoundSource.PLAYERS,.65f,1.4f);
            return true;
        }
        if(nearest==null&&block.getType()==HitResult.Type.MISS)return false;
        // Move just outside a block surface so the ray does not start inside that block.
        Vec3 center=hit.subtract(player.getLookAngle().scale(.03));
        for(var target:level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(2.5),e->enemy(player,e))) {
            if(target.position().distanceToSqr(center)>6.25||!visible(level,center,target))continue;
            if(effect==TalismanCatalog.Effect.FIRE)target.hurt(level.damageSources().indirectMagic(player,player),6);
            else target.addEffect(new MobEffectInstance(ModEffects.TALISMAN_BIND.get(),80,0));
        }
        level.sendParticles(effect==TalismanCatalog.Effect.FIRE?ParticleTypes.FLAME:ParticleTypes.HAPPY_VILLAGER,
                center.x,center.y,center.z,32,1,.25,1,.02);
        level.playSound(null,center.x,center.y,center.z,effect==TalismanCatalog.Effect.FIRE
                ?SoundEvents.FIRECHARGE_USE:SoundEvents.AZALEA_LEAVES_PLACE,SoundSource.PLAYERS,.8f,1);
        return true;
    }
}
