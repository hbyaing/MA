package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 炼丹炉核心：本身只是一个带贴图的方块（6 品级各自 32x32 贴图），是 5x5x4
 * 炼丹炉结构的组成方块之一。结构齐全时把 FORMED 置位，开启炼丹交互与炼制特效；
 * 成型时绘制完整炉体模型，原材料暂存于不遮光的结构部件中。
 *
 * <p>成型后核心、符文石与外壳任意一格都能右键打开炼丹界面；拆掉核心或任意一格组件
 * 都会让结构自动解除成型（见 {@link AlchemyFurnaceStructure#refreshAround}）。
 * 核心品级只决定可炼丹药的品级。
 */
public final class AlchemyFurnaceBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    /** 炉体正面朝向（核心所在面的外侧方向）；成型时由结构刷新写入，未成型时无意义。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final int tier;


    public AlchemyFurnaceBlock(int tier) {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F, 3.5F).noOcclusion().dynamicShape()
                .requiresCorrectToolForDrops()
                .lightLevel(state -> tier >= 3 ? 8 : 4));
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(FACING, Direction.NORTH));
    }

    @Override public int getLightBlock(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) { return 0; }
    @Override public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) { return true; }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) { return net.minecraft.world.phys.shapes.Shapes.empty(); }

    public int tier() { return tier; }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return isFormed(state) ? AlchemyFurnaceCollision.at(level,pos,true) : super.getShape(state,level,pos,context);
    }
    @Override public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        return isFormed(state) ? AlchemyFurnaceCollision.at(level,pos,false) : super.getCollisionShape(state,level,pos,context);
    }
    private net.minecraft.world.item.ItemStack resultCarrier(AlchemyFurnaceBlockEntity furnace) {
        var stack=new net.minecraft.world.item.ItemStack(this);
        var data=new net.minecraft.nbt.CompoundTag();
        data.put("CompletedResult",furnace.completedResult().save(new net.minecraft.nbt.CompoundTag()));
        data.putString("ActiveFireId",furnace.activeFireId()); data.putInt("AlchemyOutcome",1);
        if(furnace.v5Batch())data.putInt("SpiritFuel",furnace.v5DismantleFuel());
        stack.addTagElement("BlockEntityTag",data);
        return stack;
    }
    @Override public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        if (params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof AlchemyFurnaceBlockEntity furnace && furnace.hasResult())
            return java.util.List.of(resultCarrier(furnace));
        if(params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof AlchemyFurnaceBlockEntity active&&active.v5Batch()){
            var core=new net.minecraft.world.item.ItemStack(this);var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("SpiritFuel",active.v5DismantleFuel());core.addTagElement("BlockEntityTag",tag);return java.util.List.of(core);
        }
        return super.getDrops(state,params);
    }
    @Override public boolean canHarvestBlock(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,Player player) {
        // A completed batch must travel with its core even when dismantled with the wrong tool.
        return level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace && (furnace.hasResult()||furnace.v5Batch())
                || super.canHarvestBlock(state,level,pos,player);
    }
    @Override public void playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player) {
        if (!level.isClientSide && player.isCreative() && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace && furnace.hasResult())
            popResource(level,pos,resultCarrier(furnace));
        if(!level.isClientSide&&player.isCreative()&&level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity active&&active.v5Batch()&&!active.hasResult()){
            var core=new net.minecraft.world.item.ItemStack(this);var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("SpiritFuel",active.v5DismantleFuel());core.addTagElement("BlockEntityTag",tag);popResource(level,pos,core);
        }
        super.playerWillDestroy(level,pos,state,player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED, FACING);
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof AlchemyFurnaceBlock;
    }

    public static boolean isFormed(BlockState state) {
        return isCore(state) && state.getValue(FORMED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Assembled cores use the full cauldron model instead of a cube.
        return isFormed(state) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyFurnaceBlockEntity(ModBlockEntities.ALCHEMY_FURNACE.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return type == ModBlockEntities.ALCHEMY_FURNACE.get()
                ? (lvl, pos, blockState, entity) -> AlchemyFurnaceBlockEntity.tick(
                        lvl, pos, blockState, (AlchemyFurnaceBlockEntity) entity) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // 炼丹炉核心不区分手持物；成型后右键核心格（或炉体任意一格组件）都会打开炼丹界面。
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!isFormed(state)) {
            CoreStructureBuilder.activate(level, pos, player);
            return InteractionResult.CONSUME;
        }
        // 潜行时让位给正常放置方块，方便玩家贴着炉体施工。
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        return openAlchemy(level, pos, player);
    }

    @Override
    public net.minecraft.world.MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace ? furnace.menuProvider() : null;
    }

    /** 打开炼丹界面；成型结构内的符文石也可经由 RuneStoneBlock 调用这里。 */
    public static InteractionResult openAlchemy(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!isFormed(level.getBlockState(pos))) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.alchemy_not_formed"), true);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, furnace.menuProvider(), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide) AlchemyFurnaceStructure.refreshAround(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // AlchemyFurnaceEffects draws the brewing visuals without vanilla particles.
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if(newState.getBlock() instanceof AlchemyFurnaceBlock
                && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace && furnace.upgradingCore) return;
        if (!moving && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity furnace) {
            furnace.detachV5();
            if (!furnace.v5Batch() && furnace.brewing() && level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 4.0D,
                        pos.getZ() + 0.5D, 30, 1.0D, 0.8D, 1.0D, 0.025D);
                Network.cancelAlchemyAt(server, pos);
            }
            // 掉落尚未炼制的素材与异火瓶，避免结构拆解时悄悄吞掉玩家物品。
            if (!level.isClientSide) net.minecraft.world.Containers.dropContents(level, pos, furnace);
            AlchemyFurnaceStructure.dismantle(level, pos, pos);
            level.removeBlockEntity(pos);
        }
        if (!moving && state.getBlock() != newState.getBlock() && !level.isClientSide) {
            AlchemyFurnaceStructure.refreshAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    public static Component tierName(int tier) {
        return Component.translatable("alchemy.tier.wuxingxiantu." + Math.max(0, Math.min(5, tier)));
    }
}
