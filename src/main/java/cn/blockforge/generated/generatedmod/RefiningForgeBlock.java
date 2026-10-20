package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 炼器台核心；核心、底层符文石与磨制黑石砖台阶、上层符文石共同组成 3x3x2 多方块结构。
 * 核心本身是实心方块，仅用贴图表现外观；结构成型时切换 FORGED 状态点亮器台造型。
 */
public final class RefiningForgeBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty FORGED = BooleanProperty.create("forged");

    public RefiningForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FORGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FORGED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefiningForgeBlockEntity(
                cn.blockforge.generated.generatedmod.registry.ModBlockEntities.REFINING_FORGE.get(), pos, state);
    }

    /**
     * 校验唯一的 3x3x2 炼器台结构：底层中心为核心，四边为符文石，四角为磨制黑石砖台阶（下半砖）；
     * 上层外围八格为符文石，中心允许为空气或永燃灵煤块。角部允许已被结构替换成角座的成型状态。
     */
    public static boolean isStructureComplete(Level level, BlockPos corePos) {
        if (!isCore(level.getBlockState(corePos))) return false;
        BlockState upperCenter = level.getBlockState(corePos.above());
        if (!upperCenter.isAir() && !upperCenter.is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) return false;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockState lower = level.getBlockState(corePos.offset(x, 0, z));
                if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                    if (!RefiningForgeStructure.isRawCornerSlab(lower)
                            && !lower.is(WuXingBlocks.REFINING_FORGE_CORNER.get())) return false;
                } else if (!lower.is(WuXingBlocks.RUNE_STONE.get())) {
                    return false;
                }
                if (!level.getBlockState(corePos.offset(x, 1, z)).is(WuXingBlocks.RUNE_STONE.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() == WuXingBlocks.REFINING_FORGE_CORE.get();
    }

    /** Legacy/internal fire-tier proxy for older recipe consumers; not a player-controlled temperature. */
    public static int temperature(Level level, BlockPos corePos) {
        int tier = fireTier(level, corePos);
        return tier == 0 ? 0 : temperatureForTier(tier);
    }

    /** Legacy recipe encoding only. UI displays actual fire tiers. */
    public static int temperatureForTier(int tier) {
        return 300 + ExoticFireColorRegistry.clampTier(tier) * 100;
    }

    /** 返回炼器台中央当前承载的异火品阶；没有煤块或火焰时为零。 */
    public static int fireTier(Level level, BlockPos corePos) {
        BlockState coal = level.getBlockState(corePos.above());
        if (!coal.is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) return 0;
        BlockState fire = level.getBlockState(corePos.above(2));
        return ExoticFireState.isFire(fire) ? ExoticFireState.get(level, corePos.above(2)) : 0;
    }

    /** 同步切换核心、底层符文石、角座烛台与上层符文石的暗态/亮态。 */
    public static void setStructureLit(Level level, BlockPos corePos, boolean lit) {
        if (level.isClientSide) return;
        BlockState core = level.getBlockState(corePos);
        if (isCore(core) && core.getValue(LIT) != lit) {
            level.setBlock(corePos, core.setValue(LIT, lit), Block.UPDATE_ALL);
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                    setCornerLit(level, corePos.offset(x, 0, z), lit);
                } else {
                    setRuneLit(level, corePos.offset(x, 0, z), lit);
                }
                setRuneLit(level, corePos.offset(x, 1, z), lit);
            }
        }
    }

    /** 角座烛台随炼器台点火亮起。 */
    private static void setCornerLit(Level level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RefiningForgeCornerBlock
                && state.getValue(RefiningForgeCornerBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(RefiningForgeCornerBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private static void setRuneLit(Level level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == WuXingBlocks.RUNE_STONE.get()
                && state.getValue(RuneStoneBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(RuneStoneBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** 任一符文石被拆除时，寻找其所属核心并立即熄灭残余亮态。 */
    public static void clearNearbyLit(Level level, BlockPos changedPos) {
        if (level.isClientSide) return;
        for (int y = -1; y <= 0; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos candidate = changedPos.offset(x, y, z);
                    BlockState state = level.getBlockState(candidate);
                    if (isCore(state) && state.getValue(LIT)) {
                        setStructureLit(level, candidate, false);
                    }
                }
            }
        }
    }

    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!isStructureComplete(level, pos)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.refining_forge_incomplete"), true);
            return InteractionResult.CONSUME;
        }
        // 兼容旧存档：v2 之前成型的符文石没有 part/facing 位置属性，
        // 开台交互时借结构刷新补全，切到各自应裁的整幅大图切片。
        RefiningForgeStructure.refreshAround(level, pos);
        cn.blockforge.generated.generatedmod.network.service.AlchemyService.openRefining(serverPlayer,pos);
        Network.sendOpenRefining(serverPlayer, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
        if(!isStructureComplete(level,pos)){
            CoreStructureBuilder.activate(level,pos,player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        // 右键炼器台核心一律打开炼器界面；成品领取改由界面上的"领取"按钮发起。
        // 旧的"右键核心领取"途径作废。
        return open(level, pos, player);
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !level.isClientSide && state.getBlock() != newState.getBlock()) {
            if(level instanceof ServerLevel server)cn.blockforge.generated.generatedmod.network.service.AlchemyService.wakeAt(server,pos);
            // 核心消失后结构无法再被定位，直接塌缩还原周围已成型的符文与角座。
            RefiningForgeStructure.collapseAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
