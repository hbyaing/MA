package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class FurnaceRenderBoundaryGameTests {
    private record Bounded(BlockPos local,AlchemyFurnacePartEntity part) implements BlockGetter {
        @Override public BlockEntity getBlockEntity(BlockPos p){if(!p.equals(local))throw new ArrayIndexOutOfBoundsException("Outside render snapshot");return part;}
        @Override public BlockState getBlockState(BlockPos p){if(!p.equals(local))throw new ArrayIndexOutOfBoundsException("Outside render snapshot");return part.getBlockState();}
        @Override public FluidState getFluidState(BlockPos p){return Fluids.EMPTY.defaultFluidState();}
        @Override public int getHeight(){return 384;}
        @Override public int getMinBuildHeight(){return -64;}
    }
    @GameTest(template="empty") public static void boundaryShapesNeverReadRemoteCoreForAnyRotationOrLegacyPart(GameTestHelper h){
        for(int base:new int[]{-18,14})for(int turn=-1;turn<4;turn++)for(int x=-2;x<=2;x++)for(int y=0;y<4;y++)for(int z=-2;z<=2;z++){
            var core=new BlockPos(base,200,base);var local=core.offset(x,y,z);var part=new AlchemyFurnacePartEntity(local,WuXingBlocks.ALCHEMY_FURNACE_PART.get().defaultBlockState());part.core=core;part.coreTurn=turn;
            var bounded=new Bounded(local,part);for(boolean selection:new boolean[]{false,true})h.assertTrue(AlchemyFurnaceCollision.at(bounded,local,selection)!=null,"Missing shape");
        }h.succeed();
    }
    @GameTest(template="empty") public static void partOrientationSurvivesDiskAndClientSyncAndOldDataFallsBack(GameTestHelper h){var pos=new BlockPos(-17,-61,-9);var state=WuXingBlocks.ALCHEMY_FURNACE_PART.get().defaultBlockState();var original=new AlchemyFurnacePartEntity(pos,state);original.core=pos.offset(-2,0,0);original.coreTurn=3;original.original=Blocks.GRASS_BLOCK.defaultBlockState();var loaded=new AlchemyFurnacePartEntity(pos,state);loaded.load(original.saveWithoutMetadata());h.assertTrue(loaded.coreTurn==3&&loaded.core.equals(original.core)&&loaded.original.equals(original.original),"Disk orientation lost");loaded.load(original.getUpdateTag());h.assertTrue(loaded.coreTurn==3,"Client orientation lost");var old=original.getUpdateTag();old.remove("CoreTurn");loaded.load(old);h.assertTrue(loaded.coreTurn==-1,"Old part silently assumed wrong orientation");var region=new Bounded(pos,loaded);for(boolean select:new boolean[]{false,true}){var fallback=AlchemyFurnaceCollision.at(region,pos,select);for(int t=0;t<4;t++){loaded.coreTurn=t;var shape=AlchemyFurnaceCollision.at(region,pos,select);h.assertTrue(!Shapes.joinIsNotEmpty(shape,fallback,net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST),"Fallback omits a rotated collision");}loaded.coreTurn=-1;}h.succeed();}
}
