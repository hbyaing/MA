package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 化灵佩：右键将 10 点灵力转换为 1 点经验，以灵力球的形式呈现。
 * 手持时每 0.5 秒（10 tick）尝试再转换一次，直到灵力不足或松开。
 * 下蹲右键可调整转化倍率，每次 ×10，最高 ×100000；到达最高档后再按一次循环回 ×1。
 */
public final class HuaLingPeiItem extends Item {
    /** 每次转换消耗的灵力（倍率为 1 时）。 */
    public static final int AURA_PER_CONVERSION = 10;
    /** 每次转换获得的经验（倍率为 1 时）。 */
    public static final int EXPERIENCE_PER_CONVERSION = 1;
    /** 持续发动的间隔（tick）。 */
    private static final int CONVERT_INTERVAL_TICKS = 10;
    /** 转化倍率上限（档位：1、10、100、1000、10000、100000，然后循环回 1）。 */
    public static final int MAX_MULTIPLIER = 100000;
    private static final String MULTIPLIER_TAG = "ConversionMultiplier";

    public HuaLingPeiItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                adjustMultiplier(stack, player);
            } else {
                convert(player, stack, true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 下蹲右键：倍率 ×10，最高 ×100000；到达最高档后再次下蹲右键循环回 ×1。 */
    private static void adjustMultiplier(ItemStack stack, Player player) {
        int current = getMultiplier(stack);
        int next;
        if (current >= MAX_MULTIPLIER) {
            next = 1;
        } else {
            next = current * 10;
        }
        setMultiplier(stack, next);
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.hualing_pei.multiplier", next,
                next * AURA_PER_CONVERSION, next * EXPERIENCE_PER_CONVERSION), true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || !selected) return;
        // 打开任意容器界面时不持续发动，避免与界面交互冲突。
        if (player.containerMenu != player.inventoryMenu) return;
        if (player.level().getGameTime() % CONVERT_INTERVAL_TICKS == 0) {
            convert(player, stack, false);
        }
    }

    /** 服务端执行一次转换：扣 10×倍率 灵力、给 1×倍率 经验，并在玩家位置产生灵力球视觉。 */
    private static void convert(Player player, ItemStack stack, boolean notify) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return;
        int multiplier = getMultiplier(stack);
        int auraCost = AURA_PER_CONVERSION * multiplier;
        int expGain = EXPERIENCE_PER_CONVERSION * multiplier;
        if (data.wuxingxiantu$getAura() < auraCost) {
            // 持续发动时灵力耗尽不打扰玩家，仅在主动右键时提示。
            if (notify) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.spirit_insufficient", auraCost), true);
            }
            return;
        }
        if (!player.getAbilities().instabuild) {
            data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - auraCost);
        }
        player.giveExperiencePoints(expGain);
        // 灵力球视觉：以经验球同款普通粒子在掌中浮现（服务端广播给追踪玩家）。
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ(),
                    2, 0.10D, 0.06D, 0.10D, 0.0D);
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            GeneratedMod.sync(serverPlayer);
        }
    }

    /** 读取当前转化倍率（默认 1，钳制在 1 至上限之间）。 */
    public static int getMultiplier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 1
                : Math.min(MAX_MULTIPLIER, Math.max(1, tag.getInt(MULTIPLIER_TAG)));
    }

    private static void setMultiplier(ItemStack stack, int multiplier) {
        stack.getOrCreateTag().putInt(MULTIPLIER_TAG, multiplier);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int multiplier = getMultiplier(stack);
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.hualing_pei",
                AURA_PER_CONVERSION * multiplier, EXPERIENCE_PER_CONVERSION * multiplier));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.hualing_pei.multiplier", multiplier));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
