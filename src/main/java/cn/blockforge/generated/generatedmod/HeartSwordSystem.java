package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeRules;
import cn.blockforge.generated.generatedmod.combat.HeartForgeContracts;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 心剑的服务端权威状态机、心契清理与持续耗灵。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeartSwordSystem {
    @SubscribeEvent public static void sources(cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent event) {
        event.register("heart-sword",sink->sink.add(WuXingItems.HEART_SWORD.get(),
                new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence("system",
                        new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"heart_sword_manifest"),
                        java.util.List.of("empty main hand; no existing owned heart sword; six percent mana; temporary owned item"),
                        ProgressionGate.QI_CONDENSATION,true,java.util.List.of())));
    }
    private static final float SUMMON_COST = 0.06F;
    private static final float FORGE_COST = 0.10F;
    private static final float HEART_MAINTENANCE = 0.0015F;
    private static final float FORGED_MAINTENANCE = 0.0010F;
    private static final int SUMMON_TICKS = cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline.duration(0);
    private static final int DISPERSE_TICKS = cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline.duration(1);
    private static final int FORGE_TICKS = cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline.duration(2);
    private static final int UNFORGE_TICKS = cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline.duration(3);
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final String PENDING_COST_TAG="wuxingxiantu_heart_pending_mana";

    private enum Action { SUMMON, DISPERSE, FORGE, UNFORGE }
    private record Pending(Action action, long finishAt, ItemStack stack, float cost) {}

    private HeartSwordSystem() {}

    @SubscribeEvent
    public static void inventoryClosed(net.minecraftforge.event.entity.player.PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) validateInventory(player);
    }

    @SubscribeEvent
    public static void itemPickedUp(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) validateInventory(player);
    }

    public static void request(ServerPlayer player) {
        if (player == null || !player.isAlive() || PENDING.containsKey(player.getUUID())) return;
        CultivationData data = GeneratedMod.data(player);
        if (data == null || !ProgressionGate.isAtLeastRealm(data, ProgressionGate.QI_CONDENSATION)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_realm"), true);
            return;
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof HeartSwordItem) {
            if (HeartSwordItem.isOwnedBy(main, player.getUUID())) begin(player, Action.DISPERSE, main, 0.0F, DISPERSE_TICKS);
            else clearHeartStacks(player);
            return;
        }
        if (main.isEmpty()) {
            if (findOwnedHeart(player) != null) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_already"), true);
                return;
            }
            float cost = data.wuxingxiantu$getMaxMana() * SUMMON_COST;
            if (!consume(data, cost)) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"), true);
                return;
            }
            begin(player, Action.SUMMON, ItemStack.EMPTY, cost, SUMMON_TICKS);
            return;
        }
        if (main.getItem() instanceof SpiritSwordItem sword) {
            if(!player.getOffhandItem().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_empty_offhand"),true);
                return;
            }
            if(SpiritSwordItem.isHeartForged(main)&&player.getUUID().equals(SpiritSwordItem.heartOwner(main))
                    &&!HeartForgeContracts.active(player,main))SpiritSwordItem.clearHeartForge(main);
            if (inMindscape(player)) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_mindscape"), true);
                return;
            }
            if (SpiritSwordItem.isHeartForged(main)) {
                if (player.getUUID().equals(SpiritSwordItem.heartOwner(main))) {
                    begin(player, Action.UNFORGE, main, 0.0F, UNFORGE_TICKS);
                } else {
                    player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_other_owner"), true);
                }
                return;
            }
            float cost = data.wuxingxiantu$getMaxMana() * FORGE_COST;
            if (!consume(data, cost)) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"), true);
                return;
            }
            begin(player, Action.FORGE, main, cost, FORGE_TICKS);
            return;
        }
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_invalid"), true);
    }

    private static boolean inMindscape(Player player) {
        return player.level().dimension() == cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.DIMENSION;
    }

    private static boolean consume(CultivationData data, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F || data.wuxingxiantu$getCurrentMana() < amount) return false;
        data.wuxingxiantu$setCurrentMana(data.wuxingxiantu$getCurrentMana() - amount);
        return true;
    }

    private static void begin(ServerPlayer player, Action action, ItemStack stack, float cost, int duration) {
        PENDING.put(player.getUUID(), new Pending(action, player.level().getGameTime() + duration, stack.copy(), cost));
        if(cost>0)player.getPersistentData().putFloat(PENDING_COST_TAG,cost);
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartSwordVisualPacket(player.getId(), (byte) action.ordinal()));
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null) {
            if (!player.isAlive()) { PENDING.remove(player.getUUID());player.getPersistentData().remove(PENDING_COST_TAG); return; }
            if (!pendingValid(player, pending)) { refund(player, pending.cost()); cancelVisual(player); PENDING.remove(player.getUUID()); return; }
            var motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 0.94D, motion.y, motion.z * 0.94D);
            if (player.level().getGameTime() >= pending.finishAt()) {
                if (complete(player, pending)) PENDING.remove(player.getUUID());
                else { refund(player, pending.cost()); PENDING.remove(player.getUUID()); }
            }
        }
        if (player.tickCount % 20 == 0) {
            boolean heart=validateInventory(player);
            if(player.tickCount % 20 == 0)maintain(player,heart);
        }
    }

    private static void cancelVisual(ServerPlayer player) {
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartSwordVisualPacket(player.getId(), (byte)-1));
    }

    private static boolean pendingValid(ServerPlayer player, Pending pending) {
        ItemStack main = player.getMainHandItem();
        return switch (pending.action()) {
            case SUMMON -> main.isEmpty();
            case DISPERSE -> ItemStack.matches(main, pending.stack()) && HeartSwordItem.isOwnedBy(main, player.getUUID());
            case FORGE -> ItemStack.matches(main, pending.stack()) && main.getItem() instanceof SpiritSwordItem
                    && !SpiritSwordItem.isHeartForged(main) && player.getOffhandItem().isEmpty();
            case UNFORGE -> ItemStack.matches(main, pending.stack()) && main.getItem() instanceof SpiritSwordItem
                    && HeartForgeContracts.active(player,main) && player.getOffhandItem().isEmpty();
        };
    }

    private static boolean complete(ServerPlayer player, Pending pending) {
        ItemStack main = player.getMainHandItem();
        switch (pending.action()) {
            case SUMMON -> { if (!main.isEmpty() || findOwnedHeart(player) != null) return false; player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, HeartSwordItem.create(player.getUUID())); }
            case DISPERSE -> { if (!ItemStack.matches(main, pending.stack()) || !HeartSwordItem.isOwnedBy(main, player.getUUID())) return false; player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY); }
            case FORGE -> { if (!ItemStack.matches(main, pending.stack()) || !(main.getItem() instanceof SpiritSwordItem)) return false; HeartForgeContracts.bind(player,main); clearHeartStacks(player); }
            case UNFORGE -> { if (!ItemStack.matches(main, pending.stack()) || !(main.getItem() instanceof SpiritSwordItem)) return false; HeartForgeContracts.revoke(player); SpiritSwordItem.clearHeartForge(main); }
        }
        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.55F, 1.5F);
        player.getPersistentData().remove(PENDING_COST_TAG);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_" + pending.action().name().toLowerCase()), true);
        GeneratedMod.sync(player);
        return true;
    }

    private static void refund(ServerPlayer player, float cost) {
        if (!Float.isFinite(cost)||cost <= 0.0F) return;
        CultivationData data = GeneratedMod.data(player);
        if (data != null) {
            CultivationLogic.restoreMana(data,cost);
            player.getPersistentData().remove(PENDING_COST_TAG);
            GeneratedMod.sync(player);
        }
    }

    private static ItemStack findOwnedHeart(ServerPlayer player) {
        ItemStack found = null;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!HeartSwordItem.isOwnedBy(stack, player.getUUID())) continue;
            if (found != null) { player.getInventory().setItem(slot, ItemStack.EMPTY); continue; }
            found = stack;
        }
        return found;
    }

    private static boolean validateInventory(ServerPlayer player) {
        CultivationData data=GeneratedMod.data(player);
        if(data==null)return false;
        int owned = 0;
        ItemStack equipped=data.wuxingxiantu$getYujianStack();
        boolean forged=HeartForgeContracts.active(player,equipped);
        if(SpiritSwordItem.isHeartForged(equipped)&&!forged)SpiritSwordItem.clearHeartForge(equipped);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof HeartSwordItem) {
                boolean legalMindscapeSlot = !inMindscape(player)
                        || (slot == player.getInventory().selected && slot < 9
                        && MindscapeRules.isManifestedItemAllowed(player, stack));
                if (!HeartSwordItem.isOwnedBy(stack, player.getUUID()) || !legalMindscapeSlot || owned++ > 0)
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
            if (SpiritSwordItem.isHeartForged(stack)) {
                if(!HeartForgeContracts.active(player,stack)||forged)SpiritSwordItem.clearHeartForge(stack);
                else forged=true;
            }
        }
        return owned>0;
    }

    private static void maintain(ServerPlayer player,boolean heart) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return;
        boolean forged = data.wuxingxiantu$getHeartForgeContract()!=null;
        float cost = data.wuxingxiantu$getMaxMana() * ((heart ? HEART_MAINTENANCE : 0)+(forged ? FORGED_MAINTENANCE : 0));
        if (cost > 0.0F && !consume(data, cost)) {
            if (heart) clearHeartStacks(player);
            if (forged) { HeartForgeContracts.revoke(player); validateInventory(player); }
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_sword_exhausted"), true);
        }
        if (cost > 0.0F) GeneratedMod.sync(player);
    }

    private static void clearHeartStacks(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
            if (player.getInventory().getItem(slot).getItem() instanceof HeartSwordItem)
                player.getInventory().setItem(slot, ItemStack.EMPTY);
    }

    @SubscribeEvent public static void toss(ItemTossEvent event) {
        if (event.getEntity().getItem().getItem() instanceof HeartSwordItem) {
            event.setCanceled(true);
            event.getEntity().discard();
        }
    }

    @SubscribeEvent public static void itemJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ItemEntity item) {
            if(item.getItem().getItem() instanceof HeartSwordItem) {
                event.setCanceled(true);item.discard();
            } else if(SpiritSwordItem.isHeartForged(item.getItem())) {
                // A physical weapon survives dropping; only its temporary heart contract dissolves.
                var owner=event.getLevel().getServer().getPlayerList().getPlayer(SpiritSwordItem.heartOwner(item.getItem()));
                if(owner!=null&&HeartForgeContracts.active(owner,item.getItem())) {
                    HeartForgeContracts.revoke(owner);GeneratedMod.sync(owner);
                }
                SpiritSwordItem.clearHeartForge(item.getItem());
            }
        }
    }

    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearHeartStacks(player);
            HeartForgeContracts.revoke(player);
            PENDING.remove(player.getUUID());
            player.getPersistentData().remove(PENDING_COST_TAG);
        }
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        Pending pending=PENDING.remove(event.getEntity().getUUID());
        if (pending!=null && event.getEntity() instanceof ServerPlayer player) refund(player,pending.cost());
    }

    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Pending pending=PENDING.remove(player.getUUID());
        if (pending!=null) refund(player,pending.cost());
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player&&!PENDING.containsKey(player.getUUID()))
            refund(player,player.getPersistentData().getFloat(PENDING_COST_TAG));
    }

    @SubscribeEvent public static void stopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        for(var player:event.getServer().getPlayerList().getPlayers()) {
            var pending=PENDING.remove(player.getUUID());
            if(pending!=null) {refund(player,pending.cost());cancelVisual(player);}
        }
        PENDING.clear();
    }

    @SubscribeEvent public static void damage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getSource().getEntity() instanceof Player player)) return;
        if (player instanceof ServerPlayer server && PENDING.containsKey(server.getUUID())) { event.setCanceled(true); return; }
    }
}
