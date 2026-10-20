package cn.blockforge.generated.generatedmod.client.weaponflame;

import cn.blockforge.generated.generatedmod.network.packet.cultivation.WeaponFlameHitPacket;

/** Protocol 63 compatibility endpoint; impact fragments are retired pending a new visual design. */
public final class WeaponFlameHitEffects {
    public static void accept(WeaponFlameHitPacket packet) {
        // Intentionally no geometry, particle emission or retained state.
        // Target ignition is rendered separately from the synchronized exotic-burn snapshot.
    }
    private WeaponFlameHitEffects() {}
}
