package cn.blockforge.generated.generatedmod.meditationqte;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import java.util.*;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Adds the source minigame to existing seats, progression and rewards, without a second cultivation account. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class MeditationQte {
    public static final String DATA_KEY = "MyriadMeditationQte";
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> REQUESTS = new HashMap<>();
    public record Snapshot(UUID nonce, int level, long startMillis, long openedTick, int realm, int stage) {}
    private static final class Session {
        UUID nonce = UUID.randomUUID();
        int level = 1;
        final int realm, stage;
        final UUID seat;
        final BlockPos cushion;
        final ResourceKey<Level> dimension;
        final long openedTick;
        final Set<UUID> issuedNonces = new HashSet<>();
        long startMillis;
        double precision;
        Session(ServerPlayer player, long nowMillis, long tick) {
            var data = GeneratedMod.data(player);
            realm = data.wuxingxiantu$getRealm(); stage = data.wuxingxiantu$getCultivationStage();
            var entity = (MeditationSeatEntity) player.getVehicle();
            seat = entity.getUUID(); cushion = entity.cushionPos(); dimension = player.level().dimension();
            openedTick = tick; startMillis = nowMillis + 750; issuedNonces.add(nonce);
        }
    }
    private MeditationQte() {}
    private static CompoundTag data(Player player) {
        var persistent = player.getPersistentData();
        if (!persistent.contains(DATA_KEY, 10)) persistent.put(DATA_KEY, new CompoundTag());
        return persistent.getCompound(DATA_KEY);
    }
    private static String stageKey(CultivationData data) {
        return "r" + data.wuxingxiantu$getRealm() + "s" + data.wuxingxiantu$getCultivationStage();
    }
    public static boolean used(Player player) {
        var cultivation = GeneratedMod.data(player);
        return cultivation != null && data(player).getCompound("UsedStages").getBoolean(stageKey(cultivation));
    }
    public static boolean active(Player player) { return SESSIONS.containsKey(player.getUUID()); }
    public static Snapshot snapshot(Player player) {
        var s = SESSIONS.get(player.getUUID());
        return s == null ? null : new Snapshot(s.nonce, s.level, s.startMillis, s.openedTick, s.realm, s.stage);
    }
    /** Single shared penalty hook; callers must apply it once at the authoritative positive-gain boundary. */
    public static double cultivationMultiplier(Player player) {
        return player != null && GeneratedMod.data(player) instanceof CultivationDataImpl implementation
                && implementation.meditationPenaltyTicks() > 0 ? .5 : 1;
    }
    public static long deviationRemaining(Player player) {
        return GeneratedMod.data(player) instanceof CultivationDataImpl implementation ? implementation.meditationPenaltyTicks() : 0;
    }
    public static boolean eligible(ServerPlayer player) {
        var cultivation = GeneratedMod.data(player);
        if (cultivation == null || !player.isAlive() || player.isCreative() || player.isSpectator()
                || ParalysisControl.blocked(player) || GeneratedMod.isSoulProjection(player)
                || cultivation.wuxingxiantu$isDaoHeartUnstable()
                || player.level().dimension().equals(cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.DIMENSION)
                || !cultivation.wuxingxiantu$isMeditating()
                || !(player.getVehicle() instanceof MeditationSeatEntity seat)
                || !seat.isAlive() || !seat.isMeditating()
                || !seat.cushionPos().equals(cultivation.wuxingxiantu$getMeditationPos())
                || !(player.level().getBlockState(seat.cushionPos()).getBlock() instanceof CultivationBlock block)
                || !block.isMeditationCushion()) return false;
        return cultivation.wuxingxiantu$getCultivation() < CultivationLogic.breakthroughRequirement(cultivation);
    }
    public static boolean start(ServerPlayer player) {
        long tick = player.server.getTickCount();
        Long previous = REQUESTS.put(player.getUUID(), tick);
        if (previous != null && tick - previous >= 0 && tick - previous < 10) return false;
        if (active(player)) return false;
        if (!eligible(player)) { message(player, "unavailable"); return false; }
        if (used(player)) { message(player, "used"); return false; }
        var tag = data(player);
        var usedStages = tag.getCompound("UsedStages");
        usedStages.putBoolean(stageKey(GeneratedMod.data(player)), true);
        tag.put("UsedStages", usedStages);
        var session = new Session(player, Util.getMillis(), tick);
        SESSIONS.put(player.getUUID(), session);
        send(player, session, QteStatePacket.OPEN, 0, Util.getMillis());
        return true;
    }
    private static boolean matches(ServerPlayer player, Session session) {
        if (!eligible(player) || player.level().dimension() != session.dimension
                || !(player.getVehicle() instanceof MeditationSeatEntity seat)
                || !seat.getUUID().equals(session.seat) || !seat.cushionPos().equals(session.cushion)) return false;
        var cultivation = GeneratedMod.data(player);
        return cultivation.wuxingxiantu$getRealm() == session.realm
                && cultivation.wuxingxiantu$getCultivationStage() == session.stage;
    }
    public static void hit(ServerPlayer player, UUID nonce, int level) {
        hitAt(player, nonce, level, Util.getMillis(), player.server.getTickCount());
    }
    /** Package-private deterministic clock seam. The packet handler never accepts client time or position. */
    static int hitAt(ServerPlayer player, UUID nonce, int level, long nowMillis, long nowTick) {
        var s = SESSIONS.get(player.getUUID());
        if (s == null || !s.nonce.equals(nonce) || s.level != level) return -1;
        if (!matches(player, s) || nowTick < s.openedTick || nowTick - s.openedTick > QteRules.SESSION_TICKS
                || nowMillis - s.startMillis > QteRules.SESSION_MILLIS) {
            end(player, s, QteStatePacket.CANCELLED, 0, nowMillis); return QteStatePacket.CANCELLED;
        }
        if (nowMillis < s.startMillis) { send(player, s, QteStatePacket.OPEN, 0, nowMillis); return -1; }
        double position = QteRules.position(nowMillis - s.startMillis, level);
        int zone = QteRules.judge(level, position);
        if (zone == QteRules.GRAY) { end(player, s, QteStatePacket.FAIL, 0, nowMillis); return QteStatePacket.FAIL; }
        if (zone == QteRules.RED) {
            if (GeneratedMod.data(player) instanceof CultivationDataImpl implementation)
                implementation.setMeditationPenaltyTicks(QteRules.DEVIATION_TICKS);
            end(player, s, QteStatePacket.DEVIATION, 0, nowMillis); return QteStatePacket.DEVIATION;
        }
        s.precision += QteRules.precision(level, position);
        if (level < QteRules.LEVELS) {
            s.level++; s.nonce = UUID.randomUUID(); s.issuedNonces.add(s.nonce); s.startMillis = nowMillis + 600;
            send(player, s, QteStatePacket.PASS, 0, nowMillis); return QteStatePacket.PASS;
        }
        var cultivation = GeneratedMod.data(player);
        long before = cultivation.wuxingxiantu$getCultivation();
        long reward = QteRules.reward(CultivationLogic.breakthroughRequirement(cultivation), s.precision / QteRules.LEVELS);
        CultivationLogic.gainCultivation(player, cultivation, reward);
        long gained = Math.max(0, cultivation.wuxingxiantu$getCultivation() - before);
        GeneratedMod.sync(player);
        end(player, s, QteStatePacket.WIN, gained, nowMillis); return QteStatePacket.WIN;
    }
    public static void cancel(ServerPlayer player, UUID nonce) {
        var s = SESSIONS.get(player.getUUID());
        if (s != null && s.issuedNonces.contains(nonce)) end(player, s, QteStatePacket.CANCELLED, 0, Util.getMillis());
    }
    private static void end(ServerPlayer player, Session session, int result, long reward, long nowMillis) {
        SESSIONS.remove(player.getUUID());
        send(player, session, result, reward, nowMillis);
    }
    private static void message(ServerPlayer player, String suffix) {
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.qte." + suffix), true);
    }
    private static void send(ServerPlayer player, Session session, int result, long reward, long nowMillis) {
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new QteStatePacket(session.nonce, result, session.level, session.startMillis, nowMillis, reward));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (GeneratedMod.data(player) instanceof CultivationDataImpl implementation) implementation.tickMeditationPenalty();
        var s = SESSIONS.get(player.getUUID());
        if (s != null && (!matches(player, s) || player.server.getTickCount() - s.openedTick > QteRules.SESSION_TICKS
                || Util.getMillis() - s.startMillis > QteRules.SESSION_MILLIS))
            end(player, s, QteStatePacket.CANCELLED, 0, Util.getMillis());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID()); REQUESTS.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) { SESSIONS.remove(player.getUUID()); REQUESTS.remove(player.getUUID()); }
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        SESSIONS.remove(event.getEntity().getUUID()); REQUESTS.remove(event.getEntity().getUUID());
        var original = event.getOriginal().getPersistentData();
        if (original.contains(DATA_KEY, 10)) event.getEntity().getPersistentData().put(DATA_KEY, original.getCompound(DATA_KEY).copy());
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { SESSIONS.clear(); REQUESTS.clear(); }
}
