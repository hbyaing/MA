package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

/**
 * 灵肥：被灵液浸润并充满灵气的骨粉，修仙灵植的正式催熟手段。
 *
 * <p>服务端硬性校验：基质、水体、灵壤要求全部合法才允许推进；
 * 灵肥只加速合法生长，绝不绕过生态规则强制成熟。对原版作物与树苗
 * 等价普通骨粉，对普通方块无效。</p>
 */
public final class SpiritFertilizerItem extends Item {

    private static final DustParticleOptions FERTILIZER_BLUE =
            new DustParticleOptions(new Vector3f(0.15F, 0.55F, 1.0F), 1.1F);

    /** Broadcast the blue growth burst only after fertilizer succeeds. */
    public static void sendGrowthParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(FERTILIZER_BLUE,
                pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                24, 0.3D, 0.35D, 0.3D, 0.015D);
    }

    public SpiritFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof SpiritPlantBlock plant)) {
            // 非灵植：等价普通骨粉（原版作物、树苗等），对其它方块无效果。
            return applyLikeBoneMeal(context, level, state);
        }
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) {
            return plant.isMature(state) ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
        int advanced = plant.applyFertilizer(serverLevel, pos, state, player);
        if (advanced <= 0) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.fertilizer_invalid"), true);
            }
            return InteractionResult.FAIL;
        }
        sendGrowthParticles(serverLevel, pos);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.4F);
        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(false);
    }

    /** 对非灵植方块执行与 BoneMealItem.useOn 等价的普通骨粉行为。 */
    private static InteractionResult applyLikeBoneMeal(UseOnContext context, Level level, BlockState state) {
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock crop)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) {
            return crop.isValidBonemealTarget(level, pos, state, true)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
        if (!crop.isValidBonemealTarget(level, pos, state, false)
                || !crop.isBonemealSuccess(level, level.random, pos, state)) {
            return InteractionResult.PASS;
        }
        crop.performBonemeal(serverLevel, level.random, pos, state);
        sendGrowthParticles(serverLevel, pos);
        Player player = context.getPlayer();
        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

}
