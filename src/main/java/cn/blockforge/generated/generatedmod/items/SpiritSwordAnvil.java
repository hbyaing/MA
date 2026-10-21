package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Vanilla rejects two non-damageable items before merging enchants; books keep the vanilla path. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class SpiritSwordAnvil {
    @SubscribeEvent public static void combine(AnvilUpdateEvent event) {
        if (!event.getOutput().isEmpty()) return;
        ItemStack left = event.getLeft(), right = event.getRight();
        if (!(left.getItem() instanceof SpiritSwordItem sword) || !left.is(right.getItem())
                || left.getCount() != 1 || right.getCount() != 1) return;
        var existing = EnchantmentHelper.getEnchantments(left);
        int extraCost = 0;
        boolean changed = false;
        for (var entry : EnchantmentHelper.getEnchantments(right).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!sword.canApplyAtEnchantingTable(left, enchantment)) continue;
            boolean compatible = true;
            for (Enchantment other : existing.keySet()) {
                if (other != enchantment && !enchantment.isCompatibleWith(other)) { compatible = false; extraCost++; }
            }
            if (!compatible) continue;
            int previous = existing.getOrDefault(enchantment, 0);
            int level = Math.min(enchantment.getMaxLevel(), previous == entry.getValue()
                    ? previous + 1 : Math.max(previous, entry.getValue()));
            if (level <= previous) continue;
            existing.put(enchantment, level);
            extraCost += level * switch (enchantment.getRarity()) {
                case COMMON -> 1; case UNCOMMON -> 2; case RARE -> 4; case VERY_RARE -> 8;
            };
            changed = true;
        }
        if (!changed) return;
        ItemStack output = left.copy();
        String name = event.getName();
        if (name == null || name.isBlank()) {
            if (output.hasCustomHoverName()) { output.resetHoverName(); extraCost++; }
        } else if (!name.equals(output.getHoverName().getString())) {
            output.setHoverName(Component.literal(name)); extraCost++;
        }
        EnchantmentHelper.setEnchantments(existing, output);
        output.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(Math.max(left.getBaseRepairCost(), right.getBaseRepairCost())));
        int cost = (int) Math.min(Integer.MAX_VALUE, (long) left.getBaseRepairCost() + right.getBaseRepairCost() + extraCost);
        if (cost >= 40 && !event.getPlayer().getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        event.setCost(cost);
        event.setMaterialCost(1);
        event.setOutput(output);
    }

    private SpiritSwordAnvil() {}
}
