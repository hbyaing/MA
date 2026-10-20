package cn.blockforge.generated.generatedmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** 八卦炼丹炉配方：输入数量、炉品级、炼制时间和灵力消耗均由数据包定义。 */
public final class AlchemyRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final List<Integer> counts;
    private final ItemStack result;
    private final int furnaceTier;
    private final int cookTime;
    private final int auraPercent;
    private final boolean advanced;
    private final ResourceLocation formulaId;
    private final int minFireTier;
    private final int requiredFire;
    private final int minimumRealm;
    private final int fixedAuraCost;
    private final double essenceMultiplier;

    public AlchemyRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, List<Integer> counts,
                         ItemStack result, int furnaceTier, int cookTime, int auraPercent, boolean advanced) {
        this(id, ingredients, counts, result, furnaceTier, cookTime, auraPercent, advanced,
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()), 1, 0, 0, -1, 1.0D);
    }

    public AlchemyRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, List<Integer> counts,
                         ItemStack result, int furnaceTier, int cookTime, int auraPercent, boolean advanced,
                         ResourceLocation formulaId, int minFireTier, int requiredFire, int minimumRealm,
                         int fixedAuraCost, double essenceMultiplier) {
        if (ingredients.isEmpty() || ingredients.size() > AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT
                || counts.size() != ingredients.size() || result.isEmpty()) throw new IllegalArgumentException("Invalid alchemy recipe");
        if (counts.stream().anyMatch(count -> count < 1 || count > 64) || furnaceTier < 0 || furnaceTier > 5
                || minFireTier < 1 || minFireTier > 9 || requiredFire < 0 || requiredFire > 9
                || minimumRealm < 0 || minimumRealm > 8 || cookTime < 1 || fixedAuraCost < -1
                || !Double.isFinite(essenceMultiplier) || essenceMultiplier <= 0 || essenceMultiplier > 100)
            throw new IllegalArgumentException("Alchemy values out of range");
        this.id = id;
        this.ingredients = ingredients;
        this.counts = List.copyOf(counts);
        this.result = result;
        this.furnaceTier = furnaceTier;
        this.cookTime = cookTime;
        this.auraPercent = auraPercent;
        this.advanced = advanced;
        this.formulaId = formulaId;
        this.minFireTier = minFireTier;
        this.requiredFire = requiredFire;
        this.minimumRealm = minimumRealm;
        this.fixedAuraCost = fixedAuraCost;
        this.essenceMultiplier = essenceMultiplier;
    }

    public int furnaceTier() { return furnaceTier; }
    public int cookTime() { return cookTime; }
    public int auraPercent() { return auraPercent; }
    public boolean advanced() { return advanced; }
    public List<Integer> counts() { return counts; }
    public ResourceLocation formulaId() { return formulaId; }
    public int minimumRealm() { return minimumRealm; }
    public int minFireTier() { return minFireTier; }
    public int requiredFire() { return requiredFire; }
    public double essenceMultiplier() { return essenceMultiplier; }
    public boolean acceptsFire(int tier) { return tier >= minFireTier && (requiredFire == 0 || tier == requiredFire); }
    public int auraCost(CultivationData data) {
        return fixedAuraCost >= 0 ? fixedAuraCost : Math.max(1, Math.round(
                cn.blockforge.generated.generatedmod.items.CultivationLogic.maxAura(data) * auraPercent / 100F));
    }

    @Override public boolean matches(Container container, Level level) {
        return tryReservation(container).isPresent();
    }

    /** 按配方顺序消耗输入，调用前必须通过 matches。 */
    public List<ItemStack> consume(Container container) {
        if(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.explicit(result)) {
            int[] allocated=reservation(container);
            List<ItemStack> ranked=new ArrayList<>();
            for(int slot=0;slot<allocated.length;slot++)if(allocated[slot]>0){
                var stack=container.getItem(slot);ranked.add(stack.copyWithCount(allocated[slot]));stack.shrink(allocated[slot]);
            }
            container.setChanged();return List.copyOf(ranked);
        }
        List<ItemStack> consumed = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            int remaining = counts.get(i);
            for (int slot = 0; slot < Math.min(AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT, container.getContainerSize()) && remaining > 0; slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem
                        || !ingredients.get(i).test(stack)) continue;
                int used = Math.min(remaining, stack.getCount());
                if (used > 0) consumed.add(stack.copyWithCount(used));
                stack.shrink(used);
                remaining -= used;
            }
        }
        container.setChanged();
        return List.copyOf(consumed);
    }

    /** Reserve counts in the existing slots so the renderer keeps reading real ingredients. */
    public int[] reservation(Container container) {
        return tryReservation(container).orElseThrow(() -> new IllegalStateException("Invalid alchemy reservation"));
    }

    /** Non-mutating allocation query; selected formulas may lack inputs during UI previews. */
    public Optional<int[]> tryReservation(Container container) {
        int[] reserved = new int[AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT];
        int slots = Math.min(reserved.length, container.getContainerSize());
        var order=new ArrayList<Integer>();for(int slot=0;slot<slots;slot++)order.add(slot);
        if(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.explicit(result))
            order.sort(java.util.Comparator.comparingInt((Integer slot)->cn.blockforge.generated.generatedmod.plants.HerbAge.effectiveTier(container.getItem(slot))).reversed());
        for (int i=0;i<ingredients.size();i++) {
            int remaining=counts.get(i);
            for (int slot:order) {
                if(remaining<=0)break;
                ItemStack stack=container.getItem(slot);
                if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem
                        || !ingredients.get(i).test(stack)) continue;
                int used=Math.min(remaining,Math.max(0,stack.getCount()-reserved[slot]));
                reserved[slot]+=used; remaining-=used;
            }
            if (remaining>0) return Optional.empty();
        }
        return cn.blockforge.generated.generatedmod.alchemy.MedicineTier.accepts(result,container,reserved)?Optional.of(reserved):Optional.empty();
    }

    @Override public ItemStack assemble(Container container, RegistryAccess registryAccess) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.ALCHEMY_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.ALCHEMY_TYPE.get(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }

    public static final class Serializer implements RecipeSerializer<AlchemyRecipe> {
        @Override public AlchemyRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray input = json.getAsJsonArray("ingredients");
            if (input.isEmpty() || input.size() > 8) throw new JsonParseException("Alchemy recipe needs 1..8 ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            List<Integer> counts = new ArrayList<>();
            input.forEach(element -> {
                JsonObject entry = element.getAsJsonObject();
                ingredients.add(Ingredient.fromJson(entry));
                int count = entry.has("count") ? entry.get("count").getAsInt() : 1;
                if (count < 1 || count > 64) throw new JsonParseException("Ingredient count must be 1..64");
                counts.add(count);
            });
            JsonObject outputJson = json.getAsJsonObject("result");
            ItemStack result = ShapedResult.read(outputJson);
            var pill = cn.blockforge.generated.generatedmod.alchemy.PillData.read(result);
            if (pill != null) {
                var grade = json.has("result_grade")
                        ? cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.valueOf(json.get("result_grade").getAsString()) : pill.grade();
                var element = json.has("element")
                        ? cn.blockforge.generated.generatedmod.alchemy.PillData.Element.valueOf(json.get("element").getAsString()) : pill.element();
                int cultivationTier = json.has("cultivation_tier") ? json.get("cultivation_tier").getAsInt() : 0;
                int count = result.getCount();
                result = cn.blockforge.generated.generatedmod.alchemy.PillData.create(pill.pill(), grade, element, cultivationTier);
                result.setCount(count);
                if(json.has("medicine_tier"))cn.blockforge.generated.generatedmod.alchemy.MedicineTier.set(result,bounded(json,"medicine_tier",1,1,5));
            }
            int tier = Math.max(0, json.has("furnace_tier") ? json.get("furnace_tier").getAsInt() : 0);
            int time = Math.max(1, json.has("time") ? json.get("time").getAsInt() : 200);
            int aura = Math.max(0, json.has("aura_percent") ? json.get("aura_percent").getAsInt() : 5);
            boolean advanced = json.has("advanced") && json.get("advanced").getAsBoolean();
            ResourceLocation formula = json.has("formula_id") ? new ResourceLocation(json.get("formula_id").getAsString())
                    : net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem());
            int minFire = bounded(json, "min_fire_tier", 1, 1, 9);
            int fire = bounded(json, "required_fire", 0, 0, 9);
            int realm = bounded(json, "minimum_realm", 0, 0, 8);
            int fixedAura = bounded(json, "aura_cost", -1, -1, 1000000000);
            double multiplier = json.has("fire_essence_multiplier") ? json.get("fire_essence_multiplier").getAsDouble() : 1.0D;
            if (!Double.isFinite(multiplier) || multiplier <= 0 || multiplier > 100) throw new JsonParseException("Invalid essence multiplier");
            if (tier > 5) throw new JsonParseException("Furnace tier must be 0..5");
            return new AlchemyRecipe(id, ingredients, counts, result, tier, time, aura, advanced,
                    formula, minFire, fire, realm, fixedAura, multiplier);
        }

        private static int bounded(JsonObject json, String key, int fallback, int min, int max) {
            int value = json.has(key) ? json.get(key).getAsInt() : fallback;
            if (value < min || value > max) throw new JsonParseException("Out of range: " + key);
            return value;
        }

        @Override public AlchemyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            if (size < 1 || size > 8) throw new IllegalArgumentException("Invalid alchemy ingredient count");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            List<Integer> counts = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.fromNetwork(buffer));
                counts.add(buffer.readVarInt());
            }
            return new AlchemyRecipe(id, ingredients, counts, buffer.readItem(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readResourceLocation(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readInt(), buffer.readDouble());
        }

        @Override public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (int i = 0; i < recipe.ingredients.size(); i++) {
                recipe.ingredients.get(i).toNetwork(buffer);
                buffer.writeVarInt(recipe.counts.get(i));
            }
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.furnaceTier);
            buffer.writeVarInt(recipe.cookTime);
            buffer.writeVarInt(recipe.auraPercent);
            buffer.writeBoolean(recipe.advanced);
            buffer.writeResourceLocation(recipe.formulaId);
            buffer.writeVarInt(recipe.minFireTier);
            buffer.writeVarInt(recipe.requiredFire);
            buffer.writeVarInt(recipe.minimumRealm);
            buffer.writeInt(recipe.fixedAuraCost);
            buffer.writeDouble(recipe.essenceMultiplier);
        }
    }

    private static final class ShapedResult {
        private static ItemStack read(JsonObject json) {
            ResourceLocation id = new ResourceLocation(json.get("item").getAsString());
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
            if (item == null || item == net.minecraft.world.item.Items.AIR
                    || !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).equals(id)) {
                throw new JsonParseException("Unknown alchemy result: " + id);
            }
            return new ItemStack(item, json.has("count") ? Math.max(1, json.get("count").getAsInt()) : 1);
        }
    }
}
