package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.SpiritLiquidLakeFeature;
import cn.blockforge.generated.generatedmod.plants.SpiritPlantFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途世界生成特性注册。 */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, GeneratedMod.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPIRIT_LIQUID_LAKE = FEATURES.register(
            "spirit_liquid_lake", SpiritLiquidLakeFeature::new);

    /** 灵植生态植被特性：按 rule 挑选合法落点，与手动种植共用硬性条件。 */
    public static final RegistryObject<Feature<SpiritPlantFeature.Config>> SPIRIT_PLANT = FEATURES.register(
            "spirit_plant", SpiritPlantFeature::new);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPIRIT_VEIN = FEATURES.register("spirit_vein", () -> new cn.blockforge.generated.generatedmod.world.SpiritVeinFeature(NoneFeatureConfiguration.CODEC));


    private ModFeatures() {}
    public static final RegistryObject<Feature<cn.blockforge.generated.generatedmod.fire.ExoticFireSourceFeature.Config>> EXOTIC_FIRE_SOURCE =
            FEATURES.register("exotic_fire_source", cn.blockforge.generated.generatedmod.fire.ExoticFireSourceFeature::new);
}
