package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 灌顶大法：对低于自身一个大境界的玩家进行五秒引导，将灵气与风险一并传入。 */
public final class GuandingItem extends Item {
    private static final int CAST_TICKS = 100;
    private static final long COOLDOWN_TICKS = 30L * 24000L;
    private static final Map<UUID, CastState> ACTIVE = new HashMap<>();

    public GuandingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_target"), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.sidedSuccess(true);
        if (!(player instanceof ServerPlayer caster) || !(target instanceof ServerPlayer recipient)) {
            return InteractionResult.FAIL;
        }
        return beginCast(caster, recipient, stack, hand) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_target"), true);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return CAST_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof ServerPlayer caster) || level.isClientSide) return;
        CastState state = ACTIVE.get(caster.getUUID());
        ServerPlayer recipient = state == null || caster.getServer() == null
                ? null : caster.getServer().getPlayerList().getPlayer(state.recipient());
        if (state == null || recipient == null || caster.getItemInHand(state.hand()).getItem() != this
                || recipient.level() != caster.level() || recipient.distanceToSqr(caster) > 9.0D
                || caster.distanceToSqr(state.startX(), state.startY(), state.startZ()) > 0.09D) {
            cancel(caster, true);
            return;
        }
        caster.setDeltaMovement(0.0D, 0.0D, 0.0D);
        caster.hasImpulse = true;
        if (remainingUseTicks % 20 == 0) {
            caster.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.guanding_channeling", Math.max(1, (remainingUseTicks + 19) / 20)), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer caster) || level.isClientSide) return stack;
        CastState state = ACTIVE.remove(caster.getUUID());
        if (state == null) return stack;
        ServerPlayer recipient = caster.getServer() == null
                ? null : caster.getServer().getPlayerList().getPlayer(state.recipient());
        if (recipient == null || recipient.level() != caster.level() || recipient.distanceToSqr(caster) > 9.0D
                || caster.getItemInHand(state.hand()).getItem() != this) {
            caster.stopUsingItem();
            caster.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_failed"), true);
            return stack;
        }
        completeCast(caster, recipient, stack);
        caster.stopUsingItem();
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int remainingUseTicks) {
        if (!(livingEntity instanceof ServerPlayer caster) || level.isClientSide) return;
        if (remainingUseTicks > 0 && ACTIVE.containsKey(caster.getUUID())) cancel(caster, true);
    }

    /** 受击、死亡或退出时由主事件类调用，确保引导不会残留。 */
    public static void interrupt(Player player) {
        if (!(player instanceof ServerPlayer caster) || !ACTIVE.containsKey(caster.getUUID())) return;
        cancel(caster, true);
    }

    private static boolean beginCast(ServerPlayer caster, ServerPlayer recipient, ItemStack stack, InteractionHand hand) {
        CultivationData casterData = GeneratedMod.data(caster);
        CultivationData recipientData = GeneratedMod.data(recipient);
        if (casterData == null || recipientData == null) return false;
        if (casterData.wuxingxiantu$getRealm() < 2) {
            message(caster, "message.wuxingxiantu.guanding_need_realm");
            return false;
        }
        if (recipient == caster || recipientData.wuxingxiantu$getRealm() >= casterData.wuxingxiantu$getRealm()) {
            message(caster, "message.wuxingxiantu.guanding_realm_limit");
            return false;
        }
        if (recipient.distanceToSqr(caster) > 9.0D) {
            message(caster, "message.wuxingxiantu.guanding_range");
            return false;
        }
        long now = caster.level().getGameTime();
        if (casterData.wuxingxiantu$getGuandingCooldown() > now) {
            message(caster, "message.wuxingxiantu.guanding_cooldown");
            return false;
        }
        if (casterData.wuxingxiantu$getAura() < 1.0F) {
            message(caster, "message.wuxingxiantu.guanding_mana");
            return false;
        }
        if (ACTIVE.containsKey(caster.getUUID())) {
            message(caster, "message.wuxingxiantu.guanding_busy");
            return false;
        }
        ACTIVE.put(caster.getUUID(), new CastState(recipient.getUUID(), hand,
                caster.getX(), caster.getY(), caster.getZ()));
        caster.startUsingItem(hand);
        caster.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_started")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return true;
    }

