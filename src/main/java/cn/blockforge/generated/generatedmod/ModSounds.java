package cn.blockforge.generated.generatedmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途自定义音效注册。 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GeneratedMod.MOD_ID);

    public static final RegistryObject<SoundEvent> PILL_SWALLOW = SOUNDS.register(
            "pill_swallow", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(GeneratedMod.MOD_ID, "pill_swallow")));

    private ModSounds() {}
}
