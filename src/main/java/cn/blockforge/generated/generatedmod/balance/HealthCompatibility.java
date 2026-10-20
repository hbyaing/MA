package cn.blockforge.generated.generatedmod.balance;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/** Widen the shared vanilla attribute before instances cache it; all health APIs stay vanilla. */
public final class HealthCompatibility {
    private HealthCompatibility() {}
    public static void initialize() {
        RangedAttribute health = (RangedAttribute) Attributes.MAX_HEALTH;
        // Preserve a larger bound installed by another mod. Includes giant/physique/robe bonuses.
        health.maxValue = Math.max(health.maxValue, 1_000_000.0D);
        if(!Double.isFinite(health.maxValue)||health.maxValue<1_000_000.0D) {
            com.mojang.logging.LogUtils.getLogger().error("Invalid vanilla MAX_HEALTH upper bound: {}",health.maxValue);
            throw new IllegalStateException("MAX_HEALTH range extension failed");
        }
        com.mojang.logging.LogUtils.getLogger().info("Vanilla MAX_HEALTH upper bound: {} (shared by all entities)",health.maxValue);
    }
}
