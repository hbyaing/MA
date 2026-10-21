package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class EquipmentBondEvents {
    private EquipmentBondEvents() {}

    /** Inventory shift-click and dispensers may equip armor without an item-use event. */
    public static void removeForeignArmor(ServerPlayer player) {
        boolean changed = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || EquipmentBond.canUse(player, stack)) continue;
            player.getAttributes().removeAttributeModifiers(stack.getAttributeModifiers(slot));
            player.setItemSlot(slot, ItemStack.EMPTY);
            player.getInventory().add(stack);
            if (!stack.isEmpty()) player.drop(stack, false);
            changed = true;
        }
        if (changed) {
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.literal("他人已认主的护甲无法穿戴，已归还背包；背包满时落在脚边。"), true);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer player) removeForeignArmor(player);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) removeForeignArmor(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        var stack = event.getEntity().getMainHandItem();
        // Existing forged spirit swords remain physical weapons for a nonowner; heart traits stay gated elsewhere.
        if (EquipmentBond.ordinary(stack) && !EquipmentBond.canUse(event.getEntity(), stack)) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        if (EquipmentBond.ordinary(event.getItemStack()) && !EquipmentBond.canUse(event.getEntity(), event.getItemStack())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if (EquipmentBond.ordinary(event.getItemStack()) && !EquipmentBond.canUse(event.getEntity(), event.getItemStack())) event.setCanceled(true);
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (stack.is(WuXingBlocks.REFINING_FORGE_CORE.get().asItem())) {
            event.getToolTip().add(Component.literal("炼制时潜行空手右键核心：烧胚→塑形→锻打→淬炼→铭纹").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.literal("每20%一阶段，中间三分之一为精准区；精准三次可提升精炼一阶").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.literal("空闲时潜行持普通武器或护甲右键核心：认主／温养／本命祭炼").withStyle(ChatFormatting.GRAY));
        }
        var owner = EquipmentBond.owner(stack);
        if (owner != null && EquipmentBond.ordinary(stack)) {
            boolean mine = event.getEntity() != null && owner.equals(event.getEntity().getUUID());
            event.getToolTip().add(Component.literal("主人 · " + (mine ? "你" : owner.toString())).withStyle(ChatFormatting.GOLD));
            var craft = EquipmentCrafting.read(stack);
            event.getToolTip().add(Component.literal(craft.getBoolean("BondLife") ? "本命祭炼已成" : "温养 " + Math.max(0, Math.min(100, craft.getInt("BondNurture"))) + "/100").withStyle(ChatFormatting.AQUA));
        } else if (EquipmentBond.ordinary(stack)) {
            event.getToolTip().add(Component.literal("可在空闲炼器台潜行持器右键认主：20灵力、1下品灵石").withStyle(ChatFormatting.GRAY));
        }
        var craft = EquipmentCrafting.read(stack);
        if (craft.contains("ForgeFinishQuality")) event.getToolTip().add(Component.literal("锻造成色 · "
                + RefiningOperation.FINISHES[Math.max(0, Math.min(3, craft.getInt("ForgeFinishQuality")))]).withStyle(ChatFormatting.AQUA));
    }
}
