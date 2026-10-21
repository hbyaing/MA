package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class RealmVision {
    private static final Map<ServerPlayer, MobEffectInstance> OWNED = new WeakHashMap<>();
    @SubscribeEvent public static void added(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getEffectInstance().getEffect() == MobEffects.NIGHT_VISION)
            OWNED.remove(player);
    }
    public static void tick(ServerPlayer player, boolean enabled) {
        var current = player.getEffect(MobEffects.NIGHT_VISION);
        var owned = OWNED.get(player);
        if (!enabled) {
            if (owned != null && current == owned) player.removeEffect(MobEffects.NIGHT_VISION);
            OWNED.remove(player); return;
        }
        // Never replace a potion, pill, command, or another mod's effect.
        if (current == null || current == owned && current.getDuration() < 240) {
            MobEffectInstance requested = new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, true);
            boolean applied = player.addEffect(requested);
            MobEffectInstance actual = player.getEffect(MobEffects.NIGHT_VISION);
            // Forge may reject an effect, or another event listener may replace it.
            // A successful refresh normally mutates our existing instance in place.
            if (applied && (actual == requested || owned != null && actual == owned)) {
                OWNED.put(player, actual);
            }
        }
    }
    private RealmVision() {}
}
