package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.AlchemyFurnaceBlock;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceVisualLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * 炼丹炉渲染：结构未成型时显示 5x5x4 网格的结构预览——已摆对的位置显示淡蓝光罩，
 * 缺失/摆错的位置显示红色呼吸警示盒并悬浮一枚所需方块的全息缩影。
 * 成型后绘制导入的五格炉体；组成方块由不遮光的结构部件代替。
 *
 * 注意：预览几何全部使用带 PoseStack 的渲染接口（区块实体坐标系，1 单位 = 1 方块），
 * 避免在不带矩阵的 renderLineBox 重载里于世界原点画出固定红线。
 */
public final class AlchemyFurnaceBlockEntityRenderer implements BlockEntityRenderer<AlchemyFurnaceBlockEntity> {
    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;
    private static final double MAX_DISTANCE_SQR = 64.0D * 64.0D;
    private static final double INSET = 0.06D;
    private static final float GHOST_SCALE = 0.42F;

    public AlchemyFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(AlchemyFurnaceBlockEntity entity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        Minecraft mc = Minecraft.getInstance();
        if (level == null || mc.player == null) return;
        BlockPos corePos = entity.getBlockPos();
        if (mc.player.position().distanceToSqr(corePos.getX() + 0.5D, corePos.getY() + 0.5D,
                corePos.getZ() + 0.5D) > MAX_DISTANCE_SQR) return;
        BlockState state = entity.getBlockState();
        // 成型后使用完整炉体，未成型时继续显示搭建提示。
        if (AlchemyFurnaceBlock.isFormed(state)) {
            renderFormed(mc, state, pose, buffers, packedLight, packedOverlay);
            renderContents(mc,entity,partialTick,pose,buffers,packedOverlay);
            AlchemyFurnaceEffects.observe(entity);
            return;
        }
        float time = level.getGameTime() + partialTick;
        if(!CoreBuildClient.visible(corePos))return;
        renderStructurePreview(mc, level, pose, buffers, corePos, time);
    }

