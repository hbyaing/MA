package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/** 灵液表面的流光效果，流体本身仍由 ForgeFlowingFluid 负责扩散。 */
public final class SpiritLiquidBlock extends LiquidBlock {
    public SpiritLiquidBlock(java.util.function.Supplier<? extends net.minecraft.world.level.material.FlowingFluid> fluid,
                             BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    /**
     * 允许标准方块放置流程直接替换灵液，而不是先在相邻方块上寻找放置位置。
     * 这是 BlockPlaceContext 判断“点击的方块能否被放置物替换”时使用的入口。
     */
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    /** 允许原版流体交互将灵液视为可被流体替换的非固体方块。 */
    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) != 0) return;
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) return;

        Vec3 flow = fluidState.getFlow(level, pos);
        double phase = (level.getGameTime() + pos.getX() * 31L + pos.getZ() * 17L) * 0.12D;
        double flowX = flow.lengthSqr() > 0.0001D ? flow.x * 0.035D : Math.cos(phase) * 0.016D;
        double flowZ = flow.lengthSqr() > 0.0001D ? flow.z * 0.035D : Math.sin(phase) * 0.016D;
        double x = pos.getX() + 0.12D + random.nextDouble() * 0.76D;
        double y = pos.getY() + fluidState.getHeight(level, pos) + 0.012D;
        double z = pos.getZ() + 0.12D + random.nextDouble() * 0.76D;
        level.addParticle(ParticleTypes.GLOW, x, y, z, flowX, 0.003D, flowZ);

        if (random.nextInt(4) == 0) {
            double rippleAngle = phase + random.nextDouble() * Math.PI * 2.0D;
            level.addParticle(ParticleTypes.ENCHANT, x, y + 0.006D, z,
                    Math.cos(rippleAngle) * 0.008D + flowX * 0.35D,
                    0.002D,
                    Math.sin(rippleAngle) * 0.008D + flowZ * 0.35D);
        }
    }
}
