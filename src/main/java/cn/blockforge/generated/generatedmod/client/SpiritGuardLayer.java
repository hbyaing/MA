package cn.blockforge.generated.generatedmod.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-confirmed impact state. The effect is submitted by the actual armor layer. */
public final class SpiritGuardLayer {
    private static final Map<AbstractClientPlayer, Double> HIT_TIMES = new WeakHashMap<>();
    private static final Map<AbstractClientPlayer, Vec3> HIT_DIRECTIONS = new WeakHashMap<>();

    public static void impact(int entityId) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(entityId) instanceof AbstractClientPlayer player)
            impact(entityId, player.getLookAngle());
    }

    public static void impact(int entityId, Vec3 direction) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(entityId) instanceof AbstractClientPlayer player)) return;
        if (!Double.isFinite(direction.lengthSqr()) || direction.lengthSqr() < .001) return;
        HIT_TIMES.put(player, (double) level.getGameTime());
        HIT_DIRECTIONS.put(player, direction.normalize());
    }

    static float hitAge(AbstractClientPlayer player, float partialTick) {
        Double tick = HIT_TIMES.get(player);
        if (tick == null) return -1;
        float age = (float) ((player.level().getGameTime() + partialTick - tick) / 20.0);
        return age >= 0 && age <= .8F ? age : -1;
    }

    static Vec3 hitDirection(AbstractClientPlayer player) {
        return HIT_DIRECTIONS.getOrDefault(player, player.getLookAngle());
    }

    private SpiritGuardLayer() {}
}
