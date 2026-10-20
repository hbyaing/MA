package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 异火缠绕（防具）视觉发射器：按 WrapVisualPacket 同步来的品阶与缠绕等级，维持一圈小水滴
 * 沿"倾斜环面"轨迹绕玩家旋转（ExoticFireRingParticle）。
 *
 * <p>每颗水滴固定槽相位 + 全局时间推进，寿命恰等于整圈周期，补发间隔 = 周期/颗数，
 * 稳态恒为 N 颗彼此等角距、不重叠、不留下缺口。缠绕等级（=已附魔防具件数）驱动：
 * <ul>
 *   <li>1：单条 45° 斜环；</li>
 *   <li>2：第二条 -45°（135°）斜环，两条环互不重叠；</li>
 *   <li>3：整圈角速度加快（缩短周期）；</li>
 *   <li>4：环面绕竖直轴沿身材中心缓慢自转（倾斜方向缓慢旋转）。</li>
 * </ul>
 * 未装备异火时服务端下发 tier=0，本发射器随之停止特效。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExoticFireOrbitRenderer {
    /** 基础整圈周期（tick），约 10.5 秒。 */
    private static final int PERIOD_BASE = 210;
    /** 等级 >= 3 时缩短为 140 刻（约 7 秒），即角速度加快。 */
    private static final int PERIOD_FAST = 140;
    /** 等级 >= 4 时环面绕竖直轴自转一周的周期（tick），约 30 秒。 */
    private static final int SPIN_PERIOD = 600;
    /** 斜环相对水平面的倾角（弧度），45° 与 -45°（135°）。 */
    private static final double TILT_45 = Math.toRadians(45.0D);

    private static final class RingState {
        final int ringIndex;
        final double tilt;
        boolean started;
        int slotCursor;
        double nextSpawnAt;

        RingState(int ringIndex, double tilt) {
            this.ringIndex = ringIndex;
            this.tilt = tilt;
        }
    }

    private static final class Orbit {
        final int tier;
        final int level;
        final long generation;
        long expiresAt;
        final RingState[] rings;

        Orbit(int tier, int level, long expiresAt, long generation) {
            this.tier = tier;
            this.level = level;
            this.expiresAt = expiresAt;
            this.generation = generation;
            this.rings = new RingState[ringCount(level)];
            for (int i = 0; i < rings.length; i++) {
                rings[i] = new RingState(i, tiltFor(i));
            }
        }
    }

    private static final Map<Integer, Orbit> ORBITS = new HashMap<>();
    private static long generationCounter;
    private static ClientLevel trackedLevel;

    private ExoticFireOrbitRenderer() {}

    public static int visualTier(int entityId) {
        var mc=Minecraft.getInstance();var o=ORBITS.get(entityId);
        return mc.level==trackedLevel&&o!=null&&mc.level!=null&&o.expiresAt>mc.level.getGameTime()?o.tier:0;
    }

    public static int visualLevel(int entityId) {
        var o=ORBITS.get(entityId);return visualTier(entityId)>0&&o!=null?o.level:0;
    }

    /** 该实体当前是否处于指定代次的缠绕状态（重建环后旧代粒子自行消散，供粒子自检）。 */
    public static boolean isWrapping(int entityId, long generation) {
        if(cn.blockforge.generated.generatedmod.client.firecoil.Client.owns(entityId))return false;
        if(!cn.blockforge.generated.generatedmod.client.firecoil.Client.visualsEnabled())return false;
        Orbit orbit = ORBITS.get(entityId);
        return orbit != null && orbit.generation == generation;
    }

    /** 收到服务端同步；tier<=0 或 level<=0 表示应停止环绕特效。 */
    public static void update(int entityId, int tier, int level) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (trackedLevel != client.level) {
            ORBITS.clear();
            trackedLevel = client.level;
        }
        if (tier <= 0 || level <= 0) {
            ORBITS.remove(entityId);
            return;
        }
        int clampedTier = ExoticFireColorRegistry.clampTier(tier);
        int clampedLevel = Math.max(1, Math.min(5, level));
        Orbit existing = ORBITS.get(entityId);
        if (existing != null && existing.tier == clampedTier && existing.level == clampedLevel) {
            existing.expiresAt = client.level.getGameTime() + 45L;
        } else {
            // 品阶或等级变化：重建整圈，旧代水滴下一 tick 即消散，不出现双环/旧代残留。
            ORBITS.put(entityId, new Orbit(clampedTier, clampedLevel,
                    client.level.getGameTime() + 45L, ++generationCounter));
        }
    }

    /** 维护缠绕视觉状态，逐 tick 补发环形水滴。 */
    @SubscribeEvent
    public static void maintainOrbits(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            ORBITS.clear();
            trackedLevel = null;
            return;
        }
        if (trackedLevel != client.level) {
            ORBITS.clear();
            trackedLevel = client.level;
            return;
        }
        long now = client.level.getGameTime();
        ORBITS.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        if(!cn.blockforge.generated.generatedmod.client.firecoil.Client.visualsEnabled()) {
            for(var orbit:ORBITS.values())for(var ring:orbit.rings)ring.started=false;
            return;
        }
        for (Map.Entry<Integer, Orbit> entry : ORBITS.entrySet()) {
            Entity raw = client.level.getEntity(entry.getKey());
            if (raw instanceof Player player && player.isAlive() && !player.isSpectator()) {
                if(cn.blockforge.generated.generatedmod.client.firecoil.Client.owns(player.getId())) {
                    for(var ring:entry.getValue().rings)ring.started=false;
                    continue;
                }
                maintainRing(client.level, player, entry.getValue(), now);
            }
        }
    }

    /** 环面条数：等级 >= 2 出现第二条斜环。 */
    private static int ringCount(int level) {
        return level >= 2 ? 2 : 1;
    }

    /** 第 0 条环 +45°；第 1 条环 -45°（135°），与第一条镜像、互不重叠。 */
    private static double tiltFor(int ringIndex) {
        return ringIndex == 0 ? TILT_45 : -TILT_45;
    }

    /** 圈上水滴颗数：等级 1~5 对应 7~11 颗，均匀分布。 */
    private static int slots(int level) {
        return 6 + level;
    }

    /** 整圈周期：等级 >= 3 加速到 140 刻。粒子寿命与补发间隔都取这个周期，保证恒 N 颗。 */
    private static int period(int level) {
        return level >= 3 ? PERIOD_FAST : PERIOD_BASE;
    }

    /** 沿环轨迹的角速度（弧度/刻）。 */
    private static double omega(int level) {
        return Mth.TWO_PI / period(level);
    }

    /** 等级 >= 4 时开启环面绕竖直轴自转；否则为 0（不自转）。 */
    private static double spinOmega(int level) {
        return level >= 4 ? Mth.TWO_PI / SPIN_PERIOD : 0.0D;
    }

    /**
     * 环形补发：第 0 圈先错龄铺满，之后每 周期/颗数 刻在下一槽位补一颗（初龄为 0，
     * 寿命恰为整圈周期），每颗固定槽相位并以"全局时间"旋转。补发采用"初龄 0"而非
     * 复用 slot*interval，避免补发出来的粒子一出生就快到期、留下缺口；稳态恒定 N 颗。
     */
    private static void maintainRing(ClientLevel level, Player player, Orbit orbit, long now) {
        if (ExoticFireRingParticle.sprites() == null) return;
        int n = slots(orbit.level);
        double om = omega(orbit.level);
        double spinOm = spinOmega(orbit.level);
        double interval = period(orbit.level) / (double) n;
        for (RingState ring : orbit.rings) {
            if (!ring.started) {
                for (int k = 0; k < n; k++) {
                    spawnRingDroplet(level, player, orbit, ring, k, n, now, om, spinOm, true);
                }
                // 错龄铺满时槽位 n-1 最先到期，补发按到期顺序从 n-1 倒序轮转，保证每槽恒一颗粒。
                ring.slotCursor = n - 1;
                ring.started = true;
                ring.nextSpawnAt = now + interval;
            } else if (now >= ring.nextSpawnAt) {
                spawnRingDroplet(level, player, orbit, ring, ring.slotCursor % n, n, now, om, spinOm, false);
                ring.slotCursor = (ring.slotCursor + n - 1) % n;
                // 用 += 累积而非重置为 now+interval：interval 可能非整数时，若每次取到下一整数刻再
                // 重置，会累积 1 刻内的截断误差（n 颗累计约 1 刻），导致槽位补发慢于生命周期、留下缺口。
                ring.nextSpawnAt += interval;
            }
        }
    }

    /**
     * 在指定槽位补一颗水滴。槽位相位 = slot*2π/N，粒子以全局时间推进，槽位间永远等角距；
     * 初龄只在铺满时错开（slot*interval），补发粒子初龄为 0 从而拥有完整生命周期。
     */
    private static void spawnRingDroplet(ClientLevel level, Player player, Orbit orbit, RingState ring,
                                         int slot, int count, long now, double omega, double spinOmega,
                                         boolean initialFill) {
        double phase = slot * (Mth.TWO_PI / count);
        // 环半径约 1.07 格（0.80 + 0.45*体宽），在身体轮廓之外，不贴着模型糊脸。
        double radius = 0.80D + player.getBbWidth() * 0.45D;
        // 单颗 0.09~0.12 格，比旧版水滴明显更小。
        double size = 0.085D + orbit.level * 0.006D;
        double interval = period(orbit.level) / (double) count;
        // 初龄取向下取整（截断）而非四舍五入：让初始粒子寿命略长，到期时刻恰好落在该槽位
        // 首次补发所在的整数刻，避免 interval 非整数时初始一瞬出现 1 刻空缺。
        int age = initialFill ? (int) (slot * interval) : 0;
        int lifetime = period(orbit.level);
        // 1.20.1 的 ClientLevel.addParticle(Particle) 不公开，环形粒子由客户端直接构造后塞进引擎。
        Minecraft.getInstance().particleEngine.add(new ExoticFireRingParticle(level, player.getId(), phase, radius,
                0.50D, omega, ring.tilt, spinOmega, size, orbit.tier, age, lifetime, orbit.generation));
    }
}
