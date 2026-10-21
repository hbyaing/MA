package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.HeartMethodBalance;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/** Stable transient modifiers: changing methods removes the previous method's bonuses. */
public final class HeartMethodEffects {
    private static final UUID MODIFIER = UUID.fromString("fbc14240-6ecb-4b03-9046-61a2dfb2fb21");
    private HeartMethodEffects() {}
    public static void updateAttributes(Player player, CultivationData data) {
        String id = ProgressionGate.effectiveHeartMethod(player, data);
        update(player, Attributes.MAX_HEALTH, HeartMethodBalance.healthBonus(id), AttributeModifier.Operation.MULTIPLY_TOTAL);
        update(player, Attributes.ATTACK_DAMAGE, HeartMethodBalance.attackBonus(id), AttributeModifier.Operation.MULTIPLY_TOTAL);
        update(player, Attributes.MOVEMENT_SPEED, HeartMethodBalance.speedBonus(id), AttributeModifier.Operation.MULTIPLY_TOTAL);
        update(player, Attributes.KNOCKBACK_RESISTANCE, HeartMethodBalance.knockbackBonus(id), AttributeModifier.Operation.ADDITION);
        update(player, net.minecraftforge.common.ForgeMod.SWIM_SPEED.get(), HeartMethodBalance.swimBonus(id),
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        if ("xuanshui".equals(id) && player.isUnderWater()) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WATER_BREATHING, 45, 0, true, false, true));
        }
    }
    private static void update(Player player, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        var old = instance.getModifier(MODIFIER);
        if (old != null && old.getOperation() == operation && Math.abs(old.getAmount()-amount)<0.00001D) return;
        if (old != null) instance.removeModifier(MODIFIER);
        if (amount != 0.0D) instance.addTransientModifier(new AttributeModifier(MODIFIER,
                "wuxingxiantu.heart_method", amount, operation));
    }
}
