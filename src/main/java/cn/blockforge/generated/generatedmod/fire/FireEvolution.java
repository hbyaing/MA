package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.blockforge.generated.generatedmod.fire.FireBottleData.Essence.*;

/** Explicit, one-way rituals. A successful ritual mutates this bottle, never creates another. */
public final class FireEvolution {
    public enum Environment { OPEN_FLAME, NOON, SOUL_VALLEY, DRAGON_END }
    public record Route(int from, int to, Map<FireBottleData.Essence, Double> cost, Environment environment) {}
    public static final double REMAINING_RETENTION = 0.50D;
    public static final List<Route> ROUTES = List.of(
            new Route(1, 2, Map.of(METAL, 120D, FIRE, 60D), Environment.OPEN_FLAME),
            new Route(2, 7, Map.of(FIRE, 480D, YANG, 360D), Environment.NOON),
            new Route(3, 8, Map.of(YIN, 480D, SOUL, 360D), Environment.SOUL_VALLEY),
            new Route(7, 9, Map.of(FIRE, 1000D, SOUL, 750D, YANG, 500D), Environment.DRAGON_END),
            new Route(8, 9, Map.of(FIRE, 1000D, SOUL, 750D, YANG, 500D), Environment.DRAGON_END)
    );

    private static boolean essenceReady(ItemStack bottle, Route route) {
        return FireBottleData.tier(bottle) == route.from() && route.cost().entrySet().stream()
                .allMatch(cost -> FireBottleData.essence(bottle, cost.getKey()) >= cost.getValue());
    }

    public static boolean hasEssenceForRoute(ItemStack bottle) {
        return ROUTES.stream().anyMatch(route -> essenceReady(bottle, route));
    }

    private static boolean environmentReady(ServerPlayer player, Route route) {
        var level = player.serverLevel();
        var pos = player.blockPosition();
        return switch (route.environment()) {
            case OPEN_FLAME -> level.canSeeSky(pos) && (level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                    || level.getFluidState(pos.below()).is(net.minecraft.tags.FluidTags.LAVA));
            case NOON -> level.dimension().equals(Level.OVERWORLD) && level.getDayTime() % 24000L >= 5000L
                    && level.getDayTime() % 24000L <= 7000L && !level.isRaining()
                    && level.canSeeSky(pos) && level.getMaxLocalRawBrightness(pos) >= 12;
            case SOUL_VALLEY -> level.dimension().equals(Level.NETHER) && level.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY);
            case DRAGON_END -> level.dimension().equals(Level.END)
                    && hasDefeatedDragon(player)
                    && player.getOffhandItem().is(Items.DRAGON_BREATH);
        };
    }

    private static boolean hasDefeatedDragon(ServerPlayer player) {
        var advancement = player.server.getAdvancements().getAdvancement(
                new net.minecraft.resources.ResourceLocation("minecraft", "end/kill_dragon"));
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static boolean evolve(ServerPlayer player, ItemStack bottle) {
        int current = FireBottleData.tier(bottle);
        if (bottle.getCount() != 1 || !ProgressionGate.canUseFire(player, current)) {
            ProgressionGate.denyFireUse(player, current);
            return false;
        }
        for (Route route : ROUTES) {
            if (!essenceReady(bottle, route) || !ProgressionGate.meetsFireRealmRequirement(player, route.to())
                    || !environmentReady(player, route)) continue;
            var fire = FireBottleData.initialize(bottle);
            double total = 0;
            for (var essence : FireBottleData.Essence.values()) {
                double remaining = Math.max(0, FireBottleData.essence(bottle, essence) - route.cost().getOrDefault(essence, 0D))
                        * REMAINING_RETENTION;
                fire.putDouble(essence.key(), remaining);
                total += remaining;
            }
            fire.putDouble("total_essence", total);
            fire.putString("fire_id", FireCatalog.tier(route.to()).key().toString());
            int[] previous = fire.getIntArray("evolution_flags");
            int[] flags = Arrays.copyOf(previous, Math.min(previous.length, 8) + 1);
            flags[flags.length - 1] = route.to();
            fire.putIntArray("evolution_flags", flags);
            fire.putBoolean("evolution_ready", hasEssenceForRoute(bottle));
            if (route.environment() == Environment.DRAGON_END && !player.isCreative()) {
                player.getOffhandItem().shrink(1);
                player.getInventory().placeItemBackInInventory(new ItemStack(Items.GLASS_BOTTLE));
            }
            player.getInventory().setChanged();
            FireDiscovery.discover(player, route.to());
            player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_evolved",
                    Component.translatable(cn.blockforge.generated.generatedmod.ExoticFireColorRegistry.nameKey(route.to()))), false);
            return true;
        }
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_evolution_not_ready"), true);
        return false;
    }
    private FireEvolution() {}
}
