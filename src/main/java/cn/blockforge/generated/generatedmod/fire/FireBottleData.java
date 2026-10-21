package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.plants.HerbProperty;
import cn.blockforge.generated.generatedmod.plants.SpiritPlantBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/** All cultivation state belongs to this bottle's NBT, never a player or an Item singleton. */
public final class FireBottleData {
    public static final String KEY = "FireCultivation";
    public static final double MAX_ESSENCE = 1000000.0D;
    public enum Essence {
        METAL(HerbProperty.METAL), WOOD(HerbProperty.WOOD), WATER(HerbProperty.WATER),
        FIRE(HerbProperty.FIRE), EARTH(HerbProperty.EARTH), YIN(HerbProperty.YIN),
        YANG(HerbProperty.YANG), SOUL(HerbProperty.SOUL);
        public final HerbProperty property;
        Essence(HerbProperty property) { this.property = property; }
        public String key() { return name().toLowerCase(java.util.Locale.ROOT) + "_essence"; }
    }

    public static int tier(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof FireSeedItem seed) || seed.tier() == 0) return 0;
        if (stack.hasTag() && stack.getTag().contains(KEY)) {
            var definition = FireCatalog.id(stack.getTag().getCompound(KEY).getString("fire_id"));
            return definition == null ? 0 : definition.tier();
        }
        return seed.tier();
    }

    public static CompoundTag view(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getCompound(KEY) : new CompoundTag();
    }

    public static CompoundTag initialize(ItemStack stack) {
        int tier = tier(stack);
        if (tier == 0 || stack.getCount() != 1) throw new IllegalArgumentException("Not a single fire bottle");
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(KEY)) {
            CompoundTag fire = new CompoundTag();
            fire.putString("fire_id", FireCatalog.tier(tier).key().toString());
            for (Essence essence : Essence.values()) fire.putDouble(essence.key(), 0);
            fire.putDouble("total_essence", 0);
            fire.putIntArray("evolution_flags", new int[0]);
            root.put(KEY, fire);
        }
        CompoundTag fire = root.getCompound(KEY);
        if (!fire.hasUUID("BottleId")) fire.putUUID("BottleId", UUID.randomUUID());
        return fire;
    }

    public static ItemStack fresh(int tier) {
        ItemStack stack = new ItemStack(FireSeedItem.seedForTier(tier));
        initialize(stack);
        return stack;
    }

    public static double essence(ItemStack stack, Essence essence) {
        double value = view(stack).getDouble(essence.key());
        return Double.isFinite(value) ? Math.max(0, Math.min(MAX_ESSENCE, value)) : 0;
    }

    public static double[] medicinalEssence(List<ItemStack> consumed) {
        double[] result = new double[Essence.values().length];
        for (ItemStack stack : consumed) {
            if (!(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof SpiritPlantBlock plant)) continue;
            for (Essence essence : Essence.values()) {
                if (plant.definition().properties.contains(essence.property)) result[essence.ordinal()] += stack.getCount();
            }
        }
        return result;
    }

    public static void reward(ItemStack bottle, double[] contribution, ResourceLocation formula,
                              double multiplier, long now, String dimension) {
        CompoundTag fire = initialize(bottle);
        boolean recent = fire.getString("last_formula").equals(formula.toString())
                && fire.getString("last_dimension").equals(dimension)
                && now >= fire.getLong("last_brew_tick") && now - fire.getLong("last_brew_tick") <= 12000L;
        int repeats = recent ? Math.min(100, fire.getInt("repeat_count") + 1) : 0;
        double decay = repeatDecay(repeats);
        double total = 0;
        for (Essence essence : Essence.values()) {
            double gain = Math.max(0, contribution[essence.ordinal()]) * multiplier * decay;
            double value = Math.min(MAX_ESSENCE, essence(bottle, essence) + gain);
            fire.putDouble(essence.key(), value);
            total += value;
        }
        fire.putDouble("total_essence", total);
        fire.putString("last_formula", formula.toString());
        fire.putString("last_dimension", dimension);
        fire.putLong("last_brew_tick", now);
        fire.putInt("repeat_count", repeats);
        fire.putBoolean("evolution_ready", FireEvolution.hasEssenceForRoute(bottle));
    }

    public static double repeatDecay(int repeats) { return 1.0D / (1.0D + Math.max(0,Math.min(3,repeats))); }
    private FireBottleData() {}
}
