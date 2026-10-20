package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.ExoticFireState;
import cn.blockforge.generated.generatedmod.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

/**
 * 客户端异火发射器：每 tick 为可见异火生成内焰、外焰和余烬三层复合粒子。
 * 发射器只读取同步状态，不改变火焰方块的物理、燃烧和蔓延逻辑。
 */
public final class CodeFireEmitter {
    private static long lastTick = Long.MIN_VALUE;

    private CodeFireEmitter() {}

    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        long time = level.getGameTime();
        if (time == lastTick) return;
        lastTick = time;
        RandomSource random = level.random;
        for (Map.Entry<BlockPos, Integer> entry : ExoticFireState.clientSnapshot(level).entrySet()) {
            BlockPos pos = entry.getKey();
            if (pos.distSqr(client.player.blockPosition()) > 28.0D * 28.0D
                    || !level.getBlockState(pos).is(Blocks.FIRE)) continue;
            int tier = ExoticFireColorRegistry.clampTier(entry.getValue());
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.18D;
            double cz = pos.getZ() + 0.5D;
            // 底部白热内核：九幽仍保留很低的紫色亮点，避免完全不可见。
            spawn(level, ModParticles.EXOTIC_FIRE_CORE.get(), cx + jitter(random, 0.08D),
                    cy + random.nextDouble() * 0.18D, cz + jitter(random, 0.08D),
                    tier * 0.01D, 0.018D + random.nextDouble() * 0.014D, jitter(random, 0.01D));
            int flames = tier == 8 ? 2 : tier == 9 ? 4 : 3;
            for (int i = 0; i < flames; i++) {
                spawn(level, ModParticles.EXOTIC_FIRE_OUTER.get(),
                        cx + jitter(random, 0.24D), cy + random.nextDouble() * 0.62D,
                        cz + jitter(random, 0.24D), tier * 0.01D,
                        0.035D + random.nextDouble() * 0.045D, jitter(random, 0.026D));
            }
            if (random.nextInt(tier == 8 ? 3 : 2) == 0) {
                spawn(level, ModParticles.EXOTIC_FIRE_EMBER.get(),
                        cx + jitter(random, 0.22D), pos.getY() + 0.74D + random.nextDouble() * 0.32D,
                        cz + jitter(random, 0.22D), tier * 0.01D,
                        0.008D + random.nextDouble() * 0.022D, jitter(random, 0.05D));
            }
        }
    }

    private static void spawn(ClientLevel level, ParticleOptions type, double x, double y, double z,
                              double tierCode, double yd, double zd) {
        level.addParticle(type, x, y, z, tierCode, yd, zd);
    }

    private static double jitter(RandomSource random, double amount) {
        return (random.nextDouble() - 0.5D) * amount;
    }
}
