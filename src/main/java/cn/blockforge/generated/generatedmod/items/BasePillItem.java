package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.ModSounds;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.alchemy.PillEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** 所有丹药的公共规则：最大堆叠 64、无冷却、食用动画、灵力消耗与服务端效果。 */
public abstract class BasePillItem extends Item {
    /** 用户音效约 0.704 秒，按 20 tick/秒同步为 14 tick。 */
    private static final int EAT_DURATION_TICKS = 14;
    private final int manaCostPercent;

    protected BasePillItem(int ignoredCooldownSeconds, int manaCostPercent, Properties properties) {
        this(ignoredCooldownSeconds, manaCostPercent, properties, false);
    }

    protected BasePillItem(int ignoredCooldownSeconds, int manaCostPercent,
                           Properties properties, boolean ignoredSingleStack) {
        super(properties.stacksTo(64));
        this.manaCostPercent = Math.max(0, Math.min(100, manaCostPercent));
    }

    @Override
    public final InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var pill=PillData.read(stack);
        if(!level.isClientSide&&pill!=null&&pill.pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CULTIVATION
                &&!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,GeneratedMod.data(player),true))return InteractionResultHolder.fail(stack);
        player.getCooldowns().removeCooldown(this);
        if (!level.isClientSide && !ProgressionGate.canUsePill(player, stack)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_PILL, ProgressionGate.requiredRealmForPill(stack));
            return InteractionResultHolder.fail(stack);
        }
        if (PillData.read(stack) == null && !canStartEating(level, player)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public final int getUseDuration(ItemStack stack) { return EAT_DURATION_TICKS; }

    @Override
    public final UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }

    @Override
    public final SoundEvent getEatingSound() { return ModSounds.PILL_SWALLOW.get(); }

    @Override
    public final ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;
        if (level.isClientSide) {
            player.getCooldowns().removeCooldown(this);
            player.stopUsingItem();
            return stack.copy();
        }

        ItemStack result = stack.copy();
        try {
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return result;
            if (!ProgressionGate.canUsePill(player, stack)) {
                ProgressionGate.deny(player, ProgressionGate.MSG_PILL, ProgressionGate.requiredRealmForPill(stack));
                return result;
            }
            if (PillData.read(stack) != null) {
                if(PillData.read(stack).pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CULTIVATION
                        &&!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,true))return result;
                if (!PillEffects.apply(player, data, stack)) {
                    player.displayClientMessage(Component.translatable("message.wuxingxiantu.pill_state_invalid"), true);
                    return result;
                }
                result = PillData.normalize(result);
                if (!player.getAbilities().instabuild) result.shrink(1);
                if (player instanceof ServerPlayer serverPlayer) GeneratedMod.sync(serverPlayer);
                return result;
            }
            int manaCost = Math.max(0,
                    Math.round(CultivationLogic.maxAura(data) * manaCostPercent / 100.0F));
            if (data.wuxingxiantu$getAura() < manaCost || !applyEffect(player, data)) return result;
            if (manaCost > 0) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - manaCost);
            if (consumesOnUse() && !player.getAbilities().instabuild) result.shrink(1);
            Component message = successMessage(player, data);
            if (message != null) player.displayClientMessage(message, true);
            if (player instanceof ServerPlayer serverPlayer) GeneratedMod.sync(serverPlayer);
            return result;
        } finally {
            player.getCooldowns().removeCooldown(this);
            player.stopUsingItem();
            if (player instanceof ServerPlayer serverPlayer) {
                Network.stopUsingItem(serverPlayer, this, 0);
            }
        }
    }

    private boolean canStartEating(Level level, Player player) {
        CultivationData data = GeneratedMod.data(player);
        if (level.isClientSide) {
            if (data == null) return true;
            int manaCost = Math.max(0,
                    Math.round(CultivationLogic.maxAura(data) * manaCostPercent / 100.0F));
            return data.wuxingxiantu$getAura() >= manaCost;
        }
        if (data == null) {
            stopClientUse(player);
            return false;
        }
        int manaCost = Math.max(0,
                Math.round(CultivationLogic.maxAura(data) * manaCostPercent / 100.0F));
        if (data.wuxingxiantu$getAura() < manaCost) {
            player.displayClientMessage(
                    Component.translatable("message.wuxingxiantu.pill_mana_missing", manaCost), true);
            stopClientUse(player);
            return false;
        }
        return true;
    }

    private static void stopClientUse(Player player) {
        if (player instanceof ServerPlayer serverPlayer) Network.stopUsingItem(serverPlayer, null, 0);
    }

    /**
     * 增加同类效果的剩余时间；重复服用不会继续提高 amplifier。
     * 若玩家已有更高等级的同类效果，则保留更高等级并只追加时长。
     */
    protected final void extendEffect(Player player, MobEffect effect, int addedDuration, int amplifier) {
        extendEffect(player, effect, addedDuration, amplifier, false, true, true);
    }

    protected final void extendEffect(Player player, MobEffect effect, int addedDuration, int amplifier,
                                      boolean ambient, boolean visible, boolean showIcon) {
        if (addedDuration <= 0) return;
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && current.isInfiniteDuration()) return;
        long combined = (long) addedDuration + (current == null ? 0 : Math.max(0, current.getDuration()));
        int duration = (int) Math.min(Integer.MAX_VALUE, combined);
        int effectAmplifier = current == null ? amplifier : Math.max(current.getAmplifier(), amplifier);
        boolean effectAmbient = current == null ? ambient : current.isAmbient();
        boolean effectVisible = current == null ? visible : current.isVisible();
        boolean effectIcon = current == null ? showIcon : current.showIcon();
        if (current != null) player.removeEffect(effect);
        player.addEffect(new MobEffectInstance(
                effect, duration, effectAmplifier, effectAmbient, effectVisible, effectIcon));
    }

    /** 丹药默认食用一次后消耗一枚；兼容复用物品可覆盖为 false。 */
    protected boolean consumesOnUse() { return true; }

    /** 执行具体效果；返回 false 表示当前状态不能使用且不消耗灵力。 */
    protected abstract boolean applyEffect(Player player, CultivationData data);

    /** 返回成功提示；特殊丹药可以返回 null 并自行显示多条结果信息。 */
    protected Component successMessage(Player player, CultivationData data) { return null; }

    @Override public Component getName(ItemStack stack) {
        PillData.View view = PillData.read(stack);
        return view == null ? super.getName(stack) : cn.blockforge.generated.generatedmod.alchemy.MedicineTier.explicit(stack)
                ? Component.translatable("pill.wuxingxiantu.ranked_name",cn.blockforge.generated.generatedmod.alchemy.MedicineTier.name(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.of(stack)),PillData.name(view))
                : PillData.name(view);
    }

    @Override public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> lines,
                                         net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        PillData.View view = PillData.read(stack);
        if (view == null) return;
        if(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.explicit(stack))lines.add(Component.translatable(
                "pill.wuxingxiantu.medicine_tier",cn.blockforge.generated.generatedmod.alchemy.MedicineTier.name(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.of(stack)))
                .withStyle(net.minecraft.ChatFormatting.GOLD));
        lines.add(Component.translatable("pill.wuxingxiantu.quality",
                view.pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CALMING?Component.translatable("quality.wuxingxiantu.special"):
                Component.translatable("quality.wuxingxiantu." + view.grade().ordinal())).withStyle(style->style.withColor(
                view.pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CALMING?0xC99CEB:cn.blockforge.generated.generatedmod.text.ItemTextPalette.pillGrade(view.grade().ordinal()))));
        lines.add(Component.translatable("pill.wuxingxiantu.effect." + view.pill().id, view.grade().value())
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.translatable("pill.wuxingxiantu.requirement",
                CultivationLogic.realmName(ProgressionGate.requiredRealmForPill(stack))).withStyle(net.minecraft.ChatFormatting.GOLD));
        if (view.pill() == cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CULTIVATION) {
            lines.add(Component.literal("修为增加：" + cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.cultivationGain(view.cultivationTier(),view.grade().value()))
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            lines.add(Component.translatable("pill.wuxingxiantu.cultivation_tier", CultivationLogic.realmName(view.cultivationTier()))
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }
}
