package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Optional Forge tests; these are not run by the no-build source delivery. */
@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class AlchemyResultGameTests {
    @GameTest(template="empty") public static void tierCapacityAndCenterCollection(GameTestHelper h){
        int lastCapacity=0,lastSpeed=0;for(int tier=0;tier<6;tier++){
            int capacity=cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.fuelCapacity(tier),speed=cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.injectionPerSecond(tier);
            h.assertTrue(capacity>lastCapacity&&speed>lastSpeed,"Capacity/speed did not strictly increase at tier "+tier);lastCapacity=capacity;lastSpeed=speed;
        }
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.getInventory().clearContent();
        var menu=new AlchemyFurnaceMenu(5,player.getInventory(),furnace);h.assertTrue(!menu.clickMenuButton(player,249),"Unfinished preview was collectible");
        furnace.startBrewing(0,1);h.assertTrue(furnace.complete(new ItemStack(net.minecraft.world.item.Items.DIAMOND,3)),"Completion failed");
        h.assertTrue(menu.clickMenuButton(player,249)&&player.getInventory().countItem(net.minecraft.world.item.Items.DIAMOND)==3&&!furnace.hasResult(),"Shift center collection lost/duplicated result");
        h.assertTrue(!menu.clickMenuButton(player,249),"Repeated collection duplicated result");h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=20) public static void cursorPickupPreservesTagsRemainderAndClose(GameTestHelper h){
        var f=furnace(h,10);
        AlchemyFurnaceStructure.refreshAround(h.getLevel(),f.getBlockPos());
        try(var p=new cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer(h.getLevel())){
            var pos=f.getBlockPos();p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            var menu=new AlchemyFurnaceMenu(5,p.getInventory(),f);p.containerMenu=menu;
            h.assertTrue(menu.stillValid(p),"Cursor fixture furnace is not formed or reachable");
            menu.setCarried(new ItemStack(Items.STONE));h.assertTrue(!menu.clickMenuButton(p,250)&&f.completedResult().getCount()==10,"Unrelated cursor item was replaced");
            menu.setCarried(f.completedResult().copyWithCount(61));h.assertTrue(menu.clickMenuButton(p,250)&&menu.getCarried().getCount()==64&&f.completedResult().getCount()==7,"Cursor merge lost remainder");
            h.assertTrue(!menu.clickMenuButton(p,250)&&f.completedResult().getCount()==7,"Full cursor consumed result");
            p.getInventory().add(menu.getCarried());menu.setCarried(ItemStack.EMPTY);
            h.assertTrue(menu.clickMenuButton(p,250)&&menu.getCarried().getCount()==7&&!f.hasResult(),"Normal click did not carry result");
            h.assertTrue(menu.getCarried().getTag().getString("UnknownFutureTag").equals("preserve"),"Cursor pickup lost tags");
            h.assertTrue(!menu.clickMenuButton(p,250),"Repeated click duplicated output");
            menu.removed(p);p.containerMenu=p.inventoryMenu;
            h.assertTrue(p.getInventory().countItem(Items.APPLE)==71,"Closing menu lost the carried output");
        }h.succeed();
    }
    private static AlchemyFurnaceBlockEntity furnace(GameTestHelper h,int count) {
        BlockPos core=FurnaceGameTests.build(h);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(core);
        furnace.startBrewing(h.getLevel().getGameTime(),h.getLevel().getGameTime()+20);
        ItemStack result=new ItemStack(Items.APPLE,count);
        result.getOrCreateTag().putString("Grade","PERFECT");
        result.getOrCreateTag().putString("UnknownFutureTag","preserve");
        h.assertTrue(furnace.complete(result),"Completion rejected");
        return furnace;
    }
    @GameTest(template="furnace_empty",timeoutTicks=20)
    public static void fullPartialAndTwoCollectors(GameTestHelper h) {
        var furnace=furnace(h,10);
        var full=NonNullList.withSize(36,ItemStack.EMPTY);
        for(int i=0;i<36;i++)full.set(i,new ItemStack(Items.STONE,64));
        h.assertTrue(furnace.insertResult(full,64)==0&&furnace.completedResult().getCount()==10,"Full inventory lost result");
        ItemStack matching=furnace.completedResult().copyWithCount(61);full.set(0,matching);
        h.assertTrue(furnace.insertResult(full,64)==3&&furnace.completedResult().getCount()==7,"Partial insertion did not preserve remainder");
        var second=NonNullList.withSize(36,ItemStack.EMPTY);
        h.assertTrue(furnace.insertResult(second,64)==7&&!furnace.hasResult(),"Second collector received wrong quantity");
        h.assertTrue(furnace.insertResult(second,64)==0&&second.get(0).getCount()==7,"Repeat pickup duplicated output");
        h.assertTrue(second.get(0).getTag().getString("UnknownFutureTag").equals("preserve"),"Pickup lost NBT");
        h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=20)
    public static void diskPersistenceKeepsCompletionAge(GameTestHelper h) {
        var furnace=furnace(h,5);
        CompoundTag disk=furnace.saveWithoutMetadata();
        h.assertTrue(disk.contains("CompletionTick"),"Disk save lost the persistent completion timer");
        var copy=new AlchemyFurnaceBlockEntity(cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_FURNACE.get(),furnace.getBlockPos(),furnace.getBlockState());
        copy.setLevel(h.getLevel());copy.load(disk);
        h.assertTrue(copy.completedResult().getCount()==5&&Math.abs(copy.completionAge(0)-furnace.completionAge(0))<.01F,
                "Reload lost output or reset the saved completion age");
        boolean rejected=false;try{copy.startBrewing(1,20);}catch(IllegalStateException expected){rejected=true;}
        h.assertTrue(rejected&&copy.completedResult().getCount()==5,"Next batch overwrote pending result");
        copy.setItem(0,new ItemStack(Items.STONE));
        copy.handleUpdateTag(disk);
        h.assertTrue(copy.getItem(0).isEmpty(),"Empty slot absent from packet left stale client ingredient");
        h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=20)
    public static void collisionInFourDirections(GameTestHelper h) {
        BlockPos core=FurnaceGameTests.build(h);var level=h.getLevel();
        AlchemyFurnaceStructure.refreshAround(level,core);
        var centerProbe=net.minecraft.world.phys.shapes.Shapes.box(.4,0,.4,.6,.9,.6);
        for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL){
            level.setBlock(core,level.getBlockState(core).setValue(AlchemyFurnaceBlock.FACING,direction),3);
            var mouth=core.above(3);
            h.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                    AlchemyFurnaceCollision.at(level,mouth,false),centerProbe,net.minecraft.world.phys.shapes.BooleanOp.AND),"Mouth blocked: "+direction);
            h.assertTrue(!AlchemyFurnaceCollision.at(level,core,false).isEmpty(),"Base support vanished: "+direction);
        }
        h.succeed();
    }
}
