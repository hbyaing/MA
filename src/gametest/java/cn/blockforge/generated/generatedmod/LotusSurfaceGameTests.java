package cn.blockforge.generated.generatedmod;

import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import cn.blockforge.generated.generatedmod.plants.*;

@GameTestHolder("wuxingxiantu_tribulation")
@PrefixGameTestTemplate(false)
public final class LotusSurfaceGameTests {
 @GameTest(template="empty") public static void rightClickPlantsAboveBothLiquids(GameTestHelper h) {
  var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,2,2));
  var p=new cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer(level);
  p.cleanupAfter(h,()->{});p.join();p.setPos(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5);p.setXRot(90);p.setYRot(0);
  var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("wuxingxiantu","clear_heart_lotus"));
  for(var liquid:new net.minecraft.world.level.block.Block[]{Blocks.WATER,WuXingBlocks.SPIRIT_LIQUID.get()}) {
   level.setBlock(pos.above(),Blocks.AIR.defaultBlockState(),3);level.setBlock(pos,liquid.defaultBlockState(),3);
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item,2));
   var result=item.use(level,p,net.minecraft.world.InteractionHand.MAIN_HAND);
   h.assertTrue(result.getResult().consumesAction(),"water ray use succeeds");
   h.assertTrue(level.getBlockState(pos.above()).getBlock() instanceof SpiritPlantBlock,"lotus floats above liquid without replacing it");
   h.assertTrue(level.getBlockState(pos).is(liquid)&&p.getMainHandItem().getCount()==1,"source is preserved and one lotus is consumed");
  }
  h.succeed();
 }
 @GameTest(template="empty") public static void lotusSurfaceAndGrowth(GameTestHelper h) {
  var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,3,2));
  var block=(SpiritPlantBlock)net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation("wuxingxiantu","clear_heart_lotus"));
  var state=block.defaultBlockState();var def=block.definition();
  level.setBlock(pos.below(),Blocks.DIRT.defaultBlockState(),3);
  h.assertTrue(!state.canSurvive(level,pos),"lotus rejects soil");
  level.setBlock(pos.below(),Blocks.WATER.defaultBlockState(),3);level.setBlock(pos,state,3);
  h.assertTrue(state.canSurvive(level,pos)&&def.growthOneIn(level,pos)==7,"ordinary source supports base growth");
  level.setBlock(pos.below(),WuXingBlocks.SPIRIT_LIQUID.get().defaultBlockState(),3);
  h.assertTrue(state.canSurvive(level,pos)&&def.growthOneIn(level,pos)==3,"spirit liquid supports enriched growth");
  level.setBlock(pos.below(),Blocks.AIR.defaultBlockState(),3);
  h.assertTrue(!(level.getBlockState(pos).getBlock() instanceof SpiritPlantBlock),"drained surface drops the lotus");
  h.succeed();
 }
}
