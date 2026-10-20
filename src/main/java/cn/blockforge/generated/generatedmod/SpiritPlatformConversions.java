package cn.blockforge.generated.generatedmod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Supplier;

/** Small, provisional conversion table; registry suppliers resolve only when used. */
public final class SpiritPlatformConversions {
    public enum CompletionEffect { ETERNAL_FIRE, SPIRIT_INFUSION }

    public record Conversion(Supplier<? extends Item> input, Supplier<? extends Item> output,
                             int requiredAura, CompletionEffect effect) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.is(input.get());
        }

        public ItemStack result(int count) {
            return new ItemStack(output.get(), count);
        }
    }

    // Temporary balance values. Keep each conversion cost independent of storage capacity.
    private static final List<Conversion> CONVERSIONS = List.of(
            new Conversion(() -> Items.BONE_MEAL, WuXingItems.SPIRIT_FERTILIZER,
                    8, CompletionEffect.SPIRIT_INFUSION),
            new Conversion(() -> Items.DIRT, WuXingBlocks.SPIRIT_SOIL_ITEM,
                    24, CompletionEffect.SPIRIT_INFUSION),
            new Conversion(WuXingItems.SPIRIT_COAL, WuXingItems.ETERNAL_SPIRIT_COAL,
                    99, CompletionEffect.ETERNAL_FIRE),
            new Conversion(WuXingBlocks.SPIRIT_COAL_BLOCK_ITEM, WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK_ITEM,
                    891, CompletionEffect.ETERNAL_FIRE)
    );

    public static Conversion find(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Conversion conversion : CONVERSIONS) {
            if (conversion.matches(stack)) return conversion;
        }
        return null;
    }

    private SpiritPlatformConversions() {}
}
