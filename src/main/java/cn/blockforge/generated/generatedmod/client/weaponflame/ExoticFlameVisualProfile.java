package cn.blockforge.generated.generatedmod.client.weaponflame;

import net.minecraft.resources.ResourceLocation;

/** Ratios are relative to the blade; no world-sized flame constants. */
public record ExoticFlameVisualProfile(ResourceLocation fireId,Colors colors,Shape shape,Motion motion,Combat combat) {
    public record Colors(int core,int body,int edge,int spark,int glow) {}
    public record Shape(float flameWidth,float flameHeight,float coilRadius,float coilTurns,float attachmentSize) {}
    public record Motion(float flowSpeed,float coilSpeed,float flickerStrength,float turbulence) {}
    public record Combat(float attackStretch,float trailLength,float trailFadeTime,int hitPeelAmount,int scatterAmount) {}
}
