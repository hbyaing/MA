package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.tags.BlockTags;

/** One form, vanilla small-mushroom spread; legacy age and waterlogged states remain readable. */
public final class GlowingBlueMushroomBlock extends SpiritPlantBlock {
    public GlowingBlueMushroomBlock(SpiritPlantDefinition def, BlockBehaviour.Properties properties) { super(def,properties); }
    @Override public boolean isMature(BlockState state) { return true; }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.level.block.Block.box(3,0,3,13,10,13);
    }
    @Override public boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) {
        BlockPos below=pos.below();BlockState soil=level.getBlockState(below);
        return soil.is(BlockTags.MUSHROOM_GROW_BLOCK)
                || level.getRawBrightness(pos,0)<13&&soil.isSolidRender(level,below);
    }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state=defaultBlockState();
        return context.getLevel().getFluidState(context.getClickedPos()).isEmpty()&&canSurvive(state,context.getLevel(),context.getClickedPos())?state:null;
    }
    @Override public void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        HerbCultivationData.get(level).advance(level,pos,this);
        if(random.nextInt(25)!=0)return;
        int remaining=5;
        for(BlockPos nearby:BlockPos.betweenClosed(pos.offset(-4,-1,-4),pos.offset(4,1,4)))
            if(level.getBlockState(nearby).is(this)&&--remaining<=0)return;
        BlockPos origin=pos;
        BlockPos candidate=origin.offset(random.nextInt(3)-1,random.nextInt(2)-random.nextInt(2),random.nextInt(3)-1);
        for(int i=0;i<4;i++) {
            if(level.isEmptyBlock(candidate)&&canSurvive(state,level,candidate))origin=candidate;
            candidate=origin.offset(random.nextInt(3)-1,random.nextInt(2)-random.nextInt(2),random.nextInt(3)-1);
        }
        if(level.isEmptyBlock(candidate)&&canSurvive(state,level,candidate)) {
            SpiritPlantRegions sources=SpiritPlantRegions.get(level);
            if(sources.isCultivated(pos)) {
                if(level.setBlock(candidate,defaultBlockState(),2))sources.markCultivated(candidate);
            } else sources.spreadWild(level,pos,candidate);
        }
    }
}
