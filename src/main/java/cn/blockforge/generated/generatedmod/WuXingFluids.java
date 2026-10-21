package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** @deprecated 流体注册已迁移到 {@link ModFluids}。 */
@Deprecated
public final class WuXingFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = ModFluids.FLUID_TYPES;
    public static final DeferredRegister<Fluid> FLUIDS = ModFluids.FLUIDS;
    public static final RegistryObject<FluidType> SPIRIT_LIQUID_TYPE = ModFluids.SPIRIT_LIQUID_TYPE;
    public static final RegistryObject<ForgeFlowingFluid.Source> SPIRIT_LIQUID_SOURCE = ModFluids.SPIRIT_LIQUID_SOURCE;
    public static final RegistryObject<ForgeFlowingFluid.Flowing> SPIRIT_LIQUID_FLOWING = ModFluids.SPIRIT_LIQUID_FLOWING;

    private WuXingFluids() {}
}
