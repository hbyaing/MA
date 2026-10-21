package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.compat.CauldronJadePlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import snownee.jade.api.BlockAccessor;
import snownee.jade.impl.BlockAccessorImpl;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class JadeFurnaceGameTests {
    private static BlockAccessor at(GameTestHelper h,BlockPos pos) {
        return new BlockAccessorImpl.Builder().level(h.getLevel()).serverData(new CompoundTag())
                .hit(new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false))
                .blockState(h.getLevel().getBlockState(pos)).blockEntity(()->h.getLevel().getBlockEntity(pos)).build();
    }
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void redirectsAndReadsCore(GameTestHelper h) {
        BlockPos core=FurnaceGameTests.build(h);var level=h.getLevel();BlockPos part=core.east(2);
        BlockAccessor raw=at(h,part);
        h.assertTrue(CauldronJadePlugin.redirect(raw,BlockAccessorImpl.Builder::new)==raw,"Unformed Jade changed");
        AlchemyFurnaceStructure.refreshAround(level,core);
        var controller=(AlchemyFurnaceBlockEntity)level.getBlockEntity(core);
        long now=level.getGameTime();controller.startBrewing(now-50,now+50);
        for(BlockPos target:java.util.List.of(core,part,core.north(2).above(),core.offset(2,2,1),core.above(3).east())) {
            BlockAccessor redirected=CauldronJadePlugin.redirect(at(h,target),BlockAccessorImpl.Builder::new);
            h.assertTrue(redirected.getPosition().equals(core),"Jade request still targets component");
            h.assertTrue(redirected.getBlockEntity()==controller,"Jade entity isn't controller");
            h.assertTrue(redirected.isFakeBlock() && redirected.getFakeBlock().hasCustomHoverName(),"Unified Jade name absent");
            CompoundTag data=new CompoundTag();CauldronJadePlugin.FurnaceInfo.INSTANCE.appendServerData(data,redirected);
            h.assertTrue(data.getBoolean("CauldronFormed") && data.getBoolean("CauldronBrewing") && data.getInt("CauldronProgress")==50,"Core details wrong");
        }
        level.destroyBlock(core.offset(2,0,2),false);
        BlockAccessor restored=at(h,part);
        h.assertTrue(CauldronJadePlugin.redirect(restored,BlockAccessorImpl.Builder::new)==restored,"Jade redirect survives dismantling");
        h.succeed();
    }
}
