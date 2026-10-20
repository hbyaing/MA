package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.RefiningForgeBlock;
import cn.blockforge.generated.generatedmod.RefiningForgeBlockEntity;
import cn.blockforge.generated.generatedmod.RefiningForgeStructure;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 未成型炼器台的结构预览：
 * 已摆对的位置显示很淡的蓝色半透明光罩；缺失或摆错的位置显示红色呼吸警示盒，
 * 并在格子中心悬浮一枚缓慢旋转、轻微起伏的“所需方块”全息缩影（符文石 / 磨制黑石砖台阶），
 * 让玩家直接看清每一格要放什么；第二层中心空着时给出淡紫色永燃灵煤块提示（不强制）。
 *
 * 注意：所有几何都通过带 PoseStack 的渲染接口写入（区块实体坐标系），
 * 不再使用不带矩阵的 renderLineBox 重载——那会在世界原点画出固定不动的红线。
 */
public final class RefiningForgeBlockEntityRenderer implements BlockEntityRenderer<RefiningForgeBlockEntity> {
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final double MAX_DISTANCE_SQR = 26.0D * 26.0D;
    private static final double INSET = 0.06D;
    private static final float GHOST_SCALE = 0.45F;

    public RefiningForgeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(RefiningForgeBlockEntity entity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        Minecraft mc = Minecraft.getInstance();
        if (level == null || mc.player == null) return;
        BlockPos corePos = entity.getBlockPos();
        if (mc.player.position().distanceToSqr(corePos.getX() + 0.5D, corePos.getY() + 0.5D,
                corePos.getZ() + 0.5D) > MAX_DISTANCE_SQR) return;

        float time = level.getGameTime() + partialTick;
        // 炼制完成：成品悬浮于异火上方，等待玩家右键领取。
        ItemStack reward = entity.pendingReward();
        if (!reward.isEmpty()) {
            renderFloatingReward(mc, level, pose, buffers, reward, time);
            return;
        }
        if (!RefiningForgeBlock.isStructureComplete(level, corePos)) {
            if(!CoreBuildClient.visible(corePos))return;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    renderSlot(mc, level, pose, buffers, corePos, x, 0, z, time);
                    renderSlot(mc, level, pose, buffers, corePos, x, 1, z, time);
                }
            }
        }
        // 第二层中心：煤块不影响结构成型，但没有它便点不起异火，给一个温和的紫色提示。
        BlockState center = level.getBlockState(corePos.above());
        if (center.isAir()) {
            renderHintBox(pose, buffers, 0, 1, 0, 0.62F, 0.42F, 1.0F, 0.08F, 0.34F);
            renderGhost(mc, pose, buffers,
                    WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState(),
                    0.5D, 1.56D + 0.05D * Mth.sin(time * 0.05F), 0.5D, time);
        }
    }

    /** 炼制完成的成品：放置在炉心异火正上方，缓慢旋转并轻微起伏；放大悬浮模型，让玩家一眼看到成品。 */
    private void renderFloatingReward(Minecraft mc, Level level, PoseStack pose, MultiBufferSource buffers,
                                      ItemStack stack, float time) {
        float bob = 0.06F * Mth.sin(time * 0.05F);
        pose.pushPose();
        pose.translate(0.5D, 3.05D + bob, 0.5D);
        // 放大悬浮成品模型，突出"炼成待领取"。
        pose.scale(1.7F, 1.7F, 1.7F);
        pose.mulPose(Axis.YP.rotationDegrees(time * 1.6F));
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, FULL_BRIGHT, 0,
                pose, buffers, level, 0);
        pose.popPose();
    }

    private void renderSlot(Minecraft mc, Level level, PoseStack pose, MultiBufferSource buffers,
                            BlockPos corePos, int x, int y, int z, float time) {
        BlockState actual = level.getBlockState(corePos.offset(x, y, z));
        boolean cornerSlot = y == 0 && Math.abs(x) == 1 && Math.abs(z) == 1;
        boolean correct;
        BlockState wanted;
        if (cornerSlot) {
            correct = RefiningForgeStructure.isRawCornerSlab(actual)
                    || actual.is(WuXingBlocks.REFINING_FORGE_CORNER.get());
            wanted = Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState();
        } else {
            correct = actual.is(WuXingBlocks.RUNE_STONE.get());
            wanted = WuXingBlocks.RUNE_STONE.get().defaultBlockState();
        }
        if (correct) {
            renderHintBox(pose, buffers, x, y, z, 0.15F, 0.72F, 1.0F, 0.04F, 0.22F);
            return;
        }
        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.08F + (x * 3 + z * 5 + y * 7) * 0.9F);
        renderHintBox(pose, buffers, x, y, z, 1.0F, 0.26F, 0.20F,
                0.07F + 0.10F * pulse, 0.5F + 0.3F * pulse);
        renderGhost(mc, pose, buffers, wanted, x + 0.5D,
                y + 0.5D + 0.06D * Mth.sin(time * 0.05F), z + 0.5D,
                time + (x * 7 + z * 3 + y * 5) * 13.0F);
    }

    /** 半透明填充盒加描边；坐标是相对核心的格坐标（PoseStack 已由渲染器平移到核心角点）。 */
    private void renderHintBox(PoseStack pose, MultiBufferSource buffers, int x, int y, int z,
                               float r, float g, float b, float fillAlpha, float lineAlpha) {
        DebugRenderer.renderFilledBox(pose, buffers,
                x + INSET, y + INSET, z + INSET, x + 1.0D - INSET, y + 1.0D - INSET, z + 1.0D - INSET,
                r, g, b, fillAlpha);
        LevelRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.debugLineStrip(0.0D)),
                x + INSET, y + INSET, z + INSET, x + 1.0D - INSET, y + 1.0D - INSET, z + 1.0D - INSET,
                r, g, b, lineAlpha);
    }

    /** 所需方块的全息缩影：在格心缓慢旋转并轻微上下浮动，贴图即方块本体。 */
    private void renderGhost(Minecraft mc, PoseStack pose, MultiBufferSource buffers, BlockState wanted,
                             double cx, double cy, double cz, float time) {
        pose.pushPose();
        pose.translate(cx, cy, cz);
        pose.mulPose(Axis.YP.rotationDegrees(time * 1.2F));
        pose.scale(GHOST_SCALE, GHOST_SCALE, GHOST_SCALE);
        pose.translate(-0.5D, -0.5D, -0.5D);
        mc.getBlockRenderer().renderSingleBlock(wanted, pose, buffers, FULL_BRIGHT, 0);
        pose.popPose();
    }
}
