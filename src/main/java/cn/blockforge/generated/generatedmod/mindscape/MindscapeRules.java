package cn.blockforge.generated.generatedmod.mindscape;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side policy for the body/external world boundary of a mindscape. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MindscapeRules {
    private MindscapeRules() {}
    private record AuditStamp(int changes,int selected) {}
    private static final java.util.Map<ServerPlayer,AuditStamp> AUDITS=new java.util.WeakHashMap<>();
    @SubscribeEvent public static void equipmentChanged(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)AUDITS.remove(player);
    }


    public static boolean isInMindscape(Player player) {
        return player != null && player.level().dimension() == MindscapeManager.DIMENSION;
    }

    public static boolean hasActiveSession(Player player) {
        if (!(player instanceof ServerPlayer server)) return isInMindscape(player);
        var data = GeneratedMod.data(server);
        return isInMindscape(server) || (data != null && data.wuxingxiantu$getMindscapeData().hasUUID("session"));
    }

    public static boolean isExternalSealed(Player player) {
        if (!(player instanceof ServerPlayer server)) return false;
        if (GeneratedMod.data(server) == null) return false;
        return MindscapeExternalSnapshot.isSealed(server);
    }

    public static boolean isManifestedItemAllowed(Player player, ItemStack stack) {
        return isInMindscape(player) && !stack.isEmpty()
                && stack.getItem() instanceof HeartSwordItem
                && HeartSwordItem.isOwnedBy(stack, player.getUUID());
    }

    public static boolean canUseExternalItem(Player player, ItemStack stack) {
        return (!isInMindscape(player) && !isExternalSealed(player)) || isManifestedItemAllowed(player, stack);
    }

    public static boolean canUseContainer(Player player) {
        return !isInMindscape(player) && !isExternalSealed(player);
    }

    public static boolean canPickupItem(Player player) {
        return !isInMindscape(player) && !isExternalSealed(player);
    }

    public static boolean canDropItem(Player player) {
        return !isInMindscape(player) && !isExternalSealed(player);
    }

    public static boolean canInteractWithEntity(Player player) {
        return !isInMindscape(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !isInMindscape(player) || !isExternalSealed(player)) return;
        // Container/item interaction guards stay immediate; changed inventory is audited next tick.
        // A ten-tick fallback catches external mods that mutate stacks without setChanged().
        if(player.containerMenu!=player.inventoryMenu)player.closeContainer();
        var inventory=player.getInventory();
        var stamp=new AuditStamp(inventory.getTimesChanged(),inventory.selected);
        if(player.tickCount%10!=0&&stamp.equals(AUDITS.get(player)))return;
        AUDITS.put(player,stamp);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            boolean mainHeart = slot == player.getInventory().selected && slot < 9
                    && isManifestedItemAllowed(player, stack);
            if (!stack.isEmpty() && !mainHeart) player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        for (var equipment : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (equipment.getType() == net.minecraft.world.entity.EquipmentSlot.Type.ARMOR
                    || equipment == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                if (!player.getItemBySlot(equipment).isEmpty()) player.setItemSlot(equipment, ItemStack.EMPTY);
            }
        }
        if (player.containerMenu != player.inventoryMenu) player.closeContainer();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void itemUse(PlayerInteractEvent.RightClickItem event) {
        interruptMeditation(event.getEntity());
        if (!canUseExternalItem(event.getEntity(), event.getItemStack())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void blockUse(PlayerInteractEvent.RightClickBlock event) {
        interruptMeditation(event.getEntity());
        if (isInMindscape(event.getEntity())) {
            if(event.getEntity() instanceof ServerPlayer player)
                cn.blockforge.generated.generatedmod.boss.combat.BossCombatEvents.anchor(player,event.getPos());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityUse(PlayerInteractEvent.EntityInteract event) {
        interruptMeditation(event.getEntity());
        if (isInMindscape(event.getEntity())) {
            if(event.getEntity() instanceof ServerPlayer player)
                cn.blockforge.generated.generatedmod.boss.combat.BossCombatEvents.interact(player,event.getTarget());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityUseAt(PlayerInteractEvent.EntityInteractSpecific event) {
        interruptMeditation(event.getEntity());
        if (isInMindscape(event.getEntity())) {
            if(event.getEntity() instanceof ServerPlayer player)
                cn.blockforge.generated.generatedmod.boss.combat.BossCombatEvents.interact(player,event.getTarget());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        interruptMeditation(event.getEntity());
        if (isInMindscape(event.getEntity()) && !isManifestedItemAllowed(event.getEntity(), event.getEntity().getMainHandItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void toss(ItemTossEvent event) {
        interruptMeditation(event.getPlayer());
        if (!canDropItem(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pickup(EntityItemPickupEvent event) {
        if (!canPickupItem(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && isInMindscape(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void place(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) interruptMeditation(player);
        if (event.getEntity() instanceof Player player && isInMindscape(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void itemEntity(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getLevel().dimension() == MindscapeManager.DIMENSION
                && event.getEntity() instanceof ItemEntity) event.setCanceled(true);
    }

    private static void interruptMeditation(Player player) {
        if (player instanceof ServerPlayer server && !isInMindscape(player)) {
            var data = GeneratedMod.data(server);
            if (data != null && data.wuxingxiantu$isMeditating()) MindscapeManager.cancelMeditation(server);
        }
    }
}
