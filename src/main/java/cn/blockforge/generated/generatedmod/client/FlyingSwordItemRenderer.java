package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 手持与御剑实体按物品 ID 共用同一份烘焙几何。 */
public final class FlyingSwordItemRenderer extends BlockEntityWithoutLevelRenderer {
    public FlyingSwordItemRenderer(){
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());
    }
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();
        try {
            // ItemRenderer 已把中心平移至 -0.5；恢复后再应用场景姿态。
            pose.translate(.5,.5,.5);
            SwordHeldTransforms.get(stack).apply(pose, context, FlyingSwordRegistry.visual(stack).handScale());
            var client=Minecraft.getInstance();
            client.getItemRenderer().render(stack,ItemDisplayContext.NONE,false,pose,buffers,light,overlay,
                    client.getModelManager().getModel(FlyingSwordItemModels.worldModel(FlyingSwordRegistry.visual(stack))));
        } finally {pose.popPose();}
    }
}
