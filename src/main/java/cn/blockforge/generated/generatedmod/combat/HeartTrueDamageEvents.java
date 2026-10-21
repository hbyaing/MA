package cn.blockforge.generated.generatedmod.combat;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Numeric defenses cannot lower heart damage; cancellations remain gameplay-state vetoes. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class HeartTrueDamageEvents {
    private static final Map<LivingHurtEvent,Float> HURT=new WeakHashMap<>();
    private static final Map<LivingDamageEvent,Float> DAMAGE=new WeakHashMap<>();
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public static void beginHurt(LivingHurtEvent event) {
        if(HeartSwordCombatProfile.isHeartTrue(event.getSource()))HURT.put(event,event.getAmount());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void endHurt(LivingHurtEvent event) {
        Float amount=HURT.remove(event);
        if(amount!=null&&!event.isCanceled())event.setAmount(Float.isFinite(event.getAmount())?Math.max(amount,event.getAmount()):amount);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public static void beginDamage(LivingDamageEvent event) {
        if(HeartSwordCombatProfile.isHeartTrue(event.getSource()))DAMAGE.put(event,event.getAmount());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void endDamage(LivingDamageEvent event) {
        Float amount=DAMAGE.remove(event);
        if(amount!=null&&!event.isCanceled())event.setAmount(Float.isFinite(event.getAmount())?Math.max(amount,event.getAmount()):amount);
    }
    private HeartTrueDamageEvents() {}
}
