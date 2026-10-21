package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.PhysiqueType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** 将体质表转换为可重复刷新的原版属性与效果。 */
public final class PhysiqueLogic {
    private static final UUID HEALTH = UUID.fromString("7d62d743-7b8a-4f74-9f40-b1a3a1d6b001");
    private static final UUID ARMOR = UUID.fromString("7d62d743-7b8a-4f74-9f40-b1a3a1d6b002");
    private static final UUID MANA = UUID.fromString("7d62d743-7b8a-4f74-9f40-b1a3a1d6b003");
    private static final UUID DAMAGE = UUID.fromString("7d62d743-7b8a-4f74-9f40-b1a3a1d6b004");
    private PhysiqueLogic() {}

    public static void apply(Player player, CultivationData data) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        PhysiqueType type = data.wuxingxiantu$getPhysique();
        boolean advanced = data.wuxingxiantu$getRealm() >= 3;
        update(health, HEALTH, "wuxingxiantu.physique_health",
                type == PhysiqueType.WANLING ? .25D : type == PhysiqueType.XUANHUANG ? (advanced ? .20D : .10D) : 0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        update(armor, ARMOR, "wuxingxiantu.physique_armor",
                type == PhysiqueType.XUANHUANG ? (advanced ? 4D : 2D) : 0D, AttributeModifier.Operation.ADDITION);
        update(damage, DAMAGE, "wuxingxiantu.physique_damage",
                type == PhysiqueType.WANLING ? .15D : type == PhysiqueType.ZHANWANG ? (advanced ? .15D : .08D) : 0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (health != null && player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    public static float manaMultiplier(CultivationData data) {
        return switch (data.wuxingxiantu$getPhysique()) {
            case JIUQIAO -> data.wuxingxiantu$getRealm() >= 3 ? 1.20F : 1.10F;
            case WANLING -> data.wuxingxiantu$getRealm() >= 3 ? 1.25F : 1.15F;
            case QINGLIAN -> data.wuxingxiantu$getRealm() >= 3 ? 1.10F : 1.0F;
            default -> 1.0F;
        };
    }

    public static float regenMultiplier(CultivationData data) {
        return switch (data.wuxingxiantu$getPhysique()) {
            case QINGLIAN -> data.wuxingxiantu$getRealm() >= 3 ? 1.25F : 1.15F;
            case JIUQIAO -> data.wuxingxiantu$getRealm() >= 3 ? 1.10F : 1.0F;
            case WANLING -> data.wuxingxiantu$getRealm() >= 3 ? 1.25F : 1.15F;
            default -> 1.0F;
        };
    }

    public static void tick(Player player, CultivationData data) {
        apply(player, data);
        PhysiqueType type = data.wuxingxiantu$getPhysique();
        if (type == PhysiqueType.WANLING) player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 45, 0, true, false, true));
    }

    private static void update(AttributeInstance instance, UUID id, String name, double amount, AttributeModifier.Operation operation) {
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null && existing.getOperation() == operation && Double.compare(existing.getAmount(), amount) == 0) return;
        if (existing != null) instance.removeModifier(id);
        if (amount != 0D) instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
    }
}
