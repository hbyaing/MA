package cn.blockforge.generated.generatedmod.client.weaponflame;

import net.minecraft.world.phys.Vec3;

/** Uncentered baked-model coordinates, before the ItemRenderer -0.5 translation. */
public record WeaponFlameAnchorProfile(Vec3 hiltAnchor,Vec3 tipAnchor,Vec3 widthAxis,float bladeWidth,float bladeThickness) {
    public float length(){return (float)hiltAnchor.distanceTo(tipAnchor);}
}
