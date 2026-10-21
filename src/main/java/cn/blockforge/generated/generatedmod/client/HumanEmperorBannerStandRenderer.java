package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.HumanEmperorBannerStandBlock;
import cn.blockforge.generated.generatedmod.HumanEmperorBannerStandBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 插地人皇幡渲染：沿用物品模型的完整 3D 几何，杆尾落入地面并随朝向旋转。 */
public final class HumanEmperorBannerStandRenderer implements BlockEntityRenderer<HumanEmperorBannerStandBlockEntity> {
    // 物品模型总高 32 模型单位；FIXED display（scale 0.495、translation -5.28/16）折算后
    // 模型最低点在 display 空间 y=-0.5775、最高点在 y=+0.4125，总高 0.99 格。
    // 放大 5/0.99 倍后整幡正好五格高。
    private static final float MODEL_SCALE = 5.0505F;
    // 抬升 0.5775*MODEL_SCALE，使杆尖最低点正好落在插地方块的底面（地面）上。
    private static final float MODEL_Y_OFFSET = 2.9167F;

    public HumanEmperorBannerStandRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(HumanEmperorBannerStandBlockEntity entity, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack stack = entity.banner();
        Level level = entity.getLevel();
        if (stack.isEmpty() || level == null) return;

        float facing = entity.getBlockState().getValue(HumanEmperorBannerStandBlock.FACING).toYRot();
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        // 杆的中心线在模型空间正好位于 (8,8)，FIXED display 会把模型中心归零，
        // 因此只需平移到方块中心；renderStatic 内部自带居中平移，这里不能再额外偏移，
        // 否则偏移会被下面的 scale 放大出好几格，导致杆子偏离方块中心。
        pose.translate(0.5D, MODEL_Y_OFFSET, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing));
        pose.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                pose, buffers, level, 0);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(HumanEmperorBannerStandBlockEntity entity) { return true; }
}
