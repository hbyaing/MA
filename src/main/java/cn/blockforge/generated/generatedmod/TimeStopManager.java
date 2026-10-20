package cn.blockforge.generated.generatedmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 日月停轮的服务端运行时状态；只暂停范围内非玩家实体，不影响施术者和其他玩家。 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
public final class TimeStopManager {
    public static final int DURATION_TICKS = 120;
    public static final double RADIUS = 24.0D;

    private record Stop(UUID casterId, ServerLevel level, double x, double y, double z, long expiresAt) {}
    private record Frozen(Entity entity, boolean wasUpdatable) {}

    private static final Map<UUID, Stop> STOPS = new HashMap<>();
    private static final Map<UUID, Frozen> FROZEN = new HashMap<>();

    private TimeStopManager() {}

    public static void activate(ServerPlayer caster) {
        STOPS.put(caster.getUUID(), new Stop(caster.getUUID(), caster.serverLevel(), caster.getX(), caster.getY(),
                caster.getZ(), caster.serverLevel().getGameTime() + DURATION_TICKS));
        reconcile(caster.server);
        caster.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                caster.getX(), caster.getY() + 1.0D, caster.getZ(), 36, RADIUS * 0.45D, 1.0D,
                RADIUS * 0.45D, 0.02D);
        caster.serverLevel().playSound(null, caster.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 0.65F);
    }

    public static boolean isActive(ServerPlayer caster) {
        return STOPS.containsKey(caster.getUUID());
    }

    public static void clear(ServerPlayer caster) {
        STOPS.remove(caster.getUUID());
        reconcile(caster.server);
    }

    public static void clearAll(MinecraftServer server) {
        STOPS.clear();
        restoreAll();
    }

    /** 在世界 tick 后重算范围，既冻结新进入的实体，也只恢复本管理器改过的实体。 */
    public static void tick(MinecraftServer server) {
        Set<UUID> expiredCasters = new HashSet<>();
        for (Stop stop : STOPS.values()) {
            if (stop.level().getGameTime() >= stop.expiresAt()) expiredCasters.add(stop.casterId());
        }
        for (UUID casterId : expiredCasters) {
            Stop stop = STOPS.remove(casterId);
            if (stop == null) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(casterId);
            if (player != null) DivineArtLogic.endTimeStop(player);
        }
        reconcile(server);
    }

    public static boolean isStopped(Entity entity) {
        return entity != null && FROZEN.containsKey(entity.getUUID());
    }

    public static boolean isCaster(Entity entity) {
        return entity != null && STOPS.containsKey(entity.getUUID());
    }

    /** 时间暂停施术者可正常攻击和交互，其他被冻结实体不能主动行动。 */
    public static boolean canAct(Entity entity) {
        return entity != null && !isStopped(entity);
    }

    public static boolean blocksDamage(Entity target, Entity attacker) {
        if (!isStopped(target)) return false;
        return !isCaster(resolveCaster(attacker));
    }

    private static Entity resolveCaster(Entity source) {
        Entity current = source;
        for (int depth = 0; depth < 4 && current != null; depth++) {
            if (isCaster(current)) return current;
            if (!(current instanceof Projectile projectile)) return current;
            Entity owner = projectile.getOwner();
            if (owner == null || owner == current) return current;
            current = owner;
        }
        return current;
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isActive(player)) {
            DivineArtLogic.endTimeStop(player);
            clear(player);
        }
    }

    private static void reconcile(MinecraftServer server) {
        Set<UUID> desired = new HashSet<>();
        for (Stop stop : STOPS.values()) {
            AABB area = new AABB(stop.x() - RADIUS, stop.y() - RADIUS, stop.z() - RADIUS,
                    stop.x() + RADIUS, stop.y() + RADIUS, stop.z() + RADIUS);
            for (Entity entity : stop.level().getEntitiesOfClass(Entity.class, area,
                    entity -> shouldFreeze(entity, stop))) {
                desired.add(entity.getUUID());
                Frozen existing = FROZEN.get(entity.getUUID());
                if (existing == null) {
                    FROZEN.put(entity.getUUID(), new Frozen(entity, entity.canUpdate()));
                    entity.canUpdate(false);
                } else if (entity.canUpdate()) {
                    // 其他逻辑不应在暂停期间偷偷恢复实体 tick。
                    entity.canUpdate(false);
                }
            }
        }
        FROZEN.entrySet().removeIf(entry -> {
            if (desired.contains(entry.getKey())) return false;
            Frozen frozen = entry.getValue();
            if (!frozen.entity().isRemoved()) frozen.entity().canUpdate(frozen.wasUpdatable());
            return true;
        });
    }

    private static boolean shouldFreeze(Entity entity, Stop stop) {
        if (entity == null || entity.isRemoved() || entity.getUUID().equals(stop.casterId())) return false;
        if (entity instanceof Player) return false;
        if (entity instanceof Projectile projectile && isCaster(projectile.getOwner())) return false;
        // 不冻结载有玩家的载具，避免玩家输入被载具的暂停状态间接吞掉。
        return entity.getPassengers().stream().noneMatch(Player.class::isInstance);
    }

    private static void restoreAll() {
        for (Frozen frozen : FROZEN.values()) {
            if (!frozen.entity().isRemoved()) frozen.entity().canUpdate(frozen.wasUpdatable());
        }
        FROZEN.clear();
    }
}
