package cn.blockforge.generated.generatedmod;

import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.nbt.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import net.minecraft.resources.ResourceLocation;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyFollowupGameTests {
    @GameTest(template="empty") public static void absorbedLibrarySurvivesRealRemovalAndPlacement(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,1,1));
        var block=(XuanlingAnvilBlock)WuXingBlocks.XUANLING_ANVIL.get();var state=block.defaultBlockState().setValue(XuanlingAnvilBlock.DAMAGE,2);
        level.setBlock(pos,state,3);var memory=AnvilTemplateMemory.get(level);
        var template=new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);template.getOrCreateTag().putString("Variant","test-data");
        h.assertTrue(memory.learn(pos,template),"Template not absorbed");
        // Vanilla mining asks for drops after onRemove; exercise exactly that order.
        level.removeBlock(pos,false);
        var drops=net.minecraft.world.level.block.Block.getDrops(state,level,pos,null);
        h.assertTrue(drops.size()==1&&drops.get(0).getTag().getList(AnvilTemplateMemory.ITEM_TAG,10).size()==1,"Mined item lost templates");
        h.assertTrue(memory.at(pos).isEmpty(),"Removed position retained library");
        var target=h.absolutePos(new BlockPos(3,1,1));level.setBlock(target,block.defaultBlockState(),3);block.setPlacedBy(level,target,block.defaultBlockState(),null,drops.get(0));
        h.assertTrue(memory.at(target).size()==1&&"test-data".equals(memory.at(target).get(0).getTag().getString("Variant")),"Replaced block lost template NBT");
        var restored=AnvilTemplateMemory.load(memory.save(new CompoundTag()));h.assertTrue(restored.at(target).size()==1,"Restart lost template library");
        h.assertTrue(!restored.learn(target,template),"Duplicate template accepted");h.succeed();
    }
    @GameTest(template="empty") public static void recipeDefinedModItemTemplateIsSupported(GameTestHelper h){
        var manager=h.getLevel().getRecipeManager();var old=new java.util.ArrayList<Recipe<?>>(manager.getRecipes());
        try{
            var recipes=new java.util.ArrayList<Recipe<?>>(old);
            recipes.add(new SmithingTransformRecipe(new ResourceLocation("wuxingxiantu","qa_mod_template"),
                    Ingredient.of(WuXingItems.GLAZED_CRYSTAL.get()),Ingredient.of(Items.IRON_SWORD),Ingredient.of(Items.DIAMOND),new ItemStack(Items.DIAMOND_SWORD)));
            manager.replaceRecipes(recipes);
            var template=new ItemStack(WuXingItems.GLAZED_CRYSTAL.get());
            h.assertTrue(!(template.getItem() instanceof SmithingTemplateItem)&&AnvilTemplateMemory.isTemplate(h.getLevel(),template),"Recipe-defined plain mod item rejected");
            // This checks recipe-defined learning, not persistence at a reused world coordinate.
            var pos=h.absolutePos(new BlockPos(1,1,1));h.assertTrue(new AnvilTemplateMemory().learn(pos,template),"Mod template not learned");
            h.assertTrue(!AnvilTemplateMemory.isTemplate(h.getLevel(),new ItemStack(Items.DIRT)),"Unrelated item accepted");
        }finally{manager.replaceRecipes(old);}h.succeed();
    }
    @GameTest(template="empty") public static void wasteOddsMatchPersistedOutcomeAndAshRecipe(GameTestHelper h){
        var list=h.getLevel().getRecipeManager().getAllRecipesFor(cn.blockforge.generated.generatedmod.registry.ModRecipes.ALCHEMY_TYPE.get());
        h.assertTrue(!list.isEmpty(),"No alchemy recipes loaded");
        for(var recipe:list){
            int rate=AlchemyFailureOdds.percent(recipe,recipe.furnaceTier(),recipe.minFireTier(),false);
            h.assertTrue(rate>=2&&rate<=25,"Formula can guarantee success or unbounded failure");
            h.assertTrue(AlchemyFailureOdds.percent(recipe,5,9,false)<=rate,"Better furnace/fire worsens chance");
        }
        var recipe=list.get(0);var random=net.minecraft.util.RandomSource.create(4231);int failures=0;
        int rate=AlchemyFailureOdds.percent(recipe,recipe.furnaceTier(),recipe.minFireTier(),false);
        for(int i=0;i<10000;i++){var out=AlchemyFailureOdds.roll(recipe,recipe.getResultItem(h.getLevel().registryAccess()),recipe.furnaceTier(),recipe.minFireTier(),random);if(out.isEmpty())failures++;}
        h.assertTrue(Math.abs(failures-rate*100)<170,"Actual roll differs from UI chance");
        var fixed=PillData.create(PillCatalog.CALMING,PillData.Grade.PERFECT,PillData.Element.METAL,0);
        h.assertTrue(!fixed.hasTag()||!fixed.getTag().contains("Grade"),"Special pill stores variable grade");
        var display=cn.blockforge.generated.generatedmod.text.ItemGradeResolver.resolve(fixed,0);
        h.assertTrue(display!=null&&display.grade().getString().contains(net.minecraft.network.chat.Component.translatable("quality.wuxingxiantu.special").getString()),"Special grade tooltip wrong");
        var crafting=new net.minecraft.world.inventory.TransientCraftingContainer(new net.minecraft.world.inventory.AbstractContainerMenu(null,0){
            public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}
            public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}
        },2,2);
        crafting.setItem(0,new ItemStack(WuXingItems.PILL_ASH.get()));crafting.setItem(1,new ItemStack(WuXingItems.SPIRIT_LIQUID_BOTTLE.get()));
        var ink=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,crafting,h.getLevel());
        h.assertTrue(ink.isPresent()&&ink.get().assemble(crafting,h.getLevel().registryAccess()).is(WuXingItems.SPIRIT_INK.get()),"Ash cannot craft spirit ink");h.succeed();
    }
    @GameTest(template="empty") public static void ashResultPersistsAndIsCollectedOnce(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlock(pos,WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState(),3);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),3));
        furnace.reserve(FreeAlchemy.recipe(furnace));furnace.startBrewing(0,100);
        h.assertTrue(furnace.failExperiment()&&furnace.hasResult()&&furnace.getItem(0).isEmpty(),"Failure did not consume materials and create ash");
        var saved=furnace.saveWithoutMetadata();furnace.load(saved);
        var player=new net.minecraftforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"AshCollect"));
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        h.assertTrue(furnace.collectResult(player)==1&&furnace.collectResult(player)==0,"Ash cannot be collected or duplicated");
        h.assertTrue(player.getInventory().countItem(WuXingItems.PILL_ASH.get())==1,"Collected wrong ash amount");h.succeed();
    }
}
