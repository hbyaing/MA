package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

/** Invisible collision shell that neither culls the ground nor blocks skylight. */
public final class AlchemyFurnacePartBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty SEAL =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("seal");
    public AlchemyFurnacePartBlock() {
        super(Properties.of().strength(3.5F).noOcclusion().dynamicShape().pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(SEAL,false));
    }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder) {
        builder.add(SEAL);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return AlchemyFurnaceCollision.at(level,pos,true);
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return AlchemyFurnaceCollision.at(level,pos,false);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (player.isSecondaryUseActive() || hand!=net.minecraft.world.InteractionHand.MAIN_HAND) return net.minecraft.world.InteractionResult.PASS;
        return level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part
                ? AlchemyFurnaceBlock.openAlchemy(level,part.core,player) : net.minecraft.world.InteractionResult.PASS;
    }
    @Override public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) { return 0; }
    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
    @Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return Shapes.empty(); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AlchemyFurnacePartEntity(pos, state); }

    @Override public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
        if (state.getValue(SEAL)) return 0;
        return level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part
                ? part.original.getDestroyProgress(player,level,pos) : super.getDestroyProgress(state,player,level,pos);
    }
    @Override public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part && part.original.canHarvestBlock(level,pos,player);
    }
    @Override public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
                                                BlockGetter level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part
                ? part.original.getCloneItemStack(target,level,pos,player) : ItemStack.EMPTY;
    }

    @Override public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof AlchemyFurnacePartEntity part)) return List.of();
        return part.original.getDrops(params.withParameter(LootContextParams.BLOCK_STATE, part.original)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, part.originalEntity()));
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide && !AlchemyFurnaceStructure.mutating
                && level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part) {
            // The broken component's block drop is handled by getDrops; spill only its inventory here.
            if (part.originalEntity() instanceof Container inventory) Containers.dropContents(level, pos, inventory);
            AlchemyFurnaceStructure.dismantle(level, part.core, pos);
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (world, pos, current, entity) -> {
            if(entity instanceof AlchemyFurnacePartEntity legacy&&legacy.coreTurn<0&&world.hasChunkAt(legacy.core)&&AlchemyFurnaceBlock.isFormed(world.getBlockState(legacy.core))){legacy.coreTurn=AlchemyFurnaceCollision.turn(world.getBlockState(legacy.core));legacy.setChanged();world.sendBlockUpdated(pos,current,current,3);}
            if (entity instanceof AlchemyFurnacePartEntity part && (world.getGameTime() + pos.asLong()) % 40 == 0
                    && world.hasChunkAt(part.core) && !AlchemyFurnaceBlock.isFormed(world.getBlockState(part.core))) {
                AlchemyFurnaceStructure.restore(world, part);
            }
        };
    }
}