    /** The imported model keeps all six authored texture atlases. */
    private void renderFormed(Minecraft mc, BlockState state, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        Direction inward = AlchemyFurnaceStructure.inwardOf(state);
        pose.pushPose();
        // The supplied model is centered above the bottom-center core.
        pose.translate(0.5D, 0.0D, 0.5D);
        float rotation = AlchemyFurnaceVisualLayout.rotation(inward);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        net.minecraft.client.resources.model.BakedModel model = mc.getModelManager()
                .getModel(CauldronStructureVisuals.MODEL);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.entityCutoutNoCull(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)),
                state, model, 1.0F, 1.0F, 1.0F, light, overlay,
                net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        pose.popPose();
    }

    private void renderContents(Minecraft mc,AlchemyFurnaceBlockEntity furnace,float partial,PoseStack pose,MultiBufferSource buffers,int overlay) {
        double time=furnace.getLevel().getGameTime()+partial;
        float progress=furnace.brewingProgress();
        double convergence=progress<.7 ? 1 : progress<.95 ? 1-(progress-.7)*1.2 : .7*(1-(progress-.95)/.05);
        int count=0;for(int slot=0;slot<8;slot++)if(!furnace.getItem(slot).isEmpty())count++;
        int index=0;
        pose.pushPose();
        try {
            pose.translate(.5,0,.5);
            pose.mulPose(Axis.YP.rotationDegrees(AlchemyFurnaceVisualLayout.rotation(AlchemyFurnaceStructure.inwardOf(furnace.getBlockState()))));
            for(int slot=0;slot<8;slot++){
                var stack=furnace.getItem(slot);if(stack.isEmpty())continue;
                double radius=(count==1?0:count<=4?AlchemyFurnaceVisualLayout.SMALL_RING:AlchemyFurnaceVisualLayout.LARGE_RING)*convergence;
                double angle=time*(furnace.brewing()?.018:.008)+index++*Math.PI*2/count;
                var center=AlchemyFurnaceVisualLayout.MATERIAL_CENTER;
                pose.pushPose();
                pose.translate(center.x+Math.cos(angle)*radius,center.y+.04*Math.sin(time*.06+slot),center.z+Math.sin(angle)*radius);
                pose.mulPose(Axis.YP.rotationDegrees((float)(time*1.2+slot*45)));
                float scale=(float)(.3*(.4+.6*convergence));pose.scale(scale,scale,scale);
                mc.getItemRenderer().renderStatic(stack,net.minecraft.world.item.ItemDisplayContext.FIXED,FULL_BRIGHT,overlay,pose,buffers,furnace.getLevel(),slot);
                pose.popPose();
            }
            float age=furnace.completionAge(partial);
            if(furnace.hasResult()&&!furnace.hasAshResult()&&age>=AlchemyFurnaceVisualLayout.REVEAL_TICK){
                var result=AlchemyFurnaceVisualLayout.RESULT;
                pose.pushPose();pose.translate(result.x,result.y+.055*Math.sin(time*.06),result.z);
                pose.mulPose(Axis.YP.rotationDegrees((float)(time*1.3)));
                float size=.46F*Math.min(1,(age-AlchemyFurnaceVisualLayout.REVEAL_TICK+1)/12);pose.scale(size,size,size);
                mc.getItemRenderer().renderStatic(furnace.completedResult(),net.minecraft.world.item.ItemDisplayContext.FIXED,FULL_BRIGHT,overlay,pose,buffers,furnace.getLevel(),0);
                pose.popPose();
            }
        } finally {pose.popPose();}
    }

    @Override
    public boolean shouldRenderOffScreen(AlchemyFurnaceBlockEntity entity) { return true; }

    private void renderStructurePreview(Minecraft mc, Level level, PoseStack pose, MultiBufferSource buffers,
                                        BlockPos corePos, float time) {
        Direction inward = AlchemyFurnaceStructure.bestInward(level, corePos);
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                for (int k = 0; k <= 3; k++) {
                    BlockState wanted = AlchemyFurnaceStructure.expected(i, j, k);
                    if (wanted == null) continue; // 核心自身与内部留空处不显示
                    BlockPos cell = AlchemyFurnaceStructure.offset(corePos, inward, i, j, k);
                    int dx = cell.getX() - corePos.getX();
                    int dy = cell.getY() - corePos.getY();
                    int dz = cell.getZ() - corePos.getZ();
                    BlockState actual = level.getBlockState(cell);
                    // 达标判定与结构成型严格保持一致：金块与符文石不可互换。
                    boolean correct = AlchemyFurnaceStructure.accepts(i, j, k, actual);
                    renderSlot(mc, level, pose, buffers, dx, dy, dz, actual, wanted, correct, time);
                }
            }
        }
    }

    private void renderSlot(Minecraft mc, Level level, PoseStack pose, MultiBufferSource buffers,
                            int x, int y, int z, BlockState actual, BlockState wanted, boolean correct, float time) {
        if (correct && wanted.isAir()) return;
        if (correct) {
            renderHintBox(pose, buffers, x, y, z, 0.15F, 0.72F, 1.0F, 0.04F, 0.22F);
            return;
        }
        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.08F + (x * 3 + z * 5 + y * 7) * 0.9F);
        renderHintBox(pose, buffers, x, y, z, 1.0F, 0.26F, 0.20F,
                0.07F + 0.10F * pulse, 0.5F + 0.3F * pulse);
        if (wanted.isAir()) return;
        renderGhost(mc, pose, buffers, wanted, x + 0.5D,
                y + 0.5D + 0.06D * Mth.sin(time * 0.05F), z + 0.5D,
                time + (x * 7 + z * 3 + y * 5) * 13.0F);
    }

    /** 半透明填充盒加描边；坐标为相对核心的格坐标（PoseStack 已由渲染器平移到核心角点）。 */
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
