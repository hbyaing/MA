package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.SpiritLiquidFluidType;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 灵液注册：Source 与 Flowing 使用不同的注册 ID，并共享同一组流体属性。 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, GeneratedMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, GeneratedMod.MOD_ID);

    public static final RegistryObject<FluidType> SPIRIT_LIQUID_TYPE = FLUID_TYPES.register(
            "spirit_liquid", () -> new SpiritLiquidFluidType(FluidType.Properties.create()
                    .descriptionId("block.wuxingxiantu.spirit_liquid")
                    .canSwim(true)
                    .canDrown(false)
                    .canHydrate(false)
                    .lightLevel(6)
                    .density(1000)
                    .viscosity(1000)));

    public static final RegistryObject<ForgeFlowingFluid.Source> SPIRIT_LIQUID_SOURCE = FLUIDS.register(
            "spirit_liquid_source", () -> new ForgeFlowingFluid.Source(properties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> SPIRIT_LIQUID_FLOWING = FLUIDS.register(
            "spirit_liquid_flowing", () -> new ForgeFlowingFluid.Flowing(properties()));

    private static ForgeFlowingFluid.Properties properties() {
        return new ForgeFlowingFluid.Properties(
                SPIRIT_LIQUID_TYPE,
                SPIRIT_LIQUID_SOURCE,
                SPIRIT_LIQUID_FLOWING)
                .bucket(WuXingItems.SPIRIT_LIQUID_BUCKET)
                .block(() -> (net.minecraft.world.level.block.LiquidBlock) WuXingBlocks.SPIRIT_LIQUID.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }

    private ModFluids() {}
}
