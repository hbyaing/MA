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

/** 炼器台配方；材料、灵力消耗和炼制时间由数据包定义。 */
public final class RefiningRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final List<Integer> counts;
    private final ItemStack result;
    private final int auraCost;
    private final int craftTime;
    // Legacy/internal fire-tier proxy retained for existing recipe JSON and packet compatibility.
    private final int temperature;

    public RefiningRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, List<Integer> counts,
                          ItemStack result, int auraCost, int craftTime, int temperature) {
        this.id = id;
        this.ingredients = ingredients;
        this.counts = List.copyOf(counts);
        this.result = result;
        this.auraCost = Math.max(0, auraCost);
        this.craftTime = Math.max(1, craftTime);
        this.temperature = Math.max(0, temperature);
    }

    public List<Integer> counts() { return counts; }
    public int auraCost() { return auraCost; }
    public int craftTime() { return craftTime; }
    public int temperature() { return temperature; }
    public int requiredFireTier() { return Math.max(1, (int) Math.ceil((temperature - 300.0D) / 100.0D)); }

    @Override public boolean matches(Container container, Level level) {
        return missing(container).isEmpty();
    }

    /**
     * 与 matches 相同的贪心匹配，但返回每种材料还缺多少：每项为 {材料下标, 缺少数量}。
     * 服务端与界面据此把“材料不足”具体到物品与数量，不再只给一句笼统提示。
     */
    public List<int[]> missing(Container container) {
        int[] available = new int[container.getContainerSize()];
        for (int slot = 0; slot < available.length; slot++) available[slot] = container.getItem(slot).getCount();
        List<int[]> missing = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            int needed = counts.get(i);
            for (int slot = 0; slot < available.length && needed > 0; slot++) {
                if (!ingredients.get(i).test(container.getItem(slot))) continue;
                int used = Math.min(needed, available[slot]);
                available[slot] -= used;
                needed -= used;
            }
            if (needed > 0) missing.add(new int[]{i, needed});
        }
        return missing;
    }

    /** 材料下标对应的展示样本（与配方书图标同源）；无样本时返回空。 */
    public ItemStack sample(int index) {
        if (index < 0 || index >= ingredients.size()) return ItemStack.EMPTY;
        ItemStack[] items = ingredients.get(index).getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0];
    }

    public void consume(Container container) {
        for (int i = 0; i < ingredients.size(); i++) {
            int remaining = counts.get(i);
            for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = container.getItem(slot);
                if (!ingredients.get(i).test(stack)) continue;
                int used = Math.min(remaining, stack.getCount());
                stack.shrink(used);
                remaining -= used;
            }
        }
        container.setChanged();
    }

    @Override public ItemStack assemble(Container container, RegistryAccess registryAccess) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.REFINING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.REFINING_TYPE.get(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }

    public static final class Serializer implements RecipeSerializer<RefiningRecipe> {
        @Override public RefiningRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray input = json.getAsJsonArray("ingredients");
            if (input == null || input.isEmpty()) throw new JsonParseException("Refining recipe needs ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            List<Integer> counts = new ArrayList<>();
            input.forEach(element -> {
                JsonObject entry = element.getAsJsonObject();
                ingredients.add(Ingredient.fromJson(entry));
                counts.add(entry.has("count") ? Math.max(1, entry.get("count").getAsInt()) : 1);
            });
            JsonObject output = json.getAsJsonObject("result");
            ResourceLocation resultId = new ResourceLocation(output.get("item").getAsString());
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(resultId);
            if (item == net.minecraft.world.item.Items.AIR
                    || !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).equals(resultId)) {
                throw new JsonParseException("Unknown refining result: " + resultId);
            }
            ItemStack result = new ItemStack(item, output.has("count") ? Math.max(1, output.get("count").getAsInt()) : 1);
            int aura = json.has("aura_cost") ? json.get("aura_cost").getAsInt() : 25;
            int time = json.has("time") ? json.get("time").getAsInt() : 200;
            int temperature = json.has("temperature") ? json.get("temperature").getAsInt() : 0;
            return new RefiningRecipe(id, ingredients, counts, result, aura, time, temperature);
        }

        @Override public RefiningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            List<Integer> counts = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.fromNetwork(buffer));
                counts.add(buffer.readVarInt());
            }
            return new RefiningRecipe(id, ingredients, counts, buffer.readItem(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt());
        }

        @Override public void toNetwork(FriendlyByteBuf buffer, RefiningRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (int i = 0; i < recipe.ingredients.size(); i++) {
                recipe.ingredients.get(i).toNetwork(buffer);
                buffer.writeVarInt(recipe.counts.get(i));
            }
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.auraCost);
            buffer.writeVarInt(recipe.craftTime);
            buffer.writeVarInt(recipe.temperature);
        }
    }
}
