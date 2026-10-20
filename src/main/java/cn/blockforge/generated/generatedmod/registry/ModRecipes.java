package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.AlchemyRecipe;
import cn.blockforge.generated.generatedmod.RefiningRecipe;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途配方类型与序列化器注册。 */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GeneratedMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GeneratedMod.MOD_ID);

    public static final RegistryObject<RecipeType<AlchemyRecipe>> ALCHEMY_TYPE =
            RECIPE_TYPES.register("alchemy", () -> RecipeType.simple(
                    new ResourceLocation(GeneratedMod.MOD_ID, "alchemy")));
    public static final RegistryObject<RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER =
            RECIPE_SERIALIZERS.register("alchemy", AlchemyRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<RefiningRecipe>> REFINING_TYPE =
            RECIPE_TYPES.register("refining", () -> RecipeType.simple(
                    new ResourceLocation(GeneratedMod.MOD_ID, "refining")));
    public static final RegistryObject<RecipeSerializer<RefiningRecipe>> REFINING_SERIALIZER =
            RECIPE_SERIALIZERS.register("refining", RefiningRecipe.Serializer::new);

    private ModRecipes() {}
}
