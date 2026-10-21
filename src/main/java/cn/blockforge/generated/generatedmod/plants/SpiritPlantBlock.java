package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * 统一灵植方块。所有灵植共用本类与同一套事件逻辑，
 * 行为差异全部来自 {@link SpiritPlantDefinition} 的数据字段。
 *
 * <p>硬性条件（基质、水）不满足：放置被拒绝；已存在的植株脱落并掉回本体，资源链不灭绝。
 * 生长偏好（光照、昼夜、雷雨、邻水、露天）只改变生长概率，绝不致死。</p>
 */
public class SpiritPlantBlock extends BushBlock implements BonemealableBlock {

    /** 全部灵植共享 0..7 的阶段属性；具体成熟线由 definition.matureAge 决定。 */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    private final SpiritPlantDefinition def;
    private static final net.minecraft.world.phys.shapes.VoxelShape[] SHAPES={
            net.minecraft.world.level.block.Block.box(3,0,3,13,11,13),
            net.minecraft.world.level.block.Block.box(2,0,2,14,14,14),
            net.minecraft.world.level.block.Block.box(.8,0,.8,15.2,16,15.2)};

    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context){
        int age=state.getValue(AGE);return SHAPES[age<2?0:age<5?1:2];
    }

    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientBlockExtensions> consumer){
        consumer.accept(new cn.blockforge.generated.generatedmod.client.SpiritPlantEffects());
    }

    public SpiritPlantBlock(SpiritPlantDefinition def, BlockBehaviour.Properties properties) {
        super(properties);
        this.def = def;
        registerDefaultState(stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    public SpiritPlantDefinition definition() {
        return def;
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level,pos,state,placer,stack);
        if (level instanceof ServerLevel server) {
            SpiritPlantRegions.get(server).markCultivated(pos);
            HerbCultivationData.get(server).plant(server,pos,def,stack);
        }
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level instanceof ServerLevel server) {
            SpiritPlantRegions.get(server).removed(pos);
            HerbCultivationData.get(server).removeAfterDrops(pos);
        }
        super.onRemove(state,level,pos,next,moving);
    }

    @Override public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        var drops=super.getDrops(state,params);
        var pos=BlockPos.containing(params.getParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN));
        int years=HerbCultivationData.get(params.getLevel()).years(pos);boolean primary=true;
        for(var drop:drops)if(drop.is(asItem())) {
            HerbAge.setYears(drop,primary?HerbAge.boundedYears(def,years):0);
            if(primary&&HerbCultivationData.get(params.getLevel()).ashUses(pos)>0)
                drop.getOrCreateTag().putInt(HerbAge.ASH_USES,HerbCultivationData.get(params.getLevel()).ashUses(pos));
            primary=false;
        }
        return drops;
    }

    public boolean isMature(BlockState state) {
        return state.getValue(AGE) >= def.matureAge;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE, BlockStateProperties.WATERLOGGED);
    }

    /** 放置合法性统一入口：非法直接返回 null，BlockItem 的 getPlacementState 随之 FAIL。 */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        boolean replacesWater = context.getLevel().getFluidState(pos).is(Fluids.WATER);
        BlockState self = defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, replacesWater);
        return def.canPlaceAt(context.getLevel(), pos, self) ? self : null;
    }

    // ===== 灵肥催熟 =====

    /** 灵肥入口：推进 def 配置的阶段数。返回实际推进数，0 表示硬条件不合法或已成熟。 */
    public int applyFertilizer(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player) {
        if (isMature(state)) return 0;
        int advance = def.fertilizerAdvanceAt(level, pos);
        if (advance <= 0) return 0;
        grow(level, pos, state, advance);
        return advance;
    }

    public boolean applyAsh(ServerLevel level,BlockPos pos,BlockState state){
        if(isMature(state)||!def.fertilizerAllowed(level,pos))return false;
        HerbCultivationData.get(level).advance(level,pos,this);
        if(!HerbCultivationData.get(level).nourish(pos))return false;
        grow(level,pos,state,1);return true;
    }

    private void grow(Level level, BlockPos pos, BlockState state, int stages) {
        int target = Math.min(def.matureAge, state.getValue(AGE) + Math.max(1, stages));
        if (target > state.getValue(AGE)) {
            level.setBlock(pos, state.setValue(AGE, target), 2);
        }
    }

    // ===== 自然生长与脱落 =====

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        HerbCultivationData.get(level).advance(level,pos,this);
        if (isMature(state)) return;
        if (!state.canSurvive(level, pos)) {
            // 硬条件被破坏（水体消失、基质被换）：脱落并掉回植株。
            level.destroyBlock(pos, true, null);
            return;
        }
        int oneIn = def.growthOneIn(level, pos);
        if (oneIn <= 0) return;
        if (random.nextInt(oneIn) == 0) {
            grow(level, pos, state, 1);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if(def.id.equals("dragon_scale_grass")&&level instanceof Level world
                &&(world.dimension()!=Level.END||!DragonGrassCycle.mainIsland(pos)))return false;
        return def.canPlaceAt(level, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (def.aquaticInCell) {
            // 用“邻格真实水”重估浸水状态，植株自己的伪水不计入，避免排水后自锁。
            boolean wet = adjacentRealWater(level, pos);
            if (wet != state.getValue(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, wet);
            }
        }
        if (!state.canSurvive(level, pos)) {
            // 硬条件被破坏：参照甘蔗做法，掉回植株后脱落。
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(pos, true, null);
            }
            return state;
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** 六邻格存在非灵植方块携带的真实水。仅 randomTick/updateShape 触发，成本受控。 */
    static boolean adjacentRealWater(BlockGetter level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos np = pos.relative(dir);
            BlockState ns = level.getBlockState(np);
            if (ns.getBlock() instanceof SpiritPlantBlock) continue;
            if (ns.getFluidState().is(Fluids.WATER) || level.getFluidState(np).is(Fluids.WATER)) return true;
        }
        return false;
    }

    // ===== 普通骨粉：仅对 def.boneMealUsable 的基础灵植生效，且只推进 1 阶 =====

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        // 普通骨粉只对基础灵植生效，且与灵肥共用同一硬条件入口（基质 + 灵壤），
        // 防止以后给 needsSpiritSoil 的灵植加 boneMeal() 时绕过灵壤门槛。
        return def.boneMealUsable && !isMature(state) && def.fertilizerAllowed(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return def.boneMealUsable;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!def.boneMealUsable || !def.fertilizerAllowed(level, pos)) return;
        grow(level, pos, state, 1);
    }
}
