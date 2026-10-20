package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** @deprecated 配方注册已迁移到 {@link ModRecipes}。 */
@Deprecated
public final class WuXingRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = ModRecipes.RECIPE_TYPES;
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = ModRecipes.RECIPE_SERIALIZERS;
    public static final RegistryObject<RecipeType<AlchemyRecipe>> ALCHEMY_TYPE = ModRecipes.ALCHEMY_TYPE;
    public static final RegistryObject<RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER = ModRecipes.ALCHEMY_SERIALIZER;

    private WuXingRecipes() {}
}
