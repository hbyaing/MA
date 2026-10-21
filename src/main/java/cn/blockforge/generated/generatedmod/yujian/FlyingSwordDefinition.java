package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/** Per-sword tuning. Movement code stays shared while each sword can have its own feel. */
public record FlyingSwordDefinition(
        ResourceLocation itemId,
        float handScale,
        float orbitScale,
        float flightScale,
        double riderOffset,
        double baseSpeed,
        double sprintSpeed,
        double acceleration,
        double drag,
        double turnRate,
        double pitchResponse,
        double bankingStrength,
        double rollAcceleration,
        double maxRollSpeed,
        double spiralSpeedMultiplier,
        double impactDamage,
        float modelYaw,
        float modelPitch,
        float modelRoll,
        boolean baked3d,
        YujianTuning.Profile advanced
) {
    public boolean matches(Item item) {
        return item != null && itemId.equals(ForgeRegistries.ITEMS.getKey(item));
    }
}
