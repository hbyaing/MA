package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.FloatingItemHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 基座悬浮物品渲染（灵蕴台与承华台共用）：逐点对照 Goety 的 PedestalRenderer——
 * 物品平移到托盘上方 (0.5, 0.95, 0.5)（新基座模型托盘沿顶为 14/16），
 * 以 GROUND 姿态每秒 3° 绕 Y 轴旋转，并叠加正弦起伏，让“悬浮”更明显。
 */
public final class PedestalFloatingRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity
        & cn.blockforge.generated.generatedmod.FloatingItemHolder>
        implements BlockEntityRenderer<T> {
    private static final float SPIN_DEGREES_PER_TICK = 3.0F;
    private static final float BASE_HEIGHT = 0.95F;
    private static final float BOB_AMPLITUDE = 0.045F;

    public PedestalFloatingRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(T entity, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack stack = entity.floatingStack();
        if (stack.isEmpty()) return;
        Level level = entity.getLevel();
        if (level == null) return;
        Minecraft mc = Minecraft.getInstance();

        long tickBase = mc.player != null ? mc.player.tickCount % 360L : level.getGameTime() % 360L;
        float time = level.getGameTime() + partialTick;
        float spin = SPIN_DEGREES_PER_TICK * (tickBase + partialTick);
        float bob = BOB_AMPLITUDE * Mth.sin(time * 0.06F);

        pose.pushPose();
        pose.translate(0.5D, BASE_HEIGHT + bob, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, packedLight, packedOverlay,
                pose, buffers, level, 0);
        pose.popPose();
    }
}
