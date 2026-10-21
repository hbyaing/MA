package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

/** 独立的九阶灵火：Spiritfire 动画主体不依赖粒子数量或额外品阶包。 */
public final class ExoticFireBlock extends FireBlock {
    private final int tier;

    public ExoticFireBlock(int tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = ExoticFireColorRegistry.clampTier(tier);
    }

    public int tier() { return tier; }

    /** 保留原版火的附着面和年龄，只更换方块身份。 */
    public BlockState copyFireState(BlockState source) {
        BlockState result = defaultBlockState();
        for (var property : new net.minecraft.world.level.block.state.properties.BooleanProperty[]{NORTH, EAST, SOUTH, WEST, UP}) {
            if (source.hasProperty(property)) result = result.setValue(property, source.getValue(property));
        }
        return source.hasProperty(AGE) ? result.setValue(AGE, source.getValue(AGE)) : result;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        // FireBlock 的私有 getStateWithAge 会退回 minecraft:fire，不能原样调用。
        return getStateForPlacement(level, pos).setValue(AGE, state.getValue(AGE));
    }

    @Override
    public int getBurnOdds(BlockState state) { return ((FireBlock) Blocks.FIRE).getBurnOdds(state); }

    @Override
    public int getIgniteOdds(BlockState state) { return ((FireBlock) Blocks.FIRE).getIgniteOdds(state); }

    /** 异火不因雨水熄灭：吞掉原版对"近雨熄灭"的判定，让雨天仍能持续点燃与蔓延。 */
    @Override
    protected boolean isNearRain(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        if (!level.isClientSide && level.getBlockState(pos).is(this)) {
            ExoticFireState.set(level, pos, tier);
            if (level.getBlockState(pos.below()).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get())) {
                ExoticFireState.setEternalCoal(level, pos.below(), tier);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Set<BlockPos> existingOrdinaryFires = new HashSet<>();
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 4, 1))) {
            if (level.hasChunkAt(candidate) && level.getBlockState(candidate).is(Blocks.FIRE)) {
                existingOrdinaryFires.add(candidate.immutable());
            }
        }
        // 保留游戏规则、雨水熄灭、可燃性和蔓延概率。
        super.tick(state, level, pos, random);
        // 同一次服务端 tick 内转换新增火焰；发包时已经是正确品阶的独立方块。
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 4, 1))) {
            if (!existingOrdinaryFires.contains(candidate) && level.hasChunkAt(candidate)
                    && level.getBlockState(candidate).is(Blocks.FIRE)) {
                ExoticFireState.set(level, candidate, tier);
            }
        }
    }

    /** 燃烧视觉跟随点燃来源：被本方块点燃的实体按本方块品阶上色，而不是装备异火。 */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Never enter BaseFireBlock's vanilla ignition loop for exotic block contact.
        if (!level.isClientSide)cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.contact(entity,tier);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // 不调用 BaseFireBlock.animateTick：从源头禁止异火产生原版黑烟。
        if (random.nextInt(24) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    0.5F + random.nextFloat() * 0.3F, 0.8F + random.nextFloat() * 0.3F, false);
        }
        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 0.5D + random.nextDouble() * 0.7D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(ModParticles.EXOTIC_FIRE_FLAME.get(), x, y, z,
                tier * 0.01D, 0.018D + random.nextDouble() * 0.024D, 0.0D);
        int accent = ExoticFireColorRegistry.accentColor(tier, level.getGameTime());
        switch (tier) {
            case 3 -> level.addParticle(ParticleTypes.ASH, x, y, z, 0.0D, 0.01D, 0.0D);
            case 5 -> level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, 0.025D, 0.0D);
            case 6 -> {
                if (random.nextBoolean()) level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                        (random.nextDouble() - 0.5D) * 0.12D, 0.05D, (random.nextDouble() - 0.5D) * 0.12D);
            }
            case 8 -> { /* 九幽仅保留暗紫灵火，不添加亮白余烬。 */ }
            default -> level.addParticle(new DustParticleOptions(ExoticFireColorRegistry.vector(accent), 0.45F),
                    x, y, z, (random.nextDouble() - 0.5D) * 0.04D, 0.04D,
                    (random.nextDouble() - 0.5D) * 0.04D);
        }
    }
}
