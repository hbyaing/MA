package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.AlchemyRecipe;
import cn.blockforge.generated.generatedmod.plants.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;

/** Formula-free experimentation accepts only spirit herbs and consumes the submitted batch. */
public final class FreeAlchemy {
    private record RecipeSnapshot(java.util.List<AlchemyRecipe> source, java.util.List<AlchemyRecipe> sorted) {}
    private static final java.util.Map<net.minecraft.world.item.crafting.RecipeManager,RecipeSnapshot> RECIPE_CACHE =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    /** Compare recipe object identities so replaceRecipes/reload invalidates even in the same tick. */
    public static java.util.List<AlchemyRecipe> sortedRecipes(net.minecraft.world.level.Level level) {
        if(level==null)return java.util.List.of();
        var manager=level.getRecipeManager();
        var source=manager.getAllRecipesFor(cn.blockforge.generated.generatedmod.registry.ModRecipes.ALCHEMY_TYPE.get());
        synchronized(RECIPE_CACHE) {
            var cached=RECIPE_CACHE.get(manager);
            if(cached!=null && cached.source().equals(source))return cached.sorted();
            var sorted=source.stream().sorted(java.util.Comparator.comparing(recipe->recipe.getId().toString())).toList();
            RECIPE_CACHE.put(manager,new RecipeSnapshot(java.util.List.copyOf(source),sorted));return sorted;
        }
    }
    public static int openingCost(AlchemyRecipe recipe, cn.blockforge.generated.generatedmod.CultivationData data) {
        return recipe!=null ? recipe.auraCost(data) : Math.max(1,Math.round(
                cn.blockforge.generated.generatedmod.items.CultivationLogic.maxAura(data)*.05F));
    }
    /** A free batch must account for every submitted item, rather than match a subset. */
    public static AlchemyRecipe attemptRecipe(Container container, net.minecraft.world.level.Level level) {
        if (level == null) return null;
        return attemptRecipe(container,sortedRecipes(level),level);
    }
    public static AlchemyRecipe attemptRecipe(Container container, java.util.List<AlchemyRecipe> recipes,
                                              net.minecraft.world.level.Level level) {
        return recipes
                .stream().filter(recipe -> recipe.matches(container, level)).filter(recipe -> {
                    int[] reserved = cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.allocate(container,recipe.reservation(container)).counts();
                    for (int i=0;i<8;i++) if (reserved[i]!=container.getItem(i).getCount()) return false;
                    return true;
                }).findFirst().orElse(null);
    }
    /** Progression formulas retain their knowledge requirement during experimentation. */
    public static boolean requiresFormula(AlchemyRecipe recipe) {
        var pill = PillData.read(recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY));
        return pill != null && pill.pill().progression;
    }
    public static boolean unstable(AlchemyRecipe recipe, int furnaceTier, int fireTier,
                                   boolean realmAllowed, boolean formulaKnown) {
        return recipe != null && recipe.furnaceTier() > furnaceTier;
    }
    public static boolean failedBatch(AlchemyRecipe recipe, int fireTier, boolean realmAllowed, boolean formulaKnown) {
        return recipe == null || !recipe.acceptsFire(fireTier) || !realmAllowed
                || requiresFormula(recipe) && !formulaKnown;
    }
    public static final ResourceLocation ID = new ResourceLocation("wuxingxiantu", "free_alchemy");
    public static final int SUCCESS_PERCENT = 25;
    private static final int[] GRADE_WEIGHTS = {850,120,25,5};
    public static double gradePercent(PillData.Grade grade) { return GRADE_WEIGHTS[grade.ordinal()] / 10D; }
    public static AlchemyRecipe recipe(Container container) {
        var ingredients = NonNullList.<Ingredient>create();
        var counts = new ArrayList<Integer>();
        int[] properties = new int[HerbProperty.values().length];
        for (int i = 0; i < 8; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem block) || !(block.getBlock() instanceof SpiritPlantBlock herb)) return null;
            ingredients.add(Ingredient.of(stack)); counts.add(stack.getCount());
            for (var property : herb.definition().properties) properties[property.ordinal()] += stack.getCount();
        }
        if (ingredients.isEmpty()) return null;
        // Only ordinary utility pills are reachable; progression pills still need their formulas.
        PillCatalog pill = PillCatalog.AURA;
        int best = 0;
        HerbProperty[] directions = {HerbProperty.RECOVERY, HerbProperty.CLEANSE, HerbProperty.WIND, HerbProperty.BODY_REFINE};
        PillCatalog[] products = {PillCatalog.GOLDEN_WOUND, PillCatalog.CLEAR_HEART, PillCatalog.GALE, PillCatalog.DIAMOND_BODY};
        for (int i = 0; i < directions.length; i++) if (properties[directions[i].ordinal()] > best) {
            best = properties[directions[i].ordinal()]; pill = products[i];
        }
        // Higher-realm utility outcomes keep their original realm gate.
        return new AlchemyRecipe(ID, ingredients, counts, PillData.create(pill, PillData.Grade.LOW, PillData.Element.METAL, 0),
                0, 1200, 5, false, ID, 1, 0, pill.minimumRealm, -1, .25D);
    }
    public static ItemStack roll(ItemStack source, RandomSource random) {
        return roll(source,random,0);
    }
    public static ItemStack roll(ItemStack source,RandomSource random,int preparedBonus) {
        int chance=SUCCESS_PERCENT+Math.max(0,Math.min(cn.blockforge.generated.generatedmod.HerbPreparation.MAX_BONUS,preparedBonus));
        if (random.nextInt(100) >= chance) return ItemStack.EMPTY;
        int draw = random.nextInt(1000);
        ItemStack result = source.copy();
        for (var grade : PillData.Grade.values()) {
            draw -= GRADE_WEIGHTS[grade.ordinal()];
            if (draw < 0) { result.getOrCreateTag().putString("Grade", grade.name()); break; }
        }
        return result;
    }
    private FreeAlchemy() {}
}
