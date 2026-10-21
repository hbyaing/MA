package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Preserve vanilla /kill semantics when combat integrations treat it as an ordinary attack. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ForcedPlayerDeath {
    private static boolean forced(LivingEntity entity,DamageSource source) {
        return entity instanceof ServerPlayer && source.is(DamageTypes.GENERIC_KILL);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void attack(LivingAttackEvent event) {
        if(forced(event.getEntity(),event.getSource()))event.setCanceled(false);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void hurt(LivingHurtEvent event) {
        if(forced(event.getEntity(),event.getSource())) {
            event.setCanceled(false);event.setAmount(Float.MAX_VALUE);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void damage(LivingDamageEvent event) {
        if(forced(event.getEntity(),event.getSource())) {
            event.setCanceled(false);event.setAmount(Float.MAX_VALUE);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void death(LivingDeathEvent event) {
        if(forced(event.getEntity(),event.getSource())) {
            event.setCanceled(false);event.getEntity().setHealth(0);
        }
    }
    private ForcedPlayerDeath() {}
}
