package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** A held, interruptible channel is the only survival writer of learned fire qualifications. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class FireRefinement {
    public static final int CHANNEL_TICKS = 100;
    private record Channel(ItemStack stack, InteractionHand hand, int tier, Vec3 origin, String dimension, long started) {}
    private static final Map<UUID, Channel> ACTIVE = new HashMap<>();

    public static boolean start(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        int tier = FireBottleData.tier(stack);
        var data = GeneratedMod.data(player);
        if (stack.getCount() != 1 || data == null || !ProgressionGate.canRefineFire(player, tier)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_FIRE_REFINE, ProgressionGate.requiredRealmForFire(tier));
            return false;
        }
        if (data.wuxingxiantu$hasLearnedFire(tier)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_already_unlocked", tier), true);
            return false;
        }
        ACTIVE.put(player.getUUID(), new Channel(stack, hand, tier, player.position(),
                player.level().dimension().location().toString(), player.level().getGameTime()));
        player.startUsingItem(hand);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_refining"), true);
        return true;
    }

    public static boolean valid(ServerPlayer player, ItemStack stack) {
        Channel channel = ACTIVE.get(player.getUUID());
        return player.isAlive() && channel != null && channel.stack() == stack && stack.getCount() == 1
                && player.getItemInHand(channel.hand()) == stack
                && FireBottleData.tier(stack) == channel.tier()
                && player.position().distanceToSqr(channel.origin()) <= 0.25D
                && player.level().dimension().location().toString().equals(channel.dimension())
                && ProgressionGate.canRefineFire(player, channel.tier());
    }

    public static void finish(ServerPlayer player, ItemStack stack) {
        Channel channel = ACTIVE.get(player.getUUID());
        if (!valid(player, stack) || channel == null
                || player.level().getGameTime() - channel.started() < CHANNEL_TICKS) { cancel(player); return; }
        var data = GeneratedMod.data(player);
        if (data != null && data.wuxingxiantu$learnFire(channel.tier())) {
            FireBottleData.initialize(stack);
            FireDiscovery.discover(player, channel.tier());
            GeneratedMod.sync(player);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_refined"), true);
        }
        ACTIVE.remove(player.getUUID());
    }

    public static void cancel(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) != null) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.fire_refining_interrupted"), true);
        }
    }

    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if (event.getAmount() >= 2 && event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }
    private FireRefinement() {}
}
