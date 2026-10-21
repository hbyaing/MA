package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.SpiritEnchantmentLogic;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class WeaponFlameEvents {
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void damage(LivingDamageEvent event) {
        if(!event.getEntity().level().isClientSide)SpiritEnchantmentLogic.finishWeaponHit(event);
    }
    private WeaponFlameEvents() {}
}
