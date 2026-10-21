package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.UUID;

/** All equipment uses the existing HeartOwner UUID; swords keep their existing heart-forge growth. */
public final class EquipmentBond {
    private EquipmentBond() {}

    public static UUID owner(ItemStack stack) { return HeartSwordItem.owner(stack); }
    public static boolean canUse(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty()) return stack.isEmpty();
        if (stack.hasTag() && stack.getTag().contains(HeartSwordItem.OWNER_TAG)
                && !stack.getTag().hasUUID(HeartSwordItem.OWNER_TAG)) return false;
        UUID owner = owner(stack);
        return owner == null || owner.equals(player.getUUID());
    }
    public static boolean canBind(Player player, ItemStack stack) { return !stack.isEmpty() && canUse(player, stack); }
    public static boolean canConsume(Player player, List<ItemStack> stacks) {
        return stacks.stream().allMatch(stack -> canUse(player, stack));
    }

    /** Call after ordinary WuxingCraft inheritance, and validate canConsume before consuming inputs. */
    public static ItemStack inheritOwner(ItemStack result, List<ItemStack> consumed) {
        UUID inherited = owner(result);
        for (ItemStack stack : consumed) {
            UUID source = owner(stack);
            if (source == null) continue;
            if (inherited != null && !inherited.equals(source)) return ItemStack.EMPTY;
            inherited = source;
        }
        if (inherited != null) {
            if (result.getCount() != 1) return ItemStack.EMPTY;
            result.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG, inherited);
        }
        return result;
    }

    public static boolean ordinary(ItemStack stack) {
        return EquipmentCrafting.eligible(stack) && !(stack.getItem() instanceof SpiritSwordItem);
    }

    /** Shift-right-click the idle forge core while holding an ordinary sword or armor. */
    public static boolean ritual(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (!ordinary(stack) || stack != player.getMainHandItem()) return false;
        var level = player.serverLevel();
        if (!player.isAlive() || player.isSpectator() || !player.isShiftKeyDown()
                || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > 64
                || !RefiningForgeBlock.isStructureComplete(level, pos)
                || !(level.getBlockEntity(pos) instanceof RefiningForgeBlockEntity forge)
                || forge.isCrafting() || forge.hasPendingReward()) return deny(player, "器台忙碌，待取出成品后再认主或温养。");
        if (!canBind(player, stack)) return deny(player, "此器已有主人，不能改绑、温养或祭炼。");
        int fire = RefiningForgeBlock.fireTier(level, pos);
        if (!ProgressionGate.canUseFire(player, fire)) return deny(player, "需放置本人可驾驭的异火。");
        var data = GeneratedMod.data(player);
        if (data == null) return true;
        var craft = EquipmentCrafting.read(stack);
        long now = level.getGameTime();
        if (craft.contains("BondLastTick") && now - craft.getLong("BondLastTick") < 60)
            return deny(player, "气息尚未平复，温养间隔为三息。");
        boolean claim = owner(stack) == null;
        int nurtured = Math.max(0, Math.min(100, craft.getInt("BondNurture")));
        boolean life = !claim && nurtured >= 100;
        if (life && craft.getBoolean("BondLife")) return deny(player, "本命祭炼已完成。");
        int requiredFire = life ? 3 : 1;
        if (fire < requiredFire || !ProgressionGate.canUseRefiningForge(player, RefiningForgeBlock.temperatureForTier(requiredFire)))
            return deny(player, life ? "本命祭炼需三阶以上异火及对应境界。" : "当前境界不足以认主温养。");
        int aura = claim ? 20 : life ? 100 : 10;
        int stones = claim ? 1 : life ? 3 : 0;
        if (data.wuxingxiantu$getAura() < aura || SpiritCurrency.value(player.getInventory().items) < stones)
            return deny(player, "需要灵力 " + aura + "、灵石价值 " + stones + "下品。");
        if (!SpiritCurrency.pay(player.getInventory(), stones))
            return deny(player, "背包无法容纳灵石找零，先腾出空间。");
        // The currency transaction replaces inventory copies, including the held equipment.
        stack = player.getMainHandItem();
        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - aura);
        var write = EquipmentCrafting.write(stack);
        write.putLong("BondLastTick", now);
        if (claim) {
            stack.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG, player.getUUID());
            player.displayClientMessage(Component.literal("认主完成；继续潜行持器右键器台可温养。"), true);
        } else if (life) {
            write.putBoolean("BondLife", true);
            write.putInt("Refine", Math.min(9, EquipmentCrafting.refine(stack) + 1));
            player.displayClientMessage(Component.literal("本命祭炼完成，器物精炼成长一阶（最高九阶）。"), true);
        } else {
            write.putInt("BondNurture", Math.min(100, nurtured + 5));
            if (stack.isDamageableItem()) stack.setDamageValue(Math.max(0, stack.getDamageValue() - 4));
            player.displayClientMessage(Component.literal("温养 " + Math.min(100, nurtured + 5) + "/100；灵剑仍沿用心铸与原有温养体系。"), true);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        GeneratedMod.sync(player);
        return true;
    }

    private static boolean deny(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
        return true;
    }
}
