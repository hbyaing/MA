package cn.blockforge.generated.generatedmod;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

/** Per-anvil library. Full stacks preserve modded template NBT and travel with its block item. */
public final class AnvilTemplateMemory extends SavedData {
    public static final String ITEM_TAG="AbsorbedTemplates";
    private final Map<Long,List<ItemStack>> templates=new HashMap<>();
    public static AnvilTemplateMemory get(ServerLevel level){return level.getDataStorage().computeIfAbsent(AnvilTemplateMemory::load,AnvilTemplateMemory::new,"wuxing_anvil_templates");}
    public static boolean isTemplate(Level level,ItemStack stack){
        return !stack.isEmpty()&&(stack.getItem() instanceof SmithingTemplateItem||level.getRecipeManager()
                .getAllRecipesFor(RecipeType.SMITHING).stream().anyMatch(recipe->recipe.isTemplateIngredient(stack)));
    }
    public List<ItemStack> at(BlockPos pos){return templates.getOrDefault(pos.asLong(),List.of()).stream().map(ItemStack::copy).toList();}
    public boolean learn(BlockPos pos,ItemStack stack){
        if(stack.isEmpty())return false;
        var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if(!(stack.getItem() instanceof SmithingTemplateItem)&&(server==null||!isTemplate(server.overworld(),stack)))return false;
        var list=templates.computeIfAbsent(pos.asLong(),ignored->new ArrayList<>());
        if(list.size()>=64||list.stream().anyMatch(existing->ItemStack.isSameItemSameTags(existing,stack)))return false;
        list.add(stack.copyWithCount(1));setDirty();return true;
    }
    public void clear(BlockPos pos){if(templates.remove(pos.asLong())!=null)setDirty();}
    public void writeToItem(BlockPos pos,ItemStack item){
        var list=new ListTag();for(var template:at(pos))list.add(template.save(new CompoundTag()));
        if(!list.isEmpty())item.getOrCreateTag().put(ITEM_TAG,list);
    }
    public void restoreFromItem(ServerLevel level,BlockPos pos,ItemStack item){
        clear(pos);if(!item.hasTag())return;
        var list=item.getTag().getList(ITEM_TAG,Tag.TAG_COMPOUND);
        for(int i=0;i<Math.min(64,list.size());i++){
            var stack=ItemStack.of(list.getCompound(i));
            if(isTemplate(level,stack))learn(pos,stack);
        }
    }
    public static AnvilTemplateMemory load(CompoundTag tag){
        var memory=new AnvilTemplateMemory();var rows=tag.getList("Libraries",Tag.TAG_COMPOUND);
        for(int i=0;i<rows.size();i++){
            var row=rows.getCompound(i);var pos=BlockPos.of(row.getLong("Pos"));
            var stacks=row.getList("Stacks",Tag.TAG_COMPOUND);
            for(int j=0;j<Math.min(64,stacks.size());j++)memory.learn(pos,ItemStack.of(stacks.getCompound(j)));
            var ids=row.getList("Items",Tag.TAG_STRING);
            for(int j=0;j<Math.min(64,ids.size());j++){
                var id=ResourceLocation.tryParse(ids.getString(j));
                if(id!=null)memory.learn(pos,new ItemStack(BuiltInRegistries.ITEM.get(id)));
            }
        }
        return memory;
    }
    @Override public CompoundTag save(CompoundTag tag){
        var rows=new ListTag();templates.forEach((pos,stacks)->{
            var row=new CompoundTag();row.putLong("Pos",pos);var list=new ListTag();
            stacks.forEach(stack->list.add(stack.save(new CompoundTag())));row.put("Stacks",list);rows.add(row);
        });tag.put("Libraries",rows);return tag;
    }
}