    private static void completeCast(ServerPlayer caster, ServerPlayer recipient, ItemStack stack) {
        CultivationData casterData = GeneratedMod.data(caster);
        CultivationData recipientData = GeneratedMod.data(recipient);
        if (casterData == null || recipientData == null) return;
        float available = casterData.wuxingxiantu$getAura();
        float consumed = available * 0.50F;
        if (consumed < 1.0F) {
            caster.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_mana"), true);
            return;
        }
        casterData.wuxingxiantu$setAura(available - consumed);
        int oldStage = casterData.wuxingxiantu$getCultivationStage();
        if (oldStage > 0) casterData.wuxingxiantu$setCultivationStage(oldStage - 1);
        // 初期已经是当前大境界的最低小阶，灌顶不会跌出大境界。

        int perfect = 20;
        int normal = 45;
        int damaged = 25;
        int shattered = 10;
        // 目标灵气越接近满值，经脉越稳定；单次灌入量越大，反噬风险越高。
        float recipientCapacity = Math.max(1.0F, CultivationLogic.maxAura(recipientData));
        float recipientFullness = Math.min(1.0F, recipientData.wuxingxiantu$getAura() / recipientCapacity);
        int stability = Math.round(recipientFullness * 15.0F);
        perfect += stability;
        damaged -= stability / 2;
        shattered -= stability - stability / 2;
        float casterCapacity = Math.max(1.0F, CultivationLogic.maxAura(casterData));
        int pressure = Math.min(15, Math.round(consumed / casterCapacity * 15.0F));
        perfect -= pressure;
        normal -= pressure / 3;
        damaged += pressure / 2;
        shattered += pressure - pressure / 3 - pressure / 2;
        switch (recipientData.wuxingxiantu$getPhysique()) {
            case JIUQIAO -> { perfect += 15; shattered -= 5; }
            case WANLING -> { perfect += 20; shattered -= 10; }
            case TAIYIN, TAIYANG -> perfect += 5;
            case TAICHU -> { normal += 10; shattered += 5; }
            default -> { }
        }
        perfect = Math.max(0, perfect);
        normal = Math.max(0, normal);
        damaged = Math.max(0, damaged);
        shattered = Math.max(0, shattered);
        int total = perfect + normal + damaged + shattered;
        int roll = caster.getRandom().nextInt(Math.max(1, total));
        float fullTransfer = consumed * 0.30F;
        String outcome;
        if (roll < perfect) {
            recipientData.wuxingxiantu$setAura(recipientData.wuxingxiantu$getAura() + fullTransfer);
            recipientData.wuxingxiantu$setAuraBonus(recipientData.wuxingxiantu$getAuraBonus() + 2);
            recipientData.wuxingxiantu$setMeridianStatus(0);
            recipient.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 200, 0, false, true, true));
            outcome = "message.wuxingxiantu.guanding_perfect";
        } else if (roll < perfect + normal) {
            recipientData.wuxingxiantu$setAura(recipientData.wuxingxiantu$getAura() + fullTransfer);
            recipientData.wuxingxiantu$setMeridianStatus(0);
            outcome = "message.wuxingxiantu.guanding_normal";
        } else if (roll < perfect + normal + damaged) {
            recipientData.wuxingxiantu$setAura(recipientData.wuxingxiantu$getAura() + fullTransfer * 0.50F);
            recipientData.wuxingxiantu$setMeridianStatus(1);
            recipient.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.MERIDIAN_DAMAGED.get(), 20 * 60 * 10, 0, false, true, true));
            outcome = "message.wuxingxiantu.guanding_damaged";
        } else {
            recipientData.wuxingxiantu$setAuraBonus(recipientData.wuxingxiantu$getAuraBonus() - 5);
            recipientData.wuxingxiantu$setMeridianStatus(2);
            recipient.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.MERIDIAN_SHATTERED.get(), 20 * 60 * 30, 0, false, true, true));
            outcome = "message.wuxingxiantu.guanding_shattered";
        }
        if (recipientData.wuxingxiantu$getPhysique() == cn.blockforge.generated.generatedmod.PhysiqueType.QINGLIAN) {
            recipientData.wuxingxiantu$setAura(recipientData.wuxingxiantu$getAura()
                    + CultivationLogic.maxAura(recipientData) * 0.10F);
        }
        casterData.wuxingxiantu$setGuandingCooldown(caster.level().getGameTime() + COOLDOWN_TICKS);
        caster.getCooldowns().addCooldown(stack.getItem(), (int) Math.min(Integer.MAX_VALUE, COOLDOWN_TICKS));
        caster.displayClientMessage(Component.translatable(outcome,
                Math.round(fullTransfer), recipient.getName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        recipient.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_received",
                Math.round(fullTransfer), caster.getName()).withStyle(ChatFormatting.AQUA), false);
        GeneratedMod.sync(caster);
        GeneratedMod.sync(recipient);
    }

    private static void cancel(ServerPlayer caster, boolean notify) {
        if (ACTIVE.remove(caster.getUUID()) == null) return;
        caster.stopUsingItem();
        if (notify) caster.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.guanding_interrupted"), true);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private record CastState(UUID recipient, InteractionHand hand, double startX, double startY, double startZ) {}
}
