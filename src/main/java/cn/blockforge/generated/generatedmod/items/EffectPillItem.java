package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class EffectPillItem extends BasePillItem {
    public enum Effect { CLEAR_HEART, CONTINUE_VEIN, GATHERING, CALMING, BIGU, TRIBULATION, FOUNDATION, CULTIVATION, INSIGHT, DIAMOND_BODY, BERSERK, GALE, PROFOUND_WATER, GOLDEN_WOUND }

    private final Effect effect;
    private final int tier;

    public EffectPillItem(Effect effect, int tier, Properties properties) {
        super(30, manaCostPercent(effect, tier), properties);
        this.effect = effect;
        this.tier = tier;
    }

    private static int manaCostPercent(Effect effect, int tier) {
        return switch (effect) {
            case CULTIVATION -> Math.min(20, Math.max(1, tier));
            case DIAMOND_BODY, BERSERK, GALE, PROFOUND_WATER -> 5;
            case GATHERING -> 3;
            default -> 0;
        };
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        switch (effect) {
            case CALMING -> {
                if(!(player instanceof net.minecraft.server.level.ServerPlayer server)
                        ||!cn.blockforge.generated.generatedmod.mindscape.MindscapeProgression.requestFromPill(server))return false;
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.calming_mindscape_ready"),false);
            }
            case CLEAR_HEART -> {
                // 清心只处理普通“道心数值”（精神/心神层面）。
                // 主线心魔入侵（dao_heart_unstable）是独立数据，绝不允许被这些丹药清除。
                if (data.wuxingxiantu$getDaoXin() == 0) return false;
                data.wuxingxiantu$setDaoXin(0);
            }
            case BIGU -> {
                if (data.wuxingxiantu$getRealm() >= 1) return false;
                long combined = (long) data.wuxingxiantu$getBiguRemainTime() + 72000L;
                data.wuxingxiantu$setBiguRemainTime((int) Math.min(Integer.MAX_VALUE, combined));
            }
            case CONTINUE_VEIN -> {
                if (data.wuxingxiantu$getMeridianStatus() == 0 && !player.hasEffect(MobEffects.WEAKNESS)) return false;
                data.wuxingxiantu$setMeridianStatus(0);
                if (player.hasEffect(MobEffects.WEAKNESS)) player.removeEffect(MobEffects.WEAKNESS);
                player.removeEffect(ModEffects.MERIDIAN_DAMAGED.get());
                player.removeEffect(ModEffects.MERIDIAN_SHATTERED.get());
            }
            case GATHERING -> extendEffect(player, MobEffects.REGENERATION, 72000, 0);
            case TRIBULATION -> { return true; }
            case FOUNDATION -> {
                // 这类“开灵根丹”会把灵根提升到 tier 品质，同样按品质走统一门槛。
                if (!ProgressionGate.canUsePill(player, tier)) {
                    ProgressionGate.deny(player, ProgressionGate.MSG_PILL, ProgressionGate.requiredRealmForRootQuality(tier));
                    return false;
                }
                int root = player.getRandom().nextInt(5);
                if ((data.wuxingxiantu$getRootMask() & (1 << root)) != 0) return false;
                data.wuxingxiantu$setRootMask(data.wuxingxiantu$getRootMask() | (1 << root));
                data.wuxingxiantu$setRootQuality(root, tier);
            }
            case CULTIVATION -> {
                if(!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,true))return false;
                if (data.wuxingxiantu$getCultivation() >= CultivationLogic.breakthroughRequirement(data)) return false;
                long gain = Math.max(1L, CultivationLogic.breakthroughRequirement(data) * tier / 100L);
                data.wuxingxiantu$setCultivation(data.wuxingxiantu$getCultivation() + gain);
            }
            case INSIGHT -> {
                if (data.wuxingxiantu$hasInsight()) return false;
                data.wuxingxiantu$setInsight(true);
            }
            case DIAMOND_BODY -> {
                extendEffect(player, MobEffects.DAMAGE_RESISTANCE, 1200, 1);
                extendEffect(player, MobEffects.ABSORPTION, 1200, 1);
            }
            case BERSERK -> {
                extendEffect(player, MobEffects.DAMAGE_BOOST, 900, 1);
                extendEffect(player, MobEffects.MOVEMENT_SPEED, 900, 1);
            }
            case GALE -> {
                extendEffect(player, MobEffects.MOVEMENT_SPEED, 1200, 2);
                extendEffect(player, MobEffects.JUMP, 1200, 1);
            }
            case PROFOUND_WATER -> {
                extendEffect(player, MobEffects.WATER_BREATHING, 2400, 0);
                extendEffect(player, MobEffects.NIGHT_VISION, 2400, 0);
                extendEffect(player, MobEffects.DIG_SPEED, 2400, 1);
            }
            case GOLDEN_WOUND -> {
                if (player.getHealth() >= player.getMaxHealth()) return false;
                player.heal(8.0F);
            }
        }
        return true;
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) {
        return Component.translatable("message.wuxingxiantu.pill_effect_" + effect.name().toLowerCase());
    }
}
