package cn.blockforge.generated.generatedmod.alchemy.inputs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

/** Placed collector. Furnace transfer/automation remains the explicitly separate batch transaction stage. */
public final class MoonBottleBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE=Block.box(5,0,5,11,12,11);
    public MoonBottleBlock(){super(BlockBehaviour.Properties.of().strength(.5F).sound(SoundType.GLASS).noOcclusion());}
    @Override public VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new MoonBottleBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        return level.isClientSide?null:createTickerHelper(type,cn.blockforge.generated.generatedmod.registry.ModBlockEntities.MOON_BOTTLE.get(),MoonBottleBlockEntity::tick);
    }
    @Override public net.minecraft.world.InteractionResult use(BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof MoonBottleBlockEntity bottle)
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.moon_bottle",bottle.contents().total(),MoonBottleData.CAPACITY,bottle.contents().sources().size()),true);
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.level.BlockGetter level,java.util.List<net.minecraft.network.chat.Component> tooltip,net.minecraft.world.item.TooltipFlag flag){
        super.appendHoverText(stack,level,tooltip,flag);
        var be=net.minecraft.world.item.BlockItem.getBlockEntityData(stack);if(be!=null){
            try{var contents=MoonBottleData.load(be.getCompound("MoonBottle"));tooltip.add(net.minecraft.network.chat.Component.translatable("message.wuxingxiantu.moon_bottle",contents.total(),MoonBottleData.CAPACITY,contents.sources().size()));}
            catch(IllegalArgumentException e){tooltip.add(net.minecraft.network.chat.Component.literal("Invalid moon bottle data"));}
        }
    }
}
